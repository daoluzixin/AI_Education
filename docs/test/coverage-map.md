# 覆盖度说明（Case ↔ 业务规则映射）

> 生成时间：2026-05-07
> 引用文档：docs/agents/03-constraints.md、docs/test/adversarial-dataset.md

---

## 业务规则清单（来源：03-constraints.md）

| 规则ID | 规则描述 | 来源章节 |
|--------|---------|---------|
| BR-01 | 同一需求最多推荐 5 位学生 | §5 业务硬规则 |
| BR-02 | 同一教育邮箱只能绑定一个手机号 | §5 业务硬规则 |
| BR-03 | 验证码有效期 5 分钟，60 秒内不可重发 | §5 业务硬规则 |
| BR-04 | 家长看学生信息需脱敏 | §5 业务硬规则 |
| BR-05 | review_record 表只 INSERT 不 UPDATE 不 DELETE | §2 编码禁忌 |
| SM-01 | 需求状态只能前进（PENDING→MATCHING→RECOMMENDED），任意→CLOSED 例外 | §4 状态流转 |
| SM-02 | CLOSED→CLOSED 非法（重复关闭） | §4 状态流转 |
| SM-03 | 审核状态 REJECTED→DRAFT 允许回退 | §4 状态流转 |
| SM-04 | NEED_SUPPLEMENT→PENDING_REVIEW 允许回退 | §4 状态流转 |
| SM-05 | 工单状态只能前进，PROCESSING 可直接→CLOSED | §4 状态流转 |
| AUTH-01 | 管理员密码 BCrypt 加密验证 | §1 版本硬约束 + 实现 |
| AUTH-02 | 用户禁用后（status=0）阻断登录 | 实现逻辑 |
| AUTH-03 | 教育邮箱必须以 .edu.cn 结尾 | 实现逻辑 |
| MATCH-01 | 匹配引擎硬过滤：同城市 + 审核通过 | 实现逻辑 |
| MATCH-02 | 匹配评分五维度：科目30 + 标签20 + 学校15 + 预算15 + 饱和度20 | 实现逻辑 |
| MATCH-03 | 饱和度阈值=5，活跃推荐≥5时饱和度分=0 | 实现逻辑 |

---

## Case → 规则映射表

### UserService

| Case ID | 覆盖规则 | 验证要点 |
|---------|---------|---------|
| U-P-001 | — | 正常首次注册流程 |
| U-P-002 | — | 已有用户不重复创建 |
| U-P-003 | — | nickname 正确赋值 |
| U-P-004 | — | 默认昵称生成逻辑 |
| U-P-005 | BR-03 | 验证码校验失败 |
| U-P-006 | BR-03 | 验证码过期 |
| U-P-007 | AUTH-02 | 禁用账号阻断 |
| U-P-008 | — | 角色隔离（PARENT vs STUDENT） |
| U-S-001 | AUTH-03 | 正常 .edu.cn 邮箱注册 |
| U-S-002 | — | 已有用户不重复创建 |
| U-S-003 | AUTH-03 | 非 .edu.cn 邮箱拦截 |
| U-S-004 | BR-02 | 同一邮箱不可绑定多手机号 |
| U-S-005 | BR-03 | 验证码校验失败 |
| U-S-006 | AUTH-02 | 禁用账号阻断 |
| U-S-007 | AUTH-03 | 极短合法邮箱边界 |
| U-S-008 | AUTH-03 | 特殊字符邮箱边界 |
| U-A-001 | AUTH-01 | BCrypt 密码验证通过 |
| U-A-002 | AUTH-01 | 账号不存在 |
| U-A-003 | AUTH-01 | 密码错误 |
| U-A-004 | AUTH-02 | 禁用账号阻断 |
| U-A-005 | — | 角色隔离（非 ADMIN 查不到） |
| U-V-001 | BR-03 | 验证码正常发送 + TTL 设置 |
| U-V-002 | BR-03 | 60秒防刷拦截 |
| U-V-003 | BR-03 | 60秒后允许重发 |

### StudentProfileService

