<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  ElDialog,
  ElTabs,
  ElTabPane,
  ElTable,
  ElTableColumn,
  ElButton,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElPopconfirm,
  ElIcon,
  ElPagination,
} from 'element-plus'
import { Edit, Delete, Plus, Search, RefreshRight } from '@element-plus/icons-vue'
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

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'refresh'): void
}>()

const visible = ref(props.modelValue)
const activeTab = ref('category')
const loading = ref(false)

// 分类数据
const categories = ref<Category[]>([])
const categoryPage = ref(1)
const categoryPageSize = ref(10)
const categoryTotal = ref(0)
const categorySearch = ref('')
const selectedCategories = ref<Category[]>([])
const batchAddCategoryInput = ref('')

// 标签数据
const tags = ref<Tag[]>([])
const tagPage = ref(1)
const tagPageSize = ref(10)
const tagTotal = ref(0)
const tagSearch = ref('')
const selectedTags = ref<Tag[]>([])
const batchAddTagInput = ref('')

// 编辑弹窗
const editDialogVisible = ref(false)
const editDialogTitle = ref('')
const editForm = ref({ id: 0, name: '', type: '' as 'category' | 'tag' })

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    fetchCategories()
    fetchTags()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 获取分类
const fetchCategories = async (page = 1) => {
  try {
    loading.value = true
    const params: any = { page, size: categoryPageSize.value }
    if (categorySearch.value) params.name = categorySearch.value
    
    const url = categorySearch.value ? '/api/categories/search/page' : '/api/categories/page'
    const response = await axios.get(url, { params })
    
    categories.value = response.data.categories
    categoryTotal.value = response.data.pagination.totalPosts
    categoryPage.value = response.data.pagination.currentPage
  } catch (error) {
    ElMessage.error('获取分类失败')
  } finally {
    loading.value = false
  }
}

// 获取标签
const fetchTags = async (page = 1) => {
  try {
    loading.value = true
    const params: any = { page, size: tagPageSize.value }
    if (tagSearch.value) params.name = tagSearch.value
    
    const url = tagSearch.value ? '/api/tags/search/page' : '/api/tags/page'
    const response = await axios.get(url, { params })
    
    tags.value = response.data.tags
    tagTotal.value = response.data.pagination.totalPosts
    tagPage.value = response.data.pagination.currentPage
  } catch (error) {
    ElMessage.error('获取标签失败')
  } finally {
    loading.value = false
  }
}

// 显示编辑弹窗
const showEdit = (item: Category | Tag, type: 'category' | 'tag') => {
  editForm.value = { id: item.id, name: item.name, type }
  editDialogTitle.value = type === 'category' ? '编辑分类' : '编辑标签'
  editDialogVisible.value = true
}

