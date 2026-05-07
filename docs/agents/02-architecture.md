# 02 - 核心架构

## 分层架构

```
┌────────────────────────────────────────────────┐
│  Controller 层（REST API，按端分包）             │
│  - ParentController / StudentController         │
│  - AdminController / TicketController           │
├────────────────────────────────────────────────┤
│  Service 层（业务逻辑，事务边界）               │
│  - 接口 + Impl 分离                             │
│  - 状态机流转校验在此层完成                      │
├────────────────────────────────────────────────┤
│  Mapper 层（MyBatis-Plus，数据访问）            │
│  - 继承 BaseMapper<T>                           │
│  - 复杂查询用 LambdaQueryWrapper                │
├────────────────────────────────────────────────┤
│  Entity 层（数据库实体，与表一一对应）           │
│  - @TableName / @TableId / @TableField          │
├────────────────────────────────────────────────┤
│  MySQL 8.0 (InnoDB, utf8mb4)                    │
└────────────────────────────────────────────────┘
```

## 数据模型关系

```
user (1) ──── (0..1) student_profile       [学生角色才有档案]
user (1) ──── (0..N) demand                [家长角色提交需求]
user (1) ──── (0..N) ticket                [家长/学生提交工单]

student_profile (1) ──── (0..N) review_record     [每次审核一条记录]
student_profile (1) ──── (0..N) recommendation    [可被推荐给多条需求]

demand (1) ──── (0..5) recommendation       [每条需求最多推荐5人]
demand (1) ──── (0..N) ticket               [工单可关联需求]

ticket (1) ──── (0..N) ticket_reply         [一个工单多条回复]
```

## 状态机

### 需求状态（demand.status）

```
PENDING(0) → MATCHING(1) → RECOMMENDED(2) → CLOSED(3)
     │                                         ↑
     └─────────────────────────────────────────┘（任意状态可关闭）
```

### 审核状态（student_profile.review_status）

```
DRAFT(0) → PENDING_REVIEW(1) → APPROVED(2)
                │
                ├→ REJECTED(3) → DRAFT(0)（重新编辑）
                └→ NEED_SUPPLEMENT(4) → PENDING_REVIEW(1)（补充后重提）
```

### 工单状态（ticket.status）

```
PENDING(0) → PROCESSING(1) → RESOLVED(2) → CLOSED(3)
                  │                           ↑
                  └───────────────────────────┘（直接关闭无效工单）
```

## API 设计规范

- RESTful 风格，路径用 kebab-case
- 统一响应体：`{ code: int, message: string, data: T }`
- 分页参数：`pageNum`（从1开始）、`pageSize`（默认10，最大100）
- 认证方式：JWT Token，Header 携带 `Authorization: Bearer {token}`
- 接口文档：Knife4j，访问 `/doc.html`

## 编号生成

| 类型 | 前缀 | 格式 | 实现 |
|------|------|------|------|
| 需求编号 | REQ | REQ + yyyyMMdd + 4位流水 | Redis INCR |
| 工单编号 | TKT | TKT + yyyyMMdd + 4位流水 | Redis INCR |
| 学生编号 | STU | STU + yyyyMMdd + 4位流水 | Redis INCR |

Redis Key 格式：`seq:{prefix}:{yyyyMMdd}`，次日凌晨过期。
