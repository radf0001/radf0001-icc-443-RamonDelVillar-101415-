-- V1.0.1__Create_users.sql
CREATE TABLE IF NOT EXISTS users (
     id BIGSERIAL PRIMARY KEY,  -- PostgreSQL usa BIGSERIAL en lugar de AUTO_INCREMENT
     username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    email VARCHAR(255) UNIQUE,
    role VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE
);

-- Tabla de auditoría generada por Hibernate Envers
CREATE TABLE IF NOT EXISTS au_users (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype SMALLINT,  -- PostgreSQL usa SMALLINT en lugar de TINYINT
    username VARCHAR(50),
    password VARCHAR(120),
    email VARCHAR(255),
    role VARCHAR(255),
    enabled BOOLEAN,
    PRIMARY KEY (id, rev)
);

-- Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);