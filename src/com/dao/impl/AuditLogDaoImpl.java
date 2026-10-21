package com.dao.impl;

import com.bean.AuditLog;
import com.dao.AuditLogDao;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 审计日志 DAO 实现
 * 注意：此 DAO 不放在 AOP pointcut 范围内，避免递归拦截
 */
public class AuditLogDaoImpl implements AuditLogDao {

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String INSERT =
            "INSERT INTO t_audit_log(operator,nickname,method,params,duration_ms,success,error_msg) " +
            "VALUES(?,?,?,?,?,?,?)";

    @Override
    public int insert(AuditLog log) {
        return jdbcTemplate.update(INSERT,
                log.getOperator(),
                log.getNickname(),
                log.getMethod(),
                log.getParams(),
                log.getDurationMs(),
                log.getSuccess(),
                log.getErrorMsg());
    }
}
