<script setup lang="ts">
import { computed } from 'vue'
import { Plus, Folder, CollectionTag, RefreshRight, UserFilled, Edit, Delete, MoreFilled } from '@element-plus/icons-vue'
import { ElButton, ElScrollbar, ElTag, ElAvatar, ElTooltip, ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'

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
  (e: 'addCategory'): void
  (e: 'editCategory', category: Category): void
  (e: 'deleteCategory', category: Category): void
  (e: 'addTag'): void
  (e: 'editTag', tag: Tag): void
  (e: 'deleteTag', tag: Tag): void
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

const handleCategoryAction = (action: string, category: Category) => {
  if (action === 'edit') {
    emit('editCategory', category)
  } else if (action === 'delete') {
    emit('deleteCategory', category)
  }
}

const handleTagAction = (action: string, tag: Tag) => {
  if (action === 'edit') {
    emit('editTag', tag)
  } else if (action === 'delete') {
    emit('deleteTag', tag)
  }
}
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
            <div class="header-actions">
              <div class="count-badge">{{ categories.length }}</div>
              <el-tooltip content="添加分类" placement="top">
                <el-button 
                  size="small" 
                  circle 
                  class="add-btn"
                  @click="emit('addCategory')"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
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
                <div class="card-actions">
                  <el-dropdown trigger="click" @command="(cmd) => handleCategoryAction(cmd, cat)">
                    <el-button 
                      size="small" 
                      circle 
                      class="action-btn"
                      @click.stop
                    >
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">
                          <el-icon><Edit /></el-icon>
                          编辑
                        </el-dropdown-item>
                        <el-dropdown-item command="delete" divided>
                          <el-icon><Delete /></el-icon>
                          删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                  <div class="card-indicator" v-if="selectedCategoryIds.includes(cat.id)">
                    <el-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M406.656 706.944 195.84 496.256a32 32 0 1 0-45.248 45.248l256 256 512-512a32 32 0 0 0-45.248-45.248L406.592 706.944z"></path></svg></el-icon>
                  </div>
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
            <div class="header-actions">
              <div class="count-badge">{{ tags.length }}</div>
              <el-tooltip content="添加标签" placement="top">
                <el-button 
                  size="small" 
                  circle 
                  class="add-btn"
                  @click="emit('addTag')"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
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
            >
              <span class="tag-text" @click="toggleTag(tag.id)">{{ tag.name }}</span>
              <span class="tag-check" v-if="selectedTagIds.includes(tag.id)" @click="toggleTag(tag.id)">✓</span>
              <el-dropdown trigger="click" @command="(cmd) => handleTagAction(cmd, tag)" class="tag-dropdown">
                <el-button 
                  size="small" 
                  circle 
                  class="tag-action-btn"
                  @click.stop
                >
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
  border-radius: var(--radius-2xl);
  overflow: hidden;
  background: var(--bg-glass);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  box-shadow: var(--shadow-glass);
  animation: sidebarFadeIn 0.5s var(--ease-out);
}

@keyframes sidebarFadeIn {
  from {
    opacity: 0;
    transform: translateX(-24px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.sidebar-header {
  padding: var(--spacing-6);
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
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-5);
}

.logo-box {
  width: 46px;
  height: 46px;
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  font-size: 20px;
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-normal);
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

.logo-box:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: var(--shadow-primary-lg);
}

.brand-text {
  font-size: var(--text-xl);
  font-weight: var(--font-extrabold);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.create-btn {
  width: 100%;
  height: 46px;
  border-radius: var(--radius-lg);
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  background: var(--gradient-success);
  border: none;
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.25);
  transition: all var(--transition-normal);
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
  background: var(--gradient-shimmer);
  transition: left 0.5s ease;
}

.create-btn:hover::before {
  left: 100%;
}

.create-btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 10px 28px rgba(16, 185, 129, 0.35);
}

.create-btn:active {
  transform: translateY(-1px) scale(0.98);
}

.sidebar-scroll {
  flex: 1;
}

.sidebar-content {
  padding: var(--spacing-5);
}

.section {
  margin-bottom: var(--spacing-8);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  background: var(--gradient-dark);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 1.1rem;
  color: var(--primary-color);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}

.count-badge {
  background: var(--gradient-primary);
  color: var(--text-inverse);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  padding: var(--spacing-1) var(--spacing-3);
  border-radius: var(--radius-lg);
  min-width: 28px;
  text-align: center;
  box-shadow: var(--shadow-primary);
}

