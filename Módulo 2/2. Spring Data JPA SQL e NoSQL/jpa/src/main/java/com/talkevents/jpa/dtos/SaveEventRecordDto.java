package com.talkevents.jpa.dtos;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record SaveEventRecordDto(String name, LocalDate date, Set<UUID> attendees, SaveLocationRecordDto location) {
}
