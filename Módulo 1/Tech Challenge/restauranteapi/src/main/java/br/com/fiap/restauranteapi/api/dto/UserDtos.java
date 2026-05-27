package br.com.fiap.restauranteapi.api.dto;

import br.com.fiap.restauranteapi.domain.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateUserRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank String login,
            @NotBlank @Size(min = 6) String password,
            @NotBlank String address,
            @NotNull UserType type
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank String login,
            @NotBlank String address,
            @NotNull UserType type
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 6) String newPassword
    ) {
    }

    public record UserResponse(
            Long id,
            String name,
            String email,
            String login,
            String address,
            UserType type,
            OffsetDateTime lastModifiedAt
    ) {
    }
}
