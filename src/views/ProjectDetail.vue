<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, 
  Folder, 
  Clock, 
  User, 
  Link as LinkIcon,
  Document,
  Star,
  TrendCharts,
  Calendar,
  FolderOpened,
  ArrowUpBold,
  Location,
  DocumentCopy,
  Upload,
  Loading,
  CircleCheck,
  Warning,
  DocumentChecked,
  Edit,
  Position as BranchIcon,
  Refresh
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { marked } from 'marked'

interface Project {
  id: number
  name: string
  description: string
  status: string
  progress: number
  techStack: string[]
  localPath?: string
  repoUrl?: string
  readmeContent?: string
  gitCommits?: string
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
const project = ref<Project | null>(null)
const loading = ref(false)
const activeTab = ref('readme')
const showBackTop = ref(false)
const scrollProgress = ref(0)
const readmeContentRef = ref<HTMLElement | null>(null)
const commitsContentRef = ref<HTMLElement | null>(null)

// Git提交相关状态（v3.0 - 7步骤流程）
const showCommitDialog = ref(false)
const commitLoading = ref(false)
const commitStatus = ref<'idle' | 'checking' | 'fetching' | 'pulling' | 'conflict' | 'selecting' | 'editing' | 'configuring' | 'committing' | 'success' | 'error'>('idle')
const commitMessage = ref('')
const commitLogs = ref<string[]>([])
const hasConflict = ref(false)
const gitStatusInfo = ref<any>(null)
const commitStep = ref(0) // 0=预检查 1=Pull 2=冲突解决 3=检查变更 4=选择文件 5=编辑信息 6=配置推送 7=执行提交
const suggestedCommitMessage = ref('') // AI建议的提交信息
const targetBranch = ref('') // 目标分支
const shouldPush = ref(true) // 是否自动push
const stopRetrying = ref(false) // 是否停止重试
const conflictFiles = ref<string[]>([]) // 冲突文件列表
const conflictResolutionStrategy = ref<{ [key: string]: string }>({}) // 冲突解决策略

// Git分支相关
const currentBranch = ref('')
const localBranches = ref<string[]>([])
const remoteBranches = ref<string[]>([])
const selectedBranch = ref('')
const branchLoading = ref(false)

// 文件选择相关
const showFileSelector = ref(false)
const allFiles = ref<any[]>([])
const selectedFiles = ref<string[]>([])
const ignoreFiles = ref<string[]>([])

// 重试设置
const maxRetries = ref(3)
const isRetrying = ref(false)
const shouldStopRetry = ref(false)

const gitCommits = computed<GitCommit[]>(() => {
  if (!project.value?.gitCommits) return []
  try {
    return JSON.parse(project.value.gitCommits)
  } catch {
    return []
  }
})

const readmeHtml = computed(() => {
  if (!project.value?.readmeContent) return ''
  try {
    return marked(project.value.readmeContent)
  } catch (error) {
    return project.value.readmeContent
  }
})

const fetchProjectDetail = async () => {
  try {
    loading.value = true
    const id = route.params.id
    const response = await axios.get(`/api/projects/${id}`)
    project.value = response.data
    
    // 解析techStack
    if (typeof project.value.techStack === 'string') {
      try {
        project.value.techStack = JSON.parse(project.value.techStack)
      } catch {
        project.value.techStack = []
      }
    }
  } catch (error) {
    console.error('获取项目详情失败:', error)
    ElMessage.error('获取项目详情失败')
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
  nextTick(() => {
    const container = getCurrentScrollContainer()
    if (container) {
      container.scrollTop = 0
    }
  })
}

// 打开本地文件夹
const openLocalPath = async () => {
  if (!project.value?.id) return
  
  try {
    const response = await axios.post(`/api/projects/${project.value.id}/open-folder`)
    ElMessage.success({
      message: response.data.message || '文件夹已打开',
      duration: 2000
    })
  } catch (error: any) {
    console.error('打开文件夹失败:', error)
    const errorMessage = error.response?.data?.message || '打开文件夹失败'
    ElMessage.error({
      message: errorMessage,
      duration: 3000
    })
  }
}

// 复制本地路径
const copyLocalPath = async (event: Event) => {
  event.stopPropagation() // 阻止冒泡，不触发打开文件夹
  
  if (!project.value?.localPath) return
  
  try {
    await navigator.clipboard.writeText(project.value.localPath)
    ElMessage.success({
      message: '本地路径已复制到剪贴板！',
      duration: 2000
    })
  } catch (error) {
    ElMessage.warning({
      message: '无法复制路径，请手动复制：' + project.value.localPath,
      duration: 3000
    })
  }
}

// 打开Git仓库
const openGitRepo = () => {
  if (!project.value?.repoUrl) return
  window.open(project.value.repoUrl, '_blank')
}

// 智能Git提交 - 重新设计为分步流程
const smartGitCommit = async () => {
  if (!project.value?.id) return
  
  try {
    // 重置状态
    commitStatus.value = 'checking'
    commitStep.value = 1  // 从步骤1开始（拉取代码）
    commitLogs.value = []
    hasConflict.value = false
    conflictFiles.value = []
    selectedFiles.value = []
    commitMessage.value = ''
    suggestedCommitMessage.value = ''
    stopRetrying.value = false
    showCommitDialog.value = true
    
    commitLogs.value.push('🔄 步骤0: 预检查...')
    
    // 获取分支列表
    commitLogs.value.push('📋 获取分支列表...')
    await fetchBranches()
    targetBranch.value = currentBranch.value
    commitLogs.value.push(`✅ 当前分支: ${currentBranch.value}`)
    
    // 步骤1: Fetch远程更新
    commitLogs.value.push('')
    commitLogs.value.push('📡 步骤1: 获取远程更新...')
    commitStatus.value = 'fetching'
    
    try {
      const fetchResponse = await axios.post(`/api/projects/${project.value.id}/git-fetch`)
      if (fetchResponse.data.logs && Array.isArray(fetchResponse.data.logs)) {
        commitLogs.value.push(...fetchResponse.data.logs)
      }
      if (fetchResponse.data.success) {
        commitLogs.value.push('✅ Fetch成功')
      }
    } catch (fetchError: any) {
      commitLogs.value.push('⚠️  Fetch失败，继续尝试Pull...')
      console.warn('Fetch error:', fetchError)
    }
    
    // 步骤1: Pull + Rebase
    commitLogs.value.push('')
    commitLogs.value.push('⬇️  正在拉取并合并代码 (pull --rebase)...')
    commitStatus.value = 'pulling'
    
    let pullSuccess = false
    let pullRetryCount = 0
    
    // 无限重试，直到成功或用户点击终止
    while (!pullSuccess && !stopRetrying.value) {
      if (pullRetryCount > 0) {
        commitLogs.value.push('')
        commitLogs.value.push(`🔄 第 ${pullRetryCount} 次重试拉取代码...`)
        await new Promise(resolve => setTimeout(resolve, 2000)) // 等待2秒
        
        // 检查是否被用户终止
        if (stopRetrying.value) {
          commitLogs.value.push('⛔ 用户已终止重试')
          break
        }
      }
      
      try {
        const pullResponse = await axios.post(`/api/projects/${project.value.id}/git-pull`)
        
        if (pullResponse.data.logs && Array.isArray(pullResponse.data.logs)) {
          commitLogs.value.push(...pullResponse.data.logs)
        }
        
        if (pullResponse.data.success) {
          pullSuccess = true
          commitLogs.value.push('✅ 代码拉取成功！')
          if (pullRetryCount > 0) {
            commitLogs.value.push(`（重试了 ${pullRetryCount} 次）`)
          }
        } else {
          // 检测到冲突
          if (pullResponse.data.hasConflict) {
            commitStatus.value = 'conflict'
            hasConflict.value = true
            conflictFiles.value = pullResponse.data.conflictFiles || []
            
            commitLogs.value.push('')
            commitLogs.value.push(`⚠️  检测到 ${conflictFiles.value.length} 个冲突文件！`)
            
            // 显示冲突文件列表
            if (conflictFiles.value.length > 0) {
              commitLogs.value.push('')
              commitLogs.value.push('冲突文件列表：')
              conflictFiles.value.forEach(file => {
                commitLogs.value.push(`  ⚠️  ${file}`)
              })
            }
            
            commitLogs.value.push('')
            commitLogs.value.push('💡 请选择解决方式：')
            commitLogs.value.push('1. 点击"打开文件夹"手动解决')
            commitLogs.value.push('2. 使用"本地版本"或"远程版本"快速解决')
            commitLogs.value.push('3. 解决后点击"继续Rebase"')
            commitLogs.value.push('4. 或点击"放弃合并"撤销此次操作')
            
            commitStep.value = 2 // 进入冲突解决步骤
            return
          }
          
          pullRetryCount++
          commitLogs.value.push(`⚠️  Pull失败: ${pullResponse.data.message}`)
          commitLogs.value.push(`💡 已重试 ${pullRetryCount} 次，将继续重试... 点击"终止重试"可停止`)
        }
      } catch (pullError: any) {
        pullRetryCount++
        const errorMsg = pullError.response?.data?.message || pullError.message
        commitLogs.value.push(`⚠️  Pull出错: ${errorMsg}`)
        commitLogs.value.push(`💡 已重试 ${pullRetryCount} 次，将继续重试... 点击"终止重试"可停止`)
        console.warn('Pull error:', pullError)
      }
    }
    
    // 如果用户终止了Pull，不继续后续操作
    if (stopRetrying.value) {
      commitStatus.value = 'error'
      commitLogs.value.push('')
      commitLogs.value.push(`⛔ 已终止操作（共重试了 ${pullRetryCount} 次）`)
      commitLogs.value.push('💡 您可以关闭对话框或重新开始')
      commitStep.value = 7 // 跳到最后显示终止状态
      return
    }
    
    // 检查Git状态
    commitLogs.value.push('')
    commitLogs.value.push('🔍 检查本地变更...')
    const statusResponse = await axios.get(`/api/projects/${project.value.id}/git-status`)
    gitStatusInfo.value = statusResponse.data
    
    if (!statusResponse.data.success) {
      commitStatus.value = 'error'
      commitLogs.value.push(`❌ ${statusResponse.data.message}`)
      commitStep.value = 4
      return
    }
    
    if (!statusResponse.data.hasChanges) {
      commitStatus.value = 'success'
      commitLogs.value.push('ℹ️  工作区是干净的，没有需要提交的变更')
      commitStep.value = 4
      return
    }
    
    // 准备文件列表（默认全选）
    const { modifiedFiles, addedFiles, deletedFiles, untrackedFiles } = statusResponse.data
    commitLogs.value.push(`✅ 发现 ${statusResponse.data.totalChanges} 个文件变更`)
    commitLogs.value.push('')
    commitLogs.value.push('👉 请在下方选择要提交的文件...')
    
    allFiles.value = [
      ...modifiedFiles.map((f: string) => ({ path: f, status: 'M', checked: true })),
      ...addedFiles.map((f: string) => ({ path: f, status: 'A', checked: true })),
      ...deletedFiles.map((f: string) => ({ path: f, status: 'D', checked: true })),
      ...untrackedFiles.map((f: string) => ({ path: f, status: '?', checked: true }))
    ]
    
    // 默认全选
    selectedFiles.value = allFiles.value.map(f => f.path)
    
    commitStatus.value = 'idle'
    
  } catch (error: any) {
    console.error('Git提交失败:', error)
    commitStatus.value = 'error'
    commitLogs.value.push('')
    commitLogs.value.push(`❌ 操作失败: ${error.response?.data?.message || error.message}`)
    commitStep.value = 4
  }
}

// 下一步 - 从文件选择到提交信息编辑
const nextStepToCommitMessage = async () => {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning('请至少选择一个文件提交')
    return
  }
  
  // 生成建议的提交信息
  try {
    const response = await axios.post(`/api/projects/${project.value.id}/git-generate-message`, {
      selectedFiles: selectedFiles.value,
      statusInfo: gitStatusInfo.value
    })
    
    suggestedCommitMessage.value = response.data.message || ''
    commitMessage.value = suggestedCommitMessage.value
    
  } catch (error) {
    // 如果生成失败，使用默认消息
    commitMessage.value = 'chore: 更新代码'
  }
  
  commitStep.value = 2
}

// 下一步 - 从提交信息编辑到分支选择
const nextStepToBranchSelection = () => {
  if (!commitMessage.value.trim()) {
    ElMessage.warning('请填写提交信息')
    return
  }
  commitStep.value = 3
}

// 执行提交
const executeCommit = async () => {
  if (!targetBranch.value) {
    ElMessage.warning('请选择目标分支')
    return
  }
  
  commitStatus.value = 'committing'
  commitStep.value = 4
  commitLogs.value = []
  commitLogs.value.push('🚀 开始执行Git提交...')
  
  try {
    // 准备提交数据
    const commitData: any = {
      commitMessage: commitMessage.value,
      selectedFiles: selectedFiles.value,
      targetBranch: targetBranch.value,
      shouldPush: shouldPush.value,
      maxRetries: maxRetries.value
    }
    
    const commitResponse = await axios.post(`/api/projects/${project.value.id}/git-commit`, commitData)
    
    if (commitResponse.data.success) {
      commitStatus.value = 'success'
      commitLogs.value.push('')
      
      // 添加详细日志
      if (commitResponse.data.logs && Array.isArray(commitResponse.data.logs)) {
        commitLogs.value.push(...commitResponse.data.logs)
      }
      
      commitLogs.value.push('')
      commitLogs.value.push('✅ 代码提交成功！')
      
      // 刷新项目详情
      setTimeout(() => {
        fetchProjectDetail()
      }, 1000)
      
    } else if (commitResponse.data.hasConflict) {
      commitStatus.value = 'conflict'
      hasConflict.value = true
      commitLogs.value.push('')
      commitLogs.value.push('⚠️ 检测到代码冲突！')
      commitLogs.value.push(commitResponse.data.message)
      commitLogs.value.push('')
      
      if (commitResponse.data.logs && Array.isArray(commitResponse.data.logs)) {
        commitLogs.value.push(...commitResponse.data.logs)
      }
      
      commitLogs.value.push('')
      commitLogs.value.push('💡 请手动解决冲突后，点击"继续提交"按钮')
      
    } else {
      commitStatus.value = 'error'
      commitLogs.value.push('')
      commitLogs.value.push(`❌ ${commitResponse.data.message}`)
      
      if (commitResponse.data.logs && Array.isArray(commitResponse.data.logs)) {
        commitLogs.value.push('')
        commitLogs.value.push(...commitResponse.data.logs)
      }
    }
    
  } catch (error: any) {
    console.error('Git提交失败:', error)
    commitStatus.value = 'error'
    commitLogs.value.push('')
    commitLogs.value.push(`❌ 提交失败: ${error.response?.data?.message || error.message}`)
  }
}

// 上一步
const prevStep = () => {
  if (commitStep.value > 1) {
    commitStep.value--
  }
}

// 切换文件选择
const toggleFileSelection = (filePath: string) => {
  const index = selectedFiles.value.indexOf(filePath)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  } else {
    selectedFiles.value.push(filePath)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (selectedFiles.value.length === allFiles.value.length) {
    selectedFiles.value = []
  } else {
    selectedFiles.value = allFiles.value.map(f => f.path)
  }
}

// 解决冲突后继续
const continueAfterConflict = async () => {
  if (!project.value?.id) return
  
  try {
    commitStatus.value = 'committing'
    commitLogs.value.push('')
    commitLogs.value.push('🔄 正在继续提交...')
    
    const response = await axios.post(`/api/projects/${project.value.id}/git-continue`, {
      commitMessage: commitMessage.value
    })
    
    if (response.data.success) {
      commitStatus.value = 'success'
      commitLogs.value.push('')
      
      if (response.data.logs && Array.isArray(response.data.logs)) {
        commitLogs.value.push(...response.data.logs)
      }
      
      commitLogs.value.push('')
      commitLogs.value.push('✅ 代码提交成功！')
      
      // 刷新项目详情
      setTimeout(() => {
        fetchProjectDetail()
      }, 1000)
      
    } else {
      commitStatus.value = 'error'
      commitLogs.value.push('')
      commitLogs.value.push(`❌ ${response.data.message}`)
      
      if (response.data.logs && Array.isArray(response.data.logs)) {
        commitLogs.value.push('')
        commitLogs.value.push(...response.data.logs)
      }
    }
    
  } catch (error: any) {
    console.error('继续提交失败:', error)
    commitStatus.value = 'error'
    commitLogs.value.push('')
    commitLogs.value.push(`❌ 继续提交失败: ${error.response?.data?.message || error.message}`)
  }
}

// 打开本地文件夹解决冲突
const openFolderToResolve = async () => {
  await openLocalPath()
  ElMessage.info({
    message: '请在编辑器中解决冲突，完成后点击"继续提交"',
    duration: 5000
  })
}

// 关闭提交对话框
const closeCommitDialog = () => {
  if (commitStatus.value === 'committing') {
    ElMessageBox.confirm('提交正在进行中，确定要关闭吗？', '提示', {
      type: 'warning'
    }).then(() => {
      showCommitDialog.value = false
    }).catch(() => {})
  } else {
    showCommitDialog.value = false
  }
}

// 获取分支列表
const fetchBranches = async () => {
  if (!project.value?.id) return
  
  try {
    branchLoading.value = true
    const response = await axios.get(`/api/projects/${project.value.id}/git-branches`)
    
    if (response.data.success) {
      currentBranch.value = response.data.currentBranch || ''
      localBranches.value = response.data.localBranches || []
      remoteBranches.value = response.data.remoteBranches || []
      selectedBranch.value = currentBranch.value
      
      commitLogs.value.push(`📌 当前分支: ${currentBranch.value}`)
    } else {
      commitLogs.value.push(`⚠️ 无法获取分支信息: ${response.data.message}`)
    }
  } catch (error: any) {
    console.error('获取分支列表失败:', error)
    commitLogs.value.push(`⚠️ 获取分支列表失败: ${error.message}`)
  } finally {
    branchLoading.value = false
  }
}

// 切换分支
const switchBranch = async (branchName: string) => {
  if (!project.value?.id || !branchName) return
  
  // 如果就是当前分支，不需要切换
  if (branchName === currentBranch.value) {
    selectedBranch.value = branchName
    return
  }
  
  try {
    branchLoading.value = true
    commitLogs.value.push('')
    commitLogs.value.push(`🔄 正在切换到分支: ${branchName}...`)
    
    const response = await axios.post(`/api/projects/${project.value.id}/git-switch-branch`, {
      branchName: branchName
    })
    
    if (response.data.success) {
      currentBranch.value = branchName
      selectedBranch.value = branchName
      commitLogs.value.push(`✅ 已切换到分支: ${branchName}`)
      
      // 切换分支后重新检查状态
      await fetchBranches()
    } else {
      commitLogs.value.push(`❌ 切换分支失败: ${response.data.message}`)
      selectedBranch.value = currentBranch.value // 恢复到当前分支
      
      if (response.data.hasChanges) {
        ElMessage.warning({
          message: '有未提交的变更，请先提交或暂存',
          duration: 3000
        })
      }
    }
  } catch (error: any) {
    console.error('切换分支失败:', error)
    commitLogs.value.push(`❌ 切换分支失败: ${error.response?.data?.message || error.message}`)
    selectedBranch.value = currentBranch.value // 恢复到当前分支
  } finally {
    branchLoading.value = false
  }
}

onMounted(() => {
  fetchProjectDetail()
})
</script>

<template>
  <div v-loading="loading" class="project-detail">
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
            <div v-if="project.localPath" class="path-item git-commit-btn" @click="smartGitCommit">
              <el-icon class="path-icon"><Upload /></el-icon>
              <span class="path-text">智能提交</span>
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
              <div ref="readmeContentRef" class="tab-content readme-content" @scroll="handleScroll">
                <div v-if="readmeHtml" class="markdown-body" v-html="readmeHtml"></div>
                <div v-else class="empty-state">
                  <el-icon class="empty-icon"><Document /></el-icon>
                  <p>暂无README文档</p>
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
              <div ref="commitsContentRef" class="tab-content commits-content" @scroll="handleScroll">
                <div v-if="gitCommits.length === 0" class="empty-state">
                  <el-icon class="empty-icon"><Clock /></el-icon>
                  <p>暂无Git提交记录</p>
                  <p class="hint">请确保项目是一个Git仓库</p>
                </div>
                <div v-else class="timeline">
                  <div v-for="(commit, index) in gitCommits" :key="commit.hash" class="timeline-item">
                    <div class="timeline-dot"></div>
                    <div v-if="index < gitCommits.length - 1" class="timeline-line"></div>
                    <div class="timeline-content">
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
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
      </div>
    </div>

