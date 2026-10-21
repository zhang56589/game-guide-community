package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bean.User;
import com.service.StrategyService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * 管理员审核攻略 Controller
 * 路径：/admin/strategy/audit
 * 参数：id + action(approve/reject)
 */
public class AdminStrategyAuditController implements Controller {

    private StrategyService strategyService;
    public void setStrategyService(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("loginUser");
        if (u == null || u.getRole() != 1) return new ModelAndView("redirect:/");

        String idStr   = request.getParameter("id");
        String action  = request.getParameter("action");
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                if ("approve".equals(action)) {
                    strategyService.auditApprove(id);
                } else if ("reject".equals(action)) {
                    strategyService.auditReject(id);
                }
            } catch (NumberFormatException ignored) {}
        }
        return new ModelAndView("redirect:/admin/strategy");
    }
}
