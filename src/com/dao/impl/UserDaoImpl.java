package com.dao.impl;

import java.util.List;
import com.bean.User;
import com.dao.UserDao;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 用户 DAO 实现类
 * 全部 XML 配置 → 不使用注解
 */
public class UserDaoImpl implements UserDao {

    private JdbcTemplate jdbcTemplate;

    /** XML 属性注入 */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String FIND_BY_ID       = "SELECT * FROM t_user WHERE id = ?";
    private static final String FIND_BY_NAME     = "SELECT * FROM t_user WHERE username = ?";
    private static final String FIND_BY_NAME_PWD  = "SELECT * FROM t_user WHERE username = ? AND password = ?";
    private static final String INSERT_USER      = "INSERT INTO t_user(username,password,salt,nickname,role,status) VALUES(?,?,?,?,?,?)";
    private static final String UPDATE_USER      = "UPDATE t_user SET nickname=?, status=?, role=? WHERE id=?";
    private static final String UPDATE_STATUS    = "UPDATE t_user SET status=? WHERE id=?";
    private static final String UPDATE_ROLE      = "UPDATE t_user SET role=? WHERE id=?";
    private static final String UPDATE_PWD       = "UPDATE t_user SET password=?, salt=? WHERE id=?";
    private static final String FIND_ALL         = "SELECT * FROM t_user ORDER BY id DESC";
    private static final String COUNT_ALL        = "SELECT COUNT(*) FROM t_user";

    @Override
    public User findById(Integer id) {
        List<User> list = jdbcTemplate.query(FIND_BY_ID,
                new BeanPropertyRowMapper<User>(User.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public User findByUsername(String username) {
        List<User> list = jdbcTemplate.query(FIND_BY_NAME,
                new BeanPropertyRowMapper<User>(User.class), username);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public User findByUsernameAndPassword(String username, String password) {
        List<User> list = jdbcTemplate.query(FIND_BY_NAME_PWD,
                new BeanPropertyRowMapper<User>(User.class), username, password);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int insert(User user) {
        // 修复点：移除了 GeneratedKeyHolder，直接执行插入
        return jdbcTemplate.update(INSERT_USER, 
                user.getUsername(), 
                user.getPassword(), 
                user.getSalt(),
                user.getNickname(), 
                user.getRole(),
                user.getStatus());
    }

    @Override
    public int update(User user) {
        return jdbcTemplate.update(UPDATE_USER,
                user.getNickname(), user.getStatus(), user.getRole(), user.getId());
    }

    @Override
    public int updateStatus(Integer id, Integer status) {
        return jdbcTemplate.update(UPDATE_STATUS, status, id);
    }

    @Override
    public int updateRole(Integer id, Integer role) {
        return jdbcTemplate.update(UPDATE_ROLE, role, id);
    }

    @Override
    public int updatePassword(Integer id, String newPassword, String salt) {
        return jdbcTemplate.update(UPDATE_PWD, newPassword, salt, id);
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(FIND_ALL,
                new BeanPropertyRowMapper<User>(User.class));
    }

    @Override
    public int count() {
        Integer c = jdbcTemplate.queryForObject(COUNT_ALL, Integer.class);
        return c == null ? 0 : c;
    }
}