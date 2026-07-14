package com.fiap.techchallengue.application.port.out;

import com.fiap.techchallengue.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    List<User> findAll();
    Optional<User> findById(Long id);
    boolean existsByEmailIgnoreCase(String email);
    int countByTypeId(Long typeId);
    void deleteById(Long id);
}
