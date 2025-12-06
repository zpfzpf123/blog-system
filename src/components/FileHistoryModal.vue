<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'
import TreeNode from './TreeNode.vue'
import { 
  Document,
  Folder,
  FolderOpened,
  Clock,
  User,
  Search,
  Refresh,
  Loading,
  ArrowRight
} from '@element-plus/icons-vue'

interface FileCommit {
  hash: string
  author: string
  date: string
  message: string
}

interface TreeNodeData {
  name: string
  path: string
  isDirectory: boolean
  children?: TreeNodeData[]
  hasChildren?: boolean
  expanded?: boolean
}

const props = defineProps<{
  modelValue: boolean
  projectId?: number
}>()

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 状态
const treeData = ref<TreeNodeData[]>([])
const selectedPath = ref('')
const router = useRouter()

const selectedNode = ref<TreeNodeData | null>(null)
const commits = ref<FileCommit[]>([])
const loading = ref(false)
const loadingTree = ref(false)
const searchKeyword = ref('')
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)

// 筛选后的提交记录
const filteredCommits = computed(() => {
  if (!searchKeyword.value.trim()) return commits.value
  
  const keyword = searchKeyword.value.toLowerCase().trim()
  return commits.value.filter(commit => 
    commit.message.toLowerCase().includes(keyword) ||
    commit.hash.toLowerCase().includes(keyword) ||
    commit.author.toLowerCase().includes(keyword)
  )
})

// 递归恢复目录展开状态
const restoreExpandedState = async (nodes: TreeNodeData[], expandedPaths: string[]) => {
  for (const node of nodes) {
    if (expandedPaths.includes(node.path)) {
      node.expanded = true
      // 如果有子节点，加载并递归展开
      if (node.isDirectory && node.hasChildren && (!node.children || node.children.length === 0)) {
        await loadChildren(node)
      }
      if (node.children && node.children.length > 0) {
        await restoreExpandedState(node.children, expandedPaths)
      }
    }
  }
}

// 监听弹窗打开，加载目录树
watch(() => props.modelValue, async (newVal) => {
  if (newVal && props.projectId) {
    // 先加载目录树
    await loadDirectoryTree()
    
    // 检查是否需要恢复之前的状态
    let savedFilePath = sessionStorage.getItem('fileHistoryFilePath')
    let savedExpandedPaths = sessionStorage.getItem('fileHistoryExpandedPaths')
    const savedSelectedPath = sessionStorage.getItem('fileHistorySelectedPath')
    
    // 优先使用从提交详情页返回的文件路径，否则使用保存的选中路径
    const filePathToRestore = savedFilePath || savedSelectedPath
    
    if (filePathToRestore || savedExpandedPaths) {
      // 延迟恢复状态，等待目录树渲染完成
      setTimeout(async () => {
        try {
          // 恢复展开状态
          if (savedExpandedPaths) {
            const expandedPaths = JSON.parse(savedExpandedPaths)
            console.log('恢复展开路径:', expandedPaths)
            await restoreExpandedState(treeData.value, expandedPaths)
            // 强制触发响应式更新
            treeData.value = [...treeData.value]
          }
          
          // 恢复选中的文件
          if (filePathToRestore) {
            selectedPath.value = filePathToRestore
            console.log('恢复选中文件:', filePathToRestore)
            
            // 加载历史记录
            await viewHistory()
            
            // 滚动到选中的节点
            setTimeout(() => {
              const treeContainer = document.querySelector('.file-tree')
              const selectedNode = document.querySelector('.tree-node.selected')
              if (selectedNode && treeContainer) {
                selectedNode.scrollIntoView({ behavior: 'smooth', block: 'center' })
              }
            }, 300)
          }
          
          // 清理sessionStorage
          sessionStorage.removeItem('fileHistoryFilePath')
          sessionStorage.removeItem('fileHistoryCommitHash')
          sessionStorage.removeItem('fileHistoryExpandedPaths')
          sessionStorage.removeItem('fileHistorySelectedPath')
        } catch (error) {
          console.error('恢复状态失败:', error)
        }
      }, 800)
    }
  }
})

