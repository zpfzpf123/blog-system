<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar,
  Delete,
  Edit,
  Filter,
  FolderOpened,
  Plus,
  RefreshRight,
  Search,
  Star,
  StarFilled,
  TrendCharts,
  View,
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import FolderSelectorDialog from '@/components/FolderSelectorDialog.vue'

defineOptions({
  name: 'ProjectManagerPage',
})

type ProjectStatus = '进行中' | '已完成' | '暂停' | '计划中'
type StatusFilter = ProjectStatus | ''
type StatusTagType = 'primary' | 'success' | 'warning' | 'info'
type GitUserSelectValue = number | string | undefined

interface ProjectApiItem {
  id: number
  name: string
  description?: string
  status?: string
  progress?: number | string
  techStack?: string[] | string | null
  localPath?: string
  repoUrl?: string
  gitCommits?: string
  gitUserId?: number | string
  isFavorite?: boolean
  createdAt?: string
  updatedAt?: string
}

interface Project {
  id: number
  name: string
  description: string
  status: ProjectStatus
  progress: number
  techStack: string[]
  localPath: string
  repoUrl: string
  gitCommits: string
  gitUserId?: number
  isFavorite: boolean
  createdAt: string
  updatedAt: string
}

interface ProjectForm {
  id?: number
  name: string
  description: string
  status: ProjectStatus
  progress: number
  techStack: string[]
  localPath: string
  repoUrl: string
  gitCommits: string
  gitUserId?: GitUserSelectValue
  isFavorite: boolean
}

interface ProjectPayload {
  name: string
  description: string
  status: ProjectStatus
  progress: number
  techStack: string
  localPath: string
  repoUrl: string
  gitCommits: string
  gitUserId?: number
  isFavorite: boolean
}

interface GitUser {
  id: number
  name: string
  username: string
  email?: string
  description?: string
  isDefault?: boolean
}

interface GitCommitItem {
  hash?: string
  author?: string
  date?: string
  message?: string
}

interface AnalyzeResult {
  projectName?: string
  readmeContent?: string
  gitRemoteUrl?: string
  gitCommits?: GitCommitItem[]
}

interface GitUserCreatePayload {
  name: string
  username: string
  password: string
  email: string
  description: string
}

interface GitUserCreateResponse {
  id: number
}

interface ApiErrorLike {
  response?: {
    data?: {
      message?: string
    }
  }
  message?: string
}

const statusOptions: Array<{ label: string; value: StatusFilter }> = [
  { label: '全部', value: '' },
  { label: '进行中', value: '进行中' },
  { label: '已完成', value: '已完成' },
  { label: '暂停', value: '暂停' },
  { label: '计划中', value: '计划中' },
]

const techOptions = [
  'Vue 3',
  'React',
  'Angular',
  'TypeScript',
  'JavaScript',
  'Node.js',
  'Express',
  'Nest.js',
  'Spring Boot',
  'MySQL',
  'MongoDB',
  'Redis',
  'PostgreSQL',
  'Docker',
  'Kubernetes',
  'CI/CD',
]

const statusTypeMap: Record<ProjectStatus, StatusTagType> = {
  进行中: 'primary',
  已完成: 'success',
  暂停: 'warning',
  计划中: 'info',
}

const router = useRouter()
const projects = ref<Project[]>([])
const gitUsers = ref<GitUser[]>([])
const loading = ref(false)
const savingProject = ref(false)
const analyzingProject = ref(false)
const addingGitUser = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新建项目')
const isEdit = ref(false)
const folderSelectorVisible = ref(false)
const addGitUserDialogVisible = ref(false)

const searchKeyword = ref('')
const statusFilter = ref<StatusFilter>('')

const formData = ref<ProjectForm>(createDefaultForm())
const newGitUserData = ref<GitUserCreatePayload>({
  name: '',
  username: '',
  password: '',
  email: '',
  description: '',
})

function createDefaultForm(): ProjectForm {
  return {
    name: '',
    description: '',
    status: '进行中',
    progress: 0,
    techStack: [],
    localPath: '',
    repoUrl: '',
    gitCommits: '',
    gitUserId: undefined,
    isFavorite: false,
  }
}

function isProjectStatus(value: string | undefined): value is ProjectStatus {
  return value === '进行中' || value === '已完成' || value === '暂停' || value === '计划中'
}

