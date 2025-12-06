<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Delete, Edit, Search, Refresh, Download, Upload,
  Document, Connection, Check, Clock, Warning, Setting,
  CaretRight, DocumentCopy, View
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'

interface ApiItem {
  id?: number
  projectId: number
  name: string
  method: string
  path: string
  description?: string
  requestHeaders?: string
  requestParams?: string
  requestBody?: string
  responseBody?: string
  mockEnabled: boolean
  mockData?: string
  mockDelay: number
  mockStatusCode: number
  status: string
  category?: string
  tags?: string
  sortOrder: number
}

const props = defineProps<{
  projectId: number
}>()

const apis = ref<ApiItem[]>([])
const categories = ref<string[]>([])
const statusCount = ref<Record<string, number>>({})
const loading = ref(false)
const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const showApiDialog = ref(false)
const showTestDialog = ref(false)
const editingApi = ref<ApiItem | null>(null)
const selectedApi = ref<ApiItem | null>(null)
const testResult = ref<any>(null)
const testLoading = ref(false)
const scannedApis = ref<ApiItem[]>([])
const showScanDialog = ref(false)
const scanLoading = ref(false)
const showConfigDialog = ref(false) // 配置对话框
const projectApiBaseUrl = ref('http://localhost:8080') // 项目的API基础URL
const projectAccessToken = ref('') // 项目的Access Token
const configForm = ref({
  apiBaseUrl: 'http://localhost:8080',
  apiAccessToken: ''
})

const apiForm = ref<ApiItem>({
  projectId: props.projectId,
  name: '',
  method: 'GET',
  path: '',
  description: '',
  requestHeaders: '',
  requestParams: '',
  requestBody: '',
  responseBody: '',
  mockEnabled: false,
  mockData: '',
  mockDelay: 0,
  mockStatusCode: 200,
  status: 'pending',
  category: '',
  tags: '',
  sortOrder: 0
})

const testForm = ref({
  baseUrl: 'http://localhost:8080',
  headers: {} as Record<string, string>,
  pathParams: {} as Record<string, string>,
  queryParams: {} as Record<string, string>,
  body: ''
})

const methods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']
const statuses = [
  { value: 'pending', label: '待开发', type: 'info' },
  { value: 'developing', label: '开发中', type: 'warning' },
  { value: 'testing', label: '测试中', type: '' },
  { value: 'completed', label: '已完成', type: 'success' }
]

const filteredApis = computed(() => {
  let result = apis.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(api =>
      api.name.toLowerCase().includes(keyword) ||
      api.path.toLowerCase().includes(keyword)
    )
  }
  if (filterCategory.value) {
    result = result.filter(api => api.category === filterCategory.value)
  }
  if (filterStatus.value) {
    result = result.filter(api => api.status === filterStatus.value)
  }
  return result
})

const groupedApis = computed(() => {
  const groups: Record<string, ApiItem[]> = {}
  filteredApis.value.forEach(api => {
    const category = api.category || '未分类'
    if (!groups[category]) groups[category] = []
    groups[category].push(api)
  })
  return groups
})

// 获取项目信息（包括apiBaseUrl和apiAccessToken）
const fetchProjectInfo = async () => {
  try {
    const response = await axios.get(`/api/projects/${props.projectId}`)
    if (response.data) {
      projectApiBaseUrl.value = response.data.apiBaseUrl || 'http://localhost:8080'
      projectAccessToken.value = response.data.apiAccessToken || ''
      configForm.value = {
        apiBaseUrl: projectApiBaseUrl.value,
        apiAccessToken: projectAccessToken.value
      }
    }
  } catch (error: any) {
    console.error('获取项目信息失败:', error)
  }
}

// 打开配置对话框
const openConfigDialog = () => {
  configForm.value = {
    apiBaseUrl: projectApiBaseUrl.value,
    apiAccessToken: projectAccessToken.value
  }
  showConfigDialog.value = true
}

