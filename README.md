# AI博客管理系统 - 前端项目

## 📖 项目介绍

这是一个基于 Vue 3 + TypeScript + Element Plus 构建的现代化 AI 博客管理系统前端项目，提供博客管理、网站收藏、AI 对话、页面模板与统计分析等功能。

## 🚀 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **UI组件库**: Element Plus
- **构建工具**: Vite
- **状态管理**: Vue 3 Reactive API
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **图标**: Element Plus Icons

## ✨ 核心功能特性

### 1. 博客管理

- 文章列表与详情查看
- 文章创建与编辑
- 分类与标签管理

### 2. 网站收藏管理

- 网站与分类维护
- 收藏状态与快捷筛选
- 统计数据联动展示

### 3. AI 与效率工具

- AI 对话页面
- CSS 动画实验室
- 开发工具页
- 页面模板与统计看板

## 📦 安装与运行

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

### 安装依赖

```bash
npm install
# 或
yarn install
```

### 开发环境运行

```bash
npm run dev
# 或
yarn dev
```

访问: http://localhost:5173

### 生产环境构建

```bash
npm run build
# 或
yarn build
```

### 预览生产构建

```bash
npm run preview
# 或
yarn preview
```

## 📁 项目结构

```
blog/
├── src/
│   ├── components/          # 组件目录
│   │   └── ...
│   ├── utils/              # 工具函数
│   │   └── axios.ts        # Axios配置
│   ├── views/              # 页面组件
│   ├── router/             # 路由配置
│   ├── App.vue             # 根组件
│   └── main.ts             # 入口文件
├── public/                 # 静态资源
├── package.json            # 项目依赖
├── vite.config.ts          # Vite配置
├── tsconfig.json           # TypeScript配置
└── README.md               # 项目说明文档
```

## 🌐 API接口说明

- 当前接口以博客、网站收藏、AI 对话、模板与统计模块为主。

## 📝 开发规范

### 代码风格

- 使用 TypeScript 严格模式
- 遵循 Vue 3 Composition API 最佳实践
- 组件采用 `<script setup>` 语法
- 使用 ESLint 进行代码检查

### Git提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
perf: 性能优化
test: 测试相关
chore: 构建/工具链相关
```

## 🎨 UI设计说明

### 色彩方案

- 主色: Element Plus 默认蓝色 (#409EFF)
- 成功色: 绿色 (#67C23A)
- 警告色: 橙色 (#E6A23C)
- 错误色: 红色 (#F56C6C)

### 终端日志样式

- 背景: 深色主题 (#1e1e1e)
- 命令: 蓝色 (#4fc1ff)
- 成功: 绿色 (#4caf50)
- 错误: 红色 (#f44336)

## 🔥 最近更新

### v1.2.0 (2026-02-28)

- 🧹 移除项目管理与 Git 相关前后端模块
- 🗂️ 清理对应数据库迁移脚本

### v1.0.0

- 🎉 初始版本发布

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 👨‍💻 作者

AI博客团队

---

**注意**: 本项目需要配合后端服务（Spring Boot）一起使用。
后端项目路径: `../backend-spring/`
