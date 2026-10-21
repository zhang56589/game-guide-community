package com.service.impl;

import java.util.List;
import com.bean.Category;
import com.dao.CategoryDao;
import com.service.CategoryService;

public class CategoryServiceImpl implements CategoryService {

    private CategoryDao categoryDao;

    public void setCategoryDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public Category findById(Integer id) {
        return categoryDao.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryDao.findAll();
    }
}
