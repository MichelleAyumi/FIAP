package com.fiap.restapi.controller;

import com.fiap.restapi.dto.MensagemNotFoundException;
import com.fiap.restapi.service.MensagemService;
import com.fiap.restapi.models.Mensagens;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/mensagens")
public class MensagemController {
    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Mensagens> registrarMensagem(@Valid @RequestBody Mensagens mensagem){
        var mensagemRegistrada = mensagemService.registraMensagem(mensagem);
        return new ResponseEntity<>(mensagemRegistrada, HttpStatus.CREATED);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Mensagens> buscarMensagens(@PathVariable String id){
        try{
            var mensagemId = UUID.fromString(id);
            var mensagemEncontrada = mensagemService.obterMensagem(mensagemId);
            return new ResponseEntity<>(mensagemEncontrada, HttpStatus.OK);
        }catch (MensagemNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
}