    <!-- Git智能提交对话框 - 分步骤UI -->
    <el-dialog
      v-model="showCommitDialog"
      :title="`智能Git提交 - 步骤 ${commitStep}/7`"
      width="800px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @close="closeCommitDialog"
    >
      <div class="commit-dialog-content">
        <!-- 步骤指示器（7步）-->
        <el-steps :active="commitStep" finish-status="success" align-center style="margin-bottom: 24px;">
          <el-step title="拉取代码">
            <template #icon>
              <el-icon><Refresh /></el-icon>
            </template>
          </el-step>
          <el-step title="冲突解决">
            <template #icon>
              <el-icon><Warning /></el-icon>
            </template>
          </el-step>
          <el-step title="检查变更">
            <template #icon>
              <el-icon><DocumentChecked /></el-icon>
            </template>
          </el-step>
          <el-step title="选择文件">
            <template #icon>
              <el-icon><DocumentChecked /></el-icon>
            </template>
          </el-step>
          <el-step title="提交信息">
            <template #icon>
              <el-icon><Edit /></el-icon>
            </template>
          </el-step>
          <el-step title="配置推送">
            <template #icon>
              <el-icon><BranchIcon /></el-icon>
            </template>
          </el-step>
          <el-step title="执行提交">
            <template #icon>
              <el-icon><Upload /></el-icon>
            </template>
          </el-step>
        </el-steps>

