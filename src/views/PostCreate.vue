<script setup lang="ts">
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElSelect,
  ElOption,
  ElButton,
  ElMessage,
  ElInputNumber,
} from 'element-plus'
import { Star, Setting } from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { normalizeImageUrl, processImageUrlsInMarkdown } from '@/utils/imageUtils'
import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import 'github-markdown-css/github-markdown-light.css'
import AIConfigDialog from '@/components/AIConfigDialog.vue'
import {
  generateBlogSummary,
  generateBlogTitle,
  generateBlogCategory,
  generateBlogTags,
  generateBlogAllInfo,
} from '@/utils/aiService'

// 定义分类类型
interface Category {
  id: number
  name: string
}

// 定义标签类型
interface Tag {
  id: number
  name: string
}

// 表单数据
const form = ref({
  title: '',
  desc: '',
  content: '',
  author: '周鹏飞',
  categoryId: null as number | null,
  tagIds: [] as number[],
})

// 分类和标签数据
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)
const aiGenerating = ref(false)
const aiConfigDialogVisible = ref(false)
// AI服务配置 - 已重构，不再需要复杂的配置
const aiService = ref({
  provider: 'ollama',
  baseUrl: 'http://localhost:11434',
  model: 'deepseek-r1:14b',
})
// 新建分类/标签对话框
const newCategoryDialogVisible = ref(false)
const newCategoryName = ref('')
const creatingCategory = ref(false)
const newTagDialogVisible = ref(false)
const newTagName = ref('')
const creatingTag = ref(false)
const router = useRouter()
const route = useRoute()
const contentInput = ref<InstanceType<typeof ElInput> | null>(null)

// 获取所有分类
const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/categories')
    categories.value = response.data
  } catch (error) {
    ElMessage.error('获取分类失败')
    console.error('获取分类失败:', error)
  }
}

// 获取所有标签
const fetchTags = async () => {
  try {
    const response = await axios.get('/api/tags')
    tags.value = response.data
  } catch (error) {
    ElMessage.error('获取标签失败')
    console.error('获取标签失败:', error)
  }
}

// 处理图片粘贴事件
const handlePaste = (event: ClipboardEvent) => {
  const items = event.clipboardData?.items
  if (!items) return

  for (let i = 0; i < items.length; i++) {
    const item = items[i]
    if (item.type.indexOf('image') !== -1) {
      event.preventDefault()
      const file = item.getAsFile()
      if (file) {
        showImageUploadDialog(file)
      }
    }
  }
}

// 处理图片拖拽事件
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  event.stopPropagation()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy'
  }
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  event.stopPropagation()

  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return

  // 处理拖拽的图片文件
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (file.type.startsWith('image/')) {
      showImageUploadDialog(file)
      break // 一次只处理一个图片
    }
  }
}

// 处理拖拽进入和离开事件，提供视觉反馈
const isDragOver = ref(false)

const handleDragEnter = (event: DragEvent) => {
  event.preventDefault()
  event.stopPropagation()
  isDragOver.value = true
}

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  event.stopPropagation()
  // 只有当真正离开拖拽区域时才设置false
  if (!(event.currentTarget as Element)?.contains(event.relatedTarget as Node)) {
    isDragOver.value = false
  }
}

// 图片上传对话框相关
const imageUploadDialogVisible = ref(false)
const imageFile = ref<File | null>(null)
const customImageName = ref('')
const uploadingImage = ref(false)
const imageWidth = ref('')
const imageHeight = ref('')
const imageWidthUnit = ref('px')
const imageHeightUnit = ref('px')
const imagePreviewUrl = ref('')
const imageNaturalWidth = ref(0)
const imageNaturalHeight = ref(0)

// 显示图片上传对话框
const showImageUploadDialog = (file: File) => {
  imageFile.value = file
  customImageName.value = file.name.replace(/\.[^/.]+$/, '') // 去掉扩展名
  imageWidth.value = '' // 重置宽高
  imageHeight.value = ''
  imageWidthUnit.value = 'px'
  imageHeightUnit.value = 'px'

  // 创建图片预览URL
  imagePreviewUrl.value = URL.createObjectURL(file)

  // 获取图片的原始尺寸
  const img = new Image()
  img.onload = () => {
    imageNaturalWidth.value = img.naturalWidth
    imageNaturalHeight.value = img.naturalHeight

    // 根据图片比例设置默认宽高
    setDefaultImageDimensions()
  }
  img.src = imagePreviewUrl.value

  imageUploadDialogVisible.value = true
}

// 根据图片比例设置默认宽高
const setDefaultImageDimensions = () => {
  if (imageNaturalWidth.value && imageNaturalHeight.value) {
    // 计算图片比例
    const aspectRatio = imageNaturalWidth.value / imageNaturalHeight.value

    // 设置默认宽度为600px，高度按比例计算
    const defaultWidth = 600
    const defaultHeight = Math.round(defaultWidth / aspectRatio)

    imageWidth.value = defaultWidth.toString()
    imageHeight.value = defaultHeight.toString()
    imageWidthUnit.value = 'px'
    imageHeightUnit.value = 'px'
  }
}

