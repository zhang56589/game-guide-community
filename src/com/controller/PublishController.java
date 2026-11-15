package com.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.bean.Category;
import com.bean.User;
import com.service.CategoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PublishController implements Controller {

    private CategoryService categoryService;
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("loginUser");
        if (u == null) return new ModelAndView("redirect:/login");

        List<Category> categories = categoryService.findAll();
        ModelAndView mav = new ModelAndView("publish");
        mav.addObject("categories", categories);
        mav.addObject("loginUser", u);
        return mav;
    }
}
