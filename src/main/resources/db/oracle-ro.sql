-- Read-only role (SELECT only; never mutates).
-- Loaded by the Postgres container AFTER the schema/tables and seed
-- (docker-entrypoint-initdb.d runs files in filename order: 01-schema, 02-seed,
-- 03-oracle-ro), so GRANT ... ON ALL TABLES sees the tables that already exist.
-- Recreated on every fresh `compose up`. Does NOT touch the app's own `tracker`
-- owner role, the schema, or the seed data.

CREATE ROLE oracle_ro LOGIN PASSWORD 'oracle_ro';
GRANT CONNECT ON DATABASE tracker TO oracle_ro;
GRANT USAGE ON SCHEMA public TO oracle_ro;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO oracle_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO oracle_ro;
