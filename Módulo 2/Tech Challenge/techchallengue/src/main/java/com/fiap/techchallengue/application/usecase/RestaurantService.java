package com.fiap.techchallengue.application.usecase;

import com.fiap.techchallengue.application.ApiDtos.RestaurantRequest;
import com.fiap.techchallengue.application.ApiDtos.RestaurantResponse;
import com.fiap.techchallengue.application.exception.BusinessException;
import com.fiap.techchallengue.application.exception.ResourceNotFoundException;
import com.fiap.techchallengue.application.port.out.MenuItemRepositoryPort;
import com.fiap.techchallengue.application.port.out.RestaurantRepositoryPort;
import com.fiap.techchallengue.domain.model.Restaurant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RestaurantService {
    private final RestaurantRepositoryPort restaurants;
    private final MenuItemRepositoryPort menuItems;
    private final UserService userService;

    public RestaurantService(RestaurantRepositoryPort restaurants, MenuItemRepositoryPort menuItems, UserService userService) {
        this.restaurants = restaurants;
        this.menuItems = menuItems;
        this.userService = userService;
    }

    public RestaurantResponse create(RestaurantRequest request) {
        validateHours(request);
        Restaurant restaurant = new Restaurant(request.name().trim(), request.address().trim(),
            request.cuisineType().trim(), request.openingTime(), request.closingTime(),
            userService.findEntity(request.ownerId()));

        return toResponse(restaurants.save(restaurant));
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> list() {

        return restaurants.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse get(Long id) {

        return toResponse(findEntity(id));
    }

    public RestaurantResponse update(Long id, RestaurantRequest request) {
        validateHours(request);
        Restaurant restaurant = findEntity(id);
        restaurant.update(request.name().trim(), request.address().trim(), request.cuisineType().trim(),
            request.openingTime(), request.closingTime(), userService.findEntity(request.ownerId()));
        
        return toResponse(restaurant);
    }

    public void delete(Long id) {
        findEntity(id);
        if (menuItems.countByRestaurantId(id) > 0) {
            throw new BusinessException("Restaurante possui itens no cardápio");
        }
        restaurants.deleteById(id);
    }

    public Restaurant findEntity(Long id) {
        return restaurants.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getAddress(),
            restaurant.getCuisineType(), restaurant.getOpeningTime(), restaurant.getClosingTime(),
            userService.toResponse(restaurant.getOwner()));
    }

    private void validateHours(RestaurantRequest request) {
        if (request.openingTime().equals(request.closingTime())) {
            throw new BusinessException("Horários de abertura e fechamento devem ser diferentes");
        }
    }
}