        <!-- 步骤1: 文件选择 -->
        <div v-if="commitStep === 1" class="step-content">
          <!-- 准备阶段日志 -->
          <div v-if="commitStatus === 'checking' || commitStatus === 'fetching' || commitStatus === 'pulling'" class="prepare-logs">
            <el-icon class="is-loading"><Loading /></el-icon>
            <div class="prepare-logs-content">
              <div v-for="(log, index) in commitLogs" :key="index" class="log-line">
                {{ log }}
              </div>
              <div v-if="commitLogs.length > 3" class="stop-retry-btn">
                <el-button type="danger" size="small" @click="stopRetrying = true">
                  <el-icon><CircleClose /></el-icon>
                  终止重试
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 文件选择区域 -->
          <div v-else>
            <div class="step-header">
              <h3>📂 选择要提交的文件</h3>
              <el-button link type="primary" @click="toggleSelectAll">
                {{ selectedFiles.length === allFiles.length ? '取消全选' : '全选' }}
              </el-button>
            </div>
            
            <div class="file-list">
              <el-checkbox-group v-model="selectedFiles" class="file-checkbox-group">
                <div v-for="file in allFiles" :key="file.path" class="file-item">
                  <el-checkbox :label="file.path" :value="file.path">
                    <div class="file-info">
                      <el-tag :type="file.status === 'M' ? 'warning' : file.status === 'A' ? 'success' : file.status === 'D' ? 'danger' : 'info'" size="small">
                        {{ file.status === 'M' ? '修改' : file.status === 'A' ? '新增' : file.status === 'D' ? '删除' : '未跟踪' }}
                      </el-tag>
                      <span class="file-path">{{ file.path }}</span>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
            
