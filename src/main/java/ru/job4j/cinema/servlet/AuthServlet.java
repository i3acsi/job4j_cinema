package ru.job4j.cinema.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.cinema.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AuthServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession().invalidate();
        resp.sendRedirect("/job4j_cinema/");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("login".equals(action)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            if (!AuthService.checkAndSetCredentials(email, password, req)) {
                String msg = "wrong credentials: Email: " + email + ", Password: " + password;
                log.error(msg);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } else if ("reg".equals(action)) {
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            if (!AuthService.regAccount(name, email, password)) {
                String msg = "can't create a new account with these credentials: Name:" + name + ", Email: " + email + ", Password: " + password;
                log.error(msg);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        }
    }
}
