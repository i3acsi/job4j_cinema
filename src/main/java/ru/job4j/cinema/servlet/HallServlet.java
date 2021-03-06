package ru.job4j.cinema.servlet;

import ru.job4j.cinema.dto.PlacesDto;
import ru.job4j.cinema.model.User;
import ru.job4j.cinema.service.Mapper;
import ru.job4j.cinema.store.PsqlStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HallServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        List<PlacesDto> list = (List<PlacesDto>) PsqlStore.instOf().findAllPlacesDtoInHall(1, user.getId());
        String string = Mapper.toJson(list);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("json");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8));
        writer.write(string);
        writer.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean res = false;
        String action = req.getParameter("action");
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("json");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8));

        if ("select".equals(action)) {
            String[] params = req.getParameter("id").split("\\.");
            int row = Integer.parseInt(params[0]);
            int col = Integer.parseInt(params[1]);
            if (PsqlStore.instOf().processPlace(1, row, col, true, user.getId())) {
                res = true;
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else if ("unselect".equals(action)) {
            String[] params = req.getParameter("id").split("\\.");
            int row = Integer.parseInt(params[0]);
            int col = Integer.parseInt(params[1]);
            if (PsqlStore.instOf().processPlace(1, row, col, false, user.getId())) {
                res = true;
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else if ("confirmOrder".equals(action)) {
            List<int[]> list = PsqlStore.instOf().makeOrder(1, user.getId());
            writer.write(Mapper.toJson(list));
            writer.flush();
        } else if ("buy".equals(action)) {
            if (PsqlStore.instOf().doBuy(1, user.getId())) {
                res = true;
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        if (res) {
            writer.write(Mapper.toJson("OK"));
            writer.flush();
        }
    }
}
