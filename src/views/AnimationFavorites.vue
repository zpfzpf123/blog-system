<template>
  <div class="animation-favorites">
    <div class="header">
      <h1>我的收藏</h1>
      <p>您收藏的动画效果</p>
    </div>

    <div v-if="favorites.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Star /></el-icon>
      <h3>暂无收藏</h3>
      <p>去动画演示页面收藏您喜欢的动画吧！</p>
      <el-button type="primary" @click="goToAnimations">
        浏览动画
      </el-button>
    </div>

    <div v-else class="favorites-grid">
      <div 
        v-for="animation in favorites" 
        :key="animation.id"
        class="animation-card"
      >
        <div class="animation-preview">
          <div class="preview-content" v-html="animation.htmlCode"></div>
        </div>
        <div class="animation-info">
          <h3>{{ animation.name }}</h3>
          <p>{{ animation.description }}</p>
          <div class="animation-actions">
            <el-button size="small" @click="viewAnimation(animation.id)">
              查看详情
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="removeFavorite(animation.id)"
            >
              取消收藏
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElIcon, ElButton } from 'element-plus'
import { Star } from '@element-plus/icons-vue'

const router = useRouter()

interface Animation {
  id: string
  name: string
  description: string
  htmlCode: string
  cssCode: string
}

const favorites = ref<Animation[]>([])

const loadFavorites = () => {
  const stored = localStorage.getItem('animation-favorites')
  if (stored) {
    favorites.value = JSON.parse(stored)
  }
}

const removeFavorite = (id: string) => {
  favorites.value = favorites.value.filter(item => item.id !== id)
  localStorage.setItem('animation-favorites', JSON.stringify(favorites.value))
}

const viewAnimation = (id: string) => {
  router.push(`/animation-demo/${id}`)
}

const goToAnimations = () => {
  router.push('/animation-demo')
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.animation-favorites {
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  color: #ddd;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 1.5rem;
  color: #666;
  margin-bottom: 10px;
}

.empty-state p {
  color: #999;
  margin-bottom: 30px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
}

.animation-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.animation-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.animation-preview {
  height: 200px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
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

.animation-actions {
  display: flex;
  gap: 10px;
}
</style>