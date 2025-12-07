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
/* AI聊天容器 */
.ai-chat-container {
  width: 100%;
  margin: 0 auto;
  padding: var(--spacing-4);
  display: flex;
  flex-direction: column;
  background: var(--bg-body);
  min-height: calc(100vh - 64px);
}

/* 顶部导航 */
.top-nav {
  display: flex;
  justify-content: center;
  margin-bottom: var(--spacing-4);
  background: var(--bg-card);
  padding: var(--spacing-4) var(--spacing-5);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.nav-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-4);
}

.nav-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  margin: 0;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 1.5rem;
}

.nav-controls {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

/* 模型选择器 */
.model-selector {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.model-select {
  padding: var(--spacing-3) var(--spacing-4);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: var(--bg-card);
  color: var(--text-main);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  min-width: 220px;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.model-select:hover {
  border-color: var(--primary-color);
}

.model-select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-focus);
}

.refresh-btn {
  padding: var(--spacing-3);
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  color: var(--text-inverse);
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px) rotate(180deg);
  box-shadow: var(--shadow-primary-lg);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 1.1rem;
}

.btn-text {
  font-weight: var(--font-semibold);
}

/* 聊天主区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--bg-card);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-5);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-5);
  background: linear-gradient(180deg, var(--bg-muted) 0%, var(--bg-card) 100%);
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: var(--gray-300);
  border-radius: var(--radius-full);
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: var(--gray-400);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-16) var(--spacing-6);
  text-align: center;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-5);
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}

.empty-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  margin: 0 0 var(--spacing-3) 0;
  color: var(--text-main);
}

.empty-description {
  font-size: var(--text-base);
  margin: 0;
  line-height: var(--leading-relaxed);
  max-width: 400px;
  color: var(--text-secondary);
}

/* 消息样式 */
.message {
  display: flex;
  gap: var(--spacing-4);
  max-width: 85%;
  animation: messageSlideIn 0.4s var(--ease-out) forwards;
  opacity: 0;
  transform: translateY(16px);
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.assistant {
  align-self: flex-start;
}

/* 头像 */
.message-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-2);
  flex-shrink: 0;
}

.avatar-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  box-shadow: var(--shadow-md);
  transition: all var(--transition-normal);
}

.message.user .avatar-icon {
  background: var(--gradient-primary);
  color: var(--text-inverse);
  border-color: var(--primary-color);
}

.message.assistant .avatar-icon {
  background: var(--gradient-success);
  color: var(--text-inverse);
  border-color: var(--success-color);
}

.avatar-badge {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  background: var(--bg-card);
  padding: var(--spacing-1) var(--spacing-2);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
}

@keyframes messageSlideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 消息内容 */
.message-content {
  flex: 1;
  background: var(--bg-card);
  padding: var(--spacing-4) var(--spacing-5);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  max-width: 100%;
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.message.user .message-content {
  background: var(--gradient-primary);
  color: var(--text-inverse);
  box-shadow: var(--shadow-primary);
  border: none;
}

.message.assistant .message-content {
  background: var(--bg-card);
  color: var(--text-main);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-3);
  padding-bottom: var(--spacing-2);
  border-bottom: 1px solid var(--border-light);
}

.message.user .message-header {
  border-bottom-color: rgba(255, 255, 255, 0.2);
}

.message-role-text {
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.message.user .message-role-text {
  color: rgba(255, 255, 255, 0.9);
}

.message-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-weight: var(--font-medium);
}

.message.user .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.message-text {
  line-height: var(--leading-relaxed);
  word-wrap: break-word;
  font-size: var(--text-sm);
}

.message-text :deep(p) {
  margin: 0 0 var(--spacing-3) 0;
}

.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.08);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 0.9em;
}

.message.user .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.message-text :deep(pre) {
  background: var(--gray-800);
  color: var(--gray-100);
  padding: var(--spacing-4);
  border-radius: var(--radius-lg);
  overflow-x: auto;
  margin: var(--spacing-3) 0;
}

