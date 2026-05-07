# 执行计划：V2 规则匹配引擎（推荐系统第一层）

## 状态：✅ 已完成

## 目标
实现基于结构化字段的规则匹配引擎，自动为每条需求生成"候选学生排行榜"（按匹配度评分排序），从纯人工浏览升级为"系统推荐候选 + 人工确认"模式。

## 验收标准
- [x] 新增 MatchingService 接口 + MatchingServiceImpl 实现
- [x] 实现多维度加权评分：城市(硬过滤) + 科目匹配(30分) + 标签(20分) + 学校层次(15分) + 饱和度(20分) + 预算(15分)
- [x] 新增 demandType→subjects 映射配置（SubjectMappingConfig）
- [x] 新增候选学生 VO（含评分明细）
- [x] AdminController 新增 GET /api/admin/demand/{demandId}/candidates 接口
- [x] student_profile 表新增 hourly_rate 字段（期望时薪）
- [x] 新增 recommendation_feedback 表（反馈闭环）
- [x] 增强 StudentQueryDTO 筛选条件（subjects/tags）
- [x] 同步更新 sql/schema.sql
- [x] linter 零违规
- [x] 编译通过 (mvn compile)

## 步骤
1. DDL 变更：student_profile 新增 hourly_rate；新建 recommendation_feedback 表
2. Entity 层：更新 StudentProfile 实体；新建 RecommendationFeedback 实体
3. Mapper 层：新建 RecommendationFeedbackMapper
4. DTO/VO 层：新建 CandidateVO（含评分明细）；增强 StudentQueryDTO
5. Config 层：新建 SubjectMappingConfig（demandType→subjects 映射）
6. Service 层：新建 MatchingService + MatchingServiceImpl（核心评分逻辑）
7. Controller 层：AdminController 新增 candidates 接口
8. 运行 linter + 编译验证

## 执行日志
- [2026-05-07] 创建执行计划
- [2026-05-07] 完成 DDL 变更（schema.sql 更新）
- [2026-05-07] 完成 Entity 层（StudentProfile 新增 hourlyRate；RecommendationFeedback 新建）
- [2026-05-07] 完成 Mapper 层（RecommendationFeedbackMapper）
- [2026-05-07] 完成 DTO/VO 层（CandidateVO 新建；StudentQueryDTO 增强 subjects/tags）
- [2026-05-07] 完成 Config 层（SubjectMappingConfig）
- [2026-05-07] 完成 Service 层（MatchingService + MatchingServiceImpl 五维度评分引擎）
- [2026-05-07] 完成 Controller 层（AdminController 新增 /demand/{demandId}/candidates）
- [2026-05-07] linter 通过：0 ERROR / 0 WARNING
- [2026-05-07] mvn compile 通过：编译零错误
- [2026-05-07] 归档执行计划 → completed/

## 新增/修改文件清单
| 文件 | 操作 |
|------|------|
| `sql/schema.sql` | 修改：student_profile 增加 hourly_rate；新建 recommendation_feedback 表 |
| `entity/StudentProfile.java` | 修改：增加 hourlyRate 字段 |
| `entity/RecommendationFeedback.java` | 新建 |
| `mapper/RecommendationFeedbackMapper.java` | 新建 |
| `common/enums/FeedbackType.java` | 新建 |
| `common/config/SubjectMappingConfig.java` | 新建 |
| `vo/CandidateVO.java` | 新建 |
| `service/MatchingService.java` | 新建 |
| `service/impl/MatchingServiceImpl.java` | 新建 |
| `entity/dto/student/StudentQueryDTO.java` | 修改：增加 subjects/tags 字段 |
| `controller/AdminController.java` | 修改：注入 MatchingService + 新增 candidates 接口 |
