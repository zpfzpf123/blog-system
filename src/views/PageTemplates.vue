<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, CopyDocument, Edit, Delete, Refresh, Monitor, FullScreen } from '@element-plus/icons-vue'
import pageTemplateService, { type PageTemplate, TEMPLATE_CATEGORIES, TECH_STACKS } from '@/services/pageTemplateService'

const router = useRouter()

const templates = ref<PageTemplate[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref('')
const dialogVisible = ref(false)
const isEditing = ref(false)
const selectedTechStack = ref('')





const formData = ref<PageTemplate>({
  name: '',
  description: '',
  category: 'login',
  techStack: 'Vue3+ElementPlus',
  htmlCode: '',
  cssCode: '',
  jsCode: ''
})

// 完整Vue代码
const fullVueCode = ref('')

// 生成完整Vue代码
const generateFullCode = (template: PageTemplate) => {
  const scriptOpen = '<' + 'script setup>'
  const scriptClose = '</' + 'script>'
  const styleOpen = '<' + 'style scoped>'
  const styleClose = '</' + 'style>'
  
  return `<template>
${template.htmlCode || '  <!-- 模板内容 -->'}
</template>

${scriptOpen}
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  remember: false,
  agree: false
})

const loading = ref(false)
const activeTab = ref('login')

const handleSubmit = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('操作成功！')
  }, 1500)
}

${template.jsCode || '// 自定义逻辑'}
${scriptClose}

${styleOpen}
${template.cssCode || '/* 样式代码 */'}
${styleClose}`
}

// 解析完整Vue代码
const parseFullCode = (code: string) => {
  const templateMatch = code.match(/<template>([\s\S]*?)<\/template>/)
  const scriptMatch = code.match(/<script[^>]*>([\s\S]*?)<\/script>/)
  const styleMatch = code.match(/<style[^>]*>([\s\S]*?)<\/style>/)
  
  let htmlCode = templateMatch ? templateMatch[1].trim() : ''
  let jsCode = ''
  let cssCode = styleMatch ? styleMatch[1].trim() : ''
  
  if (scriptMatch) {
    const scriptContent = scriptMatch[1]
    // 提取自定义逻辑（handleSubmit 之后的内容）
    const customMatch = scriptContent.match(/handleSubmit[\s\S]*?\}\s*\n([\s\S]*)$/)
    if (customMatch) {
      jsCode = customMatch[1].trim()
    }
  }
  
  return { htmlCode, jsCode, cssCode }
}

