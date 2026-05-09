# 03 - 工程约束与协作规范

> **改代码之前必读本文件。违反以下约束的代码不允许合入。**

## 1. 版本硬约束

| 约束 | 说明 | 违反后果 |
|------|------|---------|
| JDK 必须 17 | Spring Boot 3.x 最低要求 | 编译失败 |
| Spring Boot 必须 3.2.5 | 父 POM 锁定 | 依赖冲突 |
| 命名空间必须 jakarta.* | Boot 3 已迁移 | 运行时 ClassNotFound |
| MyBatis-Plus 必须 Boot3 Starter | `mybatis-plus-spring-boot3-starter` | 启动失败 |
| Knife4j 必须 Jakarta 版 | `knife4j-openapi3-jakarta-spring-boot-starter` | 404 |

## 2. 编码禁忌

- **禁止** 使用 `javax.*` 包下任何类
- **禁止** 手动升降 pom.xml 中已锁定的依赖版本
- **禁止** 对 `review_record` 表执行 UPDATE 或 DELETE
- **禁止** 在 Controller 层写业务逻辑，Controller 只做参数校验 + 调 Service
- **禁止** 在 Service 层直接拼 SQL，复杂查询用 LambdaQueryWrapper
- **禁止** 硬编码状态值数字（如 `status = 2`），必须使用枚举常量

## 3. 命名规范

| 层级 | 命名风格 | 示例 |
|------|---------|------|
| 数据库表/字段 | snake_case | `student_profile.review_status` |
| Java 实体字段 | camelCase | `reviewStatus` |
| API 路径 | kebab-case | `/api/student-profile/submit` |
| Controller 类 | XxxController | `StudentProfileController` |
| Service 接口 | XxxService | `DemandService` |
| Service 实现 | XxxServiceImpl | `DemandServiceImpl` |
| Mapper 接口 | XxxMapper | `DemandMapper` |
| DTO 类 | XxxDTO / XxxVO | `DemandCreateDTO` |
| 枚举类 | XxxEnum / XxxStatus | `DemandStatus` |

## 4. 状态流转规则

任何状态变更必须在 Service 层校验合法性，不允许跳跃：

- 需求状态只能**前进**（PENDING→MATCHING(瞬时)→RECOMMENDED），开始匹配后系统自动完成推荐，MATCHING 为事务内瞬时态；无候选时保持 MATCHING 允许重试；唯一例外是任意状态可→CLOSED
- 审核状态 REJECTED→DRAFT 和 NEED_SUPPLEMENT→PENDING_REVIEW 是**回退**允许的唯一路径
- 工单状态只能前进，PROCESSING 可直接→CLOSED（无效工单场景）

## 5. 业务硬规则

- 同一需求最多推荐 5 位学生（应用层 + 数据库联合唯一索引双重保障）
- 同一教育邮箱只能绑定一个手机号
- 验证码有效期 5 分钟，60 秒内不可重发
- 连续输错 5 次后冷却 15 分钟
- 家长看学生信息需脱敏（隐藏手机号、邮箱）

## 6. 文档同步纪律

- 新增/修改表结构 → 同步更新 `数据库设计文档.md` + `sql/schema.sql`
- 新增/修改 API → 同步更新 Knife4j 注解（接口文档自动生成）
- 新增/修改业务流程 → 同步更新 `数据流转与流程图.md`
- 新增/修改约束规则 → 同步更新本文件

## 7. 测试约束

### 基础约束

- 每个 ServiceImpl 的**核心业务方法**必须有单元测试覆盖
- 测试必须覆盖三类场景：正常路径、边界值、异常路径
- 继承 `ServiceImpl` 的类使用 `@Spy @InjectMocks` + `doReturn().when(spy)` 模式
- 纯委托类（不继承 ServiceImpl）使用标准 `@Mock @InjectMocks` 模式
- **禁止** `when(spy.realMethod()).thenReturn()` — 会触发真实方法调用
- mvn test 零失败是代码合入的前置条件（由 lint R8 自动校验）

### 对抗测试纪律

- 新增/修改 Service 核心方法时，**必须先更新文档三件套再编写测试**
- 三件套路径：`docs/test/adversarial-dataset.md`、`docs/test/test-plan.md`、`docs/test/coverage-map.md`
- 每条业务规则至少被 2 个测试用例覆盖（正常路径 + 异常路径各至少 1 条）
- 用例 ID 格式：`<Service缩写>-<类型>-<序号>`（N=正常/B=边界/E=异常/P=权限）
- 新建 ServiceImpl 时必须在三件套中同步新增对应章节
- 覆盖度映射必须保持双向可追踪（业务规则→用例、用例→业务规则）
- lint R9 自动校验三件套文档存在性

## 8. Git 提交规范

```
<type>(<scope>): <简述>

type: feat / fix / refactor / docs / style / test / chore
scope: 模块名，如 demand / student / ticket / admin / common
```

示例：`feat(demand): 实现家长提交需求接口`
