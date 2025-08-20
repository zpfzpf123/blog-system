<!--
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-04 13:06:31
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-08-13 12:42:28
 * @FilePath: \blog\src\views\Home.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 -->
<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard,
  ElTag,
  ElPagination,
  ElSkeleton,
  ElSelect,
  ElOption,
  ElButton,
  ElInput,
  ElIcon,
  ElMessageBox,
  ElMessage,
  ElTooltip,
} from 'element-plus'
import {
  User,
  Calendar,
  Folder,
  Search,
  Menu,
  CollectionTag,
  Document,
  Edit,
  Delete,
  Plus,
  Star,
  Download,
  DocumentCopy,
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { copyEnhancedRichContent } from '@/utils/imageUtils'

// 测试用的fetch函数
const testFetch = async () => {
  try {
    console.log('测试fetch请求开始')
    const response = await fetch('/api/categories', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
    })
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const data = await response.json()
    console.log('测试fetch请求成功:', data)
    return data
  } catch (error) {
    console.error('测试fetch请求失败:', error)
  }
}

// 定义分类类型
interface Category {
  id: number
  name: string
  createdAt: string
}

// 定义标签类型
interface Tag {
  id: number
  name: string
}

// 定义博客文章类型
interface BlogPost {
  id: number
  title: string
  desc: string
  author: string
  createdAt: string
  category: Category | null
  tags: { id: number; name: string }[]
}

// 定义分页信息类型
interface Pagination {
  currentPage: number
  totalPages: number
  totalPosts: number
  hasNext: boolean
  hasPrev: boolean
}

// 响应式数据
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const selectedCategoryIds = ref<number[]>([])
const selectedTagIds = ref<number[]>([])
const blogPosts = ref<BlogPost[]>([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)
const router = useRouter()
const searchKeyword = ref('')

// 全文搜索（Ctrl+F）- 状态与类型
interface SearchResultItem {
  id: number
  title: string
  snippetHtml: string
  matchedField: 'title' | 'desc' | 'content'
}

const isSearchOpen = ref(false)
const globalSearchQuery = ref('')
const searchResults = ref<SearchResultItem[]>([])
const searchLoading = ref(false)
const searchError = ref('')
const selectedResultIndex = ref(-1) // 当前选中的搜索结果索引
let globalKeydownHandler: ((e: KeyboardEvent) => void) | null = null

const escapeHtml = (s: string) =>
  s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

// 简易去除 Markdown 标记，生成纯文本用于创建摘要
const stripMarkdown = (md: string) => {
  return md
    .replace(/```[\s\S]*?```/g, '') // 多行代码块
    .replace(/`[^`]*`/g, '') // 行内代码
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '') // 图片
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1') // 链接文本保留
    .replace(/[#>*_`~-]+/g, ' ') // 常见标记
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

const openGlobalSearch = (e?: KeyboardEvent) => {
  if (e) e.preventDefault()
  isSearchOpen.value = true
  searchError.value = ''
  searchResults.value = []
  selectedResultIndex.value = -1 // 重置选中索引
  nextTick(() => {
    const input = document.querySelector('#global-search-input') as HTMLInputElement | null
    input?.focus()
    input?.select()
  })
  document.body.style.overflow = 'hidden'
}

const closeGlobalSearch = () => {
  isSearchOpen.value = false
  globalSearchQuery.value = ''
  searchResults.value = []
  searchLoading.value = false
  searchError.value = ''
  selectedResultIndex.value = -1 // 重置选中索引
  document.body.style.overflow = ''
}

// 处理搜索面板的键盘事件
const handleSearchPanelKeydown = (e: KeyboardEvent) => {
  if (!isSearchOpen.value) return

  switch (e.key) {
    case 'ArrowUp':
      e.preventDefault()
      if (searchResults.value.length > 0) {
        selectedResultIndex.value =
          selectedResultIndex.value <= 0
            ? searchResults.value.length - 1
            : selectedResultIndex.value - 1
        // 滚动到选中的结果项
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
        // 滚动到选中的结果项
        scrollToSelectedResult()
      }
      break
    case 'Enter':
      e.preventDefault()
      if (
        selectedResultIndex.value >= 0 &&
        selectedResultIndex.value < searchResults.value.length
      ) {
        handleSearchResultClick(searchResults.value[selectedResultIndex.value])
      } else if (globalSearchQuery.value.trim()) {
        // 如果没有选中项但有搜索词，执行搜索
        performGlobalSearch()
      }
      break
    case 'Escape':
      e.preventDefault()
      closeGlobalSearch()
      break
  }
}

// 滚动到选中的搜索结果项
const scrollToSelectedResult = () => {
  nextTick(() => {
    const selectedElement = document.querySelector('.result-item.is-selected') as HTMLElement
    const resultList = document.querySelector('.result-list') as HTMLElement
    
    if (selectedElement && resultList) {
      // 计算选中元素相对于列表容器的位置
      const containerRect = resultList.getBoundingClientRect()
      const elementRect = selectedElement.getBoundingClientRect()
      
      // 如果选中元素不在可视区域内，进行滚动
      if (elementRect.top < containerRect.top || elementRect.bottom > containerRect.bottom) {
        selectedElement.scrollIntoView({
          behavior: 'smooth',
          block: 'nearest',
          inline: 'nearest'
        })
      }
    }
  })
}

// 监听搜索结果变化，重置选中索引
watch(searchResults, () => {
  selectedResultIndex.value = -1
})

