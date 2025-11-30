<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, ArrowLeft, Check } from '@element-plus/icons-vue'
import axios from '@/utils/axios'

interface DirectoryItem {
  name: string
  path: string
  type: 'folder' | 'file'
  size?: number
}

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', path: string): void
}>()

const visible = ref(props.modelValue)
const currentPath = ref('')
const parentPath = ref('')
const items = ref<DirectoryItem[]>([])
const loading = ref(false)
const selectedPath = ref('')

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadRootDrives()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 加载磁盘根目录
const loadRootDrives = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/filesystem/browse')
    items.value = response.data
    currentPath.value = ''
    parentPath.value = ''
    selectedPath.value = ''
  } catch (error) {
    console.error('加载磁盘列表失败:', error)
    ElMessage.error('加载磁盘列表失败')
  } finally {
    loading.value = false
  }
}

// 加载指定路径的文件夹
const loadDirectory = async (path: string) => {
  loading.value = true
  try {
    const response = await axios.get('/api/filesystem/browse', {
      params: { path }
    })
    items.value = response.data
    currentPath.value = path
    
    // 计算父路径
    if (path) {
      const parts = path.split(/[/\\]/)
      parts.pop()
      parentPath.value = parts.join('\\') || ''
    }
  } catch (error) {
    console.error('加载文件夹失败:', error)
    ElMessage.error('加载文件夹失败')
  } finally {
    loading.value = false
  }
}

// 点击文件夹
const handleFolderClick = (item: DirectoryItem) => {
  if (item.type === 'folder') {
    loadDirectory(item.path)
  }
}

// 选择文件夹
const handleSelectFolder = (item: DirectoryItem) => {
  if (item.type === 'folder') {
    selectedPath.value = item.path
  }
}

// 返回上一级
const goBack = () => {
  if (parentPath.value === '') {
    loadRootDrives()
  } else {
    loadDirectory(parentPath.value)
  }
}

// 确认选择
const handleConfirm = () => {
  if (!selectedPath.value) {
    ElMessage.warning('请选择一个文件夹')
    return
  }
  emit('confirm', selectedPath.value)
  visible.value = false
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="选择项目文件夹"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="folder-selector">
      <!-- 当前路径 -->
      <div class="current-path">
        <el-button :icon="ArrowLeft" size="small" @click="goBack" :disabled="currentPath === ''">
          返回上一级
        </el-button>
        <span class="path-text">{{ currentPath || '我的电脑' }}</span>
      </div>

      <!-- 文件夹列表 -->
      <div v-loading="loading" class="folder-list">
        <el-empty v-if="!loading && items.length === 0" description="文件夹为空" />
        
        <div
          v-for="item in items.filter(i => i.type === 'folder')"
          :key="item.path"
          class="folder-item"
          :class="{ 'selected': selectedPath === item.path }"
          @click="handleSelectFolder(item)"
          @dblclick="handleFolderClick(item)"
        >
          <el-icon class="folder-icon"><Folder /></el-icon>
          <span class="folder-name">{{ item.name }}</span>
          <el-icon v-if="selectedPath === item.path" class="check-icon"><Check /></el-icon>
        </div>
      </div>

      <!-- 选中的路径 -->
      <div v-if="selectedPath" class="selected-path">
        <strong>已选择:</strong> {{ selectedPath }}
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedPath">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.folder-selector {
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.current-path {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.path-text {
  flex: 1;
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}

.folder-list {
  flex: 1;
  min-height: 300px;
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 8px;
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.folder-item:hover {
  background: #f5f7fa;
}

.folder-item.selected {
  background: #ecf5ff;
  border: 1px solid #409eff;
}

.folder-icon {
  font-size: 20px;
  color: #f59e0b;
}

.folder-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.check-icon {
  font-size: 18px;
  color: #409eff;
}

.selected-path {
  margin-top: 16px;
  padding: 12px;
  background: #f0f9ff;
  border-left: 3px solid #409eff;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  word-break: break-all;
}

.selected-path strong {
  color: #409eff;
  margin-right: 8px;
}
</style>
