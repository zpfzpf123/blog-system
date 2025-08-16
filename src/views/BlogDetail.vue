<template>
  <div class="blog-detail-container">
    <Breadcrumb />

    <div v-if="loading" class="loading-container">
      <el-card class="loading-card">
        <el-skeleton :rows="10" animated />
      </el-card>
    </div>

    <div v-else-if="blogPost" class="blog-layout">
      <div class="main-content">
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
            <div v-html="renderedContent"></div>
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

        <div class="recommendations-section">
          <PostRecommendations
            v-if="blogPost"
            :post-id="blogPost.id"
            :tags="blogPost.tags || []"
            :category="blogPost.category || { id: 0, name: '未分类' }"
            :title="blogPost.title"
            :content="blogPost.content || ''"
          />
        </div>
      </div>

      <div class="sidebar">
        <div class="toc-container">
          <el-card class="toc-card">
            <template #header>
              <div class="toc-header">
                <div class="toc-title">
                  <el-icon><List /></el-icon>
                  <span>文章目录</span>
                </div>
                <div class="toc-actions">
                  <el-button
                    size="small"
                    @click="toggleToc"
                    :icon="tocExpanded ? ArrowUp : ArrowDown"
                    text
                  />
                </div>
              </div>
            </template>

            <div class="toc-content" :class="{ expanded: tocExpanded }">
              <div v-if="tocItems.length > 0" class="toc-list">
                <div
                  v-for="item in tocItems"
                  :key="item.id"
                  class="toc-item"
                  :class="[`toc-level-${item.level}`, { active: currentHeadingId === item.id }]"
                  @click="scrollToHeading(item.id)"
                >
                  <span class="toc-text">{{ item.text }}</span>
                </div>
              </div>

              <div v-else class="toc-empty">
                <el-empty description="暂无目录" :image-size="60" />
                <p class="empty-tip">文章内容中未检测到标题结构</p>
              </div>

              <div v-if="tocItems.length > 0" class="toc-stats">
                <span>共 {{ tocItems.length }} 个章节</span>
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
  List,
  DocumentCopy,
  Share,
  FullScreen,
  Close,
  ArrowUp,
  ArrowDown,
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import Breadcrumb from '@/components/Breadcrumb.vue'
import PostRecommendations from '@/components/PostRecommendations.vue'
import MarkdownIt from 'markdown-it'
import anchor from 'markdown-it-anchor'
import toc from 'markdown-it-table-of-contents'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
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

interface TocItem {
  id: string
  text: string
  level: number
}

// 响应式数据
const blogPost = ref<BlogPost | null>(null)
const loading = ref(false)
const estimatedReadingTime = ref(0)
const currentHeadingId = ref('')
const isFullscreen = ref(false)
const tocExpanded = ref(true)

const route = useRoute()

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch {
        // 忽略错误
      }
    }
    return hljs.highlightAuto(str).value
  },
})

// 配置插件
md.use(anchor, {
  permalink: true,
  permalinkBefore: true,
  permalinkSymbol: '§',
  slugify: (str: string) => {
    // 使用与tocItems相同的ID生成逻辑
    return str.toLowerCase().replace(/[^a-z0-9\u4e00-\u9fa5]/g, '-')
  },
})

md.use(toc, {
  includeLevel: [1, 2, 3, 4, 5, 6],
  containerHeaderHtml: '<h2>目录</h2>',
})

// 计算属性
const renderedContent = computed(() => {
  if (!blogPost.value?.content) return ''

  // 处理图片路径
  const content = processImageUrlsInMarkdown(blogPost.value.content)

  // 渲染 markdown
  return md.render(content)
})

const tocItems = computed(() => {
  if (!blogPost.value?.content) return []

  const headings: TocItem[] = []
  const lines = blogPost.value.content.split('\n')

  lines.forEach((line) => {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      // 使用与markdown-it-anchor相同的ID生成逻辑
      const id = text.toLowerCase().replace(/[^a-z0-9\u4e00-\u9fa5]/g, '-')

      headings.push({ id, text, level })
    }
  })

  return headings
})

