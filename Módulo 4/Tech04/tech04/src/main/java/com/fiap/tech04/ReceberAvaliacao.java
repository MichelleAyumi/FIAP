package com.fiap.tech04;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ReceberAvaliacao {
    private final Supplier<FeedbackRepository> repository;

    public ReceberAvaliacao() {
        this(TableFeedbackRepository::new);
    }

    ReceberAvaliacao(Supplier<FeedbackRepository> repository) {

        this.repository = repository;
    }

    @FunctionName("ReceberAvaliacao")
    public HttpResponseMessage run(
        @HttpTrigger(name = "request", methods = HttpMethod.POST, authLevel = AuthorizationLevel.FUNCTION,
            route = "avaliacao") HttpRequestMessage<Optional<String>> request,
        @QueueOutput(name = "alerta", queueName = "alertas-criticos",
            connection = "AzureWebJobsStorage") OutputBinding<String> alerta,
        ExecutionContext context) {

        // Lê o JSON e verifica os dados recebidos.
        Feedback feedback;
        try {
            String corpo = request.getBody().orElse("");
            feedback = lerAvaliacao(corpo);
        } catch (Exception e) {
            return response(request, HttpStatus.BAD_REQUEST,
                Map.of("erro", "Informe descricao de 1 a 2000 caracteres e nota de 0 a 10"));
        }

        // Depois de validar, salva e coloca as avaliações críticas na fila
        try {
            repository.get().save(feedback);
            if (feedback.urgencia().equals("CRITICA")) {
                alerta.setValue(Json.write(feedback));
            }
            context.getLogger().info("Avaliação registrada: " + feedback.id());
            return response(request, HttpStatus.CREATED,
                Map.of("id", feedback.id(), "urgencia", feedback.urgencia()));
        } catch (Exception e) {
            context.getLogger().severe("Falha ao registrar avaliação; tipo=" + e.getClass().getSimpleName());
            return response(request, HttpStatus.INTERNAL_SERVER_ERROR,
                Map.of("erro", "Não foi possível registrar a avaliação"));
        }
    }

    private Feedback lerAvaliacao(String corpo) throws Exception {
        if (corpo.length() > 10000) {
            throw new IllegalArgumentException("Corpo muito grande");
        }

        JsonNode dados = Json.MAPPER.readTree(corpo);
        if (dados == null || !dados.isObject()) {
            throw new IllegalArgumentException("Dados inválidos");
        }
        if (!dados.path("descricao").isTextual()) {
            throw new IllegalArgumentException("Dados inválidos");
        }
        if (!dados.path("nota").isIntegralNumber() || !dados.path("nota").canConvertToInt()) {
            throw new IllegalArgumentException("Dados inválidos");
        }

        String descricao = dados.get("descricao").textValue();
        int nota = dados.get("nota").intValue();
        return Feedback.create(descricao, nota, Instant.now());
    }

    private HttpResponseMessage response(HttpRequestMessage<?> request, HttpStatus status, Object body) {
        return request.createResponseBuilder(status)
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Cache-Control", "no-store")
            .body(Json.write(body))
            .build();
    }
}
