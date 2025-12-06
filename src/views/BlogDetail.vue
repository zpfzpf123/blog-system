<template>
  <div class="blog-detail-container">
    <div v-if="loading" class="loading-container">
      <el-card class="loading-card">
        <el-skeleton :rows="10" animated />
      </el-card>
    </div>

    <div v-else-if="blogPost" class="blog-layout">
      <div class="blog-main">
        <el-card class="blog-card">
          <div class="blog-header">
            <h1 class="blog-title">{{ blogPost.title }}</h1>
            <div class="blog-meta">
              <div class="meta-item">
                <el-icon><User /></el-icon>
                <span>{{ blogPost.author }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDate(blogPost.createdAt) }}</span>
              </div>
              <div v-if="blogPost.category" class="meta-item">
                <el-icon><Folder /></el-icon>
                <span>{{ blogPost.category.name }}</span>
              </div>
              <div class="meta-item reading-time">
                <el-icon><Timer /></el-icon>
                <span>{{ estimatedReadingTime }} 分钟</span>
              </div>
            </div>

            <div class="blog-tags">
              <el-tag
                v-for="tag in blogPost.tags"
                :key="tag.id"
                :type="getTagType(tag.name)"
                size="small"
                class="tag-item"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </div>

          <div class="blog-content">
            <div class="markdown-body" v-html="renderedHtml"></div>
          </div>

          <div class="blog-footer">
            <div class="footer-actions">
              <el-button
                type="primary"
                @click="copyFullContent"
                :icon="DocumentCopy"
                class="copy-btn"
                title="复制全文内容，支持富文本粘贴（包含图片）"
              >
                复制全文
              </el-button>
              <el-button type="success" @click="shareArticle" :icon="Share" class="share-btn">
                分享文章
              </el-button>
              <el-button
                type="info"
                @click="toggleFullscreen"
                :icon="isFullscreen ? Close : FullScreen"
                class="fullscreen-btn"
              >
                {{ isFullscreen ? '退出全屏' : '全屏阅读' }}
              </el-button>
            </div>
            <div class="footer-info">
              <span>感谢您的阅读！</span>
            </div>
          </div>
        </el-card>
      </div>

      <div class="blog-sidebar">
        <div class="sidebar-sticky">
          <el-card class="toc-card" @wheel.stop>
            <template #header>
              <div class="toc-header">
                <el-icon><List /></el-icon>
                <span>目录</span>
              </div>
            </template>
            <div class="toc-content">
              <div v-if="tocItems.length > 0" class="toc-list">
                <div
                  v-for="item in tocItems"
                  :key="item.id"
                  :class="['toc-item', `toc-level-${item.level}`, { 'toc-active': activeTocId === item.id }]"
                  @click="scrollToHeading(item.id)"
                >
                  <span class="toc-text" :title="item.text">{{ item.text }}</span>
                </div>
              </div>
              <div v-else class="toc-empty">
                <el-icon><Document /></el-icon>
                <span>暂无目录</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <div v-else class="empty-container">
      <el-card class="empty-card">
        <el-empty description="未找到该博客文章" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElCard, ElTag, ElButton, ElSkeleton, ElEmpty, ElMessage } from 'element-plus'
import {
  User,
  Clock,
  Folder,
  Timer,
  DocumentCopy,
  Share,
  FullScreen,
  Close,
  List,
  Document,
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import markdownItToc from 'markdown-it-table-of-contents'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import 'github-markdown-css/github-markdown-light.css'
import { processImageUrlsInMarkdown, copyEnhancedRichContent } from '@/utils/imageUtils'

// 类型定义
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
  content: string
  author: string
  createdAt: string
  category: Category
  tags: Tag[]
}

// 响应式数据
const blogPost = ref<BlogPost | null>(null)
const loading = ref(false)
const estimatedReadingTime = ref(0)
const isFullscreen = ref(false)
const renderedHtml = ref('')
const tocItems = ref<Array<{ id: string; text: string; level: number }>>([])
const activeTocId = ref('')

const route = useRoute()

// 初始化 markdown-it
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>'
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

// 配置插件 - 自动为标题生成 ID
md.use(markdownItAnchor, {
  permalink: false,
  level: [1, 2, 3, 4, 5, 6],
  slugify: (s: string) => {
    // 使用标题文本作为 ID（移除特殊字符）
    return encodeURIComponent(s.trim())
  }
})

