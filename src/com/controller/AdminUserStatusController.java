package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        User u = (User) request.getSession().getAttribute("loginUser");
        if (u == null || !u.isAdmin()) return new ModelAndView("redirect:/");

        String idStr   = request.getParameter("id");
        String action  = request.getParameter("action"); // disable / enable / promote / demote
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                if ("disable".equals(action))  userService.updateStatus(id, 0);
                if ("enable".equals(action))   userService.updateStatus(id, 1);
                if ("promote".equals(action))  userService.updateRole(id, 1);
                if ("demote".equals(action))   userService.updateRole(id, 0);
            } catch (NumberFormatException ignored) {}
        }
        return new ModelAndView("redirect:/admin/user");
    }
}