function normalizeProgress(value: unknown): number {
  const parsed = Number(value)
  if (Number.isNaN(parsed)) return 0
  return Math.max(0, Math.min(100, Math.round(parsed)))
}

function normalizeTechStack(value: unknown): string[] {
  if (Array.isArray(value)) {
    return value.map((item) => String(item).trim()).filter(Boolean)
  }

  if (typeof value === 'string') {
    const source = value.trim()
    if (!source) return []

    try {
      const parsed = JSON.parse(source) as unknown
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item).trim()).filter(Boolean)
      }
    } catch {
      return source
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)
    }
  }

  return []
}

function mapProjectFromApi(item: ProjectApiItem): Project {
  return {
    id: Number(item.id),
    name: typeof item.name === 'string' && item.name.trim() ? item.name.trim() : '未命名项目',
    description: typeof item.description === 'string' ? item.description : '',
    status: isProjectStatus(item.status) ? item.status : '计划中',
    progress: normalizeProgress(item.progress),
    techStack: normalizeTechStack(item.techStack),
    localPath: typeof item.localPath === 'string' ? item.localPath : '',
    repoUrl: typeof item.repoUrl === 'string' ? item.repoUrl : '',
    gitCommits: typeof item.gitCommits === 'string' ? item.gitCommits : '',
    gitUserId: typeof item.gitUserId === 'number' ? item.gitUserId : undefined,
    isFavorite: Boolean(item.isFavorite),
    createdAt: typeof item.createdAt === 'string' ? item.createdAt : '',
    updatedAt: typeof item.updatedAt === 'string' ? item.updatedAt : '',
  }
}

function buildProjectPayload(source: ProjectForm | Project): ProjectPayload {
  const normalizedTech = Array.from(
    new Set(source.techStack.map((item) => item.trim()).filter(Boolean)),
  )

  const payload: ProjectPayload = {
    name: source.name.trim(),
    description: source.description.trim(),
    status: source.status,
    progress: normalizeProgress(source.progress),
    techStack: JSON.stringify(normalizedTech),
    localPath: source.localPath.trim(),
    repoUrl: source.repoUrl.trim(),
    gitCommits: source.gitCommits || '',
    isFavorite: Boolean(source.isFavorite),
  }

  if (typeof source.gitUserId === 'number') {
    payload.gitUserId = source.gitUserId
  }

  return payload
}

function parseGitCommitCount(raw: string): number {
  if (!raw.trim()) return 0

  try {
    const parsed = JSON.parse(raw) as unknown
    if (Array.isArray(parsed)) return parsed.length
  } catch {
    return 0
  }

  return 0
}

function getErrorMessage(error: unknown, fallback: string): string {
  const resolved = error as ApiErrorLike
  return resolved.response?.data?.message || resolved.message || fallback
}

function isActionCancelled(error: unknown): boolean {
  return error === 'cancel' || error === 'close'
}

const statistics = computed(() => {
  const total = projects.value.length
  const inProgress = projects.value.filter((project) => project.status === '进行中').length
  const completed = projects.value.filter((project) => project.status === '已完成').length
  const favorites = projects.value.filter((project) => project.isFavorite).length

  return {
    total,
    inProgress,
    completed,
    favorites,
  }
})

const statCards = computed(() => [
  {
    key: 'total',
    label: '项目总数',
    hint: '全部项目',
    value: statistics.value.total,
    icon: TrendCharts,
    tone: 'is-blue',
  },
  {
    key: 'inProgress',
    label: '进行中',
    hint: '当前推进',
    value: statistics.value.inProgress,
    icon: Calendar,
    tone: 'is-cyan',
  },
  {
    key: 'completed',
    label: '已完成',
    hint: '交付归档',
    value: statistics.value.completed,
    icon: View,
    tone: 'is-green',
  },
  {
    key: 'favorites',
    label: '收藏项目',
    hint: '重点关注',
    value: statistics.value.favorites,
    icon: StarFilled,
    tone: 'is-gold',
  },
])

