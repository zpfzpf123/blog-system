<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, 
  Folder, 
  Clock, 
  User, 
  Link as LinkIcon,
  Document,
  TrendCharts,
  Calendar,
  FolderOpened,
  ArrowUpBold,
  Location,
  DocumentCopy,
  List,
  Refresh,
  Upload,
  Loading,
  DArrowLeft,
  Search,
  Connection,
  Monitor,
  Edit,
  Delete,
  MoreFilled
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import markdownItToc from 'markdown-it-table-of-contents'
import hljs from 'highlight.js'
import 'github-markdown-css/github-markdown.css'
import 'highlight.js/styles/github.css'
import GitCommitModal from '@/components/GitCommitModalNew.vue'
import FileHistoryModal from '@/components/FileHistoryModal.vue'
import GitPullModal from '@/components/GitPullModal.vue'
import GitStatusPanel from '@/components/GitStatusPanel.vue'
import GitHistoryModal from '@/components/GitHistoryModal.vue'
import GitBranchModal from '@/components/GitBranchModal.vue'
import GitStashModal from '@/components/GitStashModal.vue'
import GitFilesModal from '@/components/GitFilesModal.vue'
import GitConflictModal from '@/components/GitConflictModal.vue'
import DevEnvironmentPanel from '@/components/DevEnvironmentPanel.vue'
import ApiDocPanel from '@/components/ApiDocPanel.vue'

// 配置 Markdown-it
const md: MarkdownIt = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>'
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + str.replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</code></pre>'
  }
})

// 使用插件
md.use(markdownItAnchor, {
  permalink: markdownItAnchor.permalink.headerLink({
    safariReaderFix: true,
  })
})
md.use(markdownItToc, {
  includeLevel: [1, 2, 3, 4, 5, 6],
  containerClass: 'table-of-contents',
  listType: 'ul'
})

interface Project {
  id: number
  name: string
  description: string
  status: string
  progress: number
  techStack: string[]
  localPath?: string
  repoUrl?: string
  gitCommits?: string
  gitUserId?: number
  createdAt: string
  updatedAt: string
}

interface GitCommit {
  hash: string
  author: string
  date: string
  message: string
}

const route = useRoute()
const router = useRouter()
const projectId = computed(() => route.params.id as string)
const project = ref<Project | null>(null)
const loading = ref(false)
const activeTab = ref('readme')
const showBackTop = ref(false)
const scrollProgress = ref(0)
const readmeContentRef = ref<HTMLElement | null>(null)
const commitsContentRef = ref<HTMLElement | null>(null)
const prepareLogsRef = ref<HTMLElement | null>(null)
const tocItems = ref<Array<{id: string, text: string, level: number}>>([])
const showToc = ref(true) // 目录侧边栏显示状态，默认打开
const refreshingCommits = ref(false)
const showGitCommitModal = ref(false) // Modal visibility state
const showFileHistoryModal = ref(false) // 文件历史弹窗
const showGitPullModal = ref(false) // Git拉取弹窗
const showGitHistoryModal = ref(false) // Git历史弹窗
const showGitBranchModal = ref(false) // Git分支管理弹窗
const showGitStashModal = ref(false) // Git Stash弹窗
const showGitFilesModal = ref(false) // Git文件变更弹窗
const showGitConflictModal = ref(false) // Git冲突管理弹窗
const gitStatusPanelRef = ref<InstanceType<typeof GitStatusPanel> | null>(null) // Git状态面板引用
const initialLoading = ref(true) // 初始加载状态
let autoRefreshTimer: ReturnType<typeof setInterval> | null = null // 自动刷新定时器

// README编辑相关
const isEditingReadme = ref(false) // 是否处于编辑模式
const readmeEditContent = ref('') // 编辑中的内容
let autoSaveTimer: ReturnType<typeof setTimeout> | null = null // 自动保存定时器
const readmeContentDivRef = ref<HTMLElement | null>(null) // README内容区域引用
const editorTextareaRef = ref<any>(null) // 编辑器 textarea 引用
const previewPaneRef = ref<HTMLElement | null>(null) // 预览区域引用

// 分页相关
const currentPage = ref(1)
const pageSize = ref(50) // 每页显示50条
const isLoadingMore = ref(false)

// 提交记录搜索和筛选
const searchKeyword = ref('') // 搜索关键词
const filterAuthor = ref('') // 筛选作者
const filterDateRange = ref<[string, string] | null>(null) // 日期范围（YYYY-MM-DD字符串）
const filterFilePath = ref('') // 文件路径筛选
const showSearchPanel = ref(false) // 是否显示搜索面板

// 导航菜单项
const navItems = [
  { id: 'readme', label: '项目文档', icon: Document },
  { id: 'commits', label: '提交历史', icon: Clock },
  { id: 'apis', label: 'API文档', icon: Connection },
  { id: 'devenv', label: '开发环境', icon: Monitor },
]

// 获取所有作者列表（用于下拉选择）
const authorList = computed(() => {
  const authors = new Set<string>()
  gitCommits.value.forEach(commit => {
    if (commit.author) authors.add(commit.author)
  })
  return Array.from(authors).sort()
})

// 筛选条件变化时重置分页
watch([searchKeyword, filterAuthor, filterDateRange, filterFilePath], () => {
  currentPage.value = 1
})

// 辅助函数：检查元素是否在代码块内
const isInCodeBlock = (element: Element, container: Element): boolean => {
  let parent = element.parentElement
  while (parent && parent !== container) {
    if (parent.tagName === 'PRE' || parent.tagName === 'CODE') {
      return true
    }
    parent = parent.parentElement
  }
  return false
}

