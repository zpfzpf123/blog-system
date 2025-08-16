<script setup lang="ts">
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard,
  ElTable,
  ElTableColumn,
  ElButton,
  ElMessage,
  ElPopconfirm,
  ElTag,
  ElPagination,
  ElInput,
  ElSelect,
  ElOption,
  ElRow,
  ElCol,
  ElTooltip,
  ElMessageBox,
  ElIcon,
} from 'element-plus'
import {
  Edit,
  Delete,
  Plus,
  Download,
  Search,
  CollectionTag,
  Menu,
  Document,
  View,
  Star,
  DataAnalysis,
  Setting,
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import JSZip from 'jszip'
import Breadcrumb from '@/components/Breadcrumb.vue'

// 定义博客文章类型
interface BlogPost {
  id: number
  title: string
  desc: string
  author: string
  createdAt: string
  category: { id: number; name: string } | null
  tags: { id: number; name: string }[]
}

// 定义分类类型
interface Category {
  id: number
  name: string
}

// 定义标签类型
interface Tag {
  id: number
  name: string
}

// 响应式数据
const blogPosts = ref<BlogPost[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(true)
const router = useRouter()

// 表格选择相关
const selectedPosts = ref<number[]>([])

// 搜索和筛选相关
const searchKeyword = ref('')
const selectedCategoryIds = ref<number[]>([])
const selectedTagIds = ref<number[]>([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取所有分类
const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/categories')
    categories.value = response.data
  } catch (error) {
    ElMessage.error('获取分类失败')
    console.error('获取分类失败:', error)
  }
}

// 获取所有标签
const fetchTags = async () => {
  try {
    const response = await axios.get('/api/tags')
    tags.value = response.data
  } catch (error) {
    ElMessage.error('获取标签失败')
    console.error('获取标签失败:', error)
  }
}

// 获取博客文章列表
const fetchBlogPosts = async (page = 1) => {
  try {
    loading.value = true

    // 构建请求参数
    const params: any = {
      page: page,
      limit: pageSize.value,
    }

    // 添加分类筛选参数
    if (selectedCategoryIds.value.length > 0) {
      params.categoryIds = selectedCategoryIds.value
    }

    // 添加标签筛选参数
    if (selectedTagIds.value.length > 0) {
      params.tagIds = selectedTagIds.value
    }

    // 添加搜索关键词参数
    if (searchKeyword.value) {
      params.search = searchKeyword.value
    }

    const response = await axios.get('/api/posts', {
      params,
    })

    blogPosts.value = response.data.posts
    total.value = response.data.pagination.totalPosts
    currentPage.value = response.data.pagination.currentPage
  } catch (error) {
    ElMessage.error('获取博客文章失败')
    console.error('获取博客文章失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  const category = categories.value.find((c) => c.id === categoryId)
  return category ? category.name : '未分类'
}

// 页面改变时重新获取数据
const handlePageChange = (page: number) => {
  fetchBlogPosts(page)
}

// 每页条数改变时重新获取数据
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchBlogPosts(1) // 重置到第一页
}

// 跳转到创建博客页面
const goToCreatePost = () => {
  router.push('/admin/posts/create?from=admin')
}

// 跳转到编辑博客页面
const goToEditPost = (id: number) => {
  router.push(`/admin/posts/edit/${id}?from=admin`)
}

// 删除博客文章
const deletePost = async (id: number) => {
  try {
    await axios.delete(`/api/posts/${id}`)
    ElMessage.success('博客文章删除成功')
    fetchBlogPosts()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
    console.error('删除博客文章失败:', error)
  }
}

// 批量删除博客文章
const deleteSelectedPosts = async () => {
  if (selectedPosts.value.length === 0) {
    ElMessage.warning('请先选择要删除的文章')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedPosts.value.length} 篇文章吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    // 执行批量删除
    const deletePromises = selectedPosts.value.map((id) =>
      axios.delete(`/api/posts/${id}`).catch((error) => {
        console.error(`删除文章 ${id} 失败:`, error)
        return Promise.reject(error)
      }),
    )

    try {
      await Promise.all(deletePromises)
      ElMessage.success('选中的文章删除成功')
      selectedPosts.value = [] // 清空选择
      fetchBlogPosts() // 重新获取数据
    } catch (error) {
      ElMessage.error('部分文章删除失败，请重试')
      fetchBlogPosts() // 重新获取数据以同步状态
    }
  } catch {
    // 用户取消删除
    ElMessage.info('已取消删除')
  }
}

// 批量导出博客文章为Markdown
const exportSelectedPosts = async () => {
  if (selectedPosts.value.length === 0) {
    ElMessage.warning('请先选择要导出的文章')
    return
  }

  try {
    // 创建一个包含所有文章的ZIP文件
    const zip = new JSZip()

    // 获取选中的文章详情
    const fetchPromises = selectedPosts.value.map((id) =>
      axios.get(`/api/posts/${id}`).catch((error) => {
        console.error(`获取文章 ${id} 失败:`, error)
        return Promise.resolve(null) // 即使一篇文章失败，也不影响其他文章的导出
      }),
    )

    const results = await Promise.all(fetchPromises)
    const posts = results.filter((result) => result && result.data).map((result) => result!.data)

    // 将每篇文章添加到ZIP文件中
    posts.forEach((post: any) => {
      let markdownContent = `# ${post.title}\n\n`
      markdownContent += `**作者**: ${post.author}\n\n`
      markdownContent += `**发布日期**: ${formatDate(post.createdAt)}\n\n`

      if (post.category) {
        markdownContent += `**分类**: ${post.category.name}\n\n`
      }

      if (post.tags && post.tags.length > 0) {
        markdownContent += `**标签**: ${post.tags.map((tag: any) => tag.name).join(', ')}\n\n`
      }

      markdownContent += '---\n\n'
      markdownContent += post.content

      const fileName = `${post.title.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_')}.md`
      zip.file(fileName, markdownContent)
    })

    // 生成ZIP文件并下载
    const content = await zip.generateAsync({ type: 'blob' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(content)
    link.download = `博客文章_${new Date().toLocaleDateString('zh-CN')}.zip`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)

    ElMessage.success(`成功导出 ${posts.length} 篇文章`)
  } catch (error) {
    ElMessage.error('导出失败，请重试')
    console.error('导出失败:', error)
  }
}

// 查看博客详情
const viewPost = (id: number) => {
  router.push(`/blog/${id}`)
}

// 搜索功能
const filteredPosts = computed(() => {
  let result = blogPosts.value

  // 根据关键字搜索标题和摘要
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (post) =>
        post.title.toLowerCase().includes(keyword) || post.desc.toLowerCase().includes(keyword),
    )
  }

  // 根据分类筛选
  if (selectedCategoryIds.value.length > 0) {
    result = result.filter(
      (post) => post.category && selectedCategoryIds.value.includes(post.category.id),
    )
  }

  // 根据标签筛选
  if (selectedTagIds.value.length > 0) {
    result = result.filter((post) => post.tags.some((tag) => selectedTagIds.value.includes(tag.id)))
  }

  return result
})

// 用于过渡动画的键，当搜索或筛选条件变化时触发表格淡入淡出
const filterKey = computed(() => {
  const cats = selectedCategoryIds.value.join(',')
  const tgs = selectedTagIds.value.join(',')
  return `${searchKeyword.value}::${cats}::${tgs}`
})

// 重置筛选条件
const resetFilters = () => {
  searchKeyword.value = ''
  selectedCategoryIds.value = []
  selectedTagIds.value = []
}

// 快速编辑文章
const quickEditPost = (id: number) => {
  goToEditPost(id)
}

// 快速删除文章
const quickDeletePost = (id: number) => {
  deletePost(id)
}

// 快速查看文章
const quickViewPost = (id: number) => {
  viewPost(id)
}

// 返回上一页 - 已删除，使用面包屑导航替代

// 处理表格选择变化
const handleSelectionChange = (selection: BlogPost[]) => {
  selectedPosts.value = selection.map((post) => post.id)
}

// 动画效果相关
let cleanupFns: Array<() => void> = []

const initAnimations = () => {
  // 卡片进入动画
  const cards = document.querySelectorAll('.animated-card')
  cards.forEach((card, index) => {
    const el = card as HTMLElement
    el.style.animationDelay = `${index * 0.1}s`
    el.classList.add('slide-in')
  })

  // 按钮悬浮效果
  const buttons = document.querySelectorAll('.animated-btn')
  buttons.forEach((btn) => {
    const el = btn as HTMLElement
    const onMouseEnter = () => {
      el.style.transform = 'translateY(-2px) scale(1.05)'
    }
    const onMouseLeave = () => {
      el.style.transform = ''
    }
    el.addEventListener('mouseenter', onMouseEnter)
    el.addEventListener('mouseleave', onMouseLeave)
    cleanupFns.push(() => {
      el.removeEventListener('mouseenter', onMouseEnter)
      el.removeEventListener('mouseleave', onMouseLeave)
    })
  })
}

// 组件挂载时获取数据
onMounted(async () => {
  await Promise.all([fetchCategories(), fetchTags(), fetchBlogPosts()]).finally(() => {
    loading.value = false
  })
  await nextTick()
  initAnimations()
})

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn())
  cleanupFns = []
})
</script>

