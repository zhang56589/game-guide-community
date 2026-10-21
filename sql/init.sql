-- ============================================
-- GameStrategyPlatform 数据库初始化脚本
-- MySQL 8.0 / 5.7 兼容
-- ============================================

CREATE DATABASE IF NOT EXISTS game_strategy_platform 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE game_strategy_platform;

-- ---------- 分类表 ----------
DROP TABLE IF EXISTS t_category;
CREATE TABLE t_category (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='攻略分类表';

-- ---------- 用户表 ----------
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '密码(MD5)',
    nickname    VARCHAR(50) NOT NULL COMMENT '昵称',
    role        TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=普通用户, 1=管理员',
    status      TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------- 攻略表 ----------
DROP TABLE IF EXISTS t_strategy;
CREATE TABLE t_strategy (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL COMMENT '攻略标题',
    game_name   VARCHAR(50) NOT NULL COMMENT '游戏名称',
    content     TEXT NOT NULL COMMENT '攻略内容',
    cover_image VARCHAR(200) COMMENT '封面图URL',
    user_id     INT NOT NULL COMMENT '作者ID',
    category_id INT COMMENT '分类ID',
    view_count  INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    like_count  INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    status      TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0=已删除, 1=正常, 2=待审核',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_strategy_user     FOREIGN KEY (user_id)     REFERENCES t_user(id)     ON DELETE CASCADE,
    CONSTRAINT fk_strategy_category FOREIGN KEY (category_id) REFERENCES t_category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='攻略表';

-- ============================================
-- 初始化数据
-- ============================================

-- 默认管理员账号密码: admin / 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO t_user (username, password, nickname, role) VALUES 
('admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 1),
('player1', 'e10adc3949ba59abbe56e057f20f883e', '小王玩家', 0),
('player2', 'e10adc3949ba59abbe56e057f20f883e', '大神张三', 0);

INSERT INTO t_category (name, description) VALUES 
('动作游戏', '鬼泣、黑魂、只狼等动作类游戏攻略'),
('角色扮演', 'FF、巫师、原神等RPG游戏攻略'),
('策略模拟', '文明、三国志、模拟城市等策略类游戏攻略'),
('竞技对战', 'LOL、王者荣耀、CS等竞技游戏攻略');

INSERT INTO t_strategy (title, game_name, content, user_id, category_id, cover_image) VALUES 
('《只狼：影逝二度》全BOSS流程攻略', '只狼：影逝二度', 
 '一、序章\n推荐路线：苇名城外围 -> 平田宅邸 -> 苇名城主城\n\n二、BOSS详解\n\n1. 武士大将 河原田直盛\n位置：苇名城外围\n打法建议：善用弹反，两招即可识破后处决。\n\n2. 蝴蝶夫人\n位置：平田宅邸\n打法建议：多使用钩绳突进，烟雾弹可以拉开距离。\n\n3. 苇名一心\n位置：苇名城主城\n最终BOSS，三阶段，需要充分练习弹反节奏。', 
 2, 1, 'https://img.example.com/sekiro.jpg'),
('《原神》4.5版本深渊配队推荐', '原神', 
 '本期深渊环境对火元素有利，推荐以下配队：\n\n上层：雷神圣遗物+夜兰+行秋+班尼特\n下层：胡桃+钟离+阿贝多+枫原万叶\n\n输出循环注意点...', 
 3, 2, 'https://img.example.com/genshin.jpg'),
('《文明6》新手入门指南', '文明6', 
 '1. 开局选文明：推荐俄罗斯、希腊\n2. 科技树优先顺序：书写 -> 货币 -> 铁器 -> 工程\n3. 建造顺序：侦察兵 -> 建造者 -> 战士（视情况）\n4. 重要政策：寡头政治 -> 职业军队', 
 2, 3, 'https://img.example.com/civ6.jpg'),
('《英雄联盟》打野节奏把控技巧', '英雄联盟', 
 '打野前期节奏要点：\n1. 三级路线：红BUFF -> 蓝BUFF -> 蛤蟆/三狼 -> 反蹲\n2. 四级Gank：上路或中路选一条最有机会的线\n3. 视野控制：河道蟹必控，龙坑布眼\n4. 滚雪球：6级后开始主动控龙', 
 3, 4, 'https://img.example.com/lol.jpg');
