# Changelog

All notable changes to the `vehicle-service` microservice will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-04

### Added
- **Integración con Keycloak & Spring Security:** Configuración de OAuth2 Resource Server para validar Bearer Access Tokens emitidos por Keycloak y mapeo de autoridades con `KeycloakRoleConverter`.
- **Enrutamiento por API Gateway (Kong):** Soporte para enrutamiento centralizado y validación de tokens JWT en el perímetro a través de Kong API Gateway.
- **Categoría de Vehículo PMR:** Añadido soporte para vehículos de movilidad reducida (`CAR_PMR`) en la entidad de dominio y controlador REST.
- **Gestión de Baja Lógica (RN-11):** Implementado endpoint `PATCH /v1/vehicles/{id}/status` y `PATCH /v1/vehicles/{id}/activate` para activar o desactivar vehículos sin borrado físico de la base de datos.
- **Trazabilidad Distribuida & Logging (GW-06):** Inclusión de `CorrelationIdFilter`, `RequestIdentityFilter` y `RequestLoggingFilter` inyectando `correlationId`, `userName` y `clientId` en el MDC de SLF4J para trazabilidad de peticiones de extremo a extremo.
- **Control de Concurrencia Optimista:** Migración de esquema Flyway `V2__add_version.sql` añadiendo la columna `version` a la tabla `vehicles` para evitar pérdidas de actualización por concurrencia.
- **Pruebas de Integración con Testcontainers:** Añadida suite de pruebas concurrentes `VehiclePersistenceAdapterConcurrencyTest` ejecutando PostgreSQL en contenedor aislado.
- **Elevación de Cobertura de Tests:** Incremento de la cobertura de código por encima del 90% medido mediante el plugin JaCoCo en GitLab CI.

### Changed
- **Formato Común de Errores (SEC-11 / RFC 7807):** Estandarización de las respuestas de error mediante `CustomizedExceptionAdapter` devolviendo `ProblemDetail` / `ErrorResponse` uniforme en toda la API.
- **Control de Acceso basado en Roles (RBAC):** Restricción de endpoints según la matriz de seguridad `SEC-03` (`ADMIN` para activación/desactivación de vehículos; `ADMIN`/`OPERARIO` para consultas, altas y modificaciones).
- **Migración a Base de Datos Dedicada:** Soporte para perfil `prod` con PostgreSQL dedicada (Database-per-service en puerto 5433).

### Fixed
- **Validación de Matrículas (`Vehicle.validPlate`):** Corregida la expresión regular para validar patrones de matrícula españoles antiguos (1971-2000) y modernos (2000-actualidad), excluyendo vocales y letras no válidas (Ñ, Q).
- **Manejo de Respuestas de Autenticación (401 / 403):** Restaurada la emisión del encabezado `WWW-Authenticate` en respuestas 401 sin credenciales o con credenciales inválidas.
- **Manejo de Excepciones Concurrentes:** Tratamiento explícito de `VehicleConcurrentModificationException` y `ExistingVehicleException`.

### Security
- **Protección con `@PreAuthorize`:** Control de autorización a nivel de método en los adaptadores REST.
- **Escaneo Continuo de Vulnerabilidades:** Integración con Trivy (`docker-scan`) en el pipeline de CI/CD para la detección de vulnerabilidades en imágenes Docker.

## [0.5.0] - 2026-07-25

### Added
- **MVP Inicial de `vehicle-service`:** Implementación inicial de la arquitectura hexagonal para el catálogo de vehículos.
- **Dominio de Vehículos:** Entidad `Vehicle` con validación interna de dominio (matrícula, marca, modelo, color, número de puertas/sidecar).
- **Endpoints REST Síncronos:**
  - `POST /v1/vehicles`: Alta de vehículos comprobando unicidad de matrícula.
  - `GET /v1/vehicles`: Consulta de catálogo paginado.
  - `GET /v1/vehicles/{id}`: Consulta por UUID.
  - `PUT /v1/vehicles/{id}`: Edición de vehículo.
  - `GET /v1/vehicles/plate/{plate}`: Consulta por matrícula (usado síncronamente por `stay-service` en check-in).
- **Persistencia PostgreSQL:** Configuración inicial JPA/Hibernate con esquema `vehicle`.
- **Contrato OpenAPI:** Definición inicial de API en `openapi.yml`.

[1.0.0]: https://github.com/tartis-academy/tartis-recon-ai-parking-vehicleService/compare/v0.5.0...v1.0.0
[0.5.0]: https://github.com/tartis-academy/tartis-recon-ai-parking-vehicleService/releases/tag/v0.5.0
