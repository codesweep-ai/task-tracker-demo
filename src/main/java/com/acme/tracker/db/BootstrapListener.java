package com.acme.tracker.db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/** Waits for the external database to be reachable on startup. Wired in web.xml. */
public class BootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Schema + seed are owned by the Postgres container; just wait for it.
        Database.awaitReady();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }
}
