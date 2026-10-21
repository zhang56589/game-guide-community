package com.dao;

public interface AuditLogDao {
    /** 插入审计日志 */
    int insert(com.bean.AuditLog log);
}
