<script setup lang="ts">
import { Search, Menu, CollectionTag, Plus, RefreshRight, DataLine } from '@element-plus/icons-vue'
import { ElTooltip, ElButton, ElInput, ElSelect, ElOption, ElIcon } from 'element-plus'

interface Category {
  id: number
  name: string
}

interface Tag {
  id: number
  name: string
}

defineProps<{
  categories: Category[]
  tags: Tag[]
  searchKeyword: string
  selectedCategoryIds: number[]
  selectedTagIds: number[]
}>()

const emit = defineEmits<{
  (e: 'update:searchKeyword', value: string): void
  (e: 'update:selectedCategoryIds', value: number[]): void
  (e: 'update:selectedTagIds', value: number[]): void
  (e: 'search'): void
  (e: 'reset'): void
  (e: 'create'): void
  (e: 'open-global-search'): void
}>()
</script>

<template>
  <div class="filter-wrapper">
    <!-- 顶部主要操作区 -->
    <div class="main-bar">
      <div class="search-section">
        <el-input
          :model-value="searchKeyword"
          @update:model-value="emit('update:searchKeyword', $event)"
          placeholder="搜索感兴趣的文章..."
          class="hero-search-input"
          clearable
          @keyup.enter="emit('search')"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" class="hero-search-btn" @click="emit('search')">
          搜索
        </el-button>
      </div>
      
      <el-button class="create-btn" type="success" @click="emit('create')">
        <el-icon><Plus /></el-icon>
        <span class="btn-text">新建博客</span>
      </el-button>
    </div>

    <!-- 底部筛选工具栏 -->
    <div class="filter-bar">
      <div class="filter-group">
        <el-select
          :model-value="selectedCategoryIds"
          @update:model-value="emit('update:selectedCategoryIds', $event)"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="全部分类"
          class="filter-select"
          clearable
        >
          <template #prefix>
            <el-icon><Menu /></el-icon>
          </template>
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>

        <el-select
          :model-value="selectedTagIds"
          @update:model-value="emit('update:selectedTagIds', $event)"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="全部标签"
          class="filter-select"
          clearable
        >
          <template #prefix>
            <el-icon><CollectionTag /></el-icon>
          </template>
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
      </div>

      <div class="tools-group">
        <el-tooltip content="全局搜索 (Ctrl+F)" placement="top">
          <el-button class="tool-btn" text @click="emit('open-global-search')">
            <el-icon><DataLine /></el-icon>
            <span>全局搜索</span>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="重置筛选" placement="top">
          <el-button class="tool-btn reset" text @click="emit('reset')">
            <el-icon><RefreshRight /></el-icon>
            <span>重置</span>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-wrapper {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 24px;
  box-shadow: 
    0 4px 6px -1px rgba(0, 0, 0, 0.05),
    0 10px 15px -3px rgba(0, 0, 0, 0.05),
    inset 0 0 0 1px rgba(255, 255, 255, 0.5);
  margin-bottom: 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.filter-wrapper:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04),
    inset 0 0 0 1px rgba(255, 255, 255, 0.6);
}

/* Main Bar */
.main-bar {
  display: flex;
  gap: 16px;
  height: 48px;
}

.search-section {
  flex: 1;
  display: flex;
  gap: 12px;
  background: #fff;
  border-radius: 14px;
  padding: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.search-section:focus-within {
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.3);
}

.hero-search-input {
  flex: 1;
}

:deep(.hero-search-input .el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
  padding-left: 8px;
  font-size: 1.05rem;
}

.search-icon {
  font-size: 1.2rem;
  color: #94a3b8;
}

.hero-search-btn {
  border-radius: 10px;
  padding: 0 24px;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  transition: all 0.3s ease;
}

.hero-search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.create-btn {
  height: 100%;
  border-radius: 14px;
  padding: 0 24px;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.3);
}

.create-btn .el-icon {
  margin-right: 6px;
  font-size: 1.2rem;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.filter-group {
  display: flex;
  gap: 12px;
  flex: 1;
}

.filter-select {
  flex: 1;
  max-width: 240px;
}

:deep(.filter-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08) inset;
  transition: all 0.3s ease;
}

:deep(.filter-select .el-input__wrapper:hover) {
  background: #fff;
  box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.3) inset;
}

:deep(.filter-select .el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.5) inset !important;
}

.tools-group {
  display: flex;
  gap: 8px;
}

.tool-btn {
  color: #64748b;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.tool-btn:hover {
  background: rgba(99, 102, 241, 0.1);
  color: #4f46e5;
}

.tool-btn.reset:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.tool-btn .el-icon {
  margin-right: 4px;
}

/* Responsive */
@media (max-width: 768px) {
  .main-bar {
    flex-direction: column;
    height: auto;
  }
  
  .create-btn {
    width: 100%;
    height: 44px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    flex-direction: column;
  }
  
  .filter-select {
    max-width: none;
  }
  
  .tools-group {
    justify-content: space-between;
    margin-top: 8px;
  }
}
</style>
