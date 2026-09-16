package com.fiap.tech03.contracts;

import java.time.Instant;

/** Versioned integration contract. Never carries clinical notes or passwords. */
public record AppointmentEvent(
    String eventId,
    String type,
    int schemaVersion,
    String appointmentId,
    long version,
    String patientId,
    String patientEmail,
    String doctorId,
    String startsAt,
    String occurredAt) {
  public void validate() {
    if (schemaVersion != 1
        || version < 1
        || blank(eventId)
        || blank(appointmentId)
        || blank(patientId)
        || blank(doctorId)
        || blank(patientEmail)
        || !patientEmail.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
        || !("APPOINTMENT_CREATED".equals(type) || "APPOINTMENT_UPDATED".equals(type))) {
      throw new IllegalArgumentException("Invalid appointment event");
    }
    Instant.parse(startsAt);
    Instant.parse(occurredAt);
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }
}
