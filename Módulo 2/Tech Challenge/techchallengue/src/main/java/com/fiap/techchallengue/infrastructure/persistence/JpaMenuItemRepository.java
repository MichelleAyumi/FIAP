package com.fiap.techchallengue.infrastructure.persistence;
import com.fiap.techchallengue.application.port.out.MenuItemRepositoryPort;
import com.fiap.techchallengue.domain.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JpaMenuItemRepository extends JpaRepository<MenuItem, Long>, MenuItemRepositoryPort { }
