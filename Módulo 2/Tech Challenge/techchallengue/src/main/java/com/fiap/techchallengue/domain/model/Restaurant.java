package com.fiap.techchallengue.domain.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 250)
    private String address;

    @Column(nullable = false, length = 80)
    private String cuisineType;

    @Column(nullable = false)
    private LocalTime openingTime;

    @Column(nullable = false)
    private LocalTime closingTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    protected Restaurant() {

    }

    public Restaurant(String name, String address, String cuisineType,
                      LocalTime openingTime, LocalTime closingTime, User owner) {
        update(name, address, cuisineType, openingTime, closingTime, owner);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }


    public String getCuisineType() {
        return cuisineType;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public User getOwner() {
        return owner;
    }

    public void update(String name, String address, String cuisineType, LocalTime openingTime,
                       LocalTime closingTime, User owner) {

        this.name=name;
        this.address=address;
        this.cuisineType=cuisineType;
        this.openingTime=openingTime;
        this.closingTime=closingTime;
        this.owner=owner;
    }
}
