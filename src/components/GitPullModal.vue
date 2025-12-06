<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'
import { Download, Refresh } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  projectId?: number
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 分支相关
const currentBranch = ref('')
const localBranches = ref<string[]>([])
const remoteBranches = ref<string[]>([])
const selectedLocalBranch = ref('')
const selectedRemoteBranch = ref('')
const loadingBranches = ref(false)

// 拉取状态
const isPulling = ref(false)
const pullLogs = ref<string[]>([])
const logsContainerRef = ref<HTMLElement | null>(null)

// 冲突相关
const hasConflict = ref(false)
const conflictFiles = ref<string[]>([])

// 添加日志
const addLog = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') => {
  const timestamp = new Date().toLocaleTimeString()
  const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : type === 'warning' ? '⚠️' : 'ℹ️'
  pullLogs.value.push(`[${timestamp}] ${icon} ${message}`)
  
  setTimeout(() => {
    if (logsContainerRef.value) {
      logsContainerRef.value.scrollTop = logsContainerRef.value.scrollHeight
    }
  }, 10)
}

// 加载分支列表
const loadBranches = async () => {
  if (!props.projectId) return
  
  try {
    loadingBranches.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/git/branches`)
    
    if (response.data.success) {
      currentBranch.value = response.data.currentBranch || ''
      localBranches.value = response.data.localBranches || []
      remoteBranches.value = response.data.remoteBranches || []
      
      if (currentBranch.value && !selectedLocalBranch.value) {
        selectedLocalBranch.value = currentBranch.value
      }
      
      if (currentBranch.value && !selectedRemoteBranch.value) {
        const matchingRemote = remoteBranches.value.find(b => b.includes(currentBranch.value))
        if (matchingRemote) {
          selectedRemoteBranch.value = matchingRemote
        } else if (remoteBranches.value.length > 0) {
          selectedRemoteBranch.value = remoteBranches.value[0]
        }
      }
    }
  } catch (error) {
    console.error('加载分支列表失败:', error)
    ElMessage.error('加载分支列表失败')
  } finally {
    loadingBranches.value = false
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadBranches()
    pullLogs.value = []
  }
})

// 执行拉取
const executePull = async () => {
  if (!props.projectId) {
    ElMessage.error('项目ID不存在')
    return
  }

  if (!selectedRemoteBranch.value) {
    ElMessage.error('请选择远程分支')
    return
  }

  isPulling.value = true
  pullLogs.value = []
  
  addLog('开始拉取远程代码...', 'info')
  addLog(`本地分支: ${selectedLocalBranch.value}`, 'info')
  addLog(`远程分支: ${selectedRemoteBranch.value}`, 'info')

  try {
    // 1. 先切换到目标分支（如果需要）
    if (selectedLocalBranch.value !== currentBranch.value) {
      addLog(`切换到分支: ${selectedLocalBranch.value}`, 'info')
      const switchResponse = await axios.post(`/api/projects/${props.projectId}/switch-branch`, {
        branchName: selectedLocalBranch.value
      })
      
      if (!switchResponse.data.success) {
        addLog(switchResponse.data.message, 'error')
        ElMessage.error(switchResponse.data.message)
        isPulling.value = false
        return
      }
      addLog('分支切换成功', 'success')
    }

    // 2. 执行 fetch
    addLog('执行: git fetch', 'info')
    const fetchResponse = await axios.post(`/api/projects/${props.projectId}/git/fetch`)
    
    if (!fetchResponse.data.success) {
      addLog('获取远程更新失败', 'error')
      addLog(fetchResponse.data.message, 'error')
      ElMessage.error('获取远程更新失败')
      isPulling.value = false
      return
    }
    addLog('获取远程更新成功', 'success')

    // 3. 执行 pull --rebase
    addLog('执行: git pull --rebase', 'info')
    const pullResponse = await axios.post(`/api/projects/${props.projectId}/git/pull-rebase`)
    
    if (pullResponse.data.hasConflict) {
      addLog('检测到代码冲突！', 'error')
      conflictFiles.value = pullResponse.data.conflictFiles || []
      hasConflict.value = true
      addLog(`发现 ${conflictFiles.value.length} 个冲突文件:`, 'warning')
      conflictFiles.value.forEach((file: string) => {
        addLog(`  - ${file}`, 'error')
      })
      addLog('请在IDE中手动解决冲突，或双击下方文件打开', 'warning')
      ElMessage.warning('检测到代码冲突，请手动解决')
      isPulling.value = false
      return
    }

    if (!pullResponse.data.success) {
      addLog('拉取失败', 'error')
      addLog(pullResponse.data.message, 'error')
      ElMessage.error('拉取失败')
      isPulling.value = false
      return
    }

    if (pullResponse.data.upToDate) {
      addLog('当前分支已是最新', 'success')
      ElMessage.success('当前分支已是最新')
    } else {
      addLog('拉取成功！', 'success')
      ElMessage.success('拉取成功！')
    }

    emit('success')
    
    // 3秒后自动关闭
    setTimeout(() => {
      visible.value = false
    }, 3000)

  } catch (error: any) {
    console.error(error)
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    addLog(`拉取出错: ${errorMsg}`, 'error')
    ElMessage.error(errorMsg)
  } finally {
    isPulling.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  if (isPulling.value) return
  visible.value = false
  hasConflict.value = false
  conflictFiles.value = []
}

// 在WebStorm中打开冲突文件
const openConflictFile = async (filePath: string) => {
  if (!props.projectId) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/open-file`, {
      fileName: filePath
    })
    
    if (response.data.success) {
      ElMessage.success('已在WebStorm中打开')
    } else {
      ElMessage.error(response.data.message || '打开失败')
    }
  } catch (error) {
    ElMessage.error('打开文件失败')
  }
}

