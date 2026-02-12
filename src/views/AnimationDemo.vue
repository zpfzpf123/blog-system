<template>
  <div class="animation-demo">
    <div class="demo-header">
      <h1>动画演示</h1>
      <p>探索各种CSS动画效果</p>
      
      <div class="header-actions">
        <el-button @click="goToCategories">
          <el-icon><Folder /></el-icon>
          分类浏览
        </el-button>
        <el-button @click="goToFavorites">
          <el-icon><Star /></el-icon>
          我的收藏
        </el-button>
      </div>
    </div>

    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索动画效果..."
        size="large"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="animations-grid">
      <div 
        v-for="animation in filteredAnimations" 
        :key="animation.id"
        class="animation-card"
        @click="viewAnimation(animation.id)"
      >
        <div class="animation-preview">
          <div class="preview-content" v-html="animation.htmlCode"></div>
        </div>
        <div class="animation-info">
          <h3>{{ animation.name }}</h3>
          <p>{{ animation.description }}</p>
          <div class="animation-tags">
            <el-tag 
              v-for="tag in animation.tags" 
              :key="tag" 
              size="small"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElButton, ElIcon, ElInput, ElTag } from 'element-plus'
import { Folder, Star, Search } from '@element-plus/icons-vue'

const router = useRouter()

interface Animation {
  id: string
  name: string
  description: string
  htmlCode: string
  cssCode: string
  tags: string[]
}

const searchQuery = ref('')

// 示例动画数据
const animations = ref<Animation[]>([
  {
    id: '1',
    name: '旋转加载器',
    description: '简单的旋转加载动画效果',
    htmlCode: '<div class="demo-spinner"></div>',
    cssCode: `.demo-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: demo-spin 1s linear infinite;
}

@keyframes demo-spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}`,
    tags: ['加载', '旋转', 'CSS']
  },
  {
    id: '2',
    name: '脉冲按钮',
    description: '带有脉冲效果的按钮动画',
    htmlCode: '<button class="demo-pulse-btn">点击我</button>',
    cssCode: `.demo-pulse-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  animation: demo-pulse 2s infinite;
}

@keyframes demo-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}`,
    tags: ['按钮', '脉冲', '交互']
  },
  {
    id: '3',
    name: '弹跳球',
    description: '上下弹跳的小球动画',
    htmlCode: '<div class="demo-bounce-ball"></div>',
    cssCode: `.demo-bounce-ball {
  width: 30px;
  height: 30px;
  background: #e74c3c;
  border-radius: 50%;
  animation: demo-bounce 1s infinite;
}

@keyframes demo-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}`,
    tags: ['弹跳', '球', '物理']
  }
])

const filteredAnimations = computed(() => {
  if (!searchQuery.value) return animations.value
  
  return animations.value.filter(animation =>
    animation.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    animation.description.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    animation.tags.some(tag => tag.toLowerCase().includes(searchQuery.value.toLowerCase()))
  )
})

const viewAnimation = (id: string) => {
  router.push(`/animation-demo/${id}`)
}

const goToCategories = () => {
  router.push('/animation-demo/categories')
}

const goToFavorites = () => {
  router.push('/animation-demo/favorites')
}
</script>

<style scoped>
.animation-demo {
  padding: 20px;
  margin: 0 auto;
}

.demo-header {
  text-align: center;
  margin-bottom: 40px;
}

.demo-header h1 {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 10px;
}

.demo-header p {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.search-section {
  margin-bottom: 30px;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.animations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.animation-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.animation-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.animation-preview {
  height: 150px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.animation-info {
  padding: 20px;
}

.animation-info h3 {
  font-size: 1.3rem;
  color: #333;
  margin-bottom: 8px;
}

.animation-info p {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}

.animation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 动画样式 */
:deep(.demo-spinner) {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: demo-spin 1s linear infinite;
}

:deep(.demo-pulse-btn) {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  animation: demo-pulse 2s infinite;
}

:deep(.demo-bounce-ball) {
  width: 30px;
  height: 30px;
  background: #e74c3c;
  border-radius: 50%;
  animation: demo-bounce 1s infinite;
}

@keyframes demo-spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes demo-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes demo-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

@media (max-width: 768px) {
  .animations-grid {
    grid-template-columns: 1fr;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>