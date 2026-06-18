package com.talkevents.jpa.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serial;
import java.security.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class Session {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false)
    private Timestamp startTime;

    @Column(nullable = false)
    private Timestamp endTime;

    @ManyToOne
    @JoinColumn(name = "event_id" ,nullable = false)
    private Event event;

    @ManyToMany
    @JoinTable(
            name = "session_attendee",
            joinColumns = @JoinColumn(name = "sessionId"),
            inverseJoinColumns = @JoinColumn(name = "attendeeId")
    )
    private Set<Attendee> attendees = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "speakerId")
    private Speaker speaker;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Set<Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(Set<Attendee> attendees) {
        this.attendees = attendees;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }
}
