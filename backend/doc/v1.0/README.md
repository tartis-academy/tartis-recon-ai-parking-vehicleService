# Alcance de la Fase II (v1.0.0) — vehicle-service

Documento explicativo del alcance, responsabilidad, modelo de dominio, endpoints expuestos, seguridad e infraestructura del microservicio `vehicle-service` durante la **Fase II (v1.0.0)** del sistema de parking inteligente **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase II

En la Fase II, `vehicle-service` evoluciona desde un simple CRUD síncrono a un microservicio hexagonal robusto, seguro y resiliente:
- **Gestión de Baja y Alta Lógica (RN-11):** Desactivación y reactivación de vehículos mediante estados lógicos (`active = true/false`) evitando el borrado físico de registros asociados a estancias pasadas.
- **Soporte para Categoría PMR:** Inclusión de la categoría de movilidad reducida (`CAR_PMR`) en la entidad de dominio y validaciones.
- **Seguridad Perimetral y RBAC (SEC-03):** Integración con Keycloak como OAuth2 Resource Server para validar tokens Bearer JWT y restringir operaciones según roles (`ADMIN`, `OPERARIO`).
- **Enrutamiento por API Gateway (Kong):** Soporte para enrutamiento centralizado y validación JWT perimetral.
- **Control de Concurrencia Optimista:** Prevención de perdidas de actualización en ediciones concurrentes mediante versión de registro.
- **Trazabilidad Distribuida (GW-06):** Inyección automática de `correlationId`, `userName` y `clientId` en el contexto MDC para rastreo de peticiones de extremo a extremo.

---

## 2. Modelo de Dominio y Persistencia (Fase II)

Entidad principal **`Vehicle`** con los siguientes atributos:

| Atributo | Tipo | Descripción | Validación / Restricción |
|---|---|---|---|
| `id` | `UUID` | Identificador único universal del vehículo | Autogenerado (PK) |
| `plate` | `String` | Número de matrícula del vehículo | Único, no nulo, validado por `validPlate` (patrones 1971-2000 y 2000-actualidad) |
| `vehicleType` | `VehicleType` | Tipo de vehículo (`CAR`, `MOTORBIKE`, `VAN`, `CAR_PMR`) | No nulo |
| `brand` | `String` | Marca del vehículo | Requerido |
| `model` | `String` | Modelo del vehículo | Requerido |
| `color` | `String` | Color del vehículo | Opcional |
| `doorsCount` | `Integer` | Número de puertas (para coches/furgonetas) | Opcional |
| `sidecar` | `Boolean` | Indicador de sidecar (para motocicletas) | Opcional |
| `active` | `Boolean` | Estado de actividad lógica del vehículo | Predeterminado `true` (**RN-11**) |
| `version` | `Long` | Control de concurrencia optimista | Gestionado por JPA / Flyway `V2` |

---

## 3. Endpoints Expuestos y Matriz de Roles (Fase II)

| Método HTTP | Endpoint | Descripción | Rol Keycloak Requerido | Respuesta Éxito |
|---|---|---|---|---|
| `POST` | `/v1/vehicles` | Registro de nuevo vehículo en el catálogo | `ADMIN`, `OPERARIO` | `201 Created` (`VehicleResponse`) |
| `GET` | `/v1/vehicles` | Consulta de catálogo paginado de vehículos | `ADMIN`, `OPERARIO` | `200 OK` (Página de `VehicleResponse`) |
| `GET` | `/v1/vehicles/{id}` | Consulta de vehículo por su UUID | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `PUT` | `/v1/vehicles/{id}` | Edición/actualización completa de vehículo | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `GET` | `/v1/vehicles/plate/{plate}` | Consulta síncrona por número de matrícula | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `PATCH` | `/v1/vehicles/{id}/status` | Baja lógica de vehículo (`active=false` **RN-11**) | `ADMIN` | `200 OK` (`VehicleResponse`) |
| `PATCH` | `/v1/vehicles/{id}/activate` | Alta lógica de vehículo (`active=true` **RN-11**) | `ADMIN` | `200 OK` (`VehicleResponse`) |

---

## 4. Arquitectura, Seguridad e Infraestructura (Fase II)

- **Arquitectura Hexagonal con Spring Security:**
  - **Seguridad a nivel de Método:** Anotaciones `@PreAuthorize("hasAnyRole('ADMIN', 'OPERARIO')")`.
  - **Mapeo de Autoridades:** `KeycloakRoleConverter` transformando `realm_access.roles` a `ROLE_*`.
  - **Traducción de Errores RFC 7807 (SEC-11):** Respuestas de error estandarizadas devolviendo `ProblemDetail` / `ErrorResponse` y cabeceras `WWW-Authenticate` en 401.
- **Base de Datos y Migraciones:**
  - Perfil `dev`: Postgres compartido `parking_dev` en puerto `5432` (esquema `vehicle`).
  - Perfil `prod`: Base de datos PostgreSQL dedicada `vehicle_db` en puerto `5433`.
  - Migraciones DDL Flyway (`V1__init.sql`, `V2__add_version.sql`).
- **Pruebas y Calidad:**
  - Pruebas de integración con Testcontainers (`VehiclePersistenceAdapterConcurrencyTest`).
  - Escaneo de seguridad de imágenes Docker mediante Trivy (`docker-scan`).
