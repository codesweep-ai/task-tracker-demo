package com.acme.tracker;

import com.acme.tracker.dao.TaskDao;
import com.acme.tracker.db.Database;
import com.acme.tracker.model.Task;
import com.acme.tracker.model.TaskStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Exercises the task lifecycle.
 *
 * Runs against an ephemeral PostgreSQL (see run-tests.sh) whose schema + seed
 * are already loaded; each test creates its own task, so they don't collide.
 */
public class LifecycleTest {

    private final TaskDao tasks = new TaskDao();

    @Before
    public void setUp() {
        Database.awaitReady();
    }

    @Test
    public void testDirectOpenToDone() {
        Task t = new Task();
        t.setTitle("Spike: caching layer");
        t.setStatus(TaskStatus.OPEN);
        t.setPriority("LOW");
        t.setProjectId(1);
        long id = tasks.create(t);

        tasks.updateStatus(id, TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, tasks.findById(id).getStatus());
    }

    @Test
    public void testNormalFlow() {
        Task t = new Task();
        t.setTitle("Implement OIDC");
        t.setStatus(TaskStatus.OPEN);
        t.setPriority("HIGH");
        t.setProjectId(1);
        long id = tasks.create(t);

        tasks.updateStatus(id, TaskStatus.IN_PROGRESS);
        tasks.updateStatus(id, TaskStatus.PENDING_APPROVAL);
        tasks.updateStatus(id, TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, tasks.findById(id).getStatus());
    }
}
