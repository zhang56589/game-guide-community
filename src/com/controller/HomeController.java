package com.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

        // 1. 分类列表（导航栏始终需要）
        List<Category> categories = categoryService.findAll();
        mav.addObject("categories", categories);

        // 2. 搜索/分类筛选
        String keyword  = request.getParameter("keyword");
        String catIdStr = request.getParameter("category");
        Integer catId = null;
        if (catIdStr != null && !catIdStr.isEmpty()) {
            try {
                catId = Integer.parseInt(catIdStr);
            } catch (NumberFormatException ignored) {
                // BUG-3 修复：非法 category 参数按未筛选处理，与 DetailController 风格一致
            }
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            mav.addObject("keyword", keyword);
            List<Strategy> searchResult = strategyService.search(keyword, 1, 20);
            mav.addObject("searchList", searchResult);
        } else if (catId != null) {
            List<Strategy> list = strategyService.getByCategory(catId, 1, 20);
            mav.addObject("category", categoryService.findById(catId));
            mav.addObject("categoryList", list);
        } else {
            // 3. 最新攻略列表(10条)：PERF-1 修复，仅无搜索/筛选时才查询，避免无用 SQL
            List<Strategy> latest = strategyService.getLatest(1, 10);
            mav.addObject("strategyList", latest);
        }

        // 4. 当前用户（getSession(false) 返回 null 时不创建空 session）
        User loginUser = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            loginUser = (User) session.getAttribute("loginUser");
        }
        mav.addObject("loginUser", loginUser);

        return mav;
    }
}
