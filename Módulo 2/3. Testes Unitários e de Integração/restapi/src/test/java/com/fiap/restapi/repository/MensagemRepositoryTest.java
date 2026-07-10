package com.fiap.restapi.repository;

import com.fiap.restapi.models.Mensagens;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MensagemRepositoryTest {

        @Mock
        private MensagemRepository mensagemRepository;

        AutoCloseable mock;

        @BeforeEach
        void setup(){
                mock = MockitoAnnotations.openMocks(this);
        }

        @AfterEach
        void tearDown() throws Exception {
                mock.close();
        }


        @Test
        //Deve permitir registrar mensagens
        void registraMensagem(){
                //Arrange - preparar
                var mensagem = gerarMensagem();
                when(mensagemRepository.save(any(Mensagens.class))).thenReturn(mensagem);

                //Act - Atuar
                var mensagemArmazena = mensagemRepository.save(mensagem);

                //Assert - validar
                assertThat(mensagemArmazena).isSameAs(mensagem);
                verify(mensagemRepository, times(1)).save(mensagem);

        }

        @Test
        void consultarMensagem(){
                var id = UUID.randomUUID();
                var mensagem = gerarMensagem();
                mensagem.setId(id);

                when(mensagemRepository.findById(any(UUID.class))).thenReturn(Optional.of(mensagem));

                //Action - acao
                var mensagemEncontrada = mensagemRepository.findById(id);

                //Assert
                assertThat(mensagemEncontrada)
                        .isNotNull()
                        .containsSame(mensagem);
        }

        @Test
        void apagarMensagem(){
                var id = UUID.randomUUID();
                var mensagem = gerarMensagem();
                mensagem.setId(id);

                doNothing().when(mensagemRepository).deleteById(any(UUID.class));

                //Action - acao
                mensagemRepository.deleteById(id);
                //Assert
                verify(mensagemRepository, times(1)).deleteById(id);    
        }


        private Mensagens gerarMensagem(){
                var mensagem = new Mensagens();
                mensagem.setUsario("fiap");
                mensagem.setConteudo("Texto");
                return mensagem;
        }
}
