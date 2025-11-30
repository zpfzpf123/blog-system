<template>
  <div class="commit-detail-page">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="goBack" class="back-btn">
        返回项目
      </el-button>
      <div class="header-info">
        <div class="info-main">
          <h2 class="page-title">{{ commitDetail?.message || '提交详情' }}</h2>
          <div v-if="commitDetail" class="info-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ commitDetail.author }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formattedDate }}
            </span>
            <span class="meta-item stats-item">
              <span class="stat-add">+{{ totalAdditions }}</span>
              <span class="stat-delete">-{{ totalDeletions }}</span>
              <span class="stat-total">总{{ totalChanges }}行</span>
            </span>
          </div>
        </div>
        <span v-if="commitDetail" class="commit-hash" :title="commitDetail.hash">
          {{ commitDetail.hash }}
        </span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 提交详情内容 -->
    <div v-else-if="commitDetail" class="commit-content">
      <!-- 左右分栏布局 -->
      <div class="split-layout">
        <!-- 左侧：文件目录树 -->
        <div class="left-panel">
          <div class="panel-header">
            <el-icon><FolderOpened /></el-icon>
            <span>变更文件 ({{ fileStats.length }})</span>
          </div>
          <div class="file-tree">
            <FileTreeNode 
              v-for="node in fileTree" 
              :key="node.path"
              :node="node"
              :selected-path="selectedFilePath"
              @select="handleSelectFile"
            />
          </div>
        </div>

        <!-- 右侧：代码变更详情 -->
        <div class="right-panel">
          <div class="panel-header">
            <el-icon><Edit /></el-icon>
            <span>{{ selectedFilePath || '请选择文件查看变更' }}</span>
          </div>
          <div v-if="selectedFilePath" ref="diffContentContainer" class="diff-content">
            <!-- 文件统计信息 -->
            <div class="file-stat-bar">
              <span class="add-count">+{{ selectedFileStats.additions }}</span>
              <div class="bar-container">
                <div 
                  class="bar add-bar" 
                  :style="{ width: selectedFileStats.addPercent + '%' }"
                ></div>
                <div 
                  class="bar delete-bar" 
                  :style="{ width: selectedFileStats.deletePercent + '%' }"
                ></div>
              </div>
              <span class="delete-count">-{{ selectedFileStats.deletions }}</span>
            </div>
            <!-- 代码差异 -->
            <pre v-html="selectedFileDiff"></pre>
            
            <!-- 导航按钮 -->
            <div v-if="hunkCount > 1" class="diff-navigation">
              <div class="nav-info">
                {{ currentHunkIndex + 1 }} / {{ hunkCount }}
              </div>
              <el-button 
                :icon="ArrowUp" 
                circle 
                size="small"
                @click="navigateToHunk('prev')"
                title="上一个修改（循环）"
              />
              <el-button 
                :icon="ArrowDown" 
                circle 
                size="small"
                @click="navigateToHunk('next')"
                title="下一个修改（循环）"
              />
            </div>
          </div>
          <div v-else class="empty-state">
            <el-icon class="empty-icon"><Document /></el-icon>
            <p>点击左侧文件查看变更详情</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-container">
      <el-icon class="error-icon"><WarningFilled /></el-icon>
      <p>加载失败</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading, Document, Edit, WarningFilled, FolderOpened, ArrowUp, ArrowDown, ArrowLeft, User, Clock } from '@element-plus/icons-vue'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'
import FileTreeNode from '../components/FileTreeNode.vue'

const route = useRoute()
const router = useRouter()

// 数据
const loading = ref(false)
const commitDetail = ref<any>(null)
const rawDiff = ref('')
const selectedFilePath = ref('')
const fileDiffs = ref<Map<string, string>>(new Map())
const diffContentContainer = ref<HTMLElement | null>(null)
const currentHunkIndex = ref(0)
const hunkCount = ref(0)

// 从路由获取参数
const commitHash = computed(() => route.query.hash as string)
const projectPath = computed(() => route.query.path as string)
const projectId = computed(() => route.params.id as string)

