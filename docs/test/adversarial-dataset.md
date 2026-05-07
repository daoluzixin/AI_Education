# 对抗测试数据集

> 生成时间：2026-05-07
> 生成角色：对抗 Agent（出题者）
> 适用范围：博文学堂全部 7 个 Service 接口

---

## 1. UserService 对抗数据集

### 1.1 parentLogin（家长登录/自动注册）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| U-P-001 | 正常路径 | phone="13800138001", verifyCode="123456"（Redis中已存） | 首次登录：创建 User(role=PARENT, status=1)，返回 User |
| U-P-002 | 正常路径 | 已存在家长再次登录，phone="13800138001" | 返回已有 User，不重复创建 |
| U-P-003 | 正常路径 | 携带 nickname="张妈妈" | User.nickname = "张妈妈" |
| U-P-004 | 边界值 | nickname=null，phone="13800138001" | 自动生成昵称 "家长8001" |
| U-P-005 | 异常输入 | verifyCode="000000"（Redis中存"123456"） | 抛 400 "验证码错误" |
| U-P-006 | 异常输入 | Redis 中无该 phone 的验证码 | 抛 400 "验证码已过期" |
| U-P-007 | 异常输入 | phone="13800138001" 但该用户 status=0（已禁用） | 抛 403 "账号已被禁用" |
| U-P-008 | 权限越界 | 同一手机号尝试以 STUDENT 角色数据干扰 | 家长登录只查 role=PARENT，不受影响 |

### 1.2 studentLogin（学生登录/自动注册）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| U-S-001 | 正常路径 | phone="13900139001", email="test@pku.edu.cn", verifyCode="654321" | 创建 User(role=STUDENT) |
| U-S-002 | 正常路径 | 已存在学生再次登录 | 返回已有 User |
| U-S-003 | 异常输入 | email="test@gmail.com"（非 .edu.cn） | 抛 400 "教育邮箱必须以 .edu.cn 结尾" |
| U-S-004 | 异常输入 | email="test@pku.edu.cn" 已被另一手机号绑定 | 抛 400 "该教育邮箱已被其他账号绑定" |
| U-S-005 | 异常输入 | 验证码错误 | 抛 400 "验证码错误" |
| U-S-006 | 异常输入 | 用户 status=0 | 抛 403 "账号已被禁用" |
| U-S-007 | 边界值 | email="a@b.edu.cn"（极短合法邮箱） | 正常通过 |
| U-S-008 | 边界值 | email 含特殊字符 "test+1@pku.edu.cn" | 正常通过（@Email 允许 + 号） |

### 1.3 adminLogin（管理员登录）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| U-A-001 | 正常路径 | phone + 正确密码 | 返回 User(role=ADMIN) |
| U-A-002 | 异常输入 | 账号不存在 | 抛 401 "账号或密码错误" |
| U-A-003 | 异常输入 | 密码错误 | 抛 401 "账号或密码错误" |
| U-A-004 | 异常输入 | 用户 status=0 | 抛 403 "账号已被禁用" |
| U-A-005 | 权限越界 | phone 存在但 role=PARENT（非 ADMIN） | 抛 401 "账号或密码错误"（查不到 role=ADMIN 的记录） |

### 1.4 sendVerifyCode（发送验证码）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| U-V-001 | 正常路径 | phone="13800138001"，60秒内无发送记录 | Redis 写入验证码，设 5min TTL + 60s 防刷 |
| U-V-002 | 异常输入 | 60秒内重复发送 | 抛 400 "验证码发送过于频繁" |
| U-V-003 | 边界值 | 恰好 60 秒后再发 | 正常发送 |

---

## 2. StudentProfileService 对抗数据集

### 2.1 saveDraft（保存草稿）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| SP-D-001 | 正常路径 | 首次保存草稿，userId=1 | 创建 StudentProfile(reviewStatus=DRAFT) |
| SP-D-002 | 正常路径 | 已有 DRAFT 状态再次保存 | 更新成功 |
| SP-D-003 | 正常路径 | REJECTED 状态重新编辑 | 允许更新 |
| SP-D-004 | 异常输入 | PENDING_REVIEW 状态尝试保存草稿 | 抛 400 "当前状态不允许编辑" |
| SP-D-005 | 异常输入 | APPROVED 状态尝试保存草稿 | 抛 400 "当前状态不允许编辑" |
| SP-D-006 | 异常输入 | NEED_SUPPLEMENT 状态尝试保存草稿 | 抛 400 "当前状态不允许编辑" |

