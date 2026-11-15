package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.bean.Strategy;
import com.bean.User;
import com.service.StrategyService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/** 用户删除自己的攻略 */
public class DeleteMyStrategyController implements Controller {

    private StrategyService strategyService;
    public void setStrategyService(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HttpSession session = request.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("loginUser");
        if (u == null) return new ModelAndView("redirect:/login");

        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                Strategy s = strategyService.getByIdRaw(id); // 不加审核过滤
                if (s != null && !s.getUserId().equals(u.getId())) {
                    // BUG-6 修复：Controller 层归属校验失败也返回 403，与 AuthFilter 行为一致
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "该攻略不属于您");
                    return null;
                }
                if (s != null) {
                    strategyService.delete(id);
                }
            } catch (NumberFormatException ignored) {
                // BUG-3 修复：非法 id 按未传处理，重定向回我的攻略
            }
        }
        return new ModelAndView("redirect:/my");
    }
}
