<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View, Search, Filter, Star, StarFilled, Calendar, TrendCharts, FolderOpened } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from '@/utils/axios'
import FolderSelectorDialog from '@/components/FolderSelectorDialog.vue'

// 类型定义
interface Project {
  id: number
  name: string
  description: string
  coverImage?: string
  status: '进行中' | '已完成' | '暂停' | '计划中'
  progress: number
  techStack: string[]
  localPath?: string
  repoUrl?: string
  readmeContent?: string
  gitCommits?: string
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

interface GitCommit {
  hash: string
  author: string
  date: string
  message: string
}

// 状态管理
const router = useRouter()
const projects = ref<Project[]>([])
const gitUsers = ref<GitUser[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建项目')
const isEdit = ref(false)
const searchKeyword = ref('')
const statusFilter = ref<string>('')
const folderSelectorVisible = ref(false)
const analyzingProject = ref(false)

// 表单数据
const formData = ref<Partial<Project>>({
  name: '',
  description: '',
  status: '进行中',
  progress: 0,
  techStack: [],
  localPath: '',
  repoUrl: '',
  readmeContent: '',
  gitCommits: '',
  isFavorite: false,
})

// 技术栈选项
const techOptions = [
  'Vue 3', 'React', 'Angular', 'TypeScript', 'JavaScript',
  'Spring Boot', 'Node.js', 'Express', 'Nest.js',
  'MySQL', 'MongoDB', 'Redis', 'PostgreSQL',
  'Docker', 'Kubernetes', 'CI/CD'
]

// 状态选项
const statusOptions = [
  { label: '全部', value: '' },
  { label: '进行中', value: '进行中' },
  { label: '已完成', value: '已完成' },
  { label: '暂停', value: '暂停' },
  { label: '计划中', value: '计划中' },
]

// 计算属性 - 筛选后的项目列表
const filteredProjects = computed(() => {
  let result = projects.value

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (p) =>
        p.name.toLowerCase().includes(keyword) ||
        p.description?.toLowerCase().includes(keyword) ||
        p.techStack.some((tech) => tech.toLowerCase().includes(keyword))
    )
  }

  // 按状态筛选
  if (statusFilter.value) {
    result = result.filter((p) => p.status === statusFilter.value)
  }

  // 收藏项目排在前面
  return result.sort((a, b) => {
    if (a.isFavorite && !b.isFavorite) return -1
    if (!a.isFavorite && b.isFavorite) return 1
    return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
  })
})

// 统计数据
const statistics = computed(() => {
  return {
    total: projects.value.length,
    inProgress: projects.value.filter((p) => p.status === '进行中').length,
    completed: projects.value.filter((p) => p.status === '已完成').length,
    paused: projects.value.filter((p) => p.status === '暂停').length,
  }
})

// API 方法
// 获取Git用户列表
const fetchGitUsers = async () => {
  try {
    const response = await axios.get('/api/git-users')
    gitUsers.value = response.data
  } catch (error) {
    console.error('获取Git用户列表失败:', error)
    // 不显示错误，因为Git用户可能还没有配置
  }
}

const fetchProjects = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/projects')
    // 解析techStack字段（从JSON字符串转为数组）
    projects.value = response.data.map((project: any) => {
      let techStack = []
      try {
        if (typeof project.techStack === 'string' && project.techStack.trim()) {
          const parsed = JSON.parse(project.techStack)
          techStack = Array.isArray(parsed) ? parsed.filter(t => t && t.trim()) : []
        } else if (Array.isArray(project.techStack)) {
          techStack = project.techStack.filter(t => t && t.trim())
        }
      } catch (e) {
        console.warn('解析techStack失败:', project.id, e)
        techStack = []
      }
      return {
        ...project,
        techStack
      }
    })
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新建项目'
  formData.value = {
    name: '',
    description: '',
    status: '进行中',
    progress: 0,
    techStack: [],
    localPath: '',
    repoUrl: '',
    readmeContent: '',
    gitCommits: '',
    gitUserId: undefined,
    isFavorite: false,
  }
  dialogVisible.value = true
}

// 打开文件夹选择器
const openFolderSelector = () => {
  folderSelectorVisible.value = true
}

// 处理文件夹选择
const handleFolderSelected = async (path: string) => {
  console.log('选择的文件夹:', path)
  formData.value.localPath = path
  
  // 自动解析项目
  await analyzeProject(path)
}

