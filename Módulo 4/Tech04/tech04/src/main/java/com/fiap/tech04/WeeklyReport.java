package com.fiap.tech04;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

final class WeeklyReport {
    static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    record Period(Instant start, Instant end) { }

    static Period previousWeek(Instant now) {
        LocalDate hoje = now.atZone(ZONE).toLocalDate();
        LocalDate segundaAtual = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate segundaAnterior = segundaAtual.minusWeeks(1);

        Instant inicio = segundaAnterior.atStartOfDay(ZONE).toInstant();
        Instant fim = segundaAtual.atStartOfDay(ZONE).toInstant();
        return new Period(inicio, fim);
    }

    static Map<String, Object> create(Period period, List<Feedback> feedbacks) {
        // Inclui os sete dias, mesmo sem avaliações
        Map<String, Long> quantidadePorDia = new TreeMap<>();
        LocalDate primeiroDia = period.start().atZone(ZONE).toLocalDate();
        for (int dia = 0; dia < 7; dia++) {
            String data = primeiroDia.plusDays(dia).toString();
            quantidadePorDia.put(data, 0L);
        }

        Map<String, Long> quantidadePorUrgencia = new TreeMap<>();
        quantidadePorUrgencia.put("CRITICA", 0L);
        quantidadePorUrgencia.put("ATENCAO", 0L);
        quantidadePorUrgencia.put("NORMAL", 0L);

        // Conta cada avaliação e soma as notas para calcular a média
        long somaDasNotas = 0;
        for (Feedback feedback : feedbacks) {
            Instant envio = Instant.parse(feedback.dataEnvio());
            String dia = envio.atZone(ZONE).toLocalDate().toString();
            long totalDoDia = quantidadePorDia.getOrDefault(dia, 0L);
            quantidadePorDia.put(dia, totalDoDia + 1);

            String urgencia = feedback.urgencia();
            long totalDaUrgencia = quantidadePorUrgencia.getOrDefault(urgencia, 0L);
            quantidadePorUrgencia.put(urgencia, totalDaUrgencia + 1);
            somaDasNotas += feedback.nota();
        }

        // Sem avaliações, a média continua sendo null no JSON.
        Double mediaNotas = null;
        if (!feedbacks.isEmpty()) {
            mediaNotas = (double) somaDasNotas / feedbacks.size();
        }

        Map<String, Object> relatorio = new LinkedHashMap<>();
        relatorio.put("inicioInclusivo", period.start().toString());
        relatorio.put("fimExclusivo", period.end().toString());
        relatorio.put("fusoHorario", ZONE.toString());
        relatorio.put("quantidade", feedbacks.size());
        relatorio.put("mediaNotas", mediaNotas);
        relatorio.put("quantidadePorDia", quantidadePorDia);
        relatorio.put("quantidadePorUrgencia", quantidadePorUrgencia);
        relatorio.put("avaliacoes", feedbacks);
        return relatorio;
    }
}
