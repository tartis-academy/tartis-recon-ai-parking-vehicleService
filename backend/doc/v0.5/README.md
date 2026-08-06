# Alcance de la Fase I (MVP v0.5.0) — vehicle-service

Documento explicativo del alcance, responsabilidad, modelo de dominio, endpoints expuestos e infraestructura del microservicio `vehicle-service` durante la **Fase I (MVP v0.5.0)** del sistema de parking inteligente **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase I

En la Fase I, `vehicle-service` se encarga de actuar como el catálogo síncrono centralizado de los vehículos registrados en la plataforma:
- **Gestión del Catálogo de Vehículos:** Registro, consulta y edición de los datos descriptivos de cada vehículo.
- **Validación de Matrículas:** Verificación de formato y sintaxis de matrícula en el dominio antes de persistir.
- **Consulta Síncrona de Existencia:** Proporcionar un punto de acceso directo para que `stay-service` verifique la existencia del vehículo durante el proceso síncrono de *check-in*.

---

## 2. Modelo de Dominio (Fase I)

Entidad principal **`Vehicle`** con los siguientes atributos:

| Atributo | Tipo | Descripción | Validación / Restricción |
|---|---|---|---|
| `id` | `UUID` | Identificador único universal del vehículo | Autogenerado (PK) |
| `plate` | `String` | Número de matrícula del vehículo | Único, no nulo, validado por `validPlate` |
| `vehicleType` | `VehicleType` | Tipo de vehículo (`CAR`, `MOTORBIKE`, `VAN`) | No nulo |
| `brand` | `String` | Marca del vehículo | Requerido |
| `model` | `String` | Modelo del vehículo | Requerido |
| `color` | `String` | Color del vehículo | Opcional |
| `doorsCount` | `Integer` | Número de puertas (para coches/furgonetas) | Opcional |
| `sidecar` | `Boolean` | Indicador de sidecar (para motocicletas) | Opcional |

> **Nota:** En la Fase I la entidad no contiene el atributo de control de versión optimista (`version`) ni el campo de estado para baja/alta lógica (`active`).

---

## 3. Endpoints REST Expuestos (Fase I)

En la Fase I, todos los endpoints se exponen directamente vía HTTP sin capas perimetrales de seguridad (sin OAuth2/Keycloak ni API Gateway Kong):

| Método HTTP | Endpoint | Descripción | Cuerpo / Parámetros | Respuesta Éxito |
|---|---|---|---|---|
| `POST` | `/v1/vehicles` | Registro de nuevo vehículo en el catálogo | JSON `CreateVehicleRequest` | `201 Created` (`VehicleResponse`) |
| `GET` | `/v1/vehicles` | Consulta de catálogo paginado de vehículos | `page`, `size`, `sort` | `200 OK` (Página de `VehicleResponse`) |
| `GET` | `/v1/vehicles/{id}` | Consulta de vehículo por su UUID | `{id}` (UUID) | `200 OK` (`VehicleResponse`) |
| `PUT` | `/v1/vehicles/{id}` | Edición/actualización completa de vehículo | `{id}`, JSON `UpdateVehicleRequest` | `200 OK` (`VehicleResponse`) |
| `GET` | `/v1/vehicles/plate/{plate}` | Consulta síncrona por número de matrícula | `{plate}` (String) | `200 OK` (`VehicleResponse`) |

---

## 4. Arquitectura y Persistencia en Fase I

- **Arquitectura Hexagonal (Puertos y Adaptadores):**
  - **Adaptador de Entrada:** `VehicleRestControllerAdapter` (`@RestController` Spring MVC).
  - **Casos de Uso (Core):** `CreateVehicleUseCase`, `GetVehicleUseCase`, `UpdateVehicleUseCase`.
  - **Adaptador de Salida:** `VehiclePersistenceAdapter` utilizando Spring Data JPA / Hibernate.
- **Base de Datos:** PostgreSQL compartido (`parking_dev`) en puerto `5432`, operando sobre el esquema dedicado `vehicle`.

---

## 5. Diferencias Clave respecto a la Fase II (v1.0.0)

Para mantener la trazabilidad del alcance, las siguientes capacidades **NO forman parte de la Fase I (v0.5.0)** y fueron introducidas en la Fase II:

1. **Baja y Alta Lógica (RN-11):** No existen los endpoints `PATCH /v1/vehicles/{id}/status` ni `PATCH /v1/vehicles/{id}/activate`.
2. **Categoría PMR:** No existe el tipo de vehículo `CAR_PMR`.
3. **Seguridad OAuth2 / Keycloak:** No hay validación de Bearer Access Tokens JWT ni control de acceso por roles RBAC (`ADMIN`, `OPERARIO`).
4. **API Gateway (Kong):** No hay enrutamiento centralizado en el perímetro.
5. **Control de Concurrencia Optimista:** No existe la columna `version` en la tabla `vehicles` (migración Flyway `V2`).
6. **Trazabilidad MDC:** No existen los filtros de logging distribuido (`CorrelationIdFilter`, `RequestIdentityFilter`).
