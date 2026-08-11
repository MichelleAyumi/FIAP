package com.fiap.restapi.service;

import com.fiap.restapi.helper.MensagemHelper;
import com.fiap.restapi.models.Mensagens;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MensagemServiceIT {
    @Autowired
    private MensagemService mensagemService;

    @Test
    void permitirRegistrarMensagem() {
        var mensagem = MensagemHelper.gerarMensagem();
        var mensagemRegistrada = mensagemService.registraMensagem(mensagem);

        assertThat(mensagemRegistrada.getId()).isEqualTo(mensagem.getId());
        assertThat(mensagemRegistrada.getConteudo()).isEqualTo(mensagem.getConteudo());
    }

    @Test
    void permitirObterMensagem() {
        var mensagemRegistrada = mensagemService.registraMensagem(MensagemHelper.gerarMensagem());
        var mensagemObtida = mensagemService.obterMensagem(mensagemRegistrada.getId());

        assertThat(mensagemObtida).isInstanceOf(Mensagens.class);
        assertThat(mensagemObtida.getId()).isEqualTo(mensagemRegistrada.getId());
    }
}
