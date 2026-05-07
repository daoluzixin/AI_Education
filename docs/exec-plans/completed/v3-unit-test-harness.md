# 执行计划：V3 单元测试 + 对抗测试接入 Harness

## 目标
将单元测试和对抗测试数据集纳入 Harness 自反馈闭环。每次修改代码后，闭环升级为：
`代码 → lint 0违规 → mvn compile 通过 → mvn test 全通过 → 才允许标记完成`

## 验收标准
- [x] 为 MatchingServiceImpl 编写对抗测试（覆盖正常/边界/异常场景） — 20 用例
- [x] 为 RecommendationServiceImpl 编写核心业务规则测试（推荐上限5人） — 11 用例
- [x] 为 DemandServiceImpl 编写状态流转测试 — 11 用例
- [x] lint-project.sh 新增 R8：mvn test 必须全部通过
- [x] 04-workflow.md 更新闭环流程（加入四级 Harness + 单元测试约束）
- [x] 03-constraints.md 新增测试相关约束（第7节）
- [x] mvn compile 通过
- [x] mvn test 全部通过（43 tests, 0 failures）
- [x] lint 零违规（含新增 R8）

## 设计原则（对标 harness-practice-summary 第十节）
- 出题者视角：覆盖边界数据（null值、空串、超长字符串、非法状态码、上限溢出）
- 解题者视角：修复所有测试失败直到绿灯
- 对抗测试数据集作为代码存在，每次 mvn test 自动回归

## 步骤
1. ✅ 编写 MatchingServiceImplTest（对抗测试级别：20 用例覆盖 5 分组）
2. ✅ 编写 RecommendationServiceImplTest（推荐上限5人、需求状态、学生审核）
3. ✅ 编写 DemandServiceImplTest（状态流转：PENDING→MATCHING、任意→CLOSED）
4. ✅ lint-project.sh 新增 R8 规则
5. ✅ 更新 04-workflow.md 和 03-constraints.md
6. ✅ 全链路验证：compile + test + lint = 0 违规

## 技术决策记录
- ServiceImpl 继承类使用 `@Spy @InjectMocks` 模式绕过 MyBatis-Plus 内部 baseMapper 调用
- `doReturn().when(spy)` 替代 `when(spy).thenReturn()` 避免真实方法被触发
- `doNothing()` 不适用于返回值非 void 的方法（如 baseMapper.delete() 返回 int）

## 执行日志
- [2026-05-07] 创建执行计划
- [2026-05-07] 完成 MatchingServiceImplTest（20 用例，全部绿灯）
- [2026-05-07] 完成 DemandServiceImplTest（11 用例，全部绿灯）
- [2026-05-07] 完成 RecommendationServiceImplTest（11 用例，修复 Mockito 模式后全部绿灯）
- [2026-05-07] 新增 R8 规则到 lint-project.sh
- [2026-05-07] 更新 04-workflow.md 与 03-constraints.md
- [2026-05-07] 全链路验证通过：43 tests, 0 failures, lint R1-R8 全 PASS
- [2026-05-07] ✅ 计划完成，归档