            <el-alert v-if="selectedFiles.length === 0" title="请至少选择一个文件" type="warning" :closable="false" style="margin-top: 16px;" />
            <div v-else class="selection-summary">
              已选择 <strong>{{ selectedFiles.length }}</strong> 个文件，共 <strong>{{ allFiles.length }}</strong> 个文件
            </div>
          </div>
        </div>

        <!-- 步骤2: 编辑提交信息 -->
        <div v-if="commitStep === 2" class="step-content">
          <div class="step-header">
            <h3>📝 编辑提交信息</h3>
          </div>
          
          <el-form label-position="top">
            <el-form-item label="提交信息 (Commit Message)">
              <el-input
                v-model="commitMessage"
                type="textarea"
                :rows="6"
                placeholder="请输入提交信息，遵循 Conventional Commits 规范&#10;例如: feat: 添加用户登录功能"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-alert title="💡 AI 建议的提交信息" type="info" :closable="false" style="margin-bottom: 12px;">
              <template #default>
                <div style="white-space: pre-wrap; font-family: monospace; font-size: 13px;">{{ suggestedCommitMessage }}</div>
                <el-button link type="primary" size="small" @click="commitMessage = suggestedCommitMessage" style="margin-top: 8px;">
                  使用建议
                </el-button>
              </template>
            </el-alert>
          </el-form>
        </div>

