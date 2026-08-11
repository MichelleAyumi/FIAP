package com.fiap.restapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.restapi.dto.MensagemNotFoundException;
import com.fiap.restapi.handler.GlobalExceptionHandler;
import com.fiap.restapi.helper.MensagemHelper;
import com.fiap.restapi.models.Mensagens;
import com.fiap.restapi.service.MensagemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MensagemControllerTest {
    private MockMvc mockMvc;

    @Mock
    private MensagemService mensagemService;

    private AutoCloseable mock;

    @BeforeEach
    void setup() {
        mock = MockitoAnnotations.openMocks(this);
        var controller = new MensagemController(mensagemService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        mock.close();
    }

    @Test
    void permitirRegistrarMensagem() throws Exception {
        var mensagemRequest = MensagemHelper.gerarMensagem();
        when(mensagemService.registraMensagem(any(Mensagens.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/mensagens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mensagemRequest)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(mensagemService, times(1)).registraMensagem(any(Mensagens.class));
    }

    @Test
    void permitirObterMensagem() throws Exception {
        var id = UUID.randomUUID();
        var mensagemResponse = MensagemHelper.gerarMensagem();
        mensagemResponse.setId(id);
        mensagemResponse.setDataCriacao(LocalDateTime.now());
        mensagemResponse.setDataAtualizacao(LocalDateTime.now());

        when(mensagemService.obterMensagem(any(UUID.class))).thenReturn(mensagemResponse);

        mockMvc.perform(get("/mensagens/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(mensagemService, times(1)).obterMensagem (any(UUID.class));

    }

    @Test
    void gerarExcecaoAoObterMensagemComIdNaoExistente() throws Exception {
        var id = UUID.randomUUID();
        var mensagemResponse = MensagemHelper.gerarMensagem();
        mensagemResponse.setId(id);
        mensagemResponse.setDataCriacao(LocalDateTime.now());
        mensagemResponse.setDataAtualizacao(LocalDateTime.now());

        when(mensagemService.obterMensagem(any(UUID.class))).thenThrow(new MensagemNotFoundException("Mensagem não encontrada"));

        mockMvc.perform(get("/mensagens/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(mensagemService, times(1)).obterMensagem (any(UUID.class));

    }
}
