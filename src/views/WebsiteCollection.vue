<template>
  <div class="website-collection">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Link /></el-icon>
          网站合集
        </h1>
        <p class="page-description">收集和整理您喜欢的网站，按分类管理</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddWebsiteDialog">
          <el-icon><Plus /></el-icon>
          添加网站
        </el-button>
        <el-button @click="showAddCategoryDialog">
          <el-icon><FolderAdd /></el-icon>
          添加分类
        </el-button>
        <el-button type="success" @click="showAIGenerateCategoryDialog">
          <el-icon><Star /></el-icon>
          AI一键生成分类
        </el-button>

        <el-button type="primary" @click="showAICreateWebsiteDialog">
          <el-icon><Plus /></el-icon>
          AI一键新增网站
        </el-button>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <div class="category-header">
        <div class="header-left">
          <div class="title-container">
            <el-icon class="title-icon"><Menu /></el-icon>
            <span class="category-title">分类筛选</span>
            <div class="title-decoration"></div>
          </div>
          <span class="category-subtitle">按分类浏览和管理您的网站收藏</span>
        </div>
        <el-tooltip
          content="点击进入编辑模式，拖拽分类项目可调整顺序"
          placement="top"
          :show-after="300"
        >
          <el-button
            size="small"
            type="primary"
            text
            @click="toggleEditMode"
            class="edit-mode-btn"
            :class="{ active: isEditMode }"
          >
            <el-icon><Edit /></el-icon>
            {{ isEditMode ? '完成排序' : '调整顺序' }}
          </el-button>
        </el-tooltip>
      </div>

      <div class="categories-container" :class="{ 'edit-mode': isEditMode }">
        <el-radio-group v-model="selectedCategory" @change="handleCategoryChange">
          <!-- 全部分类（不可拖拽） -->
          <div class="category-item all-category" :class="{ selected: selectedCategory === '' }">
            <el-tooltip content="显示所有分类的网站" placement="top" :show-after="300">
              <el-radio-button label="" class="all-category-btn">
                <div class="btn-content">
                  <el-icon class="category-icon"><Grid /></el-icon>
                  <span class="category-text">全部</span>
                  <el-tag
                    size="small"
                    type="info"
                    class="count-tag"
                    :class="{ pulse: selectedCategory === '' }"
                  >
                    {{ getTotalCount() }}
                  </el-tag>
                </div>
              </el-radio-button>
            </el-tooltip>
          </div>

          <!-- 可拖拽的分类 -->
          <div
            v-for="(category, index) in sortedCategories"
            :key="category.id"
            class="category-item"
            :class="{
              selected: selectedCategory === category.id,
              dragging: draggedIndex === index,
              'drag-over': draggedIndex !== -1 && draggedIndex !== index,
            }"
            draggable="true"
            @dragstart="handleDragStart(index)"
            @dragover.prevent
            @drop="handleDrop(index)"
            @dragenter.prevent
            @dragend="handleDragEnd"
          >
            <el-tooltip
              :content="category.description || '暂无描述'"
              placement="top"
              :show-after="300"
              :disabled="!category.description"
            >
              <el-radio-button :label="category.id" class="category-btn">
                <div class="btn-content">
                  <div
                    class="category-color-dot"
                    :style="{ backgroundColor: category.color }"
                  ></div>
                  <span class="category-text">{{ category.name }}</span>
                  <el-tag
                    size="small"
                    type="info"
                    class="count-tag"
                    :class="{ pulse: selectedCategory === category.id }"
                  >
                    {{ category.websiteCount ?? 0 }}
                  </el-tag>
                </div>
              </el-radio-button>
            </el-tooltip>

            <!-- 拖拽手柄 -->
            <el-tooltip content="拖拽调整分类顺序" placement="top" :show-after="300">
              <div v-if="isEditMode" class="drag-handle" :class="{ visible: isEditMode }">
                <el-icon><Rank /></el-icon>
              </div>
            </el-tooltip>
          </div>
        </el-radio-group>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="search-section">
      <div class="search-container">
        <div class="search-header">
          <div class="search-title-container">
            <el-icon class="search-title-icon"><Search /></el-icon>
            <span class="search-title">智能搜索</span>
            <div class="search-title-decoration"></div>
          </div>
          <span class="search-subtitle">快速找到您需要的网站资源</span>
        </div>
        <div class="search-input-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索网站名称、描述或URL..."
            clearable
            @input="handleSearch"
            @focus="handleSearchFocus"
            @blur="handleSearchBlur"
            class="search-input"
            :class="{ focused: isSearchFocused }"
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 网站列表 -->
    <div class="websites-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="websites-grid">
          <div v-for="n in pageSize" :key="n" class="website-card skeleton">
            <el-skeleton :rows="4" animated />
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="websites.length === 0" class="empty-state">
        <el-empty description="暂无网站" :image-size="120">
          <el-button type="primary" @click="showAddWebsiteDialog"> 添加第一个网站 </el-button>
        </el-empty>
      </div>

      <!-- 网站列表 -->
      <div v-else class="websites-grid">
        <div
          v-for="website in websites"
          :key="website.id"
          class="website-card"
          @click="visitWebsite(website)"
        >
          <div class="card-header">
            <div class="website-icon">
              <img
                v-if="website.icon"
                :src="website.icon"
                :alt="website.name"
                @error="handleIconError"
              />
              <el-icon v-else><Link /></el-icon>
            </div>
            <div class="website-info">
              <h3 class="website-name">{{ website.name }}</h3>
              <p class="website-url">{{ website.url }}</p>
            </div>
            <div class="card-actions">
              <el-button size="small" circle @click.stop="editWebsite(website)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" type="danger" circle @click.stop="deleteWebsite(website)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>

          <div class="card-body">
            <p class="website-description">{{ website.description }}</p>
          </div>

          <div class="card-footer">
            <div class="website-meta">
              <span class="visit-count">
                <el-icon><View /></el-icon>
                {{ website.visitCount }}
              </span>
              <span class="created-time">
                {{ formatDate(website.createdAt) }}
              </span>
            </div>
            <div class="category-badge">
              <!-- 支持多分类显示 -->
              <template v-if="website.categoryIds && website.categoryIds.length > 0">
                <el-tag
                  v-for="categoryId in website.categoryIds"
                  :key="categoryId"
                  :color="getCategoryColor(categoryId)"
                  effect="dark"
                  size="small"
                  class="category-tag"
                >
                  {{ getCategoryName(categoryId) }}
                </el-tag>
              </template>
              <el-tag v-else color="#909399" effect="dark" size="small"> 未分类 </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <!-- 分页信息 -->
      <div class="pagination-info">
        <span class="pagination-text">
          <span v-if="total > 0">
            显示第 {{ (currentPage - 1) * pageSize + 1 }}-{{
              Math.min(currentPage * pageSize, total)
            }}
            条， 共 {{ total }} 条记录
          </span>
          <span v-else>暂无数据</span>
        </span>
        <span class="pagination-pages" v-if="total > 0">
          第 {{ currentPage }} 页，共 {{ totalPages }} 页
        </span>
        <span class="pagination-pages" v-else>共 0 页</span>
      </div>

      <!-- 分页控件 -->
      <div class="pagination-controls">
        <!-- 每页显示数量选择 -->
        <div class="page-size-selector">
          <span class="page-size-label">每页显示：</span>
          <el-select v-model="pageSize" @change="handleSizeChange" size="small">
            <el-option
              v-for="size in [12, 24, 48, 96]"
              :key="size"
              :label="`${size} 条`"
              :value="size"
            />
          </el-select>
        </div>

        <!-- 分页导航 -->
        <div class="pagination-navigation">
          <!-- 快速跳转 -->
          <div class="page-jumper">
            <span class="jumper-label">跳转到：</span>
            <el-input
              v-model="jumpPage"
              type="number"
              :min="1"
              :max="totalPages"
              size="small"
              class="jumper-input"
              @keyup.enter="handleJumpPage"
              @blur="handleJumpPage"
            />
            <span class="jumper-text">页</span>
          </div>

          <!-- 分页按钮 -->
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :background="true"
            layout="prev, pager, next"
            :total="total"
            :pager-count="7"
            @current-change="handlePageChange"
            class="main-pagination"
          />
        </div>
      </div>
    </div>

    <!-- 添加/编辑网站对话框 -->
    <el-dialog
      v-model="websiteDialogVisible"
      :title="editingWebsite ? '编辑网站' : '添加网站'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="websiteFormRef" :model="websiteForm" :rules="websiteRules" label-width="80px">
        <el-form-item label="网站名称" prop="name">
          <el-input v-model="websiteForm.name" placeholder="请输入网站名称" />
        </el-form-item>

        <el-form-item label="网站URL" prop="url">
          <el-input v-model="websiteForm.url" placeholder="请输入网站URL" />
        </el-form-item>

        <el-form-item label="网站描述" prop="description">
          <el-input
            v-model="websiteForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入网站描述"
          />
        </el-form-item>

        <el-form-item label="所属分类" prop="categoryIds">
          <el-select
            v-model="websiteForm.categoryIds"
            placeholder="请选择分类"
            multiple
            collapse-tags
            collapse-tags-tooltip
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="网站图标" prop="icon">
          <el-input v-model="websiteForm.icon" placeholder="请输入图标URL（可选）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="websiteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveWebsite" :loading="saving"> 保存 </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      title="添加分类"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>

        <el-form-item label="分类颜色" prop="color">
          <el-color-picker v-model="categoryForm.color" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCategory" :loading="savingCategory">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- AI生成分类对话框 -->
    <el-dialog
      v-model="aiGenerateCategoryDialogVisible"
      title="AI一键生成分类"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="aiCategoryFormRef"
        :model="aiCategoryForm"
        :rules="aiCategoryRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="aiCategoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="aiCategoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
            v-if="aiGeneratedResult"
          />
          <div v-else class="ai-placeholder">
            <el-text type="info">点击"AI生成"按钮后可以编辑描述</el-text>
          </div>
        </el-form-item>

        <el-form-item label="分类颜色" prop="color">
          <el-color-picker v-model="aiCategoryForm.color" v-if="aiGeneratedResult" show-alpha />
          <div v-else class="ai-placeholder">
            <el-text type="info">点击"AI生成"按钮后可以选择颜色</el-text>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="aiGenerateCategoryDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="generateCategoryWithAI"
            :loading="aiGenerating"
            v-if="!aiGeneratedResult"
          >
            AI生成
          </el-button>
          <el-button
            type="success"
            @click="saveAIGeneratedCategory"
            :loading="savingAICategory"
            v-if="aiGeneratedResult"
          >
            保存分类
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- AI新增网站对话框 -->
    <el-dialog
      v-model="aiCreateWebsiteDialogVisible"
      title="AI一键新增网站"
      width="700px"
      :close-on-click-modal="false"
    >
      <!-- 第一步：输入网站地址 -->
      <div v-if="!aiWebsiteResult" class="ai-step">
        <div class="step-header">
          <el-icon class="step-icon"><Link /></el-icon>
          <span class="step-title">第一步：输入网站地址</span>
        </div>
        <el-form ref="aiWebsiteFormRef" :model="aiWebsiteForm" :rules="aiWebsiteRules">
          <el-form-item label="网站地址" prop="url">
            <el-input
              v-model="aiWebsiteForm.url"
              placeholder="请输入网站地址（如：https://github.com）"
              size="large"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步：AI生成结果和手动编辑 -->
      <div v-if="aiWebsiteResult" class="ai-step">
        <div class="step-header">
          <el-icon class="step-icon"><Edit /></el-icon>
          <span class="step-title">第二步：编辑网站信息</span>
          <el-tag type="success" size="small">AI已生成，可手动修改</el-tag>
        </div>

        <el-form
          ref="aiWebsiteEditFormRef"
          :model="aiWebsiteEditForm"
          :rules="aiWebsiteEditRules"
          label-width="100px"
        >
          <el-form-item label="网站名称" prop="name">
            <el-input
              v-model="aiWebsiteEditForm.name"
              placeholder="请输入网站名称"
              show-word-limit
              maxlength="50"
            />
          </el-form-item>

          <el-form-item label="网站描述" prop="description">
            <el-input
              v-model="aiWebsiteEditForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入网站描述"
              show-word-limit
              maxlength="200"
            />
          </el-form-item>

          <el-form-item label="所属分类" prop="categoryIds">
            <el-select
              v-model="aiWebsiteEditForm.categoryIds"
              multiple
              collapse-tags
              collapse-tags-tooltip
              placeholder="请选择分类"
              style="width: 100%"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              >
                <div class="category-option">
                  <div
                    class="category-color-dot"
                    :style="{ backgroundColor: category.color }"
                  ></div>
                  <span>{{ category.name }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="网站图标">
            <div class="icon-preview">
              <img
                v-if="lastScrapedInfo?.favicon"
                :src="lastScrapedInfo.favicon"
                :alt="aiWebsiteEditForm.name"
                class="favicon-preview"
                @error="handleFaviconError"
              />
              <el-icon v-else class="favicon-placeholder"><Link /></el-icon>
              <span class="icon-info">{{ lastScrapedInfo?.favicon || '未抓取到图标' }}</span>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="aiCreateWebsiteDialogVisible = false">取消</el-button>

          <!-- 第一步按钮 -->
          <template v-if="!aiWebsiteResult">
            <el-button
              type="primary"
              :loading="aiGeneratingWebsite"
              :disabled="aiGeneratingWebsite"
              @click="generateWebsiteWithAI"
            >
              <el-icon><Star /></el-icon>
              AI生成
            </el-button>
          </template>

          <!-- 第二步按钮 -->
          <template v-if="aiWebsiteResult">
            <el-button @click="regenerateWebsiteWithAI" :loading="aiGeneratingWebsite">
              <el-icon><Refresh /></el-icon>
              重新生成
            </el-button>
            <el-button type="success" :loading="savingAIWebsite" @click="saveAIGeneratedWebsite">
              <el-icon><Check /></el-icon>
              保存网站
            </el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="delete-confirm">
        <el-icon class="warning-icon" color="#E6A23C"><Warning /></el-icon>
        <p>确定要删除网站 "{{ deletingWebsite?.name }}" 吗？</p>
        <p class="delete-tip">此操作不可恢复</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete" :loading="deleting"> 确定删除 </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import {
  Link,
  Plus,
  FolderAdd,
  Search,
  Warning,
  Edit,
  Delete,
  View,
  Rank,
  Star,
  Menu,
  Grid,
  Refresh,
  Check,
} from '@element-plus/icons-vue'
import {
  websiteApi,
  categoryApi,
  type Website,
  type Category,
  type WebsiteCreateRequest,
  type CategoryCreateRequest,
} from '../utils/websiteApi'
import { generateCategoryWithOllama, generateWebsiteInfoWithOllama } from '../utils/aiService'

