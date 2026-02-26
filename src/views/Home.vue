<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElPagination, ElSkeleton, ElIcon, ElTooltip } from 'element-plus'
import {
  Search,
  RefreshLeft,
  Top,
  Plus,
  Folder,
  CollectionTag,
  Setting,
  Monitor,
  Document,
  CopyDocument,
  Delete,
  Edit,
  ArrowRight,
  Timer,
  Check,
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { copyEnhancedRichContent } from '@/utils/imageUtils'
import { createHomeStateSnapshot, parseHomeStateSnapshot } from '@/views/homeState'

// Components
import GlobalSearch from '@/components/home/GlobalSearch.vue'
import ThemeSettings from '@/components/ThemeSettings.vue'
import CategoryTagManager from '@/components/home/CategoryTagManager.vue'

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

interface PostQueryParams {
  page: number
  limit: number
  sortBy: 'id'
  sortOrder: 'desc'
  categoryIds?: number[]
  tagIds?: number[]
  search?: string
}

defineOptions({
  name: 'HomePage',
})

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
const lastViewedPostId = ref<number | null>(null)

// Refs
const globalSearchRef = ref<InstanceType<typeof GlobalSearch> | null>(null)
const themeSettingsRef = ref<InstanceType<typeof ThemeSettings> | null>(null)
const showCategoryTagManager = ref(false)
const mainContentRef = ref<HTMLElement | null>(null)

// Actions
const openThemeSettings = () => themeSettingsRef.value?.open()
const openCategoryTagManager = () => (showCategoryTagManager.value = true)
const openGlobalSearch = () => globalSearchRef.value?.open()

const handleManagerRefresh = () => {
  fetchCategories()
  fetchTags()
  fetchBlogPosts(currentPage.value)
}

// Computed
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 5) return '夜深了，注意休息'
  if (hour < 9) return '早上好，开启美好一天'
  if (hour < 12) return '上午好，保持专注'
  if (hour < 14) return '中午好，记得午休'
  if (hour < 18) return '下午好，继续加油'
  return '晚上好，享受宁静时光'
})