const filteredProjects = computed<Project[]>(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  const activeStatus = statusFilter.value

  const list = projects.value.filter((project) => {
    const keywordMatched = !keyword
      || project.name.toLowerCase().includes(keyword)
      || project.description.toLowerCase().includes(keyword)
      || project.techStack.some((tech) => tech.toLowerCase().includes(keyword))

    const statusMatched = !activeStatus || project.status === activeStatus
    return keywordMatched && statusMatched
  })

  return [...list].sort((prev, next) => {
    if (prev.isFavorite && !next.isFavorite) return -1
    if (!prev.isFavorite && next.isFavorite) return 1
    return new Date(next.updatedAt).getTime() - new Date(prev.updatedAt).getTime()
  })
})

const activeFilterLabel = computed(() => {
  const labels: string[] = []
  if (searchKeyword.value.trim()) {
    labels.push(`关键词: ${searchKeyword.value.trim()}`)
  }
  if (statusFilter.value) {
    labels.push(`状态: ${statusFilter.value}`)
  }
  return labels.length > 0 ? labels.join(' | ') : '当前未设置筛选条件'
})

const showAddGitUserButton = computed(() => {
  if (typeof formData.value.gitUserId !== 'string') return false

  const inputName = formData.value.gitUserId.trim().toLowerCase()
  if (!inputName) return false

  return !gitUsers.value.some((user) => user.username.toLowerCase() === inputName)
})

const gitCommitCountForForm = computed(() => parseGitCommitCount(formData.value.gitCommits))

const fetchGitUsers = async () => {
  try {
    const response = await axios.get<GitUser[]>('/api/git-users')
    gitUsers.value = response.data
  } catch (error) {
    console.warn('获取 Git 用户列表失败:', error)
  }
}

const fetchProjects = async () => {
  try {
    loading.value = true
    const response = await axios.get<ProjectApiItem[]>('/api/projects')
    projects.value = response.data.map(mapProjectFromApi)
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error(getErrorMessage(error, '获取项目列表失败'))
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  dialogTitle.value = '新建项目'
  isEdit.value = false
  formData.value = createDefaultForm()
  dialogVisible.value = true
}

const openEditDialog = (project: Project) => {
  dialogTitle.value = '编辑项目'
  isEdit.value = true
  formData.value = {
    id: project.id,
    name: project.name,
    description: project.description,
    status: project.status,
    progress: project.progress,
    techStack: [...project.techStack],
    localPath: project.localPath,
    repoUrl: project.repoUrl,
    gitCommits: project.gitCommits,
    gitUserId: project.gitUserId,
    isFavorite: project.isFavorite,
  }
  dialogVisible.value = true
}

const saveProject = async () => {
  if (!formData.value.name.trim()) {
    ElMessage.warning('请输入项目名称')
    return
  }

  try {
    savingProject.value = true
    const payload = buildProjectPayload(formData.value)

    if (isEdit.value && formData.value.id) {
      await axios.put(`/api/projects/${formData.value.id}`, payload)
      ElMessage.success('项目更新成功')
    } else {
      await axios.post('/api/projects', payload)
      ElMessage.success('项目创建成功')
    }

    dialogVisible.value = false
    await fetchProjects()
  } catch (error) {
    console.error('保存项目失败:', error)
    ElMessage.error(getErrorMessage(error, '保存项目失败'))
  } finally {
    savingProject.value = false
  }
}

const deleteProject = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该项目吗？删除后不可恢复。', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await axios.delete(`/api/projects/${id}`)
    ElMessage.success('项目已删除')
    await fetchProjects()
  } catch (error) {
    if (isActionCancelled(error)) return
    console.error('删除项目失败:', error)
    ElMessage.error(getErrorMessage(error, '删除项目失败'))
  }
}

const toggleFavorite = async (project: Project) => {
  const previousValue = project.isFavorite
  project.isFavorite = !project.isFavorite

  try {
    await axios.put(`/api/projects/${project.id}`, buildProjectPayload(project))
    ElMessage.success(project.isFavorite ? '已加入收藏' : '已取消收藏')
  } catch (error) {
    project.isFavorite = previousValue
    console.error('更新收藏状态失败:', error)
    ElMessage.error(getErrorMessage(error, '收藏状态更新失败'))
  }
}

const openFolderSelector = () => {
  folderSelectorVisible.value = true
}

