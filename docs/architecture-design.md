# DataAgent 智能 BI 报告平台 — 软件架构设计

> 版本：v1.0  
> 日期：2026-07-01  
> 状态：设计草稿

---

## 目录

1. [系统概述](#1-系统概述)
2. [系统架构图](#2-系统架构图)
3. [技术架构决策](#3-技术架构决策)
4. [模块设计](#4-模块设计)
5. [数据库设计](#5-数据库设计)
6. [接口设计](#6-接口设计)
7. [页面设计](#7-页面设计)
8. [目录结构](#8-目录结构)
9. [开发计划](#9-开发计划)

---

## 1. 系统概述

### 1.1 项目定位

模仿阿里 DataWorks、腾讯云 BI、字节跳动风神等企业内部 ChatBI 产品，构建一个面向校招/实习简历展示的企业级 AI 智能 BI 分析平台。核心价值在于：

- **自然语言驱动分析**：用户用中文提问，AI Agent 自动完成 SQL 生成、执行、可视化全链路
- **知识增强**：通过 RAG 技术，让 AI 理解企业私有数据文档
- **企业级权限**：RBAC 细粒度权限控制，多角色管理
- **全生命周期管理**：报告、Prompt、上下文、文件的版本管理和追溯

### 1.2 核心业务流程

```
用户提问 → Agent 编排 → RAG 检索 → SQL 生成 → SQL 执行 → 图表生成 → 报告渲染
```

---

## 2. 系统架构图

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           前端 (Vue3 + TypeScript)                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │  登录页   │ │Dashboard │ │  ChatBI   │ │ 报告中心  │ │ 系统管理     │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │            Element Plus + ECharts + Axios + Pinia                │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└────────────────────────┬─────────────────────────────────────────────────┘
                         │ HTTP / WebSocket (JWT Token)
                         ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                     后端 (Spring Boot 3.x)                               │
│                                                                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ Controller│ │ Service  │ │  Mapper  │ │ Security │ │  WebSocket   │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │     Spring MVC + MyBatis Plus + Spring Security + JWT + Redis   │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└────────┬────────────────────────┬─────────────────────────────────────────┘
         │                        │
         ▼                        ▼
┌─────────────────┐   ┌───────────────────────────────────────────────────┐
│    MySQL 8.x     │   │           AI 服务 (Python FastAPI)               │
│  ┌───────────┐   │   │  ┌──────────┐ ┌──────────┐ ┌────────────────┐  │
│  │ 业务数据表  │   │   │  │ ChatBI   │ │   RAG    │ │  Agent 编排    │  │
│  └───────────┘   │   │  │  Workflow │ │  Pipeline│ │  (LangChain)   │  │
│  ┌───────────┐   │   │  └──────────┘ └──────────┘ └────────────────┘  │
│  │ 向量索引   │   │   │  ┌──────────┐ ┌──────────┐ ┌────────────────┐  │
│  └───────────┘   │   │  │FAISS向量库│ │ Embedding│ │   Qwen API     │  │
└─────────────────┘   │  └──────────┘ └──────────┘ └────────────────┘  │
                       └───────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────┐
│                          Docker 部署                                     │
│     Nginx (前端) + Spring Boot (后端) + FastAPI (AI) + MySQL + Redis     │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2.1 架构分层

| 层级 | 技术 | 职责 |
|------|------|------|
| **展示层** | Vue3 + TypeScript + Element Plus | 用户界面、交互、ECharts 图表渲染 |
| **网关层** | Nginx | 反向代理、静态资源托管、CORS |
| **业务层** | Spring Boot 3.x | RESTful API、RBAC 权限、业务编排 |
| **AI 服务层** | Python FastAPI + LangChain | Agent 编排、LLM 调用、RAG 检索、SQL 生成 |
| **数据层** | MySQL + Redis + FAISS | 持久化存储、缓存、向量检索 |
| **部署层** | Docker + Docker Compose | 容器化部署 |

### 2.2 数据流

```
用户提问 (ChatBI)
  → POST /api/chatbi/ask (携带 JWT Token)
  → 后端转发至 AI 服务 /api/agent/run
  → LangChain Agent Workflow:
      1. Memory 加载历史上下文
      2. Prompt 组装 + 变量注入
      3. RAG 检索相似知识文档
      4. Tool Calling (表结构查询)
      5. SQL Generator (LLM 生成 SQL)
      6. SQL Execute (执行并返回结果)
      7. Chart Generator (LLM 生成 ECharts 配置)
      8. Report Generator (组装 Markdown 报告)
  → 返回 JSON (数据 + 图表配置 + Markdown)
  → 前端 ECharts 渲染图表 + Markdown 展示报告
```

---

## 3. 技术架构决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 前端框架 | Vue3 + TypeScript | 国内生态成熟，适合中后台项目 |
| UI 库 | Element Plus | Vue3 官方推荐，组件丰富 |
| 图表库 | ECharts | 阿里出品，BI 场景首选 |
| 状态管理 | Pinia | Vue3 官方推荐，轻量 |
| 后端框架 | Spring Boot 3.x | 企业级标准，生态完善 |
| ORM | MyBatis Plus | 国内主流，开发效率高 |
| 安全 | Spring Security + JWT | 成熟认证授权方案 |
| 缓存 | Redis | 高性能，支持分布式 |
| AI 框架 | LangChain | Agent 编排业界标准 |
| 向量库 | FAISS | 轻量高性能，无需额外服务 |
| LLM | Qwen API | 通义千问，中文场景优秀 |
| API 文档 | Swagger / OpenAPI | 接口文档标准化 |
| 部署 | Docker Compose | 一键部署，简单可靠 |

---

## 4. 模块设计

### 4.1 模块总览

| 编号 | 模块 | 说明 | 优先级 |
|------|------|------|--------|
| M1 | **用户认证与权限** | JWT 登录、RBAC、菜单/接口权限 | P0 |
| M2 | **Dashboard** | 统计概览、图表展示 | P0 |
| M3 | **ChatBI 智能分析** | Agent 驱动自然语言→BI 报告 | P0 |
| M4 | **Agent 服务 (AI)** | LangChain 编排、RAG、Tool Calling | P0 |
| M5 | **RAG 知识库** | 文档上传、切片、向量检索 | P1 |
| M6 | **报告中心** | 报告 CRUD、版本管理、导出、分享 | P1 |
| M7 | **文件中心** | 文件上传、预览 | P1 |
| M8 | **上下文管理** | Memory 查看/编辑/过期管理 | P2 |
| M9 | **Prompt 管理** | 模板 CRUD、变量、版本、测试 | P2 |
| M10 | **SQL 管理** | SQL 执行历史、统计、收藏 | P2 |
| M11 | **日志中心** | Agent/异常/SQL 日志、Token 统计 | P2 |

### 4.2 模块详细设计

#### M1: 用户认证与权限

- JWT 登录：用户名密码 → JWT Token（Access + Refresh）
- RBAC：用户 → 角色 → 权限（菜单权限 + 接口权限）
- 菜单管理：动态路由，不同角色看到不同菜单
- 接口权限：Spring Security FilterChain 拦截

#### M2: Dashboard

- 统计卡片：用户数、报告数、Agent调用数、Token消耗
- 最近报告列表
- Agent 调用趋势图（ECharts 折线图）
- Token 消耗趋势图
- 报告分类统计（饼图）

#### M3: ChatBI 智能分析

- 对话式交互界面
- 流式响应（SSE/WebSocket）
- 历史对话展示
- 图表与 Markdown 混合渲染
- 报告保存入口

#### M4: Agent 服务 (AI)

Workflow 详细流程：

```
用户问题
  ↓
Memory 加载
  (从 Redis/MySQL 加载最近 N 轮对话)
  ↓
Prompt 组装
  (加载 Prompt 模板 + 注入 SQL 方言/表结构/变量)
  ↓
RAG 检索
  (Embedding 用户问题 → FAISS 检索 Top-K 文档)
  ↓
Tool Calling
  (查询数据库表结构信息：表名、字段、类型、注释)
  ↓
SQL Generator
  (LLM 根据问题 + 表结构 + 参考文档 → 生成 SQL)
  ↓
SQL Execute
  (安全执行 SQL，限制查询量，记录执行历史)
  ↓
Chart Generator
  (LLM 根据 SQL 结果 → ECharts option JSON)
  ↓
Report Generator
  (LLM 组装分析结论 + 图表 → Markdown 报告)
  ↓
返回结构化结果
```

#### M5: RAG 知识库

- 文档上传：支持 PDF / Word / Markdown
- 文档解析：提取文本内容
- 文本切片：按段落/固定长度切片
- Embedding：调用 Qwen Embedding API
- 向量存储：FAISS 索引
- 检索：用户问题 Embedding → FAISS 相似度搜索

---

## 5. 数据库设计

### 5.1 ER 图（文本描述）

```
用户 1──N 角色  N──N 权限
用户 1──N 聊天记录
聊天记录 1──N 上下文
聊天记录 1──1 报告
报告 1──N 报告版本
Agent 日志 N──1 用户
SQL 执行历史 N──1 用户
知识库文档 N──1 用户
文件 N──1 用户
角色 N──N 菜单
Prompt 模板
```

### 5.2 数据表详细设计

#### 1. 用户表 `sys_user`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| username | varchar(50) | UNIQUE, NOT NULL | 用户名 |
| password | varchar(255) | NOT NULL | 密码 (BCrypt) |
| nickname | varchar(50) | | 昵称 |
| email | varchar(100) | | 邮箱 |
| phone | varchar(20) | | 手机号 |
| avatar | varchar(255) | | 头像 URL |
| role_id | bigint | FK → sys_role.id | 角色 ID |
| status | tinyint | DEFAULT 1 | 状态：0禁用 1启用 |
| remark | varchar(255) | | 备注 |
| created_at | datetime | | 创建时间 |
| updated_at | datetime | | 更新时间 |
| deleted | tinyint | DEFAULT 0 | 逻辑删除 |

#### 2. 角色表 `sys_role`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| name | varchar(50) | UNIQUE, NOT NULL | 角色名称 |
| code | varchar(50) | UNIQUE, NOT NULL | 角色编码 |
| sort | int | DEFAULT 0 | 排序 |
| status | tinyint | DEFAULT 1 | 状态 |
| remark | varchar(255) | | 备注 |
| created_at | datetime | | |
| updated_at | datetime | | |

#### 3. 权限表 `sys_permission`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| name | varchar(100) | NOT NULL | 权限名称 |
| code | varchar(100) | UNIQUE, NOT NULL | 权限编码 (如 `chatbi:ask`) |
| type | tinyint | NOT NULL | 类型：1菜单 2按钮 3接口 |
| parent_id | bigint | FK, DEFAULT 0 | 父级 ID |
| sort | int | DEFAULT 0 | 排序 |
| remark | varchar(255) | | 备注 |
| created_at | datetime | | |

#### 4. 菜单表 `sys_menu`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| name | varchar(100) | NOT NULL | 菜单名称 |
| permission_code | varchar(100) | FK → sys_permission.code | 权限编码 |
| path | varchar(200) | | 前端路由路径 |
| component | varchar(200) | | 前端组件路径 |
| icon | varchar(100) | | 图标 |
| parent_id | bigint | FK, DEFAULT 0 | 父菜单 ID |
| sort | int | DEFAULT 0 | 排序 |
| type | tinyint | NOT NULL | 0目录 1菜单 2按钮 |
| visible | tinyint | DEFAULT 1 | 是否可见 |
| status | tinyint | DEFAULT 1 | 状态 |
| created_at | datetime | | |

中间表：`sys_role_permission` (role_id, permission_id)、`sys_role_menu` (role_id, menu_id)

#### 5. 聊天记录表 `chat_message`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| user_id | bigint | FK, NOT NULL | 用户 ID |
| session_id | varchar(64) | NOT NULL | 会话 ID |
| role | varchar(20) | NOT NULL | user / assistant / system |
| content | text | NOT NULL | 消息内容 |
| message_type | varchar(20) | DEFAULT 'text' | text / chart / report |
| metadata | json | | 附加元数据 (图表配置等) |
| token_count | int | DEFAULT 0 | Token 数 |
| created_at | datetime | | |

索引：`idx_session_id`、`idx_user_id`

#### 6. 上下文表 `chat_context`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| session_id | varchar(64) | NOT NULL | 会话 ID |
| user_id | bigint | FK, NOT NULL | 用户 ID |
| context_key | varchar(255) | NOT NULL | 上下文键 |
| context_value | text | NOT NULL | 上下文值 (JSON) |
| expire_at | datetime | | 过期时间 |
| created_at | datetime | | |
| updated_at | datetime | | |

索引：`idx_session_id`

#### 7. Prompt 模板表 `prompt_template`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| name | varchar(100) | NOT NULL | 模板名称 |
| code | varchar(100) | UNIQUE, NOT NULL | 模板编码 |
| description | varchar(500) | | 描述 |
| content | text | NOT NULL | 模板内容 |
| variables | json | | 变量定义 [{name, type, default, required}] |
| version | int | DEFAULT 1 | 版本号 |
| status | tinyint | DEFAULT 1 | 0草稿 1发布 |
| created_by | bigint | FK | 创建人 |
| created_at | datetime | | |
| updated_at | datetime | | |

#### 8. 知识库文档表 `knowledge_doc`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| title | varchar(200) | NOT NULL | 文档标题 |
| doc_type | varchar(20) | NOT NULL | pdf / word / markdown |
| file_id | bigint | FK → file.id | 关联文件 |
| content | longtext | | 原始文本内容 |
| chunk_count | int | DEFAULT 0 | 切片数 |
| status | tinyint | DEFAULT 0 | 0处理中 1已就绪 2失败 |
| created_by | bigint | FK | 上传人 |
| created_at | datetime | | |
| updated_at | datetime | | |

#### 9. 文档切片表 `knowledge_chunk`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| doc_id | bigint | FK → knowledge_doc.id | 文档 ID |
| chunk_index | int | NOT NULL | 切片序号 |
| content | text | NOT NULL | 切片文本 |
| embedding_id | varchar(64) | | FAISS 向量 ID |
| token_count | int | DEFAULT 0 | Token 数 |
| created_at | datetime | | |

#### 10. 报告表 `report`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| title | varchar(200) | NOT NULL | 报告标题 |
| description | varchar(500) | | 描述 |
| content_md | longtext | | Markdown 内容 |
| content_html | longtext | | HTML 内容 |
| chart_config | json | | ECharts 配置 |
| data_json | json | | 原始数据 |
| session_id | varchar(64) | | 关联会话 |
| status | tinyint | DEFAULT 0 | 0生成中 1已生成 2失败 |
| version | int | DEFAULT 1 | 当前版本 |
| created_by | bigint | FK | 创建人 |
| created_at | datetime | | |
| updated_at | datetime | | |

#### 11. 报告版本表 `report_version`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| report_id | bigint | FK → report.id | 报告 ID |
| version | int | NOT NULL | 版本号 |
| content_md | longtext | | Markdown 内容 |
| content_html | longtext | | HTML 内容 |
| chart_config | json | | 图表配置 |
| change_note | varchar(500) | | 变更说明 |
| created_by | bigint | FK | |
| created_at | datetime | | |

#### 12. 文件表 `file`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| original_name | varchar(255) | NOT NULL | 原文件名 |
| stored_name | varchar(255) | NOT NULL | 存储文件名 |
| path | varchar(500) | NOT NULL | 存储路径 |
| size | bigint | NOT NULL | 文件大小 (byte) |
| type | varchar(50) | NOT NULL | MIME 类型 |
| ext | varchar(20) | | 扩展名 |
| storage_type | varchar(20) | DEFAULT 'local' | local / oss |
| url | varchar(500) | | 访问 URL |
| created_by | bigint | FK | 上传人 |
| created_at | datetime | | |

#### 13. SQL 执行历史表 `sql_history`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| user_id | bigint | FK, NOT NULL | 执行人 |
| session_id | varchar(64) | | 关联会话 |
| original_question | varchar(500) | | 原始问题 |
| generated_sql | text | NOT NULL | 生成的 SQL |
| execute_result | json | | 执行结果 |
| row_count | int | | 影响/返回行数 |
| cost_ms | int | | 执行耗时 (ms) |
| status | tinyint | DEFAULT 0 | 0成功 1失败 |
| error_msg | text | | 错误信息 |
| is_favorite | tinyint | DEFAULT 0 | 是否收藏 |
| token_cost | int | DEFAULT 0 | Token 消耗 |
| created_at | datetime | | |

#### 14. Agent 日志表 `agent_log`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| user_id | bigint | FK | 用户 ID |
| session_id | varchar(64) | | 会话 ID |
| agent_type | varchar(50) | NOT NULL | Agent 类型 |
| input | text | | 输入内容 |
| output | text | | 输出内容 |
| steps | json | | Workflow 各步骤详情 |
| llm_model | varchar(50) | | LLM 模型名 |
| prompt_tokens | int | DEFAULT 0 | Prompt Token 数 |
| completion_tokens | int | DEFAULT 0 | 生成 Token 数 |
| total_tokens | int | DEFAULT 0 | 总 Token 数 |
| cost_ms | int | | 总耗时 |
| status | tinyint | DEFAULT 0 | 0成功 1失败 |
| error_msg | text | | 错误信息 |
| ip | varchar(50) | | 请求 IP |
| created_at | datetime | | |

### 5.3 Redis 缓存设计

| Key | Value | TTL | 说明 |
|-----|-------|-----|------|
| `token:access:{userId}` | JWT Token | 2h | Access Token |
| `token:refresh:{userId}` | Refresh Token | 7d | 刷新 Token |
| `captcha:{uuid}` | 验证码 | 5m | 登录验证码 |
| `context:session:{sessionId}` | 对话上下文 JSON | 1h | 最近对话历史 |
| `rate:limit:{userId}` | 计数 | 1s | 接口限流 |

---

## 6. 接口设计

### 6.1 接口规范

- 基础路径：`/api`
- 响应格式统一：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1719820800000
}
```

- 分页请求：

```json
{
  "page": 1,
  "pageSize": 10,
  "sortField": "created_at",
  "sortOrder": "desc"
}
```

- 分页响应：

```json
{
  "records": [],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

### 6.2 接口列表

#### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录 | 公开 |
| POST | `/api/auth/register` | 注册 | 公开 |
| POST | `/api/auth/refresh` | 刷新 Token | 公开 |
| POST | `/api/auth/logout` | 登出 | 已认证 |
| GET | `/api/auth/me` | 获取当前用户信息 | 已认证 |

#### 用户管理 `/api/users`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/users` | 用户分页列表 | sys:user:list |
| GET | `/api/users/{id}` | 用户详情 | sys:user:detail |
| POST | `/api/users` | 新增用户 | sys:user:add |
| PUT | `/api/users/{id}` | 更新用户 | sys:user:edit |
| DELETE | `/api/users/{id}` | 删除用户 | sys:user:delete |
| PUT | `/api/users/{id}/status` | 变更状态 | sys:user:edit |

#### 角色管理 `/api/roles`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/roles` | 角色列表 | sys:role:list |
| GET | `/api/roles/{id}` | 角色详情 | sys:role:detail |
| POST | `/api/roles` | 新增角色 | sys:role:add |
| PUT | `/api/roles/{id}` | 更新角色 | sys:role:edit |
| DELETE | `/api/roles/{id}` | 删除角色 | sys:role:delete |
| PUT | `/api/roles/{id}/permissions` | 分配权限 | sys:role:assign |

#### 权限管理 `/api/permissions`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/permissions` | 权限树 | sys:perm:list |
| GET | `/api/permissions/all` | 全部权限 | sys:perm:list |

#### 菜单管理 `/api/menus`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/menus` | 菜单树 (管理) | sys:menu:list |
| GET | `/api/menus/user` | 当前用户菜单 | 已认证 |
| POST | `/api/menus` | 新增菜单 | sys:menu:add |
| PUT | `/api/menus/{id}` | 更新菜单 | sys:menu:edit |
| DELETE | `/api/menus/{id}` | 删除菜单 | sys:menu:delete |

#### Dashboard `/api/dashboard`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/dashboard/stats` | 统计卡片 | dashboard:view |
| GET | `/api/dashboard/recent-reports` | 最近报告 | dashboard:view |
| GET | `/api/dashboard/agent-trend` | Agent 调用趋势 | dashboard:view |
| GET | `/api/dashboard/token-trend` | Token 消耗趋势 | dashboard:view |
| GET | `/api/dashboard/user-stats` | 用户统计 | dashboard:view |

#### ChatBI `/api/chatbi`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/chatbi/ask` | 提问 (同步) | chatbi:ask |
| POST | `/api/chatbi/ask/stream` | 提问 (SSE 流式) | chatbi:ask |
| GET | `/api/chatbi/sessions` | 会话列表 | chatbi:view |
| DELETE | `/api/chatbi/sessions/{sessionId}` | 删除会话 | chatbi:delete |
| GET | `/api/chatbi/messages` | 会话消息列表 | chatbi:view |

#### AI 服务 (内部接口) `/api/agent`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/agent/run` | 执行 Agent Workflow | 后端内部 |
| POST | `/api/agent/sql/generate` | 生成 SQL | 后端内部 |
| POST | `/api/agent/chart/generate` | 生成图表配置 | 后端内部 |
| POST | `/api/agent/report/generate` | 生成报告 | 后端内部 |

#### RAG 知识库 `/api/knowledge`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/knowledge/docs` | 文档列表 | knowledge:list |
| POST | `/api/knowledge/docs` | 上传文档 | knowledge:add |
| DELETE | `/api/knowledge/docs/{id}` | 删除文档 | knowledge:delete |
| GET | `/api/knowledge/docs/{id}/chunks` | 文档切片列表 | knowledge:list |
| POST | `/api/knowledge/retrieve` | 检索知识 | knowledge:search |

#### 报告中心 `/api/reports`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/reports` | 报告分页列表 | report:list |
| GET | `/api/reports/{id}` | 报告详情 | report:detail |
| PUT | `/api/reports/{id}` | 更新报告 | report:edit |
| DELETE | `/api/reports/{id}` | 删除报告 | report:delete |
| GET | `/api/reports/{id}/versions` | 版本列表 | report:version |
| POST | `/api/reports/{id}/versions` | 创建版本 | report:version |
| POST | `/api/reports/{id}/restore` | 恢复版本 | report:version |
| POST | `/api/reports/{id}/export/pdf` | 导出 PDF | report:export |
| POST | `/api/reports/{id}/export/excel` | 导出 Excel | report:export |
| POST | `/api/reports/{id}/share` | 生成分享链接 | report:share |

#### 文件中心 `/api/files`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/files/upload` | 上传文件 | file:upload |
| GET | `/api/files` | 文件列表 | file:list |
| DELETE | `/api/files/{id}` | 删除文件 | file:delete |
| GET | `/api/files/{id}/preview` | 文件预览 | file:preview |

#### 上下文管理 `/api/context`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/context/{sessionId}` | 查看上下文 | context:view |
| PUT | `/api/context/{sessionId}` | 编辑上下文 | context:edit |
| DELETE | `/api/context/{sessionId}` | 删除上下文 | context:delete |
| PUT | `/api/context/{sessionId}/expire` | 设置过期时间 | context:edit |

#### Prompt 管理 `/api/prompts`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/prompts` | 模板列表 | prompt:list |
| GET | `/api/prompts/{id}` | 模板详情 | prompt:detail |
| POST | `/api/prompts` | 新增模板 | prompt:add |
| PUT | `/api/prompts/{id}` | 更新模板 | prompt:edit |
| DELETE | `/api/prompts/{id}` | 删除模板 | prompt:delete |
| POST | `/api/prompts/{id}/test` | 测试模板 | prompt:test |
| POST | `/api/prompts/{id}/publish` | 发布模板 | prompt:publish |

#### SQL 管理 `/api/sql`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/sql/history` | 执行历史 | sql:list |
| GET | `/api/sql/history/{id}` | 执行详情 | sql:detail |
| PUT | `/api/sql/history/{id}/favorite` | 收藏/取消收藏 | sql:favorite |
| GET | `/api/sql/stats` | SQL 统计 | sql:stats |

#### 日志中心 `/api/logs`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/logs/agent` | Agent 日志列表 | log:agent |
| GET | `/api/logs/agent/{id}` | Agent 日志详情 | log:agent |
| GET | `/api/logs/error` | 异常日志 | log:error |
| GET | `/api/logs/sql` | SQL 日志 | log:sql |
| GET | `/api/logs/token-stats` | Token 消耗统计 | log:stats |

---

## 7. 页面设计

### 7.1 页面路由

| 路由 | 页面 | 说明 |
|------|------|------|
| `/login` | LoginPage | 登录页 |
| `/` | Layout | 布局容器 |
| `/dashboard` | DashboardPage | 工作台 |
| `/chatbi` | ChatBIPage | ChatBI 智能分析 |
| `/chatbi/:sessionId` | ChatBISessionPage | 对话会话 |
| `/reports` | ReportListPage | 报告列表 |
| `/reports/:id` | ReportDetailPage | 报告详情 |
| `/knowledge` | KnowledgePage | 知识库 |
| `/context` | ContextPage | 上下文管理 |
| `/prompts` | PromptListPage | Prompt 管理 |
| `/prompts/:id` | PromptEditPage | Prompt 编辑 |
| `/sql` | SQLHistoryPage | SQL 管理 |
| `/logs` | LogCenterPage | 日志中心 |
| `/system/users` | UserListPage | 用户管理 |
| `/system/roles` | RoleListPage | 角色管理 |
| `/system/menus` | MenuListPage | 菜单管理 |
| `/system/permissions` | PermissionListPage | 权限管理 |
| `/files` | FileListPage | 文件中心 |

### 7.2 页面布局

```
┌──────────────────────────────────────────────────┐
│  侧边栏 (Logo + 菜单树)        顶部栏 (用户信息)  │
│                                Breadcrumb        │
│                             ┌──────────────────┐ │
│                             │                  │ │
│                             │                  │ │
│       菜单区域               │   内容区域       │ │
│                             │   (Router View)  │ │
│                             │                  │ │
│                             │                  │ │
│                             │                  │ │
│                             └──────────────────┘ │
│                             底部栏 (版权信息)     │
└──────────────────────────────────────────────────┘
```

### 7.3 核心页面原型

#### ChatBI 页面

```
┌──────────────────────────────────────────────────────────────┐
│  ← 历史会话侧栏               智能数据分析                    │
│  ┌────────────────────┐  ┌────────────────────────────────┐ │
│  │ 历史会话1          │  │                                │ │
│  │ 历史会话2          │  │  用户：近30天销售额是多少？    │ │
│  │ 历史会话3          │  │  ────────────────────────────  │ │
│  │ ...               │  │  Agent：                        │ │
│  │                    │  │  📊 数据趋势分析报告           │ │
│  │ 新建会话          │  │                                │ │
│  │                    │  │  ┌──────────────────────────┐ │ │
│  └────────────────────┘  │  │  [ECharts 折线图]        │ │ │
│                          │  └──────────────────────────┘ │ │
│                          │                                │ │
│                          │  | 月份 | 销售额 | 增长 |     │ │
│                          │  |------|--------|------|     │ │
│                          │  | 1月  | 100万  | +5%  |     │ │
│                          │                                │ │
│                          │  **分析结论**：...              │ │
│                          │                                │ │
│                          │  [保存报告] [复制] [导出]      │ │
│                          │                                │ │
│                          │  ┌────────────────────────┐    │ │
│                          │  │ 输入问题... [发送] 🎤  │    │ │
│                          │  └────────────────────────┘    │ │
│                          └────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

#### 报告中心页面

```
┌──────────────────────────────────────────────────────────────┐
│  报告中心                                    [新建报告]      │
│  ┌──────┬──────┬──────┬──────┬──────┬──────┬──────────────┐ │
│  │ 报告标题 │ 创建人 │ 版本 │ 状态 │ 创建时间 │ 操作         │ │
│  ├──────┼──────┼──────┼──────┼──────┼──────┼──────────────┤ │
│  │ 销售分析 │ admin │ v3   │ 已生成 │ 07/01  │ 查看 版本 导出│ │
│  │ 用户增长 │ admin │ v1   │ 已生成 │ 06/30  │ 查看 版本 导出│ │
│  │ ...     │       │      │      │       │              │ │
│  └──────┴──────┴──────┴──────┴──────┴──────┴──────────────┘ │
│  [分页控件]                                                    │
└──────────────────────────────────────────────────────────────┘
```

---

## 8. 目录结构

```
DataAgent 智能 BI 报告平台/
├── frontend/                          # 前端工程 (Vue3 + TypeScript)
│   ├── public/
│   │   └── favicon.ico
│   ├── src/
│   │   ├── api/                       # Axios 接口封装
│   │   │   ├── request.ts             # Axios 实例 + 拦截器
│   │   │   ├── auth.ts                # 认证接口
│   │   │   ├── dashboard.ts           # Dashboard 接口
│   │   │   ├── chatbi.ts              # ChatBI 接口
│   │   │   ├── report.ts              # 报告接口
│   │   │   ├── knowledge.ts           # 知识库接口
│   │   │   ├── prompt.ts              # Prompt 接口
│   │   │   ├── sql.ts                 # SQL 接口
│   │   │   ├── log.ts                 # 日志接口
│   │   │   ├── file.ts                # 文件接口
│   │   │   ├── context.ts             # 上下文接口
│   │   │   └── system/                # 系统管理接口
│   │   │       ├── user.ts
│   │   │       ├── role.ts
│   │   │       ├── menu.ts
│   │   │       └── permission.ts
│   │   ├── assets/                    # 静态资源
│   │   │   ├── styles/
│   │   │   │   ├── variables.scss
│   │   │   │   ├── global.scss
│   │   │   │   └── mixins.scss
│   │   │   └── images/
│   │   ├── components/                # 公共组件
│   │   │   ├── AppLayout.vue          # 布局容器
│   │   │   ├── Sidebar.vue            # 侧边栏
│   │   │   ├── Navbar.vue             # 顶部导航
│   │   │   ├── MarkdownRenderer.vue   # Markdown 渲染
│   │   │   ├── ChartRenderer.vue      # ECharts 图表渲染
│   │   │   ├── FileUploader.vue       # 文件上传
│   │   │   ├── Pagination.vue         # 分页
│   │   │   └── PermissionGuard.vue    # 权限控制
│   │   ├── composables/               # 组合式函数
│   │   │   ├── useAuth.ts
│   │   │   ├── usePermission.ts
│   │   │   └── usePagination.ts
│   │   ├── router/                    # 路由
│   │   │   ├── index.ts
│   │   │   └── guard.ts               # 路由守卫
│   │   ├── stores/                    # Pinia 状态
│   │   │   ├── auth.ts
│   │   │   ├── app.ts
│   │   │   └── chat.ts
│   │   ├── types/                     # TypeScript 类型
│   │   │   ├── api.ts                 # API 通用类型
│   │   │   ├── auth.ts
│   │   │   ├── report.ts
│   │   │   ├── chat.ts
│   │   │   └── system.ts
│   │   ├── utils/                     # 工具函数
│   │   │   ├── format.ts
│   │   │   ├── storage.ts
│   │   │   └── constants.ts
│   │   ├── views/                     # 页面
│   │   │   ├── login/
│   │   │   │   └── LoginPage.vue
│   │   │   ├── dashboard/
│   │   │   │   └── DashboardPage.vue
│   │   │   ├── chatbi/
│   │   │   │   ├── ChatBIPage.vue
│   │   │   │   └── ChatBISessionPage.vue
│   │   │   ├── report/
│   │   │   │   ├── ReportListPage.vue
│   │   │   │   └── ReportDetailPage.vue
│   │   │   ├── knowledge/
│   │   │   │   └── KnowledgePage.vue
│   │   │   ├── context/
│   │   │   │   └── ContextPage.vue
│   │   │   ├── prompt/
│   │   │   │   ├── PromptListPage.vue
│   │   │   │   └── PromptEditPage.vue
│   │   │   ├── sql/
│   │   │   │   └── SQLHistoryPage.vue
│   │   │   ├── log/
│   │   │   │   └── LogCenterPage.vue
│   │   │   ├── file/
│   │   │   │   └── FileListPage.vue
│   │   │   └── system/
│   │   │       ├── UserListPage.vue
│   │   │       ├── RoleListPage.vue
│   │   │       ├── MenuListPage.vue
│   │   │       └── PermissionListPage.vue
│   │   ├── App.vue
│   │   └── main.ts
│   ├── index.html
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── .env.development
│   ├── .env.production
│   └── package.json
│
├── backend/                           # 后端工程 (Spring Boot)
│   ├── src/main/java/com/dataagent/
│   │   ├── DataAgentApplication.java  # 启动类
│   │   ├── common/                    # 公共模块
│   │   │   ├── config/
│   │   │   │   ├── WebMvcConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── MyBatisPlusConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── CorsConfig.java
│   │   │   ├── constant/
│   │   │   ├── enums/
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── BusinessException.java
│   │   │   ├── jackson/
│   │   │   ├── model/
│   │   │   │   ├── R.java                   # 统一响应
│   │   │   │   └── PageResult.java           # 分页结果
│   │   │   └── util/
│   │   ├── auth/                      # 认证模块
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   ├── security/
│   │   │   └── service/
│   │   ├── modules/                   # 业务模块 (按功能分包)
│   │   │   ├── dashboard/
│   │   │   ├── chatbi/
│   │   │   ├── report/
│   │   │   ├── knowledge/
│   │   │   ├── prompt/
│   │   │   ├── context/
│   │   │   ├── sql/
│   │   │   ├── log/
│   │   │   ├── file/
│   │   │   └── system/
│   │   │       ├── user/
│   │   │       ├── role/
│   │   │       ├── menu/
│   │   │       └── permission/
│   │   └── ai/                        # AI 服务调用
│   │       ├── client/
│   │       └── dto/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   ├── mapper/                    # MyBatis XML
│   │   └── db/
│   │       └── migration/
│   ├── pom.xml
│   └── Dockerfile
│
├── ai-service/                        # AI 服务 (Python FastAPI)
│   ├── app/
│   │   ├── __init__.py
│   │   ├── main.py                    # FastAPI 入口
│   │   ├── config.py                  # 配置
│   │   ├── models/                    # 数据模型
│   │   │   ├── __init__.py
│   │   │   ├── chat.py
│   │   │   ├── agent.py
│   │   │   └── knowledge.py
│   │   ├── agent/                     # LangChain Agent
│   │   │   ├── __init__.py
│   │   │   ├── workflow.py            # Agent 编排
│   │   │   ├── memory.py              # 对话记忆
│   │   │   ├── sql_generator.py       # SQL 生成
│   │   │   ├── chart_generator.py     # 图表生成
│   │   │   ├── report_generator.py    # 报告生成
│   │   │   └── tools.py              # Tool 定义
│   │   ├── rag/                       # RAG 模块
│   │   │   ├── __init__.py
│   │   │   ├── document_loader.py     # 文档加载
│   │   │   ├── text_splitter.py       # 文本切片
│   │   │   ├── embeddings.py          # Embedding
│   │   │   ├── vector_store.py        # FAISS 向量库
│   │   │   └── retriever.py           # 检索器
│   │   ├── api/                       # API 路由
│   │   │   ├── __init__.py
│   │   │   ├── agent_routes.py
│   │   │   ├── rag_routes.py
│   │   │   └── health_routes.py
│   │   └── utils/
│   │       ├── __init__.py
│   │       └── logger.py
│   ├── requirements.txt
│   ├── Dockerfile
│   └── .env.example
│
├── sql/                               # SQL 脚本
│   ├── init.sql                       # 建库建表
│   ├── seed.sql                       # 初始数据
│   └── migrations/                    # 版本迁移
│
├── docker/
│   ├── docker-compose.yml
│   ├── nginx/
│   │   ├── nginx.conf
│   │   └── conf.d/
│   │       └── dataagent.conf
│   └── mysql/
│       └── my.cnf
│
├── docs/                              # 文档
│   ├── architecture-design.md         # 本文
│   ├── api-docs.md                    # API 设计
│   └── deployment-guide.md            # 部署指南
│
├── .gitignore
└── README.md
```

---

## 9. 开发计划

### 阶段划分 (12 个 Sprint)

| Sprint | 模块 | 内容 | 预计工时 |
|--------|------|------|----------|
| **S1** | 项目初始化 | 前后端脚手架搭建、Docker 配置 | 1d |
| **S2** | M1 认证与权限 | 登录 + JWT + RBAC + 菜单权限 | 2d |
| **S3** | M2 Dashboard | 统计卡片 + 图表 + 页面 | 1d |
| **S4** | M4 AI 服务核心 | FastAPI + LangChain Agent Workflow | 2d |
| **S5** | M3 ChatBI (一) | 对话界面 + 流式响应 + Agent 集成 | 2d |
| **S6** | M3 ChatBI (二) | SQL 生成 + 图表渲染 + 报告渲染 | 2d |
| **S7** | M5 RAG 知识库 | 文档解析 + Embedding + FAISS 检索 | 2d |
| **S8** | M6 报告中心 | CRUD + 版本管理 + 导出 + 分享 | 1.5d |
| **S9** | M7 文件中心 + M8 上下文 | 上传/预览 + Memory 管理 | 1d |
| **S10** | M9 Prompt + M10 SQL | 模板管理 + SQL 历史 | 1d |
| **S11** | M11 日志中心 | Agent/异常/SQL 日志 + Token 统计 | 1d |
| **S12** | 集成测试 + 部署 | 全链路联调 + Docker 部署 | 1d |

**合计：约 17.5 天**

### 开发顺序原则

1. **先基础后业务**：脚手架 → 认证权限 → 核心业务
2. **先后端后前端**：每个模块先完成后端 API，再开发前端页面
3. **先核心后边缘**：ChatBI Agent 为核心，RAG、报告中心为次核心，日志/上下文为边缘
4. **每个 Sprint 产出可运行**：每个阶段结束时代码可编译、可运行

---

> **下一步**：请确认此架构设计，我将从 **S1: 项目初始化** 开始，搭建前后端脚手架。

