# 博客服务自动启动脚本使用说明

## 概述

本项目提供了多个脚本来自动启动和停止博客的前端和后端服务，支持Windows系统开机自动启动。

## 脚本文件说明

### 1. 启动脚本

#### `start-blog-services.bat` (批处理版本)
- **功能**: 自动启动Spring Boot后端和Vue.js前端服务
- **特点**: 兼容性好，适合所有Windows版本
- **使用**: 双击运行或在命令行中执行

#### `start-blog-services.ps1` (PowerShell版本)
- **功能**: 使用PowerShell后台任务启动服务
- **特点**: 更现代化，支持任务管理
- **使用**: 需要先设置PowerShell执行策略

### 2. 停止脚本

#### `stop-blog-services.bat`
- **功能**: 停止所有博客相关服务
- **特点**: 自动查找并终止相关进程

### 3. 安装脚本

#### `install-startup-script.bat`
- **功能**: 将启动脚本安装到Windows启动文件夹
- **特点**: 自动化安装过程

## 安装步骤

### 方法一：使用安装脚本（推荐）

1. 以管理员身份运行 `install-startup-script.bat`
2. 脚本会自动将启动脚本复制到启动文件夹
3. 系统重启后会自动启动博客服务

### 方法二：手动安装

1. 打开启动文件夹：
   ```
   Win + R → 输入: shell:startup
   ```

2. 将 `start-blog-services.bat` 复制到启动文件夹

3. 如需使用PowerShell版本，先运行：
   ```
   setup-powershell-policy.bat
   ```

## 使用方法

### 手动启动服务

```bash
# 批处理版本
start-blog-services.bat

# PowerShell版本
powershell -ExecutionPolicy Bypass -File start-blog-services.ps1
```

### 手动停止服务

```bash
stop-blog-services.bat
```

### 查看服务状态

```bash
# 查看PowerShell后台任务
powershell -Command "Get-Job | Where-Object { $_.Name -like '博客*服务' }"

# 查看进程
tasklist | findstr "java"
tasklist | findstr "node"
```

## 服务端口

- **后端服务**: http://localhost:4567
- **前端服务**: http://localhost:5173
- **管理后台**: http://localhost:5173/admin

## 环境要求

确保系统已安装以下软件：

- **Java 8+**: 运行Spring Boot后端
- **Node.js 14+**: 运行Vue.js前端
- **Maven 3.6+**: 构建Java项目
- **npm**: Node.js包管理器

## 故障排除

### 1. 服务启动失败

**问题**: 端口被占用
**解决**: 
```bash
# 查看端口占用
netstat -ano | findstr :4567
netstat -ano | findstr :5173

# 终止占用进程
taskkill /PID <进程ID> /F
```

### 2. PowerShell执行策略错误

**问题**: 无法执行PowerShell脚本
**解决**: 
```bash
# 设置执行策略
powershell -Command "Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force"
```

### 3. 环境变量问题

**问题**: 找不到java、node、mvn等命令
**解决**: 
- 检查环境变量PATH设置
- 重新安装相关软件
- 重启命令行窗口

### 4. 项目路径错误

**问题**: 脚本找不到项目文件
**解决**: 
- 修改脚本中的 `PROJECT_PATH` 变量
- 确保路径使用正确的分隔符

## 高级配置

### 修改端口

编辑启动脚本，修改以下变量：
```bash
# 批处理版本
set BACKEND_PORT=4567
set FRONTEND_PORT=5173

# PowerShell版本
param(
    [int]$BackendPort = 4567,
    [int]$FrontendPort = 5173
)
```

### 自定义项目路径

```bash
# 批处理版本
set PROJECT_PATH=你的项目路径

# PowerShell版本
param(
    [string]$ProjectPath = "你的项目路径"
)
```

## 安全注意事项

1. **执行策略**: PowerShell脚本需要适当的执行策略
2. **权限**: 某些操作可能需要管理员权限
3. **防火墙**: 确保防火墙允许相关端口通信
4. **杀毒软件**: 某些杀毒软件可能误报脚本文件

## 技术支持

如果遇到问题，请检查：

1. 系统日志
2. 服务启动日志
3. 网络连接状态
4. 防火墙设置

## 更新日志

- **v1.0**: 初始版本，支持基本的启动和停止功能
- **v1.1**: 添加PowerShell版本和安装脚本
- **v1.2**: 改进错误处理和用户界面
