package com.fiap.techchallengue.infrastructure.web;

import com.fiap.techchallengue.application.ApiDtos.MenuItemRequest;
import com.fiap.techchallengue.application.ApiDtos.MenuItemResponse;
import com.fiap.techchallengue.application.usecase.MenuItemService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {

        this.menuItemService = menuItemService;
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse createdItem = menuItemService.create(request);
        URI location = URI.create("/api/menu-items/" + createdItem.id());

        return ResponseEntity.created(location).body(createdItem);
    }

    @GetMapping
    public List<MenuItemResponse> listMenuItems(@RequestParam(required = false)Long restaurantId) {
        return menuItemService.list(restaurantId);
    }

    @GetMapping("/{id}")
    public MenuItemResponse findMenuItemById(@PathVariable Long id) {

        return menuItemService.get(id);
    }

    @PutMapping("/{id}")
    public MenuItemResponse updateMenuItem(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return menuItemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable Long id) {
        menuItemService.delete(id);
    }
}
