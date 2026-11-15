---
name: ssm-project-scan
description: 全量扫描 XML 配置的 SSM (Spring+SpringMVC+JdbcTemplate) 项目并输出 Bug/安全/性能/架构/学习路径报告。当用户要求扫描、审计、体检、排查、整理架构、全项目检查时触发。不改任何文件，所有结论必须附文件路径加行号证据。
---

# SSM Project Scan

对一个纯 XML 配置的 SSM 项目做静态扫描，输出结构化报告。**全程只读**，不改任何文件，不执行任何命令，不启动服务。

## 适用前置

项目必须满足以下特征才能触发。如果任一不满足，先停下来告诉用户，不要硬套本流程：

- Spring + SpringMVC + JdbcTemplate，**零注解或注解很少**（纯 XML `<bean>` + `<property>` 注入为主）
- 双容器：`ContextLoaderListener` 加载 `applicationContext.xml`（DAO/Service/AOP），`DispatcherServlet` 加载 `springmvc.xml`（Controller）
- 有原生 `javax.servlet.Filter` 实现类（AuthFilter/CsrfFilter/XssFilter 等）
- `JdbcTemplate` + `RowMapper`/`BeanPropertyRowMapper`，不是 MyBatis
- 配置目录通常是 `WebContent/WEB-INF/`（老项目）或 `src/main/webapp/WEB-INF/`

## 读取顺序

按这个顺序逐文件读，每个文件**完整读完**再进入下一个。**不要用 Grep 跳过读取**——后面很多问题需要交叉引用上下文：

1. **XML 配置层**（先搞懂 Bean 关系再看 Java）
   - `web.xml` → 双容器声明、Filter 链、DispatcherServlet
   - `applicationContext.xml` → DAO/Service/AOP/事务 Bean 注册
   - `springmvc.xml` → Controller Bean 注册、视图解析器、静态资源
2. **properties**
   - `jdbc.properties` → DB 连接、池参数
3. **Bean 层**
   - 所有 `com.bean.*` → 字段类型（Integer vs int）、空安全
4. **DAO 层**
   - 接口 + 全部 impl → SQL 是否用 `?` 占位符、RowMapper 是否完整映射
5. **Service 层**
   - 接口 + 全部 impl → 空检查、事务边界、业务逻辑
6. **Filter 层**（3 个或更多）
   - CharacterEncodingFilter（Spring 内置）
   - XssFilter（自定义参数清洗）
   - CsrfFilter（POST Token 校验）
   - AuthFilter（越权/登录拦截）—— 重点看 init() 是否手动取 Bean
7. **Aspect 层**
   - 审计切面、日志切面 → 是否记录敏感参数（密码！）、是否 fail-open
8. **Controller 层**（全部）
   - 每个 Controller 的 `request.getSession()` vs `getSession(false)`、参数 null 检查、管理员拦截
9. **JSP 层**（全部）
   - 所有 `<form method="post">` 是否带 `_csrf` hidden input
   - `${entity.field}` 是否用 `<c:out>` 转义（纵深防御）
   - 登录/注册表单是否在 CsrfFilter 白名单（第一次访问没 token）

## 四类扫描维度

### A. 确定 Bug（有代码证据，可复现）

按以下 checklist 扫描：

| # | 检查项 | 典型症状 |
|---|-------|---------|
| A1 | `request.getSession()` vs `getSession(false)` | 80%+ Controller 用 `getSession(true)` 创建无谓空 session |
| A2 | DAO `findById` 用 `rs.getInt("nullable_col")` | DB NULL 返回 0 而非 null，后续 `equals` 比较不 NPE 但逻辑错 |
| A3 | Filter init() 取不到 Bean 时 fail-open | 越权访问被静默放行，不报错 |
| A4 | 资源归属校验的 Controller 层未重复校验 | 即使 Filter 有，Controller 也要再做一次（纵深） |
| A5 | DAO 接口方法签名与 impl 不一致 | 编译不过，但如果 impl 有 private 方法被反射调用则静默失败 |
| A6 | `Integer id == null` 检查遗漏 | `if (id > 0)` 会 NPE |
| A7 | 审计日志记录明文密码 | Service 签名带 password 参数时 AuditAspect 会全量记录 |
| A8 | `NumberFormatException` 空 catch | 吞异常，调用方收到非预期返回值 |
| A9 | Filter 路径 `startsWith("/my")` 过宽 | `/myAccount`、`/myExtra` 等意外被拦截 |
| A10 | 用户禁用后仍能登录（status 检查顺序） | 先查用户再检查 status，顺序反了就放行 |

### B. 安全隐患（可被利用）

