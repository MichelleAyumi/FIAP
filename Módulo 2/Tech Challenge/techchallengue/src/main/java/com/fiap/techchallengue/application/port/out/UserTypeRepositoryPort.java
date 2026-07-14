package com.fiap.techchallengue.application.port.out;

import com.fiap.techchallengue.domain.model.UserType;
import java.util.List;
import java.util.Optional;

public interface UserTypeRepositoryPort {
    UserType save(UserType type);
    List<UserType> findAll();
    Optional<UserType> findById(Long id);
    boolean existsByNameIgnoreCase(String name);
    void deleteById(Long id);
}