| Case ID | 覆盖规则 | 验证要点 |
|---------|---------|---------|
| SP-D-001 | — | 首次草稿创建 |
| SP-D-002 | SM-03 | DRAFT 允许编辑 |
| SP-D-003 | SM-03 | REJECTED 允许编辑（回退） |
| SP-D-004 | SM-03 | PENDING_REVIEW 禁止编辑 |
| SP-D-005 | SM-03 | APPROVED 禁止编辑 |
| SP-D-006 | SM-03 | NEED_SUPPLEMENT 禁止草稿编辑 |
| SP-R-001 | SM-03 | DRAFT→PENDING_REVIEW 合法 |
| SP-R-002 | SM-03 | REJECTED→PENDING_REVIEW 合法 |
| SP-R-003 | SM-03 | PENDING_REVIEW 禁止重复提交 |
| SP-R-004 | SM-03 | APPROVED 禁止重复提交 |
| SP-R-005 | — | 无草稿直接提交（首次） |
| SP-R-006 | — | studentNo 不重新生成 |
| SP-SUP-001 | SM-04 | NEED_SUPPLEMENT→PENDING_REVIEW 合法 |
| SP-SUP-002 | — | 档案不存在校验 |
| SP-SUP-003 | SM-04 | DRAFT 禁止补充 |
| SP-SUP-004 | SM-04 | APPROVED 禁止补充 |
| SP-SUP-005 | SM-04 | 空字段不覆盖原值 |

### ReviewRecordService

| Case ID | 覆盖规则 | 验证要点 |
|---------|---------|---------|
| RR-001 | BR-05 | 审核通过 — INSERT 一条 record |
| RR-002 | BR-05 | 审核驳回 — INSERT 一条 record + rejectReason |
| RR-003 | BR-05 | 待补充 — INSERT 一条 record + supplementNote |
| RR-004 | — | 无效 reviewResult 拦截 |
| RR-005 | — | 无效 reviewResult 极端值 |
| RR-006 | — | 驳回时 reviewNote 必填校验 |
| RR-007 | — | 待补充时 reviewNote 必填校验 |
| RR-008 | — | 档案不存在校验 |
| RR-009 | SM-03 | 非 PENDING_REVIEW 禁止审核 |
| RR-010 | SM-03 | APPROVED 禁止重复审核 |
| RR-011 | — | APPROVED + reviewNote 非空不报错 |
| RR-012 | BR-05 | 验证只有 INSERT 无 UPDATE |

### DemandService

| Case ID | 覆盖规则 | 验证要点 |
|---------|---------|---------|
| D-C-001 | — | 需求创建，初始状态 PENDING |
| D-C-002 | — | expectations 200字边界 |
| D-C-003 | — | 可选字段 null |
| D-C-004 | — | budget 可选 |
| D-M-001 | SM-01 | PENDING→MATCHING 合法 |
| D-M-002 | — | 需求不存在校验 |
| D-M-003 | SM-01 | MATCHING 禁止重复 startMatching |
| D-M-004 | SM-01 | RECOMMENDED 禁止回退到 MATCHING |
| D-M-005 | SM-01 | CLOSED 禁止 startMatching |
| D-CL-001 | SM-01 | PENDING→CLOSED 合法 |
| D-CL-002 | SM-01 | MATCHING→CLOSED 合法 |
| D-CL-003 | SM-01 | RECOMMENDED→CLOSED 合法 |
| D-CL-004 | SM-02 | CLOSED→CLOSED 非法 |
| D-CL-005 | — | 需求不存在校验 |
| D-CL-006 | — | closeReason 可为 null |

### MatchingService

| Case ID | 覆盖规则 | 验证要点 |
|---------|---------|---------|
| M-001 | MATCH-01, MATCH-02 | 同城市已审核学生匹配 + 评分降序 |
| M-002 | MATCH-01 | limit 大于实际学生数 |
| M-003 | SM-01 | PENDING 允许匹配 |
| M-004 | MATCH-01 | 无候选学生→空列表 |
| M-005 | — | limit=0 边界 |
| M-006 | — | limit 极大值 |
| M-007 | MATCH-02 | null subjects 不崩溃 |
| M-008 | MATCH-02 | null budget → 默认半分 |
| M-009 | MATCH-02 | null expectations → 标签分=0 |
| M-010 | MATCH-02 | "200+" 格式解析 |
| M-011 | — | 需求不存在 |
| M-012 | SM-01 | CLOSED 禁止匹配 |
| M-013 | SM-01 | RECOMMENDED 禁止匹配 |
| M-014 | MATCH-02 | 985 vs 普通学校评分差异 |
| M-015 | MATCH-03 | 活跃推荐=5 → 饱和度分=0 |
| M-016 | MATCH-03 | 活跃推荐=0 → 饱和度分=20 |
| M-017 | MATCH-02 | OTHER 类型 unrestricted 逻辑 |
| M-018 | MATCH-02 | 非法 hourlyRate 格式容错 |

### RecommendationService

