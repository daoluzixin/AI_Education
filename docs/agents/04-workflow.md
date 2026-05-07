# 04 - 自反馈工作流

## 核心理念

**Harness = 文档约束 + 自反馈闭环**

Agent 每次修改代码后，必须自己验证改动是否符合项目约束，形成"改→验→修"的闭环，而不是依赖人工 review。

## 执行计划机制

### 什么时候需要创建执行计划

- 涉及 3 个以上文件的修改
- 新增一个完整功能模块
- 跨层修改（Controller + Service + Mapper + Entity）
- 数据库变更（DDL）

### 执行计划格式

在 `docs/exec-plans/active/` 下创建 markdown 文件：

```markdown
# 执行计划：<标题>

## 目标
一句话描述要做什么。

## 验收标准
- [ ] 标准1
- [ ] 标准2
- [ ] linter 零违规
- [ ] 编译通过

## 步骤
1. 步骤1
2. 步骤2
...

## 执行日志
- [时间] 完成了什么
- [时间] 遇到什么问题，如何解决
```

### 计划生命周期

1. 创建：`docs/exec-plans/active/<name>.md`
2. 执行中：逐项完成验收标准，记录日志
3. 完成：所有验收标准打勾后，移动到 `docs/exec-plans/completed/`
4. 简要记录到 `docs/exec-plans/activeLog`

## 自反馈 Linter

### 使用方式

```bash
bash scripts/lint-project.sh
```

### Linter 检查项

| 编号 | 检查内容 | 严重级别 |
|------|---------|---------|
| R1 | javax 包引用检查 | ERROR |
| R2 | Spring Boot 版本锁定 | ERROR |
| R3 | review_record 表 UPDATE/DELETE 检查 | ERROR |
| R4 | Controller 层业务逻辑检查 | WARN |
| R5 | 硬编码状态值检查 | WARN |
| R6 | 命名规范检查（snake_case ↔ camelCase） | WARN |
| R7 | 推荐数量上限校验存在性 | WARN |
| R8 | mvn test 单元测试全部通过 | ERROR |
| R9 | 对抗测试文档三件套完整性（存在性 + Service 覆盖） | WARN |

### 反馈闭环流程（四级 Harness）

```
Agent 修改代码
      ↓
Level 1: lint-project.sh (R1-R7 静态规则)
      ↓
Level 2: mvn compile（编译通过）
      ↓
Level 3: mvn test（单元测试全部绿灯）
      ↓
Level 4: lint-project.sh（含 R8，全链路 0 违规）
      ↓
┌─────────────┐    ┌─────────────┐
│  0 违规     │    │  有违规      │
│  → 完成     │    │  → 修复      │
└─────────────┘    └──────┬──────┘
                          ↓
                   定位失败层级并修复
                          ↓
                   重新从该层级开始验证
```

### 单元测试约束

- 测试框架：JUnit 5 + Mockito（已集成 `spring-boot-starter-test`）
- 对继承 `ServiceImpl` 的实现类，使用 `@Spy @InjectMocks` 模式
- 对 spy 对象必须使用 `doReturn().when(spy).method()` 而非 `when(spy.method()).thenReturn()`
- 测试粒度：每个 Service 的核心业务方法至少覆盖正常路径/边界值/异常路径
- 环境变量：测试运行需设置 `JAVA_HOME` 指向 Temurin JDK 17

## 对抗测试工作流（Part 3）

### 方法论

对抗测试采用"出题者 / 解题者 / 裁判"三角色分离模型：

```
出题者（Adversarial Dataset Generator）
      ↓  生成对抗数据集
解题者（Test Implementor）
      ↓  编写测试代码
裁判（Harness）
      ↓  四级验证闭环
      ✅ 通过 / ❌ 打回修复
```

### 文档三件套

每次新增或修改 Service 核心业务方法时，必须维护以下三份文档：

| 文档 | 路径 | 用途 |
|------|------|------|
| 对抗数据集 | `docs/test/adversarial-dataset.md` | 按 Service 分组的测试用例（正常/边界/异常/权限） |
| 测试计划 | `docs/test/test-plan.md` | 执行顺序、Mockito 配置、前置条件、判定标准 |
| 覆盖度映射 | `docs/test/coverage-map.md` | 用例 ID ↔ 业务规则双向追踪矩阵 |

### 对抗数据集格式规范

每条用例必须包含：

- **ID**：`<Service缩写>-<类型>-<序号>`（如 `USR-N-01` 表示 UserService 正常路径第1条）
- **类型标记**：N=正常 / B=边界 / E=异常 / P=权限
- **输入**：构造的请求参数
- **期望输出**：具体的返回值或异常类型
- **覆盖规则**：对应的业务规则编号

### 执行纪律

1. **先文档后代码**：修改 Service 时，先在三件套中增补用例，再编写测试
2. **覆盖度红线**：每条业务规则至少被 2 个用例覆盖（正常 + 异常各至少1条）
3. **新增 Service 必须同步**：新建 ServiceImpl 必须同时在三件套中新增对应章节
4. **Lint 校验**：R9 规则自动检查三件套文档存在且与代码模块对应

### 与四级 Harness 的衔接

```
对抗三件套更新完成
      ↓
编写/修改测试代码
      ↓
Level 1: lint-project.sh（R1-R7 静态规则 + R9 文档完整性）
      ↓
Level 2: mvn compile
      ↓
Level 3: mvn test（含新增对抗用例）
      ↓
Level 4: lint-project.sh 全链路（R8 + R9）
      ↓
覆盖度映射 ← 标记已实现用例
      ↓
✅ 完成
```

## 文档维护触发条件

| 触发 | 必须同步更新的文档 |
|------|-----------------|
| 改了表结构 | `sql/schema.sql` + `数据库设计文档.md` |
| 改了 API 接口签名 | Knife4j 注解（自动） |
| 改了业务流程/状态机 | `数据流转与流程图.md` |
| 新增了编码约束 | `docs/agents/03-constraints.md` + `.catpaw/rules/project-constraints.md` |
| 完成了执行计划 | 移动到 completed/ + 更新 activeLog |
| 新增/修改 Service 核心方法 | `docs/test/` 三件套 + 对应测试类 |
