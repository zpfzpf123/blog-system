<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElPagination, ElEmpty, ElSkeleton, ElInput, ElButton, ElIcon, ElTooltip } from 'element-plus'
import { Search, DataLine, RefreshLeft, Timer, Top, Grid, List, Delete, Edit, Download, CopyDocument, Setting } from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { copyEnhancedRichContent } from '@/utils/imageUtils'

// Components
import GlobalSearch from '@/components/home/GlobalSearch.vue'
import BlogCard from '@/components/home/BlogCard.vue'
import HomeSidebar from '@/components/home/HomeSidebar.vue'
import BackgroundEffects from '@/components/home/BackgroundEffects.vue'
import ThemeSettings from '@/components/ThemeSettings.vue'

// Types
interface Category {
  id: number
  name: string
}

interface Tag {
  id: number
  name: string
}

interface BlogPost {
  id: number
  title: string
  desc: string
  author: string
  createdAt: string
  category: Category | null
  tags: Tag[]
}

// State
const router = useRouter()
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const blogPosts = ref<BlogPost[]>([])
const loading = ref(true)
const isRestoring = ref(false)
const showBackToTop = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// Filter State
const searchKeyword = ref('')
const selectedCategoryIds = ref<number[]>([])
const selectedTagIds = ref<number[]>([])

// Refs
const globalSearchRef = ref<InstanceType<typeof GlobalSearch> | null>(null)
const themeSettingsRef = ref<InstanceType<typeof ThemeSettings> | null>(null)

const openThemeSettings = () => {
  themeSettingsRef.value?.open()
}

// Computed for Hero Section
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息'
  if (hour < 11) return '早上好，开启新的一天'
  if (hour < 14) return '中午好，午休时间到了'
  if (hour < 18) return '下午好，继续加油'
  return '晚上好，享受静谧时光'
})

const currentDate = computed(() => {
  const date = new Date()
  return date.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })
})

// API Actions
const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/categories')
    categories.value = response.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchTags = async () => {
  try {
    const response = await axios.get('/api/tags')
    tags.value = response.data
  } catch (error) {
    console.error('获取标签失败:', error)
  }
}

