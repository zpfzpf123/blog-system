<script setup lang="ts">
/* eslint-disable @typescript-eslint/no-explicit-any */
import { ref, onMounted, nextTick, onUnmounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard,
  ElTable,
  ElTableColumn,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElPopconfirm,
  ElDialog,
  ElIcon,
  ElPagination,
} from 'element-plus'
import {
  Edit,
  Delete,
  Plus,
  CollectionTag,
  Menu,
  Document,
  Star,
  Setting,
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import Breadcrumb from '@/components/Breadcrumb.vue'

// 定义分类类型
interface Category {
  id: number
  name: string
  createdAt: string
}

// 定义标签类型
interface Tag {
  id: number
  name: string
}

// 定义分页信息类型
interface PaginationInfo {
  currentPage: number
  totalPages: number
  totalPosts: number
  hasNext: boolean
  hasPrev: boolean
}

// 定义分类响应类型
interface CategoriesResponse {
  categories: Category[]
  pagination: PaginationInfo
}

// 定义标签响应类型
interface TagsResponse {
  tags: Tag[]
  pagination: PaginationInfo
}

// 响应式数据
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(true)
const router = useRouter()

// 分类多选相关数据
const selectedCategories = ref<Category[]>([])
const categorySelectionLoading = ref(false)

// 标签多选相关数据
const selectedTags = ref<Tag[]>([])
const tagSelectionLoading = ref(false)

// 批量添加标签相关数据
const batchAddTagInput = ref('')
const batchAddTagLoading = ref(false)

// 批量添加分类相关数据
const batchAddCategoryInput = ref('')
const batchAddCategoryLoading = ref(false)

// 分类分页相关数据
const categoryPage = ref(1)
const categoryPageSize = ref(10)
const categoryTotal = ref(0)
const categoryTotalPages = ref(0)

// 标签分页相关数据
const tagPage = ref(1)
const tagPageSize = ref(10)
const tagTotal = ref(0)
const tagTotalPages = ref(0)

// 分类表单相关
const categoryForm = ref({
  id: 0,
  name: '',
})
const categoryDialogVisible = ref(false)
const categoryDialogTitle = ref('')

// 标签表单相关
const tagForm = ref({
  id: 0,
  name: '',
})
const tagDialogVisible = ref(false)
const tagDialogTitle = ref('')

// 搜索相关
const categorySearch = ref('')
const tagSearch = ref('')

// 获取所有分类（带分页）
const fetchCategories = async (
  page: number = categoryPage.value,
  size: number = categoryPageSize.value,
) => {
  try {
    loading.value = true
    const response = await axios.get<CategoriesResponse>('/api/categories/page', {
      params: {
        page: page,
        size: size,
      },
    })

    categories.value = response.data.categories
    categoryTotal.value = response.data.pagination.totalPosts
    categoryTotalPages.value = response.data.pagination.totalPages
    categoryPage.value = response.data.pagination.currentPage
  } catch (error) {
    ElMessage.error('获取分类失败')
    console.error('获取分类失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取所有标签（带分页）
const fetchTags = async (page: number = tagPage.value, size: number = tagPageSize.value) => {
  try {
    loading.value = true
    const response = await axios.get<TagsResponse>('/api/tags/page', {
      params: {
        page: page,
        size: size,
      },
    })
    tags.value = response.data.tags
    tagTotal.value = response.data.pagination.totalPosts
    tagTotalPages.value = response.data.pagination.totalPages
    tagPage.value = response.data.pagination.currentPage
  } catch (error) {
    ElMessage.error('获取标签失败')
    console.error('获取标签失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索分类（带分页）
const searchCategories = async (
  page: number = categoryPage.value,
  size: number = categoryPageSize.value,
) => {
  loading.value = true
  try {
    if (!categorySearch.value) {
      await fetchCategories(page, size)
    } else {
      const response = await axios.get<CategoriesResponse>('/api/categories/search/page', {
        params: {
          name: categorySearch.value,
          page: page,
          size: size,
        },
      })

      categories.value = response.data.categories
      categoryTotal.value = response.data.pagination.totalPosts
      categoryTotalPages.value = response.data.pagination.totalPages
      categoryPage.value = response.data.pagination.currentPage
    }
  } catch (error) {
    ElMessage.error('搜索分类失败')
    console.error('搜索分类失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索标签（带分页）
const searchTags = async (page: number = tagPage.value, size: number = tagPageSize.value) => {
  loading.value = true
  try {
    if (!tagSearch.value) {
      await fetchTags(page, size)
    } else {
      const response = await axios.get<TagsResponse>('/api/tags/search/page', {
        params: {
          name: tagSearch.value,
          page: page,
          size: size,
        },
      })
      tags.value = response.data.tags
      tagTotal.value = response.data.pagination.totalPosts
      tagTotalPages.value = response.data.pagination.totalPages
      tagPage.value = response.data.pagination.currentPage
    }
  } catch (error) {
    ElMessage.error('搜索标签失败')
    console.error('搜索标签失败:', error)
  } finally {
    loading.value = false
  }
}

// 显示添加分类对话框
const showAddCategory = () => {
  categoryForm.value = { id: 0, name: '' }
  categoryDialogTitle.value = '添加分类'
  categoryDialogVisible.value = true
}

// 显示编辑分类对话框
const showEditCategory = (category: Category) => {
  categoryForm.value = { ...category }
  categoryDialogTitle.value = '编辑分类'
  categoryDialogVisible.value = true
}

// 显示添加标签对话框
const showAddTag = () => {
  tagForm.value = { id: 0, name: '' }
  tagDialogTitle.value = '添加标签'
  tagDialogVisible.value = true
}

// 显示编辑标签对话框
const showEditTag = (tag: Tag) => {
  tagForm.value = { ...tag }
  tagDialogTitle.value = '编辑标签'
  tagDialogVisible.value = true
}

// 保存分类
const saveCategory = async () => {
  if (!categoryForm.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    if (categoryForm.value.id === 0) {
      // 添加分类
      await axios.post('/api/categories', {
        name: categoryForm.value.name,
      })
      ElMessage.success('分类添加成功')
    } else {
      // 更新分类
      await axios.put(`/api/categories/${categoryForm.value.id}`, {
        name: categoryForm.value.name,
      })
      ElMessage.success('分类更新成功')
    }

    categoryDialogVisible.value = false
    fetchCategories()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
    console.error('保存分类失败:', error)
  }
}

// 保存标签
const saveTag = async () => {
  if (!tagForm.value.name) {
    ElMessage.warning('请输入标签名称')
    return
  }

  try {
    if (tagForm.value.id === 0) {
      // 添加标签
      await axios.post('/api/tags', {
        name: tagForm.value.name,
      })
      ElMessage.success('标签添加成功')
    } else {
      // 更新标签
      await axios.put(`/api/tags/${tagForm.value.id}`, {
        name: tagForm.value.name,
      })
      ElMessage.success('标签更新成功')
    }

    tagDialogVisible.value = false
    fetchTags()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
    console.error('保存标签失败:', error)
  }
}

// 删除分类
const deleteCategory = async (id: number) => {
  try {
    await axios.delete(`/api/categories/${id}`)
    ElMessage.success('分类删除成功')
    fetchCategories()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
    console.error('删除分类失败:', error)
  }
}

// 删除标签
const deleteTag = async (id: number) => {
  try {
    await axios.delete(`/api/tags/${id}`)
    ElMessage.success('标签删除成功')
    fetchTags()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
    console.error('删除标签失败:', error)
  }
}

// 批量删除标签
const batchDeleteTags = async () => {
  if (selectedTags.value.length === 0) {
    ElMessage.warning('请先选择要删除的标签')
    return
  }

  try {
    // 显示确认框
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedTags.value.length} 个标签吗？此操作不可恢复！`,
      '确认批量删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false,
      },
    )

    tagSelectionLoading.value = true
    const tagIds = selectedTags.value.map((tag) => tag.id)

    // 调用批量删除API
    await axios.delete('/api/tags/batch', {
      data: { ids: tagIds },
    })

    ElMessage.success(`成功删除 ${selectedTags.value.length} 个标签`)
    selectedTags.value = []
    fetchTags()
  } catch (error: any) {
    if (error === 'cancel') {
      // 用户取消删除
      return
    }
    ElMessage.error(error.response?.data?.message || '批量删除失败')
    console.error('批量删除标签失败:', error)
  } finally {
    tagSelectionLoading.value = false
  }
}

// 批量添加标签
const batchAddTags = async () => {
  if (!batchAddTagInput.value.trim()) {
    ElMessage.warning('请输入要添加的标签名称')
    return
  }

  try {
    batchAddTagLoading.value = true

    // 解析输入的标签名称，支持逗号、分号、换行符分隔
    const tagNames = batchAddTagInput.value
      .split(/[,，;\n]/)
      .map((name) => name.trim())
      .filter((name) => name.length > 0)

    if (tagNames.length === 0) {
      ElMessage.warning('请输入有效的标签名称')
      return
    }

    // 调用批量添加API
    const response = await axios.post('/api/tags/batch', {
      names: tagNames,
    })

    ElMessage.success(`成功添加 ${response.data.createdCount} 个标签`)
    batchAddTagInput.value = ''
    fetchTags()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '批量添加失败')
    console.error('批量添加标签失败:', error)
  } finally {
    batchAddTagLoading.value = false
  }
}

// 批量添加分类
const batchAddCategories = async () => {
  if (!batchAddCategoryInput.value.trim()) {
    ElMessage.warning('请输入要添加的分类名称')
    return
  }

  try {
    batchAddCategoryLoading.value = true

    // 解析输入的分类名称，支持逗号、分号、换行符分隔
    const categoryNames = batchAddCategoryInput.value
      .split(/[,，;\n]/)
      .map((name) => name.trim())
      .filter((name) => name.length > 0)

    if (categoryNames.length === 0) {
      ElMessage.warning('请输入有效的分类名称')
      return
    }

    // 调用批量添加API
    const response = await axios.post('/api/categories/batch', {
      names: categoryNames,
    })

    ElMessage.success(`成功添加 ${response.data.createdCount} 个分类`)
    batchAddCategoryInput.value = ''
    fetchCategories()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '批量添加失败')
    console.error('批量添加分类失败:', error)
  } finally {
    batchAddCategoryLoading.value = false
  }
}

// 批量删除分类
const batchDeleteCategories = async () => {
  if (selectedCategories.value.length === 0) {
    ElMessage.warning('请先选择要删除的分类')
    return
  }

  try {
    // 显示确认框
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCategories.value.length} 个分类吗？此操作不可恢复！`,
      '确认批量删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false,
      },
    )

    categorySelectionLoading.value = true
    const categoryIds = selectedCategories.value.map((cat) => cat.id)

    // 调用批量删除API
    await axios.delete('/api/categories/batch', {
      data: { ids: categoryIds },
    })

    ElMessage.success(`成功删除 ${selectedCategories.value.length} 个分类`)
    selectedCategories.value = []
    fetchCategories()
  } catch (error: any) {
    if (error === 'cancel') {
      // 用户取消删除
      return
    }
    ElMessage.error(error.response?.data?.message || '批量删除失败')
    console.error('批量删除分类失败:', error)
  } finally {
    categorySelectionLoading.value = false
  }
}

// 处理分类选择变化
const handleCategorySelectionChange = (selection: Category[]) => {
  selectedCategories.value = selection
}

// 处理标签选择变化
const handleTagSelectionChange = (selection: Tag[]) => {
  selectedTags.value = selection
}

// 跳转到博客文章管理页面
const goToPostManager = () => {
  router.push('/admin/posts')
}

// 返回上一页 - 已删除，使用面包屑导航替代

// 动画效果相关
let cleanupFns: Array<() => void> = []
let runningRafIds: number[] = []

// 统计数字动画相关
const displayTotal = ref(0)
const displayCategory = ref(0)
const displayTag = ref(0)

const totalCount = computed(() => (categories.value?.length || 0) + (tags.value?.length || 0))
const categoryCount = computed(() => categories.value?.length || 0)
const tagCount = computed(() => tags.value?.length || 0)

const cancelAllRafs = () => {
  runningRafIds.forEach((id) => cancelAnimationFrame(id))
  runningRafIds = []
}

const animateNumber = (
  from: number,
  to: number,
  targetRef: typeof displayTotal,
  duration = 800,
) => {
  const start = performance.now()
  const diff = to - from
  const easeOutCubic = (t: number) => 1 - Math.pow(1 - t, 3)
  const tick = (now: number) => {
    const elapsed = now - start
    const p = Math.min(1, elapsed / duration)
    const eased = easeOutCubic(p)
    targetRef.value = Math.round(from + diff * eased)
    if (p < 1) {
      const id = requestAnimationFrame(tick)
      runningRafIds.push(id)
    }
  }
  const id = requestAnimationFrame(tick)
  runningRafIds.push(id)
}

const runStatsAnimation = () => {
  cancelAllRafs()
  animateNumber(displayTotal.value, totalCount.value, displayTotal, 900)
  animateNumber(displayCategory.value, categoryCount.value, displayCategory, 900)
  animateNumber(displayTag.value, tagCount.value, displayTag, 900)
}

const initAnimations = () => {
  // 卡片进入动画
  const cards = document.querySelectorAll('.management-card')
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

const setupStatCardInteractions = () => {
  const cards = Array.from(document.querySelectorAll('.stat-card')) as HTMLElement[]
  cards.forEach((card) => {
    card.style.transformStyle = 'preserve-3d'
    const onMouseMove = (e: MouseEvent) => {
      const rect = card.getBoundingClientRect()
      const cx = rect.left + rect.width / 2
      const cy = rect.top + rect.height / 2
      const dx = (e.clientX - cx) / rect.width
      const dy = (e.clientY - cy) / rect.height
      const rotateX = dy * -8
      const rotateY = dx * 8
      card.style.transform = `perspective(800px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`
    }
    const onMouseLeave = () => {
      card.style.transform = ''
    }
    card.addEventListener('mousemove', onMouseMove)
    card.addEventListener('mouseleave', onMouseLeave)
    cleanupFns.push(() => {
      card.removeEventListener('mousemove', onMouseMove)
      card.removeEventListener('mouseleave', onMouseLeave)
    })
  })
}

// 通用点击涟漪效果（按钮/添加区域按钮）
const setupRipple = () => {
  const rippleTargets = document.querySelectorAll('.animated-btn, .batch-add-content .el-button')
  rippleTargets.forEach((btn) => {
    const el = btn as HTMLElement
    const onClick = (e: MouseEvent) => {
      const ripple = document.createElement('span')
      ripple.className = 'ripple'
      const rect = el.getBoundingClientRect()
      const size = Math.max(rect.width, rect.height)
      ripple.style.width = ripple.style.height = size + 'px'
      ripple.style.left = e.clientX - rect.left - size / 2 + 'px'
      ripple.style.top = e.clientY - rect.top - size / 2 + 'px'
      el.appendChild(ripple)
      setTimeout(() => ripple.remove(), 600)
    }
    el.addEventListener('click', onClick)
    cleanupFns.push(() => el.removeEventListener('click', onClick))
  })
}

// 分类页面改变
const handleCategoryPageChange = (page: number) => {
  if (categorySearch.value) {
    searchCategories(page)
  } else {
    fetchCategories(page)
  }
}

// 分类页面大小改变
const handleCategoryPageSizeChange = (size: number) => {
  categoryPageSize.value = size
  if (categorySearch.value) {
    searchCategories(1, size)
  } else {
    fetchCategories(1, size)
  }
}

// 标签页面改变
const handleTagPageChange = (page: number) => {
  if (tagSearch.value) {
    searchTags(page)
  } else {
    fetchTags(page)
  }
}

// 标签页面大小改变
const handleTagPageSizeChange = (size: number) => {
  tagPageSize.value = size
  if (tagSearch.value) {
    searchTags(1, size)
  } else {
    fetchTags(1, size)
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  await Promise.all([fetchCategories(), fetchTags()]).finally(() => {
    loading.value = false
  })
  await nextTick()
  initAnimations()
  setupStatCardInteractions()
  runStatsAnimation()
  setupRipple()
})

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn())
  cleanupFns = []
  cancelAllRafs()
})

// 监听路由变化，当用户从其他页面返回时重新获取数据
router.afterEach((to) => {
  if (to.path === '/admin') {
    fetchTags()
    fetchCategories()
  }
})

// 当分类或标签数量变化时，播放数字滚动动画
watch([categories, tags], () => {
  runStatsAnimation()
})
</script>

<template>
  <div class="admin-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>

    <!-- 面包屑导航 -->
    <Breadcrumb />

    <!-- 管理面板标题 -->
    <div class="admin-header">
      <h1 class="admin-title">
        <el-icon class="title-icon"><Setting /></el-icon>
        管理控制台
      </h1>
      <p class="admin-subtitle">管理您的博客内容、分类和标签</p>
    </div>

    <!-- 快速统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number count-up">{{ displayTotal }}</div>
          <div class="stat-label">总项目数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon category-icon">
          <el-icon><Menu /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number count-up">{{ displayCategory }}</div>
          <div class="stat-label">分类数量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon tag-icon">
          <el-icon><CollectionTag /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number count-up">{{ displayTag }}</div>
          <div class="stat-label">标签数量</div>
        </div>
      </div>
    </div>

    <!-- 博客文章管理入口 -->
    <el-card class="management-card animated-card featured-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="header-icon"><Document /></el-icon>
            博客文章管理
            <el-icon class="star-icon"><Star /></el-icon>
          </span>
          <el-button
            type="primary"
            @click="goToPostManager"
            class="animated-btn primary-btn hero-cta"
          >
            <el-icon><Edit /></el-icon>管理博客文章
          </el-button>
        </div>
      </template>
      <div class="card-content">
        <p class="card-description">创建、编辑、删除博客文章，管理您的所有内容</p>
        <div class="feature-tags">
          <span class="feature-tag">内容管理</span>
          <span class="feature-tag">批量操作</span>
          <span class="feature-tag">搜索筛选</span>
        </div>
      </div>
    </el-card>

    <!-- 分类管理 -->
    <el-card class="management-card animated-card category-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="header-icon category-icon"><Menu /></el-icon>
            分类管理
            <span class="count-chip" v-if="categoryTotal >= 0">总数 {{ categoryTotal }}</span>
          </span>
          <div style="display: flex; gap: 8px; align-items: center">
            <el-input
              v-model="categorySearch"
              placeholder="搜索分类名称"
              clearable
              style="width: 180px"
              @keyup.enter="searchCategories"
            />
            <el-button @click="() => searchCategories()" type="primary">搜索</el-button>
            <el-button
              @click="
                () => {
                  categorySearch = ''
                  fetchCategories()
                }
              "
              type="info"
              plain
              >重置</el-button
            >
            <el-button
              type="danger"
              :loading="categorySelectionLoading"
              :disabled="selectedCategories.length === 0"
              @click="batchDeleteCategories"
              class="animated-btn danger-btn"
            >
              <el-icon><Delete /></el-icon>
              批量删除 {{ selectedCategories.length > 0 ? `(${selectedCategories.length})` : '' }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 批量添加分类区域 -->
      <div class="batch-add-container">
        <div class="batch-add-header">
          <span class="batch-add-title">批量添加分类</span>
          <span class="batch-add-tip">支持逗号、分号、换行符分隔，如：111,222,333</span>
        </div>
        <div class="batch-add-content">
          <el-input
            v-model="batchAddCategoryInput"
            type="textarea"
            :rows="3"
            placeholder="请输入分类名称，多个分类用逗号、分号或换行符分隔"
            clearable
          />
          <el-button
            type="success"
            :loading="batchAddCategoryLoading"
            :disabled="!batchAddCategoryInput.trim()"
            @click="batchAddCategories"
            class="animated-btn success-btn"
          >
            <el-icon><Plus /></el-icon>
            批量添加
          </el-button>
        </div>
      </div>

      <div class="card-content">
        <p class="card-description">管理博客分类，组织您的内容结构</p>
      </div>

      <el-table
        :data="categories"
        v-loading="loading"
        style="width: 100%; overflow-x: hi"
        class="management-table"
        :max-height="400"
        @selection-change="handleCategorySelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="分类名称" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                size="small"
                @click="showEditCategory(scope.row)"
                type="success"
                plain
                class="action-button edit-button animated-btn"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-popconfirm title="确定要删除这个分类吗？" @confirm="deleteCategory(scope.row.id)">
                <template #reference>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    class="action-button delete-button animated-btn"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分类分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="categoryPage"
          v-model:page-size="categoryPageSize"
          :page-sizes="[5, 10, 20, 50]"
          :small="false"
          :disabled="false"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="categoryTotal"
          @current-change="handleCategoryPageChange"
          @size-change="handleCategoryPageSizeChange"
        />
      </div>
    </el-card>

    <!-- 标签管理 -->
    <el-card class="management-card animated-card tag-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="header-icon tag-icon"><CollectionTag /></el-icon>
            标签管理
            <span class="count-chip" v-if="tagTotal >= 0">总数 {{ tagTotal }}</span>
          </span>
          <div style="display: flex; gap: 8px; align-items: center">
            <el-input
              v-model="tagSearch"
              placeholder="搜索标签名称"
              clearable
              style="width: 180px"
              @keyup.enter="searchTags"
            />
            <el-button @click="() => searchTags()" type="primary">搜索</el-button>
            <el-button
              @click="
                () => {
                  tagSearch = ''
                  fetchTags()
                }
              "
              type="info"
              plain
              >重置</el-button
            >
            <el-button
              type="danger"
              :loading="tagSelectionLoading"
              :disabled="selectedTags.length === 0"
              @click="batchDeleteTags"
              class="animated-btn danger-btn"
            >
              <el-icon><Delete /></el-icon>
              批量删除 {{ selectedTags.length > 0 ? `(${selectedTags.length})` : '' }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 批量添加标签区域 -->
      <div class="batch-add-container">
        <div class="batch-add-header">
          <span class="batch-add-title">批量添加标签</span>
          <span class="batch-add-tip">支持逗号、分号、换行符分隔，如：111,222,333</span>
        </div>
        <div class="batch-add-content">
          <el-input
            v-model="batchAddTagInput"
            type="textarea"
            :rows="3"
            placeholder="请输入标签名称，多个标签用逗号、分号或换行符分隔"
            clearable
          />
          <el-button
            type="success"
            :loading="batchAddTagLoading"
            :disabled="!batchAddTagInput.trim()"
            @click="batchAddTags"
            class="animated-btn success-btn"
          >
            <el-icon><Plus /></el-icon>
            批量添加
          </el-button>
        </div>
      </div>
      <div class="card-content">
        <p class="card-description">创建和管理标签，为内容添加标识</p>
      </div>

      <el-table
        class="management-table"
        :max-height="400"
        :data="tags"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleTagSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="标签名称" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                size="small"
                @click="showEditTag(scope.row)"
                type="success"
                plain
                class="action-button edit-button animated-btn"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-popconfirm title="确定要删除这个标签吗？" @confirm="deleteTag(scope.row.id)">
                <template #reference>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    class="action-button delete-button animated-btn"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 标签分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="tagPage"
          v-model:page-size="tagPageSize"
          :page-sizes="[5, 10, 20, 50]"
          :small="false"
          :disabled="false"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="tagTotal"
          @current-change="handleTagPageChange"
          @size-change="handleTagPageSizeChange"
        />
      </div>
    </el-card>

    <!-- 分类编辑对话框 -->
    <el-dialog v-model="categoryDialogVisible" :title="categoryDialogTitle" width="500px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCategory">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 标签编辑对话框 -->
    <el-dialog v-model="tagDialogVisible" :title="tagDialogTitle" width="500px">
      <el-form :model="tagForm" label-width="80px">
        <el-form-item label="标签名称">
          <el-input v-model="tagForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="tagDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTag">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-container {
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
  width: 200px;
  height: 200px;
  top: 10%;
  left: -5%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: -3%;
  animation-delay: 7s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 15%;
  animation-delay: 14s;
}

@keyframes floatShape {
  0%,
  100% {
    transform: translateY(0px) translateX(0px) rotate(0deg);
    opacity: 0.3;
  }
  33% {
    transform: translateY(-30px) translateX(20px) rotate(120deg);
    opacity: 0.6;
  }
  66% {
    transform: translateY(20px) translateX(-15px) rotate(240deg);
    opacity: 0.4;
  }
}

/* 管理面板标题 */
.admin-header {
  text-align: center;
  margin: 30px 0 40px 0;
  animation: fadeInDown 0.8s ease-out;
}

.admin-title {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.title-icon {
  font-size: 2.2rem;
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

.admin-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 10px 0 0 0;
  opacity: 0.8;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  animation: slideInUp 0.6s ease-out;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.2);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 1.5rem;
}

.category-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.tag-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
  letter-spacing: 1px;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  opacity: 0.8;
}

/* 数字滚动微光效果 */
.count-up {
  position: relative;
}
.count-up::after {
  content: '';
  position: absolute;
  left: -120%;
  top: 0;
  height: 100%;
  width: 120%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.25), transparent);
  filter: blur(2px);
  transition: left 1s ease;
}
.stat-card:hover .count-up::after {
  left: 120%;
}

/* 卡片角落光晕 */
.stat-card::before {
  content: '';
  position: absolute;
  right: -30px;
  top: -30px;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.18), transparent 60%);
  pointer-events: none;
}

/* 管理卡片样式 */
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
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.2);
}

