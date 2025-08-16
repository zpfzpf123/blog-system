<template>
  <el-dialog
    v-model="visible"
    title="AI服务配置"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="ai-config-content">
      <!-- Ollama配置 -->
      <el-form :model="config" label-width="120px" class="ai-config-form">
        <el-form-item label="服务地址">
          <el-input v-model="config.baseUrl" placeholder="http://localhost:11434" />
          <div class="help-text text-muted">本地Ollama服务地址，默认端口11434</div>
        </el-form-item>
        <el-form-item label="模型名称">
          <el-select v-model="config.model" placeholder="请选择模型" style="width: 100%">
            <el-option
              v-for="model in availableModels"
              :key="model.name"
              :label="`${model.name} (${formatModelSize(model.size)})`"
              :value="model.name"
            />
          </el-select>
          <div class="help-text text-muted">
            需要先下载模型：ollama pull deepseek-r1:8b
            <el-button type="text" size="small" @click="refreshModels" :loading="loadingModels">
              刷新模型列表
            </el-button>
          </div>
        </el-form-item>

        <!-- 测试连接 -->
        <el-form-item>
          <el-button type="primary" :loading="testing" @click="testConnection">
            测试连接
          </el-button>
          <div v-if="testResult" class="test-result">
            <el-tag :type="testResult.success ? 'success' : 'danger'" size="small">
              {{ testResult.success ? '连接成功' : '连接失败' }}
            </el-tag>
            <span class="test-message">{{ testResult.message }}</span>
          </div>
        </el-form-item>
      </el-form>

      <!-- 使用说明 -->
      <div class="usage-guide">
        <h4>使用说明</h4>
        <div class="guide-content">
          <div class="guide-item">
            <strong>Ollama本地服务：</strong>
            完全免费，本地运行，需要下载模型文件。推荐使用deepseek-r1:8b模型。
          </div>
          <div class="guide-item">
            <strong>安装步骤：</strong>
            1. 下载安装Ollama 2. 启动服务：ollama serve 3. 下载模型：ollama pull deepseek-r1:8b
          </div>
          <div class="guide-item">
            <strong>模型检测：</strong>
            系统会自动检测本地可用的Ollama模型，您可以直接从下拉列表中选择。
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave">保存配置</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { AIServiceConfig } from '@/utils/aiService'

interface Props {
  modelValue: boolean
  initialConfig?: AIServiceConfig
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', config: AIServiceConfig): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const testing = ref(false)
const testResult = ref<{ success: boolean; message: string } | null>(null)
const loadingModels = ref(false)
const availableModels = ref<Array<{ name: string; size: number }>>([])

// 配置对象
const config = reactive<AIServiceConfig>({
  provider: 'ollama',
  apiKey: '',
  baseUrl: 'http://localhost:11434',
  model: 'deepseek-r1:14b',
})

// 监听visible变化
watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal
  },
)

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
  // 当对话框打开时自动检测模型
  if (newVal) {
    fetchModels()
  }
})

// 监听初始配置
watch(
  () => props.initialConfig,
  (newConfig) => {
    if (newConfig) {
      Object.assign(config, newConfig)
    }
  },
  { immediate: true },
)

// 获取可用模型列表
const fetchModels = async () => {
  loadingModels.value = true
  try {
    const response = await fetch(`${config.baseUrl}/api/tags`)
    if (response.ok) {
      const data = await response.json()
      availableModels.value = data.models || []

      // 如果没有选择模型，默认选择 deepseek-r1:14b
      if (!config.model && availableModels.value.length > 0) {
        const defaultModel = availableModels.value.find((model) =>
          model.name.includes('deepseek-r1:14b'),
        )
        config.model = defaultModel ? defaultModel.name : availableModels.value[0].name
      }
    } else {
      availableModels.value = []
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
    availableModels.value = []
  } finally {
    loadingModels.value = false
  }
}

// 刷新模型列表
const refreshModels = () => {
  fetchModels()
}

// 格式化模型大小
const formatModelSize = (bytes: number): string => {
  if (bytes >= 1024 * 1024 * 1024) {
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)}GB`
  } else if (bytes >= 1024 * 1024) {
    return `${(bytes / (1024 * 1024)).toFixed(1)}MB`
  } else {
    return `${(bytes / 1024).toFixed(1)}KB`
  }
}

// 测试连接
const testConnection = async () => {
  testing.value = true
  testResult.value = null

  try {
    // 测试Ollama连接并获取模型列表
    const response = await fetch(`${config.baseUrl}/api/tags`)
    if (response.ok) {
      const data = await response.json()
      const models = data.models || []

      if (models.length > 0) {
        testResult.value = {
          success: true,
          message: `Ollama服务连接成功，发现 ${models.length} 个模型`,
        }
        // 更新可用模型列表
        availableModels.value = models
      } else {
        testResult.value = {
          success: true,
          message: 'Ollama服务连接成功，但没有找到可用模型',
        }
        availableModels.value = []
      }
    } else {
      throw new Error('服务响应异常')
    }
  } catch (error) {
    testResult.value = {
      success: false,
      message: `连接失败: ${error}`,
    }
    availableModels.value = []
  } finally {
    testing.value = false
  }
}

// 保存配置
const handleSave = () => {
  emit('save', { ...config })
  ElMessage.success('配置保存成功')
  visible.value = false
}

// 取消
const handleCancel = () => {
  visible.value = false
}
</script>

<style scoped>
.ai-config-content {
  max-height: 70vh;
  overflow-y: auto;
}

.ai-config-form {
  margin-bottom: 20px;
}

.help-text {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.4;
}

.text-muted {
  color: #909399;
}

.test-result {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-message {
  font-size: 12px;
  color: #606266;
}

.usage-guide {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.usage-guide h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.guide-content {
  font-size: 12px;
  line-height: 1.6;
  color: #606266;
}

.guide-item {
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.guide-item strong {
  color: #303133;
}

.dialog-footer {
  text-align: right;
}
</style>
