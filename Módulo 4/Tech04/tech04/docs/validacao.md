## Local

- Java 21; `mvn verify`: BUILD SUCCESS.
- 6 testes JUnit, zero falhas e zero erros.
- Maven gerou e validou as configurações de três Azure Functions.
- Arquivo principal Bicep compilado pela Azure CLI.
- Scripts PowerShell passaram na análise de sintaxe.

## Azure

- Assinatura Azure for Students, grupo `rg-tech04-academico`, região France Central permitida pela política da assinatura.
- Function App: `func-tech04-gq2ei2at4mkgk`.
- Armazenamento: `sttech04gq2ei2at4mkgk`.
- Application Insights: `appi-tech04-gq2ei2at4mkgk`.
- Funções reconhecidas no runtime: ReceberAvaliacao, EnviarAlerta e GerarRelatorioSemanal.
- POST sem chave: HTTP 401.
- POST autenticado com corpo inválido: HTTP 400.
- POST autenticado com avaliação nota 9: HTTP 201, urgência NORMAL. ID: `503c7e3c-bd69-40eb-b834-c2dffbbc804b`.
- Execução administrativa manual de GerarRelatorioSemanal: aceita com HTTP 202.
- Blob observado no container privado `relatorios`: `semana-2026-11-02.json`, 350 bytes. O nome usa data UTC da execução, que já era 02/11; o período do conteúdo usa semana no fuso de São Paulo.
- JSON baixado com autenticação e validado: fuso America/Sao_Paulo, sete dias, três urgências e quantidade zero (sem avaliações naquela semana).
- Restrição de rede efetiva: ação padrão Deny e SCM usando as mesmas regras. Um IPv4 autorizado na infraestrutura.
- Relatório preenchido: 12 registros históricos fictícios, com IDs demo-20261019-01 a demo-20261019-12, inseridos administrativamente sem disparar e-mails. Função semanal executada sem mudar o timer. JSON baixado do container privado e conferido: período 19 a 25/10/2026, soma de notas 72, média 6,0; CRITICA 3, ATENCAO 3, NORMAL 6; contagens diárias 2, 1, 3, 2, 2, 2, 0.