// 保存目录树状态到sessionStorage
const saveTreeState = () => {
  if (selectedPath.value) {
    // 收集所有展开的节点路径
    const expandedPaths: string[] = []
    const collectExpandedPaths = (nodes: TreeNodeData[]) => {
      nodes.forEach(node => {
        if (node.expanded && node.isDirectory) {
          expandedPaths.push(node.path)
        }
        if (node.children) {
          collectExpandedPaths(node.children)
        }
      })
    }
    collectExpandedPaths(treeData.value)
    
    sessionStorage.setItem('fileHistoryExpandedPaths', JSON.stringify(expandedPaths))
    sessionStorage.setItem('fileHistorySelectedPath', selectedPath.value)
  }
}

// 关闭弹窗
const handleClose = () => {
  // 保存状态（如果不是通过跳转关闭的）
  if (!sessionStorage.getItem('returnToFileHistory')) {
    saveTreeState()
  }
  
  visible.value = false
  contextMenuVisible.value = false
  
  // 不立即清空数据，保留300ms以便可能的状态保存
  setTimeout(() => {
    // 只有在没有返回标记时才清空数据
    if (!sessionStorage.getItem('returnToFileHistory')) {
      treeData.value = []
      selectedPath.value = ''
      selectedNode.value = null
      commits.value = []
      searchKeyword.value = ''
    }
  }, 300)
}

// 初始化节点的expanded属性
const initNodeExpanded = (node: TreeNodeData) => {
  if (node.isDirectory && node.expanded === undefined) {
    node.expanded = false
  }
  if (node.children && node.children.length > 0) {
    node.children.forEach(child => initNodeExpanded(child))
  }
}

// 加载目录树
const loadDirectoryTree = async () => {
  if (!props.projectId) return
  
  try {
    loadingTree.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/directory-tree`)
    
    if (response.data.success && response.data.tree) {
      const tree = response.data.tree
      // 初始化所有节点的expanded属性
      initNodeExpanded(tree)
      treeData.value = [tree]
    } else {
      ElMessage.error(response.data.message || '加载目录失败')
    }
  } catch (error: any) {
    console.error('加载目录树失败:', error)
    ElMessage.error('加载目录失败')
  } finally {
    loadingTree.value = false
  }
}

// 切换节点展开/折叠
const toggleNode = async (node: TreeNodeData) => {
  if (!node.isDirectory) return
  
  // 使用Vue的响应式方式更新
  if (node.expanded === undefined) {
    node.expanded = true
  } else {
    node.expanded = !node.expanded
  }
  
  // 如果是展开且没有加载过子节点
  if (node.expanded && node.hasChildren && (!node.children || node.children.length === 0)) {
    await loadChildren(node)
  }
  
  // 强制触发响应式更新
  treeData.value = [...treeData.value]
}

// 加载子节点
const loadChildren = async (node: TreeNodeData) => {
  if (!props.projectId) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/expand-directory`, {
      path: node.path
    })
    
    if (response.data.success) {
      const children = response.data.children || []
      // 初始化每个子节点的expanded属性
      children.forEach((child: TreeNodeData) => {
        if (child.isDirectory && child.expanded === undefined) {
          child.expanded = false
        }
      })
      node.children = children
      // 强制触发响应式更新
      treeData.value = [...treeData.value]
    }
  } catch (error: any) {
    console.error('加载子节点失败:', error)
    ElMessage.error('加载失败')
  }
}

// 右键菜单
const handleContextMenu = (event: MouseEvent, node: TreeNodeData) => {
  event.preventDefault()
  event.stopPropagation()
  
  selectedNode.value = node
  selectedPath.value = node.path
  
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true
  
  // 点击其他地方关闭菜单
  const closeMenu = () => {
    contextMenuVisible.value = false
    document.removeEventListener('click', closeMenu)
  }
  document.addEventListener('click', closeMenu)
}