// 保存编辑
const saveEdit = async () => {
  if (!editForm.value.name.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  
  try {
    const url = editForm.value.type === 'category' 
      ? `/api/categories/${editForm.value.id}`
      : `/api/tags/${editForm.value.id}`
    
    await axios.put(url, { name: editForm.value.name })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    
    if (editForm.value.type === 'category') {
      fetchCategories(categoryPage.value)
    } else {
      fetchTags(tagPage.value)
    }
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

// 删除分类
const deleteCategory = async (id: number) => {
  try {
    await axios.delete(`/api/categories/${id}`)
    ElMessage.success('删除成功')
    fetchCategories(categoryPage.value)
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}

// 删除标签
const deleteTag = async (id: number) => {
  try {
    await axios.delete(`/api/tags/${id}`)
    ElMessage.success('删除成功')
    fetchTags(tagPage.value)
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}

// 批量添加分类
const batchAddCategories = async () => {
  if (!batchAddCategoryInput.value.trim()) return
  
  const names = batchAddCategoryInput.value
    .split(/[,，;\n]/)
    .map(n => n.trim())
    .filter(n => n.length > 0)
  
  if (names.length === 0) return
  
  try {
    const response = await axios.post('/api/categories/batch', { names })
    ElMessage.success(`成功添加 ${response.data.createdCount} 个分类`)
    batchAddCategoryInput.value = ''
    fetchCategories()
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

// 批量添加标签
const batchAddTags = async () => {
  if (!batchAddTagInput.value.trim()) return
  
  const names = batchAddTagInput.value
    .split(/[,，;\n]/)
    .map(n => n.trim())
    .filter(n => n.length > 0)
  
  if (names.length === 0) return
  
  try {
    const response = await axios.post('/api/tags/batch', { names })
    ElMessage.success(`成功添加 ${response.data.createdCount} 个标签`)
    batchAddTagInput.value = ''
    fetchTags()
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

// 批量删除分类
const batchDeleteCategories = async () => {
  if (selectedCategories.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedCategories.value.length} 个分类？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await axios.delete('/api/categories/batch', {
      data: { ids: selectedCategories.value.map(c => c.id) }
    })
    
    ElMessage.success('批量删除成功')
    selectedCategories.value = []
    fetchCategories()
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 批量删除标签
const batchDeleteTags = async () => {
  if (selectedTags.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedTags.value.length} 个标签？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await axios.delete('/api/tags/batch', {
      data: { ids: selectedTags.value.map(t => t.id) }
    })
    
    ElMessage.success('批量删除成功')
    selectedTags.value = []
    fetchTags()
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="分类与标签管理"
    width="800px"
    :close-on-click-modal="false"
    class="manager-dialog"
  >
    <el-tabs v-model="activeTab">
      <!-- 分类管理 -->
      <el-tab-pane label="分类管理" name="category">
        <div class="toolbar">
          <div class="search-box">
            <el-input
              v-model="categorySearch"
              placeholder="搜索分类"
              clearable
              @keyup.enter="fetchCategories(1)"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="fetchCategories(1)">搜索</el-button>
            <el-button @click="categorySearch = ''; fetchCategories(1)">
              <el-icon><RefreshRight /></el-icon>
            </el-button>
          </div>
          <el-button
            type="danger"
            :disabled="selectedCategories.length === 0"
            @click="batchDeleteCategories"
          >
            <el-icon><Delete /></el-icon>
            批量删除 ({{ selectedCategories.length }})
          </el-button>
        </div>
        
        <div class="batch-add">
          <el-input
            v-model="batchAddCategoryInput"
            type="textarea"
            :rows="2"
            placeholder="批量添加分类，用逗号、分号或换行分隔"
          />
          <el-button type="success" :disabled="!batchAddCategoryInput.trim()" @click="batchAddCategories">
            <el-icon><Plus /></el-icon>
            批量添加
          </el-button>
        </div>
        
        <el-table
          :data="categories"
          v-loading="loading"
          @selection-change="(val: Category[]) => selectedCategories = val"
          max-height="300"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="分类名称" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" type="primary" text @click="showEdit(row, 'category')">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-popconfirm title="确定删除？" @confirm="deleteCategory(row.id)">
                <template #reference>
                  <el-button size="small" type="danger" text>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        
        <el-pagination
          v-model:current-page="categoryPage"
          v-model:page-size="categoryPageSize"
          :total="categoryTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchCategories"
          @size-change="() => fetchCategories(1)"
          class="pagination"
        />
      </el-tab-pane>
      
      <!-- 标签管理 -->
      <el-tab-pane label="标签管理" name="tag">
        <div class="toolbar">
          <div class="search-box">
            <el-input
              v-model="tagSearch"
              placeholder="搜索标签"
              clearable
              @keyup.enter="fetchTags(1)"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="fetchTags(1)">搜索</el-button>
            <el-button @click="tagSearch = ''; fetchTags(1)">
              <el-icon><RefreshRight /></el-icon>
            </el-button>
          </div>
          <el-button
            type="danger"
            :disabled="selectedTags.length === 0"
            @click="batchDeleteTags"
          >
            <el-icon><Delete /></el-icon>
            批量删除 ({{ selectedTags.length }})
          </el-button>
        </div>
        
        <div class="batch-add">
          <el-input
            v-model="batchAddTagInput"
            type="textarea"
            :rows="2"
            placeholder="批量添加标签，用逗号、分号或换行分隔"
          />
          <el-button type="success" :disabled="!batchAddTagInput.trim()" @click="batchAddTags">
            <el-icon><Plus /></el-icon>
            批量添加
          </el-button>
        </div>
        
        <el-table
          :data="tags"
          v-loading="loading"
          @selection-change="(val: Tag[]) => selectedTags = val"
          max-height="300"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="标签名称" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" type="primary" text @click="showEdit(row, 'tag')">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-popconfirm title="确定删除？" @confirm="deleteTag(row.id)">
                <template #reference>
                  <el-button size="small" type="danger" text>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        
        <el-pagination
          v-model:current-page="tagPage"
          v-model:page-size="tagPageSize"
          :total="tagTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchTags"
          @size-change="() => fetchTags(1)"
          class="pagination"
        />
      </el-tab-pane>
    </el-tabs>
    
    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="editDialogTitle" width="400px" append-to-body>
      <el-input v-model="editForm.name" placeholder="请输入名称" />
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-box {
  display: flex;
  gap: 8px;
}

.search-box .el-input {
  width: 200px;
}

.batch-add {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.batch-add .el-input {
  flex: 1;
}

.pagination {
  margin-top: 16px;
  justify-content: center;
}
</style>
