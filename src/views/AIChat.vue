<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
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
const messagesContainer = ref<HTMLElement | null>(null)
const inputTextarea = ref<HTMLTextAreaElement | null>(null)
const copyStatus = ref<Record<number, string>>({})

const quickPrompts = [
  '请帮我总结这篇文章的核心观点，并输出 3 条可执行建议。',
  '我会给你一段代码，请指出潜在 bug 和性能问题。',
  '把下面内容改写成更有逻辑的博客段落，语气专业但易懂。',
  '基于这个需求给我一个 Vue3 + TS 的实现方案和目录结构。',
]

const messageCount = computed(() => messages.value.length)
const userMessageCount = computed(() => messages.value.filter((m) => m.role === 'user').length)
const aiMessageCount = computed(() => messages.value.filter((m) => m.role === 'assistant').length)
const canSend = computed(() => userInput.value.trim().length > 0 && selectedModel.value && !isLoading.value)

const fetchModels = async () => {
  try {
    const response = await fetch(`${OLLAMA_BASE_URL}/api/tags`)
    if (!response.ok) {
      throw new Error('无法连接到 Ollama 服务')
    }

    const data = await response.json()
    availableModels.value = data.models || []

    if (!selectedModel.value && availableModels.value.length > 0) {
      const preferred = availableModels.value.find((model) => model.name.includes('deepseek-r1:8b'))
      selectedModel.value = preferred?.name || availableModels.value[0].name
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
    availableModels.value = []
  }
}

const removeThinkTags = (content: string): string => {
  return content.replace(/<think>[\s\S]*?<\/think>/gi, '').trim()
}

const scrollToBottom = () => {
  if (!messagesContainer.value) return
  messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
}

const resizeTextarea = () => {
  if (!inputTextarea.value) return

  const textarea = inputTextarea.value
  textarea.style.height = 'auto'
  const nextHeight = Math.min(Math.max(textarea.scrollHeight, 56), 220)
  textarea.style.height = `${nextHeight}px`
}

const focusTextarea = () => {
  inputTextarea.value?.focus()
}

const sendMessage = async () => {
  if (!canSend.value) return

  const prompt = userInput.value.trim()
  const userMessage: Message = {
    role: 'user',
    content: prompt,
    timestamp: new Date(),
  }

  messages.value.push(userMessage)
  userInput.value = ''
  isLoading.value = true
  await nextTick()
  resizeTextarea()

  try {
    const response = await fetch(`${OLLAMA_BASE_URL}/api/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        model: selectedModel.value,
        prompt,
        stream: false,
      }),
    })

    if (!response.ok) {
      throw new Error('AI 服务响应错误')
    }

    const data = await response.json()

    const assistantMessage: Message = {
      role: 'assistant',
      content: removeThinkTags(data.response || '抱歉，我没有得到有效的回复。'),
      timestamp: new Date(),
    }

    messages.value.push(assistantMessage)
  } catch (error) {
    console.error('发送消息失败:', error)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，发生了错误。请检查 Ollama 服务是否正常运行，然后重试。',
      timestamp: new Date(),
    })
  } finally {
    isLoading.value = false
    await nextTick()
    scrollToBottom()
    resizeTextarea()
  }
}

const insertLineBreak = () => {
  if (!inputTextarea.value) {
    userInput.value += '\n'
    return
  }

  const textarea = inputTextarea.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const value = userInput.value

  userInput.value = `${value.slice(0, start)}\n${value.slice(end)}`

  nextTick(() => {
    if (!inputTextarea.value) return
    const cursor = start + 1
    inputTextarea.value.selectionStart = cursor
    inputTextarea.value.selectionEnd = cursor
    resizeTextarea()
  })
}

const applyPrompt = (prompt: string) => {
  userInput.value = prompt
  nextTick(() => {
    focusTextarea()
    resizeTextarea()
  })
}

const formatMessage = (content: string) => {
  try {
    return marked.parse(content) as string
  } catch {
    return content.replace(/\n/g, '<br>')
  }
}

const formatTime = (timestamp: Date) => {
  return timestamp.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

const onModelChange = () => {
  // keep for future side effects (e.g. warmup model)
}

const refreshModels = async () => {
  await fetchModels()
}

const clearChat = () => {
  if (messages.value.length === 0) return
  if (confirm('确定要清空当前对话吗？')) {
    messages.value = []
  }
}

const exportChat = () => {
  if (messages.value.length === 0) return

  const chatContent = messages.value
    .map((msg) => `${msg.role === 'user' ? '用户' : 'AI'} [${formatTime(msg.timestamp)}]\n${msg.content}`)
    .join('\n\n------------------------------\n\n')

  const blob = new Blob([chatContent], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `AI对话_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const copyMessage = async (content: string, key: number) => {
  try {
    const plainText = content.replace(/<[^>]*>/g, '')

    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(plainText)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = plainText
      textarea.style.position = 'fixed'
      textarea.style.left = '-999999px'
      textarea.style.top = '-999999px'
      document.body.appendChild(textarea)
      textarea.focus()
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }

    copyStatus.value[key] = '已复制'
  } catch (error) {
    console.error('复制失败:', error)
    copyStatus.value[key] = '复制失败'
  }

  setTimeout(() => {
    copyStatus.value[key] = ''
  }, 1800)
}

watch(messages, async () => {
  await nextTick()
  scrollToBottom()
})

watch(userInput, async () => {
  await nextTick()
  resizeTextarea()
})

onMounted(async () => {
  await fetchModels()
  await nextTick()
  resizeTextarea()
})
</script>

<template>
  <div class="ai-chat-page">
    <div class="bg-orb orb-left"></div>
    <div class="bg-orb orb-right"></div>
    <div class="bg-grid"></div>

    <div class="ai-chat-layout">
      <aside class="control-panel card-surface">
        <header class="panel-header">
          <p class="eyebrow">AI Chat Studio</p>
          <h1 class="panel-title">本地 AI 对话工作台</h1>
          <p class="panel-subtitle">连接 Ollama 模型，完成问答、写作和代码协作。</p>
        </header>

        <section class="panel-block">
          <label class="block-label" for="model-select">当前模型</label>
          <select
            id="model-select"
            v-model="selectedModel"
            :disabled="isLoading"
            class="model-select"
            @change="onModelChange"
          >
            <option value="">请选择模型...</option>
            <option v-for="model in availableModels" :key="model.digest" :value="model.name">
              {{ model.name }}
            </option>
          </select>
        </section>

        <section class="panel-block stats-grid">
          <article class="stat-card">
            <span class="stat-label">总消息</span>
            <strong class="stat-value">{{ messageCount }}</strong>
          </article>
          <article class="stat-card">
            <span class="stat-label">用户</span>
            <strong class="stat-value">{{ userMessageCount }}</strong>
          </article>
          <article class="stat-card">
            <span class="stat-label">AI</span>
            <strong class="stat-value">{{ aiMessageCount }}</strong>
          </article>
        </section>

        <section class="panel-block">
          <p class="block-label">快捷动作</p>
          <div class="action-row">
            <button class="tool-btn" :disabled="isLoading" @click="refreshModels">刷新模型</button>
            <button class="tool-btn" :disabled="messages.length === 0" @click="clearChat">清空对话</button>
            <button class="tool-btn" :disabled="messages.length === 0" @click="exportChat">导出文本</button>
          </div>
        </section>

        <section class="panel-block">
          <p class="block-label">提示词模板</p>
          <div class="prompt-list">
            <button
              v-for="prompt in quickPrompts"
              :key="prompt"
              class="prompt-item"
              @click="applyPrompt(prompt)"
            >
              {{ prompt }}
            </button>
          </div>
        </section>
      </aside>

      <section class="chat-stage card-surface">
        <header class="stage-header">
          <div>
            <h2 class="stage-title">对话区</h2>
            <p class="stage-subtitle">
              {{ selectedModel ? `已连接 ${selectedModel}` : '请先在左侧选择模型' }}
            </p>
          </div>
          <span class="status-pill" :class="{ offline: !selectedModel }">
            <span class="status-dot"></span>
            {{ selectedModel ? 'Ready' : 'Offline' }}
          </span>
        </header>

        <div ref="messagesContainer" class="chat-scroll">
          <div v-if="messages.length === 0" class="empty-view">
            <div class="empty-badge">AI</div>
            <h3>开始一段高质量对话</h3>
            <p>左侧选择模型后，在下方输入框输入问题即可开始。</p>
          </div>

          <article
            v-for="(message, index) in messages"
            :key="`${message.role}-${message.timestamp.getTime()}-${index}`"
            class="chat-card"
            :class="message.role"
          >
            <header class="chat-card-header">
              <div class="avatar">{{ message.role === 'user' ? '你' : 'AI' }}</div>
              <div class="meta">
                <strong>{{ message.role === 'user' ? '你' : 'AI 助手' }}</strong>
                <time>{{ formatTime(message.timestamp) }}</time>
              </div>
            </header>

            <div class="chat-card-body" v-html="formatMessage(message.content)"></div>

            <footer v-if="message.role === 'assistant'" class="chat-card-footer">
              <button class="copy-btn" @click="copyMessage(message.content, message.timestamp.getTime())">
                {{ copyStatus[message.timestamp.getTime()] || '复制内容' }}
              </button>
            </footer>
          </article>

          <article v-if="isLoading" class="chat-card assistant loading-card">
            <header class="chat-card-header">
              <div class="avatar">AI</div>
              <div class="meta">
                <strong>AI 助手</strong>
                <time>思考中...</time>
              </div>
            </header>
            <div class="typing-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </article>
        </div>

        <footer class="composer">
          <div class="composer-box">
            <textarea
              ref="inputTextarea"
              v-model="userInput"
              :disabled="isLoading || !selectedModel"
              placeholder="输入你的问题...（Enter 发送，Ctrl+Enter 换行）"
              rows="1"
              @input="resizeTextarea"
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.ctrl.enter.prevent="insertLineBreak"
            ></textarea>
            <button class="send-btn" :disabled="!canSend" @click="sendMessage">
              发送
            </button>
          </div>
          <p class="composer-hint" :class="{ warn: !selectedModel }">
            {{ !selectedModel ? '请先选择模型' : isLoading ? 'AI 正在生成内容...' : 'Enter 发送 · Ctrl+Enter 换行' }}
          </p>
        </footer>
      </section>
    </div>
  </div>
</template>

<style scoped>
.ai-chat-page {
  --bg-page: #f7f9fc;
  --bg-surface: #ffffff;
  --bg-soft: #f1f6ff;
  --line: #d9e5f3;
  --line-strong: #c7d9ee;
  --text-title: #0f1b3d;
  --text-main: #334155;
  --text-muted: #66768e;
  --accent: #2563eb;
  --accent-2: #f59e0b;
  --success: #16a34a;
  --shadow-soft: 0 20px 50px rgba(37, 99, 235, 0.09);
  --radius-xl: 24px;
  --radius-lg: 16px;
  --radius-md: 12px;
  --font-display: 'Fraunces', 'Source Han Serif SC', 'STZhongsong', serif;
  --font-body: 'Outfit', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  position: relative;
  overflow: hidden;
  min-height: calc(100vh - 64px);
  padding: 24px 0 28px;
  background: var(--bg-page);
  color: var(--text-main);
  font-family: var(--font-body);
}

.bg-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(54px);
  pointer-events: none;
}

.orb-left {
  width: 320px;
  height: 320px;
  left: -120px;
  top: -80px;
  background: rgba(59, 130, 246, 0.3);
}

.orb-right {
  width: 280px;
  height: 280px;
  right: -80px;
  top: 12%;
  background: rgba(245, 158, 11, 0.25);
}

.bg-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.09) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.09) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: radial-gradient(ellipse at center, black 0%, transparent 78%);
}