// 查看Git历史
const viewHistory = async () => {
  contextMenuVisible.value = false
  
  if (!selectedPath.value || !props.projectId) return
  
  try {
    loading.value = true
    const response = await axios.post(`/api/projects/${props.projectId}/file-history`, {
      filePath: selectedPath.value
    })
    
    if (response.data.success) {
      commits.value = response.data.commits || []
      if (commits.value.length === 0) {
        ElMessage.info('该文件/文件夹暂无提交记录')
      } else {
        ElMessage.success(`找到 ${commits.value.length} 条提交记录`)
      }
    } else {
      ElMessage.error(response.data.message || '获取失败')
      commits.value = []
    }
  } catch (error: any) {
    console.error('获取文件历史失败:', error)
    ElMessage.error('获取失败')
    commits.value = []
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 双击文件查看内容
const handleDoubleClickFile = async (node: TreeNodeData) => {
  if (node.isDirectory) return
  
  console.log('双击文件:', node.path)
  console.log('项目ID:', props.projectId)
  
  try {
    // 使用后端API获取文件内容
    const response = await axios.post(`/api/projects/${props.projectId}/read-file`, {
      filePath: node.path
    })
    
    console.log('API响应:', response.data)
    
    if (response.data.success) {
      // 创建一个新窗口显示文件内容
      const fileWindow = window.open('', '_blank', 'width=900,height=700')
      if (fileWindow) {
        fileWindow.document.write(`
          <!DOCTYPE html>
          <html>
          <head>
            <title>${node.name}</title>
            <style>
              body {
                margin: 0;
                padding: 20px;
                font-family: 'Consolas', 'Monaco', monospace;
                background: #1e1e1e;
                color: #d4d4d4;
              }
              .header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 16px 20px;
                margin: -20px -20px 20px -20px;
                border-radius: 0 0 8px 8px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.2);
              }
              .file-path {
                font-size: 14px;
                opacity: 0.9;
                margin-top: 4px;
              }
              pre {
                background: #252526;
                border: 1px solid #3e3e42;
                border-radius: 8px;
                padding: 20px;
                overflow: auto;
                line-height: 1.6;
                box-shadow: 0 4px 12px rgba(0,0,0,0.3);
              }
              code {
                color: #d4d4d4;
              }
            </style>
          </head>
          <body>
            <div class="header">
              <h2 style="margin: 0;">${node.name}</h2>
              <div class="file-path">${node.path}</div>
            </div>
            <pre><code>${response.data.content.replace(/</g, '&lt;').replace(/>/g, '&gt;')}</code></pre>
          </body>
          </html>
        `)
        fileWindow.document.close()
      }
    } else {
      console.error('读取失败:', response.data.message)
      ElMessage.error(response.data.message || '读取文件失败')
    }
  } catch (error: any) {
    console.error('读取文件异常:', error)
    console.error('错误详情:', error.response?.data)
    ElMessage.error(error.response?.data?.message || error.message || '读取文件失败')
  }
}

// 查看提交详情
const viewCommitDetail = async (commit: FileCommit) => {
  // 跳转到提交详情页面
  if (!props.projectId) return
  
  try {
    // 获取项目信息
    const response = await axios.get(`/api/projects/${props.projectId}`)
    const project = response.data
    
    if (!project || !project.localPath) {
      ElMessage.error('项目路径不存在')
      return
    }
    
    // 在跳转前保存目录树状态
    saveTreeState()
    
    // 关闭当前弹窗
    visible.value = false
    
    // 跳转到提交详情页面
    router.push({
      name: 'CommitDetail',
      params: { id: props.projectId },
      query: {
        hash: commit.hash,
        path: project.localPath,
        returnTab: 'commits',
        scrollToHash: commit.hash,
        filePath: selectedPath.value // 传递文件路径以便定位
      }
    })
  } catch (error: any) {
    console.error('获取项目信息失败:', error)
    ElMessage.error('跳转失败')
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="文件历史记录 - 右键点击文件/文件夹查看Git历史"
    width="1200px"
    :close-on-click-modal="false"
    :before-close="handleClose"
    class="file-history-dialog"
  >
    <div class="dialog-content">
      <!-- 左侧目录树面板 -->
      <div class="tree-panel">
        <div class="panel-header">
          <div class="header-left">
            <el-icon><FolderOpened /></el-icon>
            <span>项目目录</span>
          </div>
          <el-tooltip content="拖动右边框可调整宽度" placement="top">
            <el-icon class="resize-hint"><ArrowRight /></el-icon>
          </el-tooltip>
        </div>
        <div v-if="loadingTree" class="tree-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else class="tree-container">
          <TreeNode
            v-for="node in treeData"
            :key="node.path"
            :node="node"
            :level="0"
            @toggle-node="toggleNode"
            @show-context-menu="handleContextMenu"
            @double-click-file="handleDoubleClickFile"
          />
        </div>
      </div>
      
      <!-- 右侧提交历史 -->
      <div class="history-panel">
        <div class="panel-header">
          <div class="header-left">
            <el-icon><Clock /></el-icon>
            <span v-if="selectedPath">{{ selectedPath }}</span>
            <span v-else class="hint-text">请在左侧选择文件或文件夹</span>
          </div>
          <el-button 
            v-if="commits.length > 0"
            size="small"
            :icon="Refresh"
            @click="viewHistory"
            :loading="loading"
          >
            刷新
          </el-button>
        </div>

      <!-- 提交记录搜索 -->
      <div v-if="commits.length > 0" class="filter-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索提交信息、作者或Hash..."
          :prefix-icon="Search"
          clearable
          size="small"
        />
        <span class="result-count">找到 {{ filteredCommits.length }} 条记录</span>
      </div>

        <!-- 提交记录列表 -->
        <div class="commits-container">
          <!-- 空状态 - 未选择 -->
          <div v-if="commits.length === 0 && !loading && !selectedPath" class="empty-state">
            <el-icon class="empty-icon"><Document /></el-icon>
            <p class="empty-title">请在左侧选择文件或文件夹</p>
            <p class="empty-desc">右键点击文件/文件夹可查看Git提交历史</p>
          </div>

          <!-- 加载中 -->
          <div v-else-if="loading" class="loading-state">
            <el-icon class="is-loading loading-icon"><Loading /></el-icon>
            <p>正在查询历史记录...</p>
          </div>

          <!-- 无记录 -->
          <div v-else-if="commits.length === 0 && selectedPath" class="empty-state">
            <el-icon class="empty-icon"><Clock /></el-icon>
            <p class="empty-title">暂无提交记录</p>
            <p class="empty-desc">该文件/文件夹还没有Git提交记录</p>
          </div>
          <!-- 搜索无结果 -->
          <div v-else-if="filteredCommits.length === 0" class="empty-state">
            <el-icon class="empty-icon"><Search /></el-icon>
            <p class="empty-title">未找到匹配的记录</p>
            <p class="empty-desc">尝试调整搜索关键词</p>
          </div>

          <!-- 提交记录列表 -->
          <div v-else class="timeline">
            <div 
              v-for="(commit, index) in filteredCommits" 
              :key="commit.hash" 
              class="timeline-item"
              @click="viewCommitDetail(commit)"
            >
              <div class="timeline-dot"></div>
              <div v-if="index < filteredCommits.length - 1" class="timeline-line"></div>
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
      </div>
    </div>

    <!-- 右键菜单 -->
    <teleport to="body">
      <div 
        v-show="contextMenuVisible" 
        class="context-menu"
        :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      >
        <div class="context-menu-item" @click="viewHistory">
          <el-icon><Clock /></el-icon>
          <span>查看Git提交历史</span>
        </div>
      </div>
    </teleport>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-content {
  height: 70vh;
  display: flex;
  gap: 16px;
}

/* 左侧目录树面板 */
.tree-panel {
  width: 400px;
  min-width: 300px;
  max-width: 500px;
  display: flex;
  flex-direction: column;
  border: 1px solid #e1e8f0;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f9fafb 100%);
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  resize: horizontal;
}

.tree-panel:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  border-color: #c6d4e1;
}

/* 右侧历史面板 */
.history-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e1e8f0;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f9fafb 100%);
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.history-panel:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  border-color: #c6d4e1;
}

/* 面板头部 */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  font-size: 14px;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
  position: relative;
  overflow: hidden;
}

