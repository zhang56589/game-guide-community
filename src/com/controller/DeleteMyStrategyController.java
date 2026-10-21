package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        User u = (User) request.getSession().getAttribute("loginUser");
        if (u == null) return new ModelAndView("redirect:/login");

        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                Strategy s = strategyService.getByIdRaw(id); // 不加审核过滤
                if (s != null && s.getUserId().equals(u.getId())) {
                    strategyService.delete(id);
                }
            } catch (NumberFormatException ignored) {}
        }
        return new ModelAndView("redirect:/my");
    }
}