// 中止rebase
const abortRebase = async () => {
  if (!props.projectId) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/abort-operation`)
    
    if (response.data.success) {
      ElMessage.success('已中止操作')
      hasConflict.value = false
      conflictFiles.value = []
      addLog('已中止rebase操作', 'warning')
    } else {
      ElMessage.error(response.data.message || '中止失败')
    }
  } catch (error) {
    ElMessage.error('中止操作失败')
  }
}

// 继续rebase（解决冲突后）
const continueRebase = async () => {
  if (!props.projectId) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/continue-operation`)
    
    if (response.data.success) {
      ElMessage.success('操作完成')
      hasConflict.value = false
      conflictFiles.value = []
      addLog('冲突已解决，rebase完成', 'success')
      emit('success')
    } else {
      if (response.data.hasConflict) {
        conflictFiles.value = response.data.conflictFiles || []
        ElMessage.warning('仍有未解决的冲突')
        addLog('仍有未解决的冲突', 'error')
      } else {
        ElMessage.error(response.data.message || '操作失败')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="Git 拉取远程代码"
    width="700px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :before-close="handleClose"
    class="git-pull-dialog"
  >
    <div class="dialog-content">
      <!-- 分支选择区 -->
      <div class="branch-section">
        <div class="branch-item">
          <span class="label">本地分支:</span>
          <el-select
            v-model="selectedLocalBranch"
            placeholder="选择本地分支"
            :disabled="isPulling || loadingBranches"
            style="width: 220px"
          >
            <el-option
              v-for="branch in localBranches"
              :key="branch"
              :label="branch"
              :value="branch"
            >
              <span>{{ branch }}</span>
              <el-tag v-if="branch === currentBranch" size="small" type="success" style="margin-left: 8px">当前</el-tag>
            </el-option>
          </el-select>
        </div>
        
        <div class="branch-arrow">←</div>
        
        <div class="branch-item">
          <span class="label">拉取自:</span>
          <el-select
            v-model="selectedRemoteBranch"
            placeholder="选择远程分支"
            :disabled="isPulling || loadingBranches"
            style="width: 220px"
          >
            <el-option
              v-for="branch in remoteBranches"
              :key="branch"
              :label="branch"
              :value="branch"
            />
          </el-select>
        </div>
      </div>

      <!-- 提示信息 -->
      <el-alert
        title="提示"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <div style="font-size: 13px; line-height: 1.6;">
            <div>• 拉取操作会执行 <code>git fetch</code> 和 <code>git pull --rebase</code></div>
            <div>• 如果检测到冲突，需要手动在IDE中解决</div>
            <div>• 拉取不会推送代码到远程仓库</div>
          </div>
        </template>
      </el-alert>

      <!-- 日志区域 -->
      <div class="logs-section">
        <div class="logs-header">
          <span>操作日志</span>
        </div>
        <div ref="logsContainerRef" class="logs-body">
          <div v-if="pullLogs.length === 0" class="empty-logs">
            等待开始...
          </div>
          <div v-for="(log, index) in pullLogs" :key="index" class="log-line">
            {{ log }}
          </div>
          <div v-if="isPulling" class="typing-cursor">_</div>
        </div>
      </div>
      
      <!-- 冲突文件列表 -->
      <div v-if="hasConflict && conflictFiles.length > 0" class="conflict-section">
        <div class="conflict-header">
          <span class="conflict-title">⚠️ 冲突文件 ({{ conflictFiles.length }})</span>
          <span class="conflict-hint">双击在WebStorm中打开</span>
        </div>
        <div class="conflict-files">
          <div 
            v-for="(file, index) in conflictFiles" 
            :key="index"
            class="conflict-file-item"
            @dblclick="openConflictFile(file)"
          >
            <span class="file-path">{{ file }}</span>
            <el-button size="small" type="primary" text @click="openConflictFile(file)">
              打开
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" :disabled="isPulling">关闭</el-button>
        
        <!-- 冲突时显示的按钮 -->
        <template v-if="hasConflict">
          <el-button type="danger" @click="abortRebase">中止操作</el-button>
          <el-button type="success" @click="continueRebase">冲突已解决，继续</el-button>
        </template>
        
        <!-- 正常拉取按钮 -->
        <el-button 
          v-else
          type="primary" 
          @click="executePull" 
          :loading="isPulling"
          :icon="Download"
        >
          {{ isPulling ? '正在拉取...' : '开始拉取' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-content {
  padding: 10px 0;
}

.branch-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.branch-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.label {
  font-weight: 600;
  color: white;
  white-space: nowrap;
  font-size: 14px;
}

.branch-arrow {
  font-size: 24px;
  font-weight: bold;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.logs-section {
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8ecf1;
}

.logs-header {
  background: #e8ecf1;
  padding: 10px 15px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  border-bottom: 1px solid #dcdfe6;
}

.logs-body {
  height: 250px;
  padding: 15px;
  overflow-y: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.8;
  color: #303133;
  background: white;
}

.logs-body::-webkit-scrollbar {
  width: 8px;
}
.logs-body::-webkit-scrollbar-track {
  background: #f8f9fa;
}
.logs-body::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
}
.logs-body::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.empty-logs {
  color: #909399;
  text-align: center;
  margin-top: 80px;
  font-size: 14px;
}

.log-line {
  margin-bottom: 6px;
  word-break: break-all;
}

.typing-cursor {
  display: inline-block;
  width: 8px;
  height: 14px;
  background: #409eff;
  animation: blink 1s infinite;
  vertical-align: middle;
  margin-left: 4px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

:deep(.el-alert__content) {
  padding: 0;
}

:deep(code) {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  color: #e83e8c;
}

/* 冲突文件列表样式 */
.conflict-section {
  margin-top: 20px;
  border: 2px solid #f56c6c;
  border-radius: 8px;
  overflow: hidden;
}

.conflict-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fef0f0;
  border-bottom: 1px solid #fde2e2;
}

.conflict-title {
  font-weight: 600;
  color: #f56c6c;
}

.conflict-hint {
  font-size: 12px;
  color: #909399;
}

.conflict-files {
  max-height: 200px;
  overflow-y: auto;
}

.conflict-file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #fde2e2;
  cursor: pointer;
  transition: background 0.2s;
}

.conflict-file-item:last-child {
  border-bottom: none;
}

.conflict-file-item:hover {
  background: #fef0f0;
}

.conflict-file-item .file-path {
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: #303133;
}
</style>
