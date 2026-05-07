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

### 反馈闭环流程

```
Agent 修改代码
      ↓
运行 lint-project.sh
      ↓
┌─────────────┐    ┌─────────────┐
│  0 违规     │    │  有违规      │
│  → 完成     │    │  → 修复      │
└─────────────┘    └──────┬──────┘
                          ↓
                   再次运行 linter
                          ↓
                   直到 0 违规
```

## 文档维护触发条件

| 触发 | 必须同步更新的文档 |
|------|-----------------|
| 改了表结构 | `sql/schema.sql` + `数据库设计文档.md` |
| 改了 API 接口签名 | Knife4j 注解（自动） |
| 改了业务流程/状态机 | `数据流转与流程图.md` |
| 新增了编码约束 | `docs/agents/03-constraints.md` + `.catpaw/rules/project-constraints.md` |
| 完成了执行计划 | 移动到 completed/ + 更新 activeLog |
