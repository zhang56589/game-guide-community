package com.service.impl;

import java.util.List;
import com.bean.Strategy;
import com.bean.User;
import com.dao.StrategyDao;
import com.dao.UserDao;
import com.service.StrategyService;

public class StrategyServiceImpl implements StrategyService {

    private StrategyDao strategyDao;
    private UserDao     userDao;

    public void setStrategyDao(StrategyDao strategyDao) { this.strategyDao = strategyDao; }
    public void setUserDao(UserDao userDao)               { this.userDao = userDao; }

    @Override
    public String publish(Strategy s, Integer userId) {
        if (s.getTitle() == null || s.getTitle().trim().isEmpty()) return "标题不能为空";
        if (s.getTitle().length() > 100) return "标题不能超过100字";
        if (s.getContent() == null || s.getContent().trim().isEmpty()) return "内容不能为空";
        if (s.getGameName() == null || s.getGameName().trim().isEmpty()) return "请填写游戏名称";

        User u = userDao.findById(userId);
        if (u == null || u.getStatus() != 1) return "用户不存在或已被禁用";

        s.setUserId(userId);
        // status=1 audit_status=0 由 DAO INSERT SQL 写死
        s.setViewCount(0);
        s.setLikeCount(0);
        strategyDao.insert(s);
        return null;
    }

    @Override
    public Strategy getDetail(Integer id) {
        Strategy s = strategyDao.findById(id);
        if (s == null) return null;
        if (s.getStatus() == 1 && s.getAuditStatus() != null && s.getAuditStatus() == 1) {
            strategyDao.incrementViewCount(id);
            return s;
        }
        return null;
    }

    @Override
    public Strategy getByIdRaw(Integer id) {
        return strategyDao.findById(id); // 不加审核过滤
    }

    @Override
    public List<Strategy> getLatest(int page, int size) {
        return strategyDao.findLatest((page - 1) * size, size);
    }

    @Override
    public List<Strategy> getByCategory(Integer categoryId, int page, int size) {
        return strategyDao.findByCategory(categoryId, (page - 1) * size, size);
    }

    @Override
    public List<Strategy> getMy(Integer userId) {
        return strategyDao.findByUserId(userId);
    }

    @Override
    public List<Strategy> search(String keyword, int page, int size) {
        return strategyDao.search(keyword, (page - 1) * size, size);
    }

    @Override
    public List<Strategy> getAll() {
        return strategyDao.findAll();
    }

    @Override
    public void delete(Integer id) {
        strategyDao.updateStatus(id, 0);
    }

    @Override
    public void adminDelete(Integer id) {
        delete(id);
    }

    @Override
    public void auditApprove(Integer id) {
        strategyDao.updateAuditStatus(id, 1);
    }

    @Override
    public void auditReject(Integer id) {
        strategyDao.updateAuditStatus(id, 2);
    }

    @Override
    public int count() {
        return strategyDao.count();
    }
}
