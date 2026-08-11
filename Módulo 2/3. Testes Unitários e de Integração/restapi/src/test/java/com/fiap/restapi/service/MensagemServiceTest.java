package com.fiap.restapi.service;

import com.fiap.restapi.helper.MensagemHelper;
import com.fiap.restapi.repository.MensagemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MensagemServiceTest {
    @Mock
    private MensagemRepository mensagemRepository;

    private MensagemService mensagemService;
    private AutoCloseable mock;

    @BeforeEach
    void setup() {
        mock = MockitoAnnotations.openMocks(this);
        mensagemService = new MensagemServiceImp(mensagemRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mock.close();
    }

    @Test
    void registraMensagem() {
        var mensagem = MensagemHelper.gerarMensagem();
        when(mensagemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var mensagemRegistrada = mensagemService.registraMensagem(mensagem);

        assertThat(mensagemRegistrada.getId()).isEqualTo(mensagem.getId());
        assertThat(mensagemRegistrada.getConteudo()).isEqualTo(mensagem.getConteudo());
    }

    @Test
    void obterMensagemId() {
        var id = UUID.randomUUID();
        var mensagem = MensagemHelper.gerarMensagem();
        mensagem.setId(id);
        when(mensagemRepository.findById(id)).thenReturn(Optional.of(mensagem));

        var mensagemObtida = mensagemService.obterMensagem(id);

        verify(mensagemRepository, times(1)).findById(id);
        assertThat(mensagemObtida).isEqualTo(mensagem);
    }
}
