package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.bean.User;
import com.service.StrategyService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class AdminStrategyDeleteController implements Controller {

    private StrategyService strategyService;
    public void setStrategyService(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        User u = (User) request.getSession().getAttribute("loginUser");
        if (u == null || !u.isAdmin()) return new ModelAndView("redirect:/");

        String idStr = request.getParameter("id");
        if (idStr != null) {
            try { strategyService.adminDelete(Integer.parseInt(idStr)); }
            catch (NumberFormatException ignored) {}
        }
        return new ModelAndView("redirect:/admin/strategy");
    }
}
