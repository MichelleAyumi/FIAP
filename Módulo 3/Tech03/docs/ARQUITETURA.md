# Decisões de arquitetura
Foi escolhido RabbitMQ para os eventos de agendamento e entrega de notificações. 

São dois processos Spring Boot independentes, cada um proprietário de seu banco. O módulo `contracts` define somente o formato de integração e os nomes de filas. Agendamento não acessa o banco das notificações, e notificações não lê tabelas clínicas.

## Consistência

`appointments` e `outbox_events` compartilham a transação local de agendamento. O broker não participa da transação do banco. A publicação ocorre depois e admite duplicatas. A inbox registra IDs processados e a versão mais recente por consulta. Transações protegem tanto a deduplicação quanto a criação/cancelamento de jobs.

O consumer confirma uma mensagem após a transação da inbox. Uma falha entre commit e ack permite reentrega sem recriar os jobs. O SMTP, por outro lado, não oferece uma transação comum com o banco: se o email for aceito e o processo cair antes de gravar `SENT`, a tentativa seguinte pode duplicar o email.

O publicador usa lock na linha da outbox, confirmações do produtor e retorno de mensagem não roteada. Eventos não são eliminados silenciosamente ao esgotar tentativas: continuam pendentes com backoff e alerta operacional. Falhas do consumer vão para DLQ; falhas de SMTP ficam na tabela de jobs em `FAILED`, pois a mensagem do broker já cumpriu seu papel de persistir o agendamento do lembrete.

## Concorrência

Agendamento bloqueia a linha do médico e depois do paciente antes de verificar interseções de intervalos. O mesmo paciente não pode ter duas consultas sobrepostas com médicos diferentes.

O serviço de notificações usa uma linha de lock global no banco para serializar inbox e envio. 

## Segurança

HTTP Basic stateless e BCrypt atendem à autenticação solicitada. A aplicação demo usa HTTP somente em localhost. O endpoint GraphQL recebe JSON; a API não usa autenticação baseada em sessão ou formulários.

Autorização reside na camada de serviço com `@PreAuthorize`. Pacientes também passam por verificação de propriedade usando o vínculo persistido da conta. Enfermeiros editam os campos de agendamento, mas não o registro clínico.

Mensagens contêm apenas identificadores, versão, horário e email de destino. Motivos e notas clínicas não são propagados. Ainda existe dado pessoal (email) no broker e banco de notificações.

