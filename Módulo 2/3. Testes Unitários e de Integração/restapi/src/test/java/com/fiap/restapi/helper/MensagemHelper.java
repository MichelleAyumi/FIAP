package com.fiap.restapi.helper;

import com.fiap.restapi.models.Mensagens;
import com.fiap.restapi.service.MensagemService;

public abstract class MensagemHelper {

    public static Mensagens gerarMensagem(){
        var mensagem = new Mensagens();
        mensagem.setUsario("fiap");
        mensagem.setConteudo("Texto");
        return mensagem;
    }
}
