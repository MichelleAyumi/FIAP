package com.fiap.restapi.service;

import com.fiap.restapi.models.Mensagens;

import java.util.UUID;

public interface MensagemService {
    Mensagens registraMensagem (Mensagens mensagem);
    Mensagens obterMensagem(UUID id);
}
