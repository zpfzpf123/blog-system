<script setup lang="ts">
import { computed } from 'vue'
import {
  User,
  Calendar,
  Folder,
  Edit,
  Delete,
  Download,
  DocumentCopy,
  ArrowRight,
  MoreFilled
} from '@element-plus/icons-vue'
import { ElTooltip, ElButton, ElTag, ElIcon } from 'element-plus'

interface Category {
  id: number
  name: string
}

interface Tag {
  id: number
  name: string
}

interface BlogPost {
  id: number
  title: string
  desc: string
  author: string
  createdAt: string
  category: Category | null
  tags: Tag[]
}

const props = defineProps<{
  post: BlogPost
}>()

const emit = defineEmits<{
  (e: 'click', id: number): void
  (e: 'edit', post: BlogPost): void
  (e: 'delete', post: BlogPost): void
  (e: 'export', post: BlogPost): void
  (e: 'copy', post: BlogPost): void
}>()

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

const handleClick = () => {
  emit('click', props.post.id)
}

const handleAction = (action: 'edit' | 'delete' | 'export' | 'copy', e: MouseEvent) => {
  e.stopPropagation()
  emit(action, props.post)
}
</script>

<template>
  <div class="modern-card" @click="handleClick">
    <!-- 装饰性背景 -->
    <div class="card-bg-decoration"></div>

    <div class="card-body">
      <!-- 顶部元信息：分类与时间 -->
      <div class="card-meta-header">
        <div v-if="post.category" class="category-badge">
          <el-icon><Folder /></el-icon>
          <span>{{ post.category.name }}</span>
        </div>
        <div class="date-badge">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(post.createdAt) }}</span>
        </div>
      </div>

      <!-- 核心内容区 -->
      <div class="card-main">
        <h3 class="post-title">{{ post.title }}</h3>
        <p class="post-desc">{{ post.desc }}</p>
      </div>

      <!-- 底部区域：标签与操作 -->
      <div class="card-footer">
        <!-- 标签列表 -->
        <div class="card-tags" v-if="post.tags.length > 0">
          <span v-for="tag in post.tags.slice(0, 3)" :key="tag.id" class="mini-tag">
            #{{ tag.name }}
          </span>
        </div>
        <div v-else class="empty-placeholder"></div>

        <!-- 悬停时显示的操作栏 -->
        <div class="action-group">
          <div class="action-buttons">
            <el-tooltip content="编辑" placement="top" :show-after="500">
              <button class="icon-btn primary" @click="(e) => handleAction('edit', e)">
                <el-icon><Edit /></el-icon>
              </button>
            </el-tooltip>
            
            <el-tooltip content="复制" placement="top" :show-after="500">
              <button class="icon-btn info" @click="(e) => handleAction('copy', e)">
                <el-icon><DocumentCopy /></el-icon>
              </button>
            </el-tooltip>

            <el-tooltip content="导出" placement="top" :show-after="500">
              <button class="icon-btn success" @click="(e) => handleAction('export', e)">
                <el-icon><Download /></el-icon>
              </button>
            </el-tooltip>

            <el-tooltip content="删除" placement="top" :show-after="500">
              <button class="icon-btn danger" @click="(e) => handleAction('delete', e)">
                <el-icon><Delete /></el-icon>
              </button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modern-card {
  position: relative;
  background: var(--gradient-card);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  height: 100%;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(99, 102, 241, 0.08);
  box-shadow: var(--shadow-card);
}

/* 悬停效果 */
.modern-card:hover {
  transform: translateY(-8px) scale(1.01);
  box-shadow: var(--shadow-card-hover), 0 0 0 1px rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.25);
  background: linear-gradient(to bottom, #ffffff 0%, #f8f9ff 100%);
}

.card-body {
  padding: var(--spacing-6);
  display: flex;
  flex-direction: column;
  height: 100%;
  z-index: 1;
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.6) 0%, 
    rgba(248, 250, 255, 0.4) 50%,
    rgba(255, 255, 255, 0.6) 100%);
  backdrop-filter: blur(10px);
  transition: all 0.4s ease;
}

/* 顶部元信息 */
.card-meta-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4);
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.category-badge {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-4);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(139, 92, 246, 0.1) 100%);
  color: var(--primary-color);
  border-radius: var(--radius-full);
  font-weight: var(--font-semibold);
  font-size: var(--text-xs);
  letter-spacing: 0.3px;
  border: 1.5px solid rgba(99, 102, 241, 0.15);
  transition: all var(--transition-normal);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
  position: relative;
  overflow: hidden;
}

.category-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--gradient-shimmer);
  transition: left 0.5s ease;
}

.modern-card:hover .category-badge::before {
  left: 100%;
}

.modern-card:hover .category-badge {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(139, 92, 246, 0.15) 100%);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.2);
}

.date-badge {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-3);
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.1) 0%, rgba(251, 146, 60, 0.08) 100%);
  border-radius: var(--radius-full);
  color: var(--accent-color);
  font-weight: var(--font-medium);
  font-size: var(--text-xs);
  border: 1.5px solid rgba(236, 72, 153, 0.15);
  transition: all var(--transition-normal);
  box-shadow: 0 2px 8px rgba(236, 72, 153, 0.08);
  position: relative;
  overflow: hidden;
}

