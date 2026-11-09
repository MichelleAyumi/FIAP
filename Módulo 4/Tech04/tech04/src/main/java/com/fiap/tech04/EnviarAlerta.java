package com.fiap.tech04;

import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendStatus;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.*;

public class EnviarAlerta {
    @FunctionName("EnviarAlerta")
    public void run(
        @QueueTrigger(name = "mensagem", queueName = "alertas-criticos",
            connection = "AzureWebJobsStorage") String mensagem,
        ExecutionContext context) throws Exception {

        Feedback feedback = Json.MAPPER.readValue(mensagem, Feedback.class);
        var client = new EmailClientBuilder()
            .connectionString(required("EMAIL_CONNECTION_STRING"))
            .buildClient();

        String assunto = "[Tech04] Avaliação crítica " + feedback.id();
        String texto = "Descrição: " + feedback.descricao()
            + "\nUrgência: " + feedback.urgencia()
            + "\nData de envio (UTC): " + feedback.dataEnvio()
            + "\nNota: " + feedback.nota();

        EmailMessage email = new EmailMessage()
            .setSenderAddress(required("EMAIL_SENDER"))
            .setToRecipients(required("ADMIN_EMAIL"))
            .setSubject(assunto)
            .setBodyPlainText(texto);

        var resultado = client.beginSend(email).waitForCompletion();
        if (!EmailSendStatus.SUCCEEDED.equals(resultado.getValue().getStatus())) {
            throw new IllegalStateException("Serviço de e-mail não confirmou o envio");
        }
        context.getLogger().info("Alerta enviado para avaliação " + feedback.id());
    }

    // Interrompe o envio se uma configuração obrigatória estiver ausente
    private String required(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configuração ausente: " + key);
        }
        return value;
    }
}
