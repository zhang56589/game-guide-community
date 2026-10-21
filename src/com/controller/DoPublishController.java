package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.bean.Strategy;
import com.bean.User;
import com.service.StrategyService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class DoPublishController implements Controller {

    private StrategyService strategyService;
    public void setStrategyService(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        User u = (User) request.getSession().getAttribute("loginUser");
        if (u == null) return new ModelAndView("redirect:/login");

        String title      = request.getParameter("title");
        String gameName   = request.getParameter("gameName");
        String content    = request.getParameter("content");
        String coverImage = request.getParameter("coverImage");
        String catIdStr   = request.getParameter("categoryId");

        Strategy s = new Strategy();
        s.setTitle(title);
        s.setGameName(gameName);
        s.setContent(content);
        s.setCoverImage(coverImage == null ? null : coverImage.trim());
        if (catIdStr != null && !catIdStr.isEmpty()) {
            try { s.setCategoryId(Integer.parseInt(catIdStr)); }
            catch (NumberFormatException ignored) {}
        }

        String error = strategyService.publish(s, u.getId());
        if (error != null) {
            ModelAndView mav = new ModelAndView("publish");
            mav.addObject("error", error);
            mav.addObject("strategy", s);
            return mav;
        }
        return new ModelAndView("redirect:/my");
    }
}
