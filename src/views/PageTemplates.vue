<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  CopyDocument,
  Edit,
  Delete,
  Refresh,
  Monitor,
  FullScreen,
  Key,
  Document,
  Grid,
  CreditCard,
  Menu,
  DataLine,
  Box,
  Platform,
  Connection,
  Star,
  Collection
} from '@element-plus/icons-vue'
import pageTemplateService, { type PageTemplate, TEMPLATE_CATEGORIES, TECH_STACKS } from '@/services/pageTemplateService'

const router = useRouter()

const templates = ref<PageTemplate[]>([])
const loading = ref(false)
const searchInputRef = ref<{ focus: () => void } | null>(null)
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

const fullVueCode = ref('')

const handleSearchShortcut = (event: KeyboardEvent) => {
  if (event.ctrlKey && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    searchInputRef.value?.focus()
  }
}

const getCategoryIconComponent = (value: string) => {
  const iconMap: Record<string, unknown> = {
    login: Key,
    form: Document,
    table: Grid,
    card: CreditCard,
    modal: CopyDocument,
    layout: Menu,
    dashboard: DataLine,
    other: Box,
    list: Collection,
    nav: Star,
    bigscreen: Monitor
  }

  return iconMap[value] || Box
}

const getCategoryColor = (value: string) => {
  const colorMap: Record<string, string> = {
    login: '#6366f1',
    form: '#3b82f6',
    table: '#10b981',
    card: '#f59e0b',
    modal: '#ef4444',
    layout: '#8b5cf6',
    dashboard: '#ec4899',
    other: '#64748b',
    list: '#06b6d4',
    nav: '#f97316',
    bigscreen: '#0ea5e9'
  }

  return colorMap[value] || '#3b82f6'
}

const getTechStackStyle = (value: string) => {
  if (value?.includes('Vue')) return { bg: '#ecfdf5', text: '#059669', border: '#a7f3d0' }
  if (value?.includes('React')) return { bg: '#eff6ff', text: '#2563eb', border: '#bfdbfe' }
  if (value?.includes('HTML')) return { bg: '#fff7ed', text: '#ea580c', border: '#fed7aa' }
  return { bg: '#f1f5f9', text: '#475569', border: '#e2e8f0' }
}

const generateFullCode = (template: PageTemplate) => {
  const scriptOpen = '<' + 'script setup>'
  const scriptClose = '</' + 'script>'
  const styleOpen = '<' + 'style scoped>'
  const styleClose = '</' + 'style>'

  return `<template>
${template.htmlCode || '  <!-- 在这里输入模板 HTML -->'}
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
    ElMessage.success('操作成功')
  }, 1500)
}

${template.jsCode || '// 在这里补充页面逻辑'}
${scriptClose}

${styleOpen}
${template.cssCode || '/* 在这里补充样式 */'}
${styleClose}`
}

const parseFullCode = (code: string) => {
  const templateMatch = code.match(/<template>([\s\S]*?)<\/template>/)
  const scriptMatch = code.match(/<script[^>]*>([\s\S]*?)<\/script>/)
  const styleMatch = code.match(/<style[^>]*>([\s\S]*?)<\/style>/)

  const htmlCode = templateMatch ? templateMatch[1].trim() : ''
  let jsCode = ''
  const cssCode = styleMatch ? styleMatch[1].trim() : ''

  if (scriptMatch) {
    const scriptContent = scriptMatch[1]
    const customMatch = scriptContent.match(/handleSubmit[\s\S]*?\}\s*\n([\s\S]*)$/)

    if (customMatch && customMatch[1].trim()) {
      jsCode = customMatch[1].trim()
    } else {
      let content = scriptContent
      content = content.replace(/import\s+[\s\S]*?from\s+['"][^'"]+['"]\s*/g, '')
      content = content.replace(/const\s+form\s*=\s*reactive\s*\(\s*\{[\s\S]*?\}\s*\)\s*/g, '')
      content = content.replace(/const\s+loading\s*=\s*ref\s*\(\s*false\s*\)\s*/g, '')
      content = content.replace(/const\s+activeTab\s*=\s*ref\s*\(\s*['"][^'"]*['"]\s*\)\s*/g, '')
      content = content.replace(/const\s+handleSubmit\s*=\s*\(\s*\)\s*=>\s*\{[\s\S]*?\}\s*/g, '')
      jsCode = content.trim()
    }
  }

  return { htmlCode, jsCode, cssCode }
}

