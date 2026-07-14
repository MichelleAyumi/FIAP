# Gestão de Restaurantes

Este projeto é uma API  para cadastrar restaurantes e seus cardápios.

Com a aplicação é possível cadastrar:

- tipos de usuário, como cliente ou dono de restaurante;
- usuários;
- restaurantes;
- itens do cardápio.

Explicando de forma simples:

- `domain/model`: usuário, restaurante e item do cardápio;
- `application/usecase`: contém as ações realizadas pelo sistema;
- `application/port/out`: operações necessárias no banco;
- `infrastructure/persistence`: executa as operações no banco;
- `infrastructure/web`: recebe as requisições HTTP;
- `ApiDtos.java`: define os dados de entrada e saída da API.

## Relação entre os dados

Os cadastros são dependentes uns dos outros:

```text
Tipo de usuário -> Usuário -> Restaurante -> Item do cardápio
```

Ordem recomendada para cadastrar os dados:

1. criar um tipo de usuário;
2. criar um usuário usando o ID do tipo;
3. criar um restaurante usando o ID do usuário;
4. criar um item usando o ID do restaurante.


## Endpoints

### Tipos de usuário

| POST | `/api/user-types` | Cadastrar |
| GET | `/api/user-types` | Listar todos |
| GET | `/api/user-types/{id}` | Buscar pelo ID |
| PUT | `/api/user-types/{id}` | Atualizar |
| DELETE | `/api/user-types/{id}` | Excluir |


### Usuários

| POST | `/api/users` | Cadastrar |
| GET | `/api/users` | Listar todos |
| GET | `/api/users/{id}` | Buscar pelo ID |
| PUT | `/api/users/{id}` | Atualizar |
| DELETE | `/api/users/{id}` | Excluir |


### Restaurantes

| POST | `/api/restaurants` | Cadastrar |
| GET | `/api/restaurants` | Listar todos |
| GET | `/api/restaurants/{id}` | Buscar pelo ID |
| PUT | `/api/restaurants/{id}` | Atualizar |
| DELETE | `/api/restaurants/{id}` | Excluir |


### Itens do cardápio

| POST | `/api/menu-items` | Cadastrar |
| GET | `/api/menu-items` | Listar todos |
| GET | `/api/menu-items?restaurantId={id}` | Listar por restaurante |
| GET | `/api/menu-items/{id}` | Buscar pelo ID |
| PUT | `/api/menu-items/{id}` | Atualizar |
| DELETE | `/api/menu-items/{id}` | Excluir |


## Códigos de resposta

| 200 | Operação realizada|
| 201 | Cadastro criado com |
| 204 | Exclusão realizada |
| 400 | Dado enviado é inválido |
| 404 | Registro não encontrado |
| 409 | A operação não pode ser realizada |
