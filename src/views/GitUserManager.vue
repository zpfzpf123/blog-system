<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, InfoFilled } from '@element-plus/icons-vue'
import axios from 'axios'

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
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
}

.subtitle {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: #6b7280;
}

.content-card {
  border-radius: 12px;
  overflow: hidden;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.token-display {
  padding: 4px 8px;
  background: #f3f4f6;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #4b5563;
}

.form-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
}

.form-tip .el-icon {
  color: #3b82f6;
}
</style>
