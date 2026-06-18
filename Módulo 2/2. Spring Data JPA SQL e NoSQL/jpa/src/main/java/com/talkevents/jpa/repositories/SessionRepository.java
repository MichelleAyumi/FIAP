package com.talkevents.jpa.repositories;

import com.talkevents.jpa.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
}