const performGlobalSearch = async () => {
  const q = globalSearchQuery.value.trim()
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

let globalSearchTimer: any
watch(globalSearchQuery, () => {
  clearTimeout(globalSearchTimer)
  globalSearchTimer = setTimeout(() => {
    performGlobalSearch()
  }, 250)
})

// 获取所有分类
const fetchCategories = async () => {
  try {
    console.log('请求分类数据...')
    const response = await axios.get('/api/categories')
    console.log('分类数据获取成功:', response.data)
    categories.value = response.data
  } catch (error: any) {
    console.error('获取分类失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    console.error('请求URL:', error.config?.url)
  }
}

// 获取所有标签
const fetchTags = async () => {
  try {
    console.log('请求标签数据...')
    const response = await axios.get('/api/tags')
    console.log('标签数据获取成功:', response.data)
    tags.value = response.data
  } catch (error: any) {
    console.error('获取标签失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    console.error('请求URL:', error.config?.url)
  }
}

// 获取博客文章列表
const fetchBlogPosts = async (page = 1) => {
  try {
    loading.value = true
    // 构建请求参数
    const params: any = {
      page: page,
      limit: pageSize.value,
    }

    // 添加分类筛选参数 - 只有当数组不为空且长度大于0时才添加
    if (selectedCategoryIds.value && selectedCategoryIds.value.length > 0) {
      params.categoryIds = selectedCategoryIds.value
    }

    // 添加标签筛选参数 - 只有当数组不为空且长度大于0时才添加
    if (selectedTagIds.value && selectedTagIds.value.length > 0) {
      params.tagIds = selectedTagIds.value
    }

    // 添加搜索关键词参数 - 只有当关键词不为空时才添加
    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.search = searchKeyword.value.trim()
    }

    console.log('请求参数:', params)
    // 连接本地后端服务
    const response = await axios.get('/api/posts', {
      params,
      paramsSerializer: {
        indexes: null, // 使用重复键名而不是索引形式：categoryIds=1&categoryIds=2 而不是 categoryIds[0]=1
      },
    })

    console.log('响应数据:', response.data)
    blogPosts.value = response.data.posts
    total.value = response.data.pagination.totalPosts
    currentPage.value = response.data.pagination.currentPage
  } catch (error: any) {
    console.error('获取博客文章失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    console.error('请求URL:', error.config?.url)
    console.error('请求参数:', error.config?.params)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 页面改变时重新获取数据
const handlePageChange = (page: number) => {
  fetchBlogPosts(page)
}

// 每页条数改变时重新获取数据
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchBlogPosts(1) // 重置到第一页
}

// 跳转到博客详情页
const goToBlogDetail = (id: number) => {
  router.push(`/blog/${id}`)
}

// 监听筛选条件变化
const handleFilterChange = () => {
  console.log('筛选条件变化:', {
    selectedCategoryIds: selectedCategoryIds.value,
    selectedTagIds: selectedTagIds.value,
    searchKeyword: searchKeyword.value,
  })
  fetchBlogPosts(1) // 重置到第一页
}

// 处理搜索
const handleSearch = () => {
  fetchBlogPosts(1) // 重置到第一页
}

// 清空搜索
const clearSearch = () => {
  console.log('清空搜索关键词')
  searchKeyword.value = ''
  // 立即重新获取数据，不需要防抖
  fetchBlogPosts(1)
}

// 重置所有筛选条件
const resetAllFilters = () => {
  console.log('重置所有筛选条件')
  selectedCategoryIds.value = []
  selectedTagIds.value = []
  searchKeyword.value = ''
  // 立即重新获取数据，不需要防抖
  fetchBlogPosts(1)
}

// 重置分类筛选
const resetCategoryFilter = () => {
  console.log('重置分类筛选')
  selectedCategoryIds.value = []
  // 立即重新获取数据，不需要防抖
  fetchBlogPosts(1)
}

// 重置标签筛选
const resetTagFilter = () => {
  console.log('重置标签筛选')
  selectedTagIds.value = []
  // 立即重新获取数据，不需要防抖
  fetchBlogPosts(1)
}

// 返回上一页 - 已删除，使用面包屑导航替代

// 增强特效动画：卡片倾斜、滚动显现、按钮涟漪、粒子背景、视差滚动、气泡飘动
let cleanupFns: Array<() => void> = []
const animationFrameId: number | null = null

const initInteractiveEffects = () => {
  const destroyers: Array<() => void> = []

  // 1. 创建粒子背景
  createParticleBackground()

  // 2. 获取卡片元素（用于其他效果）
  const cards = Array.from(document.querySelectorAll('.blog-card')) as HTMLElement[]

  // 3. 滚动显现（进入视口淡入上移）
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        const el = entry.target as HTMLElement
        if (entry.isIntersecting) {
          el.classList.add('in-view')
          // 添加随机延迟让卡片依次出现
          const delay = Math.random() * 200
          setTimeout(() => {
            el.style.animationDelay = '0s'
          }, delay)
          observer.unobserve(el)
        }
      })
    },
    { threshold: 0.1 },
  )
  cards.forEach((c) => observer.observe(c))
  destroyers.push(() => observer.disconnect())

  // 4. 按钮涟漪效果
  const buttons = Array.from(
    document.querySelectorAll(
      '.filter-row-inline .el-button, .blog-actions .el-button, .quick-create-btn',
    ),
  ) as HTMLElement[]
  buttons.forEach((btn) => {
    const onClick = (e: MouseEvent) => {
      const ripple = document.createElement('span')
      ripple.className = 'ripple'
      const rect = btn.getBoundingClientRect()
      const size = Math.max(rect.width, rect.height)
      ripple.style.width = ripple.style.height = size + 'px'
      ripple.style.left = e.clientX - rect.left - size / 2 + 'px'
      ripple.style.top = e.clientY - rect.top - size / 2 + 'px'
      btn.appendChild(ripple)
      setTimeout(() => ripple.remove(), 600)
    }
    btn.addEventListener('click', onClick)
    destroyers.push(() => btn.removeEventListener('click', onClick))
  })

  // 5. 卡片气泡飘动
  initFloatingBubbles()

  cleanupFns.push(() => destroyers.forEach((fn) => fn()))
}

