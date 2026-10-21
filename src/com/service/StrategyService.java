package com.service;

import java.util.List;
import com.bean.Strategy;

public interface StrategyService {
    /** 发布攻略（返回提示，null=成功） */
    String       publish(Strategy s, Integer userId);
    /** 按ID查询并自增浏览量（前台，需审核通过） */
    Strategy     getDetail(Integer id);
    /** 按ID查询，不加审核过滤（内部/归属校验使用） */
    Strategy     getByIdRaw(Integer id);
    /** 首页最新列表 */
    List<Strategy> getLatest(int page, int size);
    /** 按分类 */
    List<Strategy> getByCategory(Integer categoryId, int page, int size);
    /** 我的攻略 */
    List<Strategy> getMy(Integer userId);
    /** 搜索 */
    List<Strategy> search(String keyword, int page, int size);
    /** 管理员：全部 */
    List<Strategy> getAll();
    /** 软删除 */
    void         delete(Integer id);
    /** 管理员硬删除 */
    void         adminDelete(Integer id);
    /** 审核通过 */
    void         auditApprove(Integer id);
    /** 审核拒绝 */
    void         auditReject(Integer id);
    /** 数量 */
    int          count();
}
