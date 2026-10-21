package com.dao;

import java.util.List;
import com.bean.Strategy;

/**
 * 攻略 DAO 接口
 */
public interface StrategyDao {
    /** 新增 */
    int      insert(Strategy s);
    /** 按ID查（含作者昵称、分类名） */
    Strategy findById(Integer id);
    /** 首页列表（正常状态，最新） */
    List<Strategy> findLatest(int offset, int limit);
    /** 按分类查 */
    List<Strategy> findByCategory(Integer categoryId, int offset, int limit);
    /** 我的攻略 */
    List<Strategy> findByUserId(Integer userId);
    /** 管理员-全部列表 */
    List<Strategy> findAll();
    /** 搜索（标题模糊匹配） */
    List<Strategy> search(String keyword, int offset, int limit);
    /** 更新浏览量 */
    int      incrementViewCount(Integer id);
    /** 更新状态(软删) */
    int      updateStatus(Integer id, Integer status);
    /** 更新审核状态 */
    int      updateAuditStatus(Integer id, Integer auditStatus);
    /** 计数 */
    int      count();
    int      countByCategory(Integer categoryId);
    int      countByUserId(Integer userId);
}
