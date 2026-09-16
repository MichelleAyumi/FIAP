package com.fiap.tech03.notification;

import com.fiap.tech03.contracts.AppointmentEvent;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Timestamp;
import java.time.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationInbox {
  private final JdbcTemplate jdbc;
  private final Clock clock;
  private final long leadSeconds;
  private final MeterRegistry metrics;

  public NotificationInbox(
      JdbcTemplate jdbc,
      Clock clock,
      @Value("${app.reminders.lead-seconds:86400}") long leadSeconds,
      MeterRegistry metrics) {
    this.jdbc = jdbc;
    this.clock = clock;
    this.leadSeconds = leadSeconds;
    this.metrics = metrics;
  }

  @Transactional
  public void accept(AppointmentEvent event) {
    event.validate();

    jdbc.queryForList("SELECT id FROM inbox_lock WHERE id=1 FOR UPDATE");
    if (jdbc.queryForObject(
            "SELECT COUNT(*) FROM inbox_events WHERE id=?", Integer.class, event.eventId())
        > 0) {
      metrics.counter("hospital.notifications.duplicates").increment();
      return;
    }
    var versions =
        jdbc.queryForList(
            "SELECT version FROM appointment_snapshots WHERE id=?",
            Long.class,
            event.appointmentId());
    jdbc.update(
        "INSERT INTO inbox_events(id,received_at) VALUES (?,?)",
        event.eventId(),
        ts(clock.instant()));
    if (!versions.isEmpty() && versions.getFirst() >= event.version()) {
      metrics.counter("hospital.notifications.stale").increment();
      return;
    }
    Instant startsAt = Instant.parse(event.startsAt());
    if (versions.isEmpty())
      jdbc.update(
          "INSERT INTO appointment_snapshots(id,version,patient_email,starts_at) VALUES (?,?,?,?)",
          event.appointmentId(),
          event.version(),
          event.patientEmail(),
          ts(startsAt));
    else
      jdbc.update(
          "UPDATE appointment_snapshots SET version=?,patient_email=?,starts_at=? WHERE id=?",
          event.version(),
          event.patientEmail(),
          ts(startsAt),
          event.appointmentId());
    jdbc.update(
        "UPDATE notification_jobs SET status='CANCELLED' WHERE appointment_id=? AND"
            + " status='PENDING'",
        event.appointmentId());
    if (startsAt.isAfter(clock.instant())) {
      job(event, "CONFIRMATION", clock.instant());
      Instant due = startsAt.minusSeconds(leadSeconds);
      // Near-term booking confirmation already serves as the reminder.
      if (due.isAfter(clock.instant())) job(event, "REMINDER", due);
    }
    metrics.counter("hospital.notifications.accepted").increment();
  }

  private void job(AppointmentEvent event, String kind, Instant due) {
    jdbc.update(
        "INSERT INTO"
            + " notification_jobs(id,appointment_id,appointment_version,kind,due_at,status,attempts)"
            + " VALUES (?,?,?,?,?,'PENDING',0)",
        UUID.randomUUID().toString(),
        event.appointmentId(),
        event.version(),
        kind,
        ts(due));
  }

  static Timestamp ts(Instant instant) {
    return Timestamp.from(instant);
  }
}