        <!-- 步骤3: 选择分支和推送选项 -->
        <div v-if="commitStep === 3" class="step-content">
          <div class="step-header">
            <h3>🌿 选择目标分支和推送选项</h3>
          </div>
          
          <el-form label-position="top">
            <el-form-item label="当前分支">
              <el-tag type="success" size="large">
                <el-icon><BranchIcon /></el-icon>
                {{ currentBranch }}
              </el-tag>
            </el-form-item>
            
            <el-form-item label="Push到远程分支">
              <el-select
                v-model="targetBranch"
                placeholder="选择要推送的远程分支"
                style="width: 100%"
              >
                <el-option-group label="本地分支">
                  <el-option
                    v-for="branch in localBranches"
                    :key="branch"
                    :label="branch + (branch === currentBranch ? ' (当前)' : '')"
                    :value="branch"
                  />
                </el-option-group>
                <el-option-group v-if="remoteBranches.length > 0" label="远程分支">
                  <el-option
                    v-for="branch in remoteBranches"
                    :key="branch"
                    :label="branch"
                    :value="branch"
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-checkbox v-model="shouldPush">
                自动推送到远程仓库 (git push)
              </el-checkbox>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                关闭此选项将只执行本地提交，不会推送到远程
              </div>
            </el-form-item>
            
