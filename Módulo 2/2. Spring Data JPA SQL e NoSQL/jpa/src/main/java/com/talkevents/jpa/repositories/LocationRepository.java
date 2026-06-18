package com.talkevents.jpa.repositories;

import com.talkevents.jpa.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
}