md.use(markdownItToc, {
  includeLevel: [1, 2, 3],
  containerHeaderHtml: '<div class="toc-title">目录</div>'
})

// 渲染 Markdown
const renderMarkdown = (content: string) => {
  if (!content) return ''
  const processed = processImageUrlsInMarkdown(content)
  return md.render(processed)
}

// 方法
const fetchBlogPost = async (id: string) => {
  try {
    loading.value = true
    const response = await axios.get(`/api/posts/${id}`)
    blogPost.value = response.data

    if (blogPost.value?.content) {
      estimatedReadingTime.value = calculateReadingTime(blogPost.value.content)
      renderedHtml.value = renderMarkdown(blogPost.value.content)
      
      // 等待DOM完全渲染
      await nextTick()
      
      // 延迟执行以确保v-html已经渲染完成
      setTimeout(() => {
        extractTocItems()
        enhanceCodeBlocks()
      }, 100)
      
      highlightQueryInContent()
    }
  } catch (error) {
    console.error('获取博客文章详情失败:', error)
    blogPost.value = null
  } finally {
    loading.value = false
  }
}

const calculateReadingTime = (content: string) => {
  const textContent = content.replace(/[#*`\[\]()]/g, '')
  const chineseChars = textContent.match(/[\u4e00-\u9fa5]/g)?.length || 0
  const englishWords = textContent.match(/[a-zA-Z]+/g)?.length || 0

  const chineseTime = chineseChars / 300
  const englishTime = englishWords / 200

  return Math.ceil(chineseTime + englishTime)
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 提取目录项
const extractTocItems = () => {
  const container = document.querySelector('.markdown-body')
  if (!container) return
  
  const headings = container.querySelectorAll('h1, h2, h3')
  
  tocItems.value = Array.from(headings).map((heading, index) => {
    const headingEl = heading as HTMLElement
    let id = headingEl.id
    
    // 如果标题没有ID，手动添加一个
    if (!id) {
      const text = headingEl.textContent || ''
      id = encodeURIComponent(text.trim()) || `heading-${index}`
      headingEl.id = id
    }
    
    return {
      id,
      text: headingEl.textContent || '',
      level: parseInt(headingEl.tagName.substring(1))
    }
  })
}

// 为代码块添加工具栏
const enhanceCodeBlocks = () => {
  const container = document.querySelector('.markdown-body')
  if (!container) return
  
  let codeBlocks = container.querySelectorAll('pre.hljs')
  if (codeBlocks.length === 0) {
    codeBlocks = container.querySelectorAll('pre')
  }
  
  codeBlocks.forEach((pre, index) => {
    // 避免重复添加
    if (pre.querySelector('.code-toolbar')) return
    
    const code = pre.querySelector('code')
    if (!code) return
    
    // 检查代码行数，超过10行添加展开收起功能
    const lines = code.textContent?.split('\n').length || 0
    const needCollapse = lines > 10
    
    // 创建工具栏
    const toolbar = document.createElement('div')
    toolbar.className = 'code-toolbar'
    
    // 语言标签
    const lang = pre.className.match(/language-(\w+)/)?.[1] || 'text'
    const langTag = document.createElement('span')
    langTag.className = 'code-lang'
    langTag.textContent = lang.toUpperCase()
    toolbar.appendChild(langTag)
    
    // 按钮容器
    const btnGroup = document.createElement('div')
    btnGroup.className = 'code-btn-group'
    
    // 展开/收起按钮
    if (needCollapse) {
      const collapseBtn = document.createElement('button')
      collapseBtn.className = 'code-btn code-collapse-btn'
      collapseBtn.innerHTML = '<span>展开</span>'
      collapseBtn.title = '展开/收起代码'
      
      // 初始状态：收起（添加 collapsed 类）
      pre.classList.add('code-collapsed')
      
      collapseBtn.addEventListener('click', () => {
        pre.classList.toggle('code-collapsed')
        const isCollapsed = pre.classList.contains('code-collapsed')
        collapseBtn.innerHTML = isCollapsed ? '<span>展开</span>' : '<span>收起</span>'
      })
      
      btnGroup.appendChild(collapseBtn)
    }
    
    // 复制按钮
    const copyBtn = document.createElement('button')
    copyBtn.className = 'code-btn code-copy-btn'
    copyBtn.innerHTML = '<span>复制</span>'
    copyBtn.title = '复制代码'
    
    copyBtn.addEventListener('click', async () => {
      const codeText = code.textContent || ''
      try {
        await navigator.clipboard.writeText(codeText)
        copyBtn.innerHTML = '<span>✓ 已复制</span>'
        copyBtn.classList.add('copied')
        
        setTimeout(() => {
          copyBtn.innerHTML = '<span>复制</span>'
          copyBtn.classList.remove('copied')
        }, 2000)
      } catch (err) {
        console.error('复制失败:', err)
        copyBtn.innerHTML = '<span>✗ 失败</span>'
        setTimeout(() => {
          copyBtn.innerHTML = '<span>复制</span>'
        }, 2000)
      }
    })
    
    btnGroup.appendChild(copyBtn)
    toolbar.appendChild(btnGroup)
    
    // 插入工具栏
    pre.insertBefore(toolbar, pre.firstChild)
  })
}

// 跳转到标题
const scrollToHeading = (id: string) => {
  const element = document.getElementById(id)
  if (!element) return
  
  const elementTop = element.getBoundingClientRect().top + window.pageYOffset
  const offsetTop = 80
  
  window.scrollTo({
    top: elementTop - offsetTop,
    behavior: 'smooth'
  })
}

const getTagType = (tagName: string): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
  const tagTypes: { [key: string]: 'primary' | 'success' | 'warning' | 'info' | 'danger' } = {
    Vue: 'success',
    React: 'primary',
    JavaScript: 'warning',
    TypeScript: 'info',
    'Node.js': 'success',
    Python: 'warning',
    Java: 'danger',
    Go: 'info',
    Rust: 'danger',
    'C++': 'danger',
    'C#': 'primary',
    PHP: 'warning',
    Ruby: 'danger',
    Swift: 'warning',
    Kotlin: 'info',
    Dart: 'success',
    Flutter: 'success',
    'React Native': 'primary',
    Angular: 'danger',
    Svelte: 'warning',
    'Next.js': 'primary',
    'Nuxt.js': 'success',
    Express: 'info',
    Koa: 'success',
    FastAPI: 'warning',
    Django: 'success',
    Flask: 'info',
    Spring: 'success',
    Laravel: 'danger',
    Symfony: 'warning',
    'ASP.NET': 'primary',
    WebSocket: 'info',
    GraphQL: 'warning',
    'REST API': 'success',
    微服务: 'info',
    Docker: 'primary',
    Kubernetes: 'warning',
    AWS: 'danger',
    Azure: 'primary',
    GCP: 'success',
    阿里云: 'warning',
    腾讯云: 'success',
    数据库: 'info',
    MySQL: 'success',
    PostgreSQL: 'info',
    MongoDB: 'warning',
    Redis: 'danger',
    Elasticsearch: 'warning',
    机器学习: 'success',
    深度学习: 'primary',
    人工智能: 'warning',
    区块链: 'info',
    物联网: 'success',
    移动开发: 'primary',
    前端开发: 'success',
    后端开发: 'info',
    全栈开发: 'warning',
    DevOps: 'danger',
    测试: 'info',
    运维: 'warning',
    架构设计: 'primary',
    性能优化: 'success',
    安全: 'danger',
    工具: 'info',
    教程: 'success',
    经验分享: 'warning',
    技术探索: 'info',
    项目实战: 'primary',
  }

  // 精确匹配
  if (tagTypes[tagName]) {
    return tagTypes[tagName]
  }

  // 模糊匹配
  for (const [key, value] of Object.entries(tagTypes)) {
    if (
      tagName.toLowerCase().includes(key.toLowerCase()) ||
      key.toLowerCase().includes(tagName.toLowerCase())
    ) {
      return value
    }
  }

  return 'primary'
}

const copyFullContent = async () => {
  if (!blogPost.value) return

  try {
    const success = await copyEnhancedRichContent(blogPost.value.content)
    if (success) {
      ElMessage.success('全文内容已复制到剪贴板（支持富文本粘贴）')
    } else {
      ElMessage.error('复制失败')
    }
  } catch {
    ElMessage.error('复制失败')
  }
}

const shareArticle = () => {
  if (navigator.share) {
    navigator.share({
      title: blogPost.value?.title || '博客文章',
      url: window.location.href,
    })
  } else {
    // 降级到复制链接
    navigator.clipboard.writeText(window.location.href)
    ElMessage.success('文章链接已复制')
  }
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  if (isFullscreen.value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
}



// 滚动监听，更新活动目录项
const updateActiveToc = () => {
  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3')
  let currentId = ''
  
  for (let i = headings.length - 1; i >= 0; i--) {
    const heading = headings[i] as HTMLElement
    const rect = heading.getBoundingClientRect()
    if (rect.top <= 100) {
      currentId = heading.id
      break
    }
  }
  
  if (currentId && currentId !== activeTocId.value) {
    activeTocId.value = currentId
  }
}

// 生命周期
onMounted(() => {
  const blogId = route.params.id as string
  if (blogId) {
    fetchBlogPost(blogId)
  }

  setTimeout(() => highlightQueryInContent(), 400)
  
  // 添加滚动监听
  window.addEventListener('scroll', updateActiveToc)
})

onUnmounted(() => {
  document.body.style.overflow = ''
  window.removeEventListener('scroll', updateActiveToc)
})

// ========= 全文搜索高亮与定位 =========
const removeOldHighlights = (root: HTMLElement) => {
  const marks = root.querySelectorAll('mark.search-hit')
  marks.forEach((m) => {
    const parent = m.parentNode as Node | null
    if (!parent) return
    while (m.firstChild) parent.insertBefore(m.firstChild, m)
    parent.removeChild(m)
  })
}

const highlightTextNodes = (root: Node, keyword: string) => {
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
    acceptNode(node) {
      const text = node.nodeValue || ''
      return text.trim().length > 0 && text.toLowerCase().includes(keyword.toLowerCase())
        ? NodeFilter.FILTER_ACCEPT
        : NodeFilter.FILTER_SKIP
    },
  })
  const hits: HTMLElement[] = []
  const regexp = new RegExp(keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi')
  let current: Node | null
  while ((current = walker.nextNode())) {
    const textNode = current as Text
    const frag = document.createDocumentFragment()
    let lastIndex = 0
    const text = textNode.nodeValue || ''
    text.replace(regexp, (match, offset) => {
      const before = text.slice(lastIndex, offset)
      if (before) frag.appendChild(document.createTextNode(before))
      const mark = document.createElement('mark')
      mark.className = 'search-hit'
      mark.textContent = match
      frag.appendChild(mark)
      hits.push(mark)
      lastIndex = offset + match.length
      return match
    })
    const after = text.slice(lastIndex)
    if (after) frag.appendChild(document.createTextNode(after))
    textNode.parentNode?.replaceChild(frag, textNode)
  }
  return hits
}

const highlightQueryInContent = () => {
  const q = (route.query.q as string) || ''
  console.log('BlogDetail - 接收到查询参数 q:', q)
  console.log('BlogDetail - 当前路由查询参数:', route.query)

  if (!q.trim()) {
    console.log('BlogDetail - 没有查询参数，跳过高亮')
    return
  }

  const container = document.querySelector('.blog-content') as HTMLElement | null
  if (!container) {
    console.log('BlogDetail - 找不到 .blog-content 容器')
    return
  }

  console.log('BlogDetail - 开始高亮关键词:', q.trim())
  removeOldHighlights(container)
  const hits = highlightTextNodes(container, q.trim())
  console.log('BlogDetail - 找到命中数量:', hits.length)

  if (hits.length > 0) {
    console.log('BlogDetail - 滚动到第一个命中位置')
    // 增加延迟确保DOM完全渲染
    setTimeout(() => {
      const firstHit = hits[0]
      const elementTop = firstHit.getBoundingClientRect().top + window.pageYOffset
      const offsetTop = 100 // 距离顶部的偏移量，避免被导航栏遮挡
      
      window.scrollTo({
        top: elementTop - offsetTop,
        behavior: 'smooth'
      })
    }, 200)
  }
}

watch(
  () => route.query.q,
  (newQ, oldQ) => {
    console.log('BlogDetail - 路由查询参数变化:', { newQ, oldQ })
    console.log('BlogDetail - 完整路由对象:', route)
    nextTick(() => {
      console.log('BlogDetail - 开始处理查询参数变化')
      highlightQueryInContent()
    })
  },
  { immediate: true }, // 立即执行一次
)

// 监听整个路由对象的变化
watch(
  () => route,
  (newRoute, oldRoute) => {
    console.log('BlogDetail - 路由对象变化:', {
      newPath: newRoute.path,
      newQuery: newRoute.query,
      oldPath: oldRoute?.path,
      oldQuery: oldRoute?.query,
    })
  },
  { deep: true },
)
</script>

<style scoped>
/* 基础布局 */
.blog-detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
}

.blog-layout {
  display: flex;
  gap: 24px;
  /* align-items: flex-start;  <-- 已移除，使侧边栏高度自动撑开 */
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
}

.blog-main {
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
}

.blog-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.sidebar-sticky {
  position: sticky;
  top: 24px; /* 距离顶部的距离 */
}

/* 目录卡片 */
.toc-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: none;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 100px); /* 限制最大高度，留出更多上下间距 */
}

