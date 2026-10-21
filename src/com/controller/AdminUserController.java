package com.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.bean.User;
import com.service.UserService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class AdminUserController implements Controller {

    private UserService userService;
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        User u = (User) request.getSession().getAttribute("loginUser");
        if (u == null || !u.isAdmin()) return new ModelAndView("redirect:/");

        List<User> users = userService.findAll();
        ModelAndView mav = new ModelAndView("admin_user");
        mav.addObject("users", users);
        mav.addObject("loginUser", u);
        return mav;
    }
}
