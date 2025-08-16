<!--
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-04 16:12:07
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-08-12 10:56:51
 * @FilePath: \blog\src\views\Categories.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElCard, ElTag, ElPagination, ElSkeleton, ElEmpty } from 'element-plus'
import axios from '@/utils/axios'
import Breadcrumb from '@/components/Breadcrumb.vue'

// 定义组件名称
defineOptions({
  name: 'CategoriesView',
})

// 定义分类类型
interface Category {
  id: number
  name: string
  createdAt: string
}

// 定义博客文章类型
interface BlogPost {
  id: number
  title: string
  desc: string
  author: string
  createdAt: string
  category: Category
  tags: { id: number; name: string }[]
}

// 响应式数据
const categories = ref<Category[]>([])
const selectedCategory = ref<Category | null>(null)
const blogPosts = ref<BlogPost[]>([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
const router = useRouter()

// 获取所有分类
const fetchCategories = async () => {
  try {
    const response = await axios.get('/categories')
    categories.value = response.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 根据分类获取博客文章列表
const fetchBlogPostsByCategory = async (categoryId: number, page = 1) => {
  try {
    loading.value = true
    const response = await axios.get(`/api/categories/${categoryId}/posts`, {
      params: {
        page: page,
        limit: pageSize.value,
      },
    })

    blogPosts.value = response.data.posts
    total.value = response.data.pagination.totalPosts
    currentPage.value = response.data.pagination.currentPage
  } catch (error) {
    console.error('获取博客文章失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择分类
const selectCategory = (category: Category) => {
  selectedCategory.value = category
  fetchBlogPostsByCategory(category.id)
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 页面改变时重新获取数据
const handlePageChange = (page: number) => {
  if (selectedCategory.value) {
    fetchBlogPostsByCategory(selectedCategory.value.id, page)
  }
}

// 跳转到博客详情页
const goToBlogDetail = (id: number) => {
  router.push(`/blog/${id}`)
}

// 返回上一页 - 已删除，使用面包屑导航替代
// 组件挂载时获取数据
onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <div class="categories-container">
    <!-- 面包屑导航 -->
    <Breadcrumb />

    <div class="header">
      <div class="header-top">
        <h1>博客分类</h1>
      </div>
      <p>浏览不同分类下的博客文章</p>
    </div>

    <div class="content">
      <!-- 分类列表 -->
      <div class="categories-list">
        <el-card
          v-for="category in categories"
          :key="category.id"
          class="category-card"
          :class="{ active: selectedCategory?.id === category.id }"
          @click="selectCategory(category)"
        >
          <div class="category-content">
            <h3>{{ category.name }}</h3>
            <p>创建时间: {{ formatDate(category.createdAt) }}</p>
          </div>
        </el-card>
      </div>

      <!-- 博客文章列表 -->
      <div class="blog-section" v-if="selectedCategory">
        <h2>{{ selectedCategory.name }} 分类下的文章</h2>

        <div class="blog-list" v-if="!loading && blogPosts.length > 0">
          <el-card
            v-for="post in blogPosts"
            :key="post.id"
            class="blog-card"
            shadow="hover"
            @click="goToBlogDetail(post.id)"
          >
            <template #header>
              <div class="blog-header">
                <h3 class="blog-title">{{ post.title }}</h3>
                <div class="blog-meta">
                  <span class="author">作者: {{ post.author }}</span>
                  <span class="date">发布时间: {{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
            </template>

            <div class="blog-excerpt">
              <p>{{ post.desc }}</p>
            </div>

            <div class="blog-footer">
              <el-tag
                v-for="tag in post.tags"
                :key="tag.id"
                class="tag"
                type="primary"
                effect="plain"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </el-card>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              :small="false"
              :disabled="false"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <!-- 空状态-->
        <div class="blog-list empty" v-else-if="!loading && blogPosts.length === 0">
          <el-empty description="该分类下暂无文章" />
        </div>

        <!-- 加载状态-->
        <div class="blog-list" v-else>
          <el-card v-for="n in pageSize" :key="n" class="blog-card" shadow="hover">
            <template #header>
              <el-skeleton :rows="0" animated>
                <template #template>
                  <el-skeleton-item variant="h3" style="width: 50%" />
                  <div>
                    <el-skeleton-item variant="text" style="width: 30%" />
                    <el-skeleton-item variant="text" style="width: 30%; margin-left: 20px" />
                  </div>
                </template>
              </el-skeleton>
            </template>

            <el-skeleton :rows="3" animated />
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.categories-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
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

/* 返回按钮样式已删除*/

.header p {
  font-size: 1.2rem;
  color: #666;
}

.content {
  display: flex;
  gap: 30px;
}

.categories-list {
  flex: 0 0 300px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.category-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.category-card.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.category-content h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.category-content p {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
}

.blog-section {
  flex: 1;
}

.blog-section h2 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.blog-list {
  margin-bottom: 30px;
}

.blog-card {
  margin-bottom: 20px;
  cursor: pointer;
}

.blog-card:hover {
  border-color: #409eff;
}

.blog-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.blog-title {
  margin: 0;
  font-size: 1.3rem;
  color: #333;
  cursor: pointer;
}

.blog-title:hover {
  color: #409eff;
}

.blog-meta {
  display: flex;
  gap: 20px;
}

.blog-meta span {
  font-size: 0.9rem;
  color: #666;
}

.blog-excerpt {
  margin: 15px 0;
  line-height: 1.6;
  color: #444;
}

.blog-excerpt p {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.blog-footer {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tag {
  margin-right: 5px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.empty {
  text-align: center;
  padding: 50px 0;
}

/* 响应式设置*/
@media (max-width: 768px) {
  .content {
    flex-direction: column;
  }

  .categories-list {
    flex: 0 0 auto;
  }

  .blog-meta {
    flex-direction: column;
    gap: 5px;
  }

  .header-top {
    justify-content: center;
  }

  /* 返回按钮样式已删除*/
}
</style>
