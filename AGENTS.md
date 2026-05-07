# AGENTS.md — 博文学堂 Agent 导引地图

> 本文件是 Agent 进入项目时的**唯一入口**。请先阅读本文件，再根据指引查阅对应知识文档。

## 项目一句话

博文学堂（AI_Educatin）：连接家长与大学生家教的 O2O 撮合平台，包含家长端 H5、学生端 H5、后台管理端 PC Web，后端为 Spring Boot 3 单体应用。

## 知识文档索引

| 文档 | 路径 | 读什么时候看 |
|------|------|------------|
| 项目元信息 | `docs/agents/01-project-meta.md` | 了解技术栈、目录结构、构建方式 |
| 核心架构 | `docs/agents/02-architecture.md` | 了解分层架构、数据模型、API 设计规范 |
| 工程约束与协作规范 | `docs/agents/03-constraints.md` | 写代码前必读，版本约束/命名规则/禁忌 |
| 自反馈工作流 | `docs/agents/04-workflow.md` | 了解如何创建执行计划、跑 linter、自验证 |

## 执行计划

- 活跃计划目录：`docs/exec-plans/active/`
- 已完成计划归档：`docs/exec-plans/completed/`
- 执行日志：`docs/exec-plans/activeLog`

## 自动注入规则

以下规则文件通过 `.catpaw/rules/` 在每次对话中自动生效：

| 规则 | 作用 |
|------|------|
| `project-constraints.md` | 技术栈版本约束、编码禁忌 |
| `doc-maintenance.md` | 文档维护纪律（改代码必须同步文档） |
| `exec-plan-discipline.md` | 执行计划创建与完结纪律 |

## 自反馈机制

- Linter 脚本：`scripts/lint-project.sh`
- 每次修改代码后必须运行 linter，确认 0 违规才算完成
- Linter 检查内容：版本约束、命名规范、状态机合法性、SQL 安全

## 核心约束速览（详见 03-constraints.md）

1. **JDK 17 + Spring Boot 3.2.5** — 禁止 JDK 8、禁止 Spring Boot 2.x
2. **jakarta 命名空间** — 禁止 javax.*
3. **MyBatis-Plus Boot3 Starter** — 禁止旧版 starter
4. **数据库 snake_case / Java camelCase** — ORM 自动映射
5. **审核记录只增不删不改** — review_record 表只允许 INSERT
6. **每条需求最多推荐 5 位学生** — 应用层必须校验
