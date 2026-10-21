package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String idStr = request.getParameter("id");
        ModelAndView mav;
        if (idStr == null) {
            return new ModelAndView("redirect:/");
        }
        try {
            Integer id = Integer.parseInt(idStr);
            Strategy s = strategyService.getDetail(id);
            if (s == null || s.getStatus() != 1) {
                mav = new ModelAndView("home");
                mav.addObject("error", "攻略不存在或已删除");
            } else {
                mav = new ModelAndView("detail");
                mav.addObject("strategy", s);
            }
        } catch (NumberFormatException e) {
            return new ModelAndView("redirect:/");
        }
        User loginUser = (User) request.getSession().getAttribute("loginUser");
        mav.addObject("loginUser", loginUser);
        return mav;
    }
}
