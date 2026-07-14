package com.fiap.techchallengue.infrastructure.web;

import com.fiap.techchallengue.application.ApiDtos.UserTypeRequest;
import com.fiap.techchallengue.application.ApiDtos.UserTypeResponse;
import com.fiap.techchallengue.application.usecase.UserTypeService;
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
@RequestMapping("/api/user-types")
@Tag(name = "Tipos de usuário", description = "Gerenciamento dos tipos associados aos usuários")
public class UserTypeController {

    private final UserTypeService userTypeService;

    public UserTypeController(UserTypeService userTypeService) {

        this.userTypeService = userTypeService;
    }

    @PostMapping
    public ResponseEntity<UserTypeResponse> createUserType(@Valid @RequestBody UserTypeRequest request) {
        UserTypeResponse createdUserType = userTypeService.create(request);
        URI location = URI.create("/api/user-types/" + createdUserType.id());

        return ResponseEntity.created(location).body(createdUserType);
    }

    @GetMapping
    @Operation(summary = "Listar tipos de usuário")
    public List<UserTypeResponse> listUserTypes() {

        return userTypeService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar tipo de usuário por ID")
    public UserTypeResponse findUserTypeById(@PathVariable Long id) {

        return userTypeService.get(id);
    }

    @PutMapping("/{id}")
    public UserTypeResponse updateUserType(@PathVariable Long id, @Valid @RequestBody UserTypeRequest request) {
        return userTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserType(@PathVariable Long id) {
        userTypeService.delete(id);
    }
}
