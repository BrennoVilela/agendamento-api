# Agendamento API

![CI](https://github.com/brennovilela/agendamento-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)

API REST para agendamento de serviços (barbearias, clínicas, salões), com validação de expediente dos profissionais, prevenção de conflitos de horário e regras de cancelamento.

## Funcionalidades

- Cadastro de **clientes**, **profissionais** e **serviços** (com duração e preço)
- Definição do **expediente** de cada profissional por dia da semana
- **Agendamento** com cálculo automático do horário de término a partir da duração do serviço
- **Cancelamento** com regra de antecedência mínima
- Documentação interativa com **Swagger / OpenAPI**
- Respostas de erro padronizadas no formato **Problem Details (RFC 9457)**

## Regras de negócio

| Regra | Resposta |
|---|---|
| Não é possível agendar no passado | `422` |
| O agendamento deve estar inteiramente dentro do expediente do profissional | `422` |
| Um profissional não pode ter dois agendamentos sobrepostos | `409` |
| Agendamentos cancelados liberam o horário | — |
| Cancelamento exige pelo menos 2 horas de antecedência | `422` |
| Agendamentos cancelados ou concluídos não podem ser cancelados novamente | `422` |
| Profissionais e serviços inativos não podem receber agendamentos | `422` |

## Tecnologias

- **Java 25** e **Spring Boot 4**
- **Spring Data JPA** / Hibernate
- **PostgreSQL 17** com migrations versionadas via **Flyway**
- **Bean Validation** para validação das requisições
- **springdoc-openapi** (Swagger UI)
- **JUnit 5**, **Mockito**, **AssertJ** e **Testcontainers**
- **Docker Compose** para o ambiente local
- **GitHub Actions** para integração contínua

## Arquitetura

O código é organizado **por funcionalidade**, e não por camada técnica:

```
src/main/java/io/github/brennovilela/agendamento_api
├── agendamento/     # Agendamento, regras, service, controller e repositório
├── cliente/         # Cadastro de clientes
├── profissional/    # Profissionais e horários de trabalho
├── servico/         # Catálogo de serviços
└── compartilhado/   # Exceções, tratamento global de erros e configurações
```

### Decisões técnicas

- **Modelo de domínio rico**: as entidades não expõem setters. As mudanças de estado acontecem por métodos com significado de negócio (ex.: `agendamento.cancelar(agora)`), o que mantém as regras próximas dos dados e facilita os testes.
- **`Clock` injetado**: o horário atual é injetado via `Clock`, permitindo testar regras dependentes de tempo de forma determinística.
- **DTOs com `record`**: a API nunca expõe as entidades JPA diretamente.
- **Schema controlado pelo Flyway**: o Hibernate roda com `ddl-auto: validate`, apenas conferindo se as entidades batem com o banco.
- **Detecção de sobreposição** com a condição clássica de intervalos: `inicio < novoFim AND fim > novoInicio`.

## Como rodar

**Pré-requisitos:** Java 25 e Docker.

```bash
git clone https://github.com/brennovilela/agendamento-api.git
cd agendamento-api
./mvnw spring-boot:run
```

O Spring Boot sobe automaticamente o PostgreSQL via Docker Compose e aplica as migrations.

Acesse a documentação em **http://localhost:8080/swagger-ui.html**.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/clientes` | Cadastra um cliente |
| `GET` | `/clientes` | Lista clientes |
| `GET` | `/clientes/{id}` | Busca um cliente |
| `POST` | `/servicos` | Cadastra um serviço |
| `GET` | `/servicos` | Lista serviços |
| `POST` | `/profissionais` | Cadastra um profissional |
| `GET` | `/profissionais` | Lista profissionais |
| `POST` | `/profissionais/{id}/horarios` | Adiciona um horário de trabalho |
| `GET` | `/profissionais/{id}/horarios` | Lista o expediente do profissional |
| `POST` | `/agendamentos` | Cria um agendamento |
| `GET` | `/agendamentos/{id}` | Busca um agendamento |
| `PATCH` | `/agendamentos/{id}/cancelamento` | Cancela um agendamento |

### Exemplo: criando um agendamento

```http
POST /agendamentos
Content-Type: application/json

{
  "clienteId": 1,
  "profissionalId": 1,
  "servicoId": 1,
  "inicio": "2026-11-18T14:00:00"
}
```

**201 Created**
```json
{
  "id": 1,
  "clienteId": 1,
  "profissionalId": 1,
  "servicoId": 1,
  "inicio": "2026-11-18T14:00:00",
  "fim": "2026-11-18T14:30:00",
  "status": "AGENDADO"
}
```

### Exemplo: conflito de horário

**409 Conflict**
```json
{
  "title": "Conflict",
  "status": 409,
  "detail": "Profissional já possui agendamento nesse horário",
  "instance": "/agendamentos"
}
```

## Testes

```bash
./mvnw test
```

- **Testes unitários** das regras de domínio (`AgendamentoTest`), sem banco nem Spring
- **Testes do service** com Mockito e relógio fixo (`AgendamentoServiceTest`)
- **Teste de integração** subindo um PostgreSQL real com Testcontainers

## Próximos passos

- [ ] Autenticação com Spring Security + JWT
- [ ] Endpoint de horários disponíveis de um profissional em um dia
- [ ] Notificação por e-mail na confirmação e lembrete do agendamento
- [ ] Constraint de exclusão no PostgreSQL para impedir sobreposição mesmo sob concorrência
- [ ] Deploy em nuvem