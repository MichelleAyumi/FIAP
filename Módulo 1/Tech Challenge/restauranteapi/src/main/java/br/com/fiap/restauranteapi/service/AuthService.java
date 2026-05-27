package br.com.fiap.restauranteapi.service;

import br.com.fiap.restauranteapi.api.dto.AuthDtos.LoginValidationRequest;
import br.com.fiap.restauranteapi.api.dto.AuthDtos.LoginValidationResponse;
import br.com.fiap.restauranteapi.exception.BusinessException;
import br.com.fiap.restauranteapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AuthService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Transactional(readOnly = true)
    public LoginValidationResponse validate(LoginValidationRequest request) {
        return userRepository.findByLoginIgnoreCase(request.login())
                .filter(user -> passwordService.matches(request.password(), user.getPasswordHash()))
                .map(user -> new LoginValidationResponse(true, user.getId(), user.getName(), user.getLogin(), user.getType()))
                .orElseThrow(() -> new BusinessException("Login ou senha invalidos."));
    }
}
