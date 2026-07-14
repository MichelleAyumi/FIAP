package com.fiap.techchallengue.application.port.out;

import com.fiap.techchallengue.domain.model.Restaurant;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepositoryPort {
    Restaurant save(Restaurant restaurant);
    List<Restaurant> findAll();
    Optional<Restaurant> findById(Long id);
    int countByOwnerId(Long ownerId);
    void deleteById(Long id);
}
