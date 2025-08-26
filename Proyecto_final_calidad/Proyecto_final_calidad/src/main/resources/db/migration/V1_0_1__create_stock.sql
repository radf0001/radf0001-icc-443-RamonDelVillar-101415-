-- V1.0.3__Create_stock.sql
CREATE TABLE IF NOT EXISTS stock (
    id BIGSERIAL PRIMARY KEY,  -- PostgreSQL usa BIGSERIAL en lugar de AUTO_INCREMENT
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    tipo VARCHAR(255),  -- EnumType.STRING
    fecha TIMESTAMP,
    usuario VARCHAR(255),
    CONSTRAINT fk_stock_producto FOREIGN KEY (producto_id)
    REFERENCES producto (id) ON DELETE CASCADE
);

-- Tabla de auditoría de Envers
CREATE TABLE IF NOT EXISTS au_stock (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype SMALLINT,  -- PostgreSQL usa SMALLINT en lugar de TINYINT
    producto_id BIGINT,
    cantidad INT,
    tipo VARCHAR(255),
    fecha TIMESTAMP,
    usuario VARCHAR(255),
    PRIMARY KEY (id, rev)
);