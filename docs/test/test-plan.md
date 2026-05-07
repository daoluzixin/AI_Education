# 对抗测试计划

> 生成时间：2026-05-07
> 生成角色：对抗 Agent（出题者）
> 数据集引用：docs/test/adversarial-dataset.md

---

## 执行原则

1. **依赖顺序**：底层 Service 先测，上层依赖后测
2. **隔离性**：每个测试方法独立，不依赖测试执行顺序
3. **Mockito 模式**：继承 ServiceImpl 的用 `@Spy @InjectMocks`；纯委托的用 `@Mock @InjectMocks`
4. **判定标准**：mvn test 全部绿灯 = 通过；任何一个 case 红灯 = 阻塞

---

## Phase 1：UserServiceImpl（基础认证层）

### 执行顺序与前置条件

| 步骤 | Case IDs | 前置条件 | 判定标准 |
|------|----------|---------|---------|
| 1.1 | U-V-001~003 | Mock StringRedisTemplate | 验证码写入/防刷逻辑正确 |
| 1.2 | U-P-001~008 | Mock Redis(验证码)、Mock UserMapper | 家长登录全路径覆盖 |
| 1.3 | U-S-001~008 | Mock Redis、Mock UserMapper | 学生登录 + 邮箱校验 |
| 1.4 | U-A-001~005 | Mock UserMapper、Mock BCryptPasswordEncoder | 管理员密码验证 |

### Mockito 配置

```
@Spy @InjectMocks UserServiceImpl userService
@Mock StringRedisTemplate redisTemplate (+ ValueOperations)
@Mock BCryptPasswordEncoder passwordEncoder
// UserServiceImpl extends ServiceImpl<UserMapper, User>
// 需 doReturn().when(userService).getOne(any()) 拦截查询
```

### 期望结果摘要

- 正常路径：返回正确 User 对象，首次自动注册
- 异常路径：抛出 BusinessException，code + message 精确匹配
- 权限越界：角色隔离生效，跨角色数据不干扰

---

## Phase 2：StudentProfileServiceImpl（档案层）

### 执行顺序与前置条件

| 步骤 | Case IDs | 前置条件 | 判定标准 |
|------|----------|---------|---------|
| 2.1 | SP-D-001~006 | Mock Redis、Spy StudentProfileServiceImpl | 草稿状态流转正确 |
| 2.2 | SP-R-001~006 | 同上 | 提交审核状态校验 + studentNo 生成 |
| 2.3 | SP-SUP-001~005 | 同上 | 补充材料状态流转 |

### Mockito 配置

```
@Spy @InjectMocks StudentProfileServiceImpl studentProfileService
@Mock StringRedisTemplate redisTemplate (+ ValueOperations)
// 继承 ServiceImpl<StudentProfileMapper, StudentProfile>
// 需 doReturn().when(spy).getByUserId(userId) 返回预设 profile
// 需 doReturn(true).when(spy).save(any()) 和 updateById(any())
```

### 状态机验证矩阵

| 当前状态 | saveDraft | submitForReview | supplement |
|---------|-----------|----------------|-----------|
| DRAFT | ✅ 允许 | ✅ 允许 | ❌ 400 |
| PENDING_REVIEW | ❌ 400 | ❌ 400 | ❌ 400 |
| APPROVED | ❌ 400 | ❌ 400 | ❌ 400 |
| REJECTED | ✅ 允许 | ✅ 允许 | ❌ 400 |
| NEED_SUPPLEMENT | ❌ 400 | ❌ 400 | ✅ 允许 |

---

## Phase 3：ReviewRecordServiceImpl（审核层）

### 执行顺序与前置条件

| 步骤 | Case IDs | 前置条件 | 判定标准 |
|------|----------|---------|---------|
| 3.1 | RR-001~003 | Mock StudentProfileService | 正常审核三种结果 |
| 3.2 | RR-004~007 | 同上 | 参数校验（无效结果、空备注） |
| 3.3 | RR-008~010 | 同上 | 档案/状态校验 |
| 3.4 | RR-011~012 | 同上 | 数据完整性验证 |

### Mockito 配置

```
@Spy @InjectMocks ReviewRecordServiceImpl reviewRecordService
@Mock StudentProfileService studentProfileService
// 继承 ServiceImpl<ReviewRecordMapper, ReviewRecord>
// 需 doReturn(true).when(spy).save(any(ReviewRecord.class))
```

### 判定标准

- APPROVED 时 reviewNote 可空
- REJECTED/NEED_SUPPLEMENT 时 reviewNote 必须非空
- 只允许 PENDING_REVIEW 状态的档案被审核
- 每次审核只 INSERT 一条 ReviewRecord（验证 save 被调用且仅调用一次）

---

## Phase 4：DemandServiceImpl（需求层）

### 执行顺序与前置条件

| 步骤 | Case IDs | 前置条件 | 判定标准 |
|------|----------|---------|---------|
| 4.1 | D-C-001~004 | Mock Redis | 需求创建 + 编号生成 |
| 4.2 | D-M-001~005 | Spy DemandServiceImpl | startMatching 状态机 |
| 4.3 | D-CL-001~006 | 同上 | closeDemand 状态机 |

