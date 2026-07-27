package com.fiap.restapi.service;

import com.fiap.restapi.helper.MensagemHelper;
import com.fiap.restapi.models.Mensagens;
import com.fiap.restapi.repository.MensagemRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static jdk.dynalink.linker.support.Guards.isNotNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class MensagemServiceIT {

    @Autowired
    private MensagemRepository mensagemRepository;
    @Autowired
    private MensagemService mensagemService;

    @Test
    void permitirRegistrarMensagem(){
        //arrange
        var mensagem = MensagemHelper.gerarMensagem();
        //act
        var mensagemRegistrada = mensagemService.registraMensagem(mensagem);

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
    };

    void permitirObterMensagem(){
        var mensagem = MensagemHelper.gerarMensagem();
        var mensagemRegistrada = mensagemService.obterMensagem(mensagem);
        var mensagemObtida = mensagemService.obterMensagem(mensagemRegistrada.getId());
        //assert
        assertThat(mensagemObtida)
                .isNotNull()
                .isInstanceOf(Mensagem.class);

        assertThat(mensagemObtida.getId())
                .isNotNull();

        assertThat(mensagemObtida.getId())
                .isNotNull();

        assertThat(mensagemObtida.getConteudo())
                .isNotNull();
    };

    @Test
    void permitirRemoverMensagem(){
        //arrange
        var mensagem = MensagemHelper.gerarMensagem();
        var mensagemRegistrada = mensagemService.registraMensagem(mensagem);
        //act
        var mensagemRemovida = mensagemService.removerMensagem(mensagemRegistrada.getId());

        //assert
        assertThat(mensagemRemovida).isTrue();


    }

}
