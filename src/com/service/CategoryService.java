package com.service;

import java.util.List;
import com.bean.Category;

public interface CategoryService {
    Category       findById(Integer id);
    List<Category> findAll();
}
