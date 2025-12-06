<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import { 
  Document, 
  Plus, 
  Minus, 
  Delete, 
  Refresh,
  View,
  Close
} from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  projectId?: number
}>()

const emit = defineEmits(['update:modelValue', 'refresh', 'commit'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 文件状态
const loading = ref(false)
const modifiedFiles = ref<string[]>([])
const untrackedFiles = ref<string[]>([])
const deletedFiles = ref<string[]>([])
const stagedFiles = ref<any[]>([])

// Diff 查看
const showDiffDialog = ref(false)
const diffContent = ref('')
const diffFileName = ref('')
const loadingDiff = ref(false)

// 加载文件状态
const loadFiles = async () => {
  if (!props.projectId) return
  
  try {
    loading.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/git/detailed-status`)
    
    if (response.data.success) {
      modifiedFiles.value = response.data.modifiedFiles || []
      untrackedFiles.value = response.data.untrackedFiles || []
      deletedFiles.value = response.data.deletedFiles || []
      stagedFiles.value = response.data.stagedFiles || []
    }
  } catch (error) {
    console.error('加载文件状态失败:', error)
    ElMessage.error('加载文件状态失败')
  } finally {
    loading.value = false
  }
}

// 暂存文件
const stageFile = async (filePath: string) => {
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/stage`, {
      filePath
    })
    
    if (response.data.success) {
      ElMessage.success('已暂存')
      loadFiles()
    } else {
      ElMessage.error(response.data.message || '暂存失败')
    }
  } catch (error) {
    ElMessage.error('暂存失败')
  }
}

// 暂存所有文件
const stageAll = async () => {
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/add`)
    
    if (response.data.success) {
      ElMessage.success('已暂存所有文件')
      loadFiles()
    } else {
      ElMessage.error(response.data.message || '暂存失败')
    }
  } catch (error) {
    ElMessage.error('暂存失败')
  }
}

// 取消暂存
const unstageFile = async (filePath: string) => {
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/unstage`, {
      filePath
    })
    
    if (response.data.success) {
      ElMessage.success('已取消暂存')
      loadFiles()
    } else {
      ElMessage.error(response.data.message || '取消暂存失败')
    }
  } catch (error) {
    ElMessage.error('取消暂存失败')
  }
}

// 取消所有暂存
const unstageAll = async () => {
  for (const file of stagedFiles.value) {
    await axios.post(`/api/projects/${props.projectId}/git/unstage`, {
      filePath: file.path
    })
  }
  ElMessage.success('已取消所有暂存')
  loadFiles()
}

// 放弃修改
const discardChanges = async (filePath: string) => {
  try {
    await ElMessageBox.confirm(
      `确定要放弃对 "${filePath}" 的修改吗？\n\n此操作不可恢复！`,
      '确认放弃修改',
      {
        confirmButtonText: '放弃修改',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/discard`, {
      filePath
    })
    
    if (response.data.success) {
      ElMessage.success('已放弃修改')
      loadFiles()
      emit('refresh')
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 查看 Diff
const viewDiff = async (filePath: string, staged: boolean = false) => {
  try {
    loadingDiff.value = true
    diffFileName.value = filePath
    showDiffDialog.value = true
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/diff`, {
      filePath,
      staged
    })
    
    if (response.data.success) {
      diffContent.value = response.data.diff || '无差异'
    } else {
      diffContent.value = '获取差异失败'
    }
  } catch (error) {
    diffContent.value = '获取差异失败'
  } finally {
    loadingDiff.value = false
  }
}

// 获取状态图标
const getStatusIcon = (status: string) => {
  switch (status) {
    case 'M': return { icon: Document, color: '#1890ff', text: '修改' }
    case 'A': return { icon: Plus, color: '#52c41a', text: '新增' }
    case 'D': return { icon: Delete, color: '#ff4d4f', text: '删除' }
    default: return { icon: Document, color: '#909399', text: '未知' }
  }
}

// 所有未暂存文件
const allUnstagedFiles = computed(() => {
  const files: any[] = []
  modifiedFiles.value.forEach(f => files.push({ path: f, status: 'M' }))
  untrackedFiles.value.forEach(f => files.push({ path: f, status: 'A' }))
  deletedFiles.value.forEach(f => files.push({ path: f, status: 'D' }))
  return files
})

// 开始提交
const startCommit = () => {
  visible.value = false
  emit('commit')
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadFiles()
  }
})
</script>

