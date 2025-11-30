<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'
import { 
  Check, 
  Loading, 
  Close, 
  VideoPlay, 
  Document, 
  Upload, 
  Download, 
  Connection 
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

// 分支相关
const currentBranch = ref('')
const localBranches = ref<string[]>([])
const remoteBranches = ref<string[]>([])
const selectedLocalBranch = ref('')
const selectedRemoteBranch = ref('')
const loadingBranches = ref(false)

// 步骤定义
const steps = [
  { title: '环境检查', icon: Document, description: '检查工作区状态' },
  { title: '本地提交', icon: Check, description: '暂存并提交更改' },
  { title: '智能同步', icon: Download, description: '获取并变基拉取' },
  { title: '冲突检测', icon: Connection, description: '检测合并冲突' },
  { title: '推送代码', icon: Upload, description: '推送到远程仓库' }
]

// 步骤状态：wait, process, finish, error
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
  
  // 自动滚动到底部
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
  commitMessage.value = ''
  isProcessing.value = false
  hasConflictPending.value = false
  // 不重置分支选择，保留用户的选择
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
      
      // 默认选择当前分支
      if (currentBranch.value && !selectedLocalBranch.value) {
        selectedLocalBranch.value = currentBranch.value
      }
      
      // 默认选择对应的远程分支
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

// 监听弹窗打开，加载分支列表
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadBranches()
  }
})

