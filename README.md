# AI智能博客系统 🚀

[![Vue](https://img.shields.io/badge/Vue-3.5.18-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7.14-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8.0-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

> 🌟 **项目地址**: [https://github.com/your-username/ai-blog-system](https://github.com/your-username/ai-blog-system)  
> 📖 **在线演示**: [https://your-demo-url.com](https://your-demo-url.com)  
> 🚀 **快速开始**: [部署指南](#部署)  
> 📚 **详细文档**: [项目文档](https://your-docs-url.com)

一个基于 Vue 3 + Spring Boot 的全栈博客应用，集成AI写作、智能推荐等先进功能。

## 📸 项目截图

> _项目截图展示区域 - 包含主要功能界面截图_

## ✨ 功能特性

- 🤖 **AI写作助手**: 智能生成文章内容、标题和摘要
- 🧠 **智能推荐系统**: 基于AI的内容推荐和外部资源推荐
- 📝 **文章管理**: 创建、编辑、删除、查看文章
- 🏷️ **分类管理**: 创建、编辑、删除文章分类
- 🏷️ **标签管理**: 创建、编辑、删除文章标签
- 📱 **响应式设计**: 适配不同屏幕尺寸
- 📖 **Markdown支持**: 使用Markdown语法编写文章
- 🔍 **智能搜索**: 根据标题和内容搜索文章
- 🌐 **外部资源推荐**: 根据文章内容推荐网络资源
- 📊 **数据统计**: 文章访问量、分类统计等
- 🎨 **现代化UI**: 基于Element Plus的美观界面
- 🔒 **用户认证**: 安全的用户登录和权限管理
- 📱 **移动端优化**: 完美的移动端体验

## 🛠️ 技术栈

### 前端技术

- **Vue 3** - 渐进式JavaScript框架
- **TypeScript** - 类型安全的JavaScript超集
- **Element Plus** - 基于Vue 3的组件库
- **Vite** - 下一代前端构建工具
- **Pinia** - Vue 3状态管理库
- **Vue Router** - Vue.js官方路由管理器

### 后端技术

- **Spring Boot 2.7.14** - Java企业级应用框架
- **Java 8** - 企业级编程语言
- **Spring Data JPA** - 数据访问层框架
- **PostgreSQL** - 强大的开源关系型数据库
- **Jsoup** - Java HTML解析器

### AI集成

- **OpenAI API** - 先进的AI语言模型
- **自定义AI模型** - 针对博客场景优化
- **智能内容分析** - 自动标签和分类推荐

### 部署和运维

- **Docker** - 容器化部署
- **Railway** - 云平台部署
- **Render** - 静态网站托管
- **GitHub Actions** - 自动化CI/CD

## 🏗️ 项目结构

```
blog/
├── 📁 backend-spring/          # Spring Boot 后端
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/com/blog/
│   │   │   │   ├── 📁 controller/   # 控制器层
│   │   │   │   ├── 📁 dto/          # 数据传输对象
│   │   │   │   ├── 📁 entity/       # 实体类
│   │   │   │   ├── 📁 repository/   # 数据访问层
│   │   │   │   └── 📁 service/      # 业务逻辑层
│   │   │   └── 📁 resources/
│   │   └── 📁 test/
│   ├── 📄 pom.xml
│   └── 📄 README-网站合集后端系统.md
├── 📁 src/                     # Vue 前端
│   ├── 📁 assets/              # 静态资源
│   ├── 📁 components/          # 组件
│   ├── 📁 router/              # 路由配置
│   ├── 📁 stores/              # 状态管理
│   ├── 📁 types/               # 类型定义
│   ├── 📁 utils/               # 工具类
│   ├── 📁 views/               # 页面组件
│   ├── 📄 App.vue              # 根组件
│   └── 📄 main.ts              # 入口文件
├── 📁 public/                  # 公共资源
│   ├── 📄 favicon.ico          # 网站图标
│   └── 📄 logo.svg             # SVG图标
├── 📁 python-scraper/          # Python爬虫工具
├── 📄 package.json             # 前端依赖配置
├── 📄 project-info.json        # 项目信息配置
├── 📄 badges.md                # 项目徽章
└── 📄 README.md                # 项目说明文档
```

## 🚀 快速开始

### 环境要求

- **Node.js** >= 20.19.0 或 >= 22.12.0
- **npm** >= 8
- **Java** 8 或更高版本
- **Maven** >= 3.6
- **PostgreSQL** 12 或更高版本

### 安装依赖

```bash
# 克隆项目
git clone https://github.com/your-username/ai-blog-system.git
cd ai-blog-system

# 安装前端依赖
npm install

# 后端依赖通过 Maven 管理，无需手动安装
```

### 开发环境配置

#### 1. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE ai_blog_system;

-- 创建用户（可选）
CREATE USER ai_blog_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ai_blog_system TO ai_blog_user;
```

#### 2. 后端配置

```bash
# 进入后端目录
cd backend-spring

# 配置数据库连接
# 编辑 src/main/resources/application.properties
```

```properties
# 数据库配置
spring.datasource.url=jdbc:postgresql://localhost:5432/ai_blog_system
spring.datasource.username=ai_blog_user
spring.datasource.password=your_password

# AI API配置
ai.openai.api-key=your_openai_api_key
ai.openai.model=gpt-3.5-turbo
```

#### 3. 启动服务

```bash
# 启动后端服务
cd backend-spring
mvn spring-boot:run

# 启动前端开发服务器（新终端）
npm run dev
```

### 生产环境部署

#### 使用Docker部署

```bash
# 构建镜像
docker build -t ai-blog-system .

# 运行容器
docker run -d -p 8080:8080 --name ai-blog ai-blog-system
```

#### 使用Railway部署

```bash
# 安装Railway CLI
npm install -g @railway/cli

# 登录Railway
railway login

# 部署项目
railway up
```

#### 使用Render部署

```bash
# 连接GitHub仓库
# 在Render控制台中选择GitHub仓库

# 配置构建命令
npm run build

# 配置启动命令
npm start
```

## 📚 功能详解

### 1. AI写作助手 🤖

AI写作助手是系统的核心功能之一，提供以下能力：

- **智能标题生成**: 根据文章内容自动生成吸引人的标题
- **内容摘要**: 自动提取文章关键信息生成摘要
- **标签推荐**: 基于内容分析推荐相关标签
- **分类建议**: 智能分析文章主题并推荐分类
- **内容优化**: 提供写作建议和语法检查

### 2. 智能推荐系统 🧠

智能推荐系统通过多维度分析提供精准推荐：

#### 内部推荐

- 基于相同标签的文章推荐
- 基于用户阅读历史的个性化推荐
- 基于文章相似度的内容推荐

#### 外部推荐

- 网络资源智能搜索
- 相关文章和资料推荐
- 实时内容更新和同步

### 3. 文章管理系统 📝

完整的文章生命周期管理：

- **创建**: 支持Markdown编辑器和富文本编辑器
- **编辑**: 实时预览和版本控制
- **发布**: 定时发布和草稿管理
- **归档**: 自动归档和备份
- **统计**: 访问量、阅读时长等数据分析

### 4. 分类和标签系统 🏷️

灵活的层级分类和标签管理：

- **多级分类**: 支持无限层级的分类结构
- **智能标签**: 自动标签推荐和去重
- **批量操作**: 支持批量分类和标签管理
- **统计分析**: 分类和标签使用情况统计

## 🔌 API 接口

### 认证接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `POST /api/auth/refresh` - 刷新令牌

### 分类相关

- `GET /api/categories` - 获取所有分类
- `POST /api/categories` - 创建分类
- `PUT /api/categories/{id}` - 更新分类
- `DELETE /api/categories/{id}` - 删除分类
- `GET /api/categories/{id}/posts` - 获取分类下的文章

### 标签相关

- `GET /api/tags` - 获取所有标签
- `POST /api/tags` - 创建标签
- `PUT /api/tags/{id}` - 更新标签
- `DELETE /api/tags/{id}` - 删除标签
- `GET /api/tags/{id}/posts` - 获取标签下的文章

### 文章相关

- `GET /api/posts` - 获取文章列表(支持分页、筛选)
- `GET /api/posts/{id}` - 获取文章详情
- `POST /api/posts` - 创建文章
- `PUT /api/posts/{id}` - 更新文章
- `DELETE /api/posts/{id}` - 删除文章
- `GET /api/posts/search` - 搜索文章

### 推荐相关

- `GET /api/recommendations/external` - 获取外部推荐内容
- `GET /api/recommendations/external/smart` - 获取智能外部推荐内容
- `GET /api/recommendations/external/preview` - 获取外部内容预览
- `GET /api/recommendations/internal` - 获取内部推荐内容
- `GET /api/recommendations/ai` - 获取AI生成推荐

### AI写作接口

- `POST /api/ai/generate-title` - 生成文章标题
- `POST /api/ai/generate-summary` - 生成文章摘要
- `POST /api/ai/generate-tags` - 生成文章标签
- `POST /api/ai/optimize-content` - 优化文章内容

## 🚀 部署指南

### 开发环境部署

1. **启动后端服务**：

```bash
cd backend-spring
mvn spring-boot:run
```

2. **启动前端服务**：

```bash
npm run dev
```

3. **访问应用**：

- 前端: http://localhost:5173
- 后端: http://localhost:8080

### 生产环境部署

#### 方法一：Docker部署

1. **构建项目**：

```bash
# 构建前端
npm run build

# 构建后端
cd backend-spring
mvn clean package
```

2. **创建Dockerfile**：

```dockerfile
FROM openjdk:8-jre-alpine
COPY backend-spring/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

3. **运行容器**：

```bash
docker build -t ai-blog-system .
docker run -d -p 8080:8080 ai-blog-system
```

#### 方法二：传统部署

1. **构建项目**：

```bash
# 前端构建
npm run build:prod

# 后端构建
cd backend-spring
mvn clean package
```

2. **部署文件**：

```bash
# 前端文件部署到Nginx
cp -r dist/* /var/www/html/

# 后端JAR文件部署
java -jar backend-spring/target/backend-1.0.0.jar
```

#### 方法三：云平台部署

**Railway部署**：

```bash
# 安装Railway CLI
npm install -g @railway/cli

# 登录并部署
railway login
railway up
```

**Render部署**：

- 连接GitHub仓库
- 配置构建命令：`npm run build`
- 配置启动命令：`npm start`

## 🧪 测试

### 单元测试

```bash
# 前端测试
npm run test:unit

# 后端测试
cd backend-spring
mvn test
```

### 集成测试

```bash
# 运行所有测试
npm run test

# 后端集成测试
cd backend-spring
mvn verify
```

### 端到端测试

```bash
# 启动测试环境
npm run test:e2e
```

## 📊 性能优化

### 前端优化

- **代码分割**: 按路由懒加载组件
- **图片优化**: WebP格式和响应式图片
- **缓存策略**: 静态资源长期缓存
- **CDN加速**: 静态资源CDN分发

### 后端优化

- **数据库索引**: 优化查询性能
- **缓存机制**: Redis缓存热点数据
- **连接池**: 数据库连接池优化
- **异步处理**: 非阻塞异步操作

## 🔒 安全特性

- **HTTPS**: 强制HTTPS访问
- **CORS**: 跨域资源共享配置
- **XSS防护**: 输入输出过滤
- **CSRF防护**: 跨站请求伪造防护
- **SQL注入防护**: 参数化查询
- **权限控制**: 基于角色的访问控制

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 贡献方式

1. **报告Bug**: 在GitHub Issues中报告问题
2. **功能建议**: 提出新功能想法
3. **代码贡献**: 提交Pull Request
4. **文档改进**: 完善项目文档
5. **社区支持**: 帮助其他用户

### 开发流程

1. Fork项目仓库
2. 创建功能分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 创建Pull Request

### 代码规范

- 遵循各语言的官方代码规范
- 保持代码风格一致
- 添加必要的注释和文档
- 编写单元测试覆盖新功能

## 📝 更新日志

### [1.0.0] - 2024-01-01

- ✨ 初始版本发布
- 🚀 基础博客功能
- 🤖 AI写作助手
- 🧠 智能推荐系统
- 📱 响应式设计
- 🎨 现代化UI界面

### [0.9.0] - 2023-12-15

- 🔧 项目基础架构搭建
- 📚 基础文档编写
- 🏗️ 项目结构规划

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE) - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

感谢以下开源项目和技术社区的支持：

- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java企业级应用框架
- [Element Plus](https://element-plus.org/) - Vue 3组件库
- [OpenAI](https://openai.com/) - AI语言模型服务

## 📞 联系我们

- **项目地址**: [https://github.com/your-username/ai-blog-system](https://github.com/your-username/ai-blog-system)
- **问题反馈**: [GitHub Issues](https://github.com/your-username/ai-blog-system/issues)
- **讨论交流**: [GitHub Discussions](https://github.com/your-username/ai-blog-system/discussions)
- **邮箱联系**: contact@ai-blog.com

## ⭐ 支持项目

如果这个项目对您有帮助，请给我们一个⭐️！

---

**AI智能博客系统** - 让写作更智能，让博客更精彩！ 🚀