            <el-form-item label="失败重试次数">
              <el-input-number v-model="maxRetries" :min="0" :max="5" />
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                推送失败时的自动重试次数
              </div>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤4: 执行提交（日志显示） -->
        <div v-if="commitStep === 4" class="step-content">
          <div class="status-indicator">
            <el-icon v-if="commitStatus === 'committing'" class="status-icon loading">
              <Loading />
            </el-icon>
            <el-icon v-else-if="commitStatus === 'success'" class="status-icon success">
              <CircleCheck />
            </el-icon>
            <el-icon v-else-if="commitStatus === 'conflict'" class="status-icon warning">
              <Warning />
            </el-icon>
            <el-icon v-else-if="commitStatus === 'error'" class="status-icon error">
              <Warning />
            </el-icon>
            
            <div class="status-text">
              <span v-if="commitStatus === 'committing'">正在提交代码...</span>
              <span v-else-if="commitStatus === 'success'">提交成功！</span>
              <span v-else-if="commitStatus === 'conflict'">检测到冲突</span>
              <span v-else-if="commitStatus === 'error'">提交失败</span>
            </div>
          </div>
          
          <!-- 日志输出 -->
          <div class="commit-logs">
            <div v-for="(log, index) in commitLogs" :key="index" class="log-line">
              {{ log }}
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <!-- 步骤1的按钮 -->
          <template v-if="commitStep === 1">
            <el-button @click="showCommitDialog = false">取消</el-button>
            <el-button type="primary" @click="nextStepToCommitMessage" :disabled="selectedFiles.length === 0">
              下一步：编辑提交信息
            </el-button>
          </template>
          