// 根据宽度自动计算高度（保持比例）
const autoCalculateHeight = () => {
  if (imageWidth.value && imageNaturalWidth.value && imageNaturalHeight.value) {
    const width = parseFloat(imageWidth.value)
    const aspectRatio = imageNaturalWidth.value / imageNaturalHeight.value

    if (imageWidthUnit.value === 'px') {
      // 如果宽度是px，高度也设为px
      const height = Math.round(width / aspectRatio)
      imageHeight.value = height.toString()
      imageHeightUnit.value = 'px'
    } else if (imageWidthUnit.value === '%') {
      // 如果宽度是%，高度也设为%
      const height = Math.round(width / aspectRatio)
      imageHeight.value = height.toString()
      imageHeightUnit.value = '%'
    }
  }
}

// 根据高度自动计算宽度（保持比例）
const autoCalculateWidth = () => {
  if (imageHeight.value && imageNaturalWidth.value && imageNaturalHeight.value) {
    const height = parseFloat(imageHeight.value)
    const aspectRatio = imageNaturalWidth.value / imageNaturalHeight.value

    if (imageHeightUnit.value === 'px') {
      // 如果高度是px，宽度也设为px
      const width = Math.round(height * aspectRatio)
      imageWidth.value = width.toString()
      imageWidthUnit.value = 'px'
    } else if (imageHeightUnit.value === '%') {
      // 如果高度是%，宽度也设为%
      const width = Math.round(height * aspectRatio)
      imageWidth.value = width.toString()
      imageWidthUnit.value = '%'
    }
  }
}

