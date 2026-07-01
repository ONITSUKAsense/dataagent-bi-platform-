# DataAgent 智能 BI 报告平台

AI 驱动的智能商业智能（BI）分析平台。通过自然语言交互，自动完成数据库查询、数据分析和报告生成，帮助企业快速获取数据洞察。

## 核心功能

- **智能对话分析** — 用自然语言提问，AI 自动生成 SQL、执行查询、返回图表和报告
- **RAG 知识库** — 上传文档（PDF/DOCX/MD），AI 结合知识库内容回答问题
- **报告中心** — 自动生成分析报告，支持版本管理、导出（HTML/CSV）、分享
- **Prompt 管理** — 可视化编辑 AI 提示词模板，变量定义，在线测试渲染
- **文件中心** — 文件上传 / 预览 / 管理
- **上下文管理** — 管理 AI 对话上下文，支持过期策略
- **SQL 管理** — 历史查询记录、收藏、执行统计
- **日志中心** — Agent 执行日志、Token 用量统计、错误追踪
- **系统管理** — 用户、角色、权限、菜单的完整 RBAC 管理

## 技术栈

### 前端
| 技术 | 说明 |
|------|------|
| Vue 3.4 | 渐进式 JavaScript 框架 |
| TypeScript | 类型安全 |
| Vite | 构建工具 |
| Element Plus | UI 组件库 |
| ECharts 5 | 图表可视化 |
| Pinia | 状态管理 |
| Axios | HTTP 请求 |
| Marked + Highlight.js | Markdown 渲染与代码高亮 |

### 后端
| 技术 | 说明 |
|------|------|
| Spring Boot 3.2.4 | 应用框架 |
| Spring Security | 认证授权 |
| JWT (JJWT 0.12.5) | 无状态令牌（access / refresh 双令牌） |
| MyBatis Plus 3.5.6 | ORM |
| MySQL 8.0 | 关系型数据库 |
| Redis 7 | 缓存 / SSE 会话管理 |
| Knife4j 4.5.0 | API 文档 |
| Spring SSE | 流式响应推送 |

### AI 服务
| 技术 | 说明 |
|------|------|
| Python FastAPI | AI 服务框架 |
| LangChain | Agent 工作流编排 |
| Qwen API | 大语言模型与 Embedding |
| FAISS | 向量相似度搜索 |
| PyMuPDF / python-docx | 文档解析 |
| RAG | 检索增强生成 |

### 部署
| 技术 | 说明 |
|------|------|
| Docker | 容器化 |
| Docker Compose | 多服务编排 |
| Nginx | 反向代理 + 前端静态资源 |

## 系统架构

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│  前端       │     │  后端         │     │  AI 服务      │
│  Vue 3      │────▶│  Spring Boot  │────▶│  FastAPI      │
│  Element +  │     │  JWT Auth     │     │  LangChain    │
│  ECharts    │     │  SSE Stream   │     │  Qwen API     │
└─────────────┘     └──────┬───────┘     └──────┬────────┘
                           │                    │
                           ▼                    ▼
                    ┌──────────────┐     ┌──────────────┐
                    │  MySQL       │     │  Redis        │
                    │  16 张表     │     │  缓存 / 会话  │
                    └──────────────┘     └──────────────┘
```

### Agent 工作流

```
用户提问 → 记忆加载 → RAG 检索 → 工具调用(表结构)
  → SQL 生成 → SQL 执行 → 图表生成 → 报告生成 → 响应返回
```

## 模块清单

| 模块 | 后端 | 前端页面 | 说明 |
|------|------|----------|------|
| 登录认证 | AuthController | LoginPage | JWT 登录/注册/刷新 |
| 工作台 | DashboardController | DashboardPage | 统计卡片 + 趋势图 |
| 智能分析 | ChatBiController + SSE | ChatBIPage | AI 对话 + 实时流式响应 |
| 报告中心 | ReportController + ExportController | ReportListPage / DetailPage | CRUD + 版本 + 导出 + 分享 |
| 知识库 | KnowledgeController | KnowledgePage | 文档上传 / 切片 / 检索 |
| Prompt 管理 | PromptController | PromptListPage / EditPage | 模板 CRUD / 变量 / 测试渲染 |
| 上下文管理 | ContextController | ContextPage | 会话上下文 / 过期策略 |
| SQL 管理 | SqlHistoryController | SQLHistoryPage | 执行历史 / 收藏 / 统计 |
| 日志中心 | LogController | LogCenterPage | Agent 日志 / Token 趋势 |
| 文件中心 | FileController | FileListPage | 文件上传 / 列表 / 预览 |
| 用户管理 | UserController | UserListPage | 用户 CRUD |
| 角色管理 | RoleController | RoleListPage | 角色 CRUD + 权限分配 |
| 菜单管理 | MenuController | MenuListPage | 菜单树管理 |
| 权限管理 | PermissionController | PermissionListPage | 权限树查看 |

## 数据库（16 表）

```
sys_user              — 用户
sys_role              — 角色
sys_permission        — 权限
sys_menu              — 菜单
sys_role_permission   — 角色-权限关联
sys_role_menu         — 角色-菜单关联
chat_message          — 对话消息
chat_context          — 对话上下文
prompt_template       — Prompt 模板
knowledge_doc         — 知识库文档
knowledge_chunk       — 知识库切片
report                — 报告
report_version        — 报告版本
file                  — 文件
sql_history           — SQL 执行历史
agent_log             — Agent 执行日志
```

## 快速启动

### 方式一：Docker 一键部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/ONITSUKAsense/dataagent-bi-platform-.git
cd dataagent-bi-platform-

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，可按需修改 MySQL 密码、JWT 密钥等

# 3. 启动所有服务
cd docker
docker compose up -d --build
```

