-- ============================================
-- 数据库变更脚本（在 init.sql 之后执行）
-- ============================================
USE game_strategy_platform;

-- ---------- Task 3：t_user 加 salt 字段 ----------
ALTER TABLE t_user ADD COLUMN salt VARCHAR(32) DEFAULT NULL COMMENT '密码盐值' AFTER password;

-- ---------- Task 4：t_strategy 加 audit_status 字段 ----------
-- 0=待审核  1=已通过  2=已拒绝
ALTER TABLE t_strategy ADD COLUMN audit_status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '审核状态 0=待审核 1=通过 2=拒绝' AFTER status;

-- 已有攻略设为已通过，保持现有可见性
UPDATE t_strategy SET audit_status = 1 WHERE audit_status IS NULL OR audit_status = 0;

-- ---------- 可选：清理已删除攻略的 audit_status（无业务影响） ----------
UPDATE t_strategy SET audit_status = 0 WHERE status = 0;
