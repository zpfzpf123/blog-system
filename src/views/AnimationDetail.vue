<template>
  <div class="animation-detail">
    <div v-if="animation" class="detail-content">
      <div class="detail-header">
        <h1>{{ animation.name }}</h1>
        <div class="header-actions">
          <el-button @click="toggleFavorite" :type="isFavorite ? 'danger' : 'primary'">
            <el-icon><Star /></el-icon>
            {{ isFavorite ? '取消收藏' : '收藏' }}
          </el-button>
          <el-button @click="goBack">返回</el-button>
        </div>
      </div>

      <div class="detail-body">
        <div class="animation-preview">
          <h3>预览效果</h3>
          <div class="preview-container">
            <div class="preview-content" v-html="animation.htmlCode"></div>
          </div>
        </div>

        <div class="code-section">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="HTML" name="html">
              <div class="code-block">
                <pre><code>{{ animation.htmlCode }}</code></pre>
              </div>
            </el-tab-pane>
            <el-tab-pane label="CSS" name="css">
              <div class="code-block">
                <pre><code>{{ animation.cssCode }}</code></pre>
              </div>
            </el-tab-pane>
            <el-tab-pane v-if="animation.jsCode" label="JavaScript" name="js">
              <div class="code-block">
                <pre><code>{{ animation.jsCode }}</code></pre>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <div v-else class="loading">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElButton, ElIcon, ElTabs, ElTabPane } from 'element-plus'
import { Star, Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

interface Animation {
  id: string
  name: string
  htmlCode: string
  cssCode: string
  jsCode?: string
}

const animation = ref<Animation | null>(null)
const activeTab = ref('html')

// 模拟动画数据
const mockAnimations: Animation[] = [
  {
    id: '1',
    name: '旋转加载器',
    htmlCode: '<div class="spinner"></div>',
    cssCode: `.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}`
  }
]

const isFavorite = computed(() => {
  const favorites = JSON.parse(localStorage.getItem('animation-favorites') || '[]')
  return favorites.some((fav: Animation) => fav.id === animation.value?.id)
})

const loadAnimation = () => {
  const id = route.params.id as string
  animation.value = mockAnimations.find(anim => anim.id === id) || null
}

const toggleFavorite = () => {
  if (!animation.value) return
  
  const favorites = JSON.parse(localStorage.getItem('animation-favorites') || '[]')
  
  if (isFavorite.value) {
    const filtered = favorites.filter((fav: Animation) => fav.id !== animation.value!.id)
    localStorage.setItem('animation-favorites', JSON.stringify(filtered))
  } else {
    favorites.push(animation.value)
    localStorage.setItem('animation-favorites', JSON.stringify(favorites))
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadAnimation()
})
</script>

<style scoped>
.animation-detail {
  padding: 20px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.detail-header h1 {
  font-size: 2rem;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.detail-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.animation-preview h3 {
  margin-bottom: 15px;
  color: #333;
}

.preview-container {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.code-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.code-block {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 15px;
  overflow-x: auto;
}

.code-block pre {
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
}

.loading {
  text-align: center;
  padding: 60px 20px;
}

.loading-icon {
  font-size: 3rem;
  color: #409eff;
  margin-bottom: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .detail-body {
    grid-template-columns: 1fr;
  }
  
  .detail-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style>