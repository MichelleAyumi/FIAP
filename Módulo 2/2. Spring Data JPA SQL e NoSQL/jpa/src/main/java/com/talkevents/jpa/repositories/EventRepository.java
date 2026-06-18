package com.talkevents.jpa.repositories;

import com.talkevents.jpa.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

//Sistema de busca para log/documentacao
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Event findByName(String name);

    @Query(value = "SELECT * FROM event WHERE date = :date", nativeQuery = true)
    List<Event> findEventByDate(@Param("date") String date);
}