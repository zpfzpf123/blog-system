<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  VideoPlay,
  VideoPause,
  Refresh,
  Plus,
  Delete,
  Edit,
  Monitor,
  Connection,
  Document,
  Setting,
  CircleCheck,
  CircleClose,
  Loading,
  Warning,
  Search
} from '@element-plus/icons-vue'
import axios from '@/utils/axios'

interface ServiceConfig {
  id?: number
  projectId: number
  name: string
  type: string
  startCommand: string
  stopCommand?: string
  workingDirectory?: string
  port?: number
  healthCheckUrl?: string
  envVariables?: string
  autoRestart: boolean
  startOrder: number
  enabled: boolean
  description?: string
  status?: string
  isRunning?: boolean
}

const props = defineProps<{
  projectId: number
  projectPath?: string
}>()

const emit = defineEmits(['close'])

const services = ref<ServiceConfig[]>([])
const loading = ref(false)
const showAddDialog = ref(false)
const editingService = ref<ServiceConfig | null>(null)
const selectedServiceId = ref<number | null>(null)
const serviceLogs = ref<string[]>([])
const logsLoading = ref(false)
const logsAutoScroll = ref(true)
const logsContainerRef = ref<HTMLElement | null>(null)

let refreshTimer: NodeJS.Timeout | null = null
let logsTimer: NodeJS.Timeout | null = null

// 扫描相关
const showScanDialog = ref(false)
const scanLoading = ref(false)
const scannedServices = ref<ServiceConfig[]>([])
const selectedScannedServices = ref<ServiceConfig[]>([])

const serviceForm = ref<ServiceConfig>({
  projectId: props.projectId,
  name: '',
  type: 'frontend',
  startCommand: '',
  stopCommand: '',
  workingDirectory: '',
  port: undefined,
  healthCheckUrl: '',
  envVariables: '',
  autoRestart: false,
  startOrder: 0,
  enabled: true,
  description: ''
})

const serviceTypes = [
  { value: 'frontend', label: '前端服务', icon: Monitor },
  { value: 'backend', label: '后端服务', icon: Connection },
  { value: 'database', label: '数据库', icon: Document },
  { value: 'other', label: '其他', icon: Setting }
]

const runningCount = computed(() => services.value.filter(s => s.isRunning).count)
const stoppedCount = computed(() => services.value.filter(s => !s.isRunning).count)

