package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class LogoutController implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        // BUG-4 修复：仅允许 POST 退出；GET /logout 不再销毁会话（防 CSRF 强制退出）
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new ModelAndView("redirect:/");
        }
        // 不新建无谓 session，无 session 直接视为已退出
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return new ModelAndView("redirect:/");
    }
}
