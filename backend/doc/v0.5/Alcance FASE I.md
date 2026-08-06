# vehicle-service — Alcance y Especificación de Fase 1 (v0.5.0)

Este documento especifica el alcance funcional, el modelo de datos y los endpoints del microservicio `vehicle-service` correspondientes a la **Fase 1 (MVP - v0.5.0)** del sistema de gestión de parking **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 1

En la Fase 1 (`v0.5.0`), `vehicle-service` actúa como el **catálogo síncrono centralizado de vehículos** registrados en la plataforma:

- **Gestión de Ficha de Vehículo:** Registro, consulta paginada, edición y recuperación por UUID o matrícula.
- **Validación de Dominio:** Garantía de sintaxis correcta en matrículas españolas (formatos moderno y antiguo) y restricción estricta de unicidad de matrícula.
- **Soporte Síncrono al Check-in:** Exposición del endpoint `GET /v1/vehicles/plate/{plate}` consumido síncronamente por `stay-service` al validar la entrada de un vehículo en la barrera.

---

## 2. Modelo de Dominio (`Vehicle`) en Fase 1

### Entidad de Dominio
El núcleo del dominio gestiona la entidad `Vehicle` con los siguientes atributos:

| Atributo | Tipo Java | Descripción | Obligatorio |
|---|---|---|---|
| `id` | `UUID` | Identificador único universal del vehículo | Sí |
| `licensePlate` | `String` | Matrícula del vehículo (normalizada en mayúsculas) | Sí (Única) |
| `brand` | `String` | Marca del vehículo (ej: SEAT, Toyota) | No |
| `model` | `String` | Modelo del vehículo (ej: Ibiza, Corolla) | No |
| `color` | `String` | Color de la carrocería | No |
| `type` | `VehicleType` | Categoría del vehículo (`CAR`, `CAR_PMR`, `MOTORBIKE`) | Sí |

### Reglas de Validación de Dominio (`Vehicle.validPlate`)
La entidad valida internamente la sintaxis de la matrícula según los patrones oficiales de la DGT española:
1. **Formato Moderno (2000 - Actualidad):** 4 dígitos seguidos de 3 consonantes (excluyendo vocales y las letras Ñ y Q, ej: `1234BBB`, `9999ZZZ`).
2. **Formato Antiguo (1971 - 2000):** 1 o 2 letras de código de provincia + 4 dígitos + 1 o 2 letras de serie (ej: `M1234AB`, `MA1234AB`).

---

## 3. Endpoints REST Expuestos en Fase 1 (v0.5.0)

Todos los endpoints devuelven la estructura DTO acordada en la especificación OpenAPI de la versión `v0.5.0`.

### Tabla de Endpoints API v0.5.0

| Método HTTP | Endpoint | Descripción | Parámetros / Body | Respuesta Exitosa |
|---|---|---|---|---|
| `POST` | `/v1/vehicles` | Alta de un nuevo vehículo en el catálogo | `CreateVehicleRequest` | `201 Created` (`VehicleResponse`) |
| `GET` | `/v1/vehicles` | Consulta paginada de vehículos | `page`, `size` | `200 OK` (`Page<VehicleResponse>`) |
| `GET` | `/v1/vehicles/{id}` | Consulta de vehículo por UUID | `id` (UUID en path) | `200 OK` (`VehicleResponse`) |
| `PUT` | `/v1/vehicles/{id}` | Edición de datos de un vehículo | `id` (path) + `UpdateVehicleRequest` | `200 OK` (`VehicleResponse`) |
| `GET` | `/v1/vehicles/plate/{plate}` | Búsqueda síncrona por matrícula | `plate` (String en path) | `200 OK` (`VehicleResponse`) |

---

## 4. Modelo de Persistencia (PostgreSQL) — Fase 1

En la Fase 1, la persistencia se realiza sobre la base de datos PostgreSQL compartida (`parking_dev`) en el esquema `vehicle`.

### Migration Baseline (`V1__init.sql`)

```sql
CREATE SCHEMA IF NOT EXISTS vehicle;

CREATE TABLE vehicle.vehicles (
    id UUID PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL CONSTRAINT uk_vehicle_license_plate UNIQUE,
    brand VARCHAR(50),
    model VARCHAR(50),
    color VARCHAR(30),
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. Exclusiones y Límites de la Fase 1 (Diferencias con Fase 2 / v1.0.0)

Para evitar ambigüedades con la versión actual (`v1.0.0`), las siguientes funcionalidades **NO formaban parte del alcance de la Fase 1 (`v0.5.0`)**:

- ❌ **No incluía baja/alta lógica (RN-11):** En Fase 1 los vehículos no disponían del campo `active` ni los endpoints `PATCH /v1/vehicles/{id}/status` o `PATCH /v1/vehicles/{id}/activate`.
- ❌ **No incluía autenticación Keycloak ni RBAC:** Los endpoints se ejecutaban sin validación de Bearer Tokens JWT ni roles (`ADMIN`/`OPERARIO`).
- ❌ **No incluía enrutamiento Kong API Gateway:** Las peticiones se realizaban directamente contra el puerto `8081`.
- ❌ **No incluía control de concurrencia optimista:** No existía la columna `version` en la tabla `vehicles` (`V2__add_version.sql`).
- ❌ **No incluía trazabilidad distribuida MDC (GW-06):** No se inyectaban los filtros `CorrelationIdFilter`, `RequestIdentityFilter` ni `RequestLoggingFilter`.