// 目录加载状态
const tocLoading = ref(false)

// 生成目录 - 异步非阻塞方式
const generateToc = (retryCount = 0) => {
  // 使用 requestIdleCallback 或 setTimeout 让出主线程
  const scheduleTask = window.requestIdleCallback || ((cb: () => void) => setTimeout(cb, 10))
  
  scheduleTask(() => {
    const container = readmeContentRef.value
    if (!container) {
      if (retryCount < 3) {
        setTimeout(() => generateToc(retryCount + 1), 200)
      }
      return
    }
    
    const markdownBody = container.querySelector('.markdown-body')
    if (!markdownBody) {
      if (retryCount < 3) {
        setTimeout(() => generateToc(retryCount + 1), 200)
      }
      return
    }
    
    const headers = markdownBody.querySelectorAll('h1, h2, h3, h4, h5, h6')
    if (headers.length === 0) {
      tocItems.value = []
      return
    }
    
    tocLoading.value = true
    
    // 收集目录项（不阻塞）
    const newTocItems: Array<{id: string, text: string, level: number}> = []
    let validIndex = 0
    
    headers.forEach((header) => {
      if (isInCodeBlock(header, markdownBody)) return
      
      if (!header.id) {
        header.id = `heading-${validIndex}`
      }
      
      newTocItems.push({
        id: header.id,
        text: header.textContent || '',
        level: parseInt(header.tagName.substring(1))
      })
      
      validIndex++
    })
    
    // 一次性更新，减少响应式触发次数
    tocItems.value = newTocItems
    tocLoading.value = false
  })
}

// 初始化图片懒加载
const initLazyLoad = () => {
  const container = readmeContentRef.value
  if (!container) return
  
  const lazyImages = container.querySelectorAll('img.lazy-image')
  
  if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target as HTMLImageElement
          const dataSrc = img.getAttribute('data-src')
          if (dataSrc) {
            img.src = dataSrc
            img.classList.remove('lazy-image')
            img.classList.add('loaded')
            imageObserver.unobserve(img)
          }
        }
      })
    }, {
      rootMargin: '50px' // 提前50px开始加载
    })
    
    lazyImages.forEach(img => imageObserver.observe(img))
  } else {
    // 不支持IntersectionObserver时直接加载所有图片
    lazyImages.forEach((img: Element) => {
      const htmlImg = img as HTMLImageElement
      const dataSrc = htmlImg.getAttribute('data-src')
      if (dataSrc) {
        htmlImg.src = dataSrc
      }
    })
  }
}

// 跳转到标题
const scrollToHeading = (id: string) => {
  if (isEditingReadme.value) {
    // 编辑模式：同时滚动编辑器和预览区域
    const tocItem = tocItems.value.find(item => item.id === id)
    if (!tocItem) return
    
    // 1. 滚动预览区域
    const previewPane = previewPaneRef.value
    if (previewPane) {
      // 使用 getElementById 避免 ID 中特殊字符导致的选择器错误
      const element = document.getElementById(id) as HTMLElement
      
      if (element) {
        const elementRect = element.getBoundingClientRect()
        const paneRect = previewPane.getBoundingClientRect()
        const offsetTop = previewPane.scrollTop + (elementRect.top - paneRect.top) - 20
        
        previewPane.scrollTo({
          top: offsetTop,
          behavior: 'smooth'
        })
      }
    }
    
    // 2. 滚动编辑器 textarea 到对应行
    if (editorTextareaRef.value) {
      const lines = readmeEditContent.value.split('\n')
      const targetText = tocItem.text
      const headingPrefix = '#'.repeat(tocItem.level)
      
      // 寻找匹配的标题行
      let targetLineIndex = -1
      for (let i = 0; i < lines.length; i++) {
        const trimmedLine = lines[i].trim()
        const headingRegex = new RegExp(`^${headingPrefix}\\s+(.+)$`)
        const match = trimmedLine.match(headingRegex)
        
        if (match && match[1].trim() === targetText) {
          targetLineIndex = i
          break
        }
      }
      
      if (targetLineIndex !== -1) {
        let cursorPosition = 0
        for (let i = 0; i < targetLineIndex; i++) {
          cursorPosition += lines[i].length + 1
        }
        
        const textarea = editorTextareaRef.value.$el?.querySelector('textarea') || editorTextareaRef.value.$refs?.textarea || editorTextareaRef.value
        
        if (textarea && textarea.setSelectionRange) {
          textarea.focus()
          textarea.setSelectionRange(cursorPosition, cursorPosition + lines[targetLineIndex].length)
          
          const computedStyle = window.getComputedStyle(textarea)
          const lineHeight = parseFloat(computedStyle.lineHeight) || 22
          const textareaHeight = textarea.clientHeight
          const targetScrollTop = (targetLineIndex * lineHeight) - (textareaHeight * 0.25)
          
          textarea.scrollTo({
            top: Math.max(0, targetScrollTop),
            behavior: 'smooth'
          })
        }
      }
    }
  } else {
    // 阅读模式：滚动容器到目标位置
    const container = readmeContentRef.value
    if (!container) return
    
    // 使用 getElementById 避免 ID 中特殊字符导致的选择器错误
    const element = document.getElementById(id) as HTMLElement
    if (!element) return
    
    // 计算元素相对于容器的位置
    const elementRect = element.getBoundingClientRect()
    const containerRect = container.getBoundingClientRect()
    const offsetTop = container.scrollTop + (elementRect.top - containerRect.top) - 20
    
    // 平滑滚动到目标位置
    container.scrollTo({
      top: offsetTop,
      behavior: 'smooth'
    })
  }
}

