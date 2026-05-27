package br.com.fiap.restauranteapi.api;

import br.com.fiap.restauranteapi.api.dto.AuthDtos.LoginValidationRequest;
import br.com.fiap.restauranteapi.api.dto.AuthDtos.LoginValidationResponse;
import br.com.fiap.restauranteapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacao")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/validate")
    @Operation(summary = "Valida login e senha")
    public LoginValidationResponse validate(@Valid @RequestBody LoginValidationRequest request) {
        return authService.validate(request);
    }
}
