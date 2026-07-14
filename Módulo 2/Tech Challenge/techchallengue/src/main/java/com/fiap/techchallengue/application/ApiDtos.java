package com.fiap.techchallengue.application;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record UserTypeRequest(@NotBlank @Size(max = 80) String name) {
    }

    public record UserTypeResponse(Long id, String name) {
    }

    public record UserRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 160) String email,
            @NotNull Long typeId) {
    }

    public record UserResponse(Long id, String name, String email, UserTypeResponse type) {
    }

    public record RestaurantRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 250) String address,
            @NotBlank @Size(max = 80) String cuisineType,
            @NotNull LocalTime openingTime,
            @NotNull LocalTime closingTime,
            @NotNull Long ownerId) {
    }

    public record RestaurantResponse(
            Long id,
            String name,
            String address,
            String cuisineType,
            LocalTime openingTime,
            LocalTime closingTime,
            UserResponse owner) {
    }

    public record MenuItemRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 500) String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            boolean dineInOnly,
            @NotBlank @Size(max = 500) String photoPath,
            @NotNull Long restaurantId) {
    }

    public record MenuItemResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            boolean dineInOnly,
            String photoPath,
            Long restaurantId) {
    }
}