// 保存API配置
const saveApiConfig = async () => {
  try {
    const response = await axios.patch(`/api/projects/${props.projectId}`, {
      apiBaseUrl: configForm.value.apiBaseUrl,
      apiAccessToken: configForm.value.apiAccessToken
    })
    
    if (response.data) {
      projectApiBaseUrl.value = configForm.value.apiBaseUrl
      projectAccessToken.value = configForm.value.apiAccessToken
      ElMessage.success('API配置保存成功')
      showConfigDialog.value = false
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

// 获取API列表
const fetchApis = async () => {
  try {
    loading.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/apis`)
    if (response.data.success) {
      apis.value = response.data.apis
      categories.value = response.data.categories || []
      statusCount.value = response.data.statusCount || {}
    }
  } catch (error: any) {
    console.error('获取API列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存API
const saveApi = async () => {
  if (!apiForm.value.name || !apiForm.value.path) {
    ElMessage.warning('请填写接口名称和路径')
    return
  }
  try {
    const url = editingApi.value
      ? `/api/projects/${props.projectId}/apis/${editingApi.value.id}`
      : `/api/projects/${props.projectId}/apis`
    const method = editingApi.value ? 'put' : 'post'
    const response = await axios[method](url, apiForm.value)
    if (response.data.success) {
      ElMessage.success(editingApi.value ? 'API更新成功' : 'API添加成功')
      showApiDialog.value = false
      resetForm()
      fetchApis()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 删除API
const deleteApi = async (api: ApiItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除接口 "${api.name}" 吗？`, '确认删除', { type: 'warning' })
    const response = await axios.delete(`/api/projects/${props.projectId}/apis/${api.id}`)
    if (response.data.success) {
      ElMessage.success('API删除成功')
      fetchApis()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 更新状态
const updateStatus = async (api: ApiItem, status: string) => {
  try {
    await axios.patch(`/api/projects/${props.projectId}/apis/${api.id}/status`, { status })
    api.status = status
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    ElMessage.error('状态更新失败')
  }
}

// 切换Mock
const toggleMock = async (api: ApiItem) => {
  try {
    await axios.patch(`/api/projects/${props.projectId}/apis/${api.id}/mock`, {
      enabled: !api.mockEnabled
    })
    api.mockEnabled = !api.mockEnabled
    ElMessage.success(api.mockEnabled ? 'Mock已启用' : 'Mock已禁用')
  } catch (error: any) {
    ElMessage.error('操作失败')
  }
}

// 测试API
const testApi = async () => {
  if (!selectedApi.value) return
  try {
    testLoading.value = true
    testResult.value = null
    const response = await axios.post(
      `/api/projects/${props.projectId}/apis/${selectedApi.value.id}/test`,
      testForm.value
    )
    testResult.value = response.data
  } catch (error: any) {
    testResult.value = { success: false, message: error.message }
  } finally {
    testLoading.value = false
  }
}

// 扫描API
const scanApis = async () => {
  try {
    scanLoading.value = true
    const response = await axios.post(`/api/projects/${props.projectId}/apis/scan`)
    if (response.data.success) {
      scannedApis.value = response.data.scannedApis
      showScanDialog.value = true
      // 显示扫描日志
      const logs = response.data.scanLogs || []
      const logMsg = logs.length > 0 ? '\n' + logs.join('\n') : ''
      ElMessage.success(`扫描完成，发现 ${response.data.count} 个API${logMsg}`)
    } else {
      ElMessage.error(response.data.message || '扫描失败')
    }
  } catch (error: any) {
    const errMsg = error.response?.data?.message || error.message || '扫描失败'
    ElMessage.error(errMsg)
    console.error('扫描API失败:', error.response?.data || error)
  } finally {
    scanLoading.value = false
  }
}

// 导入扫描的API
const importScannedApis = async () => {
  const selected = scannedApis.value.filter((a: any) => a.selected)
  if (selected.length === 0) {
    ElMessage.warning('请选择要导入的API')
    return
  }
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/apis/import`, selected)
    if (response.data.success) {
      ElMessage.success(`成功导入 ${response.data.imported} 个API`)
      showScanDialog.value = false
      fetchApis()
    }
  } catch (error: any) {
    ElMessage.error('导入失败')
  }
}

// 导出文档
const exportDocs = async (format: string) => {
  try {
    const response = await axios.get(`/api/projects/${props.projectId}/apis/export?format=${format}`)
    if (response.data.success) {
      const content = format === 'markdown' ? response.data.content : JSON.stringify(response.data.content, null, 2)
      const blob = new Blob([content], { type: 'text/plain' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `api-docs.${format === 'markdown' ? 'md' : 'json'}`
      a.click()
      URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (error: any) {
    ElMessage.error('导出失败')
  }
}

// 编辑API
const editApi = (api: ApiItem) => {
  editingApi.value = api
  apiForm.value = { ...api }
  showApiDialog.value = true
}

// 打开测试对话框
const openTestDialog = (api: ApiItem) => {
  selectedApi.value = api
  testResult.value = null
  
  // 自动应用项目配置的headers
  const headers: Record<string, string> = {}
  if (projectAccessToken.value) {
    headers['Authorization'] = projectAccessToken.value
  }
  
  testForm.value = {
    baseUrl: projectApiBaseUrl.value, // 使用项目配置的baseUrl
    headers: headers, // 自动添加Access Token
    pathParams: {},
    queryParams: {},
    body: api.requestBody || ''
  }
  showTestDialog.value = true
}

// 重置表单
const resetForm = () => {
  editingApi.value = null
  apiForm.value = {
    projectId: props.projectId,
    name: '',
    method: 'GET',
    path: '',
    description: '',
    requestHeaders: '',
    requestParams: '',
    requestBody: '',
    responseBody: '',
    mockEnabled: false,
    mockData: '',
    mockDelay: 0,
    mockStatusCode: 200,
    status: 'pending',
    category: '',
    tags: '',
    sortOrder: 0
  }
}

// 获取方法颜色
const getMethodColor = (method: string) => {
  const colors: Record<string, string> = {
    GET: '#67c23a',
    POST: '#409eff',
    PUT: '#e6a23c',
    DELETE: '#f56c6c',
    PATCH: '#909399'
  }
  return colors[method] || '#909399'
}

// 复制路径
const copyPath = (path: string) => {
  navigator.clipboard.writeText(path)
  ElMessage.success('路径已复制')
}

// 复制响应结果
const copyResponse = () => {
  if (!testResult.value) return
  const text = JSON.stringify(testResult.value.data || testResult.value.message, null, 2)
  navigator.clipboard.writeText(text)
  ElMessage.success('响应结果已复制到剪贴板')
}

// 格式化JSON（带语法高亮）
const formatJson = (obj: any): string => {
  let json = JSON.stringify(obj, null, 2)
  
  // 语法高亮替换
  json = json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, (match) => {
    let cls = 'json-number'
    if (/^"/.test(match)) {
      if (/:$/.test(match)) {
        cls = 'json-key'
        match = match.slice(0, -1) // 移除末尾的冒号
        return `<span class="${cls}">${match}</span>:`
      } else {
        cls = 'json-string'
      }
    } else if (/true|false/.test(match)) {
      cls = 'json-boolean'
    } else if (/null/.test(match)) {
      cls = 'json-null'
    }
    return `<span class="${cls}">${match}</span>`
  })
  
  return json
}

onMounted(() => {
  fetchProjectInfo() // 获取项目配置的apiBaseUrl
  fetchApis()
})
</script>

<template>
  <div class="api-doc-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <div class="header-left">
        <h3>API 文档管理</h3>
        <div class="status-tags">
          <el-tag size="small">总计: {{ statusCount.total || 0 }}</el-tag>
          <el-tag type="success" size="small">已完成: {{ statusCount.completed || 0 }}</el-tag>
          <el-tag type="warning" size="small">开发中: {{ statusCount.developing || 0 }}</el-tag>
          <el-tag type="info" size="small">待开发: {{ statusCount.pending || 0 }}</el-tag>
        </div>
      </div>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索接口..."
          :prefix-icon="Search"
          clearable
          style="width: 200px"
        />
        <el-select v-model="filterCategory" placeholder="分类" clearable style="width: 120px">
          <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px">
          <el-option v-for="s in statuses" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button :icon="Setting" @click="openConfigDialog">
          API配置
        </el-button>
        <el-button :icon="Refresh" @click="fetchApis" :loading="loading">刷新</el-button>
        <el-button type="primary" :icon="Search" @click="scanApis" :loading="scanLoading">
          扫描代码
        </el-button>
        <el-button type="primary" :icon="Plus" @click="showApiDialog = true; resetForm()">
          添加接口
        </el-button>
        <el-dropdown>
          <el-button :icon="Download">导出</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="exportDocs('markdown')">Markdown</el-dropdown-item>
              <el-dropdown-item @click="exportDocs('json')">OpenAPI JSON</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- API列表 -->
    <div class="api-list">
      <div v-for="(apiList, category) in groupedApis" :key="category" class="api-group">
        <div class="group-header">
          <span class="group-name">{{ category }}</span>
          <span class="group-count">{{ apiList.length }} 个接口</span>
        </div>
        <div class="api-items">
          <div v-for="api in apiList" :key="api.id" class="api-item">
            <div class="api-main">
              <span class="api-method" :style="{ backgroundColor: getMethodColor(api.method) }">
                {{ api.method }}
              </span>
              <span class="api-path" @click="copyPath(api.path)" title="点击复制">
                {{ api.path }}
              </span>
              <span class="api-name">{{ api.name }}</span>
              <el-tag v-if="api.mockEnabled" type="warning" size="small">Mock</el-tag>
              <el-tag :type="statuses.find(s => s.value === api.status)?.type || 'info'" size="small">
                {{ statuses.find(s => s.value === api.status)?.label }}
              </el-tag>
            </div>
            <div class="api-actions">
              <el-button size="small" :icon="CaretRight" @click="openTestDialog(api)">
                测试
              </el-button>
              <el-button size="small" :icon="Setting" @click="toggleMock(api)">
                {{ api.mockEnabled ? '关闭Mock' : '开启Mock' }}
              </el-button>
              <el-dropdown trigger="click">
                <el-button size="small">状态</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="s in statuses"
                      :key="s.value"
                      @click="updateStatus(api, s.value)"
                    >
                      {{ s.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button size="small" :icon="Edit" @click="editApi(api)" />
              <el-button size="small" type="danger" :icon="Delete" @click="deleteApi(api)" />
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="Object.keys(groupedApis).length === 0" description="暂无API接口">
        <el-button type="primary" @click="scanApis">扫描代码自动发现</el-button>
      </el-empty>
    </div>

    <!-- 添加/编辑API对话框 -->
    <el-dialog v-model="showApiDialog" :title="editingApi ? '编辑接口' : '添加接口'" width="700px">
      <el-form :model="apiForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="接口名称" required>
              <el-input v-model="apiForm.name" placeholder="如: 获取用户列表" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-input v-model="apiForm.category" placeholder="如: 用户管理" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="请求方法">
              <el-select v-model="apiForm.method" style="width: 100%">
                <el-option v-for="m in methods" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item label="接口路径" required>
              <el-input v-model="apiForm.path" placeholder="/api/users/{id}" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="接口描述">
          <el-input v-model="apiForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="请求参数">
          <el-input v-model="apiForm.requestParams" type="textarea" :rows="3" placeholder="JSON格式" />
        </el-form-item>
        <el-form-item label="请求体">
          <el-input v-model="apiForm.requestBody" type="textarea" :rows="3" placeholder="JSON格式" />
        </el-form-item>
        <el-form-item label="响应示例">
          <el-input v-model="apiForm.responseBody" type="textarea" :rows="3" placeholder="JSON格式" />
        </el-form-item>
        <el-divider>Mock 配置</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="启用Mock">
              <el-switch v-model="apiForm.mockEnabled" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态码">
              <el-input-number v-model="apiForm.mockStatusCode" :min="100" :max="599" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="延迟(ms)">
              <el-input-number v-model="apiForm.mockDelay" :min="0" :max="10000" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Mock数据">
          <el-input v-model="apiForm.mockData" type="textarea" :rows="4" placeholder="JSON格式的Mock响应数据" />
        </el-form-item>
        <el-form-item label="开发状态">
          <el-radio-group v-model="apiForm.status">
            <el-radio v-for="s in statuses" :key="s.value" :label="s.value">{{ s.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApiDialog = false">取消</el-button>
        <el-button type="primary" @click="saveApi">保存</el-button>
      </template>
    </el-dialog>

    <!-- 测试对话框 -->
    <el-dialog v-model="showTestDialog" title="接口测试" width="800px">
      <div v-if="selectedApi" class="test-dialog">
        <div class="test-info">
          <span class="method" :style="{ backgroundColor: getMethodColor(selectedApi.method) }">
            {{ selectedApi.method }}
          </span>
          <span class="path">{{ selectedApi.path }}</span>
          <el-tag v-if="selectedApi.mockEnabled" type="warning" size="small">Mock模式</el-tag>
        </div>
        <el-form :model="testForm" label-width="100px">
          <el-form-item label="Base URL">
            <el-input v-model="testForm.baseUrl" placeholder="http://localhost:8080">
              <template #append>
                <span style="color: #909399; font-size: 12px;">{{ projectApiBaseUrl ? '✓ 已应用项目配置' : '' }}</span>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="请求头">
            <div style="display: flex; flex-direction: column; gap: 8px;">
              <div v-for="(value, key) in testForm.headers" :key="key" style="display: flex; gap: 8px;">
                <el-input :value="key" placeholder="Header名称" style="width: 200px;" disabled />
                <el-input v-model="testForm.headers[key]" placeholder="Header值" />
                <el-button :icon="Delete" @click="delete testForm.headers[key]" />
              </div>
              <div v-if="projectAccessToken && testForm.headers['Authorization']" style="font-size: 12px; color: #67c23a;">
                ✓ 已自动应用项目配置的 Access Token
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="请求体" v-if="['POST', 'PUT', 'PATCH'].includes(selectedApi.method)">
            <el-input v-model="testForm.body" type="textarea" :rows="4" placeholder="JSON格式" />
          </el-form-item>
        </el-form>
        <div class="test-actions">
          <el-button type="primary" :icon="CaretRight" @click="testApi" :loading="testLoading">
            发送请求
          </el-button>
        </div>
        <div v-if="testResult" class="test-result">
          <div class="result-header">
            <div class="result-status">
              <span :class="['status-code', testResult.success ? 'success' : 'error']">
                <el-icon v-if="testResult.success" style="vertical-align: middle;"><Check /></el-icon>
                <el-icon v-else style="vertical-align: middle;"><Warning /></el-icon>
                {{ testResult.statusCode || 'Error' }}
              </span>
              <span class="response-time">
                <el-icon style="vertical-align: middle; font-size: 14px;"><Clock /></el-icon>
                {{ testResult.responseTime }}ms
              </span>
              <el-tag v-if="testResult.isMock" type="warning" size="small">
                <el-icon style="vertical-align: middle;"><Warning /></el-icon>
                Mock响应
              </el-tag>
            </div>
            <el-button 
              size="small" 
              :icon="DocumentCopy" 
              @click="copyResponse"
              type="primary"
              text
            >
              复制
            </el-button>
          </div>
          <div class="result-body-wrapper">
            <pre class="result-body"><code v-html="formatJson(testResult.data || testResult.message)"></code></pre>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 扫描结果对话框 -->
    <el-dialog v-model="showScanDialog" title="扫描结果" width="800px">
      <div class="scan-result">
        <p>发现 {{ scannedApis.length }} 个API接口，请选择要导入的接口：</p>
        <el-table :data="scannedApis" max-height="400" @selection-change="(val: any[]) => scannedApis.forEach((a: any) => a.selected = val.includes(a))">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="method" label="方法" width="80">
            <template #default="{ row }">
              <span class="method-tag" :style="{ backgroundColor: getMethodColor(row.method) }">
                {{ row.method }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="path" label="路径" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="category" label="分类" width="100" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showScanDialog = false">取消</el-button>
        <el-button type="primary" @click="importScannedApis">导入选中</el-button>
      </template>
    </el-dialog>

    <!-- API配置对话框 -->
    <el-dialog v-model="showConfigDialog" title="API配置" width="600px">
      <el-alert 
        title="配置说明" 
        type="info" 
        :closable="false"
        style="margin-bottom: 20px;"
      >
        配置后的 Base URL 和 Access Token 将自动应用到所有API测试请求中
      </el-alert>
      
      <el-form :model="configForm" label-width="120px">
        <el-form-item label="API Base URL">
          <el-input 
            v-model="configForm.apiBaseUrl" 
            placeholder="如：http://localhost:8080"
          >
            <template #prepend>
              <el-icon><Connection /></el-icon>
            </template>
          </el-input>
          <div style="margin-top: 8px; font-size: 12px; color: #909399;">
            💡 后端服务的基础地址，测试接口时会自动拼接该地址
          </div>
        </el-form-item>

        <el-form-item label="Access Token">
          <el-input 
            v-model="configForm.apiAccessToken" 
            type="textarea"
            :rows="4"
            placeholder="如：Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            show-word-limit
          >
          </el-input>
          <div style="margin-top: 8px; font-size: 12px; color: #909399;">
            🔐 访问令牌，将自动添加到请求的 Authorization 头中
          </div>
        </el-form-item>

        <el-form-item>
          <div style="background: #f5f7fa; padding: 12px; border-radius: 4px; font-size: 13px;">
            <div style="margin-bottom: 8px; color: #606266; font-weight: 500;">✨ 使用示例：</div>
            <div style="color: #909399; line-height: 1.8;">
              • 配置 Base URL 为 <code>http://localhost:8080</code><br>
              • 配置 Token 为 <code>Bearer xxx...</code><br>
              • 测试路径 <code>/api/users</code> 时<br>
              • 实际请求：<code>http://localhost:8080/api/users</code><br>
              • 请求头：<code>Authorization: Bearer xxx...</code>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button type="primary" @click="saveApiConfig">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.api-doc-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.status-tags {
  display: flex;
  gap: 8px;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.api-list {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.api-group {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.group-name {
  font-weight: 600;
  font-size: 14px;
}

.group-count {
  color: #909399;
  font-size: 12px;
}

.api-items {
  padding: 8px 0;
}

.api-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.api-item:last-child {
  border-bottom: none;
}

.api-item:hover {
  background: #f9fafc;
}

.api-main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.api-method {
  padding: 2px 8px;
  border-radius: 4px;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  min-width: 60px;
  text-align: center;
}

.api-path {
  font-family: monospace;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.api-path:hover {
  color: #409eff;
}

.api-name {
  color: #303133;
  font-size: 14px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.api-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 测试对话框 */
.test-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.test-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.test-info .method {
  padding: 4px 12px;
  border-radius: 4px;
  color: #fff;
  font-weight: 600;
}

.test-info .path {
  font-family: monospace;
  font-size: 14px;
}

.test-actions {
  display: flex;
  justify-content: center;
}

.test-result {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-top: 16px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ebf0 100%);
  border-bottom: 2px solid #e4e7ed;
}

.result-status {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-code {
  font-weight: 700;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.8);
}

.status-code.success {
  color: #67c23a;
  border: 2px solid #67c23a;
}

.status-code.error {
  color: #f56c6c;
  border: 2px solid #f56c6c;
}

.response-time {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 6px;
}

.result-body-wrapper {
  background: #282c34;
  position: relative;
}

.result-body {
  margin: 0;
  padding: 20px;
  background: #282c34;
  color: #abb2bf;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  max-height: 400px;
  overflow: auto;
  white-space: pre;
  word-wrap: break-word;
}

.result-body::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.result-body::-webkit-scrollbar-track {
  background: #1e2227;
}

.result-body::-webkit-scrollbar-thumb {
  background: #4b5263;
  border-radius: 4px;
}

.result-body::-webkit-scrollbar-thumb:hover {
  background: #5c6370;
}

.result-body code {
  color: #abb2bf;
  font-family: inherit;
}

/* 扫描结果 */
.scan-result p {
  margin-bottom: 16px;
  color: #606266;
}

.method-tag {
  padding: 2px 6px;
  border-radius: 3px;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
}
</style>