.ai-chat-layout {
  position: relative;
  z-index: 2;
  width: min(100%, 1880px);
  margin: 0 auto;
  padding: 0 clamp(12px, 1.25vw, 24px);
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: 18px;
  height: calc(100vh - 120px);
}

.card-surface {
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(10px);
}

.control-panel {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
}

.panel-header {
  display: grid;
  gap: 7px;
}

.eyebrow {
  margin: 0;
  display: inline-flex;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: #1843aa;
  background: #e4eeff;
  font-weight: 700;
  text-transform: uppercase;
}

.panel-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 1.72rem;
  line-height: 1.2;
  letter-spacing: -0.01em;
  color: var(--text-title);
}

.panel-subtitle {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.55;
  font-size: 13px;
}

.panel-block {
  display: grid;
  gap: 10px;
  padding: 13px;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: linear-gradient(145deg, #ffffff 0%, #f7fbff 100%);
}

.block-label {
  margin: 0;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.model-select {
  width: 100%;
  height: 40px;
  border-radius: var(--radius-md);
  border: 1px solid var(--line);
  background: #ffffff;
  color: var(--text-main);
  font-size: 14px;
  padding: 0 12px;
  transition: all 0.2s ease;
}

.model-select:focus {
  outline: none;
  border-color: rgba(37, 99, 235, 0.5);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.14);
}

.stats-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #deebfa;
  background: #f8fbff;
  padding: 10px 8px;
  display: grid;
  gap: 4px;
  justify-items: center;
}

