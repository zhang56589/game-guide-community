-- ============================================
-- 审计日志表
-- ============================================
USE game_strategy_platform;

DROP TABLE IF EXISTS t_audit_log;
CREATE TABLE t_audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator    VARCHAR(50)  COMMENT '操作人账号',
    nickname    VARCHAR(50)  COMMENT '操作人昵称',
    method      VARCHAR(100) NOT NULL COMMENT '完整方法名(类名.方法名)',
    params      VARCHAR(2000) COMMENT '入参JSON',
    duration_ms INT          COMMENT '耗时毫秒',
    success     TINYINT(1)   COMMENT '是否成功 1=成功 0=异常',
    error_msg   VARCHAR(500) COMMENT '异常信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
