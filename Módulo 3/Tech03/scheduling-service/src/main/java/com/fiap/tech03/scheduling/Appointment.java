package com.fiap.tech03.scheduling;

public record Appointment(
    String id,
    String patientId,
    String doctorId,
    String startsAt,
    int durationMinutes,
    String reason,
    String clinicalNotes,
    long version,
    String createdAt,
    String updatedAt) {}