// 上传图片到后端，返回图片 URL 并插入 Markdown
const uploadImage = async (
  file: File,
  customName?: string,
  width?: string,
  height?: string,
  widthUnit?: string,
  heightUnit?: string,
) => {
  try {
    uploadingImage.value = true
    const formData = new FormData()
    formData.append('file', file)
    if (customName) {
      formData.append('customName', customName)
    }

    const res = await axios.post('/api/uploads/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    const imageUrl: string = res.data.url

    // 构建图片Markdown语法，包含可选的宽高
    let imageMarkdown = `\n![${customName || file.name}](${imageUrl})`

    // 如果设置了宽高，添加到Markdown中
    if (width || height) {
      const styleParts = []
      if (width) {
        styleParts.push(`width: ${width}${widthUnit || 'px'}`)
      }
      if (height) {
        styleParts.push(`height: ${height}${heightUnit || 'px'}`)
      }
      if (styleParts.length > 0) {
        imageMarkdown = `\n<img src="${imageUrl}" alt="${customName || file.name}" style="${styleParts.join('; ')}" />\n`
      } else {
        imageMarkdown = `\n![${customName || file.name}](${imageUrl})\n`
      }
    } else {
      imageMarkdown = `\n![${customName || file.name}](${imageUrl})\n`
    }

    // 在光标位置插入图片Markdown语法
    const textarea = contentInput.value?.textarea
    if (textarea) {
      const startPos = textarea.selectionStart
      const endPos = textarea.selectionEnd
      const textBefore = form.value.content.substring(0, startPos)
      const textAfter = form.value.content.substring(endPos)

      form.value.content = textBefore + imageMarkdown + textAfter

      await nextTick()
      const newCursorPos = startPos + imageMarkdown.length
      textarea.setSelectionRange(newCursorPos, newCursorPos)
      textarea.focus()
    }
  } catch (e) {
    ElMessage.error('图片上传失败')
    console.error('图片上传失败:', e)
  } finally {
    uploadingImage.value = false
  }
}

// 确认上传图片
const confirmUploadImage = async () => {
  if (!imageFile.value) return

  await uploadImage(
    imageFile.value,
    customImageName.value,
    imageWidth.value,
    imageHeight.value,
    imageWidthUnit.value,
    imageHeightUnit.value,
  )
  imageUploadDialogVisible.value = false
  // 清理图片预览URL
  if (imagePreviewUrl.value) {
    URL.revokeObjectURL(imagePreviewUrl.value)
  }
  imageFile.value = null
  customImageName.value = ''
  imageWidth.value = ''
  imageHeight.value = ''
  imageWidthUnit.value = 'px'
  imageHeightUnit.value = 'px'
  imagePreviewUrl.value = ''
  imageNaturalWidth.value = 0
  imageNaturalHeight.value = 0
}

// 取消上传图片
const cancelUploadImage = () => {
  imageUploadDialogVisible.value = false
  // 清理图片预览URL
  if (imagePreviewUrl.value) {
    URL.revokeObjectURL(imagePreviewUrl.value)
  }
  imageFile.value = null
  customImageName.value = ''
  imageWidth.value = ''
  imageHeight.value = ''
  imageWidthUnit.value = 'px'
  imageHeightUnit.value = 'px'
  imagePreviewUrl.value = ''
  imageNaturalWidth.value = 0
  imageNaturalHeight.value = 0
}

// 初始化 markdown-it
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>'
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

// 配置插件 - 自动为标题生成 ID
md.use(markdownItAnchor, {
  permalink: false,
  level: [1, 2, 3, 4, 5, 6],
  slugify: (s: string) => {
    return encodeURIComponent(s.trim())
  }
})

// Markdown 实时预览
const previewHtml = computed(() => {
  const content = form.value.content || ''
  if (!content) return ''
  const processed = processImageUrlsInMarkdown(content)
  return md.render(processed)
})

// 编辑/预览融合视图控制
const viewMode = ref<'edit' | 'preview'>('edit')
const editorWidth = ref(50) // 分屏时中间编辑区在(编辑+预览)中的百分比
const tocWidth = ref(220) // 目录宽度（px）
const mdBodyRef = ref<HTMLDivElement | null>(null)
const editorPaneRef = ref<HTMLDivElement | null>(null)
const previewPaneRef = ref<HTMLDivElement | null>(null)
const previewContentRef = ref<HTMLDivElement | null>(null)
const containerWidth = ref(0)

let resizing: null | 'main' | 'toc' = null
const updateContainerWidth = () => {
  containerWidth.value = mdBodyRef.value?.getBoundingClientRect().width || 0
}
const startResize = (type: 'main' | 'toc') => {
  resizing = type
  const onMove = (evt: MouseEvent) => {
    if (!resizing || !mdBodyRef.value) return
    const rect = mdBodyRef.value.getBoundingClientRect()
    if (resizing === 'main') {
      // 以目录右侧为起点计算百分比
      const usable = rect.width - tocWidth.value
      const x = Math.min(rect.width, Math.max(tocWidth.value + 60, evt.clientX - rect.left))
      const percent = ((x - tocWidth.value) / usable) * 100
      editorWidth.value = Math.max(20, Math.min(80, Math.round(percent)))
    } else if (resizing === 'toc') {
      const x = Math.min(rect.width - 200, Math.max(140, evt.clientX - rect.left))
      tocWidth.value = Math.round(x)
    }
  }
  const onUp = () => {
    resizing = null
    window.removeEventListener('mousemove', onMove)
    window.removeEventListener('mouseup', onUp)
  }
  window.addEventListener('mousemove', onMove)
  window.addEventListener('mouseup', onUp)
}
onMounted(() => {
  updateContainerWidth()
  window.addEventListener('resize', updateContainerWidth)
})

// 编辑与预览同步滚动
const syncScroll = (source: 'edit' | 'preview') => {
  if (!editorPaneRef.value || !previewPaneRef.value) return
  const editor = editorPaneRef.value
  const preview = previewPaneRef.value

  if (source === 'edit') {
    const ratio = editor.scrollTop / (editor.scrollHeight - editor.clientHeight)
    const targetScrollTop = ratio * (preview.scrollHeight - preview.clientHeight)
    preview.scrollTop = targetScrollTop
  } else {
    const ratio = preview.scrollTop / (preview.scrollHeight - preview.clientHeight)
    const targetScrollTop = ratio * (editor.scrollHeight - editor.clientHeight)
    editor.scrollTop = targetScrollTop
  }
}

// 提取目录（标题）
interface TocItem {
  text: string
  level: number
  index: number
}
const extractHeadings = (md: string): TocItem[] => {
  const items: TocItem[] = []
  const regex = /^(#{1,6})\s+(.+)$/gm
  let match: RegExpExecArray | null
  while ((match = regex.exec(md)) !== null) {
    items.push({ text: match[2].trim(), level: match[1].length, index: match.index })
  }
  return items
}
const tocItems = computed(() => extractHeadings(form.value.content || ''))

let previewHeadingEls: Element[] = []
let editorHeadingEls: Element[] = []
const capturePreviewHeadings = () => {
  const container = previewContentRef.value || previewPaneRef.value
  if (!container) return
  previewHeadingEls = Array.from(container.querySelectorAll('h1, h2, h3, h4, h5, h6'))
}

const captureEditorHeadings = () => {
  const textarea = contentInput.value?.textarea
  if (!textarea) return

  // 获取编辑器内容并解析标题位置
  const content = textarea.value || ''
  const lines = content.split('\n')
  let currentIndex = 0
  editorHeadingEls = []

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    if (/^#{1,6}\s/.test(line)) {
      // 创建一个虚拟元素来模拟标题位置
      const virtualHeading = {
        offsetTop: currentIndex,
        lineIndex: i,
        text: line.trim(),
      }
      editorHeadingEls.push(virtualHeading as any)
    }
    currentIndex += line.length + 1 // +1 for newline
  }
}

// 当前激活的目录项索引
const activeTocIndex = ref(-1)

// 监听编辑器滚动，更新侧边目录高亮
const updateTocHighlight = () => {
  if (!editorPaneRef.value) return

  const editor = editorPaneRef.value
  const scrollTop = editor.scrollTop
  const containerHeight = editor.clientHeight

  // 找到当前滚动位置对应的标题
  let activeIndex = -1
  let minDistance = Infinity

  for (let i = 0; i < editorHeadingEls.length; i++) {
    const heading = editorHeadingEls[i] as any
    const distance = Math.abs(heading.offsetTop - scrollTop)

    if (distance < minDistance) {
      minDistance = distance
      activeIndex = i
    }
  }

  if (activeIndex !== activeTocIndex.value) {
    activeTocIndex.value = activeIndex
  }
}

// 监听编辑器滚动事件
const handleEditorScroll = () => {
  updateTocHighlight()
}

// 监听内容变化，重新捕获标题
watch(
  form,
  () => {
    nextTick(() => {
      captureEditorHeadings()
    })
  },
  { deep: true },
)

// 监听预览内容变化
watch(previewHtml, async () => {
  await nextTick()
  capturePreviewHeadings()
})

// 监听视图模式变化
watch(viewMode, async (v) => {
  if (v !== 'edit') {
    await nextTick()
    capturePreviewHeadings()
  }
  if (v !== 'preview') {
    await nextTick()
    captureEditorHeadings()
  }
})

// 目录跳转：一次点击同步编辑和预览位置
const jumpToHeading = (i: number) => {
  const item = tocItems.value[i]
  if (!item) return

  // 1. 设置编辑器光标位置
  const textarea = contentInput.value?.textarea
  if (textarea) {
    textarea.focus()
    textarea.setSelectionRange(item.index, item.index)

    // 2. 计算编辑器应该滚动到的位置（让光标在可视区域中央）
    const textareaHeight = textarea.clientHeight
    const lineHeight = 20 // 估算行高
    const targetScrollTop = Math.max(0, item.index - textareaHeight / 2)
    textarea.scrollTop = targetScrollTop
  }

  // 3. 等待DOM更新后同步预览位置
  nextTick(() => {
    // 重新捕获预览区标题元素
    capturePreviewHeadings()

    // 找到对应的预览标题并滚动到该位置
    const previewHeading = previewHeadingEls[i] as HTMLElement
    if (previewHeading && previewPaneRef.value) {
      const container = previewPaneRef.value
      const headingTop = previewHeading.offsetTop
      const containerHeight = container.clientHeight

      // 让标题显示在预览区域的中央
      const targetScrollTop = Math.max(0, headingTop - containerHeight / 2)
      container.scrollTo({
        top: targetScrollTop,
        behavior: 'smooth',
      })
    }
  })
}

const activeIndex = ref(-1)

// 获取来源页面类型
const getSourcePageType = () => {
  // 优先使用路由查询参数
  const fromQuery = route.query.from as string
  if (fromQuery) {
    return fromQuery === 'admin' ? 'admin' : 'home'
  }

  // 备用方案：检查 referrer
  const referrer = document.referrer
  if (referrer.includes('/admin/posts')) {
    return 'admin'
  } else if (referrer.includes(window.location.origin + '/') && !referrer.includes('/admin')) {
    return 'home'
  }

  // 默认返回首页
  return 'home'
}

// 根据来源返回对应页面
const returnToSource = () => {
  const sourceType = getSourcePageType()
  if (sourceType === 'admin') {
    router.push('/admin/posts')
  } else {
    router.push('/')
  }
}

// 提交表单
const submitForm = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('标题和内容是必填项')
    return
  }

  try {
    loading.value = true
    await axios.post('/api/posts', {
      title: form.value.title,
      desc: form.value.desc,
      content: form.value.content,
      author: form.value.author || 'Anonymous',
      categoryId: form.value.categoryId,
      tagIds: form.value.tagIds,
    })

    ElMessage.success('博客文章创建成功')
    returnToSource()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '创建失败')
    console.error('创建博客文章失败:', error)
  } finally {
    loading.value = false
  }
}

