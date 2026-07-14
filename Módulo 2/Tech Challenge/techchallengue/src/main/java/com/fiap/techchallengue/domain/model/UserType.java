package com.fiap.techchallengue.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_types", uniqueConstraints = @UniqueConstraint(name = "uk_user_type_name", columnNames = "name"))
public class UserType {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    protected UserType() {

    }

    public UserType(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