### 状态流转验证

```
PENDING ──→ MATCHING ──→ RECOMMENDED ──→ CLOSED
  │                                        ↑
  └────────────────────────────────────────┘ (任意→CLOSED)
  
非法：MATCHING → PENDING (回退禁止)
非法：CLOSED → CLOSED (重复关闭)
非法：MATCHING → startMatching (重复触发)
```

---

## Phase 5：MatchingServiceImpl（匹配引擎层）

### 执行顺序与前置条件

| 步骤 | Case IDs | 前置条件 | 判定标准 |
|------|----------|---------|---------|
| 5.1 | M-001~003 | Mock DemandService、Mock StudentProfileMapper、Mock RecommendationMapper、Mock SubjectMappingConfig | 正常匹配返回排序列表 |
| 5.2 | M-004~010 | 同上 | 边界值（null字段、空列表、特殊格式） |
| 5.3 | M-011~013 | 同上 | 状态/不存在校验 |
| 5.4 | M-014~018 | 同上 | 评分维度独立验证 |

### Mockito 配置

```
@InjectMocks MatchingServiceImpl matchingService
@Mock DemandService demandService
@Mock StudentProfileMapper studentProfileMapper
@Mock RecommendationMapper recommendationMapper
@Mock SubjectMappingConfig subjectMappingConfig
// MatchingServiceImpl 不继承 ServiceImpl，纯委托模式
```

### 评分验证标准

| 维度 | 满分 | 验证条件 |
|------|------|---------|
| 科目匹配 | 30 | demandSubjects 与 studentSubjects 全匹配 = 30 |
| 标签匹配 | 20 | 所有 tag 命中 expectations |
| 学校层次 | 15 | 985=15, 211=10, 普通=5 |
| 预算匹配 | 15 | 区间完全重叠 = 15 |
| 接单余量 | 20 | activeCount=0 → 20 |

---

## Phase 6：RecommendationServiceImpl（推荐层）

### 执行顺序与前置条件

| 步骤 | Case IDs | 前置条件 | 判定标准 |
|------|----------|---------|---------|
| 6.1 | R-001~004 | Mock DemandService、Mock StudentProfileService、Spy RecommendationServiceImpl | 正常推荐 + 边界 |
| 6.2 | R-005~012 | 同上 | 数量限制 + 状态 + 学生审核 |
| 6.3 | R-RM-001~002 | 同上 | 删除推荐（幂等） |

### 业务规则验证

- **核心规则**：`studentIds.size() > 5` 时必须阻断
- **状态规则**：只有 MATCHING / RECOMMENDED 可配置推荐
- **审核规则**：所有学生必须 reviewStatus=APPROVED
- **顺序规则**：按 studentIds 顺序写入 sortOrder
- **状态变更**：配置后需求状态变为 RECOMMENDED

---

## Phase 7：TicketServiceImpl（工单层）

### 执行顺序与前置条件

| 步骤 | Case IDs | 前置条件 | 判定标准 |
|------|----------|---------|---------|
| 7.1 | T-C-001~004 | Mock Redis、Spy TicketServiceImpl | 工单创建 |
| 7.2 | T-A-001~005 | 同上 | 接单状态机 |
| 7.3 | T-R-001~005 | Mock TicketReplyMapper | 回复 + 自动接单 |
| 7.4 | T-CL-001~005 | Spy TicketServiceImpl | 关闭工单 |

### 工单状态流转验证

```
PENDING ──→ PROCESSING ──→ RESOLVED ──→ CLOSED
  │              │                        ↑
  │              └────────────────────────┘ (PROCESSING→CLOSED)
  └──────────────────────────────────────┘ (PENDING→CLOSED)
  
自动接单：PENDING 状态下 replyTicket → 自动切为 PROCESSING
非法：CLOSED/RESOLVED 下 replyTicket
非法：PROCESSING/CLOSED/RESOLVED 下 acceptTicket
```

---

## 整体执行顺序

```
Phase 1: UserServiceImpl           ← 基础，无外部 Service 依赖
Phase 2: StudentProfileServiceImpl ← 依赖 Redis
Phase 3: ReviewRecordServiceImpl   ← 依赖 StudentProfileService
Phase 4: DemandServiceImpl         ← 依赖 Redis
Phase 5: MatchingServiceImpl       ← 依赖 DemandService + Mapper
Phase 6: RecommendationServiceImpl ← 依赖 DemandService + StudentProfileService
Phase 7: TicketServiceImpl         ← 依赖 Redis + TicketReplyMapper
```

---

## 通过标准

| 级别 | 标准 |
|------|------|
| 单 Phase 通过 | 该 Phase 所有 case 绿灯 |
| 全量通过 | 7 个 Phase 全部绿灯 + lint R1-R8 = 0 ERROR |
| 覆盖率达标 | 每个核心方法 ≥3 正常 + ≥3 边界/异常 |
