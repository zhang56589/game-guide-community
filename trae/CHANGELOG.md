# 修改记录 CHANGELOG
> 倒序排列，最新在上。编号：CHG-YYYYMMDD-NNN（当日递增）

## [CHG-20261104-004] docs - README 与当前代码对齐

- 时间：2026-11-04
- 类型：docs
- 模块：文档（README.md）
- 根因：二次静态扫描发现 README 多处描述与代码实际实现不一致（DelegatingFilterProxy、GET 生成 token、logout GET、Controller 数量、XSS 转义方式等）
- 改动要点（仅 README.md，未动任何代码）：
  - Filter 注册方式：全部改为「web.xml 原生 Filter 直接注册；AuthFilter init() 手动取 Bean」（涉及技术栈表、applicationContext.xml 示例、web.xml 示例、Filter 对比表、"为什么用 DelegatingFilterProxy" 章节、新增 Filter 指南、安全设计总览）
  - CSRF token 生命周期：GET 不创建 session 不生成 token；首个 POST 生成；登录/注册成功后 Controller 在新 session 重新发放（两处流程图 + CsrfFilter 表格 + Q2 故障排查）
  - `/logout`：GET → POST（需 CSRF；GET 仅重定向不退出），功能概览 + 路由表同步
  - 新增「会话固定」安全设计行；「暴力破解」行改为实际实现（5 次锁 15 分钟）
  - 控制台概览去掉不存在的"待审核数"；用户管理补充"禁止对自己禁用/降级"
  - 详情页补充"不存在/未过审返回 404"；审计写入改为"auditExecutor 异步"
  - applicationContext.xml 示例：Filter Bean 块替换为 auditExecutor（ThreadPoolTaskExecutor 2/5/200）
  - springmvc.xml 示例：移除不存在的 component-scan，Controller 数量 15 → 17
  - XssFilter 描述："HTML 转义" → "全角化替换"（与代码一致）
- 影响范围：仅文档，无运行时影响
- 验证结果：grep 确认 DelegatingFilterProxy / 首次 GET / component-scan / 共 15 个 等过时表述已清零
- 回滚方法：git checkout -- README.md

## [CHG-20261104-003] fix - 二次静态扫描 Bug 批量修复（BUG-1~5/7/8 + SEC-1/2/4 + PERF-1）

- 时间：2026-11-04
- 类型：fix
- 模块：Security / Controller / Filter / DAO / JSP / 配置
- 根因：二次静态扫描发现 SEC-2（会话固定修复销毁旧 session）与 PERF-5（GET 不再生成 CSRF token）叠加导致登录/注册后首个 POST 必 403；另发现管理员自我禁用、parseInt 风格不一、logout 无 token、死代码等多项缺陷
- 改动要点：
  - BUG-1：DoLoginController / DoRegisterController 重建 session 后补种 `_csrf` token（UUID），修复登录/注册后首个 POST 必 403
  - BUG-2：admin_user.jsp 禁用按钮排除自己；AdminUserStatusController 服务端禁止 disable/demote 自己（防唯一管理员自我锁死）
  - BUG-3：HomeController / DoPublishController / AdminUserStatusController / AdminStrategyDeleteController / DeleteMyStrategyController / AuthFilter 六处 parseInt 统一 try-catch（Filter 返回 400，Controller 忽略非法输入），与 DetailController 既有风格一致
  - BUG-4：header.jspf 退出表单补 `_csrf` hidden；CsrfFilter 白名单移除 /logout；LogoutController 仅接受 POST 且改 getSession(false)，GET /logout 不再销毁会话
  - BUG-5：DetailController 攻略不可见时改返回 404，不再渲染缺数据的 home 视图
  - BUG-7：StrategyDaoImpl.insert 返回 jdbcTemplate.update 真实影响行数，不再恒返回 1
  - BUG-8：UserDaoImpl INSERT 补 status 列，与 User 实体/register 业务字段对齐
  - SEC-1：auditExecutor 由 SimpleAsyncTaskExecutor（每任务新建线程）换为 ThreadPoolTaskExecutor（core2/max5/queue200），防线程爆炸与连接池耗尽
  - SEC-2：AuditAspect 增加无调试信息兜底——login 方法第 2 个参数（明文密码）强制脱敏
  - SEC-4：header.jspf 昵称、login/register/publish 三处 ${error} 改用 <c:out> 转义，补齐输出侧纵深防御
  - PERF-1：HomeController 仅在无搜索/筛选时执行 getLatest 查询，消除搜索页无用 SQL
  - BUG-6（UserDao.findByUsernameAndPassword 死代码）按 rule.md「禁止删现有 public 方法」约束保留未动