/* 消息操作按钮 */
.message-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-3);
  padding-top: var(--spacing-2);
  border-top: 1px solid var(--border-light);
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-3);
  background: var(--bg-muted);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  cursor: pointer;
  transition: all var(--transition-fast);
  min-width: 72px;
  justify-content: center;
}

.copy-btn:hover {
  background: var(--primary-lighter);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-1px);
}

.copy-btn:active {
  transform: translateY(0);
}

.copy-icon {
  font-size: 0.9rem;
}

.copy-text {
  font-size: var(--text-xs);
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: var(--spacing-2);
  align-items: center;
  padding: var(--spacing-2) 0;
}

.typing-indicator .dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--gradient-success);
  animation: typingDot 1.4s infinite ease-in-out;
}

.typing-indicator .dot:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator .dot:nth-child(2) { animation-delay: -0.16s; }

.typing-message .message-content {
  background: var(--bg-muted);
  border: 2px dashed var(--border-color);
}

@keyframes typingDot {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 输入区域 */
.input-container {
  padding: var(--spacing-4) var(--spacing-5);
  background: var(--bg-card);
  border-top: 1px solid var(--border-light);
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-3);
}

.input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--spacing-3);
}

.input-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  color: var(--text-main);
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
}

.label-icon {
  font-size: 1.1rem;
}

.input-hint {
  color: var(--text-muted);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.message-input {
  flex: 1;
  padding: var(--spacing-4);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-xl);
  resize: vertical;
  min-height: 80px;
  height: 100px;
  font-family: inherit;
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
  color: var(--text-main);
  background: var(--bg-card);
  transition: all var(--transition-normal);
}

.message-input:hover {
  border-color: var(--gray-300);
}

.message-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-focus);
}

.message-input::placeholder {
  color: var(--text-placeholder);
}

.input-footer {
  display: flex;
  justify-content: flex-end;
}

.send-btn {
  padding: var(--spacing-3) var(--spacing-6);
  background: var(--gradient-primary);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  box-shadow: var(--shadow-primary);
  min-width: 130px;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-primary-lg);
}

.send-btn:active:not(:disabled) {
  transform: translateY(0);
}

.send-btn:disabled {
  background: var(--gray-300);
  cursor: not-allowed;
  box-shadow: none;
}

/* 操作按钮 */
.input-actions {
  display: flex;
  gap: var(--spacing-3);
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  padding: var(--spacing-2) var(--spacing-4);
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: var(--bg-card);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  color: var(--text-secondary);
}

.action-btn:hover {
  transform: translateY(-2px);
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--primary-lighter);
}

.clear-btn:hover {
  border-color: var(--danger-color);
  color: var(--danger-color);
  background: var(--danger-light);
}

.export-btn:hover {
  border-color: var(--success-color);
  color: var(--success-color);
  background: var(--success-light);
}

/* 浮动动画 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 响应式 */
@media (max-width: 768px) {
  .ai-chat-container {
    padding: var(--spacing-3);
  }

  .top-nav {
    padding: var(--spacing-3);
  }

  .nav-title {
    font-size: var(--text-lg);
  }

  .model-select {
    min-width: 160px;
    font-size: var(--text-xs);
    padding: var(--spacing-2) var(--spacing-3);
  }

  .message {
    max-width: 95%;
    gap: var(--spacing-3);
  }

  .avatar-icon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .avatar-badge {
    display: none;
  }

  .message-content {
    padding: var(--spacing-3) var(--spacing-4);
  }

  .input-container {
    padding: var(--spacing-3);
  }

  .input-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-2);
  }

  .message-input {
    padding: var(--spacing-3);
    min-height: 70px;
    height: 80px;
  }

  .send-btn {
    min-width: 100px;
    padding: var(--spacing-3) var(--spacing-4);
  }

  .action-btn {
    padding: var(--spacing-2) var(--spacing-3);
    font-size: var(--text-xs);
  }
}
</style>
