package com.acme.tracker.web;

import com.acme.tracker.dao.PersonDao;
import com.acme.tracker.dao.ProjectDao;
import com.acme.tracker.model.Person;
import com.acme.tracker.model.Project;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Lists projects, creates new ones, and archives an existing one.
 */
public class ProjectServlet extends HttpServlet {

    private final ProjectDao projects = new ProjectDao();
    private final PersonDao people = new PersonDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("projects", projects.findAll());
        req.setAttribute("people", people.findAll());
        req.getRequestDispatcher("/WEB-INF/jsp/projects.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if ("archive".equals(action)) {
            projects.archive(Long.parseLong(req.getParameter("id")));
            resp.sendRedirect(req.getContextPath() + "/projects");
            return;
        }
        // create
        String name = req.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project name is required");
            return;
        }
        Person user = (Person) req.getSession().getAttribute("user");
        Project p = new Project();
        p.setName(name.trim());
        p.setDescription(req.getParameter("description"));
        p.setOwnerId(user.getId());
        projects.create(p);
        resp.sendRedirect(req.getContextPath() + "/projects");
    }
}
