package br.com.fiap.restauranteapi.api.dto;

import br.com.fiap.restauranteapi.domain.UserType;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginValidationRequest(@NotBlank String login, @NotBlank String password) {
    }

    public record LoginValidationResponse(boolean valid, Long userId, String name, String login, UserType type) {
    }
}
