<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
  Upload
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { marked } from 'marked'
import GitCommitModal from '@/components/GitCommitModal.vue'

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
const refreshingCommits = ref(false)
const showGitCommitModal = ref(false) // Modal visibility state

// 生成目录
const generateToc = () => {
  nextTick(() => {
    const container = readmeContentRef.value
    if (!container) return
    
    // 清空旧目录
    tocItems.value = []
    
    const headers = container.querySelectorAll('h1, h2, h3, h4')
    headers.forEach((header, index) => {
      // 确保有ID
      if (!header.id) {
        header.id = `heading-${index}`
      }
      
      tocItems.value.push({
        id: header.id,
        text: header.textContent || '',
        level: parseInt(header.tagName.substring(1))
      })
    })
  })
}

// 监听项目内容变化重新生成目录
watch(() => project.value?.description, () => {
  generateToc()
})

// 跳转到标题
const scrollToHeading = (id: string) => {
  const container = readmeContentRef.value
  const element = container?.querySelector(`#${id}`) as HTMLElement
  
  if (container && element) {
    const top = element.offsetTop
    container.scrollTo({
      top: top - 20, // 留点余量
      behavior: 'smooth'
    })
  }
}

const gitCommits = computed<GitCommit[]>(() => {
  if (!project.value?.gitCommits) return []
  try {
    return JSON.parse(project.value.gitCommits)
  } catch {
    return []
  }
})

const readmeHtml = computed(() => {
  const content = project.value?.description
  if (!content) return ''
  try {
    return marked(content)
  } catch (error) {
    return content
  }
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
    
    if (activeTab.value === 'readme') {
      generateToc()
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

// 刷新Git提交记录
const refreshGitCommits = async () => {
  if (!project.value?.id) return
  
  try {
    refreshingCommits.value = true
    const response = await axios.post(`/api/projects/${project.value.id}/refresh-commits`)
    
    if (response.data.success) {
      // 更新项目的 git 提交记录
      project.value.gitCommits = JSON.stringify(response.data.commits)
      
      ElMessage.success({
        message: response.data.message,
        duration: 2000
      })
    } else {
      ElMessage.warning({
        message: response.data.message,
        duration: 3000
      })
    }
  } catch (error: any) {
    console.error('刷新Git提交记录失败:', error)
    const errorMessage = error.response?.data?.message || '刷新失败，请检查项目路径是否正确'
    ElMessage.error({
      message: errorMessage,
      duration: 3000
    })
  } finally {
    refreshingCommits.value = false
  }
}

// 查看提交详情
function viewCommitDetail(commit: any) {
  if (!project.value?.localPath) {
    ElMessage.error('项目路径不存在')
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
            <div class="path-item action-btn" @click="showGitCommitModal = true">
              <el-icon class="path-icon"><Upload /></el-icon>
              <span class="path-text">Git提交</span>
            </div>
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
                <!-- 目录侧边栏 -->
                <div v-if="tocItems.length > 0" class="toc-sidebar">
                  <div class="toc-header">
                    <el-icon><List /></el-icon>
                    <span>目录</span>
                  </div>
                  <div class="toc-list">
                    <div 
                      v-for="item in tocItems" 
                      :key="item.id"
                      class="toc-item"
                      :class="`toc-level-${item.level}`"
                      @click="scrollToHeading(item.id)"
                    >
                      {{ item.text }}
                    </div>
                  </div>
                </div>

                <!-- 内容区域 -->
                <div ref="readmeContentRef" class="tab-content readme-content" @scroll="handleScroll">
                  <div v-if="readmeHtml" class="markdown-body" v-html="readmeHtml"></div>
                  <div v-else class="empty-state">
                    <el-icon class="empty-icon"><Document /></el-icon>
                    <p>暂无README文档</p>
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
                  <span class="info-text">共 {{ gitCommits.length }} 条提交记录</span>
                </div>
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
              <div ref="commitsContentRef" class="tab-content commits-content" @scroll="handleScroll">
                <div v-if="gitCommits.length === 0" class="empty-state">
                  <el-icon class="empty-icon"><Clock /></el-icon>
                  <p>暂无Git提交记录</p>
                  <p class="hint">请确保项目是一个Git仓库</p>
                </div>
                <div v-else class="timeline">
                  <div v-for="(commit, index) in gitCommits" :key="commit.hash" class="timeline-item" :data-hash="commit.hash">
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
                </div>
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


</style>
