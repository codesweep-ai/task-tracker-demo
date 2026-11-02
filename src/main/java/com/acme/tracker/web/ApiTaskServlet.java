package com.acme.tracker.web;

import com.acme.tracker.dao.TaskDao;
import com.acme.tracker.model.Task;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * GET /api/tasks[?status=DONE] returns a JSON read-back of task rows straight
 * from the DB, outside the UI path. JSON is hand-rolled (no Jackson) to keep
 * the dependency surface legacy-thin.
 */
public class ApiTaskServlet extends HttpServlet {

    private final TaskDao tasks = new TaskDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String status = req.getParameter("status");
        List<Task> rows = (status == null || status.isEmpty())
                ? tasks.findAll()
                : tasks.search(status, null, null);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.append('[');
        for (int i = 0; i < rows.size(); i++) {
            Task t = rows.get(i);
            if (i > 0) out.append(',');
            out.append('{')
               .append("\"id\":").append(String.valueOf(t.getId())).append(',')
               .append("\"title\":\"").append(esc(t.getTitle())).append("\",")
               .append("\"status\":\"").append(esc(t.getStatus())).append("\",")
               .append("\"priority\":\"").append(esc(t.getPriority())).append("\",")
               .append("\"assigneeId\":").append(t.getAssigneeId() == null ? "null" : String.valueOf(t.getAssigneeId())).append(',')
               .append("\"projectId\":").append(String.valueOf(t.getProjectId())).append(',')
               .append("\"dueDate\":").append(t.getDueDate() == null ? "null" : "\"" + esc(t.getDueDate()) + "\"").append(',')
               .append("\"updatedAt\":\"").append(esc(t.getUpdatedAt())).append("\"")
               .append('}');
        }
        out.append(']');
        out.flush();
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
