# tartis-recon-ai-parking — vehicle-service

## 1. Responsabilidad del microservicio

`vehicle-service` es el microservicio encargado del catálogo y gestión del dominio de **Vehículos** en el sistema de parking **TARTIS Recon-AI**. Sus responsabilidades principales incluyen:

- **Alta y Registro de Vehículos:** Registro de vehículos garantizando la unicidad de la matrícula y la validación estricta de las reglas de dominio (patrón de matrícula DGT española, categoría `CAR`, `CAR_PMR` o `MOTORBIKE`, marca, modelo, color, número de puertas o sidecar).
- **Consulta y Búsqueda:** Consulta paginada de vehículos, búsqueda por UUID y búsqueda por matrícula (utilizada síncronamente por `stay-service` durante el flujo de check-in).
- **Modificación:** Edición de atributos modificables del vehículo (la matrícula se mantiene inmutable).
- **Baja y Alta Lógica (RN-11):** Gestión del estado de activación (`active=true/false`). Según **RN-11**, los vehículos no se eliminan físicamente de la base de datos para preservar el historial de estancias. Un vehículo dado de baja lógica (`active=false`) tiene bloqueado el acceso al parking al intentar el check-in.
- **Emisión de Eventos de Dominio:** Publicación asíncrona del evento `VehicleChangedEvent` tras operaciones de creación, edición o cambio de estado.

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

Los casos de uso de la capa de aplicación orquestan las reglas de dominio utilizando los puertos de salida:

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

Para ejecutar y probar `vehicle-service` de forma independiente sin depender del resto de microservicios:

1. **Opción 1: Entorno de Desarrollo (Perfil `dev`)**
   Navegar a la carpeta del microservicio y arrancar con Maven:
   ```bash
   cd backend/vehicle-service
   mvn spring-boot:run
   ```
   *El servicio se conectará al esquema `vehicle` del Postgres compartido.*

2. **Opción 2: Base de Datos Dedicada (Perfil `prod` / Contenedores Aislados)**
   Para ejecutar contra una base de datos PostgreSQL exclusiva en puerto `5433`:
   ```bash
   cd backend/vehicle-service
   cp .env.example .env
   docker compose up -d
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## 7. Levantar el entorno local

Las herramientas compartidas (Postgres de dev con los 5 schemas, pgAdmin, SonarQube) viven en el repositorio de infraestructura [`tartis-recon-ia-parking-infra`](https://github.com/tartis-academy/tartis-recon-ia-parking-infra). Levántalas desde allí primero:

```bash
cd ../tartis-recon-ia-parking-infra
./setup.sh
```

Esto es lo único necesario para el desarrollo diario en perfil `dev`: `vehicle-service` se conecta al Postgres compartido utilizando el esquema `vehicle`.

El PostgreSQL DEDICADO de `vehicle-service` (database-per-service real, perfil `prod` o para levantar este servicio aislado) vive en `backend/vehicle-service`:

```bash
cd backend/vehicle-service
cp .env.example .env
docker compose up -d
```

Comprueba que el contenedor esté `healthy`:
```bash
docker compose ps
```

Para detener el contenedor (con `-v` borra además los datos de la BD):
```bash
docker compose down
```

---

## 8. Datos de conexión

| Dato | Valor |
|---|---|
| BD dedicada desde tu máquina | `localhost:5433` · `vehicle_db` · `vehicle_user` |
| BD dedicada desde pgAdmin | `parking-vehicle-postgres:5432` (nombre del contenedor, puerto interno) |

pgAdmin y SonarQube se levantan desde `tartis-recon-ia-parking-infra`.

---

## 9. Migraciones de base de datos (Flyway)

El esquema ya no se crea a mano ni con un `schema.sql` montado como init script: `V1__init.sql` (en `src/main/resources/db/migration`) es la baseline, y Flyway la aplica solo al arrancar la app contra la BD dedicada (perfil `prod`). En dev, Flyway está desactivado (`spring.flyway.enabled=false` en `application-dev.properties`): el Postgres compartido con 5 schemas sigue gestionado por `ddl-auto=update`, fuera del alcance de esta migración.

Para añadir un cambio de esquema: crea `V2__descripcion.sql` (nunca edites `V1__init.sql` una vez desplegado) en la misma carpeta, con el DDL nuevo. Flyway lo detecta y lo aplica en el siguiente arranque.

---

## 10. Escaneo de imagen (Trivy)

El job `docker-scan` de la CI construye la imagen final del Dockerfile y la escanea con [Trivy](https://trivy.dev/). El informe completo (`CRITICAL` + `HIGH`) se publica siempre en la pestaña **Security** del repo; solo una vulnerabilidad `CRITICAL` hace fallar el job.

Si una `CRITICAL` no tiene fix disponible todavía y hay que aceptar el riesgo de forma consciente, se ignora explícitamente añadiendo su CVE a un `.trivyignore` en la raíz del repo (no existe ninguno hoy).

---

## 11. Problemas frecuentes

- `network parking-shared ... not found` $\rightarrow$ te falta crear la red desde el repo de infra (`docker network create parking-shared`, o `./setup.sh` allí).
- Cambias el `.env` y no se entera $\rightarrow$ `docker compose up -d --force-recreate` (si tocas usuario o contraseña de Postgres, además `docker compose down -v`).
