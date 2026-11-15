# GameStrategyPlatform — 游戏攻略分享平台

> 基于 **SSM (Spring5 + SpringMVC5 + JdbcTemplate)** 的游戏攻略分享 Web 应用，
> **全程零注解**，所有 Bean / AOP / Filter / Controller 均采用纯 XML 配置，
> 是学习传统 SSM 架构安全加固实战的完整样本。

---

## 📋 目录

- [项目简介](#-项目简介)
- [技术栈](#-技术栈)
- [环境要求](#-环境要求)
- [项目结构](#-项目结构)
- [快速启动](#-快速启动)
  - [Step 1: 数据库初始化](#step-1-数据库初始化)
  - [Step 2: 修改数据源配置](#step-2-修改数据源配置)
  - [Step 3: 部署到 Tomcat](#step-3-部署到-tomcat)
  - [Step 4: 验证启动](#step-4-验证启动)
- [功能模块](#-功能模块)
- [页面一览](#-页面一览)
- [路由表](#-路由表)
- [请求处理流程](#-请求处理流程)
- [配置文件详解](#-配置文件详解)
  - [jdbc.properties](#1-jdbcproperties)
  - [applicationContext.xml](#2-applicationcontextxml)
  - [springmvc.xml](#3-springmvcxml)
  - [web.xml](#4-webxml)
- [数据库设计](#-数据库设计)
- [安全改造详解](#-安全改造详解)
  - [1. Spring AOP 审计切面](#1-spring-aop-审计切面xml-声明式)
  - [2. 三个自定义 Filter](#2-三个自定义-filterxss--csrf--越权)
  - [3. 加盐 MD5 密码改造](#3-加盐-md5-密码改造向后兼容)
  - [4. 攻略人工审核流程](#4-攻略人工审核流程)
- [过滤器链执行顺序](#-过滤器链执行顺序)
- [安全设计总览](#-安全设计总览)
- [测试账号](#-测试账号)
- [开发指南](#-开发指南)
  - [新增 Controller](#新增-controller)
  - [新增 DAO](#新增-dao)
  - [新增 JSP](#新增-jsp)
  - [新增 Filter](#新增-filter)
  - [新增 AOP 切面](#新增-aop-切面)
- [运维排障](#-运维排障)
- [License](#-license)

---

## 🎮 项目简介

GameStrategyPlatform 是一个面向游戏玩家的攻略内容分享社区：

| 角色 | 能力 |
|------|------|
| **游客** | 浏览已审核通过的攻略、按分类筛选、搜索 |
| **普通用户** | 注册/登录、发布攻略（待审核）、管理自己的攻略（查看/删除） |
| **管理员** | 用户管理（禁/启、升降级）、攻略审核（通过/拒绝）、攻略删除 |

核心业务流程：

```
用户注册 → 登录 → 发布攻略(状态:待审) → 管理员审核
                                          ├─ 通过 → 前台可见
                                          └─ 拒绝 → 前台不可见(作者可在"我的攻略"看到状态)
```

---

## 🧱 技术栈

| 分类 | 组件 | 版本 | 备注 |
|------|------|------|------|
| 核心框架 | Spring Context | 5.3.39 | 零注解，XML Bean 配置 |
| | Spring WebMVC | 5.3.39 | `SimpleUrlHandlerMapping` + `Controller` 接口 |
| | Spring JDBC | 5.3.39 | `JdbcTemplate` |
| | Spring AOP (AspectJ) | 5.3.39 + AspectJ 1.9.9.1 | XML `<aop:config>` 声明式切面 |
| 数据库 | MySQL | 8.0 / 5.7 | utf8mb4 |
| 连接池 | Druid | 1.2.20 | 阿里巴巴连接池 |
| 视图层 | JSP | 2.3.3 | 无 JSTL/TagLib 注解依赖 |
| 标签库 | JSTL Core / fmt | 1.2 | `<c:forEach>` / `<c:if>` / `<fmt:formatDate>` |
| Servlet | javax.servlet-api | 3.1.0 | provided，Tomcat 提供 |
| | javax.servlet.jsp-api | 2.3.3 | provided |
| AOP 依赖 | aspectjweaver | 1.9.9.1 | LTW 运行时织入 |
| 前端 | 原生 HTML + CSS | — | 无 Vue/React，表格+表单 |

**约束红线（本项目刻意不引入）**：
无 Redis / RocketMQ / MyBatis / Spring Boot / Spring Security / Maven / Gradle。

---

## 🖥️ 环境要求

| 组件 | 最低版本 | 推荐版本 | 备注 |
|------|----------|----------|------|
| JDK | 8 | 8 | 兼容 Java 11/17 |
| Tomcat | 9.0 | 9.0.x | Servlet 3.1，需同 Spring 5 匹配 |
| MySQL | 5.7 | 8.0 | utf8mb4 编码 |
| IDE | STS 3.9+ / IntelliJ 2020+ | STS 3.9.4 | 需安装 Spring Tools 插件 |
| 操作系统 | Windows / macOS / Linux | Windows 10+ | 项目本身跨平台 |

### 端口与路径约定

| 项目 | 默认值 | 说明 |
|------|--------|------|
| Tomcat HTTP 端口 | 8080 | 可在 `TOMCAT_HOME/conf/server.xml` 修改 |
| 项目访问 ContextPath | `/GameStrategyPlatform` | 由部署时 WAR 名决定 |
| MySQL 端口 | 3306 | 标准端口 |

---

## 📁 项目结构

```
GameStrategyPlatform/
├── README.md                  # 项目说明文档（本文件）
├── DATABASE.md                # 数据库初始化 & 表结构详细文档
├── .gitignore                 # Git 忽略规则
│
├── sql/                       # SQL 脚本（按执行顺序排列）
│   ├── init.sql               # ① 建库建表 + 初始数据
│   ├── migration.sql          # ② ALTER 加字段 + 数据迁移
│   └── audit.sql              # ③ 审计日志表
│
├── src/
│   ├── jdbc.properties        # 数据源配置（**需修改密码**）
│   └── com/
│       ├── bean/              # 实体（POJO，全部 implements Serializable）
│       │   ├── User.java          # 用户（含 salt、role、status）
│       │   ├── Strategy.java      # 攻略（含 auditStatus + 附加查询字段）
│       │   ├── Category.java      # 分类
│       │   └── AuditLog.java      # 审计日志
│       ├── dao/               # DAO 接口
│       │   ├── UserDao.java
│       │   ├── StrategyDao.java
│       │   ├── CategoryDao.java
│       │   ├── AuditLogDao.java
│       │   └── impl/          # DAO 实现（纯 JdbcTemplate，全部 XML 注入）
│       │       ├── UserDaoImpl.java
│       │       ├── StrategyDaoImpl.java
│       │       ├── CategoryDaoImpl.java
│       │       └── AuditLogDaoImpl.java
│       ├── service/           # Service 接口
│       │   ├── UserService.java
│       │   ├── StrategyService.java
│       │   ├── CategoryService.java
│       │   └── impl/          # Service 实现（含 Md5 工具、加盐水印）
│       │       ├── UserServiceImpl.java
│       │       ├── StrategyServiceImpl.java
│       │       └── CategoryServiceImpl.java
│       ├── controller/        # 15 个 Controller，全部 implements Controller
│       ├── filter/            # 3 个 Filter
│       │   ├── XssFilter.java
│       │   ├── CsrfFilter.java
│       │   └── AuthFilter.java
│       └── aspect/            # AuditAspect（纯 POJO，无 @Aspect 注解）
│
└── WebContent/                # Web 应用根目录
    ├── index.jsp                  # 欢迎页（转发到 /home）
    ├── META-INF/
    │   └── MANIFEST.MF
    └── WEB-INF/
        ├── web.xml                # Filter + Servlet + Listener 注册
        ├── applicationContext.xml # Spring 根容器（DAO/Service/AOP/Filter）
        ├── springmvc.xml          # SpringMVC 子容器（HandlerMapping + Controller）
        ├── lib/                   # 20 个 JAR 依赖
        └── pages/                 # JSP 页面
            ├── header.jspf            # 公共导航（<%@ include %> 片段）
            ├── home.jsp               # 首页 / 攻略列表
            ├── detail.jsp             # 攻略详情
            ├── login.jsp              # 登录页
            ├── register.jsp           # 注册页
            ├── publish.jsp            # 发布攻略页
            ├── my.jsp                 # 我的攻略
            ├── admin_home.jsp         # 后台控制台
            ├── admin_user.jsp         # 用户管理
            └── admin_strategy.jsp     # 攻略管理
```

### 目录职责一览

| 目录 | 职责 | Spring 容器 |
|------|------|-------------|
| `src/com/bean/` | 实体类，纯 POJO | 不注册为 Bean |
| `src/com/dao/impl/` | DAO 实现，JdbcTemplate 操作数据库 | **applicationContext.xml** 注册 |
| `src/com/service/impl/` | Service 实现，业务逻辑 + 事务边界 | **applicationContext.xml** 注册 |
| `src/com/controller/` | Controller，接收请求 → 调用 Service → 返回视图 | **springmvc.xml** 注册 |
| `src/com/filter/` | Filter，Servlet 标准接口实现 | **web.xml** 直接注册（原生 Filter；AuthFilter 在 `init()` 从 root context 取依赖） |
| `src/com/aspect/` | AOP 切面，XML `<aop:config>` 声明 | **applicationContext.xml** 注册 |

---

## 🚀 快速启动

### Step 1: 数据库初始化

数据库脚本位于 `sql/` 目录，**必须严格按顺序执行**。

```bash
# Step 1: 建库建表 + 初始数据
mysql -u root -p < sql/init.sql

# Step 2: ALTER 加字段 + 迁移已有数据
mysql -u root -p < sql/migration.sql

# Step 3: 审计日志表（可选但强烈建议执行）
mysql -u root -p < sql/audit.sql
```

> **详细数据库文档**：请阅读 [DATABASE.md](./DATABASE.md) 了解每张表的字段说明、ER 关系、初始化数据含义。

### Step 2: 修改数据源配置

打开 `src/jdbc.properties`，按你的 MySQL 环境修改：

```properties
# ====== MySQL 数据库连接 ======
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/game_strategy_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
jdbc.username=root          ← 改成你的 MySQL 账号
jdbc.password=123456        ← 改成你的 MySQL 密码
```

配置说明：

| 参数 | 作用 |
|------|------|
| `useUnicode=true` | 启用 Unicode 编码 |
| `characterEncoding=utf8` | UTF-8 编码，与数据库 utf8mb4 兼容 |
| `useSSL=false` | 本地开发环境关闭 SSL |
| `serverTimezone=Asia/Shanghai` | 时区，否则 Java DateTime 可能 8 小时偏差 |
| `allowPublicKeyRetrieval=true` | MySQL 8.0 默认认证方式需要此项 |

### Step 3: 部署到 Tomcat

#### 方式 A：STS / IntelliJ IDE

1. **配置 Tomcat Runtime**
   - STS: `Window → Preferences → Server → Runtime Environments → Add → Apache Tomcat v9.0`
   - IDEA: `Run → Edit Configurations → Tomcat Server → Local`

2. **添加项目到 Server**
   - 右键项目 → `Run As → Run on Server`
   - 选择 Tomcat 9.0 → Finish

#### 方式 B：命令行手动部署

```bash
# 打包 WAR（实际是把 WebContent 目录打成 WAR）
cd game-guide-community
# 方式 1: 使用 jar 命令打 WAR
jar -cf GameStrategyPlatform.war -C WebContent .

# 复制到 Tomcat
copy GameStrategyPlatform.war %TOMCAT_HOME%\webapps\

# 启动 Tomcat
%TOMCAT_HOME%\bin\startup.bat
```

### Step 4: 验证启动

浏览器打开：

```
http://localhost:8080/GameStrategyPlatform/
```

预期结果：
- 首页正常显示 4 条预置攻略
- 右上角"登录"链接可点击
- 控制台无异常

使用管理员账号登录验证：

| 账号 | 密码 | 预期 |
|------|------|------|
| `admin` | `123456` | 登录后出现"管理后台"入口 |
| `player1` | `123456` | 登录后出现"发布攻略"、"我的攻略" |

---

## 🧩 功能模块

| 模块 | URL 前缀 | 功能 | 权限 |
|------|----------|------|------|
| 前台浏览 | `/` `/search` `/strategy/detail` | 攻略列表、分类筛选、搜索、详情 | 游客 |
| 认证 | `/login` `/register` `/logout` `/dologin` `/doregister` | 登录/注册/退出 | 游客 |
| 用户内容 | `/strategy/publish` `/strategy/dopublish` `/my` `/my/delete` | 发布攻略、我的攻略、删除自己的攻略 | 登录用户 |
| 管理后台 | `/admin` `/admin/user` `/admin/user/status` `/admin/strategy` `/admin/strategy/audit` `/admin/strategy/delete` | 控制台、用户管理、审核、删除 | 管理员 |

### 模块详述

#### 前台浏览
- **首页**：展示 audit_status=1（已通过）的攻略，按最新发布排序
- **分类筛选**：4 个默认分类（动作游戏 / 角色扮演 / 策略模拟 / 竞技对战）
- **搜索**：按标题 / 游戏名模糊搜索，匹配已审核攻略
- **详情页**：展示单篇攻略正文，view_count 自动 +1；攻略不存在或未过审返回 404

#### 认证模块
- **注册**：校验用户名唯一性；密码生成随机 salt，存储 `md5(plain+salt)`
- **登录**：根据 salt 是否存在判断是否加盐校验（向后兼容老用户）
- **退出**：POST `/logout`（表单携带 CSRF token），销毁 session；`GET /logout` 仅重定向、不退出

#### 用户内容
- **发布**：POST 表单提交；写入时 status=1, audit_status=0（待审核）
- **我的攻略**：查询当前用户全部攻略（含待审/拒绝），展示审核状态标签
- **删除自己的攻略**：AuthFilter 资源归属校验 + CSRF Token

#### 管理后台
- **控制台**：用户数、攻略数概览
- **用户管理**：列表展示，支持禁用/启用、普通用户 ↔ 管理员（禁止对自己禁用/降级，防自我锁死）
- **攻略管理**：全量攻略列表，支持审核通过/拒绝、永久删除

---

## 🧭 页面一览

| JSP 文件 | 路由 | 说明 | 需要登录 |
|----------|------|------|----------|
| `index.jsp` | `/` | 欢迎页（meta 转发到 home） | ❌ |
| `home.jsp` | `/home` `/search` | 攻略列表，含搜索框 + 分类侧栏 | ❌ |
| `detail.jsp` | `/detail` `/strategy/detail` | 攻略详情页 | ❌ |
| `login.jsp` | `/login` | 登录表单 | ❌ |
| `register.jsp` | `/register` | 注册表单 | ❌ |
| `publish.jsp` | `/strategy/publish` | 发布攻略表单 | ✅ 普通用户 |
| `my.jsp` | `/my` | 我的攻略列表 | ✅ 普通用户 |
| `admin_home.jsp` | `/admin` | 后台控制台 | ✅ 管理员 |
| `admin_user.jsp` | `/admin/user` | 用户管理 | ✅ 管理员 |
| `admin_strategy.jsp` | `/admin/strategy` | 攻略管理 | ✅ 管理员 |

所有 JSP 通过 `<%@ include file="header.jspf" %>` 引入公共导航栏。

---

## 🧭 路由表

`springmvc.xml` 中通过 `SimpleUrlHandlerMapping` 硬编码映射：

| URL | Controller Bean | HTTP Method | 说明 |
|-----|----------------|-------------|------|
| `/` `/home` `/index` | homeController | GET | 首页 |
| `/detail` `/strategy/detail` | detailController | GET | 攻略详情 |
| `/search` | homeController | GET | 搜索/分类筛选 |
| `/login` | loginController | GET | 登录页 |
| `/dologin` | dologinController | POST | 登录提交 |
| `/register` | registerController | GET | 注册页 |
| `/doregister` | doregisterController | POST | 注册提交 |
| `/logout` | logoutController | POST | 退出登录（需 CSRF；GET 不退出） |
| `/strategy/publish` | publishController | GET | 发布页 |
| `/strategy/dopublish` | dopublishController | POST | 发布提交（需 CSRF） |
| `/my` | myController | GET | 我的攻略 |
| `/my/delete` | deleteMyController | POST | 删除自己攻略（需 CSRF + 归属校验） |
| `/admin` | adminHomeController | GET | 后台控制台 |
| `/admin/user` | adminUserController | GET | 用户列表 |
| `/admin/user/status` | adminUserStatusController | POST | 禁/启用/升降级（需 CSRF） |
| `/admin/strategy` | adminStrategyController | GET | 攻略列表 |
| `/admin/strategy/audit` | adminStrategyAuditController | POST | 审核通过/拒绝（需 CSRF） |
| `/admin/strategy/delete` | adminStrategyDeleteController | POST | 攻略删除（需 CSRF） |

---

## 🔗 请求处理流程

完整请求链路如下：

```
浏览器请求 http://localhost:8080/GameStrategyPlatform/my/delete?id=5
    │
    │ web.xml Filter 链（按 filter-mapping 顺序）
    ▼
① CharacterEncodingFilter (Spring 内置)
   └─ request.setCharacterEncoding("UTF-8")
    │
    ▼
② xssFilter (com.filter.XssFilter)
   └─ HttpServletRequestWrapper 包装
   └─ 清洗参数值中的 <script> on*= 等危险字符串
    │
    ▼
③ csrfFilter (com.filter.CsrfFilter)
   ├─ 首个 POST → session 无 token 则生成 UUID 写入 → 继续
   │   （登录/注册成功后由 Controller 在新 session 重新发放 token）
   ├─ GET 方法 → 直接放行（不创建 session、不生成 token）
   ├─ POST + 白名单(/dologin, /doregister) → 放行
   └─ POST → 校验 _csrf 参数 == sessionScope._csrf
              ├─ 匹配 → 继续
              └─ 不匹配 → 403 Forbidden
    │
    ▼
④ authFilter (com.filter.AuthFilter)
   ├─ /my/* → 未登录 → 302 到 /login
   │         → /my/delete → 直连 StrategyDao.findById 校验归属
   │            ├─ strategy.userId == session.user.id → 继续
   │            └─ 不匹配 → 403
   └─ /admin/* → session.user.role == 1 → 继续
              └─ 非管理员 → 403
    │
    ▼
⑤ DispatcherServlet (SpringMVC)
   └─ SimpleUrlHandlerMapping 匹配 URL → Controller Bean
   └─ controller.handleRequest() 返回 ModelAndView
    │
    ▼
⑥ Service 层调用
   └─ 被 AOP auditAspect (XML 声明式) 拦截
   └─ Around 通知：记录操作人、方法名、入参、耗时、异常
    │
    ▼
⑦ DAO 层 (JdbcTemplate)
   └─ 执行 SQL（不在 AOP pointcut 内，避免递归）
    │
    ▼
⑧ MySQL 数据库
   └─ 执行 → 返回结果集
    │
    ▼
⑨ AOP finally 块 → auditLogDao.insert() → t_audit_log
    │
    ▼
⑩ Controller 返回 JSP → 渲染 HTML → 浏览器
```

---

## ⚙️ 配置文件详解

### 1. jdbc.properties

**位置**：`src/jdbc.properties`

数据源属性文件，由 `applicationContext.xml` 的 `PropertyPlaceholderConfigurer` 加载。

```properties
# ====== MySQL 数据库连接 ======
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/game_strategy_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=123456
```

**可扩展参数**（Druid 连接池支持）：

```properties
# 连接池大小
jdbc.initialSize=5          # 初始化连接数
jdbc.maxActive=20           # 最大活跃连接数
jdbc.minIdle=5              # 最小空闲连接数
jdbc.maxWait=60000          # 获取连接最大等待(ms)
```

### 2. applicationContext.xml

**位置**：`WebContent/WEB-INF/applicationContext.xml`

Spring **根容器**配置，负责 DAO / Service / Filter / AOP / 数据源 的 Bean 注册。

```xml
<!-- 1. 属性文件加载 -->
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <property name="locations" value="classpath:jdbc.properties" />
</bean>

<!-- 2. Druid 数据源 -->
<bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource">
    <property name="driverClassName" value="${jdbc.driver}" />
    <property name="url" value="${jdbc.url}" />
    <property name="username" value="${jdbc.username}" />
    <property name="password" value="${jdbc.password}" />
</bean>

<!-- 3. DAO Bean（全部注入 JdbcTemplate） -->
<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
    <property name="dataSource" ref="dataSource" />
</bean>
<bean id="userDao"        class="com.dao.impl.UserDaoImpl">
    <property name="jdbcTemplate" ref="jdbcTemplate" />
</bean>
<bean id="strategyDao"    class="com.dao.impl.StrategyDaoImpl">...</bean>
<bean id="categoryDao"    class="com.dao.impl.CategoryDaoImpl">...</bean>
<bean id="auditLogDao"    class="com.dao.impl.AuditLogDaoImpl">...</bean>

<!-- 4. Service Bean -->
<bean id="userService"        class="com.service.impl.UserServiceImpl">
    <property name="userDao" ref="userDao" />
</bean>
<bean id="strategyService"    class="com.service.impl.StrategyServiceImpl">...</bean>
<bean id="categoryService"    class="com.service.impl.CategoryServiceImpl">...</bean>

<!-- 5. 审计异步线程池（有界，防每任务新建线程导致线程爆炸） -->
<bean id="auditExecutor" class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
    <property name="corePoolSize" value="2" />
    <property name="maxPoolSize" value="5" />
    <property name="queueCapacity" value="200" />
    <property name="threadNamePrefix" value="audit-" />
</bean>

<!-- 6. AOP 审计切面 -->
<bean id="auditAspect" class="com.aspect.AuditAspect">
    <property name="auditLogDao" ref="auditLogDao" />
    <property name="auditExecutor" ref="auditExecutor" />
</bean>
<aop:config>
    <aop:pointcut id="servicePointcut"
                  expression="execution(* com.service.impl.*.*(..))" />
    <aop:aspect id="auditAop" ref="auditAspect">
        <aop:around method="around" pointcut-ref="servicePointcut" />
    </aop:aspect>
</aop:config>
```

### 3. springmvc.xml

**位置**：`WebContent/WEB-INF/springmvc.xml`

SpringMVC **子容器**配置，负责 Controller 注册和 URL 映射。

```xml
<!-- 纯手动注册，不使用组件扫描（零注解约束） -->
<mvc:annotation-driven />

<!-- 视图解析器：WebContent/WEB-INF/pages/*.jsp -->
<bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    <property name="prefix" value="/WEB-INF/pages/" />
    <property name="suffix" value=".jsp" />
</bean>

<!-- Controller Bean 全部手动注册 -->
<bean id="homeController"       class="com.controller.HomeController">...</bean>
<bean id="dologinController"    class="com.controller.DoLoginController">...</bean>
<!-- ... 共 17 个 -->

<!-- URL 映射：SimpleUrlHandlerMapping -->
<bean id="handlerMapping" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
    <property name="mappings">
        <props>
            <prop key="/home">homeController</prop>
            <prop key="/">homeController</prop>
            <prop key="/detail">detailController</prop>
            <!-- ... -->
        </props>
    </property>
</bean>
```

### 4. web.xml

**位置**：`WebContent/WEB-INF/web.xml`

Servlet 规范标准配置文件，定义 Filter → Listener → Servlet 链。

```xml
<!-- ===== 编码 Filter（Spring 内置） ===== -->
<filter>
    <filter-name>characterEncodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>characterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<!-- ===== XSS Filter（原生 Filter，Tomcat 直接实例化） ===== -->
<filter>
    <filter-name>xssFilter</filter-name>
    <filter-class>com.filter.XssFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>xssFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<!-- ===== CSRF Filter ===== -->
<!-- 同上，filter-class=com.filter.CsrfFilter -->

<!-- ===== 越权 Filter（filter-class=com.filter.AuthFilter） ===== -->
<!-- 映射仅 /my/* 和 /admin/* -->
<filter-mapping>
    <filter-name>authFilter</filter-name>
    <url-pattern>/my/*</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>authFilter</filter-name>
    <url-pattern>/admin/*</url-pattern>
</filter-mapping>

<!-- ===== Spring Context Listener ===== -->
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>/WEB-INF/applicationContext.xml</param-value>
</context-param>

<!-- ===== SpringMVC DispatcherServlet ===== -->
<servlet>
    <servlet-name>springmvc</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/springmvc.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>springmvc</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

**Filter 依赖获取方式**：web.xml 里配的是原生 Filter 类名，由 Tomcat 直接实例化。XssFilter / CsrfFilter 无依赖，开箱即用；AuthFilter 需要 StrategyDao，它在 `init()` 中通过 `WebApplicationContextUtils.getWebApplicationContext()` 从 Spring root context 手动取 Bean（取不到时后续 /my/delete 请求 fail-closed 返回 500）。

---

## 🗄️ 数据库设计

> 📌 **详细数据库文档**（表结构、字段说明、ER 图、初始化数据）请查看 [DATABASE.md](./DATABASE.md)

### 执行顺序（必须严格遵守）

```
① sql/init.sql       → 建库 game_strategy_platform + 建 3 张主表 + 初始数据
② sql/migration.sql  → ALTER TABLE 加 salt / audit_status + 数据迁移
③ sql/audit.sql      → 建审计日志表（AOP 写日志依赖）
```

### 涉及表（共 4 张）

| 表名 | 中文名 | 说明 |
|------|--------|------|
| `t_user` | 用户表 | 含 salt（加盐 MD5）、role（0用户/1管理员）、status（0禁用/1正常） |
| `t_category` | 分类表 | 4 条默认分类 |
| `t_strategy` | 攻略表 | 含 status（软删）、audit_status（0待审/1通过/2拒绝）|
| `t_audit_log` | 审计日志表 | AOP 自动写入，service 层每次方法调用一条 |

### ER 关系图

```
                    ┌─────────────────┐
                    │    t_category    │
                    │ id (PK)          │
                    │ name             │
                    └────────┬──────────┘
                             │
                ON DELETE SET NULL
                             │
┌─────────────────┐     ┌────┴──────────┐
│     t_user      │     │   t_strategy   │
│ id (PK)◄────────┼─────┤ user_id (FK)   │
│ username        │     │ category_id(FK)│
│ password+salt   │     │ audit_status   │
│ role            │     │ status         │
│ status          │     │ title/content  │
└─────────────────┘     └────────────────┘

                    ┌─────────────────┐
                    │   t_audit_log    │  (独立，无外键)
                    │ id (BIGINT PK)   │
                    │ operator        │
                    │ method          │
                    │ duration_ms     │
                    └─────────────────┘
```

### 状态字段对照表

#### t_user.role — 用户角色

| 值 | 含义 |
|----|------|
| 0 | 普通用户 |
| 1 | 管理员 |

#### t_user.status — 用户状态

| 值 | 含义 |
|----|------|
| 0 | 禁用（登录时会拦截） |
| 1 | 正常 |

#### t_strategy.status — 攻略软删

| 值 | 含义 |
|----|------|
| 0 | 已删除 |
| 1 | 正常 |

#### t_strategy.audit_status — 审核状态

| 值 | 含义 | 前台可见 | 后台可见 |
|----|------|----------|----------|
| 0 | 待审核 | ❌ | ✅ |
| 1 | 已通过 | ✅ | ✅ |
| 2 | 已拒绝 | ❌ | ✅（作者自己可见）|

**注意**：`status` 和 `audit_status` 是**两个独立维度**，互不干扰：
- `status` 只管数据生命周期（删除/正常）
- `audit_status` 只管内容审核（待审/通过/拒绝）

---

## 🔧 安全改造详解

### 1. Spring AOP 审计切面（XML 声明式）

**目标**：拦截所有 Service 层方法，自动记录操作人、目标方法、入参、耗时、是否成功。

**核心文件**：
- `src/com/aspect/AuditAspect.java` — 纯 POJO，无任何注解
- `src/com/dao/impl/AuditLogDaoImpl.java` — 写入 t_audit_log（**不在 AOP pointcut 中**，避免递归）
- `sql/audit.sql` — 审计日志表

**applicationContext.xml 配置**：

```xml
<aop:config>
    <!-- 只拦截 Service 包，DAO 包不受影响（避免递归） -->
    <aop:pointcut id="servicePointcut"
                  expression="execution(* com.service.impl.*.*(..))" />
    <aop:aspect id="auditAop" ref="auditAspect">
        <aop:around method="around" pointcut-ref="servicePointcut" />
    </aop:aspect>
</aop:config>
```

**切面核心逻辑**：

```java
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.currentTimeMillis();
    AuditLog log = new AuditLog();
    fillOperator(log);           // RequestContextHolder → session → loginUser
    log.setMethod(pjp.getTarget().getClass().getName() + "." + pjp.getSignature().getName());
    log.setParams(buildParams(pjp.getArgs()));

    Object result;
    try {
        result = pjp.proceed();  // 执行业务方法
        log.setSuccess(1);
    } catch (Throwable t) {
        log.setSuccess(0);
        log.setErrorMsg(t.getMessage());
        throw t;                 // 异常继续抛出，不吞掉
    } finally {
        log.setDurationMs((int)(System.currentTimeMillis() - start));
        auditLogDao.insert(log); // finally 中写，静默失败（不让日志影响业务）
    }
    return result;
}
```

**为什么 AuditLogDao 不被拦截**？
pointcut 只匹配 `com.service.impl.*`，DAO 包不在范围内，避免 DAO 写日志时再次被 AOP 拦截 → 无限递归。

---

### 2. 三个自定义 Filter（XSS / CSRF / 越权）

#### 2.1 XssFilter — XSS 输入过滤

| 属性 | 说明 |
|------|------|
| 文件 | `src/com/filter/XssFilter.java` |
| 注册位置 | web.xml（原生 Filter，Tomcat 直接实例化） |
| 作用范围 | 全部 URL `/*` |
| 技术 | HttpServletRequestWrapper 包装 + 正则清洗 |

**清洗策略**：
- 内部类 `XssRequestWrapper` 继承 `HttpServletRequestWrapper`
- 覆盖方法：`getParameter` / `getParameterValues` / `getHeader` / `getQueryString` / `getRequestURI`
- 12 条危险正则：`<script` / `javascript:` / `vbscript:` / `on[a-z]+=` / `<iframe` / `<link` / `expression(` 等
- 基础字符替换：`<` `>` `"` `'` → 对应全角字符（阻断标签闭合）

#### 2.2 CsrfFilter — CSRF Token 校验

| 属性 | 说明 |
|------|------|
| 文件 | `src/com/filter/CsrfFilter.java` |
| 作用范围 | 全部 URL `/*` |
| Token 来源 | 首个 POST 请求时（session 无 token）生成 UUID 存入 `session._csrf`；登录/注册成功后 Controller 在新 session 中重新发放 |
| 校验规则 | GET / HEAD → 放行（不创建 session）；POST / PUT / DELETE 必须带 `_csrf` 参数 |
| 白名单 | `/dologin`、`/doregister`（提交时 session 中还没有有效 token） |
| 校验失败 | `403 Forbidden` |

**前端表单写法**（以 publish.jsp 为例）：

```html
<form action="/GameStrategyPlatform/strategy/dopublish" method="post">
    <!-- CSRF Token -->
    <input type="hidden" name="_csrf" value="${sessionScope._csrf}" />

    <label>标题:</label>
    <input name="title" required />
    <!-- ... -->
</form>
```

#### 2.3 AuthFilter — 越权访问拦截

| 属性 | 说明 |
|------|------|
| 文件 | `src/com/filter/AuthFilter.java` |
| 作用范围 | 仅 `/my/*` 和 `/admin/*` |
| 注入依赖 | StrategyDao（`init()` 中通过 WebApplicationContextUtils 从 root context 手动获取，用于归属校验） |

**校验逻辑**：

| URL | 校验规则 | 失败响应 |
|-----|----------|----------|
| `/my/*`（GET） | session 有 loginUser | 302 跳 `/login` |
| `/my/delete`（POST） | loginUser.id == strategy.userId | 403 "该攻略不属于您" |
| `/admin/*` | loginUser.role == 1 | 403 |

**为什么 /my/delete 不直接信任用户**？
恶意用户可以把表单里的 `id=其他用户的攻略ID` 硬改掉直接提交，所以 Filter 必须**直连 DAO 查一次 strategy.userId**，确保资源归属。

#### Filter 如何拿到 Spring 的 Bean？

普通 Filter 由 web.xml 反射实例化，无法享受 Spring 的依赖注入。本项目做法：无依赖的 XssFilter / CsrfFilter 直接写类名由 Tomcat 实例化；AuthFilter 需要 StrategyDao，在 `init()` 里通过 `WebApplicationContextUtils.getWebApplicationContext(getServletContext())` 拿到 root context，再手动 `getBean("strategyDao")`。

---

### 3. 加盐 MD5 密码改造（向后兼容）

**Schema 变化**：`t_user` 新增 `salt VARCHAR(32) NULL`

**向后兼容 login 逻辑**：

```java
User u = userDao.findByUsername(username);

// salt 存在 → md5(plain + salt)；salt 为空 → 纯 md5（老用户兼容）
String expected = (u.getSalt() != null && !u.getSalt().isEmpty())
                    ? md5(password + u.getSalt())
                    : md5(password);

return expected.equals(u.getPassword());
```

| 场景 | salt 状态 | 存储公式 | 登录校验 |
|------|-----------|----------|----------|
| 新注册用户 | 随机 16 字节 hex (32 字符) | `md5(plain + salt)` | `md5(plain + salt) == db.password` |
| 老用户（init.sql 预置） | NULL | `md5(plain)` | fallback：`md5(plain) == db.password` |

**关键文件**：
- `src/com/service/impl/UserServiceImpl.java` — `generateSalt()` / `hashPassword()` / `register()` / `login()`
- `src/com/dao/impl/UserDaoImpl.java` — INSERT / UPDATE_PWD 带 salt 列

**安全性提升**：攻击者拿到数据库后，无法用彩虹表反推密码；同样的明文密码，不同用户生成的 hash 完全不同。

---

### 4. 攻略人工审核流程

**Schema 变化**：`t_strategy` 新增 `audit_status TINYINT(1) DEFAULT 1`

**字段分工**：
- `status`：只管软删（0已删 / 1正常）
- `audit_status`：只管审核（0待审 / 1通过 / 2拒绝）

**SQL 层面的可见性控制**：

| 场景 | SQL 条件 | 效果 |
|------|----------|------|
| 前台列表/详情 | `WHERE ... AND audit_status = 1 AND status = 1` | 只看已审核的 |
| 我的攻略 | `WHERE user_id = ? AND status = 1`（**不加 audit_status**）| 作者能看到自己所有状态 |
| 管理员列表 | `WHERE status = 1`（**不加 audit_status**）| 管理员看全部 |

**发布流程**：DAO INSERT 写死 `(..., status, audit_status) VALUES(..., 1, 0)` → 新攻略默认**待审核**。

**审核操作**：`POST /admin/strategy/audit?id=N&action=approve|reject`

**migration.sql 数据迁移**：init.sql 预置的 4 条攻略统一 UPDATE audit_status=1，保持迁移后仍可见。

---

## 🔗 过滤器链执行顺序

```
HTTP Request
    │
    ▼
① CharacterEncodingFilter (Spring 内置) — UTF-8 编码
    │
    ▼
② xssFilter  ── HttpServletRequestWrapper 包装
                ├─ getParameter() / getHeader() / getQueryString() / getRequestURI()
                └─ 正则 + 全角化清洗所有参数
    │
    ▼
③ csrfFilter ── session 生成/校验 _csrf token
                ├─ 首个 POST → session 无 token 则生成 UUID 写入 → 继续
                │   （登录/注册成功后由 Controller 在新 session 重新发放 token）
                ├─ GET / HEAD → 放行（不创建 session）
                ├─ POST + 白名单(/dologin, /doregister) → 放行
                └─ 其他 POST → 校验 _csrf 参数 == session._csrf → 403
    │
    ▼
④ authFilter ── 仅 /my/* /admin/* 进入
                ├─ /my/* → 校验 session 有 loginUser
                │         └─ /my/delete → StrategyDao.findById 校验归属
                └─ /admin/* → 校验 loginUser.role == 1
                               └─ 非管理员 → 403
    │
    ▼
⑤ DispatcherServlet → SimpleUrlHandlerMapping → Controller.handleRequest()
    │
    ▼
⑥ Service 层 → 被 AOP auditAspect Around 拦截（XML 声明式）
    │
    ▼
⑦ DAO (JdbcTemplate) → MySQL（DAO 不在 pointcut 内）
    │
    ▼
⑧ finally 块 auditExecutor 异步 auditLogDao.insert() → t_audit_log
    │
    ▼
⑨ Controller 返回 ModelAndView → JSP 渲染 → Response
```

---

## 🔐 安全设计总览

| 维度 | 方案 | 代码位置 | 防护目标 |
|------|------|----------|----------|
| 输入安全 | XSS Filter（正则 + 全角化替换） | `filter/XssFilter.java` | 反射型 XSS、存储型 XSS |
| CSRF | session UUID token + POST 校验 + 白名单 | `filter/CsrfFilter.java` | 跨站请求伪造 |
| 水平越权 | /my/delete 资源归属校验 | `filter/AuthFilter.java` | A 用户删除 B 用户内容 |
| 垂直越权 | /admin/* role==1 校验 | `filter/AuthFilter.java` | 普通用户访问管理后台 |
| 密码存储 | 加盐 MD5（SecureRandom salt） | `service/impl/UserServiceImpl.java` | 彩虹表、数据库拖库反查 |
| 暴力破解 | 连续失败 5 次锁定 15 分钟（JVM 内存计数） | `UserServiceImpl.isLoginLocked()` | 线上爆破 |
| 会话固定 | 登录/注册成功后 invalidate 旧 session 并重建（同时补发 CSRF token） | `DoLoginController` / `DoRegisterController` | Session Fixation |
| 操作审计 | AOP XML 切面自动记录 | `aspect/AuditAspect.java` | 管理员操作追踪 |
| 内容安全 | 发布默认待审核 | `StrategyDaoImpl` SQL + 审核 Controller | 用户发布垃圾内容/违规内容 |
| SQL 注入 | JdbcTemplate PreparedStatement | 所有 DAO | 拼接 SQL |
| 依赖注入安全 | 无依赖 Filter 由 Tomcat 直接实例化；AuthFilter init() 手动取 Bean，取不到 fail-closed | web.xml + `filter/AuthFilter.java` | Filter 实例化顺序问题 |
| 异常防护 | DAO 层静默失败写审计日志 | AuditAspect.finally | 日志本身影响业务 |

---

## 🔑 测试账号

init.sql 预置 3 个"老用户"（salt=NULL，纯 md5 兼容）：

| 角色 | 账号 | 密码 | salt | 登录校验方式 |
|------|------|------|------|-------------|
| 超级管理员 | `admin` | `123456` | NULL | 纯 md5 = `e10adc3949ba59abbe56e057f20f883e` |
| 普通用户 | `player1` | `123456` | NULL | 纯 md5 |
| 普通用户 | `player2` | `123456` | NULL | 纯 md5 |

> **新注册账号**：系统自动生成随机 salt（16 字节 hex = 32 字符），密码存为 `md5(plain + salt)`。

---

## 🛠️ 开发指南

### 新增 Controller

**场景**：新增一个 `/strategy/like` 点赞接口。

**Step 1**：编写 Controller 类

```java
// src/com/controller/LikeController.java
public class LikeController implements Controller {
    private StrategyService strategyService;
    public void setStrategyService(StrategyService s) { this.strategyService = s; }

    @Override
    public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp) {
        Integer strategyId = Integer.valueOf(req.getParameter("id"));
        strategyService.like(strategyId);
        return new ModelAndView("redirect:/strategy/detail?id=" + strategyId);
    }
}
```

**Step 2**：在 `applicationContext.xml` 注册 Service Bean（如果还没）

```xml
<bean id="strategyService" class="com.service.impl.StrategyServiceImpl">
    <property name="strategyDao" ref="strategyDao" />
</bean>
```

**Step 3**：在 `springmvc.xml` 注册 Controller Bean + 映射

```xml
<bean id="likeController" class="com.controller.LikeController">
    <property name="strategyService" ref="strategyService" />
</bean>

<!-- 在 SimpleUrlHandlerMapping 的 <props> 里加 -->
<prop key="/strategy/like">likeController</prop>
```

**Step 4**：CSRF 保护（如果是 POST）

在对应 JSP 的表单里加 `<input type="hidden" name="_csrf" value="${sessionScope._csrf}" />`

---

### 新增 DAO

**Step 1**：写接口（`src/com/dao/CommentDao.java`）
```java
public interface CommentDao {
    List<Comment> findByStrategyId(Integer strategyId);
    int insert(Comment comment);
}
```

**Step 2**：写实现类（`src/com/dao/impl/CommentDaoImpl.java`）
```java
public class CommentDaoImpl implements CommentDao {
    private JdbcTemplate jdbcTemplate;
    public void setJdbcTemplate(JdbcTemplate t) { this.jdbcTemplate = t; }
    // ... 实现
}
```

**Step 3**：`applicationContext.xml` 注册
```xml
<bean id="commentDao" class="com.dao.impl.CommentDaoImpl">
    <property name="jdbcTemplate" ref="jdbcTemplate" />
</bean>
```

---

### 新增 JSP

**Step 1**：创建文件 `WebContent/WEB-INF/pages/newpage.jsp`

**Step 2**：引入公共头
```jsp
<%@ include file="header.jspf" %>
<!-- 业务内容 -->
<%@ include file="footer.jspf" %>
```

**Step 3**：通过 Controller 返回视图名 `"newpage"`，SpringMVC 视图解析器会自动拼 `/WEB-INF/pages/newpage.jsp`

**Step 4**：CSRF Token（所有 POST 表单都要加）
```html
<input type="hidden" name="_csrf" value="${sessionScope._csrf}" />
```

---

### 新增 Filter

**Step 1**：编写 Filter 类（实现 `javax.servlet.Filter`，原生接口，不加注解）

**Step 2**：在 `web.xml` 直接注册类名（本项目 Filter 均为原生 Filter，由 Tomcat 实例化）
```xml
<filter>
    <filter-name>myFilter</filter-name>
    <filter-class>com.filter.MyFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>myFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

**Step 3（可选）**：若 Filter 需要 DAO / Service 依赖，在 `init()` 中从 Spring root context 手动获取（参考 `AuthFilter.init()`）：
```java
WebApplicationContext ctx = WebApplicationContextUtils
        .getWebApplicationContext(filterConfig.getServletContext());
this.someDao = (SomeDao) ctx.getBean("someDao");
```

**注意**：filter-mapping 的顺序决定执行顺序，Spring 内置 CharacterEncodingFilter 应在最前。

---

### 新增 AOP 切面

**Step 1**：写 POJO 类（不要加 `@Aspect`，用 XML）
```java
public class MyAspect {
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("Before: " + pjp.getSignature());
        Object result = pjp.proceed();
        System.out.println("After: " + pjp.getSignature());
        return result;
    }
}
```

**Step 2**：`applicationContext.xml` 注册
```xml
<bean id="myAspect" class="com.aspect.MyAspect" />

<aop:config>
    <aop:pointcut id="myPointcut" expression="execution(* com.service.impl.*.*(..))" />
    <aop:aspect id="myAop" ref="myAspect">
        <aop:around method="around" pointcut-ref="myPointcut" />
    </aop:aspect>
</aop:config>
```

**避免递归**：pointcut 不要包含 AuditLogDao 或写日志的类，否则会无限递归。

---

## 🩺 运维排障

### Q1. 启动后首次调用 Service 报错

```
Table 'game_strategy_platform.t_audit_log' doesn't exist
```

**原因**：没执行 `sql/audit.sql`

**解决**：按顺序重新执行三个 SQL：init → migration → audit

---

### Q2. 登录成功后 POST 提交报 403 Forbidden

**原因**：CSRF Token 校验失败

**排查步骤**：
1. 检查 JSP 表单里有没有 `<input type="hidden" name="_csrf" value="${sessionScope._csrf}" />`
2. 用浏览器 F12 → Network → 看 POST 请求 Payload 里有没有 `_csrf` 参数
3. 如果 session 过期，刷新页面让表单带上最新 token 再提交（GET 不会生成 token；登录/注册成功后 Controller 会在新 session 中自动发放 token）

---

### Q3. 老用户 admin / player1 登录失败

**原因**：migration.sql 未执行（salt 字段不存在）

**排查**：
```sql
USE game_strategy_platform;
DESCRIBE t_user;          -- 看有没有 salt 列
SELECT username, salt FROM t_user;
```

**修复**：
```sql
-- 直接重置 admin 密码
UPDATE t_user SET password = 'e10adc3949ba59abbe56e057f20f883e', salt = NULL WHERE username = 'admin';
```

---

### Q4. STS 中项目有红色 X

**检查项**：

1. `Project → Properties → Java Build Path → Libraries` → 添加 Tomcat 9 Runtime
2. `Project → Properties → Project Facets` → Dynamic Web Module 3.1
3. `Project → Clean`
4. 确认 `WebContent/WEB-INF/lib/` 下 JAR 没有被误删（20 个）

---

### Q5. 控制台乱码 / 编译报 "不可映射字符"

**原因**：文件编码非 UTF-8

**解决**：
- STS: `Window → Preferences → General → Workspace → Text file encoding = UTF-8`
- javac 加 `-encoding UTF-8`
- Tomcat 启动脚本 `JAVA_OPTS` 加 `-Dfile.encoding=UTF-8`

---

### Q6. 访问 /my/delete?id=5 提示 403 "该攻略不属于您"

**原因**：AuthFilter 资源归属校验拦截（player2 发的攻略，player1 去删）

**这是正常行为**（水平越权防护）。如果你是管理员，应该用 `/admin/strategy/delete` 接口删除。

---

### Q7. 攻略发布后前台看不到

**原因**：新攻略默认 audit_status=0（待审核）

**解决**：用管理员账号登录 → `/admin/strategy` → 找到待审核攻略 → 点"通过"

---

### Q8. 审计日志表没有新记录

**排查**：
```sql
SELECT operator, method, success, create_time
FROM t_audit_log ORDER BY create_time DESC LIMIT 20;
```
如果表是空的：
1. 确认 `sql/audit.sql` 已执行
2. 看 Tomcat 启动日志有没有 `auditAspect` 相关的 AOP 织入错误
3. 确认 `applicationContext.xml` 的 `<aop:config>` 没被注释

---

### Q9. MySQL 8.0 报 Public Key Retrieval 错误

**原因**：MySQL 8 默认 caching_sha2_password 认证

**解决**：jdbc.url 必须包含 `allowPublicKeyRetrieval=true`：
```
jdbc:mysql://localhost:3306/game_strategy_platform?...&allowPublicKeyRetrieval=true
```

---

### Q10. 端口 8080 被占用

```powershell
# Windows 查看占用
netstat -ano | findstr :8080

# 杀掉进程（PID 用上一步结果）
taskkill /PID <PID> /F
```

---

### Q11. 如何查看审计日志

```sql
USE game_strategy_platform;

-- 最近 20 条
SELECT operator, nickname, method, duration_ms, success, create_time
FROM t_audit_log ORDER BY create_time DESC LIMIT 20;

-- 谁删了攻略？
SELECT * FROM t_audit_log 
WHERE method LIKE '%StrategyService%delete%'
ORDER BY create_time DESC;
```

---

### Q12. 为什么 AOP 不拦截 AuditLogDao

pointcut 表达式是 `execution(* com.service.impl.*.*(..))`，只匹配 Service 包。DAO 包不在范围内，这是**刻意设计**的——避免 DAO 写日志时再次被 AOP 拦截 → 无限递归。

---

## 📄 License

本项目仅用于学习交流，禁止商用。