.toc-card :deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0; /* 防止头部被压缩 */
}

.toc-card :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto; /* 让卡片主体内容区域可滚动 */
  min-height: 0; /* 关键：防止 flex 子项溢出 */
  padding: 10px 0; /* 调整内边距 */
}

.toc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
}

.toc-header .el-icon {
  color: #667eea;
}

.toc-content {
  /* 移除之前的滚动设置，交给 el-card__body 管理 */
  padding: 0 10px; 
}

/* 自定义滚动条 - 作用于 el-card__body */
.toc-card :deep(.el-card__body)::-webkit-scrollbar {
  width: 4px;
}

.toc-card :deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 2px;
}

.toc-card :deep(.el-card__body)::-webkit-scrollbar-track {
  background: transparent;
}

/* 目录列表样式 */
.toc-list {
  padding: 0;
}

.toc-item {
  padding: 8px 12px;
  border-radius: 8px;
  color: #666;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: center;
  border-left: 3px solid transparent;
  margin-bottom: 2px;
}

.toc-item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.toc-item.toc-active {
  background-color: #f0f4ff;
  color: #667eea;
  font-weight: 600;
  border-left-color: #667eea;
}

.toc-level-1 {
  font-weight: 600;
  font-size: 1rem;
}

.toc-level-2 {
  padding-left: 24px;
  font-size: 0.92rem;
}

