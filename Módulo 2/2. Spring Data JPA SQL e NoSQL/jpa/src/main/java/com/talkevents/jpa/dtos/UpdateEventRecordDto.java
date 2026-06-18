package com.talkevents.jpa.dtos;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateEventRecordDto(UUID id, String name, LocalDate date) {
}
