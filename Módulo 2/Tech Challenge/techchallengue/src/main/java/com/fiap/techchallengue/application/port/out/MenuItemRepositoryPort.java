package com.fiap.techchallengue.application.port.out;

import com.fiap.techchallengue.domain.model.MenuItem;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepositoryPort {
    MenuItem save(MenuItem item);
    List<MenuItem> findAll();
    List<MenuItem> findAllByRestaurantId(Long restaurantId);
    Optional<MenuItem> findById(Long id);
    int countByRestaurantId(Long restaurantId);
    void delete(MenuItem item);
}
