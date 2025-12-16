<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close, FullScreen, Edit, Refresh, CopyDocument, ArrowLeft } from '@element-plus/icons-vue'
import pageTemplateService, { type PageTemplate } from '@/services/pageTemplateService'

const route = useRoute()
const router = useRouter()

const template = ref<PageTemplate | null>(null)
const loading = ref(true)
const previewIframeRef = ref<HTMLIFrameElement | null>(null)
const showEditor = ref(false)
const editorCode = ref('')
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })

// 生成完整Vue代码
const generateFullCode = (t: PageTemplate) => {
  const scriptOpen = '<' + 'script setup>'
  const scriptClose = '</' + 'script>'
  const styleOpen = '<' + 'style scoped>'
  const styleClose = '</' + 'style>'
  
  return `<template>
${t.htmlCode || '  <!-- 模板内容 -->'}
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

${t.jsCode || '// 自定义逻辑'}
${scriptClose}

${styleOpen}
${t.cssCode || '/* 样式代码 */'}
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
    const customMatch = scriptContent.match(/handleSubmit[\s\S]*?\}\s*\n([\s\S]*)$/)
    if (customMatch) {
      jsCode = customMatch[1].trim()
    }
  }
  
  return { htmlCode, jsCode, cssCode }
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

// 加载模板
const loadTemplate = async () => {
  const id = route.params.id as string
  if (!id) {
    ElMessage.error('模板ID不存在')
    router.push('/page-templates')
    return
  }
  
  loading.value = true
  try {
    template.value = await pageTemplateService.getTemplateById(Number(id))
    if (template.value) {
      editorCode.value = generateFullCode(template.value)
      await pageTemplateService.incrementViewCount(Number(id))
      setTimeout(() => updatePreview(), 100)
    } else {
      ElMessage.error('模板不存在')
      router.push('/page-templates')
    }
  } catch (e) {
    ElMessage.error('加载模板失败')
    router.push('/page-templates')
  } finally {
    loading.value = false
  }
}

// 更新预览
const updatePreview = () => {
  if (!previewIframeRef.value) return
  const parsed = parseFullCode(editorCode.value)
  const html = generatePreviewHtml(parsed.htmlCode, parsed.cssCode, parsed.jsCode)
  
  // 创建新的 iframe 内容，避免重复声明变量
  const iframe = previewIframeRef.value
  const blob = new Blob([html], { type: 'text/html' })
  iframe.src = URL.createObjectURL(blob)
}

// 保存修改
const saveChanges = async () => {
  if (!template.value?.id) return
  
  const parsed = parseFullCode(editorCode.value)
  template.value.htmlCode = parsed.htmlCode
  template.value.cssCode = parsed.cssCode
  template.value.jsCode = parsed.jsCode
  
  try {
    await pageTemplateService.updateTemplate(template.value.id, template.value)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// 复制代码
const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(editorCode.value)
    ElMessage.success('代码已复制到剪贴板')
    if (template.value?.id) {
      await pageTemplateService.incrementCopyCount(template.value.id)
    }
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

// 右键菜单
const handleContextMenu = (e: MouseEvent) => {
  e.preventDefault()
  contextMenuPosition.value = { x: e.clientX, y: e.clientY }
  contextMenuVisible.value = true
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

// 返回列表
const goBack = () => {
  router.push('/page-templates')
}

// 切换编辑器
const toggleEditor = () => {
  showEditor.value = !showEditor.value
}

// 键盘快捷键
const handleKeydown = (e: KeyboardEvent) => {
  // 如果在编辑器中输入，不处理快捷键
  if (e.target instanceof HTMLTextAreaElement) {
    if (e.ctrlKey && e.key === 's') {
      e.preventDefault()
      saveChanges()
    }
    return
  }
  
  if (e.ctrlKey && e.key === 's') {
    e.preventDefault()
    saveChanges()
  }
  if (e.key === 'Escape') {
    if (showEditor.value) {
      showEditor.value = false
    } else {
      goBack()
    }
  }
  // E 键快速打开/关闭编辑器
  if (e.key === 'e' || e.key === 'E') {
    toggleEditor()
  }
}

onMounted(() => {
  loadTemplate()
  document.addEventListener('click', closeContextMenu)
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeContextMenu)
  document.removeEventListener('keydown', handleKeydown)
})
</script>


<template>
  <div class="template-preview-page" @contextmenu="handleContextMenu">
    <!-- 顶部工具栏 -->
    <div class="preview-toolbar" :class="{ 'toolbar-hidden': !showEditor }">
      <div class="toolbar-left">
        <button class="toolbar-btn back-btn" @click="goBack" title="返回列表 (Esc)">
          <ArrowLeft class="btn-icon" />
          <span>返回</span>
        </button>
        <div class="template-info" v-if="template">
          <span class="template-name">{{ template.name }}</span>
          <span class="template-category">{{ template.category }}</span>
        </div>
      </div>
      <div class="toolbar-right">
        <button class="toolbar-btn" @click="updatePreview" title="刷新预览">
          <Refresh class="btn-icon" />
        </button>
        <button class="toolbar-btn" @click="copyCode" title="复制代码">
          <CopyDocument class="btn-icon" />
        </button>
        <button class="toolbar-btn" :class="{ active: showEditor }" @click="toggleEditor" title="编辑代码">
          <Edit class="btn-icon" />
          <span>{{ showEditor ? '隐藏编辑器' : '编辑代码' }}</span>
        </button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="preview-main" v-loading="loading">
      <!-- 预览区域 -->
      <div class="preview-area" :class="{ 'with-editor': showEditor }">
        <iframe ref="previewIframeRef" class="preview-iframe"></iframe>
        

      </div>

      <!-- 代码编辑器 -->
      <transition name="slide">
        <div v-if="showEditor" class="editor-panel">
          <div class="editor-header">
            <div class="editor-title">
              <Edit class="title-icon" />
              <span>实时代码编辑</span>
            </div>
            <div class="editor-actions">
              <button class="editor-btn" @click="updatePreview" title="刷新预览">
                <Refresh class="btn-icon" />
                刷新
              </button>
              <button class="editor-btn primary" @click="saveChanges" title="保存修改 (Ctrl+S)">
                保存
              </button>
              <button class="editor-btn close" @click="showEditor = false" title="关闭编辑器">
                <Close class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="editor-body">
            <textarea 
              v-model="editorCode" 
              class="code-textarea"
              @input="updatePreview"
              spellcheck="false"
            ></textarea>
          </div>
          <div class="editor-footer">
            <span class="shortcut-hint">Ctrl+S 保存 | Esc 关闭编辑器</span>
          </div>
        </div>
      </transition>
    </div>

    <!-- 右键菜单 -->
    <transition name="fade">
      <div 
        v-if="contextMenuVisible" 
        class="context-menu"
        :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
      >
        <div class="menu-item" @click="toggleEditor">
          <Edit class="menu-icon" />
          <span>{{ showEditor ? '隐藏编辑器' : '打开编辑器' }}</span>
        </div>
        <div class="menu-item" @click="updatePreview">
          <Refresh class="menu-icon" />
          <span>刷新预览</span>
        </div>
        <div class="menu-item" @click="copyCode">
          <CopyDocument class="menu-icon" />
          <span>复制代码</span>
        </div>
        <div class="menu-divider"></div>
        <div class="menu-item" @click="goBack">
          <ArrowLeft class="menu-icon" />
          <span>返回列表</span>
        </div>
      </div>
    </transition>
  </div>
</template>


<style scoped>
.template-preview-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #1a1a2e;
  overflow: hidden;
}

/* 顶部工具栏 */
.preview-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  flex-shrink: 0;
  z-index: 100;
  transition: transform 0.3s ease;
}

.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.toolbar-btn.active {
  background: rgba(255, 255, 255, 0.3);
}

.toolbar-btn.back-btn {
  background: rgba(0, 0, 0, 0.2);
}

.btn-icon {
  width: 18px;
  height: 18px;
}

.template-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 16px;
  padding-left: 16px;
  border-left: 1px solid rgba(255, 255, 255, 0.2);
}

.template-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.template-category {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
}

/* 主内容区 */
.preview-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

/* 预览区域 */
.preview-area {
  flex: 1;
  position: relative;
  transition: all 0.3s ease;
}

.preview-area.with-editor {
  flex: 0 0 55%;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: #fff;
}



/* 编辑器面板 */
.editor-panel {
  flex: 0 0 45%;
  display: flex;
  flex-direction: column;
  background: #1e1e1e;
  border-left: 1px solid #333;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #252526;
  border-bottom: 1px solid #333;
}

.editor-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.title-icon {
  width: 16px;
  height: 16px;
  color: #667eea;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.editor-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #3c3c3c;
  border: none;
  border-radius: 4px;
  color: #ccc;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.editor-btn:hover {
  background: #4c4c4c;
  color: #fff;
}

.editor-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.editor-btn.primary:hover {
  opacity: 0.9;
}

.editor-btn.close {
  padding: 6px;
}

.editor-body {
  flex: 1;
  overflow: hidden;
}

.code-textarea {
  width: 100%;
  height: 100%;
  padding: 16px;
  background: #1e1e1e;
  border: none;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: none;
  outline: none;
}

.code-textarea:focus {
  background: #1e1e1e;
}

.editor-footer {
  padding: 8px 16px;
  background: #252526;
  border-top: 1px solid #333;
}

.shortcut-hint {
  font-size: 11px;
  color: #666;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  min-width: 180px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  z-index: 1000;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 14px;
  color: #333;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #667eea;
}

.menu-icon {
  width: 16px;
  height: 16px;
  color: #909399;
}

.menu-item:hover .menu-icon {
  color: #667eea;
}

.menu-divider {
  height: 1px;
  background: #eee;
  margin: 6px 0;
}

/* 动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .preview-area.with-editor {
    flex: 0 0 40%;
  }
  
  .editor-panel {
    flex: 0 0 60%;
  }
}

@media (max-width: 768px) {
  .preview-toolbar {
    padding: 0 12px;
    height: 50px;
  }
  
  .toolbar-btn span {
    display: none;
  }
  
  .template-info {
    display: none;
  }
  
  .preview-area.with-editor {
    display: none;
  }
  
  .editor-panel {
    flex: 1;
  }
}
</style>
