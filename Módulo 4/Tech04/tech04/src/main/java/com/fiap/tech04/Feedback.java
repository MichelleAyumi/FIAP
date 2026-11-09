package com.fiap.tech04;

import java.time.Instant;
import java.util.UUID;

public record Feedback(String id, String descricao, int nota, String urgencia, String dataEnvio) {
    public static Feedback create(String descricao, Integer nota, Instant now) {
        if (descricao == null || descricao.isBlank() || descricao.length() > 2000) {
            throw new IllegalArgumentException("descricao deve conter de 1 a 2000 caracteres");
        }
        if (nota == null || nota < 0 || nota > 10) {
            throw new IllegalArgumentException("nota deve ser um inteiro de 0 a 10");
        }

        String urgencia;
        if (nota <= 3) {
            urgencia = "CRITICA";
        } else if (nota <= 6) {
            urgencia = "ATENCAO";
        } else {
            urgencia = "NORMAL";
        }

        String id = UUID.randomUUID().toString();
        String descricaoSemEspacos = descricao.strip();
        return new Feedback(id, descricaoSemEspacos, nota, urgencia, now.toString());
    }
}
