<template>
  <div class="ai-chat-container">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="nav-content">
        <h1 class="nav-title">
          <span class="title-icon">🤖</span>
          AI 智能问答
        </h1>
        <div class="nav-controls">
          <div class="model-selector">
            <select
              id="model-select"
              v-model="selectedModel"
              @change="onModelChange"
              :disabled="isLoading"
              class="model-select"
            >
              <option value="">加载中...</option>
              <option v-for="model in availableModels" :key="model.name" :value="model.name">
                {{ model.name }} ({{ formatModelSize(model.size) }})
              </option>
            </select>
            <button @click="refreshModels" :disabled="isLoading" class="refresh-btn">
              <span class="btn-icon">🔄</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域：已移除旧版左右布局，统一使用下方 chat-main -->

    <div class="chat-main">
      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon">💬</div>
          <h3 class="empty-title">开始你的AI对话</h3>
          <p class="empty-description">选择一个AI模型，输入你的问题，开始智能对话之旅</p>
        </div>

        <div v-for="(message, index) in messages" :key="index" :class="['message', message.role]">
          <div class="message-avatar">
            <div class="avatar-icon">
              {{ message.role === 'user' ? '👤' : '🤖' }}
            </div>
            <div class="avatar-badge">{{ message.role === 'user' ? '用户' : 'AI' }}</div>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-role-text">{{ message.role === 'user' ? '你' : 'AI助手' }}</span>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            <div v-if="message.role === 'assistant'" class="message-actions">
              <button 
                @click="copyMessage(message.content, message.timestamp.getTime())" 
                class="copy-btn"
                :title="copyStatus[message.timestamp.getTime()] || '复制回答'"
              >
                <span class="copy-icon">📋</span>
                <span class="copy-text">{{ copyStatus[message.timestamp.getTime()] || '复制' }}</span>
              </button>
            </div>
          </div>
        </div>

        <div v-if="isLoading" class="message assistant typing-message">
          <div class="message-avatar">
            <div class="avatar-icon">🤖</div>
            <div class="avatar-badge">AI</div>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-role-text">AI助手</span>
              <span class="message-time">正在思考...</span>
            </div>
            <div class="typing-indicator">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-container">
        <div class="input-wrapper">
          <div class="input-header">
            <span class="input-label">
              <span class="label-icon">💭</span>
              输入你的问题
            </span>
            <span class="input-hint">Enter发送，Ctrl+Enter换行</span>
          </div>
          <textarea
            v-model="userInput"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.ctrl.enter="sendMessage"
            placeholder="请描述你的问题，AI助手将为你提供专业解答..."
            :disabled="isLoading || !selectedModel"
            ref="inputTextarea"
            rows="4"
            class="message-input"
          ></textarea>
          <div class="input-footer">
            <button
              @click="sendMessage"
              :disabled="isLoading || !userInput.trim() || !selectedModel"
              class="send-btn"
            >
              <span class="btn-icon">📤</span>
              <span class="btn-text">发送消息</span>
            </button>
          </div>
        </div>

        <div class="input-actions">
          <button @click="clearChat" class="action-btn clear-btn">
            <span class="btn-icon">🗑️</span>
            <span class="btn-text">清空对话</span>
          </button>
          <button @click="exportChat" class="action-btn export-btn">
            <span class="btn-icon">📥</span>
            <span class="btn-text">导出对话</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { marked } from 'marked'

interface Model {
  name: string
  size: number
  modified_at: string
  digest: string
}

interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

const OLLAMA_BASE_URL = 'http://localhost:11434'

const selectedModel = ref('')
const availableModels = ref<Model[]>([])
const messages = ref<Message[]>([])
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement>()
const inputTextarea = ref<HTMLTextAreaElement>()
const copyStatus = ref<Record<number, string>>({})

