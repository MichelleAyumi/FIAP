package br.com.fiap.restauranteapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.fiap.restauranteapi.api.dto.AuthDtos.LoginValidationRequest;
import br.com.fiap.restauranteapi.domain.User;
import br.com.fiap.restauranteapi.domain.UserType;
import br.com.fiap.restauranteapi.exception.BusinessException;
import br.com.fiap.restauranteapi.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordService passwordService = new PasswordService();

    @Test
    void validateShouldAcceptCorrectPassword() {
        AuthService authService = new AuthService(userRepository, passwordService);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setName("Joao");
        user.setLogin("joao");
        user.setType(UserType.CLIENTE);
        user.setPasswordHash(passwordService.hash("123456"));
        when(userRepository.findByLoginIgnoreCase("joao")).thenReturn(Optional.of(user));

        var response = authService.validate(new LoginValidationRequest("joao", "123456"));

        assertThat(response.valid()).isTrue();
        assertThat(response.userId()).isEqualTo(1L);
    }

    @Test
    void validateShouldRejectWrongPassword() {
        AuthService authService = new AuthService(userRepository, passwordService);
        User user = new User();
        user.setPasswordHash(passwordService.hash("123456"));
        when(userRepository.findByLoginIgnoreCase("joao")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.validate(new LoginValidationRequest("joao", "errada")))
                .isInstanceOf(BusinessException.class);
    }
}
