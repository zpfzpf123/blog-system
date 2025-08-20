# AI智能博客系统 - 自动Git上传脚本
# 功能：检测文件变化，自动提交并推送到GitHub仓库

param(
    [string]$CommitMessage = "",
    [switch]$Force = $false,
    [switch]$Help = $false
)

# 显示帮助信息
if ($Help) {
    Write-Host @"
AI智能博客系统 - 自动Git上传脚本

用法:
    .\auto-git-upload.ps1 [选项]

选项:
    -CommitMessage <消息>    指定提交信息（可选）
    -Force                   强制推送（覆盖远程分支）
    -Help                    显示此帮助信息

示例:
    .\auto-git-upload.ps1
    .\auto-git-upload.ps1 -CommitMessage "更新README文档"
    .\auto-git-upload.ps1 -Force

注意:
    - 首次使用前请确保已配置Git用户信息和远程仓库
    - 脚本会自动检测所有文件变化并提交
    - 使用-Force选项时要谨慎，会覆盖远程分支
"@
    exit 0
}

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# 检查Git是否安装
function Test-GitInstalled {
    try {
        $null = git --version
        return $true
    }
    catch {
        return $false
    }
}

# 检查是否为Git仓库
function Test-GitRepository {
    return Test-Path ".git"
}

# 检查远程仓库配置
function Test-RemoteRepository {
    try {
        $remote = git remote get-url origin 2>$null
        return $remote -ne $null
    }
    catch {
        return $false
    }
}

# 获取Git状态
function Get-GitStatus {
    try {
        $status = git status --porcelain
        return $status
    }
    catch {
        return $null
    }
}

# 获取当前分支
function Get-CurrentBranch {
    try {
        $branch = git branch --show-current
        return $branch
    }
    catch {
        return $null
    }
}

# 主函数
function Main {
    Write-ColorOutput "🚀 AI智能博客系统 - 自动Git上传脚本" "Cyan"
    Write-ColorOutput "===============================================" "Cyan"
    
    # 检查Git是否安装
    if (-not (Test-GitInstalled)) {
        Write-ColorOutput "❌ 错误: Git未安装或不在PATH中" "Red"
        Write-ColorOutput "请先安装Git: https://git-scm.com/" "Yellow"
        exit 1
    }
    
    # 检查是否为Git仓库
    if (-not (Test-GitRepository)) {
        Write-ColorOutput "❌ 错误: 当前目录不是Git仓库" "Red"
        Write-ColorOutput "请先初始化Git仓库: git init" "Yellow"
        exit 1
    }
    
    # 检查远程仓库配置
    if (-not (Test-RemoteRepository)) {
        Write-ColorOutput "❌ 错误: 未配置远程仓库" "Red"
        Write-ColorOutput "请先添加远程仓库: git remote add origin <仓库URL>" "Yellow"
        exit 1
    }
    
    # 获取当前分支
    $currentBranch = Get-CurrentBranch
    if (-not $currentBranch) {
        Write-ColorOutput "❌ 错误: 无法获取当前分支" "Red"
        exit 1
    }
    
    Write-ColorOutput "📍 当前分支: $currentBranch" "Green"
    
    # 获取Git状态
    $gitStatus = Get-GitStatus
    if (-not $gitStatus) {
        Write-ColorOutput "❌ 错误: 无法获取Git状态" "Red"
        exit 1
    }
    
    # 检查是否有文件变化
    if ($gitStatus.Count -eq 0) {
        Write-ColorOutput "✅ 没有文件变化，无需提交" "Green"
        return
    }
    
    Write-ColorOutput "📝 检测到以下文件变化:" "Yellow"
    foreach ($file in $gitStatus) {
        $status = $file.Substring(0, 2).Trim()
        $filename = $file.Substring(3)
        $statusIcon = switch ($status) {
            "M" { "📝" }  # Modified
            "A" { "➕" }  # Added
            "D" { "🗑️" }  # Deleted
            "R" { "🔄" }  # Renamed
            "C" { "📋" }  # Copied
            default { "❓" }
        }
        Write-ColorOutput "  $statusIcon $filename" "White"
    }
    
    # 添加所有文件到暂存区
    Write-ColorOutput "📦 添加文件到暂存区..." "Yellow"
    try {
        git add .
        Write-ColorOutput "✅ 文件已添加到暂存区" "Green"
    }
    catch {
        Write-ColorOutput "❌ 添加文件失败: $($_.Exception.Message)" "Red"
        exit 1
    }
    
    # 生成提交信息
    if ([string]::IsNullOrEmpty($CommitMessage)) {
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        $CommitMessage = "自动更新 - $timestamp"
    }
    
    Write-ColorOutput "💬 提交信息: $CommitMessage" "Yellow"
    
    # 提交更改
    Write-ColorOutput "📝 提交更改..." "Yellow"
    try {
        git commit -m $CommitMessage
        Write-ColorOutput "✅ 更改已提交" "Green"
    }
    catch {
        Write-ColorOutput "❌ 提交失败: $($_.Exception.Message)" "Red"
        exit 1
    }
    
    # 获取最新提交信息
    try {
        $lastCommit = git log -1 --oneline
        Write-ColorOutput "📋 最新提交: $lastCommit" "Green"
    }
    catch {
        Write-ColorOutput "⚠️ 无法获取最新提交信息" "Yellow"
    }
    
    # 推送到远程仓库
    Write-ColorOutput "🚀 推送到远程仓库..." "Yellow"
    try {
        if ($Force) {
            Write-ColorOutput "⚠️ 使用强制推送模式" "Red"
            git push origin $currentBranch --force
        } else {
            git push origin $currentBranch
        }
        Write-ColorOutput "✅ 成功推送到远程仓库" "Green"
    }
    catch {
        Write-ColorOutput "❌ 推送失败: $($_.Exception.Message)" "Red"
        Write-ColorOutput "💡 提示: 如果远程有更新，请先执行 git pull" "Yellow"
        exit 1
    }
    
    # 显示推送结果
    try {
        $remoteUrl = git remote get-url origin
        $remoteUrl = $remoteUrl -replace "\.git$", ""
        Write-ColorOutput "🌐 远程仓库: $remoteUrl" "Cyan"
        Write-ColorOutput "🌿 分支: $currentBranch" "Cyan"
    }
    catch {
        Write-ColorOutput "⚠️ 无法获取远程仓库信息" "Yellow"
    }
    
    Write-ColorOutput "🎉 自动上传完成！" "Green"
    Write-ColorOutput "===============================================" "Cyan"
}

# 执行主函数
try {
    Main
}
catch {
    Write-ColorOutput "❌ 脚本执行出错: $($_.Exception.Message)" "Red"
    Write-ColorOutput "💡 请检查Git配置和网络连接" "Yellow"
    exit 1
}
