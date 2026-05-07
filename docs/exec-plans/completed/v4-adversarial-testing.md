# 对抗测试执行计划：V4 全 Service 对抗测试

## 状态
执行中

## 执行记录要求
- 阶段级进展 → activeLog
- case 级测试日志、请求响应、bug 记录 → 本文档执行日志

## 目的
以"出题者"视角，为博文学堂全部 7 个 Service 接口生成对抗测试数据集，
验证所有业务规则在正常路径、边界值、异常输入、并发场景、权限越界下的稳定性。

## 优先级规则
当本文档与其他测试说明冲突时，以本文档为准。

## 验收标准
- [ ] 对抗测试数据集文档完成（docs/test/adversarial-dataset.md）
- [ ] 测试计划文档完成（docs/test/test-plan.md）
- [ ] 覆盖度说明文档完成（docs/test/coverage-map.md）
- [ ] 全部测试用例代码实现并编译通过
- [ ] mvn test 全部绿灯
- [ ] lint-project.sh R1-R8 全部 PASS
- [ ] 测试覆盖率：每个 Service 核心方法至少 3 条正常 + 3 条边界 + 3 条异常

## 步骤
1. 生成对抗测试数据集文档
2. 生成测试计划文档
3. 生成覆盖度说明文档
4. 实现测试用例代码（补充 UserService/StudentProfileService/ReviewRecordService/TicketService）
5. 全链路验证
6. 归档

## 执行日志
- [2026-05-07] 创建执行计划
- [2026-05-07] 完成对抗测试数据集文档（118 条 case，覆盖 7 个 Service）
- [2026-05-07] 完成测试计划文档（7 Phase 执行顺序 + Mockito 配置 + 判定标准）
- [2026-05-07] 完成覆盖度说明文档（16 条业务规则 ↔ 118 条 case 双向映射）
- [2026-05-07] 实现测试代码：UserServiceImplTest(24) + StudentProfileServiceImplTest(17) + ReviewRecordServiceImplTest(12) + TicketServiceImplTest(19) + 已有(43) = 115 tests
- [2026-05-07] mvn test 115 tests, 0 failures — 全部绿灯
- [2026-05-07] lint R1-R8 全部 PASS, 0 ERROR, 0 WARNING
- [2026-05-07] ✅ 计划完成
