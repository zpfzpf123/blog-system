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
  { title: '暂存变更', icon: Check, description: 'git add .' },
  { title: '同步远程', icon: Download, description: '拉取最新代码' },
  { title: '本地提交', icon: Document, description: '提交到本地仓库' },
  { title: '推送代码', icon: Upload, description: '推送到远程仓库' }
]

// 步骤状态
const stepStatus = ref('process')

// 终端日志
const logs = ref<string[]>([])
const logsContainerRef = ref<HTMLElement | null>(null)

// 添加日志（支持延迟）
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

// 延迟函数
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 逐步添加日志（带动画效果）
const addLogWithDelay = async (message: string, type: 'info' | 'success' | 'warning' | 'error' | 'cmd' = 'info', delayMs: number = 300) => {
  await delay(delayMs)
  addLog(message, type)
}

// 更新步骤（带延迟）
const updateStepWithDelay = async (step: number, delayMs: number = 500) => {
  await delay(delayMs)
  activeStep.value = step
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

// 开始智能提交（新流程 - 逐步显示进度）
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
  await addLogWithDelay(`提交信息: ${commitMessage.value}`, 'info', 200)
  await addLogWithDelay(`最大重试次数: ${maxRetries.value}`, 'info', 200)

  try {
    const response = await axios.post(`/api/projects/${props.projectId}/smart-commit`, {
      commitMessage: commitMessage.value,
      localBranch: selectedLocalBranch.value,
      remoteBranch: selectedRemoteBranch.value,
      maxRetries: maxRetries.value
    })

    const data = response.data

    // 步骤1: 环境检查 (索引0 -> 完成后进入索引1)
    if (data.step1_status) {
      await addLogWithDelay('git status', 'cmd', 400)
      const status = data.step1_status
      await addLogWithDelay(`当前分支: ${status.currentBranch}`, 'info', 300)
      await addLogWithDelay(`检测到 ${status.totalChanges} 个文件变更`, 'info', 300)
      await updateStepWithDelay(1, 400)
    }

    // 如果没有变更
    if (data.noChanges) {
      await addLogWithDelay('工作区干净，没有需要提交的更改', 'warning', 300)
      stepStatus.value = 'finish'
      isProcessing.value = false
      return
    }

    // 步骤2: 暂存变更 (索引1 -> 完成后进入索引2)
    if (data.step2_stage) {
      await addLogWithDelay('git add .', 'cmd', 400)
      await addLogWithDelay(data.step2_stage.message || '文件已暂存', 'success', 300)
      await updateStepWithDelay(2, 400)
    }

    // 步骤3: 同步远程 (索引2 -> 完成后进入索引3)
    if (data.step3_pull) {
      await addLogWithDelay('git fetch', 'cmd', 400)
      await addLogWithDelay('获取远程最新状态完成', 'success', 300)
      await addLogWithDelay('git pull --rebase', 'cmd', 400)
      await addLogWithDelay(data.step3_pull.message || '同步完成', 'success', 300)
      await updateStepWithDelay(3, 400)
    }

    // 检查是否有冲突（冲突发生在同步远程阶段）
    if (data.hasConflict) {
      stepStatus.value = 'error'
      await addLogWithDelay('检测到代码冲突！', 'error', 300)
      
      conflictFiles.value = data.conflictFiles || []
      await addLogWithDelay(`发现 ${data.conflictCount || conflictFiles.value.length} 个冲突文件:`, 'warning', 300)
      for (const file of conflictFiles.value) {
        await addLogWithDelay(`  - ${file}`, 'error', 150)
      }
      
      await addLogWithDelay('请解决冲突后，点击"继续提交"按钮', 'warning', 300)
      hasConflictPending.value = true
      isProcessing.value = false
      return
    }

    // 步骤4: 本地提交 (索引3 -> 完成后进入索引4)
    if (data.step4_commit) {
      await addLogWithDelay(`git commit -m "${commitMessage.value}"`, 'cmd', 400)
      await addLogWithDelay(data.step4_commit.message || '提交成功', 'success', 300)
      if (data.step4_commit.commitHash) {
        await addLogWithDelay(`提交哈希: ${data.step4_commit.commitHash}`, 'info', 200)
      }
      await updateStepWithDelay(4, 400)
    }

    // 步骤5: 推送代码 (索引4 -> 完成)
    if (data.step5_push) {
      const pushCmd = selectedRemoteBranch.value 
        ? `git push ${selectedRemoteBranch.value.split('/')[0]} ${selectedLocalBranch.value}`
        : 'git push'
      await addLogWithDelay(pushCmd, 'cmd', 400)
      
      // 显示重试日志
      if (data.step5_push.retryLogs && data.step5_push.retryLogs.length > 0) {
        for (const log of data.step5_push.retryLogs) {
          await addLogWithDelay(log, 'warning', 200)
        }
      }
      
      // 检查推送阶段是否有冲突（自动 pull 时发现的冲突）
      if (data.step5_push.hasConflict) {
        stepStatus.value = 'error'
        activeStep.value = 4
        await addLogWithDelay('推送时自动拉取检测到代码冲突！', 'error', 300)
        
        conflictFiles.value = data.step5_push.conflictFiles || []
        await addLogWithDelay(`发现 ${data.step5_push.conflictCount || conflictFiles.value.length} 个冲突文件:`, 'warning', 300)
        for (const file of conflictFiles.value) {
          await addLogWithDelay(`  - ${file}`, 'error', 150)
        }
        
        await addLogWithDelay('请解决冲突后，点击"继续提交"按钮', 'warning', 300)
        hasConflictPending.value = true
        isProcessing.value = false
        return
      }
      
      await addLogWithDelay(data.step5_push.message || '推送成功', 'success', 300)
      if (data.step5_push.retryCount > 0) {
        await addLogWithDelay(`共重试 ${data.step5_push.retryCount} 次`, 'info', 200)
      }
    }

    // 检查最终结果
    if (data.success) {
      stepStatus.value = 'success'
      await addLogWithDelay('✅ 智能提交全流程执行完毕', 'success', 400)
      
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
        await addLogWithDelay('远程有新提交，请重新执行智能提交', 'error', 300)
      } else {
        await addLogWithDelay(data.message || '提交失败', 'error', 300)
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

// 继续提交（解决冲突后 - 逐步显示进度）
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
      await addLogWithDelay('仍有未解决的冲突！', 'error', 300)
      conflictFiles.value = data.conflictFiles || []
      for (const file of conflictFiles.value) {
        await addLogWithDelay(`  - ${file}`, 'error', 150)
      }
      await addLogWithDelay('请继续解决冲突', 'warning', 300)
      isProcessing.value = false
      return
    }

    if (data.success) {
      await addLogWithDelay('git add .', 'cmd', 400)
      await addLogWithDelay('已暂存解决后的文件', 'success', 300)
      await updateStepWithDelay(3, 400)
      
      await addLogWithDelay('git rebase --continue', 'cmd', 400)
      await addLogWithDelay('rebase继续成功', 'success', 300)
      
      if (data.commitResult) {
        await addLogWithDelay(`git commit -m "${commitMessage.value}"`, 'cmd', 400)
        await addLogWithDelay('提交成功', 'success', 300)
        await updateStepWithDelay(4, 400)
      }
      
      await addLogWithDelay('git push', 'cmd', 400)
      await addLogWithDelay('推送成功！', 'success', 300)
      
      stepStatus.value = 'success'
      await addLogWithDelay('✅ 智能提交全流程执行完毕', 'success', 400)
      
      hasConflictPending.value = false
      emit('success')
      
      ElMessage.success('提交成功！')
      
      setTimeout(() => {
        visible.value = false
      }, 3000)
    } else {
      await addLogWithDelay(data.message || '继续提交失败', 'error', 300)
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
  padding: var(--spacing-3) 0;
}

.status-header {
  margin-bottom: var(--spacing-8);
}

.branch-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-6);
  padding: var(--spacing-5);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(139, 92, 246, 0.03) 100%);
  border-radius: var(--radius-xl);
  margin-bottom: var(--spacing-5);
  border: 1.5px solid rgba(99, 102, 241, 0.15);
  transition: all var(--transition-normal);
}