<template>
  <div class="post-manager-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>

    <!-- 面包屑导航 -->
    <Breadcrumb />

    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon class="title-icon"><Document /></el-icon>
        博客文章管理
      </h1>
      <p class="page-subtitle">管理您的所有博客文章内容</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ blogPosts.length }}</div>
          <div class="stat-label">总文章数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon selected-icon">
          <el-icon><Star /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ selectedPosts.length }}</div>
          <div class="stat-label">已选择</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon filter-icon">
          <el-icon><Search /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ filteredPosts.length }}</div>
          <div class="stat-label">筛选结果</div>
        </div>
      </div>
    </div>

    <!-- 筛选卡片 -->
    <el-card class="filter-card animated-card">
      <template #header>
        <div class="filter-header">
          <span class="filter-title">
            <el-icon class="header-icon"><Search /></el-icon>
            搜索与筛选
          </span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="input-wrapper">
            <label class="input-label">搜索关键词</label>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索标题或摘要..."
              clearable
              class="search-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="input-wrapper">
            <label class="input-label">分类筛选</label>
            <el-select
              v-model="selectedCategoryIds"
              placeholder="选择分类"
              clearable
              multiple
              style="width: 100%"
              class="filter-select"
            >
              <template #prefix>
                <el-icon class="input-icon"><Menu /></el-icon>
              </template>
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="input-wrapper">
            <label class="input-label">标签筛选</label>
            <el-select
              v-model="selectedTagIds"
              placeholder="选择标签"
              clearable
              multiple
              style="width: 100%"
              class="filter-select"
            >
              <template #prefix>
                <el-icon class="input-icon"><CollectionTag /></el-icon>
              </template>
              <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
            </el-select>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="input-wrapper">
            <label class="input-label">操作</label>
            <el-button @click="resetFilters" class="animated-btn reset-btn" type="info">
              <el-icon><Delete /></el-icon>重置筛选
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="management-card animated-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">
            <el-icon class="header-icon"><Document /></el-icon>
            博客文章列表
            <span class="count-badge">{{ filteredPosts.length }} 篇</span>
          </span>
          <div class="header-actions">
            <el-button
              type="danger"
              @click="deleteSelectedPosts"
              :disabled="selectedPosts.length === 0"
              class="animated-btn danger-btn"
            >
              <el-icon><Delete /></el-icon>批量删除
              <span v-if="selectedPosts.length > 0" class="action-count"
                >({{ selectedPosts.length }})</span
              >
            </el-button>
            <el-button
              type="success"
              @click="exportSelectedPosts"
              :disabled="selectedPosts.length === 0"
              class="animated-btn success-btn"
            >
              <el-icon><Download /></el-icon>批量导出
              <span v-if="selectedPosts.length > 0" class="action-count"
                >({{ selectedPosts.length }})</span
              >
            </el-button>
            <el-button type="primary" @click="goToCreatePost" class="animated-btn primary-btn">
              <el-icon><Plus /></el-icon>创建博客
            </el-button>
          </div>
        </div>
      </template>
      <transition name="fade-slide" mode="out-in">
        <el-table
          :key="filterKey"
          :data="filteredPosts"
          v-loading="loading"
          style="width: 100%"
          stripe
          class="posts-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column
            prop="title"
            align="center"
            label="标题"
            min-width="200"
            show-overflow-tooltip
          >
            <template #default="scope">
              <div class="title-cell">
                <el-tooltip :content="scope.row.title" placement="top">
                  <span class="title-text" @click="quickViewPost(scope.row.id)">
                    <el-icon class="title-icon"><View /></el-icon>{{ scope.row.title }}
                  </span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="作者" width="120" align="center" />
          <el-table-column label="分类" width="120" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.category" type="primary" class="category-tag"
                ><el-icon><Menu /></el-icon>{{ scope.row.category.name }}</el-tag
              >
              <el-tag v-else type="info" class="category-tag">未分类</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标签" width="200" align="center">
            <template #default="scope">
              <div class="tags-container">
                <el-tag
                  v-for="tag in scope.row.tags.slice(0, 3)"
                  :key="tag.id"
                  size="small"
                  class="tag-item"
                >
                  <el-icon class="tag-icon"><CollectionTag /></el-icon>{{ tag.name }}
                </el-tag>
                <span v-if="scope.row.tags.length > 3" class="more-tags">...</span>
                <span v-if="scope.row.tags.length === 0" class="no-tags">无标签</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="120" align="center">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right" align="center">
            <template #default="scope">
              <div class="action-buttons">
                <el-button
                  size="small"
                  @click="quickViewPost(scope.row.id)"
                  type="primary"
                  plain
                  class="action-button view-button animated-btn"
                >
                  <el-icon><View /></el-icon>查看
                </el-button>
                <el-button
                  size="small"
                  @click="quickEditPost(scope.row.id)"
                  type="success"
                  plain
                  class="action-button edit-button animated-btn"
                >
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-popconfirm
                  title="确定要删除这篇文章吗？"
                  @confirm="quickDeletePost(scope.row.id)"
                >
                  <template #reference>
                    <el-button
                      size="small"
                      type="danger"
                      plain
                      class="action-button delete-button animated-btn"
                    >
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </transition>
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :small="false"
          :disabled="false"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.post-manager-container {
  width: 90vw;
  max-width: 1600px;
  margin: 0 auto;
  padding: 10px 24px;
  position: relative;
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.03) 0%,
    rgba(118, 75, 162, 0.02) 25%,
    rgba(240, 147, 251, 0.03) 50%,
    rgba(248, 250, 252, 0.8) 75%,
    rgba(224, 231, 239, 0.05) 100%
  );
  min-height: 100vh;
  overflow-x: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.1), rgba(240, 147, 251, 0.1));
  animation: floatShape 20s ease-in-out infinite;
}