// 获取服务列表
const fetchServices = async () => {
  try {
    loading.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/dev/services`)
    if (response.data.success) {
      services.value = response.data.services
    }
  } catch (error: any) {
    console.error('获取服务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加/编辑服务
const saveService = async () => {
  if (!serviceForm.value.name || !serviceForm.value.startCommand) {
    ElMessage.warning('请填写服务名称和启动命令')
    return
  }

  try {
    const url = editingService.value
      ? `/api/projects/${props.projectId}/dev/services/${editingService.value.id}`
      : `/api/projects/${props.projectId}/dev/services`
    
    const method = editingService.value ? 'put' : 'post'
    const response = await axios[method](url, serviceForm.value)
    
    if (response.data.success) {
      ElMessage.success(editingService.value ? '服务配置更新成功' : '服务配置添加成功')
      showAddDialog.value = false
      resetForm()
      fetchServices()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 删除服务
const deleteService = async (service: ServiceConfig) => {
  try {
    await ElMessageBox.confirm(`确定要删除服务 "${service.name}" 吗？`, '确认删除', {
      type: 'warning'
    })
    
    const response = await axios.delete(`/api/projects/${props.projectId}/dev/services/${service.id}`)
    if (response.data.success) {
      ElMessage.success('服务删除成功')
      fetchServices()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 启动服务
const startService = async (service: ServiceConfig) => {
  try {
    service.status = 'starting'
    const response = await axios.post(`/api/projects/${props.projectId}/dev/services/${service.id}/start`)
    
    if (response.data.success) {
      // 显示启动成功消息
      const msg = response.data.message || `服务 ${service.name} 启动成功`
      if (response.data.killedProcess) {
        ElMessage.success({
          message: msg,
          duration: 4000
        })
      } else {
        ElMessage.success(msg)
      }
      fetchServices()
    } else {
      ElMessage.error(response.data.message)
      if (response.data.portConflict) {
        ElMessageBox.alert(
          response.data.message + '\n\n' + (response.data.processInfo || ''),
          '端口冲突',
          { type: 'error' }
        )
      }
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '启动失败')
  } finally {
    fetchServices()
  }
}

// 停止服务
const stopService = async (service: ServiceConfig) => {
  try {
    service.status = 'stopping'
    const response = await axios.post(`/api/projects/${props.projectId}/dev/services/${service.id}/stop`)
    
    if (response.data.success) {
      ElMessage.success(`服务 ${service.name} 已停止`)
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '停止失败')
  } finally {
    fetchServices()
  }
}

// 重启服务
const restartService = async (service: ServiceConfig) => {
  try {
    service.status = 'restarting'
    const response = await axios.post(`/api/projects/${props.projectId}/dev/services/${service.id}/restart`)
    
    if (response.data.success) {
      ElMessage.success(`服务 ${service.name} 重启成功`)
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '重启失败')
  } finally {
    fetchServices()
  }
}

// 启动所有服务
const startAllServices = async () => {
  try {
    loading.value = true
    const response = await axios.post(`/api/projects/${props.projectId}/dev/start-all`)
    
    if (response.data.success) {
      const results = response.data.results
      const successCount = results.filter((r: any) => r.success).length
      ElMessage.success(`成功启动 ${successCount}/${results.length} 个服务`)
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '批量启动失败')
  } finally {
    loading.value = false
    fetchServices()
  }
}

// 停止所有服务
const stopAllServices = async () => {
  try {
    await ElMessageBox.confirm('确定要停止所有服务吗？', '确认停止', { type: 'warning' })
    
    loading.value = true
    const response = await axios.post(`/api/projects/${props.projectId}/dev/stop-all`)
    
    if (response.data.success) {
      ElMessage.success('所有服务已停止')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '批量停止失败')
    }
  } finally {
    loading.value = false
    fetchServices()
  }
}

// 检查端口
const checkPort = async (port: number) => {
  try {
    const response = await axios.get(`/api/projects/${props.projectId}/dev/check-port/${port}`)
    
    if (response.data.inUse) {
      ElMessageBox.confirm(
        `端口 ${port} 被以下进程占用:\n\n${response.data.processInfo || '无法获取进程信息'}\n\n是否杀死占用进程？`,
        '端口占用信息',
        { 
          type: 'warning',
          confirmButtonText: '杀死进程',
          cancelButtonText: '取消'
        }
      ).then(async () => {
        await killPort(port)
      }).catch(() => {
        // 用户取消
      })
    } else {
      ElMessage.success(`端口 ${port} 可用`)
    }
  } catch (error: any) {
    ElMessage.error('检查端口失败')
  }
}

// 杀死占用端口的进程
const killPort = async (port: number) => {
  try {
    const response = await axios.post(`/api/projects/${props.projectId}/dev/kill-port/${port}`)
    
    if (response.data.success) {
      ElMessage.success(response.data.message)
      fetchServices()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '杀死进程失败')
  }
}

// 获取服务日志
const fetchServiceLogs = async (serviceId: number) => {
  try {
    logsLoading.value = true
    const response = await axios.get(`/api/projects/${props.projectId}/dev/services/${serviceId}/logs`)
    
    if (response.data.success) {
      serviceLogs.value = response.data.logs
      
      // 自动滚动到底部
      if (logsAutoScroll.value && logsContainerRef.value) {
        setTimeout(() => {
          logsContainerRef.value!.scrollTop = logsContainerRef.value!.scrollHeight
        }, 100)
      }
    }
  } catch (error: any) {
    console.error('获取日志失败:', error)
  } finally {
    logsLoading.value = false
  }
}

// 清除日志
const clearLogs = async () => {
  if (!selectedServiceId.value) return
  
  try {
    await axios.delete(`/api/projects/${props.projectId}/dev/services/${selectedServiceId.value}/logs`)
    serviceLogs.value = []
    ElMessage.success('日志已清除')
  } catch (error: any) {
    ElMessage.error('清除日志失败')
  }
}

// 扫描项目自动识别服务
const scanServices = async () => {
  try {
    scanLoading.value = true
    const response = await axios.post(`/api/projects/${props.projectId}/dev/scan`)
    
    if (response.data.success) {
      scannedServices.value = response.data.services || []
      selectedScannedServices.value = [...scannedServices.value] // 默认全选
      
      if (scannedServices.value.length > 0) {
        showScanDialog.value = true
        ElMessage.success(`扫描完成，发现 ${scannedServices.value.length} 个服务`)
      } else {
        ElMessage.warning('未发现可识别的服务配置')
      }
    } else {
      ElMessage.error(response.data.message || '扫描失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '扫描失败')
  } finally {
    scanLoading.value = false
  }
}

// 导入扫描到的服务
const importScannedServices = async () => {
  if (selectedScannedServices.value.length === 0) {
    ElMessage.warning('请选择要导入的服务')
    return
  }
  
  try {
    const response = await axios.post(
      `/api/projects/${props.projectId}/dev/import`,
      selectedScannedServices.value
    )
    
    if (response.data.success) {
      ElMessage.success(`成功导入 ${response.data.imported} 个服务配置`)
      showScanDialog.value = false
      fetchServices()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '导入失败')
  }
}

// 处理扫描服务选择变化
const handleScanSelectionChange = (selection: ServiceConfig[]) => {
  selectedScannedServices.value = selection
}

// 选择服务查看日志
const selectService = (service: ServiceConfig) => {
  selectedServiceId.value = service.id!
  fetchServiceLogs(service.id!)
  
  // 启动日志轮询
  if (logsTimer) clearInterval(logsTimer)
  logsTimer = setInterval(() => {
    if (selectedServiceId.value) {
      fetchServiceLogs(selectedServiceId.value)
    }
  }, 2000)
}

// 编辑服务
const editService = (service: ServiceConfig) => {
  editingService.value = service
  serviceForm.value = { ...service }
  showAddDialog.value = true
}

// 重置表单
const resetForm = () => {
  editingService.value = null
  serviceForm.value = {
    projectId: props.projectId,
    name: '',
    type: 'frontend',
    startCommand: '',
    stopCommand: '',
    workingDirectory: '',
    port: undefined,
    healthCheckUrl: '',
    envVariables: '',
    autoRestart: false,
    startOrder: 0,
    enabled: true,
    description: ''
  }
}

// 获取状态图标
const getStatusIcon = (service: ServiceConfig) => {
  if (service.status === 'starting' || service.status === 'stopping' || service.status === 'restarting') {
    return Loading
  }
  return service.isRunning ? CircleCheck : CircleClose
}

// 获取状态颜色
const getStatusColor = (service: ServiceConfig) => {
  if (service.status === 'starting' || service.status === 'stopping' || service.status === 'restarting') {
    return '#e6a23c'
  }
  return service.isRunning ? '#67c23a' : '#909399'
}

// 获取类型图标
const getTypeIcon = (type: string) => {
  const found = serviceTypes.find(t => t.value === type)
  return found ? found.icon : Setting
}

onMounted(() => {
  fetchServices()
  
  // 定时刷新状态
  refreshTimer = setInterval(fetchServices, 50000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (logsTimer) clearInterval(logsTimer)
})
</script>

<template>
  <div class="dev-environment-panel">
    <!-- 头部操作栏 -->
    <div class="panel-header">
      <div class="header-left">
        <h3>开发环境管理</h3>
        <div class="status-summary">
          <el-tag type="success" size="small">运行中: {{ services.filter(s => s.isRunning).length }}</el-tag>
          <el-tag type="info" size="small">已停止: {{ services.filter(s => !s.isRunning).length }}</el-tag>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="VideoPlay" @click="startAllServices" :loading="loading">
          全部启动
        </el-button>
        <el-button type="danger" :icon="VideoPause" @click="stopAllServices" :loading="loading">
          全部停止
        </el-button>
        <el-button type="success" :icon="Search" @click="scanServices" :loading="scanLoading">
          自动扫描
        </el-button>
        <el-button :icon="Plus" @click="showAddDialog = true; resetForm()">
          添加服务
        </el-button>
        <el-button :icon="Refresh" @click="fetchServices" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>

    <div class="panel-content">
      <!-- 服务列表 -->
      <div class="services-section">
        <div class="services-grid">
          <div
            v-for="service in services"
            :key="service.id"
            class="service-card"
            :class="{ 'is-running': service.isRunning, 'is-selected': selectedServiceId === service.id }"
            @click="selectService(service)"
          >
            <div class="service-header">
              <div class="service-info">
                <el-icon :size="20" class="type-icon">
                  <component :is="getTypeIcon(service.type)" />
                </el-icon>
                <span class="service-name">{{ service.name }}</span>
              </div>
              <el-icon :size="16" :color="getStatusColor(service)" class="status-icon">
                <component :is="getStatusIcon(service)" />
              </el-icon>
            </div>
            
            <div class="service-details">
              <div v-if="service.port" class="detail-item">
                <span class="label">端口:</span>
                <span class="value">{{ service.port }}</span>
              </div>
              <div class="detail-item">
                <span class="label">命令:</span>
                <span class="value command">{{ service.startCommand }}</span>
              </div>
            </div>

            <div class="service-actions">
              <el-button
                v-if="!service.isRunning"
                type="primary"
                size="small"
                :icon="VideoPlay"
                @click.stop="startService(service)"
              >
                启动
              </el-button>
              <el-button
                v-else
                type="danger"
                size="small"
                :icon="VideoPause"
                @click.stop="stopService(service)"
              >
                停止
              </el-button>
              <el-button
                size="small"
                :icon="Refresh"
                @click.stop="restartService(service)"
                :disabled="!service.isRunning"
              >
                重启
              </el-button>
              <el-dropdown trigger="click" @click.stop>
                <el-button size="small" :icon="Setting" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="editService(service)">
                      <el-icon><Edit /></el-icon> 编辑配置
                    </el-dropdown-item>
                    <el-dropdown-item v-if="service.port" @click="checkPort(service.port)">
                      <el-icon><Connection /></el-icon> 检查端口
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="deleteService(service)">
                      <el-icon color="#f56c6c"><Delete /></el-icon>
                      <span style="color: #f56c6c">删除服务</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="services.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无服务配置">
              <el-button type="primary" @click="showAddDialog = true">添加服务</el-button>
            </el-empty>
          </div>
        </div>
      </div>

      <!-- 日志面板 -->
      <div v-if="selectedServiceId" class="logs-section">
        <div class="logs-header">
          <span class="logs-title">
            服务日志 - {{ services.find(s => s.id === selectedServiceId)?.name }}
          </span>
          <div class="logs-actions">
            <el-checkbox v-model="logsAutoScroll" label="自动滚动" size="small" />
            <el-button size="small" @click="clearLogs">清除日志</el-button>
            <el-button size="small" @click="selectedServiceId = null">关闭</el-button>
          </div>
        </div>
        <div ref="logsContainerRef" class="logs-container">
          <div v-if="serviceLogs.length === 0" class="logs-empty">
            暂无日志输出
          </div>
          <div v-else class="logs-content">
            <div v-for="(log, index) in serviceLogs" :key="index" class="log-line">
              {{ log }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑服务对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingService ? '编辑服务配置' : '添加服务配置'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="serviceForm" label-width="100px">
        <el-form-item label="服务名称" required>
          <el-input v-model="serviceForm.name" placeholder="如: 前端开发服务器" />
        </el-form-item>
        
        <el-form-item label="服务类型">
          <el-select v-model="serviceForm.type" style="width: 100%">
            <el-option
              v-for="type in serviceTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="启动命令" required>
          <el-input
            v-model="serviceForm.startCommand"
            placeholder="如: npm run dev"
            type="textarea"
            :rows="2"
          />
        </el-form-item>
        
        <el-form-item label="工作目录">
          <el-input
            v-model="serviceForm.workingDirectory"
            placeholder="留空则使用项目根目录，可填相对路径如 frontend"
          />
        </el-form-item>
        
        <el-form-item label="端口号">
          <el-input-number v-model="serviceForm.port" :min="1" :max="65535" />
        </el-form-item>
        
        <el-form-item label="健康检查URL">
          <el-input
            v-model="serviceForm.healthCheckUrl"
            placeholder="如: http://localhost:3000/health"
          />
        </el-form-item>
        
        <el-form-item label="环境变量">
          <el-input
            v-model="serviceForm.envVariables"
            type="textarea"
            :rows="3"
            placeholder='JSON格式，如: {"NODE_ENV": "development"}'
          />
        </el-form-item>
        
        <el-form-item label="启动顺序">
          <el-input-number v-model="serviceForm.startOrder" :min="0" />
          <span class="form-tip">数字越小越先启动</span>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input v-model="serviceForm.description" type="textarea" :rows="2" />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="serviceForm.enabled">启用服务</el-checkbox>
          <el-checkbox v-model="serviceForm.autoRestart">自动重启</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveService">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 扫描结果对话框 -->
    <el-dialog
      v-model="showScanDialog"
      title="扫描结果 - 自动识别的服务"
      width="800px"
    >
      <div class="scan-result">
        <p class="scan-tip">
          <el-icon><Warning /></el-icon>
          已自动识别以下服务配置，请选择要导入的服务：
        </p>
        <el-table
          :data="scannedServices"
          max-height="400"
          @selection-change="handleScanSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="name" label="服务名称" width="150" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.type === 'frontend' ? 'success' : row.type === 'backend' ? 'primary' : 'info'" size="small">
                {{ row.type === 'frontend' ? '前端' : row.type === 'backend' ? '后端' : row.type === 'database' ? '数据库' : '其他' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="port" label="端口" width="80" />
          <el-table-column prop="startCommand" label="启动命令" show-overflow-tooltip />
          <el-table-column prop="description" label="说明" width="180" show-overflow-tooltip />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showScanDialog = false">取消</el-button>
        <el-button type="primary" @click="importScannedServices" :disabled="selectedScannedServices.length === 0">
          导入选中 ({{ selectedScannedServices.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dev-environment-panel {
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

.status-summary {
  display: flex;
  gap: 8px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.panel-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px;
  gap: 16px;
}

.services-section {
  flex: 1;
  overflow: auto;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.service-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s;
}

.service-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.service-card.is-running {
  border-color: #67c23a;
}

.service-card.is-selected {
  border-color: #409eff;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.service-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-icon {
  color: #409eff;
}

.service-name {
  font-weight: 600;
  font-size: 15px;
}

.service-details {
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  font-size: 13px;
  margin-bottom: 4px;
}

.detail-item .label {
  color: #909399;
  width: 50px;
}

.detail-item .value {
  color: #606266;
  flex: 1;
}

.detail-item .value.command {
  font-family: monospace;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.service-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.logs-section {
  height: 300px;
  background: #1e1e1e;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #333;
}

.logs-title {
  color: #fff;
  font-weight: 500;
}

.logs-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logs-actions :deep(.el-checkbox__label) {
  color: #aaa;
}

.logs-container {
  flex: 1;
  overflow: auto;
  padding: 12px 16px;
}

.logs-empty {
  color: #666;
  text-align: center;
  padding: 40px;
}

.logs-content {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.6;
}

.log-line {
  color: #d4d4d4;
  white-space: pre-wrap;
  word-break: break-all;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

/* 扫描结果样式 */
.scan-result {
  padding: 0;
}

.scan-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fdf6ec;
  border-radius: 6px;
  margin-bottom: 16px;
  color: #e6a23c;
  font-size: 14px;
}

.scan-tip .el-icon {
  font-size: 18px;
}
</style>