// 文件统计信息
const fileStats = computed(() => {
  if (!rawDiff.value) return []
  
  const lines = rawDiff.value.split('\n')
  const stats: any[] = []
  
  for (const line of lines) {
    const match = line.match(/^(\d+)\s+(\d+)\s+(.+)$/)
    if (match) {
      const additions = parseInt(match[1])
      const deletions = parseInt(match[2])
      const total = additions + deletions
      
      stats.push({
        name: match[3],
        additions,
        deletions,
        addPercent: total > 0 ? (additions / total) * 100 : 0,
        deletePercent: total > 0 ? (deletions / total) * 100 : 0
      })
    }
  }
  
  return stats
})

// 构建文件目录树
const fileTree = computed(() => {
  if (!fileStats.value.length) return []
  
  interface TreeNode {
    name: string
    path: string
    isFile: boolean
    children?: TreeNode[]
    stats?: any
  }
  
  const root: TreeNode[] = []
  
  fileStats.value.forEach(file => {
    const parts = file.name.split('/')
    let currentLevel = root
    let currentPath = ''
    
    parts.forEach((part: string, index: number) => {
      currentPath = currentPath ? `${currentPath}/${part}` : part
      const isFile = index === parts.length - 1
      
      let node = currentLevel.find(n => n.name === part)
      
      if (!node) {
        node = {
          name: part,
          path: currentPath,
          isFile,
          children: isFile ? undefined : []
        }
        
        if (isFile) {
          node.stats = file
        }
        
        currentLevel.push(node)
      }
      
      if (!isFile && node.children) {
        currentLevel = node.children
      }
    })
  })
  
  return root
})

// 选中文件的统计信息
const selectedFileStats = computed(() => {
  if (!selectedFilePath.value) return { additions: 0, deletions: 0, addPercent: 0, deletePercent: 0 }
  const file = fileStats.value.find(f => f.name === selectedFilePath.value)
  return file || { additions: 0, deletions: 0, addPercent: 0, deletePercent: 0 }
})

// 选中文件的diff
const selectedFileDiff = computed(() => {
  if (!selectedFilePath.value || !fileDiffs.value.has(selectedFilePath.value)) return ''
  return formatFileDiff(fileDiffs.value.get(selectedFilePath.value) || '')
})

// 总代码统计
const totalAdditions = computed(() => {
  return fileStats.value.reduce((sum, file) => sum + file.additions, 0)
})

const totalDeletions = computed(() => {
  return fileStats.value.reduce((sum, file) => sum + file.deletions, 0)
})

const totalChanges = computed(() => {
  return totalAdditions.value + totalDeletions.value
})

// 格式化时间
const formattedDate = computed(() => {
  if (!commitDetail.value?.date) return ''
  
  const dateStr = commitDetail.value.date
  const date = new Date(dateStr)
  
  if (isNaN(date.getTime())) return dateStr
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
})

// 解析diff，按文件分割
function parseDiffByFile() {
  if (!rawDiff.value) return
  
  const lines = rawDiff.value.split('\n')
  let currentFile = ''
  let tempOldFile = ''
  let currentDiff: string[] = []
  const diffs = new Map<string, string>()
  
  for (const line of lines) {
    if (/^\d+\s+\d+\s+.+$/.test(line)) {
      continue
    }
    
    if (line.startsWith('diff --git')) {
      if (currentFile && currentDiff.length > 0) {
        diffs.set(currentFile, currentDiff.join('\n'))
      }
      currentDiff = [line]
      currentFile = ''
      tempOldFile = ''
    }
    else if (line.startsWith('---')) {
      const match = line.match(/^---\s+a\/(.+)$/)
      if (match) {
        tempOldFile = match[1]
      }
      currentDiff.push(line)
    }
    else if (line.startsWith('+++')) {
      const match = line.match(/^\+\+\+\s+b\/(.+)$/)
      if (match && match[1] !== '/dev/null') {
        currentFile = match[1]
      } else if (tempOldFile) {
        currentFile = tempOldFile
      }
      currentDiff.push(line)
    }
    else if (currentFile || tempOldFile) {
      currentDiff.push(line)
    }
  }
  
  if (currentFile && currentDiff.length > 0) {
    diffs.set(currentFile, currentDiff.join('\n'))
  }
  
  fileDiffs.value = diffs
}