启动后访问：
- 前端页面：http://localhost
- 后端 API：http://localhost:8080/api
- Swagger 文档：http://localhost:8080/swagger-ui.html
- AI 服务：http://localhost:8000

### 方式二：本地开发

需要先安装 MySQL 8.0、Redis 7、JDK 17、Node 20、Python 3.10+。

**1. 初始化数据库**
```bash
# 在 MySQL 中执行
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/seed.sql
```

**2. 启动后端**
```bash
cd backend
# 修改 application-dev.yml 中的数据库连接配置
mvn spring-boot:run -Dspring.profiles.active=dev
```

**3. 启动前端**
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

**4. 启动 AI 服务**
```bash
cd ai-service
pip install -r requirements.txt
# 如需使用 Qwen API，设置环境变量
# export QWEN_API_KEY=your_api_key
uvicorn app.main:app --reload --port 8000
```

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员（所有权限） |

## 项目结构

```
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/             # Axios API 接口
│   │   ├── components/      # 公共组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── types/           # TypeScript 类型
│   │   └── views/           # 页面组件
│   ├── Dockerfile           # 多阶段构建
│   └── package.json
│
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/dataagent/
│   │   ├── auth/            # 认证授权模块
│   │   ├── log/             # 日志模块
│   │   ├── modules/         # 业务模块
│   │   │   ├── chatbi/      # 智能分析
│   │   │   ├── context/     # 上下文管理
│   │   │   ├── dashboard/   # 工作台
│   │   │   ├── file/        # 文件中心
│   │   │   ├── knowledge/   # 知识库
│   │   │   ├── prompt/      # Prompt 管理
│   │   │   └── sql/         # SQL 管理
│   │   └── report/          # 报告模块
│   ├── Dockerfile
│   └── pom.xml
│
├── ai-service/              # Python AI 服务
│   ├── app/
│   │   ├── agent/           # Agent 工作流
│   │   ├── api/             # API 路由
│   │   ├── rag/             # RAG 检索
│   │   └── utils/           # LLM 客户端
│   ├── Dockerfile
│   └── requirements.txt
│
├── sql/                     # 数据库脚本
│   ├── init.sql             # 建表
│   └── seed.sql             # 初始数据
│
├── docker/                  # 部署配置
│   ├── docker-compose.yml   # 多服务编排
│   ├── nginx/               # Nginx 配置
│   ├── mysql/               # MySQL 配置
│   └── deploy.sh            # 一键部署脚本
│
├── docs/                    # 设计文档
│   └── architecture-design.md
│
└── .env.example             # 环境变量模板
```

## API 概览（75+ 接口）

所有 API 以 `/api` 为前缀，通过 JWT Bearer Token 认证。

| 分类 | 端点 | 说明 |
|------|------|------|
| 认证 | `POST /api/auth/login` | 用户登录 |
| 认证 | `POST /api/auth/refresh` | 刷新令牌 |
| 用户 | `GET/POST/PUT/DELETE /api/users` | 用户 CRUD |
| 角色 | `GET/POST/PUT/DELETE /api/roles` | 角色 CRUD + 权限分配 |
| 菜单 | `GET/POST/PUT/DELETE /api/menus` | 菜单管理 |
| 权限 | `GET /api/permissions` | 权限树 |
| 工作台 | `GET /api/dashboard/stats` | 统计概览 |
| 对话 | `POST /api/chatbi/ask` | AI 提问 |
| 对话 | `GET /api/chatbi/stream` (SSE) | 流式响应 |
| 报告 | `GET/POST/PUT/DELETE /api/reports` | 报告 CRUD |
| 报告 | `GET/POST /api/reports/{id}/versions` | 版本管理 |
| 报告 | `POST /api/reports/{id}/export/pdf` | 导出 HTML |
| 知识库 | `GET/POST/DELETE /api/knowledge/docs` | 文档管理 |
| Prompt | `GET/POST/PUT/DELETE /api/prompts` | 模板管理 |
| Prompt | `POST /api/prompts/{id}/publish` | 发布模板 |
| Prompt | `POST /api/prompts/{id}/test` | 测试渲染 |
| 上下文 | `GET/POST/PUT/DELETE /api/context` | 上下文管理 |
| SQL | `GET /api/sql/history` | 执行历史 |
| 日志 | `GET /api/logs/agent` | Agent 日志 |
| 日志 | `GET /api/logs/token-stats` | Token 统计 |
| 文件 | `POST /api/files/upload` | 文件上传 |

## 开发计划

项目按 12 个 Sprint 完成：

| Sprint | 内容 |
|--------|------|
| S1 | 项目初始化 + 目录结构 + 数据库 |
| S2 | 登录认证 + JWT + RBAC |
| S3 | 用户/角色/菜单/权限管理 |
| S4 | 工作台 Dashboard |
| S5 | ChatBI 智能分析 + SSE |
| S6 | 报告中心 + 版本管理 |
| S7 | RAG 知识库 |
| S8 | 报告导出 + 文件中心 |
| S9 | 上下文管理 |
| S10 | Prompt 管理 |
| S11 | 日志中心 |
| S12 | 集成测试 + Docker 部署 |
