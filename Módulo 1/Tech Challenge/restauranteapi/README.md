# Usuarios API

Backend Spring Boot para cadastro e gerenciamento de usuarios, com validacao simples de login, troca de senha em endpoint separado e respostas de erro padronizadas com `ProblemDetail`.

## Stack

- Java 21
- Spring Boot 3.3
- Maven
- JDBC com `JdbcTemplate`
- MySQL/MariaDB via XAMPP ou MySQL em Docker
- Swagger/OpenAPI

## Como executar com XAMPP

1. Abra o XAMPP Control Panel.
2. Inicie o servico `MySQL`.
3. Acesse `http://localhost/phpmyadmin`.
4. Crie um banco chamado `restauranteapi`.
5. Rode a aplicacao:

```bash
mvn spring-boot:run
```

A configuracao padrao usa:

- URL: `jdbc:mysql://localhost:3306/restauranteapi`
- Usuario: `root`
- Senha: vazia

## Como executar com Docker Compose

```bash
docker compose up --build
```

A API ficara disponivel em:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Endpoints

- `POST /api/v1/users`: cadastra usuario.
- `GET /api/v1/users`: lista usuarios.
- `GET /api/v1/users?name=ana`: busca usuarios por nome.
- `GET /api/v1/users/{id}`: busca usuario por id.
- `PUT /api/v1/users/{id}`: atualiza dados gerais do usuario.
- `PATCH /api/v1/users/{id}/password`: troca senha em endpoint separado.
- `DELETE /api/v1/users/{id}`: exclui usuario.
- `POST /api/v1/auth/validate`: valida login e senha.

## Regras implementadas

- Tipos de usuario: `CLIENTE` e `DONO_RESTAURANTE`.
- E-mail e login de usuario sao unicos.
- Senhas sao armazenadas com hash SHA-256 e salt, sem Spring Security.
- A resposta de usuario nao expoe a senha nem o hash.
- Atualizacao de senha fica separada da atualizacao dos demais dados.
- A data da ultima alteracao e registrada em `lastModifiedAt`.
- Erros seguem `ProblemDetail` RFC 7807.
- A validacao de login e feita pelo `AuthService`, consultando o usuario no banco pelo login e comparando a senha informada com o hash salvo.

## Colecao Postman

A colecao esta em `postman/RestauranteAPI.postman_collection.json`.

Importe no Postman e execute os requests. A colecao usa a variavel `baseUrl` com valor padrao `http://localhost:8080`.