          <!-- 步骤2的按钮 -->
          <template v-if="commitStep === 2">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="nextStepToBranchSelection" :disabled="!commitMessage.trim()">
              下一步：选择分支
            </el-button>
          </template>
          
          <!-- 步骤3的按钮 -->
          <template v-if="commitStep === 3">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="success" @click="executeCommit" :disabled="!targetBranch">
              开始提交
              <el-icon style="margin-left: 4px;"><Upload /></el-icon>
            </el-button>
          </template>
          
          <!-- 步骤4的按钮 -->
          <template v-if="commitStep === 4">
            <el-button
              v-if="commitStatus === 'conflict'"
              type="primary"
              @click="openFolderToResolve"
            >
              打开文件夹
            </el-button>
            <el-button
              v-if="commitStatus === 'conflict'"
              type="success"
              @click="continueAfterConflict"
            >
              继续提交
            </el-button>
            <el-button
              v-if="commitStatus !== 'committing'"
              @click="showCommitDialog = false"
            >
              关闭
            </el-button>
          </template>
        </div>
      </template>
    </el-dialog>

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
  min-height: 600px;
  max-height: 70vh;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.readme-content {
  padding: 0 32px 10px;
}

.commits-content {
  padding: 24px;
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
  padding: 100px 24px;
  color: #909399;
}

.empty-icon {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  margin: 8px 0;
}

.empty-state .hint {
  font-size: 14px;
  color: #c0c4cc;
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
}

.timeline-content:hover {
  background: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transform: translateX(4px);
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

/* Git提交对话框步骤样式 */
.step-content {
  min-height: 400px;
  padding: 20px 0;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.step-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.file-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px;
  background: #f5f7fa;
}

.file-checkbox-group {
  width: 100%;
}

.file-item {
  padding: 8px 12px;
  margin-bottom: 8px;
  background: white;
  border-radius: 6px;
  transition: all 0.3s;
}

.file-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-path {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #606266;
}

.selection-summary {
  margin-top: 16px;
  padding: 12px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 6px;
  color: #0050b3;
  text-align: center;
}

.selection-summary strong {
  color: #1890ff;
  font-size: 16px;
}

/* 准备阶段日志 */
.prepare-logs {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.prepare-logs .is-loading {
  font-size: 24px;
  color: #409eff;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.prepare-logs-content {
  flex: 1;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.8;
  color: #606266;
}

.prepare-logs-content .log-line {
  margin-bottom: 4px;
}

/* Git提交按钮特殊样式 */
.git-commit-btn {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border-color: transparent !important;
}

.git-commit-btn:hover {
  background: linear-gradient(135deg, #764ba2, #667eea) !important;
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4) !important;
}

/* 提交对话框样式 */
.commit-dialog-content {
  padding: 20px 0;
}

/* 分支选择器 */
.branch-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
  border: 2px solid #e8ecf1;
  border-radius: 8px;
}

.branch-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #667eea;
  white-space: nowrap;
}

.branch-label .el-icon {
  font-size: 18px;
}

.branch-select {
  flex: 1;
}

.current-branch {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #67c23a;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.status-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.status-icon.loading {
  color: #409eff;
  animation: rotate 1s linear infinite;
}

.status-icon.success {
  color: #67c23a;
}

.status-icon.warning {
  color: #e6a23c;
}

.status-icon.error {
  color: #f56c6c;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.status-text {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.commit-logs {
  background: #2d3748;
  color: #e2e8f0;
  padding: 20px;
  border-radius: 8px;
  min-height: 300px;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.8;
}

.log-line {
  margin-bottom: 2px;
  white-space: pre-wrap;
  word-break: break-word;
}

.log-line:empty {
  height: 8px;
}

.commit-logs::-webkit-scrollbar {
  width: 8px;
}

.commit-logs::-webkit-scrollbar-track {
  background: #1a202c;
  border-radius: 4px;
}

.commit-logs::-webkit-scrollbar-thumb {
  background: #4a5568;
  border-radius: 4px;
}

.commit-logs::-webkit-scrollbar-thumb:hover {
  background: #667eea;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

</style>
