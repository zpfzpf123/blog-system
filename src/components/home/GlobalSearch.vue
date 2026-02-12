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
  
  // 保存首页滚动位置，以便返回时恢复
  const mainContent = document.querySelector('.main-container') as HTMLElement
  if (mainContent) {
    localStorage.setItem('homeScrollPosition', mainContent.scrollTop.toString())
    // 保存当前页码等状态（从 localStorage 读取当前值或使用默认值）
    const currentPage = localStorage.getItem('homeCurrentPage') || '1'
    const pageSize = localStorage.getItem('homePageSize') || '12'
    const categoryIds = localStorage.getItem('homeSelectedCategoryIds') || '[]'
    const tagIds = localStorage.getItem('homeSelectedTagIds') || '[]'
    
    // 如果这些值不存在，设置默认值
    if (!localStorage.getItem('homeCurrentPage')) {
      localStorage.setItem('homeCurrentPage', currentPage)
    }
    if (!localStorage.getItem('homePageSize')) {
      localStorage.setItem('homePageSize', pageSize)
    }
    if (!localStorage.getItem('homeSelectedCategoryIds')) {
      localStorage.setItem('homeSelectedCategoryIds', categoryIds)
    }
    if (!localStorage.getItem('homeSelectedTagIds')) {
      localStorage.setItem('homeSelectedTagIds', tagIds)
    }
    localStorage.setItem('lastViewedPostId', item.id.toString())
  }
  
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
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(12px) saturate(180%);
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 100px;
  animation: overlayFadeIn 0.25s var(--ease-out);
}

@keyframes overlayFadeIn {
  from { 
    opacity: 0;
    backdrop-filter: blur(0);
  }
  to { 
    opacity: 1;
    backdrop-filter: blur(12px) saturate(180%);
  }
}

.global-search-panel {
  width: min(800px, 92vw);
  max-height: 70vh;
  background: var(--bg-glass-strong);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-2xl), 0 0 80px rgba(99, 102, 241, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: panelSlideDown 0.35s var(--ease-spring);
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(24px) saturate(180%);
}

@keyframes panelSlideDown {
  from { 
    transform: translateY(-30px) scale(0.95); 
    opacity: 0; 
  }
  to { 
    transform: translateY(0) scale(1); 
    opacity: 1; 
  }
}

.search-header {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-4);
  padding: var(--spacing-5);
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(248, 250, 255, 0.6) 100%);
  position: relative;
}

.search-header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 10%;
  right: 10%;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(99, 102, 241, 0.3) 50%, transparent 100%);
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
  background: linear-gradient(180deg, var(--gray-50) 0%, var(--bg-card) 100%);
}

.search-status {
  padding: var(--spacing-12);
  text-align: center;
  color: var(--text-muted);
  font-size: var(--text-sm);
}

.search-results-header {
  padding: var(--spacing-4) var(--spacing-5);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 255, 0.8) 100%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.results-summary {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  font-size: var(--text-sm);
}

.results-tips {
  font-size: var(--text-xs);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
}

.result-list {
  list-style: none;
  padding: var(--spacing-4);
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.result-item {
  padding: var(--spacing-4);
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  border: 1.5px solid rgba(99, 102, 241, 0.08);
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.result-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.05), transparent);
  transition: left 0.4s ease;
}

.result-item:hover::before {
  left: 100%;
}

.result-item:hover, .result-item.is-selected {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-primary);
  transform: translateY(-2px) scale(1.01);
}

.result-item.is-selected {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(139, 92, 246, 0.05) 100%);
}

.result-title {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-2);
  font-size: var(--text-base);
}

.result-snippet {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-snippet :deep(mark) {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.3) 0%, rgba(251, 191, 36, 0.25) 100%);
  color: var(--warning-hover);
  padding: var(--spacing-0) var(--spacing-1);
  border-radius: var(--radius-sm);
  font-weight: var(--font-semibold);
}

.result-meta {
  margin-top: var(--spacing-3);
  font-size: var(--text-xs);
  color: var(--text-muted);
  display: flex;
  justify-content: flex-end;
}

.search-footer {
  padding: var(--spacing-3) var(--spacing-5);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(248, 250, 255, 0.6) 100%);
  border-top: 1px solid rgba(99, 102, 241, 0.08);
  font-size: var(--text-xs);
  color: var(--text-muted);
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
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: var(--radius-full);
}
.search-body::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, var(--primary-hover) 0%, var(--secondary-hover) 100%);
}
</style>
