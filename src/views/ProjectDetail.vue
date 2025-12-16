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
  Monitor
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
let autoRefreshTimer: NodeJS.Timeout | null = null // 自动刷新定时器

// README编辑相关
const isEditingReadme = ref(false) // 是否处于编辑模式
const readmeEditContent = ref('') // 编辑中的内容
let autoSaveTimer: NodeJS.Timeout | null = null // 自动保存定时器
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
    
    // 初始加载完成
    setTimeout(() => {
      initialLoading.value = false
    }, 500)
  } catch (error) {
    console.error('获取项目详情失败:', error)
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
  if (progress >= 80) return '#67c23a'
  if (progress >= 50) return '#409eff'
  if (progress >= 30) return '#e6a23c'
  return '#f56c6c'
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
const handleTabChange = () => {
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
  <div class="project-detail">
    <!-- 骨架屏加载状态 -->
    <div v-if="initialLoading" class="skeleton-container">
      <div class="skeleton-header">
        <div class="skeleton-circle"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-stats">
            <div class="skeleton-stat"></div>
            <div class="skeleton-stat"></div>
            <div class="skeleton-stat"></div>
          </div>
        </div>
      </div>
      <div class="skeleton-body">
        <div class="skeleton-tab-header">
          <div class="skeleton-tab"></div>
          <div class="skeleton-tab"></div>
        </div>
        <div class="skeleton-content-area">
          <div class="skeleton-line" v-for="i in 8" :key="i"></div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div v-else class="content-wrapper">
      <!-- 精简头部 - 单行展示 -->
      <div v-if="project" class="hero-header">
      <div class="hero-content">
        <el-button :icon="ArrowLeft" class="back-btn" @click="goBack" circle />
        
        <div class="hero-info">
          <!-- 项目标题和状态 -->
          <div class="project-title-section">
            <el-icon class="project-icon"><FolderOpened /></el-icon>
            <h1 class="hero-title">{{ project.name }}</h1>
            <el-tag :type="getStatusType(project.status)" effect="dark" class="status-tag">
              {{ project.status }}
            </el-tag>
          </div>

          <!-- 统计信息 -->
          <div class="stats-section">
            <div class="stat-box">
              <el-icon><TrendCharts /></el-icon>
              <span class="stat-text">{{ project.progress }}% 完成</span>
            </div>
            <div class="stat-box">
              <el-icon><Clock /></el-icon>
              <span class="stat-text">{{ gitCommits.length }} 次提交</span>
            </div>
            <div class="stat-box">
              <el-icon><Calendar /></el-icon>
              <span class="stat-text">{{ formatDate(project.createdAt).split(' ')[0] }}</span>
            </div>
          </div>

          <!-- 技术栈标签 -->
          <div v-if="project.techStack && project.techStack.length > 0" class="tech-section">
            <el-tag 
              v-for="tech in project.techStack.slice(0, 5)" 
              :key="tech" 
              class="tech-tag"
              size="small"
            >
              {{ tech }}
            </el-tag>
          </div>

          <!-- Git 操作按钮组 -->
          <div class="git-actions-section">
            <el-button-group>
              <el-button type="primary" :icon="Upload" @click="showGitCommitModal = true">
                提交
              </el-button>
              <el-button :icon="ArrowLeft" @click="showGitPullModal = true">
                拉取
              </el-button>
              <el-button @click="showGitFilesModal = true">
                文件
              </el-button>
              <el-button @click="showGitHistoryModal = true">
                历史
              </el-button>
              <el-button @click="showGitBranchModal = true">
                分支
              </el-button>
              <el-button @click="showGitStashModal = true">
                Stash
              </el-button>
              <el-button type="danger" @click="showGitConflictModal = true">
                冲突
              </el-button>
            </el-button-group>
          </div>

          <!-- 项目地址和操作按钮 -->
          <div class="path-section">
            <div v-if="project.localPath" class="path-item" @click="openLocalPath">
              <el-icon class="path-icon"><Folder /></el-icon>
              <span class="path-text">本地路径</span>
              <el-icon class="copy-icon" @click="copyLocalPath"><DocumentCopy /></el-icon>
            </div>
            <div v-if="project.repoUrl" class="path-item" @click="openGitRepo">
              <el-icon class="path-icon"><LinkIcon /></el-icon>
              <span class="path-text">Git仓库</span>
              <el-icon class="jump-icon"><Location /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 - 全宽标签页 -->
    <div v-if="project" class="main-container">
      <div class="content-full">
        <el-tabs v-model="activeTab" class="project-tabs" type="card" @tab-change="handleTabChange">
            <!-- README标签 -->
            <el-tab-pane name="readme">
              <template #label>
                <div class="tab-label">
                  <el-icon><Document /></el-icon>
                  <span>项目文档</span>
                </div>
              </template>
              
              <div class="readme-layout">
                <!-- 目录切换按钮 -->
                <div v-if="readmeHtml" class="toc-toggle-btn" @click="toggleToc" :title="showToc ? '收起目录' : '展开目录'">
                  <el-icon :class="{ 'rotated': !showToc }">
                    <DArrowLeft />
                  </el-icon>
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
                      <!-- 加载中状态 -->
                      <div v-if="tocLoading" class="toc-loading-state">
                        <div class="toc-skeleton" v-for="i in 5" :key="i"></div>
                      </div>
                      <!-- 目录内容 -->
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
                      <!-- 空状态 -->
                      <div v-else class="toc-empty">
                        <el-icon><Document /></el-icon>
                        <span>文档暂无目录</span>
                        <p class="toc-hint">请在文档中使用 # 标题语法</p>
                      </div>
                    </div>
                  </div>
                </transition>

                <!-- 内容区域 -->
                <div ref="readmeContentRef" class="tab-content readme-content" @scroll="handleScroll">
                  <!-- 编辑模式 -->
                  <div v-if="isEditingReadme" class="readme-editor">
                    <div class="editor-header">
                      <span class="editor-title">📝 编辑README</span>
                      <div class="editor-actions">
                        <span class="hint-text">Ctrl+S保存 | Esc取消</span>
                        <el-button size="small" @click="cancelEditingReadme">取消</el-button>
                        <el-button type="primary" size="small" @click="saveReadme">保存</el-button>
                      </div>
                    </div>
                    <div class="editor-layout">
                      <div class="editor-pane">
                        <div class="pane-title">Markdown源码</div>
                        <el-input
                          ref="editorTextareaRef"
                          v-model="readmeEditContent"
                          type="textarea"
                          :autosize="{ minRows: 20, maxRows: 30 }"
                          placeholder="请输入README内容，支持Markdown格式..."
                          class="editor-textarea"
                        />
                      </div>
                      <div class="preview-pane" ref="previewPaneRef">
                        <div class="pane-title">实时预览</div>
                        <div class="markdown-body preview-content" v-html="previewHtml"></div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 阅读模式 -->
                  <div v-else>
                    <div 
                      v-if="readmeHtml" 
                      class="markdown-body" 
                      v-html="readmeHtml"
                      @dblclick="startEditingReadme"
                      title="双击编辑README"
                      style="cursor: text;"
                    ></div>
                    <div v-else class="empty-state">
                      <el-icon class="empty-icon"><Document /></el-icon>
                      <p class="empty-title">暂无README文档</p>
                      <p class="empty-desc">该项目还没有README文档，您可以创建一个来介绍项目</p>
                      <el-button type="primary" size="small" plain @click="startEditingReadme">创建README</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- Git提交记录标签 -->
            <el-tab-pane name="commits">
              <template #label>
                <div class="tab-label">
                  <el-icon><Clock /></el-icon>
                  <span>提交历史</span>
                  <el-badge v-if="gitCommits.length > 0" :value="gitCommits.length" class="tab-badge" />
                </div>
              </template>
              <div class="commits-header">
                <div class="commits-info">
                  <span class="info-text">
                    共 {{ gitCommits.length }} 条提交记录
                    <template v-if="hasActiveFilters">
                      / 筛选后 <span class="highlight-text">{{ filteredCommits.length }}</span> 条
                    </template>
                  </span>
                </div>
                <div class="header-actions">
                  <el-button 
                    size="small" 
                    :icon="Document"
                    type="success"
                    @click="showFileHistoryModal = true"
                  >
                    文件历史
                  </el-button>
                  <el-button 
                    size="small" 
                    :icon="Search"
                    @click="showSearchPanel = !showSearchPanel"
                    :type="hasActiveFilters ? 'primary' : 'default'"
                  >
                    {{ showSearchPanel ? '收起筛选' : '展开筛选' }}
                  </el-button>
                  <el-button 
                    type="primary" 
                    size="small" 
                    :icon="Refresh" 
                    :loading="refreshingCommits"
                    @click="refreshGitCommits"
                  >
                    {{ refreshingCommits ? '刷新中...' : '刷新记录' }}
                  </el-button>
                </div>
              </div>
              
              <!-- 搜索和筛选面板 -->
              <transition name="el-zoom-in-top">
                <div v-show="showSearchPanel" class="search-panel">
                  <div class="filter-grid">
                    <div class="filter-card">
                      <div class="filter-label">关键词搜索</div>
                      <el-input
                        v-model="searchKeyword"
                        placeholder="搜索提交信息或 Hash ..."
                        :prefix-icon="Search"
                        clearable
                        size="small"
                      />
                    </div>
                    <div class="filter-card">
                      <div class="filter-label">提交作者</div>
                      <el-select
                        v-model="filterAuthor"
                        placeholder="选择作者筛选"
                        clearable
                        size="small"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="author in authorList"
                          :key="author"
                          :label="author"
                          :value="author"
                        />
                      </el-select>
                    </div>
                    <div class="filter-card">
                      <div class="filter-label">提交时间范围</div>
                      <el-date-picker
                        v-model="filterDateRange"
                        type="daterange"
                        unlink-panels
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        size="small"
                        class="commit-date-range-picker"
                        value-format="YYYY-MM-DD"
                      />
                    </div>
                    <div class="filter-card">
                      <div class="filter-label">修改文件路径</div>
                      <el-input
                        v-model="filterFilePath"
                        placeholder="支持输入目录或文件名"
                        clearable
                        size="small"
                      />
                    </div>
                  </div>
                  <div class="search-actions">
                    <div class="filter-summary">
                      <span class="filter-hint" v-if="hasActiveFilters">筛选结果：{{ filteredCommits.length }} 条</span>
                      <span class="filter-hint muted" v-else>可组合多个条件快速定位目标提交</span>
                    </div>
                    <div class="action-buttons">
                      <el-button size="small" @click="resetFilters" :disabled="!hasActiveFilters">
                        清空筛选
                      </el-button>
                    </div>
                  </div>
                </div>
              </transition>
              <div ref="commitsContentRef" class="tab-content commits-content" @scroll="handleCommitsScroll">
                <!-- 原始数据为空 -->
                <div v-if="gitCommits.length === 0" class="empty-state">
                  <el-icon class="empty-icon"><Clock /></el-icon>
                  <p class="empty-title">暂无Git提交记录</p>
                  <p class="empty-desc">该项目还没有Git提交记录，请检查：</p>
                  <ul class="empty-tips">
                    <li>✓ 确保项目是一个Git仓库</li>
                    <li>✓ 检查是否有提交历史</li>
                    <li>✓ 尝试点击右侧刷新按钮</li>
                  </ul>
                  <div class="empty-actions">
                    <el-button type="primary" size="small" :icon="Refresh" @click="refreshGitCommits">刷新记录</el-button>
                    <el-button size="small" plain>查看帮助</el-button>
                  </div>
                </div>
                <!-- 筛选结果为空 -->
                <div v-else-if="filteredCommits.length === 0" class="empty-state">
                  <el-icon class="empty-icon"><Search /></el-icon>
                  <p class="empty-title">未找到匹配的提交记录</p>
                  <p class="empty-desc">当前筛选条件下没有结果，建议：</p>
                  <ul class="empty-tips">
                    <li>✓ 尝试修改搜索关键词</li>
                    <li>✓ 调整筛选条件范围</li>
                    <li>✓ 点击"清空筛选"查看所有记录</li>
                  </ul>
                  <div class="empty-actions">
                    <el-button type="primary" size="small" @click="resetFilters">清空筛选</el-button>
                  </div>
                </div>
                <!-- 显示提交记录 -->
                <div v-else class="timeline">
                  <div v-for="(commit, index) in paginatedCommits" :key="commit.hash" class="timeline-item" :data-hash="commit.hash" v-memo="[commit.hash]">
                    <div class="timeline-dot"></div>
                    <div v-if="index < gitCommits.length - 1" class="timeline-line"></div>
                    <div class="timeline-content" @click="viewCommitDetail(commit)">
                      <div class="commit-header">
                        <span class="commit-hash">#{{ commit.hash.substring(0, 7) }}</span>
                        <span class="commit-date">{{ formatDate(commit.date) }}</span>
                      </div>
                      <div class="commit-message">{{ commit.message }}</div>
                      <div class="commit-author">
                        <el-icon><User /></el-icon>
                        <span>{{ commit.author }}</span>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 加载更多提示 -->
                  <div v-if="hasMore" class="load-more-container">
                    <div v-if="isLoadingMore" class="loading-indicator">
                      <el-icon class="is-loading"><Loading /></el-icon>
                      <span>加载中...</span>
                    </div>
                    <div v-else class="load-more-hint">
                      <span>继续滚动加载更多...</span>
                      <span class="hint-text">已显示 {{ paginatedCommits.length }} / {{ gitCommits.length }} 条</span>
                    </div>
                  </div>
                  
                  <!-- 全部加载完成提示 -->
                  <div v-else-if="gitCommits.length > 100" class="all-loaded">
                    <span>✓ 已加载全部 {{ gitCommits.length }} 条提交记录</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- API文档管理标签 -->
            <el-tab-pane name="apis">
              <template #label>
                <div class="tab-label">
                  <el-icon><Connection /></el-icon>
                  <span>API文档</span>
                </div>
              </template>
              <div class="tab-content full-height">
                <ApiDocPanel v-if="project" :project-id="project.id" />
              </div>
            </el-tab-pane>

            <!-- 开发环境管理标签 -->
            <el-tab-pane name="devenv">
              <template #label>
                <div class="tab-label">
                  <el-icon><Monitor /></el-icon>
                  <span>开发环境</span>
                </div>
              </template>
              <div class="tab-content full-height">
                <DevEnvironmentPanel 
                  v-if="project" 
                  :project-id="project.id" 
                  :project-path="project.localPath"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
      </div>
    </div>

    <!-- 回到顶部按钮 + 滚动进度 -->
    <transition name="fade">
      <div v-show="showBackTop" class="back-to-top-wrapper" @click="scrollToTop">
        <!-- 圆形进度环 -->
        <svg class="progress-ring" width="56" height="56">
          <defs>
            <linearGradient id="progressGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#a8edea;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#fed6e3;stop-opacity:1" />
            </linearGradient>
          </defs>
          <circle
            class="progress-ring-bg"
            cx="28"
            cy="28"
            r="24"
          />
          <circle
            class="progress-ring-circle"
            cx="28"
            cy="28"
            r="24"
            :style="{
              strokeDashoffset: 150.8 - (150.8 * scrollProgress) / 100
            }"
          />
        </svg>
        
        <!-- 按钮和百分比 -->
        <div class="back-to-top-content">
          <el-icon class="back-icon"><ArrowUpBold /></el-icon>
          <span class="progress-text">{{ scrollProgress }}%</span>
        </div>
      </div>
    </transition>

    <!-- Git智能提交弹窗 -->
    <GitCommitModal 
      v-model="showGitCommitModal"
      :project-id="project?.id"
      @success="refreshGitCommits"
    />

    <!-- Git拉取弹窗 -->
    <GitPullModal 
      v-model="showGitPullModal"
      :project-id="project?.id"
      @success="refreshGitCommits"
    />

    <!-- Git历史弹窗 -->
    <GitHistoryModal 
      v-model="showGitHistoryModal"
      :project-id="project?.id"
      @refresh="refreshGitCommits"
    />

    <!-- Git分支管理弹窗 -->
    <GitBranchModal 
      v-model="showGitBranchModal"
      :project-id="project?.id"
      @refresh="refreshGitCommits"
    />

    <!-- Git Stash弹窗 -->
    <GitStashModal 
      v-model="showGitStashModal"
      :project-id="project?.id"
      @refresh="refreshGitCommits"
    />

    <!-- Git文件变更弹窗 -->
    <GitFilesModal 
      v-model="showGitFilesModal"
      :project-id="project?.id"
      @refresh="refreshGitCommits"
      @commit="showGitCommitModal = true"
    />

    <!-- Git冲突管理弹窗 -->
    <GitConflictModal 
      v-model="showGitConflictModal"
      :project-id="project?.id"
      @resolved="refreshGitCommits"
    />

    <!-- 文件历史记录弹窗 -->
    <FileHistoryModal 
      v-model="showFileHistoryModal"
      :project-id="project?.id"
    />
    </div>
  </div>
</template>

<style scoped>
/* 主容器 */
.project-detail {
  height: 92vh;
  background: linear-gradient(to bottom, #f5f7fa 0%, #e8ecf1 100%);
  padding: 0;
  overflow-y: hidden;
}

/* 骨架屏样式 */
.skeleton-container {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.skeleton-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.skeleton-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-content {
  flex: 1;
}

.skeleton-title {
  height: 28px;
  width: 200px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  margin-bottom: 12px;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-stats {
  display: flex;
  gap: 12px;
}

.skeleton-stat {
  height: 32px;
  width: 120px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-body {
  background: white;
  max-width: 1600px;
  margin: 0 auto;
}

.skeleton-tab-header {
  display: flex;
  gap: 20px;
  padding: 20px 30px;
  border-bottom: 2px solid #f5f7fa;
}

.skeleton-tab {
  height: 40px;
  width: 100px;
  background: #f0f2f5;
  border-radius: 4px;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-content-area {
  padding: 24px;
}

.skeleton-line {
  height: 16px;
  background: linear-gradient(90deg, #f0f2f5 25%, #e4e7ed 50%, #f0f2f5 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin-bottom: 16px;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-line:nth-child(1) { width: 90%; }
.skeleton-line:nth-child(2) { width: 85%; }
.skeleton-line:nth-child(3) { width: 95%; }
.skeleton-line:nth-child(4) { width: 80%; }
.skeleton-line:nth-child(5) { width: 90%; }
.skeleton-line:nth-child(6) { width: 75%; }
.skeleton-line:nth-child(7) { width: 88%; }
.skeleton-line:nth-child(8) { width: 82%; }

@keyframes skeleton-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 内容包装器 */
.content-wrapper {
  animation: fadeIn 0.5s ease;
}

/* Hero头部 - 紧凑单行 */
.hero-header {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 5px;
  color: white;
}

.hero-content {
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 30px;
  position: relative;
}

.back-btn {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
  flex-shrink: 0;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}

.hero-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 30px;
  flex-wrap: wrap;
}

/* 项目标题区 */
.project-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.project-icon {
  font-size: 28px;
  opacity: 0.9;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  white-space: nowrap;
}

.status-tag {
  margin-left: 8px;
}

/* 统计信息区 */
.stats-section {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.stat-box {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  font-size: 14px;
  white-space: nowrap;
}

.stat-box .el-icon {
  font-size: 16px;
}

.stat-text {
  font-weight: 500;
}

/* 技术栈区 */
.tech-section {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}

.tech-tag {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 500;
  backdrop-filter: blur(10px);
}

/* Git操作按钮组 */
.git-actions-section {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.git-actions-section .el-button-group {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.git-actions-section .el-button {
  font-size: 13px;
}

/* 项目地址区 */
.path-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.path-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
  white-space: nowrap;
}

.action-btn {
  background: rgba(102, 126, 234, 0.3); /* Distinct background for action button */
  border-color: rgba(102, 126, 234, 0.4);
}

.action-btn:hover {
  background: rgba(102, 126, 234, 0.5);
}

.path-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.path-icon {
  font-size: 15px;
  opacity: 0.9;
}

.path-text {
  font-weight: 500;
}

.copy-icon,
.jump-icon {
  font-size: 13px;
  opacity: 0.8;
  margin-left: 2px;
}

.path-item:hover .copy-icon,
.path-item:hover .jump-icon {
  opacity: 1;
  animation: bounce 0.5s ease;
}

@keyframes bounce {
  0%, 100% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(3px);
  }
}

/* 主内容区域 */
.main-container {
  max-width: 1600px;
  margin: 0 auto;
  position: relative;
}

/* 全宽内容区 */
.content-full {
  max-width: 1600px;
  margin: 0 auto;
}

/* 标签页样式 */
.project-tabs {
  background: white;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  overflow: hidden;
}

.project-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 2px solid #f5f7fa;
  background: linear-gradient(to right, #f8f9fa, #ffffff);
}

.project-tabs :deep(.el-tabs__nav) {
  border: none;
}

.project-tabs :deep(.el-tabs__item) {
  border: none !important;
  color: #606266;
  font-weight: 500;
  padding: 20px 30px;
  transition: all 0.3s;
}

.project-tabs :deep(.el-tabs__item:hover) {
  color: #667eea;
}

.project-tabs :deep(.el-tabs__item.is-active) {
  color: #667eea;
  background: white;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

.tab-label .el-icon {
  font-size: 18px;
}

.tab-badge {
  margin-left: 8px;
}

/* 标签内容区域 */
.tab-content {
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* 为readme布局定制 */
.readme-layout {
  display: flex;
  height: 79vh; /* 固定高度 */
  overflow: hidden;
  background: white;
}

/* 目录切换按钮 */
.toc-toggle-btn {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 48px;
  background: white;
  border: 1px solid #e8ecf1;
  border-left: none;
  border-radius: 0 8px 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.toc-toggle-btn:hover {
  background: #f5f7fa;
  transform: translateY(-50%) translateX(2px);
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.1);
}

.toc-toggle-btn .el-icon {
  font-size: 16px;
  color: #606266;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toc-toggle-btn .el-icon.rotated {
  transform: rotate(180deg);
}

/* 目录侧边栏展开/收起动画 */
.toc-slide-enter-active,
.toc-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toc-slide-enter-from,
.toc-slide-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

.toc-slide-enter-to,
.toc-slide-leave-from {
  transform: translateX(0);
  opacity: 1;
}

/* 目录侧边栏 */
.toc-sidebar {
  width: 260px;
  flex-shrink: 0;
  border-right: 1px solid #e8ecf1;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
}

.toc-header {
  padding: 16px 20px;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 1px solid #e8ecf1;
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
}

.toc-header .toc-loading {
  margin-left: auto;
  animation: spin 1s linear infinite;
  color: #667eea;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.toc-loading-state {
  padding: 10px 20px;
}

.toc-skeleton {
  height: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 12px;
}

.toc-skeleton:nth-child(2) { width: 85%; margin-left: 15px; }
.toc-skeleton:nth-child(3) { width: 70%; margin-left: 30px; }
.toc-skeleton:nth-child(4) { width: 80%; margin-left: 15px; }
.toc-skeleton:nth-child(5) { width: 60%; margin-left: 30px; }

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.toc-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

/* 自定义滚动条 */
.toc-list::-webkit-scrollbar {
  width: 4px;
}
.toc-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}

.toc-item {
  padding: 8px 20px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  line-height: 1.4;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.toc-item:hover {
  background: #eef1f6;
  color: #667eea;
  border-left-color: #667eea;
}

.toc-level-1 { font-weight: 600; color: #303133; }
.toc-level-2 { padding-left: 35px; }
.toc-level-3 { padding-left: 50px; font-size: 13px; }
.toc-level-4 { padding-left: 65px; font-size: 13px; color: #909399; }

.toc-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
  font-size: 13px;
  text-align: center;
}

.toc-empty .el-icon {
  font-size: 36px;
  margin-bottom: 12px;
  color: #c0c4cc;
}

.toc-hint {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 8px;
  line-height: 1.5;
}

.readme-content {
  flex: 1;
  height: 100%;
  padding: 0 32px 10px;
}

/* 提交记录头部 */
.commits-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8ecf1;
}

.commits-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.highlight-text {
  color: #409eff;
  font-weight: bold;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 搜索面板 */
.search-panel {
  padding: 20px 24px;
  background: #fafbfc;
  border-bottom: 1px solid #eef0f5;
  box-shadow: inset 0 4px 8px -4px rgba(0, 0, 0, 0.05);
  position: relative;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

/* 提交时间范围的日期选择器宽度略窄一些 */
.commit-date-range-picker {
  width: 230px;
  max-width: 100%;
}

.filter-card {
  background: #fff;
  border: 1px solid #edf0f5;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.filter-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #4b5563;
  letter-spacing: 0.02em;
}

/* 优化 Element Plus 输入框样式 */
.search-panel :deep(.el-input__wrapper),
.search-panel :deep(.el-select__wrapper),
.search-panel :deep(.el-range-editor.el-input__wrapper) {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05) !important;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  background-color: #ffffff;
}

.search-panel :deep(.el-input__wrapper:hover),
.search-panel :deep(.el-select__wrapper:hover),
.search-panel :deep(.el-range-editor.el-input__wrapper:hover) {
  border-color: #c0c4cc;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05) !important;
}

.search-panel :deep(.el-input__wrapper.is-focus),
.search-panel :deep(.el-select__wrapper.is-focus),
.search-panel :deep(.el-range-editor.is-active) {
  box-shadow: 0 0 0 1px #667eea, 0 4px 12px rgba(102, 126, 234, 0.15) !important;
  border-color: #667eea;
}

.search-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.filter-summary {
  display: flex;
  align-items: center;
}

.filter-hint.muted {
  color: #a0a8b7;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.filter-hint {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-hint::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background-color: #667eea;
  border-radius: 50%;
}

/* 提交记录Tab还是使用原来的高度限制 */
.commits-content {
  padding: 24px;
  max-height: calc(79vh - 60px);
}

/* 滚动条样式 */
.tab-content::-webkit-scrollbar {
  width: 8px;
}

.tab-content::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 4px;
}

.tab-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
}

.tab-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2, #667eea);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  color: #909399;
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  border-radius: 12px;
  margin: 40px;
  border: 2px dashed #e4e7ed;
}

.empty-icon {
  font-size: 72px;
  color: #667eea;
  margin-bottom: 24px;
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  margin: 16px 0;
  color: #303133;
}

.empty-desc {
  font-size: 14px;
  margin: 12px 0 20px;
  color: #606266;
  line-height: 1.6;
}

.empty-tips {
  list-style: none;
  padding: 0;
  margin: 20px 0;
  text-align: left;
  display: inline-block;
}

.empty-tips li {
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.empty-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* Markdown样式 */
.markdown-body {
  line-height: 1.8;
  color: #2c3e50;
  font-size: 15px;
}

.markdown-body h1 {
  font-size: 2em;
  font-weight: 700;
  color: #1a202c;
  border-bottom: 3px solid #667eea;
  padding-bottom: 12px;
  margin: 24px 0 16px;
}

.markdown-body h2 {
  font-size: 1.6em;
  font-weight: 600;
  color: #2d3748;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 8px;
  margin: 28px 0 16px;
}

.markdown-body h3 {
  font-size: 1.3em;
  font-weight: 600;
  color: #4a5568;
  margin: 24px 0 12px;
}

.markdown-body p {
  margin-bottom: 16px;
  line-height: 1.7;
}

.markdown-body ul,
.markdown-body ol {
  margin-bottom: 16px;
  padding-left: 28px;
}

.markdown-body li {
  margin-bottom: 8px;
}

.markdown-body code {
  background: #f7fafc;
  color: #e83e8c;
  padding: 3px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.88em;
  border: 1px solid #e2e8f0;
}

.markdown-body pre {
  background: #2d3748;
  color: #e2e8f0;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin-bottom: 20px;
  box-shadow: inset 0 2px 6px rgba(0,0,0,0.1);
}

.markdown-body pre code {
  background: none;
  border: none;
  color: inherit;
  padding: 0;
}

.markdown-body a {
  color: #667eea;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.2s;
}

.markdown-body a:hover {
  border-bottom-color: #667eea;
}

/* Git提交记录 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state .hint {
  font-size: 14px;
  margin-top: 8px;
}

/* 时间线样式 */
.timeline {
  position: relative;
  padding: 24px;
}

.timeline-item {
  position: relative;
  padding-left: 40px;
  padding-bottom: 30px;
}

.timeline-dot {
  position: absolute;
  left: 0;
  top: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: 3px solid white;
  box-shadow: 0 0 0 3px #e8ecf1;
  z-index: 2;
}

.timeline-line {
  position: absolute;
  left: 5px;
  top: 12px;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #e8ecf1 0%, transparent 100%);
}

.timeline-content {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e8ecf1;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.timeline-content:hover {
  background: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transform: translateX(4px);
  border-color: #667eea;
}

.timeline-content:active {
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

/* 高亮动画效果 */
.highlight-commit .timeline-content {
  animation: highlightPulse 2s ease-in-out;
  background: linear-gradient(135deg, #e8f4fd 0%, #d4e9f7 100%);
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
}

@keyframes highlightPulse {
  0%, 100% {
    transform: translateX(0);
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
  }
  50% {
    transform: translateX(8px);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
  }
}

.commit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.commit-hash {
  font-family: 'Consolas', 'Monaco', monospace;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.commit-date {
  color: #909399;
  font-size: 13px;
}

.commit-message {
  font-size: 15px;
  color: #2c3e50;
  font-weight: 500;
  margin-bottom: 10px;
  line-height: 1.6;
}

.commit-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.commit-author .el-icon {
  font-size: 14px;
  color: #667eea;
}

/* 加载更多容器 */
.load-more-container {
  text-align: center;
  padding: 30px 20px;
  margin-top: 20px;
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 14px;
  color: #667eea;
}

.loading-indicator .el-icon {
  font-size: 20px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.load-more-hint {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.load-more-hint .hint-text {
  font-size: 12px;
  color: #c0c4cc;
}

.all-loaded {
  text-align: center;
  padding: 20px;
  margin-top: 20px;
  font-size: 14px;
  color: #67c23a;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px dashed #67c23a;
}

/* 图片懒加载样式 */
.markdown-body img.lazy-image {
  filter: blur(5px);
  transition: filter 0.3s ease;
}

.markdown-body img.loaded {
  filter: none;
}

/* README编辑器样式 */
.readme-editor {
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f2f5;
}

.editor-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hint-text {
  font-size: 12px;
  color: #909399;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.editor-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.editor-pane,
.preview-pane {
  min-height: 500px;
  max-height: 70vh;
  overflow-y: auto;
  position: relative;
  scroll-behavior: smooth;
}

/* 美化滚动条 */
.editor-pane::-webkit-scrollbar,
.preview-pane::-webkit-scrollbar {
  width: 8px;
}

.editor-pane::-webkit-scrollbar-track,
.preview-pane::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 4px;
}

.editor-pane::-webkit-scrollbar-thumb,
.preview-pane::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
}

.editor-pane::-webkit-scrollbar-thumb:hover,
.preview-pane::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.pane-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 3px solid #667eea;
}

.editor-textarea :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
}

.preview-content {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 24px;
  min-height: 500px;
  background: #fafafa;
  overflow-y: auto;
}

/* 回到顶部按钮 + 进度环 */
.back-to-top-wrapper {
  position: fixed;
  right: 40px;
  bottom: 40px;
  width: 56px;
  height: 56px;
  z-index: 1000;
  cursor: pointer;
  transition: all 0.3s;
}

.back-to-top-wrapper:hover {
  transform: translateY(-4px) scale(1.1);
}

/* 进度环SVG */
.progress-ring {
  position: absolute;
  top: 0;
  left: 0;
  transform: rotate(-90deg);
}

.progress-ring-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.2);
  stroke-width: 3;
}

.progress-ring-circle {
  fill: none;
  stroke: url(#progressGradient);
  stroke-width: 3;
  stroke-linecap: round;
  stroke-dasharray: 150.8;
  stroke-dashoffset: 150.8;
  transition: stroke-dashoffset 0.3s ease;
}

/* 按钮内容 */
.back-to-top-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.back-to-top-wrapper:hover .back-to-top-content {
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.6);
  background: linear-gradient(135deg, #764ba2, #667eea);
}

.back-icon {
  font-size: 18px;
  margin-bottom: -2px;
}

.progress-text {
  font-size: 9px;
  font-weight: 700;
  line-height: 1;
}

/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.8);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.8);
}

/* Markdown-it 自动生成的目录样式 */
.table-of-contents {
  background: linear-gradient(135deg, #fafbfd 0%, #f6f8fb 100%);
  border-left: 4px solid #667eea;
  padding: 20px 24px;
  margin: 24px 0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.table-of-contents::before {
  content: '📑 目录';
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 16px;
  letter-spacing: 0.5px;
}

.table-of-contents ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
}

.table-of-contents li {
  padding: 6px 0;
  line-height: 1.8;
}

.table-of-contents a {
  color: #4a5568;
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}

.table-of-contents a:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(4px);
}

/* 代码块高亮增强 */
.markdown-body pre.hljs {
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
  overflow-x: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.markdown-body code {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  color: #d73a49;
  border: 1px solid #e1e4e8;
}

.markdown-body pre code {
  background: transparent;
  padding: 0;
  border: none;
  color: inherit;
}

/* 标题锚点样式 */
.markdown-body h1 .header-anchor,
.markdown-body h2 .header-anchor,
.markdown-body h3 .header-anchor,
.markdown-body h4 .header-anchor,
.markdown-body h5 .header-anchor,
.markdown-body h6 .header-anchor {
  opacity: 0;
  transition: opacity 0.3s ease;
  margin-left: 8px;
  color: #667eea;
  text-decoration: none;
}

.markdown-body h1:hover .header-anchor,
.markdown-body h2:hover .header-anchor,
.markdown-body h3:hover .header-anchor,
.markdown-body h4:hover .header-anchor,
.markdown-body h5:hover .header-anchor,
.markdown-body h6:hover .header-anchor {
  opacity: 1;
}

/* 全高度内容区 */
.tab-content.full-height {
  height: calc(100vh - 280px);
  overflow: hidden;
}
</style>
