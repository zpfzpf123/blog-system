<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CollectionTag,
  DataAnalysis,
  Delete,
  Edit,
  Folder,
  Plus,
  RefreshRight,
  Search,
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'

interface Category {
  id: number
  name: string
  createdAt: string
}

interface Tag {
  id: number
  name: string
}

interface PaginationInfo {
  currentPage: number
  totalPages: number
  totalPosts: number
}

interface CategoryPageResponse {
  categories: Category[]
  pagination: PaginationInfo
}

interface TagPageResponse {
  tags: Tag[]
  pagination: PaginationInfo
}

interface BatchCreateResponse {
  createdCount: number
}

interface ErrorResponse {
  response?: {
    data?: {
      message?: string
    }
  }
}

type ManagerType = 'category' | 'tag'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'refresh'): void
}>()

const visible = ref(false)
const activeTab = ref<ManagerType>('category')

const loadingCategory = ref(false)
const loadingTag = ref(false)

const categories = ref<Category[]>([])
const categoryPage = ref(1)
const categoryPageSize = ref(10)
const categoryTotal = ref(0)
const categorySearch = ref('')
const selectedCategories = ref<Category[]>([])
const batchAddCategoryInput = ref('')

const tags = ref<Tag[]>([])
const tagPage = ref(1)
const tagPageSize = ref(10)
const tagTotal = ref(0)
const tagSearch = ref('')
const selectedTags = ref<Tag[]>([])
const batchAddTagInput = ref('')

const editDialogVisible = ref(false)
const editDialogTitle = ref('')
const editForm = ref<{ id: number; name: string; type: ManagerType }>({
  id: 0,
  name: '',
  type: 'category',
})

const isAnyLoading = computed(() => loadingCategory.value || loadingTag.value)

const summaryCards = computed(() => {
  return [
    {
      key: 'categories',
      label: '分类总数',
      value: categoryTotal.value,
      selected: selectedCategories.value.length,
      icon: Folder,
    },
    {
      key: 'tags',
      label: '标签总数',
      value: tagTotal.value,
      selected: selectedTags.value.length,
      icon: CollectionTag,
    },
    {
      key: 'active',
      label: '当前管理',
      value: activeTab.value === 'category' ? '分类' : '标签',
      selected: activeTab.value === 'category' ? selectedCategories.value.length : selectedTags.value.length,
      icon: DataAnalysis,
    },
  ]
})

const getErrorMessage = (error: unknown, fallback: string) => {
  const resolved = error as ErrorResponse
  return resolved.response?.data?.message || fallback
}

const initData = async () => {
  await Promise.all([fetchCategories(categoryPage.value), fetchTags(tagPage.value)])
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      void initData()
    }
  },
  { immediate: true },
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const fetchCategories = async (page = 1) => {
  try {
    loadingCategory.value = true
    const params: Record<string, string | number> = {
      page,
      size: categoryPageSize.value,
    }

    if (categorySearch.value.trim()) {
      params.name = categorySearch.value.trim()
    }

    const endpoint = categorySearch.value.trim() ? '/api/categories/search/page' : '/api/categories/page'
    const response = await axios.get<CategoryPageResponse>(endpoint, { params })

    categories.value = response.data.categories
    categoryTotal.value = response.data.pagination.totalPosts
    categoryPage.value = response.data.pagination.currentPage
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '获取分类失败'))
  } finally {
    loadingCategory.value = false
  }
}

const fetchTags = async (page = 1) => {
  try {
    loadingTag.value = true
    const params: Record<string, string | number> = {
      page,
      size: tagPageSize.value,
    }

    if (tagSearch.value.trim()) {
      params.name = tagSearch.value.trim()
    }

    const endpoint = tagSearch.value.trim() ? '/api/tags/search/page' : '/api/tags/page'
    const response = await axios.get<TagPageResponse>(endpoint, { params })

    tags.value = response.data.tags
    tagTotal.value = response.data.pagination.totalPosts
    tagPage.value = response.data.pagination.currentPage
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '获取标签失败'))
  } finally {
    loadingTag.value = false
  }
}

const resetCategorySearch = () => {
  categorySearch.value = ''
  void fetchCategories(1)
}

const resetTagSearch = () => {
  tagSearch.value = ''
  void fetchTags(1)
}

const handleCategorySelectionChange = (rows: Category[]) => {
  selectedCategories.value = rows
}

const handleTagSelectionChange = (rows: Tag[]) => {
  selectedTags.value = rows
}

const showEdit = (item: Category | Tag, type: ManagerType) => {
  editForm.value = {
    id: item.id,
    name: item.name,
    type,
  }
  editDialogTitle.value = type === 'category' ? '编辑分类' : '编辑标签'
  editDialogVisible.value = true
}

