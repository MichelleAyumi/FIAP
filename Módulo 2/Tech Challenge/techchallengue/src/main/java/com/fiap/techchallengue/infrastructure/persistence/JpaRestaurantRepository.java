package com.fiap.techchallengue.infrastructure.persistence;
import com.fiap.techchallengue.application.port.out.RestaurantRepositoryPort;
import com.fiap.techchallengue.domain.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JpaRestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantRepositoryPort { }
