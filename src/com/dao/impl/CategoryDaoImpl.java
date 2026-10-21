package com.dao.impl;

import java.util.List;
import com.bean.Category;
import com.dao.CategoryDao;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public class CategoryDaoImpl implements CategoryDao {

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Category findById(Integer id) {
        List<Category> list = jdbcTemplate.query(
                "SELECT * FROM t_category WHERE id=?",
                new BeanPropertyRowMapper<Category>(Category.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Category> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM t_category ORDER BY id",
                new BeanPropertyRowMapper<Category>(Category.class));
    }
}