// 获取可用模型列表
const fetchModels = async () => {
  try {
    const response = await fetch(`${OLLAMA_BASE_URL}/api/tags`)
    if (!response.ok) {
      throw new Error('无法连接到Ollama服务')
    }
    const data = await response.json()
    availableModels.value = data.models || []

    // 如果没有选择模型，默认选择 deepseek-r1:8b
    if (!selectedModel.value && availableModels.value.length > 0) {
      const defaultModel = availableModels.value.find((model) =>
        model.name.includes('deepseek-r1:8b'),
      )
      selectedModel.value = defaultModel ? defaultModel.name : availableModels.value[0].name
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
    availableModels.value = []
  }
}

// 发送消息到AI
const sendMessage = async () => {
  if (!userInput.value.trim() || !selectedModel.value || isLoading.value) {
    return
  }

  const userMessage: Message = {
    role: 'user',
    content: userInput.value.trim(),
    timestamp: new Date(),
  }

  messages.value.push(userMessage)
  const currentInput = userInput.value.trim()
  userInput.value = ''
  isLoading.value = true

  try {
    const response = await fetch(`${OLLAMA_BASE_URL}/api/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        model: selectedModel.value,
        prompt: currentInput,
        stream: false,
      }),
    })

    if (!response.ok) {
      throw new Error('AI服务响应错误')
    }

    const data = await response.json()

    // 去除<think>标签中的内容
    const cleanedContent = removeThinkTags(data.response || '抱歉，我没有得到有效的回复。')

    const assistantMessage: Message = {
      role: 'assistant',
      content: cleanedContent,
      timestamp: new Date(),
    }

    messages.value.push(assistantMessage)
  } catch (error) {
    console.error('发送消息失败:', error)
    const errorMessage: Message = {
      role: 'assistant',
      content: '抱歉，发生了错误，请检查Ollama服务是否正常运行。',
      timestamp: new Date(),
    }
    messages.value.push(errorMessage)
  } finally {
    isLoading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化消息内容（支持Markdown）
const formatMessage = (content: string) => {
  try {
    return marked(content)
  } catch {
    return content.replace(/\n/g, '<br>')
  }
}

// 格式化时间
const formatTime = (timestamp: Date) => {
  return timestamp.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 格式化模型大小
const formatModelSize = (size: number) => {
  if (size >= 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024 * 1024)).toFixed(1)}GB`
  } else if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)}MB`
  } else {
    return `${(size / 1024).toFixed(1)}KB`
  }
}

// 去除<think>标签中的内容
const removeThinkTags = (content: string): string => {
  // 移除所有<think>...</think>标签及其内容
  return content.replace(/<think>[\s\S]*?<\/think>/gi, '').trim()
}

// 模型变更处理
const onModelChange = () => {
  console.log('切换到模型:', selectedModel.value)
}

// 刷新模型列表
const refreshModels = async () => {
  await fetchModels()
}

// 清空对话
const clearChat = () => {
  if (confirm('确定要清空所有对话吗？')) {
    messages.value = []
  }
}

// 导出对话
const exportChat = () => {
  const chatContent = messages.value
    .map((msg) => `${msg.role === 'user' ? '用户' : 'AI'}: ${msg.content}`)
    .join('\n\n')

  const blob = new Blob([chatContent], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `AI对话_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.txt`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// 复制消息内容
const copyMessage = async (content: string, messageTimestamp: number) => {
  try {
    // 移除HTML标签，获取纯文本内容
    const tempDiv = document.createElement('div')
    tempDiv.innerHTML = content
    const plainText = tempDiv.textContent || tempDiv.innerText || ''
    
    // 使用现代Clipboard API
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(plainText)
    } else {
      // 降级方案：使用传统的复制方法
      const textArea = document.createElement('textarea')
      textArea.value = plainText
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      document.execCommand('copy')
      document.body.removeChild(textArea)
    }
    
    // 显示复制成功状态
    copyStatus.value[messageTimestamp] = '已复制!'
    
    // 3秒后恢复原始状态
    setTimeout(() => {
      copyStatus.value[messageTimestamp] = ''
    }, 3000)
    
  } catch (error) {
    console.error('复制失败:', error)
    // 显示复制失败状态
    copyStatus.value[messageTimestamp] = '复制失败'
    
    // 3秒后恢复原始状态
    setTimeout(() => {
      copyStatus.value[messageTimestamp] = ''
    }, 3000)
  }
}

// 监听消息变化，自动滚动
watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
})

onMounted(() => {
  fetchModels()
})
</script>

<style scoped>
.top-nav {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.nav-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  margin: 0;
}

.nav-controls {
  display: flex;
  align-items: center;
}

.ai-chat-container {
  width: 100%;
  margin: 0 auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.chat-header {
  margin-bottom: 30px;
  padding: 30px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.title-icon {
  font-size: 2.8rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.page-subtitle {
  margin: 0;
  color: #7f8c8d;
  font-size: 1.1rem;
  font-weight: 400;
}

.model-selector {
  display: flex;
  align-items: center;
  gap: 30px;
  justify-content: center;
  flex-wrap: wrap;
}

.selector-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.selector-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #34495e;
  font-weight: 600;
  font-size: 0.95rem;
}

.label-icon {
  font-size: 1.1rem;
}

.model-select {
  padding: 12px 20px;
  border: 2px solid #e1e8ed;
  border-radius: 12px;
  background: white;
  color: #2c3e50;
  font-size: 0.95rem;
  font-weight: 500;
  min-width: 250px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.model-select:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}

.model-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.refresh-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  color: white;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 140px;
  justify-content: center;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.refresh-btn:active:not(:disabled) {
  transform: translateY(0);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-icon {
  font-size: 1.1rem;
}

.btn-text {
  font-weight: 600;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

.messages-container::-webkit-scrollbar {
  width: 8px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: #6c757d;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.7;
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #495057;
}

.empty-description {
  font-size: 1rem;
  margin: 0;
  line-height: 1.6;
  max-width: 400px;
}

.message {
  display: flex;
  gap: 16px;
  max-width: 90%;
  animation: messageSlideIn 0.5s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.assistant {
  align-self: flex-start;
}

.message-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.avatar-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: white;
  border: 3px solid #e0e0e0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.message.user .avatar-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
}

.message.assistant .avatar-icon {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border-color: #28a745;
}

.avatar-badge {
  font-size: 0.7rem;
  font-weight: 600;
  color: #6c757d;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 8px;
  border-radius: 10px;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

@keyframes messageSlideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-content {
  flex: 1;
  background: white;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  max-width: 100%;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.message.assistant .message-content {
  background: white;
  color: #2c3e50;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.message.user .message-header {
  border-bottom-color: rgba(255, 255, 255, 0.2);
}

.message-role-text {
  font-weight: 600;
  font-size: 0.9rem;
  color: #6c757d;
}

.message.user .message-role-text {
  color: rgba(255, 255, 255, 0.9);
}

.message-time {
  font-size: 0.8rem;
  color: #adb5bd;
  font-weight: 500;
}

.message.user .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.message-text {
  line-height: 1.7;
  word-wrap: break-word;
  font-size: 0.95rem;
}

.message-text :deep(p) {
  margin: 0 0 12px 0;
}

.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message.user .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.1);
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 10px 0;
}

/* 消息操作按钮样式 */
.message-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  color: #6c757d;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 80px;
  justify-content: center;
}

.copy-btn:hover {
  background: #e9ecef;
  border-color: #dee2e6;
  color: #495057;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.copy-btn:active {
  transform: translateY(0);
}

.copy-icon {
  font-size: 0.9rem;
}

.copy-text {
  font-size: 0.8rem;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  text-align: right;
}

.message.user .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.typing-indicator {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 8px 0;
}

.typing-indicator .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: -0.16s;
}

.typing-message .message-content {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 2px dashed #dee2e6;
}

@keyframes typing {
  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.input-container {
  padding: 16px;
  background: rgba(255, 255, 255, 0.95);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 12px;
}

.input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #34495e;
  font-weight: 600;
  font-size: 1rem;
}

.label-icon {
  font-size: 1.2rem;
}

.input-hint {
  color: #6c757d;
  font-size: 0.85rem;
  font-weight: 500;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e1e8ed;
  border-radius: 16px;
  resize: vertical;
  min-height: 80px;
  height: 100px;
  font-family: inherit;
  font-size: 0.95rem;
  line-height: 1.6;
  color: #2c3e50;
  background: white;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow:
    0 0 0 3px rgba(102, 126, 234, 0.1),
    0 4px 16px rgba(102, 126, 234, 0.15);
}

.message-input::placeholder {
  color: #adb5bd;
  font-style: italic;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
}

.send-btn {
  padding: 14px 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  min-width: 140px;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.send-btn:active:not(:disabled) {
  transform: translateY(0);
}

.send-btn:disabled {
  background: #adb5bd;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.input-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  padding: 10px 20px;
  border: 2px solid #e1e8ed;
  border-radius: 10px;
  background: white;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c757d;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.action-btn:hover {
  transform: translateY(-2px);
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  color: #667eea;
}

.clear-btn:hover {
  border-color: #dc3545;
  color: #dc3545;
  box-shadow: 0 4px 16px rgba(220, 53, 69, 0.15);
}

.export-btn:hover {
  border-color: #28a745;
  color: #28a745;
  box-shadow: 0 4px 16px rgba(40, 167, 69, 0.15);
}

@media (max-width: 768px) {
  .ai-chat-container {
    padding: 10px;
    height: auto;
  }

  .chat-header {
    padding: 20px;
    margin-bottom: 20px;
  }

  .page-title {
    font-size: 2rem;
    flex-direction: column;
    gap: 10px;
  }

  .title-icon {
    font-size: 2.2rem;
  }

  .page-subtitle {
    font-size: 1rem;
  }

  .model-selector {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }

  .selector-group {
    width: 100%;
    max-width: 300px;
  }

  .model-select {
    min-width: auto;
    width: 100%;
  }

  .refresh-btn {
    width: 100%;
    max-width: 200px;
  }

  .message {
    max-width: 95%;
    gap: 12px;
  }

  .message-avatar {
    gap: 4px;
  }

  .avatar-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .avatar-badge {
    font-size: 0.65rem;
    padding: 1px 6px;
  }

  .message-content {
    padding: 16px;
  }

  .input-container {
    padding: 12px;
  }

  .input-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .message-input {
    padding: 10px 12px;
    min-height: 80px;
    height: 90px;
  }

  .input-actions {
    justify-content: center;
  }

  .action-btn {
    padding: 8px 16px;
    font-size: 0.85rem;
  }
}
</style>