// 切换目录显示
const toggleToc = () => {
  showToc.value = !showToc.value
}

// 编辑模式的预览HTML
const previewHtml = computed(() => {
  if (!readmeEditContent.value) return ''
  try {
    return md.render(readmeEditContent.value)
  } catch (error) {
    console.error('预览HTML处理失败:', error)
    return readmeEditContent.value
  }
})

const gitCommits = computed<GitCommit[]>(() => {
  if (!project.value?.gitCommits) return []
  try {
    return JSON.parse(project.value.gitCommits)
  } catch {
    return []
  }
})

// 筛选后的提交记录
const filteredCommits = computed(() => {
  let commits = gitCommits.value
  
  // 按关键词搜索（搜索提交信息和hash）
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    commits = commits.filter(commit => 
      commit.message.toLowerCase().includes(keyword) ||
      commit.hash.toLowerCase().includes(keyword)
    )
  }
  
  // 按作者筛选
  if (filterAuthor.value) {
    commits = commits.filter(commit => commit.author === filterAuthor.value)
  }
  
  // 按日期范围筛选
  if (filterDateRange.value && filterDateRange.value.length === 2) {
    const [startDateStr, endDateStr] = filterDateRange.value
    const startDate = startDateStr ? new Date(`${startDateStr}T00:00:00`) : null
    const endDate = endDateStr ? new Date(`${endDateStr}T23:59:59`) : null
    commits = commits.filter(commit => {
      const commitDate = new Date(commit.date)
      if (Number.isNaN(commitDate.getTime())) return false
      if (startDate && commitDate < startDate) return false
      if (endDate && commitDate > endDate) return false
      return true
    })
  }
  
  // 按文件路径筛选（假设提交信息中包含文件路径，实际可能需要后端支持）
  if (filterFilePath.value.trim()) {
    const filePath = filterFilePath.value.toLowerCase().trim()
    commits = commits.filter(commit => 
      commit.message.toLowerCase().includes(filePath)
    )
  }
  
  // 按日期降序排序（最新的在前面）
  return commits.sort((a, b) => {
    const dateA = new Date(a.date).getTime()
    const dateB = new Date(b.date).getTime()
    return dateB - dateA
  })
})

// 分页后的提交记录
const paginatedCommits = computed(() => {
  const total = filteredCommits.value.length
  if (total <= 100) {
    // 少于100条直接全部显示
    return filteredCommits.value
  }
  // 超过100条使用分页
  return filteredCommits.value.slice(0, currentPage.value * pageSize.value)
})

// 是否还有更多数据
const hasMore = computed(() => {
  return paginatedCommits.value.length < filteredCommits.value.length
})

// 重置所有筛选条件
const resetFilters = () => {
  searchKeyword.value = ''
  filterAuthor.value = ''
  filterDateRange.value = null
  filterFilePath.value = ''
  currentPage.value = 1
}

// 是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return searchKeyword.value.trim() !== '' ||
         filterAuthor.value !== '' ||
         filterDateRange.value !== null ||
         filterFilePath.value.trim() !== ''
})

// 项目描述HTML
const readmeHtml = computed(() => {
  const content = project.value?.description
  if (!content) return ''
  try {
    return md.render(content)
  } catch (error) {
    console.error('项目描述HTML处理失败:', error)
    return content
  }
})
watch(readmeHtml, () => {
  // 等待DOM更新后再生成目录
  nextTick(() => {
    generateToc()
    initLazyLoad()
  })
})

const fetchProjectDetail = async () => {
  try {
    loading.value = true
    const id = route.params.id
    const response = await axios.get(`/api/projects/${id}`)
    project.value = response.data
    
    // 解析techStack
    if (project.value && typeof project.value.techStack === 'string') {
      try {
        project.value.techStack = JSON.parse(project.value.techStack)
      } catch {
        project.value.techStack = []
      }
    }
    
    // 初始加载完成 - 立即设置，不再延迟
    initialLoading.value = false
  } catch (error) {
    console.error('获取项目详情失败:', error)
    initialLoading.value = false // 错误时也要关闭加载状态
    ElMessage({
      message: '获取项目详情失败',
      type: 'error',
      duration: 3000,
      offset: 60
    })
    router.push('/project-manager')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/project-manager')
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    '进行中': 'primary',
    '已完成': 'success',
    '暂停': 'warning',
    '计划中': 'info'
  }
  return typeMap[status] || 'info'
}

const getProgressColor = (progress: number) => {
  if (progress >= 80) return '#10b981'
  if (progress >= 50) return '#6366f1'
  if (progress >= 30) return '#f59e0b'
  return '#ef4444'
}

// 获取当前激活标签页的滚动容器
const getCurrentScrollContainer = () => {
  return activeTab.value === 'readme' ? readmeContentRef.value : commitsContentRef.value
}

// 监听标签页内容滚动
const handleScroll = () => {
  const container = getCurrentScrollContainer()
  if (container) {
    const scrollTop = container.scrollTop
    const scrollHeight = container.scrollHeight
    const clientHeight = container.clientHeight
    
    // 计算滚动百分比
    const scrollable = scrollHeight - clientHeight
    if (scrollable > 0) {
      scrollProgress.value = Math.round((scrollTop / scrollable) * 100)
    } else {
      scrollProgress.value = 0
    }
    
    showBackTop.value = scrollTop > 300
  }
}