.branch-section:hover {
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: var(--shadow-sm);
}

.branch-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.branch-arrow {
  font-size: 22px;
  font-weight: var(--font-bold);
  color: var(--primary-color);
  animation: arrowPulse 2s ease-in-out infinite;
}

@keyframes arrowPulse {
  0%, 100% { transform: translateX(0); opacity: 1; }
  50% { transform: translateX(3px); opacity: 0.7; }
}

.input-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-4);
}

/* 修复输入框图标和文字重叠问题 */
.input-section :deep(.el-input__wrapper) {
  padding-left: 30px;
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
}

.input-section :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.input-section :deep(.el-input__prefix) {
  position: absolute;
  left: 10px;
  color: var(--text-muted);
}

.retry-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  padding: var(--spacing-4) var(--spacing-5);
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(251, 191, 36, 0.08) 100%);
  border-radius: var(--radius-lg);
  border: 1.5px solid rgba(245, 158, 11, 0.3);
}

.label {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  white-space: nowrap;
}

.hint-text {
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

.steps-container {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(248, 250, 255, 0.6) 100%);
  padding: var(--spacing-8) var(--spacing-6);
  border-radius: var(--radius-2xl);
  margin-bottom: var(--spacing-6);
  border: 1.5px solid rgba(99, 102, 241, 0.1);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-sm);
}