.date-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--gradient-shimmer);
  transition: left 0.6s ease;
}

.modern-card:hover .date-badge::before {
  left: 100%;
}

.modern-card:hover .date-badge {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.15) 0%, rgba(251, 146, 60, 0.12) 100%);
  border-color: rgba(236, 72, 153, 0.25);
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 4px 16px rgba(236, 72, 153, 0.15);
}

/* 核心内容 */
.card-main {
  flex: 1;
  margin-bottom: var(--spacing-5);
}

.post-title {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-main);
  line-height: var(--leading-snug);
  margin: 0 0 var(--spacing-3) 0;
  letter-spacing: -0.02em;
  display: block;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.modern-card:hover .post-title {
  background: var(--gradient-secondary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transform: translateX(4px);
}

.post-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-fast);
}

.modern-card:hover .post-desc {
  color: var(--text-regular);
}

/* 底部区域 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--spacing-4);
  border-top: 1px solid rgba(99, 102, 241, 0.08);
  margin-bottom: var(--spacing-1);
  min-height: 44px;
  position: relative;
}

.card-footer::before {
  content: '';
  position: absolute;
  top: -1px;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(99, 102, 241, 0.2) 50%, 
    transparent 100%);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.modern-card:hover .card-footer::before {
  opacity: 1;
}

.empty-placeholder {
  flex: 1;
}

/* 底部标签 */
.card-tags {
  display: flex;
  gap: var(--spacing-2);
  flex-wrap: wrap;
}

.mini-tag {
  font-size: var(--text-xs);
  padding: var(--spacing-1) var(--spacing-3);
  border-radius: var(--radius-full);
  font-weight: var(--font-medium);
  letter-spacing: 0.2px;
  transition: all var(--transition-normal);
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.1) 0%, rgba(236, 72, 153, 0.08) 100%);
  color: var(--secondary-color);
  border: 1px solid rgba(168, 85, 247, 0.15);
  box-shadow: 0 2px 4px rgba(168, 85, 247, 0.08);
  position: relative;
  overflow: hidden;
}

.mini-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--gradient-shimmer);
  transition: left 0.4s ease;
}

.modern-card:hover .mini-tag::before {
  left: 100%;
}

.mini-tag:nth-child(2) {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.1) 0%, rgba(99, 102, 241, 0.08) 100%);
  color: var(--info-color);
  border-color: rgba(14, 165, 233, 0.15);
  box-shadow: 0 2px 4px rgba(14, 165, 233, 0.08);
}

.mini-tag:nth-child(3) {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(34, 197, 94, 0.08) 100%);
  color: var(--success-color);
  border-color: rgba(16, 185, 129, 0.15);
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.08);
}

.modern-card:hover .mini-tag {
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 4px 10px rgba(168, 85, 247, 0.2);
}

.modern-card:hover .mini-tag:nth-child(2) {
  box-shadow: 0 4px 10px rgba(14, 165, 233, 0.2);
}

.modern-card:hover .mini-tag:nth-child(3) {
  box-shadow: 0 4px 10px rgba(16, 185, 129, 0.2);
}

/* 操作按钮组 */
.action-group {
  opacity: 0;
  transform: translateX(10px);
  transition: all var(--transition-normal);
  margin-left: auto;
}

.modern-card:hover .action-group {
  opacity: 1;
  transform: translateX(0);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-2);
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  background: transparent;
  color: var(--text-muted);
}

.icon-btn:hover {
  transform: scale(1.15);
}

.icon-btn:active {
  transform: scale(1.05);
}

.icon-btn.primary:hover {
  background: var(--primary-lighter);
  color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
}

.icon-btn.danger:hover {
  background: var(--danger-light);
  color: var(--danger-color);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}

.icon-btn.info:hover {
  background: var(--info-light);
  color: var(--info-color);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.2);
}

.icon-btn.success:hover {
  background: var(--success-light);
  color: var(--success-color);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

/* 装饰背景 */
.card-bg-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle at top right, 
    rgba(99, 102, 241, 0.06) 0%, 
    rgba(168, 85, 247, 0.04) 30%,
    transparent 70%);
  border-radius: 0 0 0 100%;
  pointer-events: none;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0.8;
}

.modern-card:hover .card-bg-decoration {
  background: radial-gradient(circle at top right, 
    rgba(99, 102, 241, 0.12) 0%, 
    rgba(168, 85, 247, 0.08) 30%,
    transparent 70%);
  opacity: 1;
  transform: scale(1.15) rotate(5deg);
}

/* 响应式 */
@media (max-width: 640px) {
  .card-body {
    padding: var(--spacing-4);
  }
  
  .post-title {
    font-size: var(--text-lg);
  }
  
  .card-meta-header {
    flex-wrap: wrap;
    gap: var(--spacing-2);
  }
  
  .category-badge,
  .date-badge {
    padding: var(--spacing-1) var(--spacing-3);
    font-size: 0.7rem;
  }
}
</style>
