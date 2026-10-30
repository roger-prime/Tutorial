CREATE TABLE IF NOT EXISTS lugar (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL
);
CREATE TABLE IF NOT EXISTS categoria (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL
);
CREATE TABLE IF NOT EXISTS reporte (
    uuid VARCHAR(36) PRIMARY KEY,
    lugar_id BIGINT NOT NULL REFERENCES lugar(id),
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    descripcion VARCHAR(500) NOT NULL,
    fecha_creacion VARCHAR(40) NOT NULL,
    CHECK (CHAR_LENGTH(TRIM(descripcion)) BETWEEN 10 AND 500)
);

-- Migración aditiva: preserva los registros de instalaciones existentes.
ALTER TABLE reporte ADD COLUMN IF NOT EXISTS anonimo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE reporte ADD COLUMN IF NOT EXISTS autor VARCHAR(80);

CREATE TABLE IF NOT EXISTS sync_lock(id BIGINT PRIMARY KEY);
INSERT INTO sync_lock(id) SELECT 1 WHERE NOT EXISTS(SELECT 1 FROM sync_lock WHERE id=1);
CREATE TABLE IF NOT EXISTS catalog_receipt(
    tipo VARCHAR(10) NOT NULL, uuid VARCHAR(36) NOT NULL, nombre VARCHAR(80) NOT NULL,
    item_id BIGINT NOT NULL, PRIMARY KEY(tipo,uuid)
);
