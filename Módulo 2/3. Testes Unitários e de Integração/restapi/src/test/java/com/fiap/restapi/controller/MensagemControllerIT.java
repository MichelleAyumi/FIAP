package com.fiap.restapi.controller;

import com.fiap.restapi.helper.MensagemHelper;
import com.fiap.restapi.repository.MensagemRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MensagemControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private MensagemRepository mensagemRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void devePermitirRegistrarMensagem() throws Exception {
        var mensagemRequest = MensagemHelper.gerarMensagem();
        given()
                .contentType(ContentType.JSON)
                .body(mensagemRequest)
                .when().post("/mensagens")
                .then().statusCode(201)
                .body("$", hasKey("id"))
                .body("$", hasKey("usario"))
                .body("$", hasKey("conteudo"))
                .body("$", hasKey("gostei"))
                .body("$", hasKey("dataCriacao"))
                .body("usario", equalTo(mensagemRequest.getUsario()))
                .body("conteudo", equalTo(mensagemRequest.getConteudo()));
    }

    @Test
    void devePermitirObterMensagem() throws Exception {
        var mensagemRequest = MensagemHelper.gerarMensagem();
        var id = UUID.randomUUID();
        mensagemRequest.setId(id);
        mensagemRepository.save(mensagemRequest);

        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/mensagens/" + id))
                .GET()
                .build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"usario\":\"" + mensagemRequest.getUsario() + "\""));
        assertTrue(response.body().contains("\"conteudo\":\"" + mensagemRequest.getConteudo() + "\""));
    }
}