// 创建粒子背景
const createParticleBackground = () => {
  const container = document.querySelector('.home-container') as HTMLElement
  if (!container) return

  const particleContainer = document.createElement('div')
  particleContainer.className = 'particle-container'
  container.appendChild(particleContainer)

  // 创建50个粒子
  for (let i = 0; i < 50; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    particle.style.left = Math.random() * 100 + '%'
    particle.style.top = Math.random() * 100 + '%'
    particle.style.animationDelay = Math.random() * 20 + 's'
    particle.style.animationDuration = Math.random() * 10 + 10 + 's'
    particleContainer.appendChild(particle)
  }

  cleanupFns.push(() => {
    particleContainer.remove()
  })
}

// 视差滚动效果 - 已移除卡片上下浮动

// 卡片气泡飘动
const initFloatingBubbles = () => {
  const cards = Array.from(document.querySelectorAll('.blog-card')) as HTMLElement[]

  cards.forEach((card) => {
    // 为每个卡片创建3-5个气泡
    const bubbleCount = Math.floor(Math.random() * 3) + 3

    for (let i = 0; i < bubbleCount; i++) {
      const bubble = document.createElement('div')
      bubble.className = 'floating-bubble'
      bubble.style.left = Math.random() * 100 + '%'
      bubble.style.top = Math.random() * 100 + '%'
      bubble.style.animationDelay = Math.random() * 5 + 's'
      bubble.style.animationDuration = Math.random() * 3 + 4 + 's'
      card.appendChild(bubble)
    }
  })
}

// 鼠标跟随光效
const initMouseFollowEffect = () => {
  const cursor = document.createElement('div')
  cursor.className = 'cursor-glow'
  document.body.appendChild(cursor)

  const onMouseMove = (e: MouseEvent) => {
    cursor.style.left = e.clientX + 'px'
    cursor.style.top = e.clientY + 'px'
  }

  document.addEventListener('mousemove', onMouseMove)

  cleanupFns.push(() => {
    document.removeEventListener('mousemove', onMouseMove)
    cursor.remove()
  })
}

// 在页面挂载时获取数据
onMounted(async () => {
  fetchCategories()
  fetchTags()
  await fetchBlogPosts()
  // 测试fetch请求
  testFetch()
  await nextTick()
  initInteractiveEffects()
  initMouseFollowEffect()

  // 绑定 Ctrl/Cmd + F 打开全文搜索
  globalKeydownHandler = (e: KeyboardEvent) => {
    const isModifier = e.ctrlKey || e.metaKey
    if (isModifier && (e.key === 'f' || e.key === 'F')) {
      // 如果焦点在输入框中，仍然允许打开我们的搜索面板
      openGlobalSearch(e)
    } else if (isSearchOpen.value) {
      // 如果搜索面板打开，处理面板内的键盘事件
      handleSearchPanelKeydown(e)
    }
  }
  window.addEventListener('keydown', globalKeydownHandler, { capture: true })
})

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn())
  cleanupFns = []
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
  if (globalKeydownHandler) {
    // 兼容旧浏览器：第三个参数传布尔值
    window.removeEventListener('keydown', globalKeydownHandler, true)
    globalKeydownHandler = null
  }
  document.body.style.overflow = ''
})

// 监听路由变化，当用户从其他页面返回时重新获取数据
router.afterEach((to, from) => {
  if (to.path === '/') {
    fetchTags()
    fetchCategories()
    fetchBlogPosts()
  }
})

// 监听搜索关键词变化，添加防抖
let searchTimer: NodeJS.Timeout
watch(searchKeyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleFilterChange()
  }, 500)
})

// 监听分类和标签筛选条件变化
watch(
  [selectedCategoryIds, selectedTagIds],
  (newValues, oldValues) => {
    console.log('筛选条件watch触发:', {
      selectedCategoryIds: newValues[0],
      selectedTagIds: newValues[1],
      oldSelectedCategoryIds: oldValues?.[0],
      oldSelectedTagIds: oldValues?.[1],
    })

    // 如果筛选条件被清空，立即重新获取数据
    if (
      (newValues[0] && newValues[0].length === 0) ||
      (newValues[1] && newValues[1].length === 0)
    ) {
      console.log('筛选条件被清空，重新获取所有博客')
      fetchBlogPosts(1)
    } else {
      // 否则使用防抖处理
      handleFilterChange()
    }
  },
  { deep: true },
)

// 渐变色生成器（根据tag id生成不同渐变）
const tagGradient = (id: number) => {
  const gradients = [
    'linear-gradient(90deg, #42e695 0%, #3bb2b8 100%)',
    'linear-gradient(90deg, #ffaf7b 0%, #d76d77 100%)',
    'linear-gradient(90deg, #43cea2 0%, #185a9d 100%)',
    'linear-gradient(90deg, #f7971e 0%, #ffd200 100%)',
    'linear-gradient(90deg, #f953c6 0%, #b91d73 100%)',
    'linear-gradient(90deg, #30cfd0 0%, #330867 100%)',
    'linear-gradient(90deg, #5ee7df 0%, #b490ca 100%)',
    'linear-gradient(90deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(90deg, #f7797d 0%, #FBD786 100%)',
    'linear-gradient(90deg, #c471f5 0%, #fa71cd 100%)',
  ]
  return gradients[id % gradients.length]
}

const handleEdit = (post: BlogPost, e: MouseEvent) => {
  e.stopPropagation()
  router.push(`/admin/posts/edit/${post.id}?from=home`)
}