// 回到顶部
const scrollToTop = () => {
  const container = getCurrentScrollContainer()
  if (container) {
    container.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

// 标签切换时重置滚动位置和按钮状态
const handleTabChange = (tabId: string) => {
  activeTab.value = tabId
  showBackTop.value = false
  
  // 切换到提交历史时
  if (activeTab.value === 'commits') {
    currentPage.value = 1 // 重置分页
    
    // 立即刷新一次
    refreshGitCommits()
    
    // 启动自动刷新定时器（每60秒）
    if (autoRefreshTimer) {
      clearInterval(autoRefreshTimer)
    }
    autoRefreshTimer = setInterval(() => {
      refreshGitCommits()
    }, 60000) // 60秒
  } else {
    // 离开提交历史页面，停止自动刷新
    if (autoRefreshTimer) {
      clearInterval(autoRefreshTimer)
      autoRefreshTimer = null
    }
  }
  
  nextTick(() => {
    scrollProgress.value = 0
    
    if (activeTab.value === 'readme') {
      generateToc()
      initLazyLoad()
    }
  })
}

// 加载更多提交记录
const loadMoreCommits = async () => {
  if (isLoadingMore.value || !hasMore.value) return
  
  isLoadingMore.value = true
  // 模拟加载延迟，让用户看到加载状态
  await new Promise(resolve => setTimeout(resolve, 300))
  currentPage.value++
  isLoadingMore.value = false
}

// 监听提交列表滚动，实现无限滚动
const handleCommitsScroll = (event: Event) => {
  handleScroll() // 调用原有的滚动处理
  
  const container = event.target as HTMLElement
  const scrollTop = container.scrollTop
  const scrollHeight = container.scrollHeight
  const clientHeight = container.clientHeight
  
  // 距离底部100px时加载更多
  if (scrollHeight - scrollTop - clientHeight < 100 && hasMore.value) {
    loadMoreCommits()
  }
}

// 打开本地文件夹
const openLocalPath = async () => {
  if (!project.value?.id) return
  
  try {
    const response = await axios.post(`/api/projects/${project.value.id}/open-folder`)
    ElMessage({
      message: response.data.message || '文件夹已打开',
      type: 'success',
      duration: 2000,
      offset: 60
    })
  } catch (error: any) {
    console.error('打开文件夹失败:', error)
    const errorMessage = error.response?.data?.message || '打开文件夹失败'
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 3000,
      offset: 60
    })
  }
}

// 复制本地路径
const copyLocalPath = async (event: Event) => {
  event.stopPropagation() // 阻止冒泡，不触发打开文件夹
  
  if (!project.value?.localPath) return
  
  try {
    await navigator.clipboard.writeText(project.value.localPath)
    ElMessage({
      message: '本地路径已复制到剪贴板！',
      type: 'success',
      duration: 2000,
      offset: 60
    })
  } catch (error) {
    ElMessage({
      message: '无法复制路径，请手动复制：' + project.value.localPath,
      type: 'warning',
      duration: 3000,
      offset: 60
    })
  }
}

// 打开Git仓库
const openGitRepo = () => {
  if (!project.value?.repoUrl) return
  window.open(project.value.repoUrl, '_blank')
}

// 刷新Git提交记录
const refreshGitCommits = async () => {
  if (!project.value?.id) return
  
  try {
    refreshingCommits.value = true
    const response = await axios.post(`/api/projects/${project.value.id}/refresh-commits`)
    
    if (response.data.success) {
      // 更新项目的 git 提交记录
      project.value.gitCommits = JSON.stringify(response.data.commits)
      
      ElMessage({
        message: response.data.message,
        type: 'success',
        duration: 2000,
        offset: 60,
        showClose: true
      })
    } else {
      ElMessage({
        message: response.data.message,
        type: 'warning',
        duration: 3000,
        offset: 60
      })
    }
  } catch (error: any) {
    console.error('刷新Git提交记录失败:', error)
    const errorMessage = error.response?.data?.message || '刷新失败，请检查项目路径是否正确'
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 3000,
      offset: 60
    })
  } finally {
    refreshingCommits.value = false
  }
}

// 查看提交详情
function viewCommitDetail(commit: any) {
  if (!project.value?.localPath) {
    ElMessage({
      message: '项目路径不存在',
      type: 'error',
      duration: 2000,
      offset: 60
    })
    return
  }
  
  // 跳转到提交详情页面，记录当前tab和提交hash
  router.push({
    name: 'CommitDetail',
    params: { id: projectId.value },
    query: {
      hash: commit.hash,
      path: project.value.localPath,
      returnTab: 'commits', // 记录返回时要激活的tab
      scrollToHash: commit.hash // 记录需要滚动到的提交hash
    }
  })
}

onMounted(() => {
  fetchProjectDetail()
  
  // 添加快捷键监听
  document.addEventListener('keydown', handleKeyDown)
  
  // 初始化懒加载
  nextTick(() => {
    initLazyLoad()
  })
  
  // 检查是否有返回tab参数，如果有则激活对应tab
  const returnTab = route.query.returnTab as string
  const scrollToHash = route.query.scrollToHash as string
  
  if (returnTab) {
    activeTab.value = returnTab
    
    // 如果有scrollToHash，等待DOM更新后滚动到对应位置
    if (scrollToHash) {
      nextTick(() => {
        // 等待tab切换完成
        setTimeout(() => {
          const targetElement = document.querySelector(`[data-hash="${scrollToHash}"]`)
          if (targetElement) {
            targetElement.scrollIntoView({
              behavior: 'smooth',
              block: 'center'
            })
            // 添加高亮效果
            targetElement.classList.add('highlight-commit')
            setTimeout(() => {
              targetElement.classList.remove('highlight-commit')
            }, 2000)
          }
        }, 300)
      })
    }
    
    // 清理URL中的参数
    router.replace({
      path: route.path,
      query: {}
    })
  }
  
  // 检查是否需要返回到文件历史记录弹窗
  const returnToFileHistory = sessionStorage.getItem('returnToFileHistory')
  if (returnToFileHistory === 'true') {
    // 延迟打开文件历史记录弹窗，确保页面已加载完成
    setTimeout(() => {
      showFileHistoryModal.value = true
      // 只清除返回标记，保留文件路径信息给FileHistoryModal使用
      sessionStorage.removeItem('returnToFileHistory')
    }, 800)
  }
})