const fetchBlogPosts = async (page = 1) => {
  try {
    loading.value = true
    const params: any = {
      page: page,
      limit: pageSize.value,
      sortBy: 'id',           // 改为按 ID 排序
      sortOrder: 'desc'       // 降序（ID大的在前面）
    }

    if (selectedCategoryIds.value?.length > 0) {
      params.categoryIds = selectedCategoryIds.value
    }

    if (selectedTagIds.value?.length > 0) {
      params.tagIds = selectedTagIds.value
    }

    if (searchKeyword.value?.trim()) {
      params.search = searchKeyword.value.trim()
    }

    const response = await axios.get('/api/posts', {
      params,
      paramsSerializer: {
        indexes: null,
      },
    })

    blogPosts.value = response.data.posts
    total.value = response.data.pagination.totalPosts
    currentPage.value = response.data.pagination.currentPage
  } catch (error) {
    console.error('获取博客文章失败:', error)
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

// Event Handlers
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  showBackToTop.value = target.scrollTop > 300
}

const scrollToTop = () => {
  const mainContent = document.querySelector('.main-scroll-area') as HTMLElement
  if (mainContent) {
    mainContent.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const handlePageChange = (page: number) => {
  fetchBlogPosts(page)
  const mainContent = document.querySelector('.main-scroll-area') as HTMLElement
  if (mainContent) {
    mainContent.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchBlogPosts(1)
}

const handleSearch = () => {
  fetchBlogPosts(1)
}

const handleReset = () => {
  searchKeyword.value = ''
  selectedCategoryIds.value = []
  selectedTagIds.value = []
  fetchBlogPosts(1)
}

const goToCreateBlog = () => {
  router.push('/admin/posts/create?from=home')
}

const goToBlogDetail = (id: number) => {
  const mainContent = document.querySelector('.main-scroll-area') as HTMLElement
  if (mainContent) {
    const scrollPosition = mainContent.scrollTop
    localStorage.setItem('homeScrollPosition', scrollPosition.toString())
    localStorage.setItem('homeCurrentPage', currentPage.value.toString())
    localStorage.setItem('homePageSize', pageSize.value.toString())
    localStorage.setItem('homeSelectedCategoryIds', JSON.stringify(selectedCategoryIds.value))
    localStorage.setItem('homeSelectedTagIds', JSON.stringify(selectedTagIds.value))
  }
  router.push(`/blog/${id}`)
}

const handleEdit = (post: BlogPost) => {
  router.push(`/admin/posts/edit/${post.id}?from=home`)
}

const handleDelete = (post: BlogPost) => {
  ElMessageBox.confirm(`确定要删除《${post.title}》吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await axios.delete(`/api/posts/${post.id}`)
      ElMessage.success('删除成功')
      fetchBlogPosts(currentPage.value)
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleExport = (post: BlogPost) => {
  let content = `# ${post.title}\n\n`
  content += `**作者**: ${post.author}\n`
  content += `**时间**: ${new Date(post.createdAt).toLocaleDateString()}\n`
  if (post.category) content += `**分类**: ${post.category.name}\n`
  if (post.tags?.length > 0)
    content += `**标签**: ${post.tags.map((t) => t.name).join(', ')}\n`
  content += `\n---\n\n`
  if (post.desc) content += `${post.desc}\n\n`
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${post.title.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_')}.md`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleCopy = async (post: BlogPost) => {
  let content = ''
  if (post.desc) content += post.desc + '\n\n'
  try {
    const { data } = await axios.get(`/api/posts/${post.id}`)
    if (data?.content) content += data.content
    const ok = await copyEnhancedRichContent(content)
    if (ok) {
      ElMessage.success('全文已复制到剪贴板')
    } else {
      ElMessage.error('复制失败')
    }
  } catch {
    ElMessage.error('复制失败')
  }
}

const openGlobalSearch = () => {
  globalSearchRef.value?.open()
}

// Watchers
let searchTimer: NodeJS.Timeout
watch(searchKeyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 500)
})

watch(
  [selectedCategoryIds, selectedTagIds],
  () => {
    if (isRestoring.value) return
    handleSearch()
  },
  { deep: true }
)

// 全局快捷键处理函数
const handleGlobalKeydown = (e: KeyboardEvent) => {
  // Ctrl+K 或 Cmd+K 打开全局搜索
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault() // 阻止浏览器默认行为
    openGlobalSearch()
  }
}

// Lifecycle
onMounted(async () => {
  const savedPage = localStorage.getItem('homeCurrentPage')
  const savedScrollPosition = localStorage.getItem('homeScrollPosition')
  const savedPageSize = localStorage.getItem('homePageSize')
  const savedCategoryIds = localStorage.getItem('homeSelectedCategoryIds')
  const savedTagIds = localStorage.getItem('homeSelectedTagIds')
  
  if (savedPage && savedScrollPosition) {
    isRestoring.value = true
    currentPage.value = parseInt(savedPage)
    if (savedPageSize) pageSize.value = parseInt(savedPageSize)

    if (savedCategoryIds) {
      try {
        selectedCategoryIds.value = JSON.parse(savedCategoryIds)
      } catch (e) {
        console.error('恢复分类筛选失败', e)
      }
    }
    
    if (savedTagIds) {
      try {
        selectedTagIds.value = JSON.parse(savedTagIds)
      } catch (e) {
        console.error('恢复标签筛选失败', e)
      }
    }
    
    await Promise.all([fetchCategories(), fetchTags()])
    await fetchBlogPosts(currentPage.value)
    
    setTimeout(() => {
      const mainContent = document.querySelector('.main-scroll-area') as HTMLElement
      if (mainContent) {
        mainContent.scrollTop = parseInt(savedScrollPosition)
      }
      setTimeout(() => {
        isRestoring.value = false
      }, 200)
      
      localStorage.removeItem('homeCurrentPage')
      localStorage.removeItem('homeScrollPosition')
      localStorage.removeItem('homePageSize')
      localStorage.removeItem('homeSelectedCategoryIds')
      localStorage.removeItem('homeSelectedTagIds')
    }, 300)
  } else {
    fetchCategories()
    fetchTags()
    fetchBlogPosts()
  }
  
  // 添加全局键盘事件监听
  window.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<template>
  <div class="home-layout">
    <BackgroundEffects />
    
    <!-- 恢复状态Loading遮罩 -->
    <transition name="fade">
      <div v-if="isRestoring" class="restoring-overlay">
        <div class="restoring-spinner">
          <div class="spinner-ring"></div>
          <div class="spinner-text">正在恢复浏览位置...</div>
        </div>
      </div>
    </transition>
    
    <!-- 左侧侧边栏 -->
    <aside class="sidebar-area">
      <HomeSidebar
        v-model:selectedCategoryIds="selectedCategoryIds"
        v-model:selectedTagIds="selectedTagIds"
        :categories="categories"
        :tags="tags"
        @create="goToCreateBlog"
        @reset="handleReset"
      />
    </aside>

    <!-- 右侧主内容区 -->
    <main class="main-scroll-area" @scroll="handleScroll">
      <div class="content-wrapper">
        <!-- 顶部 Hero 区域 (全新设计) -->
        <header class="hero-header">
          <div class="hero-content glass-card-hero">
            <!-- 简化的装饰 -->
            <div class="hero-decoration-simple"></div>
            
            <div class="hero-inner">
              <!-- 左侧：问候语区域 -->
              <div class="greeting-section">
                <div class="greeting-date">
                   <el-icon class="mr-1"><Timer /></el-icon>
                   {{ currentDate }}
                </div>
                <h1 class="greeting-title">{{ greeting }}</h1>
                <p class="greeting-subtitle">记录代码，记录生活，记录成长的每一个瞬间。</p>
              </div>
              
              <!-- 右侧：统计数据 -->
              <div class="hero-stats-grid">
                <div class="stat-card">
                  <div class="stat-icon">📝</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ total }}</div>
                    <div class="stat-name">文章总数</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">📚</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ categories.length }}</div>
                    <div class="stat-name">专栏分类</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">🏷️</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ tags.length }}</div>
                    <div class="stat-name">标签数量</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </header>

        <!-- 工具栏 & 搜索 (悬浮式) -->
        <div class="toolbar-section">
          <div class="search-bar glass-panel">
            <!-- 搜索栏光泽效果 -->
            <div class="search-glow"></div>
            
            <el-icon class="search-icon-main"><Search /></el-icon>
            <input 
              v-model="searchKeyword" 
              type="text" 
              placeholder="🔍 搜索文章标题、内容或标签..." 
              class="clean-input"
            >
            <div class="toolbar-actions">
              <el-tooltip content="全局搜索 (Ctrl+K)" placement="top">
                <button class="icon-btn" @click="openGlobalSearch">
                  <el-icon><DataLine /></el-icon>
                </button>
              </el-tooltip>
              <div class="divider-v"></div>
              <el-tooltip content="阅读主题设置" placement="top">
                <button class="icon-btn theme-btn" @click="openThemeSettings">
                  <el-icon><Setting /></el-icon>
                </button>
              </el-tooltip>
              <div class="divider-v"></div>
              <el-tooltip content="重置筛选" placement="top">
                 <button class="icon-btn" @click="handleReset">
                  <el-icon><RefreshLeft /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </div>
        </div>

        <!-- 文章列表区域 -->
        <div class="posts-container">
          <transition-group name="list" tag="div" class="posts-grid" v-if="!loading && blogPosts.length > 0">
            <BlogCard
              v-for="(post, index) in blogPosts"
              :key="post.id"
              :post="post"
              class="post-card-item"
              :style="{ '--delay': `${index * 0.05}s` }"
              @click="goToBlogDetail"
              @edit="handleEdit"
              @delete="handleDelete"
              @export="handleExport"
              @copy="handleCopy"
            />
          </transition-group>

          <!-- 骨架屏 -->
          <div v-else-if="loading" class="posts-grid">
            <div v-for="n in 6" :key="n" class="skeleton-card glass-panel">
              <el-skeleton animated>
                <template #template>
                  <el-skeleton-item variant="image" class="skeleton-img" />
                  <div class="skeleton-content">
                    <el-skeleton-item variant="h3" style="width: 60%; margin-bottom: 12px" />
                    <el-skeleton-item variant="text" style="width: 90%" />
                    <el-skeleton-item variant="text" style="width: 40%; margin-top: 8px" />
                  </div>
                </template>
              </el-skeleton>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-state glass-panel">
            <div class="empty-icon-box">
              <el-icon><List /></el-icon>
            </div>
            <p class="empty-text">暂无相关文章</p>
            <el-button type="primary" round class="create-first-btn" @click="goToCreateBlog">
              写第一篇文章
            </el-button>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-section" v-if="total > 0">
          <div class="pagination-wrapper glass-panel">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[12, 24, 36]"
              :background="true"
              layout="prev, pager, next"
              :total="total"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </div>
    </main>

    <GlobalSearch ref="globalSearchRef" />
    <ThemeSettings ref="themeSettingsRef" />
    
    <!-- 回到顶部 -->
    <transition name="fade">
      <button 
        v-if="showBackToTop" 
        class="back-to-top-btn glass-panel"
        @click="scrollToTop"
      >
        <el-icon><Top /></el-icon>
      </button>
    </transition>
  </div>
</template>

<style scoped>
/* 基础布局 */
.home-layout {
  display: flex;
  height: calc(100vh - 64px);
  width: 100%;
  overflow: hidden;
  gap: var(--spacing-4);
  max-width: 1800px;
  margin: 0 auto;
  box-sizing: border-box;
  font-family: var(--font-sans);
  padding: 0 var(--spacing-4);
}

.sidebar-area {
  width: 280px;
  flex-shrink: 0;
  height: 100%;
  padding-top: var(--spacing-4);
}

.main-scroll-area {
  flex: 1;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: var(--spacing-2);
  padding-top: var(--spacing-4);
}

/* 滚动条样式 */
.main-scroll-area::-webkit-scrollbar { width: 6px; }
.main-scroll-area::-webkit-scrollbar-track { background: transparent; }
.main-scroll-area::-webkit-scrollbar-thumb { 
  background-color: var(--gray-300); 
  border-radius: var(--radius-full); 
}
.main-scroll-area::-webkit-scrollbar-thumb:hover { 
  background-color: var(--gray-400); 
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4);
  padding-bottom: var(--spacing-10);
}

/* 通用面板样式 */
.glass-panel {
  background: var(--bg-glass);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: var(--shadow-glass);
  border-radius: var(--radius-2xl);
  transition: all 0.4s var(--ease-out);
  position: relative;
  overflow: hidden;
}

/* Hero Section */
.hero-header {
  width: 100%;
  margin-bottom: var(--spacing-1);
}

.glass-card-hero {
  background: var(--bg-glass-strong);
  backdrop-filter: blur(32px) saturate(180%);
  border: 1.5px solid rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-lg), inset 0 1px 0 rgba(255, 255, 255, 1);
  border-radius: var(--radius-xl);
  padding: var(--spacing-5) var(--spacing-6);
  position: relative;
  overflow: hidden;
  animation: heroFadeIn 0.6s var(--ease-out);
}

@keyframes heroFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-inner {
  position: relative;
  z-index: 2;
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-8);
}

