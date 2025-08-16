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
  ElSkeleton,
  ElIcon,
} from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import axios from '@/utils/axios'
import { marked } from 'marked'
import { normalizeImageUrl } from '@/utils/imageUtils'
import Breadcrumb from '@/components/Breadcrumb.vue'

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

// 定义博客文章类型
interface BlogPost {
  id: number
  title: string
  desc: string
  content: string
  author: string
  categoryId: number | null
  tagIds: number[]
}

// 表单数据
const form = ref({
  title: '',
  desc: '',
  content: '',
  author: '',
  categoryId: null as number | null,
  tagIds: [] as number[],
})

// 保存编辑前的原始内容，用于对比图片差异
const originalContent = ref('')

// 分类和标签数据
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)
const dataLoading = ref(true)
const submitting = ref(false)
const router = useRouter()
const route = useRoute()
const postId = ref(Number(route.params.id))
const contentInput = ref<InstanceType<typeof ElInput> | null>(null)
// 新建分类/标签对话框
const newCategoryDialogVisible = ref(false)
const newCategoryName = ref('')
const creatingCategory = ref(false)
const newTagDialogVisible = ref(false)
const newTagName = ref('')
const creatingTag = ref(false)

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

// 获取博客文章详情
const fetchBlogPost = async () => {
  try {
    dataLoading.value = true
    const response = await axios.get(`/api/posts/${postId.value}`)
    // 后端返回的是单个PostDTO对象
    const post = response.data

    form.value = {
      title: post.title,
      desc: post.desc,
      content: post.content,
      author: post.author,
      categoryId: post.category?.id || null,
      tagIds: post.tags?.map((tag: any) => tag.id) || [],
    }
    originalContent.value = post.content // 保存原始内容
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取文章失败')
    console.error('获取博客文章失败:', error)
  } finally {
    dataLoading.value = false
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

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 提取内容中的图片“文件名”（与相对/绝对URL无关）
const extractImageFilenames = (content: string): string[] => {
  const filenames: string[] = []

  const pushFilename = (url: string) => {
    try {
      let path = url
      if (!path) return
      if (path.startsWith('http://') || path.startsWith('https://')) {
        const u = new URL(path)
        path = u.pathname
      }
      const idx = path.lastIndexOf('/')
      const name = idx >= 0 ? path.substring(idx + 1) : path
      if (name && !name.includes('/') && !name.includes('..')) {
        filenames.push(name)
      }
    } catch {
      // ignore
    }
  }

  // Markdown 图片
  const mdRegex = /!\[[^\]]*\]\(([^)]+)\)/g
  let m: RegExpExecArray | null
  while ((m = mdRegex.exec(content)) !== null) {
    pushFilename(m[1])
  }

  // HTML <img>
  const imgRegex = /<img[^>]+src=\"([^\"]+)\"[^>]*>/gi
  let h: RegExpExecArray | null
  while ((h = imgRegex.exec(content)) !== null) {
    pushFilename(h[1])
  }

  return filenames
}

// 基于文件名对比找出被删除的图片，并标准化为 /api/uploads/{filename}
const getDeletedImages = (originalContent: string, newContent: string): string[] => {
  const originalNames = new Set(extractImageFilenames(originalContent))
  const newNames = new Set(extractImageFilenames(newContent))
  const deleted: string[] = []
  originalNames.forEach((name) => {
    if (!newNames.has(name)) deleted.push(name)
  })
  return deleted.map((name) => `/api/uploads/${name}`)
}

