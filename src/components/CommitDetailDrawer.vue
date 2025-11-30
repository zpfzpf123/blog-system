<template>
  <el-drawer
    v-model="visible"
    title="提交详情"
    direction="rtl"
    size="80%"
    :before-close="handleClose"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 提交详情内容 -->
    <div v-else-if="commitDetail" class="commit-detail">
      <!-- 提交基本信息 -->
      <div class="commit-info-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="label">提交哈希:</span>
            <span class="value hash">{{ commitDetail.hash }}</span>
          </div>
          <div class="info-item">
            <span class="label">提交时间:</span>
            <span class="value">{{ formattedDate }}</span>
          </div>
          <div class="info-item">
            <span class="label">提交信息:</span>
            <span class="value">{{ commitDetail.message }}</span>
          </div>
          <div class="info-item">
            <span class="label">作者:</span>
            <span class="value">{{ commitDetail.author }}</span>
          </div>
        </div>
      </div>

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
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { Loading, Document, Edit, WarningFilled, FolderOpened, Folder, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'
import FileTreeNode from './FileTreeNode.vue'

// Props
interface Props {
  modelValue: boolean
  commitHash: string
  projectPath: string
}

const props = defineProps<Props>()
const emit = defineEmits(['update:modelValue'])

// 数据
const loading = ref(false)
const commitDetail = ref<any>(null)
const rawDiff = ref('')
const selectedFilePath = ref('')
const fileDiffs = ref<Map<string, string>>(new Map())
const diffContentContainer = ref<HTMLElement | null>(null)
const currentHunkIndex = ref(0)
const hunkCount = ref(0)

// 控制显示
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 文件统计信息
const fileStats = computed(() => {
  if (!rawDiff.value) return []
  
  const lines = rawDiff.value.split('\n')
  const stats: any[] = []
  
  // 解析 numstat 格式的行（添加\t删除\t文件名）
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

// 格式化时间
const formattedDate = computed(() => {
  if (!commitDetail.value?.date) return ''
  
  // 解析 Git 时间格式: "Mon Dec 1 00:33:28 2025 +0800"
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
  let tempOldFile = '' // 临时存储 --- 行的文件路径
  let currentDiff: string[] = []
  const diffs = new Map<string, string>()
  
  for (const line of lines) {
    // 跳过numstat行
    if (/^\d+\s+\d+\s+.+$/.test(line)) {
      continue
    }
    
    // 检测新文件开始
    if (line.startsWith('diff --git')) {
      // 保存上一个文件的diff
      if (currentFile && currentDiff.length > 0) {
        diffs.set(currentFile, currentDiff.join('\n'))
      }
      // 重置
      currentDiff = [line]
      currentFile = ''
      tempOldFile = ''
    }
    // 提取旧文件路径（删除文件时需要）
    else if (line.startsWith('---')) {
      const match = line.match(/^---\s+a\/(.+)$/)
      if (match) {
        tempOldFile = match[1]
      }
      currentDiff.push(line)
    }
    // 提取新文件路径
    else if (line.startsWith('+++')) {
      const match = line.match(/^\+\+\+\s+b\/(.+)$/)
      if (match && match[1] !== '/dev/null') {
        // 新增或修改的文件
        currentFile = match[1]
      } else if (tempOldFile) {
        // 删除的文件（+++ /dev/null），使用 --- 的路径
        currentFile = tempOldFile
      }
      currentDiff.push(line)
    }
    else if (currentFile || tempOldFile) {
      currentDiff.push(line)
    }
  }
  
  // 保存最后一个文件
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
  
  for (const line of lines) {
    // diff文件头
    if (line.startsWith('diff --git')) {
      inDiff = true
      continue
    }
    // 文件路径
    else if (line.startsWith('---') || line.startsWith('+++')) {
      continue
    }
    // 位置信息 - 转换为可读文本
    else if (line.startsWith('@@')) {
      const readableText = parseHunkHeader(line)
      html += `<div class="diff-hunk">${readableText}</div>`
    }
    // 添加的行
    else if (line.startsWith('+') && inDiff) {
      html += `<div class="diff-line add-line">${escapeHtml(line)}</div>`
    }
    // 删除的行
    else if (line.startsWith('-') && inDiff) {
      html += `<div class="diff-line delete-line">${escapeHtml(line)}</div>`
    }
    // 普通行
    else if (inDiff) {
      html += `<div class="diff-line normal-line">${escapeHtml(line)}</div>`
    }
  }
  
  return html
}

// 解析并转换 hunk header 为可读文本
function parseHunkHeader(hunk: string): string {
  // 格式: @@ -oldStart,oldLines +newStart,newLines @@
  const match = hunk.match(/@@\s+-(\d+)(?:,(\d+))?\s+\+(\d+)(?:,(\d+))?\s+@@/)
  
  if (!match) return hunk
  
  const oldStart = parseInt(match[1])
  const oldLines = match[2] ? parseInt(match[2]) : 1
  const newStart = parseInt(match[3])
  const newLines = match[4] ? parseInt(match[4]) : 1
  
  // 根据不同情况生成可读文本
  if (oldLines === 0 && newLines > 0) {
    // 新增内容
    return `📝 从第 ${newStart} 行开始，新增了 ${newLines} 行`
  } else if (oldLines > 0 && newLines === 0) {
    // 删除内容
    return `🗑️ 从第 ${oldStart} 行开始，删除了 ${oldLines} 行`
  } else if (oldLines === newLines) {
    // 修改内容
    return `✏️ 第 ${oldStart}-${oldStart + oldLines - 1} 行有修改（共 ${oldLines} 行）`
  } else {
    // 混合修改
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
  
  // 等待 DOM 更新后计算 hunk 数量
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
  
  // 更新索引（循环导航）
  if (direction === 'next') {
    // 到达最后一个后，跳回第一个
    currentHunkIndex.value = (currentHunkIndex.value + 1) % hunks.length
  } else if (direction === 'prev') {
    // 到达第一个后，跳到最后一个
    currentHunkIndex.value = currentHunkIndex.value === 0 ? hunks.length - 1 : currentHunkIndex.value - 1
  }
  
  // 滚动到目标位置
  const targetHunk = hunks[currentHunkIndex.value] as HTMLElement
  if (targetHunk) {
    // 移除之前的高亮
    hunks.forEach(h => h.classList.remove('active-hunk'))
    // 添加当前高亮
    targetHunk.classList.add('active-hunk')
    
    // 获取滚动容器
    const container = diffContentContainer.value
    
    // 计算目标元素相对于容器的位置
    const containerRect = container.getBoundingClientRect()
    const targetRect = targetHunk.getBoundingClientRect()
    
    // 计算需要滚动的距离（让目标元素显示在容器顶部下方20px）
    const scrollOffset = targetRect.top - containerRect.top - 60
    
    container.scrollBy({
      top: scrollOffset,
      behavior: 'smooth'
    })
  }
}

// 获取提交详情
async function fetchCommitDetail() {
  if (!props.commitHash || !props.projectPath) return
  
  loading.value = true
  commitDetail.value = null
  rawDiff.value = ''
  selectedFilePath.value = ''
  fileDiffs.value = new Map()
  
  try {
    const response = await axios.get('/api/filesystem/commit-detail', {
      params: {
        projectPath: props.projectPath,
        commitHash: props.commitHash
      }
    })
    
    if (response.data.success) {
      rawDiff.value = response.data.diff
      
      // 解析基本信息（从diff的前几行提取）
      const lines = response.data.diff.split('\n')
      commitDetail.value = {
        hash: lines[0] || props.commitHash,
        author: lines[1] || '未知',
        email: lines[2] || '',
        date: lines[3] || '',
        message: lines[4] || '无提交信息'
      }
      
      // 解析diff按文件分割
      parseDiffByFile()
      
      // 默认选中第一个文件
      if (fileStats.value.length > 0) {
        selectedFilePath.value = fileStats.value[0].name
        // 等待 DOM 更新后更新 hunk 计数
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

// 关闭抽屉
function handleClose() {
  visible.value = false
}

// 监听显示状态
watch(() => props.modelValue, (newVal) => {
  if (newVal && props.commitHash) {
    fetchCommitDetail()
  } else {
    // 关闭时重置
    currentHunkIndex.value = 0
    hunkCount.value = 0
  }
})

// 监听选中文件变化，更新 hunk 计数
watch(() => selectedFileDiff.value, () => {
  nextTick(() => {
    updateHunkCount()
  })
})
</script>

<style scoped>
/* 加载和错误状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #909399;
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

/* 提交详情 */
.commit-detail {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px;
  gap: 20px;
}

/* 提交信息卡片 */
.commit-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 20px;
  border-radius: 12px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.info-item .label {
  font-weight: 600;
  font-size: 13px;
  opacity: 0.9;
  white-space: nowrap;
  flex-shrink: 0;
}

.info-item .value {
  flex: 1;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-item .hash {
  font-family: 'Monaco', 'Courier New', monospace;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

/* 左右分栏布局 */
.split-layout {
  display: flex;
  flex: 1;
  gap: 20px;
  min-height: 0;
  overflow: hidden;
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
  font-size: 15px;
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
  padding: 4px 16px;
  line-height: 1.8;
  transition: background-color 0.2s;
}

:deep(.add-line) {
  background: #f0f9ff;
  color: #059669;
  border-left: 3px solid #67c23a;
}

:deep(.add-line:hover) {
  background: #e0f5ff;
}

:deep(.delete-line) {
  background: #fef2f2;
  color: #dc2626;
  border-left: 3px solid #f56c6c;
}

:deep(.delete-line:hover) {
  background: #fee2e2;
}

:deep(.normal-line) {
  color: #606266;
  background: transparent;
}

:deep(.normal-line:hover) {
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
