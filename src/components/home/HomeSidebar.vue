<script setup lang="ts">
import { computed } from 'vue'
import { Plus, Folder, CollectionTag, RefreshRight, UserFilled } from '@element-plus/icons-vue'
import { ElButton, ElScrollbar, ElTag, ElAvatar, ElTooltip } from 'element-plus'

interface Category {
  id: number
  name: string
}

interface Tag {
  id: number
  name: string
}

const props = defineProps<{
  categories: Category[]
  tags: Tag[]
  selectedCategoryIds: number[]
  selectedTagIds: number[]
}>()

const emit = defineEmits<{
  (e: 'update:selectedCategoryIds', value: number[]): void
  (e: 'update:selectedTagIds', value: number[]): void
  (e: 'create'): void
  (e: 'reset'): void
}>()

const toggleCategory = (id: number) => {
  const newIds = props.selectedCategoryIds.includes(id)
    ? props.selectedCategoryIds.filter(cid => cid !== id)
    : [...props.selectedCategoryIds, id]
  emit('update:selectedCategoryIds', newIds)
}

const toggleTag = (id: number) => {
  const newIds = props.selectedTagIds.includes(id)
    ? props.selectedTagIds.filter(tid => tid !== id)
    : [...props.selectedTagIds, id]
  emit('update:selectedTagIds', newIds)
}

const hasActiveFilters = computed(() => {
  return props.selectedCategoryIds.length > 0 || props.selectedTagIds.length > 0
})
</script>

<template>
  <div class="sidebar-wrapper glass-panel">
    <!-- Profile / Brand Section -->
    <div class="sidebar-header">
      <div class="brand">
        <div class="logo-box">
          <el-icon><UserFilled /></el-icon>
        </div>
        <span class="brand-text">我的博客</span>
      </div>
      <el-button type="primary" class="create-btn" @click="emit('create')">
        <el-icon><Plus /></el-icon>
        <span>写文章</span>
      </el-button>
    </div>

    <el-scrollbar class="sidebar-scroll">
      <div class="sidebar-content">
        <!-- Categories -->
        <div class="section">
          <div class="section-header">
            <div class="section-title">
              <el-icon class="title-icon"><Folder /></el-icon>
              <span>分类导航</span>
            </div>
            <div class="count-badge">{{ categories.length }}</div>
          </div>
          <div class="category-grid">
            <div
              v-for="(cat, index) in categories"
              :key="cat.id"
              class="category-card"
              :class="{ active: selectedCategoryIds.includes(cat.id) }"
              :style="{ animationDelay: `${index * 0.05}s` }"
              @click="toggleCategory(cat.id)"
            >
              <div class="card-icon">
                <el-icon><Folder /></el-icon>
              </div>
              <div class="card-content">
                <span class="card-name">{{ cat.name }}</span>
                <div class="card-indicator" v-if="selectedCategoryIds.includes(cat.id)">
                  <el-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M406.656 706.944 195.84 496.256a32 32 0 1 0-45.248 45.248l256 256 512-512a32 32 0 0 0-45.248-45.248L406.592 706.944z"></path></svg></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tags -->
        <div class="section">
          <div class="section-header">
            <div class="section-title">
              <el-icon class="title-icon"><CollectionTag /></el-icon>
              <span>热门标签</span>
            </div>
            <div class="count-badge">{{ tags.length }}</div>
          </div>
          <div class="tag-cloud-new">
            <div
              v-for="(tag, index) in tags"
              :key="tag.id"
              class="tag-bubble"
              :class="{ active: selectedTagIds.includes(tag.id) }"
              :style="{ 
                animationDelay: `${index * 0.05}s`,
                '--tag-hue': (index * 40) % 360
              }"
              @click="toggleTag(tag.id)"
            >
              <span class="tag-text">{{ tag.name }}</span>
              <span class="tag-check" v-if="selectedTagIds.includes(tag.id)">✓</span>
            </div>
          </div>
        </div>
      </div>
    </el-scrollbar>

    <!-- Footer Actions -->
    <div class="sidebar-footer" v-if="hasActiveFilters">
      <el-button class="reset-btn" text bg @click="emit('reset')">
        <el-icon><RefreshRight /></el-icon>
        <span>重置筛选</span>
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.sidebar-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 28px;
  overflow: hidden;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.75) 0%, 
    rgba(252, 253, 255, 0.7) 100%);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 12px 40px rgba(99, 102, 241, 0.08),
              0 4px 16px rgba(0, 0, 0, 0.04),
              inset 0 1px 0 rgba(255, 255, 255, 0.9);
  animation: sidebarFadeIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes sidebarFadeIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.sidebar-header {
  padding: 28px;
  border-bottom: 1px solid rgba(99, 102, 241, 0.08);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(248, 250, 255, 0.3) 100%);
  position: relative;
}

