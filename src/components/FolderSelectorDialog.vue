<script setup lang="ts">
import { ref, watch, computed } from 'vue'
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
      const parts = path.split(/[/\\]/).filter(p => p) // 过滤空字符串
      parts.pop()
      if (parts.length === 0) {
        // 已经在根目录，返回磁盘列表
        parentPath.value = ''
      } else if (parts.length === 1 && parts[0].endsWith(':')) {
        // 盘符根目录，如 E:\ 
        parentPath.value = parts[0] + '\\'
      } else {
        // 普通目录
        parentPath.value = parts.join('\\')
      }
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

// 生成面包屑导航
const breadcrumbs = computed(() => {
  if (!currentPath.value) {
    return [{ name: '我的电脑', path: '' }]
  }
  
  const parts = currentPath.value.split(/[/\\]/).filter(p => p)
  const crumbs = [{ name: '我的电脑', path: '' }]
  
  let accumulatedPath = ''
  parts.forEach((part, index) => {
    if (index === 0) {
      // 盘符，如 E:
      accumulatedPath = part + '\\'
    } else {
      // 子目录
      accumulatedPath += part
      if (index < parts.length - 1) {
        accumulatedPath += '\\'
      }
    }
    crumbs.push({ name: part, path: accumulatedPath })
  })
  
  return crumbs
})

// 点击面包屑跳转
const handleBreadcrumbClick = (path: string) => {
  if (path === '') {
    loadRootDrives()
  } else {
    loadDirectory(path)
  }
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
        <div class="breadcrumb-container">
          <span
            v-for="(crumb, index) in breadcrumbs"
            :key="crumb.path"
            class="breadcrumb-item"
          >
            <span
              class="breadcrumb-link"
              :class="{ 'active': index === breadcrumbs.length - 1 }"
              @click="handleBreadcrumbClick(crumb.path)"
            >
              {{ crumb.name }}
            </span>
            <span v-if="index < breadcrumbs.length - 1" class="breadcrumb-separator">\</span>
          </span>
        </div>
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

.breadcrumb-container {
  flex: 1;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  font-size: 14px;
  overflow-x: auto;
}

.breadcrumb-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.breadcrumb-link {
  color: #409eff;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.2s;
  user-select: none;
}

.breadcrumb-link:hover {
  background: #ecf5ff;
  color: #66b1ff;
}

.breadcrumb-link.active {
  color: #303133;
  font-weight: 500;
  cursor: default;
}

.breadcrumb-link.active:hover {
  background: transparent;
}

.breadcrumb-separator {
  color: #909399;
  font-size: 12px;
  user-select: none;
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
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
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
