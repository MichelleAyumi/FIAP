Feedback de aulas

## Arquitetura

`POST /api/avaliacao → ReceberAvaliacao → Azure Table Storage`

`avaliação crítica → Queue Storage → EnviarAlerta → Azure Communication Services Email`

`timer semanal → GerarRelatorioSemanal → relatório JSON em Blob Storage privado`

As três funções têm responsabilidades separadas. O plano executa sob demanda; o Application Insights registra execuções e falhas. O armazenamento é Standard LRS. A infraestrutura atribui à identidade gerenciada apenas os papéis de dados de tabelas, blobs e filas na conta do projeto.

## Regras

- Descrição: 1 a 2000 caracteres. Nota: inteiro de 0 a 10.
- Notas 0–3: CRITICA; 4–6: ATENCAO; 7–10: NORMAL.
- Relatório: segunda-feira às 12:00 UTC (09:00 de São Paulo). Inclui média, descrição, urgência, data e contagens por dia/urgência. Semana vazia tem média null e contagens zero.
- Endpoint canônico sem acento: `/api/avaliacao`, equivalente à referênxscia de entrada do enunciado.

## Segurança e privacidade

Não há acesso anônimo ao endpoint em cloud: é necessária chave de função no cabeçalho `x-functions-key` e origem no IPv4 autorizado pelo deploy. Outros IPs são negados. O endereço DNS continua existindo, mas não permite acesso de origens não autorizadas. Isso não equivale a uma VPN.

## Testes e pacote

```powershell
mvn clean verify
```

O pacote pronto para Azure fica em `target/azure-functions/tech04-local`. Testes cobrem validação, classificação, falha de persistência, roteamento de alertas, limites semanais e agregações. Não substituem a validação em cloud.

## Configuração do e-mail

O script abaixo provisiona o serviço com domínio gerenciado Azure e configura os segredos na Function App sem exibi-los. Não envia uma mensagem de teste; habilita o processamento dos alertas pendentes.

```powershell
./scripts/configure-email.ps1 -AdminEmail 'EMAIL_DO_ADMINISTRADOR'
```

Alternativa manual:

Criar Email Communication Service com domínio gerenciado Azure, conectar esse domínio a um Azure Communication Services e obter o remetente verificado. Configurar na Function App `EMAIL_CONNECTION_STRING`, `EMAIL_SENDER`, `ADMIN_EMAIL` usando o portal, sem colocar os valores em arquivos versionados ou no chat. Só então alterar `AzureWebJobs.EnviarAlerta.Disabled` para `false`.

Até essa configuração, avaliações críticas permanecem na fila e não são tratadas como enviadas. O sucesso retornado pelo serviço confirma aceitação/processamento, não leitura do destinatário. Conferir caixa de entrada e spam na demonstração.

## Monitoramento e limites conhecidos

Logs registram IDs e contagens, sem descrição e destinatário. Consultas em `docs/monitoramento.kql`. Após cinco tentativas de falha da função de e-mail, mensagens vão para `alertas-criticos-poison`: verificar e reenfileirar após corrigir a causa. O timer não tem o mesmo mecanismo de retry das filas; conferir falhas e executar manualmente após correção.

Persistir avaliação e publicar na fila não é uma transação única: uma falha no output binding pode deixar uma avaliação salva sem alerta. As filas têm entrega pelo menos uma vez: um retry pode reenviar e-mail. O cliente também pode duplicar avaliações ao repetir POST. Para este trabalho os limites ficam documentados; para garantia mais forte seria necessário outbox/idempotência. Relatórios grandes são carregados em memória e o escopo é o volume acadêmico.

Status: implantado na assinatura Azure for Students, grupo `rg-tech04-academico`, região `francecentral`. A API foi validada com respostas 401/400/201.