.greeting-section {
  flex: 1;
  min-width: 0;
}

.greeting-date {
  display: inline-flex;
  align-items: center;
  font-size: var(--text-xs);
  color: var(--primary-color);
  font-weight: var(--font-semibold);
  background: var(--primary-lighter);
  padding: var(--spacing-1) var(--spacing-3);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-2);
  border: 1px solid rgba(99, 102, 241, 0.15);
  animation: dateSlideIn 0.5s var(--ease-out) 0.1s backwards;
}

@keyframes dateSlideIn {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.mr-1 { margin-right: var(--spacing-1); }

.greeting-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-extrabold);
  color: var(--text-main);
  letter-spacing: -0.02em;
  margin: 0 0 var(--spacing-2) 0;
  line-height: var(--leading-tight);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: titleSlideIn 0.5s var(--ease-out) 0.2s backwards;
}

@keyframes titleSlideIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.greeting-subtitle {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
  font-weight: var(--font-medium);
  animation: subtitleFadeIn 0.5s var(--ease-out) 0.3s backwards;
}

@keyframes subtitleFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 统计网格 */
.hero-stats-grid {
  display: flex;
  flex-direction: row;
  gap: 12px;
  flex-shrink: 0;
  justify-content: center;
  align-items: center;
  animation: statsSlideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.5s backwards;
}

