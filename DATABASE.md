# 数据库初始化与表结构文档

> GameStrategyPlatform 项目数据库完整参考，涵盖 **建库建表、变更脚本、字段说明、ER 图、初始数据**。
> 所有 SQL 脚本位于 `sql/` 目录，**必须严格按顺序执行**。

---

## 📋 目录

- [环境要求](#-环境要求)
- [脚本总览与执行顺序](#-脚本总览与执行顺序)
- [Step 1: init.sql — 建库建表 + 初始数据](#-step-1-initsql--建库建表--初始数据)
  - [建库语句](#11-建库语句)
  - [表：t_category（分类表）](#12-表-t_category分类表)
  - [表：t_user（用户表）](#13-表-t_user用户表)
  - [表：t_strategy（攻略表）](#14-表-t_strategy攻略表)
  - [初始数据说明](#15-初始数据说明)
- [Step 2: migration.sql — 结构变更 + 数据迁移](#-step-2-migrationsql--结构变更--数据迁移)
  - [ALTER: t_user 加 salt 字段](#21-alter-t_user-加-salt-字段)
  - [ALTER: t_strategy 加 audit_status 字段](#22-alter-t_strategy-加-audit_status-字段)
  - [数据迁移 SQL 含义](#23-数据迁移-sql-含义)
- [Step 3: audit.sql — 审计日志表](#-step-3-auditsql--审计日志表)
- [ER 关系图](#-er-关系图)
- [完整数据字典](#-完整数据字典)
  - [t_category](#表-t_category)
  - [t_user](#表-t_user)
  - [t_strategy](#表-t_strategy)
  - [t_audit_log](#表-t_audit_log)
- [状态字段对照表](#-状态字段对照表)
- [约束与索引说明](#-约束与索引说明)
- [常见问题](#-常见问题)

---

## 🖥️ 环境要求

| 组件 | 最低版本 | 推荐版本 | 说明 |
|------|----------|----------|------|
| MySQL Server | 5.7.x | 8.0.x | 本项目兼容两个大版本 |
| 编码 | utf8mb4 | utf8mb4_unicode_ci | 必须用 utf8mb4 才能存 emoji |
| 存储引擎 | InnoDB | InnoDB | 需要事务、外键、行锁 |
| 字符集排序规则 | utf8mb4_unicode_ci | — | 大小写不敏感排序 |

### 前置条件

1. 已安装 MySQL 5.7+ 或 8.0+
2. 有 `root` 或有 `CREATE DATABASE / CREATE TABLE` 权限的账号
3. 客户端可用：`mysql -u root -p` 能正常登录

---

## 📜 脚本总览与执行顺序

| 序号 | 文件 | 作用 | 是否可重复执行 |
|------|------|------|----------------|
| ① | `sql/init.sql` | 建库 + 建 3 张主表 + 初始数据 | ⚠️ 用了 `DROP TABLE IF EXISTS`，**会删除旧数据** |
| ② | `sql/migration.sql` | ALTER 加字段 + 数据迁移 | ✅ 幂等（先 `ADD COLUMN`，已是 ALTERTABLE） |
| ③ | `sql/audit.sql` | 建审计日志表 | ⚠️ 用了 `DROP TABLE IF EXISTS` |

### 执行命令（三种方式任选）

#### 方式 A：命令行 mysql 客户端

```bash
# 进入项目根目录后
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/migration.sql
mysql -u root -p < sql/audit.sql
```

或一条命令串起来：

```bash
mysql -u root -p -e "source sql/init.sql; source sql/migration.sql; source sql/audit.sql;"
```

#### 方式 B：mysql 命令行内执行

```bash
mysql -u root -p
> source /path/to/game-guide-community/sql/init.sql;
> source /path/to/game-guide-community/sql/migration.sql;
> source /path/to/game-guide-community/sql/audit.sql;
```

#### 方式 C：GUI 工具

使用 Navicat / DBeaver / MySQL Workbench 依次打开三个文件，点击"运行"。

---

## 📜 Step 1: init.sql — 建库建表 + 初始数据

> 文件位置：[sql/init.sql](file:///c:/Users/zhang/Documents/workspace-sts-3.9.4.RELEASE/game-guide-community/sql/init.sql)
> 这是最核心的脚本，负责创建数据库和三张业务表。

### 1.1 建库语句

```sql
CREATE DATABASE IF NOT EXISTS game_strategy_platform 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE game_strategy_platform;
```

| 子句 | 作用 |
|------|------|
| `IF NOT EXISTS` | 库已存在则跳过，不报错 |
| `DEFAULT CHARACTER SET utf8mb4` | 支持 4 字节 Unicode，能存 emoji、生僻字 |
| `COLLATE utf8mb4_unicode_ci` | 大小写不敏感（ci = case-insensitive） |

> **命名约定**：数据库名用小写下划线风格 `game_strategy_platform`

---

### 1.2 表：t_category（分类表）

```sql
DROP TABLE IF EXISTS t_category;
CREATE TABLE t_category (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='攻略分类表';
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `id` | INT | ✅ | PK, AUTO_INCREMENT | 主键，自增 |
| `name` | VARCHAR(50) | ✅ | NOT NULL | 分类名称（如 "动作游戏"） |
| `description` | VARCHAR(200) | ❌ | NULL | 分类描述 |

**数据规模**：通常 < 20 条，由管理员预设。

---

### 1.3 表：t_user（用户表）

```sql
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
```

> ⚠️ **注意**：这里是 init.sql 的**初始版本**，没有 `salt` 字段。
> `salt` 字段由 **migration.sql** 的 ALTER 语句添加，见后文。

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `id` | INT | ✅ | PK, AUTO_INCREMENT | 主键，自增 |
| `username` | VARCHAR(50) | ✅ | UNIQUE | 登录账号，唯一不重复 |
| `password` | VARCHAR(100) | ✅ | NOT NULL | **加盐 MD5** 值（见 migration.sql 后） |
| `nickname` | VARCHAR(50) | ✅ | NOT NULL | 显示给其他用户看的昵称 |
| `role` | TINYINT(1) | ✅ | DEFAULT 0 | 角色：0=普通用户, 1=管理员 |
| `status` | TINYINT(1) | ✅ | DEFAULT 1 | 账号状态：0=禁用, 1=正常 |
| `create_time` | DATETIME | ✅ | DEFAULT CURRENT_TIMESTAMP | 注册时间，MySQL 自动填 |

**设计考量**：
- `role` / `status` 用 TINYINT 节省空间（比 ENUM 更通用，方便前端映射）
- `username` 加 UNIQUE，数据库层面防重复
- `create_time` DEFAULT CURRENT_TIMESTAMP，不需要应用层手动设置

---

### 1.4 表：t_strategy（攻略表）

```sql
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
```

> ⚠️ **注意**：这里是 init.sql 的**初始版本**，没有 `audit_status` 字段。
> `audit_status` 由 **migration.sql** 的 ALTER 语句添加，见后文。

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `id` | INT | ✅ | PK, AUTO_INCREMENT | 主键，自增 |
| `title` | VARCHAR(100) | ✅ | NOT NULL | 攻略标题 |
| `game_name` | VARCHAR(50) | ✅ | NOT NULL | 游戏名称（用于搜索和分类） |
| `content` | TEXT | ✅ | NOT NULL | 攻略正文内容，TEXT 最大 65535 字节 |
| `cover_image` | VARCHAR(200) | ❌ | NULL | 封面图片 URL |
| `user_id` | INT | ✅ | FK → t_user.id | 作者，外键级联删除 |
| `category_id` | INT | ❌ | FK → t_category.id | 分类，外键 SET NULL |
| `view_count` | INT | ✅ | DEFAULT 0 | 浏览次数（详情页 +1） |
| `like_count` | INT | ✅ | DEFAULT 0 | 点赞次数 |
| `status` | TINYINT(1) | ✅ | DEFAULT 1 | 软删：0=已删, 1=正常 |
| `create_time` | DATETIME | ✅ | DEFAULT CURRENT_TIMESTAMP | 发布时间 |
| `update_time` | DATETIME | ✅ | ON UPDATE | 任何更新自动刷新 |

**外键行为**：

| 外键 | ON DELETE | 含义 |
|------|-----------|------|
| `fk_strategy_user` (user_id → t_user.id) | **CASCADE** | 用户被删除 → 该用户所有攻略也被删除 |
| `fk_strategy_category` (category_id → t_category.id) | **SET NULL** | 分类被删除 → 该分类下攻略的 category_id 设为 NULL，攻略保留 |

**TEXT 说明**：
- `TEXT` 最大 65,535 字节（约 2 万汉字）
- 超过这个长度改用 `MEDIUMTEXT`（16MB）或 `LONGTEXT`（4GB）

---

### 1.5 初始数据说明

#### 1.5.1 用户数据（3 条）

```sql
INSERT INTO t_user (username, password, nickname, role) VALUES 
('admin',  'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 1),
('player1','e10adc3949ba59abbe56e057f20f883e', '小王玩家',   0),
('player2','e10adc3949ba59abbe56e057f20f883e', '大神张三',   0);
```

| 账号 | 明文密码 | MD5 值 | role | 含义 |
|------|----------|--------|------|------|
| admin | `123456` | `e10adc3949ba59abbe56e057f20f883e` | 1 | 超级管理员 |
| player1 | `123456` | 同上 | 0 | 普通用户 |
| player2 | `123456` | 同上 | 0 | 普通用户 |

> **为什么密码明文是 `123456`**：因为这是**老用户**场景，init.sql 里没有 salt（salt 由 migration.sql 后续 ALTER 加上），这三条用户的 `salt` 为 NULL，登录时 UserServiceImpl 会 fallback 到"纯 md5"校验。
>
> **新注册用户**才会自动生成随机 salt，密码存为 `md5(plain + salt)`。

#### 1.5.2 分类数据（4 条）

```sql
INSERT INTO t_category (name, description) VALUES 
('动作游戏',  '鬼泣、黑魂、只狼等动作类游戏攻略'),
('角色扮演',  'FF、巫师、原神等RPG游戏攻略'),
('策略模拟',  '文明、三国志、模拟城市等策略类游戏攻略'),
('竞技对战',  'LOL、王者荣耀、CS等竞技游戏攻略');
```

| id | name | description |
|----|------|-------------|
| 1 | 动作游戏 | 鬼泣、黑魂、只狼等动作类游戏攻略 |
| 2 | 角色扮演 | FF、巫师、原神等RPG游戏攻略 |
| 3 | 策略模拟 | 文明、三国志、模拟城市等策略类游戏攻略 |
| 4 | 竞技对战 | LOL、王者荣耀、CS等竞技游戏攻略 |

#### 1.5.3 攻略数据（4 条）

```sql
INSERT INTO t_strategy (title, game_name, content, user_id, category_id, cover_image) VALUES 
('《只狼：影逝二度》全BOSS流程攻略', '只狼：影逝二度', '...', 2, 1, 'https://img.example.com/sekiro.jpg'),
('《原神》4.5版本深渊配队推荐',       '原神',           '...', 3, 2, 'https://img.example.com/genshin.jpg'),
('《文明6》新手入门指南',             '文明6',          '...', 2, 3, 'https://img.example.com/civ6.jpg'),
('《英雄联盟》打野节奏把控技巧',       '英雄联盟',       '...', 3, 4, 'https://img.example.com/lol.jpg');
```

| id | 标题 | 作者 (user_id) | 分类 (category_id) |
|----|------|----------------|-------------------|
| 1 | 《只狼：影逝二度》全BOSS流程攻略 | player1 (id=2) | 动作游戏 (id=1) |
| 2 | 《原神》4.5版本深渊配队推荐 | player2 (id=3) | 角色扮演 (id=2) |
| 3 | 《文明6》新手入门指南 | player1 (id=2) | 策略模拟 (id=3) |
| 4 | 《英雄联盟》打野节奏把控技巧 | player2 (id=3) | 竞技对战 (id=4) |

**注意**：这些是 init.sql 写入的**初始数据**，此时 `status=1`（DEFAULT）。migration.sql 会把它们的 `audit_status` 统一 UPDATE 为 1（已通过），保持迁移后前台仍可见。

---

## 📜 Step 2: migration.sql — 结构变更 + 数据迁移

> 文件位置：[sql/migration.sql](file:///c:/Users/zhang/Documents/workspace-sts-3.9.4.RELEASE/game-guide-community/sql/migration.sql)
> **必须**在 init.sql 之后执行。负责**结构变更**（加字段）和**数据迁移**。

### 2.1 ALTER: t_user 加 salt 字段

```sql
ALTER TABLE t_user ADD COLUMN salt VARCHAR(32) DEFAULT NULL COMMENT '密码盐值' AFTER password;
```

| 操作 | 说明 |
|------|------|
| `ADD COLUMN` | 新增一列 |
| `VARCHAR(32)` | 16 字节随机盐值的 hex 编码（32 字符） |
| `DEFAULT NULL` | 老用户保留 NULL，向后兼容 |
| `AFTER password` | 插在 password 列后面，语义上密码和盐是一对 |

**加完后的 t_user 结构**：

```
id, username, password, salt, nickname, role, status, create_time
                ↑          ↑    ↑
              原有       新增   原有
```

**为什么 salt 允许 NULL**：
- init.sql 预置的 admin / player1 / player2 是"老用户"，他们的密码是纯 md5
- `salt = NULL` 是"纯 md5 用户"的标志
- UserServiceImpl.login() 会判断：salt 非空 → md5(pwd+salt)；salt 为空 → md5(pwd)

### 2.2 ALTER: t_strategy 加 audit_status 字段

```sql
ALTER TABLE t_strategy ADD COLUMN audit_status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '审核状态 0=待审核 1=通过 2=拒绝' AFTER status;
```

| 操作 | 说明 |
|------|------|
| `AFTER status` | 插在 status 后面，两个状态字段紧挨着 |
| `DEFAULT 1` | 迁移时给老数据一个默认值，避免 NULL |

**加完后的 t_strategy 结构**：

```
id, title, game_name, content, cover_image, user_id, category_id, 
view_count, like_count, status, audit_status, create_time, update_time
                         ↑       ↑            ↑
                      原有    原有        新增
```

**两个状态字段分工**：

| 字段 | 职责 | 取值 | 说明 |
|------|------|------|------|
| `status` | 数据生命周期 | 0=已删 / 1=正常 | 软删，数据还在库里 |
| `audit_status` | 内容审核 | 0=待审 / 1=通过 / 2=拒绝 | 审核结果，决定前台是否可见 |

两者**完全独立**，互不干扰：
- `status=0, audit_status=0` 含义：已删除，曾经是待审核
- `status=1, audit_status=2` 含义：正常存在，但被审核拒绝
- 等等...

### 2.3 数据迁移 SQL 含义

```sql
-- 把默认值 1 显式设一遍（双保险）
UPDATE t_strategy SET audit_status = 1 WHERE audit_status IS NULL OR audit_status = 0;

-- 可选：已删除攻略的 audit_status 统一设为 0
UPDATE t_strategy SET audit_status = 0 WHERE status = 0;
```

| 语句 | 作用 |
|------|------|
| `UPDATE ... WHERE IS NULL OR = 0` | 把迁移过来的"老攻略"统一标记为已审核，保持可见 |
| `UPDATE ... WHERE status = 0` | 可选，已删除攻略统一设待审核。对业务无影响 |

**迁移前后对比**：

| 状态 | 迁移前 | 迁移后 |
|------|--------|--------|
| init.sql 预置 4 条攻略 | audit_status 字段不存在 | audit_status = 1（已通过）|
| 老用户 | salt 字段不存在 | salt = NULL（纯 md5 兼容）|

---

## 📜 Step 3: audit.sql — 审计日志表

> 文件位置：[sql/audit.sql](file:///c:/Users/zhang/Documents/workspace-sts-3.9.4.RELEASE/game-guide-community/sql/audit.sql)
> AOP 切面自动往这里写日志。**强烈建议执行**，否则 Service 层一调用就会报错。

```sql
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
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | BIGINT | ✅ | 用 BIGINT（8 字节），日志表增长快，INT 不够 |
| `operator` | VARCHAR(50) | ❌ | 操作人账号（如 "admin"） |
| `nickname` | VARCHAR(50) | ❌ | 操作人昵称（如 "超级管理员"） |
| `method` | VARCHAR(100) | ✅ | 完整方法名，如 `com.service.impl.UserServiceImpl.login` |
| `params` | VARCHAR(2000) | ❌ | 方法入参 JSON 字符串，截断到 2000 字符 |
| `duration_ms` | INT | ❌ | 方法执行耗时（毫秒） |
| `success` | TINYINT(1) | ❌ | 1=正常执行，0=抛异常 |
| `error_msg` | VARCHAR(500) | ❌ | 异常时的 error.getMessage() |
| `create_time` | DATETIME | ✅ | 自动填当前时间 |

**AOP 写入时机**：

```
Service 方法调用
    │
    ▼
AuditAspect.around()
    ├─ try { pjp.proceed() → success=1 }
    ├─ catch → success=0, error_msg = t.getMessage()
    └─ finally → auditLogDao.insert()  ← 写这里
```

**为什么 finally 写**？
- 不管成功还是异常都记一条日志
- finally 里写，且 AuditLogDao 不在 AOP pointcut 内，不会递归
- 审计日志的异常是"静默"的（不让日志本身影响业务）

---

## 🔗 ER 关系图

```
                     ┌─────────────────────────┐
                     │     t_category          │
                     │─────────────────────────│
                     │ PK id        INT        │
                     │    name      VARCHAR(50)│
                     │    description VARCHAR(200)│
                     └──────────┬──────────────┘
                                │
                   ON DELETE SET NULL
                                │
┌─────────────────────────┐     │     ┌─────────────────────────┐
│       t_user            │     │     │       t_strategy       │
│─────────────────────────│     │     │─────────────────────────│
│ PK id          INT      │◄────┴────│ FK category_id INT       │
│    username    VARCHAR(50)│         │ FK user_id     INT       │
│    password    VARCHAR(100)│        │    title       VARCHAR(100)│
│    salt        VARCHAR(32)│         │    game_name   VARCHAR(50)│
│    nickname    VARCHAR(50)│         │    content     TEXT      │
│    role        TINYINT(1) │         │    cover_image VARCHAR(200)│
│    status      TINYINT(1) │         │    view_count  INT       │
│    create_time DATETIME   │         │    like_count  INT       │
└─────────────────────────┘          │    status      TINYINT(1)│
        ON DELETE CASCADE             │    audit_status TINYINT(1)│
                                       │    create_time DATETIME │
                                       │    update_time DATETIME │
                                       └─────────────────────────┘
                                                │
                                                │ 逻辑关联（无 FK）
                                                ▼
                     ┌─────────────────────────┐
                     │     t_audit_log         │   ← 独立表，无外键
                     │─────────────────────────│
                     │ PK id          BIGINT   │     解耦设计：不引 FK
                     │    operator    VARCHAR(50)│    避免级联删除审计
                     │    nickname    VARCHAR(50)│    日志（审计必须保留）
                     │    method      VARCHAR(100)│
                     │    params      VARCHAR(2000)│
                     │    duration_ms INT       │
                     │    success     TINYINT(1)│
                     │    error_msg   VARCHAR(500)│
                     │    create_time DATETIME  │
                     └─────────────────────────┘
```

### 关系说明

| 关系 | 类型 | 外键 | 含义 |
|------|------|------|------|
| t_user → t_strategy | 1:N | fk_strategy_user ON DELETE CASCADE | 一个用户可发表多篇攻略，删除用户时级联删除攻略 |
| t_category → t_strategy | 1:N | fk_strategy_category ON DELETE SET NULL | 一个分类下有多个攻略，删除分类时攻略保留（category_id 设 NULL）|
| t_strategy → t_audit_log | 逻辑关联 | 无 | 攻略操作（发布/删除/审核）会触发 AuditAspect 写审计日志 |

### 为什么 t_audit_log 不加外键

审计日志的设计意图是**永不删除**，所以不引用 user_id / strategy_id 作为 FK：
1. 用户被 CASCADE 删除 → 审计日志不受影响
2. 攻略被删除 → 审计日志保留（可追溯谁删了什么）
3. 日志用 `method` 字段的类名+方法名区分业务类型，可读性更强

---

## 📊 完整数据字典

### 表：t_category

| # | 字段名 | 数据类型 | 长度 | 可空 | 默认值 | PK | 约束 | 说明 |
|---|--------|----------|------|------|--------|----|------|------|
| 1 | id | INT | — | ❌ | AUTO_INCREMENT | ✅ | PRIMARY KEY | 主键，自增 |
| 2 | name | VARCHAR | 50 | ❌ | — | | | 分类名称 |
| 3 | description | VARCHAR | 200 | ✅ | NULL | | | 分类描述 |

---

### 表：t_user

| # | 字段名 | 数据类型 | 长度 | 可空 | 默认值 | PK | 约束 | 说明 |
|---|--------|----------|------|------|--------|----|------|------|
| 1 | id | INT | — | ❌ | AUTO_INCREMENT | ✅ | PRIMARY KEY | 主键，自增 |
| 2 | username | VARCHAR | 50 | ❌ | — | | UNIQUE | 登录账号，唯一 |
| 3 | password | VARCHAR | 100 | ❌ | — | | | **加盐 MD5** 值（老用户为纯 md5）|
| 4 | salt | VARCHAR | 32 | ✅ | NULL | | | 16 字节随机盐的 hex 编码（migration.sql 新增）|
| 5 | nickname | VARCHAR | 50 | ❌ | — | | | 显示昵称 |
| 6 | role | TINYINT | 1 | ❌ | 0 | | | 0=普通用户, 1=管理员 |
| 7 | status | TINYINT | 1 | ❌ | 1 | | | 0=禁用, 1=正常 |
| 8 | create_time | DATETIME | — | ❌ | CURRENT_TIMESTAMP | | | 注册时间 |

---

### 表：t_strategy

| # | 字段名 | 数据类型 | 长度 | 可空 | 默认值 | PK | 约束 | 说明 |
|---|--------|----------|------|------|--------|----|------|------|
| 1 | id | INT | — | ❌ | AUTO_INCREMENT | ✅ | PRIMARY KEY | 主键，自增 |
| 2 | title | VARCHAR | 100 | ❌ | — | | | 攻略标题 |
| 3 | game_name | VARCHAR | 50 | ❌ | — | | | 游戏名称 |
| 4 | content | TEXT | 65535 | ❌ | — | | | 攻略正文 |
| 5 | cover_image | VARCHAR | 200 | ✅ | NULL | | | 封面图 URL |
| 6 | user_id | INT | — | ❌ | — | | FK → t_user.id CASCADE | 作者 ID |
| 7 | category_id | INT | — | ✅ | NULL | | FK → t_category.id SET NULL | 分类 ID |
| 8 | view_count | INT | — | ❌ | 0 | | | 浏览次数 |
| 9 | like_count | INT | — | ❌ | 0 | | | 点赞次数 |
| 10 | status | TINYINT | 1 | ❌ | 1 | | | 0=已删, 1=正常 |
| 11 | audit_status | TINYINT | 1 | ❌ | 1 | | | 0=待审, 1=通过, 2=拒绝（migration.sql 新增）|
| 12 | create_time | DATETIME | — | ❌ | CURRENT_TIMESTAMP | | | 发布时间 |
| 13 | update_time | DATETIME | — | ❌ | CURRENT_TIMESTAMP ON UPDATE | | | 任何更新自动刷新 |

---

### 表：t_audit_log

| # | 字段名 | 数据类型 | 长度 | 可空 | 默认值 | PK | 约束 | 说明 |
|---|--------|----------|------|------|--------|----|------|------|
| 1 | id | BIGINT | — | ❌ | AUTO_INCREMENT | ✅ | PRIMARY KEY | 主键，自增（用 BIGINT 防溢出）|
| 2 | operator | VARCHAR | 50 | ✅ | NULL | | | 操作人账号（未登录时为空）|
| 3 | nickname | VARCHAR | 50 | ✅ | NULL | | | 操作人昵称 |
| 4 | method | VARCHAR | 100 | ❌ | — | | | 完整方法名（类全名.方法名）|
| 5 | params | VARCHAR | 2000 | ✅ | NULL | | | 入参 JSON 字符串（截断存储）|
| 6 | duration_ms | INT | — | ✅ | NULL | | | 方法耗时（毫秒）|
| 7 | success | TINYINT | 1 | ✅ | NULL | | | 1=成功, 0=抛异常 |
| 8 | error_msg | VARCHAR | 500 | ✅ | NULL | | | 异常信息（仅 success=0 时有值）|
| 9 | create_time | DATETIME | — | ❌ | CURRENT_TIMESTAMP | | | 日志记录时间 |

---

## 🔢 状态字段对照表

### t_user.role — 用户角色

| 值 | 含义 | 对应常量（Java）| 可访问模块 |
|----|------|-----------------|------------|
| 0 | 普通用户 | `ROLE_USER = 0` | 前台 / 我的攻略 / 发布 |
| 1 | 管理员 | `ROLE_ADMIN = 1` | 全部 + 管理后台 |

### t_user.status — 账号状态

| 值 | 含义 | 登录行为 |
|----|------|----------|
| 0 | 禁用 | 登录接口返回"账号已被禁用" |
| 1 | 正常 | 正常登录 |

### t_strategy.status — 软删状态

| 值 | 含义 | 前台可见 | 后台可见 |
|----|------|----------|----------|
| 0 | 已删除 | ❌ | ❌（DAO 默认 SQL 过滤）|
| 1 | 正常 | 视 audit_status | ✅ |

### t_strategy.audit_status — 审核状态

| 值 | 含义 | 前台可见 | 作者自己可见 | 后台可见 |
|----|------|----------|-------------|----------|
| 0 | 待审核 | ❌ | ✅（我的攻略）| ✅ |
| 1 | 已通过 | ✅ | ✅ | ✅ |
| 2 | 已拒绝 | ❌ | ✅（我的攻略显示"已拒绝"）| ✅ |

**两个状态组合场景**：

| status | audit_status | 含义 | 前台 | 后台 |
|--------|--------------|------|------|------|
| 1 | 1 | 正常已通过（最常见）| ✅ | ✅ |
| 1 | 0 | 正常待审核（刚发布）| ❌ | ✅ |
| 1 | 2 | 正常但被拒绝 | ❌ | ✅ |
| 0 | 0 | 已删除（曾经待审）| ❌ | ❌ |
| 0 | 1 | 已删除（曾经通过）| ❌ | ❌ |

> DAO 查询时 `WHERE status = 1` 已过滤掉 status=0，所以"已删除"的攻略在后台也不出现。管理员删除是硬删（DELETE FROM）还是软删（UPDATE status=0）取决于具体 DAO 实现。

---

## 🔑 约束与索引说明

### 当前脚本中的约束

| 表 | 约束名 | 类型 | 字段 | 行为 |
|----|--------|------|------|------|
| t_user | — | PRIMARY KEY | id | AUTO_INCREMENT |
| t_user | — | UNIQUE | username | 数据库层防重复 |
| t_strategy | — | PRIMARY KEY | id | AUTO_INCREMENT |
| t_strategy | fk_strategy_user | FOREIGN KEY | user_id → t_user.id | ON DELETE CASCADE |
| t_strategy | fk_strategy_category | FOREIGN KEY | category_id → t_category.id | ON DELETE SET NULL |
| t_category | — | PRIMARY KEY | id | AUTO_INCREMENT |
| t_audit_log | — | PRIMARY KEY | id | BIGINT AUTO_INCREMENT |

### 建议补充的索引（提升查询性能）

以下索引当前脚本没有，建议根据实际使用情况添加：

```sql
-- 1. 首页按创建时间倒序浏览
ALTER TABLE t_strategy ADD INDEX idx_status_audit_ctime 
    (status, audit_status, create_time DESC);

-- 2. 分类筛选
ALTER TABLE t_strategy ADD INDEX idx_category_audit 
    (category_id, audit_status, status);

-- 3. 搜索（按 game_name 或 title）
ALTER TABLE t_strategy ADD INDEX idx_game_name 
    (game_name);

-- 4. 我的攻略（按用户查）
ALTER TABLE t_strategy ADD INDEX idx_user_status 
    (user_id, status);

-- 5. 审计日志按时间 + 方法查
ALTER TABLE t_audit_log ADD INDEX idx_ctime_method 
    (create_time DESC, method(50));
```

---

## ❓ 常见问题

### Q1. 执行 init.sql 报 "Access denied; you need (at least one of) the CREATE USER privilege"

**原因**：当前 MySQL 账号没有 CREATE DATABASE 权限

**解决**：用 root 执行，或给当前账号授权：
```sql
GRANT ALL PRIVILEGES ON game_strategy_platform.* TO 'your_user'@'%';
FLUSH PRIVILEGES;
```

---

### Q2. 执行 init.sql 报 "Unknown database 'game_strategy_platform'"

**原因**：`USE game_strategy_platform` 执行时库还没建好（不太可能，脚本顺序正确）

**排查**：确认 CREATE DATABASE 语句没报错

---

### Q3. init.sql 能跑通，但 migration.sql 报 "Duplicate column name 'salt'"

**原因**：你已经跑过一遍 migration.sql 了（ALTER TABLE 重复执行）

**解决**：手动检查是否已有 salt 字段：
```sql
DESCRIBE t_user;
```
如果有 salt 列，跳过 migration.sql 就行。脚本设计时没加"列存在则跳过"的判断，建议手动跳过。

---

### Q4. migration.sql 执行后，init.sql 预置的 admin 账号 salt 列都是 NULL

**这是正确行为**。init.sql 的 3 个用户是"老用户"，salt 本来就是 NULL：
- 新注册用户：自动生成随机 salt，存 md5(pwd+salt)
- 老用户：salt=NULL，登录时 fallback 到 md5(pwd)

可以验证：
```sql
SELECT username, salt, LEFT(password, 8) FROM t_user;
```

---

### Q5. 建库时提示 "Incorrect string value: '\xF0\x9F\x92\xBB...'"

**原因**：utf8（旧版）不支持 4 字节字符（emoji、生僻字）

**解决**：确认建库时用的是 `utf8mb4`（不是 `utf8`）。脚本里已写死 utf8mb4。

---

### Q6. 外键约束 "Cannot delete or update a parent row: a foreign key constraint fails"

**场景**：想手动删除 t_category 里某个分类，但它被 t_strategy 引用

**解决**：
- 先把引用它的 t_strategy.category_id 设为 NULL（外键会自动 SET NULL）
- 或者直接 UPDATE 那些攻略的 category_id
```sql
UPDATE t_strategy SET category_id = NULL WHERE category_id = 1;
DELETE FROM t_category WHERE id = 1;
```

---

### Q7. 查看当前数据库所有表

```sql
USE game_strategy_platform;
SHOW TABLES;
```

预期输出：
```
+-------------------------------------------+
| Tables_in_game_strategy_platform          |
+-------------------------------------------+
| t_audit_log                               |
| t_category                                |
| t_strategy                                |
| t_user                                    |
+-------------------------------------------+
```

---

### Q8. 重置全部表（清空数据重跑）

```bash
# 最快方式：删库重建
mysql -u root -p -e "DROP DATABASE game_strategy_platform;"
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/migration.sql
mysql -u root -p < sql/audit.sql
```

或在 MySQL 里：
```sql
DROP DATABASE game_strategy_platform;
-- 然后重新 source 三个脚本
```

---

### Q9. 导出数据库备份

```bash
mysqldump -u root -p game_strategy_platform > backup_$(date +%Y%m%d).sql
```

只导数据（不带 CREATE TABLE）：
```bash
mysqldump -u root -p --no-create-info game_strategy_platform > data_backup.sql
```

---

### Q10. 为什么不把 audit_status 直接合到 status 里

因为两个字段代表**独立维度**：
- `status` = 数据生命周期（存在 vs 删除）
- `audit_status` = 内容审核状态（待审 vs 通过 vs 拒绝）

如果合成一个枚举 `(0已删, 1待审, 2通过, 3拒绝)`，会有业务语义冲突：
- "已删除但曾经是通过的" 这种状态无法表达
- DAO 查询"所有已删除"时需要写多个条件
- 两个独立字段 **WHERE status=1 AND audit_status=1** 更清晰

---

### Q11. TEXT 类型能存多少

| 类型 | 最大字节数 | 大致可存汉字 |
|------|-----------|-------------|
| TINYTEXT | 255 | ~80 |
| TEXT | 65,535 | ~21,000 |
| MEDIUMTEXT | 16,777,215 | ~530 万 |
| LONGTEXT | 4,294,967,295 | ~13 亿 |

当前 t_strategy.content 用的是 TEXT（65K）。如果攻略有更长内容，改用 MEDIUMTEXT。

---

### Q12. 查看表状态（行数、大小）

```sql
SELECT TABLE_NAME, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'game_strategy_platform';
```

---

## 📌 附录：脚本文件速查

| 脚本 | 路径 | 主要职责 |
|------|------|----------|
| init.sql | `sql/init.sql` | `CREATE DATABASE` + 3 张主表 CREATE + INSERT 初始数据 |
| migration.sql | `sql/migration.sql` | `ALTER TABLE t_user ADD salt` + `ALTER TABLE t_strategy ADD audit_status` + UPDATE 迁移 |
| audit.sql | `sql/audit.sql` | `CREATE TABLE t_audit_log`（独立表，AOP 写日志依赖）|

> **执行顺序不可变**：init → migration → audit
