# 快速部署指南

## 方案一：Railway部署（推荐）

### 1. 准备工作

1. 注册GitHub账号
2. 将代码推送到GitHub仓库
3. 注册Railway账号：<https://railway.app>

### 2. 部署步骤

#### 2.1 部署后端

1. 登录Railway
2. 点击"New Project" -> "Deploy from GitHub repo"
3. 选择您的GitHub仓库
4. 选择`backend-spring`目录
5. 配置环境变量：

   ```
   SPRING_PROFILES_ACTIVE=prod
   SERVER_PORT=4567
   ```

6. 点击"Deploy Now"

#### 2.2 部署数据库

1. 在Railway项目中点击"New"
2. 选择"Database" -> "MySQL"
3. 等待数据库创建完成
4. 复制数据库连接信息

#### 2.3 配置数据库连接

1. 在Railway项目中找到后端服务
2. 点击"Variables"标签
3. 添加以下环境变量：

   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://[数据库主机]:[端口]/[数据库名]
   SPRING_DATASOURCE_USERNAME=[用户名]
   SPRING_DATASOURCE_PASSWORD=[密码]
   ```

#### 2.4 部署前端

1. 在Railway项目中点击"New"
2. 选择"Deploy from GitHub repo"
3. 选择您的GitHub仓库
4. 选择根目录（前端代码）
5. 配置环境变量：

   ```
   VITE_API_BASE_URL=https://[后端服务域名]
   ```

6. 点击"Deploy Now"

### 3. 验证部署

1. 访问前端域名
2. 测试各项功能
3. 检查后端API是否正常

## 方案二：Render部署

### 1. 准备工作

1. 注册Render账号：<https://render.com>
2. 确保代码已推送到GitHub

### 2. 部署步骤

#### 2.1 使用Blueprint部署

1. 登录Render
2. 点击"New +" -> "Blueprint"
3. 连接GitHub仓库
4. Render会自动检测`render.yaml`文件
5. 点击"Apply"

#### 2.2 手动部署

如果Blueprint部署失败，可以手动部署：

1. **部署后端**：
   - 点击"New +" -> "Web Service"
   - 连接GitHub仓库
   - 选择`backend-spring`目录
   - 构建命令：`mvn clean package -DskipTests`
   - 启动命令：`java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`

2. **部署数据库**：
   - 点击"New +" -> "PostgreSQL"
   - 创建数据库实例

3. **部署前端**：
   - 点击"New +" -> "Static Site"
   - 连接GitHub仓库
   - 构建命令：`npm ci && npm run build:prod`
   - 发布目录：`dist`

### 3. 配置环境变量

在Render控制台中为每个服务配置相应的环境变量。

## 方案三：Vercel + Railway分离部署

### 1. 部署后端到Railway

按照方案一的步骤部署后端和数据库。

### 2. 部署前端到Vercel

1. 注册Vercel账号：<https://vercel.com>
2. 导入GitHub仓库
3. 配置构建设置：
   - Framework Preset: Vite
   - Build Command: `npm run build:prod`
   - Output Directory: `dist`
4. 配置环境变量：

   ```
   VITE_API_BASE_URL=https://[Railway后端域名]
   ```

5. 点击"Deploy"

## 常见问题解决

### 1. 构建失败

- 检查Node.js和Java版本
- 确认依赖包是否正确安装
- 查看构建日志

### 2. 数据库连接失败

- 检查数据库连接字符串
- 确认数据库服务是否启动
- 验证用户名和密码

### 3. CORS错误

- 检查后端CORS配置
- 确认前端API地址是否正确
- 验证域名是否在允许列表中

### 4. 静态资源404

- 检查前端构建输出
- 确认静态文件路径
- 验证服务器配置

## 部署后维护

### 1. 监控服务状态

- 定期检查服务健康状态
- 监控资源使用情况
- 查看错误日志

### 2. 更新部署

- 推送代码到GitHub
- 平台会自动重新部署
- 验证新版本功能

### 3. 备份数据

- 定期备份数据库
- 保存重要配置文件
- 记录部署配置

## 成本控制

### 免费额度

- Railway: 每月500小时
- Render: 每月750小时
- Vercel: 无限制

### 优化建议

- 合理使用资源
- 监控使用情况
- 及时清理无用服务
