package com.fiap.techchallengue.infrastructure.web;

import com.fiap.techchallengue.application.ApiDtos.RestaurantRequest;
import com.fiap.techchallengue.application.ApiDtos.RestaurantResponse;
import com.fiap.techchallengue.application.usecase.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {

        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse createdRestaurant = restaurantService.create(request);
        URI location = URI.create("/api/restaurants/" + createdRestaurant.id());

        return ResponseEntity.created(location).body(createdRestaurant);
    }

    @GetMapping
    public List<RestaurantResponse> listRestaurants() {

        return restaurantService.list();
    }

    @GetMapping("/{id}")
    public RestaurantResponse findRestaurantById(@PathVariable Long id) {

        return restaurantService.get(id);
    }

    @PutMapping("/{id}")
    public RestaurantResponse updateRestaurant(@PathVariable Long id, @Valid @RequestBody RestaurantRequest request) {
        return restaurantService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRestaurant(@PathVariable Long id) {

        restaurantService.delete(id);
    }
}