<template>
  <el-dialog
    v-model="visible"
    title="文件变更管理"
    width="800px"
    :close-on-click-modal="false"
    class="git-files-dialog"
  >
    <div class="files-container" v-loading="loading">
      <!-- 已暂存文件 -->
      <div class="file-section">
        <div class="section-header staged">
          <span class="section-title">
            <el-icon><Plus /></el-icon>
            已暂存 ({{ stagedFiles.length }})
          </span>
          <div class="section-actions">
            <el-button 
              v-if="stagedFiles.length > 0"
              size="small" 
              text 
              @click="unstageAll"
            >
              取消全部
            </el-button>
          </div>
        </div>
        <div class="file-list">
          <div v-if="stagedFiles.length === 0" class="empty-list">
            暂无已暂存的文件
          </div>
          <div 
            v-for="file in stagedFiles" 
            :key="'staged-' + file.path"
            class="file-item"
          >
            <div class="file-status" :style="{ color: getStatusIcon(file.status).color }">
              {{ file.status }}
            </div>
            <div class="file-path">{{ file.path }}</div>
            <div class="file-actions">
              <el-tooltip content="查看差异" placement="top">
                <el-button size="small" text :icon="View" @click="viewDiff(file.path, true)" />
              </el-tooltip>
              <el-tooltip content="取消暂存" placement="top">
                <el-button size="small" text type="warning" :icon="Minus" @click="unstageFile(file.path)" />
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>

      <!-- 未暂存文件 -->
      <div class="file-section">
        <div class="section-header unstaged">
          <span class="section-title">
            <el-icon><Document /></el-icon>
            未暂存 ({{ allUnstagedFiles.length }})
          </span>
          <div class="section-actions">
            <el-button 
              v-if="allUnstagedFiles.length > 0"
              size="small" 
              type="primary"
              @click="stageAll"
            >
              暂存全部
            </el-button>
          </div>
        </div>
        <div class="file-list">
          <div v-if="allUnstagedFiles.length === 0" class="empty-list">
            暂无未暂存的文件
          </div>
          <div 
            v-for="file in allUnstagedFiles" 
            :key="'unstaged-' + file.path"
            class="file-item"
          >
            <div class="file-status" :style="{ color: getStatusIcon(file.status).color }">
              {{ file.status }}
            </div>
            <div class="file-path">{{ file.path }}</div>
            <div class="file-actions">
              <el-tooltip content="查看差异" placement="top">
                <el-button 
                  v-if="file.status !== 'A'"
                  size="small" 
                  text 
                  :icon="View" 
                  @click="viewDiff(file.path, false)" 
                />
              </el-tooltip>
              <el-tooltip content="暂存" placement="top">
                <el-button size="small" text type="success" :icon="Plus" @click="stageFile(file.path)" />
              </el-tooltip>
              <el-tooltip content="放弃修改" placement="top">
                <el-button 
                  v-if="file.status === 'M'"
                  size="small" 
                  text 
                  type="danger" 
                  :icon="Close" 
                  @click="discardChanges(file.path)" 
                />
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button :icon="Refresh" @click="loadFiles">刷新</el-button>
        <el-button @click="visible = false">关闭</el-button>
        <el-button 
          type="primary" 
          @click="startCommit"
          :disabled="stagedFiles.length === 0 && allUnstagedFiles.length === 0"
        >
          去提交
        </el-button>
      </div>
    </template>

    <!-- Diff 查看对话框 -->
    <el-dialog
      v-model="showDiffDialog"
      :title="'文件差异: ' + diffFileName"
      width="800px"
      append-to-body
    >
      <div class="diff-container" v-loading="loadingDiff">
        <pre class="diff-content">{{ diffContent }}</pre>
      </div>
      <template #footer>
        <el-button @click="showDiffDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
.files-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 400px;
}

.file-section {
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-weight: 600;
}

.section-header.staged {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.section-header.unstaged {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: white;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-actions .el-button {
  color: white;
}

.file-list {
  max-height: 200px;
  overflow-y: auto;
}

.empty-list {
  padding: 30px;
  text-align: center;
  color: #909399;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.2s;
}

.file-item:last-child {
  border-bottom: none;
}

.file-item:hover {
  background: #f5f7fa;
}

.file-status {
  width: 24px;
  font-weight: bold;
  font-family: 'Consolas', monospace;
  text-align: center;
}

.file-path {
  flex: 1;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.file-item:hover .file-actions {
  opacity: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.diff-container {
  max-height: 500px;
  overflow: auto;
  background: #1e1e1e;
  border-radius: 8px;
}

.diff-content {
  margin: 0;
  padding: 16px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #d4d4d4;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
