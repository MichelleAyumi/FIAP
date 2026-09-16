package com.fiap.tech03.scheduling;

import static com.fiap.tech03.scheduling.SchedulingService.ts;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import org.slf4j.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnProperty(name = "app.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(OutboxPublisher.class);
  private final JdbcTemplate jdbc;
  private final EventSender sender;
  private final Clock clock;
  private final TransactionTemplate tx;
  private final MeterRegistry metrics;

  public OutboxPublisher(
      JdbcTemplate jdbc,
      EventSender sender,
      Clock clock,
      PlatformTransactionManager manager,
      MeterRegistry metrics) {
    this.jdbc = jdbc;
    this.sender = sender;
    this.clock = clock;
    this.tx = new TransactionTemplate(manager);
    this.metrics = metrics;
    metrics.gauge(
        "hospital.outbox.pending",
        jdbc,
        j ->
            j.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE sent_at IS NULL", Long.class));
  }

  @Scheduled(fixedDelayString = "${app.outbox.poll-ms:3000}")
  public void publish() {
    for (String id :
        jdbc.queryForList(
            "SELECT id FROM outbox_events WHERE sent_at IS NULL AND next_attempt_at<=? ORDER BY"
                + " created_at LIMIT 50",
            String.class,
            ts(clock.instant()))) {
      tx.executeWithoutResult(
          status -> {
            var rows =
                jdbc.queryForList(
                    "SELECT payload,attempts FROM outbox_events WHERE id=? AND sent_at IS NULL AND"
                        + " next_attempt_at<=? FOR UPDATE",
                    id,
                    ts(clock.instant()));
            if (rows.isEmpty()) return;
            try {
              sender.send(id, (String) rows.getFirst().get("payload"));
              jdbc.update("UPDATE outbox_events SET sent_at=? WHERE id=?", ts(clock.instant()), id);
              metrics.counter("hospital.outbox.published").increment();
            } catch (Exception e) {
              if (e instanceof InterruptedException) Thread.currentThread().interrupt();
              int attempts = ((Number) rows.getFirst().get("attempts")).intValue() + 1;
              long delay = Math.min(300, 1L << Math.min(attempts, 8));
              jdbc.update(
                  "UPDATE outbox_events SET attempts=?,next_attempt_at=? WHERE id=?",
                  attempts,
                  ts(clock.instant().plusSeconds(delay)),
                  id);
              metrics.counter("hospital.outbox.failures").increment();
              LOG.warn(
                  "event=outbox_retry eventId={} attempts={} cause={}",
                  id,
                  attempts,
                  e.getClass().getSimpleName());
            }
          });
    }
  }
}
