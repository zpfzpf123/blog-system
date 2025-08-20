#!/bin/bash

# AI智能博客系统 - 自动Git上传脚本
# 功能：检测文件变化，自动提交并推送到GitHub仓库

# 默认参数
COMMIT_MESSAGE=""
FORCE_PUSH=false
SHOW_HELP=false

# 颜色输出函数
print_color() {
    local color=$1
    local text=$2
    case $color in
        "red") echo -e "\033[31m$text\033[0m" ;;
        "green") echo -e "\033[32m$text\033[0m" ;;
        "yellow") echo -e "\033[33m$text\033[0m" ;;
        "blue") echo -e "\033[34m$text\033[0m" ;;
        "cyan") echo -e "\033[36m$text\033[0m" ;;
        *) echo "$text" ;;
    esac
}

# 显示帮助信息
show_help() {
    cat << EOF
🚀 AI智能博客系统 - 自动Git上传脚本

用法:
    ./auto-git-upload.sh [选项]

选项:
    -m, --message <消息>    指定提交信息（可选）
    -f, --force             强制推送（覆盖远程分支）
    -h, --help              显示此帮助信息

示例:
    ./auto-git-upload.sh
    ./auto-git-upload.sh -m "更新README文档"
    ./auto-git-upload.sh --force

注意:
    - 首次使用前请确保已配置Git用户信息和远程仓库
    - 脚本会自动检测所有文件变化并提交
    - 使用--force选项时要谨慎，会覆盖远程分支
EOF
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -m|--message)
            COMMIT_MESSAGE="$2"
            shift 2
            ;;
        -f|--force)
            FORCE_PUSH=true
            shift
            ;;
        -h|--help)
            SHOW_HELP=true
            shift
            ;;
        *)
            print_color "red" "❌ 未知选项: $1"
            show_help
            exit 1
            ;;
    esac
done

# 显示帮助信息
if [ "$SHOW_HELP" = true ]; then
    show_help
    exit 0
fi

# 检查Git是否安装
check_git() {
    if ! command -v git &> /dev/null; then
        print_color "red" "❌ 错误: Git未安装或不在PATH中"
        print_color "yellow" "请先安装Git: https://git-scm.com/"
        exit 1
    fi
}

# 检查是否为Git仓库
check_git_repo() {
    if [ ! -d ".git" ]; then
        print_color "red" "❌ 错误: 当前目录不是Git仓库"
        print_color "yellow" "请先初始化Git仓库: git init"
        exit 1
    fi
}

# 检查远程仓库配置
check_remote_repo() {
    if ! git remote get-url origin &> /dev/null; then
        print_color "red" "❌ 错误: 未配置远程仓库"
        print_color "yellow" "请先添加远程仓库: git remote add origin <仓库URL>"
        exit 1
    fi
}

# 获取Git状态
get_git_status() {
    git status --porcelain 2>/dev/null
}

# 获取当前分支
get_current_branch() {
    git branch --show-current 2>/dev/null
}

# 主函数
main() {
    print_color "cyan" "🚀 AI智能博客系统 - 自动Git上传脚本"
    print_color "cyan" "==============================================="
    
    # 检查Git是否安装
    check_git
    
    # 检查是否为Git仓库
    check_git_repo
    
    # 检查远程仓库配置
    check_remote_repo
    
    # 获取当前分支
    local current_branch=$(get_current_branch)
    if [ -z "$current_branch" ]; then
        print_color "red" "❌ 错误: 无法获取当前分支"
        exit 1
    fi
    
    print_color "green" "📍 当前分支: $current_branch"
    
    # 获取Git状态
    local git_status=$(get_git_status)
    if [ -z "$git_status" ]; then
        print_color "green" "✅ 没有文件变化，无需提交"
        return
    fi
    
    print_color "yellow" "📝 检测到以下文件变化:"
    echo "$git_status" | while IFS= read -r line; do
        local status="${line:0:2}"
        local filename="${line:3}"
        local status_icon=""
        
        case $status in
            "M") status_icon="📝" ;;  # Modified
            "A") status_icon="➕" ;;  # Added
            "D") status_icon="🗑️" ;;  # Deleted
            "R") status_icon="🔄" ;;  # Renamed
            "C") status_icon="📋" ;;  # Copied
            *) status_icon="❓" ;;
        esac
        
        echo "  $status_icon $filename"
    done
    
    # 添加所有文件到暂存区
    print_color "yellow" "📦 添加文件到暂存区..."
    if ! git add .; then
        print_color "red" "❌ 添加文件失败"
        exit 1
    fi
    print_color "green" "✅ 文件已添加到暂存区"
    
    # 生成提交信息
    if [ -z "$COMMIT_MESSAGE" ]; then
        local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        COMMIT_MESSAGE="自动更新 - $timestamp"
    fi
    
    print_color "yellow" "💬 提交信息: $COMMIT_MESSAGE"
    
    # 提交更改
    print_color "yellow" "📝 提交更改..."
    if ! git commit -m "$COMMIT_MESSAGE"; then
        print_color "red" "❌ 提交失败"
        exit 1
    fi
    print_color "green" "✅ 更改已提交"
    
    # 获取最新提交信息
    local last_commit=$(git log -1 --oneline 2>/dev/null)
    if [ -n "$last_commit" ]; then
        print_color "green" "📋 最新提交: $last_commit"
    else
        print_color "yellow" "⚠️ 无法获取最新提交信息"
    fi
    
    # 推送到远程仓库
    print_color "yellow" "🚀 推送到远程仓库..."
    if [ "$FORCE_PUSH" = true ]; then
        print_color "red" "⚠️ 使用强制推送模式"
        if ! git push origin "$current_branch" --force; then
            print_color "red" "❌ 推送失败"
            print_color "yellow" "💡 提示: 请检查网络连接和权限"
            exit 1
        fi
    else
        if ! git push origin "$current_branch"; then
            print_color "red" "❌ 推送失败"
            print_color "yellow" "💡 提示: 如果远程有更新，请先执行 git pull"
            exit 1
        fi
    fi
    print_color "green" "✅ 成功推送到远程仓库"
    
    # 显示推送结果
    local remote_url=$(git remote get-url origin 2>/dev/null)
    if [ -n "$remote_url" ]; then
        remote_url=${remote_url%.git}
        print_color "cyan" "🌐 远程仓库: $remote_url"
        print_color "cyan" "🌿 分支: $current_branch"
    else
        print_color "yellow" "⚠️ 无法获取远程仓库信息"
    fi
    
    print_color "green" "🎉 自动上传完成！"
    print_color "cyan" "==============================================="
}

# 执行主函数
main