.shape-1 {
  width: 180px;
  height: 180px;
  top: 15%;
  left: -3%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  top: 65%;
  right: -2%;
  animation-delay: 7s;
}

.shape-3 {
  width: 90px;
  height: 90px;
  bottom: 25%;
  left: 20%;
  animation-delay: 14s;
}

@keyframes floatShape {
  0%,
  100% {
    transform: translateY(0px) translateX(0px) rotate(0deg);
    opacity: 0.3;
  }
  33% {
    transform: translateY(-25px) translateX(15px) rotate(120deg);
    opacity: 0.6;
  }
  66% {
    transform: translateY(15px) translateX(-10px) rotate(240deg);
    opacity: 0.4;
  }
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin: 30px 0 40px 0;
  animation: fadeInDown 0.8s ease-out;
}

.page-title {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  font-size: 2rem;
  color: #667eea;
  animation: rotateIcon 3s ease-in-out infinite;
}

@keyframes rotateIcon {
  0%,
  100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(180deg);
  }
}

.page-subtitle {
  font-size: 1rem;
  color: #666;
  margin: 8px 0 0 0;
  opacity: 0.8;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  animation: slideInUp 0.6s ease-out;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.2);
}

.stat-icon {
  width: 45px;
  height: 45px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 1.3rem;
}

