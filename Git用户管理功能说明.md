# Git用户管理功能 - 实现进度与使用说明

## ✅ 已完成部分（后端）

### 1. 数据库层 ✅
- ✅ 创建了 `git_users` 表
- ✅ 在 `projects` 表添加了 `git_user_id` 字段
- ✅ 插入了默认示例数据

### 2. 实体层 ✅
- ✅ `GitUser.java` - Git用户实体类
- ✅ `Project.java` - 添加了gitUserId字段

### 3. 数据访问层 ✅
- ✅ `GitUserRepository.java` - 包含基础CRUD和查找默认用户

### 4. 业务逻辑层 ✅
- ✅ `GitUserService.java` - 完整的CRUD服务
  - 创建/更新/删除Git用户
  - 获取所有用户/根据ID获取
  - 设置默认用户
  - 自动处理唯一默认用户

### 5. API接口层 ✅
- ✅ `GitUserController.java` - 完整的REST API
  - `GET /api/git-users` - 获取所有Git用户
  - `GET /api/git-users/{id}` - 获取指定用户
  - `GET /api/git-users/default/user` - 获取默认用户
  - `POST /api/git-users` - 创建Git用户
  - `PUT /api/git-users/{id}` - 更新Git用户
  - `DELETE /api/git-users/{id}` - 删除Git用户
  - `POST /api/git-users/{id}/set-default` - 设置为默认用户

---

## ⏳ 还需完成的部分

### 1. 后端 - Git认证集成 ❌
**需要修改 `GitService.java`：**
- 在执行git push/pull前配置凭证
- 使用Personal Access Token进行认证
- 修改remote URL包含token（格式：`https://token@github.com/user/repo.git`）

**实现思路：**
```java
// 在commitAndPush方法中添加：
1. 根据project.gitUserId获取GitUser
2. 如果没有gitUserId，使用默认GitUser
3. 配置git credential：
   - 方案A：修改remote URL嵌入token
   - 方案B：使用git credential helper
   - 推荐方案A（简单直接）
```

### 2. 修改ProjectController ❌
- 智能提交时自动获取项目关联的Git用户
- 传递凭证给GitService

### 3. 前端 - Git用户管理页面 ❌
**需要创建：** `src/views/GitUserManager.vue`
- 用户列表展示（表格）
- 创建用户对话框
- 编辑用户对话框
- 删除确认
- 设置默认用户
- Personal Access Token输入（密码框）

### 4. 前端 - 路由配置 ❌
在 `src/router/index.ts` 添加路由：
```javascript
{
  path: '/git-users',
  name: 'GitUsers',
  component: () => import('@/views/GitUserManager.vue')
}
```

### 5. 前端 - 项目表单集成 ❌
**需要修改：** `ProjectManager.vue` 或项目创建/编辑表单
- 添加Git用户下拉选择框
- 自动带出用户名和密码
- 保存项目时保存gitUserId

### 6. 前端 - 导航菜单 ❌
在侧边栏或顶部导航添加"Git用户管理"入口

---

## 📋 数据库结构

### git_users表
```sql
- id: 主键
- name: 显示名称（如：张三的GitHub账号）
- username: Git用户名
- password: Personal Access Token
- email: 邮箱
- description: 描述
- is_default: 是否默认账号
- created_at: 创建时间
- updated_at: 更新时间
```

### projects表（新增字段）
```sql
- git_user_id: 关联的Git用户ID（外键）
```

---

## 🚀 使用流程（完成后）

### 1. 管理Git用户
1. 进入"Git用户管理"页面
2. 点击"新建Git用户"
3. 填写：
   - 名称：如"我的GitHub账号"
   - 用户名：GitHub用户名
   - Token：Personal Access Token
   - 邮箱：（可选）
4. 保存

### 2. 获取Personal Access Token
1. 登录GitHub
2. Settings → Developer settings → Personal access tokens
3. Generate new token (classic)
4. 勾选权限：`repo`（完整仓库访问）
5. 复制生成的token
6. 粘贴到系统中

### 3. 关联项目
1. 创建或编辑项目
2. 选择对应的Git用户
3. 保存

### 4. 智能提交
1. 打开项目详情
2. 点击"智能提交"
3. 系统自动使用该项目关联的Git用户进行认证
4. 完成提交

---

## 💡 Personal Access Token 认证原理

### Git HTTPS认证方式：
```bash
# 原始方式（需要输入用户名密码）
git clone https://github.com/user/repo.git

# 使用Token方式（无需输入）
git clone https://token@github.com/user/repo.git

# 或者配置credential helper
git config credential.helper store
```

### 在代码中实现：
```java
// 获取原始URL
String repoUrl = "https://github.com/user/repo.git";

// 获取token
String token = gitUser.getPassword();

// 构建带token的URL
String authenticatedUrl = repoUrl.replace("https://", "https://" + token + "@");

// 使用authenticatedUrl进行git操作
```

---

## ⚠️ 安全注意事项

1. **Token安全**：
   - Personal Access Token相当于密码，需妥善保管
   - 不要提交token到代码仓库
   - 定期更换token

2. **数据库加密**（建议）：
   - 可以对password字段进行加密存储
   - 使用时解密

3. **权限控制**：
   - Token只授予必要的权限
   - 使用精细化权限控制

---

## 📝 开发优先级建议

### 必须完成（核心功能）：
1. ✅ 后端API - 已完成
2. ❌ Git认证集成 - **最重要**
3. ❌ 前端Git用户管理页面 - **次重要**
4. ❌ 项目表单集成 - **必需**

### 可选功能（增强体验）：
- Token有效性验证
- Token过期提醒
- 多平台支持（GitHub/GitLab/Gitee）
- SSH密钥支持

---

## 🔧 快速测试后端API

### 使用Postman或curl测试：

```bash
# 1. 获取所有Git用户
GET http://localhost:4567/api/git-users

# 2. 创建Git用户
POST http://localhost:4567/api/git-users
Content-Type: application/json

{
  "name": "我的GitHub",
  "username": "your-username",
  "password": "ghp_your_token_here",
  "email": "your@email.com",
  "description": "个人GitHub账号",
  "isDefault": true
}

# 3. 获取默认用户
GET http://localhost:4567/api/git-users/default/user

# 4. 更新用户
PUT http://localhost:4567/api/git-users/1
Content-Type: application/json

{
  "name": "更新后的名称",
  ...
}

# 5. 删除用户
DELETE http://localhost:4567/api/git-users/1
```

---

## 📊 当前进度

- **后端**: 80% ✅（API完成，缺Git认证）
- **数据库**: 100% ✅
- **前端**: 0% ❌（全部未开始）
- **整体**: 40% 

---

## 🎯 下一步行动

1. **立即可用**：后端API已经可以通过Postman测试
2. **核心任务**：修改GitService添加Token认证（约15分钟）
3. **前端开发**：创建Git用户管理页面（约20分钟）
4. **集成测试**：完整流程测试

---

## 💬 需要帮助？

如果需要我继续完成剩余部分，请告诉我：
1. 是否需要立即完成Git认证集成？
2. 是否需要创建完整的前端页面？
3. 是否需要其他自定义功能？

**预计剩余开发时间：35-40分钟**
