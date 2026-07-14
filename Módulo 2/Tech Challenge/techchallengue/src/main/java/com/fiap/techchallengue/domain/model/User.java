package com.fiap.techchallengue.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email"))
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 160)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_type_id", nullable = false)
    private UserType type;

    protected User() {

    }

    public User(String name, String email, UserType type) {
        this.name = name;
        this.email = email;
        this.type = type;
    }


    public Long getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserType getType() {
        return type;
    }

    public void update(String name, String email, UserType type) {
        this.name = name;
        this.email = email;
        this.type = type;
    }
}