.sidebar-header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 20%;
  right: 20%;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(99, 102, 241, 0.3) 50%, 
    transparent 100%);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.logo-box {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.3),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.logo-box:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4),
              inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.brand-text {
  font-size: 1.3rem;
  font-weight: 900;
  background: linear-gradient(135deg, #1e293b 0%, #4f46e5 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.create-btn {
  width: 100%;
  height: 48px;
  border-radius: 14px;
  font-weight: 700;
  font-size: 1rem;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.25),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.create-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.create-btn:hover::before {
  left: 100%;
}

.create-btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 10px 28px rgba(16, 185, 129, 0.35),
              inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.create-btn:active {
  transform: translateY(-1px) scale(0.98);
}

.sidebar-scroll {
  flex: 1;
}

.sidebar-content {
  padding: 24px;
}

.section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.95rem;
  font-weight: 800;
  background: linear-gradient(135deg, #334155 0%, #4f46e5 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 1.1rem;
  color: #6366f1;
}

.count-badge {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-size: 0.7rem;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 12px;
  min-width: 28px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* 分类卡片网格 */
.category-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.category-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.6) 0%, 
    rgba(250, 251, 255, 0.5) 100%);
  border: 2px solid transparent;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideIn 0.4s ease-out backwards;
  position: relative;
  overflow: hidden;
}

.category-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.1), transparent);
  transition: left 0.5s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.category-card:hover::before {
  left: 100%;
}

.category-card:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(252, 253, 255, 0.9) 100%);
  border-color: #6366f1;
  transform: translateX(8px) scale(1.02);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.2),
              inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.category-card.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.1) 100%);
  border-color: #6366f1;
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.25),
              inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transform: translateX(4px);
}

.card-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.category-card:hover .card-icon {
  transform: rotate(12deg) scale(1.15);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4),
              inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.category-card.active .card-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  transform: scale(1.05);
}

.card-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #475569;
  transition: color 0.3s ease;
}

.category-card:hover .card-name {
  color: #6366f1;
}

.category-card.active .card-name {
  color: #6366f1;
}

.card-indicator {
  color: #10b981;
  font-size: 1.2rem;
  animation: checkPop 0.3s ease-out;
}

@keyframes checkPop {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 标签云 - 新设计 */
.tag-cloud-new {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-bubble {
  position: relative;
  padding: 9px 18px;
  background: hsl(var(--tag-hue), 70%, 95%);
  color: hsl(var(--tag-hue), 70%, 40%);
  border: 2px solid hsl(var(--tag-hue), 70%, 85%);
  border-radius: 22px;
  font-size: 0.85rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: bubbleIn 0.4s ease-out backwards;
  box-shadow: 0 3px 10px hsla(var(--tag-hue), 70%, 50%, 0.15);
  overflow: hidden;
}

.tag-bubble::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s ease;
}

@keyframes bubbleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.tag-bubble:hover::before {
  left: 100%;
}

.tag-bubble:hover {
  transform: translateY(-4px) scale(1.08);
  box-shadow: 0 8px 20px hsla(var(--tag-hue), 70%, 50%, 0.3);
  background: hsl(var(--tag-hue), 70%, 88%);
  border-color: hsl(var(--tag-hue), 70%, 75%);
}

.tag-bubble.active {
  background: linear-gradient(135deg, 
    hsl(var(--tag-hue), 70%, 55%) 0%, 
    hsl(var(--tag-hue), 70%, 45%) 100%);
  color: white;
  border-color: hsl(var(--tag-hue), 70%, 45%);
  box-shadow: 0 6px 16px hsla(var(--tag-hue), 70%, 50%, 0.45),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.tag-text {
  display: inline-block;
}

.tag-check {
  margin-left: 6px;
  font-weight: 700;
  animation: checkPop 0.3s ease-out;
}

.sidebar-footer {
  padding: 20px 28px;
  border-top: 1px solid rgba(99, 102, 241, 0.08);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(248, 250, 255, 0.3) 100%);
  position: relative;
}

.sidebar-footer::before {
  content: '';
  position: absolute;
  top: -1px;
  left: 20%;
  right: 20%;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(99, 102, 241, 0.3) 50%, 
    transparent 100%);
}

.reset-btn {
  width: 100%;
  color: #ef4444;
}

.reset-btn:hover {
  background: #fee2e2;
}
</style>
