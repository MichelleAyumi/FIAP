package br.com.fiap.restauranteapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.fiap.restauranteapi.api.dto.UserDtos.ChangePasswordRequest;
import br.com.fiap.restauranteapi.api.dto.UserDtos.CreateUserRequest;
import br.com.fiap.restauranteapi.domain.User;
import br.com.fiap.restauranteapi.domain.UserType;
import br.com.fiap.restauranteapi.exception.BusinessException;
import br.com.fiap.restauranteapi.exception.DuplicateResourceException;
import br.com.fiap.restauranteapi.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordService passwordService = new PasswordService();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordService);
    }

    @Test
    void createShouldHashPasswordAndNotExposeIt() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByLoginIgnoreCase("ana")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });

        var response = userService.create(new CreateUserRequest(
                "Ana", "ana@example.com", "ana", "123456", "Rua A", UserType.CLIENTE
        ));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("ana@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createShouldRejectDuplicatedEmail() {
        User existing = new User();
        ReflectionTestUtils.setField(existing, "id", 1L);
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.create(new CreateUserRequest(
                "Ana", "ana@example.com", "ana", "123456", "Rua A", UserType.CLIENTE
        ))).isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordShouldRequireCurrentPassword() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setPasswordHash(passwordService.hash("123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword(1L, new ChangePasswordRequest("errada", "654321")))
                .isInstanceOf(BusinessException.class);
    }
}
