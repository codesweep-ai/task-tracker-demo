package com.acme.tracker.model;

/**
 * The user roles. The same four values are also declared as a CHECK constraint
 * in schema.sql (person.role), so they are spelled out in two places.
 */
public enum Role {
    ADMIN,
    MANAGER,
    MEMBER,
    VIEWER;

    /** Whether this role may approve or reject tasks. */
    public boolean canApprove() {
        return this == ADMIN || this == MANAGER;
    }
}
