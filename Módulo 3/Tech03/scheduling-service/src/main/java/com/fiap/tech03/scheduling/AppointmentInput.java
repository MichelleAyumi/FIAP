package com.fiap.tech03.scheduling;

public record AppointmentInput(
    String patientId, String doctorId, String startsAt, int durationMinutes, String reason) {

}