@keyframes statsSlideIn {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.7) 0%, rgba(249, 250, 255, 0.6) 100%);
  border-radius: 14px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
  position: relative;
  overflow: hidden;
  min-width: 85px;
  text-align: center;
}

.stat-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(139, 92, 246, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.35s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(252, 253, 255, 0.9) 100%);
  border-color: rgba(99, 102, 241, 0.25);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.15);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-icon {
  font-size: 1.5rem;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.stat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 900;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.stat-name {
  font-size: 0.7rem;
  color: #64748b;
  font-weight: 600;
  letter-spacing: 0.3px;
  line-height: 1.2;
  white-space: nowrap;
}

/* Hero 简化装饰 */
.hero-decoration-simple {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 140px;
  height: 140px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.06) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
  animation: decorationPulse 4s ease-in-out infinite;
}

@keyframes decorationPulse {
  0%, 100% { 
    transform: scale(1);
    opacity: 0.6;
  }
  50% { 
    transform: scale(1.1);
    opacity: 0.8;
  }
}

/* Toolbar */
.toolbar-section {
  position: sticky;
  top: 0;
  z-index: 20;
  margin-top: 4px;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  gap: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.85) 0%, 
    rgba(250, 251, 255, 0.85) 100%);
  position: relative;
}

.search-glow {
  position: absolute;
  inset: 0;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.search-bar:focus-within {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.98) 0%, 
    rgba(252, 253, 255, 0.98) 100%);

  border-color: rgba(99, 102, 241, 0.4);
}

