package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.bean.User;
import com.service.UserService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class DoLoginController implements Controller {

    private UserService userService;
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        ModelAndView mav = new ModelAndView("login");
        if (username == null || password == null) {
            mav.addObject("error", "请填写完整信息");
            return mav;
        }
        User u = userService.login(username.trim(), password);
        if (u == null) {
            mav.addObject("error", "账号或密码错误，或账号已被禁用");
            return mav;
        }
        // 登录成功，放入 session
        request.getSession().setAttribute("loginUser", u);
        // 根据角色跳转
        if (u.isAdmin()) {
            return new ModelAndView("redirect:/admin");
        }
        return new ModelAndView("redirect:/");
    }
}
