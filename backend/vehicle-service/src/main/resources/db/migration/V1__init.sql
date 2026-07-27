-- RECON-812: migracion baseline de Flyway, sustituye al schema.sql que se
-- montaba como init script de Postgres. Debe reflejar exactamente
-- VehicleEntity.
CREATE TABLE vehicles (
    unique_id   UUID PRIMARY KEY,
    type        VARCHAR(20) NOT NULL,
    plate       VARCHAR(255) NOT NULL UNIQUE,
    brand       VARCHAR(255),
    model       VARCHAR(255),
    color       VARCHAR(255),
    num_doors   INTEGER,
    has_sidecar BOOLEAN,
    active      BOOLEAN NOT NULL
);
