package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.bean.Strategy;
import com.bean.User;
import com.service.StrategyService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class DetailController implements Controller {

    private StrategyService strategyService;
    public void setStrategyService(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr = request.getParameter("id");
        ModelAndView mav;
        if (idStr == null) {
            return new ModelAndView("redirect:/");
        }
        Integer id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return new ModelAndView("redirect:/");
        }
        Strategy s = strategyService.getDetail(id);
        if (s == null || s.getStatus() != 1) {
            // BUG-5 修复：攻略不可见时返回 404，不再渲染缺数据的 home 视图
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "攻略不存在或已删除");
            return null;
        } else {
            mav = new ModelAndView("detail");
            mav.addObject("strategy", s);
        }
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        mav.addObject("loginUser", loginUser);
        return mav;
    }
}