// README编辑相关方法
const startEditingReadme = () => {
  // 检查是否有草稿
  const draft = localStorage.getItem(`readme_draft_${projectId.value}`)
  
  if (draft) {
    ElMessageBox.confirm(
      '检测到未保存的草稿，是否恢复？',
      '恢复草稿',
      {
        confirmButtonText: '恢复草稿',
        cancelButtonText: '不恢复',
        type: 'info',
      }
    ).then(() => {
      readmeEditContent.value = draft
      isEditingReadme.value = true
    }).catch(() => {
      readmeEditContent.value = project.value?.description || ''
      isEditingReadme.value = true
      localStorage.removeItem(`readme_draft_${projectId.value}`)
    })
  } else {
    readmeEditContent.value = project.value?.description || ''
    isEditingReadme.value = true
  }
}

const cancelEditingReadme = () => {
  isEditingReadme.value = false
  // 清理草稿
  localStorage.removeItem(`readme_draft_${projectId.value}`)
}

const saveReadme = async () => {
  if (!project.value?.id) return
  
  try {
    // 构建更新数据，只包含必要字段
    const updateData: any = {
      id: project.value.id,
      name: project.value.name,
      description: readmeEditContent.value,
      status: project.value.status,
      progress: project.value.progress || 0
    }
    
    // 添加可选字段
    if (project.value.localPath) {
      updateData.localPath = project.value.localPath
    }
    if (project.value.repoUrl) {
      updateData.repoUrl = project.value.repoUrl
    }
    if (project.value.gitUserId) {
      updateData.gitUserId = project.value.gitUserId
    }
    
    // techStack需要是JSON字符串
    if (project.value.techStack) {
      updateData.techStack = typeof project.value.techStack === 'string' 
        ? project.value.techStack 
        : JSON.stringify(project.value.techStack)
    }
    
    // gitCommits需要是JSON字符串
    if (project.value.gitCommits) {
      updateData.gitCommits = typeof project.value.gitCommits === 'string'
        ? project.value.gitCommits
        : JSON.stringify(project.value.gitCommits)
    }
    
    console.log('保存数据:', updateData)
    
    const response = await axios.put(`/api/projects/${project.value.id}`, updateData)
    
    project.value.description = readmeEditContent.value
    isEditingReadme.value = false
    
    // 清理草稿
    localStorage.removeItem(`readme_draft_${projectId.value}`)
    
    ElMessage({
      message: 'README保存成功！',
      type: 'success',
      duration: 2000,
      offset: 60
    })
  } catch (error: any) {
    console.error('保存README失败:', error)
    ElMessage({
      message: error.response?.data?.message || error.message || '保存失败',
      type: 'error',
      duration: 3000,
      offset: 60
    })
  }
}

// 自动保存草稿
const autoSaveDraft = () => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
  autoSaveTimer = setTimeout(() => {
    localStorage.setItem(`readme_draft_${projectId.value}`, readmeEditContent.value)
  }, 1000) // 1秒后保存
}

// 监听编辑内容变化，自动保存草稿
watch(readmeEditContent, () => {
  if (isEditingReadme.value) {
    autoSaveDraft()
  }
})

// 快捷键监听
const handleKeyDown = (event: KeyboardEvent) => {
  // Ctrl+S 保存
  if ((event.ctrlKey || event.metaKey) && event.key === 's') {
    event.preventDefault()
    if (isEditingReadme.value) {
      saveReadme()
    }
  }
  // Esc 取消编辑
  if (event.key === 'Escape' && isEditingReadme.value) {
    cancelEditingReadme()
  }
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
    autoSaveTimer = null
  }
  // 移除快捷键监听
  document.removeEventListener('keydown', handleKeyDown)
})
</script>

