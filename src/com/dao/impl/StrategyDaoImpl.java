package com.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.bean.Strategy;
import com.dao.StrategyDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/**
 * 攻略 DAO 实现类
 */
public class StrategyDaoImpl implements StrategyDao {

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 带 JOIN 的通用 RowMapper，填充 authorName / categoryName
    private final RowMapper<Strategy> FULL_MAPPER = new RowMapper<Strategy>() {
        @Override
        public Strategy mapRow(ResultSet rs, int rowNum) throws SQLException {
            Strategy s = new Strategy();
            s.setId(rs.getInt("id"));
            s.setTitle(rs.getString("title"));
            s.setGameName(rs.getString("game_name"));
            s.setContent(rs.getString("content"));
            s.setCoverImage(rs.getString("cover_image"));
            s.setUserId(rs.getInt("user_id"));
            s.setCategoryId(rs.getObject("category_id") == null ? null : rs.getInt("category_id"));
            s.setViewCount(rs.getInt("view_count"));
            s.setLikeCount(rs.getInt("like_count"));
            s.setStatus(rs.getInt("status"));
            s.setAuditStatus(rs.getInt("audit_status"));
            s.setCreateTime(rs.getTimestamp("create_time"));
            s.setUpdateTime(rs.getTimestamp("update_time"));
            s.setAuthorName(rs.getString("author_name"));
            s.setCategoryName(rs.getString("category_name"));
            return s;
        }
    };

    private static final String INSERT =
            "INSERT INTO t_strategy(title,game_name,content,cover_image,user_id,category_id,status,audit_status) " +
            "VALUES(?,?,?,?,?,?,1,0)";
    private static final String FIND_BY_ID =
            "SELECT s.*, u.nickname AS author_name, c.name AS category_name " +
            "FROM t_strategy s LEFT JOIN t_user u ON s.user_id=u.id " +
            "LEFT JOIN t_category c ON s.category_id=c.id WHERE s.id=?";
    private static final String FIND_LATEST =
            "SELECT s.*, u.nickname AS author_name, c.name AS category_name " +
            "FROM t_strategy s LEFT JOIN t_user u ON s.user_id=u.id " +
            "LEFT JOIN t_category c ON s.category_id=c.id " +
            "WHERE s.status=1 AND s.audit_status=1 ORDER BY s.create_time DESC LIMIT ?,?";
    private static final String FIND_BY_CATEGORY =
            "SELECT s.*, u.nickname AS author_name, c.name AS category_name " +
            "FROM t_strategy s LEFT JOIN t_user u ON s.user_id=u.id " +
            "LEFT JOIN t_category c ON s.category_id=c.id " +
            "WHERE s.status=1 AND s.audit_status=1 AND s.category_id=? ORDER BY s.create_time DESC LIMIT ?,?";
    private static final String FIND_BY_USER =
            "SELECT s.*, u.nickname AS author_name, c.name AS category_name " +
            "FROM t_strategy s LEFT JOIN t_user u ON s.user_id=u.id " +
            "LEFT JOIN t_category c ON s.category_id=c.id " +
            "WHERE s.user_id=? ORDER BY s.create_time DESC";
    private static final String FIND_ALL =
            "SELECT s.*, u.nickname AS author_name, c.name AS category_name " +
            "FROM t_strategy s LEFT JOIN t_user u ON s.user_id=u.id " +
            "LEFT JOIN t_category c ON s.category_id=c.id " +
            "ORDER BY s.create_time DESC";
    private static final String SEARCH =
            "SELECT s.*, u.nickname AS author_name, c.name AS category_name " +
            "FROM t_strategy s LEFT JOIN t_user u ON s.user_id=u.id " +
            "LEFT JOIN t_category c ON s.category_id=c.id " +
            "WHERE s.status=1 AND s.audit_status=1 AND (s.title LIKE ? OR s.game_name LIKE ?) " +
            "ORDER BY s.create_time DESC LIMIT ?,?";
    private static final String INC_VIEW = "UPDATE t_strategy SET view_count=view_count+1 WHERE id=?";
    private static final String UPDATE_ST = "UPDATE t_strategy SET status=? WHERE id=?";
    private static final String UPDATE_AUDIT = "UPDATE t_strategy SET audit_status=? WHERE id=?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM t_strategy";
    private static final String COUNT_BY_C = "SELECT COUNT(*) FROM t_strategy WHERE category_id=?";
    private static final String COUNT_BY_U = "SELECT COUNT(*) FROM t_strategy WHERE user_id=?";

    @Override
    public int insert(Strategy s) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(INSERT, new String[]{"id"});
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getGameName());
            ps.setString(3, s.getContent());
            ps.setString(4, s.getCoverImage());
            ps.setInt(5, s.getUserId());
            if (s.getCategoryId() != null) {
                ps.setInt(6, s.getCategoryId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            return ps;
        }, kh);
        if (kh.getKey() != null) {
            s.setId(kh.getKey().intValue());
        }
        return 1;
    }

    @Override
    public Strategy findById(Integer id) {
        List<Strategy> list = jdbcTemplate.query(FIND_BY_ID, FULL_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Strategy> findLatest(int offset, int limit) {
        return jdbcTemplate.query(FIND_LATEST, FULL_MAPPER, offset, limit);
    }

    @Override
    public List<Strategy> findByCategory(Integer categoryId, int offset, int limit) {
        return jdbcTemplate.query(FIND_BY_CATEGORY, FULL_MAPPER, categoryId, offset, limit);
    }

    @Override
    public List<Strategy> findByUserId(Integer userId) {
        return jdbcTemplate.query(FIND_BY_USER, FULL_MAPPER, userId);
    }

    @Override
    public List<Strategy> findAll() {
        return jdbcTemplate.query(FIND_ALL, FULL_MAPPER);
    }

    @Override
    public List<Strategy> search(String keyword, int offset, int limit) {
        String like = "%" + keyword + "%";
        return jdbcTemplate.query(SEARCH, FULL_MAPPER, like, like, offset, limit);
    }

    @Override
    public int incrementViewCount(Integer id) {
        return jdbcTemplate.update(INC_VIEW, id);
    }

    @Override
    public int updateStatus(Integer id, Integer status) {
        return jdbcTemplate.update(UPDATE_ST, status, id);
    }

    @Override
    public int count() {
        Integer c = jdbcTemplate.queryForObject(COUNT_ALL, Integer.class);
        return c == null ? 0 : c;
    }

    @Override
    public int countByCategory(Integer categoryId) {
        Integer c = jdbcTemplate.queryForObject(COUNT_BY_C, Integer.class, categoryId);
        return c == null ? 0 : c;
    }

    @Override
    public int countByUserId(Integer userId) {
        Integer c = jdbcTemplate.queryForObject(COUNT_BY_U, Integer.class, userId);
        return c == null ? 0 : c;
    }

    @Override
    public int updateAuditStatus(Integer id, Integer auditStatus) {
        return jdbcTemplate.update(UPDATE_AUDIT, auditStatus, id);
    }
}
