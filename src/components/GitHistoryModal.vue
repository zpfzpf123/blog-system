<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import { Clock, User, Document, Refresh, Back, View, ArrowUp, ArrowDown, CopyDocument, Search, Select } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  projectId?: number
}>()

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 提交历史
const commits = ref<any[]>([])
const loading = ref(false)
const selectedCommit = ref<any>(null)
const commitFiles = ref<any[]>([])
const loadingFiles = ref(false)

// 搜索过滤
const searchKeyword = ref('')
const filteredCommits = computed(() => {
  if (!searchKeyword.value.trim()) return commits.value
  const keyword = searchKeyword.value.toLowerCase()
  return commits.value.filter(commit => 
    commit.message.toLowerCase().includes(keyword) ||
    commit.shortHash.toLowerCase().includes(keyword) ||
    commit.author.toLowerCase().includes(keyword)
  )
})

// 文件差异查看
const showDiffDialog = ref(false)
const diffContent = ref('')
const diffFileName = ref('')
const loadingDiff = ref(false)

// 差异导航相关
const diffContainerRef = ref<HTMLElement | null>(null)
const diffHunks = ref<number[]>([]) // 存储每个差异块的位置
const currentHunkIndex = ref(-1) // 当前差异块索引

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const contextMenuFile = ref<any>(null)

// 冲突相关
const showConflictPanel = ref(false)
const conflictFiles = ref<string[]>([])
const conflictOperation = ref('')

// 格式化差异内容，添加语法高亮和行号
const formatDiffContent = (content: string): string => {
  if (!content) return ''
  
  let hunkIndex = 0
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .split('\n')
    .map((line, index) => {
      if (line.startsWith('+++') || line.startsWith('---')) {
        return `<span class="diff-file">${line}</span>`
      } else if (line.startsWith('@@')) {
        // 为差异块添加 data-hunk-index 属性，方便导航
        return `<span class="diff-hunk" data-hunk-index="${hunkIndex++}">${line}</span>`
      } else if (line.startsWith('+')) {
        return `<span class="diff-add">${line}</span>`
      } else if (line.startsWith('-')) {
        return `<span class="diff-del">${line}</span>`
      }
      return line
    })
    .join('\n')
}

// 解析差异块位置
const parseDiffHunks = () => {
  nextTick(() => {
    if (!diffContainerRef.value) return
    
    const hunks = diffContainerRef.value.querySelectorAll('[data-hunk-index]')
    diffHunks.value = Array.from(hunks).map(el => (el as HTMLElement).offsetTop)
    currentHunkIndex.value = diffHunks.value.length > 0 ? 0 : -1
  })
}

// 跳转到上一个差异块
const goToPrevHunk = () => {
  if (diffHunks.value.length === 0) return
  
  if (currentHunkIndex.value <= 0) {
    // 循环到最后一个
    currentHunkIndex.value = diffHunks.value.length - 1
  } else {
    currentHunkIndex.value--
  }
  
  scrollToHunk(currentHunkIndex.value)
}

// 跳转到下一个差异块
const goToNextHunk = () => {
  if (diffHunks.value.length === 0) return
  
  if (currentHunkIndex.value >= diffHunks.value.length - 1) {
    // 循环到第一个
    currentHunkIndex.value = 0
  } else {
    currentHunkIndex.value++
  }
  
  scrollToHunk(currentHunkIndex.value)
}

// 滚动到指定差异块
const scrollToHunk = (index: number) => {
  if (!diffContainerRef.value || index < 0 || index >= diffHunks.value.length) return
  
  const targetPosition = diffHunks.value[index]
  diffContainerRef.value.scrollTo({
    top: targetPosition - 50, // 留出一些顶部空间
    behavior: 'smooth'
  })
}

// 显示右键菜单
const showContextMenu = (event: MouseEvent, file: any) => {
  event.preventDefault()
  contextMenuFile.value = file
  contextMenuPosition.value = { x: event.clientX, y: event.clientY }
  contextMenuVisible.value = true
  
  // 点击其他地方关闭菜单
  document.addEventListener('click', hideContextMenu, { once: true })
}

// 隐藏右键菜单
const hideContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuFile.value = null
}