.featured-card {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(240, 147, 251, 0.05) 100%);
  border: 2px solid rgba(102, 126, 234, 0.2);
}

.category-card {
  border-left: 4px solid #4facfe;
}

.tag-card {
  border-left: 4px solid #fa709a;
}

.management-card :deep(.el-card__header) {
  background: rgba(248, 250, 252, 0.8);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  padding: 20px 24px;
  position: relative;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Header 渐变光带 */
.management-card :deep(.el-card__header)::after {
  content: '';
  position: absolute;
  left: -30%;
  top: 0;
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.15), transparent);
  transform: skewX(-20deg);
  filter: blur(1px);
  animation: headerSheen 6s infinite;
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

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.card-content {
  padding: 0 24px 10px 24px;
}

.card-description {
  color: #666;
  margin: 0 0 15px 0;
  font-size: 0.95rem;
  line-height: 1.5;
  animation: fadeInDown 0.6s ease both;
}

.feature-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.feature-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  transform: translateY(0);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}
.feature-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(102, 126, 234, 0.25);
}

/* 统计小徽标 */
.count-chip {
  margin-left: 10px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  color: #2563eb;
  background: rgba(37, 99, 235, 0.12);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.management-table {
  border-radius: 4px;
  overflow: hidden;
}

.management-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.management-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
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

.edit-button :deep(.el-button__icon) {
  color: #67c23a;
}

.delete-button :deep(.el-button__icon) {
  color: #f56c6c;
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
/* 图标动画 */
.header-icon {
  font-size: 1.4rem;
  color: #667eea;
  animation: iconFloat 3s ease-in-out infinite;
}

.star-icon {
  color: #ffd700;
  animation: starTwinkle 2s ease-in-out infinite;
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

@keyframes starTwinkle {
  0%,
  100% {
    transform: scale(1) rotate(0deg);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.2) rotate(180deg);
    opacity: 1;
  }
}

/* 按钮样式 */
.animated-btn {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

/* 涟漪元素样式 */
.ripple {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  transform: scale(0);
  animation: ripple 0.6s linear;
  pointer-events: none;
}

@keyframes ripple {
  to {
    transform: scale(3);
    opacity: 0;
  }
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

/* CTA 呼吸光圈与磁吸指针 */
.hero-cta {
  position: relative;
}
.hero-cta::after {
  content: '';
  position: absolute;
  inset: -6px;
  border-radius: 14px;
  background: radial-gradient(60% 60% at 50% 50%, rgba(102, 126, 234, 0.35), transparent 70%);
  filter: blur(8px);
  z-index: -1;
  opacity: 0.6;
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}
.hero-cta:hover::after {
  opacity: 1;
  transform: scale(1.05);
}

.success-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
}

.success-btn:hover {
  background: linear-gradient(135deg, #3d8bfe 0%, #00d4fe 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 25px rgba(79, 172, 254, 0.3);
}

.warning-btn {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  border: none;
}

.warning-btn:hover {
  background: linear-gradient(135deg, #f85e88 0%, #fdd835 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 25px rgba(250, 112, 154, 0.3);
}

.danger-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  border: none;
}

.danger-btn:hover {
  background: linear-gradient(135deg, #ff5252 0%, #d32f2f 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.3);
}

/* 禁用状态的批量删除按钮样式 */
.danger-btn.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: linear-gradient(135deg, #ccc 0%, #999 100%) !important;
  border-color: #ccc !important;
  color: #666 !important;
}

.danger-btn.is-disabled:hover {
  transform: none !important;
  box-shadow: none !important;
  background: linear-gradient(135deg, #ccc 0%, #999 100%) !important;
}

/* 批量添加标签容器样式 */
.batch-add-container {
  margin: 20px 0;
  padding: 20px;
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.05) 0%, rgba(0, 242, 254, 0.05) 100%);
  border-radius: 12px;
  border: 1px solid rgba(79, 172, 254, 0.2);
  transition: all 0.3s ease;
  background-size: 200% 200%;
  animation: subtleGradientShift 12s ease infinite;
}

.batch-add-container:hover {
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.1) 0%, rgba(0, 242, 254, 0.1) 100%);
  border: 1px solid rgba(79, 172, 254, 0.3);
}

.batch-add-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.batch-add-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.batch-add-tip {
  font-size: 0.9rem;
  color: #666;
  opacity: 0.8;
}

.batch-add-content {
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.batch-add-content .el-textarea {
  flex: 1;
}

.batch-add-content .el-button {
  flex-shrink: 0;
  margin-top: 5px;
}

/* 输入框、搜索框聚焦高亮 */
.category-card :deep(.el-input__wrapper),
.tag-card :deep(.el-input__wrapper),
.batch-add-content :deep(.el-input__wrapper) {
  transition:
    box-shadow 0.25s ease,
    transform 0.25s ease;
}
.category-card :deep(.is-focus.el-input__wrapper),
.tag-card :deep(.is-focus.el-input__wrapper) {
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.15);
  transform: translateY(-1px);
}
.batch-add-content :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.15);
}

