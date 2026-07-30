-- VehicleEntity declara @Version Long version para el control de concurrencia
-- optimista, pero V1__init.sql nunca creo la columna. Sin esto Hibernate falla
-- al arrancar con ddl-auto=validate (application-prod.properties):
--
--   SchemaManagementException: Schema validation:
--   missing column [version] in table [vehicles]
--
-- Silencioso en dev y en CI: en dev ddl-auto=update crea la columna sola y CI
-- no arranca nunca en perfil prod, asi que solo se ve al levantar el stack.
--
-- Nullable porque las filas existentes no tienen version, y porque la entidad
-- no marca la columna como NOT NULL: Hibernate trata NULL como "sin version
-- todavia" y arranca el contador en el primer UPDATE. Mismo criterio que
-- V2__add_version.sql de tariff-service.
ALTER TABLE vehicles
    ADD COLUMN version BIGINT;