| Case ID | 覆盖规则 | 验证要点 |
|---------|---------|---------|
| R-001 | BR-01, SM-01 | 正常推荐 + 需求状态变 RECOMMENDED |
| R-002 | BR-01 | 重新配置覆盖旧推荐 |
| R-003 | BR-01 | 恰好5人（上限边界） |
| R-004 | BR-01 | 1人（下限） |
| R-005 | BR-01 | 6人 → 拦截 |
| R-006 | BR-01 | 100人 → 拦截 |
| R-007 | — | 需求不存在 |
| R-008 | SM-01 | PENDING 禁止配置推荐 |
| R-009 | SM-01 | CLOSED 禁止配置推荐 |
| R-010 | — | 学生档案不存在 |
| R-011 | — | 学生未审核通过拦截 |
| R-012 | — | 顺序检查中断（第3人异常） |
| R-RM-001 | — | 正常删除 |
| R-RM-002 | — | 幂等删除（不存在不报错） |

### TicketService

| Case ID | 覆盖规则 | 验证要点 |
|---------|---------|---------|
| T-C-001 | — | 家长创建工单 |
| T-C-002 | — | 学生创建工单 |
| T-C-003 | — | 可选字段 null |
| T-C-004 | — | attachments 可选 |
| T-A-001 | SM-05 | PENDING→PROCESSING 合法 |
| T-A-002 | — | 工单不存在 |
| T-A-003 | SM-05 | PROCESSING 禁止重复接单 |
| T-A-004 | SM-05 | CLOSED 禁止接单 |
| T-A-005 | SM-05 | RESOLVED 禁止接单 |
| T-R-001 | SM-05 | PROCESSING 下正常回复 |
| T-R-002 | SM-05 | PENDING 下回复自动接单 |
| T-R-003 | — | 工单不存在 |
| T-R-004 | SM-05 | CLOSED 禁止回复 |
| T-R-005 | SM-05 | RESOLVED 禁止回复 |
| T-CL-001 | SM-05 | PENDING→CLOSED 合法 |
| T-CL-002 | SM-05 | PROCESSING→CLOSED 合法 |
| T-CL-003 | — | 工单不存在 |
| T-CL-004 | SM-05 | CLOSED→CLOSED 非法 |
| T-CL-005 | SM-05 | RESOLVED→CLOSED 合法 |

---

## 规则覆盖统计

| 规则ID | 规则描述 | 覆盖 Case 数 | Case IDs |
|--------|---------|-------------|----------|
| BR-01 | 最多推荐5人 | 6 | R-001~006 |
| BR-02 | 邮箱唯一绑定 | 1 | U-S-004 |
| BR-03 | 验证码5分钟/60秒防刷 | 6 | U-P-005/006, U-S-005, U-V-001/002/003 |
| BR-05 | review_record 只INSERT | 4 | RR-001/002/003/012 |
| SM-01 | 需求状态前进 | 13 | D-M-001/003/004/005, D-CL-001~004, M-003/012/013, R-001/008/009 |
| SM-02 | CLOSED不可重复关闭 | 1 | D-CL-004 |
| SM-03 | 审核状态回退规则 | 10 | SP-D-002~006, SP-R-001~004, RR-009/010 |
| SM-04 | NEED_SUPPLEMENT→PENDING_REVIEW | 4 | SP-SUP-001/003/004/005 |
| SM-05 | 工单状态前进 | 10 | T-A-001/003/004/005, T-R-001/002/004/005, T-CL-001~005 |
| AUTH-01 | BCrypt 密码验证 | 3 | U-A-001/002/003 |
| AUTH-02 | 禁用账号阻断 | 3 | U-P-007, U-S-006, U-A-004 |
| AUTH-03 | .edu.cn 邮箱校验 | 4 | U-S-001/003/007/008 |
| MATCH-01 | 同城市+已审核过滤 | 3 | M-001/002/004 |
| MATCH-02 | 五维度评分 | 8 | M-007~010/014/017/018, M-001 |
| MATCH-03 | 饱和度阈值 | 2 | M-015/016 |

---

## 未覆盖规则说明

| 规则ID | 原因 | 建议 |
|--------|------|------|
| BR-04 | 脱敏在 Controller/VO 层实现，Service 层不负责 | 后续补充集成测试 |
| 并发场景 | 单元测试无法有效模拟真实并发 | 后续压测/集成测试覆盖 |
| 连续错误5次冷却15分钟 | 当前未实现此逻辑 | 待实现后补充 |

---

## 追踪说明

本文档提供完整的 Case ID → 业务规则 双向映射。任何测试 case 的增删改都必须同步更新本文档，确保可追踪性。
