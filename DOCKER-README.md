# 🐳 Docker 部署指南

这份文档会教你如何使用 Docker 来运行整个博客项目。

## 📖 什么是 Docker?

Docker 是一个容器化平台，可以把你的应用程序和所有依赖打包在一起。

**为什么要用 Docker?**
- ✅ **环境一致性**：不用担心"在我电脑上能跑"的问题
- ✅ **快速部署**：一条命令启动整个项目
- ✅ **隔离性好**：每个服务在独立的容器中运行
- ✅ **易于管理**：可以轻松启动、停止、重启服务

## 🛠️ 前置要求

### 1. 安装 Docker Desktop

- **Windows/Mac**: 下载并安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- **Linux**: 安装 Docker Engine 和 Docker Compose

安装完成后，打开命令行验证：

```bash
docker --version
docker-compose --version
```

### 2. 确保端口未被占用

项目会使用以下端口：
- `3000`: 前端服务
- `4567`: 后端服务
- `3307`: MySQL 数据库（映射到容器内的 3306）

## 🚀 快速启动

### 方法一：使用 Docker Compose（推荐）

在项目根目录 `E:\ai博客\blog\` 下运行：

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

**等待 2-3 分钟**，让所有服务完全启动，然后访问：
- 前端界面：http://localhost:3000
- 后端 API：http://localhost:4567

### 方法二：分步启动（学习用）

```bash
# 1. 先启动数据库
docker-compose up -d mysql

# 2. 等待数据库完全启动（约 30 秒）
docker-compose logs mysql

# 3. 启动后端
docker-compose up -d backend

# 4. 启动前端
docker-compose up -d frontend
```

## 📋 常用命令

### 查看运行状态
```bash
# 查看所有容器状态
docker-compose ps

# 查看特定服务的日志
docker-compose logs frontend
docker-compose logs backend
docker-compose logs mysql

# 实时查看日志
docker-compose logs -f backend
```

### 停止和重启
```bash
# 停止所有服务
docker-compose stop

# 启动所有服务
docker-compose start

# 重启特定服务
docker-compose restart backend

# 停止并删除容器（数据会保留）
docker-compose down

# 停止并删除容器和数据卷（彻底清理）
docker-compose down -v
```

### 重新构建
```bash
# 当你修改了代码后，需要重新构建镜像
docker-compose build

# 重新构建并启动
docker-compose up -d --build

# 只重新构建某个服务
docker-compose build backend
docker-compose up -d backend
```

## 🔍 项目结构说明

### Docker 相关文件

```
blog/
├── docker-compose.yml          # 主配置文件：定义所有服务
├── Dockerfile                  # 前端镜像构建文件
├── nginx.conf                  # Nginx 配置：前端服务器和 API 代理
├── .dockerignore              # 前端构建时忽略的文件
└── backend-spring/
    ├── Dockerfile              # 后端镜像构建文件
    ├── .dockerignore          # 后端构建时忽略的文件
    └── src/main/resources/
        └── application-docker.properties  # Docker 环境配置
```

### 核心概念解释

#### 1. **docker-compose.yml**
这是编排文件，定义了三个服务：
- `mysql`: MySQL 数据库容器
- `backend`: Spring Boot 后端容器
- `frontend`: Vue + Nginx 前端容器

#### 2. **Dockerfile（前端）**
- **构建阶段**：使用 Node.js 编译 Vue 项目
- **运行阶段**：使用 Nginx 提供静态文件服务

#### 3. **Dockerfile（后端）**
- **构建阶段**：使用 Maven 编译 Spring Boot 项目
- **运行阶段**：使用 JRE 运行打包好的 JAR 文件

#### 4. **nginx.conf**
配置 Nginx 做两件事：
- 提供前端静态文件
- 将 `/api/*` 请求代理到后端服务

## 🐛 常见问题

### 1. 端口被占用
**错误**: `Bind for 0.0.0.0:3000 failed: port is already allocated`

**解决**:
```bash
# 查看端口占用
netstat -ano | findstr :3000

# 修改 docker-compose.yml 中的端口映射
ports:
  - "8000:80"  # 将 3000 改为 8000
```

### 2. MySQL 连接失败
**错误**: `Communications link failure`

**解决**:
```bash
# 查看 MySQL 日志
docker-compose logs mysql

# 等待健康检查通过
docker-compose ps
# 状态显示 "healthy" 后再启动后端
```

### 3. 前端无法访问后端
**问题**: 前端页面打开了，但 API 请求失败

**检查**:
```bash
# 确保后端正在运行
docker-compose ps backend

# 查看后端日志
docker-compose logs backend

# 测试后端 API
curl http://localhost:4567/api/posts
```

### 4. 修改代码后没有生效
**原因**: Docker 使用的是构建时的代码快照

**解决**:
```bash
# 重新构建并启动
docker-compose up -d --build
```

### 5. 数据库数据丢失
**原因**: 使用了 `docker-compose down -v`

**避免**:
```bash
# 停止时不要加 -v 参数
docker-compose down

# 如果需要重置数据库
docker-compose down -v
docker-compose up -d
```

## 🎯 开发工作流

### 场景一：修改前端代码
```bash
# 1. 修改 src/ 下的 Vue 文件
# 2. 重新构建前端
docker-compose build frontend
# 3. 重启前端容器
docker-compose up -d frontend
# 4. 刷新浏览器查看效果
```

### 场景二：修改后端代码
```bash
# 1. 修改 backend-spring/src/ 下的 Java 文件
# 2. 重新构建后端
docker-compose build backend
# 3. 重启后端容器
docker-compose up -d backend
```

### 场景三：修改数据库结构
```bash
# 1. 修改 database-migration/init-database.sql
# 2. 重建数据库容器
docker-compose down mysql
docker volume rm blog_mysql-data
docker-compose up -d mysql
```

## 🔧 进阶使用

### 进入容器内部
```bash
# 进入后端容器
docker-compose exec backend sh

# 进入 MySQL 容器
docker-compose exec mysql bash

# 在 MySQL 容器中连接数据库
docker-compose exec mysql mysql -uroot -p990328 blogdb
```

### 查看资源使用
```bash
# 查看容器资源使用情况
docker stats

# 清理未使用的资源
docker system prune -a
```

### 导出和导入数据
```bash
# 导出数据库
docker-compose exec mysql mysqldump -uroot -p990328 blogdb > backup.sql

# 导入数据库
docker-compose exec -T mysql mysql -uroot -p990328 blogdb < backup.sql
```

## 📚 学习建议

1. **理解分层构建**: Dockerfile 使用多阶段构建减小镜像体积
2. **网络通信**: 容器之间通过服务名（如 `mysql`、`backend`）通信
3. **数据持久化**: 使用 volumes 保证数据不丢失
4. **健康检查**: 确保服务启动顺序正确

## 🆘 获取帮助

- Docker 官方文档: https://docs.docker.com/
- Docker Compose 文档: https://docs.docker.com/compose/

---

**提示**: 第一次构建可能需要 5-10 分钟，因为要下载所有依赖。后续构建会快很多！