// AI生成摘要
const generateAISummary = async () => {
  if (!form.value.content || form.value.content.trim().length < 50) {
    ElMessage.warning('请先输入文章内容（至少50个字符）')
    return
  }

  try {
    aiGenerating.value = true

    const content = form.value.content.trim()
    const title = form.value.title || ''

    // 使用AI服务生成摘要
    const result = await generateBlogSummary(content, title)

    if (result.success && result.content) {
      form.value.desc = result.content
      ElMessage.success(`AI摘要生成成功！使用${result.provider}服务`)
    } else {
      ElMessage.warning(result.error || '无法生成摘要，请手动输入')
    }
  } catch (error) {
    ElMessage.error('AI摘要生成失败，请稍后重试')
    console.error('AI摘要生成失败:', error)
  } finally {
    aiGenerating.value = false
  }
}

// AI一键生成所有内容
const generateAIAll = async () => {
  if (!form.value.content || form.value.content.trim().length < 50) {
    ElMessage.warning('请先输入文章内容（至少50个字符）')
    return
  }

  try {
    aiGenerating.value = true

    const content = form.value.content.trim()

    // 使用统一的AI生成函数，一次性获取所有信息
    const result = await generateBlogAllInfo(content, categories.value, tags.value)

    // 自动填充表单字段
    if (result.title && result.title !== '未生成标题') {
      form.value.title = result.title
    }

    if (result.summary && result.summary !== '未生成摘要') {
      form.value.desc = result.summary
    }

    if (result.categoryId) {
      form.value.categoryId = result.categoryId
    }

    if (result.tagIds && result.tagIds.length > 0) {
      form.value.tagIds = result.tagIds
    }

    // 处理新标签推荐
    if (result.newTags && result.newTags.length > 0) {
      // 显示新标签确认弹窗
      showNewTagsConfirm(result.newTags)
      // 等待用户确认后再继续
      return
    }

    ElMessage.success('AI一键生成完成！请检查生成的内容')

    // 显示发布确认弹窗
    showPublishConfirm()
  } catch (error) {
    ElMessage.error('AI生成失败，请稍后重试')
    console.error('AI生成失败:', error)
  } finally {
    aiGenerating.value = false
  }
}