// 解析项目
const analyzeProject = async (path: string) => {
  try {
    analyzingProject.value = true
    ElMessage.info('正在解析项目...')
    
    const response = await axios.post('/api/filesystem/analyze', JSON.stringify(path), {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    const result = response.data
    console.log('项目解析结果:', result)
    
    // 自动填充项目名称
    if (result.projectName && !formData.value.name) {
      formData.value.name = result.projectName
    }
    
    // 自动填充README内容
    if (result.readmeContent) {
      formData.value.readmeContent = result.readmeContent
      formData.value.description = result.readmeContent // 使用完整README内容作为描述
    }
    
    // 自动填充Git远程仓库地址
    if (result.gitRemoteUrl) {
      formData.value.repoUrl = result.gitRemoteUrl
      console.log('自动填充仓库地址:', result.gitRemoteUrl)
    }
    
    // 保存Git提交记录
    if (result.gitCommits && result.gitCommits.length > 0) {
      formData.value.gitCommits = JSON.stringify(result.gitCommits)
      ElMessage.success(`成功解析项目！找到 ${result.gitCommits.length} 条Git提交记录`)
    } else {
      ElMessage.success('项目解析成功！')
    }
    
  } catch (error) {
    console.error('解析项目失败:', error)
    ElMessage.error('解析项目失败')
  } finally {
    analyzingProject.value = false
  }
}

const openEditDialog = (project: Project) => {
  isEdit.value = true
  dialogTitle.value = '编辑项目'
  formData.value = { ...project }
  dialogVisible.value = true
}

const saveProject = async () => {
  if (!formData.value.name) {
    ElMessage.warning('请输入项目名称')
    return
  }

  try {
    // 准备数据：将数组字段转换为JSON字符串
    const projectData = {
      ...formData.value,
      techStack: JSON.stringify(formData.value.techStack || [])
    }
    
    if (isEdit.value) {
      await axios.put(`/api/projects/${formData.value.id}`, projectData)
      ElMessage.success('项目更新成功')
    } else {
      await axios.post('/api/projects', projectData)
      ElMessage.success('项目创建成功')
    }
    dialogVisible.value = false
    fetchProjects()
  } catch (error) {
    console.error('保存项目失败:', error)
    ElMessage.error('保存项目失败')
  }
}

const deleteProject = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await axios.delete(`/api/projects/${id}`)
    ElMessage.success('项目删除成功')
    fetchProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
      ElMessage.error('删除项目失败')
    }
  }
}

const toggleFavorite = async (project: Project) => {
  try {
    project.isFavorite = !project.isFavorite
    await axios.put(`/api/projects/${project.id}`, project)
    ElMessage.success(project.isFavorite ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    console.error('更新收藏状态失败:', error)
    project.isFavorite = !project.isFavorite
    ElMessage.error('操作失败')
  }
}

// 状态标签样式
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    进行中: 'primary',
    已完成: 'success',
    暂停: 'warning',
    计划中: 'info',
  }
  return typeMap[status] || 'info'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 查看项目详情
const viewProjectDetail = (project: Project) => {
  router.push({
    name: 'ProjectDetail',
    params: { id: project.id }
  })
}

onMounted(() => {
  fetchProjects()
  fetchGitUsers()
})
</script>