// 删除已删除的图片
const deleteUnusedImages = async (deletedImageUrls: string[]): Promise<void> => {
  if (deletedImageUrls.length === 0) return
  try {
    await axios.post('/api/uploads/delete-unused', { imageUrls: deletedImageUrls })
    console.log(`成功删除 ${deletedImageUrls.length} 张未使用的图片`)
  } catch (error) {
    console.error('删除未使用的图片失败:', error)
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

    // 标准化图片URL为相对路径
    const normalizedImageUrl = normalizeImageUrl(imageUrl)

    // 构建图片Markdown语法，包含可选的宽高
    let imageMarkdown = `\n![](${normalizedImageUrl})`

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
        imageMarkdown = `\n<img src="${normalizedImageUrl}" alt="${customName || file.name}" style="${styleParts.join('; ')}" />\n`
      } else {
        imageMarkdown = `\n![](${normalizedImageUrl})\n`
      }
    } else {
      imageMarkdown = `\n![](${normalizedImageUrl})\n`
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

// Markdown 实时预览
const previewHtml = computed(() => {
  const content = form.value.content || ''
  return marked.parse(content)
})

// 编辑/预览融合视图控制
const viewMode = ref<'split' | 'edit' | 'preview'>('split')
const editorWidth = ref(50) // 分屏时左侧编辑区宽度百分比
const mdBodyRef = ref<HTMLDivElement | null>(null)
const editorPaneRef = ref<HTMLDivElement | null>(null)
const previewPaneRef = ref<HTMLDivElement | null>(null)
const previewContentRef = ref<HTMLDivElement | null>(null)

let resizing = false
const startResize = (e: MouseEvent) => {
  resizing = true
  const onMove = (evt: MouseEvent) => {
    if (!resizing || !mdBodyRef.value) return
    const rect = mdBodyRef.value.getBoundingClientRect()
    const percent = Math.min(80, Math.max(20, ((evt.clientX - rect.left) / rect.width) * 100))
    editorWidth.value = Math.round(percent)
  }
  const onUp = () => {
    resizing = false
    window.removeEventListener('mousemove', onMove)
    window.removeEventListener('mouseup', onUp)
  }
  window.addEventListener('mousemove', onMove)
  window.addEventListener('mouseup', onUp)
}

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

// 目录抽取与跳转
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
const capturePreviewHeadings = () => {
  const container = previewContentRef.value || previewPaneRef.value
  if (!container) return
  previewHeadingEls = Array.from(container.querySelectorAll('h1, h2, h3, h4, h5, h6'))
}

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

watch(previewHtml, async () => {
  await nextTick()
  capturePreviewHeadings()
})

watch(viewMode, async (v) => {
  if (v !== 'edit') {
    await nextTick()
    capturePreviewHeadings()
  }
})

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
    submitting.value = true

    // 先更新文章内容
    await axios.put(`/api/posts/${postId.value}`, {
      title: form.value.title,
      desc: form.value.desc,
      content: form.value.content,
      author: form.value.author,
      categoryId: form.value.categoryId,
      tagIds: form.value.tagIds,
    })

    // 更新成功后，清理已删除的图片
    const deletedImages = getDeletedImages(originalContent.value, form.value.content)
    if (deletedImages.length > 0) {
      await deleteUnusedImages(deletedImages)
      console.log(`清理了 ${deletedImages.length} 张已删除的图片`)
    }

    ElMessage.success('博客文章更新成功')
    returnToSource()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
    console.error('更新博客文章失败:', error)
  } finally {
    submitting.value = false
  }
}

// 取消操作
const cancel = () => {
  returnToSource()
}

// 返回上一页 - 已删除，使用面包屑导航替代

// 组件挂载时获取数据
onMounted(() => {
  Promise.all([fetchCategories(), fetchTags(), fetchBlogPost()])

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
  if (to.path === `/admin/posts/edit/${postId.value}`) {
    fetchTags()
    fetchCategories()
  }
})
</script>

