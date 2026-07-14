package com.fiap.techchallengue.infrastructure.persistence;
import com.fiap.techchallengue.application.port.out.UserTypeRepositoryPort;
import com.fiap.techchallengue.domain.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JpaUserTypeRepository extends JpaRepository<UserType, Long>, UserTypeRepositoryPort { }
