package com.fiap.techchallengue.infrastructure.web;

import com.fiap.techchallengue.application.ApiDtos.UserRequest;
import com.fiap.techchallengue.application.ApiDtos.UserResponse;
import com.fiap.techchallengue.application.usecase.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Usuários e associação aos seus tipos")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse createdUser = userService.create(request);
        URI location = URI.create("/api/users/" + createdUser.id());

        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping
    @Operation(summary = "Listar usuários")
    public List<UserResponse> listUsers() {

        return userService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuário por ID")
    public UserResponse findUserById(@PathVariable Long id) {

        return userService.get(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {

        userService.delete(id);
    }
}