.stat-label {
  font-size: 11px;
  color: var(--text-muted);
}

.stat-value {
  font-size: 20px;
  line-height: 1;
  color: var(--text-title);
}

.action-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.tool-btn {
  height: 36px;
  border-radius: 10px;
  border: 1px solid var(--line);
  background: #ffffff;
  color: var(--text-main);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tool-btn:hover:not(:disabled) {
  border-color: rgba(37, 99, 235, 0.35);
  color: #1e40af;
  transform: translateY(-1px);
}

.tool-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.prompt-list {
  display: grid;
  gap: 8px;
}

.prompt-item {
  text-align: left;
  border: 1px solid #dfeafb;
  border-radius: 11px;
  background: #f8fbff;
  color: #354968;
  font-size: 12px;
  line-height: 1.5;
  padding: 9px 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.prompt-item:hover {
  border-color: rgba(37, 99, 235, 0.35);
  background: #eef5ff;
}

.chat-stage {
  padding: 16px;
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 12px;
  min-height: 0;
}

.stage-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 2px 4px;
}

.stage-title {
  margin: 0;
  color: var(--text-title);
  font-size: 1.25rem;
  font-family: var(--font-display);
}

.stage-subtitle {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 12px;
  border-radius: 999px;
  border: 1px solid #cef0d8;
  background: #ecfdf3;
  color: #0f8c42;
  font-size: 12px;
  font-weight: 700;
}

.status-pill.offline {
  border-color: #f3dbc5;
  background: #fff7ed;
  color: #b45309;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: currentColor;
}

.chat-scroll {
  min-height: 0;
  overflow-y: auto;
  border: 1px solid var(--line-strong);
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-scroll::-webkit-scrollbar {
  width: 8px;
}

.chat-scroll::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #c8d6ea;
}

.empty-view {
  min-height: 240px;
  border: 1px dashed #d6e2f4;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.85);
  display: grid;
  place-items: center;
  text-align: center;
  gap: 8px;
  padding: 18px;
}

.empty-badge {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: #ffffff;
  background: linear-gradient(130deg, #2563eb 0%, #1d4ed8 100%);
}

.empty-view h3 {
  margin: 0;
  color: var(--text-title);
  font-size: 20px;
}

.empty-view p {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
}

.chat-card {
  max-width: min(82%, 760px);
  border-radius: 16px;
  border: 1px solid #dce6f3;
  background: #ffffff;
  padding: 12px;
  display: grid;
  gap: 10px;
  animation: riseIn 0.28s ease;
}

.chat-card.user {
  margin-left: auto;
  border-color: rgba(37, 99, 235, 0.25);
  background: linear-gradient(135deg, #2f6ff0 0%, #2563eb 100%);
  color: #ffffff;
}

.chat-card.assistant {
  margin-right: auto;
}

.chat-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  background: #edf4ff;
  color: #1d4ed8;
}

.chat-card.user .avatar {
  background: rgba(255, 255, 255, 0.22);
  color: #ffffff;
}

.meta {
  display: grid;
  gap: 2px;
}

.meta strong {
  font-size: 13px;
  line-height: 1;
}

.meta time {
  font-size: 11px;
  color: var(--text-muted);
}

.chat-card.user .meta time {
  color: rgba(255, 255, 255, 0.82);
}

.chat-card-body {
  font-size: 14px;
  line-height: 1.72;
  color: var(--text-main);
  word-break: break-word;
}

.chat-card.user .chat-card-body {
  color: #ffffff;
}

.chat-card-body :deep(p) {
  margin: 0 0 12px;
}

.chat-card-body :deep(p:last-child) {
  margin-bottom: 0;
}

.chat-card-body :deep(code) {
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  font-size: 12px;
  border-radius: 8px;
  padding: 2px 6px;
  background: #eef3fc;
}

.chat-card.user .chat-card-body :deep(code) {
  background: rgba(255, 255, 255, 0.22);
}

.chat-card-body :deep(pre) {
  margin: 12px 0;
  border-radius: 12px;
  padding: 12px;
  background: #0b1220;
  color: #e2e8f0;
  overflow-x: auto;
}

.chat-card-body :deep(pre code) {
  background: transparent;
  padding: 0;
}

.chat-card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px dashed #dce6f4;
}

.copy-btn {
  height: 30px;
  border-radius: 9px;
  padding: 0 10px;
  border: 1px solid #dbe7f7;
  background: #f7faff;
  color: #3f556f;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  border-color: rgba(37, 99, 235, 0.32);
  color: #1e40af;
}

.loading-card {
  border-style: dashed;
}

.typing-dots {
  display: flex;
  gap: 6px;
  padding: 2px 0 4px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #2563eb;
  animation: typing 1.2s infinite ease-in-out;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.3s;
}

.composer {
  border: 1px solid var(--line-strong);
  border-radius: 16px;
  background: #ffffff;
  padding: 12px;
  display: grid;
  gap: 8px;
}

.composer-box {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.composer-box textarea {
  flex: 1;
  min-height: 56px;
  max-height: 220px;
  resize: none;
  border-radius: 12px;
  border: 1px solid #dbe7f7;
  background: #f7faff;
  padding: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-main);
  font-family: inherit;
  transition: all 0.2s ease;
}

.composer-box textarea:focus {
  outline: none;
  border-color: rgba(37, 99, 235, 0.5);
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.14);
}

.composer-box textarea:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.send-btn {
  width: 84px;
  height: 42px;
  border: none;
  border-radius: 11px;
  background: linear-gradient(130deg, #2563eb 0%, #1d4ed8 100%);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.25);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.composer-hint {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

.composer-hint.warn {
  color: #b45309;
  font-weight: 600;
}

@keyframes typing {
  0%,
  80%,
  100% {
    transform: scale(0.85);
    opacity: 0.45;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes riseIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1160px) {
  .ai-chat-layout {
    grid-template-columns: 1fr;
    height: auto;
    min-height: calc(100vh - 120px);
  }

  .control-panel {
    max-height: none;
  }

  .chat-stage {
    min-height: 620px;
  }
}

@media (max-width: 760px) {
  .ai-chat-page {
    padding-top: 14px;
  }

  .ai-chat-layout {
    gap: 12px;
  }

  .control-panel,
  .chat-stage {
    padding: 12px;
  }

  .panel-title {
    font-size: 1.45rem;
  }

  .stats-grid,
  .action-row {
    grid-template-columns: 1fr;
  }

  .chat-card {
    max-width: 92%;
  }

  .composer-box {
    flex-direction: column;
    align-items: stretch;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