const analyzeProject = async (path: string) => {
  try {
    analyzingProject.value = true
    ElMessage.info('正在解析项目目录...')

    const response = await axios.post<AnalyzeResult>(
      '/api/filesystem/analyze',
      JSON.stringify(path),
      {
        headers: {
          'Content-Type': 'application/json',
        },
      },
    )

    const result = response.data

    if (result.projectName && !formData.value.name.trim()) {
      formData.value.name = result.projectName
    }
    if (result.readmeContent) {
      formData.value.description = result.readmeContent
    }
    if (result.gitRemoteUrl) {
      formData.value.repoUrl = result.gitRemoteUrl
    }
    if (result.gitCommits && result.gitCommits.length > 0) {
      formData.value.gitCommits = JSON.stringify(result.gitCommits)
      ElMessage.success(`解析成功，读取到 ${result.gitCommits.length} 条提交记录`)
      return
    }

    ElMessage.success('项目解析成功')
  } catch (error) {
    console.error('解析项目失败:', error)
    ElMessage.error(getErrorMessage(error, '项目解析失败'))
  } finally {
    analyzingProject.value = false
  }
}

const handleFolderSelected = async (path: string) => {
  formData.value.localPath = path
  await analyzeProject(path)
}

const openAddGitUserDialog = () => {
  if (typeof formData.value.gitUserId !== 'string') return

  const username = formData.value.gitUserId.trim()
  if (!username) {
    ElMessage.warning('请先输入新的 Git 用户名')
    return
  }

  newGitUserData.value = {
    name: username,
    username,
    password: '',
    email: '',
    description: '',
  }
  addGitUserDialogVisible.value = true
}

