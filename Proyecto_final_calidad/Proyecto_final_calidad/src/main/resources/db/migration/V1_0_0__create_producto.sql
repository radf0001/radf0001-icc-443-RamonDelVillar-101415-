-- V1.0.2__Create_producto.sql
CREATE TABLE IF NOT EXISTS producto (
    id BIGSERIAL PRIMARY KEY,  -- PostgreSQL usa BIGSERIAL en lugar de AUTO_INCREMENT
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500),
    categoria VARCHAR(255),  -- Se guarda como texto porque usas EnumType.STRING
    precio DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL,
    stock_minimo INT NOT NULL,
    esta_activo BOOLEAN DEFAULT TRUE
);

-- Tabla de auditoría generada por Hibernate Envers
CREATE TABLE IF NOT EXISTS au_producto (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype SMALLINT,  -- PostgreSQL usa SMALLINT en lugar de TINYINT
    nombre VARCHAR(255),
    descripcion VARCHAR(500),
    categoria VARCHAR(255),
    precio DECIMAL(10,2),
    cantidad INT,
    stock_minimo INT,
    esta_activo BOOLEAN,
    PRIMARY KEY (id, rev)
);

-- Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_producto_nombre ON producto(nombre);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto(categoria);