// 发布确认弹窗
const publishConfirmVisible = ref(false)
const showPublishConfirm = () => {
  publishConfirmVisible.value = true
}

// 确认发布
const confirmPublish = async () => {
  publishConfirmVisible.value = false
  await submitForm()
}

// 取消发布，继续编辑
const cancelPublish = () => {
  publishConfirmVisible.value = false
  ElMessage.info('您可以继续编辑文章内容，完成后手动发布')
}

// 新标签确认弹窗（可勾选且可编辑名称）
interface EditableNewTag {
  original: string
  name: string
  selected: boolean
}

const newTagsConfirmVisible = ref(false)
const editableNewTags = ref<EditableNewTag[]>([])
const showNewTagsConfirm = (tags: string[]) => {
  editableNewTags.value = tags.map((t) => ({ original: t, name: t, selected: true }))
  newTagsConfirmVisible.value = true
}

// 确认创建新标签
const confirmCreateTags = async () => {
  try {
    const createdTagIds: number[] = []

    // 处理用户选择并可编辑后的新标签
    for (const item of editableNewTags.value) {
      if (!item.selected) continue
      const tagName = item.name.trim()
      if (!tagName) continue

      // 如果与现有标签重名，则直接复用现有标签ID
      const existing = tags.value.find((t) => t.name.trim().toLowerCase() === tagName.toLowerCase())
      if (existing) {
        createdTagIds.push(existing.id)
        continue
      }

      // 创建新的标签
      const response = await axios.post('/api/tags', { name: tagName })
      if (response.data && response.data.id) {
        createdTagIds.push(response.data.id)
        // 将新创建的标签添加到本地标签列表
        tags.value.push({
          id: response.data.id,
          name: tagName,
        })
      }
    }

    // 将新创建的标签ID添加到当前选择的标签中
    if (createdTagIds.length > 0) {
      form.value.tagIds = [...(form.value.tagIds || []), ...createdTagIds]
    }

    if (createdTagIds.length > 0) {
      // 去重合并到已选标签ID
      const current = new Set(form.value.tagIds || [])
      createdTagIds.forEach((id) => current.add(id))
      form.value.tagIds = Array.from(current)
      ElMessage.success(`已添加 ${createdTagIds.length} 个标签`)
    } else {
      ElMessage.info('未选择新标签，已跳过创建')
    }
    newTagsConfirmVisible.value = false
    editableNewTags.value = []

    // 继续完成AI一键生成流程
    ElMessage.success('AI一键生成完成！请检查生成的内容')
    showPublishConfirm()
  } catch (error) {
    ElMessage.error('创建标签失败，请稍后重试')
    console.error('创建标签失败:', error)
  }
}

// 取消创建新标签
const cancelCreateTags = () => {
  newTagsConfirmVisible.value = false
  editableNewTags.value = []
  ElMessage.info('已取消创建新标签')
}

// 新建分类
const openNewCategoryDialog = () => {
  newCategoryName.value = ''
  newCategoryDialogVisible.value = true
}

const createCategory = async () => {
  if (!newCategoryName.value.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    creatingCategory.value = true
    const response = await axios.post('/api/categories', { name: newCategoryName.value.trim() })
    const created = response.data
    if (created && created.id) {
      categories.value.push({ id: created.id, name: created.name })
      form.value.categoryId = created.id
      ElMessage.success('分类创建成功')
    }
    newCategoryDialogVisible.value = false
  } catch (error) {
    ElMessage.error('创建分类失败')
    console.error('创建分类失败:', error)
  } finally {
    creatingCategory.value = false
  }
}

// 新建标签
const openNewTagDialog = () => {
  newTagName.value = ''
  newTagDialogVisible.value = true
}

