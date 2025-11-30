<template>
  <div class="animation-categories">
    <div class="header">
      <h1>动画分类</h1>
      <p>浏览不同类型的动画效果</p>
    </div>

    <div class="categories-grid">
      <div 
        v-for="category in categories" 
        :key="category.id"
        class="category-card"
        @click="navigateToCategory(category.id)"
      >
        <div class="category-icon">
          <el-icon><component :is="category.icon" /></el-icon>
        </div>
        <h3>{{ category.name }}</h3>
        <p>{{ category.description }}</p>
        <div class="category-stats">
          <span>{{ category.count }} 个动画</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElIcon } from 'element-plus'
import { Star, Lightning, Refresh, View } from '@element-plus/icons-vue'

const router = useRouter()

const categories = ref([
  {
    id: 'loading',
    name: '加载动画',
    description: '各种加载效果和进度指示器',
    icon: Refresh,
    count: 12
  },
  {
    id: 'hover',
    name: '悬停效果',
    description: '鼠标悬停时的交互动画',
    icon: View,
    count: 8
  },
  {
    id: 'transition',
    name: '过渡动画',
    description: '页面和元素的过渡效果',
    icon: Lightning,
    count: 15
  },
  {
    id: 'special',
    name: '特效动画',
    description: '创意特效和视觉效果',
    icon: Star,
    count: 6
  }
])

const navigateToCategory = (categoryId: string) => {
  router.push(`/animation-demo?category=${categoryId}`)
}
</script>

<style scoped>
.animation-categories {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 10px;
}

.header p {
  font-size: 1.1rem;
  color: #666;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.category-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.category-icon {
  font-size: 3rem;
  color: #409eff;
  margin-bottom: 20px;
}

.category-card h3 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 10px;
}

.category-card p {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.5;
}

.category-stats {
  color: #999;
  font-size: 0.9rem;
}
</style>