// 格式化单个文件的diff
function formatFileDiff(diff: string): string {
  if (!diff) return ''
  
  const lines = diff.split('\n')
  let html = ''
  let inDiff = false
  let oldLineNum = 0
  let newLineNum = 0
  
  for (const line of lines) {
    if (line.startsWith('diff --git')) {
      inDiff = true
      continue
    }
    else if (line.startsWith('---') || line.startsWith('+++')) {
      continue
    }
    else if (line.startsWith('@@')) {
      const readableText = parseHunkHeader(line)
      html += `<div class="diff-hunk">${readableText}</div>`
      
      // 解析行号
      const match = line.match(/@@\s+-(\d+)(?:,\d+)?\s+\+(\d+)(?:,\d+)?\s+@@/)
      if (match) {
        oldLineNum = parseInt(match[1])
        newLineNum = parseInt(match[2])
      }
    }
    else if (line.startsWith('+') && inDiff) {
      html += `<div class="diff-line add-line">
        <span class="line-num old-num"></span>
        <span class="line-num new-num">${newLineNum}</span>
        <span class="line-content">${escapeHtml(line)}</span>
      </div>`
      newLineNum++
    }
    else if (line.startsWith('-') && inDiff) {
      html += `<div class="diff-line delete-line">
        <span class="line-num old-num">${oldLineNum}</span>
        <span class="line-num new-num"></span>
        <span class="line-content">${escapeHtml(line)}</span>
      </div>`
      oldLineNum++
    }
    else if (inDiff) {
      html += `<div class="diff-line normal-line">
        <span class="line-num old-num">${oldLineNum}</span>
        <span class="line-num new-num">${newLineNum}</span>
        <span class="line-content">${escapeHtml(line)}</span>
      </div>`
      oldLineNum++
      newLineNum++
    }
  }
  
  return html
}

// 解析并转换 hunk header 为可读文本
function parseHunkHeader(hunk: string): string {
  const match = hunk.match(/@@\s+-(\d+)(?:,(\d+))?\s+\+(\d+)(?:,(\d+))?\s+@@/)
  
  if (!match) return hunk
  
  const oldStart = parseInt(match[1])
  const oldLines = match[2] ? parseInt(match[2]) : 1
  const newStart = parseInt(match[3])
  const newLines = match[4] ? parseInt(match[4]) : 1
  
  if (oldLines === 0 && newLines > 0) {
    return `📝 从第 ${newStart} 行开始，新增了 ${newLines} 行`
  } else if (oldLines > 0 && newLines === 0) {
    return `🗑️ 从第 ${oldStart} 行开始，删除了 ${oldLines} 行`
  } else if (oldLines === newLines) {
    return `✏️ 第 ${oldStart}-${oldStart + oldLines - 1} 行有修改（共 ${oldLines} 行）`
  } else {
    return `🔄 第 ${oldStart} 行附近：删除 ${oldLines} 行，新增 ${newLines} 行`
  }
}

