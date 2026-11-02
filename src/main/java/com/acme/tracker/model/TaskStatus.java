package com.acme.tracker.model;

/**
 * Stringly-typed task statuses: compared with .equals all over the servlet
 * layer rather than being a real enum. The authoritative list of values lives
 * in the schema.sql CHECK constraint; this class is just a convenience holder
 * so the magic strings have one nominal home.
 */
public final class TaskStatus {

    public static final String OPEN = "OPEN";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String DONE = "DONE";

    private TaskStatus() {
    }
}
