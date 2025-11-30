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
  background: linear-gradient(to bottom, #ffffff 0%, #fafbff 100%);
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  height: 100%;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(99, 102, 241, 0.08);
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.06), 0 2px 8px rgba(0, 0, 0, 0.02);
}

/* 悬停效果 */
.modern-card:hover {
  transform: translateY(-8px) scale(1.01);
  box-shadow: 0 24px 48px rgba(99, 102, 241, 0.16), 
              0 8px 20px rgba(139, 92, 246, 0.08),
              0 0 0 1px rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.3);
  background: linear-gradient(to bottom, #ffffff 0%, #f8f9ff 100%);
}

.card-body {
  padding: 28px;
  display: flex;
  flex-direction: column;
  height: 100%;
  z-index: 1;
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(240, 242, 255, 0.3) 50%,
    rgba(255, 255, 255, 0.5) 100%);
  backdrop-filter: blur(10px);
  transition: all 0.4s ease;
}

/* 顶部元信息 */
.card-meta-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 0.85rem;
  color: #94a3b8;
}

.category-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.12) 100%);
  color: #5b5fc7;
  border-radius: 24px;
  font-weight: 700;
  font-size: 0.82rem;
  letter-spacing: 0.3px;
  border: 1.5px solid rgba(99, 102, 241, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 3px 12px rgba(99, 102, 241, 0.15), inset 0 1px 2px rgba(255, 255, 255, 0.5);
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
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s ease;
}

.modern-card:hover .category-badge::before {
  left: 100%;
}

.modern-card:hover .category-badge {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.25) 0%, rgba(139, 92, 246, 0.2) 100%);
  border-color: rgba(99, 102, 241, 0.4);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.3), inset 0 1px 2px rgba(255, 255, 255, 0.6);
}

.date-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.12) 0%, rgba(251, 146, 60, 0.1) 100%);
  border-radius: 24px;
  color: #db2777;
  font-weight: 600;
  font-size: 0.82rem;
  letter-spacing: 0.3px;
  border: 1.5px solid rgba(236, 72, 153, 0.18);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 3px 12px rgba(236, 72, 153, 0.12), inset 0 1px 2px rgba(255, 255, 255, 0.5);
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
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.6s ease;
}

.modern-card:hover .date-badge::before {
  left: 100%;
}

.modern-card:hover .date-badge {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.18) 0%, rgba(251, 146, 60, 0.15) 100%);
  border-color: rgba(236, 72, 153, 0.3);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(236, 72, 153, 0.25), inset 0 1px 2px rgba(255, 255, 255, 0.6);
}

/* 核心内容 */
.card-main {
  flex: 1;
  margin-bottom: 20px;
}

.post-title {
  font-size: 1.45rem;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.4;
  margin: 0 0 14px 0;
  letter-spacing: -0.02em;
  display: block;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.modern-card:hover .post-title {
  background: linear-gradient(120deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transform: translateX(4px);
  text-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}

.post-desc {
  font-size: 0.96rem;
  color: #64748b;
  line-height: 1.7;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.modern-card:hover .post-desc {
  color: #475569;
}

/* 底部区域 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 18px;
  border-top: 1px solid rgba(99, 102, 241, 0.08);
  margin-bottom: 4px;
  min-height: 48px;
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
  transition: opacity 0.3s ease;
}

.modern-card:hover .card-footer::before {
  opacity: 1;
}

.empty-placeholder {
  flex: 1;
}

/* 底部标签装饰 */
.card-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mini-tag {
  font-size: 0.75rem;
  padding: 5px 12px;
  border-radius: 14px;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.12) 0%, rgba(236, 72, 153, 0.1) 100%);
  color: #a855f7;
  border: 1px solid rgba(168, 85, 247, 0.2);
  box-shadow: 0 2px 6px rgba(168, 85, 247, 0.12);
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
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  transition: left 0.4s ease;
}

.modern-card:hover .mini-tag::before {
  left: 100%;
}

.mini-tag:nth-child(2) {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.12) 0%, rgba(99, 102, 241, 0.1) 100%);
  color: #0ea5e9;
  border-color: rgba(14, 165, 233, 0.2);
  box-shadow: 0 2px 6px rgba(14, 165, 233, 0.12);
}

.mini-tag:nth-child(3) {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.12) 0%, rgba(34, 197, 94, 0.1) 100%);
  color: #10b981;
  border-color: rgba(16, 185, 129, 0.2);
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.12);
}

.modern-card:hover .mini-tag {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 4px 12px rgba(168, 85, 247, 0.25);
  border-color: rgba(168, 85, 247, 0.3);
}

.modern-card:hover .mini-tag:nth-child(2) {
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.25);
  border-color: rgba(14, 165, 233, 0.3);
}

.modern-card:hover .mini-tag:nth-child(3) {
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);
  border-color: rgba(16, 185, 129, 0.3);
}

/* 操作按钮组 */
.action-group {
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-left: auto;
}

.modern-card:hover .action-group {
  opacity: 1;
  transform: translateX(0);
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
  color: #94a3b8;
}

.icon-btn:hover {
  transform: scale(1.1);
}

.icon-btn.primary:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.1) 100%);
  color: #6366f1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
}

.icon-btn.danger:hover {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.15) 0%, rgba(220, 38, 38, 0.1) 100%);
  color: #ef4444;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}

.icon-btn.info:hover {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.15) 0%, rgba(6, 182, 212, 0.1) 100%);
  color: #0ea5e9;
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.2);
}

.icon-btn.success:hover {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(5, 150, 105, 0.1) 100%);
  color: #10b981;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

/* 底部标签装饰 */
.card-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mini-tag {
  font-size: 0.75rem;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.08) 0%, rgba(236, 72, 153, 0.08) 100%);
  color: #a855f7;
  border: 1px solid rgba(168, 85, 247, 0.15);
}

.mini-tag:nth-child(2) {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.08) 0%, rgba(99, 102, 241, 0.08) 100%);
  color: #0ea5e9;
  border-color: rgba(14, 165, 233, 0.15);
}

.mini-tag:nth-child(3) {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.08) 0%, rgba(34, 197, 94, 0.08) 100%);
  color: #10b981;
  border-color: rgba(16, 185, 129, 0.15);
}

.modern-card:hover .mini-tag {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(168, 85, 247, 0.15);
}

.modern-card:hover .mini-tag:nth-child(2) {
  box-shadow: 0 2px 8px rgba(14, 165, 233, 0.15);
}

.modern-card:hover .mini-tag:nth-child(3) {
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.15);
}

/* 装饰背景线 */
.card-bg-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle at top right, 
    rgba(99, 102, 241, 0.08) 0%, 
    rgba(168, 85, 247, 0.05) 25%,
    rgba(236, 72, 153, 0.03) 50%,
    transparent 75%);
  border-radius: 0 0 0 100%;
  pointer-events: none;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0.7;
}

.modern-card:hover .card-bg-decoration {
  background: radial-gradient(circle at top right, 
    rgba(99, 102, 241, 0.15) 0%, 
    rgba(168, 85, 247, 0.1) 25%,
    rgba(236, 72, 153, 0.06) 50%,
    transparent 75%);
  opacity: 1;
  transform: scale(1.1) rotate(5deg);
}
</style>
