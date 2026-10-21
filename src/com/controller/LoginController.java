package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/** 登录页面跳转 */
public class LoginController implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        // 如果已登录，直接跳首页
        Object u = request.getSession().getAttribute("loginUser");
        if (u != null) {
            return new ModelAndView("redirect:/");
        }
        return new ModelAndView("login");
    }
}
