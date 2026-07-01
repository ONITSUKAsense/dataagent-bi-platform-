# DataAgent 智能 BI 报告平台

AI 驱动的智能数据分析平台，支持自然语言查询、自动 SQL 生成、智能图表展示和报告管理。

## 技术栈

- **前端**: Vue3 + TypeScript + Vite + Element Plus + ECharts
- **后端**: Spring Boot 3.x + Spring Security + JWT + MyBatis Plus
- **AI**: Python FastAPI + LangChain + Qwen API + FAISS + RAG
- **部署**: Docker + Docker Compose

## 快速启动

```bash
# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d

# 前端开发
cd frontend && npm install && npm run dev

# 后端开发
cd backend && mvn spring-boot:run

# AI 服务开发
cd ai-service && pip install -r requirements.txt && uvicorn app.main:app --reload
```

## 项目结构

```
frontend/     — Vue3 前端
backend/      — Spring Boot 后端
ai-service/   — Python AI 服务
docs/         — 设计文档
sql/          — 数据库脚本
docker/       — 部署配置
```
