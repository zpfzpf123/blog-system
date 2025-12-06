# Git 功能完整说明

## 📋 功能概览

项目已实现完整的 Git 管理功能，包括：

| 功能 | 说明 | 状态 |
|------|------|------|
| 智能提交 | 一键完成 add → pull → commit → push | ✅ |
| 拉取代码 | 独立的 fetch + pull --rebase | ✅ |
| 文件管理 | 暂存/取消暂存/放弃修改/查看Diff | ✅ |
| 提交历史 | 查看历史记录、回退到某个提交、Cherry-pick | ✅ |
| 分支管理 | 创建/切换/删除/合并分支 | ✅ |
| Stash | 暂存当前修改、恢复、删除 | ✅ |
| 冲突管理 | 查看冲突文件、在IDE中打开、中止/继续操作 | ✅ |
| 状态面板 | 显示当前分支、变更数、领先/落后 | ✅ |

---

## 🎯 使用入口

在项目详情页顶部，有一组 Git 操作按钮：

```
[提交] [拉取] [文件] [历史] [分支] [Stash] [冲突]
```

---

## 📖 功能详解

### 1. 智能提交（提交按钮）

**流程**：
1. 检查工作区状态
2. 暂存文件 (git add)
3. 拉取远程代码 (git fetch + git pull --rebase)
4. 检测冲突（如有冲突暂停，等待解决）
5. 提交到本地 (git commit)
6. 推送到远程 (git push，支持自动重试)

**特点**：
- 先拉取后提交，避免冲突时丢失提交信息
- 支持配置自动重试次数（0-10次）
- 冲突时显示文件列表，双击可打开文件
- 支持取消提交（中止 rebase）

### 2. 拉取代码（拉取按钮）

**功能**：
- 选择本地分支和远程分支
- 执行 git fetch + git pull --rebase
- 检测冲突并提示

**适用场景**：
- 只想拉取最新代码，不提交
- 开始工作前同步远程更新

### 3. 文件变更管理（文件按钮）

**功能**：
- 查看已暂存和未暂存的文件
- 单个文件暂存/取消暂存
- 一键暂存全部/取消全部
- 查看文件 Diff（修改对比）
- 放弃单个文件的修改

**文件状态**：
- `M` - 修改（蓝色）
- `A` - 新增（绿色）
- `D` - 删除（红色）

### 4. 提交历史（历史按钮）

**功能**：
- 查看全部提交记录（支持搜索过滤）
- 点击查看提交详情和变更文件
- 双击文件查看差异（支持上下导航跳转）
- 右键文件可回退单个文件或复制路径
- 回退到某个提交（支持 soft/mixed/hard 模式）
- Cherry-pick 拣选提交到当前分支
- 复制提交哈希

**回退模式**：
- `soft` - 保留暂存区和工作区
- `mixed` - 保留工作区，清空暂存区
- `hard` - 丢弃所有修改（危险）

**差异导航快捷键**：
- `↑` 或 `k` - 上一个差异块
- `↓` 或 `j` - 下一个差异块
- `Esc` - 关闭差异窗口
- 支持无限循环跳转

### 5. 分支管理（分支按钮）

**功能**：
- 查看当前分支
- 查看本地和远程分支列表
- 创建新分支（可选择是否切换）
- 切换分支
- 删除本地/远程分支
- 合并分支

**注意**：
- 有未提交更改时无法切换分支
- 删除远程分支不可恢复

### 6. Stash 暂存（Stash按钮）

**功能**：
- 暂存当前修改（可添加备注）
- 查看 Stash 列表
- 恢复暂存（保留或删除）
- 删除暂存

**适用场景**：
- 临时切换分支处理紧急任务
- 保存当前工作进度

### 7. 冲突管理（冲突按钮）

**功能**：
- 查看当前所有冲突文件
- 预览冲突文件内容（带冲突标记高亮）
- 双击或右键在WebStorm中打开文件
- 标记文件为已解决
- 中止当前操作（rebase/merge/cherry-pick）
- 继续操作（解决冲突后）

**冲突标记说明**：
- `<<<<<<< HEAD` - 当前分支的内容开始
- `=======` - 分隔线
- `>>>>>>> xxx` - 传入的更改结束

**冲突触发场景**：
- 拉取代码时（pull --rebase）
- 合并分支时（merge）
- Cherry-pick 时
- 智能提交时

**所有冲突场景都会**：
- 显示冲突文件列表
- 支持双击在WebStorm中打开
- 提供"中止操作"和"继续操作"按钮

---

## 🔌 API 接口

### 状态相关
- `GET /api/projects/{id}/git/detailed-status` - 获取详细状态
- `GET /api/projects/{id}/git/branches` - 获取分支列表
- `GET /api/projects/{id}/git/commits?limit=20` - 获取提交历史
- `GET /api/projects/{id}/git/commits/{hash}/files` - 获取提交文件