.search-bar:focus-within .search-glow {
  opacity: 1;
}

.search-icon-main {
  font-size: 1.5rem;
  color: #94a3b8;
  transition: all 0.3s ease;
}

.search-bar:focus-within .search-icon-main {
  color: #6366f1;
}

.clean-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 1.05rem;
  color: #334155;
  outline: none;
  padding: 14px 0;
  font-weight: 500;
  letter-spacing: 0.01em;
}

.clean-input::placeholder {
  color: #94a3b8;
  font-weight: 400;
}

.search-bar:focus-within .clean-input::placeholder {
  color: #cbd5e1;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-btn {
  background: transparent;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 10px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.icon-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.1) 100%);
  border-radius: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.icon-btn:hover {
  color: #6366f1;
  transform: translateY(-2px) scale(1.05);
}

.icon-btn:hover::before {
  opacity: 1;
}

.icon-btn:active {
  transform: translateY(0) scale(0.95);
}

.divider-v {
  width: 1px;
  height: 20px;
  background: #e2e8f0;
  margin: 0 4px;
}

.theme-btn:hover {
  color: #8b5cf6 !important;
}

.theme-btn:hover::before {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.15) 0%, rgba(168, 85, 247, 0.1) 100%) !important;
}

/* Grid */
.posts-container {
  min-height: 400px;
  margin-top: 8px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 28px;
}

.post-card-item {
  opacity: 0;
  animation: slideUp 0.7s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: var(--delay);
}

@keyframes slideUp {
  from { 
    opacity: 0; 
    transform: translateY(40px) scale(0.95);
  }
  to { 
    opacity: 1; 
    transform: translateY(0) scale(1);
  }
}

