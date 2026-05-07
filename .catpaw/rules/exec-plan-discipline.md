---
description: 执行计划创建与完结纪律
globs: "**/*"
alwaysApply: true
---

# 执行计划纪律

## 何时必须创建执行计划

满足以下任一条件时，必须先在 `docs/exec-plans/active/` 创建执行计划再动手：

- 涉及 3 个以上文件的修改
- 新增一个完整功能模块（Entity + Mapper + Service + Controller）
- 数据库 DDL 变更
- 跨多层的重构

## 执行计划要求

- 必须包含明确的「验收标准」（checkboxes）
- 验收标准必须包含「linter 零违规」和「编译通过」
- 执行过程中需记录日志（遇到的问题、做出的决策）

## 完结流程

1. 所有验收标准打勾
2. 运行 `bash scripts/lint-project.sh` 确认零违规
3. 将计划文件从 `active/` 移动到 `completed/`
4. 在 `docs/exec-plans/activeLog` 追加一行记录

## 小改动豁免

以下情况不需要执行计划（直接改即可）：

- 修复 typo / 调整注释
- 单个文件的 bug fix
- 配置项微调