### 提交相关
- `POST /api/projects/{id}/smart-commit` - 智能提交
- `POST /api/projects/{id}/continue-commit` - 继续提交（冲突后）
- `POST /api/projects/{id}/abort-commit` - 取消提交

### 拉取相关
- `POST /api/projects/{id}/git/fetch` - 获取远程更新
- `POST /api/projects/{id}/git/pull-rebase` - 变基拉取

### 文件相关
- `POST /api/projects/{id}/git/stage` - 暂存文件
- `POST /api/projects/{id}/git/unstage` - 取消暂存
- `POST /api/projects/{id}/git/discard` - 放弃修改
- `POST /api/projects/{id}/git/diff` - 获取文件差异

### 分支相关
- `POST /api/projects/{id}/git/branches/create` - 创建分支
- `POST /api/projects/{id}/git/branches/delete` - 删除分支
- `POST /api/projects/{id}/switch-branch` - 切换分支
- `POST /api/projects/{id}/git/merge` - 合并分支

### Stash 相关
- `POST /api/projects/{id}/git/stash` - 创建暂存
- `GET /api/projects/{id}/git/stash/list` - 获取暂存列表
- `POST /api/projects/{id}/git/stash/apply` - 应用暂存
- `POST /api/projects/{id}/git/stash/drop` - 删除暂存

### 回退相关
- `POST /api/projects/{id}/git/reset` - 回退到某个提交
- `POST /api/projects/{id}/git/revert-file` - 回退单个文件到某个提交
- `POST /api/projects/{id}/git/cherry-pick` - Cherry-pick 拣选提交

### 冲突管理
- `GET /api/projects/{id}/git/conflicts` - 获取冲突文件列表
- `GET /api/projects/{id}/git/conflict-content` - 获取冲突文件内容
- `POST /api/projects/{id}/git/mark-resolved` - 标记文件为已解决
- `POST /api/projects/{id}/git/abort-operation` - 中止当前操作
- `POST /api/projects/{id}/git/continue-operation` - 继续当前操作

---

## 📁 文件结构

### 后端
```
backend-spring/src/main/java/com/blog/
├── service/
│   └── GitSmartCommitService.java    # Git 服务（所有功能实现）
└── controller/
    └── ProjectController.java         # API 端点
```

### 前端
```
src/components/
├── GitCommitModalNew.vue    # 智能提交对话框
├── GitPullModal.vue         # 拉取代码对话框
├── GitFilesModal.vue        # 文件变更管理对话框
├── GitHistoryModal.vue      # 提交历史对话框
├── GitBranchModal.vue       # 分支管理对话框
├── GitStashModal.vue        # Stash 管理对话框
├── GitConflictModal.vue     # 冲突管理对话框
└── GitStatusPanel.vue       # Git 状态面板组件
```

---

## ⚠️ 注意事项

1. **冲突处理**：系统只能检测冲突，需要手动在 IDE 中解决
2. **硬回退**：`git reset --hard` 会丢弃所有修改，请谨慎使用
3. **远程分支删除**：删除后无法恢复，请确认后再操作
4. **切换分支**：有未提交更改时无法切换，请先提交或 Stash
5. **自动重试**：仅针对 push 失败，commit 失败不会重试

---

## 🎉 更新日志

**2024-12-06 (v3)**
- ✅ 新增"冲突"按钮和冲突管理弹窗
- ✅ 所有冲突场景统一显示冲突文件列表
- ✅ 冲突文件支持双击/右键在WebStorm中打开
- ✅ Cherry-pick/合并/拉取冲突时显示冲突文件
- ✅ 支持中止操作和继续操作（解决冲突后）
- ✅ 冲突文件内容预览（带冲突标记高亮）

**2024-12-06 (v2)**
- ✅ 变更文件右键菜单（回退单个文件、复制路径）
- ✅ 双击文件进入差异页面
- ✅ 差异页面上下导航控制器（可循环跳转）
- ✅ 差异导航键盘快捷键支持（↑/k, ↓/j, Esc）
- ✅ 提交历史搜索过滤功能
- ✅ 点击复制提交哈希
- ✅ Cherry-pick 拣选提交功能

**2024-12-06 (v1)**
- ✅ 修复提交顺序问题（先 pull 后 commit）
- ✅ 添加独立的拉取功能
- ✅ 添加文件变更管理（暂存/取消/Diff）
- ✅ 添加提交历史查看和回退
- ✅ 添加分支管理（创建/删除/合并）
- ✅ 添加 Stash 功能
- ✅ 优化 UI 布局，添加 Git 操作按钮组
