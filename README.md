# Agendamento API

![CI](https://github.com/brennovilela/agendamento-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)

API REST para agendamento de serviços (barbearias, clínicas, salões), com validação de expediente dos profissionais, prevenção de conflitos de horário, consulta de horários disponíveis e autenticação com JWT.

## Funcionalidades

- **Autenticação com JWT** e dois perfis de acesso: `ADMIN` e `CLIENTE`
- Cadastro de **profissionais** e **serviços** (com duração e preço)
- Definição do **expediente** de cada profissional por dia da semana
- Consulta de **horários disponíveis** de um profissional para um serviço em uma data
- **Agendamento** com cálculo automático do horário de término a partir da duração do serviço
- **Cancelamento** com regra de antecedência mínima
- Cada cliente acessa **apenas os próprios agendamentos**
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
| Um cliente não pode ver, criar ou cancelar agendamentos de outro cliente | `403` |

## Autenticação e permissões

A API usa **JWT (JSON Web Token)** assinado com HMAC-SHA256. O fluxo é:

1. O usuário se cadastra (`POST /auth/registrar`) ou faz login (`POST /auth/login`) e recebe um token
2. Nas requisições protegidas, envia o token no cabeçalho `Authorization: Bearer <token>`
3. O token expira em **2 horas**

| Acesso | Pode fazer |
|---|---|
| **Público** (sem login) | Cadastro, login, consultar serviços, profissionais, expediente e disponibilidade |
| **CLIENTE** | Criar, consultar e cancelar **os próprios** agendamentos |
| **ADMIN** | Tudo: cadastrar serviços, profissionais e horários, listar clientes e gerenciar qualquer agendamento |

Um usuário **ADMIN** é criado automaticamente na primeira inicialização. Em desenvolvimento, as credenciais padrão são `admin@agendamento.com` / `admin12345`. Em produção, elas são definidas pelas variáveis de ambiente abaixo.

| Variável | Descrição |
|---|---|
| `JWT_SECRET` | Chave de assinatura dos tokens (mínimo de 32 caracteres) |
| `ADMIN_EMAIL` | E-mail do administrador inicial |
| `ADMIN_SENHA` | Senha do administrador inicial |

## Tecnologias

- **Java 25** e **Spring Boot 4**
- **Spring Security** com **OAuth2 Resource Server** (JWT) e senhas com **BCrypt**
- **Spring Data JPA** / Hibernate
- **PostgreSQL 17** com migrations versionadas via **Flyway**
- **Bean Validation** para validação das requisições
- **springdoc-openapi** (Swagger UI)
- **JUnit 5**, **Mockito**, **AssertJ**, **MockMvc** e **Testcontainers**
- **Docker Compose** para o ambiente local
- **GitHub Actions** para integração contínua

## Arquitetura

O código é organizado **por funcionalidade**, e não por camada técnica:

```
src/main/java/io/github/brennovilela/agendamento_api
├── agendamento/     # Agendamento, disponibilidade, regras, services e controllers
├── cliente/         # Cadastro de clientes
├── profissional/    # Profissionais e horários de trabalho
├── servico/         # Catálogo de serviços
├── seguranca/       # Usuários, login, geração e validação de tokens JWT
└── compartilhado/   # Exceções, tratamento global de erros e configurações
```

### Decisões técnicas

- **Modelo de domínio rico**: as entidades não expõem setters. As mudanças de estado acontecem por métodos com significado de negócio (ex.: `agendamento.cancelar(agora)`), o que mantém as regras próximas dos dados e facilita os testes.
- **`Clock` injetado**: o horário atual é injetado via `Clock`, permitindo testar regras dependentes de tempo de forma determinística.
- **DTOs com `record`**: a API nunca expõe as entidades JPA diretamente.
- **Schema controlado pelo Flyway**: o Hibernate roda com `ddl-auto: validate`, apenas conferindo se as entidades batem com o banco.
- **Detecção de sobreposição** com a condição clássica de intervalos: `inicio < novoFim AND fim > novoInicio`. A mesma regra é reaproveitada no cálculo de disponibilidade.
- **Seguro por padrão**: qualquer endpoint que não esteja explicitamente liberado exige perfil `ADMIN`. Um endpoint novo esquecido na configuração fica bloqueado, e não exposto.
- **Autorização por dono do recurso**: além do perfil, a API verifica se o agendamento pertence ao cliente logado. Isso evita que um cliente acesse dados de outro apenas trocando o ID na URL.
- **Login sem vazamento de informação**: e-mail inexistente e senha errada retornam a mesma mensagem, impedindo descobrir quais e-mails estão cadastrados.

## Como rodar

**Pré-requisitos:** Java 25 e Docker.

```bash
git clone https://github.com/brennovilela/agendamento-api.git
cd agendamento-api
./mvnw spring-boot:run
```

O Spring Boot sobe automaticamente o PostgreSQL via Docker Compose e aplica as migrations.

Acesse a documentação em **http://localhost:8080/swagger-ui.html**. Para testar endpoints protegidos, faça login em `POST /auth/login`, copie o token e clique em **Authorize**.

## Endpoints

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/auth/registrar` | Cadastra um cliente e retorna o token | Público |
| `POST` | `/auth/login` | Autentica e retorna o token | Público |
| `GET` | `/servicos` | Lista serviços | Público |
| `POST` | `/servicos` | Cadastra um serviço | ADMIN |
| `GET` | `/profissionais` | Lista profissionais | Público |
| `POST` | `/profissionais` | Cadastra um profissional | ADMIN |
| `GET` | `/profissionais/{id}/horarios` | Lista o expediente do profissional | Público |
| `POST` | `/profissionais/{id}/horarios` | Adiciona um horário de trabalho | ADMIN |
| `GET` | `/profissionais/{id}/disponibilidade?servicoId=&data=` | Lista horários livres para um serviço em uma data | Público |
| `GET` | `/clientes` | Lista clientes | ADMIN |
| `GET` | `/clientes/{id}` | Busca um cliente | ADMIN |
| `POST` | `/agendamentos` | Cria um agendamento | Dono ou ADMIN |
| `GET` | `/agendamentos/{id}` | Busca um agendamento | Dono ou ADMIN |
| `PATCH` | `/agendamentos/{id}/cancelamento` | Cancela um agendamento | Dono ou ADMIN |

### Exemplo: login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "cliente@email.com",
  "senha": "minhasenha"
}
```

**200 OK**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Exemplo: consultando horários disponíveis

```http
GET /profissionais/1/disponibilidade?servicoId=1&data=2026-11-18
```

**200 OK**
```json
["2026-11-18T09:00:00", "2026-11-18T09:30:00", "2026-11-18T10:00:00", "..."]
```

### Exemplo: criando um agendamento

```http
POST /agendamentos
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
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

### Exemplo: acesso a agendamento de outro cliente

**403 Forbidden**
```json
{
  "title": "Forbidden",
  "status": 403,
  "detail": "Você não tem permissão para acessar este recurso",
  "instance": "/agendamentos/3"
}
```

## Testes

```bash
./mvnw test
```

- **Testes unitários** das regras de domínio (`AgendamentoTest`), sem banco nem Spring
- **Testes dos services** com Mockito e relógio fixo (`AgendamentoServiceTest`, `DisponibilidadeServiceTest`)
- **Testes de integração de segurança** com MockMvc e PostgreSQL real (`SegurancaIntegrationTest`), cobrindo autenticação, perfis e acesso por dono do recurso
- **Teste de contexto** subindo a aplicação completa com Testcontainers

## Próximos passos

- [x] Autenticação com Spring Security + JWT
- [x] Endpoint de horários disponíveis de um profissional em um dia
- [ ] Notificação por e-mail na confirmação e lembrete do agendamento
- [ ] Constraint de exclusão no PostgreSQL para impedir sobreposição mesmo sob concorrência
- [ ] Deploy em nuvem