const handleDelete = async (post: BlogPost, e: MouseEvent) => {
  e.stopPropagation()
  ElMessageBox.confirm(`确定要删除《${post.title}》吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await axios.delete(`/api/posts/${post.id}`)
      ElMessage.success('删除成功')
      fetchBlogPosts(currentPage.value) // 删除后刷新当前页
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleExport = (post: BlogPost, e: MouseEvent) => {
  e.stopPropagation()
  // 导出为Markdown，包含全部信息
  let content = `# ${post.title}\n\n`
  content += `**作者**: ${post.author}\n`
  content += `**时间**: ${formatDate(post.createdAt)}\n`
  if (post.category) content += `**分类**: ${post.category.name}\n`
  if (post.tags && post.tags.length > 0)
    content += `**标签**: ${post.tags.map((t) => t.name).join(', ')}\n`
  content += `\n---\n\n`
  if (post.desc) content += `${post.desc}\n\n`
  if ((post as any).content) content += `${(post as any).content}\n`
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${post.title.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_')}.md`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleCopyFull = async (post: BlogPost, e: MouseEvent) => {
  e.stopPropagation()
  let content = ''
  if (post.desc) content += post.desc + '\n\n'
  try {
    // 获取文章详情以拿到完整正文
    const { data } = await axios.get(`/api/posts/${post.id}`)
    if (data?.content) content += data.content
    const ok = await copyEnhancedRichContent(content)
    if (ok) {
      ElMessage.success('全文已复制到剪贴板（支持富文本粘贴）')
    } else {
      ElMessage.error('复制失败')
    }
  } catch {
    ElMessage.error('复制失败')
  }
}

const goToCreateBlog = () => {
  router.push('/admin/posts/create?from=home')
}

// 添加一个专门的跳转函数来处理搜索结果的点击
const handleSearchResultClick = (item: SearchResultItem) => {
  console.log('点击搜索结果:', item)
  console.log('当前搜索关键词:', globalSearchQuery.value)

  // 保存当前搜索关键词，避免在关闭搜索面板时被清空
  const searchKeyword = globalSearchQuery.value

  closeGlobalSearch()

  // 构建跳转URL，使用保存的关键词
  const targetUrl = `/blog/${item.id}?q=${encodeURIComponent(searchKeyword)}`
  console.log('跳转URL:', targetUrl)

  // 使用 router.push 进行跳转
  router.push(targetUrl)
}
</script>

<template>
  <div class="home-container">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <!-- 只保留面包屑导航和后续内容 -->
    <Breadcrumb />
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="search-hint">
        <el-icon><Search /></el-icon>
        <span>按 <kbd>Ctrl</kbd>/<kbd>Cmd</kbd> + <kbd>F</kbd> 开启全文搜索</span>
      </div>
      <div class="filter-row-inline">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章标题或内容..."
          class="filter-input"
          clearable
          @clear="clearSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="selectedCategoryIds"
          multiple
          placeholder="分类"
          class="filter-select"
          clearable
        >
          <template #prefix>
            <el-icon><Menu /></el-icon>
          </template>
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
        <el-select
          v-model="selectedTagIds"
          multiple
          placeholder="标签"
          class="filter-select"
          clearable
        >
          <template #prefix>
            <el-icon><CollectionTag /></el-icon>
          </template>
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
        <el-button type="primary" @click="handleSearch" class="filter-btn">搜索</el-button>
        <el-button type="info" plain class="filter-btn" @click="openGlobalSearch()">
          <el-icon><Search /></el-icon>
          全文搜索
        </el-button>
        <el-button @click="resetAllFilters" class="reset-btn" type="danger" plain>重置</el-button>
        <!-- 新建博客按钮 -->
        <el-tooltip content="快速创建新博客" placement="bottom">
          <el-button class="quick-create-btn" type="success" size="large" @click="goToCreateBlog">
            <el-icon class="quick-create-icon"><Plus /></el-icon>
            <span class="quick-create-text">新建博客</span>
          </el-button>
        </el-tooltip>
      </div>
    </el-card>

    <!-- 博客文章列表 -->
    <transition-group name="fade-list" tag="div" class="blog-list">
      <el-card
        v-for="post in blogPosts"
        :key="post.id"
        class="blog-card"
        shadow="hover"
        @click="goToBlogDetail(post.id)"
      >
        <div class="blog-header">
          <span class="blog-icon-wrap"
            ><el-icon class="blog-icon animated-icon"><Star /></el-icon
          ></span>
          <h2 class="blog-title">{{ post.title }}</h2>
          <div class="blog-actions">
            <el-tooltip content="编辑" placement="top">
              <el-button
                class="blog-action-btn"
                type="primary"
                circle
                size="small"
                @click.stop="handleEdit(post, $event)"
              >
                <el-icon class="animated-icon"><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button
                class="blog-action-btn"
                type="danger"
                circle
                size="small"
                @click.stop="handleDelete(post, $event)"
              >
                <el-icon class="animated-icon"><Delete /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="导出为Markdown" placement="top">
              <el-button
                class="blog-action-btn"
                type="success"
                circle
                size="small"
                @click.stop="handleExport(post, $event)"
              >
                <el-icon class="animated-icon"><Download /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="复制全文" placement="top">
              <el-button
                class="blog-action-btn copy-full-btn"
                type="info"
                circle
                size="small"
                @click.stop="handleCopyFull(post, $event)"
              >
                <el-icon class="animated-icon"><DocumentCopy /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
        <div class="blog-meta">
          <span class="meta-item">
            <el-icon class="meta-icon"><User /></el-icon> {{ post.author }}
          </span>
          <span class="meta-item">
            <el-icon class="meta-icon"><Calendar /></el-icon>
            {{ formatDate(post.createdAt) }}
          </span>
          <span v-if="post.category" class="meta-item">
            <el-icon class="meta-icon"><Folder /></el-icon> {{ post.category.name }}
          </span>
        </div>
        <div class="blog-footer">
          <el-tag
            v-for="tag in post.tags"
            :key="tag.id"
            class="tag"
            effect="dark"
            round
            :style="{ background: tagGradient(tag.id) }"
          >
            <el-icon class="tag-icon"><CollectionTag /></el-icon> {{ tag.name }}
          </el-tag>
        </div>
        <div class="blog-excerpt">
          <p class="desc">{{ post.desc }}</p>
        </div>
      </el-card>
    </transition-group>

    <!-- 加载状态 -->
    <div class="blog-list" v-if="loading">
      <el-card v-for="n in 6" :key="n" class="blog-card skeleton" shadow="hover">
        <el-skeleton :rows="4" animated />
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[6, 12, 24, 48]"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 全文搜索面板（Ctrl/Cmd + F） -->
    <div v-if="isSearchOpen" class="global-search-overlay" @click.self="closeGlobalSearch">
      <div class="global-search-panel" @keydown.stop>
        <div class="search-header">
          <div class="search-input-container">
            <el-input
              id="global-search-input"
              v-model="globalSearchQuery"
              placeholder="全文搜索（输入关键词后回车或稍候自动搜索）"
              clearable
              @keyup.enter="performGlobalSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>

            <!-- 搜索状态指示器 -->
            <div v-if="globalSearchQuery" class="search-status-indicator">
              <div v-if="searchLoading" class="status-loading">
                <el-icon class="loading-icon"><Search /></el-icon>
                <span>正在搜索...</span>
                <div class="loading-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
              <div v-else-if="searchError" class="status-error">
                <el-icon class="error-icon"><Document /></el-icon>
                <span>搜索出错: {{ searchError }}</span>
              </div>
            </div>
          </div>
          <el-button class="close-btn" circle @click="closeGlobalSearch">✕</el-button>
        </div>
        <div class="search-body">
          <div v-if="searchLoading" class="search-status">正在搜索...</div>
          <div v-else-if="searchError" class="search-status error">{{ searchError }}</div>
          <div v-else-if="!globalSearchQuery" class="search-status">输入关键词开始搜索</div>
          <div v-else-if="searchResults.length === 0" class="search-status">未找到相关内容</div>
          <div v-else>
            <!-- 搜索结果统计头部 -->
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
                @click="handleSearchResultClick(item)"
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

      <!-- 右下角悬浮全文搜索按钮 -->
      <el-tooltip content="全文搜索（Ctrl/Cmd + F）" placement="left">
        <el-button class="global-search-fab" type="primary" circle @click="openGlobalSearch()">
          <el-icon><Search /></el-icon>
        </el-button>
      </el-tooltip>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  width: 95vw;
  margin: 0 auto;
  padding: 0 12px 40px 12px;
  position: relative;
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.05) 0%,
    rgba(118, 75, 162, 0.03) 25%,
    rgba(240, 147, 251, 0.05) 50%,
    rgba(248, 250, 252, 0.8) 75%,
    rgba(224, 231, 239, 0.1) 100%
  );
  background-size: 400% 400%;
  min-height: 100vh;
  animation: backgroundShift 20s ease-in-out infinite;
  overflow-x: hidden;
}

