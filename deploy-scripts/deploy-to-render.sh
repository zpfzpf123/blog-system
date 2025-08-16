#!/bin/bash

# Render部署脚本
# 使用方法: ./deploy-to-render.sh

set -e

echo "🚀 开始部署博客系统到Render..."

# 检查是否安装了必要的工具
if ! command -v git &> /dev/null; then
    echo "❌ Git未安装"
    exit 1
fi

# 创建Render配置文件
echo "📝 创建Render配置文件..."

# 后端服务配置
cat > render.yaml << EOF
services:
  - type: web
    name: blog-backend
    env: java
    buildCommand: mvn clean package -DskipTests
    startCommand: java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: SERVER_PORT
        value: 4567
    healthCheckPath: /api/health

  - type: web
    name: blog-frontend
    env: static
    buildCommand: npm ci && npm run build:prod
    staticPublishPath: ./dist
    envVars:
      - key: VITE_API_BASE_URL
        value: https://blog-backend.onrender.com

databases:
  - name: blogdb
    databaseName: blogdb
    user: bloguser
EOF

echo "✅ Render配置文件已创建"

# 提交更改到Git
echo "📤 提交更改到Git..."
git add .
git commit -m "Add Render deployment configuration"
git push

echo "🎉 配置完成！"
echo "📋 接下来请："
echo "1. 访问 https://render.com"
echo "2. 使用GitHub账号登录"
echo "3. 点击'New +' -> 'Blueprint'"
echo "4. 选择您的GitHub仓库"
echo "5. Render将自动检测render.yaml并部署服务"
echo ""
echo "🔗 部署完成后，您将获得："
echo "- 后端API: https://blog-backend.onrender.com"
echo "- 前端应用: https://blog-frontend.onrender.com"
echo "- 数据库: 自动创建的PostgreSQL数据库"
