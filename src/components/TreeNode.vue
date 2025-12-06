<script setup lang="ts">
import { ArrowRight, Folder, FolderOpened, Document } from '@element-plus/icons-vue'

interface TreeNodeData {
  name: string
  path: string
  isDirectory: boolean
  children?: TreeNodeData[]
  hasChildren?: boolean
  expanded?: boolean
}

const props = defineProps<{
  node: TreeNodeData
  level?: number
}>()

const emit = defineEmits<{
  'toggle-node': [node: TreeNodeData]
  'show-context-menu': [event: MouseEvent, node: TreeNodeData]
  'double-click-file': [node: TreeNodeData]
}>()

const handleToggle = () => {
  emit('toggle-node', props.node)
}

const handleContextMenu = (e: MouseEvent) => {
  emit('show-context-menu', e, props.node)
}

const handleDoubleClick = () => {
  if (!props.node.isDirectory) {
    emit('double-click-file', props.node)
  }
}
</script>

<template>
  <div class="tree-node">
    <div 
      class="node-content"
      :class="{ 
        'is-directory': node.isDirectory, 
        'is-file': !node.isDirectory,
        'has-children': node.isDirectory && node.children && node.children.length > 0
      }"
      :style="{ paddingLeft: (Math.min((level || 0), 10) * 1.5 + 8) + 'px' }"
      @click="handleToggle"
      @dblclick="handleDoubleClick"
      @contextmenu.prevent="handleContextMenu"
    >
      <!-- 层级指示线 -->
      <div 
        v-for="i in Math.min(level || 0, 10)" 
        :key="i" 
        class="level-indicator"
        :style="{ left: ((i - 1) * 1.5 + 4) + 'px' }"
      ></div>
      
      <el-icon v-if="node.isDirectory" class="expand-icon" :class="{ expanded: node.expanded }">
        <ArrowRight />
      </el-icon>
      <el-icon class="node-icon">
        <FolderOpened v-if="node.isDirectory && node.expanded" />
        <Folder v-else-if="node.isDirectory" />
        <Document v-else />
      </el-icon>
      <span class="node-name" :title="node.path">{{ node.name }}</span>
    </div>
    
    <template v-if="node.expanded && node.children">
      <TreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :level="(level || 0) + 1"
        @toggle-node="(n) => $emit('toggle-node', n)"
        @show-context-menu="(e, n) => $emit('show-context-menu', e, n)"
        @double-click-file="(n) => $emit('double-click-file', n)"
      />
    </template>
  </div>
</template>

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

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  padding-right: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 6px;
  margin: 2px 8px;
  margin-right: 12px;
  position: relative;
  overflow: visible;
  min-width: fit-content;
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

.node-content:hover .level-indicator {
  background: linear-gradient(180deg, #c6d4e1 0%, #a0aec0 50%, #c6d4e1 100%);
  opacity: 0.8;
}

/* 添加渐变背景效果 */
.node-content::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 0;
  height: 100%;
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, transparent 100%);
  transition: width 0.3s ease;
  z-index: 0;
}

.node-content:hover::before {
  width: 100%;
}

.node-content > * {
  position: relative;
  z-index: 1;
}

.node-content:hover {
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f2ff 100%);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.node-content.is-directory:hover {
  background: linear-gradient(135deg, #e6f7ff 0%, #d9efff 100%);
  box-shadow: 0 3px 12px rgba(64, 158, 255, 0.15);
}

.node-content:active {
  transform: translateX(2px) scale(0.98);
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

.node-content:hover .expand-icon {
  color: #409eff;
  transform: scale(1.1);
}

.node-content:hover .expand-icon.expanded {
  transform: rotate(90deg) scale(1.1);
}

.node-icon {
  font-size: 18px;
  color: #409eff;
  transition: all 0.3s ease;
  filter: drop-shadow(0 1px 2px rgba(64, 158, 255, 0.2));
}

.is-directory .node-icon {
  color: #409eff;
}

.is-file .node-icon {
  color: #67c23a;
  filter: drop-shadow(0 1px 2px rgba(103, 194, 58, 0.2));
}

.node-content:hover .node-icon {
  transform: scale(1.15);
}

.is-directory:hover .node-icon {
  color: #3a8ee6;
}

.is-file:hover .node-icon {
  color: #5daf34;
}

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

.node-content:hover .node-name {
  color: #409eff;
  font-weight: 600;
  max-width: none;
  white-space: normal;
  word-break: break-all;
}

.is-file .node-name {
  font-weight: 400;
}

.is-file:hover .node-name {
  color: #67c23a;
}

/* 添加选中状态的左侧边框装饰 */
.node-content::after {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #409eff 0%, #79bbff 100%);
  border-radius: 0 3px 3px 0;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
}

.node-content:hover::after {
  height: 70%;
  opacity: 1;
}

/* 文件夹特殊样式 */
.is-directory {
  font-weight: 500;
}

.is-directory::after {
  background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
}

.is-file::after {
  background: linear-gradient(180deg, #67c23a 0%, #85ce61 100%);
}
</style>
