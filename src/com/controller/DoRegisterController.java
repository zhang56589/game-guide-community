package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.bean.User;
import com.service.UserService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class DoRegisterController implements Controller {

    private UserService userService;
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String confirm  = request.getParameter("confirm");

        ModelAndView mav = new ModelAndView("register");

        if (username == null || password == null || nickname == null || confirm == null) {
            mav.addObject("error", "请填写完整信息");
            return mav;
        }
        if (!password.equals(confirm)) {
            mav.addObject("error", "两次密码不一致");
            mav.addObject("username", username);
            mav.addObject("nickname", nickname);
            return mav;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setNickname(nickname.trim());

        String error = userService.register(user);
        if (error != null) {
            mav.addObject("error", error);
            mav.addObject("username", username);
            mav.addObject("nickname", nickname);
            return mav;
        }
        // 注册成功自动登录
        User loginUser = userService.login(username.trim(), password);
        request.getSession().setAttribute("loginUser", loginUser);
        return new ModelAndView("redirect:/");
    }
}