@keyframes backgroundShift {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.home-header {
  display: none;
}
.main-navbar {
  display: none;
}
.filter-card {
  margin-bottom: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}
.filter-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.25);
}
.global-search-fab {
  position: fixed;
  right: 22px;
  bottom: 26px;
  z-index: 10000;
  width: 48px;
  height: 48px;
  box-shadow: 0 10px 24px rgba(102, 126, 234, 0.25);
}
.search-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
  font-size: 12px;
  padding: 10px 12px 0 12px;
}
.search-hint kbd {
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-bottom-width: 2px;
  border-radius: 6px;
  padding: 1px 6px;
  font-weight: 600;
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}
.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  padding: 20px 0 4px 0;
  justify-content: flex-start;
}
.filter-input {
  width: 220px;
  border-radius: 10px;
  transition: all 0.3s ease;
}
.filter-input:focus-within {
  transform: scale(1.02);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}
.filter-select {
  min-width: 110px;
  border-radius: 10px;
  transition: all 0.3s ease;
}
.filter-select:hover {
  transform: translateY(-1px);
}
.filter-btn,
.reset-btn {
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 1px;
  padding: 0 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: none;
  color: #fff;
}
.filter-btn {
  background: linear-gradient(135deg, #ff9f43 0%, #ff6a00 100%);
  box-shadow: 0 6px 18px rgba(255, 122, 0, 0.28);
}
.filter-btn:hover {
  transform: translateY(-2px) scale(1.03);
  background: linear-gradient(135deg, #ffa94d 0%, #ff7a1a 100%);
  box-shadow: 0 10px 24px rgba(255, 122, 0, 0.35);
}
.filter-btn::before,
.reset-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}
.filter-btn:hover::before,
.reset-btn:hover::before {
  left: 100%;
}
.reset-btn {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  box-shadow: 0 6px 18px rgba(16, 185, 129, 0.25);
}
.reset-btn:hover {
  transform: translateY(-2px) scale(1.03);
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
  box-shadow: 0 10px 24px rgba(16, 185, 129, 0.35);
}

.blog-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 28px;
  margin-bottom: 32px;
}

.blog-card {
  position: relative;
  border-radius: 18px;
  overflow: visible;
  box-shadow: 0 6px 30px rgba(102, 126, 234, 0.12);
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 220px;
  z-index: 2;
  padding: 28px 24px 20px 24px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  border: 1px solid rgba(255, 255, 255, 0.2);
}
.blog-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  opacity: 0;
  transition: opacity 0.3s ease;
}
.blog-card:hover {
  background: rgba(255, 255, 255, 0.98);
  box-shadow:
    0 25px 60px rgba(102, 126, 234, 0.35),
    0 0 40px rgba(240, 147, 251, 0.2);
  transform: translateY(-8px) scale(1.02);
}
.blog-card:hover::before {
  opacity: 1;
}
.blog-header {
  position: relative;
  z-index: 2;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.blog-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 2px;
}
.blog-icon {
  font-size: 1.5rem;
  color: #667eea;
  transition: transform 0.5s cubic-bezier(0.4, 2, 0.6, 1);
}
.blog-card:hover .blog-icon {
  transform: rotate(-18deg) scale(1.18);
  color: #f093fb;
}
.blog-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 6px 0;
  letter-spacing: 1px;
  line-height: 1.3;
  transition: all 0.3s ease;
}
.blog-card:hover .blog-title {
  color: #667eea;
  transform: translateX(4px);
  text-shadow: 0 2px 12px #f093fb33;
}
.blog-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 0.95rem;
  color: #666;
  margin-bottom: 12px;
  z-index: 2;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 6px;
  transition: all 0.3s ease;
}
.meta-icon {
  font-size: 1.1em;
  color: #764ba2;
  transition: transform 0.5s cubic-bezier(0.4, 2, 0.6, 1);
}
.blog-card:hover .meta-icon {
  transform: rotate(12deg) scale(1.18);
  color: #f093fb;
}
.meta-item:hover {
  background: rgba(102, 126, 234, 0.15);
  transform: translateY(-1px) scale(1.06);
}
.blog-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  z-index: 2;
}
.tag {
  border-radius: 18px;
  color: #fff;
  font-weight: 600;
  border: none;
  font-size: 0.9rem;
  padding: 6px 16px;
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 4px;
}
.tag-icon {
  font-size: 1em;
  margin-right: 2px;
  color: #fff;
  transition: transform 0.5s cubic-bezier(0.4, 2, 0.6, 1);
}
.tag:hover .tag-icon {
  transform: scale(1.3) rotate(-18deg);
  color: #ffd700;
}
.tag:hover {
  transform: translateY(-2px) scale(1.08);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}
