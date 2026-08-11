#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
production_properties="$repo_root/src/main/resources/application.properties"
compose_file="$repo_root/compose.dev.yml"

if [[ ! -f "$production_properties" ]]; then
  echo "Không tìm thấy application.properties production." >&2
  exit 1
fi

production_url="$(sed -n 's/^spring.datasource.url=//p' "$production_properties")"
production_user="$(sed -n 's/^spring.datasource.username=//p' "$production_properties")"
production_password="$(sed -n 's/^spring.datasource.password=//p' "$production_properties")"

if [[ ! "$production_url" =~ ^jdbc:postgresql://([^:/?]+):([0-9]+)/([^?]+) ]]; then
  echo "JDBC URL production không đúng định dạng PostgreSQL mong đợi." >&2
  exit 1
fi

production_host="${BASH_REMATCH[1]}"
production_port="${BASH_REMATCH[2]}"
production_database="${BASH_REMATCH[3]}"

docker compose -f "$compose_file" up -d postgres-dev

until docker compose -f "$compose_file" exec -T postgres-dev \
  pg_isready -U quizcit_dev -d quizcit_dev >/dev/null 2>&1; do
  if [[ "$(docker inspect -f '{{.State.Running}}' quizcit-postgres-dev 2>/dev/null || true)" != "true" ]]; then
    echo "PostgreSQL dev đã dừng trước khi sẵn sàng." >&2
    docker logs quizcit-postgres-dev >&2 || true
    exit 1
  fi
  sleep 1
done

schema_dump="$(mktemp)"
flyway_dump="$(mktemp)"
trap 'rm -f "$schema_dump" "$flyway_dump"' EXIT

# This guard is intentionally passed to PostgreSQL itself: pg_dump may only
# open read-only transactions against production.
docker run --rm \
  -e "PGPASSWORD=$production_password" \
  -e "PGOPTIONS=-c default_transaction_read_only=on" \
  postgres:18-alpine \
  pg_dump \
    --host "$production_host" \
    --port "$production_port" \
    --username "$production_user" \
    --dbname "$production_database" \
    --schema-only \
    --no-owner \
    --no-privileges > "$schema_dump"

# Flyway history is schema metadata, not application data. Keeping it makes
# the cloned database start from exactly the same migration version.
docker run --rm \
  -e "PGPASSWORD=$production_password" \
  -e "PGOPTIONS=-c default_transaction_read_only=on" \
  postgres:18-alpine \
  pg_dump \
    --host "$production_host" \
    --port "$production_port" \
    --username "$production_user" \
    --dbname "$production_database" \
    --data-only \
    --table public.flyway_schema_history \
    --no-owner \
    --no-privileges > "$flyway_dump"

# Destructive operations below target only the explicitly named Docker service.
docker compose -f "$compose_file" exec -T postgres-dev \
  psql -v ON_ERROR_STOP=1 -U quizcit_dev -d postgres \
  -c "DROP DATABASE IF EXISTS quizcit_dev WITH (FORCE)" \
  -c "CREATE DATABASE quizcit_dev OWNER quizcit_dev"

docker compose -f "$compose_file" exec -T postgres-dev \
  psql -v ON_ERROR_STOP=1 -U quizcit_dev -d quizcit_dev < "$schema_dump"

docker compose -f "$compose_file" exec -T postgres-dev \
  psql -v ON_ERROR_STOP=1 -U quizcit_dev -d quizcit_dev < "$flyway_dump"

echo "Đã clone schema production sang PostgreSQL Docker local tại 127.0.0.1:5433."
