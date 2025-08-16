#!/bin/bash

# Railway部署脚本
# 使用方法: ./deploy-to-railway.sh

set -e

echo "🚀 开始部署博客系统到Railway..."

# 检查Railway CLI是否安装
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI未安装，正在安装..."
    npm install -g @railway/cli
fi

# 登录Railway
echo "🔐 登录Railway..."
railway login

# 创建项目（如果不存在）
echo "📁 创建Railway项目..."
railway init

# 部署后端
echo "🔧 部署后端服务..."
cd backend-spring
railway up

# 等待后端部署完成
echo "⏳ 等待后端服务启动..."
sleep 30

# 获取后端服务URL
BACKEND_URL=$(railway status --json | jq -r '.services[] | select(.name == "backend") | .url')
echo "✅ 后端服务URL: $BACKEND_URL"

# 部署前端
echo "🎨 部署前端应用..."
cd ..
# 更新前端环境变量
echo "VITE_API_BASE_URL=$BACKEND_URL" > .env.production

# 构建前端
npm run build:prod

# 部署到Railway
railway up

echo "🎉 部署完成！"
echo "📱 前端地址: $(railway status --json | jq -r '.services[] | select(.name == "frontend") | .url')"
echo "🔧 后端地址: $BACKEND_URL"
