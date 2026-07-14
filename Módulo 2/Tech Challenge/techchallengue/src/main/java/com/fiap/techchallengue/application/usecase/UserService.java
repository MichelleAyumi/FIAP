package com.fiap.techchallengue.application.usecase;

import com.fiap.techchallengue.application.ApiDtos.UserRequest;
import com.fiap.techchallengue.application.ApiDtos.UserResponse;
import com.fiap.techchallengue.application.exception.BusinessException;
import com.fiap.techchallengue.application.exception.ResourceNotFoundException;
import com.fiap.techchallengue.application.port.out.RestaurantRepositoryPort;
import com.fiap.techchallengue.application.port.out.UserRepositoryPort;
import com.fiap.techchallengue.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    private final UserRepositoryPort users;
    private final RestaurantRepositoryPort restaurants;
    private final UserTypeService userTypeService;

    public UserService(UserRepositoryPort users, RestaurantRepositoryPort restaurants, UserTypeService userTypeService) {
        this.users = users;
        this.restaurants = restaurants;
        this.userTypeService = userTypeService;
    }

    public UserResponse create(UserRequest request) {
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, null);
        User user = new User(request.name().trim(), email, userTypeService.findEntity(request.typeId()));

        return toResponse(users.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {

        return users.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {

        return toResponse(findEntity(id));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = findEntity(id);
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, user);
        user.update(request.name().trim(), email, userTypeService.findEntity(request.typeId()));

        return toResponse(user);
    }

    public void delete(Long id) {
        findEntity(id);
        if (restaurants.countByOwnerId(id) > 0) {
            throw new BusinessException("Usuário é dono de restaurante");
        }
        users.deleteById(id);
    }

    public User findEntity(Long id) {
        return users.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
            userTypeService.toResponse(user.getType()));
    }

    private String normalizeEmail(String email) {

        return email.trim().toLowerCase();
    }

    private void ensureEmailAvailable(String email, User current) {
        boolean unchanged = current != null && current.getEmail().equalsIgnoreCase(email);

        if (!unchanged && users.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("E-mail já cadastrado");
        }
    }
}
