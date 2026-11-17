CREATE TABLE cliente (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL UNIQUE,
    telefone    VARCHAR(20),
    criado_em   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE profissional (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL UNIQUE,
    ativo       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE servico (
    id                BIGSERIAL PRIMARY KEY,
    nome              VARCHAR(120) NOT NULL,
    duracao_minutos   INTEGER NOT NULL CHECK (duracao_minutos > 0),
    preco             NUMERIC(10, 2) NOT NULL CHECK (preco >= 0),
    ativo             BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE horario_trabalho (
    id               BIGSERIAL PRIMARY KEY,
    profissional_id  BIGINT NOT NULL REFERENCES profissional(id),
    dia_semana       SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio      TIME NOT NULL,
    hora_fim         TIME NOT NULL,
    CHECK (hora_fim > hora_inicio)
);

CREATE TABLE agendamento (
    id               BIGSERIAL PRIMARY KEY,
    cliente_id       BIGINT NOT NULL REFERENCES cliente(id),
    profissional_id  BIGINT NOT NULL REFERENCES profissional(id),
    servico_id       BIGINT NOT NULL REFERENCES servico(id),
    inicio           TIMESTAMP NOT NULL,
    fim              TIMESTAMP NOT NULL,
    status           VARCHAR(20) NOT NULL,
    criado_em        TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (fim > inicio)
);

CREATE INDEX idx_agendamento_profissional_inicio
    ON agendamento (profissional_id, inicio);