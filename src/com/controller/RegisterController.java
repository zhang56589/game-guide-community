package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class RegisterController implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        Object u = request.getSession().getAttribute("loginUser");
        if (u != null) return new ModelAndView("redirect:/");
        return new ModelAndView("register");
    }
}
