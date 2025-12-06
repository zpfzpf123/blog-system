<template>
  <div class="tree-node">
    <!-- 文件夹节点 -->
    <div 
      v-if="!node.isFile" 
      class="folder-node"
      :class="{ 'has-children': node.children && node.children.length > 0 }"
      :style="{ paddingLeft: (Math.min((level || 0), 10) * 1.5 + 8) + 'px' }"
      @click="toggleFolder"
    >
      <!-- 层级指示线 -->
      <div 
        v-for="i in Math.min(level || 0, 10)" 
        :key="i" 
        class="level-indicator"
        :style="{ left: ((i - 1) * 1.5 + 4) + 'px' }"
      ></div>
      
      <el-icon class="expand-icon" :class="{ expanded: isExpanded }">
        <ArrowRight />
      </el-icon>
      <el-icon class="folder-icon">
        <component :is="isExpanded ? FolderOpened : Folder" />
      </el-icon>
      <span class="node-name" :title="node.path">{{ node.name }}</span>
    </div>

    <!-- 文件节点 -->
    <div 
      v-else 
      class="file-node"
      :class="{ selected: isSelected }"
      :style="{ paddingLeft: (Math.min((level || 0), 10) * 1.5 + 28) + 'px' }"
      @click="handleSelectFile"
      :title="node.path"
    >
      <!-- 层级指示线 -->
      <div 
        v-for="i in Math.min(level || 0, 10)" 
        :key="i" 
        class="level-indicator"
        :style="{ left: ((i - 1) * 1.5 + 4) + 'px' }"
      ></div>
      
      <el-icon class="file-icon">
        <Document />
      </el-icon>
      <div class="file-info">
        <span class="file-name">{{ node.name }}</span>
        <span class="file-path" v-if="hasPath">{{ getDirectoryPath }}</span>
      </div>
      <div class="file-stats">
        <span class="add-stat">+{{ node.stats?.additions || 0 }}</span>
        <span class="delete-stat">-{{ node.stats?.deletions || 0 }}</span>
      </div>
    </div>

    <!-- 子节点（递归） -->
    <template v-if="!node.isFile && isExpanded && node.children">
      <FileTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :level="(level || 0) + 1"
        :selected-path="selectedPath"
        @select="$emit('select', $event)"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Document, Folder, FolderOpened, ArrowRight } from '@element-plus/icons-vue'

interface TreeNode {
  name: string
  path: string
  isFile: boolean
  children?: TreeNode[]
  stats?: {
    additions: number
    deletions: number
  }
}

interface Props {
  node: TreeNode
  selectedPath: string
  level?: number
}

const props = defineProps<Props>()
const emit = defineEmits(['select'])

const isExpanded = ref(true) // 默认展开

const isSelected = computed(() => {
  return props.node.isFile && props.node.path === props.selectedPath
})

// 判断是否有路径（多级目录）
const hasPath = computed(() => {
  return props.node.path.includes('/')
})

// 获取目录路径（不包含文件名）
const getDirectoryPath = computed(() => {
  if (!hasPath.value) return ''
  const parts = props.node.path.split('/')
  return parts.slice(0, -1).join('/')
})

function toggleFolder() {
  isExpanded.value = !isExpanded.value
}

function handleSelectFile() {
  if (props.node.isFile) {
    emit('select', props.node.path)
  }
}
</script>

<style scoped>
.tree-node {
  user-select: none;
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 层级指示线 */
.level-indicator {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, #e8ecf0 0%, #d1d5db 50%, #e8ecf0 100%);
  opacity: 0.5;
  transition: all 0.3s ease;
  pointer-events: none;
}

.folder-node:hover .level-indicator,
.file-node:hover .level-indicator {
  background: linear-gradient(180deg, #c6d4e1 0%, #a0aec0 50%, #c6d4e1 100%);
  opacity: 0.8;
}

/* 文件夹节点 */
.folder-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  padding-right: 12px;
  cursor: pointer;
  border-radius: 6px;
  margin: 2px 8px;
  margin-right: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible;
  min-width: fit-content;
}

.folder-node:hover {
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f2ff 100%);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.expand-icon {
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  color: #909399;
  width: 14px;
  height: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.expand-icon.expanded {
  transform: rotate(90deg);
  color: #409eff;
}

.folder-node:hover .expand-icon {
  color: #409eff;
  transform: scale(1.1);
}

.folder-node:hover .expand-icon.expanded {
  transform: rotate(90deg) scale(1.1);
}

.folder-icon {
  font-size: 18px;
  color: #409eff;
  transition: all 0.3s ease;
  filter: drop-shadow(0 1px 2px rgba(64, 158, 255, 0.2));
}

.folder-node:hover .folder-icon {
  transform: scale(1.15);
  color: #3a8ee6;
}

/* 文件节点 */
.file-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  padding-right: 12px;
  cursor: pointer;
  border-radius: 6px;
  margin: 2px 8px;
  margin-right: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible;
  min-height: 36px;
  min-width: fit-content;
}

.file-node:hover {
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f2ff 100%);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.file-node.selected {
  background: linear-gradient(135deg, #e8f4fd 0%, #d4e9f7 100%);
  border-left: 3px solid #409eff;
  padding-left: 25px;
}

.file-node.selected::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #409eff;
  border-radius: 2px;
}

.file-icon {
  font-size: 16px;
  color: #67c23a;
  flex-shrink: 0;
  transition: all 0.3s ease;
  filter: drop-shadow(0 1px 2px rgba(103, 194, 58, 0.2));
}

.file-node:hover .file-icon {
  transform: scale(1.15);
  color: #5daf34;
}

/* 文件信息容器 */
.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  overflow: hidden;
}

/* 节点名称 */
.node-name {
  font-size: 14px;
  color: #303133;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  letter-spacing: 0.3px;
  transition: all 0.3s ease;
  display: inline-block;
  max-width: 300px;
}

.folder-node:hover .node-name,
.file-node:hover .node-name {
  color: #409eff;
  font-weight: 600;
  max-width: none;
  white-space: normal;
  word-break: break-all;
}

/* 文件名 - 始终可见且完整显示 */
.file-name {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  font-family: 'Monaco', 'Courier New', monospace;
  word-break: break-all;
  line-height: 1.4;
  transition: all 0.3s ease;
}

.file-node.selected .file-name {
  color: #409eff;
  font-weight: 600;
}

/* 文件路径 - 辅助信息 */
.file-path {
  font-size: 11px;
  color: #909399;
  font-family: 'Monaco', 'Courier New', monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  opacity: 0.8;
}

.file-node:hover .file-path {
  opacity: 1;
  color: #606266;
}

.file-node.selected .file-path {
  color: #79bbff;
}

/* 文件统计 */
.file-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  flex-shrink: 0;
}

.add-stat {
  color: #67c23a;
  font-size: 11px;
  font-weight: 600;
  background: rgba(103, 194, 58, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.delete-stat {
  color: #f56c6c;
  font-size: 11px;
  font-weight: 600;
  background: rgba(245, 108, 108, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

</style>
