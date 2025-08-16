<!--
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-13 12:46:17
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-08-13 13:22:17
 * @FilePath: \blog\AI问答功能说明.md
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->

# AI问答功能说明

## 功能概述

AI问答功能基于Ollama本地大语言模型，提供智能对话服务。

## 主要特性

- ✅ 自动检测本地Ollama模型
- ✅ 默认使用qwen3:8b模型
- ✅ 支持Markdown渲染
- ✅ 对话导出功能
- ✅ 响应式设计
- ✅ 智能摘要生成（不超过100字）
- ✅ 自动过滤<think>标签内容

## 使用前准备

### 1. 安装Ollama

```bash
# 访问 https://ollama.ai/download 下载安装
```

### 2. 启动服务

```bash
ollama serve
```

### 3. 下载模型

```bash
ollama pull qwen3:8b
```

## 使用方法

1. 点击导航菜单中的"AI问答"
2. 选择可用模型
3. 输入问题并发送
4. 查看AI回复

## 智能摘要功能

### 摘要生成规则

- 摘要长度：严格控制在100字以内
- 语言：中文
- 格式：纯文本，不包含"摘要："等前缀
- 内容：智能提取文章核心要点

### 内容过滤

- 自动去除`<think>xxx</think>`标签中的内容
- 确保返回内容简洁明了
- 避免模型内部思考过程泄露

## 故障排除

- **连接失败**：检查Ollama服务是否启动
- **无模型**：运行 `ollama pull qwen3:8b`
- **响应慢**：使用更小的模型或检查系统资源

## 测试

打开 `test-ai-chat.html` 进行功能测试。