/* Skeleton */
.skeleton-card {
  height: 380px;
  overflow: hidden;
  animation: skeletonPulse 1.5s ease-in-out infinite;
}

@keyframes skeletonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.skeleton-img {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, var(--gray-100) 25%, var(--gray-200) 50%, var(--gray-100) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.skeleton-content {
  padding: var(--spacing-6);
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-20) 0;
  min-height: 400px;
  animation: emptyFadeIn 0.6s var(--ease-out);
}

@keyframes emptyFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.empty-icon-box {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, var(--primary-lighter) 0%, var(--secondary-light) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: var(--primary-color);
  margin-bottom: var(--spacing-6);
  box-shadow: var(--shadow-primary);
  animation: emptyIconFloat 3s ease-in-out infinite;
}

@keyframes emptyIconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.empty-text {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-8);
  font-weight: var(--font-medium);
}

.create-first-btn {
  padding: var(--spacing-4) var(--spacing-10);
  font-weight: var(--font-bold);
  font-size: var(--text-base);
  background: var(--gradient-primary);
  border: none;
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.create-first-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--gradient-shimmer);
  transition: left 0.5s ease;
}

.create-first-btn:hover::before {
  left: 100%;
}

.create-first-btn:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: var(--shadow-primary-lg);
}

/* Pagination */
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-6);
  animation: paginationFadeIn 0.5s var(--ease-out) 0.3s backwards;
}

@keyframes paginationFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pagination-wrapper {
  padding: var(--spacing-3) var(--spacing-8);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(250, 251, 255, 0.9) 100%);
}

.pagination-wrapper :deep(.el-pagination) {
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: var(--primary-color);
}

.pagination-wrapper :deep(.el-pagination .btn-prev),
.pagination-wrapper :deep(.el-pagination .btn-next) {
  background: transparent;
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
}

.pagination-wrapper :deep(.el-pagination .btn-prev:hover),
.pagination-wrapper :deep(.el-pagination .btn-next:hover) {
  background: var(--primary-lighter);
  color: var(--primary-color);
  transform: scale(1.1);
}

.pagination-wrapper :deep(.el-pager li) {
  border-radius: var(--radius-lg);
  font-weight: var(--font-semibold);
  transition: all var(--transition-normal);
}

.pagination-wrapper :deep(.el-pager li:hover) {
  transform: translateY(-2px);
}

.pagination-wrapper :deep(.el-pager li.is-active) {
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary);
}

/* Back to Top */
.back-to-top-btn {
  position: fixed;
  bottom: 40px;
  right: 40px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 2px solid rgba(99, 102, 241, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #6366f1;
  font-size: 1.6rem;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 50;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(250, 251, 255, 0.95) 100%);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.15),
              inset 0 1px 0 rgba(255, 255, 255, 1);
  animation: backToTopFadeIn 0.4s ease;
}

@keyframes backToTopFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.back-to-top-btn:hover {
  transform: translateY(-8px) scale(1.1) rotate(5deg);
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 16px 40px rgba(99, 102, 241, 0.35),
              0 8px 20px rgba(139, 92, 246, 0.2);
}

.back-to-top-btn:active {
  transform: translateY(-4px) scale(1.05);
}

/* Restoring Overlay */
.restoring-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.spinner-ring {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 1s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

.spinner-text {
  margin-top: 20px;
  color: #64748b;
  font-weight: 600;
  font-size: 1.1rem;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* Responsive */
@media (max-width: 1024px) {
  .sidebar-area { display: none; }
  .home-layout { padding: 0 16px; }
  
  .glass-card-hero {
    padding: 24px 20px;
  }
  
  .hero-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .greeting-section {
    width: 100%;
  }
  
  .hero-stats-grid {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .stat-card {
    flex: 1;
    min-width: calc(33.333% - 7px);
  }
  
  .hero-decoration-simple { display: none; }
  
  .posts-grid {
    grid-template-columns: 1fr;
  }
}
</style>