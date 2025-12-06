# Git智能提交 - 迁移指南

## 🎯 快速开始

### 方案A：直接替换（推荐）

如果你想立即使用新版本，直接替换组件：

```vue
<!-- ProjectDetail.vue -->

<!-- 旧版 -->
<GitCommitModal 
  v-model="showGitCommitModal"
  :project-id="project?.id"
  @success="refreshGitCommits"
/>

<!-- 新版 -->
<GitCommitModalNew 
  v-model="showGitCommitModal"
  :project-id="project?.id"
  @success="refreshGitCommits"
/>
```

然后更新import：
```javascript
// 旧版
import GitCommitModal from '@/components/GitCommitModal.vue'

// 新版
import GitCommitModalNew from '@/components/GitCommitModalNew.vue'
```

### 方案B：并行测试

如果你想先测试新版本，可以同时保留两个版本：

```vue
<template>
  <!-- 添加切换按钮 -->
  <el-switch 
    v-model="useNewCommit" 
    active-text="新版提交" 
    inactive-text="旧版提交"
  />
  
  <!-- 旧版 -->
  <GitCommitModal 
    v-if="!useNewCommit"
    v-model="showGitCommitModal"
    :project-id="project?.id"
    @success="refreshGitCommits"
  />
  
  <!-- 新版 -->
  <GitCommitModalNew 
    v-if="useNewCommit"
    v-model="showGitCommitModal"
    :project-id="project?.id"
    @success="refreshGitCommits"
  />
</template>

<script setup lang="ts">
import GitCommitModal from '@/components/GitCommitModal.vue'
import GitCommitModalNew from '@/components/GitCommitModalNew.vue'

const useNewCommit = ref(true) // 默认使用新版
</script>
```

## 📋 完整迁移步骤

### 1. 后端部署

#### 步骤1：添加新服务类
```bash
# 确保文件存在
backend-spring/src/main/java/com/blog/service/GitSmartCommitService.java
```

#### 步骤2：更新Controller
```bash
# 确保已添加新的API方法到
backend-spring/src/main/java/com/blog/controller/ProjectController.java
```

#### 步骤3：重启后端服务
```bash
cd backend-spring
mvn clean package
java -jar target/blog-backend.jar
```

#### 步骤4：验证API
```bash
# 测试新API是否可用
curl -X POST http://localhost:8080/api/projects/1/smart-commit \
  -H "Content-Type: application/json" \
  -d '{"commitMessage": "test"}'
```

### 2. 前端部署

#### 步骤1：添加新组件
```bash
# 确保文件存在
src/components/GitCommitModalNew.vue
```

#### 步骤2：更新引用
在 `src/views/ProjectDetail.vue` 中：

```vue
<script setup lang="ts">
// 添加import
import GitCommitModalNew from '@/components/GitCommitModalNew.vue'

// 其他代码保持不变...
</script>

<template>
  <!-- 找到原来的GitCommitModal，替换为GitCommitModalNew -->
  <GitCommitModalNew 
    v-model="showGitCommitModal"
    :project-id="project?.id"
    @success="refreshGitCommits"
  />
</template>
```

#### 步骤3：重新构建前端
```bash
npm run build
# 或开发模式
npm run dev
```

## 🔍 验证清单

### 后端验证

- [ ] GitSmartCommitService.java 编译通过
- [ ] ProjectController.java 编译通过
- [ ] 后端服务启动成功
- [ ] 新API端点可访问

测试命令：
```bash
# 检查编译
mvn compile

# 检查API
curl http://localhost:8080/api/projects/1/git/branches
```

### 前端验证

- [ ] GitCommitModalNew.vue 无语法错误
- [ ] ProjectDetail.vue 引用正确
- [ ] 前端构建成功
- [ ] 页面加载无错误

测试步骤：
1. 打开浏览器控制台
2. 访问项目详情页
3. 点击"Git提交"按钮
4. 检查是否显示新版弹窗（标题显示"新版"）

## 🐛 常见问题

### 问题1：后端编译错误

**错误信息**：
```
Cannot find symbol: class GitSmartCommitService
```

**解决方案**：
1. 确认 `GitSmartCommitService.java` 文件存在
2. 确认包名正确：`package com.blog.service;`
3. 重新编译：`mvn clean compile`

### 问题2：API 404错误

**错误信息**：
```
POST /api/projects/1/smart-commit 404 Not Found
```

**解决方案**：
1. 确认后端已重启
2. 检查 `ProjectController` 中是否添加了新方法
3. 检查 `@PostMapping` 注解路径是否正确

### 问题3：前端组件不显示

**现象**：点击"Git提交"按钮无反应

**解决方案**：
1. 打开浏览器控制台查看错误
2. 确认 `GitCommitModalNew.vue` 导入路径正确
3. 确认 `v-model` 绑定正确
4. 清除浏览器缓存后重试

### 问题4：冲突检测不工作

**现象**：有冲突但没有提示

**解决方案**：
1. 检查后端日志
2. 确认 `parseConflictFiles` 方法正常工作
3. 手动测试：`git diff --name-only --diff-filter=U`

## 🔄 回滚方案

如果新版本出现问题，可以快速回滚：

### 前端回滚
```vue
<!-- 恢复旧版import -->
import GitCommitModal from '@/components/GitCommitModal.vue'

<!-- 恢复旧版组件 -->
<GitCommitModal 
  v-model="showGitCommitModal"
  :project-id="project?.id"
  @success="refreshGitCommits"
/>
```

### 后端回滚
旧版API仍然保留，前端回滚后会自动使用旧API。

## 📊 功能对比

| 功能 | 旧版 | 新版 |
|------|------|------|
| 提交顺序 | ❌ commit → pull | ✅ pull → commit |
| 冲突处理 | ⚠️ 不完整 | ✅ 完整 |
| 自动重试 | ❌ 无 | ✅ 有（可配置） |
| 提交信息保留 | ❌ 冲突时丢失 | ✅ 始终保留 |
| 取消提交 | ❌ 无 | ✅ 有 |
| 重试配置 | ❌ 无 | ✅ 0-10次 |
| 日志显示 | ✅ 有 | ✅ 更清晰 |
| 分支选择 | ✅ 有 | ✅ 有 |

## 🎓 使用建议

### 开发环境
- 建议使用方案B（并行测试）
- 可以随时切换对比
- 发现问题及时反馈

### 生产环境
- 建议先在开发环境充分测试
- 确认无问题后使用方案A（直接替换）
- 保留旧版代码以备回滚

## 📞 技术支持

如果遇到问题：

1. 查看控制台错误信息
2. 检查后端日志
3. 参考本文档的"常见问题"部分
4. 查看详细说明：`GIT智能提交-重构说明.md`

## 🚀 下一步

迁移完成后，你可以：

1. 测试正常提交流程
2. 测试冲突处理流程
3. 测试自动重试功能
4. 根据需要调整重试次数
5. 考虑添加更多功能（见重构说明文档）

## ✅ 迁移完成检查

- [ ] 后端服务正常启动
- [ ] 新API可以访问
- [ ] 前端页面正常显示
- [ ] 可以打开提交弹窗
- [ ] 可以正常提交代码
- [ ] 冲突检测正常工作
- [ ] 自动重试功能正常
- [ ] 日志显示正确

全部完成后，迁移成功！🎉