const hasActiveFilters = computed(() => {
  return (
    searchKeyword.value.trim() !== '' ||
    selectedCategoryIds.value.length > 0 ||
    selectedTagIds.value.length > 0
  )
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
    const params: PostQueryParams = {
      page: page,
      limit: pageSize.value,
      sortBy: 'id',
      sortOrder: 'desc',
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
      paramsSerializer: { indexes: null },
    })

    blogPosts.value = response.data.posts
    total.value = response.data.pagination.totalPosts
    currentPage.value = response.data.pagination.currentPage
  } catch (error) {
    console.error('获取博客文章失败:', error)
    ElMessage.error('获取文章列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// Event Handlers
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  showBackToTop.value = target.scrollTop > 400
}

const scrollToTop = () => {
  mainContentRef.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

const handlePageChange = (page: number) => {
  fetchBlogPosts(page)
  scrollToTop()
}

const handleSearch = () => fetchBlogPosts(1)

const handleReset = () => {
  searchKeyword.value = ''
  selectedCategoryIds.value = []
  selectedTagIds.value = []
  fetchBlogPosts(1)
}

const goToCreateBlog = () => router.push('/admin/posts/create?from=home')

const goToBlogDetail = (id: number) => {
  if (mainContentRef.value) {
    // 保存状态以便返回时恢复
    const state = createHomeStateSnapshot({
      scrollTop: mainContentRef.value.scrollTop,
      page: currentPage.value,
      pageSize: pageSize.value,
      categoryIds: selectedCategoryIds.value,
      tagIds: selectedTagIds.value,
      searchKeyword: searchKeyword.value,
      lastViewedId: id,
    })
    localStorage.setItem('homeState', JSON.stringify(state))
  }
  router.push(`/blog/${id}`)
}

const handleEdit = (post: BlogPost, e: MouseEvent) => {
  e.stopPropagation()
  router.push(`/admin/posts/edit/${post.id}?from=home`)
}

const handleDelete = (post: BlogPost, e: MouseEvent) => {
  e.stopPropagation()
  ElMessageBox.confirm(`确定要删除文章《${post.title}》吗？此操作不可恢复。`, '删除确认', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
  }).then(async () => {
    try {
      await axios.delete(`/api/posts/${post.id}`)
      ElMessage.success('文章已删除')
      fetchBlogPosts(currentPage.value)
    } catch {
      ElMessage.error('删除失败，请重试')
    }
  })
}

const handleCopy = async (post: BlogPost, e: MouseEvent) => {
  e.stopPropagation()
  try {
    const { data } = await axios.get(`/api/posts/${post.id}`)
    let content = post.desc ? post.desc + '\n\n' : ''
    if (data?.content) content += data.content
    const ok = await copyEnhancedRichContent(content)
    if (ok) ElMessage.success('内容已复制到剪贴板')
    else ElMessage.error('复制失败')
  } catch {
    ElMessage.error('获取内容失败')
  }
}

const toggleCategory = (id: number) => {
  if (selectedCategoryIds.value.includes(id)) {
    selectedCategoryIds.value = selectedCategoryIds.value.filter((cid) => cid !== id)
  } else {
    selectedCategoryIds.value = [...selectedCategoryIds.value, id]
  }
}

const toggleTag = (id: number) => {
  if (selectedTagIds.value.includes(id)) {
    selectedTagIds.value = selectedTagIds.value.filter((tid) => tid !== id)
  } else {
    selectedTagIds.value = [...selectedTagIds.value, id]
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 3天内显示相对时间
  if (diff < 3 * 24 * 60 * 60 * 1000) {
    if (diff < 60 * 1000) return '刚刚'
    if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))}分钟前`
    if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }

  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// Keyboard Shortcuts
const handleGlobalKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    openGlobalSearch()
  }
}

// Watchers
let searchTimer: ReturnType<typeof setTimeout>
watch(searchKeyword, () => {
  if (isRestoring.value) return
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => handleSearch(), 300)
})

watch(
  [selectedCategoryIds, selectedTagIds],
  () => {
    if (!isRestoring.value) handleSearch()
  },
  { deep: true },
)

// Lifecycle
onMounted(async () => {
  const savedStateStr = localStorage.getItem('homeState')
  const savedState = parseHomeStateSnapshot(savedStateStr)

  if (savedState) {
    try {
      isRestoring.value = true

      currentPage.value = savedState.page
      pageSize.value = savedState.pageSize
      searchKeyword.value = savedState.searchKeyword
      selectedCategoryIds.value = savedState.categoryIds
      selectedTagIds.value = savedState.tagIds
      lastViewedPostId.value = savedState.lastViewedId

      await Promise.all([fetchCategories(), fetchTags()])
      await fetchBlogPosts(currentPage.value)

      // Restore scroll position
      setTimeout(() => {
        if (mainContentRef.value) {
          mainContentRef.value.scrollTop = savedState.scrollTop
        }
        setTimeout(() => {
          isRestoring.value = false
        }, 300)
        localStorage.removeItem('homeState')
      }, 100)
    } catch (e) {
      console.error('Failed to restore state', e)
      fetchCategories()
      fetchTags()
      fetchBlogPosts()
    }
  } else {
    fetchCategories()
    fetchTags()
    fetchBlogPosts()
  }

  window.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  clearTimeout(searchTimer)
  window.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<template>
  <div class="home-light">
    <main class="main-scroll" ref="mainContentRef" @scroll="handleScroll">
      <header class="hero">
        <div class="hero-orb hero-orb-left"></div>
        <div class="hero-orb hero-orb-right"></div>
        <div class="hero-pattern"></div>
        <div class="hero-content">
          <div class="hero-copy">
            <span class="hero-greeting">
              <span class="greeting-dot"></span>
              {{ greeting }}
            </span>
            <h1 class="hero-title">
              发现值得反复阅读的
              <span class="hero-title-highlight">文章卡片</span>
            </h1>
            <p class="hero-subtitle">
              用更通透的浅色阅读体验，帮你在更短时间里找到值得收藏和复读的内容。
            </p>
            <div class="hero-command-line" aria-label="主页动态提示">
              <span class="command-dot"></span>
              <span class="command-text">LIVE / 知识流正在加载新的思路片段</span>
              <span class="command-chip">CTRL + K 全局检索</span>
            </div>
          </div>
          <div class="hero-metrics">
            <div class="metric-card">
              <el-icon class="metric-icon"><Document /></el-icon>
              <span class="metric-value">{{ total }}</span>
              <span class="metric-label">总文章</span>
            </div>
            <div class="metric-card">
              <el-icon class="metric-icon"><Folder /></el-icon>
              <span class="metric-value">{{ categories.length }}</span>
              <span class="metric-label">分类</span>
            </div>
            <div class="metric-card">
              <el-icon class="metric-icon"><CollectionTag /></el-icon>
              <span class="metric-value">{{ tags.length }}</span>
              <span class="metric-label">标签</span>
            </div>
          </div>
        </div>
      </header>

      <div class="page-layout">
        <aside class="left-panel">
          <div class="left-panel-sticky">
            <section class="panel action-panel">
              <button class="btn-create" @click="goToCreateBlog">
                <el-icon><Plus /></el-icon>
                写文章
              </button>
              <div class="quick-actions">
                <el-tooltip content="全局搜索 (Ctrl+K)" placement="top">
                  <button class="icon-square" @click="openGlobalSearch" title="全局搜索">
                    <el-icon><Search /></el-icon>
                  </button>
                </el-tooltip>
                <el-tooltip content="分类标签管理" placement="top">
                  <button class="icon-square" @click="openCategoryTagManager" title="分类标签管理">
                    <el-icon><Setting /></el-icon>
                  </button>
                </el-tooltip>
                <el-tooltip content="主题设置" placement="top">
                  <button class="icon-square" @click="openThemeSettings" title="主题设置">
                    <el-icon><Monitor /></el-icon>
                  </button>
                </el-tooltip>
              </div>
            </section>

            <section class="panel search-panel">
              <label class="search-label" for="home-search-input">检索文章</label>
              <div class="search-field">
                <el-icon class="search-field-icon"><Search /></el-icon>
                <input
                  id="home-search-input"
                  v-model="searchKeyword"
                  type="text"
                  class="search-input"
                  placeholder="按标题或内容搜索..."
                />
              </div>
            </section>

            <section v-if="categories.length" class="panel filter-panel">
              <h3 class="panel-title">
                <el-icon><Folder /></el-icon>
                分类筛选
              </h3>
              <div class="category-list">
                <button
                  v-for="cat in categories"
                  :key="cat.id"
                  class="category-chip"
                  :class="{ active: selectedCategoryIds.includes(cat.id) }"
                  @click="toggleCategory(cat.id)"
                >
                  <span>{{ cat.name }}</span>
                  <span v-if="selectedCategoryIds.includes(cat.id)" class="chip-check">
                    <el-icon><Check /></el-icon>
                  </span>
                </button>
              </div>
            </section>

            <section v-if="tags.length" class="panel filter-panel">
              <h3 class="panel-title">
                <el-icon><CollectionTag /></el-icon>
                标签筛选
              </h3>
              <div class="tag-cloud">
                <button
                  v-for="tag in tags"
                  :key="tag.id"
                  class="tag-chip"
                  :class="{ active: selectedTagIds.includes(tag.id) }"
                  @click="toggleTag(tag.id)"
                >
                  #{{ tag.name }}
                </button>
              </div>
            </section>

            <button v-if="hasActiveFilters" class="btn-reset-filters" @click="handleReset">
              <el-icon><RefreshLeft /></el-icon>
              清除筛选
            </button>
          </div>
        </aside>

        <section class="feed">
          <header class="feed-head">
            <div>
              <h2 class="feed-title">精选文章卡片</h2>
              <p class="feed-desc">更突出标题、摘要和标签，帮助你快速浏览并进入正文。</p>
            </div>
            <div class="feed-pills">
              <span class="feed-pill">共 {{ total }} 篇</span>
              <span class="feed-pill muted"
                >{{ categories.length }} 分类 · {{ tags.length }} 标签</span
              >
            </div>
            <div class="feed-head-glow" aria-hidden="true"></div>
          </header>

          <div v-if="hasActiveFilters" class="active-strip">
            <span class="active-result">当前筛选结果：{{ total }} 篇文章</span>
            <div class="active-strip-tags">
              <span v-if="searchKeyword" class="active-pill">
                搜索 "{{ searchKeyword }}"
                <el-icon class="active-pill-close" @click="searchKeyword = ''"><Delete /></el-icon>
              </span>
              <span v-if="selectedCategoryIds.length" class="active-pill">
                分类 {{ selectedCategoryIds.length }}
              </span>
              <span v-if="selectedTagIds.length" class="active-pill">
                标签 {{ selectedTagIds.length }}
              </span>
            </div>
          </div>
          <div v-if="!loading && blogPosts.length > 0" class="posts-grid">
            <article
              v-for="(post, index) in blogPosts"
              :key="post.id"
              class="article-card"
              :class="{ 'is-viewed': lastViewedPostId === post.id }"
              :style="{ '--delay': `${index * 60}ms` }"
              @click="goToBlogDetail(post.id)"
            >
              <span class="card-sheen" aria-hidden="true"></span>
              <div :class="['card-cover', `tone-${(index % 6) + 1}`]">
                <span class="cover-serial">VOL.{{ String(index + 1).padStart(2, '0') }}</span>
                <span class="cover-date">
                  <el-icon><Timer /></el-icon>
                  {{ formatDate(post.createdAt) }}
                </span>
                <span v-if="post.category" class="cover-category">{{ post.category.name }}</span>
              </div>

              <div class="card-body">
                <h3 class="card-title" :title="post.title">{{ post.title }}</h3>
                <p class="card-excerpt">{{ post.desc || '暂无简介，点击查看全文内容。' }}</p>

                <div class="card-tags">
                  <span v-for="tag in post.tags.slice(0, 3)" :key="tag.id" class="card-tag">
                    #{{ tag.name }}
                  </span>
                  <span v-if="post.tags.length === 0" class="card-tag card-tag-empty"
                    >未设置标签</span
                  >
                </div>

                <div class="card-bottom">
                  <span class="author-chip">{{ post.author || '匿名作者' }}</span>
                  <div class="card-actions">
                    <button class="action-btn" title="编辑" @click="handleEdit(post, $event)">
                      <el-icon><Edit /></el-icon>
                    </button>
                    <button class="action-btn" title="复制内容" @click="handleCopy(post, $event)">
                      <el-icon><CopyDocument /></el-icon>
                    </button>
                    <button
                      class="action-btn danger"
                      title="删除"
                      @click="handleDelete(post, $event)"
                    >
                      <el-icon><Delete /></el-icon>
                    </button>
                  </div>
                </div>
              </div>

              <div class="card-read-more">
                阅读详情
                <el-icon><ArrowRight /></el-icon>
              </div>
            </article>
          </div>

          <div v-else-if="loading" class="posts-grid">
            <div v-for="n in 6" :key="n" class="skeleton-card">
              <el-skeleton animated>
                <template #template>
                  <el-skeleton-item
                    variant="image"
                    style="width: 100%; height: 98px; border-radius: 16px"
                  />
                  <div style="margin-top: 16px; display: grid; gap: 10px">
                    <el-skeleton-item variant="h3" style="width: 82%; height: 24px" />
                    <el-skeleton-item variant="text" style="width: 100%; height: 16px" />
                    <el-skeleton-item variant="text" style="width: 88%; height: 16px" />
                    <div
                      style="
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-top: 8px;
                      "
                    >
                      <el-skeleton-item variant="text" style="width: 84px; height: 24px" />
                      <el-skeleton-item variant="circle" style="width: 30px; height: 30px" />
                    </div>
                  </div>
                </template>
              </el-skeleton>
            </div>
          </div>

          <div v-else class="empty-state">
            <div class="empty-icon">
              <el-icon><Document /></el-icon>
            </div>
            <h3 class="empty-title">没有找到相关文章</h3>
            <p class="empty-text">换个关键词试试，或者立即写一篇新文章。</p>
            <button class="btn-empty-reset" @click="handleReset">
              <el-icon><RefreshLeft /></el-icon>
              重置筛选
            </button>
          </div>

          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              :background="true"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </section>
      </div>
    </main>

    <transition name="fade-slide">
      <button v-if="showBackToTop" class="back-top-btn" @click="scrollToTop" title="回到顶部">
        <el-icon><Top /></el-icon>
      </button>
    </transition>

    <GlobalSearch ref="globalSearchRef" />
    <ThemeSettings ref="themeSettingsRef" />
    <CategoryTagManager v-model="showCategoryTagManager" @refresh="handleManagerRefresh" />
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Noto+Serif+SC:wght@500;600;700&display=swap');

.home-light {
  --bg-page: #f4f6fa;
  --bg-surface: #ffffff;
  --bg-surface-soft: #f8fbff;
  --bg-panel: rgba(255, 255, 255, 0.88);
  --text-title: #1c2739;
  --text-main: #34465f;
  --text-secondary: #617592;
  --text-muted: #92a3bc;
  --border-soft: #d8e3f2;
  --border-strong: #c4d4ea;
  --accent: #2a66b7;
  --accent-hover: #1f4f94;
  --accent-soft: #e7f0ff;
  --accent-warm: #d28a3f;
  --accent-cyan: #0f7c96;
  --accent-ink: #13253f;
  --danger: #dc2626;
  --danger-soft: #fee2e2;
  --radius-xl: 24px;
  --radius-lg: 16px;
  --radius-md: 12px;
  --shadow-soft: 0 15px 34px rgba(25, 49, 92, 0.09);
  --shadow-card: 0 20px 42px rgba(21, 44, 84, 0.1);
  --content-max-width: 1880px;
  --content-gutter: clamp(14px, 1.6vw, 30px);
  --font-display: 'Noto Serif SC', 'STZhongsong', 'Source Han Serif SC', serif;
  --font-body: 'Manrope', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  position: relative;
  isolation: isolate;
  height: calc(100vh - 64px);
  overflow: hidden;
  color: var(--text-main);
  background:
    radial-gradient(circle at 8% -10%, rgba(176, 208, 255, 0.5) 0%, transparent 44%),
    radial-gradient(circle at 92% -4%, rgba(255, 220, 186, 0.44) 0%, transparent 42%),
    linear-gradient(180deg, #fbfcff 0%, var(--bg-page) 100%);
  font-family: var(--font-body);
}

.home-light::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 0% 100%, rgba(220, 235, 255, 0.62) 0%, transparent 35%),
    radial-gradient(circle at 100% 100%, rgba(255, 233, 211, 0.52) 0%, transparent 32%);
  z-index: 0;
}

.home-light::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0.42), rgba(255, 255, 255, 0));
  z-index: 0;
}

.main-scroll {
  position: relative;
  z-index: 1;
  height: 100%;
  overflow-y: auto;
  scroll-behavior: smooth;
  padding-bottom: 56px;
}

.hero {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  padding: 22px 0 14px;
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  z-index: -1;
  filter: blur(2px);
  opacity: 0.55;
  animation: orbFloat 14s ease-in-out infinite;
}

.hero-orb-left {
  width: 240px;
  height: 240px;
  left: -92px;
  top: -104px;
  background: radial-gradient(
    circle at 35% 30%,
    rgba(188, 206, 231, 0.78) 0%,
    rgba(188, 206, 231, 0) 72%
  );
}

.hero-orb-right {
  width: 280px;
  height: 280px;
  right: -116px;
  top: -134px;
  background: radial-gradient(
    circle at 45% 35%,
    rgba(223, 197, 163, 0.78) 0%,
    rgba(223, 197, 163, 0) 74%
  );
  animation-delay: -5s;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(131, 151, 182, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(131, 151, 182, 0.1) 1px, transparent 1px);
  background-size: 30px 30px;
  mask-image: linear-gradient(to bottom, black 0%, transparent 85%);
  opacity: 0.22;
  z-index: -1;
}

.hero-content {
  position: relative;
  max-width: var(--content-max-width);
  width: calc(100% - (var(--content-gutter) * 2));
  margin: 0 auto;
  padding: 20px clamp(16px, 1.8vw, 26px);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 22px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.9) 0%, rgba(245, 250, 255, 0.82) 100%);
  border: 1px solid rgba(255, 255, 255, 0.96);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(29, 62, 118, 0.14);
  backdrop-filter: blur(6px);
  transform-style: preserve-3d;
}

.hero-content::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 1px;
  background: linear-gradient(
    110deg,
    rgba(15, 124, 150, 0.42) 0%,
    rgba(42, 102, 183, 0.28) 38%,
    rgba(210, 138, 63, 0.28) 70%,
    rgba(19, 37, 63, 0.4) 100%
  );
  -webkit-mask:
    linear-gradient(#000 0 0) content-box,
    linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.hero-content::after {
  content: '';
  position: absolute;
  inset: -36% -8% auto;
  height: 180px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.68) 0%, rgba(255, 255, 255, 0) 68%);
  pointer-events: none;
}

.hero-copy {
  display: grid;
  gap: 11px;
  max-width: 700px;
}

.hero-greeting {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 13px;
  border-radius: 999px;
  color: var(--accent-hover);
  font-size: 12px;
  font-weight: 700;
  background: rgba(232, 241, 255, 0.92);
  border: 1px solid rgba(42, 102, 183, 0.22);
}

.greeting-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 0 6px rgba(47, 74, 114, 0.12);
  animation: dotPulse 2.4s infinite;
}

.hero-title {
  margin: 0;
  color: var(--text-title);
  font-family: var(--font-display);
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.015em;
  font-size: clamp(1.7rem, 2.8vw, 2.6rem);
}

.hero-title-highlight {
  color: var(--accent-hover);
  position: relative;
  display: inline-block;
  padding-bottom: 2px;
}

.hero-title-highlight::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(42, 102, 183, 0.28), rgba(210, 138, 63, 0.3));
}

.hero-subtitle {
  margin: 0;
  max-width: 580px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

.hero-command-line {
  width: fit-content;
  max-width: 100%;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 2px;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(19, 37, 63, 0.14);
  background: linear-gradient(125deg, rgba(19, 37, 63, 0.9) 0%, rgba(31, 79, 148, 0.9) 100%);
  color: #eaf4ff;
  font-size: 12px;
  letter-spacing: 0.03em;
  box-shadow: 0 12px 24px rgba(19, 37, 63, 0.24);
}

.command-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #67f0c5;
  box-shadow: 0 0 0 5px rgba(103, 240, 197, 0.2);
  animation: commandPulse 1.8s ease-in-out infinite;
}

.command-text {
  position: relative;
  max-width: 320px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.command-text::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: rgba(255, 255, 255, 0.65);
  animation: cursorBlink 1s steps(1) infinite;
}

.command-chip {
  padding: 4px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-weight: 700;
  color: #f6fbff;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(112px, 1fr));
  gap: 10px;
  min-width: 366px;
}

.metric-card {
  position: relative;
  overflow: hidden;
  display: grid;
  justify-items: center;
  gap: 3px;
  padding: 13px 10px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 14px;
  box-shadow: 0 10px 22px rgba(30, 64, 120, 0.14);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.metric-card::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 3px;
  background: linear-gradient(90deg, #2a66b7 0%, #5f95df 100%);
}

.metric-card:nth-child(2)::before {
  background: linear-gradient(90deg, #3577c5 0%, #74afe8 100%);
}

.metric-card:nth-child(3)::before {
  background: linear-gradient(90deg, #bf7b34 0%, #e3ac67 100%);
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(20, 28, 40, 0.12);
}

.metric-icon {
  font-size: 17px;
  color: var(--accent-hover);
  display: inline-flex;
}

.metric-value {
  color: var(--text-title);
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
}

.metric-label {
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.page-layout {
  max-width: var(--content-max-width);
  margin: 22px auto 0;
  padding: 0 var(--content-gutter);
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 22px;
}

.left-panel-sticky {
  position: sticky;
  top: 20px;
  display: grid;
  gap: 14px;
}

.panel {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(246, 251, 255, 0.86) 100%);
  border: 1px solid rgba(255, 255, 255, 0.96);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow-soft);
  padding: 16px;
}

.action-panel {
  display: grid;
  gap: 10px;
}

.btn-create {
  border: 0;
  width: 100%;
  height: 46px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(122deg, #2a66b7 0%, #4b88d1 62%, #d28a3f 130%);
  box-shadow: 0 12px 26px rgba(41, 101, 182, 0.32);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.btn-create:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 34px rgba(41, 101, 182, 0.36);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 9px;
}

.icon-square {
  border: 1px solid #dce7f6;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  color: var(--text-secondary);
  height: 40px;
  border-radius: 11px;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.22s ease;
}

.icon-square:hover {
  color: var(--accent-hover);
  border-color: rgba(42, 102, 183, 0.34);
  box-shadow: 0 8px 18px rgba(42, 102, 183, 0.14);
  transform: translateY(-2px);
}

.search-panel {
  display: grid;
  gap: 10px;
}

.search-label {
  color: var(--text-secondary);
  font-size: 12px;
  letter-spacing: 0.08em;
  font-weight: 700;
  text-transform: uppercase;
}

.search-field {
  position: relative;
}

.search-field-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 42px;
  border-radius: 12px;
  border: 1px solid #d9e6f7;
  background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
  padding: 0 12px 0 36px;
  font-size: 14px;
  color: var(--text-main);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-input:focus {
  outline: none;
  border-color: rgba(42, 102, 183, 0.46);
  box-shadow: 0 0 0 4px rgba(42, 102, 183, 0.12);
}

.panel-title {
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.category-list {
  display: grid;
  gap: 8px;
}

.category-chip {
  width: 100%;
  border: 0;
  padding: 10px 12px;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-main);
  background: rgba(255, 255, 255, 0.86);
  cursor: pointer;
  font-size: 13px;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;
}

.category-chip:hover {
  transform: translateX(1px);
  background: rgba(245, 248, 252, 0.9);
}

.category-chip.active {
  color: var(--accent-hover);
  background: linear-gradient(120deg, rgba(231, 241, 255, 0.95) 0%, rgba(239, 247, 255, 0.92) 100%);
  font-weight: 600;
}

.chip-check {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(42, 102, 183, 0.16);
  color: var(--accent-hover);
  font-weight: 700;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  border: 1px solid transparent;
  border-radius: 999px;
  padding: 6px 11px;
  font-size: 12px;
  background: #eef4ff;
  color: #3f5f8e;
  cursor: pointer;
  transition: all 0.22s ease;
}

.tag-chip:hover {
  background: #e2ecff;
  border-color: rgba(42, 102, 183, 0.18);
  transform: translateY(-1px);
}

.tag-chip.active {
  color: #ffffff;
  background: linear-gradient(120deg, #2a66b7 0%, #4b88d1 100%);
}

.btn-reset-filters {
  border: 1px dashed var(--border-strong);
  background: rgba(255, 255, 255, 0.8);
  color: var(--text-secondary);
  border-radius: 12px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-reset-filters:hover {
  color: var(--accent-hover);
  border-color: rgba(47, 74, 114, 0.34);
  background: rgba(243, 246, 251, 0.85);
}

.feed {
  display: grid;
  gap: 18px;
  align-content: start;
}

.feed-head {
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(132deg, rgba(255, 255, 255, 0.96) 0%, rgba(244, 250, 255, 0.9) 70%);
  border: 1px solid rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 28px rgba(31, 68, 128, 0.12);
}

.feed-head-glow {
  position: absolute;
  width: 360px;
  height: 360px;
  right: -140px;
  top: -220px;
  border-radius: 50%;
  pointer-events: none;
  background:
    radial-gradient(circle at center, rgba(42, 102, 183, 0.25) 0%, rgba(42, 102, 183, 0) 62%),
    radial-gradient(circle at 58% 60%, rgba(210, 138, 63, 0.2) 0%, rgba(210, 138, 63, 0) 70%);
  animation: feedGlowFloat 8s ease-in-out infinite;
}

.feed-title {
  margin: 0;
  color: var(--text-title);
  font-size: 29px;
  line-height: 1.12;
  letter-spacing: -0.015em;
  font-family: var(--font-display);
}

.feed-desc {
  margin: 7px 0 0;
  color: var(--text-secondary);
  font-size: 14px;
  max-width: 640px;
}

.feed-pills {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.feed-pill {
  padding: 7px 12px;
  border-radius: 999px;
  background: linear-gradient(130deg, #e6f0ff 0%, #eff6ff 100%);
  color: var(--accent-hover);
  font-size: 12px;
  font-weight: 700;
  border: 1px solid rgba(42, 102, 183, 0.16);
}

.feed-pill.muted {
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.88);
  border-color: rgba(130, 141, 158, 0.22);
}

.active-strip {
  border: 1px solid #d1e0f6;
  border-radius: 14px;
  padding: 10px 13px;
  background: linear-gradient(120deg, rgba(232, 242, 255, 0.74) 0%, rgba(255, 255, 255, 0.9) 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.active-result {
  font-size: 13px;
  color: #2d4f83;
  font-weight: 700;
}

.active-strip-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.active-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 11px;
  border-radius: 999px;
  background: #ffffff;
  color: var(--text-secondary);
  border: 1px solid #d5e2f4;
  font-size: 12px;
  font-weight: 600;
}

.active-pill-close {
  cursor: pointer;
  color: var(--accent-hover);
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.article-card {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  border: 1px solid rgba(154, 182, 225, 0.36);
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  box-shadow: 0 14px 32px rgba(28, 62, 122, 0.12);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  min-height: 0;
  animation: cardIn 0.55s ease backwards;
  animation-delay: var(--delay, 0ms);
  transition:
    transform 0.28s ease,
    box-shadow 0.28s ease,
    border-color 0.28s ease;
  transform-origin: center;
}

.article-card:hover {
  transform: translateY(-7px) rotateX(1.2deg) rotateY(-1.2deg);
  box-shadow: 0 22px 44px rgba(28, 62, 122, 0.18);
  border-color: rgba(42, 102, 183, 0.42);
}

.article-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background: linear-gradient(
    165deg,
    rgba(108, 153, 218, 0.16) 0%,
    rgba(255, 255, 255, 0) 35%,
    rgba(210, 138, 63, 0.15) 100%
  );
  opacity: 0;
  transition: opacity 0.25s ease;
}

.article-card:hover::before {
  opacity: 1;
}

.card-sheen {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0) 20%,
    rgba(255, 255, 255, 0.44) 48%,
    rgba(255, 255, 255, 0) 72%
  );
  transform: translateX(-130%);
  z-index: 1;
}

.article-card:hover .card-sheen {
  animation: cardSheen 0.75s ease;
}

.article-card.is-viewed {
  border-color: rgba(63, 135, 110, 0.35);
}

.article-card.is-viewed::after {
  content: '最近阅读';
  position: absolute;
  right: 14px;
  top: 14px;
  background: #edf8f2;
  color: #206b52;
  border: 1px solid rgba(32, 107, 82, 0.22);
  border-radius: 999px;
  font-size: 11px;
  padding: 4px 9px;
  z-index: 3;
  font-weight: 700;
}

.card-cover {
  position: relative;
  min-height: 86px;
  padding: 12px 14px 10px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid rgba(198, 217, 246, 0.9);
}

.card-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
}

.card-cover::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 80% 18%, rgba(255, 255, 255, 0.82) 0%, transparent 46%),
    linear-gradient(150deg, transparent 0%, rgba(255, 255, 255, 0.62) 100%);
  z-index: 1;
}

.card-cover > * {
  position: relative;
  z-index: 2;
}

.tone-1::before {
  background: linear-gradient(120deg, #e6efff 0%, #dbe8ff 100%);
}

.tone-2::before {
  background: linear-gradient(120deg, #fff1e3 0%, #ffe6cf 100%);
}

.tone-3::before {
  background: linear-gradient(120deg, #e6f8f1 0%, #dcf0e7 100%);
}

.tone-4::before {
  background: linear-gradient(120deg, #eaf2ff 0%, #e2ebff 100%);
}

.tone-5::before {
  background: linear-gradient(120deg, #ffeef4 0%, #ffe4eb 100%);
}

.tone-6::before {
  background: linear-gradient(120deg, #e8f4ff 0%, #e0efff 100%);
}

.cover-serial {
  justify-self: start;
  align-self: center;
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #3f5579;
  font-weight: 800;
  border-radius: 999px;
  padding: 4px 9px;
  border: 1px solid rgba(255, 255, 255, 0.93);
  background: rgba(255, 255, 255, 0.82);
}

.cover-category {
  justify-self: end;
  align-self: center;
  font-size: 11px;
  padding: 4px 11px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  color: #2f4a72;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 10px rgba(20, 58, 122, 0.08);
}

.cover-date {
  grid-column: 2 / 3;
  justify-self: center;
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #5a6780;
  font-weight: 600;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 15px 15px 12px;
}

.card-title {
  margin: 0;
  color: var(--text-title);
  font-size: clamp(20px, 1.26vw, 23px);
  line-height: 1.32;
  letter-spacing: -0.018em;
  font-family: var(--font-display);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-excerpt {
  margin: 0;
  color: #63728b;
  font-size: 14px;
  line-height: 1.62;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: calc(1.62em * 2);
}

.card-tags {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 7px;
  min-height: 32px;
  max-height: 32px;
  overflow: hidden;
}

.card-tag {
  flex: 0 1 auto;
  max-width: 126px;
  font-size: 11px;
  color: #395985;
  background: #eef4ff;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(57, 89, 133, 0.18);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-tag-empty {
  color: #647f9f;
  background: #f2f6fd;
  border-style: dashed;
}

.card-bottom {
  margin-top: 1px;
  padding-top: 10px;
  border-top: 1px dashed #dfd7cb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.author-chip {
  font-size: 12px;
  color: #4f6889;
  border-radius: 999px;
  padding: 5px 11px;
  background: #f2f6fd;
  border: 1px solid rgba(104, 138, 184, 0.22);
}

.card-actions {
  display: flex;
  gap: 6px;
  opacity: 1;
}

.action-btn {
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 9px;
  display: grid;
  place-items: center;
  background: #edf4ff;
  color: #4f6f95;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  color: #1f4f94;
  background: #e2ecff;
  transform: translateY(-1px);
}

.action-btn.danger:hover {
  color: #be2f24;
  background: #ffeceb;
}

.card-read-more {
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #2459a5;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.01em;
  background: linear-gradient(112deg, rgba(230, 241, 255, 0.86) 0%, rgba(255, 243, 231, 0.8) 100%);
  border-top: 1px solid rgba(198, 217, 246, 0.88);
  transition:
    gap 0.2s ease,
    background 0.2s ease;
}

.article-card:hover .card-read-more {
  gap: 11px;
  background: linear-gradient(112deg, rgba(214, 231, 255, 0.94) 0%, rgba(255, 236, 216, 0.9) 100%);
}

.skeleton-card {
  border-radius: var(--radius-xl);
  padding: 14px;
  background: var(--bg-surface);
  border: 1px solid #e8dfd4;
  box-shadow: var(--shadow-card);
}

.empty-state {
  padding: 70px 16px;
  text-align: center;
  border-radius: var(--radius-xl);
  border: 1px dashed #d5ccc0;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.9) 0%, rgba(251, 246, 239, 0.82) 100%);
}

.empty-icon {
  margin-bottom: 16px;
  font-size: 56px;
  color: #c3b9ab;
}

.empty-title {
  margin: 0;
  color: var(--text-title);
  font-size: 20px;
  font-family: var(--font-display);
}

.empty-text {
  margin: 8px 0 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.btn-empty-reset {
  margin-top: 18px;
  border: 0;
  border-radius: 11px;
  background: linear-gradient(120deg, #2a66b7 0%, #4b88d1 100%);
  color: #ffffff;
  height: 40px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.2s ease;
}

.btn-empty-reset:hover {
  transform: translateY(-1px);
}

.pagination-wrap {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination) {
  --el-pagination-button-bg-color: #ffffff;
  --el-pagination-hover-color: #1f4f94;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 10px;
  border: 1px solid #d9e5f6;
}

:deep(.el-pagination .el-pager li.is-active) {
  border-color: transparent;
  background: linear-gradient(120deg, #2a66b7 0%, #4b88d1 100%);
}

.back-top-btn {
  position: fixed;
  right: 30px;
  bottom: 26px;
  width: 48px;
  height: 48px;
  border: 0;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: var(--accent-hover);
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(42, 102, 183, 0.18);
  box-shadow: 0 14px 30px rgba(27, 63, 121, 0.2);
  cursor: pointer;
  z-index: 100;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease;
}

.back-top-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 36px rgba(27, 63, 121, 0.28);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(16px);
}

@keyframes orbFloat {
  0%,
  100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(8px) scale(1.04);
  }
}

@keyframes dotPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.12);
    opacity: 0.7;
  }
}

@keyframes commandPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.7;
  }
}

@keyframes cursorBlink {
  0%,
  49% {
    opacity: 1;
  }
  50%,
  100% {
    opacity: 0;
  }
}

@keyframes feedGlowFloat {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
  }
  50% {
    transform: translate3d(-14px, 10px, 0);
  }
}

@keyframes cardSheen {
  from {
    transform: translateX(-130%);
  }
  to {
    transform: translateX(130%);
  }
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1320px) {
  .page-layout {
    grid-template-columns: 260px 1fr;
  }

  .hero-metrics {
    min-width: 320px;
  }

  .posts-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1040px) {
  .hero-content {
    flex-direction: column;
    padding: 18px;
  }

  .hero-metrics {
    width: 100%;
    min-width: 0;
  }

  .hero-command-line {
    flex-wrap: wrap;
  }

  .page-layout {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .left-panel-sticky {
    position: static;
  }
}

@media (max-width: 760px) {
  .page-layout {
    padding-left: 14px;
    padding-right: 14px;
  }

  .hero-content {
    width: calc(100% - 28px);
    padding: 16px 14px;
    border-radius: 20px;
  }

  .hero-orb {
    opacity: 0.55;
  }

  .hero {
    padding-top: 16px;
  }

  .feed-head {
    flex-direction: column;
    padding: 14px;
  }

  .hero-command-line {
    width: 100%;
    justify-content: flex-start;
    gap: 8px;
  }

  .command-chip {
    font-size: 11px;
  }

  .feed-pills {
    justify-content: flex-start;
  }

  .posts-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .article-card:hover {
    transform: translateY(-4px);
  }

  .card-read-more {
    height: 36px;
  }

  .back-top-btn {
    right: 16px;
    bottom: 16px;
    width: 42px;
    height: 42px;
  }
}
</style>
