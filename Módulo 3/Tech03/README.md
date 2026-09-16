# Tech03 — Agendamento hospitalar

Backend do Tech Challenge da Fase 3: consultas e histórico por GraphQL, autenticação HTTP Basic com Spring Security, autorização por perfil e lembretes por RabbitMQ.

| Recurso | Endereço | Credencial local |
|---|---|---|
| API GraphQL | `http://localhost:8080/graphql` | Perfis abaixo |
| Identidade autenticada | `http://localhost:8080/api/me` | Perfis abaixo |
| Saúde do agendamento | `http://localhost:8080/actuator/health` | Pública, sem detalhes |
| Saúde das notificações | `http://localhost:8081/actuator/health` | Pública, sem detalhes |
| Caixa de emails Mailpit | `http://localhost:8025` | Sem login; somente local |
| Gerenciamento RabbitMQ | `http://localhost:15672` | `tech03` / `Tech03RabbitLocal!` |
| Métricas | `http://localhost:8080/actuator/prometheus` | `monitor` / `Monitor123!` |


Grafana: `http://localhost:3000`, `admin` / `Grafana123!`, dashboard Tech03 - Hospital. Prometheus: `http://localhost:9090`, incluindo regras na tela `/alerts`. As regras locais não têm um destino externo de notificação configurado. 

## Contas e permissões

| Usuário | Senha demo        | Perfil | Identidade |
|---|-------------------|---|---|
| `medico` | `medico1234`      | DOCTOR | `doc-001` |
| `enfermeiro` | `enfermeiro1234!` | NURSE | — |
| `paciente` | `paciente1234`    | PATIENT | `pac-001` |
| `paciente2` | `Paciente456`     | PATIENT | `pac-002` |
| `monitor` | `monitor1234`        | MONITOR | Apenas métricas |

Médicos e enfermeiros criam e reagendam consultas e leem o histórico. Apenas médicos alteram `clinicalNotes`. Pacientes leem somente suas próprias consultas, inclusive por busca individual de ID. A propriedade é verificada no serviço, além da autenticação HTTP. O perfil técnico MONITOR não tem acesso à API clínica.

O enunciado exige autenticação, mas não cadastro de usuários. As contas demo são carregadas exclusivamente pelo perfil . 


Datas devem incluir fuso e estar no futuro para criar/reagendar. O exemplo usa UTC. Duração: 10 a 240 minutos. São bloqueadas sobreposições de horários do médico ou do paciente, inclusive com requisições concorrentes. Paciente e médico de uma consulta existente não podem ser substituídos.


Use sempre a `version` retornada pela consulta mais recente como `expectedVersion`. Alterações concorrentes com versão antiga falham sem sobrescrever dados. O histórico é formado pelas consultas e pelo registro clínico atual de cada uma.

Sem credenciais ou com senha incorreta: HTTP 401. Falhas de autorização e validação de operações GraphQL: HTTP 200 com `errors`, usando `FORBIDDEN`, `BAD_REQUEST` ou `NOT_FOUND`. Clientes devem verificar `errors`, não apenas o status HTTP. Consultas de histórico têm no máximo 100 registros por página e profundidade GraphQL máxima 8.


- `contracts`: evento versionado e topologia AMQP compartilhados; não compartilha entidades clínicas nem banco.
- `scheduling-service`: regras de agenda, segurança, GraphQL e outbox transacional.
- `notification-service`: inbox, controle da versão da consulta, programação e envio de emails.
- A consulta e o evento são gravados na mesma transação. O publicador só marca o evento como enviado após confirmação do broker e verificação de que não foi devolvido por falta de rota.
- Quedas do RabbitMQ deixam eventos pendentes com backoff de até 300 segundos. Reentregas são esperadas; a inbox duplica por `eventId` e ignora versões antigas.
- Exchange `hospital.appointments.v1`, routing key `appointment.changed`, fila `hospital.notifications.v1`. Após tentativas limitadas de processamento, mensagens rejeitadas seguem para `hospital.notifications.dlq` pela exchange `hospital.notifications.dlx`.
- O consumer confirma a mensagem somente depois de persistir o processamento. A confirmação RabbitMQ não significa que o email já foi enviado.
- Criação/edição de consulta futura gera confirmação por email. Também agenda um lembrete para 24 horas antes. Se a consulta já está dentro dessa janela, a confirmação funciona como lembrete imediato, evitando dois emails simultâneos.
- Reagendamento cancela jobs pendentes antigos. Consultas passadas não geram novos lembretes. Alterações de registro clínico também publicam atualização, sem incluir o conteúdo clínico no evento.
- Falha SMTP recebe até cinco tentativas com backoff, depois o job fica `FAILED`, visível na métrica. O log contém identificadores e tipo de erro, sem endereço de email, senha ou registro clínico.

