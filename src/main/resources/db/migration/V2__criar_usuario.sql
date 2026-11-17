CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(160) NOT NULL UNIQUE,
    senha       VARCHAR(100) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL,
    cliente_id  BIGINT UNIQUE REFERENCES cliente(id)
);