package com.fiap.tech04;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

final class TableFeedbackRepository implements FeedbackRepository {
    private final TableClient table;

    TableFeedbackRepository() {
        TableClientBuilder builder = new TableClientBuilder().tableName("avaliacoes");
        String conexaoLocal = System.getenv("DATA_STORAGE_CONNECTION");

        if (conexaoLocal != null && !conexaoLocal.isBlank()) {
            builder.connectionString(conexaoLocal);
        } else {
            String conta = System.getenv("DATA_STORAGE_ACCOUNT");
            builder.endpoint("https://" + conta + ".table.core.windows.net")
                .credential(new DefaultAzureCredentialBuilder().build());
        }
        table = builder.buildClient();
    }

    public void save(Feedback feedback) {
        // Agrupa as avaliações pela data
        String dia = feedback.dataEnvio().substring(0, 10);
        TableEntity registro = new TableEntity(dia, feedback.id());
        registro.addProperty("descricao", feedback.descricao());
        registro.addProperty("nota", feedback.nota());
        registro.addProperty("urgencia", feedback.urgencia());
        registro.addProperty("dataEnvio", feedback.dataEnvio());
        table.createEntity(registro);
    }

    public List<Feedback> between(Instant start, Instant end) {
        List<Feedback> avaliacoes = new ArrayList<>();
        String diaInicial = start.toString().substring(0, 10);
        String diaFinal = end.toString().substring(0, 10);
        String filtro = "PartitionKey ge '" + diaInicial + "' and PartitionKey le '" + diaFinal + "'";
        ListEntitiesOptions opcoes = new ListEntitiesOptions().setFilter(filtro);

        for (TableEntity registro : table.listEntities(opcoes, null, null)) {
            String dataEnvio = (String) registro.getProperty("dataEnvio");
            Instant instanteEnvio = Instant.parse(dataEnvio);

            // Inclui o início do período, mas não o instante final
            if (!instanteEnvio.isBefore(start) && instanteEnvio.isBefore(end)) {
                String id = registro.getRowKey();
                String descricao = (String) registro.getProperty("descricao");
                int nota = ((Number) registro.getProperty("nota")).intValue();
                String urgencia = (String) registro.getProperty("urgencia");
                Feedback feedback = new Feedback(id, descricao, nota, urgencia, dataEnvio);
                avaliacoes.add(feedback);
            }
        }
        return avaliacoes;
    }
}
