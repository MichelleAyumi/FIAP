package com.talkevents.jpa.repositories;

import com.talkevents.jpa.entities.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttendeeRepository extends JpaRepository<Attendee, UUID> {
}
