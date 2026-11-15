package com.dao;

import java.util.List;
import com.bean.User;

/**
 * 用户 DAO 接口
 */
public interface UserDao {
    /** 根据ID查用户 */
    User     findById(Integer id);

    /** 根据账号查用户（登录/注册校验） */
    User     findByUsername(String username);

    /** 账号+密码查用户（登录） */
    User     findByUsernameAndPassword(String username, String password);

    /** 新增用户 */
    int      insert(User user);

    /** 更新用户(昵称/状态等) */
    int      update(User user);

    /** PERF-2 性能优化：直接按 id 改状态，避免先 findById 再 update 的 N+1 */
    int      updateStatus(Integer id, Integer status);

    /** PERF-2 性能优化：直接按 id 改角色，避免先 findById 再 update 的 N+1 */
    int      updateRole(Integer id, Integer role);

    /** 更新密码（含 salt） */
    int      updatePassword(Integer id, String newPassword, String salt);

    /** 查询所有用户 */
    List<User> findAll();

    /** 统计用户数 */
    int      count();
}
