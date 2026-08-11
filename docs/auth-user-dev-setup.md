# Auth/User development database

The Auth/User migrations must be developed and tested against the Docker
database, never directly against the datasource in `application.properties`.

## Clone the production schema safely

```bash
./scripts/clone-production-schema-to-dev.sh
```

The script:

1. reads the production connection only from the ignored
   `src/main/resources/application.properties`;
2. forces `default_transaction_read_only=on` for every production connection;
3. exports schema and Flyway history only (no application rows);
4. recreates only the explicitly named `quizcit_dev` database inside
   `quizcit-postgres-dev`;
5. imports the schema into PostgreSQL 18 + pgvector on `127.0.0.1:5433`.

The destructive `DROP DATABASE` in the script is executed through
`docker compose exec postgres-dev` and can therefore target only the local
container.

## Run the backend locally

```bash
docker compose -f compose.dev.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile overrides the datasource with
`jdbc:postgresql://127.0.0.1:5433/quizcit_dev`. Hibernate DDL mutation is
disabled; Flyway migrations are the only schema mutation mechanism.

## Stop the local database

```bash
docker compose -f compose.dev.yml down
```

Use `down -v` only when intentionally discarding the local development volume.