| # | 检查项 | 说明 |
|---|-------|------|
| B1 | XSS 输入侧/输出侧 | XssFilter 正则清洗 + JSP `<c:out>` 转义 = 双层；缺任何一层都有纵深缺口 |
| B2 | CSRF 白名单完整性 | `/dologin`、`/doregister` 必须在白名单，**第一次访问**时 session 还没 token |
| B3 | 越权访问（水平/垂直） | AuthFilter 覆盖 `/my/*` 和 `/admin/*`；Controller 也要再校验 |
| B4 | `/logout` 用 GET | GET 状态变更违反 REST，CsrfFilter 不拦 GET |
| B5 | 暴力破解 | 无限流、无验证码、登录失败不锁定 |
| B6 | Session 固定攻击 | 登录成功后 `request.getSession().invalidate()` + 重新创建 |
| B7 | 密码哈希算法 | MD5 加盐可接受（学习项目），生产至少 SHA-256 |
| B8 | 敏感配置明文 | `jdbc.password=123456` 不要提交到公共仓库 |
| B9 | SQL 注入 | 全部用 JdbcTemplate + `?` 占位符 = 通过 |
| B10 | 审计日志脱敏 | AuditAspect 里 `password`、`newPassword`、`pwd` 字段必须替换为 `******` |

### C. 性能问题

| # | 检查项 | 说明 |
|---|-------|------|
| C1 | 审计日志同步写入 | `finally` 块同步 INSERT 阻塞主请求；学习项目可接受，生产改异步 |
| C2 | 无缓存 | `categoryService.findAll()`、`count()` 等低频变化数据每次查 DB |
| C3 | N+1 SELECT | `updateNickname/status/role` 先 `findById` 再 `update`，2 次 DB → 可直接 `UPDATE SET ... WHERE id=?` |
| C4 | 查询方法合并 | `getDetail(id)` 先 `findById` 再 `incrementViewCount` → 可一条 SQL 完成 |
| C5 | 首页无条件查询 | 无论 keyword/category 是否有值都执行 `getLatest(1,10)` |
| C6 | Filter 链冗余 | CharacterEncodingFilter 用 Spring Bean，其他用 Tomcat new —— 合理，无依赖 Filter 保持直接类名 |

### D. 架构与 README 差异

1. 画清楚双容器 + Filter 链 + Bean 依赖图（用文字 + ASCII）
2. README 描述 vs 代码实际实现是否一致（特别是 DelegatingFilterProxy vs init() 自取 Bean）
3. AOP pointcut 是否只匹配 `com.service.impl.*`（DAO 必须在范围外，避免递归拦截 AuditLogDao）
4. `tx:annotation-driven` 开了但代码没加 `@Transactional` 注解 → 等于事务未配置
5. Filter 链顺序：CharacterEncoding → Xss → Csrf → Auth（不能变）

## 证据规则

**每条问题结论必须包含**：

1. 文件路径（完整绝对路径 + `file://` 链接）
2. 精确行号
3. 最小代码片段（3-10 行，含该行及其上下文）
4. 触发条件描述（什么请求、什么边界）
5. 风险等级（HIGH / MEDIUM / LOW / INFO）

**不允许**：
- 用"可能/也许/如果"作为结论
- 只给文件名不给行号
- 只说"这里有 NPE 风险"不解释为什么
- 猜测（"通常 Spring 会这样"→ 必须读实际配置确认）

## 输出格式

报告按以下结构输出（中文）：

```
# SSM 项目静态扫描报告

> 扫描范围：文件清单
> 权威版本：代码（或指定）
> 所有改动建议未执行，仅列证据

## 一、确定 Bug（有代码证据）
- BUG-N [等级] 标题
  - 位置：file:///path#L行号
  - 证据：代码片段
  - 影响：xxx
  - 触发条件：xxx
  - 修复建议：xxx

## 二、安全隐患
（同上格式）

## 三、性能问题
（同上格式）

## 四、架构梳理
（文字图 + 依赖关系）

## 五、README vs 代码差异清单
（表格：README 行号 | README 描述 | 实际代码 | 严重度）

## 六、学习路径建议
（分阶段，每阶段具体到改哪个文件哪几行）

## 七、安全审计清单
（表格：# | 检查项 | 当前状态 | 风险）

## 八、变更摘要
改动清单：无。全程只读扫描。
（汇总表格：分类 | 数量 | 关键项）
```

## 红线

1. **只读不写**：扫描阶段不改任何文件。所有修复建议等用户二次确认后再执行
2. **不执行命令**：不启动 Tomcat、不跑测试、不 mvn compile。静态分析足够
3. **不假设**：遇到"这份 web.xml 应该是 X 配置"这种情况，停下来读实际内容
4. **不偏航**：用户说"扫描"就输出扫描报告，不要顺手把 Bug 修了或 README 改了
5. **不遗漏**：39 个 Java 文件 + 所有 JSP + 所有 XML + properties，一个都不能跳过
