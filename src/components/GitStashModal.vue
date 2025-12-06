<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import { Document, Plus, Delete, Refresh, Download } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  projectId?: number
}>()

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// Stash 列表
const stashes = ref<any[]>([])
const loading = ref(false)

// 创建 Stash
const showCreateDialog = ref(false)
const stashMessage = ref('')
const creating = ref(false)

// 加载 Stash 列表
const loadStashes = async () => {
  if (!props.projectId) return
  
  try {
    loading.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/git/stash/list`)
    
    if (response.data.success) {
      stashes.value = response.data.stashes || []
    }
  } catch (error) {
    console.error('加载Stash列表失败:', error)
    ElMessage.error('加载Stash列表失败')
  } finally {
    loading.value = false
  }
}

// 创建 Stash
const createStash = async () => {
  try {
    creating.value = true
    const response = await axios.post(`/api/projects/${props.projectId}/git/stash`, {
      message: stashMessage.value || undefined
    })
    
    if (response.data.success) {
      if (response.data.noChanges) {
        ElMessage.warning('没有需要暂存的修改')
      } else {
        ElMessage.success('暂存成功')
        showCreateDialog.value = false
        stashMessage.value = ''
        loadStashes()
        emit('refresh')
      }
    } else {
      ElMessage.error(response.data.message || '暂存失败')
    }
  } catch (error) {
    ElMessage.error('暂存失败')
  } finally {
    creating.value = false
  }
}

// 应用 Stash
const applyStash = async (index: number, pop: boolean) => {
  const action = pop ? '恢复并删除' : '恢复'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}这个暂存吗？`,
      '确认操作',
      {
        confirmButtonText: action,
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/stash/apply`, {
      index,
      pop
    })
    
    if (response.data.success) {
      ElMessage.success(response.data.message)
      if (pop) {
        loadStashes()
      }
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

// 删除 Stash
const dropStash = async (index: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个暂存吗？此操作不可恢复！',
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await axios.post(`/api/projects/${props.projectId}/git/stash/drop`, {
      index
    })
    
    if (response.data.success) {
      ElMessage.success('已删除')
      loadStashes()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadStashes()
  }
})
</script>

<template>
  <el-dialog
    v-model="visible"
    title="Stash 暂存管理"
    width="600px"
    :close-on-click-modal="false"
    class="git-stash-dialog"
  >
    <div class="stash-container" v-loading="loading">
      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">
          暂存当前修改
        </el-button>
        <el-button :icon="Refresh" @click="loadStashes">
          刷新
        </el-button>
      </div>

      <!-- 提示信息 -->
      <el-alert
        title="Stash 功能说明"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      >
        <template #default>
          <div style="font-size: 13px; line-height: 1.6;">
            Stash 可以临时保存当前的修改，方便切换分支或处理紧急任务。
          </div>
        </template>
      </el-alert>

      <!-- Stash 列表 -->
      <div class="stash-list">
        <div v-if="stashes.length === 0" class="empty-stash">
          <el-icon size="48"><Document /></el-icon>
          <p>暂无暂存记录</p>
        </div>
        
        <div 
          v-for="stash in stashes" 
          :key="stash.index"
          class="stash-item"
        >
          <div class="stash-info">
            <div class="stash-name">
              <el-tag size="small">{{ stash.name }}</el-tag>
            </div>
            <div class="stash-desc">{{ stash.description }}</div>
          </div>
          <div class="stash-actions">
            <el-tooltip content="恢复（保留暂存）" placement="top">
              <el-button 
                size="small" 
                type="primary"
                :icon="Download"
                @click="applyStash(stash.index, false)"
              >
                恢复
              </el-button>
            </el-tooltip>
            <el-tooltip content="恢复并删除暂存" placement="top">
              <el-button 
                size="small" 
                type="success"
                @click="applyStash(stash.index, true)"
              >
                Pop
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除暂存" placement="top">
              <el-button 
                size="small" 
                type="danger"
                :icon="Delete"
                @click="dropStash(stash.index)"
              />
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>

    <!-- 创建 Stash 对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="暂存当前修改"
      width="400px"
      append-to-body
    >
      <el-form>
        <el-form-item label="备注信息">
          <el-input 
            v-model="stashMessage" 
            placeholder="可选，描述这次暂存的内容"
            @keyup.enter="createStash"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createStash" :loading="creating">暂存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
.stash-container {
  min-height: 300px;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.stash-list {
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  overflow: hidden;
}

.empty-stash {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-stash p {
  margin-top: 12px;
}

.stash-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.2s;
}

.stash-item:last-child {
  border-bottom: none;
}

.stash-item:hover {
  background: #f5f7fa;
}

.stash-info {
  flex: 1;
  min-width: 0;
}

.stash-name {
  margin-bottom: 6px;
}

.stash-desc {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stash-actions {
  display: flex;
  gap: 8px;
  margin-left: 16px;
}
</style>
