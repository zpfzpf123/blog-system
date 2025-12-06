<script setup lang="ts">
import { ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Document, CollectionTag } from '@element-plus/icons-vue'
import axios from '@/utils/axios'

const router = useRouter()

// 接口定义
interface SearchResultItem {
  id: number
  title: string
  snippetHtml: string
  matchedField: 'title' | 'desc' | 'content'
}

// 状态
const isVisible = ref(false)
const searchQuery = ref('')
const searchResults = ref<SearchResultItem[]>([])
const searchLoading = ref(false)
const searchError = ref('')
const selectedResultIndex = ref(-1)
let searchTimer: NodeJS.Timeout

// 工具函数
const escapeHtml = (s: string) =>
  s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const stripMarkdown = (md: string) => {
  return md
    .replace(/```[\s\S]*?```/g, '')
    .replace(/`[^`]*`/g, '')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[#>*_`~-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

const buildSnippetHtml = (text: string, query: string, radius = 60) => {
  if (!text || !query) return ''
  const lowerText = text.toLowerCase()
  const lowerQ = query.toLowerCase()
  const idx = lowerText.indexOf(lowerQ)
  if (idx === -1) return escapeHtml(text.slice(0, radius * 2))
  const start = Math.max(0, idx - radius)
  const end = Math.min(text.length, idx + query.length + radius)
  const prefix = escapeHtml((start > 0 ? '…' : '') + text.slice(start, idx))
  const match = `<mark>${escapeHtml(text.slice(idx, idx + query.length))}</mark>`
  const suffix = escapeHtml(text.slice(idx + query.length, end) + (end < text.length ? '…' : ''))
  return prefix + match + suffix
}

// 核心逻辑
const open = () => {
  isVisible.value = true
  searchError.value = ''
  searchResults.value = []
  selectedResultIndex.value = -1
  nextTick(() => {
    const input = document.querySelector('#global-search-input') as HTMLInputElement | null
    input?.focus()
    input?.select()
  })
  document.body.style.overflow = 'hidden'
}

const close = () => {
  isVisible.value = false
  searchQuery.value = ''
  searchResults.value = []
  searchLoading.value = false
  searchError.value = ''
  selectedResultIndex.value = -1
  document.body.style.overflow = ''
}

const performSearch = async () => {
  const q = searchQuery.value.trim()
  if (!q) {
    searchResults.value = []
    return
  }
  try {
    searchLoading.value = true
    searchError.value = ''
    const { data } = await axios.get('/api/posts', {
      params: { page: 1, limit: 50, search: q },
    })
    const posts = (data?.posts || []) as Array<any>
    const results: SearchResultItem[] = []
    posts.forEach((p) => {
      const title: string = p.title || ''
      const desc: string = p.desc || ''
      const content: string = p.content || ''
      const plain = stripMarkdown(content)
      const lowerQ = q.toLowerCase()
      
      if (title.toLowerCase().includes(lowerQ)) {
        results.push({
          id: p.id,
          title,
          snippetHtml: buildSnippetHtml(title, q, 80),
          matchedField: 'title',
        })
        return
      }
      if (desc.toLowerCase().includes(lowerQ)) {
        results.push({
          id: p.id,
          title,
          snippetHtml: buildSnippetHtml(desc, q),
          matchedField: 'desc',
        })
        return
      }
      if (plain.toLowerCase().includes(lowerQ)) {
        results.push({
          id: p.id,
          title,
          snippetHtml: buildSnippetHtml(plain, q),
          matchedField: 'content',
        })
      }
    })
    searchResults.value = results
  } catch (err: any) {
    searchError.value = err?.message || '搜索失败'
  } finally {
    searchLoading.value = false
  }
}

const handleResultClick = (item: SearchResultItem) => {
  const currentQuery = searchQuery.value
  close()
  router.push(`/blog/${item.id}?q=${encodeURIComponent(currentQuery)}`)
}

const scrollToSelectedResult = () => {
  nextTick(() => {
    const selectedElement = document.querySelector('.result-item.is-selected') as HTMLElement
    const searchBody = document.querySelector('.search-body') as HTMLElement
    
    if (selectedElement && searchBody) {
      const containerRect = searchBody.getBoundingClientRect()
      const elementRect = selectedElement.getBoundingClientRect()
      const elementTop = selectedElement.offsetTop
      const containerScrollTop = searchBody.scrollTop
      
      // 计算元素相对于容器的位置
      const elementRelativeTop = elementTop - containerScrollTop
      const elementRelativeBottom = elementRelativeTop + selectedElement.offsetHeight
      
      // 如果元素在可视区域外，滚动到合适位置
      if (elementRelativeTop < 60) {
        // 元素在顶部被遮挡，向上滚动
        searchBody.scrollTop = elementTop - 80
      } else if (elementRelativeBottom > containerRect.height) {
        // 元素在底部被遮挡，向下滚动
        searchBody.scrollTop = elementTop - containerRect.height + selectedElement.offsetHeight + 80
      }
    }
  })
}

const handleKeydown = (e: KeyboardEvent) => {
  if (!isVisible.value) return

  switch (e.key) {
    case 'ArrowUp':
      e.preventDefault()
      if (searchResults.value.length > 0) {
        selectedResultIndex.value =
          selectedResultIndex.value <= 0
            ? searchResults.value.length - 1
            : selectedResultIndex.value - 1
        scrollToSelectedResult()
      }
      break
    case 'ArrowDown':
      e.preventDefault()
      if (searchResults.value.length > 0) {
        selectedResultIndex.value =
          selectedResultIndex.value >= searchResults.value.length - 1
            ? 0
            : selectedResultIndex.value + 1
        scrollToSelectedResult()
      }
      break
    case 'Enter':
      e.preventDefault()
      if (
        selectedResultIndex.value >= 0 &&
        selectedResultIndex.value < searchResults.value.length
      ) {
        // 有选中项，跳转到选中的结果
        handleResultClick(searchResults.value[selectedResultIndex.value])
      } else if (searchResults.value.length > 0) {
        // 没有选中项但有搜索结果，跳转到第一个结果
        handleResultClick(searchResults.value[0])
      } else if (searchQuery.value.trim()) {
        // 没有搜索结果，执行搜索
        performSearch()
      }
      break
    case 'Escape':
      e.preventDefault()
      close()
      break
  }
}

// Watchers
watch(searchQuery, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    performSearch()
  }, 250)
})