/* 表格行悬浮与选中态美化 */
.management-table :deep(.el-table__row:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.06) 0%, rgba(240, 147, 251, 0.06) 100%);
  box-shadow: inset 0 0 0 1px rgba(102, 126, 234, 0.12);
}
.management-table :deep(.el-table__row .el-checkbox__input .el-checkbox__inner) {
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}
.management-table :deep(.el-table__row .el-checkbox__input.is-checked .el-checkbox__inner) {
  transform: scale(1.05);
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.15);
}

/* 操作按钮图标轻微动效 */
.action-button.animated-btn :deep(.el-button__icon) {
  transition: transform 0.25s ease;
}
.action-button.animated-btn:hover :deep(.el-button__icon) {
  transform: translateY(-1px) scale(1.1);
}

@keyframes subtleGradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
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

/* 表格美化 */
.management-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
}

/* 分页容器样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}

.management-table :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  color: #334155;
  font-weight: 700;
  border: none;
}

.management-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.management-table :deep(.el-table__row:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(240, 147, 251, 0.05) 100%);
  transform: scale(1.01);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-button {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.edit-button:hover {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-1px);
}

.delete-button:hover {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-1px);
}

/* 对话框美化 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 20px 24px;
}

:deep(.el-dialog__title) {
  font-weight: 700;
  color: #334155;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-title {
    font-size: 2rem;
    flex-direction: column;
    gap: 10px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .card-title {
    font-size: 1.1rem;
  }

  .floating-shape {
    display: none;
  }
}
</style>
