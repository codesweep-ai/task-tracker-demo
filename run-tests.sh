#!/usr/bin/env bash
# Run the JUnit tests against an EPHEMERAL PostgreSQL — spun up just for this
# run and destroyed after (no data persists). Nothing is installed on the host:
# the build happens inside a maven container, so there's no ~/.m2 or target/
# pollution either.
set -euo pipefail
cd "$(dirname "$0")"

NET=tt-test-net
DB=tt-test-db

cleanup() {
  podman rm -f "$DB" >/dev/null 2>&1 || true
  podman network rm "$NET" >/dev/null 2>&1 || true
}
trap cleanup EXIT
cleanup  # clear anything left over from a previous run

podman network create "$NET" >/dev/null

echo ">> starting ephemeral Postgres (schema + seed via initdb)..."
podman run -d --name "$DB" --network "$NET" \
  -e POSTGRES_DB=tracker -e POSTGRES_USER=tracker -e POSTGRES_PASSWORD=tracker \
  -v "$PWD/src/main/resources/db/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro,z" \
  -v "$PWD/src/main/resources/db/seed.sql:/docker-entrypoint-initdb.d/02-seed.sql:ro,z" \
  docker.io/library/postgres:16 >/dev/null

# Bail out loudly, with the DB's own log, instead of pressing on into the test
# run — a dead Postgres otherwise resurfaces much later as a misleading
# "UnknownHostException: tt-test-db" from the test JVM (the container's name
# stops resolving once it exits).
db_failed() {
  echo ">> ERROR: $1" >&2
  echo ">> last 30 lines of the $DB log:" >&2
  podman logs --tail 30 "$DB" >&2 2>&1 || true
  exit 1
}

echo ">> waiting for Postgres to be ready..."
ready=
for _ in $(seq 1 30); do
  # A failing initdb script (e.g. a mount postgres cannot read) makes the
  # entrypoint exit, so confirm the container is still alive before polling it.
  if [ "$(podman inspect -f '{{.State.Running}}' "$DB" 2>/dev/null)" != "true" ]; then
    db_failed "Postgres exited before becoming ready (initdb likely failed)."
  fi
  if podman exec "$DB" pg_isready -U tracker -d tracker >/dev/null 2>&1; then
    ready=1
    break
  fi
  sleep 1
done
[ -n "$ready" ] || db_failed "Postgres did not become ready within 30s."

echo ">> running tests..."
podman run --rm --network "$NET" -v "$PWD":/src:ro,z \
  -e DB_URL="jdbc:postgresql://$DB:5432/tracker" \
  -e DB_USER=tracker -e DB_PASSWORD=tracker \
  docker.io/library/maven:3.9-eclipse-temurin-17 \
  sh -c 'mkdir -p /work && cp -r /src/pom.xml /src/src /work/ && cd /work && mvn test'

echo ">> tests passed."
