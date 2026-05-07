---
description: 博文学堂项目技术约束（每次对话自动生效）
globs: "**/*.java,**/pom.xml"
alwaysApply: true
---

# 项目技术约束

你正在操作「博文学堂」项目。以下约束在整个对话过程中始终有效：

## 版本约束（违反即编译/运行失败）

- JDK 17，禁止 JDK 8
- Spring Boot 3.2.5，禁止 2.x
- 命名空间 jakarta.*，禁止 javax.*
- MyBatis-Plus 使用 `mybatis-plus-spring-boot3-starter`，禁止旧版
- Knife4j 使用 Jakarta 版

## 编码禁忌

- 禁止对 review_record 表 UPDATE/DELETE（审计轨迹只增不删）
- 禁止在 Controller 写业务逻辑
- 禁止硬编码状态值数字，使用枚举常量
- 禁止手动升降已锁定的依赖版本

## 命名

- DB 字段 snake_case / Java 字段 camelCase / API 路径 kebab-case
- 类名：XxxController / XxxService / XxxServiceImpl / XxxMapper / XxxDTO

## 业务规则

- 每条需求最多推荐 5 位学生
- 验证码 5 分钟有效，60s 不重发，错 5 次冷却 15 分钟
- 家长看学生信息需脱敏（隐藏手机号/邮箱）

## 测试纪律

- 新增/修改 Service 核心方法必须先更新 `docs/test/` 三件套，再编写测试
- Harness 包含 R9 对抗测试文档完整性检查
- 详见 `.catpaw/rules/adversarial-testing.md`
