# AI问答功能使用指南

## 🚀 快速开始

### 1. 启动Ollama服务

**Windows用户：**

```bash
# 双击运行
start-ollama.bat

# 或手动启动
ollama serve
```

**Linux/macOS用户：**

```bash
# 运行脚本
./start-ollama.sh

# 或手动启动
ollama serve
```

### 2. 下载模型

```bash
# 下载推荐的模型
ollama pull qwen3:8b

# 或下载其他模型
ollama pull llama2:7b
ollama pull deepseek-r1:8b
```

### 3. 启动博客应用

```bash
npm run dev
```

### 4. 访问AI问答

- 打开浏览器访问：`http://localhost:5173`
- 点击导航菜单中的"AI问答"
- 或直接访问：`http://localhost:5173/ai-chat`

## ✨ 功能特性

### 智能对话

- 🤖 基于本地Ollama大语言模型
- 💬 实时对话，支持多轮交互
- 📝 Markdown格式支持
- 💻 代码高亮显示

### 模型管理

- 🔍 自动检测本地可用模型
- 🎯 默认使用qwen3:8b
- 🔄 支持模型切换
- 📊 显示模型大小信息

### 用户体验

- 📱 响应式设计，支持移动端
- ⌨️ 键盘快捷键（Enter发送，Ctrl+Enter换行）
- 📜 自动滚动到最新消息
- ⏱️ 消息时间戳

### 管理功能

- 🗑️ 一键清空对话
- 💾 导出对话为文本文件
- 🔄 刷新模型列表

## 🛠️ 技术实现

### 前端技术

- **Vue 3** + **TypeScript**
- **Vue Router** 路由管理
- **Marked** Markdown渲染
- **响应式设计**

### API接口

- **Ollama API** 本地服务通信
- **模型列表** `GET /api/tags`
- **生成回复** `POST /api/generate`

### 核心功能

1. **模型管理模块** - 获取和管理可用模型
2. **对话模块** - 处理用户输入和AI回复
3. **渲染模块** - Markdown和代码高亮
4. **导出模块** - 对话历史导出

## 🔧 配置说明

### 服务地址

默认使用 `http://localhost:11434`

如需修改，编辑 `src/views/AIChat.vue`：

```javascript
const OLLAMA_BASE_URL = 'http://your-ollama-host:11434'
```

### 默认模型

默认优先使用 `qwen3:8b`

如需修改，编辑 `src/views/AIChat.vue` 中的 `fetchModels` 函数。

## 🧪 测试功能

项目包含测试页面 `test-ai-chat.html`：

1. **连接测试** - 验证Ollama服务
2. **模型检测** - 查看可用模型
3. **对话测试** - 验证AI回复功能

使用方法：

```bash
# 在浏览器中打开
open test-ai-chat.html
```

## ❗ 故障排除

### 常见问题

**1. 连接失败**

```
错误: 无法连接到Ollama服务
```

**解决方案：**

- 检查Ollama服务：`ollama serve`
- 确认端口11434未被占用
- 检查防火墙设置

**2. 没有可用模型**

```
错误: 模型列表为空
```

**解决方案：**

```bash
# 下载模型
ollama pull qwen3:8b

# 查看模型列表
ollama list
```

**3. 响应速度慢**

```
问题: AI回复很慢
```

**解决方案：**

- 使用更小的模型（2B/3B版本）
- 检查系统资源使用情况
- 考虑GPU加速（如果支持）

**4. 页面显示异常**

```
问题: 页面样式或功能异常
```

**解决方案：**

- 清除浏览器缓存
- 检查控制台错误信息
- 确认依赖已正确安装

## 📋 使用示例

### 基础对话

```
用户: 你好，请介绍一下自己
AI: 你好！我是一个基于Ollama的AI助手...
```

### 代码相关

````
用户: 请写一个Python函数来计算斐波那契数列
AI: 好的，这里是一个计算斐波那契数列的Python函数：

```python
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)
````

```

### 多轮对话
```

用户: 什么是机器学习？
AI: 机器学习是人工智能的一个分支...

用户: 能详细解释一下监督学习吗？
AI: 监督学习是机器学习的一种方法...

```

## 🔄 扩展功能

### 计划中的功能
- [ ] 流式回复（实时显示）
- [ ] 对话上下文管理
- [ ] 模型参数配置
- [ ] 对话历史保存
- [ ] 多语言支持
- [ ] 语音输入/输出

### 自定义扩展
如需添加新功能，可以修改 `src/views/AIChat.vue` 文件。

## 📞 技术支持

如遇到问题：

1. 查看浏览器控制台错误信息
2. 检查Ollama服务状态
3. 参考故障排除部分
4. 使用测试页面验证功能

## 📄 相关文件

- `src/views/AIChat.vue` - AI问答页面组件
- `src/router/index.ts` - 路由配置
- `src/components/NavigationMenu.vue` - 导航菜单
- `test-ai-chat.html` - 功能测试页面
- `start-ollama.bat` - Windows启动脚本
- `start-ollama.sh` - Linux/macOS启动脚本

---

**注意：** AI问答功能需要本地Ollama服务支持，请确保服务正常运行。
```
