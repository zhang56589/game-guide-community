package com.service;

import java.util.List;
import com.bean.User;

public interface UserService {
    /** 注册：返回提示消息，null 表示成功 */
    String  register(User user);
    /** 登录：返回用户或null */
    User    login(String username, String password);
    /** 按ID查 */
    User    findById(Integer id);
    /** 所有用户 */
    List<User> findAll();
    /** 更新昵称 */
    void    updateNickname(Integer id, String nickname);
    /** 更新状态(禁用/启用) */
    void    updateStatus(Integer id, Integer status);
    /** 管理员切换 */
    void    updateRole(Integer id, Integer role);
    /** 用户数 */
    int     count();
}