<template>
  <div class="project-manager">
    <!-- 统计卡片区 -->
    <div class="statistics-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-total">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.total }}</div>
              <div class="stat-label">项目总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-progress">
            <div class="stat-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.inProgress }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-completed">
            <div class="stat-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-paused">
            <div class="stat-icon">
              <el-icon><Filter /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.paused }}</div>
              <div class="stat-label">已暂停</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目名称、描述、技术栈..."
          :prefix-icon="Search"
          clearable
          style="width: 300px"
        />
        <el-select
          v-model="statusFilter"
          placeholder="选择状态"
          clearable
          style="width: 150px; margin-left: 12px"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <div class="filter-right">
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建项目</el-button>
      </div>
    </div>

    <!-- 项目列表 -->
    <div v-loading="loading" class="projects-grid">
      <div v-if="!loading && filteredProjects.length === 0" class="empty-container">
        <el-empty description="暂无项目" />
      </div>
      
      <div v-for="project in filteredProjects" :key="project.id" class="project-card">
        <div class="card-header">
          <div class="card-title">
            <el-icon
              class="favorite-icon"
              :class="{ 'is-favorite': project.isFavorite }"
              @click="toggleFavorite(project)"
            >
              <StarFilled v-if="project.isFavorite" />
              <Star v-else />
            </el-icon>
            <span class="project-name">{{ project.name }}</span>
          </div>
          <el-tag :type="getStatusType(project.status)" size="small">
            {{ project.status }}
          </el-tag>
        </div>

        <div class="card-body">
          <p class="project-description">{{ project.description || '暂无描述' }}</p>

          <div v-if="project.techStack && project.techStack.length > 0" class="tech-tags">
            <el-tag
              v-for="tech in project.techStack.slice(0, 4)"
              :key="tech"
              size="small"
              effect="plain"
              class="tech-tag"
            >
              {{ tech }}
            </el-tag>
            <el-tag v-if="project.techStack.length > 4" size="small" effect="plain">
              +{{ project.techStack.length - 4 }}
            </el-tag>
          </div>

          <div class="project-meta">
            <span class="meta-item">创建于 {{ formatDate(project.createdAt) }}</span>
            <span class="meta-item">更新于 {{ formatDate(project.updatedAt) }}</span>
          </div>
        </div>

        <div class="card-footer">
          <el-button size="small" :icon="View" @click="viewProjectDetail(project)">查看详情</el-button>
          <el-button size="small" :icon="Edit" @click="openEditDialog(project)">编辑</el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="deleteProject(project.id)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="项目名称" required>
          <el-input v-model="formData.name" placeholder="请输入项目名称" />
        </el-form-item>

        <el-form-item label="项目描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="8"
            :autosize="{ minRows: 8, maxRows: 20 }"
            placeholder="请输入项目描述（支持任意长度）"
          />
        </el-form-item>

        <el-form-item label="项目状态">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="暂停" value="暂停" />
            <el-option label="计划中" value="计划中" />
          </el-select>
        </el-form-item>

        <el-form-item label="项目进度">
          <el-slider v-model="formData.progress" :min="0" :max="100" :step="5" show-input />
        </el-form-item>

        <el-form-item label="技术栈">
          <el-select
            v-model="formData.techStack"
            multiple
            filterable
            allow-create
            placeholder="选择或输入技术栈"
            style="width: 100%"
          >
            <el-option
              v-for="tech in techOptions"
              :key="tech"
              :label="tech"
              :value="tech"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="本地路径">
          <div style="display: flex; gap: 12px;">
            <el-input 
              v-model="formData.localPath" 
              placeholder="点击右侧按钮选择项目文件夹"
              readonly
              style="flex: 1;"
            />
            <el-button 
              :icon="FolderOpened" 
              @click="openFolderSelector"
              :loading="analyzingProject"
            >
              {{ analyzingProject ? '解析中...' : '选择文件夹' }}
            </el-button>
          </div>
          <div v-if="formData.readmeContent" style="margin-top: 8px; font-size: 12px; color: #67c23a;">
            ✓ 已读取README.md ({{ formData.readmeContent.length }} 字符)
          </div>
          <div v-if="formData.gitCommits" style="margin-top: 4px; font-size: 12px; color: #409eff;">
            ✓ 已读取 {{ JSON.parse(formData.gitCommits).length }} 条Git提交记录
          </div>
        </el-form-item>

        <el-form-item label="仓库地址">
          <el-input v-model="formData.repoUrl" placeholder="如：https://github.com/username/repo" />
        </el-form-item>

        <el-form-item label="Git用户">
          <el-select 
            v-model="formData.gitUserId" 
            placeholder="选择Git用户（用于代码提交）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="user in gitUsers"
              :key="user.id"
              :label="`${user.name} (${user.username})`"
              :value="user.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ user.name }}</span>
                <span style="font-size: 12px; color: #999;">{{ user.username }}</span>
                <el-tag v-if="user.isDefault" size="small" type="success">默认</el-tag>
              </div>
            </el-option>
          </el-select>
          <div style="margin-top: 8px; font-size: 12px; color: #909399;">
            💡 提示：选择后在Git提交时将使用此账号
          </div>
        </el-form-item>

        <el-form-item label="收藏项目">
          <el-switch v-model="formData.isFavorite" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProject">保存</el-button>
      </template>
    </el-dialog>

    <!-- 文件夹选择器 -->
    <FolderSelectorDialog 
      v-model="folderSelectorVisible"
      @confirm="handleFolderSelected"
    />
  </div>
</template>

<style scoped>
.project-manager {
  padding: 20px;
  background: #f3f6fa;
  min-height: 100vh;
}

/* 统计卡片 */
.statistics-section {
  margin-bottom: 24px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.2);
  transition: all 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
}

.stat-card.stat-total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.stat-progress {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.stat-completed {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card.stat-paused {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-info {
  color: white;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 筛选栏 */
.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

/* 项目卡片网格 */
.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  min-height: 300px;
}

.empty-container {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.project-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
}

.card-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.favorite-icon {
  font-size: 20px;
  color: #ddd;
  cursor: pointer;
  transition: all 0.3s;
}

.favorite-icon:hover {
  transform: scale(1.2);
}

.favorite-icon.is-favorite {
  color: #f59e0b;
}

.project-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.card-body {
  padding: 20px;
  flex: 1;
}

.project-description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tech-tag {
  border-radius: 6px;
}

.project-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.card-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .projects-grid {
    grid-template-columns: 1fr;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left {
    flex-direction: column;
  }

  .filter-left .el-input,
  .filter-left .el-select {
    width: 100% !important;
    margin-left: 0 !important;
  }
}
</style>
