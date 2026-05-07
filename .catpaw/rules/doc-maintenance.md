---
description: 文档维护纪律（每次代码变更后检查）
globs: "**/*.java,**/*.sql,**/*.md"
alwaysApply: true
---

# 文档维护纪律

每次修改代码后，必须检查是否需要同步更新文档：

## 触发规则

| 你改了什么 | 必须同步更新 |
|-----------|------------|
| 表结构（DDL） | `sql/schema.sql` + `数据库设计文档.md` |
| API 接口签名 | Controller 上的 Knife4j 注解（自动生成文档） |
| 业务流程/状态机 | `数据流转与流程图.md` |
| 新增编码约束 | `docs/agents/03-constraints.md` + `.catpaw/rules/project-constraints.md` |
| 目录结构变化 | `docs/agents/01-project-meta.md` |

## 纪律

- 如果改了代码但没同步文档 → 任务未完成，不能标记 done
- 如果不确定是否需要更新文档 → 宁可多更新，不要漏更新
- 文档更新和代码修改应在同一次执行中完成，不要拆成两步
