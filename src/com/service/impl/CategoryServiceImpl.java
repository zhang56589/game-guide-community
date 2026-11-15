package com.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.bean.Category;
import com.dao.CategoryDao;
import com.service.CategoryService;

public class CategoryServiceImpl implements CategoryService {

    private CategoryDao categoryDao;

    public void setCategoryDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    // ===== PERF-4 分类缓存（只读数据，一次加载永不过期；重启 Tomcat 自动刷新） =====
    private volatile List<Category> cacheAll;
    private volatile Map<Integer, Category> cacheById;
    private volatile boolean initialized = false;

    /** 懒加载缓存（双检锁模式，只读场景简单可靠） */
    private void ensureCache() {
        if (initialized) return;
        synchronized (this) {
            if (initialized) return;
            List<Category> list = categoryDao.findAll();
            Map<Integer, Category> map = new HashMap<>();
            for (Category c : list) {
                map.put(c.getId(), c);
            }
            cacheAll = list;
            cacheById = map;
            initialized = true;
        }
    }

    @Override
    public Category findById(Integer id) {
        ensureCache();
        return cacheById.get(id);
    }

    @Override
    public List<Category> findAll() {
        ensureCache();
        return cacheAll;
    }
}