<template>
  <div class="project-detail-layout">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="grid-pattern"></div>
    </div>

    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-left">
        <button class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>
        
        <div class="project-info-compact" v-if="project">
          <div class="project-icon-small">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <div class="project-meta">
            <h1 class="project-name">{{ project.name }}</h1>
            <div class="project-stats-inline">
              <el-tag :type="getStatusType(project.status)" size="small" effect="plain">{{ project.status }}</el-tag>
              <span class="stat-inline">
                <el-icon><Clock /></el-icon>
                {{ gitCommits.length }} 提交
              </span>
              <span class="stat-inline">进度 {{ project.progress }}%</span>
            </div>
          </div>
        </div>
      </div>

      <div class="nav-right">
        <!-- Git操作按钮组 -->
        <el-button-group>
          <el-button type="primary" :icon="Upload" size="small" @click="showGitCommitModal = true">提交</el-button>
          <el-button :icon="Refresh" size="small" @click="showGitPullModal = true">拉取</el-button>
        </el-button-group>
        
        <el-dropdown trigger="click">
          <el-button size="small" :icon="MoreFilled">更多</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="showGitFilesModal = true">文件变更</el-dropdown-item>
              <el-dropdown-item @click="showGitHistoryModal = true">历史记录</el-dropdown-item>
              <el-dropdown-item @click="showGitBranchModal = true">分支管理</el-dropdown-item>
              <el-dropdown-item @click="showGitStashModal = true">Stash暂存</el-dropdown-item>
              <el-dropdown-item divided @click="showGitConflictModal = true" style="color: #f56c6c">冲突解决</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-tooltip content="打开本地文件夹" placement="bottom" v-if="project?.localPath">
          <el-button size="small" :icon="Folder" @click="openLocalPath" circle />
        </el-tooltip>
        <el-tooltip content="打开Git仓库" placement="bottom" v-if="project?.repoUrl">
          <el-button size="small" :icon="LinkIcon" @click="openGitRepo" circle />
        </el-tooltip>
      </div>
    </header>

    <!-- 主布局 -->
    <div class="main-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <button 
            v-for="item in navItems" 
            :key="item.id"
            class="nav-item"
            :class="{ active: activeTab === item.id }"
            @click="handleTabChange(item.id)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
            <el-badge v-if="item.id === 'commits' && gitCommits.length > 0" :value="gitCommits.length" class="nav-badge" type="info" />
          </button>
        </nav>

        <!-- 项目信息卡片 -->
        <div class="project-card" v-if="project">
          <div class="project-icon">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <h2 class="project-title">项目信息</h2>
          
          <!-- 进度条 -->
          <div class="progress-track">
            <div 
              class="progress-fill" 
              :style="{ width: project.progress + '%', background: getProgressColor(project.progress) }"
            ></div>
          </div>
          <div class="progress-text">完成度 {{ project.progress }}%</div>

          <!-- 统计信息 -->
          <div class="project-stats">
            <div class="stat-row">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(project.createdAt).split(' ')[0] }}</span>
            </div>
          </div>

          <!-- 技术栈 -->
          <div class="tech-tags" v-if="project.techStack && project.techStack.length">
            <span v-for="tech in project.techStack" :key="tech" class="tech-tag">{{ tech }}</span>
          </div>
        </div>
      </aside>

      <!-- 内容区域 -->
      <div class="content-area">
        <div class="content-header">
          <h2 class="section-title">
            {{ navItems.find(i => i.id === activeTab)?.label }}
          </h2>
        </div>

        <div class="content-body">
          <!-- README -->
          <div v-show="activeTab === 'readme'" class="tab-view readme-view">
            <div class="readme-layout">
              <!-- 目录切换按钮 -->
              <div v-if="readmeHtml" class="toc-toggle-btn" @click="toggleToc" :title="showToc ? '收起目录' : '展开目录'">
                <el-icon :class="{ 'rotated': !showToc }"><DArrowLeft /></el-icon>
              </div>
              
              <!-- 目录侧边栏 -->
              <transition name="toc-slide">
                <div v-show="showToc && readmeHtml" class="toc-sidebar">
                  <div class="toc-header">
                    <el-icon><List /></el-icon>
                    <span>目录</span>
                    <el-icon v-if="tocLoading" class="toc-loading"><Loading /></el-icon>
                  </div>
                  <div class="toc-list">
                    <div v-if="tocLoading" class="toc-loading-state">
                      <div class="toc-skeleton" v-for="i in 5" :key="i"></div>
                    </div>
                    <template v-else-if="tocItems.length > 0">
                      <div 
                        v-for="item in tocItems" 
                        :key="item.id"
                        class="toc-item"
                        :class="`toc-level-${item.level}`"
                        @click="scrollToHeading(item.id)"
                      >
                        {{ item.text }}
                      </div>
                    </template>
                    <div v-else class="toc-empty">
                      <el-icon><Document /></el-icon>
                      <span>暂无目录</span>
                    </div>
                  </div>
                </div>
              </transition>

              <!-- README内容 -->
              <div ref="readmeContentRef" class="readme-content" @scroll="handleScroll">
                <div v-if="isEditingReadme" class="readme-editor">
                  <div class="editor-toolbar">
                    <span>编辑模式</span>
                    <div class="editor-btns">
                      <el-button size="small" @click="cancelEditingReadme">取消</el-button>
                      <el-button type="primary" size="small" @click="saveReadme">保存 (Ctrl+S)</el-button>
                    </div>
                  </div>
                  <div class="editor-split">
                    <el-input
                      ref="editorTextareaRef"
                      v-model="readmeEditContent"
                      type="textarea"
                      class="editor-textarea"
                      placeholder="请输入Markdown内容..."
                    />
                    <div class="editor-preview markdown-body" v-html="previewHtml"></div>
                  </div>
                </div>
                
                <div v-else>
                  <div 
                    v-if="readmeHtml" 
                    class="markdown-body" 
                    v-html="readmeHtml"
                    @dblclick="startEditingReadme"
                    title="双击编辑"
                  ></div>
                  <div v-else class="empty-state">
                    <el-icon class="empty-icon"><Document /></el-icon>
                    <p>暂无文档</p>
                    <el-button type="primary" @click="startEditingReadme">创建文档</el-button>
                  </div>
                </div>
              </div>
            </div>
            <!-- 结束 readme-layout -->
          </div>
          <!-- 结束 readme tab-view -->

          <!-- Commits -->
          <div v-show="activeTab === 'commits'" class="tab-view commits-view">
            <div class="commits-toolbar">
              <div class="search-bar">
              <el-input v-model="searchKeyword" placeholder="搜索提交..." :prefix-icon="Search" clearable />
            </div>
            <div class="filter-actions">
              <el-select v-model="filterAuthor" placeholder="作者" clearable style="width: 120px">
                <el-option v-for="author in authorList" :key="author" :label="author" :value="author" />
              </el-select>
              <el-button :icon="Refresh" @click="refreshGitCommits" :loading="refreshingCommits" circle />
            </div>
          </div>

          <div ref="commitsContentRef" class="commits-list" @scroll="handleCommitsScroll">
            <div v-if="paginatedCommits.length > 0" class="timeline">
              <div v-for="(commit, index) in paginatedCommits" :key="commit.hash" class="timeline-item" @click="viewCommitDetail(commit)">
                <div class="timeline-left">
                  <div class="commit-avatar">{{ commit.author[0].toUpperCase() }}</div>
                  <div class="timeline-line" v-if="index < paginatedCommits.length - 1"></div>
                </div>
                <div class="timeline-content">
                  <div class="commit-header">
                    <span class="commit-msg">{{ commit.message }}</span>
                    <span class="commit-time">{{ formatDate(commit.date) }}</span>
                  </div>
                  <div class="commit-meta">
                    <span class="commit-author">{{ commit.author }}</span>
                    <span class="commit-hash">{{ commit.hash.substring(0, 7) }}</span>
                  </div>
                </div>
              </div>
              <div v-if="isLoadingMore" class="loading-more">加载中...</div>
            </div>
            <div v-else class="empty-state">
              <el-icon class="empty-icon"><Search /></el-icon>
              <p>未找到提交记录</p>
            </div>
          </div>
        </div>

        <!-- API Docs -->
        <div v-if="activeTab === 'apis'" class="tab-view">
          <ApiDocPanel v-if="project" :project-id="project.id" />
        </div>

        <!-- Dev Env -->
        <div v-if="activeTab === 'devenv'" class="tab-view">
          <DevEnvironmentPanel 
            v-if="project" 
            :project-id="project.id" 
            :project-path="project.localPath"
          />
        </div>
      </div>
    </div>
    <!-- 结束 content-area -->
    
    </div>
    <!-- 结束 main-layout -->

    <!-- Modals -->
    <GitCommitModal v-model="showGitCommitModal" :project-id="project?.id" @success="refreshGitCommits" />
    <GitPullModal v-model="showGitPullModal" :project-id="project?.id" @success="refreshGitCommits" />
    <GitHistoryModal v-model="showGitHistoryModal" :project-id="project?.id" @refresh="refreshGitCommits" />
    <GitBranchModal v-model="showGitBranchModal" :project-id="project?.id" @refresh="refreshGitCommits" />
    <GitStashModal v-model="showGitStashModal" :project-id="project?.id" @refresh="refreshGitCommits" />
    <GitFilesModal v-model="showGitFilesModal" :project-id="project?.id" @refresh="refreshGitCommits" @commit="showGitCommitModal = true" />
    <GitConflictModal v-model="showGitConflictModal" :project-id="project?.id" @resolved="refreshGitCommits" />
    <FileHistoryModal v-model="showFileHistoryModal" :project-id="project?.id" />

    <!-- Back to Top -->
    <transition name="fade">
      <div v-show="showBackTop" class="back-to-top" @click="scrollToTop">
        <el-icon><ArrowUpBold /></el-icon>
      </div>
    </transition>
  </div>
