# tartis-recon-ai-parking — vehicle-service

## Responsabilidad del microservicio

`vehicle-service` es el microservicio encargado del catálogo y gestión del dominio de **Vehículos** en el sistema de parking. Sus responsabilidades principales incluyen:
- **Alta y Registro:** Registro de vehículos comprobando la unicidad de la matrícula y validando coherencia de datos según el dominio (tipo de vehículo `CAR`, `CAR_PMR` o `MOTORBIKE`, marca, modelo, color, número de puertas o sidecar).
- **Consulta y Búsqueda:** Consulta detallada por UUID o búsqueda por matrícula (usado síncronamente por `stay-service` durante el check-in).
- **Modificación:** Actualización de datos editables del vehículo (la matrícula es inmutable).
- **Baja Lógica / Desactivación:** Gestión del estado de activación (`active=true/false`). Según **RN-11**, los vehículos no se eliminan físicamente de la base de datos para preservar el historial; un vehículo desactivado (`active=false`) tiene denegada la entrada en el parking al intentar el check-in.

## Endpoints expuestos

Todos los endpoints requieren autenticación mediante Bearer Token (Access Token emitido por Keycloak), exceptuando el endpoint público de salud.

| Método | Endpoint | Descripción | Roles Autorizados |
|---|---|---|---|
| `GET` | `/v1/vehicles` | Listado paginado de vehículos (filtros por `active` y `type`) | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/vehicles` | Registra un nuevo vehículo en el catálogo | `ADMIN`, `OPERARIO` |
| `GET` | `/v1/vehicles/{id}` | Obtiene el detalle de un vehículo por su UUID | `ADMIN`, `OPERARIO` |
| `PUT` | `/v1/vehicles/{id}` | Actualiza los datos editables de un vehículo | `ADMIN`, `OPERARIO` |
| `GET` | `/v1/vehicles/plate/{plate}` | Consulta un vehículo por su matrícula | `ADMIN`, `OPERARIO` |
| `PATCH` | `/v1/vehicles/{id}/status` | Modifica el estado de activación (alta / baja lógica según RN-11) | `ADMIN` |
| `PATCH` | `/v1/vehicles/{id}/activate` | Activa explícitamente un vehículo desactivado | `ADMIN` |
| `GET` | `/actuator/health` | Probes de salud del servicio (Liveness / Readiness) | Público |

## Eventos publicados y consumidos

Este microservicio opera bajo un modelo de comunicación REST síncrono.
- **Eventos publicados en RabbitMQ:** Ninguno.
- **Eventos consumidos de RabbitMQ:** Ninguno.

## Variables de entorno

| Variable | Descripción | Valor por defecto (Dev) | Perfil / Uso |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot | `dev` | `dev` / `prod` |
| `DB_HOST` | Host de la BD compartida de desarrollo | `localhost` | Dev |
| `DB_PORT` | Puerto de la BD compartida | `5432` | Dev |
| `DB_NAME` | Nombre de la BD de desarrollo | `parking_dev` | Dev |
| `DB_USER` | Usuario de la BD de desarrollo | `parking_dev` | Dev |
| `DB_PASSWORD` | Contraseña de la BD de desarrollo | `change.me` | Dev |
| `VEHICLE_DB_HOST` | Host de la BD dedicada de vehículos | — | Prod / Aislado |
| `VEHICLE_DB_PORT` | Puerto de la BD dedicada | `5432` | Prod / Aislado |
| `VEHICLE_DB_NAME` | Nombre de la BD dedicada | `vehicle_db` | Prod / Aislado |
| `VEHICLE_DB_USER` | Usuario de la BD dedicada | — | Prod / Aislado |
| `VEHICLE_DB_PASSWORD` | Contraseña de la BD dedicada | — | Prod / Aislado |
| `KEYCLOAK_ISSUER_URI` | URI del emisor de Keycloak (Issuer URI) | `http://localhost:8180/realms/parking` | Dev / Prod |

## Ejecución de forma aislada

Para ejecutar y probar `vehicle-service` de forma independiente sin depender del resto de microservicios:

1. **Opción 1: Entorno de Desarrollo (Perfil `dev`)**
   Navegar a la carpeta del microservicio y arrancar con Maven:
   ```bash
   cd backend/vehicle-service
   mvn spring-boot:run
   ```
   *El servicio se conectará al esquema `vehicle` del Postgres compartido.*

2. **Opción 2: Base de Datos Dedicada (Perfil `prod` / Contenedores Aislados)**
   Para ejecutar contra una base de datos PostgreSQL exclusiva (Database-per-service):
   ```bash
   cd backend/vehicle-service
   cp .env.example .env
   docker compose up -d
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

## Levantar el entorno local

Las herramientas compartidas (Postgres de dev con los 5 schemas, pgAdmin,
SonarQube) ya NO viven en este repo: están en
[`tartis-recon-ia-parking-infra`](https://github.com/tartis-academy/tartis-recon-ia-parking-infra).
Levántalas desde allí primero:

```bash
cd ../tartis-recon-ia-parking-infra
./setup.sh
```

Esto es lo único que hace falta para el día a día en dev: vehicle-service se
conecta al Postgres compartido usando el schema `vehicle`.

El PostgreSQL DEDICADO de vehicle-service (database-per-service real, perfil
`prod` o para levantar este servicio aislado) sí vive aquí, en
`backend/vehicle-service`:

```bash
cd backend/vehicle-service
cp .env.example .env
docker compose up -d
```

Comprueba que el contenedor esté `healthy`.

```bash
docker compose ps
```

Para el contenedor (con `-v` además borra los datos de la BD).

```bash
docker compose down
```

## Datos de conexión

| Dato | Valor |
|---|---|
| BD dedicada desde tu máquina | `localhost:5433` · `vehicle_db` · `vehicle_user` |
| BD dedicada desde pgAdmin | `parking-vehicle-postgres:5432` (nombre del contenedor, puerto interno) |

pgAdmin y SonarQube se levantan desde `tartis-recon-ia-parking-infra`.

## Migraciones de base de datos (Flyway)

El esquema ya no se crea a mano ni con un `schema.sql` montado como init
script: `V1__init.sql` (en `src/main/resources/db/migration`) es la baseline,
y Flyway la aplica solo al arrancar la app contra la BD dedicada (perfil
`prod`). En dev, Flyway está desactivado (`spring.flyway.enabled=false` en
`application-dev.properties`): el Postgres compartido con 5 schemas sigue
gestionado por `ddl-auto=update`, fuera del alcance de esta migración.

Para añadir un cambio de esquema: crea `V2__descripcion.sql` (nunca edites
`V1__init.sql` una vez desplegado) en la misma carpeta, con el DDL nuevo.
Flyway lo detecta y lo aplica en el siguiente arranque.

## Escaneo de imagen (Trivy)

El job `docker-scan` de la CI construye la imagen final del Dockerfile y la
escanea con [Trivy](https://trivy.dev/). El informe completo (`CRITICAL` +
`HIGH`) se publica siempre en la pestaña **Security** del repo; solo una
vulnerabilidad `CRITICAL` hace fallar el job.

Si una `CRITICAL` no tiene fix disponible todavía y hay que aceptar el riesgo
de forma consciente, se ignora explícitamente añadiendo su CVE a un
`.trivyignore` en la raíz del repo (no existe ninguno hoy).

## Problemas frecuentes

`network parking-shared ... not found` → te falta crear la red desde el repo
de infra (`docker network create parking-shared`, o `./setup.sh` allí).

Cambias el `.env` y no se entera → `docker compose up -d --force-recreate` (si tocas usuario o contraseña de Postgres, además `docker compose down -v`).
