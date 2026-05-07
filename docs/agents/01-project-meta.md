# 01 - 项目元信息

## 基本信息

- **项目名**：博文学堂（AI_Educatin）
- **定位**：家长-大学生家教 O2O 撮合平台
- **三端**：家长端 H5 / 学生端 H5 / 后台管理端 PC Web
- **当前版本**：V1.0（MVP）
- **团队**：黄慕鑫（产品/需求）、冯云浩（后端开发）

## 技术栈

### 后端

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | LTS，编译与运行统一 |
| Spring Boot | 3.2.5 | 父 POM |
| MyBatis-Plus | 3.5.6 | Boot3 Starter |
| MySQL | 8.0+ | InnoDB, utf8mb4 |
| Redis | — | 编号生成、验证码、频率限制 |
| Knife4j | Jakarta 版 | API 文档 |
| Hutool | 5.8.25 | 工具库 |
| Lombok | 1.18.30 | BOM 管理 |

### 前端

| 组件 | 版本 | 说明 |
|------|------|------|
| Node.js | 18.x LTS | 构建环境 |
| Vue | 3.4.x | 三端统一 |
| Vant | 4.8.x | 移动端组件库（家长端/学生端） |
| Element Plus | 2.6.x | PC 组件库（后台管理端） |
| Axios | 1.6.x | HTTP 请求 |

## 目录结构

```
AI_Educatin/
├── AGENTS.md                  # Agent 导引地图
├── docs/
│   ├── agents/                # Agent 知识文档
│   └── exec-plans/            # 执行计划
├── src/main/
│   ├── java/org/example/ai_educatin/
│   │   ├── controller/        # REST Controller
│   │   ├── service/           # Service 接口 + 实现
│   │   ├── mapper/            # MyBatis-Plus Mapper
│   │   ├── entity/            # 数据库实体（对应7张表）
│   │   ├── dto/               # 数据传输对象
│   │   ├── config/            # 配置类
│   │   ├── enums/             # 枚举（状态机、角色等）
│   │   └── utils/             # 工具类
│   └── resources/
│       ├── application.yml    # 主配置
│       └── mapper/            # XML（如有）
├── frontend/                  # Vue 3 前端（三端合一）
├── sql/
│   └── schema.sql             # 建表脚本
├── scripts/                   # 自反馈脚本
├── 需求文档v1.md              # 产品需求
├── 数据库设计文档.md           # DB 设计
└── 数据流转与流程图.md         # 业务流程
```

## 构建与运行

```bash
# 后端
mvn clean package -DskipTests
java -jar target/AI_Educatin-0.0.1-SNAPSHOT.jar

# 前端
cd frontend && npm install && npm run build
# 产物在 frontend/dist/，由后端 static 或 Nginx 托管
```

## 数据库

- 库名：`ai_education`
- 7 张核心表：user / student_profile / review_record / demand / recommendation / ticket / ticket_reply
- 建表脚本：`sql/schema.sql`
- 详细设计：`数据库设计文档.md`