const saveEdit = async () => {
  const nextName = editForm.value.name.trim()
  if (!nextName) {
    ElMessage.warning('请输入名称')
    return
  }

  try {
    const endpoint =
      editForm.value.type === 'category' ? `/api/categories/${editForm.value.id}` : `/api/tags/${editForm.value.id}`

    await axios.put(endpoint, { name: nextName })
    ElMessage.success('更新成功')
    editDialogVisible.value = false

    if (editForm.value.type === 'category') {
      await fetchCategories(categoryPage.value)
    } else {
      await fetchTags(tagPage.value)
    }

    emit('refresh')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '更新失败'))
  }
}

const deleteCategory = async (id: number) => {
  try {
    await axios.delete(`/api/categories/${id}`)
    ElMessage.success('删除成功')
    await fetchCategories(categoryPage.value)
    emit('refresh')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '删除失败'))
  }
}

const deleteTag = async (id: number) => {
  try {
    await axios.delete(`/api/tags/${id}`)
    ElMessage.success('删除成功')
    await fetchTags(tagPage.value)
    emit('refresh')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '删除失败'))
  }
}

const parseBatchNames = (value: string) => {
  return value
    .split(/[,，;；\n]/)
    .map((name) => name.trim())
    .filter((name) => name.length > 0)
}

const batchAddCategories = async () => {
  const names = parseBatchNames(batchAddCategoryInput.value)
  if (names.length === 0) {
    ElMessage.warning('请先输入分类名称')
    return
  }

  try {
    const response = await axios.post<BatchCreateResponse>('/api/categories/batch', { names })
    ElMessage.success(`成功添加 ${response.data.createdCount} 个分类`)
    batchAddCategoryInput.value = ''
    await fetchCategories(1)
    emit('refresh')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '添加分类失败'))
  }
}

const batchAddTags = async () => {
  const names = parseBatchNames(batchAddTagInput.value)
  if (names.length === 0) {
    ElMessage.warning('请先输入标签名称')
    return
  }

  try {
    const response = await axios.post<BatchCreateResponse>('/api/tags/batch', { names })
    ElMessage.success(`成功添加 ${response.data.createdCount} 个标签`)
    batchAddTagInput.value = ''
    await fetchTags(1)
    emit('refresh')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '添加标签失败'))
  }
}