- 影响范围：
  - 登录/注册后新 session 内多一个 `_csrf` 属性（行为对外无感）
  - GET /logout 不再退出登录，仅 POST 退出；退出表单现携带 token（存量老会话首次退出若 403，刷新页面后重试即可自愈）
  - 非法数字参数（id/category 等）从 500 变为忽略/400；攻略不存在/未过审从渲染首页改为 404
  - 审计写入线程池有界化，极端流量下审计任务可能排队（队列 200）
- 验证结果：待用户重启 Tomcat 后按按钮级清单验证
- 回滚方法：对每个修改的文件执行 git checkout

## [CHG-20261104-002] feat+fix - 安全加固 + 性能优化（SEC-1~5, PERF-1~5）

- 时间：2026-11-04
- 类型：feat / fix
- 模块：Security / Performance
- 根因：全项目静态扫描发现 5 项安全隐患 + 4 项性能问题
- 改动要点：
  - SEC-2 Session 固定攻击：DoLoginController / DoRegisterController 登录成功后先 invalidate 旧 session 再创建新 session 写入 loginUser
  - SEC-3 logout GET→POST：header.jspf 退出链接改为 POST form；CsrfFilter 白名单新增 "/logout"
  - SEC-4 暴力破解防护：UserServiceImpl 新增 ConcurrentHashMap 失败计数器，5 次失败后锁定 15 分钟；UserService 接口新增 isLoginLocked 方法；DoLoginController 登录前先查锁定状态
  - SEC-5 + PERF-5 CsrfFilter 双重修复：删除白名单冗余 jsessionid 项；GET/HEAD/OPTIONS/TRACE 跳过 session 创建和 token 校验
  - SEC-1 JSP c:out 输出转义：home.jsp / detail.jsp / my.jsp / admin_user.jsp / admin_strategy.jsp 所有用户输入字段 ${} 改为 <c:out value="${}"/>
  - PERF-1 审计异步写入：applicationContext.xml 新增 auditExecutor(SimpleAsyncTaskExecutor)；AuditAspect 注入它，finally 块 auditLogDao.insert 改为 executor.execute() 异步执行，null 则 fallback 同步
  - PERF-2 UserService N+1：UserDao 新增 updateStatus/updateRole 直接 SQL 方法；UserDaoImpl 实现 UPDATE t_user SET ... WHERE id=?；UserServiceImpl 改用新 DAO 方法避免先 findById 再 update
  - PERF-4 分类缓存：CategoryServiceImpl 新增 volatile cacheAll + cacheById，双检锁懒加载；CategoryService 无增删改方法，缓存永不过期，重启自动刷新
- 影响范围：
  - 登录/注册后 JSESSIONID 会变化（Session 固定防护）
  - 退出按钮从链接变成按钮
  - 登录失败 5 次后锁 15 分钟
  - 匿名访问首页不再创建 session
  - 审计日志异步写入，服务器关闭时极少量审计可能丢失（学习项目可接受）
  - 分类列表不再每次查 DB
- 验证结果：待用户重启 Tomcat 后验证
- 回滚方法：对每个修改的文件执行 git checkout；applicationContext.xml 删除 auditExecutor bean 和 auditAspect 的 auditExecutor property

## [CHG-20261104-001] fix - 全项目 Bug 批量修复（BUG-1~BUG-7）

- 时间：2026-11-04
- 类型：fix
- 模块：Aspect / Filter / Controller 层 / DAO 层
- 根因：静态扫描发现 7 类 Bug，覆盖审计日志明文密码、AuthFilter fail-open/路径过宽、Controller 无谓创建 session + NumberFormatException 空 catch、归属校验静默跳过、RowMapper 风格不一致
- 改动要点：
  - AuditAspect.java：新增 PARAM_BLACKLIST + LocalVariableTableParameterNameDiscoverer 精准脱敏参数名（password/pwd/newPassword/salt）；兜底 User 对象含密码时整体脱敏；去掉 ByteArrayOutputStream/PrintStream 无用 import
  - AuthFilter.java：strategyDao==null 时 fail-closed sendError(500)；路径匹配从 startsWith("/my")/startsWith("/admin") 改为 equals + startsWith("/") 精确匹配；去掉 NumberFormatException 空 catch，让异常自然暴露
  - 13 个 Controller：public 页面用 getSession(false) + session==null 检查防 NPE；protected 页面统一 getSession(false) + session==null→redirect:/login；7 处空 catch 统一去掉让异常自然暴露；DeleteMyStrategyController 归属校验失败时返回 403（与 AuthFilter 行为一致）
  - StrategyDaoImpl.java：user_id 从 rs.getInt 改为 rs.getObject + null 保护，与 category_id 风格统一
- 影响范围：全项目 Controller/Filter/Aspect/DAO 的行为边界变更；AuthFilter 初始化失败时从静默放行变为显式 500；NumberFormatException 从静默吞掉变为 500 响应
- 验证结果：待用户重启 Tomcat 后验证
- 回滚方法：对每个修改的文件执行 git checkout，或手动还原修改前原文（完整原文在对话历史中已贴出）