</template>

<style scoped>
/* ===== 布局容器 ===== */
.project-detail-layout {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
  width: 100vw;
  overflow: hidden;
  position: relative;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

/* ===== 动态背景 ===== */
.animated-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.25;
  animation: float 25s ease-in-out infinite;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.3) 0%, transparent 70%);
  top: -200px;
  right: -200px;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.2) 0%, transparent 70%);
  bottom: -150px;
  left: -150px;
  animation-delay: -10s;
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(99, 102, 241, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.02) 1px, transparent 1px);
  background-size: 40px 40px;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, -40px) scale(1.1); }
}

/* ===== 顶部导航栏 ===== */
.top-nav {
  position: relative;
  z-index: 10;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.9);
  padding: 16px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.03);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  border-color: #6366f1;
  color: #6366f1;
  transform: translateX(-2px);
}

.project-info-compact {
  display: flex;
  align-items: center;
  gap: 16px;
}

.project-icon-small {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.project-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.project-name {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.project-stats-inline {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}

.stat-inline {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ===== 主内容区 ===== */
.main-layout {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 240px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow-y: auto;
  flex-shrink: 0;
}

.sidebar::-webkit-scrollbar {
  width: 4px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: transparent;
  border: none;
  border-radius: 10px;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  background: rgba(99, 102, 241, 0.08);
  color: #6366f1;
}

.nav-item.active {
  background: rgba(99, 102, 241, 0.12);
  color: #6366f1;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: #6366f1;
  border-radius: 0 2px 2px 0;
}

.nav-badge {
  margin-left: auto;
}

/* ===== 内容区域 ===== */
.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  padding: 20px 32px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.content-body {
  flex: 1;
  overflow: hidden;
}

.tab-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* ===== README 视图 ===== */
.readme-view {
  background: white;
}

.readme-layout {
  display: flex;
  height: 100%;
  position: relative;
}

/* 目录侧边栏 */
.toc-sidebar {
  width: 240px;
  border-right: 1px solid #e2e8f0;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.toc-header {
  padding: 16px;
  font-weight: 600;
  color: #334155;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.toc-loading {
  margin-left: auto;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.toc-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.toc-list::-webkit-scrollbar {
  width: 4px;
}

.toc-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.toc-loading-state {
  padding: 10px 16px;
}

.toc-skeleton {
  height: 20px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: loading 1.5s ease-in-out infinite;
  border-radius: 4px;
  margin-bottom: 8px;
}

@keyframes loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.toc-item {
  padding: 6px 16px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  border-left: 2px solid transparent;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.toc-item:hover {
  color: #6366f1;
  background: #f1f5f9;
  border-left-color: #6366f1;
}

.toc-level-1 {
  font-weight: 600;
  color: #334155;
}

.toc-level-2 {
  padding-left: 28px;
}

.toc-level-3 {
  padding-left: 40px;
}

.toc-level-4 {
  padding-left: 52px;
}

.toc-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #94a3b8;
  gap: 8px;
}

/* 目录切换按钮 */
.toc-toggle-btn {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 40px;
  background: white;
  border: 1px solid #e2e8f0;
  border-left: none;
  border-radius: 0 8px 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.toc-toggle-btn:hover {
  background: #f8fafc;
}

.toc-toggle-btn .el-icon {
  transition: transform 0.3s;
}

.toc-toggle-btn .el-icon.rotated {
  transform: rotate(180deg);
}

/* 目录滑动动画 */
.toc-slide-enter-active,
.toc-slide-leave-active {
  transition: all 0.3s ease;
}

.toc-slide-enter-from,
.toc-slide-leave-to {
  width: 0;
  opacity: 0;
}

/* README 内容区 */
.readme-content {
  flex: 1;
  overflow-y: auto;
  padding: 40px;
  scroll-behavior: smooth;
}

.readme-content::-webkit-scrollbar {
  width: 8px;
}

.readme-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.03);
}

.readme-content::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.3);
  border-radius: 4px;
}

.readme-content::-webkit-scrollbar-thumb:hover {
  background: rgba(99, 102, 241, 0.5);
}

/* README 编辑器 */
.readme-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.editor-toolbar {
  padding: 12px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
}

.editor-btns {
  display: flex;
  gap: 8px;
}

.editor-split {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.editor-textarea {
  flex: 1;
  height: 100%;
}

.editor-textarea :deep(.el-textarea__inner) {
  height: 100%;
  border: none;
  padding: 24px;
  resize: none;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.editor-preview {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  border-left: 1px solid #e2e8f0;
  background: white;
}

.editor-preview::-webkit-scrollbar {
  width: 8px;
}

.editor-preview::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

/* ===== 提交历史视图 ===== */
.commits-view {
  background: white;
}

.commits-toolbar {
  padding: 16px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.search-bar {
  flex: 1;
  max-width: 400px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.commits-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.commits-list::-webkit-scrollbar {
  width: 8px;
}

.commits-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.03);
}

.commits-list::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.3);
  border-radius: 4px;
}

.timeline {
  position: relative;
  max-width: 800px;
  margin: 0 auto;
}

.timeline-item {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  cursor: pointer;
  transition: transform 0.2s;
}

.timeline-item:hover {
  transform: translateX(6px);
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
  flex-shrink: 0;
}

.commit-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  z-index: 1;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: #e2e8f0;
  margin-top: 4px;
}