.blog-excerpt {
  margin: 0 0 8px 0;
  z-index: 2;
  min-height: 2.4em;
  animation: fadeInUp 0.7s;
}
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.desc {
  color: #555;
  font-size: 1.05rem;
  line-height: 1.7;
  margin: 0;
  /* display: -webkit-box; */
  /* -webkit-line-clamp: 2; */
  /* -webkit-box-orient: vertical; */
  /* overflow: hidden; */
  /* text-overflow: ellipsis; */
  min-height: 2.4em;
  white-space: pre-line;
}
.skeleton {
  background: linear-gradient(135deg, #f6f8fa, #e8ecf0);
  min-height: 220px;
}

.fade-list-enter-active,
.fade-list-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-list-enter-from,
.fade-list-leave-to {
  opacity: 0;
  transform: translateY(40px) scale(0.95);
}
.fade-list-move {
  transition: transform 0.5s ease;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
}

.filter-row-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 10px 0 2px 0;
  justify-content: flex-start;
  min-height: 0;
}
.filter-row-inline .el-input,
.filter-row-inline .el-select {
  min-width: 160px;
  max-width: 220px;
  flex: 1 1 160px;
  transition: all 0.2s;
  filter: drop-shadow(0 0 0 rgba(0, 0, 0, 0));
}
.filter-row-inline .el-input .el-input__wrapper,
.filter-row-inline .el-select .el-input__wrapper {
  border-radius: 8px;
  transition:
    box-shadow 0.2s,
    transform 0.2s;
}
.filter-row-inline .el-input:hover .el-input__wrapper,
.filter-row-inline .el-select:hover .el-input__wrapper {
  box-shadow: 0 0 0 2px #a3bffa;
  transform: scale(1.03);
}
.filter-row-inline .el-input:focus-within,
.filter-row-inline .el-select:focus-within {
  filter: drop-shadow(0 8px 16px rgba(102, 126, 234, 0.18));
}
.filter-row-inline .el-button {
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 1px;
  padding: 0 16px;
  transition: all 0.2s;
  min-width: 64px;
  height: 38px;
  display: flex;
  align-items: center;
}
.filter-row-inline .el-button:hover {
  transform: scale(1.07) translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.18);
}

.blog-header {
  position: relative;
}
.blog-actions {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 8px;
  opacity: 0;
  pointer-events: none;
  transition:
    opacity 0.3s,
    right 0.3s;
  z-index: 10;
}
.blog-card:hover .blog-actions {
  opacity: 1;
  pointer-events: auto;
  right: 12px;
}
.blog-action-btn {
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.12);
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}
.blog-action-btn:hover {
  transform: scale(1.15) rotate(-8deg);
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.18);
}

