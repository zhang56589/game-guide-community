package com.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.bean.Strategy;
import com.bean.User;
import com.service.StrategyService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class AdminStrategyController implements Controller {

    private StrategyService strategyService;
    public void setStrategyService(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        User u = (User) request.getSession().getAttribute("loginUser");
        if (u == null || !u.isAdmin()) return new ModelAndView("redirect:/");

        List<Strategy> list = strategyService.getAll();
        ModelAndView mav = new ModelAndView("admin_strategy");
        mav.addObject("strategies", list);
        mav.addObject("loginUser", u);
        return mav;
    }
}
