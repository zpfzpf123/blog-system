<template>
  <div class="tree-node">
    <!-- 文件夹节点 -->
    <div 
      v-if="!node.isFile" 
      class="folder-node"
      @click="toggleFolder"
    >
      <el-icon class="expand-icon" :class="{ expanded: isExpanded }">
        <ArrowRight />
      </el-icon>
      <el-icon class="folder-icon">
        <component :is="isExpanded ? FolderOpened : Folder" />
      </el-icon>
      <span class="node-name">{{ node.name }}</span>
    </div>

    <!-- 文件节点 -->
    <div 
      v-else 
      class="file-node"
      :class="{ selected: isSelected }"
      @click="handleSelectFile"
    >
      <el-icon class="file-icon">
        <Document />
      </el-icon>
      <span class="node-name">{{ node.name }}</span>
      <div class="file-stats">
        <span class="add-stat">+{{ node.stats?.additions || 0 }}</span>
        <span class="delete-stat">-{{ node.stats?.deletions || 0 }}</span>
      </div>
    </div>

    <!-- 子节点（递归） -->
    <div v-if="!node.isFile && isExpanded && node.children" class="children">
      <FileTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :selected-path="selectedPath"
        @select="$emit('select', $event)"
      />
    </div>
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
}

const props = defineProps<Props>()
const emit = defineEmits(['select'])

const isExpanded = ref(true) // 默认展开

const isSelected = computed(() => {
  return props.node.isFile && props.node.path === props.selectedPath
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
}

/* 文件夹节点 */
.folder-node {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.folder-node:hover {
  background: #f0f2f5;
}

.expand-icon {
  font-size: 14px;
  color: #909399;
  transition: transform 0.2s;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.folder-icon {
  font-size: 16px;
  color: #409eff;
}

/* 文件节点 */
.file-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 8px 8px 28px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
  position: relative;
}

.file-node:hover {
  background: #f5f7fa;
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
  font-size: 15px;
  color: #606266;
  flex-shrink: 0;
}

.node-name {
  flex: 1;
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'Monaco', 'Courier New', monospace;
}

.file-node.selected .node-name {
  color: #409eff;
  font-weight: 600;
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

/* 子节点缩进 */
.children {
  padding-left: 16px;
  margin-top: 4px;
}

/* 动画效果 */
.folder-node,
.file-node {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-4px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
