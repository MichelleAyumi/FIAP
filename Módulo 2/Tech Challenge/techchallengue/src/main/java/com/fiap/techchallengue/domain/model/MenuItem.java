package com.fiap.techchallengue.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean dineInOnly;

    @Column(length = 500)
    private String photoPath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    protected MenuItem() {

    }

    public MenuItem(String name, String description, BigDecimal price,
                    boolean dineInOnly, String photoPath, Restaurant restaurant) {
        update(name, description, price, dineInOnly, photoPath, restaurant);
    }

    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public BigDecimal getPrice(){
        return price;
    }

    public boolean isDineInOnly(){
        return dineInOnly;
    }

    public String getPhotoPath(){
        return photoPath;
    }

    public Restaurant getRestaurant(){
        return restaurant;
    }

    public void update(String name, String description, BigDecimal price,
                       boolean dineInOnly, String photoPath, Restaurant restaurant) {
        this.name=name;
        this.description=description;
        this.price=price;
        this.dineInOnly=dineInOnly;
        this.photoPath=photoPath;
        this.restaurant=restaurant;
    }
}
