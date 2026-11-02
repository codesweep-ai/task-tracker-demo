# task-tracker

A small **project & task management** web app: raw Servlets + JSP + JDBC,
packaged as a WAR for Tomcat 9, backed by an external **PostgreSQL** database.
No Spring, no ORM — routing lives in `web.xml`, queries are literal SQL in the
DAOs, and business rules are enforced imperatively in the servlets.

It is old-school, but it is a real, working, self-contained application: it
builds, tests, and deploys entirely in containers, so nothing needs a JDK,
Maven, or Postgres on the host.

## Domain

| Entity   | Notable fields                                              | Lifecycle |
|----------|------------------------------------------------------------|-----------|
| Person   | username, full_name, role (`ADMIN/MANAGER/MEMBER/VIEWER`)   | — |
| Project  | name, description, state (`ACTIVE/ARCHIVED`), owner         | ACTIVE → ARCHIVED (forward-only) |
| Task     | title, description, status, priority, assignee, project, due | OPEN → IN_PROGRESS → PENDING_APPROVAL → DONE; DONE → OPEN |

Only `ADMIN`/`MANAGER` may approve or reject tasks awaiting approval.

## Requirements

Just **[Podman](https://podman.io/)** (v4+). The JDK 17 + Maven toolchain and
Postgres all run inside containers — the host stays clean. (You *can* build with
a local JDK 17 + Maven if you prefer; see below.)

## Build

Build the runnable image (multi-stage: Maven compiles the WAR, then it is
deployed onto Tomcat 9) — no host build tools involved:

```bash
podman build -t task-tracker:dev .
```

Just want the **WAR** artifact, still without touching the host dev env?

```bash
./build-war.sh        # -> target/task-tracker.war, built inside a container
```

Have a local JDK 17 + Maven and want to build on the host instead?

```bash
mvn package           # -> target/task-tracker.war
```

## Test

`run-tests.sh` spins up a throwaway Postgres, runs the JUnit suite against it in
a Maven container, and tears everything down — no host install, no residue:

```bash
./run-tests.sh
```

## Run / deploy

Deployment is a **`podman kube play`** manifest (`deploy/`) — the app and its
PostgreSQL as two containers in one Pod. First build the image (above), then:

```bash
podman kube play --configmap deploy/tracker-db-initdb-configmap.yaml deploy/task-tracker.yaml
```

- App: **http://localhost:25173/**
- Postgres: **localhost:25432** (db `tracker`, user `tracker`, pass `tracker`)

Tear it down with:

```bash
podman kube down deploy/task-tracker.yaml
```

Sign in with any seeded user (password = username): `admin`, `mgr`, `dev`,
`view`. Only `admin`/`mgr` can approve or reject tasks awaiting approval.

The Postgres data dir is an **in-memory** volume (`emptyDir: { medium: Memory }`),
so `kube play` re-runs `schema.sql` → `seed.sql` → `oracle-ro.sql` and gives a
fresh seed every time — nothing persists across a down/up.

### Configuration

The app reads its database connection from the environment (the manifest sets
these); the defaults match the in-Pod Postgres:

| Var           | Meaning     | Default                                |
|---------------|-------------|----------------------------------------|
| `DB_URL`      | JDBC URL    | `jdbc:postgresql://db:5432/tracker`    |
| `DB_USER`     | DB user     | `tracker`                              |
| `DB_PASSWORD` | DB password | `tracker`                              |

To point at an external database, run the `task-tracker:dev` image with those
set. The database owns its own schema — the app never creates tables; load
`src/main/resources/db/{schema,seed,oracle-ro}.sql` (in that order) into any
external DB before first use.

## Interfaces beyond the UI

- **HTTP JSON:** `GET /api/tasks[?status=DONE]` returns tasks straight from the
  DB as JSON.
- **Read-only DB login:** `oracle-ro.sql` creates an `oracle_ro` / `oracle_ro`
  role with `SELECT` on all tables (denied INSERT/UPDATE/DELETE) — handy for
  read-only reporting or verification without risking a mutation.

## CI

`.github/workflows/ci.yml` runs on every push/PR:

- **build-test** — JDK 17 + Maven against a Postgres service container: compiles,
  runs the JUnit suite, packages the WAR, and uploads it as an artifact.
- **container-build** — builds the image with `podman build` to keep the fully
  containerized build path honest.

## Layout

```
pom.xml                       WAR build, Java 8 bytecode (built on a JDK 17 toolchain)
Dockerfile                    multi-stage: maven build -> tomcat:9
build-war.sh                  build just the WAR, inside a container
run-tests.sh                  JUnit against an ephemeral Postgres
deploy/
  task-tracker.yaml           podman kube play Pod (app + postgres)
  tracker-db-initdb-configmap.yaml  schema/seed/oracle-ro as a ConfigMap (generated)
  gen-initdb-configmap.sh     regenerates that ConfigMap from src/main/resources/db
.github/workflows/ci.yml      build + test + containerized image build
src/main/resources/db/        schema.sql + seed.sql + oracle-ro.sql
src/main/java/com/acme/tracker/
  db/      Database (env-configured Postgres), BootstrapListener (waits for DB)
  model/   Person, Project, Task, Role, TaskStatus
  dao/     PersonDao, ProjectDao, TaskDao   (literal SQL)
  web/     AuthFilter + servlets
src/main/webapp/              styles.css, index.jsp, WEB-INF/ (web.xml + JSP views)
src/test/java/                LifecycleTest
```