const filteredTemplates = computed(() => {
  let result = templates.value

  if (selectedCategory.value) {
    result = result.filter((template) => template.category === selectedCategory.value)
  }

  if (selectedTechStack.value) {
    result = result.filter((template) => template.techStack === selectedTechStack.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (template) =>
        template.name.toLowerCase().includes(keyword) ||
        template.description?.toLowerCase().includes(keyword)
    )
  }

  return result
})

const getTechStackLabel = (value: string) => {
  return TECH_STACKS.find((tech) => tech.value === value)?.label || value
}

const getCategoryLabel = (value: string) => {
  return TEMPLATE_CATEGORIES.find((category) => category.value === value)?.label || value
}

const loadTemplates = async () => {
  loading.value = true
  try {
    templates.value = await pageTemplateService.getAllTemplates()
  } catch {
    ElMessage.error('加载模板失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEditing.value = false
  formData.value = {
    name: '',
    description: '',
    category: 'login',
    techStack: 'Vue3+ElementPlus',
    htmlCode: '',
    cssCode: '',
    jsCode: ''
  }
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
  } catch {
    ElMessage.error('保存失败')
  }
}

const deleteTemplate = async (template: PageTemplate) => {
  try {
    await ElMessageBox.confirm(`确定删除模板「${template.name}」吗？`, '确认删除', { type: 'warning' })
    await pageTemplateService.deleteTemplate(template.id!)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

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
  <script src="https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js"><\/script>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    html, body, #app { width: 100%; height: 100%; }
    ${cssCode || ''}
  </style>
</head>
<body>
  <div id="app">${htmlCode}</div>
  <script>
    const { createApp, ref, reactive, onMounted, onUnmounted, computed, watch, nextTick } = Vue
    const app = createApp({
      setup() {
        const form = reactive({ username: '', password: '', email: '', phone: '', remember: false, agree: false })
        const loading = ref(false)
        const activeTab = ref('login')
        const previewVisible = ref(false)
        const openPreview = () => { previewVisible.value = true }
        const handleSubmit = () => {
          loading.value = true
          setTimeout(() => { loading.value = false; ElementPlus.ElMessage.success('操作成功') }, 1500)
        }
        ${jsCode || ''}
        return {
          form,
          loading,
          handleSubmit,
          activeTab,
          previewVisible,
          openPreview,
          tableData: typeof tableData !== 'undefined' ? tableData : ref([]),
          treeData: typeof treeData !== 'undefined' ? treeData : ref([]),
          selectedRows: typeof selectedRows !== 'undefined' ? selectedRows : ref([]),
          handleSelectionChange: typeof handleSelectionChange !== 'undefined' ? handleSelectionChange : () => {},
          getSummaries: typeof getSummaries !== 'undefined' ? getSummaries : () => [],
          currentTime: typeof currentTime !== 'undefined' ? currentTime : ref(''),
          metrics: typeof metrics !== 'undefined' ? metrics : ref([]),
          items: typeof items !== 'undefined' ? items : ref([])
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

const openFullscreenPreview = async (template: PageTemplate) => {
  const html = generatePreviewHtml(template.htmlCode || '', template.cssCode || '', template.jsCode || '')
  const newWindow = window.open('', '_blank')

  if (newWindow) {
    newWindow.document.write(html)
    newWindow.document.close()
  } else {
    ElMessage.error('无法打开新窗口，请检查浏览器弹窗拦截设置')
  }

  try {
    await pageTemplateService.incrementViewCount(template.id!)
  } catch {
    // ignore
  }
}

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
    ElMessage.success('操作成功')
  }, 1500)
}
${template.jsCode || ''}
${scriptEndTag}

${styleTag}
${template.cssCode || ''}
${styleEndTag}`

  try {
    await navigator.clipboard.writeText(fullCode)
    ElMessage.success('代码已复制')
    await pageTemplateService.incrementCopyCount(template.id!)
  } catch {
    ElMessage.error('复制失败')
  }
}

onMounted(() => {
  loadTemplates()
  window.addEventListener('keydown', handleSearchShortcut)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleSearchShortcut)
})
</script>

<template>
  <div class="template-studio-page">
    <span class="ambient ambient-a"></span>
    <span class="ambient ambient-b"></span>
    <span class="ambient ambient-c"></span>
    <span class="mesh-grid"></span>

    <div class="studio-shell">
      <section class="studio-hero">
        <header class="hero-top">
          <div class="brand-block">
            <div class="brand-mark" aria-hidden="true">
              <Menu />
            </div>
            <div class="brand-copy">
              <p class="hero-kicker">模板工坊</p>
              <h1>页面模板中心</h1>
              <p>在统一工作台中筛选、预览、复用和维护你的页面模板资产。</p>
            </div>
          </div>

          <div class="hero-cta-group">
            <button class="ghost-icon-btn" title="刷新模板" @click="loadTemplates">
              <Refresh />
            </button>
            <button class="main-cta" @click="openCreateDialog">
              <Plus class="icon-sm" />
              新建模板
            </button>
          </div>
        </header>

        <div class="search-select-row">
          <el-input
            ref="searchInputRef"
            v-model="searchKeyword"
            placeholder="搜索模板名称或描述（Ctrl+K）"
            class="studio-input"
            :prefix-icon="Search"
            clearable
          />

          <el-select
            v-model="selectedTechStack"
            placeholder="技术栈"
            clearable
            class="studio-select"
            popper-class="studio-select-popper"
          >
            <el-option
              v-for="tech in TECH_STACKS"
              :key="tech.value"
              :label="tech.label"
              :value="tech.value"
            />
          </el-select>

          <button
            class="clear-filter-btn"
            :disabled="!searchKeyword && !selectedCategory && !selectedTechStack"
            @click="searchKeyword = ''; selectedCategory = ''; selectedTechStack = ''"
          >
            清空筛选
          </button>
        </div>

        <div class="hero-metrics">
          <article class="metric-card">
            <div class="metric-icon-wrap"><Collection class="icon-sm" /></div>
            <div>
              <p class="metric-label">模板总数</p>
              <p class="metric-value">{{ templates.length }}</p>
            </div>
          </article>

          <article class="metric-card">
            <div class="metric-icon-wrap"><Grid class="icon-sm" /></div>
            <div>
              <p class="metric-label">筛选结果</p>
              <p class="metric-value">{{ filteredTemplates.length }}</p>
            </div>
          </article>

          <article class="metric-card">
            <div class="metric-icon-wrap"><Connection class="icon-sm" /></div>
            <div>
              <p class="metric-label">分类数量</p>
              <p class="metric-value">{{ TEMPLATE_CATEGORIES.length }}</p>
            </div>
          </article>

          <article class="metric-card">
            <div class="metric-icon-wrap"><Platform class="icon-sm" /></div>
            <div>
              <p class="metric-label">技术栈数量</p>
              <p class="metric-value">{{ TECH_STACKS.length }}</p>
            </div>
          </article>
        </div>
      </section>

      <section class="filter-strip">
        <div class="filter-strip-head">
          <Connection class="icon-xs" />
          <span>分类筛选</span>
        </div>

        <div class="filter-scroll">
          <button
            :class="['filter-chip', { active: !selectedCategory }]"
            @click="selectedCategory = ''"
          >
            <Collection class="icon-xs" />
            全部模板
          </button>

          <button
            v-for="cat in TEMPLATE_CATEGORIES"
            :key="cat.value"
            :class="['filter-chip', { active: selectedCategory === cat.value }]"
            @click="selectedCategory = cat.value"
          >
            <component :is="getCategoryIconComponent(cat.value)" class="icon-xs" />
            {{ cat.label }}
          </button>
        </div>
      </section>

      <main class="gallery-panel" v-loading="loading">
        <div v-if="filteredTemplates.length > 0" class="templates-grid">
          <article
            v-for="template in filteredTemplates"
            :key="template.id"
            class="template-card"
            :style="{ '--accent-color': getCategoryColor(template.category) }"
          >
            <div class="card-preview" @click="openLivePreview(template)">
              <div class="preview-gradient"></div>
              <div class="preview-overlay">
                <span class="preview-hint">
                  <Monitor class="icon-xs" /> 在线预览
                </span>
                <button class="preview-ghost-btn" @click.stop="openFullscreenPreview(template)">
                  <FullScreen class="icon-xs" /> 全屏
                </button>
              </div>

              <div class="preview-icon-wrap">
                <component :is="getCategoryIconComponent(template.category)" class="preview-icon" />
              </div>

              <span class="category-pill">{{ getCategoryLabel(template.category) }}</span>
            </div>

            <div class="card-body">
              <div class="card-title-row">
                <h3 class="template-name" :title="template.name">{{ template.name }}</h3>
                <el-dropdown trigger="click">
                  <button class="more-btn" @click.stop>
                    ...
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :icon="Edit" @click="openEditDialog(template)">编辑</el-dropdown-item>
                      <el-dropdown-item :icon="CopyDocument" @click="copyCode(template)">复制代码</el-dropdown-item>
                      <el-dropdown-item :icon="Monitor" @click="openLivePreview(template)">在线预览</el-dropdown-item>
                      <el-dropdown-item :icon="FullScreen" @click="openFullscreenPreview(template)">全屏</el-dropdown-item>
                      <el-dropdown-item divided :icon="Delete" class="text-danger" @click="deleteTemplate(template)">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <p class="template-desc">{{ template.description || '暂无描述，建议补充使用场景和交互说明。' }}</p>

              <div class="card-foot">
                <span
                  v-if="template.techStack"
                  class="stack-pill"
                  :style="{
                    backgroundColor: getTechStackStyle(template.techStack).bg,
                    color: getTechStackStyle(template.techStack).text,
                    borderColor: getTechStackStyle(template.techStack).border
                  }"
                >
                  {{ getTechStackLabel(template.techStack) }}
                </span>

                <div class="usage-stats">
                  <span class="usage-item" title="浏览次数">
                    <Monitor class="icon-xxs" /> {{ template.viewCount || 0 }}
                  </span>
                  <span class="usage-item" title="复制次数">
                    <CopyDocument class="icon-xxs" /> {{ template.copyCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <section v-else-if="!loading" class="empty-state">
          <div class="empty-icon-wrap">
            <Box />
          </div>
          <h3>没有匹配的模板</h3>
          <p>尝试调整筛选条件，或创建一个新的模板。</p>
          <button class="main-cta" @click="openCreateDialog">
            <Plus class="icon-sm" />
            新建模板
          </button>
        </section>
      </main>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑模板' : '新建模板'"
      width="1120px"
      top="3vh"
      class="studio-dialog"
      destroy-on-close
    >
      <div class="dialog-head-tip">
        <Document class="icon-sm" />
        支持粘贴完整 Vue SFC 代码（template/script/style），并自动解析。
      </div>

      <el-form :model="formData" label-position="top" class="studio-form">
        <div class="meta-grid">
          <el-form-item label="模板名称" required class="field-span-2">
            <el-input v-model="formData.name" placeholder="例如：登录页 V1" />
          </el-form-item>

          <el-form-item label="分类">
            <el-select v-model="formData.category" style="width: 100%">
              <el-option
                v-for="cat in TEMPLATE_CATEGORIES"
                :key="cat.value"
                :label="cat.label"
                :value="cat.value"
              >
                <span class="option-line">
                  <component :is="getCategoryIconComponent(cat.value)" class="icon-xs" />
                  {{ cat.label }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="技术栈">
            <el-select v-model="formData.techStack" style="width: 100%" clearable>
              <el-option
                v-for="tech in TECH_STACKS"
                :key="tech.value"
                :label="tech.label"
                :value="tech.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="简要描述模板用途、关键交互和推荐场景"
          />
        </el-form-item>

        <el-form-item label="Vue 单文件代码" required class="code-editor-field">
          <div class="editor-wrap">
            <div class="editor-head">
              <span class="editor-title">SFC 编辑器</span>
              <span class="editor-sub">建议包含 template + script setup + style scoped 三段结构</span>
            </div>
            <el-input
              v-model="fullVueCode"
              type="textarea"
              :rows="20"
              placeholder="粘贴完整 Vue 单文件代码"
              class="code-input"
            />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <button class="dialog-btn secondary" @click="dialogVisible = false">取消</button>
        <button class="dialog-btn primary" @click="saveTemplate">保存模板</button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Space+Grotesk:wght@500;600;700&display=swap');

.template-studio-page {
  --bg-page: #f6f9ff;
  --bg-surface: rgba(255, 255, 255, 0.82);
  --bg-surface-strong: rgba(255, 255, 255, 0.96);
  --line-soft: #d9e6f5;
  --line-strong: #c7d8ee;
  --text-title: #0d2142;
  --text-main: #344b66;
  --text-muted: #6c8199;
  --primary: #1663d8;
  --primary-soft: #e7f0ff;
  --accent: #f59e0b;
  --danger: #d83b2f;
  --shadow-large: 0 28px 56px rgba(15, 23, 42, 0.12);
  --shadow-soft: 0 12px 24px rgba(15, 23, 42, 0.09);

  position: relative;
  min-height: calc(100vh - 64px);
  overflow: hidden;
  padding: 24px 22px 28px;
  color: var(--text-main);
  background:
    radial-gradient(75vw 40vh at -10% -15%, #dbeafe 0%, transparent 58%),
    radial-gradient(68vw 40vh at 105% -12%, #ffeacc 0%, transparent 55%),
    linear-gradient(150deg, #f4f8ff 0%, var(--bg-page) 46%, #f8fbff 100%);
  font-family: 'Manrope', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.ambient {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.ambient-a {
  width: 280px;
  height: 280px;
  top: 4%;
  left: 42%;
  background: radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0) 70%);
}

.ambient-b {
  width: 320px;
  height: 320px;
  right: -70px;
  top: 18%;
  background: radial-gradient(circle at 30% 30%, rgba(245, 158, 11, 0.22) 0%, rgba(245, 158, 11, 0) 72%);
}

.ambient-c {
  width: 350px;
  height: 350px;
  left: -130px;
  bottom: -130px;
  background: radial-gradient(circle at 50% 50%, rgba(22, 99, 216, 0.15) 0%, rgba(22, 99, 216, 0) 70%);
}

.mesh-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.24;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.15) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.15) 1px, transparent 1px);
  background-size: 34px 34px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.4) 0%, rgba(0, 0, 0, 0.05) 72%, rgba(0, 0, 0, 0) 100%);
}

.studio-shell {
  position: relative;
  z-index: 1;
  width: min(100%, 1700px);
  margin: 0 auto;
  display: grid;
  gap: 14px;
}

.studio-hero,
.filter-strip,
.gallery-panel {
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  background: var(--bg-surface);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-large);
}

.studio-hero {
  padding: 22px;
  display: grid;
  gap: 16px;
}

.hero-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.brand-block {
  display: flex;
  gap: 14px;
  min-width: 0;
}

.brand-mark {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(140deg, #1156c1 0%, #2877ea 60%, #4b95ff 100%);
  box-shadow: 0 14px 22px rgba(17, 86, 193, 0.3);
  flex-shrink: 0;
}

.brand-mark :deep(svg) {
  width: 28px;
  height: 28px;
}

.hero-kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: #5f7390;
}

.brand-copy h1 {
  margin: 8px 0;
  font-family: 'Space Grotesk', 'Noto Sans SC', 'PingFang SC', sans-serif;
  color: var(--text-title);
  font-size: clamp(1.7rem, 2.4vw, 2.25rem);
  line-height: 1.15;
}

.brand-copy p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.62;
}

.hero-cta-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.ghost-icon-btn {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: 1px solid var(--line-strong);
  background: var(--bg-surface-strong);
  color: #5a7292;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.22s ease;
}

.ghost-icon-btn:hover {
  border-color: #9eb8dc;
  color: #1d4ed8;
  transform: translateY(-1px);
}

.ghost-icon-btn :deep(svg) {
  width: 18px;
  height: 18px;
}

.main-cta {
  border: 0;
  height: 42px;
  border-radius: 12px;
  padding: 0 16px;
  background: linear-gradient(140deg, #0f55c2 0%, #1663d8 55%, #2e7eef 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
  box-shadow: 0 14px 24px rgba(15, 85, 194, 0.27);
}

.main-cta:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 28px rgba(15, 85, 194, 0.34);
}

.search-select-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px 116px;
  gap: 10px;
}

.studio-input,
.studio-select {
  width: 100%;
}

.studio-input :deep(.el-input__wrapper),
.studio-select :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 12px;
  box-shadow: none;
  border: 1px solid var(--line-soft);
  background: rgba(255, 255, 255, 0.92);
}

.studio-input :deep(.el-input__wrapper.is-focus),
.studio-select :deep(.el-input__wrapper.is-focus) {
  border-color: #7fa8da;
  box-shadow: 0 0 0 3px rgba(22, 99, 216, 0.14);
}

.clear-filter-btn {
  border: 1px solid var(--line-strong);
  background: var(--bg-surface-strong);
  color: #4f6580;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-filter-btn:hover:not(:disabled) {
  color: #1e40af;
  border-color: #96b3da;
}

.clear-filter-btn:disabled {
  opacity: 0.48;
  cursor: not-allowed;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 68px;
  border-radius: 14px;
  border: 1px solid #d4e2f2;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96) 0%, rgba(235, 244, 255, 0.8) 100%);
  padding: 12px;
}

.metric-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 11px;
  background: var(--primary-soft);
  color: #1550b2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-label {
  margin: 0;
  color: #67809c;
  font-size: 12px;
  font-weight: 600;
}

.metric-value {
  margin: 3px 0 0;
  color: var(--text-title);
  font-size: 21px;
  font-family: 'Space Grotesk', 'Noto Sans SC', 'PingFang SC', sans-serif;
  font-weight: 700;
  line-height: 1;
}

.filter-strip {
  display: grid;
  grid-template-columns: 130px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  padding: 12px 14px;
}

.filter-strip-head {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  font-weight: 700;
  color: #4f6480;
}

.filter-scroll {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.filter-scroll::-webkit-scrollbar {
  height: 6px;
}

.filter-scroll::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #c4d6ed;
}

.filter-chip {
  height: 34px;
  border-radius: 999px;
  border: 1px solid #d3e0f2;
  background: #fff;
  color: #5d7390;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-chip:hover {
  border-color: #9eb8dc;
  color: #1843aa;
}

.filter-chip.active {
  border-color: rgba(22, 99, 216, 0.3);
  color: #0f4db7;
  background: var(--primary-soft);
}

.gallery-panel {
  min-height: 62vh;
  padding: 14px;
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 14px;
}

.template-card {
  border-radius: 16px;
  border: 1px solid #d7e4f5;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-soft);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.template-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 30px rgba(15, 23, 42, 0.12);
  border-color: color-mix(in srgb, var(--accent-color) 40%, #d7e4f5);
}

.card-preview {
  position: relative;
  height: 170px;
  cursor: pointer;
  overflow: hidden;
  border-bottom: 1px solid #dbe7f6;
  background: linear-gradient(160deg, #f5f9ff 0%, #f9fbff 75%);
}

.preview-gradient {
  position: absolute;
  inset: -30% -20% auto;
  height: 130%;
  background: radial-gradient(circle at 40% 45%, color-mix(in srgb, var(--accent-color) 20%, #ffffff) 0%, transparent 65%);
}

.preview-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 12px;
  opacity: 0;
  background: linear-gradient(180deg, rgba(13, 33, 66, 0.08) 0%, rgba(13, 33, 66, 0.18) 100%);
  transition: opacity 0.22s ease;
  z-index: 2;
}

.template-card:hover .preview-overlay {
  opacity: 1;
}

.preview-hint {
  height: 30px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #1843aa;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
}

.preview-ghost-btn {
  border: 1px solid rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  border-radius: 999px;
  height: 30px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.preview-icon-wrap {
  position: absolute;
  left: 16px;
  bottom: 14px;
  width: 62px;
  height: 62px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-color);
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 12px 20px rgba(15, 23, 42, 0.16);
  z-index: 1;
}

.preview-icon {
  width: 32px;
  height: 32px;
}

.category-pill {
  position: absolute;
  right: 12px;
  bottom: 14px;
  z-index: 1;
  border-radius: 999px;
  padding: 6px 11px;
  font-size: 11px;
  font-weight: 700;
  color: #17489e;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(196, 214, 237, 0.9);
}

.card-body {
  padding: 14px;
  display: grid;
  gap: 11px;
  min-height: 178px;
}

.card-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.template-name {
  margin: 0;
  color: var(--text-title);
  font-size: 16px;
  line-height: 1.35;
  font-weight: 700;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.more-btn {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  border: 1px solid #d6e3f3;
  background: #fff;
  color: #68809d;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  flex-shrink: 0;
}

.more-btn:hover {
  color: #1f4fb1;
  border-color: #9eb8dc;
  background: #f4f8ff;
}

.template-desc {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-foot {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.stack-pill {
  border: 1px solid transparent;
  border-radius: 999px;
  height: 26px;
  padding: 0 10px;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.usage-stats {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.usage-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #7389a2;
  font-size: 11px;
  font-weight: 600;
}

.empty-state {
  min-height: 58vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.empty-icon-wrap {
  width: 76px;
  height: 76px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6c8ab0;
  background: linear-gradient(145deg, #edf4ff 0%, #f7faff 100%);
  margin-bottom: 16px;
}

.empty-icon-wrap :deep(svg) {
  width: 34px;
  height: 34px;
}

.empty-state h3 {
  margin: 0;
  color: var(--text-title);
  font-size: 22px;
  font-family: 'Space Grotesk', 'Noto Sans SC', 'PingFang SC', sans-serif;
}

.empty-state p {
  margin: 8px 0 16px;
  color: var(--text-muted);
  font-size: 14px;
}

.dialog-head-tip {
  margin-bottom: 14px;
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px solid #d5e3f4;
  background: #eef5ff;
  color: #2f5182;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.studio-form {
  display: grid;
  gap: 8px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.field-span-2 {
  grid-column: span 2;
}

.option-line {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.editor-wrap {
  border-radius: 14px;
  border: 1px solid #d6e3f3;
  overflow: hidden;
  background: #f9fbff;
}

.editor-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid #d6e3f3;
  background: linear-gradient(180deg, #f5f8ff 0%, #fcfdff 100%);
}

.editor-title {
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  font-weight: 800;
  color: #355987;
}

.editor-sub {
  font-size: 12px;
  color: #7a8ea6;
}

.code-input :deep(.el-textarea__inner) {
  border: 0;
  border-radius: 0;
  box-shadow: none;
  background: #f9fbff;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.6;
}

.dialog-btn {
  height: 40px;
  border-radius: 11px;
  padding: 0 18px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  border: 0;
}

.dialog-btn.secondary {
  border: 1px solid #d0dded;
  background: #fff;
  color: #4a607d;
  margin-right: 10px;
}

.dialog-btn.primary {
  color: #fff;
  background: linear-gradient(140deg, #0f55c2 0%, #1663d8 60%, #2e7eef 100%);
  box-shadow: 0 12px 20px rgba(15, 85, 194, 0.28);
}

.icon-sm {
  width: 16px;
  height: 16px;
}

.icon-xs {
  width: 14px;
  height: 14px;
}

.icon-xxs {
  width: 12px;
  height: 12px;
}

.text-danger {
  color: var(--danger);
}

:deep(.studio-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.studio-dialog .el-dialog__header) {
  margin: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #d8e5f5;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 100%);
}

:deep(.studio-dialog .el-dialog__title) {
  font-family: 'Space Grotesk', 'Noto Sans SC', 'PingFang SC', sans-serif;
  font-size: 20px;
  color: var(--text-title);
}

:deep(.studio-dialog .el-dialog__body) {
  padding: 18px 20px;
}

:deep(.studio-dialog .el-dialog__footer) {
  padding: 14px 20px 20px;
  border-top: 1px solid #d8e5f5;
}

:deep(.studio-dialog .el-form-item__label) {
  font-size: 13px;
  font-weight: 700;
  color: #38597e;
}

:deep(.studio-dialog .el-input__wrapper),
:deep(.studio-dialog .el-textarea__inner),
:deep(.studio-dialog .el-select__wrapper) {
  border-radius: 10px;
  border: 1px solid #d4e1f3;
  box-shadow: none;
}

:deep(.studio-dialog .el-input__wrapper.is-focus),
:deep(.studio-dialog .el-select__wrapper.is-focused),
:deep(.studio-dialog .el-textarea__inner:focus) {
  border-color: #7fa8da;
  box-shadow: 0 0 0 3px rgba(22, 99, 216, 0.14);
}

@media (max-width: 1200px) {
  .template-studio-page {
    min-height: calc(100vh - 64px);
    overflow: auto;
  }

  .hero-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-cta-group {
    align-self: flex-end;
  }

  .search-select-row {
    grid-template-columns: 1fr 220px;
  }

  .clear-filter-btn {
    grid-column: 1 / -1;
    height: 40px;
  }

  .hero-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .template-studio-page {
    padding: 14px 12px 18px;
  }

  .studio-hero,
  .gallery-panel {
    border-radius: 18px;
    padding: 14px;
  }

  .filter-strip {
    grid-template-columns: 1fr;
  }

  .search-select-row {
    grid-template-columns: 1fr;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .templates-grid {
    grid-template-columns: 1fr;
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }

  .field-span-2 {
    grid-column: span 1;
  }

  .editor-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .dialog-btn {
    width: 100%;
  }

  .dialog-btn.secondary {
    margin: 0 0 10px;
  }
}
</style>
