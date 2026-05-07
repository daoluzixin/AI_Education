# 执行计划：V1 后端全量重写（对齐新需求文档）

## 目标
将后端代码从旧版设计（TeacherProfile/ParentProfile/StudentInfo/TutoringDemand）全量重写为新需求文档定义的 7 张核心表架构（user/student_profile/review_record/demand/recommendation/ticket/ticket_reply），严格遵守 Harness 框架。

## 验收标准
- [x] pom.xml 存在且锁定 JDK 17 + Spring Boot 3.2.5 + MyBatis-Plus Boot3 Starter
- [x] 7 个 Entity 类与 schema.sql 严格对应
- [x] 枚举类覆盖所有状态机（ReviewStatus/DemandStatus/TicketStatus/UserRole/Gender/TicketType）
- [x] Mapper 层 7 个接口继承 BaseMapper
- [x] DTO 层覆盖各端输入输出
- [x] Service 层包含完整状态机校验、推荐数量上限等业务规则
- [x] Controller 按端分包（Parent/Student/Admin），无业务逻辑
- [x] 零 javax.* 引用，全部使用 jakarta.*
- [x] linter 运行 0 ERROR
- [x] 编译通过 (mvn compile)

## 步骤
1. ✅ 创建 pom.xml
2. ✅ 删除旧实体/Mapper/Service/Controller/DTO
3. ✅ 创建新 Entity（7张表）
4. ✅ 创建新枚举
5. ✅ 创建新 Mapper
6. ✅ 创建新 DTO
7. ✅ 创建新 Service 接口 + 实现
8. ✅ 创建新 Controller
9. ✅ 更新通用配置
10. ✅ 运行 linter（0 ERROR, 0 WARNING）

## 执行日志
- [2026-05-07] 开始执行，分析现有代码与需求差距
- [2026-05-07] 完成 pom.xml 创建、旧代码清理、Entity/Enum/Mapper/DTO 创建
- [2026-05-07] 完成 6 个 ServiceImpl（含状态机、推荐上限校验、Redis 流水号）
- [2026-05-07] 完成 3 个 Controller（ParentController/StudentController/AdminController）
- [2026-05-07] 修复编译问题：添加 spring-security-crypto 依赖、配置 Lombok annotationProcessor
- [2026-05-07] Lint 全部通过（0 ERROR, 0 WARNING），编译通过
- [2026-05-07] ✅ 计划完成，准备归档