const batchDeleteCategories = async () => {
  if (selectedCategories.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedCategories.value.length} 个分类吗？此操作不可恢复。`,
      '批量删除分类',
      { type: 'warning' },
    )

    await axios.delete('/api/categories/batch', {
      data: {
        ids: selectedCategories.value.map((item) => item.id),
      },
    })

    ElMessage.success('批量删除成功')
    selectedCategories.value = []
    await fetchCategories(categoryPage.value)
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(getErrorMessage(error, '批量删除失败'))
    }
  }
}

const batchDeleteTags = async () => {
  if (selectedTags.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedTags.value.length} 个标签吗？此操作不可恢复。`,
      '批量删除标签',
      { type: 'warning' },
    )

    await axios.delete('/api/tags/batch', {
      data: {
        ids: selectedTags.value.map((item) => item.id),
      },
    })

    ElMessage.success('批量删除成功')
    selectedTags.value = []
    await fetchTags(tagPage.value)
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(getErrorMessage(error, '批量删除失败'))
    }
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    width="960px"
    top="4vh"
    :close-on-click-modal="false"
    class="manager-dialog"
    destroy-on-close
  >
    <template #header>
      <div class="dialog-hero">
        <div class="hero-copy">
          <p class="hero-eyebrow">Content Taxonomy Hub</p>
          <h2 class="hero-title">分类与标签管理中心</h2>
          <p class="hero-subtitle">集中维护分类和标签，快速批量操作，提升内容组织效率。</p>
        </div>

        <div class="hero-stats">
          <article v-for="card in summaryCards" :key="card.key" class="hero-stat-card">
            <div class="hero-stat-icon">
              <el-icon><component :is="card.icon" /></el-icon>
            </div>
            <div class="hero-stat-main">{{ card.value }}</div>
            <div class="hero-stat-label">{{ card.label }}</div>
            <div class="hero-stat-selected">已选 {{ card.selected }}</div>
          </article>
        </div>
      </div>
    </template>

    <div class="manager-body">
      <el-tabs v-model="activeTab" class="manager-tabs">
        <el-tab-pane name="category">
          <template #label>
            <span class="tab-label">
              <el-icon><Folder /></el-icon>
              分类管理
            </span>
          </template>

          <section class="control-bar panel-card">
            <div class="search-group">
              <el-input
                v-model="categorySearch"
                class="search-input"
                clearable
                placeholder="搜索分类名称"
                @keyup.enter="fetchCategories(1)"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="fetchCategories(1)">查询</el-button>
              <el-button @click="resetCategorySearch">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </div>

            <el-button
              type="danger"
              :disabled="selectedCategories.length === 0"
              @click="batchDeleteCategories"
            >
              <el-icon><Delete /></el-icon>
              批量删除（{{ selectedCategories.length }}）
            </el-button>
          </section>

          <section class="batch-panel panel-card">
            <div class="batch-head">
              <h3>批量新增分类</h3>
              <span>支持逗号、分号或换行分隔</span>
            </div>
            <el-input
              v-model="batchAddCategoryInput"
              type="textarea"
              :rows="3"
              resize="none"
              placeholder="例如：前端开发，后端架构，性能优化"
            />
            <div class="batch-foot">
              <span>{{ parseBatchNames(batchAddCategoryInput).length }} 项待添加</span>
              <el-button type="success" :disabled="!batchAddCategoryInput.trim()" @click="batchAddCategories">
                <el-icon><Plus /></el-icon>
                批量添加
              </el-button>
            </div>
          </section>

          <section class="table-panel panel-card">
            <div class="table-head">
              <h3>分类列表</h3>
              <span>共 {{ categoryTotal }} 项</span>
            </div>

            <el-table
              :data="categories"
              :loading="loadingCategory"
              max-height="320"
              class="manager-table"
              @selection-change="handleCategorySelectionChange"
            >
              <el-table-column type="selection" width="52" />
              <el-table-column prop="id" label="ID" width="90" />
              <el-table-column prop="name" label="分类名称" min-width="220" />
              <el-table-column prop="createdAt" label="创建时间" min-width="180" />
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button text type="primary" @click="showEdit(row, 'category')">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-popconfirm title="确定删除该分类吗？" @confirm="deleteCategory(row.id)">
                    <template #reference>
                      <el-button text type="danger">
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>

            <div class="table-pagination">
              <el-pagination
                v-model:current-page="categoryPage"
                v-model:page-size="categoryPageSize"
                :page-sizes="[10, 20, 50]"
                :total="categoryTotal"
                layout="total, sizes, prev, pager, next"
                @current-change="fetchCategories"
                @size-change="() => fetchCategories(1)"
              />
            </div>
          </section>
        </el-tab-pane>

        <el-tab-pane name="tag">
          <template #label>
            <span class="tab-label">
              <el-icon><CollectionTag /></el-icon>
              标签管理
            </span>
          </template>

          <section class="control-bar panel-card">
            <div class="search-group">
              <el-input
                v-model="tagSearch"
                class="search-input"
                clearable
                placeholder="搜索标签名称"
                @keyup.enter="fetchTags(1)"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="fetchTags(1)">查询</el-button>
              <el-button @click="resetTagSearch">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </div>

            <el-button
              type="danger"
              :disabled="selectedTags.length === 0"
              @click="batchDeleteTags"
            >
              <el-icon><Delete /></el-icon>
              批量删除（{{ selectedTags.length }}）
            </el-button>
          </section>

          <section class="batch-panel panel-card">
            <div class="batch-head">
              <h3>批量新增标签</h3>
              <span>支持逗号、分号或换行分隔</span>
            </div>
            <el-input
              v-model="batchAddTagInput"
              type="textarea"
              :rows="3"
              resize="none"
              placeholder="例如：Vue3，TypeScript，设计系统"
            />
            <div class="batch-foot">
              <span>{{ parseBatchNames(batchAddTagInput).length }} 项待添加</span>
              <el-button type="success" :disabled="!batchAddTagInput.trim()" @click="batchAddTags">
                <el-icon><Plus /></el-icon>
                批量添加
              </el-button>
            </div>
          </section>

          <section class="table-panel panel-card">
            <div class="table-head">
              <h3>标签列表</h3>
              <span>共 {{ tagTotal }} 项</span>
            </div>

            <el-table
              :data="tags"
              :loading="loadingTag"
              max-height="320"
              class="manager-table"
              @selection-change="handleTagSelectionChange"
            >
              <el-table-column type="selection" width="52" />
              <el-table-column prop="id" label="ID" width="90" />
              <el-table-column prop="name" label="标签名称" min-width="260" />
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button text type="primary" @click="showEdit(row, 'tag')">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-popconfirm title="确定删除该标签吗？" @confirm="deleteTag(row.id)">
                    <template #reference>
                      <el-button text type="danger">
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>

            <div class="table-pagination">
              <el-pagination
                v-model:current-page="tagPage"
                v-model:page-size="tagPageSize"
                :page-sizes="[10, 20, 50]"
                :total="tagTotal"
                layout="total, sizes, prev, pager, next"
                @current-change="fetchTags"
                @size-change="() => fetchTags(1)"
              />
            </div>
          </section>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="editDialogVisible" :title="editDialogTitle" width="420px" append-to-body class="edit-dialog">
      <el-input
        v-model="editForm.name"
        maxlength="30"
        show-word-limit
        placeholder="请输入名称"
        @keyup.enter="saveEdit"
      />
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="isAnyLoading" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
.manager-dialog {
  --surface: #ffffff;
  --surface-soft: #f7fbff;
  --line: #dbe7f5;
  --line-strong: #c9daf1;
  --text-title: #0f2248;
  --text-main: #334155;
  --text-muted: #64748b;
  --accent: #2563eb;
  --danger: #dc2626;
  --success: #16a34a;
  --shadow-soft: 0 22px 54px rgba(37, 99, 235, 0.12);
  --radius-xl: 22px;
  --radius-lg: 16px;
  --radius-md: 12px;

  font-family: 'Outfit', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

:deep(.manager-dialog .el-dialog) {
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: linear-gradient(160deg, #f7fbff 0%, #ffffff 38%, #f8fbff 100%);
  border: 1px solid rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-soft);
}

:deep(.manager-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 0;
}

:deep(.manager-dialog .el-dialog__body) {
  padding: 16px 18px 18px;
}

.dialog-hero {
  position: relative;
  display: grid;
  gap: 14px;
  padding: 20px 22px 16px;
  border-bottom: 1px solid var(--line);
  background:
    radial-gradient(circle at 88% 0%, rgba(37, 99, 235, 0.14) 0%, transparent 48%),
    radial-gradient(circle at 10% 0%, rgba(22, 163, 74, 0.1) 0%, transparent 38%),
    linear-gradient(140deg, #f0f7ff 0%, #ffffff 70%);
}

.hero-copy {
  display: grid;
  gap: 6px;
  max-width: 640px;
}

.hero-eyebrow {
  margin: 0;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #1d4ed8;
  background: #e5efff;
}

.hero-title {
  margin: 0;
  color: var(--text-title);
  font-size: 30px;
  line-height: 1.15;
  font-family: 'Fraunces', 'Source Han Serif SC', 'STZhongsong', serif;
  letter-spacing: -0.01em;
}

.hero-subtitle {
  margin: 0;
  font-size: 14px;
  line-height: 1.55;
  color: var(--text-muted);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.hero-stat-card {
  border-radius: 14px;
  border: 1px solid #d9e7fa;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(6px);
  padding: 10px;
  display: grid;
  gap: 4px;
}

.hero-stat-icon {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  color: #1d4ed8;
  background: #e8f0ff;
}

.hero-stat-main {
  color: var(--text-title);
  font-size: 21px;
  font-weight: 700;
  line-height: 1;
}

.hero-stat-label {
  color: var(--text-muted);
  font-size: 12px;
}

.hero-stat-selected {
  color: #3f6a97;
  font-size: 11px;
  font-weight: 600;
}

.manager-body {
  display: grid;
  gap: 12px;
}

:deep(.manager-tabs .el-tabs__header) {
  margin: 0 0 12px;
}

:deep(.manager-tabs .el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--line);
}

:deep(.manager-tabs .el-tabs__item) {
  height: 38px;
  padding: 0 14px;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
}

.panel-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--line);
  background: linear-gradient(145deg, #ffffff 0%, var(--surface-soft) 100%);
  padding: 12px;
}

.control-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.search-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.search-input {
  max-width: 320px;
}

.batch-panel {
  display: grid;
  gap: 10px;
}

.batch-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.batch-head h3 {
  margin: 0;
  font-size: 15px;
  color: var(--text-title);
}

.batch-head span {
  font-size: 12px;
  color: var(--text-muted);
}

.batch-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.batch-foot span {
  font-size: 12px;
  color: var(--text-muted);
}

.table-panel {
  display: grid;
  gap: 10px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-head h3 {
  margin: 0;
  font-size: 15px;
  color: var(--text-title);
}

.table-head span {
  color: var(--text-muted);
  font-size: 12px;
}

.manager-table {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.manager-table .el-table__header-wrapper th) {
  background: #f0f6ff;
  color: #3b5677;
  font-weight: 700;
}

:deep(.manager-table .el-table__row td) {
  background: transparent;
}

.table-pagination {
  display: flex;
  justify-content: center;
  padding-top: 2px;
}

:deep(.el-pagination) {
  --el-pagination-button-bg-color: #ffffff;
  --el-pagination-hover-color: var(--accent);
}

:deep(.el-pager li) {
  border-radius: 9px;
}

:deep(.el-pager li.is-active) {
  background: linear-gradient(120deg, #2563eb 0%, #1d4ed8 100%);
}

:deep(.edit-dialog .el-dialog) {
  border-radius: 16px;
}

@media (max-width: 980px) {
  :deep(.manager-dialog .el-dialog) {
    width: calc(100vw - 18px) !important;
    margin: 0 auto;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }

  .control-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-group {
    width: 100%;
  }

  .search-input {
    max-width: none;
  }

  .batch-head,
  .batch-foot,
  .table-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