.quick-create-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 18px;
  margin-top: 2px;
  animation: fadeInDown 0.7s;
}
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.quick-create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  padding: 10px 22px 10px 14px;
  border-radius: 30px;
  background: linear-gradient(90deg, #c471f5 0%, #fa71cd 100%);
  box-shadow: 0 6px 22px rgba(202, 88, 255, 0.25);
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 2, 0.6, 1);
  position: relative;
  overflow: hidden;
}
.quick-create-btn:hover {
  background: linear-gradient(90deg, #d39bf9 0%, #ff89d6 100%);
  transform: scale(1.08) translateY(-2px) rotate(-2deg);
  box-shadow: 0 12px 36px rgba(202, 88, 255, 0.35);
}
.quick-create-icon {
  font-size: 1.5em;
  color: #fff;
  animation: pulseGlow 2s infinite;
}
@keyframes pulseGlow {
  0%,
  100% {
    text-shadow: 0 0 0 #fff;
  }
  50% {
    text-shadow:
      0 0 14px #fff,
      0 0 28px #ff8dd6;
  }
}
.quick-create-text {
  color: #fff;
  letter-spacing: 1px;
  font-weight: 700;
  font-size: 1.1em;
  text-shadow: 0 2px 8px #ff8dd633;
}

.animated-icon {
  animation: iconPulse 2.2s infinite;
  transition:
    color 0.3s,
    transform 0.3s;
}
@keyframes iconPulse {
  0%,
  100% {
    filter: drop-shadow(0 0 0 #42a5f5);
    transform: scale(1);
  }
  50% {
    filter: drop-shadow(0 0 8px #42a5f5);
    transform: scale(1.18) rotate(-8deg);
  }
}
.blog-action-btn:hover .animated-icon {
  color: #ffd700;
  transform: scale(1.25) rotate(-12deg);
}
.blog-icon-wrap .animated-icon {
  color: #42a5f5;
  font-size: 1.6em;
}
.blog-card:hover .blog-icon-wrap .animated-icon {
  color: #f093fb;
  filter: drop-shadow(0 0 12px #f093fb);
}

.copy-full-btn {
  background: linear-gradient(90deg, #42e695 0%, #3bb2b8 100%);
  color: #fff;
  border: none;
  box-shadow: 0 2px 8px rgba(66, 230, 149, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 2, 0.6, 1);
  font-weight: 700;
}
.copy-full-btn:hover {
  background: linear-gradient(90deg, #43cea2 0%, #185a9d 100%);
  color: #fff;
  transform: scale(1.18) rotate(-8deg);
  box-shadow: 0 4px 16px rgba(66, 230, 149, 0.18);
}

/* 粒子背景 */
.particle-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: radial-gradient(
    circle,
    rgba(102, 126, 234, 0.8) 0%,
    rgba(240, 147, 251, 0.4) 50%,
    transparent 100%
  );
  border-radius: 50%;
  animation: particleFloat 15s infinite linear;
}

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

.blog-card.in-view {
  opacity: 1;
  transform: translateY(0) scale(1);
  animation: cardSlideIn 0.8s ease-out;
}

@keyframes cardSlideIn {
  0% {
    opacity: 0;
    transform: translateY(40px) scale(0.95) rotateX(10deg);
  }
  50% {
    transform: translateY(-5px) scale(1.02) rotateX(-2deg);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1) rotateX(0deg);
  }
}

/* 卡片气泡飘动 */
.floating-bubble {
  position: absolute;
  width: 8px;
  height: 8px;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.6) 0%,
    rgba(102, 126, 234, 0.3) 50%,
    transparent 100%
  );
  border-radius: 50%;
  pointer-events: none;
  animation: bubbleFloat 6s infinite ease-in-out;
  z-index: 1;
}

@keyframes bubbleFloat {
  0%,
  100% {
    transform: translateY(0px) translateX(0px) scale(1);
    opacity: 0.7;
  }
  25% {
    transform: translateY(-20px) translateX(10px) scale(1.2);
    opacity: 1;
  }
  50% {
    transform: translateY(-40px) translateX(-5px) scale(0.8);
    opacity: 0.8;
  }
  75% {
    transform: translateY(-20px) translateX(-10px) scale(1.1);
    opacity: 0.9;
  }
}

/* 鼠标跟随光效 */
.cursor-glow {
  position: fixed;
  width: 20px;
  height: 20px;
  background: radial-gradient(
    circle,
    rgba(102, 126, 234, 0.4) 0%,
    rgba(240, 147, 251, 0.2) 50%,
    transparent 100%
  );
  border-radius: 50%;
  pointer-events: none;
  z-index: 9999;
  transform: translate(-50%, -50%);
  transition: all 0.1s ease;
  mix-blend-mode: screen;
}

/* 涟漪效果 */
.filter-row-inline .el-button,
.blog-actions .el-button,
.quick-create-btn {
  position: relative;
  overflow: hidden;
}
:deep(.ripple) {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  transform: scale(0);
  animation: ripple 0.6s linear;
  pointer-events: none;
}
@keyframes ripple {
  to {
    transform: scale(3);
    opacity: 0;
  }
}

/* 增强的背景动画 */
@keyframes backgroundShift {
  0%,
  100% {
    background-position: 0% 50%;
    filter: hue-rotate(0deg);
  }
  25% {
    background-position: 100% 25%;
    filter: hue-rotate(90deg);
  }
  50% {
    background-position: 100% 75%;
    filter: hue-rotate(180deg);
  }
  75% {
    background-position: 0% 100%;
    filter: hue-rotate(270deg);
  }
}

/* 卡片悬浮增强 - 已移除彩虹边框效果 */

/* 筛选卡片增强动画 */
.filter-card {
  animation: filterCardFloat 6s ease-in-out infinite;
}

@keyframes filterCardFloat {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-3px) rotate(0.5deg);
  }
}

/* 分页容器增强 */
.pagination-container {
  animation: paginationGlow 4s ease-in-out infinite;
}

@keyframes paginationGlow {
  0%,
  100% {
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
  }
  50% {
    box-shadow:
      0 8px 30px rgba(102, 126, 234, 0.2),
      0 0 20px rgba(240, 147, 251, 0.1);
  }
}

/* 标签增强动画 */
.tag {
  animation: tagPulse 3s ease-in-out infinite;
}

.tag:nth-child(odd) {
  animation-delay: 0.5s;
}

.tag:nth-child(even) {
  animation-delay: 1s;
}

@keyframes tagPulse {
  0%,
  100% {
    transform: scale(1);
    filter: brightness(1);
  }
  50% {
    transform: scale(1.05);
    filter: brightness(1.1);
  }
}

/* 图标增强动画 */
.animated-icon {
  animation: iconFloat 4s ease-in-out infinite;
}

@keyframes iconFloat {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
    filter: drop-shadow(0 0 5px rgba(102, 126, 234, 0.3));
  }
  50% {
    transform: translateY(-2px) rotate(5deg);
    filter: drop-shadow(0 0 10px rgba(240, 147, 251, 0.5));
  }
}

/* 响应式优化 - 在小屏幕上减少动画 */
@media (max-width: 768px) {
  .particle-container {
    display: none; /* 移动端隐藏粒子以提升性能 */
  }

  .floating-bubble {
    display: none; /* 移动端隐藏气泡 */
  }

  .cursor-glow {
    display: none; /* 移动端隐藏鼠标光效 */
  }

  .blog-card {
    animation: none; /* 简化移动端动画 */
  }
}

/* 背景装饰元素 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -2;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(102, 126, 234, 0.1) 0%,
    rgba(240, 147, 251, 0.05) 50%,
    transparent 100%
  );
  animation: bgCircleFloat 20s ease-in-out infinite;
}

.bg-circle-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: -10%;
  animation-delay: 0s;
}

.bg-circle-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: -5%;
  animation-delay: 7s;
}

.bg-circle-3 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 20%;
  animation-delay: 14s;
}

@keyframes bgCircleFloat {
  0%,
  100% {
    transform: translateY(0px) translateX(0px) scale(1);
    opacity: 0.3;
  }
  33% {
    transform: translateY(-30px) translateX(20px) scale(1.1);
    opacity: 0.5;
  }
  66% {
    transform: translateY(20px) translateX(-15px) scale(0.9);
    opacity: 0.4;
  }
}
/* 性能优化 - 减少重绘 */
.blog-card,
.filter-card,
.pagination-container {
  will-change: transform, opacity;
}

