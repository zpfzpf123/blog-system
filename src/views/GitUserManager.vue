<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, InfoFilled } from '@element-plus/icons-vue'
import axios from '@/utils/axios'

interface GitUser {
  id?: number
  name: string
  username: string
  password: string
  email?: string
  description?: string
  isDefault?: boolean
  createdAt?: string
  updatedAt?: string
}

const gitUsers = ref<GitUser[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建Git用户')
const isEdit = ref(false)

const formData = ref<GitUser>({
  name: '',
  username: '',
  password: '',
  email: '',
  description: '',
  isDefault: false
})

// 获取所有Git用户
const fetchGitUsers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/git-users')
    gitUsers.value = response.data
  } catch (error: any) {
    ElMessage.error('获取Git用户列表失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 打开新建对话框
const openCreateDialog = () => {
  dialogTitle.value = '新建Git用户'
  isEdit.value = false
  formData.value = {
    name: '',
    username: '',
    password: '',
    email: '',
    description: '',
    isDefault: false
  }
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (user: GitUser) => {
  dialogTitle.value = '编辑Git用户'
  isEdit.value = true
  formData.value = { ...user }
  dialogVisible.value = true
}

// 保存Git用户
const saveGitUser = async () => {
  if (!formData.value.name || !formData.value.username || !formData.value.password) {
    ElMessage.warning('请填写名称、用户名和Token')
    return
  }

  loading.value = true
  try {
    if (isEdit.value && formData.value.id) {
      await axios.put(`/api/git-users/${formData.value.id}`, formData.value)
      ElMessage.success('更新成功')
    } else {
      await axios.post('/api/git-users', formData.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchGitUsers()
  } catch (error: any) {
    ElMessage.error('保存失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 删除Git用户
const deleteGitUser = async (user: GitUser) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除Git用户"${user.name}"吗？`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
    
    loading.value = true
    await axios.delete(`/api/git-users/${user.id}`)
    ElMessage.success('删除成功')
    await fetchGitUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.response?.data?.message || error.message))
    }
  } finally {
    loading.value = false
  }
}

// 设置为默认
const setAsDefault = async (user: GitUser) => {
  loading.value = true
  try {
    await axios.post(`/api/git-users/${user.id}/set-default`)
    ElMessage.success('已设置为默认账号')
    await fetchGitUsers()
  } catch (error: any) {
    ElMessage.error('设置失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchGitUsers()
})
</script>

<template>
  <div class="git-user-manager">
    <div class="page-header">
      <div class="header-left">
        <h1>Git用户管理</h1>
        <p class="subtitle">管理Git账号的Personal Access Token</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建Git用户
      </el-button>
    </div>

    <el-card class="content-card" shadow="never">
      <el-table :data="gitUsers" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" width="180">
          <template #default="{ row }">
            <div class="user-name">
              {{ row.name }}
              <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="password" label="Token" min-width="200">
          <template #default="{ row }">
            <code class="token-display">{{ row.password.substring(0, 20) }}...</code>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="!row.isDefault" link type="success" @click="setAsDefault(row)">
              设为默认
            </el-button>
            <el-button link type="danger" @click="deleteGitUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && gitUsers.length === 0" description="还没有Git用户，点击右上角创建" />
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="名称" required>
          <el-input v-model="formData.name" placeholder="例如：我的GitHub账号" />
        </el-form-item>
        
        <el-form-item label="Git用户名" required>
          <el-input v-model="formData.username" placeholder="GitHub/GitLab用户名" />
        </el-form-item>
        
        <el-form-item label="Personal Access Token" required>
          <el-input 
            v-model="formData.password" 
            type="password" 
            show-password
            placeholder="ghp_xxxxxxxxxxxx"
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>在GitHub Settings → Developer settings → Personal access tokens创建</span>
          </div>
        </el-form-item>
        
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="可选" />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            :rows="2"
            placeholder="可选"
          />
        </el-form-item>
        
        <el-form-item label="设为默认账号">
          <el-switch v-model="formData.isDefault" />
          <div class="form-tip">
            <span>默认账号将在项目未指定Git用户时使用</span>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="saveGitUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.git-user-manager {
  padding: var(--spacing-6);
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 100px);
  animation: pageEnter 0.5s var(--ease-out);
}

@keyframes pageEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-8);
  padding: var(--spacing-6);
  background: var(--bg-glass);
  backdrop-filter: blur(24px) saturate(180%);
  border-radius: var(--radius-2xl);
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  box-shadow: var(--shadow-glass);
}

.header-left h1 {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: var(--font-extrabold);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.subtitle {
  margin: var(--spacing-2) 0 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

.page-header .el-button {
  border-radius: var(--radius-lg);
  font-weight: var(--font-semibold);
  padding: var(--spacing-3) var(--spacing-6);
  background: var(--gradient-primary);
  border: none;
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-normal);
}

.page-header .el-button:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-primary-lg);
}

.content-card {
  border-radius: var(--radius-2xl);
  overflow: hidden;
  background: var(--bg-glass);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  box-shadow: var(--shadow-glass);
  transition: all var(--transition-normal);
}

.content-card:hover {
  box-shadow: var(--shadow-card-hover);
}

.user-name {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.token-display {
  padding: var(--spacing-1) var(--spacing-3);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.08) 100%);
  border-radius: var(--radius-md);
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--primary-color);
  border: 1px solid rgba(99, 102, 241, 0.15);
}

.form-tip {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  margin-top: var(--spacing-2);
  font-size: var(--text-xs);
  color: var(--text-secondary);
  padding: var(--spacing-2) var(--spacing-3);
  background: var(--info-bg);
  border-radius: var(--radius-md);
  border: 1px solid rgba(14, 165, 233, 0.15);
}

.form-tip .el-icon {
  color: var(--info-color);
}

/* 表格样式优化 */
:deep(.el-table) {
  --el-table-border-color: var(--border-light);
  --el-table-header-bg-color: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(139, 92, 246, 0.03) 100%);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

:deep(.el-table th) {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

:deep(.el-table tr) {
  transition: all var(--transition-fast);
}

:deep(.el-table tr:hover) {
  background: rgba(99, 102, 241, 0.04);
}

/* 对话框样式优化 */
:deep(.el-dialog) {
  border-radius: var(--radius-2xl);
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(139, 92, 246, 0.05) 100%);
  padding: var(--spacing-5) var(--spacing-6);
}

:deep(.el-dialog__title) {
  font-weight: var(--font-bold);
  color: var(--text-primary);
}

:deep(.el-dialog__body) {
  padding: var(--spacing-6);
}

:deep(.el-dialog__footer) {
  padding: var(--spacing-4) var(--spacing-6);
  border-top: 1px solid var(--border-light);
}

/* 表格操作按钮样式 */
:deep(.el-table .el-button--link) {
  padding: var(--spacing-1) var(--spacing-2);
  font-weight: var(--font-medium);
  transition: all var(--transition-fast);
}

:deep(.el-table .el-button--link:hover) {
  transform: translateY(-1px);
}

:deep(.el-table .el-button--link.el-button--primary) {
  color: var(--primary-color);
}

:deep(.el-table .el-button--link.el-button--primary:hover) {
  color: var(--primary-hover);
  background: rgba(99, 102, 241, 0.08);
  border-radius: var(--radius-md);
}

:deep(.el-table .el-button--link.el-button--success) {
  color: var(--success-color);
}

:deep(.el-table .el-button--link.el-button--success:hover) {
  color: var(--success-hover);
  background: rgba(16, 185, 129, 0.08);
  border-radius: var(--radius-md);
}

:deep(.el-table .el-button--link.el-button--danger) {
  color: var(--danger-color);
}

:deep(.el-table .el-button--link.el-button--danger:hover) {
  color: var(--danger-hover);
  background: rgba(239, 68, 68, 0.08);
  border-radius: var(--radius-md);
}

/* 对话框底部按钮样式 */
:deep(.el-dialog__footer .el-button) {
  border-radius: var(--radius-lg);
  padding: var(--spacing-2) var(--spacing-5);
  font-weight: var(--font-medium);
  transition: all var(--transition-normal);
}

:deep(.el-dialog__footer .el-button--default) {
  background: var(--bg-muted);
  border: 1px solid var(--border-default);
  color: var(--text-secondary);
}

:deep(.el-dialog__footer .el-button--default:hover) {
  background: var(--gray-200);
  border-color: var(--border-strong);
  color: var(--text-primary);
}

:deep(.el-dialog__footer .el-button--primary) {
  background: var(--gradient-primary);
  border: none;
  box-shadow: var(--shadow-primary);
}

:deep(.el-dialog__footer .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-primary-lg);
}
</style>
