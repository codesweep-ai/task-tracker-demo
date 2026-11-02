package com.acme.tracker.web;

import com.acme.tracker.dao.PersonDao;
import com.acme.tracker.dao.ProjectDao;
import com.acme.tracker.dao.TaskDao;
import com.acme.tracker.model.Task;
import com.acme.tracker.model.TaskStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Creates and edits tasks. GET renders the form (blank for new, pre-filled
 * when ?id= is present); POST validates and persists.
 */
public class TaskFormServlet extends HttpServlet {

    private final TaskDao tasks = new TaskDao();
    private final ProjectDao projects = new ProjectDao();
    private final PersonDao people = new PersonDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            req.setAttribute("task", tasks.findById(Long.parseLong(idParam)));
        }
        req.setAttribute("projects", projects.findAll());
        req.setAttribute("people", people.findAll());
        req.getRequestDispatcher("/WEB-INF/jsp/task-form.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        String title = trim(req.getParameter("title"));
        String projectIdParam = req.getParameter("projectId");

        // The title must be present (mirrors NOT NULL on task.title).
        if (title.isEmpty()) {
            fail(req, resp, "Title is required");
            return;
        }
        // The referenced project must exist; checked here as well as by the FK.
        if (projectIdParam == null || projectIdParam.isEmpty() || !projects.exists(Long.parseLong(projectIdParam))) {
            fail(req, resp, "A task must belong to an existing project");
            return;
        }

        Task t = new Task();
        t.setTitle(title);
        t.setDescription(req.getParameter("description"));
        t.setPriority(orDefault(req.getParameter("priority"), "MEDIUM"));
        t.setProjectId(Long.parseLong(projectIdParam));
        String assignee = req.getParameter("assigneeId");
        t.setAssigneeId((assignee == null || assignee.isEmpty()) ? null : Long.valueOf(assignee));
        t.setDueDate(req.getParameter("dueDate"));

        long savedId;
        if (idParam == null || idParam.isEmpty()) {
            t.setStatus(TaskStatus.OPEN); // new tasks always start OPEN
            savedId = tasks.create(t);
        } else {
            savedId = Long.parseLong(idParam);
            t.setId(savedId);
            tasks.update(t);
        }
        // Land on the just-saved task's detail page, not back on the list.
        resp.sendRedirect(req.getContextPath() + "/tasks/form?id=" + savedId + "&saved=1");
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String msg)
            throws ServletException, IOException {
        req.setAttribute("error", msg);
        req.setAttribute("projects", projects.findAll());
        req.setAttribute("people", people.findAll());
        req.getRequestDispatcher("/WEB-INF/jsp/task-form.jsp").forward(req, resp);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String orDefault(String s, String def) {
        return (s == null || s.isEmpty()) ? def : s;
    }
}