.add-btn {
  width: 28px;
  height: 28px;
  background: var(--gradient-success) !important;
  border: none !important;
  color: white !important;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.25) !important;
  transition: all var(--transition-normal) !important;
}

.add-btn:hover {
  transform: scale(1.1) rotate(90deg) !important;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.35) !important;
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
  transition: background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  position: relative;
}

.category-card:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(252, 253, 255, 0.9) 100%);
  border-color: #6366f1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
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
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.category-card.active .card-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.card-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-2);
}

.card-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #475569;
  transition: color 0.3s ease;
  flex: 1;
}

.category-card:hover .card-name {
  color: #6366f1;
}

.category-card.active .card-name {
  color: #6366f1;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
}

.action-btn {
  width: 24px !important;
  height: 24px !important;
  min-width: 24px !important;
  padding: 0 !important;
  background: rgba(99, 102, 241, 0.1) !important;
  border: none !important;
  color: var(--primary-color) !important;
  visibility: hidden;
  transition: background var(--transition-fast), color var(--transition-fast) !important;
}

.category-card:hover .action-btn {
  visibility: visible;
}

.action-btn:hover {
  background: var(--primary-color) !important;
  color: white !important;
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
  transition: background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 3px 10px hsla(var(--tag-hue), 70%, 50%, 0.15);
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tag-bubble:hover {
  box-shadow: 0 6px 16px hsla(var(--tag-hue), 70%, 50%, 0.25);
  background: hsl(var(--tag-hue), 70%, 90%);
  border-color: hsl(var(--tag-hue), 70%, 75%);
}

.tag-bubble:hover .tag-action-btn {
  visibility: visible;
}

.tag-bubble.active {
  background: linear-gradient(135deg, 
    hsl(var(--tag-hue), 70%, 55%) 0%, 
    hsl(var(--tag-hue), 70%, 45%) 100%);
  color: white;
  border-color: hsl(var(--tag-hue), 70%, 45%);
  box-shadow: 0 6px 16px hsla(var(--tag-hue), 70%, 50%, 0.45),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.tag-text {
  display: inline-block;
  cursor: pointer;
  z-index: 1;
  position: relative;
}

.tag-check {
  margin-left: 0;
  font-weight: 700;
  animation: checkPop 0.3s ease-out;
  cursor: pointer;
  z-index: 1;
  position: relative;
}

.tag-dropdown {
  display: inline-flex;
  z-index: 2;
  position: relative;
}

.tag-action-btn {
  width: 20px !important;
  height: 20px !important;
  min-width: 20px !important;
  padding: 0 !important;
  background: rgba(0, 0, 0, 0.1) !important;
  border: none !important;
  color: currentColor !important;
  visibility: hidden;
  transition: background var(--transition-fast) !important;
  margin-left: 4px;
}

.tag-action-btn:hover {
  background: rgba(0, 0, 0, 0.2) !important;
}

.sidebar-footer {
  padding: var(--spacing-5) var(--spacing-6);
  border-top: 1px solid rgba(99, 102, 241, 0.08);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.5) 0%, 
    rgba(248, 250, 255, 0.3) 100%);
  position: relative;
  animation: fadeInUp 0.3s var(--ease-out);
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
  height: 42px;
  border-radius: var(--radius-lg);
  font-weight: var(--font-semibold);
  color: var(--danger-color);
  background: var(--danger-light);
  border: 1.5px solid transparent;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.reset-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(239, 68, 68, 0.1), transparent);
  transition: left 0.5s ease;
}

.reset-btn:hover::before {
  left: 100%;
}

.reset-btn:hover {
  background: var(--danger-light);
  border-color: var(--danger-color);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.2);
}

.reset-btn:active {
  transform: translateY(0);
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .sidebar-wrapper {
    border-radius: var(--radius-xl);
  }
  
  .sidebar-header {
    padding: var(--spacing-5);
  }
  
  .sidebar-content {
    padding: var(--spacing-4);
  }
  
  .category-card {
    padding: 12px;
  }
  
  .card-icon {
    width: 36px;
    height: 36px;
    font-size: 1rem;
  }
  
  .tag-bubble {
    padding: 7px 14px;
    font-size: 0.8rem;
  }
}
</style>
