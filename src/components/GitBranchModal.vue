<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import { 
  FolderOpened, 
  Plus, 
  Delete, 
  Refresh,
  Check,
  Connection
} from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  projectId?: number
}>()

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 分支数据
const currentBranch = ref('')
const localBranches = ref<string[]>([])
const remoteBranches = ref<string[]>([])
const loading = ref(false)

// 创建分支
const showCreateDialog = ref(false)
const newBranchName = ref('')
const checkoutAfterCreate = ref(true)
const creating = ref(false)

// 合并分支
const showMergeDialog = ref(false)
const mergeBranchName = ref('')
const merging = ref(false)

// 合并冲突
const mergeConflict = ref(false)
const mergeConflictFiles = ref<string[]>([])

// 加载分支列表
const loadBranches = async () => {
  if (!props.projectId) return
  
  try {
    loading.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/git/branches`)
    
    if (response.data.success) {
      currentBranch.value = response.data.currentBranch || ''
      localBranches.value = response.data.localBranches || []
      remoteBranches.value = response.data.remoteBranches || []
    }
  } catch (error) {
    console.error('加载分支列表失败:', error)
    ElMessage.error('加载分支列表失败')
  } finally {
    loading.value = false
  }
}

// 切换分支
const switchBranch = async (branchName: string) => {
  if (branchName === currentBranch.value) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/switch-branch`, {
      branchName
    })
    
    if (response.data.success) {
      ElMessage.success(`已切换到分支: ${branchName}`)
      currentBranch.value = branchName
      emit('refresh')
    } else {
      if (response.data.hasUncommittedChanges) {
        ElMessage.warning('有未提交的更改，无法切换分支')
      } else {
        ElMessage.error(response.data.message || '切换分支失败')
      }
    }
  } catch (error) {
    ElMessage.error('切换分支失败')
  }
}

// 创建分支
const createBranch = async () => {
  if (!newBranchName.value.trim()) {
    ElMessage.warning('请输入分支名称')
    return
  }
  
  try {
    creating.value = true
    const response = await axios.post(`/api/projects/${props.projectId}/git/branches/create`, {
      branchName: newBranchName.value,
      checkout: checkoutAfterCreate.value
    })
    
    if (response.data.success) {
      ElMessage.success(response.data.message)
      showCreateDialog.value = false
      newBranchName.value = ''
      loadBranches()
      emit('refresh')
    } else {
      ElMessage.error(response.data.message || '创建分支失败')
    }
  } catch (error) {
    ElMessage.error('创建分支失败')
  } finally {
    creating.value = false
  }
}

// 删除本地分支
const deleteLocalBranch = async (branchName: string) => {
  if (branchName === currentBranch.value) {
    ElMessage.warning('不能删除当前分支')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除本地分支 "${branchName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/branches/delete`, {
      branchName,
      force: false,
      remote: false
    })
    
    if (response.data.success) {
      ElMessage.success('分支已删除')
      loadBranches()
    } else {
      // 如果普通删除失败，询问是否强制删除
      await ElMessageBox.confirm(
        '该分支可能有未合并的更改，是否强制删除？',
        '强制删除',
        {
          confirmButtonText: '强制删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      const forceResponse = await axios.post(`/api/projects/${props.projectId}/git/branches/delete`, {
        branchName,
        force: true,
        remote: false
      })
      
      if (forceResponse.data.success) {
        ElMessage.success('分支已强制删除')
        loadBranches()
      } else {
        ElMessage.error(forceResponse.data.message || '删除失败')
      }
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除分支失败')
    }
  }
}

// 删除远程分支
const deleteRemoteBranch = async (branchName: string) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除远程分支 "${branchName}" 吗？\n\n此操作不可恢复！`,
      '确认删除远程分支',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/branches/delete`, {
      branchName,
      force: false,
      remote: true
    })
    
    if (response.data.success) {
      ElMessage.success('远程分支已删除')
      loadBranches()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除远程分支失败')
    }
  }
}

