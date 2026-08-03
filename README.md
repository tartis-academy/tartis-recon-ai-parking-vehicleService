# tartis-recon-ai-parking — vehicle-service

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
