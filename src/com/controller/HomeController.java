package com.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.bean.Category;
import com.bean.Strategy;
import com.bean.User;
import com.service.CategoryService;
import com.service.StrategyService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class HomeController implements Controller {

    private StrategyService strategyService;
    private CategoryService categoryService;

    public void setStrategyService(StrategyService strategyService) {
        this.strategyService = strategyService;
    }
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("home");

        // 1. 最新攻略列表(10条)
        List<Strategy> latest = strategyService.getLatest(1, 10);
        mav.addObject("strategyList", latest);

        // 2. 分类列表
        List<Category> categories = categoryService.findAll();
        mav.addObject("categories", categories);

        // 3. 搜索/分类筛选
        String keyword  = request.getParameter("keyword");
        String catIdStr = request.getParameter("category");
        if (keyword != null && !keyword.trim().isEmpty()) {
            mav.addObject("keyword", keyword);
            List<Strategy> searchResult = strategyService.search(keyword, 1, 20);
            mav.addObject("searchList", searchResult);
        } else if (catIdStr != null && !catIdStr.isEmpty()) {
            try {
                Integer catId = Integer.parseInt(catIdStr);
                List<Strategy> list = strategyService.getByCategory(catId, 1, 20);
                mav.addObject("category", categoryService.findById(catId));
                mav.addObject("categoryList", list);
            } catch (NumberFormatException ignored) {}
        }

        // 4. 当前用户
        User loginUser = (User) request.getSession().getAttribute("loginUser");
        mav.addObject("loginUser", loginUser);

        return mav;
    }
}
