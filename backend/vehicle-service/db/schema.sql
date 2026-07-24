-- DDL para el perfil prod (ddl-auto=validate): sin Flyway/Liquibase todavia,
-- el esquema se crea fuera de banda. Debe reflejar exactamente VehicleEntity.
-- Se monta como init script en la Postgres dedicada de vehicle-service.

CREATE TABLE IF NOT EXISTS vehicles (
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
