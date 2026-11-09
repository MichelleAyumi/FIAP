package com.fiap.tech04;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class GerarRelatorioSemanal {
    @FunctionName("GerarRelatorioSemanal")
    public void run(
        @TimerTrigger(name = "timer", schedule = "%WEEKLY_SCHEDULE%") String timer,
        @BlobOutput(name = "relatorio", path = "relatorios/semana-{datetime:yyyy-MM-dd}.json",
            connection = "AzureWebJobsStorage", dataType = "string") OutputBinding<String> output,
        ExecutionContext context) {

        // Busca a semana anterior, monta o relatório e entrega o JSON ao Blob Storage
        WeeklyReport.Period periodo = WeeklyReport.previousWeek(Instant.now());
        FeedbackRepository repository = new TableFeedbackRepository();
        List<Feedback> feedbacks = repository.between(periodo.start(), periodo.end());
        Map<String, Object> relatorio = WeeklyReport.create(periodo, feedbacks);
        output.setValue(Json.write(relatorio));

        context.getLogger().info("Relatório semanal gerado; avaliações=" + feedbacks.size());
    }
}
