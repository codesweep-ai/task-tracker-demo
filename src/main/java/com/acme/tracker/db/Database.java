package com.acme.tracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Thin JDBC plumbing over an external PostgreSQL database: no connection pool,
 * no ORM, statics everywhere. Connection settings come from
 * the environment (DB_URL / DB_USER / DB_PASSWORD) with compose-friendly
 * defaults. The database owns its own schema (loaded by the Postgres container
 * at init time) — the app never creates tables.
 */
public final class Database {

    private static final String URL = env("DB_URL", "jdbc:postgresql://db:5432/tracker");
    private static final String USER = env("DB_USER", "tracker");
    private static final String PASSWORD = env("DB_PASSWORD", "tracker");

    static {
        // Old-school explicit driver registration.
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver not on classpath", e);
        }
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Block until the database accepts connections. Schema + seed are loaded by
     * the Postgres container, not here — this only waits for it to be ready so
     * the first request doesn't fail if the app raced ahead of the DB.
     */
    public static void awaitReady() {
        SQLException last = null;
        for (int attempt = 1; attempt <= 30; attempt++) {
            try (Connection c = getConnection(); Statement s = c.createStatement()) {
                s.execute("SELECT 1");
                return;
            } catch (SQLException e) {
                last = e;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        throw new RuntimeException("Database not reachable at " + URL, last);
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
