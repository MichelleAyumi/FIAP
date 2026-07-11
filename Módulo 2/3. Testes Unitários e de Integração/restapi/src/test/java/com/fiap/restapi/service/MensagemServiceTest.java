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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MensagemServiceTest {

    @Mock
    private MensagemRepository mensagemRepository;
    private MensagemService mensagemService;

    AutoCloseable mock;

    @BeforeEach
    void setup(){
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
        when(mensagemRepository.save(any(mensagem.getClass()))).thenAnswer(i -> i.getArgument(0));
        var mensagemRegistrada  = mensagemService.registraMensagem(mensagem);

        //assert
        assertThat(mensagemRegistrada)
                .isNotNull()
                .isInstanceOf(mensagem.getClass());
        assertThat(mensagemRegistrada.getId())
                .isNotNull();

        assertThat(mensagemRegistrada.getId())
                .isEqualTo((mensagem.getUsario()));

        assertThat(mensagemRegistrada.getConteudo())
                .isEqualTo(mensagem.getConteudo());
    }

    @Test
    void obterMensagemId(){
        //Arrange
        var id = UUID.randomUUID();
        var mensagem = MensagemHelper.gerarMensagem();

        mensagem.setId(id);
        when(mensagemRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(mensagem));

        //act
        var mensagemObtida  = mensagemService.obterMensagem(id);

        //Assert
        verify(mensagemRepository, times(1)).findById(any(UUID.class));
        assertThat(mensagemObtida).isEqualTo(mensagem);
        

    }

    @Test
    void  obterMensagem(){
        fail("Logica não implementada");
    }

    @Test
    void modificaMensagem(){
        fail("Logica não implementada");
    }

    @Test
    void removerMensagem(){
        fail("Logica não implementada");
    }

}