### 2.2 submitForReview（提交审核）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| SP-R-001 | 正常路径 | DRAFT 状态提交 | 状态变为 PENDING_REVIEW，生成 studentNo |
| SP-R-002 | 正常路径 | REJECTED 状态重新提交 | 状态变为 PENDING_REVIEW |
| SP-R-003 | 异常输入 | PENDING_REVIEW 状态重复提交 | 抛 400 "当前状态不允许提交审核" |
| SP-R-004 | 异常输入 | APPROVED 状态重复提交 | 抛 400 |
| SP-R-005 | 边界值 | 首次无草稿直接提交 | 创建新 Profile + 生成 studentNo |
| SP-R-006 | 边界值 | studentNo 已存在（被驳回后重新提交） | 保持原 studentNo 不变 |

### 2.3 supplement（补充材料）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| SP-SUP-001 | 正常路径 | NEED_SUPPLEMENT 状态，补充 supplements 字段 | 状态变为 PENDING_REVIEW |
| SP-SUP-002 | 异常输入 | 档案不存在 | 抛 404 "学生档案不存在" |
| SP-SUP-003 | 异常输入 | DRAFT 状态调用 supplement | 抛 400 "当前状态不允许补充材料" |
| SP-SUP-004 | 异常输入 | APPROVED 状态调用 supplement | 抛 400 |
| SP-SUP-005 | 边界值 | 所有补充字段都为空字符串 | 状态仍变为 PENDING_REVIEW（空字段不覆盖） |

---

## 3. ReviewRecordService 对抗数据集

### 3.1 doReview（执行审核）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| RR-001 | 正常路径 | reviewResult=2(APPROVED), reviewNote=null | 审核通过，StudentProfile.reviewStatus=2 |
| RR-002 | 正常路径 | reviewResult=3(REJECTED), reviewNote="资料不完整" | 审核驳回，StudentProfile.rejectReason="资料不完整" |
| RR-003 | 正常路径 | reviewResult=4(NEED_SUPPLEMENT), reviewNote="请补充学生证" | 待补充，StudentProfile.supplementNote 更新 |
| RR-004 | 异常输入 | reviewResult=0（无效结果） | 抛 400 "无效的审核结果" |
| RR-005 | 异常输入 | reviewResult=99 | 抛 400 "无效的审核结果" |
| RR-006 | 异常输入 | reviewResult=3(REJECTED), reviewNote=""（空） | 抛 400 "驳回或要求补充时必须填写审核备注" |
| RR-007 | 异常输入 | reviewResult=4(NEED_SUPPLEMENT), reviewNote=null | 抛 400 "驳回或要求补充时必须填写审核备注" |
| RR-008 | 异常输入 | studentProfileId 不存在 | 抛 404 "学生档案不存在" |
| RR-009 | 异常输入 | StudentProfile.reviewStatus=DRAFT（非 PENDING_REVIEW） | 抛 400 "当前状态不允许审核操作" |
| RR-010 | 异常输入 | StudentProfile.reviewStatus=APPROVED（已审核通过） | 抛 400 "当前状态不允许审核操作" |
| RR-011 | 权限越界 | reviewResult=2(APPROVED) 但 reviewNote 非空 | 正常通过（APPROVED 时 note 可选） |
| RR-012 | 数据完整性 | 审核后检查 review_record 表是否只新增了一条记录 | ReviewRecord INSERT 一条，无 UPDATE/DELETE |

---

## 4. DemandService 对抗数据集

### 4.1 createDemand（创建需求）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| D-C-001 | 正常路径 | 完整合法 DTO | 创建 Demand(status=PENDING)，生成 demandNo |
| D-C-002 | 边界值 | expectations 恰好 200 字 | 正常创建 |
| D-C-003 | 边界值 | remark=null（可选字段） | 正常创建，remark 为 null |
| D-C-004 | 边界值 | budget=null（可选字段） | 正常创建 |

