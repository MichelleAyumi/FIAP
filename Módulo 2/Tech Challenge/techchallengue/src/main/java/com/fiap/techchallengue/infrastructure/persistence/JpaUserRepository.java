package com.fiap.techchallengue.infrastructure.persistence;
import com.fiap.techchallengue.application.port.out.UserRepositoryPort;
import com.fiap.techchallengue.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepositoryPort { }
