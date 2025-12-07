<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'
import { 
  Check, 
  Document, 
  Upload, 
  Download, 
  Connection,
  Close
} from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  projectId?: number
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 提交信息
const commitMessage = ref('')
// 是否正在处理
const isProcessing = ref(false)
// 当前步骤索引
const activeStep = ref(0)
// 是否有冲突待解决
const hasConflictPending = ref(false)
// 冲突文件列表
const conflictFiles = ref<string[]>([])

// 分支相关
const currentBranch = ref('')
const localBranches = ref<string[]>([])
const remoteBranches = ref<string[]>([])
const selectedLocalBranch = ref('')
const selectedRemoteBranch = ref('')
const loadingBranches = ref(false)

// 重试设置
const maxRetries = ref(3)

// 步骤定义
const steps = [
  { title: '环境检查', icon: Document, description: '检查工作区状态' },
  { title: '同步远程', icon: Download, description: '拉取最新代码' },
  { title: '冲突检测', icon: Connection, description: '检测合并冲突' },
  { title: '本地提交', icon: Check, description: '提交到本地仓库' },
  { title: '推送代码', icon: Upload, description: '推送到远程仓库' }
]

// 步骤状态
const stepStatus = ref('process')

// 终端日志
const logs = ref<string[]>([])
const logsContainerRef = ref<HTMLElement | null>(null)

// 添加日志
const addLog = (message: string, type: 'info' | 'success' | 'warning' | 'error' | 'cmd' = 'info') => {
  const timestamp = new Date().toLocaleTimeString()
  let prefix = ''
  if (type === 'cmd') prefix = '$ '
  
  logs.value.push(`[${timestamp}] ${prefix}${message}`)
  
  setTimeout(() => {
    if (logsContainerRef.value) {
      logsContainerRef.value.scrollTop = logsContainerRef.value.scrollHeight
    }
  }, 10)
}

// 关闭弹窗
const handleClose = () => {
  if (isProcessing.value) return
  visible.value = false
  resetState()
}

// 重置状态
const resetState = () => {
  activeStep.value = 0
  stepStatus.value = 'process'
  logs.value = []
  // 保留提交信息，方便用户修改后重试
  // commitMessage.value = ''
  isProcessing.value = false
  hasConflictPending.value = false
  conflictFiles.value = []
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
  } finally {
    loadingBranches.value = false
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadBranches()
  }
})

// 开始智能提交（新流程）
const startSmartCommit = async () => {
  if (!commitMessage.value.trim()) {
    addLog('请输入提交信息', 'error')
    ElMessage.error('请输入提交信息')
    return
  }

  if (!props.projectId) {
    addLog('项目ID不存在', 'error')
    return
  }

  isProcessing.value = true
  activeStep.value = 0
  stepStatus.value = 'process'
  logs.value = []
  hasConflictPending.value = false
  conflictFiles.value = []
  
  addLog('开始智能提交流程...', 'info')
  addLog(`提交信息: ${commitMessage.value}`, 'info')
  addLog(`最大重试次数: ${maxRetries.value}`, 'info')

  try {
    const response = await axios.post(`/api/projects/${props.projectId}/smart-commit`, {
      commitMessage: commitMessage.value,
      localBranch: selectedLocalBranch.value,
      remoteBranch: selectedRemoteBranch.value,
      maxRetries: maxRetries.value
    })

    const data = response.data

    // 步骤1: 环境检查
    if (data.step1_status) {
      addLog('git status', 'cmd')
      const status = data.step1_status
      addLog(`当前分支: ${status.currentBranch}`, 'info')
      addLog(`检测到 ${status.totalChanges} 个文件变更`, 'info')
      activeStep.value = 1
    }

    // 如果没有变更
    if (data.noChanges) {
      addLog('工作区干净，没有需要提交的更改', 'warning')
      stepStatus.value = 'finish'
      isProcessing.value = false
      return
    }

    // 步骤2: 暂存文件
    if (data.step2_stage) {
      addLog('git add .', 'cmd')
      addLog(data.step2_stage.message, 'success')
      activeStep.value = 2
    }

    // 步骤3: 拉取远程
    if (data.step3_pull) {
      addLog('git fetch', 'cmd')
      addLog('获取远程最新状态完成', 'success')
      addLog('git pull --rebase', 'cmd')
      addLog(data.step3_pull.message, 'success')
      activeStep.value = 3
    }

    // 检查是否有冲突
    if (data.hasConflict) {
      stepStatus.value = 'error'
      addLog('检测到代码冲突！', 'error')
      
      conflictFiles.value = data.conflictFiles || []
      addLog(`发现 ${data.conflictCount} 个冲突文件:`, 'warning')
      conflictFiles.value.forEach(file => {
        addLog(`  - ${file}`, 'error')
      })
      
      addLog('请解决冲突后，点击"继续提交"按钮', 'warning')
      hasConflictPending.value = true
      isProcessing.value = false
      return
    }

    // 步骤4: 本地提交
    if (data.step4_commit) {
      addLog(`git commit -m "${commitMessage.value}"`, 'cmd')
      addLog(data.step4_commit.message, 'success')
      if (data.step4_commit.commitHash) {
        addLog(`提交哈希: ${data.step4_commit.commitHash}`, 'info')
      }
      activeStep.value = 4
    }

    // 步骤5: 推送
    if (data.step5_push) {
      const pushCmd = selectedRemoteBranch.value 
        ? `git push ${selectedRemoteBranch.value.split('/')[0]} ${selectedLocalBranch.value}`
        : 'git push'
      addLog(pushCmd, 'cmd')
      
      // 显示重试日志
      if (data.step5_push.retryLogs && data.step5_push.retryLogs.length > 0) {
        data.step5_push.retryLogs.forEach((log: string) => {
          addLog(log, 'warning')
        })
      }
      
      addLog(data.step5_push.message, 'success')
      if (data.retryCount > 0) {
        addLog(`共重试 ${data.retryCount} 次`, 'info')
      }
      activeStep.value = 5
    }

    // 检查最终结果
    if (data.success) {
      stepStatus.value = 'success'
      addLog('✅ 智能提交全流程执行完毕', 'success')
      
      ElMessage.success('提交成功！')
      emit('success')
      
      // 3秒后自动关闭
      setTimeout(() => {
        if (!hasConflictPending.value) {
          visible.value = false
        }
      }, 3000)
    } else {
      stepStatus.value = 'error'
      
      if (data.needPull) {
        addLog('远程有新提交，请重新执行智能提交', 'error')
      } else {
        addLog(data.message || '提交失败', 'error')
      }
      
      ElMessage.error(data.message || '提交失败')
    }

    isProcessing.value = false

  } catch (error: any) {
    console.error(error)
    stepStatus.value = 'error'
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    addLog(`流程执行出错: ${errorMsg}`, 'error')
    isProcessing.value = false
    
    ElMessage.error(errorMsg)
  }
}

// 继续提交（解决冲突后）
const continueCommit = async () => {
  if (!props.projectId) {
    addLog('项目ID不存在', 'error')
    return
  }

  isProcessing.value = true
  addLog('', 'info')
  addLog('继续提交流程...', 'info')

  try {
    const response = await axios.post(`/api/projects/${props.projectId}/continue-commit`, {
      commitMessage: commitMessage.value,
      localBranch: selectedLocalBranch.value,
      remoteBranch: selectedRemoteBranch.value,
      maxRetries: maxRetries.value
    })

    const data = response.data

    if (data.hasConflict) {
      addLog('仍有未解决的冲突！', 'error')
      conflictFiles.value = data.conflictFiles || []
      conflictFiles.value.forEach(file => {
        addLog(`  - ${file}`, 'error')
      })
      addLog('请继续解决冲突', 'warning')
      isProcessing.value = false
      return
    }

    if (data.success) {
      addLog('git add .', 'cmd')
      addLog('已暂存解决后的文件', 'success')
      addLog('git rebase --continue', 'cmd')
      addLog('rebase继续成功', 'success')
      
      if (data.commitResult) {
        addLog(`git commit -m "${commitMessage.value}"`, 'cmd')
        addLog('提交成功', 'success')
      }
      
      addLog('git push', 'cmd')
      addLog('推送成功！', 'success')
      
      activeStep.value = 5
      stepStatus.value = 'success'
      addLog('✅ 智能提交全流程执行完毕', 'success')
      
      hasConflictPending.value = false
      emit('success')
      
      ElMessage.success('提交成功！')
      
      setTimeout(() => {
        visible.value = false
      }, 3000)
    } else {
      addLog(data.message || '继续提交失败', 'error')
      ElMessage.error(data.message || '继续提交失败')
    }

    isProcessing.value = false

  } catch (error: any) {
    console.error(error)
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    addLog(`继续提交出错: ${errorMsg}`, 'error')
    isProcessing.value = false
    
    ElMessage.error(errorMsg)
  }
}

// 取消提交
const abortCommit = async () => {
  if (!props.projectId) return

  try {
    const response = await axios.post(`/api/projects/${props.projectId}/abort-commit`)
    
    if (response.data.success) {
      addLog('已取消提交', 'warning')
      ElMessage.success('已取消提交')
      hasConflictPending.value = false
      resetState()
    } else {
      addLog('取消失败: ' + response.data.message, 'error')
      ElMessage.error('取消失败')
    }
  } catch (error: any) {
    console.error(error)
    ElMessage.error('取消失败')
  }
}

// 双击冲突文件，在IDE中打开
const openFileInIDE = async (fileName: string) => {
  if (!props.projectId) {
    ElMessage.error('项目ID不存在')
    return
  }

  try {
    addLog(`正在打开文件: ${fileName}`, 'info')
    
    const response = await axios.post(`/api/projects/${props.projectId}/open-file`, {
      fileName: fileName
    })
    
    if (response.data.success) {
      addLog(`已在IDE中打开: ${fileName}`, 'success')
      ElMessage.success('文件已在IDE中打开')
    } else {
      addLog(`打开文件失败: ${response.data.message}`, 'error')
      ElMessage.error(response.data.message || '打开文件失败')
    }
  } catch (error: any) {
    console.error('打开文件失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '打开文件失败'
    addLog(`打开文件失败: ${errorMsg}`, 'error')
    ElMessage.error(errorMsg)
  }
}


</script>

<template>
  <el-dialog
    v-model="visible"
    title="Git智能提交 (新版)"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :before-close="handleClose"
    class="git-commit-dialog"
  >
    <div class="dialog-content">
      <!-- 顶部状态区 -->
      <div class="status-header">
        <!-- 分支选择区 -->
        <div class="branch-section">
          <div class="branch-item">
            <span class="label">本地分支:</span>
            <el-select
              v-model="selectedLocalBranch"
              placeholder="选择本地分支"
              :disabled="isProcessing || loadingBranches"
              style="width: 200px"
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
          
          <div class="branch-arrow">→</div>
          
          <div class="branch-item">
            <span class="label">推送到:</span>
            <el-select
              v-model="selectedRemoteBranch"
              placeholder="选择远程分支"
              :disabled="isProcessing || loadingBranches"
              style="width: 200px"
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
        
        <!-- 提交信息输入 -->
        <div class="input-section">
          <span class="label">提交信息:</span>
          <el-input
            v-model="commitMessage"
            placeholder="请输入本次提交的描述信息 (feat: 新增功能...)"
            :disabled="isProcessing"
            @keyup.enter="!isProcessing && startSmartCommit()"
          >
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-input>
        </div>
        
        <!-- 重试设置 -->
        <div class="retry-section">
          <span class="label">推送失败自动重试:</span>
          <el-input-number 
            v-model="maxRetries" 
            :min="0" 
            :max="10" 
            :disabled="isProcessing"
            size="small"
          />
          <span class="hint-text">次</span>
        </div>
      </div>

      <!-- 步骤条 -->
      <div class="steps-container">
        <el-steps :active="activeStep" finish-status="success" align-center>
          <el-step 
            v-for="(step, index) in steps" 
            :key="index" 
            :title="step.title"
            :description="step.description"
            :icon="step.icon"
            :status="index === activeStep ? stepStatus : (index < activeStep ? 'success' : 'wait')"
          />
        </el-steps>
      </div>

      <!-- 终端日志区 -->
      <div class="terminal-window">
        <div class="terminal-header">
          <div class="window-dots">
            <span class="dot red"></span>
            <span class="dot yellow"></span>
            <span class="dot green"></span>
          </div>
          <span class="terminal-title">操作日志</span>
        </div>
        <div ref="logsContainerRef" class="terminal-body">
          <div v-if="logs.length === 0" class="empty-logs">
            等待开始...
          </div>
          <div v-for="(log, index) in logs" :key="index" class="log-line">
            <span class="log-content" :class="{
              'is-cmd': log.includes('$ '),
              'is-error': log.includes('error') || log.includes('失败') || log.includes('冲突'),
              'is-success': log.includes('success') || log.includes('成功') || log.includes('✅'),
              'is-warning': log.includes('warning') || log.includes('重试')
            }">
              {{ log }}
            </span>
          </div>
          <div v-if="isProcessing" class="typing-cursor">_</div>
        </div>
      </div>

      <!-- 冲突文件列表区 -->
      <div v-if="hasConflictPending && conflictFiles.length > 0" class="conflict-files-section">
        <div class="conflict-header">
          <el-icon class="warning-icon"><Connection /></el-icon>
          <span class="conflict-title">冲突文件列表 (双击打开)</span>
          <el-tag type="danger" size="small">{{ conflictFiles.length }} 个文件</el-tag>
        </div>
        <div class="conflict-files-list">
          <div 
            v-for="(file, index) in conflictFiles" 
            :key="index"
            class="conflict-file-item"
            @dblclick="openFileInIDE(file)"
          >
            <el-icon class="file-icon"><Document /></el-icon>
            <span class="file-name">{{ file }}</span>
            <el-icon class="open-hint"><Upload /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" :disabled="isProcessing">关闭</el-button>
        
        <!-- 取消提交按钮（冲突时显示） -->
        <el-button 
          v-if="hasConflictPending"
          @click="abortCommit" 
          :disabled="isProcessing"
          :icon="Close"
        >
          取消提交
        </el-button>
        
        <!-- 正常提交按钮 -->
        <el-button 
          v-if="!hasConflictPending"
          type="primary" 
          @click="startSmartCommit" 
          :loading="isProcessing"
          :disabled="!commitMessage.trim()"
        >
          {{ isProcessing ? '正在执行...' : '开始智能提交' }}
        </el-button>
        
        <!-- 继续提交按钮（冲突时显示） -->
        <el-button 
          v-if="hasConflictPending"
          type="success" 
          @click="continueCommit" 
          :loading="isProcessing"
        >
          {{ isProcessing ? '正在推送...' : '继续提交' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>


<style scoped>
.dialog-content {
  padding: 10px 0;
}

.status-header {
  margin-bottom: 30px;
}

.branch-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: var(--el-fill-color-light, #f8f9fa);
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid var(--el-border-color, #e8ecf1);
}

.branch-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.branch-arrow {
  font-size: 20px;
  font-weight: bold;
  color: var(--el-color-primary, #409eff);
}

.input-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

/* 修复输入框图标和文字重叠问题 */
.input-section :deep(.el-input__wrapper) {
  padding-left: 30px;
}

.input-section :deep(.el-input__prefix) {
  position: absolute;
  left: 10px;
  color: var(--el-text-color-placeholder, #a8abb2);
}

.retry-section {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--el-color-warning-light-9, #fff7e6);
  border-radius: 6px;
  border: 1px solid var(--el-color-warning-light-5, #ffd666);
}

.label {
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
  white-space: nowrap;
}

.hint-text {
  color: var(--el-text-color-secondary, #909399);
  font-size: 14px;
}

.steps-container {
  background: var(--el-fill-color-light, #f8f9fa);
  padding: 30px 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  border: 1px solid var(--el-border-color, #e8ecf1);
}

.terminal-window {
  background: #1e1e1e;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  font-family: 'Consolas', 'Monaco', monospace;
}

.terminal-header {
  background: #2d2d2d;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #333;
}

.window-dots {
  display: flex;
  gap: 6px;
  margin-right: 15px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.red { background: #ff5f56; }
.yellow { background: #ffbd2e; }
.green { background: #27c93f; }

.terminal-title {
  color: #999;
  font-size: 13px;
  flex: 1;
  text-align: center;
  margin-right: 40px;
}

.terminal-body {
  height: 300px;
  padding: 15px;
  overflow-y: auto;
  color: #d4d4d4;
  font-size: 14px;
  line-height: 1.6;
}

.terminal-body::-webkit-scrollbar {
  width: 8px;
}
.terminal-body::-webkit-scrollbar-track {
  background: #1e1e1e;
}
.terminal-body::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 4px;
}
.terminal-body::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.log-line {
  margin-bottom: 4px;
  word-break: break-all;
}

.is-cmd {
  color: #4fc1ff;
  font-weight: bold;
}

.is-success {
  color: #4caf50;
}

.is-error {
  color: #f44336;
}

.is-warning {
  color: #ff9800;
}

.empty-logs {
  color: #666;
  text-align: center;
  margin-top: 100px;
}

.typing-cursor {
  display: inline-block;
  width: 8px;
  height: 15px;
  background: #d4d4d4;
  animation: blink 1s infinite;
  vertical-align: middle;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.conflict-files-section {
  margin-top: 20px;
  border: 2px solid #f56c6c;
  border-radius: 8px;
  background: #fef0f0;
  overflow: hidden;
}

.conflict-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f56c6c;
  color: white;
  font-weight: 600;
}

.warning-icon {
  font-size: 20px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.conflict-title {
  flex: 1;
  font-size: 15px;
}

.conflict-files-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
}

.conflict-file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 6px;
  background: white;
  border: 1px solid #f5c6cb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.conflict-file-item:hover {
  background: #fff5f5;
  border-color: #f56c6c;
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

.conflict-file-item:last-child {
  margin-bottom: 0;
}

.file-icon {
  color: var(--el-color-danger, #f56c6c);
  font-size: 18px;
  flex-shrink: 0;
}

.file-name {
  flex: 1;
  color: var(--el-text-color-primary, #303133);
  font-size: 14px;
  font-family: 'Consolas', 'Monaco', monospace;
  word-break: break-all;
}

.open-hint {
  color: var(--el-text-color-secondary, #909399);
  font-size: 16px;
  opacity: 0;
  transition: opacity 0.3s;
  flex-shrink: 0;
}

.conflict-file-item:hover .open-hint {
  opacity: 1;
  color: var(--el-color-primary, #409eff);
}

.conflict-files-list::-webkit-scrollbar {
  width: 6px;
}
.conflict-files-list::-webkit-scrollbar-track {
  background: var(--el-color-danger-light-9, #fef0f0);
}
.conflict-files-list::-webkit-scrollbar-thumb {
  background: var(--el-color-danger, #f56c6c);
  border-radius: 3px;
}
.conflict-files-list::-webkit-scrollbar-thumb:hover {
  background: var(--el-color-danger-dark-2, #f45656);
}

/* 暗黑模式适配 */
:global(.dark) .branch-section,
:global(.dark) .steps-container {
  background: var(--el-bg-color-overlay, #1d1e1f);
  border-color: var(--el-border-color-darker, #414243);
}

:global(.dark) .retry-section {
  background: rgba(230, 162, 60, 0.1);
  border-color: rgba(230, 162, 60, 0.3);
}

:global(.dark) .conflict-files-section {
  background: rgba(245, 108, 108, 0.1);
  border-color: var(--el-color-danger, #f56c6c);
}

:global(.dark) .conflict-file-item {
  background: var(--el-bg-color-overlay, #1d1e1f);
  border-color: rgba(245, 108, 108, 0.3);
}

:global(.dark) .conflict-file-item:hover {
  background: rgba(245, 108, 108, 0.15);
}
</style>
