package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.bean.User;
import com.service.StrategyService;
import com.service.UserService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class AdminHomeController implements Controller {

    private UserService     userService;
    private StrategyService strategyService;

    public void setUserService(UserService userService)             { this.userService = userService; }
    public void setStrategyService(StrategyService strategyService) { this.strategyService = strategyService; }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("loginUser");
        if (u == null || !u.isAdmin()) return new ModelAndView("redirect:/");

        ModelAndView mav = new ModelAndView("admin_home");
        mav.addObject("userCount", userService.count());
        mav.addObject("strategyCount", strategyService.count());
        mav.addObject("loginUser", u);
        return mav;
    }
}