### 4.2 startMatching（开始匹配）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| D-M-001 | 正常路径 | PENDING 状态 → startMatching | 状态变为 MATCHING |
| D-M-002 | 异常输入 | demandId 不存在 | 抛 404 "需求不存在" |
| D-M-003 | 异常输入 | MATCHING 状态再调 startMatching | 抛 400 "当前状态不允许开始匹配" |
| D-M-004 | 异常输入 | RECOMMENDED 状态调 startMatching | 抛 400 |
| D-M-005 | 异常输入 | CLOSED 状态调 startMatching | 抛 400 |

### 4.3 closeDemand（关闭需求）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| D-CL-001 | 正常路径 | PENDING → close | 状态变为 CLOSED，记录 closeReason |
| D-CL-002 | 正常路径 | MATCHING → close | 状态变为 CLOSED |
| D-CL-003 | 正常路径 | RECOMMENDED → close | 状态变为 CLOSED |
| D-CL-004 | 异常输入 | CLOSED → close（重复关闭） | 抛 400 "需求已关闭" |
| D-CL-005 | 异常输入 | demandId 不存在 | 抛 404 "需求不存在" |
| D-CL-006 | 边界值 | closeReason=null | 正常关闭，closeReason 为 null |

---

## 5. MatchingService 对抗数据集

### 5.1 findCandidates（匹配候选学生）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| M-001 | 正常路径 | MATCHING 需求，城市有3名已审核学生 | 返回≤3个 CandidateVO，按 totalScore 降序 |
| M-002 | 正常路径 | limit=5，但只有2名学生 | 返回2个 |
| M-003 | 正常路径 | PENDING 状态需求 | 正常匹配（PENDING/MATCHING 都允许） |
| M-004 | 边界值 | 城市无任何已审核学生 | 返回空列表 |
| M-005 | 边界值 | limit=0 | 返回空列表 |
| M-006 | 边界值 | limit=100（远超实际学生数） | 返回全部学生（不超过实际数） |
| M-007 | 边界值 | 学生 subjects=null | 科目评分=0，不报错 |
| M-008 | 边界值 | demand.budget=null | 预算评分=SCORE_BUDGET/2 |
| M-009 | 边界值 | demand.expectations=null | 标签评分=0 |
| M-010 | 边界值 | 学生 hourlyRate="200+"（开放区间） | 正常解析 |
| M-011 | 异常输入 | demandId 不存在 | 抛 404 "需求不存在" |
| M-012 | 异常输入 | CLOSED 状态需求 | 抛 400 "需求当前状态不适合匹配" |
| M-013 | 异常输入 | RECOMMENDED 状态需求 | 抛 400 |
| M-014 | 评分验证 | 985学校学生 vs 普通学校学生 | 985学校 schoolScore=15 > 普通=5 |
| M-015 | 评分验证 | 学生活跃推荐数=5（饱和） | saturationScore=0 |
| M-016 | 评分验证 | 学生活跃推荐数=0 | saturationScore=20 |
| M-017 | 评分验证 | demandType="OTHER"（unrestricted） | 科目评分=SCORE_SUBJECT/2=15 |
| M-018 | 边界值 | hourlyRate="abc"（非法格式） | parseRange 返回 null，预算评分=SCORE_BUDGET/2 |

---

## 6. RecommendationService 对抗数据集

