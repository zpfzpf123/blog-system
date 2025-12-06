# API配置功能说明 🔐

## 功能概述

每个项目现在可以在 **API文档页面** 单独配置 **API Base URL** 和 **Access Token**，配置后会自动应用到该项目的所有API接口测试中。

---

## 🎯 核心功能

### 1. 在API文档页面直接配置
- **API Base URL**: 后端服务的基础地址（如 `http://localhost:8080`）
- **Access Token**: API访问令牌（如 `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`）
- **配置入口**: API文档页面的 "API配置" 按钮

### 2. 自动应用到所有接口
配置完成后，测试该项目下的任何API接口时：
- ✅ Base URL 自动填充
- ✅ Authorization 请求头自动添加
- ✅ 无需每次手动输入
- ✅ 随时可以修改配置

---

## 📝 使用方法

### 第一步：打开API配置

1. 进入项目的 **API文档** 页面
2. 点击顶部工具栏的 **"API配置"** 按钮（齿轮图标）
3. 弹出配置对话框

### 第二步：填写配置信息

在配置对话框中填写：
- **API Base URL**: 你的后端服务地址（如 `http://localhost:8080`）
- **Access Token**: 你的认证令牌（完整格式，包括 `Bearer` 前缀）
- 点击 **"保存配置"**

### 第三步：测试API接口

1. 点击任意接口的 **"测试"** 按钮
2. 系统会自动：
   - 填充配置的 Base URL
   - 添加 Authorization 请求头
   - 显示 "✓ 已应用项目配置" 提示
3. 直接点击 **"发送请求"** 即可

---

## 💡 示例配置

### API Base URL 示例
```
http://localhost:8080
https://api.example.com
http://192.168.1.100:3000
```

### Access Token 示例
```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

sk-1234567890abcdef

Basic dXNlcm5hbWU6cGFzc3dvcmQ=
```

---

## 🔍 界面说明

### API配置对话框
- 📡 **API Base URL**: 后端服务的基础地址，测试接口时会自动拼接该地址
- 🔐 **Access Token**: 访问令牌，将自动添加到请求的 Authorization 头中
- ✨ **使用示例**: 对话框中包含详细的配置示例说明

### API测试页面
- ✓ 已应用项目配置（Base URL输入框旁边的提示）
- ✓ 已自动应用项目配置的 Access Token（请求头区域的提示）
- 可以在测试时临时修改配置值

---

## 🛠️ 技术实现

### 数据库
- 表: `projects`
- 字段: 
  - `api_base_url` (VARCHAR 500)
  - `api_access_token` (VARCHAR 1000)

### 前端
- API文档管理和配置: `ApiDocPanel.vue`
  - 新增配置对话框
  - 自动应用配置到测试表单
  - 实时保存和读取配置

### 后端
- 实体: `Project.java`
- 使用 PATCH 方法更新配置
- 自动保存和返回配置信息

---

## ⚠️ 安全提示

1. **Access Token 是敏感信息**，请妥善保管
2. 不要在公开的截图或日志中暴露 Token
3. 定期更换 Token 以确保安全
4. 建议使用环境变量或配置中心管理生产环境的 Token

---

## 🎉 优势

✅ **一次配置，处处使用** - 不用每次测试都输入
✅ **在API页面配置** - 测试时就近配置，更加方便
✅ **即时生效** - 保存后立即应用到所有接口
✅ **团队协作友好** - 每个项目独立配置
✅ **提高效率** - 快速切换不同环境
✅ **减少错误** - 避免手动输入错误
✅ **随时修改** - 可以随时调整配置无需离开API页面

---

## 📞 问题反馈

如有问题或建议，请联系开发团队。