.panel-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.panel-header:hover::before {
  left: 100%;
}

.panel-header .header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.resize-hint {
  font-size: 16px;
  opacity: 0.7;
  transition: all 0.3s ease;
  cursor: help;
}

.resize-hint:hover {
  opacity: 1;
  transform: scale(1.2);
}

.hint-text {
  color: rgba(255, 255, 255, 0.85);
  font-weight: normal;
}

/* 树加载状态 */
.tree-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 8px;
  color: #409eff;
}

/* 树容器 */
.tree-container {
  flex: 1;
  overflow-x: auto;
  overflow-y: auto;
  padding: 12px 0;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
}

/* 横向滚动条 */
.tree-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.tree-container::-webkit-scrollbar-track {
  background: linear-gradient(180deg, #f5f7fa 0%, #e8ecf0 100%);
  border-radius: 4px;
}

.tree-container::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #c0c4cc 0%, #a8abb2 100%);
  border-radius: 4px;
  border: 2px solid #f5f7fa;
  transition: all 0.3s ease;
}

.tree-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #909399 0%, #73767a 100%);
  border-color: #e8ecf0;
}

.tree-container::-webkit-scrollbar-corner {
  background: #f5f7fa;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: linear-gradient(180deg, #ffffff 0%, #f9fafb 100%);
  border: 1px solid #e1e8f0;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 2px 6px rgba(0, 0, 0, 0.08);
  padding: 6px;
  z-index: 9999;
  min-width: 200px;
  animation: menuFadeIn 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

@keyframes menuFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-5px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #303133;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  font-weight: 500;
}