.timeline-content {
  flex: 1;
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #f1f5f9;
  transition: all 0.2s;
}

.timeline-item:hover .timeline-content {
  background: white;
  border-color: #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.commit-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  gap: 16px;
}

.commit-msg {
  font-weight: 600;
  color: #1e293b;
  flex: 1;
}

.commit-time {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
}

.commit-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
  gap: 16px;
}

.commit-author {
  flex: 1;
}

.commit-hash {
  font-family: 'Consolas', 'Monaco', monospace;
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: #94a3b8;
  font-size: 14px;
}

/* ===== Markdown 样式覆盖 ===== */
.markdown-body {
  font-family: inherit;
  color: #334155;
  font-size: 15px;
  line-height: 1.7;
}

.markdown-body h1,
.markdown-body h2 {
  border-bottom-color: #e2e8f0;
}

.markdown-body pre {
  background: #1e293b;
  border-radius: 8px;
}

.markdown-body code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.markdown-body pre code {
  background: transparent;
  padding: 0;
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #94a3b8;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* ===== 项目卡片（侧边栏） ===== */
.project-card {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.project-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.project-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px;
  line-height: 1.3;
}

.project-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.progress-text {
  font-size: 13px;
  font-weight: 700;
  color: #6366f1;
}

.progress-track {
  height: 6px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 16px;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.project-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tech-tag {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 600;
  background: rgba(99, 102, 241, 0.08);
  color: #6366f1;
  border-radius: 6px;
}

/* ===== 侧边栏底部 ===== */
.sidebar-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 8px;
}

.icon-btn {
  flex: 1;
  padding: 10px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 18px;
}

.icon-btn:hover {
  border-color: #6366f1;
  color: #6366f1;
  transform: translateY(-2px);
}

/* ===== 回到顶部按钮 ===== */
.back-to-top {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border: none;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all 0.3s;
}

.back-to-top:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.5);
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .sidebar {
    width: 200px;
  }
  
  .content-header,
  .content-body {
    padding-left: 24px;
    padding-right: 24px;
  }
}

@media (max-width: 768px) {
  .project-detail-layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }
  
  .nav-menu {
    flex-direction: row;
    overflow-x: auto;
  }
  
  .nav-item {
    flex-shrink: 0;
  }
}
</style>