const filteredTemplates = computed(() => {
  let result = templates.value
  if (selectedCategory.value) {
    result = result.filter(t => t.category === selectedCategory.value)
  }
  if (selectedTechStack.value) {
    result = result.filter(t => t.techStack === selectedTechStack.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(t => 
      t.name.toLowerCase().includes(kw) || 
      t.description?.toLowerCase().includes(kw)
    )
  }
  return result
})

const getTechStackLabel = (value: string) => {
  return TECH_STACKS.find(t => t.value === value)?.label || value
}

const getTechStackIcon = (value: string) => {
  return TECH_STACKS.find(t => t.value === value)?.icon || '📦'
}

const getCategoryLabel = (value: string) => {
  return TEMPLATE_CATEGORIES.find(c => c.value === value)?.label || value
}

const getCategoryIcon = (value: string) => {
  return TEMPLATE_CATEGORIES.find(c => c.value === value)?.icon || '📦'
}

const getCategoryColor = (value: string) => {
  const colors: Record<string, string> = {
    login: '#667eea',
    form: '#409eff',
    table: '#67c23a',
    card: '#e6a23c',
    modal: '#f56c6c',
    layout: '#909399',
    dashboard: '#9c27b0',
    other: '#606266'
  }
  return colors[value] || '#409eff'
}

const loadTemplates = async () => {
  loading.value = true
  try {
    templates.value = await pageTemplateService.getAllTemplates()
  } catch (e) {
    ElMessage.error('加载模板失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEditing.value = false
  formData.value = { name: '', description: '', category: 'login', techStack: 'Vue3+ElementPlus', htmlCode: '', cssCode: '', jsCode: '' }
  fullVueCode.value = generateFullCode(formData.value)
  dialogVisible.value = true
}

const openEditDialog = (template: PageTemplate) => {
  isEditing.value = true
  formData.value = { ...template }
  fullVueCode.value = generateFullCode(template)
  dialogVisible.value = true
}

const saveTemplate = async () => {
  if (!formData.value.name) {
    ElMessage.warning('请填写模板名称')
    return
  }
  // 从完整代码解析出 html/css/js
  const parsed = parseFullCode(fullVueCode.value)
  formData.value.htmlCode = parsed.htmlCode
  formData.value.cssCode = parsed.cssCode
  formData.value.jsCode = parsed.jsCode
  
  if (!formData.value.htmlCode) {
    ElMessage.warning('请填写模板代码')
    return
  }
  try {
    if (isEditing.value && formData.value.id) {
      await pageTemplateService.updateTemplate(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await pageTemplateService.createTemplate(formData.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTemplates()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const deleteTemplate = async (template: PageTemplate) => {
  try {
    await ElMessageBox.confirm(`确定删除模板 "${template.name}" 吗？`, '确认删除', { type: 'warning' })
    await pageTemplateService.deleteTemplate(template.id!)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// 生成预览HTML
const generatePreviewHtml = (htmlCode: string, cssCode: string, jsCode: string) => {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="https://unpkg.com/element-plus/dist/index.css">
  <script src="https://unpkg.com/vue@3/dist/vue.global.js"><\/script>
  <script src="https://unpkg.com/element-plus"><\/script>
  <script src="https://unpkg.com/@element-plus/icons-vue"><\/script>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    html, body, #app { width: 100%; height: 100%; }
    ${cssCode || ''}
  </style>
</head>
<body>
  <div id="app">${htmlCode}</div>
  <script>
    const { createApp, ref, reactive } = Vue
    const app = createApp({
      setup() {
        const form = reactive({ username: '', password: '', email: '', phone: '', remember: false, agree: false })
        const loading = ref(false)
        const activeTab = ref('login')
        const previewVisible = ref(false)
        const openPreview = (i) => { previewVisible.value = true }
        const handleSubmit = () => {
          loading.value = true
          setTimeout(() => { loading.value = false; ElementPlus.ElMessage.success('操作成功！') }, 1500)
        }
        ${jsCode || ''}
        return { 
          form, loading, handleSubmit, activeTab, previewVisible, openPreview,
          tableData: typeof tableData !== 'undefined' ? tableData : ref([]),
          treeData: typeof treeData !== 'undefined' ? treeData : ref([]),
          selectedRows: typeof selectedRows !== 'undefined' ? selectedRows : ref([]),
          handleSelectionChange: typeof handleSelectionChange !== 'undefined' ? handleSelectionChange : () => {},
          getSummaries: typeof getSummaries !== 'undefined' ? getSummaries : () => []
        }
      }
    })
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) { app.component(key, component) }
    app.use(ElementPlus)
    app.mount('#app')
  <\/script>
</body>
</html>`
}

// 全屏预览（新窗口）
const openFullscreenPreview = async (template: PageTemplate) => {
  const html = generatePreviewHtml(template.htmlCode || '', template.cssCode || '', template.jsCode || '')
  const newWindow = window.open('', '_blank')
  if (newWindow) {
    newWindow.document.write(html)
    newWindow.document.close()
  } else {
    ElMessage.error('无法打开新窗口，请检查浏览器是否阻止了弹窗')
  }
  
  try {
    await pageTemplateService.incrementViewCount(template.id!)
  } catch (e) { /* ignore */ }
}



// 打开全屏预览页面（新页面）
const openLivePreview = (template: PageTemplate) => {
  router.push(`/template-preview/${template.id}`)
}





const copyCode = async (template: PageTemplate) => {
  const scriptTag = '<' + 'script setup lang="ts">'
  const scriptEndTag = '</' + 'script>'
  const styleTag = '<' + 'style scoped>'
  const styleEndTag = '</' + 'style>'
  
  const fullCode = `<template>
${template.htmlCode}
</template>

${scriptTag}
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  remember: false
})

const loading = ref(false)

const handleSubmit = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('操作成功！')
  }, 1500)
}
${template.jsCode || ''}
${scriptEndTag}

${styleTag}
${template.cssCode || ''}
${styleEndTag}`
  
  try {
    await navigator.clipboard.writeText(fullCode)
    ElMessage.success('完整代码已复制到剪贴板')
    await pageTemplateService.incrementCopyCount(template.id!)
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

onMounted(loadTemplates)
</script>

<template>
  <div class="page-templates">
    <!-- 紧凑头部 -->
    <div class="compact-header">
      <div class="header-left">
        <span class="header-icon">📄</span>
        <span class="header-title">页面模板库</span>
        <span class="header-divider"></span>
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索模板..." 
          :prefix-icon="Search" 
          clearable 
          class="search-input" 
        />
        <el-select v-model="selectedTechStack" placeholder="技术栈" clearable class="tech-select">
          <el-option v-for="tech in TECH_STACKS" :key="tech.value" :label="`${tech.icon} ${tech.label}`" :value="tech.value" />
        </el-select>
      </div>
      <div class="header-right">
        <el-button :icon="Refresh" @click="loadTemplates" circle size="small" />
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建</el-button>
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="category-bar">
      <button :class="['cat-btn', { active: !selectedCategory }]" @click="selectedCategory = ''">全部</button>
      <button 
        v-for="cat in TEMPLATE_CATEGORIES" 
        :key="cat.value"
        :class="['cat-btn', { active: selectedCategory === cat.value }]"
        @click="selectedCategory = cat.value"
      >
        {{ cat.icon }} {{ cat.label }}
      </button>
    </div>

    <!-- 模板网格 - 主要展示区 -->
    <div v-loading="loading" class="templates-grid">
      <div 
        v-for="template in filteredTemplates" 
        :key="template.id" 
        class="template-card"
        :style="{ '--card-color': getCategoryColor(template.category) }"

      >
        <!-- 卡片顶部装饰 -->
        <div class="card-accent"></div>
        
        <!-- 卡片主体 -->
        <div class="card-content">
          <!-- 分类和技术栈标签 -->
          <div class="card-tags">
            <span class="tag category-tag">
              {{ getCategoryIcon(template.category) }} {{ getCategoryLabel(template.category) }}
            </span>
            <span v-if="template.techStack" class="tag tech-tag">
              {{ getTechStackIcon(template.techStack) }} {{ getTechStackLabel(template.techStack) }}
            </span>
          </div>

          <!-- 模板名称 -->
          <h3 class="card-name">{{ template.name }}</h3>
          
          <!-- 模板描述 -->
          <p class="card-desc">{{ template.description || '暂无描述' }}</p>

          <!-- 统计信息 -->
          <div class="card-stats">
            <span class="stat-item">
              <span class="stat-icon">👁</span>
              <span class="stat-value">{{ template.viewCount || 0 }}</span>
            </span>
            <span class="stat-item">
              <span class="stat-icon">📋</span>
              <span class="stat-value">{{ template.copyCount || 0 }}</span>
            </span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
          <button class="action-btn primary" @click="openLivePreview(template)" title="预览编辑">
            <Monitor class="btn-icon" />
            <span>预览</span>
          </button>
          <button class="action-btn" @click="openFullscreenPreview(template)" title="全屏预览">
            <FullScreen class="btn-icon" />
            <span>全屏</span>
          </button>
          <button class="action-btn icon-only" @click="copyCode(template)" title="复制代码">
            <CopyDocument class="btn-icon" />
          </button>
          <button class="action-btn icon-only" @click="openEditDialog(template)" title="编辑">
            <Edit class="btn-icon" />
          </button>
          <button class="action-btn icon-only danger" @click="deleteTemplate(template)" title="删除">
            <Delete class="btn-icon" />
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && filteredTemplates.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p class="empty-text">暂无模板</p>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">创建第一个模板</el-button>
      </div>
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑模板' : '新建模板'" width="1000px" top="3vh">
      <el-form :model="formData" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="模板名称" required>
              <el-input v-model="formData.name" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类">
              <el-select v-model="formData.category" style="width: 100%">
                <el-option v-for="cat in TEMPLATE_CATEGORIES" :key="cat.value" :label="`${cat.icon} ${cat.label}`" :value="cat.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="技术栈">
              <el-select v-model="formData.techStack" style="width: 100%" clearable>
                <el-option v-for="tech in TECH_STACKS" :key="tech.value" :label="`${tech.icon} ${tech.label}`" :value="tech.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="模板描述" />
        </el-form-item>
        <el-form-item label="Vue 代码" required>
          <el-input v-model="fullVueCode" type="textarea" :rows="20" placeholder="完整的 Vue 单文件组件代码" class="code-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-templates {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 16px 24px;
  background: #f5f7fa;
  overflow: hidden;
}

/* 紧凑头部 */
.compact-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.header-divider {
  width: 1px;
  height: 20px;
  background: rgba(255,255,255,0.3);
  margin: 0 8px;
}

.search-input {
  width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.15);
  border: none;
  box-shadow: none;
}

.search-input :deep(.el-input__inner) {
  color: #fff;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: rgba(255,255,255,0.7);
}

.search-input :deep(.el-input__prefix) {
  color: rgba(255,255,255,0.7);
}

.tech-select {
  width: 140px;
}

.tech-select :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.15);
  border: none;
  box-shadow: none;
}

.tech-select :deep(.el-input__inner) {
  color: #fff;
}

.tech-select :deep(.el-input__inner::placeholder) {
  color: rgba(255,255,255,0.7);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-right .el-button {
  background: rgba(255,255,255,0.2);
  border: none;
  color: #fff;
}

.header-right .el-button:hover {
  background: rgba(255,255,255,0.3);
}

/* 分类标签栏 */
.category-bar {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  margin-bottom: 12px;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.cat-btn {
  padding: 6px 14px;
  border: 1px solid #e4e7ed;
  background: #fff;
  border-radius: 16px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  transition: all 0.2s;
}

.cat-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.cat-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-color: transparent;
}

/* 模板网格 - 主要区域 */
.templates-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  overflow-y: auto;
  padding: 4px;
  align-content: start;
}

/* 模板卡片 */
.template-card {
  --card-color: #667eea;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 220px;
}

.template-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.18);
}

.card-accent {
  height: 4px;
  background: var(--card-color);
}

.card-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.tag {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.category-tag {
  background: color-mix(in srgb, var(--card-color) 12%, transparent);
  color: var(--card-color);
}

.tech-tag {
  background: #f0f2f5;
  color: #606266;
}

.card-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-desc {
  color: #909399;
  font-size: 13px;
  margin: 0;
  line-height: 1.5;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-stats {
  display: flex;
  gap: 16px;
  margin-top: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.stat-icon {
  font-size: 12px;
}

.stat-value {
  font-weight: 500;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fafbfc;
  border-top: 1px solid #f0f2f5;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  background: #fff;
  color: #606266;
  border: 1px solid #e4e7ed;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
}

.action-btn.primary:hover {
  opacity: 0.9;
}

.action-btn.icon-only {
  padding: 6px;
}

.action-btn.danger:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

.btn-icon {
  width: 14px;
  height: 14px;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 15px;
  color: #909399;
  margin: 0 0 16px 0;
}

/* 代码输入框 */
.code-input :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  background: #fafafa;
}





/* 响应式 */
@media (max-width: 768px) {
  .page-templates {
    padding: 12px;
  }
  
  .compact-header {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }
  
  .header-left {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .header-divider {
    display: none;
  }
  
  .search-input {
    width: 100%;
  }
  
  .templates-grid {
    grid-template-columns: 1fr;
  }
  
  .template-card {
    height: auto;
    min-height: 200px;
  }
}
</style>