// HTML转义
function escapeHtml(text: string): string {
  const map: any = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return text.replace(/[&<>"']/g, (m) => map[m])
}

// 选择文件
function handleSelectFile(filePath: string) {
  selectedFilePath.value = filePath
  currentHunkIndex.value = 0
  
  nextTick(() => {
    updateHunkCount()
  })
}

// 更新 hunk 数量
function updateHunkCount() {
  if (!diffContentContainer.value) {
    hunkCount.value = 0
    return
  }
  
  const hunks = diffContentContainer.value.querySelectorAll('.diff-hunk')
  hunkCount.value = hunks.length
}

// 导航到指定 hunk
function navigateToHunk(direction: 'prev' | 'next') {
  if (!diffContentContainer.value) return
  
  const hunks = diffContentContainer.value.querySelectorAll('.diff-hunk')
  if (hunks.length === 0) return
  
  if (direction === 'next') {
    currentHunkIndex.value = (currentHunkIndex.value + 1) % hunks.length
  } else if (direction === 'prev') {
    currentHunkIndex.value = currentHunkIndex.value === 0 ? hunks.length - 1 : currentHunkIndex.value - 1
  }
  
  const targetHunk = hunks[currentHunkIndex.value] as HTMLElement
  if (targetHunk) {
    hunks.forEach(h => h.classList.remove('active-hunk'))
    targetHunk.classList.add('active-hunk')
    
    const container = diffContentContainer.value
    const containerRect = container.getBoundingClientRect()
    const targetRect = targetHunk.getBoundingClientRect()
    const scrollOffset = targetRect.top - containerRect.top - 60
    
    container.scrollBy({
      top: scrollOffset,
      behavior: 'smooth'
    })
  }
}

// 获取提交详情
async function fetchCommitDetail() {
  if (!commitHash.value || !projectPath.value) {
    ElMessage.error('缺少必要参数')
    return
  }
  
  loading.value = true
  commitDetail.value = null
  rawDiff.value = ''
  selectedFilePath.value = ''
  fileDiffs.value = new Map()
  
  try {
    const response = await axios.get('/api/filesystem/commit-detail', {
      params: {
        projectPath: projectPath.value,
        commitHash: commitHash.value
      }
    })
    
    if (response.data.success) {
      rawDiff.value = response.data.diff
      
      const lines = response.data.diff.split('\n')
      commitDetail.value = {
        hash: lines[0] || commitHash.value,
        author: lines[1] || '未知',
        email: lines[2] || '',
        date: lines[3] || '',
        message: lines[4] || '无提交信息'
      }
      
      parseDiffByFile()
      
      if (fileStats.value.length > 0) {
        selectedFilePath.value = fileStats.value[0].name
        nextTick(() => {
          updateHunkCount()
        })
      }
    } else {
      ElMessage.error('获取提交详情失败')
    }
  } catch (error: any) {
    console.error('获取提交详情失败:', error)
    ElMessage.error(error.response?.data || '获取提交详情失败')
  } finally {
    loading.value = false
  }
}

// 返回项目详情
function goBack() {
  if (projectId.value) {
    // 带上returnTab和scrollToHash参数返回到提交记录tab的对应位置
    const returnTab = route.query.returnTab as string
    const scrollToHash = route.query.scrollToHash as string
    
    const query: any = {}
    if (returnTab) query.returnTab = returnTab
    if (scrollToHash) query.scrollToHash = scrollToHash
    
    router.push({
      path: `/project-manager/${projectId.value}`,
      query
    })
  } else {
    router.back()
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCommitDetail()
})
</script>

<style scoped>
.commit-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  background: white;
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.back-btn {
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  min-width: 0;
}

.info-main {
  flex: 1;
  min-width: 0;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.meta-item .el-icon {
  font-size: 14px;
  color: #909399;
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  background: #f5f7fa;
  border-radius: 6px;
  font-weight: 600;
}

.stat-add {
  color: #67c23a;
}

.stat-delete {
  color: #f56c6c;
}

.stat-total {
  color: #909399;
  font-size: 12px;
}

.commit-hash {
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  flex-shrink: 0;
  user-select: all;
  cursor: pointer;
  transition: all 0.2s;
}

.commit-hash:hover {
  background: #e8f4fd;
  border-color: #409eff;
  color: #409eff;
}

/* 加载和错误状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #909399;
  background: white;
  border-radius: 12px;
}

.loading-container .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-container .error-icon {
  font-size: 48px;
  color: #f56c6c;
  margin-bottom: 16px;
}

/* 提交详情内容 */
.commit-content {
  height: calc(100vh - 120px);
}

/* 左右分栏布局 */
.split-layout {
  display: flex;
  gap: 20px;
  height: 100%;
}

/* 左侧面板 */
.left-panel {
  width: 320px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
}

/* 右侧面板 */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
}

/* 面板标题 */
.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.panel-header span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 文件树容器 */
.file-tree {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* 文件统计条 */
.file-stat-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
}

.add-count {
  color: #67c23a;
  font-size: 13px;
  font-weight: 600;
  min-width: 50px;
}

.delete-count {
  color: #f56c6c;
  font-size: 13px;
  font-weight: 600;
  min-width: 50px;
  text-align: right;
}

.bar-container {
  flex: 1;
  height: 10px;
  background: #e4e7ed;
  border-radius: 5px;
  overflow: hidden;
  display: flex;
}

.bar {
  height: 100%;
  transition: width 0.3s;
}

.add-bar {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.delete-bar {
  background: linear-gradient(90deg, #f56c6c 0%, #f78989 100%);
}

/* 代码差异内容 */
.diff-content {
  flex: 1;
  overflow-y: auto;
  background: #fafafa;
  position: relative;
}

.diff-content pre {
  margin: 0;
  padding: 20px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.5;
}

/* Diff样式 */
:deep(.diff-hunk) {
  background: linear-gradient(135deg, #e8f4fd 0%, #d4e9f7 100%);
  color: #1f2d3d;
  padding: 8px 16px;
  margin: 12px 0;
  border-radius: 6px;
  font-weight: 600;
  border-left: 4px solid #409eff;
  transition: all 0.3s;
  scroll-margin-top: 20px;
}

:deep(.diff-hunk.active-hunk) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  border-left: 4px solid #0066cc;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transform: scale(1.02);
}

:deep(.diff-line) {
  display: flex;
  align-items: stretch;
  line-height: 1.8;
  transition: background-color 0.2s;
  font-size: 13px;
}

:deep(.line-num) {
  flex-shrink: 0;
  width: 50px;
  padding: 2px 8px;
  text-align: right;
  color: #909399;
  background: #fafafa;
  border-right: 1px solid #e4e7ed;
  user-select: none;
  font-size: 12px;
  font-family: 'Monaco', 'Courier New', monospace;
}

:deep(.old-num) {
  border-right: 1px solid #e4e7ed;
}

:deep(.new-num) {
  border-right: 2px solid #e4e7ed;
}

:deep(.line-content) {
  flex: 1;
  padding: 2px 16px;
  white-space: pre-wrap;
  word-break: break-all;
}

:deep(.add-line) {
  background: #f0f9ff;
}

:deep(.add-line .line-content) {
  color: #059669;
  background: #f0f9ff;
}

:deep(.add-line .new-num) {
  background: #e6f7ff;
  color: #67c23a;
  font-weight: 600;
}

:deep(.add-line:hover) {
  background: #e0f5ff;
}

:deep(.add-line:hover .line-content) {
  background: #e0f5ff;
}

:deep(.delete-line) {
  background: #fef2f2;
}

:deep(.delete-line .line-content) {
  color: #dc2626;
  background: #fef2f2;
}

:deep(.delete-line .old-num) {
  background: #ffe6e6;
  color: #f56c6c;
  font-weight: 600;
}

:deep(.delete-line:hover) {
  background: #fee2e2;
}

:deep(.delete-line:hover .line-content) {
  background: #fee2e2;
}

:deep(.normal-line .line-content) {
  color: #606266;
}

:deep(.normal-line:hover) {
  background: #f5f7fa;
}

:deep(.normal-line:hover .line-content) {
  background: #f5f7fa;
}

/* 滚动条美化 */
.diff-content::-webkit-scrollbar,
.file-tree::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.diff-content::-webkit-scrollbar-track,
.file-tree::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.diff-content::-webkit-scrollbar-thumb,
.file-tree::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.diff-content::-webkit-scrollbar-thumb:hover,
.file-tree::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 导航按钮 */
.diff-navigation {
  position: fixed;
  right: 40px;
  bottom: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 8px 12px;
  border-radius: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 100;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.nav-info {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  padding: 0 8px;
  user-select: none;
}

.diff-navigation :deep(.el-button) {
  transition: all 0.2s;
}

.diff-navigation :deep(.el-button:hover:not(:disabled)) {
  transform: scale(1.1);
}

.diff-navigation :deep(.el-button:active) {
  transform: scale(0.95);
}
</style>