<template>
  <div class="post-edit-container">
    <!-- 面包屑导航 -->
    <Breadcrumb />

    <div class="header">
      <div class="header-top">
        <h1>编辑博客文章</h1>
      </div>
      <p>修改博客文章内容</p>
    </div>

    <el-card class="form-card" v-loading="loading">
      <el-skeleton v-if="loading" :rows="10" animated />

      <el-form v-else :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input v-model="form.desc" type="textarea" :rows="3" placeholder="请输入文章摘要" />
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
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="split">分屏</el-radio-button>
                <el-radio-button label="edit">编辑</el-radio-button>
                <el-radio-button label="preview">预览</el-radio-button>
              </el-radio-group>
            </div>

            <div
              class="md-body"
              :class="`mode-${viewMode}`"
              :style="
                viewMode === 'split'
                  ? {
                      gridTemplateColumns:
                        tocWidth + 'px ' + editorWidth + '% ' + (100 - editorWidth) + '%',
                    }
                  : {}
              "
              ref="mdBodyRef"
            >
              <div class="md-toc" v-show="viewMode !== 'edit'">
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
                <div class="toc-resizer" @mousedown="startResize('toc')"></div>
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
                <div class="markdown-preview" ref="previewContentRef" v-html="previewHtml"></div>
              </div>
              <div
                v-if="viewMode === 'split'"
                class="md-resizer"
                :style="{ left: `calc(${tocWidth}px + ${editorWidth}% - 3px)` }"
                @mousedown="startResize('main')"
              ></div>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">更新</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图片上传对话框 -->
    <el-dialog
      v-model="imageUploadDialogVisible"
      title="上传图片"
      width="500px"
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
              <p><strong>大小:</strong> {{ formatFileSize(imageFile.size) }}</p>
              <p><strong>类型:</strong> {{ imageFile.type }}</p>
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
          <div v-else class="no-image">
            <el-icon class="no-image-icon"><Picture /></el-icon>
            <p>请选择图片文件</p>
          </div>
        </div>

        <!-- 表单区域 -->
        <el-form :model="form" label-width="80px" class="upload-form">
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
              <el-option label="vh" value="vh" />
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
  </div>
</template>

<style scoped>
.post-edit-container {
  width: 95vw;
  /* max-width: 1200px; */
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  position: relative;
}

.header-top h1 {
  margin: 0;
}

/* 返回按钮样式已删除 */

.header p {
  font-size: 1.2rem;
  color: #666;
}

.form-card {
  margin-bottom: 30px;
}

.md-editor {
  width: 100%;
}
.md-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}
.md-body {
  position: relative;
  display: grid;
  gap: 12px;
}
.md-body.mode-split {
  grid-template-columns: 220px 1fr 1fr; /* 初始：TOC + 编辑 + 预览，运行时用内联样式覆盖 */
}
.md-body.mode-edit {
  grid-template-columns: 1fr;
}
.md-body.mode-preview {
  grid-template-columns: 1fr;
}
.md-editor-pane,
.md-preview-pane {
  min-height: 420px;
}
.md-toc {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 10px;
  background: #fff;
  max-height: 600px;
  overflow: auto;
  position: relative;
}
.md-toc .toc-resizer {
  position: absolute;
  right: -3px;
  top: 0;
  width: 6px;
  bottom: 0;
  cursor: col-resize;
}
.toc-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.toc-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.toc-list li {
  line-height: 1.8;
}
.toc-list li.active > a {
  color: #1f2937;
  font-weight: 600;
}
.toc-list a {
  color: #409eff;
  text-decoration: none;
}
.toc-list a:hover {
  text-decoration: underline;
}
.md-editor-pane {
  overflow: auto;
}
.md-preview-pane {
  overflow: auto;
}
.md-resizer {
  position: absolute;
  width: 6px;
  top: 0;
  bottom: 0;
  cursor: col-resize;
  background: transparent;
}

.markdown-preview {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
  background: #fafafa;
  max-height: 600px;
  overflow: auto;
}

.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3),
.markdown-preview :deep(h4),
.markdown-preview :deep(h5),
.markdown-preview :deep(h6) {
  margin: 16px 0 8px;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 8px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-edit-container {
    padding: 10px;
  }

  .header h1 {
    font-size: 2rem;
  }

  .header p {
    font-size: 1rem;
  }

  .header-top {
    justify-content: center;
  }

  /* 返回按钮样式已删除 */
}

/* 图片上传对话框样式 */
.image-upload-dialog {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.image-preview-container {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  background: #fafafa;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  width: 100%;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  object-fit: contain;
}

.image-info {
  text-align: left;
  background: white;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  width: 100%;
}

.image-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.image-info strong {
  color: #333;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #999;
}

.no-image-icon {
  font-size: 48px;
  color: #d9d9d9;
}

.upload-form {
  margin-top: 10px;
}
</style>
