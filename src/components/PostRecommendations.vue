<template>
  <div class="recommendations-container">
    <el-card class="recommendations-card">
      <template #header>
        <h3>相关推荐</h3>
      </template>
      
      <!-- 内部推荐文章 -->
      <div v-if="recommendations.length > 0" class="recommendations-section">
        <h4>本站内容</h4>
        <div class="recommendations-list">
          <div 
            v-for="post in recommendations" 
            :key="post.id"
            class="recommendation-item"
            @click="goToPost(post.id)"
          >
            <h5 class="recommendation-title">{{ post.title }}</h5>
            <p class="recommendation-summary">{{ truncateText(post.content, 100) }}</p>
            <div class="recommendation-meta">
              <span class="recommendation-date">{{ formatDate(post.createdAt) }}</span>
              <el-tag 
                v-for="tag in post.tags" 
                :key="tag.id" 
                size="small" 
                type="info"
                class="recommendation-tag"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 外部推荐文章 -->
      <div v-if="externalRecommendations.length > 0" class="recommendations-section">
        <h4>网络资源</h4>
        <div class="recommendations-list">
          <div 
            v-for="(item, index) in externalRecommendations" 
            :key="index"
            class="recommendation-item external"
            @click="openExternalLink(item.url)"
          >
            <h5 class="recommendation-title">{{ item.title }}</h5>
            <p class="recommendation-summary">{{ truncateText(item.snippet, 100) }}</p>
            <div class="recommendation-meta">
              <span class="recommendation-source">{{ getDomain(item.url) }}</span>
              <span v-if="item.score" class="recommendation-score">相关度: {{ Math.round(item.score * 100) }}%</span>
            </div>
            <!-- 显示外部文章内容预览 -->
            <div v-if="item.previewContent" class="recommendation-preview">
              <p>{{ truncateText(item.previewContent, 150) }}</p>
            </div>
            <!-- 加载预览按钮 -->
            <div v-else-if="!item.previewLoading" class="preview-button">
              <el-button size="small" @click.stop="loadPreview(item)" type="primary" link>
                加载内容预览
              </el-button>
            </div>
            <!-- 加载状态 -->
            <div v-else class="preview-loading">
              <el-skeleton animated>
                <template #template>
                  <el-skeleton-item variant="p" style="width: 100%; height: 40px" />
                </template>
              </el-skeleton>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 调试信息 -->
      <div v-if="showDebugInfo" class="debug-info">
        <h4>调试信息</h4>
        <p>文章ID: {{ props.postId }}</p>
        <p>标签数量: {{ props.tags?.length || 0 }}</p>
        <p>标题: {{ props.title }}</p>
        <p>内部推荐数量: {{ recommendations.length }}</p>
        <p>外部推荐数量: {{ externalRecommendations.length }}</p>
      </div>
      
      <!-- 当没有推荐内容时显示提示信息 -->
      <div v-if="recommendations.length === 0 && externalRecommendations.length === 0 && !showDebugInfo" class="no-recommendations">
        <p>暂无相关推荐内容</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElCard, ElTag, ElButton, ElSkeleton, ElSkeletonItem } from 'element-plus'
import axios from '@/utils/axios'

// 定义内部推荐文章类型
interface Post {
  id: number
  title: string
  content: string
  author: string
  createdAt: string
  category: {
    id: number
    name: string
  }
  tags: Array<{
    id: number
    name: string
  }>
}

// 定义外部推荐文章类型
interface ExternalRecommendation {
  title: string
  url: string
  snippet: string
  previewContent?: string
  previewLoading?: boolean
  score?: number
}

// Props
const props = defineProps<{
  postId: number
  tags: Array<{id: number, name: string}>
  category: {id: number, name: string}
  title: string
  content?: string
}>()

// 响应式数据
const recommendations = ref<Post[]>([])
const externalRecommendations = ref<ExternalRecommendation[]>([])
const router = useRouter()
// 调试信息开关
const showDebugInfo = ref(false)

// 获取推荐内容
const fetchRecommendations = async () => {
  try {
    console.log('获取推荐内容，参数:', props)
    // 获取内部推荐（基于相同标签的文章）
    if (props.tags && props.tags.length > 0) {
      const tagIds = props.tags.map(tag => tag.id)
      console.log('请求标签ID:', tagIds)
      const response = await axios.get(`/api/posts`, {
        params: {
          tagIds: tagIds.join(','),
          pageSize: 3
        }
      })
      
      console.log('内部推荐响应:', response.data)
      
      // 过滤掉当前文章本身
      recommendations.value = response.data.posts.filter((post: Post) => post.id !== props.postId)
    } else {
      console.log('没有标签信息，跳过内部推荐')
    }
    
    // 获取外部推荐（基于文章标题、内容和标签的智能搜索）
    if (props.title) {
      await fetchSmartExternalRecommendations()
    } else {
      console.log('没有标题信息，跳过外部推荐')
    }
    
    // 如果没有推荐内容，显示调试信息
    if (recommendations.value.length === 0 && externalRecommendations.value.length === 0) {
      showDebugInfo.value = true
      console.log('没有推荐内容，显示调试信息')
    }
  } catch (error) {
    console.error('获取推荐内容失败:', error)
    showDebugInfo.value = true
  }
}