// 回退单个文件
const revertSingleFile = async () => {
  if (!props.projectId || !selectedCommit.value || !contextMenuFile.value) return
  
  const file = contextMenuFile.value
  hideContextMenu()
  
  try {
    await ElMessageBox.confirm(
      `确定要将文件 "${file.path}" 回退到提交 ${selectedCommit.value.shortHash} 的状态吗？\n\n这将覆盖当前工作区中该文件的内容。`,
      '确认回退文件',
      {
        confirmButtonText: '确定回退',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/revert-file`, {
      commitHash: selectedCommit.value.hash,
      filePath: file.path
    })
    
    if (response.data.success) {
      ElMessage.success(`文件 ${file.path} 已回退`)
      emit('refresh')
    } else {
      ElMessage.error(response.data.message || '回退文件失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('回退文件失败')
    }
  }
}

// 复制文件路径
const copyFilePath = () => {
  if (!contextMenuFile.value) return
  
  navigator.clipboard.writeText(contextMenuFile.value.path)
    .then(() => {
      ElMessage.success('已复制文件路径')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
  
  hideContextMenu()
}

// 复制提交哈希
const copyCommitHash = (hash: string) => {
  navigator.clipboard.writeText(hash)
    .then(() => {
      ElMessage.success('已复制提交哈希')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

// Cherry-pick 拣选提交
const cherryPickCommit = async (commit: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要将提交 "${commit.message}" (${commit.shortHash}) 拣选到当前分支吗？`,
      '确认 Cherry-pick',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/cherry-pick`, {
      commitHash: commit.hash
    })
    
    if (response.data.success) {
      ElMessage.success('Cherry-pick 成功')
      emit('refresh')
      loadHistory()
    } else {
      if (response.data.hasConflict) {
        // 显示冲突文件列表
        conflictFiles.value = response.data.conflictFiles || []
        conflictOperation.value = 'cherry-pick'
        showConflictPanel.value = true
        ElMessage.warning(`Cherry-pick 产生 ${conflictFiles.value.length} 个冲突文件`)
      } else {
        ElMessage.error(response.data.message || 'Cherry-pick 失败')
      }
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Cherry-pick 失败')
    }
  }
}

// 在WebStorm中打开冲突文件
const openConflictFileInIDE = async (filePath: string) => {
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

// 中止当前操作
const abortConflictOperation = async () => {
  if (!props.projectId) return
  
  try {
    await ElMessageBox.confirm(
      '确定要中止当前操作吗？这将放弃所有未完成的更改。',
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
      showConflictPanel.value = false
      conflictFiles.value = []
      emit('refresh')
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
const continueConflictOperation = async () => {
  if (!props.projectId) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/continue-operation`)
    
    if (response.data.success) {
      ElMessage.success('操作完成')
      showConflictPanel.value = false
      conflictFiles.value = []
      emit('refresh')
      loadHistory()
    } else {
      if (response.data.hasConflict) {
        conflictFiles.value = response.data.conflictFiles || []
        ElMessage.warning('仍有未解决的冲突')
      } else {
        ElMessage.error(response.data.message || '操作失败')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 键盘快捷键处理
const handleKeydown = (event: KeyboardEvent) => {
  if (!showDiffDialog.value) return
  
  // 上下箭头键导航差异块
  if (event.key === 'ArrowUp' || event.key === 'k') {
    event.preventDefault()
    goToPrevHunk()
  } else if (event.key === 'ArrowDown' || event.key === 'j') {
    event.preventDefault()
    goToNextHunk()
  } else if (event.key === 'Escape') {
    showDiffDialog.value = false
  }
}

// 注册/注销键盘事件
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})


// 加载提交历史 - 不限制条数
const loadHistory = async () => {
  if (!props.projectId) return
  
  try {
    loading.value = true
    // 移除limit参数，获取全部提交记录
    const response = await axios.get(`/api/projects/${props.projectId}/git/commits`)
    
    if (response.data.success) {
      commits.value = response.data.commits || []
    }
  } catch (error) {
    console.error('加载提交历史失败:', error)
    ElMessage.error('加载提交历史失败')
  } finally {
    loading.value = false
  }
}

// 加载提交文件
const loadCommitFiles = async (commit: any) => {
  if (!props.projectId) return
  
  selectedCommit.value = commit
  
  try {
    loadingFiles.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/git/commits/${commit.hash}/files`)
    
    if (response.data.success) {
      commitFiles.value = response.data.files || []
    }
  } catch (error) {
    console.error('加载提交文件失败:', error)
  } finally {
    loadingFiles.value = false
  }
}

// 查看文件差异
const viewFileDiff = async (file: any) => {
  if (!props.projectId || !selectedCommit.value) return
  
  try {
    loadingDiff.value = true
    diffFileName.value = file.path
    showDiffDialog.value = true
    currentHunkIndex.value = -1
    diffHunks.value = []
    
    const response = await axios.get(`/api/projects/${props.projectId}/git/commits/${selectedCommit.value.hash}/diff`, {
      params: { filePath: file.path }
    })
    
    if (response.data.success) {
      diffContent.value = response.data.diff || '无差异内容'
      // 解析差异块位置
      nextTick(() => {
        parseDiffHunks()
      })
    } else {
      diffContent.value = response.data.message || '获取差异失败'
    }
  } catch (error: any) {
    console.error('获取文件差异失败:', error)
    diffContent.value = '获取差异失败: ' + (error.message || '未知错误')
  } finally {
    loadingDiff.value = false
  }
}

// 格式化时间
const formatTime = (timestamp: number) => {
  if (!timestamp) return '未知'
  const date = new Date(timestamp)
  return date.toLocaleString()
}

// 格式化相对时间
const formatRelativeTime = (timestamp: number) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前'
  
  return date.toLocaleDateString()
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'A': return '#52c41a'
    case 'M': return '#1890ff'
    case 'D': return '#ff4d4f'
    default: return '#909399'
  }
}

// 获取状态背景色
const getStatusBgColor = (status: string) => {
  switch (status) {
    case 'A': return '#f6ffed'
    case 'M': return '#e6f7ff'
    case 'D': return '#fff2f0'
    default: return '#f5f5f5'
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

// 回退到某个提交
const resetToCommit = async (commit: any, mode: string) => {
  const modeText = mode === 'soft' ? '软回退（保留修改）' : 
                   mode === 'mixed' ? '混合回退（保留文件）' : '硬回退（丢弃所有）'
  
  try {
    await ElMessageBox.confirm(
      `确定要${modeText}到提交 ${commit.shortHash} 吗？\n\n提交信息: ${commit.message}`,
      '确认回退',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/reset`, {
      commitHash: commit.hash,
      mode: mode
    })
    
    if (response.data.success) {
      ElMessage.success('回退成功')
      emit('refresh')
      loadHistory()
    } else {
      ElMessage.error(response.data.message || '回退失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('回退失败')
    }
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadHistory()
    selectedCommit.value = null
    commitFiles.value = []
  }
})
</script>

<template>
  <el-dialog
    v-model="visible"
    title="提交历史"
    width="1000px"
    :close-on-click-modal="false"
    class="git-history-dialog"
  >
    <div class="history-container">
      <!-- 左侧：提交列表 -->
      <div class="commits-list" v-loading="loading">
        <div class="list-header">
          <span>提交记录 ({{ filteredCommits.length }})</span>
          <el-button :icon="Refresh" size="small" text @click="loadHistory">刷新</el-button>
        </div>
        
        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索提交信息、哈希、作者..."
            :prefix-icon="Search"
            clearable
            size="small"
          />
        </div>
        
        <div class="commits-scroll">
          <div 
            v-for="commit in filteredCommits" 
            :key="commit.hash"
            class="commit-item"
            :class="{ active: selectedCommit?.hash === commit.hash }"
            @click="loadCommitFiles(commit)"
          >
            <div class="commit-line">
              <div class="commit-dot"></div>
            </div>
            <div class="commit-content">
              <div class="commit-message">{{ commit.message }}</div>
              <div class="commit-meta">
                <span class="commit-hash">{{ commit.shortHash }}</span>
                <span class="commit-author">
                  <el-icon><User /></el-icon>
                  {{ commit.author }}
                </span>
                <span class="commit-time">{{ formatRelativeTime(commit.timestamp) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：提交详情 -->
      <div class="commit-detail" v-loading="loadingFiles">
        <template v-if="selectedCommit">
          <div class="detail-header">
            <div class="detail-title">
              <el-icon><Document /></el-icon>
              提交详情
            </div>
            
            <div class="action-buttons">
              <!-- Cherry-pick 操作 -->
              <el-tooltip placement="bottom">
                <template #content>
                  <div style="max-width: 280px; line-height: 1.6;">
                    <strong>Cherry-pick（拣选提交）</strong><br/>
                    将这个提交的修改"复制"到当前分支。<br/><br/>
                    <strong>使用场景：</strong><br/>
                    • 只想要某个分支的某一次提交<br/>
                    • 把修复bug的提交应用到其他分支<br/>
                    • 不想合并整个分支，只要部分提交
                  </div>
                </template>
                <el-button size="small" type="success" :icon="Select" @click="cherryPickCommit(selectedCommit)">
                  Cherry-pick
                </el-button>
              </el-tooltip>
              
              <!-- 回退操作 -->
              <el-dropdown trigger="click" @command="(mode: string) => resetToCommit(selectedCommit, mode)">
                <el-button size="small" type="warning" :icon="Back">
                  回退到此提交
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="soft">
                      <strong>软回退 (soft)</strong>
                      <div style="font-size: 12px; color: #909399;">保留暂存区和工作区</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="mixed">
                      <strong>混合回退 (mixed)</strong>
                      <div style="font-size: 12px; color: #909399;">保留工作区，清空暂存区</div>
                    </el-dropdown-item>
                    <el-dropdown-item command="hard" divided>
                      <strong style="color: #f56c6c;">硬回退 (hard)</strong>
                      <div style="font-size: 12px; color: #909399;">丢弃所有修改（危险）</div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          
          <div class="detail-info">
            <div class="info-row">
              <span class="info-label">提交哈希:</span>
              <code class="info-value hash-value clickable" @click="copyCommitHash(selectedCommit.hash)" title="点击复制">
                {{ selectedCommit.hash }}
                <el-icon class="copy-icon"><CopyDocument /></el-icon>
              </code>
            </div>
            <div class="info-row">
              <span class="info-label">提交信息:</span>
              <span class="info-value">{{ selectedCommit.message }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">作者:</span>
              <span class="info-value">{{ selectedCommit.author }} &lt;{{ selectedCommit.email }}&gt;</span>
            </div>
            <div class="info-row">
              <span class="info-label">时间:</span>
              <span class="info-value">{{ formatTime(selectedCommit.timestamp) }}</span>
            </div>
          </div>
          
          <div class="files-header">
            <span>变更文件 ({{ commitFiles.length }})</span>
            <span class="files-hint">双击查看差异</span>
          </div>
          
          <div class="files-list">
            <div 
              v-for="file in commitFiles" 
              :key="file.path" 
              class="file-item"
              :style="{ backgroundColor: getStatusBgColor(file.status) }"
              @dblclick="viewFileDiff(file)"
              @contextmenu="showContextMenu($event, file)"
            >
              <span class="file-status-badge" :style="{ backgroundColor: getStatusColor(file.status) }">
                {{ file.status }}
              </span>
              <div class="file-info">
                <span class="file-name">{{ getFileName(file.path) }}</span>
                <span class="file-dir" v-if="getFileDir(file.path)">{{ getFileDir(file.path) }}</span>
              </div>
              <el-tooltip content="双击查看差异" placement="left">
                <el-icon class="view-icon"><View /></el-icon>
              </el-tooltip>
            </div>
            <div v-if="commitFiles.length === 0" class="empty-files">
              暂无文件变更
            </div>
          </div>
          
          <!-- 右键菜单 -->
          <Teleport to="body">
            <div 
              v-if="contextMenuVisible" 
              class="context-menu"
              :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
            >
              <div class="context-menu-item" @click="viewFileDiff(contextMenuFile)">
                <el-icon><View /></el-icon>
                <span>查看差异</span>
              </div>
              <div class="context-menu-item" @click="revertSingleFile">
                <el-icon><Back /></el-icon>
                <span>回退此文件</span>
              </div>
              <div class="context-menu-divider"></div>
              <div class="context-menu-item" @click="copyFilePath">
                <el-icon><CopyDocument /></el-icon>
                <span>复制文件路径</span>
              </div>
            </div>
          </Teleport>
        </template>
        
        <div v-else class="empty-detail">
          <el-icon size="48"><Clock /></el-icon>
          <p>选择一个提交查看详情</p>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 冲突文件面板 -->
  <el-dialog
    v-model="showConflictPanel"
    :title="`${conflictOperation} 产生冲突`"
    width="600px"
    :close-on-click-modal="false"
    class="conflict-panel-dialog"
  >
    <div class="conflict-panel">
      <div class="conflict-warning">
        <el-icon class="warning-icon"><Back /></el-icon>
        <span>以下 {{ conflictFiles.length }} 个文件存在冲突，请解决后继续</span>
      </div>
      
      <div class="conflict-files-list">
        <div 
          v-for="(file, index) in conflictFiles" 
          :key="index"
          class="conflict-file-item"
          @dblclick="openConflictFileInIDE(file)"
        >
          <el-icon class="file-icon"><Document /></el-icon>
          <span class="file-path">{{ file }}</span>
          <el-button size="small" type="primary" text @click="openConflictFileInIDE(file)">
            在WebStorm中打开
          </el-button>
        </div>
      </div>
      
      <div class="conflict-hint">
        <p>💡 提示：双击文件可在WebStorm中打开</p>
        <p>解决冲突后，点击"冲突已解决，继续"按钮完成操作</p>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="showConflictPanel = false">稍后处理</el-button>
      <el-button type="danger" @click="abortConflictOperation">中止操作</el-button>
      <el-button type="success" @click="continueConflictOperation">冲突已解决，继续</el-button>
    </template>
  </el-dialog>

  <!-- 文件差异对话框 -->
  <el-dialog
    v-model="showDiffDialog"
    :title="`文件差异: ${diffFileName}`"
    width="900px"
    :close-on-click-modal="true"
    class="diff-dialog"
  >
    <!-- 差异导航控制器 -->
    <div class="diff-navigator" v-if="diffHunks.length > 0">
      <el-button-group>
        <el-button :icon="ArrowUp" @click="goToPrevHunk" title="上一个差异 (可循环)">
          上一个
        </el-button>
        <el-button disabled class="hunk-counter">
          {{ currentHunkIndex + 1 }} / {{ diffHunks.length }}
        </el-button>
        <el-button :icon="ArrowDown" @click="goToNextHunk" title="下一个差异 (可循环)">
          下一个
        </el-button>
      </el-button-group>
      <span class="navigator-hint">快捷键: ↑/k 上一个, ↓/j 下一个, Esc 关闭</span>
    </div>
    
    <div ref="diffContainerRef" class="diff-container" v-loading="loadingDiff">
      <pre class="diff-content"><code v-html="formatDiffContent(diffContent)"></code></pre>
    </div>
    <template #footer>
      <el-button @click="showDiffDialog = false">关闭</el-button>
    </template>
  </el-dialog>
</template>


<style scoped>
.history-container {
  display: flex;
  gap: 20px;
  height: 550px;
}

.commits-list {
  width: 420px;
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
  font-weight: 600;
  background: #f8f9fa;
}

.search-box {
  padding: 8px 12px;
  border-bottom: 1px solid #e8ecf1;
  background: #fafafa;
}

.commits-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.commit-item {
  display: flex;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.commit-item:hover {
  background: #f5f7fa;
}

.commit-item.active {
  background: #ecf5ff;
}

.commit-line {
  width: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 12px;
}

.commit-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #409eff;
  margin-top: 6px;
}

.commit-content {
  flex: 1;
  min-width: 0;
}

.commit-message {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.commit-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.commit-hash {
  font-family: 'Consolas', monospace;
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.commit-author {
  display: flex;
  align-items: center;
  gap: 4px;
}

.commit-detail {
  flex: 1;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8ecf1;
  background: #f8f9fa;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.detail-info {
  padding: 16px;
  border-bottom: 1px solid #e8ecf1;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 80px;
  color: #909399;
  font-size: 13px;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: 13px;
  word-break: break-all;
}

code.info-value {
  font-family: 'Consolas', monospace;
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.hash-value {
  font-size: 11px !important;
}

.hash-value.clickable {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.hash-value.clickable:hover {
  background: #e6f7ff;
  color: #409eff;
}

.copy-icon {
  font-size: 12px;
  opacity: 0.5;
  transition: opacity 0.2s;
}

.hash-value.clickable:hover .copy-icon {
  opacity: 1;
}

.files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 1px solid #e8ecf1;
  background: #fafafa;
}

.files-hint {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.files-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 13px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.file-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.file-item:last-child {
  margin-bottom: 0;
}

.file-status-badge {
  width: 22px;
  height: 22px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 12px;
  font-family: 'Consolas', monospace;
  flex-shrink: 0;
  margin-right: 12px;
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
  word-break: break-all;
}

.view-icon {
  color: #909399;
  font-size: 16px;
  opacity: 0;
  transition: opacity 0.2s;
  flex-shrink: 0;
  margin-left: 8px;
}

.file-item:hover .view-icon {
  opacity: 1;
  color: #409eff;
}

.empty-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.empty-detail p {
  margin-top: 12px;
}

.empty-files {
  text-align: center;
  color: #909399;
  padding: 20px;
}

/* 差异对话框样式 */
.diff-container {
  max-height: 500px;
  overflow: auto;
  background: #1e1e1e;
  border-radius: 8px;
}

.diff-content {
  margin: 0;
  padding: 16px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #d4d4d4;
  white-space: pre;
  overflow-x: auto;
}

:deep(.diff-file) {
  color: #569cd6;
  font-weight: bold;
}

:deep(.diff-hunk) {
  color: #c586c0;
  background: rgba(197, 134, 192, 0.1);
}

:deep(.diff-add) {
  color: #4ec9b0;
  background: rgba(78, 201, 176, 0.15);
}

:deep(.diff-del) {
  color: #f14c4c;
  background: rgba(241, 76, 76, 0.15);
}

/* 滚动条美化 */
.commits-scroll::-webkit-scrollbar,
.files-list::-webkit-scrollbar,
.diff-container::-webkit-scrollbar {
  width: 6px;
}

.commits-scroll::-webkit-scrollbar-track,
.files-list::-webkit-scrollbar-track {
  background: #f5f5f5;
}

.diff-container::-webkit-scrollbar-track {
  background: #1e1e1e;
}

.commits-scroll::-webkit-scrollbar-thumb,
.files-list::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.diff-container::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 3px;
}

.commits-scroll::-webkit-scrollbar-thumb:hover,
.files-list::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

.diff-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 差异导航控制器 */
.diff-navigator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.hunk-counter {
  min-width: 80px;
  font-weight: 600;
  color: #409eff !important;
}

.navigator-hint {
  font-size: 12px;
  color: #909399;
}

/* 右键菜单样式 */
.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  min-width: 160px;
  z-index: 9999;
  animation: contextMenuFadeIn 0.15s ease;
}

@keyframes contextMenuFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
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

.context-menu-item .el-icon {
  font-size: 16px;
}

.context-menu-divider {
  height: 1px;
  background: #e4e7ed;
  margin: 6px 0;
}

/* 差异块高亮当前位置 */
:deep([data-hunk-index]) {
  scroll-margin-top: 50px;
}

/* 冲突面板样式 */
.conflict-panel {
  padding: 0;
}

.conflict-warning {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #f56c6c;
  font-weight: 600;
}

.conflict-warning .warning-icon {
  font-size: 24px;
  animation: pulse 1.5s infinite;
}

.conflict-files-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
}

.conflict-panel .conflict-file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-bottom: 1px solid #e8ecf1;
  cursor: pointer;
  transition: background 0.2s;
}

.conflict-panel .conflict-file-item:last-child {
  border-bottom: none;
}

.conflict-panel .conflict-file-item:hover {
  background: #fef0f0;
}

.conflict-panel .file-icon {
  color: #f56c6c;
  font-size: 18px;
  flex-shrink: 0;
}

.conflict-panel .file-path {
  flex: 1;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  word-break: break-all;
}

.conflict-hint {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
}

.conflict-hint p {
  margin: 4px 0;
}
</style>