.particle,
.floating-bubble {
  will-change: transform, opacity;
}
@keyframes ripple {
  to {
    transform: scale(3);
    opacity: 0;
  }
}

@media (max-width: 900px) {
  .home-container {
    max-width: 100vw;
    padding: 0 2vw 24px 2vw;
  }
  .blog-list {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 24px;
  }
  .blog-card {
    padding: 20px 18px 16px 18px;
  }
  .filter-row-inline {
    gap: 8px;
  }
  .filter-row-inline .el-input,
  .filter-row-inline .el-select {
    min-width: 120px;
    max-width: 180px;
  }
}
@media (max-width: 600px) {
  .home-container {
    padding: 0 2px 18px 2px;
  }
  .home-banner {
    margin: 12px 0 8px 0;
    padding: 12px 0 8px 0;
    border-radius: 12px;
  }
  .banner-content h1 {
    font-size: 1.1rem;
  }
  .banner-content p {
    font-size: 0.92rem;
  }
  .filter-card {
    margin-bottom: 10px;
    border-radius: 12px;
  }
  .filter-row {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    padding: 10px 0 4px 0;
  }
  .blog-list {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .blog-card {
    border-radius: 12px;
    min-height: 160px;
    padding: 16px 12px 12px 12px;
  }
  .blog-title {
    font-size: 1.1rem;
  }
  .meta-item {
    padding: 3px 6px;
    font-size: 0.85rem;
  }
  .filter-row-inline {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    padding: 6px 0 2px 0;
  }
  .filter-row-inline .el-input,
  .filter-row-inline .el-select {
    min-width: 100px;
    max-width: 100%;
  }
}
</style>

<style scoped>
.global-search-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}
.global-search-panel {
  width: min(900px, 92vw);
  max-height: 76vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.search-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 14px 0 14px;
}

.search-input-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-count {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.search-count .el-tag {
  font-size: 12px;
  font-weight: 600;
  border-radius: 12px;
  padding: 2px 8px;
  animation: countFadeIn 0.3s ease-out;
}

@keyframes countFadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.search-status-indicator {
  margin-top: 4px;
}

.status-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #667eea;
  padding: 6px 10px;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 6px;
  border-left: 2px solid #667eea;
}

.loading-icon {
  font-size: 14px;
  animation: iconSpin 1s linear infinite;
}

@keyframes iconSpin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-dots {
  display: flex;
  gap: 2px;
}

.loading-dots span {
  width: 4px;
  height: 4px;
  background: #667eea;
  border-radius: 50%;
  animation: dotsPulse 1.4s ease-in-out infinite both;
}

.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes dotsPulse {
  0%,
  80%,
  100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.status-error {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #e53e3e;
  padding: 6px 10px;
  background: rgba(229, 62, 62, 0.08);
  border-radius: 6px;
  border-left: 2px solid #e53e3e;
}

.error-icon {
  font-size: 14px;
  color: #e53e3e;
}
.close-btn {
  width: 36px;
  height: 36px;
  font-size: 16px;
}
.search-body {
  padding: 14px;
  overflow: auto;
}
.search-status {
  color: #666;
  text-align: center;
  padding: 26px 0;
}
.search-status.error {
  color: #e53935;
}

.search-results-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 2px solid #f0f2f5;
  margin-bottom: 16px;
}

.results-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.results-icon {
  font-size: 20px;
  color: #667eea;
  animation: iconBounce 0.6s ease-out;
}

@keyframes iconBounce {
  0% {
    transform: scale(0.8) rotate(-10deg);
  }
  50% {
    transform: scale(1.2) rotate(5deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
  }
}

.results-count {
  color: #374151;
  font-weight: 700;
}

.results-badge {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  font-weight: 700;
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
  }
}

.results-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
  padding: 8px 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
  border-left: 3px solid #667eea;
}
.result-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
  scroll-behavior: smooth;
  scrollbar-width: thin;
  scrollbar-color: rgba(102, 126, 234, 0.3) transparent;
}

.result-list::-webkit-scrollbar {
  width: 6px;
}

.result-list::-webkit-scrollbar-track {
  background: transparent;
}

.result-list::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 3px;
}

.result-list::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.5);
}
.result-item {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #eef2f7;
  cursor: pointer;
  transition:
    background 0.2s,
    transform 0.1s,
    box-shadow 0.2s;
}
.result-item:hover {
  background: #f9fafb;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.12);
}
.result-item.is-selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  border-color: #667eea;
  position: relative;
  z-index: 1;
}

.result-item.is-selected::before {
  content: '';
  position: absolute;
  left: -2px;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 2px;
  animation: selectedPulse 1.5s ease-in-out infinite;
}

@keyframes selectedPulse {
  0%, 100% {
    opacity: 1;
    transform: scaleY(1);
  }
  50% {
    opacity: 0.7;
    transform: scaleY(1.1);
  }
}

.result-item.is-selected .result-title {
  color: white;
}

.result-item.is-selected .result-snippet {
  color: rgba(255, 255, 255, 0.9);
}

.result-item.is-selected .result-meta {
  color: rgba(255, 255, 255, 0.7);
}

.result-item.is-selected .result-snippet mark {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}
.result-title {
  font-weight: 700;
  color: #111827;
}
.result-snippet {
  margin-top: 6px;
  color: #374151;
}
.result-snippet mark {
  background: #fff3a3;
  padding: 0 2px;
}
.result-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}
.search-footer {
  padding: 10px 14px 14px 14px;
  font-size: 12px;
  color: #6b7280;
  border-top: 1px dashed #eef2f7;
}
</style>
