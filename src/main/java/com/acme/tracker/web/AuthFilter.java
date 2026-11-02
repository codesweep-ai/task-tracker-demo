package com.acme.tracker.web;

import com.acme.tracker.model.Person;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet filter applied to incoming requests (mapped to /* in web.xml).
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getServletPath();
        String ctx = request.getContextPath();

        if (path.equals("/login") || path.startsWith("/api/") || path.endsWith(".css")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        Person user = session == null ? null : (Person) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(ctx + "/login");
            return;
        }

        if (path.equals("/tasks/action")) {
            String action = request.getParameter("action");
            if (("approve".equals(action) || "reject".equals(action)) && !user.getRole().canApprove()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Only managers or admins may approve or reject tasks");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