.selected-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.filter-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 1.6rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 2px;
  letter-spacing: 1px;
}

/* 统计数字微光 */
.stat-number::after {
  content: '';
  position: absolute;
  left: -120%;
  top: 0;
  height: 100%;
  width: 120%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
  filter: blur(2px);
  transition: left 1s ease;
}

.stat-card:hover .stat-number::after {
  left: 120%;
}

/* 卡片角落光晕 */
.stat-card::before {
  content: '';
  position: absolute;
  right: -24px;
  top: -24px;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.18), transparent 60%);
  pointer-events: none;
}

.stat-label {
  font-size: 0.85rem;
  color: #666;
  opacity: 0.8;
}
.animated-card {
  animation: fadeInUp 0.7s;
  transition:
    box-shadow 0.3s,
    transform 0.3s;
}
.animated-card:hover {
  box-shadow:
    0 8px 32px rgba(66, 230, 149, 0.13),
    0 2px 12px rgba(102, 126, 234, 0.13);
  transform: translateY(-4px) scale(1.01);
}
.header-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 1.08rem;
  font-weight: 600;
}
.header-icon {
  font-size: 1.3em;
  margin-right: 0;
  color: #42a5f5;
  animation: iconPulse 2.2s infinite;
  display: flex;
  align-items: center;
}
@keyframes iconPulse {
  0%,
  100% {
    filter: drop-shadow(0 0 0 #42a5f5);
    transform: scale(1);
  }
  50% {
    filter: drop-shadow(0 0 8px #42a5f5);
    transform: scale(1.18) rotate(-8deg);
  }
}
/* 按钮样式 */
.animated-btn {
  border-radius: 12px;
  padding: 10px 16px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.animated-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.animated-btn:hover::before {
  left: 100%;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.primary-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.success-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  color: white !important;
  font-weight: 600;
}

.success-btn:hover {
  background: linear-gradient(135deg, #3d8bfe 0%, #00d4fe 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 25px rgba(79, 172, 254, 0.3);
  color: white !important;
}

.success-btn:disabled {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  color: white !important;
  opacity: 0.6;
  cursor: not-allowed;
}

.danger-btn {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  border: none;
  color: white !important;
  font-weight: 600;
}

.danger-btn:hover {
  background: linear-gradient(135deg, #f85e88 0%, #fdd835 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 25px rgba(250, 112, 154, 0.3);
  color: white !important;
}

.danger-btn:disabled {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  color: white !important;
  opacity: 0.6;
  cursor: not-allowed;
}
.title-icon {
  font-size: 1em;
  margin-right: 2px;
  color: #42a5f5;
  vertical-align: middle;
}
.tag-icon {
  font-size: 1em;
  margin-right: 2px;
  color: #42a5f5;
  vertical-align: middle;
}
.edit-button.animated-btn:hover {
  background: linear-gradient(90deg, #67c23a 0%, #42e695 100%) !important;
  color: #fff !important;
  border-color: #42e695 !important;
}
.delete-button.animated-btn:hover {
  background: linear-gradient(90deg, #ff6b6b 0%, #ee5a52 100%) !important;
  color: #fff !important;
  border-color: #ee5a52 !important;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.header-top h1 {
  margin: 0;
}

/* 返回按钮样式已删除 */

.header p {
  font-size: 1.2rem;
  color: #666;
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 30px;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 6px 30px rgba(102, 126, 234, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.filter-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.2);
}

.filter-card :deep(.el-card__header) {
  background: rgba(248, 250, 252, 0.8);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  padding: 20px 24px;
}

.filter-header {
  display: flex;
  align-items: center;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.input-wrapper {
  margin-bottom: 10px;
}

.input-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #555;
  margin-bottom: 8px;
}

.search-input,
.filter-select {
  border-radius: 12px;
}

/* 输入与选择框增强：悬浮/聚焦高亮与上浮 */
.search-input :deep(.el-input__wrapper),
.filter-select :deep(.el-input__wrapper) {
  transition:
    box-shadow 0.25s ease,
    transform 0.2s ease;
  background: rgba(255, 255, 255, 0.95);
}
.search-input:hover :deep(.el-input__wrapper),
.filter-select:hover :deep(.el-input__wrapper) {
  transform: translateY(-1px);
  box-shadow: 0 0 0 2px rgba(163, 191, 250, 0.8);
}
.input-wrapper:focus-within .search-input :deep(.el-input__wrapper),
.input-wrapper:focus-within .filter-select :deep(.el-input__wrapper) {
  transform: translateY(-1px) scale(1.01);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.25);
}

.input-icon {
  color: #667eea;
  transition: color 0.25s ease;
}

.input-wrapper:focus-within .input-icon {
  color: #5a6fd8;
}

.reset-btn {
  width: 100%;
  border-radius: 12px;
  background: linear-gradient(135deg, #94a3b8 0%, #4b5563 100%);
  border: none;
  color: white;
  box-shadow: 0 6px 18px rgba(100, 116, 139, 0.25);
}

.reset-btn:hover {
  background: linear-gradient(135deg, #9aa6b2 0%, #475569 100%);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 10px 26px rgba(100, 116, 139, 0.35);
}

/* 管理卡片 */
.management-card {
  margin-bottom: 30px;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 6px 30px rgba(102, 126, 234, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.management-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.2);
}

.management-card :deep(.el-card__header) {
  background: rgba(248, 250, 252, 0.8);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  padding: 20px 24px;
  position: relative;
  overflow: hidden;
}

/* Header 渐变扫光 */
.management-card :deep(.el-card__header)::after {
  content: '';
  position: absolute;
  left: -30%;
  top: 0;
  width: 55%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.12), transparent);
  transform: skewX(-18deg);
  filter: blur(1px);
  animation: headerSheen 6s infinite;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
}

.count-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  margin-left: 8px;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-count {
  background: rgba(255, 255, 255, 0.3);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 0.75rem;
  margin-left: 4px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 表格样式 */
.posts-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
}

/* 表格淡入淡出+轻微上移动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition:
    opacity 0.28s ease,
    transform 0.28s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.posts-table :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  color: #334155;
  font-weight: 700;
  border: none;
  padding: 16px 12px;
}

.posts-table :deep(.el-table__row) {
  transition: all 0.3s ease;
  animation: rowFadeIn 0.32s ease-out both;
  will-change: opacity, transform;
}

.posts-table :deep(.el-table__row:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(240, 147, 251, 0.05) 100%);
  transform: scale(1.01);
}

.posts-table :deep(.el-table__cell) {
  padding: 12px;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

/* 行级渐进显现延迟（前10行） */
.posts-table :deep(.el-table__row:nth-child(1)) {
  animation-delay: 30ms;
}
.posts-table :deep(.el-table__row:nth-child(2)) {
  animation-delay: 60ms;
}
.posts-table :deep(.el-table__row:nth-child(3)) {
  animation-delay: 90ms;
}
.posts-table :deep(.el-table__row:nth-child(4)) {
  animation-delay: 120ms;
}
.posts-table :deep(.el-table__row:nth-child(5)) {
  animation-delay: 150ms;
}
.posts-table :deep(.el-table__row:nth-child(6)) {
  animation-delay: 180ms;
}
.posts-table :deep(.el-table__row:nth-child(7)) {
  animation-delay: 210ms;
}
.posts-table :deep(.el-table__row:nth-child(8)) {
  animation-delay: 240ms;
}
.posts-table :deep(.el-table__row:nth-child(9)) {
  animation-delay: 270ms;
}
.posts-table :deep(.el-table__row:nth-child(10)) {
  animation-delay: 300ms;
}

@keyframes rowFadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 图标动画 */
.header-icon {
  font-size: 1.4rem;
  color: #667eea;
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
    filter: drop-shadow(0 0 5px rgba(102, 126, 234, 0.3));
  }
  50% {
    transform: translateY(-3px) rotate(5deg);
    filter: drop-shadow(0 0 10px rgba(102, 126, 234, 0.5));
  }
}

@keyframes headerSheen {
  0% {
    left: -30%;
  }
  50% {
    left: 130%;
  }
  100% {
    left: 130%;
  }
}

/* 动画关键帧 */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-in {
  animation: fadeInUp 0.6s ease-out forwards;
}

.posts-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.posts-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.title-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-text {
  font-weight: 600;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  position: relative;
  transition: color 0.2s ease;
}

.title-text:hover {
  color: #5aa9ff;
}

/* 标题下划线滑入效果 */
.title-text::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  height: 2px;
  width: 0;
  background: linear-gradient(90deg, #67b8ff, #8ec5ff);
  transition: width 0.25s ease;
}
.title-text:hover::after {
  width: 100%;
}
.title-text .title-icon {
  transition: transform 0.25s ease;
}
.title-text:hover .title-icon {
  transform: rotate(-10deg) scale(1.05);
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 4px;
  max-height: 60px;
  overflow: hidden;
}

.tag-item {
  margin: 0;
}

.category-tag {
  margin: 0;
}

.more-tags {
  color: #909399;
  font-size: 12px;
}

.no-tags {
  color: #909399;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-button :deep(.el-button__icon) {
  color: #409eff;
}

.edit-button :deep(.el-button__icon) {
  color: #67c23a;
}

.delete-button :deep(.el-button__icon) {
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-card .el-row {
    flex-direction: column;
    gap: 10px;
  }

  .filter-card .el-col {
    width: 100%;
  }

  .action-buttons {
    justify-content: center;
  }

  .header-top {
    justify-content: center;
  }

  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .back-button {
    position: static;
    margin-right: auto;
  }

  .tags-container {
    justify-content: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: center;
    flex-direction: column;
    gap: 10px;
  }

  .header-actions .el-button {
    width: 100%;
  }

  .page-title {
    font-size: 1.8rem;
    flex-direction: column;
    gap: 8px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .floating-shape {
    display: none;
  }
}

/* 分页样式增强 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
}

.pagination-container :deep(.el-pagination) {
  --el-pagination-button-color: #667eea;
  --el-pagination-hover-color: #5a6fd8;
}
</style>