const createTag = async () => {
  if (!newTagName.value.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  try {
    creatingTag.value = true
    // 如果与现有标签重名，直接复用
    const name = newTagName.value.trim()
    const existing = tags.value.find((t) => t.name.trim().toLowerCase() === name.toLowerCase())
    if (existing) {
      if (!form.value.tagIds.includes(existing.id)) {
        form.value.tagIds.push(existing.id)
      }
      ElMessage.success('已添加已存在的标签')
      newTagDialogVisible.value = false
      return
    }
    const response = await axios.post('/api/tags', { name })
    const created = response.data
    if (created && created.id) {
      tags.value.push({ id: created.id, name })
      form.value.tagIds = [...(form.value.tagIds || []), created.id]
      ElMessage.success('标签创建成功')
    }
    newTagDialogVisible.value = false
  } catch (error) {
    ElMessage.error('创建标签失败')
    console.error('创建标签失败:', error)
  } finally {
    creatingTag.value = false
  }
}

// 打开AI配置对话框
const openAIConfig = () => {
  aiConfigDialogVisible.value = true
}

// 保存AI配置
const saveAIConfig = (newConfig: any) => {
  aiService.value = newConfig
  // 保存到本地存储
  localStorage.setItem('aiConfig', JSON.stringify(newConfig))
  ElMessage.success('AI配置已保存')
}

// 取消操作
const cancel = () => {
  returnToSource()
}

// 返回上一页 - 已删除，使用面包屑导航替代

// 组件挂载时获取数据
onMounted(() => {
  Promise.all([fetchCategories(), fetchTags()])

  // 加载AI配置
  const savedConfig = localStorage.getItem('aiConfig')
  if (savedConfig) {
    try {
      const config = JSON.parse(savedConfig)
      aiService.value = config
    } catch (error) {
      console.error('加载AI配置失败:', error)
    }
  } else {
    // 使用默认Ollama配置
    aiService.value = {
      provider: 'ollama',
      baseUrl: 'http://localhost:11434',
      model: 'deepseek-r1:14b',
    }
  }

  // 添加粘贴事件监听器
  // 使用nextTick确保DOM已经渲染完成
  nextTick(() => {
    const textarea = contentInput.value?.textarea
    if (textarea) {
      textarea.addEventListener('paste', handlePaste)
    }
  })
})

// 监听路由变化，当用户从其他页面返回时重新获取数据
router.afterEach((to, from) => {
  if (to.path === '/admin/posts/create') {
    fetchTags()
    fetchCategories()
  }
})
</script>

<template>
  <div class="post-create-container">
    <el-card class="form-card" v-loading="loading">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input
            v-model="form.desc"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要，如果不填写将从内容中自动截取"
          />
        </el-form-item>

        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="请输入作者名称" />
        </el-form-item>

        <el-form-item label="分类">
          <el-select
            v-model="form.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
            clearable
            popper-class="category-select-dropdown"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
          <el-button type="primary" link @click="openNewCategoryDialog">+ 新建分类</el-button>
        </el-form-item>

        <el-form-item label="标签">
          <el-select v-model="form.tagIds" multiple placeholder="请选择标签" style="width: 100%">
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
              :disabled="tag.name === '默认标签'"
            />
          </el-select>
          <el-button type="primary" link @click="openNewTagDialog">+ 新建标签</el-button>
        </el-form-item>

        <el-form-item label="内容" required>
          <div class="md-editor">
            <div class="md-toolbar">
              <div class="toolbar-left">
                <el-radio-group v-model="viewMode" size="small">
                  <el-radio-button label="edit">编辑</el-radio-button>
                  <el-radio-button label="preview">预览</el-radio-button>
                </el-radio-group>
              </div>
              <div class="toolbar-right">
                <el-button
                  class="ai-config-btn"
                  type="info"
                  :icon="Setting"
                  size="small"
                  @click="openAIConfig"
                  title="配置AI模型"
                >
                  AI配置
                </el-button>
                <el-button
                  class="ai-generate-all-btn"
                  type="primary"
                  :icon="Star"
                  :loading="aiGenerating"
                  @click="generateAIAll"
                  title="AI一键生成标题、摘要、分类和标签"
                  size="small"
                >
                  AI一键生成
                </el-button>
              </div>
            </div>

            <div
              class="md-body"
              :class="`mode-${viewMode}`"
              ref="mdBodyRef"
            >
              <div class="md-toc">
                <div class="toc-title">目录</div>
                <ul class="toc-list">
                  <li
                    v-for="(item, i) in tocItems"
                    :key="i"
                    :class="{ active: i === activeIndex }"
                    :style="{ paddingLeft: (item.level - 1) * 12 + 'px' }"
                  >
                    <a href="javascript:void(0)" @click="jumpToHeading(i)">{{ item.text }}</a>
                  </li>
                </ul>
              </div>
              <div
                v-show="viewMode !== 'preview'"
                class="md-editor-pane"
                ref="editorPaneRef"
                @scroll="syncScroll('edit')"
              >
                <el-input
                  ref="contentInput"
                  v-model="form.content"
                  type="textarea"
                  :rows="18"
                  placeholder="请输入文章内容，支持Markdown语法。可以通过Ctrl+V粘贴图片或直接拖拽图片到此处"
                  @paste="handlePaste"
                  @dragover="handleDragOver"
                  @drop="handleDrop"
                  @dragenter="handleDragEnter"
                  @dragleave="handleDragLeave"
                  :class="{ 'drag-over': isDragOver }"
                />
              </div>
              <div
                v-show="viewMode !== 'edit'"
                class="md-preview-pane"
                ref="previewPaneRef"
                @scroll="syncScroll('preview')"
              >
                <div class="markdown-body markdown-preview" ref="previewContentRef" v-html="previewHtml"></div>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <div class="action-buttons">
              <el-button type="primary" @click="submitForm" :loading="loading">发布文章</el-button>
              <el-button @click="cancel">取消</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- AI配置对话框 -->
    <AIConfigDialog v-model="aiConfigDialogVisible" @save="saveAIConfig" />

    <!-- 图片上传对话框 -->
    <el-dialog
      v-model="imageUploadDialogVisible"
      title="上传图片"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="image-upload-dialog">
        <!-- 图片预览区域 -->
        <div class="image-preview-container">
          <div v-if="imageFile" class="image-preview">
            <img :src="imagePreviewUrl" :alt="imageFile.name" class="preview-image" />
            <div class="image-info">
              <p><strong>文件名:</strong> {{ imageFile.name }}</p>
              <p><strong>原始尺寸:</strong> {{ imageNaturalWidth }} × {{ imageNaturalHeight }}</p>
              <p>
                <strong>比例:</strong>
                {{
                  imageNaturalWidth && imageNaturalHeight
                    ? (imageNaturalWidth / imageNaturalHeight).toFixed(2)
                    : '-'
                }}
              </p>
            </div>
          </div>
        </div>

        <!-- 表单区域 -->
        <el-form :model="form" label-width="100px" class="upload-form">
          <el-form-item label="自定义名称">
            <el-input v-model="customImageName" placeholder="请输入自定义名称" />
          </el-form-item>
          <el-form-item label="宽度">
            <el-input-number
              v-model="imageWidth"
              :min="0"
              :max="1000"
              placeholder="请输入宽度"
              @change="autoCalculateHeight"
            />
            <el-select
              v-model="imageWidthUnit"
              style="width: 100px; margin-left: 10px"
              @change="autoCalculateHeight"
            >
              <el-option label="px" value="px" />
              <el-option label="%" value="%" />
            </el-select>
          </el-form-item>
          <el-form-item label="高度">
            <el-input-number
              v-model="imageHeight"
              :min="0"
              :max="1000"
              placeholder="请输入高度"
              @change="autoCalculateWidth"
            />
            <el-select
              v-model="imageHeightUnit"
              style="width: 100px; margin-left: 10px"
              @change="autoCalculateWidth"
            >
              <el-option label="px" value="px" />
              <el-option label="%" value="%" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="setDefaultImageDimensions" type="info" size="small">
              重置为默认尺寸
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelUploadImage">取消</el-button>
          <el-button type="primary" @click="confirmUploadImage" :loading="uploadingImage">
            上传
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新建分类对话框 -->
    <el-dialog
      v-model="newCategoryDialogVisible"
      title="新建分类"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-input v-model="newCategoryName" placeholder="请输入分类名称" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newCategoryDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="creatingCategory" @click="createCategory"
            >创建</el-button
          >
        </span>
      </template>
    </el-dialog>

    <!-- 新建标签对话框 -->
    <el-dialog
      v-model="newTagDialogVisible"
      title="新建标签"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-input v-model="newTagName" placeholder="请输入标签名称" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newTagDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="creatingTag" @click="createTag">创建</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 发布确认弹窗 -->
    <el-dialog
      v-model="publishConfirmVisible"
      title="确认发布"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <p>您确定要发布这篇文章吗？发布后将无法修改。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelPublish">取消</el-button>
          <el-button type="primary" @click="confirmPublish" :loading="loading">
            确认发布
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新标签确认弹窗（可勾选且可编辑） -->
    <el-dialog
      v-model="newTagsConfirmVisible"
      title="选择并编辑要创建的新标签"
      width="460px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <p style="margin: 0 0 8px 0; color: #666">勾选需要创建的标签（默认全选），可直接修改名称：</p>
      <div
        v-for="(item, index) in editableNewTags"
        :key="index"
        style="display: flex; align-items: center; gap: 8px; margin: 6px 0"
      >
        <el-checkbox v-model="item.selected" />
        <el-input v-model="item.name" size="small" placeholder="输入标签名称" style="flex: 1" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelCreateTags">取消</el-button>
          <el-button type="primary" @click="confirmCreateTags" :loading="loading">
            创建所选
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.post-create-container {
  width: 95vw;
  margin: 0 auto;
  padding: var(--spacing-6);
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

.image-upload-dialog {
  display: flex;
  gap: var(--spacing-6);
}

.image-preview-container {
  flex: 1;
  max-width: 300px;
}

.image-preview {
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-3);
  box-shadow: var(--shadow-sm);
}

.image-info {
  text-align: left;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.image-info p {
  margin: var(--spacing-1) 0;
}

.upload-form {
  flex: 1;
}

.form-card {
  margin-bottom: var(--spacing-8);
  border-radius: var(--radius-2xl);
  background: var(--bg-glass);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  box-shadow: var(--shadow-glass);
  transition: all var(--transition-normal);
}

.form-card:hover {
  box-shadow: var(--shadow-card-hover);
}

.markdown-preview {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--spacing-5);
  background: var(--bg-glass);
  max-height: 600px;
  overflow: auto;
  backdrop-filter: blur(10px);
}

.md-editor {
  width: 100%;
}

.md-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-3);
  padding: var(--spacing-3) var(--spacing-4);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(139, 92, 246, 0.03) 100%);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.md-body {
  display: grid;
  gap: var(--spacing-4);
}

