# Git智能提交功能说明

## ✅ 已实现功能

### 1. 冲突处理 ✅ **已完全满足**
- ✅ 每次提交前自动执行 `git pull --rebase` 拉取最新代码
- ✅ 智能检测代码冲突
- ✅ 冲突时友好提示用户
- ✅ 提供"打开文件夹"按钮让用户解决冲突
- ✅ 提供"继续提交"按钮，解决后继续

**使用方法：**
1. 点击"智能提交"按钮
2. 系统自动pull并检测冲突
3. 如有冲突，对话框显示冲突信息
4. 点击"打开文件夹"按钮，在IDE中解决冲突
5. 解决完成后，点击"继续提交"按钮

### 2. 选择性提交文件 ✅ **后端已实现，前端UI待完成**

**后端已实现：**
- ✅ GitService支持选择性提交文件列表
- ✅ API接口支持 `selectedFiles` 参数
- ✅ 保存忽略规则到 `.git-commit-config.json`
- ✅ 读取忽略规则功能

**前端待实现：**
- ⏳ 文件选择界面（显示所有变更文件）
- ⏳ 文件勾选功能
- ⏳ "选择文件"按钮
- ⏳ 保存忽略规则按钮
- ⏳ 加载已保存的忽略规则

**API接口：**
```
POST /api/projects/{id}/git-commit
{
  "selectedFiles": ["file1.java", "file2.vue"],  // 可选
  "maxRetries": 3
}

POST /api/projects/{id}/git-ignore-rules
{
  "ignoreFiles": ["node_modules/", "*.log"]
}

GET /api/projects/{id}/git-ignore-rules
```

### 3. 自动重试机制 ✅ **已实现**
- ✅ 支持设置最大重试次数
- ✅ push失败自动重试
- ✅ 每次重试间隔2秒
- ✅ 显示重试进度
- ✅ 显示重试次数

**已实现特性：**
- 默认重试3次（可配置）
- 自动间隔2秒重试
- 日志显示重试进度
- 最终显示总重试次数

**前端待实现：**
- ⏳ 重试次数输入框
- ⏳ 终止重试按钮
- ⏳ 实时显示重试状态

## 🔄 需要完成的前端UI部分

### 1. 文件选择器UI
需要在对话框中添加：
```vue
<!-- 文件选择按钮 -->
<el-button @click="showFileSelector = true">选择要提交的文件</el-button>

<!-- 文件选择对话框 -->
<el-dialog title="选择要提交的文件">
  <el-checkbox-group v-model="selectedFiles">
    <el-checkbox v-for="file in allFiles" :label="file.path">
      {{ file.path }} ({{ file.status }})
    </el-checkbox>
  </el-checkbox-group>
  
  <!-- 保存忽略规则按钮 -->
  <el-button @click="saveIgnoreRules">保存此规则（下次自动应用）</el-button>
</el-dialog>
```

### 2. 重试设置UI
需要添加：
```vue
<!-- 重试设置 -->
<div class="retry-settings">
  <el-input-number v-model="maxRetries" :min="0" :max="10" label="最大重试次数" />
  <el-button v-if="isRetrying" @click="shouldStopRetry = true">终止重试</el-button>
</div>
```

## 📋 使用流程

### 完整提交流程：
1. **打开项目详情页**
2. **点击"智能提交"按钮**
3. **【可选】选择分支**（下拉菜单）
4. **【可选】点击"选择文件"**选择要提交的文件
5. **【可选】设置重试次数**
6. **系统自动执行：**
   - pull最新代码
   - 检测冲突
   - 生成提交信息
   - add选中的文件
   - commit
   - push（失败自动重试）
7. **成功或失败提示**

### 冲突处理流程：
1. 系统检测到冲突
2. 显示冲突文件列表
3. 点击"打开文件夹"
4. 在IDE中解决冲突
5. 点击"继续提交"
6. 系统继续push

## 🎯 后端实现细节

### GitService核心方法：
```java
// 智能提交（支持文件选择和重试）
public Map<String, Object> commitAndPush(
    String projectPath, 
    String commitMessage,
    List<String> selectedFiles,  // null表示提交所有
    int maxRetries
)

// 保存忽略规则
public Map<String, Object> saveIgnoreRules(
    String projectPath,
    List<String> ignoreFiles
)

// 读取忽略规则
public Map<String, Object> loadIgnoreRules(String projectPath)
```

### 自动重试逻辑：
```java
while (!pushSuccess && retryCount <= maxRetries) {
    if (retryCount > 0) {
        logs.add("第 " + retryCount + " 次重试推送...");
        Thread.sleep(2000);
    }
    
    Map<String, Object> pushResult = executeGitCommand(projectDir, "git", "push");
    
    if (pushResult.get("success")) {
        pushSuccess = true;
    } else {
        retryCount++;
    }
}
```

## 📝 配置文件

忽略规则保存在项目根目录：
```
project/
  .git/
  .git-commit-config.json  ← 忽略规则配置
```

配置文件格式：
```json
{
  "ignoreFiles": [
    "node_modules/",
    "*.log",
    "dist/"
  ],
  "updateTime": 1701234567890
}
```

## 🔧 下一步开发任务

1. ✅ 后端API - 已完成
2. ⏳ 前端文件选择器UI
3. ⏳ 前端重试设置UI
4. ⏳ 前端加载/保存忽略规则
5. ⏳ 测试完整流程

## 💡 技术要点

### 分支选择：
- 显示本地和远程分支
- 切换分支前检查是否有未提交变更
- 自动显示当前分支

### 文件选择：
- 显示文件变更类型（M/A/D/?）
- 支持全选/反选
- 保存规则后自动应用

### 自动重试：
- 失败后自动重试
- 可配置重试次数
- 实时显示重试进度
- 支持手动终止

## ⚠️ 注意事项

1. **冲突处理**：必须手动解决，系统无法自动处理
2. **忽略规则**：保存后每次提交自动应用
3. **重试机制**：只针对push失败，commit失败不重试
4. **分支切换**：有未提交变更时无法切换
