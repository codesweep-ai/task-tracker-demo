package com.acme.tracker.web;

import com.acme.tracker.dao.PersonDao;
import com.acme.tracker.model.Person;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private final PersonDao people = new PersonDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        Person user = people.authenticate(username, password);
        if (user == null) {
            req.setAttribute("error", "Invalid credentials");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }
        req.getSession(true).setAttribute("user", user);
        resp.sendRedirect(req.getContextPath() + "/tasks");
    }
}
