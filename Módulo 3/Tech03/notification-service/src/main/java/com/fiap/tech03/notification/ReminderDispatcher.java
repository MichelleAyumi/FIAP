package com.fiap.tech03.notification;

import static com.fiap.tech03.notification.NotificationInbox.ts;

import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Timestamp;
import java.time.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnProperty(name = "app.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class ReminderDispatcher {
  private static final Logger LOG = LoggerFactory.getLogger(ReminderDispatcher.class);
  private final JdbcTemplate jdbc;
  private final JavaMailSender mail;
  private final Clock clock;
  private final TransactionTemplate tx;
  private final MeterRegistry metrics;
  private final String from;

  public ReminderDispatcher(
      JdbcTemplate jdbc,
      JavaMailSender mail,
      Clock clock,
      PlatformTransactionManager manager,
      MeterRegistry metrics,
      @Value("${app.mail.from:hospital@example.test}") String from) {
    this.jdbc = jdbc;
    this.mail = mail;
    this.clock = clock;
    this.tx = new TransactionTemplate(manager);
    this.metrics = metrics;
    this.from = from;
    metrics.gauge(
        "hospital.reminders.pending",
        jdbc,
        j ->
            j.queryForObject(
                "SELECT COUNT(*) FROM notification_jobs WHERE status='PENDING'", Long.class));
    metrics.gauge(
        "hospital.reminders.failed",
        jdbc,
        j ->
            j.queryForObject(
                "SELECT COUNT(*) FROM notification_jobs WHERE status='FAILED'", Long.class));
  }

  @Scheduled(fixedDelayString = "${app.reminders.poll-ms:5000}")
  public void dispatch() {
    for (String id :
        jdbc.queryForList(
            "SELECT id FROM notification_jobs WHERE status='PENDING' AND due_at<=? ORDER BY due_at"
                + " LIMIT 50",
            String.class,
            ts(clock.instant()))) {
      tx.executeWithoutResult(
          status -> {
            // Same lock order as the inbox: an edit cannot race a reminder send.
            jdbc.queryForList("SELECT id FROM inbox_lock WHERE id=1 FOR UPDATE");
            var jobs =
                jdbc.queryForList(
                    "SELECT * FROM notification_jobs WHERE id=? AND status='PENDING' AND due_at<=?"
                        + " FOR UPDATE",
                    id,
                    ts(clock.instant()));
            if (jobs.isEmpty()) return;
            var job = jobs.getFirst();
            var snapshot =
                jdbc.queryForMap(
                    "SELECT * FROM appointment_snapshots WHERE id=?", job.get("appointment_id"));
            Object storedTime = snapshot.get("starts_at");
            Instant startsAt =
                storedTime instanceof java.time.OffsetDateTime odt
                    ? odt.toInstant()
                    : ((Timestamp) storedTime).toInstant();
            if (((Number) job.get("appointment_version")).longValue()
                    != ((Number) snapshot.get("version")).longValue()
                || !startsAt.isAfter(clock.instant())) {
              jdbc.update("UPDATE notification_jobs SET status='CANCELLED' WHERE id=?", id);
              return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo((String) snapshot.get("patient_email"));
            message.setSubject(
                "REMINDER".equals(job.get("kind"))
                    ? "Lembrete de consulta"
                    : "Consulta registrada/atualizada");
            message.setText(
                "Sua consulta esta agendada para "
                    + startsAt
                    + " (UTC). Consulte o portal para detalhes. Referencia: "
                    + job.get("appointment_id"));
            try {
              mail.send(message);
              jdbc.update(
                  "UPDATE notification_jobs SET status='SENT',sent_at=? WHERE id=?",
                  ts(clock.instant()),
                  id);
              metrics
                  .counter("hospital.reminders.sent", "kind", (String) job.get("kind"))
                  .increment();
              LOG.info("event=notification_sent jobId={} kind={}", id, job.get("kind"));
            } catch (Exception e) {
              int attempts = ((Number) job.get("attempts")).intValue() + 1;
              jdbc.update(
                  "UPDATE notification_jobs SET attempts=?,status=?,due_at=? WHERE id=?",
                  attempts,
                  attempts >= 5 ? "FAILED" : "PENDING",
                  ts(clock.instant().plusSeconds(Math.min(300, 5L << attempts))),
                  id);
              metrics.counter("hospital.reminders.errors").increment();
              LOG.warn(
                  "event=notification_retry jobId={} attempts={} cause={}",
                  id,
                  attempts,
                  e.getClass().getSimpleName());
            }
          });
    }
  }
}
