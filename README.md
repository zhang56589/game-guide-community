# GameStrategyPlatform — 游戏攻略分享平台

> 基于 **SSM (Spring5 + SpringMVC5 + JdbcTemplate)** 的游戏攻略分享 Web 应用，**全程零注解**，所有 Bean / AOP / Filter / Controller 均采用纯 XML 配置，是学习传统 SSM 架构安全加固实战的完整样本。

---

## 📋 目录

- [项目简介](#-项目简介)
- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [数据库部署](#-数据库部署)
- [快速启动](#-快速启动)
- [功能模块](#-功能模块)
- [路由表](#-路由表)
- [四项安全改造详解](#-四项安全改造详解)
  - 1. Spring AOP 审计切面（XML 声明式）
  - 2. 3 个自定义 Filter（XSS / CSRF / 越权）
  - 3. 加盐 MD5 密码改造（向后兼容）
  - 4. 攻略人工审核流程
- [过滤器链执行顺序](#-过滤器链执行顺序)
- [安全设计总览](#-安全设计总览)
- [测试账号](#-测试账号)
- [FAQ](#-faq)

---

## 🎮 项目简介

GameStrategyPlatform 是一个面向游戏玩家的攻略内容分享社区：

| 角色 | 能力 |
|------|------|
| 游客 | 浏览已审核通过的攻略、按分类筛选、搜索 |
| 普通用户 | 注册/登录、发布攻略（待审核）、管理自己的攻略 |
| 管理员 | 用户管理（禁/启、升降级）、攻略审核（通过/拒绝）、攻略删除 |

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
| 前端 | 原生 HTML + CSS | — | 无 Vue/React，表格+表单 |

**约束红线**：无 Redis / RocketMQ / MyBatis / Spring Boot / Spring Security。

---

## 📁 项目结构

```
GameStrategyPlatform/
├── sql/
│   ├── init.sql            # 建库建表 + 初始数据（优先执行）
│   ├── migration.sql       # ALTER 加字段（在 init.sql 之后执行）
│   └── audit.sql           # 审计日志表（单独执行）
│
├── src/
│   ├── jdbc.properties     # 数据源配置（需改密码）
│   └── com/
│       ├── bean/           # 实体（User, Strategy, Category, AuditLog）
│       ├── dao/            # DAO 接口 + impl
│       │   └── impl/       #   （纯 JdbcTemplate，全部 XML 注入）
│       ├── service/        # Service 接口 + impl
│       │   └── impl/       #   （Md5 工具、加盐水印）
│       ├── controller/     # 15 个 Controller，全部 implements Controller
│       ├── filter/         # XssFilter / CsrfFilter / AuthFilter
│       └── aspect/         # AuditAspect（纯 POJO，无 @Aspect）
│
└── WebContent/
    ├── WEB-INF/
    │   ├── web.xml                          # Filter + Servlet + Listener
    │   ├── applicationContext.xml           # Spring 容器（DAO/Service/AOP/Filter）
    │   ├── springmvc.xml                    # MVC 配置（HandlerMapping + Controller）
    │   ├── lib/                             # 20 个 JAR 依赖
    │   └── pages/                           # JSP 页面
    │       ├── header.jspf                  # 公共导航（include 片段）
    │       ├── home.jsp / detail.jsp
    │       ├── login.jsp / register.jsp
    │       ├── publish.jsp / my.jsp
    │       ├── admin_home.jsp / admin_user.jsp / admin_strategy.jsp
    │       └── index.jsp                    # 转发到 /home
    └── index.jsp                            # 欢迎页
```

---

## 🗄️ 数据库部署

### 1. 先建库（执行顺序**严格**）

```bash
# Step 1: 建库建表 + 初始数据
mysql -u root -p < sql/init.sql

# Step 2: ALTER 加字段 + 迁移已有数据
mysql -u root -p < sql/migration.sql

# Step 3: 审计日志表（可选但建议执行，否则 AOP 写日志会报错）
mysql -u root -p < sql/audit.sql
```

### 2. ER 关系

```
t_category (分类)
    │
    ├─── t_strategy (攻略) ────── t_user (作者)
    │        │                              │
    │        │ FK user_id                   │ PK id
    │        │ FK category_id               │ role: 0用户/1管理员
    │        │                              │ salt: 随机盐值(加盐MD5)
    │        status:     0已删 / 1正常
    │        audit_status: 0待审 / 1通过 / 2拒绝
    │
    └─── t_audit_log (审计日志，AOP 自动写入)
             operator (操作人)
             method   (类名.方法名)
             duration_ms (耗时)
             success  (是否成功)
```

### 3. 改数据库连接

打开 `src/jdbc.properties`：

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/game_strategy_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
jdbc.username=root        ← 改成你自己的账号
jdbc.password=root        ← 改成你自己的密码
```

---

## 🚀 快速启动

1. **数据库**：按 [上一节](#-数据库部署) 执行三个 SQL 脚本
2. **改密码**：`src/jdbc.properties` 中用户名/密码匹配你的 MySQL
3. **部署**：
   - IDE：STS / IntelliJ → 右键项目 → Run on Server → Tomcat 9.x
   - 命令行：把 `WebContent` 目录打包 WAR 放入 `TOMCAT_HOME/webapps/`
4. **访问**：浏览器打开 `http://localhost:8080/GameStrategyPlatform/`

---

## 🧩 功能模块

| 模块 | 路径前缀 | 功能 |
|------|----------|------|
| 前台浏览 | `/`, `/search`, `/strategy/detail` | 攻略列表、分类筛选、搜索、详情 |
| 认证 | `/login`, `/register`, `/logout`, `/dologin`, `/doregister` | 登录/注册/退出 |
| 用户内容 | `/strategy/publish`, `/strategy/dopublish`, `/my`, `/my/delete` | 发布攻略、我的攻略、删除 |
| 管理后台 | `/admin`, `/admin/user`, `/admin/user/status`, `/admin/strategy`, `/admin/strategy/audit`, `/admin/strategy/delete` | 控制台、用户管理、审核、删除 |

---

## 🧭 路由表

`springmvc.xml` 中通过 `SimpleUrlHandlerMapping` 硬编码映射：

| URL | Controller Bean | 说明 |
|-----|----------------|------|
| `/` `/home` `/index` | homeController | 首页 |
| `/detail` `/strategy/detail` | detailController | 攻略详情 |
| `/search` | homeController | 搜索/分类 |
| `/login` | loginController | 登录页 |
| `/dologin` | dologinController | 登录提交 |
| `/register` | registerController | 注册页 |
| `/doregister` | doregisterController | 注册提交 |
| `/logout` | logoutController | 退出 |
| `/strategy/publish` | publishController | 发布页（需登录） |
| `/strategy/dopublish` | dopublishController | 发布提交（需登录+CSRF） |
| `/my` | myController | 我的攻略（需登录+越权校验） |
| `/my/delete` | deleteMyController | 删除自己的攻略（需登录+资源归属+CSRF） |
| `/admin` | adminHomeController | 后台控制台（管理员） |
| `/admin/user` | adminUserController | 用户列表（管理员） |
| `/admin/user/status` | adminUserStatusController | 禁/启用/升降级（管理员+CSRF） |
| `/admin/strategy` | adminStrategyController | 攻略列表（管理员） |
| `/admin/strategy/audit` | adminStrategyAuditController | 审核通过/拒绝（管理员+CSRF） |
| `/admin/strategy/delete` | adminStrategyDeleteController | 攻略删除（管理员+CSRF） |

---

## 🔧 四项安全改造详解

### 1. Spring AOP 审计切面（XML 声明式）

**目标**：拦截所有 Service 层方法，自动记录操作人、目标方法、入参、耗时、是否成功。

**核心文件**：
- `src/com/aspect/AuditAspect.java` — 纯 POJO，无任何注解
- `src/com/dao/impl/AuditLogDaoImpl.java` — 写入 t_audit_log（**不在 AOP pointcut 中**，避免递归）
- `sql/audit.sql` — 审计日志表

**applicationContext.xml 配置片段**：

```xml
<bean id="auditAspect" class="com.aspect.AuditAspect">
    <property name="auditLogDao" ref="auditLogDao" />
</bean>

<aop:config>
    <!-- 拦截 Service impl 所有 public 方法 -->
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
    fillOperator(log);               // RequestContextHolder → session → loginUser
    log.setMethod(pjp.getTarget().getClass().getName() + "." + pjp.getSignature().getName());
    log.setParams(buildParams(pjp.getArgs()));

    Object result;
    try { result = pjp.proceed(); }
    catch (Throwable t) { /* 记 error_msg，继续抛出 */ throw t; }
    finally {
        log.setDurationMs((int)(System.currentTimeMillis() - start));
        auditLogDao.insert(log);     // finally 中写，静默失败
    }
    return result;
}
```

**为什么 AuditLogDao 不被拦截**：pointcut 只匹配 `com.service.impl.*`，DAO 包不受影响。

---

### 2. 3 个自定义 Filter

#### 2.1 XssFilter — XSS 输入过滤

- `src/com/filter/XssFilter.java`
- 内部类 `XssRequestWrapper` 继承 `HttpServletRequestWrapper`
- 覆盖 `getParameter / getParameterValues / getHeader / getQueryString / getRequestURI`
- 12 条危险正则（script/VBScript/on*=iframe/link/expression 等）+ `< > " '` 基本转义

#### 2.2 CsrfFilter — CSRF Token 校验

- `src/com/filter/CsrfFilter.java`
- 首次请求 → 生成 UUID 存入 `session._csrf`
- GET / HEAD 直接放行；POST / PUT / DELETE 必须带 `_csrf` 参数（form hidden input）
- 白名单 `/dologin` `/doregister`（还没 session token 就提交）
- token 不匹配 → `403 Forbidden`

#### 2.3 AuthFilter — 越权访问

- `src/com/filter/AuthFilter.java`
- 映射仅 `/my/*` 和 `/admin/*`（其他路径不受影响）
- `/my/*`：未登录 → 302 登录页；`/my/delete?id=N` 时直连 `StrategyDao.findById()` 校验 `strategy.userId == loginUser.id`
- `/admin/*`：必须 `loginUser.role == 1`

#### web.xml 注册（全部用 DelegatingFilterProxy）

```xml
<filter>
    <filter-name>xssFilter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    <init-param><param-name>targetBeanName</param-name><param-value>xssFilter</param-value></init-param>
</filter>
<filter-mapping><filter-name>xssFilter</filter-name><url-pattern>/*</url-pattern></filter-mapping>
<!-- csrfFilter / authFilter 同理 -->
```

用 `DelegatingFilterProxy` 让 Spring 管理 Filter，实现 setter 注入 Bean（比如 AuthFilter 注入 StrategyDao）。

---

### 3. 加盐 MD5 密码改造

**schema 变化**：`t_user` 新增 `salt VARCHAR(32) NULL`

**向后兼容 login 逻辑**：

```java
User u = userDao.findByUsername(username); // 先拿用户（含 salt）
// salt 存在 → md5(plain + salt)；salt 为空 → 纯 md5（老用户）
String expected = (u.getSalt() != null && !u.getSalt().isEmpty())
                    ? md5(password + u.getSalt())
                    : md5(password);
```

- **新注册**：`SecureRandom` 生成 16 字节随机 salt → md5(pwd+salt) → 入库
- **老用户**：salt=NULL → login 自动 fallback 纯 md5（init.sql 预置的 admin / player1 / player2 均可正常登录）

**关键文件**：
- `src/com/service/impl/UserServiceImpl.java` — `generateSalt()` / `hashPassword()` / `register()` / `login()`
- `src/com/dao/impl/UserDaoImpl.java` — INSERT / UPDATE_PWD 带 salt 列

---

### 4. 攻略人工审核流程

**schema 变化**：`t_strategy` 新增 `audit_status TINYINT(1) DEFAULT 1`

| 值 | 含义 | 前台可见 | 后台可见 |
|----|------|----------|----------|
| 0 | 待审核 | ❌ | ✅ |
| 1 | 已通过 | ✅ | ✅ |
| 2 | 已拒绝 | ❌ | ✅ |

**两个字段分工**：`status` 只管软删（0/1），`audit_status` 只管审核（0/1/2），**互不干扰**。

**SQL 层面**：前台查询全加 `AND audit_status = 1`：
- `FIND_LATEST`、`FIND_BY_CATEGORY`、`SEARCH`、`FIND_BY_ID`（详情）
- `FIND_BY_USER`（我的攻略）**不加过滤**，让作者能看到自己的审核状态
- `FIND_ALL`（管理员）**不加过滤**，显示全部待审/通过/拒绝

**发布流程**：DAO INSERT SQL 写死 `(..., status, audit_status) VALUES(..., 1, 0)` → 新攻略默认**待审**。

**审核操作**：`/admin/strategy/audit?id=N&action=approve|reject`（POST + CSRF Token）

**migration.sql 数据迁移**：已有 4 条攻略 UPDATE audit_status=1 保持可见。

---

## 🔗 过滤器链执行顺序

```
HTTP Request
    │
    ▼
① characterEncodingFilter (Spring) — UTF-8
    │
    ▼
② xssFilter  ── HttpServletRequestWrapper 清洗所有参数
    │
    ▼
③ csrfFilter ── session 生成/校验 _csrf token
    │              ├─ GET → 放行
    │              ├─ POST + 白名单 → 放行
    │              └─ POST 需 _csrf 参数 → 403
    │
    ▼
④ authFilter ── 仅 /my/* /admin/* 进入
    │              ├─ /my/* → 登录态 + 资源归属校验
    │              └─ /admin/* → 管理员 role==1
    │
    ▼
⑤ DispatcherServlet → Controller.handleRequest()
    │
    ▼
   Service → 被 AOP auditAspect 拦截（XML 声明式）
    │
    ▼
   DAO (JdbcTemplate) → DB
    │
    ▼
   Response
```

---

## 🔐 安全设计总览

| 维度 | 方案 | 代码位置 |
|------|------|----------|
| 输入安全 | XSS Filter（正则 + HTML 转义） | `filter/XssFilter.java` |
| CSRF | session UUID token + POST 校验 + 白名单 | `filter/CsrfFilter.java` |
| 越权访问 | /my/delete 资源归属校验；/admin/* role 校验 | `filter/AuthFilter.java` |
| 密码存储 | 加盐 MD5（SecureRandom salt + md5(pwd+salt)） | `service/impl/UserServiceImpl.java` |
| 暴力破解 | 前端无明文；密码存储加盐后无法彩虹表 | `UserServiceImpl.hashPassword()` |
| 操作审计 | AOP XML 切面，自动记录 Service 层所有操作 | `aspect/AuditAspect.java` |
| 内容安全 | 发布默认待审核，管理员审核通过才前台可见 | `StrategyDaoImpl` SQL + 审核 Controller |
| 水平越权 | AuthFilter 在删除自己攻略前直连 DAO 校验归属 | `AuthFilter` + `StrategyDao.findById()` |
| 依赖注入安全 | Filter 全部用 DelegatingFilterProxy，避免 new 实例 | web.xml + applicationContext.xml |

---

## 🔑 测试账号

| 角色 | 账号 | 密码 | salt |
|------|------|------|------|
| 超级管理员 | `admin` | `123456` | `NULL`（兼容老用户，纯 md5） |
| 普通用户 | `player1` | `123456` | `NULL` |
| 普通用户 | `player2` | `123456` | `NULL` |

**注意**：上述三个是 init.sql 预置的"老用户"，salt=NULL。用 `123456` 登录时，系统会自动 fallback 纯 md5（e10adc3949ba59abbe56e057f20f883e）。

如果你用"注册"页面新建账号，会自动生成随机 salt，密码存储为 md5(plain+salt)。

---

## ❓ FAQ

### Q1. 启动后首次调用 Service 报错 "Table 'game_strategy_platform.t_audit_log' doesn't exist"

没执行 `sql/audit.sql`。三个 SQL 执行顺序：init.sql → migration.sql → audit.sql。

### Q2. 登录成功后 POST 提交攻略/删除报 403 Forbidden

CSRF Token 校验失败。原因：
- JSP 表单没加 `<input type="hidden" name="_csrf" value="${sessionScope._csrf}" />`
- 或 session 过期后换了新 token
- 或用 GET 链接触发 DELETE 操作（必须改成 POST mini-form）

### Q3. admin / player1 老用户登录失败

检查 migration.sql 是否已执行（salt 字段是否存在）。migration.sql 不会改老用户密码（salt 仍为 NULL），login 有 fallback 逻辑。如果仍失败，直接：
```sql
UPDATE t_user SET password = 'e10adc3949ba59abbe56e057f20f883e', salt = NULL WHERE username = 'admin';
```

### Q4. STS 中项目有红色 X

1. Project → Properties → Java Build Path → Libraries → 添加 Tomcat 9 Runtime
2. Project → Properties → Project Facets → Dynamic Web Module 3.1
3. Project → Clean
4. 确认 WebContent/WEB-INF/lib 里的 JAR 没被删除

### Q5. 控制台乱码 / 编译报 "不可映射字符"

文件编码用 UTF-8。javac 编译加 `-encoding UTF-8`；eclipse/STS 中：Preferences → General → Workspace → Text file encoding = UTF-8。

### Q6. 访问 /my/delete?id=5 提示 403 "该攻略不属于您"

AuthFilter 拦截后校验 `strategy.userId == 当前登录用户.id`。如果这篇攻略是 player2 发的，player1 去删 → 被拦截，这是正常行为（水平越权防护）。

### Q7. 攻略发布后前台看不到

新攻略默认 audit_status=0（待审核），需要管理员登录 → 管理后台 → 攻略管理 → 点"通过"按钮。

### Q8. 为什么 AOP 切面不拦截 AuditLogDao

pointcut 只匹配 `execution(* com.service.impl.*.*(..))`，DAO 包不在范围内，避免 DAO 写日志时再次被 AOP 拦截 → 无限递归。

### Q9. 为什么 Filter 要用 DelegatingFilterProxy，不能直接 new

因为 AuthFilter 需要注入 StrategyDao。普通 Filter 走 web.xml 反射实例化，无法 set 注入。DelegatingFilterProxy 让 Spring 容器管理 Filter 的生命周期和依赖注入。

### Q10. 如何看到审计日志

```sql
USE game_strategy_platform;
SELECT operator, method, duration_ms, success, create_time
FROM t_audit_log ORDER BY create_time DESC LIMIT 20;
```

Service 层任何调用都会自动写入，包括 login/register/publish/delete/审核等。

---

## 📄 License

本项目仅用于学习交流，禁止商用。
