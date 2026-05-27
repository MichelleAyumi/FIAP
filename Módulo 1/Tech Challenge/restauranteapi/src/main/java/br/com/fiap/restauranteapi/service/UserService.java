package br.com.fiap.restauranteapi.service;

import br.com.fiap.restauranteapi.api.dto.UserDtos.ChangePasswordRequest;
import br.com.fiap.restauranteapi.api.dto.UserDtos.CreateUserRequest;
import br.com.fiap.restauranteapi.api.dto.UserDtos.UpdateUserRequest;
import br.com.fiap.restauranteapi.api.dto.UserDtos.UserResponse;
import br.com.fiap.restauranteapi.domain.User;
import br.com.fiap.restauranteapi.exception.BusinessException;
import br.com.fiap.restauranteapi.exception.DuplicateResourceException;
import br.com.fiap.restauranteapi.exception.ResourceNotFoundException;
import br.com.fiap.restauranteapi.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        ensureEmailAvailable(request.email(), null);
        ensureLoginAvailable(request.login(), null);
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setLogin(request.login());
        user.setPasswordHash(passwordService.hash(request.password()));
        user.setAddress(request.address());
        user.setType(request.type());
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(String name) {
        List<User> users = name == null || name.isBlank()
                ? userRepository.findAll()
                : userRepository.findByNameContainingIgnoreCase(name);
        return users.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUser(id));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getUser(id);
        ensureEmailAvailable(request.email(), id);
        ensureLoginAvailable(request.login(), id);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setLogin(request.login());
        user.setAddress(request.address());
        user.setType(request.type());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = getUser(id);
        if (!passwordService.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Senha atual invalida.");
        }
        user.setPasswordHash(passwordService.hash(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getUser(id);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + id));
    }

    UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getAddress(),
                user.getType(),
                user.getLastModifiedAt()
        );
    }

    private void ensureEmailAvailable(String email, Long currentUserId) {
        userRepository.findByEmailIgnoreCase(email)
                .filter(user -> !user.getId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new DuplicateResourceException("E-mail ja cadastrado.");
                });
    }

    private void ensureLoginAvailable(String login, Long currentUserId) {
        userRepository.findByLoginIgnoreCase(login)
                .filter(user -> !user.getId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new DuplicateResourceException("Login ja cadastrado.");
                });
    }
}