// 合并分支
const mergeBranch = async () => {
  if (!mergeBranchName.value) {
    ElMessage.warning('请选择要合并的分支')
    return
  }
  
  try {
    merging.value = true
    const response = await axios.post(`/api/projects/${props.projectId}/git/merge`, {
      branchName: mergeBranchName.value
    })
    
    if (response.data.success) {
      ElMessage.success('合并成功')
      showMergeDialog.value = false
      mergeBranchName.value = ''
      mergeConflict.value = false
      mergeConflictFiles.value = []
      emit('refresh')
    } else if (response.data.hasConflict) {
      // 显示冲突文件列表
      mergeConflictFiles.value = response.data.conflictFiles || []
      mergeConflict.value = true
      ElMessage.warning(`合并产生 ${mergeConflictFiles.value.length} 个冲突文件`)
      showMergeDialog.value = false
    } else {
      ElMessage.error(response.data.message || '合并失败')
    }
  } catch (error) {
    ElMessage.error('合并分支失败')
  } finally {
    merging.value = false
  }
}

// 在WebStorm中打开冲突文件
const openMergeConflictFile = async (filePath: string) => {
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

// 中止合并
const abortMerge = async () => {
  if (!props.projectId) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/abort-operation`)
    
    if (response.data.success) {
      ElMessage.success('已中止合并')
      mergeConflict.value = false
      mergeConflictFiles.value = []
    } else {
      ElMessage.error(response.data.message || '中止失败')
    }
  } catch (error) {
    ElMessage.error('中止操作失败')
  }
}

// 继续合并（解决冲突后）
const continueMerge = async () => {
  if (!props.projectId) return
  
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/git/continue-operation`)
    
    if (response.data.success) {
      ElMessage.success('合并完成')
      mergeConflict.value = false
      mergeConflictFiles.value = []
      emit('refresh')
    } else {
      if (response.data.hasConflict) {
        mergeConflictFiles.value = response.data.conflictFiles || []
        ElMessage.warning('仍有未解决的冲突')
      } else {
        ElMessage.error(response.data.message || '操作失败')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadBranches()
  }
})
</script>

<template>
  <el-dialog
    v-model="visible"
    title="分支管理"
    width="700px"
    :close-on-click-modal="false"
    class="git-branch-dialog"
  >
    <div class="branch-container" v-loading="loading">
      <!-- 当前分支 -->
      <div class="current-branch">
        <el-icon><FolderOpened /></el-icon>
        <span>当前分支:</span>
        <el-tag type="success" size="large">{{ currentBranch }}</el-tag>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">
          创建分支
        </el-button>
        <el-button :icon="Connection" @click="showMergeDialog = true">
          合并分支
        </el-button>
        <el-button :icon="Refresh" @click="loadBranches">
          刷新
        </el-button>
      </div>

      <!-- 分支列表 -->
      <div class="branches-section">
        <!-- 本地分支 -->
        <div class="branch-group">
          <div class="group-header">
            <span>本地分支 ({{ localBranches.length }})</span>
          </div>
          <div class="branch-list">
            <div 
              v-for="branch in localBranches" 
              :key="branch"
              class="branch-item"
              :class="{ current: branch === currentBranch }"
            >
              <div class="branch-name" @click="switchBranch(branch)">
                <el-icon v-if="branch === currentBranch" class="current-icon"><Check /></el-icon>
                {{ branch }}
              </div>
              <div class="branch-actions">
                <el-button 
                  v-if="branch !== currentBranch"
                  size="small" 
                  text 
                  type="primary"
                  @click="switchBranch(branch)"
                >
                  切换
                </el-button>
                <el-button 
                  v-if="branch !== currentBranch"
                  size="small" 
                  text 
                  type="danger"
                  :icon="Delete"
                  @click="deleteLocalBranch(branch)"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 远程分支 -->
        <div class="branch-group">
          <div class="group-header">
            <span>远程分支 ({{ remoteBranches.length }})</span>
          </div>
          <div class="branch-list">
            <div 
              v-for="branch in remoteBranches" 
              :key="branch"
              class="branch-item remote"
            >
              <div class="branch-name">
                {{ branch }}
              </div>
              <div class="branch-actions">
                <el-button 
                  size="small" 
                  text 
                  type="danger"
                  :icon="Delete"
                  @click="deleteRemoteBranch(branch)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>

    <!-- 创建分支对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建新分支"
      width="400px"
      append-to-body
    >
      <el-form label-width="80px">
        <el-form-item label="分支名称">
          <el-input 
            v-model="newBranchName" 
            placeholder="例如: feature/new-feature"
            @keyup.enter="createBranch"
          />
        </el-form-item>
        <el-form-item label="选项">
          <el-checkbox v-model="checkoutAfterCreate">创建后切换到新分支</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createBranch" :loading="creating">创建</el-button>
      </template>
    </el-dialog>

    <!-- 合并分支对话框 -->
    <el-dialog
      v-model="showMergeDialog"
      title="合并分支"
      width="400px"
      append-to-body
    >
      <el-form label-width="100px">
        <el-form-item label="当前分支">
          <el-tag>{{ currentBranch }}</el-tag>
        </el-form-item>
        <el-form-item label="合并来源">
          <el-select v-model="mergeBranchName" placeholder="选择要合并的分支" style="width: 100%">
            <el-option-group label="本地分支">
              <el-option 
                v-for="branch in localBranches.filter(b => b !== currentBranch)" 
                :key="branch"
                :label="branch"
                :value="branch"
              />
            </el-option-group>
            <el-option-group label="远程分支">
              <el-option 
                v-for="branch in remoteBranches" 
                :key="branch"
                :label="branch"
                :value="branch"
              />
            </el-option-group>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMergeDialog = false">取消</el-button>
        <el-button type="primary" @click="mergeBranch" :loading="merging">合并</el-button>
      </template>
    </el-dialog>
    
    <!-- 合并冲突面板 -->
    <el-dialog
      v-model="mergeConflict"
      title="合并冲突"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="merge-conflict-panel">
        <div class="conflict-warning">
          <span>⚠️ 合并产生 {{ mergeConflictFiles.length }} 个冲突文件，请解决后继续</span>
        </div>
        
        <div class="conflict-files-list">
          <div 
            v-for="(file, index) in mergeConflictFiles" 
            :key="index"
            class="conflict-file-item"
            @dblclick="openMergeConflictFile(file)"
          >
            <span class="file-path">{{ file }}</span>
            <el-button size="small" type="primary" text @click="openMergeConflictFile(file)">
              在WebStorm中打开
            </el-button>
          </div>
        </div>
        
        <div class="conflict-hint">
          <p>💡 双击文件可在WebStorm中打开</p>
          <p>解决冲突后，点击"冲突已解决，继续"按钮完成合并</p>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="mergeConflict = false">稍后处理</el-button>
        <el-button type="danger" @click="abortMerge">中止合并</el-button>
        <el-button type="success" @click="continueMerge">冲突已解决，继续</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