// 模拟执行命令的延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 开始智能提交
const startSmartCommit = async () => {
  if (!commitMessage.value.trim()) {
    addLog('请输入提交信息', 'error')
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
  
  addLog('开始智能提交流程...', 'info')

  try {
    // 1. 环境检查
    addLog('git status', 'cmd')
    addLog('检查工作区状态...', 'info')
    
    const statusResponse = await axios.post(`/api/projects/${props.projectId}/git/check-status`)
    
    if (!statusResponse.data.success) {
      stepStatus.value = 'error'
      addLog(statusResponse.data.message, 'error')
      isProcessing.value = false
      return
    }
    
    const statusData = statusResponse.data
    addLog(`当前分支: ${statusData.currentBranch}`, 'info')
    
    if (!statusData.hasChanges) {
      addLog('工作区干净，没有待提交的更改', 'warning')
      addLog('流程终止', 'info')
      stepStatus.value = 'finish'
      isProcessing.value = false
      return
    }
    
    addLog(`检测到 ${statusData.modifiedCount} 个已修改文件`, 'info')
    if (statusData.untrackedCount > 0) {
      addLog(`检测到 ${statusData.untrackedCount} 个未跟踪文件`, 'info')
    }
    
    // 显示部分文件列表
    const allFiles = [...statusData.modifiedFiles, ...statusData.untrackedFiles]
    const displayFiles = allFiles.slice(0, 5)
    displayFiles.forEach(file => {
      addLog(`  - ${file}`, 'info')
    })
    if (allFiles.length > 5) {
      addLog(`  ... 还有 ${allFiles.length - 5} 个文件`, 'info')
    }
    
    activeStep.value = 1

    // 2. 本地提交 - git add
    addLog('git add .', 'cmd')
    addLog('正在暂存所有更改...', 'info')
    
    const addResponse = await axios.post(`/api/projects/${props.projectId}/git/add`)
    
    if (!addResponse.data.success) {
      stepStatus.value = 'error'
      addLog(addResponse.data.message, 'error')
      isProcessing.value = false
      return
    }
    
    addLog('已暂存所有更改', 'success')
    
    // 2. 本地提交 - git commit
    addLog(`git commit -m "${commitMessage.value}"`, 'cmd')
    addLog('正在提交到本地仓库...', 'info')
    
    const commitResponse = await axios.post(`/api/projects/${props.projectId}/git/commit`, {
      message: commitMessage.value
    })
    
    if (!commitResponse.data.success) {
      stepStatus.value = 'error'
      addLog(commitResponse.data.message, 'error')
      isProcessing.value = false
      return
    }
    
    if (commitResponse.data.noChanges) {
      addLog('工作区干净，没有需要提交的更改', 'warning')
    } else {
      addLog('本地提交成功', 'success')
      if (commitResponse.data.commitInfo) {
        addLog(`提交信息: ${commitResponse.data.commitInfo}`, 'info')
      }
    }
    
    activeStep.value = 2

    // 3. 智能同步 - git fetch
    addLog('git fetch', 'cmd')
    addLog('正在获取远程最新状态...', 'info')
    
    const fetchResponse = await axios.post(`/api/projects/${props.projectId}/git/fetch`)
    
    if (!fetchResponse.data.success) {
      stepStatus.value = 'error'
      addLog(fetchResponse.data.message, 'error')
      isProcessing.value = false
      return
    }
    
    addLog('获取远程最新状态完成', 'success')
    
    // 3. 智能同步 - git pull --rebase
    addLog('git pull --rebase', 'cmd')
    addLog('正在变基拉取远程代码...', 'info')
    
    const pullResponse = await axios.post(`/api/projects/${props.projectId}/git/pull-rebase`)
    
    // 检查是否有冲突
    if (pullResponse.data.hasConflict) {
      stepStatus.value = 'error'
      addLog('检测到代码冲突！', 'error')
      addLog('请在IDE中编辑解决冲突文件', 'warning')
      addLog('解决完成后，点击下方"继续推送"按钮', 'warning')
      hasConflictPending.value = true
      isProcessing.value = false
      return
    }
    
    if (!pullResponse.data.success) {
      stepStatus.value = 'error'
      addLog(pullResponse.data.message, 'error')
      isProcessing.value = false
      return
    }
    
    // 显示同步结果
    if (pullResponse.data.upToDate) {
      addLog('当前分支已是最新', 'success')
    } else {
      addLog('变基拉取成功', 'success')
    }
    
    activeStep.value = 3
    
    // 4. 冲突检测已在pull-rebase中完成
    addLog('代码合并状态良好，无冲突', 'success')
    activeStep.value = 4

    // 5. 推送代码
    const pushCmd = selectedRemoteBranch.value 
      ? `git push ${selectedRemoteBranch.value.split('/')[0]} ${selectedLocalBranch.value}:${selectedRemoteBranch.value.split('/').slice(1).join('/')}`
      : 'git push'
    addLog(pushCmd, 'cmd')
    addLog('正在推送到远程服务器...', 'info')
    
    const pushResponse = await axios.post(`/api/projects/${props.projectId}/git/push`, {
      localBranch: selectedLocalBranch.value,
      remoteBranch: selectedRemoteBranch.value
    })
    
    if (!pushResponse.data.success) {
      stepStatus.value = 'error'
      addLog(pushResponse.data.message, 'error')
      
      if (pushResponse.data.needPull) {
        addLog('远程有新提交，请重新执行智能提交', 'warning')
      }
      
      isProcessing.value = false
      return
    }
    
    // 推送成功
    if (pushResponse.data.upToDate) {
      addLog('已是最新，无需推送', 'success')
    } else {
      addLog('推送成功！', 'success')
    }
    
    activeStep.value = 5
    stepStatus.value = 'success'
    addLog('✅ 智能提交全流程执行完毕', 'success')
    
    emit('success')
    isProcessing.value = false
    
  } catch (error: any) {
    console.error(error)
    stepStatus.value = 'error'
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    addLog(`流程执行出错: ${errorMsg}`, 'error')
    isProcessing.value = false
    
    ElMessage.error({
      message: errorMsg,
      duration: 3000
    })
  }
}

// 继续推送（解决冲突后）
const continuePush = async () => {
  if (!props.projectId) {
    addLog('项目ID不存在', 'error')
    return
  }

  isProcessing.value = true
  addLog('', 'info')
  addLog('继续推送流程...', 'info')
  addLog('$ git add .', 'cmd')
  addLog('正在暂存解决后的文件...', 'info')

  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/continue-push`)
    
    if (!response.data.success) {
      if (response.data.hasConflict) {
        addLog('仍有未解决的冲突！', 'error')
        addLog('请继续在IDE中解决冲突', 'warning')
      } else {
        addLog(response.data.message, 'error')
      }
      isProcessing.value = false
      return
    }
    
    // 成功
    addLog('已暂存所有文件', 'success')
    addLog('$ git rebase --continue', 'cmd')
    addLog('rebase继续成功', 'success')
    addLog('$ git push', 'cmd')
    addLog('正在推送到远程服务器...', 'info')
    addLog('推送成功！', 'success')
    
    activeStep.value = 5
    stepStatus.value = 'success'
    addLog('✅ 智能提交全流程执行完毕', 'success')
    
    hasConflictPending.value = false
    emit('success')
    isProcessing.value = false
    
  } catch (error: any) {
    console.error(error)
    stepStatus.value = 'error'
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    addLog(`继续推送出错: ${errorMsg}`, 'error')
    isProcessing.value = false
    
    ElMessage.error({
      message: errorMsg,
      duration: 3000
    })
  }
}

</script>

<template>
  <el-dialog
    v-model="visible"
    title="Git智能提交"
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
            <span class="log-content" :class="log.includes('cmd') ? 'is-cmd' : (log.includes('error') ? 'is-error' : (log.includes('success') ? 'is-success' : ''))">
              {{ log }}
            </span>
          </div>
          <div v-if="isProcessing" class="typing-cursor">_</div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" :disabled="isProcessing">关闭</el-button>
        
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
        
        <!-- 继续推送按钮（冲突时显示） -->
        <el-button 
          v-if="hasConflictPending"
          type="success" 
          @click="continuePush" 
          :loading="isProcessing"
        >
          {{ isProcessing ? '正在推送...' : '继续推送' }}
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

/* 分支选择区 */
.branch-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e8ecf1;
}

.branch-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.branch-arrow {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.input-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.label {
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.steps-container {
  background: #f8f9fa;
  padding: 30px 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  border: 1px solid #e8ecf1;
}

/* 终端窗口样式 */
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
  margin-right: 40px; /* 抵消dots的宽度以居中 */
}

.terminal-body {
  height: 300px;
  padding: 15px;
  overflow-y: auto;
  color: #d4d4d4;
  font-size: 14px;
  line-height: 1.6;
}

/* 滚动条美化 */
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
  color: #4fc1ff; /* 蓝色命令 */
  font-weight: bold;
}

.is-success {
  color: #4caf50; /* 绿色成功 */
}

.is-error {
  color: #f44336; /* 红色错误 */
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

/* 自定义Steps样式 */
:deep(.el-step__title) {
  font-size: 14px;
  font-weight: 600;
}

:deep(.el-step__description) {
  font-size: 12px;
}
</style>
