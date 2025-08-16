#!/bin/bash

# 博客系统一键部署脚本
# 支持Railway、Render、Vercel等平台

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查必要的工具
check_requirements() {
    print_step "检查部署环境..."
    
    if ! command -v git &> /dev/null; then
        print_error "Git未安装，请先安装Git"
        exit 1
    fi
    
    if ! command -v node &> /dev/null; then
        print_error "Node.js未安装，请先安装Node.js"
        exit 1
    fi
    
    if ! command -v java &> /dev/null; then
        print_error "Java未安装，请先安装Java 8或更高版本"
        exit 1
    fi
    
    print_message "环境检查通过"
}

# 显示菜单
show_menu() {
    echo ""
    echo "=========================================="
    echo "          博客系统部署工具"
    echo "=========================================="
    echo "1. Railway部署 (推荐)"
    echo "2. Render部署"
    echo "3. Vercel + Railway分离部署"
    echo "4. 本地构建测试"
    echo "5. 查看部署状态"
    echo "6. 退出"
    echo "=========================================="
    echo ""
}

# Railway部署
deploy_railway() {
    print_step "开始Railway部署..."
    
    # 检查Railway CLI
    if ! command -v railway &> /dev/null; then
        print_warning "Railway CLI未安装，正在安装..."
        npm install -g @railway/cli
    fi
    
    # 登录Railway
    print_message "请登录Railway..."
    railway login
    
    # 创建项目
    print_message "创建Railway项目..."
    railway init
    
    # 部署后端
    print_message "部署后端服务..."
    cd backend-spring
    railway up
    
    # 等待后端启动
    print_message "等待后端服务启动..."
    sleep 30
    
    # 获取后端URL
    BACKEND_URL=$(railway status --json | jq -r '.services[] | select(.name == "backend") | .url')
    print_message "后端服务URL: $BACKEND_URL"
    
    # 部署前端
    print_message "部署前端应用..."
    cd ..
    echo "VITE_API_BASE_URL=$BACKEND_URL" > .env.production
    npm run build:prod
    railway up
    
    print_message "Railway部署完成！"
}

# Render部署
deploy_render() {
    print_step "开始Render部署..."
    
    # 检查render.yaml是否存在
    if [ ! -f "render.yaml" ]; then
        print_error "render.yaml文件不存在"
        exit 1
    fi
    
    # 提交代码
    print_message "提交代码到GitHub..."
    git add .
    git commit -m "Add Render deployment configuration"
    git push
    
    print_message "Render部署配置完成！"
    print_message "请访问 https://render.com 完成部署："
    print_message "1. 登录Render账号"
    print_message "2. 点击'New +' -> 'Blueprint'"
    print_message "3. 选择您的GitHub仓库"
    print_message "4. 点击'Apply'开始部署"
}

# Vercel + Railway分离部署
deploy_vercel_railway() {
    print_step "开始Vercel + Railway分离部署..."
    
    # 先部署Railway后端
    print_message "部署后端到Railway..."
    deploy_railway
    
    # 获取后端URL
    BACKEND_URL=$(railway status --json | jq -r '.services[] | select(.name == "backend") | .url')
    
    print_message "后端部署完成，URL: $BACKEND_URL"
    print_message "现在请部署前端到Vercel："
    print_message "1. 访问 https://vercel.com"
    print_message "2. 导入GitHub仓库"
    print_message "3. 配置环境变量: VITE_API_BASE_URL=$BACKEND_URL"
    print_message "4. 点击'Deploy'"
}

# 本地构建测试
local_build_test() {
    print_step "开始本地构建测试..."
    
    # 构建后端
    print_message "构建后端..."
    cd backend-spring
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_message "后端构建成功"
    else
        print_error "后端构建失败"
        exit 1
    fi
    
    # 构建前端
    print_message "构建前端..."
    cd ..
    npm run build:prod
    
    if [ $? -eq 0 ]; then
        print_message "前端构建成功"
    else
        print_error "前端构建失败"
        exit 1
    fi
    
    print_message "本地构建测试完成！"
}

# 查看部署状态
check_deployment_status() {
    print_step "检查部署状态..."
    
    # 检查Railway状态
    if command -v railway &> /dev/null; then
        print_message "Railway服务状态："
        railway status
    fi
    
    # 检查Git状态
    print_message "Git仓库状态："
    git status
    
    # 检查本地构建
    if [ -f "backend-spring/target/backend-0.0.1-SNAPSHOT.jar" ]; then
        print_message "后端JAR包已构建"
    else
        print_warning "后端JAR包未构建"
    fi
    
    if [ -d "dist" ]; then
        print_message "前端构建目录存在"
    else
        print_warning "前端构建目录不存在"
    fi
}

# 主函数
main() {
    check_requirements
    
    while true; do
        show_menu
        read -p "请选择部署方案 (1-6): " choice
        
        case $choice in
            1)
                deploy_railway
                break
                ;;
            2)
                deploy_render
                break
                ;;
            3)
                deploy_vercel_railway
                break
                ;;
            4)
                local_build_test
                break
                ;;
            5)
                check_deployment_status
                ;;
            6)
                print_message "退出部署工具"
                exit 0
                ;;
            *)
                print_error "无效选择，请重新输入"
                ;;
        esac
    done
}

# 运行主函数
main "$@"
