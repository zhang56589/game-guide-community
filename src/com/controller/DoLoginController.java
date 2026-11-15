package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;
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
        // SEC-4 暴力破解防护：先查锁定状态
        if (userService.isLoginLocked(username.trim())) {
            mav.addObject("error", "该账号已被锁定，请 15 分钟后再试");
            return mav;
        }
        User u = userService.login(username.trim(), password);
        if (u == null) {
            mav.addObject("error", "账号或密码错误，或账号已被禁用");
            return mav;
        }
        // 登录成功，销毁旧 session（防 Session 固定攻击），创建新 session 再写入 loginUser
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) oldSession.invalidate();
        HttpSession newSession = request.getSession(true);
        // BUG-1 修复：旧 session 销毁后 CSRF token 随之丢失，新 session 需补种，否则首个 POST 必 403
        newSession.setAttribute("_csrf", UUID.randomUUID().toString());
        newSession.setAttribute("loginUser", u);
        // 根据角色跳转
        if (u.isAdmin()) {
            return new ModelAndView("redirect:/admin");
        }
        return new ModelAndView("redirect:/");
    }
}
