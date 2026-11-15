# GameStrategyPlatform — Trae 编码规则 v1.1

## 一、项目背景
纯 XML 配置的 SSM（Spring 5.3.39 + SpringMVC + JdbcTemplate），零注解。
双容器：applicationContext.xml（DAO/Service/Filter/AOP）、springmvc.xml（Controller）。
学习型项目，改动必须可解释、可回滚。

## 二、硬性约束（违反即失败）
1. 只改任务相关文件，禁止顺手重构
2. 禁止引入新依赖、新框架、新注解
3. 禁止改表结构、SQL、JSP 业务逻辑
4. 禁止删现有 public 方法
5. 保持零注解，依赖注入走 XML <property>
6. 改前先列"改哪些文件、哪几行、为什么"，等确认
7. 改后给：①原文 ②diff ③验证 ④回滚
8. 一次对话只解决一个任务

## 三、禁止盲改
- 改任何文件前必须完整读取当前内容
- 禁止用"常见写法"猜测代码
- 文件读不到就停下来问，不许假设

## 四、信息冲突处理
优先级：实际代码 > README > 我的描述。
三者不一致时，先列出差异，问我按哪个为准，不许擅自选。

## 五、改动前快照
给 diff 前，先贴"修改前"完整原文（不用省略号）。
多文件改动，每个文件都先贴原文。

## 六、诊断必须带证据
每个结论附证据：代码/XML 行号+原文、异常栈关键行、复现步骤。
证据不足就说"证据不足，需要补充 XXX"。

## 七、最小改动
- diff 控制在 20 行以内，超了说明为什么不能拆
- 禁止为风格统一改无关行（空格、注释也不行）
- 禁止格式化整文件

## 八、技术判断原则
- Filter 实例化：web.xml 直接写类名 = Tomcat new，无 Spring 注入；
  写 DelegatingFilterProxy + targetBeanName = Spring Bean，可注入
- 无依赖 Filter（XssFilter/CsrfFilter）保留直接类名
- 有依赖 Filter（AuthFilter 依赖 StrategyDao）必须用 DelegatingFilterProxy
- filter-mapping 顺序不变：CharacterEncoding → Xss → Csrf → Auth
- 双容器：子（Controller）可访问父（Service），反向不可
- AOP pointcut 只匹配 com.service.impl.*，DAO 不在范围（防递归）
- 加盐 MD5：salt==null 走纯 MD5，salt!=null 走 md5(plain+salt)

## 九、不许动清单（除非任务明确要求）
- XssFilter.java、CsrfFilter.java
- web.xml 中 xss/csrf 的 filter 配置及整体顺序
- 任何 JSP、SQL、springmvc.xml
- 表结构、字段、Druid 数据源配置

## 十、验证清单
- 必须按钮级可执行：操作 → 预期 → 不符合说明什么
- 修复 Filter 类 bug 必覆盖 5 边界：
  ① 未登录 /my/delete → 302，不 NPE
  ② 登录删自己 → 成功
  ③ 登录删别人 → 403，不 NPE
  ④ 管理员 /admin/strategy/delete → 正常
  ⑤ 无 session GET /home → 正常
- 其他模块按"主流程 + 2 边界 + 1 异常"设计

## 十一、输出格式（按序输出，不跳步）
1. 诊断结论（是否 bug、根因、证据）
2. 修复方案（分步，说明改哪个文件、为什么、备选为何不选）
3. 修改前原文（完整，不用省略号）
4. 改动 diff（完整可粘贴，标注行号和原因）
5. 验证清单（按钮级）
6. 回滚方法
7. 变更摘要（根因/文件/要点/风险/后续）
全程中文，代码注释中文。

## 十二、修改记录（强制写 CHANGELOG.md）
- 每次改文件（代码/配置/SQL/文档/依赖），在trae文件夹 `CHANGELOG.md` 追加一条
- 时机：diff 执行完、验证清单给出后；失败回滚也要补一条 revert 记录
- 没有该文件则自动创建，头部固定：
  # 修改记录 CHANGELOG
  > 倒序排列，最新在上。编号：CHG-YYYYMMDD-NNN（当日递增）
- 每条 9 字段：编号 / 时间 / 类型(fix|feat|refactor|docs|config|revert) /
  模块 / 根因 / 改动要点 / 影响范围 / 验证结果 / 回滚方法
- 格式：`## [CHG-YYYYMMDD-NNN] 类型 - 一句话标题`，字段用无序列表
- diff 和超 10 行原文不写进 CHANGELOG，只写要点
- 禁止改历史记录、合并记录、写"详见对话"、写"修复了一些问题"

## 十三、交互节奏
- 诊断 → 等确认 → 方案 → 等确认 → 再改
- 除非我明确说"直接改"
- 每阶段结束一句话总结"结论 + 待确认什么"
- "继续""可以""按你说的来"= 确认上一阶段

## 十四、失败处理
验证不通过不许盲试，按序排查：
① 配置是否生效 ② 依赖是否注入 ③ 执行顺序 ④ 缓存/编译产物
每次报告"试了什么、结果、下一步"；连续 2 次无效停下问我。

## 十五、红线
- 不读文件不许改代码
- 不用"通常/一般/默认"作依据
- 不删/注释现有功能来绕过问题
- 不改测试账号、预置数据让测试通过
- 不把 bug 说成 feature，不把猜测当事实
- 不擅自改 README 对齐代码