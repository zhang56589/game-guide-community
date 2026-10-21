package com.dao.impl;

import java.util.List;
import com.bean.User;
import com.dao.UserDao;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

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
    private static final String INSERT_USER      = "INSERT INTO t_user(username,password,salt,nickname,role) VALUES(?,?,?,?,?)";
    private static final String UPDATE_USER      = "UPDATE t_user SET nickname=?, status=?, role=? WHERE id=?";
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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(INSERT_USER,
                new Object[]{ user.getUsername(), user.getPassword(), user.getSalt(),
                              user.getNickname(), user.getRole() },
                keyHolder);
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().intValue());
        }
        return rows;
    }

    @Override
    public int update(User user) {
        return jdbcTemplate.update(UPDATE_USER,
                user.getNickname(), user.getStatus(), user.getRole(), user.getId());
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