watch(searchResults, (newResults) => {
  // 当有搜索结果时，自动选中第一项，方便键盘导航
  if (newResults.length > 0) {
    selectedResultIndex.value = 0
    scrollToSelectedResult()
  } else {
    selectedResultIndex.value = -1
  }
})

// Lifecycle
let globalKeydownHandler: ((e: KeyboardEvent) => void) | null = null

onMounted(() => {
  globalKeydownHandler = (e: KeyboardEvent) => {
    const isModifier = e.ctrlKey || e.metaKey
    if (isModifier && (e.key === 'f' || e.key === 'F')) {
      e.preventDefault()
      open()
    }
  }
  window.addEventListener('keydown', globalKeydownHandler)
})

onUnmounted(() => {
  if (globalKeydownHandler) {
    window.removeEventListener('keydown', globalKeydownHandler)
  }
  document.body.style.overflow = ''
})

defineExpose({
  open,
  close
})
</script>

<template>
  <div v-if="isVisible" class="global-search-overlay" @click.self="close">
    <div class="global-search-panel" @keydown.stop="handleKeydown">
      <div class="search-header">
        <div class="search-input-container">
          <el-input
            id="global-search-input"
            v-model="searchQuery"
            placeholder="全文搜索（输入关键词后回车或稍候自动搜索）"
            clearable
            @keyup.enter="performSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <div v-if="searchQuery" class="search-status-indicator">
            <div v-if="searchLoading" class="status-loading">
              <el-icon class="loading-icon"><Search /></el-icon>
              <span>正在搜索...</span>
              <div class="loading-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
            <div v-else-if="searchError" class="status-error">
              <el-icon class="error-icon"><Document /></el-icon>
              <span>搜索出错: {{ searchError }}</span>
            </div>
          </div>
        </div>
        <el-button class="close-btn" circle @click="close">✕</el-button>
      </div>
      
      <div class="search-body">
        <div v-if="searchLoading" class="search-status">正在搜索...</div>
        <div v-else-if="searchError" class="search-status error">{{ searchError }}</div>
        <div v-else-if="!searchQuery" class="search-status">输入关键词开始搜索</div>
        <div v-else-if="searchResults.length === 0" class="search-status">未找到相关内容</div>
        <div v-else>
          <div class="search-results-header">
            <div class="results-summary">
              <el-icon class="results-icon"><Document /></el-icon>
              <span class="results-count">找到 {{ searchResults.length }} 个结果</span>
              <el-tag
                v-if="searchResults.length > 0"
                type="success"
                size="small"
                effect="light"
                class="results-badge"
              >
                {{ searchResults.length }}
              </el-tag>
            </div>
            <div class="results-tips">
              <el-icon><CollectionTag /></el-icon>
              <span>点击结果项可跳转到对应文章</span>
            </div>
          </div>
          <ul class="result-list">
            <li
              v-for="(item, index) in searchResults"
              :key="item.id + '-' + item.matchedField"
              class="result-item"
              :class="{ 'is-selected': index === selectedResultIndex }"
              @click="handleResultClick(item)"
            >
              <div class="result-title">{{ item.title }}</div>
              <div class="result-snippet" v-html="item.snippetHtml"></div>
              <div class="result-meta">
                匹配于：{{
                  item.matchedField === 'title'
                    ? '标题'
                    : item.matchedField === 'desc'
                      ? '摘要'
                      : '正文'
                }}
              </div>
            </li>
          </ul>
        </div>
        <div class="search-footer">按 Esc 关闭 • ↑↓ 选择 • Enter 跳转</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.global-search-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 80px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.global-search-panel {
  width: min(800px, 92vw);
  max-height: 70vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideDown 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideDown {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.search-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f0f2f5;
  background: #fff;
}

.search-input-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-status-indicator {
  margin-top: 4px;
}

.status-loading, .status-error {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 6px;
}

.status-loading {
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
}

.status-error {
  color: #e53e3e;
  background: rgba(229, 62, 62, 0.08);
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-dots {
  display: flex;
  gap: 3px;
}

.loading-dots span {
  width: 4px;
  height: 4px;
  background: currentColor;
  border-radius: 50%;
  animation: pulse 1.4s infinite both;
}

.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes pulse {
  0%, 100% { transform: scale(0.5); opacity: 0.5; }
  50% { transform: scale(1); opacity: 1; }
}

.search-body {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background: #f9fafb;
}

.search-status {
  padding: 40px;
  text-align: center;
  color: #9ca3af;
}

.search-results-header {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
}

.results-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #374151;
}

.results-tips {
  font-size: 12px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 4px;
}

.result-list {
  list-style: none;
  padding: 12px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-item {
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
}

.result-item:hover, .result-item.is-selected {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.result-item.is-selected {
  background: #f0f4ff;
}

.result-title {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.result-snippet {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-snippet :deep(mark) {
  background: #fef3c7;
  color: #92400e;
  padding: 0 2px;
  border-radius: 2px;
}

.result-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #9ca3af;
  display: flex;
  justify-content: flex-end;
}

.search-footer {
  padding: 8px 16px;
  background: #fff;
  border-top: 1px solid #f0f2f5;
  font-size: 12px;
  color: #9ca3af;
  text-align: right;
}

/* Scrollbar */
.search-body::-webkit-scrollbar {
  width: 6px;
}
.search-body::-webkit-scrollbar-track {
  background: transparent;
}
.search-body::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}
.search-body::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
