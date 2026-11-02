package com.acme.tracker.web;

import com.acme.tracker.dao.TaskDao;
import com.acme.tracker.model.Task;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Serves the task list. With no filter params it shows every task; the
 * status and assignee params narrow it down.
 */
public class TaskListServlet extends HttpServlet {

    private final TaskDao tasks = new TaskDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status = req.getParameter("status");
        String assigneeParam = req.getParameter("assignee");
        String sort = req.getParameter("sort");

        // Route everything through search — with null filters it returns all
        // tasks, so the chosen sort applies to the default list too. Default
        // (no sort param) is most-recently-updated first.
        Long assigneeId = (assigneeParam == null || assigneeParam.isEmpty()) ? null : Long.valueOf(assigneeParam);
        List<Task> result = tasks.search(status, assigneeId, sort);

        req.setAttribute("tasks", result);
        req.setAttribute("statusFilter", status);
        req.setAttribute("sortBy", (sort == null || sort.isEmpty()) ? "updated" : sort);
        req.getRequestDispatcher("/WEB-INF/jsp/tasks.jsp").forward(req, resp);
    }
}