.md-body.mode-edit {
  grid-template-columns: 220px 1fr;
}

.md-body.mode-preview {
  grid-template-columns: 220px 1fr;
}

.md-editor-pane,
.md-preview-pane {
  min-height: 420px;
}

.md-toc {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--spacing-4);
  background: var(--bg-glass);
  backdrop-filter: blur(10px);
  max-height: 600px;
  overflow: auto;
  position: relative;
  box-shadow: var(--shadow-sm);
}

.toc-title {
  font-weight: var(--font-bold);
  margin-bottom: var(--spacing-3);
  color: var(--text-primary);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.toc-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.toc-list li {
  line-height: var(--leading-loose);
  transition: all var(--transition-fast);
}

.toc-list li.active > a {
  color: var(--primary-color);
  font-weight: var(--font-semibold);
}

.toc-list a {
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
  display: block;
  padding: var(--spacing-1) var(--spacing-2);
  border-radius: var(--radius-sm);
}

.toc-list a:hover {
  color: var(--primary-color);
  background: rgba(99, 102, 241, 0.08);
}

.md-editor-pane {
  overflow: auto;
}

.md-preview-pane {
  overflow: auto;
}

.markdown-preview {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--spacing-5);
  background: var(--bg-card);
  max-height: 600px;
  overflow: auto;
  font-family: var(--font-sans);
  box-shadow: var(--shadow-sm);
}