// 方法
const fetchBlogPost = async (id: string) => {
  try {
    loading.value = true
    const response = await axios.get(`/api/posts/${id}`)
    blogPost.value = response.data

    if (blogPost.value?.content) {
      estimatedReadingTime.value = calculateReadingTime(blogPost.value.content)
      await nextTick()
      setupScrollSync()
      // 渲染后进行查询高亮与定位
      await nextTick()
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
  const textContent = content.replace(/<[^>]*>/g, '')
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

const toggleToc = () => {
  tocExpanded.value = !tocExpanded.value
}

const scrollToHeading = (id: string) => {
  const element = document.getElementById(id)
  if (element) {
    const headerOffset = 80
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - headerOffset

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth',
    })
  }
}

const updateCurrentHeading = () => {
  const headings = document.querySelectorAll('h1, h2, h3, h4, h5, h6')
  if (headings.length === 0) return

  const viewportHeight = window.innerHeight
  const threshold = Math.min(100, viewportHeight * 0.2)

  let currentHeading: Element | null = null
  let minDistance = Infinity

  headings.forEach((heading) => {
    const rect = heading.getBoundingClientRect()
    const distance = Math.abs(rect.top - threshold)

    if (rect.top <= threshold && distance < minDistance) {
      minDistance = distance
      currentHeading = heading
    }
  })

  if (!currentHeading) {
    headings.forEach((heading) => {
      const rect = heading.getBoundingClientRect()
      const distance = Math.abs(rect.top)

      if (distance < minDistance) {
        minDistance = distance
        currentHeading = heading
      }
    })
  }

  if (currentHeading && (currentHeading as HTMLElement).id) {
    currentHeadingId.value = (currentHeading as HTMLElement).id
  }
}

const setupScrollSync = () => {
  setTimeout(() => {
    updateCurrentHeading()

    nextTick(() => {
      const headings = document.querySelectorAll('h1, h2, h3, h4, h5, h6')
      if (headings.length > 0) {
        handleScroll()

        setTimeout(() => {
          updateCurrentHeading()
          // 在滚动同步设置完成后添加代码复制按钮
          addCodeCopyButtons()
        }, 500)
      }
    })
  }, 300)
}

const handleScroll = () => {
  updateCurrentHeading()
}

// 节流函数
const throttle = (func: () => void, wait: number) => {
  let inThrottle: boolean
  return function executedFunction() {
    if (!inThrottle) {
      func()
      inThrottle = true
      setTimeout(() => (inThrottle = false), wait)
    }
  }
}

const throttledHandleScroll = throttle(handleScroll, 100)

// 监听内容变化，添加代码复制按钮
watch(renderedContent, () => {
  nextTick(() => {
    // 立即添加一次
    addCodeCopyButtons()

    // 延迟再次添加，确保DOM完全渲染
    setTimeout(() => {
      addCodeCopyButtons()
    }, 500)

    // 再次延迟检查，确保所有内容都已加载
    setTimeout(() => {
      addCodeCopyButtons()
    }, 1000)
    // 设置图片双击放大/还原
    setupImageZoom()
    setTimeout(() => setupImageZoom(), 500)
    setTimeout(() => setupImageZoom(), 1000)
  })
})

// 添加代码复制按钮的函数
const addCodeCopyButtons = () => {
  console.log('开始添加代码复制按钮...')
  const codeBlocks = document.querySelectorAll('pre code')
  console.log('找到代码块数量:', codeBlocks.length)

  codeBlocks.forEach((block, index) => {
    // 检查是否已经添加了复制按钮
    if (block.parentElement?.querySelector('.copy-code-btn')) {
      console.log(`代码块 ${index} 已存在复制按钮，跳过`)
      return
    }

    console.log(`为代码块 ${index} 添加复制按钮和折叠功能`)

    // 创建按钮容器
    const buttonContainer = document.createElement('div')
    buttonContainer.className = 'code-controls'
    buttonContainer.style.cssText = `
      position: absolute;
      top: 12px;
      right: 12px;
      display: flex;
      gap: 8px;
      z-index: 10;
    `

    // 创建折叠展开按钮
    const collapseButton = document.createElement('button')
    collapseButton.className = 'collapse-code-btn'
    collapseButton.innerHTML = `
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M6 9l6 6 6-6"/>
      </svg>
      <span>展开</span>
    `

    // 设置折叠按钮样式
    collapseButton.style.cssText = `
      background: rgba(0, 0, 0, 0.8);
      color: white;
      border: none;
      border-radius: 6px;
      padding: 8px 12px;
      font-size: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      gap: 6px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      opacity: 0.8;
    `

    // 创建复制按钮
    const copyButton = document.createElement('button')
    copyButton.className = 'copy-code-btn'
    copyButton.innerHTML = `
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
        <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
      </svg>
      <span>复制</span>
    `

    // 设置复制按钮样式
    copyButton.style.cssText = `
      background: rgba(0, 0, 0, 0.8);
      color: white;
      border: none;
      border-radius: 6px;
      padding: 8px 12px;
      font-size: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      gap: 6px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      opacity: 0.8;
    `

    // 将按钮添加到容器
    buttonContainer.appendChild(collapseButton)
    buttonContainer.appendChild(copyButton)

    // 悬停效果
    buttonContainer.addEventListener('mouseenter', () => {
      collapseButton.style.opacity = '1'
      collapseButton.style.background = 'rgba(0, 0, 0, 0.9)'
      collapseButton.style.transform = 'scale(1.05)'
      copyButton.style.opacity = '1'
      copyButton.style.background = 'rgba(0, 0, 0, 0.9)'
      copyButton.style.transform = 'scale(1.05)'
    })

    buttonContainer.addEventListener('mouseleave', () => {
      collapseButton.style.opacity = '0.8'
      collapseButton.style.background = 'rgba(0, 0, 0, 0.8)'
      collapseButton.style.transform = 'scale(1)'
      copyButton.style.opacity = '0.8'
      copyButton.style.background = 'rgba(0, 0, 0, 0.8)'
      copyButton.style.transform = 'scale(1)'
    })

    // 折叠展开功能
    let isCollapsed = true // 默认折叠
    const preElement = block.parentElement

    if (preElement) {
      // 保存原始高度
      const originalHeight = preElement.scrollHeight
      const maxHeight = Math.min(originalHeight, 200) // 最大显示200px高度

      // 初始状态：折叠
      preElement.style.maxHeight = `${maxHeight}px`
      preElement.style.overflow = 'hidden'
      preElement.style.transition = 'max-height 0.3s ease'

      // 添加渐变遮罩
      const overlay = document.createElement('div')
      overlay.className = 'code-overlay'
      overlay.style.cssText = `
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 40px;
        background: linear-gradient(transparent, #1a202c);
        pointer-events: none;
        z-index: 5;
      `
      preElement.appendChild(overlay)

      // 折叠展开点击事件
      collapseButton.addEventListener('click', (e) => {
        e.preventDefault()
        e.stopPropagation()

        if (isCollapsed) {
          // 展开
          preElement.style.maxHeight = `${originalHeight}px`
          overlay.style.display = 'none'
          collapseButton.innerHTML = `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 15l-6-6-6 6"/>
            </svg>
            <span>折叠</span>
          `
          isCollapsed = false
        } else {
          // 折叠
          preElement.style.maxHeight = `${maxHeight}px`
          overlay.style.display = 'block'
          collapseButton.innerHTML = `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 9l6 6 6-6"/>
            </svg>
            <span>展开</span>
          `
          isCollapsed = true
        }
      })
    }

    // 点击复制功能
    copyButton.addEventListener('click', async (e) => {
      e.preventDefault()
      e.stopPropagation()

      try {
        const codeText = block.textContent || ''
        await navigator.clipboard.writeText(codeText)

        // 更新按钮状态
        const span = copyButton.querySelector('span')
        if (span) {
          span.textContent = '已复制!'
        }
        copyButton.style.background = 'rgba(34, 197, 94, 0.9)'
        copyButton.style.transform = 'scale(1.1)'

        // 2秒后恢复原状态
        setTimeout(() => {
          if (span) {
            span.textContent = '复制'
          }
          copyButton.style.background = 'rgba(0, 0, 0, 0.8)'
          copyButton.style.transform = 'scale(1)'
        }, 2000)

        ElMessage.success('代码已复制到剪贴板')
      } catch (err) {
        console.error('复制失败:', err)
        ElMessage.error('复制失败，请手动复制')
      }
    })

    // 将按钮容器添加到pre元素
    if (preElement) {
      preElement.style.position = 'relative'
      preElement.appendChild(buttonContainer)

      // 确保pre元素有足够的右边距来显示按钮
      if (!preElement.style.paddingRight || parseInt(preElement.style.paddingRight) < 160) {
        preElement.style.paddingRight = '160px'
      }

      console.log(`代码块 ${index} 的复制按钮和折叠功能已添加`)
    } else {
      console.error(`代码块 ${index} 的父元素不存在`)
    }
  })

  console.log('代码复制按钮和折叠功能添加完成')
}

// 图片双击放大/还原功能
let currentZoomedImg: HTMLImageElement | null = null
let imageOverlayEl: HTMLDivElement | null = null

const createImageOverlay = () => {
  if (imageOverlayEl) return imageOverlayEl
  const overlay = document.createElement('div')
  overlay.className = 'image-zoom-overlay'
  overlay.style.position = 'fixed'
  overlay.style.inset = '0'
  overlay.style.background = 'rgba(0,0,0,0.6)'
  overlay.style.zIndex = '9998'
  overlay.style.opacity = '1'
  overlay.style.cursor = 'zoom-out'
  overlay.addEventListener('dblclick', () => {
    if (currentZoomedImg) {
      toggleImageZoom(currentZoomedImg)
    }
  })
  document.body.appendChild(overlay)
  imageOverlayEl = overlay
  return overlay
}

const removeImageOverlay = () => {
  if (imageOverlayEl) {
    imageOverlayEl.remove()
    imageOverlayEl = null
  }
}

const toggleImageZoom = (img: HTMLImageElement) => {
  const isZoomed = img.classList.contains('zoomed')
  if (isZoomed) {
    img.classList.remove('zoomed')
    currentZoomedImg = null
    removeImageOverlay()
    // 若未处于全文全屏模式，恢复滚动
    if (!isFullscreen.value) {
      document.body.style.overflow = ''
    }
    document.body.classList.remove('image-zoom-active')
    img.style.removeProperty('will-change')
  } else {
    // 关闭其他已放大图片
    if (currentZoomedImg && currentZoomedImg !== img) {
      currentZoomedImg.classList.remove('zoomed')
    }
    currentZoomedImg = img
    createImageOverlay()
    img.classList.add('zoomed')
    document.body.style.overflow = 'hidden'
    document.body.classList.add('image-zoom-active')
    img.style.willChange = 'transform'
  }
}

const setupImageZoom = () => {
  const imgs = Array.from(document.querySelectorAll('.blog-content img')) as HTMLImageElement[]
  imgs.forEach((img) => {
    if ((img as any)._zoomBound) return
    ;(img as any)._zoomBound = true
    img.style.cursor = 'zoom-in'
    // 避免浏览器在图片上触发默认的拖拽、选择等引起抖动
    img.setAttribute('draggable', 'false')
    img.style.userSelect = 'none'
    img.style.webkitUserSelect = 'none'
    // 兼容性属性移除，TypeScript下不再设置 msUserSelect
    img.addEventListener('dblclick', (e) => {
      e.preventDefault()
      e.stopPropagation()
      toggleImageZoom(img)
      // 切换指针样式
      img.style.cursor = img.classList.contains('zoomed') ? 'zoom-out' : 'zoom-in'
    })
  })
}

// 生命周期
onMounted(() => {
  const blogId = route.params.id as string
  if (blogId) {
    fetchBlogPost(blogId)
  }

  window.addEventListener('scroll', throttledHandleScroll, { passive: true })
  window.addEventListener('resize', updateCurrentHeading, { passive: true })
  // 初次挂载后，确保图片放大功能绑定
  setTimeout(() => setupImageZoom(), 300)
  // 如果直接带有查询参数，尝试高亮
  setTimeout(() => highlightQueryInContent(), 400)
})

onUnmounted(() => {
  window.removeEventListener('scroll', throttledHandleScroll)
  window.removeEventListener('resize', updateCurrentHeading)
  document.body.style.overflow = ''
  // 清理可能残留的遮罩
  removeImageOverlay()
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
    setTimeout(() => {
      hits[0].scrollIntoView({ behavior: 'smooth', block: 'center' })
    }, 50)
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
  gap: 30px;
  align-items: flex-start;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
  align-self: flex-start;
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

/* 博客内容 */
.blog-content {
  padding: 30px;
  line-height: 1.8;
  color: #2c3e50;
  font-size: 1.05rem;
}

.blog-content :deep(h1) {
  font-size: 2rem;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
  margin: 30px 0 20px 0;
  color: #2c3e50;
}

.blog-content :deep(h2) {
  font-size: 1.7rem;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
  margin: 25px 0 18px 0;
  color: #2c3e50;
}

.blog-content :deep(h3) {
  font-size: 1.4rem;
  margin: 20px 0 16px 0;
  color: #2c3e50;
}

.blog-content :deep(h4) {
  font-size: 1.2rem;
  margin: 18px 0 14px 0;
  color: #2c3e50;
}

.blog-content :deep(h5) {
  font-size: 1.1rem;
  margin: 16px 0 12px 0;
  color: #2c3e50;
}

.blog-content :deep(h6) {
  font-size: 1rem;
  margin: 14px 0 10px 0;
  color: #2c3e50;
}

.blog-content :deep(p) {
  margin: 12px 0;
  font-size: 1rem;
  line-height: 1.8;
}

.blog-content :deep(a) {
  color: #667eea;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.blog-content :deep(a:hover) {
  border-bottom-color: #667eea;
}

.blog-content :deep(strong) {
  font-weight: 700;
  color: #1a202c;
}

.blog-content :deep(em) {
  font-style: italic;
  color: #4a5568;
}

.blog-content :deep(blockquote) {
  margin: 20px 0;
  padding: 15px 20px;
  border-left: 4px solid #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 0 8px 8px 0;
  color: #4a5568;
}

.blog-content :deep(blockquote p) {
  margin: 0;
}

.blog-content :deep(ul),
.blog-content :deep(ol) {
  padding-left: 30px;
  margin: 15px 0;
}

.blog-content :deep(ul li),
.blog-content :deep(ol li) {
  margin: 8px 0;
}

.blog-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  display: block;
  margin: 15px auto;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.blog-content :deep(img:hover) {
  transform: scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* 图片双击放大样式 */
.blog-content :deep(img) {
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.blog-content :deep(img.zoomed) {
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) translateZ(0) !important;
  max-width: 92vw !important;
  max-height: 92vh !important;
  width: auto !important;
  height: auto !important;
  z-index: 9999 !important;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.35) !important;
  cursor: zoom-out !important;
  pointer-events: none !important; /* 避免图片拦截鼠标事件导致布局反复计算 */
  backface-visibility: hidden !important;
  -webkit-backface-visibility: hidden !important;
  user-select: none !important;
  transition: none !important; /* 放大状态禁用过渡，避免抖动 */
}

/* 当图片放大时，将鼠标事件交给遮罩，避免hover/布局抖动 */
.image-zoom-overlay {
  cursor: zoom-out !important;
  will-change: opacity;
}

/* 放大模式下，禁用正文区域图片的悬停过渡，避免闪烁 */
:global(body.image-zoom-active) .blog-content :deep(img) {
  transition: none !important;
}

:global(body.image-zoom-active) .blog-content :deep(img:hover) {
  transform: none !important;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15) !important;
}

.blog-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.blog-content :deep(th),
.blog-content :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 12px 16px;
  text-align: left;
}

.blog-content :deep(th) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.blog-content :deep(tr:nth-child(even)) {
  background-color: #f8fafc;
}

.blog-content :deep(tr:hover) {
  background-color: #f1f5f9;
}

.blog-content :deep(hr) {
  border: none;
  border-top: 2px solid #e2e8f0;
  margin: 30px 0;
}

.blog-content :deep(pre) {
  background: #1a202c;
  color: #f8f8f2;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  overflow: auto;
  position: relative;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.blog-content :deep(code) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.blog-content :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
  border-radius: 0;
  font-size: inherit;
}

/* 代码复制按钮样式 */
.blog-content :deep(.code-controls) {
  position: absolute !important;
  top: 12px !important;
  right: 12px !important;
  display: flex !important;
  gap: 8px !important;
  z-index: 10 !important;
}

.blog-content :deep(.copy-code-btn),
.blog-content :deep(.collapse-code-btn) {
  background: rgba(0, 0, 0, 0.8) !important;
  color: white !important;
  border: none !important;
  border-radius: 6px !important;
  padding: 8px 12px !important;
  font-size: 12px !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
  display: flex !important;
  align-items: center !important;
  gap: 6px !important;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
  opacity: 0.8 !important;
}

.blog-content :deep(.copy-code-btn:hover),
.blog-content :deep(.collapse-code-btn:hover) {
  opacity: 1 !important;
  background: rgba(0, 0, 0, 0.9) !important;
  transform: scale(1.05) !important;
}

.blog-content :deep(.copy-code-btn svg),
.blog-content :deep(.collapse-code-btn svg) {
  flex-shrink: 0;
}

.blog-content :deep(.copy-code-btn span),
.blog-content :deep(.collapse-code-btn span) {
  white-space: nowrap;
}

/* 代码渐变遮罩 */
.blog-content :deep(.code-overlay) {
  position: absolute !important;
  bottom: 0 !important;
  left: 0 !important;
  right: 0 !important;
  height: 40px !important;
  background: linear-gradient(transparent, #1a202c) !important;
  pointer-events: none !important;
  z-index: 5 !important;
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

/* 推荐内容 */
.recommendations-section {
  margin-top: 20px;
}

/* 目录容器 */
.toc-container {
  width: 100%;
}

.toc-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
}

.toc-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.toc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.toc-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
  font-weight: 600;
}

.toc-title .el-icon {
  font-size: 1.2rem;
}

.toc-actions .el-button {
  color: white;
  border-color: rgba(255, 255, 255, 0.3);
}

.toc-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 目录内容 */
.toc-content {
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  transition: max-height 0.3s ease;
}

.toc-content.expanded {
  max-height: 600px;
}

/* 自定义滚动条样式 */
.toc-content::-webkit-scrollbar {
  width: 6px;
}

.toc-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.toc-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.toc-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 目录列表 */
.toc-list {
  padding: 16px 0;
}

.toc-item {
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
  position: relative;
}

.toc-item:hover {
  background: rgba(102, 126, 234, 0.05);
  border-left-color: #667eea;
  transform: translateX(4px);
}

.toc-item.active {
  background: rgba(102, 126, 234, 0.1);
  border-left-color: #667eea;
  color: #667eea;
  font-weight: 600;
}

.toc-text {
  font-size: 0.9rem;
  line-height: 1.5;
}

/* 目录层级样式 */
.toc-level-1 {
  padding-left: 24px;
  font-weight: 600;
  font-size: 1rem;
}

.toc-level-2 {
  padding-left: 40px;
  font-weight: 500;
  font-size: 0.9rem;
}

.toc-level-3 {
  padding-left: 56px;
  font-weight: 400;
  font-size: 0.85rem;
}

.toc-level-4 {
  padding-left: 72px;
  font-weight: 400;
  font-size: 0.8rem;
}

.toc-level-5 {
  padding-left: 88px;
  font-weight: 400;
  font-size: 0.75rem;
}

.toc-level-6 {
  padding-left: 104px;
  font-weight: 400;
  font-size: 0.7rem;
}

/* 空目录状态 */
.toc-empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-tip {
  margin-top: 10px;
  font-size: 0.9rem;
  color: #bbb;
}

/* 目录统计 */
.toc-stats {
  padding: 16px 24px;
  text-align: center;
  background: rgba(102, 126, 234, 0.05);
  border-top: 1px solid #e2e8f0;
  color: #667eea;
  font-size: 0.9rem;
  font-weight: 600;
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
@media (max-width: 1200px) {
  .blog-layout {
    gap: 20px;
  }

  .sidebar {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .blog-detail-container {
    padding: 10px;
  }

  .blog-layout {
    flex-direction: column;
    gap: 20px;
  }

  .sidebar {
    width: 100%;
    position: static;
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

  .toc-content {
    max-height: none;
    overflow: visible;
  }

  .toc-content.expanded {
    max-height: none;
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

  .toc-header {
    padding: 15px 20px;
  }

  .toc-item {
    padding: 10px 20px;
  }
}
</style>
