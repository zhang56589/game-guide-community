package com.dao;

import java.util.List;
import com.bean.Category;

public interface CategoryDao {
    Category       findById(Integer id);
    List<Category> findAll();
}
