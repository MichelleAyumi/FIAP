package com.fiap.techchallengue.application.usecase;

import com.fiap.techchallengue.application.ApiDtos.MenuItemRequest;
import com.fiap.techchallengue.application.ApiDtos.MenuItemResponse;
import com.fiap.techchallengue.application.exception.ResourceNotFoundException;
import com.fiap.techchallengue.application.port.out.MenuItemRepositoryPort;
import com.fiap.techchallengue.domain.model.MenuItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MenuItemService {
    private final MenuItemRepositoryPort menuItems;
    private final RestaurantService restaurantService;

    public MenuItemService(MenuItemRepositoryPort menuItems, RestaurantService restaurantService) {
        this.menuItems = menuItems;
        this.restaurantService = restaurantService;
    }

    public MenuItemResponse create(MenuItemRequest request) {
        MenuItem item = new MenuItem(request.name().trim(), request.description().trim(), request.price(),
            request.dineInOnly(), request.photoPath().trim(), restaurantService.findEntity(request.restaurantId()));
        return toResponse(menuItems.save(item));
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> list(Long restaurantId) {
        if (restaurantId == null) {
            return menuItems.findAll().stream().map(this::toResponse).toList();
        }

        restaurantService.findEntity(restaurantId);
        return menuItems.findAllByRestaurantId(restaurantId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse get(Long id) {

        return toResponse(findEntity(id));
    }

    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = findEntity(id);
        item.update(request.name().trim(), request.description().trim(), request.price(), request.dineInOnly(),
            request.photoPath().trim(), restaurantService.findEntity(request.restaurantId()));

        return toResponse(item);
    }

    public void delete(Long id) {

        menuItems.delete(findEntity(id));
    }

    private MenuItem findEntity(Long id) {
        return menuItems.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item de cardápio não encontrado"));
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getName(), item.getDescription(), item.getPrice(),
            item.isDineInOnly(), item.getPhotoPath(), item.getRestaurant().getId());
    }
}