/* 使用与文章详情页相同的样式 */
.markdown-preview :deep(h1) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom: 2px solid #667eea;
  padding-bottom: 12px;
  margin-top: 24px;
  margin-bottom: 16px;
}

.markdown-preview :deep(h2) {
  color: #2c3e50;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-top: 20px;
  margin-bottom: 14px;
}

.markdown-preview :deep(h3) {
  color: #2c3e50;
  margin-top: 18px;
  margin-bottom: 12px;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 0 8px 8px 0;
  padding: 15px 20px;
  margin: 20px 0;
}

.markdown-preview :deep(a) {
  color: #667eea;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.markdown-preview :deep(a:hover) {
  border-bottom-color: #667eea;
}

.markdown-preview :deep(img) {
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  max-width: 100%;
}

.markdown-preview :deep(img:hover) {
  transform: scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.markdown-preview :deep(pre) {
  border-radius: 8px;
  margin: 16px 0;
  background: #f6f8fa !important;
  padding: 16px;
  overflow-x: auto;
}

.markdown-preview :deep(code:not(pre code)) {
  font-family: 'Monaco', 'Courier New', monospace;
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 3px;
  color: #e83e8c;
  font-size: 0.9em;
}

.markdown-preview :deep(table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  width: 100%;
  margin: 20px 0;
}

.markdown-preview :deep(th) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px;
}

.markdown-preview :deep(td) {
  padding: 10px 12px;
}

.markdown-preview :deep(tr:nth-child(even)) {
  background-color: #f8fafc;
}

.markdown-preview :deep(tr:hover) {
  background-color: #f1f5f9;
}

/* 分类选择器下拉框样式 */
:deep(.category-select-dropdown) {
  max-height: 200px !important;
  overflow-y: auto !important;
}

:deep(.category-select-dropdown .el-select-dropdown__list) {
  max-height: 200px !important;
  overflow-y: auto !important;
}

/* 摘要输入容器样式 */
.summary-input-container {
  width: 100%;
}

.ai-generate-all-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  font-size: 12px;
}

.ai-generate-all-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #66b1ff 0%, #85c8ff 100%);
}

.ai-generate-all-btn:active {
  transform: translateY(0);
}

.ai-generate-all-btn .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.ai-config-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(144, 147, 153, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 12px;
}

.ai-config-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(144, 147, 153, 0.4);
  background: linear-gradient(135deg, #a6a9ad 0%, #b8bbbf 100%);
}

/* 表单操作区域样式 */
.form-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-create-container {
    padding: 10px;
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .action-buttons {
    justify-content: center;
  }

  .md-toolbar {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }

  .toolbar-right {
    justify-content: center;
  }

  /* 返回按钮样式已删除 */
}
</style>
