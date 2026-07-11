package com.fiap.restapi.service;

import com.fiap.restapi.dto.MensagemNotFoundException;
import com.fiap.restapi.models.Mensagens;
import com.fiap.restapi.repository.MensagemRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MensagemServiceImp implements MensagemService{

    private final MensagemRepository mensagemRepository;


    @Override
    //@RequiredArgsConstructor
    public Mensagens registraMensagem(Mensagens mensagem) {
        mensagem.setId(UUID.randomUUID());
        return mensagemRepository.save(mensagem);
    }

    @Override
    public Mensagens obterMensagem(UUID id) {
        return mensagemRepository.findById(id)
                .orElseThrow(() -> new MensagemNotFoundException("Mensagem não encontrada"));
    }

}
