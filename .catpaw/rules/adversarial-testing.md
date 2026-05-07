---
description: 对抗测试纪律（新增/修改 Service 必须同步测试三件套）
globs: "**/*ServiceImpl.java,**/*Test.java,docs/test/**"
alwaysApply: true
---

# 对抗测试纪律

你正在操作「博文学堂」项目。以下对抗测试约束在整个对话过程中始终有效：

## 核心原则

采用"出题者 / 解题者 / 裁判"三角色分离模型：
- 出题者：生成对抗数据集（覆盖正常/边界/异常/权限越界）
- 解题者：根据数据集编写测试代码
- 裁判：Harness 四级验证闭环

## 文档三件套（必须同步维护）

| 文档 | 路径 | 必须包含 |
|------|------|---------|
| 对抗数据集 | `docs/test/adversarial-dataset.md` | 用例 ID、输入、期望输出、覆盖规则 |
| 测试计划 | `docs/test/test-plan.md` | 执行顺序、Mockito 配置、判定标准 |
| 覆盖度映射 | `docs/test/coverage-map.md` | 业务规则 ↔ 用例 ID 双向映射 |

## 强制执行流程

1. 修改 Service 核心方法 → 先更新三件套中对应章节
2. 编写/修改测试代码 → 测试必须覆盖三件套中定义的所有新增用例
3. 运行 Harness → `bash scripts/lint-project.sh`（包含 R9 文档完整性检查）
4. 标记完成 → 在 coverage-map 中标记已实现的用例

## 用例 ID 格式

`<Service缩写>-<类型>-<序号>`

- USR = UserService, SP = StudentProfileService, RR = ReviewRecordService
- TK = TicketService, DM = DemandService, MT = MatchingService, RC = RecommendationService
- 类型：N=正常 / B=边界 / E=异常 / P=权限

## 覆盖度红线

- 每条业务规则至少被 2 个用例覆盖（正常 + 异常各至少 1 条）
- 新建 ServiceImpl 必须在三件套中新增对应章节
- 覆盖度映射必须双向可追踪

## 禁忌

- **禁止** 未更新三件套就编写测试代码
- **禁止** 删除已有用例（可标记为 deprecated 并说明原因）
- **禁止** 用例无对应业务规则（每条用例必须关联至少 1 条规则）
