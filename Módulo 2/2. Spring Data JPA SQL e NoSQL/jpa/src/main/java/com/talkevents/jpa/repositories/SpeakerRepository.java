package com.talkevents.jpa.repositories;

import com.talkevents.jpa.entities.Speaker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpeakerRepository extends JpaRepository<Speaker, UUID> {
}