.toc-level-3 {
  padding-left: 36px;
  font-size: 0.88rem;
  color: #909399;
}

.toc-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toc-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
  font-size: 0.9rem;
}

.toc-empty .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

/* 加载状态 */
.loading-container {
  max-width: 800px;
  margin: 0 auto;
}

.loading-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 博客卡片 */
.blog-card {
  margin-bottom: 20px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
}

.blog-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

/* 博客头部 */
.blog-header {
  padding: 30px 30px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.blog-title {
  margin: 0 0 20px 0;
  font-size: 2.2rem;
  color: #2c3e50;
  font-weight: 700;
  line-height: 1.3;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.blog-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  color: #666;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  backdrop-filter: blur(5px);
  border: 1px solid #eee;
}

.meta-item .el-icon {
  color: #409eff;
  font-size: 1rem;
}

.meta-item.reading-time {
  color: #67c23a;
  font-weight: 600;
}

/* 博客标签 */
.blog-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  border-radius: 20px;
  font-size: 0.8rem;
  padding: 4px 12px;
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 博客内容样式 */
.blog-content {
  padding: 30px;
  line-height: 1.8;
  color: #2c3e50;
  font-size: 1.05rem;
}

.markdown-body {
  font-family: 'Segoe UI', 'PingFang SC', 'Hiragino Sans', Arial, sans-serif;
}

/* 标题样式 */
.markdown-body :deep(h1) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom: 2px solid #667eea;
  padding-bottom: 12px;
  margin-top: 24px;
  margin-bottom: 16px;
  scroll-margin-top: 20px;
}

