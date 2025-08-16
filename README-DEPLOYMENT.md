# 博客系统云服务器部署说明

## 项目概述

这是一个基于Vue.js + Spring Boot + MySQL的现代化博客系统，支持文章管理、分类标签、网站合集等功能。

## 技术栈

- **前端**: Vue.js 3 + TypeScript + Element Plus + Vite
- **后端**: Spring Boot 2.7 + Java 8 + JPA
- **数据库**: MySQL 8.0 / PostgreSQL
- **部署**: Railway / Render / Vercel

## 快速部署

### 方案一：Railway一键部署（推荐）

1. **注册Railway账号**
   - 访问 https://railway.app
   - 使用GitHub账号注册

2. **部署后端**
   ```bash
   # 克隆项目到本地
   git clone <your-repo-url>
   cd blog
   
   # 运行部署脚本
   chmod +x deploy-scripts/deploy-to-railway.sh
   ./deploy-scripts/deploy-to-railway.sh
   ```

3. **手动部署步骤**
   - 登录Railway控制台
   - 创建新项目
   - 连接GitHub仓库
   - 选择`backend-spring`目录部署后端
   - 添加MySQL数据库服务
   - 配置环境变量
   - 部署前端应用

### 方案二：Render部署

1. **注册Render账号**
   - 访问 https://render.com
   - 使用GitHub账号注册

2. **使用Blueprint部署**
   ```bash
   # 运行Render部署脚本
   chmod +x deploy-scripts/deploy-to-render.sh
   ./deploy-scripts/deploy-to-render.sh
   ```

3. **手动部署**
   - 登录Render控制台
   - 点击"New +" -> "Blueprint"
   - 选择GitHub仓库
   - Render会自动检测`render.yaml`配置
   - 点击"Apply"开始部署

## 环境变量配置

### 后端环境变量

```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:mysql://host:port/database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password

# 应用配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=4567

# CORS配置
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

### 前端环境变量

```bash
# API配置
VITE_API_BASE_URL=https://your-backend-domain.com

# 应用信息
VITE_APP_TITLE=Blog System
VITE_APP_DESCRIPTION=A modern blog system
```

## 数据库配置

### MySQL配置

```sql
-- 创建数据库
CREATE DATABASE blogdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 运行初始化脚本
source database-migration/init-database.sql;
```

### PostgreSQL配置

```sql
-- 创建数据库
CREATE DATABASE blogdb;

-- 运行初始化脚本
\i database-migration/init-database.sql;
```

## 本地开发

### 启动后端

```bash
cd backend-spring
mvn spring-boot:run
```

### 启动前端

```bash
npm install
npm run dev
```

### 启动数据库

```bash
# MySQL
mysql -u root -p

# PostgreSQL
psql -U postgres
```

## 部署架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用      │    │   后端API       │    │   数据库        │
│   (Vue.js)      │◄──►│   (Spring Boot) │◄──►│   (MySQL/PostgreSQL)
│                 │    │                 │    │                 │
│ - 静态文件服务   │    │ - RESTful API   │    │ - 数据存储      │
│ - 用户界面      │    │ - 业务逻辑      │    │ - 事务管理      │
│ - 路由管理      │    │ - 数据访问      │    │ - 索引优化      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 功能特性

### 文章管理
- 文章的增删改查
- Markdown编辑器
- 分类和标签管理
- 文章状态管理

### 网站合集
- 网站收藏管理
- 分类组织
- 状态跟踪
- 批量操作

### AI功能
- AI问答
- 智能推荐
- 内容生成
- 自动分类

### 系统管理
- 用户管理
- 权限控制
- 系统监控
- 数据备份

## 性能优化

### 前端优化
- 代码分割
- 懒加载
- 缓存策略
- 压缩优化

### 后端优化
- 连接池配置
- 缓存机制
- 索引优化
- 分页查询

### 数据库优化
- 查询优化
- 索引设计
- 分区策略
- 备份恢复

## 安全配置

### 应用安全
- HTTPS强制
- CORS配置
- 输入验证
- SQL注入防护

### 数据安全
- 数据加密
- 访问控制
- 审计日志
- 备份策略

## 监控维护

### 健康检查
- 应用状态监控
- 数据库连接检查
- API响应时间
- 错误率统计

### 日志管理
- 应用日志
- 访问日志
- 错误日志
- 性能日志

### 备份策略
- 数据库备份
- 文件备份
- 配置备份
- 版本管理

## 故障排除

### 常见问题

1. **构建失败**
   - 检查Node.js和Java版本
   - 确认依赖包安装
   - 查看构建日志

2. **数据库连接失败**
   - 检查连接字符串
   - 确认数据库服务状态
   - 验证用户权限

3. **CORS错误**
   - 检查后端CORS配置
   - 确认前端API地址
   - 验证域名配置

4. **静态资源404**
   - 检查构建输出
   - 确认文件路径
   - 验证服务器配置

### 调试方法

1. **查看日志**
   ```bash
   # Railway
   railway logs
   
   # Render
   render logs
   ```

2. **检查状态**
   ```bash
   # 健康检查
   curl https://your-backend-domain.com/api/health
   ```

3. **数据库连接**
   ```bash
   # 测试连接
   mysql -h host -u user -p database
   ```

## 更新部署

### 自动部署
- 推送代码到GitHub
- 平台自动触发部署
- 验证新版本功能

### 手动部署
```bash
# 重新部署
railway up
# 或
render deploy
```

### 回滚策略
- 保留历史版本
- 快速回滚机制
- 数据备份恢复

## 成本控制

### 免费额度
- Railway: 每月500小时
- Render: 每月750小时
- Vercel: 无限制

### 优化建议
- 合理使用资源
- 监控使用情况
- 及时清理服务
- 选择合适方案

## 技术支持

### 文档资源
- [Vue.js官方文档](https://vuejs.org/)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Railway文档](https://docs.railway.app/)
- [Render文档](https://render.com/docs)

### 社区支持
- GitHub Issues
- Stack Overflow
- 技术论坛
- 开发者社区

## 许可证

本项目采用MIT许可证，详见LICENSE文件。
