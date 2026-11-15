package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.bean.User;
import com.service.UserService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class AdminUserStatusController implements Controller {

    private UserService userService;
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("loginUser");
        if (u == null || !u.isAdmin()) return new ModelAndView("redirect:/");

        String idStr   = request.getParameter("id");
        String action  = request.getParameter("action"); // disable / enable / promote / demote
        if (idStr != null) {
            Integer id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                // BUG-3 修复：非法 id 忽略，与其他入口行为一致
                return new ModelAndView("redirect:/admin/user");
            }
            // BUG-2 修复：禁止管理员禁用/降级自己，防止唯一管理员自我锁死
            boolean self = id.equals(u.getId());
            if ("disable".equals(action) && !self)  userService.updateStatus(id, 0);
            if ("enable".equals(action))            userService.updateStatus(id, 1);
            if ("promote".equals(action))           userService.updateRole(id, 1);
            if ("demote".equals(action) && !self)   userService.updateRole(id, 0);
        }
        return new ModelAndView("redirect:/admin/user");
    }
}
