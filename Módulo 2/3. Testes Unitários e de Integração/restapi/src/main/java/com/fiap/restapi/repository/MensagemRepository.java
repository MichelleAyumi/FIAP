package com.fiap.restapi.repository;

import com.fiap.restapi.models.Mensagens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MensagemRepository extends JpaRepository <Mensagens, UUID>{
}