// 响应式数据
const websites = ref<Website[]>([])
const categories = ref<Category[]>([])

const selectedCategory = ref<number | ''>('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const totalAllWebsites = ref(0) // 存储所有网站的总数量，不受分类筛选影响，用于"全部"分类显示
const loading = ref(false)
const jumpPage = ref<number>(1)
const isSearchFocused = ref(false) // 搜索框焦点状态

// 拖拽排序相关状态
const isEditMode = ref(false)
const draggedIndex = ref<number>(-1)
const originalCategories = ref<Category[]>([])

// 对话框状态
const websiteDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const aiGenerateCategoryDialogVisible = ref(false)
const editingWebsite = ref<Website | null>(null)
const deletingWebsite = ref<Website | null>(null)
const saving = ref(false)
// 占位图（当网站图标加载失败时使用）
const FALLBACK_ICON_DATA_URL =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64"><defs><linearGradient id="g" x1="0" x2="1" y1="0" y2="1"><stop stop-color="%23667eea" offset="0%"/><stop stop-color="%23764ba2" offset="100%"/></linearGradient></defs><rect rx="12" ry="12" width="64" height="64" fill="url(%23g)"/><g fill="white" opacity="0.95"><circle cx="32" cy="32" r="16" fill="none" stroke="white" stroke-width="3"/><path d="M16 32h32M32 16v32" stroke="white" stroke-width="3" stroke-linecap="round"/></g></svg>'
const savingCategory = ref(false)
const deleting = ref(false)
const aiGenerating = ref(false)
const savingAICategory = ref(false)

// AI新增网站相关状态
const aiCreateWebsiteDialogVisible = ref(false)
const aiGeneratingWebsite = ref(false)
const savingAIWebsite = ref(false)
const aiWebsiteForm = ref({
  url: '',
})
const aiWebsiteResult = ref<{
  name: string
  description: string
  categoryId: number
  icon: string
} | null>(null)

// AI网站编辑表单
const aiWebsiteEditForm = ref({
  name: '',
  description: '',
  categoryIds: [] as number[],
})

// AI网站编辑表单引用
const aiWebsiteEditFormRef = ref<FormInstance>()

// 保存最近一次从后端抓取的网站信息（包含 favicon）
const lastScrapedInfo = ref<{
  title?: string
  description?: string
  keywords?: string
  favicon?: string
} | null>(null)

// 表单引用
const websiteFormRef = ref<FormInstance>()
const categoryFormRef = ref<FormInstance>()
const aiCategoryFormRef = ref<FormInstance>()
const aiWebsiteFormRef = ref<FormInstance>()

// 表单数据
const websiteForm = ref<WebsiteCreateRequest>({
  name: '',
  url: '',
  description: '',
  categoryIds: [],
  icon: '',
})

const categoryForm = ref<CategoryCreateRequest>({
  name: '',
  description: '',
  color: '#409EFF',
  sortOrder: 0,
})

const aiCategoryForm = ref({
  name: '',
  description: '',
  color: '#409EFF',
  sortOrder: 0,
})

const aiGeneratedResult = ref<{
  description: string
  color: string
} | null>(null)

// 表单验证规则
const websiteRules = {
  name: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  url: [
    { required: false, message: '请输入网站URL', trigger: 'blur' },
    {
      validator: (rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value && value.trim() !== '') {
          // 如果URL不为空，则验证格式
          const urlPattern = /^https?:\/\/\S+$/i
          if (!urlPattern.test(value)) {
            callback(new Error('请输入有效的 http/https URL'))
          } else {
            callback()
          }
        } else {
          // 如果URL为空，则通过验证
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  description: [{ required: true, message: '请输入网站描述', trigger: 'blur' }],
  categoryIds: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

const aiWebsiteRules = {
  url: [{ required: true, message: '请输入网站地址', trigger: 'blur' }],
}

const aiWebsiteEditRules = {
  name: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入网站描述', trigger: 'blur' }],
  categoryIds: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

const categoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入分类描述', trigger: 'blur' }],
  color: [{ required: true, message: '请选择分类颜色', trigger: 'change' }],
}

const aiCategoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入分类描述', trigger: 'blur' }],
  color: [{ required: true, message: '请选择分类颜色', trigger: 'change' }],
}

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 排序后的分类列表
const sortedCategories = computed(() => {
  return [...categories.value].sort((a, b) => a.sortOrder - b.sortOrder)
})

// 加载网站列表
const loadWebsites = async () => {
  loading.value = true
  try {
    const requestParams = {
      page: currentPage.value,
      size: pageSize.value,
      categoryIds: selectedCategory.value === '' ? undefined : [selectedCategory.value],
      keyword: searchKeyword.value || undefined,
    }
    console.log('loadWebsites 请求参数:', requestParams)
    console.log('categoryIds 值:', requestParams.categoryIds)
    console.log('categoryIds 类型:', typeof requestParams.categoryIds)

    const response = await websiteApi.getWebsites(requestParams)
    websites.value = response.websites || []
    total.value = response.totalCount || 0

    // 只在第一次加载时更新所有网站的总数量
    if (totalAllWebsites.value === 0) {
      totalAllWebsites.value = response.totalCount || 0
    }
  } catch (error) {
    ElMessage.error('加载网站列表失败')
    console.error('加载网站失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await categoryApi.getAllCategories()
    categories.value = response || []
  } catch (error) {
    ElMessage.error('加载分类列表失败')
    console.error('加载分类失败:', error)
  }
}

// 加载所有网站的总数量（不受分类筛选影响）
const loadTotalAllWebsites = async () => {
  try {
    // 调用API获取所有网站的总数量，不传任何筛选条件
    const response = await websiteApi.getWebsites({
      page: 1,
      size: 1, // 只需要获取总数，不需要实际数据
    })
    totalAllWebsites.value = response.totalCount || 0
  } catch (error) {
    console.error('加载所有网站总数失败:', error)
    // 如果失败，不影响主要功能，使用默认值
    totalAllWebsites.value = 0
  }
}

// 获取所有分类的总网站数量（来自后端返回的 websiteCount）
const getTotalCount = () => {
  // 始终返回所有网站的总数量，不受分类筛选影响
  const count = totalAllWebsites.value
  console.log('getTotalCount 返回:', count, '(不受分类筛选影响)')
  return count
}

// 拖拽排序相关方法
const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
  if (isEditMode.value) {
    // 进入编辑模式时，保存原始顺序
    originalCategories.value = [...categories.value]
    ElMessage.info('拖拽分类项目可调整顺序，完成后点击"完成排序"保存')
  } else {
    // 退出编辑模式时，保存排序结果
    saveCategoryOrder()
  }
}

const handleDragStart = (index: number) => {
  if (!isEditMode.value) return
  draggedIndex.value = index
}

const handleDrop = (targetIndex: number) => {
  if (!isEditMode.value || draggedIndex.value === -1) return

  const sourceIndex = draggedIndex.value
  if (sourceIndex === targetIndex) return

  // 重新排序分类
  const newCategories = [...categories.value]
  const [movedCategory] = newCategories.splice(sourceIndex, 1)
  newCategories.splice(targetIndex, 0, movedCategory)

  // 更新排序字段
  newCategories.forEach((category, index) => {
    category.sortOrder = index
  })

  categories.value = newCategories
  draggedIndex.value = -1
}

const handleDragEnd = () => {
  draggedIndex.value = -1
}

const saveCategoryOrder = async () => {
  try {
    // 调用后端API保存分类顺序
    const orderData = categories.value.map((category, index) => ({
      id: category.id,
      sortOrder: index,
    }))

    // 调用后端API保存分类顺序
    await categoryApi.updateCategoryOrder(orderData)

    // 重新加载分类列表以确保数据同步
    await loadCategories()

    ElMessage.success('分类顺序保存成功')
  } catch (error) {
    ElMessage.error('保存分类顺序失败')
    console.error('保存分类顺序失败:', error)

    // 恢复原始顺序
    categories.value = [...originalCategories.value]
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 事件处理方法
const handleCategoryChange = async () => {
  console.log('handleCategoryChange 被调用，selectedCategory:', selectedCategory.value)
  console.log('selectedCategory 类型:', typeof selectedCategory.value)

  currentPage.value = 1
  jumpPage.value = 1
  await loadWebsites()
}

// AI新增网站相关方法
const showAICreateWebsiteDialog = () => {
  aiCreateWebsiteDialogVisible.value = true
  aiWebsiteForm.value.url = ''
  aiWebsiteResult.value = null
}

const generateWebsiteWithAI = async () => {
  if (!aiWebsiteFormRef.value) return

  // 并发防护：避免重复触发
  if (aiGeneratingWebsite.value) return

  try {
    await aiWebsiteFormRef.value.validate()
    aiGeneratingWebsite.value = true

    // 抓取网站信息（API内部已经处理了重试逻辑）
    let scrapedInfo: {
      title?: string
      description?: string
      keywords?: string
      favicon?: string
    } | null = null

    try {
      scrapedInfo = await websiteApi.scrapeWebsiteInfo(aiWebsiteForm.value.url)
      console.log('网站信息抓取成功:', scrapedInfo)
    } catch (error) {
      console.error('网站信息抓取失败:', error)
      ElMessage.error('网站信息抓取失败，请检查网址后重试')
      throw error
    }

    lastScrapedInfo.value = scrapedInfo

    // 调用AI生成网站信息，传递完整的抓取信息
    const result = await generateWebsiteInfoWithOllama(
      aiWebsiteForm.value.url,
      scrapedInfo || {},
      categories.value,
    )

    aiWebsiteResult.value = result

    // 将AI生成的结果填充到编辑表单中
    aiWebsiteEditForm.value = {
      name: result.name,
      description: result.description,
      categoryIds: [result.categoryId],
    }

    ElMessage.success('AI生成完成，请检查并编辑信息')
  } catch (error) {
    ElMessage.error('AI生成失败，请重试')
    console.error('AI生成失败:', error)
  } finally {
    aiGeneratingWebsite.value = false
  }
}

const regenerateWebsiteWithAI = async () => {
  await generateWebsiteWithAI()
}

const handleFaviconError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

const saveAIGeneratedWebsite = async () => {
  if (!aiWebsiteEditFormRef.value) return

  try {
    await aiWebsiteEditFormRef.value.validate()
    savingAIWebsite.value = true

    // 准备网站创建请求，使用编辑表单的数据
    const websiteRequest: WebsiteCreateRequest = {
      name: aiWebsiteEditForm.value.name,
      url: aiWebsiteForm.value.url,
      description: aiWebsiteEditForm.value.description,
      categoryIds: aiWebsiteEditForm.value.categoryIds,
      // 仅使用后端抓取到的 favicon（网络链接），不使用 AI 返回的占位图标名
      icon: lastScrapedInfo.value?.favicon || '',
      favicon: lastScrapedInfo.value?.favicon,
    }

    // 创建网站
    await websiteApi.createWebsite(websiteRequest)

    ElMessage.success('网站创建成功')
    aiCreateWebsiteDialogVisible.value = false

    // 重新加载数据
    await loadWebsites()
    await loadCategories()
  } catch (error) {
    ElMessage.error('保存网站失败')
    console.error('保存网站失败:', error)
  } finally {
    savingAIWebsite.value = false
  }
}

const getCategoryName = (categoryId: number) => {
  const category = categories.value.find((c) => c.id === categoryId)
  return category ? category.name : '未知分类'
}

const getCategoryColor = (categoryId: number) => {
  const category = categories.value.find((c) => c.id === categoryId)
  return category ? category.color : '#909399'
}

const handleSearch = async () => {
  currentPage.value = 1
  jumpPage.value = 1
  await loadWebsites()
}

// 搜索框焦点处理方法
const handleSearchFocus = () => {
  isSearchFocused.value = true
}

const handleSearchBlur = () => {
  isSearchFocused.value = false
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  jumpPage.value = page
  loadWebsites()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  jumpPage.value = 1
  loadWebsites()
}

const handleJumpPage = () => {
  const page = parseInt(jumpPage.value.toString())
  if (page && page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadWebsites()
  } else {
    jumpPage.value = currentPage.value
    ElMessage.warning('请输入有效的页码')
  }
}

// 网站管理方法
const showAddWebsiteDialog = () => {
  editingWebsite.value = null
  resetWebsiteForm()
  websiteDialogVisible.value = true
}

const editWebsite = (website: Website) => {
  editingWebsite.value = { ...website }
  websiteForm.value = {
    name: website.name,
    url: website.url,
    description: website.description,
    categoryIds: website.categoryIds || [], // 使用现有的categoryIds
    icon: website.icon || '',
  }
  websiteDialogVisible.value = true
}

const resetWebsiteForm = () => {
  websiteForm.value = {
    name: '',
    url: '',
    description: '',
    categoryIds: [],
    icon: '',
  }
}

const saveWebsite = async () => {
  if (!websiteFormRef.value) return

  try {
    await websiteFormRef.value.validate()
    saving.value = true

    if (editingWebsite.value) {
      // 编辑现有网站
      await websiteApi.updateWebsite(editingWebsite.value.id, websiteForm.value)
      ElMessage.success('网站更新成功')
    } else {
      // 添加新网站
      await websiteApi.createWebsite(websiteForm.value)
      ElMessage.success('网站添加成功')
    }

    websiteDialogVisible.value = false
    editingWebsite.value = null
    resetWebsiteForm()
    loadWebsites() // 重新加载数据
  } catch (error) {
    ElMessage.error('保存失败，请重试')
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

const deleteWebsite = (website: Website) => {
  deletingWebsite.value = website
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!deletingWebsite.value) return

  deleting.value = true
  try {
    await websiteApi.deleteWebsite(deletingWebsite.value.id)

    ElMessage.success('网站删除成功')
    deleteDialogVisible.value = false
    deletingWebsite.value = null
    loadWebsites() // 重新加载数据
  } catch (error) {
    ElMessage.error('删除失败，请重试')
    console.error('删除失败:', error)
  } finally {
    deleting.value = false
  }
}

const visitWebsite = async (website: Website) => {
  try {
    // 增加访问次数
    await websiteApi.incrementVisitCount(website.id)

    // 更新本地数据
    website.visitCount++

    // 在新标签页打开网站
    window.open(website.url, '_blank')
  } catch (error) {
    console.error('访问统计失败:', error)
    // 即使统计失败，仍然打开网站
    window.open(website.url, '_blank')
  }
}

// 分类管理方法
const showAddCategoryDialog = () => {
  categoryForm.value = {
    name: '',
    description: '',
    color: '#409EFF',
    sortOrder: 0,
  }
  categoryDialogVisible.value = true
}

const showAIGenerateCategoryDialog = () => {
  aiCategoryForm.value = {
    name: '',
    description: '',
    color: '#409EFF',
    sortOrder: 0,
  }
  aiGeneratedResult.value = null
  aiGenerateCategoryDialogVisible.value = true
}

const saveCategory = async () => {
  if (!categoryFormRef.value) return

  try {
    await categoryFormRef.value.validate()
    savingCategory.value = true

    await categoryApi.createCategory(categoryForm.value)

    ElMessage.success('分类添加成功')
    categoryDialogVisible.value = false
    loadCategories() // 重新加载分类
  } catch (error) {
    ElMessage.error('保存失败，请重试')
    console.error('保存失败:', error)
  } finally {
    savingCategory.value = false
  }
}

const generateCategoryWithAI = async () => {
  if (!aiCategoryFormRef.value) return

  try {
    // 只验证分类名称字段，其他字段在AI生成后填充
    await aiCategoryFormRef.value.validateField('name')
    aiGenerating.value = true

    // 调用AI服务生成分类描述和颜色
    console.log('开始调用AI服务，分类名称:', aiCategoryForm.value.name)
    const result = await generateCategoryWithOllama(aiCategoryForm.value.name)
    console.log('AI服务返回结果:', result)

    // 将AI生成的结果填充到表单中，用户可以进一步编辑
    aiCategoryForm.value.description = result.description
    aiCategoryForm.value.color = result.color
    console.log('表单字段已填充:', {
      description: aiCategoryForm.value.description,
      color: aiCategoryForm.value.color,
    })

    // 标记AI生成完成，显示编辑字段
    aiGeneratedResult.value = {
      description: result.description,
      color: result.color,
    }

    ElMessage.success('AI生成完成，您可以进一步编辑内容')
  } catch (error) {
    ElMessage.error('AI生成失败，请重试')
    console.error('AI生成失败:', error)
  } finally {
    aiGenerating.value = false
  }
}

const saveAIGeneratedCategory = async () => {
  if (!aiGeneratedResult.value) return

  // 验证表单
  if (!aiCategoryFormRef.value) return

  try {
    await aiCategoryFormRef.value.validate()
    savingAICategory.value = true

    const categoryData = {
      name: aiCategoryForm.value.name,
      description: aiCategoryForm.value.description,
      color: aiCategoryForm.value.color,
      sortOrder: aiCategoryForm.value.sortOrder,
    }

    await categoryApi.createCategory(categoryData)

    ElMessage.success('AI生成分类保存成功')
    aiGenerateCategoryDialogVisible.value = false
    loadCategories() // 重新加载分类
  } catch (error) {
    ElMessage.error('保存失败，请重试')
    console.error('保存失败:', error)
  } finally {
    savingAICategory.value = false
  }
}

const handleIconError = (event: Event) => {
  const img = event.target as HTMLImageElement
  // 防止循环触发
  img.onerror = null
  // 使用内置的 SVG 占位图
  img.src = FALLBACK_ICON_DATA_URL
  img.style.display = 'block'
  img.classList?.add('placeholder-icon')
}

// 生命周期
onMounted(async () => {
  // 并行加载分类列表和所有网站总数
  await Promise.all([loadCategories(), loadTotalAllWebsites()])
  // 然后加载网站列表
  await loadWebsites()
})

// 监听器
watch([selectedCategory, searchKeyword], () => {
  currentPage.value = 1
})
</script>

<style scoped>
.website-collection {
  width: 98vw;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.header-content {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.title-icon {
  margin-right: 12px;
  font-size: 32px;
}

.page-description {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.category-filter {
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.category-filter:hover {
  transform: translateY(-2px);
  box-shadow:
    0 8px 30px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.15);
}

.category-filter::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  background-size: 200% 100%;
  animation: gradient-shift 3s ease-in-out infinite;
}

@keyframes gradient-shift {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.title-container {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  position: relative;
}

.title-icon {
  font-size: 20px;
  color: #667eea;
  margin-right: 12px;
  animation: icon-bounce 2s ease-in-out infinite;
}

@keyframes icon-bounce {
  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-8px);
  }
  60% {
    transform: translateY(-4px);
  }
}

.category-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a202c;
  margin-right: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-decoration {
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 2px;
  animation: decoration-grow 0.6s ease-out;
}

@keyframes decoration-grow {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 40px;
    opacity: 1;
  }
}

.category-subtitle {
  font-size: 14px;
  color: #718096;
  font-weight: 400;
  opacity: 0;
  animation: fade-in-up 0.6s ease-out 0.2s forwards;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.edit-mode-btn {
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.edit-mode-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.edit-mode-btn:hover::before {
  left: 100%;
}

.edit-mode-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.categories-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  animation: container-fade-in 0.8s ease-out;
}

@keyframes container-fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.categories-container.edit-mode {
  gap: 12px;
}

.category-item {
  position: relative;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: item-slide-in 0.6s ease-out;
  animation-fill-mode: both;
}

.category-item:nth-child(1) {
  animation-delay: 0.1s;
}
.category-item:nth-child(2) {
  animation-delay: 0.2s;
}
.category-item:nth-child(3) {
  animation-delay: 0.3s;
}
.category-item:nth-child(4) {
  animation-delay: 0.4s;
}
.category-item:nth-child(5) {
  animation-delay: 0.5s;
}
.category-item:nth-child(6) {
  animation-delay: 0.6s;
}
.category-item:nth-child(7) {
  animation-delay: 0.7s;
}
.category-item:nth-child(8) {
  animation-delay: 0.8s;
}
.category-item:nth-child(9) {
  animation-delay: 0.9s;
}
.category-item:nth-child(10) {
  animation-delay: 1s;
}
.category-item:nth-child(11) {
  animation-delay: 1.1s;
}
.category-item:nth-child(12) {
  animation-delay: 1.2s;
}

@keyframes item-slide-in {
  from {
    opacity: 0;
    transform: translateX(-30px) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

.category-item:hover {
  transform: translateY(-4px) scale(1.02);
  z-index: 10;
}

.category-item.selected .el-radio-button__inner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
  animation: selected-bounce 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes selected-bounce {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1.05);
  }
}

/* 移除空规则集，整合到具体子元素样式中 */

.category-item.all-category .el-radio-button__inner {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-color: #f093fb;
  color: white;
}

.category-item.all-category.selected .el-radio-button__inner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.category-icon {
  font-size: 16px;
  color: currentColor;
  transition: all 0.3s ease;
}

.category-text {
  font-weight: 500;
  transition: all 0.3s ease;
}

.category-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.category-item:hover .category-color-dot {
  transform: scale(1.2);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.count-tag {
  margin-left: 4px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.count-tag.pulse {
  animation: count-pulse 0.6s ease-in-out;
}

@keyframes count-pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.count-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s;
}

.count-tag:hover::before {
  left: 100%;
}

.drag-handle {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  font-size: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  transform: scale(0.8);
}

.drag-handle.visible {
  opacity: 1;
  transform: scale(1);
}

.drag-handle:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.drag-handle:active {
  cursor: grabbing;
  transform: scale(0.95);
}

.category-item.dragging {
  opacity: 0.6;
  transform: scale(0.95) rotate(3deg);
  z-index: 1000;
  cursor: grabbing;
  filter: drop-shadow(0 8px 25px rgba(0, 0, 0, 0.2));
}

.category-item.drag-over {
  border: 2px dashed #667eea;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.category-item.dragging .drag-handle {
  cursor: grabbing;
  transform: scale(1.2);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.8);
}

/* AI生成分类对话框样式 */
.ai-result {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
}

.result-item {
  margin-bottom: 16px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item label {
  display: block;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.result-item .description {
  margin: 0;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  color: #606266;
  line-height: 1.6;
}

.color-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-value {
  font-family: monospace;
  font-size: 14px;
  color: #606266;
  background: white;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.count-tag {
  margin-left: 8px;
}

.search-section {
  margin-bottom: 24px;
}

.search-container {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 24px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-container:hover {
  transform: translateY(-2px);
  box-shadow:
    0 8px 30px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.15);
}

.search-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #67c23a, #e6a23c, #f56c6c);
  animation: border-flow 3s ease-in-out infinite;
}

@keyframes border-flow {
  0%,
  100% {
    transform: translateX(-100%);
  }
  50% {
    transform: translateX(100%);
  }
}

.search-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 20px;
}

.search-title-container {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  position: relative;
}

.search-title-icon {
  font-size: 20px;
  color: #409eff;
  margin-right: 12px;
  animation: icon-bounce 2s ease-in-out infinite;
}

@keyframes icon-bounce {
  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-6px);
  }
  60% {
    transform: translateY(-3px);
  }
}

.search-title {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-right: 12px;
}

.search-title-decoration {
  width: 20px;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 2px;
  animation: decoration-grow 2s ease-in-out infinite;
}

@keyframes decoration-grow {
  0%,
  100% {
    width: 20px;
  }
  50% {
    width: 40px;
  }
}

.search-subtitle {
  font-size: 14px;
  color: #606266;
  opacity: 0;
  animation: fade-in-up 0.6s ease-out 0.3s forwards;
}

.search-status {
  margin-top: 8px;
  animation: fade-in-up 0.6s ease-out 0.4s forwards;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  flex: 1;
  max-width: 500px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e4e7ed;
  background: #ffffff;
  transition: all 0.3s cubic-bezier(0.4, 0.2, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-input.focused :deep(.el-input__wrapper) {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  transform: scale(1.02);
}

.search-input :deep(.el-input__inner) {
  font-size: 16px;
  padding: 12px 16px;
  color: #303133;
}

.search-input :deep(.el-input__prefix) {
  margin-right: 8px;
}

.search-icon {
  font-size: 18px;
  color: #909399;
  transition: all 0.3s ease;
}

.search-input.focused .search-icon {
  color: #409eff;
  transform: scale(1.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    padding: 20px;
    border-radius: 12px;
  }

  .search-header {
    margin-bottom: 16px;
  }

  .search-title {
    font-size: 18px;
  }

  .search-subtitle {
    font-size: 13px;
  }

  .search-input-wrapper {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-input {
    max-width: none;
  }
}

.websites-container {
  margin-bottom: 30px;
}

.loading-state {
  padding: 20px 0;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.websites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.website-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  position: relative;
}

.website-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.website-card.skeleton {
  cursor: default;
}

.website-card.skeleton:hover {
  transform: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #f0f0f0;
}

.card-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

.website-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.website-icon img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.website-icon img.placeholder-icon {
  width: 32px;
  height: 32px;
  object-fit: contain;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

.website-icon .el-icon {
  font-size: 24px;
  color: #909399;
}

.website-info {
  flex: 1;
  min-width: 0;
}

.website-name {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.website-url {
  margin: 0;
  font-size: 14px;
  color: #909399;
  word-break: break-all;
}

.card-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.website-card:hover .card-actions {
  opacity: 1;
}

.card-body {
  margin-bottom: 16px;
}

.website-description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  line-clamp: 2;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.website-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.visit-count,
.created-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.category-badge {
  flex-shrink: 0;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.category-tag {
  margin-right: 4px;
}

.category-tag:last-child {
  margin-right: 0;
}

.pagination-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 20px;
  padding: 0 20px;
}

.pagination-text {
  font-size: 14px;
  color: #606266;
}

.pagination-pages {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 20px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-selector .el-select {
  width: 80px;
}

.page-size-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.pagination-navigation {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-jumper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.jumper-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.jumper-input {
  width: 60px;
}

.jumper-text {
  font-size: 14px;
  color: #606266;
}

.main-pagination {
  margin: 0;
}

.delete-confirm {
  text-align: center;
  padding: 20px 0;
}

.warning-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.delete-tip {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

/* AI生成相关样式 */
.ai-placeholder {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  text-align: center;
}

.ai-result {
  margin-top: 16px;
  padding: 16px;
  background-color: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 6px;
}

.result-item {
  margin-bottom: 12px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item label {
  font-weight: 600;
  color: #374151;
  margin-right: 8px;
}

.description {
  margin: 8px 0;
  padding: 8px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.color-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-value {
  font-family: monospace;
  color: #6b7280;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .header-actions {
    justify-content: center;
  }

  .websites-grid {
    grid-template-columns: 1fr;
  }

  .category-filter {
    overflow-x: auto;
    padding: 20px;
    border-radius: 12px;
  }

  .category-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .title-container {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .categories-container {
    gap: 12px;
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .category-item {
    flex-shrink: 0;
  }

  .category-item:nth-child(n) {
    animation-delay: 0.1s;
  }

  .website-card {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .website-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }

  .card-actions {
    opacity: 1;
    position: absolute;
    top: 16px;
    right: 16px;
  }

  .pagination-container {
    padding: 16px;
  }

  .pagination-info {
    flex-direction: column;
    gap: 12px;
    text-align: center;
    padding: 0;
  }

  .pagination-controls {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }

  .pagination-navigation {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }

  .page-jumper {
    order: 1;
  }

  .main-pagination {
    order: 2;
  }
}

/* AI新增网站对话框样式 */
.ai-step {
  margin-bottom: 24px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.step-icon {
  font-size: 20px;
  color: #667eea;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.icon-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.favicon-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  object-fit: contain;
}

.favicon-placeholder {
  width: 24px;
  height: 24px;
  color: #6c757d;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-info {
  font-size: 14px;
  color: #6c757d;
  word-break: break-all;
}

/* 对话框底部按钮样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.dialog-footer .el-button {
  min-width: 100px;
}
</style>
