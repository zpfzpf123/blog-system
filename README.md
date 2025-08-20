# AI智能博客系统 🚀

[![Vue](https://img.shields.io/badge/Vue-3.5.18-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7.14-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8.0-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

> 🌟 **项目地址**: [https://github.com/your-username/ai-blog-system](https://github.com/your-username/ai-blog-system)  
> 📖 **在线演示**: [https://your-demo-url.com](https://your-demo-url.com)  
> 🚀 **快速开始**: [部署指南](#部署)

一个基于 Vue 3 + Spring Boot 的全栈博客应用，集成AI写作、智能推荐等先进功能。

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

## 技术栈

### 前端
- Vue 3 (Composition API)
- Vite
- Element Plus
- Axios
- Markdown (marked.js)

### 后端
- Spring Boot 2.7.14
- Java 8
- Spring Data JPA
- H2 Database (开发环境)
- Jsoup (网页内容解析)

### 构建工具
- Maven (后端)
- npm (前端)

## 项目结构

```
blog/
├── backend-spring/     # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/blog/
│   │   │   │   ├── controller/   # 控制器层
│   │   │   │   ├── dto/          # 数据传输对象
│   │   │   │   ├── entity/       # 实体类
│   │   │   │   ├── repository/   # 数据访问层
│   │   │   │   └── service/      # 业务逻辑层
│   │   └── resources/
├── src/                # Vue 前端
│   ├── assets/         # 静态资源
│   ├── components/     # 组件
│   ├── router/         # 路由配置
│   ├── utils/          # 工具类
│   ├── views/          # 页面组件
│   ├── App.vue         # 根组件
│   └── main.ts         # 入口文件
├── README.md
└── package.json
```

## 快速开始

### 环境要求

- Node.js >= 16
- npm >= 8
- Java 8
- Maven >= 3.6

### 安装依赖

```bash
# 安装前端依赖
npm install

# 后端依赖通过 Maven 管理，无需手动安装
```

### 开发环境

```bash
# 启动后端服务
cd backend-spring
mvn spring-boot:run

# 启动前端开发服务器
npm run dev
```

### 构建部署

```bash
# 构建前端
npm run build

# 构建后端
cd backend-spring
mvn clean package

# 运行构建后的应用
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## 功能详解

### 1. 文章管理

- 支持创建、编辑、删除文章
- 使用 Markdown 语法编写文章内容
- 支持文章分类和标签
- 支持文章摘要和封面图

### 2. 分类和标签

- 可以为文章添加分类和标签
- 支持分类和标签的增删改查

### 3. 推荐功能

博客系统包含智能推荐功能，分为两个部分：

1. **内部推荐**：基于相同标签的文章推荐
2. **外部推荐**：基于文章标题、内容和标签的智能搜索推荐

### 4. 智能推荐算法

智能推荐功能通过以下方式提高推荐准确性：

1. **多维度关键词提取**：
   - 从文章标题中提取关键词
   - 从文章内容中提取关键句子
   - 结合文章标签生成复合查询

2. **相关性评分**：
   - 为每个推荐结果计算相关性评分
   - 按评分高低排序推荐结果
   - 去重并保留最相关的结果

3. **内容预览**：
   - 支持按需加载外部文章内容预览
   - 自动识别并翻译英文内容
   - 提供流畅的用户体验

### 5. 外部资源获取(fetch工具使用)

本系统使用多种fetch工具来获取和处理外部资源：

- **search_web**: 搜索网络上的相关信息，获取相关链接和摘要
- **fetch_content**: 提取网页的主要内容
- **mcp_fetch_fetch_html**: 获取网页的完整HTML结构
- **mcp_fetch_fetch_json**: 获取并解析JSON格式的数据
- **mcp_fetch_fetch_txt**: 获取纯文本内容

在博客系统中，这些工具主要用于：

1. **外部文章推荐**：通过搜索网络获取与当前文章主题相关的内容
2. **内容预览**：获取外部文章的部分内容作为预览显示
3. **资源聚合**：从多个来源获取数据并整合到博客系统中

后端使用Jsoup库来解析网页内容，提取有用信息并提供给前端展示。

## API 接口

### 分类相关
- `GET /api/categories` - 获取所有分类
- `POST /api/categories` - 创建分类
- `PUT /api/categories/{id}` - 更新分类
- `DELETE /api/categories/{id}` - 删除分类

### 标签相关
- `GET /api/tags` - 获取所有标签
- `POST /api/tags` - 创建标签
- `PUT /api/tags/{id}` - 更新标签
- `DELETE /api/tags/{id}` - 删除标签

### 文章相关
- `GET /api/posts` - 获取文章列表(支持分页、筛选)
- `GET /api/posts/{id}` - 获取文章详情
- `POST /api/posts` - 创建文章
- `PUT /api/posts/{id}` - 更新文章
- `DELETE /api/posts/{id}` - 删除文章

### 推荐相关
- `GET /api/recommendations/external` - 获取外部推荐内容
- `GET /api/recommendations/external/smart` - 获取智能外部推荐内容
- `GET /api/recommendations/external/preview` - 获取外部内容预览
- `GET /api/recommendations/internal` - 获取内部推荐内容

## 部署

### 开发环境部署

1. 启动后端服务：
```bash
cd backend-spring
mvn spring-boot:run
```

2. 启动前端服务：
```bash
npm run dev
```

### 生产环境部署

1. 构建项目：
```bash
# 构建前端
npm run build

# 构建后端
cd backend-spring
mvn clean package
```

2. 运行应用：
```bash
java -jar backend-spring/target/backend-0.0.1-SNAPSHOT.jar
```

## 开发指南

### 添加新功能

1. 在后端对应的 controller/service 中添加新接口
2. 在前端添加相应的页面和组件
3. 更新 README.md 文档

### 代码规范

- 遵循各语言的官方代码规范
- 保持代码风格一致
- 添加必要的注释

## 常见问题

### 端口冲突

- 后端默认端口：3000
- 前端默认端口：5173

如需修改端口，请相应修改配置文件。

### 数据库配置

开发环境使用 H2 内存数据库，无需额外配置。

## 贡献

欢迎提交 Issue 和 Pull Request 来改进本项目。