### 6.1 configureRecommendation（配置推荐）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| R-001 | 正常路径 | MATCHING 需求 + 3名已审核学生 | 推荐成功，需求状态变为 RECOMMENDED |
| R-002 | 正常路径 | RECOMMENDED 需求重新配置 | 删除旧推荐，写入新推荐 |
| R-003 | 边界值 | 恰好 5 名学生（上限） | 正常通过 |
| R-004 | 边界值 | 1 名学生（下限） | 正常通过 |
| R-005 | 异常输入 | 6 名学生 | 抛 400 "每条需求最多推荐5位学生" |
| R-006 | 异常输入 | 100 名学生（极端溢出） | 抛 400 |
| R-007 | 异常输入 | 需求不存在 | 抛 404 "需求不存在" |
| R-008 | 异常输入 | 需求状态=PENDING | 抛 400 "需求状态不允许配置推荐" |
| R-009 | 异常输入 | 需求状态=CLOSED | 抛 400 |
| R-010 | 异常输入 | 学生档案不存在 | 抛 404 "学生档案不存在" |
| R-011 | 异常输入 | 学生未审核通过（PENDING_REVIEW） | 抛 400 "未审核通过" |
| R-012 | 权限越界 | studentIds 中第3人未审核，前2人通过 | 在第3人处中断抛异常 |

### 6.2 removeRecommendation（删除单条推荐）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| R-RM-001 | 正常路径 | 存在的推荐记录 | 删除成功 |
| R-RM-002 | 边界值 | 不存在的推荐记录 | 无异常（幂等操作） |

---

## 7. TicketService 对抗数据集

### 7.1 createTicket（创建工单）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| T-C-001 | 正常路径 | 家长创建工单，userType=1 | 创建 Ticket(status=PENDING)，生成 ticketNo |
| T-C-002 | 正常路径 | 学生创建工单，userType=2 | 创建成功 |
| T-C-003 | 边界值 | relatedDemandId=null（可选） | 正常创建 |
| T-C-004 | 边界值 | attachments=null | 正常创建 |

### 7.2 acceptTicket（客服接单）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| T-A-001 | 正常路径 | PENDING 工单 → accept | 状态变为 PROCESSING，handlerId 记录 |
| T-A-002 | 异常输入 | ticketId 不存在 | 抛 404 "工单不存在" |
| T-A-003 | 异常输入 | PROCESSING 状态重复接单 | 抛 400 "只有待处理工单才能接单" |
| T-A-004 | 异常输入 | CLOSED 状态接单 | 抛 400 |
| T-A-005 | 异常输入 | RESOLVED 状态接单 | 抛 400 |

### 7.3 replyTicket（客服回复）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| T-R-001 | 正常路径 | PROCESSING 工单回复 | 新增 TicketReply 记录 |
| T-R-002 | 正常路径 | PENDING 工单回复 | 自动接单(→PROCESSING) + 新增回复 |
| T-R-003 | 异常输入 | ticketId 不存在 | 抛 404 "工单不存在" |
| T-R-004 | 异常输入 | CLOSED 工单回复 | 抛 400 "工单已关闭或已解决" |
| T-R-005 | 异常输入 | RESOLVED 工单回复 | 抛 400 "工单已关闭或已解决" |

### 7.4 closeTicket（关闭工单）

| Case ID | 场景分类 | 输入 | 期望结果 |
|---------|---------|------|---------|
| T-CL-001 | 正常路径 | PENDING → close | 状态变为 CLOSED |
| T-CL-002 | 正常路径 | PROCESSING → close | 状态变为 CLOSED |
| T-CL-003 | 异常输入 | ticketId 不存在 | 抛 404 "工单不存在" |
| T-CL-004 | 异常输入 | CLOSED → close（重复关闭） | 抛 400 "工单已关闭" |
| T-CL-005 | 边界值 | RESOLVED → close | 状态变为 CLOSED（RESOLVED 可关闭） |

---

## 数据集统计

| Service | 正常路径 | 边界值 | 异常输入 | 权限越界 | 合计 |
|---------|---------|--------|---------|---------|------|
| UserService | 8 | 5 | 10 | 2 | 25 |
| StudentProfileService | 5 | 4 | 8 | 0 | 17 |
| ReviewRecordService | 3 | 0 | 7 | 1 | 11+1 |
| DemandService | 5 | 3 | 7 | 0 | 15 |
| MatchingService | 3 | 10 | 3 | 0 | 16+2 |
| RecommendationService | 4 | 3 | 7 | 1 | 15 |
| TicketService | 6 | 4 | 9 | 0 | 19 |
| **总计** | **34** | **29** | **51** | **4** | **118** |