// 获取智能外部推荐内容
const fetchSmartExternalRecommendations = async () => {
  try {
    console.log('获取智能外部推荐')
    
    // 准备标签参数
    const tagNames = props.tags?.map(tag => tag.name).join(',') || ''
    
    // 调用后端API获取智能外部推荐，使用POST方法，参数放入请求体
    // 限制content长度，避免请求体过大
    const truncatedContent = props.content ? props.content.substring(0, 1000) : '';
    const response = await axios.post(`/api/recommendations/external/smart`, {
      title: props.title,
      content: truncatedContent,
      tags: tagNames
    })
    
    console.log('智能外部推荐响应:', response.data)
    externalRecommendations.value = response.data
    
    // 初始化预览状态
    externalRecommendations.value.forEach(item => {
      item.previewLoading = false;
    });
  } catch (error) {
    console.error('获取智能外部推荐失败:', error)
    // 降级到基本的外部推荐
    await fetchExternalRecommendations(props.title)
  }
}

// 获取外部推荐内容（降级方案）
const fetchExternalRecommendations = async (query: string) => {
  try {
    console.log('获取外部推荐，查询词:', query)
    // 调用后端API获取外部推荐
    const response = await axios.get(`/api/recommendations/external`, {
      params: { query }
    })
    
    console.log('外部推荐响应:', response.data)
    externalRecommendations.value = response.data
    
    // 初始化预览状态
    externalRecommendations.value.forEach(item => {
      item.previewLoading = false;
    });
  } catch (error) {
    console.error('获取外部推荐失败:', error)
    // 出错时使用模拟数据
    const mockData: ExternalRecommendation[] = [
      {
        title: "Vue 3 性能优化技巧",
        url: "https://vuejs.org/guide/best-practices/performance.html",
        snippet: "了解如何优化Vue 3应用的性能，包括响应式系统优化、组件优化等重要技巧。"
      },
      {
        title: "Vue 3 Composition API 完全指南",
        url: "https://vuejs.org/guide/extras/composition-api-faq.html",
        snippet: "深入学习Vue 3的Composition API，掌握新的组件组织方式和逻辑复用机制。"
      }
    ]
    
    externalRecommendations.value = mockData
  }
}

// 加载外部内容预览
const loadPreview = async (item: ExternalRecommendation) => {
  try {
    item.previewLoading = true;
    const response = await axios.get(`/api/recommendations/external/preview`, {
      params: { url: item.url }
    });
    
    item.previewContent = response.data;
  } catch (error) {
    console.error('获取外部内容预览失败:', error);
    item.previewContent = "无法加载内容预览";
  } finally {
    item.previewLoading = false;
  }
}

// 跳转到文章详情
const goToPost = (id: number) => {
  router.push(`/post/${id}`)
}

// 打开外部链接
const openExternalLink = (url: string) => {
  window.open(url, '_blank')
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  const cleanText = text.replace(/<[^>]*>/g, '') // 移除HTML标签
  return cleanText.length > length ? cleanText.substring(0, length) + '...' : cleanText
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 获取域名
const getDomain = (url: string) => {
  try {
    const domain = new URL(url).hostname
    return domain.startsWith('www.') ? domain.substring(4) : domain
  } catch (error) {
    return url
  }
}

// 监听props变化
watch(() => [props.postId, props.tags, props.title], () => {
  fetchRecommendations()
})

// 组件挂载时获取推荐
onMounted(() => {
  fetchRecommendations()
})
</script>

<style scoped>
.recommendations-container {
  margin-top: 20px;
}

.recommendations-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.recommendations-section {
  margin-bottom: 20px;
}

.recommendations-section:last-child {
  margin-bottom: 0;
}

.recommendations-section h4 {
  margin: 0 0 10px 0;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
  color: #606266;
}

.recommendation-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.recommendation-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
}

.recommendation-item.external {
  background-color: #f9f9f9;
}

.recommendation-title {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.recommendation-summary {
  margin: 5px 0;
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}

.recommendation-meta {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}

.recommendation-date {
  margin-right: 10px;
}

.recommendation-tag {
  margin-right: 5px;
}

.recommendation-source {
  font-style: italic;
  margin-right: 10px;
}

.recommendation-score {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 2px 6px;
  border-radius: 4px;
}

.recommendation-preview {
  margin-top: 8px;
  padding: 8px;
  background-color: #fffbe6;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.preview-button {
  margin-top: 8px;
}

.preview-loading {
  margin-top: 8px;
}

.debug-info {
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
}

.no-recommendations {
  text-align: center;
  padding: 20px;
  color: #909399;
}
</style>