.context-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 0;
  height: 100%;
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, transparent 100%);
  transition: width 0.3s ease;
  z-index: 0;
}

.context-menu-item:hover::before {
  width: 100%;
}

.context-menu-item > * {
  position: relative;
  z-index: 1;
}

.context-menu-item:hover {
  background: linear-gradient(135deg, #e6f7ff 0%, #d9efff 100%);
  color: #409eff;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.context-menu-item .el-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.context-menu-item:hover .el-icon {
  transform: scale(1.15) rotate(5deg);
  color: #409eff;
}

/* 筛选区域 */
.filter-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f0f2f5;
  border-radius: 6px;
}

.filter-section .el-input {
  flex: 1;
}

.result-count {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  font-weight: 500;
}

/* 提交记录容器 */
.commits-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

/* 滚动条美化 */
.commits-container::-webkit-scrollbar {
  width: 6px;
}
.commits-container::-webkit-scrollbar-track {
  background: #f5f7fa;
}
.commits-container::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}
.commits-container::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
}

.empty-icon {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #409eff;
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 时间轴样式 */
.timeline {
  position: relative;
  padding: 10px 0;
}

.timeline-item {
  position: relative;
  padding-left: 40px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.timeline-item:hover {
  transform: translateX(4px);
}

.timeline-item:hover .timeline-content {
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: 0;
  top: 6px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #409eff;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px #409eff;
  z-index: 2;
}

.timeline-line {
  position: absolute;
  left: 5px;
  top: 18px;
  bottom: -20px;
  width: 2px;
  background: #e8ecf1;
  z-index: 1;
}

.timeline-content {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e8ecf1;
  transition: all 0.3s ease;
}

.timeline-content:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.commit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.commit-hash {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  color: #409eff;
  font-weight: 600;
  background: #ecf5ff;
  padding: 3px 8px;
  border-radius: 4px;
}

.commit-date {
  font-size: 12px;
  color: #909399;
}

.commit-message {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 10px;
  font-weight: 500;
}

.commit-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.commit-author .el-icon {
  color: #909399;
}

/* 对话框页脚 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