.markdown-body :deep(h2) {
  color: #2c3e50;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-top: 20px;
  margin-bottom: 14px;
  scroll-margin-top: 20px;
}

.markdown-body :deep(h3) {
  color: #2c3e50;
  margin-top: 18px;
  margin-bottom: 12px;
  scroll-margin-top: 20px;
}

/* 引用样式 */
.markdown-body :deep(blockquote) {
  border-left: 4px solid #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 0 8px 8px 0;
  padding: 15px 20px;
  margin: 20px 0;
}

/* 链接样式 */
.markdown-body :deep(a) {
  color: #667eea;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.markdown-body :deep(a:hover) {
  border-bottom-color: #667eea;
}

/* 表格样式 */
.markdown-body :deep(table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  width: 100%;
  margin: 20px 0;
}

.markdown-body :deep(th) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px;
}

.markdown-body :deep(td) {
  padding: 10px 12px;
}

.markdown-body :deep(tr:nth-child(even)) {
  background-color: #f8fafc;
}

.markdown-body :deep(tr:hover) {
  background-color: #f1f5f9;
}

/* 图片样式 */
.markdown-body :deep(img) {
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  max-width: 100%;
}

.markdown-body :deep(img:hover) {
  transform: scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* 代码块样式 */
.markdown-body :deep(pre) {
  border-radius: 8px;
  margin: 16px 0;
  position: relative;
  background: #f6f8fa !important;
  padding: 0 !important;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.markdown-body :deep(pre code) {
  font-family: 'Monaco', 'Courier New', monospace;
  display: block;
  padding: 16px;
  overflow-x: auto;
  max-height: none;
  transition: max-height 0.3s ease;
}

/* 代码块收起状态 */
.markdown-body :deep(pre.code-collapsed code) {
  max-height: 100px;
  overflow: hidden;
  position: relative;
}

.markdown-body :deep(pre.code-collapsed code::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(to bottom, transparent, #f6f8fa);
  pointer-events: none;
}

/* 代码工具栏 */
.markdown-body :deep(.code-toolbar) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #e8eaed;
  border-bottom: 1px solid #d0d4d9;
}

.markdown-body :deep(.code-lang) {
  font-size: 12px;
  font-weight: 600;
  color: #667eea;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.markdown-body :deep(.code-btn-group) {
  display: flex;
  gap: 8px;
}

.markdown-body :deep(.code-btn) {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  color: #606266;
  background: white;
  border: 1px solid #d0d4d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.markdown-body :deep(.code-btn:hover) {
  color: #409eff;
  border-color: #409eff;
  background: #ecf5ff;
}

.markdown-body :deep(.code-btn.copied) {
  color: #67c23a;
  border-color: #67c23a;
  background: #f0f9ff;
}

.markdown-body :deep(.code-btn span) {
  display: inline-block;
}

/* 行内代码 */
.markdown-body :deep(code:not(pre code)) {
  font-family: 'Monaco', 'Courier New', monospace;
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 3px;
  color: #e83e8c;
  font-size: 0.9em;
}

/* 博客底部 */
.blog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-top: 1px solid #f0f0f0;
  background: #fafbfc;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.footer-actions .el-button {
  border-radius: 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.footer-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.footer-info {
  color: #999;
  font-size: 0.9rem;
  font-style: italic;
}

/* 空状态容器 */
.empty-container {
  max-width: 600px;
  margin: 0 auto;
}

.empty-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 1100px) {
  .blog-sidebar {
    display: none; /* 在中等屏幕及以下隐藏侧边栏 */
  }
  
  .blog-layout {
    display: block;
    max-width: 1000px;
  }
}

@media (max-width: 768px) {
  .blog-detail-container {
    padding: 10px;
  }

  .blog-title {
    font-size: 1.8rem;
  }

  .blog-meta {
    gap: 10px;
  }

  .meta-item {
    font-size: 0.8rem;
    padding: 4px 8px;
  }

  .blog-content {
    padding: 20px;
    font-size: 1rem;
  }

  .blog-footer {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .footer-actions {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .blog-content {
    padding: 15px;
  }

  .blog-header {
    padding: 20px 20px 15px;
  }

  .blog-footer {
    padding: 15px 20px;
  }
}

/* 全文搜索高亮样式 */
.blog-content :deep(mark.search-hit) {
  background: #fef3c7;
  color: #92400e;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: 600;
  box-shadow: 0 0 0 2px rgba(251, 191, 36, 0.3);
  animation: highlight-pulse 1s ease-in-out;
}

@keyframes highlight-pulse {
  0%, 100% {
    box-shadow: 0 0 0 2px rgba(251, 191, 36, 0.3);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(251, 191, 36, 0.5);
  }
}
</style>
