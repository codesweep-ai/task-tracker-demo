package com.acme.tracker.web;

import com.acme.tracker.dao.TaskDao;
import com.acme.tracker.model.Person;
import com.acme.tracker.model.Task;
import com.acme.tracker.model.TaskStatus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Changes a task's status, and closes several tasks at once. Each action
 * checks the task's current status is the one it expects before writing the new
 * one.
 */
public class TaskActionServlet extends HttpServlet {

    private final TaskDao tasks = new TaskDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");

        if ("bulkClose".equals(action)) {
            handleBulkClose(req, resp);
            return;
        }

        long id = Long.parseLong(req.getParameter("id"));
        Task t = tasks.findById(id);
        if (t == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No such task");
            return;
        }
        String current = t.getStatus();

        switch (action == null ? "" : action) {
            case "start":
                if (!guard(resp, current, TaskStatus.OPEN, action)) return;
                tasks.updateStatus(id, TaskStatus.IN_PROGRESS);
                break;
            case "submit":
                if (!guard(resp, current, TaskStatus.IN_PROGRESS, action)) return;
                tasks.updateStatus(id, TaskStatus.PENDING_APPROVAL);
                break;
            case "approve":
                if (!requireApprover(req, resp)) return;
                if (!guard(resp, current, TaskStatus.PENDING_APPROVAL, action)) return;
                tasks.updateStatus(id, TaskStatus.DONE);
                break;
            case "reject":
                if (!requireApprover(req, resp)) return;
                if (!guard(resp, current, TaskStatus.PENDING_APPROVAL, action)) return;
                tasks.updateStatus(id, TaskStatus.IN_PROGRESS);
                break;
            case "reopen":
                if (!guard(resp, current, TaskStatus.DONE, action)) return;
                tasks.updateStatus(id, TaskStatus.OPEN);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
                return;
        }
        if (!resp.isCommitted()) {
            // Optional: return to the focused task detail page (used by the
            // detail view's transition buttons). Default stays the list.
            String target = "detail".equals(req.getParameter("return"))
                    ? "/tasks/form?id=" + id
                    : "/tasks";
            resp.sendRedirect(req.getContextPath() + target);
        }
    }

    private void handleBulkClose(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] raw = req.getParameterValues("ids");
        if (raw == null || raw.length == 0) {
            resp.sendRedirect(req.getContextPath() + "/tasks");
            return;
        }
        long[] ids = new long[raw.length];
        for (int i = 0; i < raw.length; i++) {
            ids[i] = Long.parseLong(raw[i]);
        }
        tasks.bulkClose(ids);
        resp.sendRedirect(req.getContextPath() + "/tasks");
    }

    /** Authorization check for the approve and reject actions. */
    private boolean requireApprover(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Person user = (Person) req.getSession().getAttribute("user");
        if (user == null || !user.getRole().canApprove()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Approval requires a manager or admin");
            return false;
        }
        return true;
    }

    /** Refuse the change unless the task is already in the expected status. */
    private boolean guard(HttpServletResponse resp, String current, String expected, String action)
            throws IOException {
        if (!expected.equals(current)) {
            resp.sendError(HttpServletResponse.SC_CONFLICT,
                    "Cannot " + action + " a task in status " + current + " (expected " + expected + ")");
            return false;
        }
        return true;
    }
}
