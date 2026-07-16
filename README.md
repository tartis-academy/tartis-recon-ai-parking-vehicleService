# tartis-recon-ai-parking — vehicle-service

## Levantar el entorno local

Lo más rápido: un script que hace todos los pasos de abajo (red, `.env`, contenedores) y espera a que estén listos.

```bash
./setup.sh              # levanta todo
./setup.sh down         # para los contenedores
./setup.sh clean        # para y BORRA los datos de la BD
```

Si prefieres ir a mano, los pasos son estos.

Crea la red compartida que conecta pgAdmin con los Postgres de cada servicio (solo la primera vez).

```bash
docker network create parking-shared
```

Levanta las herramientas compartidas: pgAdmin en el 5050 y SonarQube en el 9000.

```bash
cp .env.example .env
docker compose up -d
```

Levanta el PostgreSQL dedicado de vehicle-service en el puerto 5433.

```bash
cd backend/vehicle-service
cp .env.example .env
docker compose up -d
```

Comprueba que los contenedores estén `healthy`.

```bash
docker compose ps
```

Para los contenedores (con `-v` además borra los datos de la BD).

```bash
docker compose down
```

## Datos de conexión

| Dato | Valor |
|---|---|
| pgAdmin | http://localhost:5050 (usuario y contraseña de tu `.env`) |
| SonarQube | http://localhost:9000 (`admin`/`admin`) |
| BD desde tu máquina | `localhost:5433` · `vehicle_db` · `vehicle_user` |
| BD desde pgAdmin | `parking-vehicle-postgres:5432` (nombre del contenedor, puerto interno) |

## Problemas frecuentes

`network parking-shared ... not found` → te falta el primer comando.

SonarQube arranca y se muere → `sudo sysctl -w vm.max_map_count=262144`.

pgAdmin reinicia en bucle → tu `PGADMIN_EMAIL` no es válido; pgAdmin rechaza dominios reservados como `.local`.

Cambias el `.env` y no se entera → `docker compose up -d --force-recreate` (si tocas usuario o contraseña de Postgres, además `docker compose down -v`).