const addGitUser = async () => {
  if (!newGitUserData.value.username.trim() || !newGitUserData.value.password.trim()) {
    ElMessage.warning('请输入用户名和 Token')
    return
  }

  try {
    addingGitUser.value = true
    const response = await axios.post<GitUserCreateResponse>('/api/git-users', newGitUserData.value)

    await fetchGitUsers()

    if (typeof response.data.id === 'number') {
      formData.value.gitUserId = response.data.id
    }

    addGitUserDialogVisible.value = false
    ElMessage.success('Git 用户创建成功')
  } catch (error) {
    console.error('创建 Git 用户失败:', error)
    ElMessage.error(getErrorMessage(error, '创建 Git 用户失败'))
  } finally {
    addingGitUser.value = false
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
}

const getStatusCount = (status: StatusFilter) => {
  if (!status) return projects.value.length
  return projects.value.filter((project) => project.status === status).length
}

const getStatusType = (status: ProjectStatus) => statusTypeMap[status]

const getProgressColor = (progress: number) => {
  if (progress >= 80) return '#1fa971'
  if (progress >= 55) return '#3d78ff'
  if (progress >= 30) return '#f2a53c'
  return '#e46a8a'
}

const formatDate = (dateValue: string) => {
  if (!dateValue) return '--'
  const date = new Date(dateValue)
  if (Number.isNaN(date.getTime())) return '--'
  return date.toLocaleDateString('zh-CN')
}

const viewProjectDetail = (project: Project) => {
  router.push({
    name: 'ProjectDetail',
    params: { id: project.id },
  })
}

onMounted(() => {
  void Promise.all([fetchProjects(), fetchGitUsers()])
})
</script>

<template>
  <div class="project-page">
    <div class="page-bg-orb orb-left"></div>
    <div class="page-bg-orb orb-right"></div>

    <section class="hero-panel">
      <div class="hero-main">
        <p class="hero-kicker">PROJECT WORKSPACE</p>
        <h1 class="hero-title">项目管理中心</h1>
        <p class="hero-subtitle">
          以卡片视图集中管理项目进度、技术栈和仓库信息，从概览到详情只需一次点击。
        </p>
      </div>

      <div class="hero-actions">
        <el-button class="hero-btn hero-btn-light" :icon="RefreshRight" @click="fetchProjects">
          刷新列表
        </el-button>
        <el-button class="hero-btn hero-btn-primary" type="primary" :icon="Plus" @click="openCreateDialog">
          新建项目
        </el-button>
      </div>

      <div class="stats-grid">
        <article
          v-for="item in statCards"
          :key="item.key"
          class="stat-card"
          :class="item.tone"
        >
          <div class="stat-icon">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="stat-text">
            <p class="stat-label">{{ item.label }}</p>
            <p class="stat-value">{{ item.value }}</p>
            <p class="stat-hint">{{ item.hint }}</p>
          </div>
        </article>
      </div>
    </section>

    <section class="controls-panel">
      <div class="search-row">
        <el-input
          v-model="searchKeyword"
          class="search-input"
          :prefix-icon="Search"
          clearable
          placeholder="搜索项目名称、描述或技术栈"
        />
        <el-button
          class="reset-btn"
          :disabled="!searchKeyword.trim() && !statusFilter"
          @click="resetFilters"
        >
          清空筛选
        </el-button>
      </div>

      <div class="status-pills">
        <button
          v-for="item in statusOptions"
          :key="item.value || 'all'"
          class="status-pill"
          :class="{ 'is-active': statusFilter === item.value }"
          type="button"
          @click="statusFilter = item.value"
        >
          <span>{{ item.label }}</span>
          <em>{{ getStatusCount(item.value) }}</em>
        </button>
      </div>

      <p class="filter-tip">
        <el-icon><Filter /></el-icon>
        <span>{{ activeFilterLabel }}</span>
      </p>
    </section>

    <section v-loading="loading" class="cards-panel">
      <el-empty v-if="!loading && filteredProjects.length === 0" description="暂无项目，点击“新建项目”开始">
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">创建第一个项目</el-button>
      </el-empty>

      <article
        v-for="project in filteredProjects"
        :key="project.id"
        class="project-card"
        :class="{ 'is-favorite': project.isFavorite }"
      >
        <header class="card-header">
          <button class="favorite-btn" type="button" @click="toggleFavorite(project)">
            <el-icon>
              <StarFilled v-if="project.isFavorite" />
              <Star v-else />
            </el-icon>
          </button>

          <div class="title-block">
            <h3 class="project-name">{{ project.name }}</h3>
            <p class="project-repo">{{ project.repoUrl || '未配置仓库地址' }}</p>
          </div>

          <el-tag class="status-tag" :type="getStatusType(project.status)" round>
            {{ project.status }}
          </el-tag>
        </header>

        <p class="project-description">
          {{ project.description || '暂无项目描述，可在编辑中补充目标、里程碑和关键实现。' }}
        </p>

        <div class="progress-card">
          <div class="progress-top">
            <span>项目进度</span>
            <strong>{{ project.progress }}%</strong>
          </div>
          <el-progress
            :percentage="project.progress"
            :stroke-width="10"
            :color="getProgressColor(project.progress)"
          />
        </div>

        <div class="tech-stack" v-if="project.techStack.length > 0">
          <el-tag
            v-for="tech in project.techStack.slice(0, 5)"
            :key="tech"
            class="tech-chip"
            effect="plain"
          >
            {{ tech }}
          </el-tag>
          <span v-if="project.techStack.length > 5" class="more-tech">
            +{{ project.techStack.length - 5 }}
          </span>
        </div>
        <div class="tech-stack empty-tech" v-else>
          <span>尚未配置技术栈</span>
        </div>

        <div class="meta-row">
          <span>
            <el-icon><Calendar /></el-icon>
            创建 {{ formatDate(project.createdAt) }}
          </span>
          <span>
            <el-icon><Calendar /></el-icon>
            更新 {{ formatDate(project.updatedAt) }}
          </span>
        </div>

        <footer class="card-actions">
          <el-button text bg :icon="View" @click="viewProjectDetail(project)">详情</el-button>
          <el-button text bg :icon="Edit" @click="openEditDialog(project)">编辑</el-button>
          <el-button text bg type="danger" :icon="Delete" @click="deleteProject(project.id)">
            删除
          </el-button>
        </footer>
      </article>
    </section>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="760px"
      class="project-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-position="top" class="project-form">
        <div class="form-grid">
          <el-form-item label="项目名称" required class="span-2">
            <el-input v-model="formData.name" placeholder="例如：企业知识库平台" />
          </el-form-item>

          <el-form-item label="项目状态">
            <el-select v-model="formData.status" style="width: 100%">
              <el-option
                v-for="item in statusOptions.filter((option) => option.value)"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="项目进度">
            <el-slider v-model="formData.progress" :min="0" :max="100" :step="5" show-input />
          </el-form-item>

          <el-form-item label="项目描述" class="span-2">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="8"
              :autosize="{ minRows: 8, maxRows: 18 }"
              placeholder="简要描述项目目标、功能范围和当前阶段"
            />
          </el-form-item>

          <el-form-item label="技术栈" class="span-2">
            <el-select
              v-model="formData.techStack"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入技术栈标签"
              style="width: 100%"
            >
              <el-option v-for="tech in techOptions" :key="tech" :label="tech" :value="tech" />
            </el-select>
          </el-form-item>

          <el-form-item label="本地路径" class="span-2">
            <div class="path-picker">
              <el-input
                v-model="formData.localPath"
                readonly
                placeholder="点击右侧按钮选择项目文件夹"
              />
              <el-button :icon="FolderOpened" :loading="analyzingProject" @click="openFolderSelector">
                {{ analyzingProject ? '解析中...' : '选择文件夹' }}
              </el-button>
            </div>
            <p v-if="gitCommitCountForForm > 0" class="inline-hint success">
              已读取 {{ gitCommitCountForForm }} 条 Git 提交记录
            </p>
          </el-form-item>

          <el-form-item label="仓库地址" class="span-2">
            <el-input v-model="formData.repoUrl" placeholder="例如：https://github.com/username/project" />
          </el-form-item>

          <el-form-item label="Git 用户" class="span-2">
            <div class="git-user-row">
              <el-select
                v-model="formData.gitUserId"
                filterable
                allow-create
                clearable
                placeholder="选择已有用户或输入新用户名"
                style="flex: 1"
              >
                <el-option
                  v-for="user in gitUsers"
                  :key="user.id"
                  :label="`${user.name} (${user.username})`"
                  :value="user.id"
                />
              </el-select>
              <el-button v-if="showAddGitUserButton" type="primary" plain @click="openAddGitUserDialog">
                新增用户
              </el-button>
            </div>
            <p class="inline-hint">可直接输入用户名，再点击“新增用户”快速创建。</p>
          </el-form-item>

          <el-form-item label="收藏项目" class="span-2">
            <el-switch v-model="formData.isFavorite" active-text="已收藏" inactive-text="未收藏" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingProject" @click="saveProject">保存项目</el-button>
      </template>
    </el-dialog>

    <FolderSelectorDialog
      v-model="folderSelectorVisible"
      @confirm="handleFolderSelected"
    />

    <el-dialog
      v-model="addGitUserDialogVisible"
      title="新增 Git 用户"
      width="560px"
      :close-on-click-modal="false"
      class="project-dialog"
    >
      <el-form :model="newGitUserData" label-position="top">
        <el-form-item label="显示名称" required>
          <el-input v-model="newGitUserData.name" placeholder="例如：工作账号" />
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="newGitUserData.username" placeholder="Git 用户名" />
        </el-form-item>
        <el-form-item label="Token" required>
          <el-input
            v-model="newGitUserData.password"
            type="password"
            show-password
            placeholder="Personal Access Token"
          />
          <p class="inline-hint">用于 Git 操作鉴权，请妥善保管。</p>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="newGitUserData.email" placeholder="可选" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newGitUserData.description" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addGitUserDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addingGitUser" @click="addGitUser">创建用户</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.project-page {
  --pm-bg: #f4f8ff;
  --pm-surface: #ffffff;
  --pm-surface-soft: #f7faff;
  --pm-text: #1f2f4e;
  --pm-text-soft: #627393;
  --pm-border: #dfe7f5;
  --pm-accent: #3869ff;
  --pm-accent-soft: #e8efff;
  --pm-teal: #35b4ad;
  --pm-green: #34b57f;
  --pm-gold: #f0a32d;
  --pm-danger: #e46a8a;
  --pm-shadow: 0 24px 48px rgba(39, 73, 142, 0.1);
  --pm-shadow-soft: 0 12px 24px rgba(39, 73, 142, 0.08);
  position: relative;
  min-height: 100vh;
  padding: 28px clamp(14px, 3vw, 40px) 34px;
  overflow: hidden;
  color: var(--pm-text);
  font-family: Manrope, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background:
    radial-gradient(circle at 8% 6%, #e9f1ff 0, #e9f1ff 17%, transparent 48%),
    radial-gradient(circle at 94% 12%, #eef9ff 0, #eef9ff 18%, transparent 50%),
    linear-gradient(180deg, #f9fbff 0%, var(--pm-bg) 100%);
}

.page-bg-orb {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(2px);
  opacity: 0.7;
  animation: floatY 10s ease-in-out infinite;
}

.orb-left {
  width: 220px;
  height: 220px;
  left: -95px;
  top: 320px;
  background: radial-gradient(circle at 30% 30%, #dce8ff 0, #eaf1ff 50%, transparent 100%);
}

.orb-right {
  width: 280px;
  height: 280px;
  right: -130px;
  top: 120px;
  background: radial-gradient(circle at 40% 40%, #d9f7f4 0, #ebfbf9 58%, transparent 100%);
  animation-delay: 2s;
}

.hero-panel {
  position: relative;
  z-index: 1;
  border: 1px solid var(--pm-border);
  border-radius: 26px;
  padding: clamp(20px, 3vw, 30px);
  background: linear-gradient(135deg, #ffffff 0%, #f4f8ff 100%);
  box-shadow: var(--pm-shadow);
  margin-bottom: 24px;
}

.hero-main {
  max-width: 760px;
}

.hero-kicker {
  margin: 0 0 10px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 700;
  color: #7f93b7;
}

.hero-title {
  margin: 0;
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.1;
  font-weight: 800;
  color: #1a2a49;
}

.hero-subtitle {
  margin: 14px 0 0;
  font-size: 15px;
  line-height: 1.7;
  color: var(--pm-text-soft);
}

.hero-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-btn {
  height: 42px;
  border-radius: 12px;
  padding: 0 18px;
  font-weight: 600;
}

.hero-btn-light {
  border: 1px solid #cedaf4;
  color: #315cba;
  background: #f5f8ff;
}

.hero-btn-light:hover {
  color: #214a9e;
  border-color: #b7caee;
  background: #ebf2ff;
}

.hero-btn-primary {
  background: linear-gradient(135deg, #4e7cff 0%, #3a66f5 100%);
  border-color: transparent;
}

.stats-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  border-radius: 18px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid transparent;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--pm-shadow-soft);
}

.stat-card.is-blue {
  background: linear-gradient(160deg, #ecf2ff 0%, #f7f9ff 100%);
  border-color: #d4e0fb;
}

.stat-card.is-cyan {
  background: linear-gradient(160deg, #eafaf9 0%, #f8fdfd 100%);
  border-color: #ccefea;
}

.stat-card.is-green {
  background: linear-gradient(160deg, #effbf5 0%, #f8fdfb 100%);
  border-color: #d8efdf;
}

.stat-card.is-gold {
  background: linear-gradient(160deg, #fff5e8 0%, #fffaf2 100%);
  border-color: #f5e2bf;
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #355baf;
  background: rgba(255, 255, 255, 0.82);
}

.stat-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  margin: 0;
  font-size: 12px;
  color: #7487a8;
}

.stat-value {
  margin: 0;
  font-size: 24px;
  line-height: 1;
  font-weight: 800;
  color: #20365f;
}

.stat-hint {
  margin: 0;
  font-size: 12px;
  color: #8898b5;
}

.controls-panel {
  position: relative;
  z-index: 1;
  margin-bottom: 22px;
  border-radius: 20px;
  border: 1px solid var(--pm-border);
  background: var(--pm-surface);
  padding: 16px 18px;
  box-shadow: var(--pm-shadow-soft);
}

.search-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  flex: 1;
}

.reset-btn {
  border-radius: 10px;
  border: 1px solid #d2ddf5;
  color: #4b6698;
}

.status-pills {
  margin-top: 14px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.status-pill {
  border: 1px solid #d2ddf5;
  background: #f8fbff;
  color: #4f648d;
  border-radius: 999px;
  padding: 7px 12px 7px 14px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.status-pill em {
  font-style: normal;
  min-width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 999px;
  background: rgba(54, 102, 214, 0.12);
  color: #3561c6;
  font-size: 12px;
}

.status-pill:hover {
  border-color: #b5caef;
  color: #3458a8;
}

.status-pill.is-active {
  background: linear-gradient(135deg, #4e7cff 0%, #3d6df9 100%);
  border-color: transparent;
  color: #ffffff;
}

.status-pill.is-active em {
  background: rgba(255, 255, 255, 0.22);
  color: #ffffff;
}

.filter-tip {
  margin: 12px 0 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6f81a0;
}

.cards-panel {
  position: relative;
  z-index: 1;
  min-height: 320px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 16px;
}

.project-card {
  border-radius: 20px;
  border: 1px solid #dbe5f5;
  background: linear-gradient(180deg, #ffffff 0%, var(--pm-surface-soft) 100%);
  box-shadow: 0 14px 30px rgba(41, 71, 126, 0.08);
  padding: 18px;
  display: flex;
  flex-direction: column;
  min-height: 350px;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.project-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 36px rgba(41, 71, 126, 0.12);
  border-color: #c4d5f2;
}

.project-card.is-favorite {
  border-color: #f1d39f;
  box-shadow: 0 20px 34px rgba(240, 163, 45, 0.16);
}

.card-header {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: flex-start;
  gap: 10px;
}

.favorite-btn {
  width: 34px;
  height: 34px;
  border: 1px solid #d8e0f0;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  color: #96a4be;
  cursor: pointer;
  transition: all 0.2s ease;
}

.favorite-btn:hover {
  transform: translateY(-1px);
  border-color: #f0bf68;
  color: var(--pm-gold);
}

.project-card.is-favorite .favorite-btn {
  border-color: #f0bf68;
  color: var(--pm-gold);
  background: #fff7eb;
}

.title-block {
  min-width: 0;
}

.project-name {
  margin: 2px 0 0;
  font-size: 20px;
  line-height: 1.25;
  font-weight: 800;
  color: #21365f;
  word-break: break-all;
}

.project-repo {
  margin: 6px 0 0;
  font-size: 12px;
  color: #7a8baa;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  margin-top: 2px;
}

.project-description {
  margin: 14px 0;
  font-size: 14px;
  line-height: 1.72;
  color: #556a8f;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  min-height: 72px;
}

.progress-card {
  border: 1px solid #dce6f6;
  background: #f9fbff;
  border-radius: 14px;
  padding: 12px;
}

.progress-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #5f739a;
}

.progress-top strong {
  color: #284780;
  font-weight: 700;
}

.tech-stack {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-height: 32px;
}

.tech-chip {
  border-radius: 999px;
}

.more-tech {
  font-size: 12px;
  color: #6e7f9f;
  padding: 4px 9px;
  border-radius: 999px;
  background: #edf2fc;
}

.empty-tech {
  font-size: 12px;
  color: #91a0ba;
}

.meta-row {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #7283a3;
}

.meta-row span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.card-actions {
  margin-top: 12px;
  border-top: 1px solid #e0e8f7;
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.project-form {
  padding-right: 4px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 14px;
}

.span-2 {
  grid-column: span 2;
}

.path-picker {
  display: flex;
  gap: 10px;
}

.git-user-row {
  display: flex;
  gap: 10px;
}

.inline-hint {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #7a8eaf;
}

.inline-hint.success {
  color: #1fa971;
}

:deep(.project-dialog .el-dialog) {
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid #dbe6f7;
}

:deep(.project-dialog .el-dialog__header) {
  margin: 0;
  padding: 18px 22px 16px;
  border-bottom: 1px solid #e2eafa;
  background: linear-gradient(180deg, #f6f9ff 0%, #ffffff 100%);
}

:deep(.project-dialog .el-dialog__title) {
  font-size: 17px;
  font-weight: 700;
  color: #233a64;
}

:deep(.project-dialog .el-dialog__body) {
  padding: 18px 22px;
}

:deep(.project-dialog .el-input__wrapper),
:deep(.project-dialog .el-textarea__inner),
:deep(.project-dialog .el-select__wrapper) {
  border-radius: 12px;
}

:deep(.project-dialog .el-input__wrapper.is-focus),
:deep(.project-dialog .el-select__wrapper.is-focused),
:deep(.project-dialog .el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #5f86f4 inset;
}

@keyframes floatY {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-12px);
  }
}

@media (max-width: 1320px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .cards-panel {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 960px) {
  .project-page {
    padding: 18px 14px 28px;
  }

  .hero-title {
    font-size: 30px;
  }

  .hero-actions {
    width: 100%;
  }

  .hero-btn {
    flex: 1;
    min-width: 130px;
  }

  .search-row {
    flex-direction: column;
    align-items: stretch;
  }

  .cards-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: span 1;
  }

  .path-picker,
  .git-user-row {
    flex-direction: column;
  }

  .project-card {
    min-height: 0;
  }
}
</style>