.branch-container {
  min-height: 400px;
}

.current-branch {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  font-size: 16px;
  margin-bottom: 20px;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.branches-section {
  display: flex;
  gap: 20px;
}

.branch-group {
  flex: 1;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  overflow: hidden;
}

.group-header {
  padding: 12px 16px;
  background: #f8f9fa;
  font-weight: 600;
  border-bottom: 1px solid #e8ecf1;
}

.branch-list {
  max-height: 300px;
  overflow-y: auto;
}

.branch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.2s;
}

.branch-item:last-child {
  border-bottom: none;
}

.branch-item:hover {
  background: #f5f7fa;
}

.branch-item.current {
  background: #ecf5ff;
}

.branch-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Consolas', monospace;
  font-size: 14px;
  cursor: pointer;
}

.current-icon {
  color: #67c23a;
}

.branch-item.remote .branch-name {
  color: #909399;
  cursor: default;
}

.branch-actions {
  display: flex;
  gap: 4px;
}

/* 合并冲突面板样式 */
.merge-conflict-panel {
  padding: 0;
}

.merge-conflict-panel .conflict-warning {
  padding: 16px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #f56c6c;
  font-weight: 600;
}

.merge-conflict-panel .conflict-files-list {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
}

.merge-conflict-panel .conflict-file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8ecf1;
  cursor: pointer;
  transition: background 0.2s;
}

.merge-conflict-panel .conflict-file-item:last-child {
  border-bottom: none;
}

.merge-conflict-panel .conflict-file-item:hover {
  background: #fef0f0;
}

.merge-conflict-panel .file-path {
  font-family: 'Consolas', monospace;
  font-size: 13px;
}

.merge-conflict-panel .conflict-hint {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
}

.merge-conflict-panel .conflict-hint p {
  margin: 4px 0;
}
</style>
