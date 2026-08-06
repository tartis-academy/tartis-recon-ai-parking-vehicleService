# vehicle-service — Alcance y Especificación de Fase 2 (v1.0.0)

Este documento especifica el alcance funcional completo, el modelo de datos, la seguridad y los endpoints del microservicio `vehicle-service` correspondientes a la **Fase 2 (v1.0.0)** del sistema de gestión de parking **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 2

En la Fase 2 (`v1.0.0`), `vehicle-service` evoluciona a un microservicio hexagonal maduro y seguro con capacidad de despliegue en entorno de producción aislado:

- **Gestión Completa del Ciclo de Vida del Vehículo:** Alta, consulta paginada, edición, búsqueda por matrícula y **baja/alta lógica (RN-11)**.
- **Soporte para Movilidad Reducida:** Clasificación explícita de categorías de vehículos (`CAR`, `CAR_PMR`, `MOTORBIKE`).
- **Seguridad Perimetral & RBAC (SEC-03):** Integración con **Keycloak IdP** y **Kong API Gateway** mediante OAuth2 Resource Server (JWT) con control de acceso por roles (`ADMIN`, `OPERARIO`).
- **Control de Concurrencia Optimista:** Protección contra pérdidas de actualización por peticiones simultáneas mediante bloqueos optimistas (`@Version`).
- **Trazabilidad Distribuida (GW-06):** Inyección de cabeceras de correlación e identidad (`X-Correlation-ID`, `X-User-Name`, `X-Client-ID`) en el MDC de SLF4J.
- **Formato Común de Errores (SEC-11 / RFC 7807):** Respuestas de error estructuradas tipo `ProblemDetail`.

---

## 2. Modelo de Dominio (`Vehicle`) en Fase 2

### Entidad de Dominio
La entidad `Vehicle` incluye los atributos necesarios para el control de inventario y estado:

| Atributo | Tipo Java | Descripción | Obligatorio | Novedad v1.0 |
|---|---|---|---|---|
| `id` | `UUID` | Identificador único universal del vehículo | Sí | No |
| `licensePlate` | `String` | Matrícula normalizada en mayúsculas (única) | Sí | No |
| `brand` | `String` | Marca del vehículo | No | No |
| `model` | `String` | Modelo del vehículo | No | No |
| `color` | `String` | Color de la carrocería | No | No |
| `type` | `VehicleType` | Categoría (`CAR`, `CAR_PMR`, `MOTORBIKE`) | Sí | No |
| `active` | `boolean` | Estado lógico del vehículo (`true` activo / `false` baja) | Sí | **SÍ (RN-11)** |
| `version` | `Long` | Versión para control de concurrencia optimista | Sí | **SÍ** |

### Reglas de Negocio de Dominio
1. **Validación de Matrículas (`Vehicle.validPlate`):** Validación mediante expresiones regulares de matrículas españolas modernas (4 números + 3 consonantes sin vocales ni Ñ/Q) y antiguas (provincia + 4 números + letras).
2. **Baja y Alta Lógica (RN-11):** Un vehículo dado de baja (`active = false`) no puede ser utilizado para realizar un check-in en `stay-service`.
3. **Unicidad:** Restricción de unicidad estricta sobre el campo `licensePlate`.

---

## 3. Matriz de Endpoints REST & Seguridad RBAC (v1.0.0)

Todos los endpoints están protegidos mediante `@PreAuthorize` según la matriz de seguridad **SEC-03** y enrutados a través de **Kong API Gateway**.

| Método HTTP | Endpoint | Descripción | Roles Permitidos (RBAC) | Respuesta Exitosa |
|---|---|---|---|---|
| `POST` | `/v1/vehicles` | Alta de un nuevo vehículo | `ADMIN`, `OPERARIO` | `201 Created` (`VehicleResponse`) |
| `GET` | `/v1/vehicles` | Listado paginado de vehículos | `ADMIN`, `OPERARIO` | `200 OK` (`Page<VehicleResponse>`) |
| `GET` | `/v1/vehicles/{id}` | Consulta de vehículo por UUID | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `PUT` | `/v1/vehicles/{id}` | Edición de datos de un vehículo | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `GET` | `/v1/vehicles/plate/{plate}` | Búsqueda síncrona por matrícula | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `PATCH` | `/v1/vehicles/{id}/status` | **Baja lógica** de vehículo (RN-11) | `ADMIN` | `200 OK` (`VehicleResponse`) |
| `PATCH` | `/v1/vehicles/{id}/activate` | **Reactivación** de vehículo (RN-11) | `ADMIN` | `200 OK` (`VehicleResponse`) |

---

## 4. Modelo de Persistencia (PostgreSQL) — Migraciones Flyway

### 1. Migration Baseline (`V1__init.sql`)
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

### 2. Migration Concurrencia & Estado (`V2__add_version_and_active.sql`)
```sql
ALTER TABLE vehicle.vehicles 
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

---

## 5. Arquitectura Hexagonal y Componentes Técnicos (v1.0.0)

```text
[ Kong Gateway / Keycloak ]
           |
           v (HTTP + Bearer Token)
+-------------------------------------------------------------------+
| Input Adapter: VehicleRestAdapter (@RestController)               |
+-------------------------------------------------------------------+
           |
           v (Invoca puertos de entrada)
+-------------------------------------------------------------------+
| Use Cases Core: CreateVehicle, UpdateVehicle, ChangeStatusUseCase |
+-------------------------------------------------------------------+
           |
           v (Invoca puertos de salida)
+-------------------------------------------------------------------+
| Output Adapter: VehiclePersistenceAdapter (Spring Data JPA)       |
+-------------------------------------------------------------------+
           |
           v (SQL PostgreSQL)
[( vehicle_db : 5433 )]
```

### Principales Componentes Añadidos en v1.0.0:
- **`KeycloakRoleConverter`:** Mapeador personalizado de JWT extrayendo los roles del claim `realm_access.roles`.
- **`CustomizedExceptionAdapter`:** Controlador global de excepciones transformando errores en `ProblemDetail` (RFC 7807 / SEC-11).
- **Filtros MDC (`GW-06`):** `CorrelationIdFilter`, `RequestIdentityFilter`, `RequestLoggingFilter`.
- **Suite de Pruebas Testcontainers:** `VehiclePersistenceAdapterConcurrencyTest` ejecutando PostgreSQL aislado en contenedor Docker para validar bloqueos pesimistas/optimistas.
