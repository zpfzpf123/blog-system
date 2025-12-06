<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import { Warning, Document, Refresh, FolderOpened, View, Check } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  projectId?: number
}>()

const emit = defineEmits(['update:modelValue', 'resolved'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 冲突文件列表
const conflictFiles = ref<string[]>([])
const loading = ref(false)
const selectedFile = ref<string | null>(null)
const fileDiff = ref('')
const loadingDiff = ref(false)

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const contextMenuFile = ref<string | null>(null)

// 加载冲突文件列表
const loadConflictFiles = async () => {
  if (!props.projectId) return
  
  try {
    loading.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/git/conflicts`)
    
    if (response.data.success) {
      conflictFiles.value = response.data.conflictFiles || []
    }
  } catch (error) {
    console.error('加载冲突文件失败:', error)
    ElMessage.error('加载冲突文件失败')
  } finally {
    loading.value = false
  }
}

// 查看文件内容（带冲突标记）
const viewFileContent = async (filePath: string) => {
  if (!props.projectId) {
    ElMessage.error('项目ID不存在')
    return
  }
  
  if (!filePath) {
    ElMessage.error('文件路径不能为空')
    return
  }
  
  selectedFile.value = filePath
  loadingDiff.value = true
  fileDiff.value = ''
  
  try {
    const response = await axios.get(`/api/projects/${props.projectId}/git/conflict-content`, {
      params: { filePath }
    })
    
    if (response.data.success) {
      fileDiff.value = response.data.content || ''
      if (!fileDiff.value) {
        fileDiff.value = '// 文件内容为空'
      }
    } else {
      const errorMsg = response.data.message || '未知错误'
      fileDiff.value = `// 获取文件内容失败: ${errorMsg}\n// 文件路径: ${filePath}`
      ElMessage.error(`获取文件内容失败: ${errorMsg}`)
    }
  } catch (error: any) {
    console.error('获取文件内容失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '网络请求失败'
    fileDiff.value = `// 获取文件内容失败: ${errorMsg}\n// 文件路径: ${filePath}`
    ElMessage.error(`获取文件内容失败: ${errorMsg}`)
  } finally {
    loadingDiff.value = false
  }
}


// 格式化冲突内容，高亮显示冲突标记
const formatConflictContent = (content: string): string => {
  if (!content) return ''
  
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .split('\n')
    .map(line => {
      if (line.startsWith('<<<<<<<')) {
        return `<span class="conflict-marker conflict-ours">${line}</span>`
      } else if (line.startsWith('=======')) {
        return `<span class="conflict-marker conflict-separator">${line}</span>`
      } else if (line.startsWith('>>>>>>>')) {
        return `<span class="conflict-marker conflict-theirs">${line}</span>`
      }
      return line
    })
    .join('\n')
}

// 显示右键菜单
const showContextMenu = (event: MouseEvent, file: string) => {
  event.preventDefault()
  contextMenuFile.value = file
  contextMenuPosition.value = { x: event.clientX, y: event.clientY }
  contextMenuVisible.value = true
  document.addEventListener('click', hideContextMenu, { once: true })
}

// 隐藏右键菜单
const hideContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuFile.value = null
}

// 在WebStorm中打开文件
const openInWebStorm = async (filePath?: string) => {
  const file = filePath || contextMenuFile.value
  
  if (!props.projectId) {
    ElMessage.error('项目ID不存在')
    return
  }
  
  if (!file) {
    ElMessage.error('请先选择要打开的文件')
    return
  }
  
  hideContextMenu()
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/open-file`, {
      fileName: file
    })
    
    if (response.data.success) {
      ElMessage.success('已在WebStorm中打开')
    } else {
      const errorMsg = response.data.message || '打开失败'
      const hint = response.data.hint || ''
      ElMessage.error({
        message: errorMsg,
        duration: 5000
      })
      if (hint) {
        console.log('提示:', hint, '文件路径:', response.data.filePath)
      }
    }
  } catch (error: any) {
    console.error('打开文件失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '网络请求失败'
    ElMessage.error(`打开文件失败: ${errorMsg}`)
  }
}

// 标记文件为已解决
const markAsResolved = async (filePath?: string) => {
  const file = filePath || contextMenuFile.value
  if (!props.projectId || !file) return
  
  hideContextMenu()
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/mark-resolved`, {
      filePath: file
    })
    
    if (response.data.success) {
      ElMessage.success('已标记为已解决')
      loadConflictFiles()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 全部在WebStorm中打开
const openAllInWebStorm = async () => {
  for (const file of conflictFiles.value) {
    await openInWebStorm(file)
  }
}

// 中止当前操作（rebase/merge/cherry-pick）
const abortOperation = async () => {
  if (!props.projectId) return
  
  try {
    await ElMessageBox.confirm(
      '确定要中止当前操作吗？这将放弃所有未完成的合并/变基操作。',
      '确认中止',
      {
        confirmButtonText: '确定中止',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/abort-operation`)
    
    if (response.data.success) {
      ElMessage.success('已中止操作')
      conflictFiles.value = []
      emit('resolved')
      visible.value = false
    } else {
      ElMessage.error(response.data.message || '中止失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('中止操作失败')
    }
  }
}

// 继续操作（解决冲突后）
const continueOperation = async () => {
  if (!props.projectId) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/continue-operation`)
    
    if (response.data.success) {
      ElMessage.success('操作完成')
      emit('resolved')
      visible.value = false
    } else {
      if (response.data.hasConflict) {
        ElMessage.warning('仍有未解决的冲突')
        loadConflictFiles()
      } else {
        ElMessage.error(response.data.message || '操作失败')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 获取文件名
const getFileName = (path: string) => {
  return path.split('/').pop() || path
}

// 获取文件目录
const getFileDir = (path: string) => {
  const parts = path.split('/')
  if (parts.length <= 1) return ''
  return parts.slice(0, -1).join('/')
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadConflictFiles()
    selectedFile.value = null
    fileDiff.value = ''
  }
})
</script>


<template>
  <el-dialog
    v-model="visible"
    title="冲突管理"
    width="1000px"
    :close-on-click-modal="false"
    class="git-conflict-dialog"
  >
    <div class="conflict-container">
      <!-- 左侧：冲突文件列表 -->
      <div class="conflict-list" v-loading="loading">
        <div class="list-header">
          <div class="header-title">
            <el-icon class="warning-icon"><Warning /></el-icon>
            <span>冲突文件 ({{ conflictFiles.length }})</span>
          </div>
          <el-button :icon="Refresh" size="small" text @click="loadConflictFiles">刷新</el-button>
        </div>
        
        <div v-if="conflictFiles.length === 0" class="empty-state">
          <el-icon size="48" color="#67c23a"><Check /></el-icon>
          <p>没有冲突文件</p>
          <span class="hint">当前工作区状态良好</span>
        </div>
        
        <div v-else class="files-scroll">
          <div 
            v-for="file in conflictFiles" 
            :key="file"
            class="conflict-file-item"
            :class="{ active: selectedFile === file }"
            @click="viewFileContent(file)"
            @dblclick="openInWebStorm(file)"
            @contextmenu="showContextMenu($event, file)"
          >
            <el-icon class="file-icon"><Document /></el-icon>
            <div class="file-info">
              <span class="file-name">{{ getFileName(file) }}</span>
              <span class="file-dir" v-if="getFileDir(file)">{{ getFileDir(file) }}</span>
            </div>
            <el-tooltip content="在WebStorm中打开" placement="left">
              <el-icon class="open-icon" @click.stop="openInWebStorm(file)"><FolderOpened /></el-icon>
            </el-tooltip>
          </div>
        </div>
        
        <!-- 批量操作 -->
        <div v-if="conflictFiles.length > 0" class="batch-actions">
          <el-button size="small" type="primary" @click="openAllInWebStorm">
            全部在WebStorm中打开
          </el-button>
        </div>
      </div>

      <!-- 右侧：文件内容预览 -->
      <div class="content-preview">
        <div class="preview-header">
          <span v-if="selectedFile">{{ selectedFile }}</span>
          <span v-else class="placeholder">选择文件查看内容</span>
        </div>
        
        <div class="preview-body" v-loading="loadingDiff">
          <div v-if="!selectedFile" class="empty-preview">
            <el-icon size="48"><View /></el-icon>
            <p>点击左侧文件查看冲突内容</p>
            <p class="hint">双击或右键可在WebStorm中打开</p>
          </div>
          <pre v-else class="conflict-content"><code v-html="formatConflictContent(fileDiff)"></code></pre>
        </div>
        
        <!-- 冲突说明 -->
        <div class="conflict-legend">
          <div class="legend-item">
            <span class="legend-color ours"></span>
            <span>&lt;&lt;&lt;&lt;&lt;&lt;&lt; HEAD (当前分支)</span>
          </div>
          <div class="legend-item">
            <span class="legend-color separator"></span>
            <span>======= (分隔线)</span>
          </div>
          <div class="legend-item">
            <span class="legend-color theirs"></span>
            <span>&gt;&gt;&gt;&gt;&gt;&gt;&gt; (传入的更改)</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右键菜单 -->
    <Teleport to="body">
      <div 
        v-if="contextMenuVisible" 
        class="context-menu"
        :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
      >
        <div class="context-menu-item" @click="openInWebStorm()">
          <el-icon><FolderOpened /></el-icon>
          <span>在WebStorm中打开</span>
        </div>
        <div class="context-menu-item" @click="markAsResolved()">
          <el-icon><Check /></el-icon>
          <span>标记为已解决</span>
        </div>
      </div>
    </Teleport>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button type="danger" @click="abortOperation" v-if="conflictFiles.length > 0">
          中止操作
        </el-button>
        <el-button type="success" @click="continueOperation" v-if="conflictFiles.length > 0">
          冲突已解决，继续
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>


<style scoped>
.conflict-container {
  display: flex;
  gap: 20px;
  height: 500px;
}

.conflict-list {
  width: 350px;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8ecf1;
  background: #fef0f0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #f56c6c;
}

.warning-icon {
  font-size: 18px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #67c23a;
  padding: 20px;
}

.empty-state p {
  margin: 12px 0 4px;
  font-weight: 600;
}

.empty-state .hint {
  font-size: 12px;
  color: #909399;
}

.files-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conflict-file-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 6px;
  background: #fff5f5;
  border: 1px solid #fde2e2;
}

.conflict-file-item:hover {
  background: #fef0f0;
  border-color: #f56c6c;
}

.conflict-file-item.active {
  background: #fef0f0;
  border-color: #f56c6c;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

.file-icon {
  color: #f56c6c;
  font-size: 18px;
  margin-right: 10px;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-weight: 500;
  color: #303133;
  word-break: break-all;
}

.file-dir {
  font-size: 11px;
  color: #909399;
  font-family: 'Consolas', monospace;
}

.open-icon {
  color: #909399;
  font-size: 16px;
  opacity: 0;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.conflict-file-item:hover .open-icon {
  opacity: 1;
  color: #409eff;
}

.batch-actions {
  padding: 12px;
  border-top: 1px solid #e8ecf1;
  background: #fafafa;
}

.content-preview {
  flex: 1;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.preview-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e8ecf1;
  background: #f8f9fa;
  font-weight: 600;
  font-size: 13px;
  font-family: 'Consolas', monospace;
}

.preview-header .placeholder {
  color: #909399;
  font-weight: normal;
}

.preview-body {
  flex: 1;
  overflow: auto;
  background: #1e1e1e;
}

.empty-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #606266;
  background: #f5f7fa;
}

.empty-preview p {
  margin: 8px 0 0;
}

.empty-preview .hint {
  font-size: 12px;
  color: #909399;
}

.conflict-content {
  margin: 0;
  padding: 16px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #d4d4d4;
  white-space: pre;
}

:deep(.conflict-marker) {
  display: block;
  padding: 2px 8px;
  margin: 4px -8px;
  font-weight: bold;
}

:deep(.conflict-ours) {
  background: rgba(78, 201, 176, 0.3);
  color: #4ec9b0;
}

:deep(.conflict-separator) {
  background: rgba(255, 193, 7, 0.3);
  color: #ffc107;
}

:deep(.conflict-theirs) {
  background: rgba(241, 76, 76, 0.3);
  color: #f14c4c;
}

.conflict-legend {
  padding: 12px 16px;
  border-top: 1px solid #e8ecf1;
  background: #fafafa;
  display: flex;
  gap: 20px;
  font-size: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.ours {
  background: #4ec9b0;
}

.legend-color.separator {
  background: #ffc107;
}

.legend-color.theirs {
  background: #f14c4c;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  min-width: 180px;
  z-index: 9999;
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #303133;
  transition: all 0.2s;
}

.context-menu-item:hover {
  background: #ecf5ff;
  color: #409eff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 滚动条 */
.files-scroll::-webkit-scrollbar,
.preview-body::-webkit-scrollbar {
  width: 6px;
}

.files-scroll::-webkit-scrollbar-track {
  background: #f5f5f5;
}

.preview-body::-webkit-scrollbar-track {
  background: #1e1e1e;
}

.files-scroll::-webkit-scrollbar-thumb {
  background: #f56c6c;
  border-radius: 3px;
}

.preview-body::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 3px;
}
</style>
