<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'
import { 
  Refresh, 
  Download, 
  Upload, 
  Document, 
  FolderOpened,
  Clock,
  User,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'

const props = defineProps<{
  projectId?: number
}>()

const emit = defineEmits(['pull', 'commit', 'history', 'branch', 'stash', 'files'])

// 状态数据
const loading = ref(false)
const status = ref({
  currentBranch: '',
  hasChanges: false,
  totalChanges: 0,
  modifiedFiles: [] as string[],
  untrackedFiles: [] as string[],
  deletedFiles: [] as string[],
  stagedFiles: [] as any[],
  stagedCount: 0,
  ahead: 0,
  behind: 0,
  lastCommit: {
    shortHash: '',
    message: '',
    author: '',
    timestamp: 0
  }
})

// 自动刷新定时器
let refreshTimer: number | null = null

// 加载状态
const loadStatus = async () => {
  if (!props.projectId) return
  
  try {
    loading.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/git/detailed-status`)
    
    if (response.data.success) {
      status.value = {
        currentBranch: response.data.currentBranch || '',
        hasChanges: response.data.hasChanges || false,
        totalChanges: response.data.totalChanges || 0,
        modifiedFiles: response.data.modifiedFiles || [],
        untrackedFiles: response.data.untrackedFiles || [],
        deletedFiles: response.data.deletedFiles || [],
        stagedFiles: response.data.stagedFiles || [],
        stagedCount: response.data.stagedCount || 0,
        ahead: response.data.ahead || 0,
        behind: response.data.behind || 0,
        lastCommit: response.data.lastCommit || {}
      }
    }
  } catch (error) {
    console.error('加载Git状态失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (timestamp: number) => {
  if (!timestamp) return '未知'
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前'
  
  return date.toLocaleDateString()
}

// 手动刷新
const refresh = () => {
  loadStatus()
  ElMessage.success('已刷新')
}

onMounted(() => {
  loadStatus()
  // 每30秒自动刷新
  refreshTimer = window.setInterval(loadStatus, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 暴露刷新方法
defineExpose({ refresh: loadStatus })
</script>

<template>
  <div class="git-status-panel" v-loading="loading">
    <!-- 顶部状态栏 -->
    <div class="status-header">
      <div class="branch-info">
        <el-icon class="branch-icon"><FolderOpened /></el-icon>
        <span class="branch-name">{{ status.currentBranch || '未知分支' }}</span>
        
        <!-- 领先/落后标记 -->
        <el-tag v-if="status.ahead > 0" type="success" size="small" class="sync-tag">
          <el-icon><ArrowUp /></el-icon> {{ status.ahead }}
        </el-tag>
        <el-tag v-if="status.behind > 0" type="warning" size="small" class="sync-tag">
          <el-icon><ArrowDown /></el-icon> {{ status.behind }}
        </el-tag>
      </div>
      
      <el-button-group class="action-buttons">
        <el-tooltip content="刷新状态" placement="top">
          <el-button :icon="Refresh" size="small" @click="refresh" />
        </el-tooltip>
        <el-tooltip content="拉取代码" placement="top">
          <el-button :icon="Download" size="small" @click="emit('pull')" />
        </el-tooltip>
        <el-tooltip content="提交代码" placement="top">
          <el-button :icon="Upload" size="small" type="primary" @click="emit('commit')" />
        </el-tooltip>
      </el-button-group>
    </div>

    <!-- 变更统计 -->
    <div class="changes-summary">
      <div class="change-item" @click="emit('files')">
        <span class="change-count" :class="{ 'has-changes': status.totalChanges > 0 }">
          {{ status.totalChanges }}
        </span>
        <span class="change-label">待提交</span>
      </div>
      <div class="change-item" @click="emit('files')">
        <span class="change-count staged">{{ status.stagedCount }}</span>
        <span class="change-label">已暂存</span>
      </div>
      <div class="change-item" @click="emit('stash')">
        <span class="change-count stash">
          <el-icon><Document /></el-icon>
        </span>
        <span class="change-label">Stash</span>
      </div>
    </div>

    <!-- 最后提交信息 -->
    <div class="last-commit" v-if="status.lastCommit?.shortHash" @click="emit('history')">
      <div class="commit-header">
        <el-icon><Clock /></el-icon>
        <span class="commit-hash">{{ status.lastCommit.shortHash }}</span>
        <span class="commit-time">{{ formatTime(status.lastCommit.timestamp) }}</span>
      </div>
      <div class="commit-message">{{ status.lastCommit.message }}</div>
      <div class="commit-author">
        <el-icon><User /></el-icon>
        {{ status.lastCommit.author }}
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-button size="small" text @click="emit('history')">
        <el-icon><Clock /></el-icon> 历史
      </el-button>
      <el-button size="small" text @click="emit('branch')">
        <el-icon><FolderOpened /></el-icon> 分支
      </el-button>
      <el-button size="small" text @click="emit('stash')">
        <el-icon><Document /></el-icon> Stash
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.git-status-panel {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 16px;
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.branch-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.branch-icon {
  font-size: 18px;
}

.branch-name {
  font-size: 16px;
  font-weight: 600;
}

.sync-tag {
  margin-left: 4px;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.changes-summary {
  display: flex;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.change-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s;
}

.change-item:hover {
  transform: scale(1.05);
}

.change-count {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
}

.change-count.has-changes {
  color: #ffd666;
}

.change-count.staged {
  color: #95de64;
}

.change-count.stash {
  font-size: 20px;
}

.change-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}

.last-commit {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.last-commit:hover {
  background: rgba(255, 255, 255, 0.2);
}

.commit-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 6px;
}

.commit-hash {
  font-family: 'Consolas', monospace;
  background: rgba(0, 0, 0, 0.2);
  padding: 2px 6px;
  border-radius: 4px;
}

.commit-time {
  margin-left: auto;
}

.commit-message {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.commit-author {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  opacity: 0.8;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.quick-actions .el-button {
  color: white;
  opacity: 0.9;
}

.quick-actions .el-button:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.1);
}
</style>
