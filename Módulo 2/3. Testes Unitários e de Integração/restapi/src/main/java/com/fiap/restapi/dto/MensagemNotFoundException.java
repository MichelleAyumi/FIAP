package com.fiap.restapi.dto;

public class MensagemNotFoundException extends RuntimeException {

    public MensagemNotFoundException(String mensagem) {
        super(mensagem);
    }
}
