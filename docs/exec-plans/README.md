# 执行计划（Exec Plans）

## 目录说明

- `active/` — 正在执行的计划
- `completed/` — 已完成归档的计划
- `activeLog` — 活跃计划的简要时间线

## 使用规则

1. 每个需要 3+ 文件修改的任务，**必须先创建执行计划**再动手
2. 计划文件命名：`<日期>-<简述>.md`，如 `20260507-implement-demand-api.md`
3. 所有验收标准打勾 + linter 零违规后，才能将计划移动到 `completed/`
4. 移动时在 `activeLog` 追加一行记录
