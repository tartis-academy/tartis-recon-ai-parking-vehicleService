-- Añade la columna version con valor por defecto 0 para las filas existentes
ALTER TABLE vehicle 
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;