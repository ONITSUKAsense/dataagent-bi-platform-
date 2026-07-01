#!/bin/bash
# DataAgent 一键部署脚本
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

echo "===== DataAgent 部署开始 ====="

# 1. 环境变量
if [ ! -f .env ]; then
    echo "[1/4] 创建 .env 文件..."
    cp .env.example .env
else
    echo "[1/4] .env 文件已存在，跳过"
fi

# 2. 构建前端
echo "[2/4] 构建前端..."
cd frontend
npm install --silent
npm run build
cd ..

# 3. Docker 部署
echo "[3/4] 启动 Docker 服务..."
cd docker
docker compose pull
docker compose up -d --build

# 4. 健康检查
echo "[4/4] 健康检查..."
sleep 5
if curl -s http://localhost:8080/api/dashboard/stats > /dev/null 2>&1; then
    echo "✅ 后端服务正常"
else
    echo "⚠️  后端服务未响应，请检查日志: docker compose logs backend"
fi

echo ""
echo "===== DataAgent 部署完成 ====="
echo "前端地址: http://localhost"
echo "后端 API: http://localhost:8080/api"
echo "Swagger:  http://localhost:8080/swagger-ui.html"
echo "AI 服务:  http://localhost:8000"
echo ""
echo "默认账号: admin / admin123"