.terminal-window {
  background: linear-gradient(180deg, #1a1b26 0%, #16161e 100%);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-2xl), 0 0 40px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  font-family: var(--font-mono);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.terminal-header {
  background: linear-gradient(180deg, #2d2d3a 0%, #252530 100%);
  padding: var(--spacing-3) var(--spacing-5);
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.window-dots {
  display: flex;
  gap: 8px;
  margin-right: var(--spacing-5);
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  transition: all var(--transition-fast);
  cursor: pointer;
}

.dot:hover {
  transform: scale(1.2);
}

.red { 
  background: linear-gradient(135deg, #ff5f56 0%, #ff3b30 100%);
  box-shadow: 0 0 8px rgba(255, 95, 86, 0.5);
}
.yellow { 
  background: linear-gradient(135deg, #ffbd2e 0%, #ff9500 100%);
  box-shadow: 0 0 8px rgba(255, 189, 46, 0.5);
}
.green { 
  background: linear-gradient(135deg, #27c93f 0%, #34c759 100%);
  box-shadow: 0 0 8px rgba(39, 201, 63, 0.5);
}

.terminal-title {
  color: rgba(255, 255, 255, 0.6);
  font-size: var(--text-sm);
  flex: 1;
  text-align: center;
  margin-right: 50px;
  font-weight: var(--font-medium);
}

.terminal-body {
  height: 300px;
  padding: var(--spacing-5);
  overflow-y: auto;
  color: #e4e4e7;
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, transparent 100%);
}

.terminal-body::-webkit-scrollbar {
  width: 8px;
}
.terminal-body::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-full);
}
.terminal-body::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: var(--radius-full);
}
.terminal-body::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #4f46e5 0%, #7c3aed 100%);
}

.log-line {
  margin-bottom: var(--spacing-1);
  word-break: break-all;
  padding: var(--spacing-1) 0;
  transition: all var(--transition-fast);
}

.log-line:hover {
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-sm);
  padding-left: var(--spacing-2);
}

.is-cmd {
  color: #7dd3fc;
  font-weight: var(--font-semibold);
}

.is-success {
  color: #4ade80;
}

.is-error {
  color: #f87171;
}

.is-warning {
  color: #fbbf24;
}

.empty-logs {
  color: rgba(255, 255, 255, 0.4);
  text-align: center;
  margin-top: 100px;
  font-size: var(--text-sm);
}

.typing-cursor {
  display: inline-block;
  width: 10px;
  height: 18px;
  background: linear-gradient(180deg, #6366f1 0%, #8b5cf6 100%);
  animation: cursorBlink 1s infinite;
  vertical-align: middle;
  border-radius: 2px;
}

@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
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
