# tartis-recon-ai-parking — vehicle-service

## 1. Responsabilidad del microservicio

`vehicle-service` es el microservicio encargado del catálogo y gestión del dominio de **Vehículos** en el sistema de parking **TARTIS Recon-AI**. Sus responsabilidades principales incluyen:

- **Alta y Registro de Vehículos:** Registro de vehículos garantizando la unicidad de la matrícula y la validación estricta de las reglas de dominio (patrón de matrícula DGT española, categoría `CAR`, `CAR_PMR` o `MOTORBIKE`, marca, modelo, color, número de puertas o sidecar).
- **Consulta y Búsqueda:** Consulta paginada de vehículos, búsqueda por UUID y búsqueda por matrícula (utilizada síncronamente por `stay-service` durante el flujo de check-in).
- **Modificación:** Edición de atributos modificables del vehículo (la matrícula se mantiene inmutable).
- **Baja y Alta Lógica (RN-11):** Gestión del estado de activación (`active=true/false`). Según **RN-11**, los vehículos no se eliminan físicamente de la base de datos para preservar el historial de estancias. Un vehículo dado de baja lógica (`active=false`) tiene bloqueado el acceso al parking al intentar el check-in.

---

## 2. Endpoints expuestos

Todos los endpoints requieren autenticación perimetral y validación mediante Bearer Access Token (emitido por Keycloak), exceptuando las sondas públicas de salud.

| Método | Endpoint | Descripción | Roles Autorizados (RBAC SEC-03) | Respuesta Exitosa |
|---|---|---|---|---|
| `GET` | `/v1/vehicles` | Listado paginado de vehículos (filtros por `active` y `type`) | `ADMIN`, `OPERARIO` | `200 OK` (`Page<VehicleResponse>`) |
| `POST` | `/v1/vehicles` | Registra un nuevo vehículo comprobando la unicidad de matrícula | `ADMIN`, `OPERARIO` | `201 Created` (`VehicleResponse`) |
| `GET` | `/v1/vehicles/{id}` | Obtiene el detalle completo de un vehículo por su UUID | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `PUT` | `/v1/vehicles/{id}` | Actualiza los datos editables de un vehículo | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `GET` | `/v1/vehicles/plate/{plate}` | Consulta un vehículo por su matrícula (usado síncronamente por stay-service) | `ADMIN`, `OPERARIO` | `200 OK` (`VehicleResponse`) |
| `PATCH` | `/v1/vehicles/{id}/status` | Modifica el estado de activación (baja lógica RN-11) | `ADMIN` | `200 OK` (`VehicleResponse`) |
| `PATCH` | `/v1/vehicles/{id}/activate` | Reactiva explícitamente un vehículo desactivado | `ADMIN` | `200 OK` (`VehicleResponse`) |
| `GET` | `/actuator/health` | Probes de salud del servicio (Liveness / Readiness) | Público | `200 OK` |

---

## 3. Casos de Uso (Arquitectura Hexagonal)

Los casos de uso de la capa de aplicación orchestran las reglas de dominio utilizando los puertos de salida:

- **`CreateVehicleUseCase`:** Valida la unicidad de la matrícula y persiste la entidad `Vehicle` (nace en estado `active=true`).
- **`GetVehicleByPlateUseCase`:** Recupera la entidad por matrícula para la verificación de check-in.
- **`GetVehicleUseCase`:** Obtiene el detalle de un vehículo por su identificador UUID.
- **`ListVehiclesUseCase`:** Retorna la lista paginada filtrada opcionalmente por categoría o estado activo.
- **`UpdateVehicleUseCase`:** Actualiza los atributos de marca, modelo, color y características de puertas/sidecar.
- **`UpdateVehicleStatusUseCase`:** Ejecuta la baja o reactivación lógica cambiando el flag `active` según **RN-11**.

### Puertos de Dominio:
- **Puerto de Entrada:** `CreateVehiclePort`, `GetVehiclePort`, `ListVehiclesPort`, `UpdateVehiclePort`, `UpdateVehicleStatusPort`.
- **Puertos de Salida:** `VehiclePersistence` (`VehiclePersistenceAdapter`), `VehicleEventPublisher` (`VehicleEventPublisherAdapter` / `VehicleChangedEventRelay`).

---

## 4. Eventos publicados y consumidos

- **Eventos publicados en RabbitMQ:**
  - **`VehicleChangedEvent`:** Publicado en la Exchange `parking-events-exchange` con routing key `vehicle-changed-v1` tras la creación, edición, baja lógica (RN-11) o reactivación de un vehículo (`VehicleEventPublisherAdapter`). Se procesa de forma segura tras la confirmación de transacción mediante `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- **Eventos consumidos de RabbitMQ:** Ninguno.

---

## 5. Variables de entorno

| Variable | Descripción | Valor por defecto (Dev) | Perfil / Uso |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot | `dev` | `dev` / `prod` |
| `SERVER_PORT` | Puerto de escucha HTTP del servicio | `8081` | Dev / Prod |
| `DB_HOST` | Host de la BD compartida de desarrollo | `localhost` | Dev |
| `DB_PORT` | Puerto de la BD compartida | `5432` | Dev |
| `DB_NAME` | Nombre de la BD de desarrollo | `parking_dev` | Dev |
| `DB_USER` | Usuario de la BD de desarrollo | `parking_dev` | Dev |
| `DB_PASSWORD` | Contraseña de la BD de desarrollo | `change.me` | Dev |
| `VEHICLE_DB_HOST` | Host de la BD dedicada de vehículos | `parking-vehicle-postgres` | Prod / Aislado |
| `VEHICLE_DB_PORT` | Puerto del host para la BD dedicada | `5433` (externo) / `5432` (interno) | Prod / Aislado |
| `VEHICLE_DB_NAME` | Nombre de la BD dedicada | `vehicle_db` | Prod / Aislado |
| `VEHICLE_DB_USER` | Usuario de la BD dedicada | `vehicle_user` | Prod / Aislado |
| `VEHICLE_DB_PASSWORD` | Contraseña de la BD dedicada | `vehicle_pass` | Prod / Aislado |
| `KEYCLOAK_ISSUER_URI` | URI del emisor de Keycloak (Issuer URI) | `http://localhost:8180/realms/parking` | Dev / Prod |

---

## 6. Ejecución de forma aislada

Para ejecutar y probar `vehicle-service` de forma independiente sin depender de otros microservicios:

### Opción 1: Entorno de Desarrollo (Perfil `dev`)
Conectándose al Postgres compartido (esquema `vehicle`):
```bash
cd backend/vehicle-service
mvn spring-boot:run
```

### Opción 2: Base de Datos Dedicada (Perfil `prod` / Contenedor Aislado)
1. Levantar el contenedor PostgreSQL dedicado en el puerto `5433`:
   ```bash
   cd backend/vehicle-service
   cp .env.example .env
   docker compose up -d
   ```
2. Ejecutar la aplicación Spring Boot activando el perfil `prod` para aplicar migraciones Flyway (`V1__init.sql`, `V2__add_version.sql`):
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## 7. Migraciones de Base de Datos (Flyway)

Las migraciones de base de datos se aplican automáticamente en perfil `prod`:
- `V1__init.sql`: Creación del esquema base `vehicle.vehicles` y tabla de vehículos.
- `V2__add_version.sql`: Adición de la columna `version` para el control de concurrencia optimista (`@Version`).

---

## 8. Escaneo de Seguridad (Trivy)

El pipeline de CI ejecuta la herramienta Trivy para el análisis de vulnerabilidades en la imagen Docker del microservicio.
