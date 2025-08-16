/*
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-08 10:48:20
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-08-08 10:50:32
 * @FilePath: \blog\src\utils\imageUtils.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
/**
 * 图片工具函数
 */

/**
 * 将相对路径转换为完整的URL
 * @param path 相对路径，如 /api/uploads/filename.png
 * @returns 完整的URL，如 http://localhost:3000/api/uploads/filename.png
 */
export function getFullImageUrl(path: string): string {
  if (!path) return ''

  // 如果已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  // 如果是相对路径，添加域名
  if (path.startsWith('/')) {
    return `http://localhost:3000${path}`
  }

  // 如果只是文件名，添加完整路径
  return `http://localhost:3000/api/uploads/${path}`
}

/**
 * 标准化图片URL，确保使用相对路径
 * @param path 图片路径
 * @returns 标准化的相对路径
 */
export function normalizeImageUrl(path: string): string {
  if (!path) return ''

  // 如果已经是相对路径，直接返回
  if (path.startsWith('/api/uploads/')) {
    return path
  }

  // 如果是完整URL，提取相对路径部分
  if (path.startsWith('http://') || path.startsWith('https://')) {
    const url = new URL(path)
    if (url.pathname.startsWith('/api/uploads/')) {
      return url.pathname
    }
  }

  // 如果只是文件名，添加相对路径前缀
  if (!path.startsWith('/') && !path.startsWith('http')) {
    return `/api/uploads/${path}`
  }

  return path
}

/**
 * 处理Markdown内容中的图片路径
 * @param content Markdown内容
 * @returns 处理后的Markdown内容
 */
export function processImageUrlsInMarkdown(content: string): string {
  if (!content) return content

  // 先处理HTML img标签，保持其样式属性
  let processedContent = content.replace(/<img\s+([^>]*?)src="([^"]+)"([^>]*?)>/gi, (match, beforeSrc, url, afterSrc) => {
    const normalizedUrl = normalizeImageUrl(url)
    return `<img ${beforeSrc}src="${normalizedUrl}"${afterSrc}>`
  })

  // 再处理Markdown图片语法 ![alt](url)
  processedContent = processedContent.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (match, alt, url) => {
    const normalizedUrl = normalizeImageUrl(url)
    return `![${alt}](${normalizedUrl})`
  })

  return processedContent
}

/**
 * 验证图片URL是否有效
 * @param url 图片URL
 * @returns Promise<boolean>
 */
export async function validateImageUrl(url: string): Promise<boolean> {
  try {
    const response = await fetch(url, { method: 'HEAD' })
    return response.ok
  } catch {
    return false
  }
}

import { marked } from 'marked'

/**
 * 将Markdown内容转换为HTML，用于富文本复制（包含<img>）
 * @param markdown Markdown内容
 * @returns HTML字符串
 */
export function markdownToHtml(markdown: string): string {
  if (!markdown) return ''
  // 使用 marked 做标准 Markdown → HTML 转换，自动将图片转为 <img>
  return marked.parse(markdown) as string
}

/**
 * 创建富文本复制内容，包含HTML和纯文本两种格式
 * @param markdown Markdown内容
 * @returns 包含HTML和纯文本的复制数据
 */
export function createRichCopyData(markdown: string): { html: string; text: string } {
  const processedMarkdown = processImageUrlsInMarkdown(markdown)
  const html = markdownToHtml(processedMarkdown)
  const text = markdown // 保持原始Markdown格式作为纯文本

  return { html, text }
}

/**
 * 执行富文本复制到剪贴板
 * @param markdown Markdown内容
 * @returns Promise<boolean> 复制是否成功
 */
export async function copyRichContent(markdown: string): Promise<boolean> {
  try {
    const { html, text } = createRichCopyData(markdown)

    // 创建包含多种格式的剪贴板数据
    const clipboardData = [
      new ClipboardItem({
        'text/html': new Blob([html], { type: 'text/html' }),
        'text/plain': new Blob([text], { type: 'text/plain' }),
      }),
    ]

    await navigator.clipboard.write(clipboardData)
    return true
  } catch (error) {
    console.error('富文本复制失败:', error)

    // 降级到纯文本复制
    try {
      await navigator.clipboard.writeText(markdown)
      return true
    } catch (fallbackError) {
      console.error('纯文本复制也失败:', fallbackError)
      return false
    }
  }
}

/**
 * 将图片URL转换为base64格式
 * @param imageUrl 图片URL
 * @returns Promise<string> base64字符串
 */
export async function imageUrlToBase64(imageUrl: string): Promise<string> {
  try {
    // 检查URL是否有效
    if (!imageUrl || imageUrl.startsWith('data:')) {
      return imageUrl // 如果已经是base64或无效URL，直接返回
    }

    // 处理相对路径
    let fullUrl = imageUrl
    if (imageUrl.startsWith('/')) {
      // 对于相对路径，使用当前域名
      fullUrl = window.location.origin + imageUrl
    } else if (!imageUrl.startsWith('http')) {
      // 如果是相对路径，尝试构建完整URL
      fullUrl = window.location.origin + '/' + imageUrl.replace(/^\/+/, '')
    }

    // 尝试不同的请求模式
    let response
    try {
      // 首先尝试同源请求
      response = await fetch(fullUrl)
    } catch (error) {
      // 如果同源请求失败，尝试跨域请求
      try {
        response = await fetch(fullUrl, {
          mode: 'cors',
          credentials: 'omit',
        })
      } catch (corsError) {
        // 如果跨域也失败，尝试不设置mode
        response = await fetch(fullUrl, {
          credentials: 'omit',
        })
      }
    }

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    let blob = await response.blob()

    // 若返回的类型不是 image/*，尝试按扩展名猜测并纠正 MIME，避免 data:application/octet-stream
    const contentType = (response.headers.get('Content-Type') || '').toLowerCase()
    const ensureImageMime = (url: string, fallback: string): string => {
      try {
        const lower = url.toLowerCase()
        if (lower.endsWith('.png')) return 'image/png'
        if (lower.endsWith('.jpg') || lower.endsWith('.jpeg')) return 'image/jpeg'
        if (lower.endsWith('.gif')) return 'image/gif'
        if (lower.endsWith('.webp')) return 'image/webp'
        if (lower.endsWith('.svg')) return 'image/svg+xml'
      } catch {}
      return fallback
    }

    let mime = contentType
    if (!mime.startsWith('image/')) {
      mime = ensureImageMime(fullUrl, 'image/png')
      blob = new Blob([blob], { type: mime })
    }

    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => {
        if (typeof reader.result === 'string') {
          resolve(reader.result)
        } else {
          reject(new Error('Failed to convert image to base64'))
        }
      }
      reader.onerror = reject
      reader.readAsDataURL(blob)
    })
  } catch (error) {
    console.error('转换图片为base64失败:', error)
    // 如果转换失败，返回原始URL
    return imageUrl
  }
}

/**
 * 将Markdown内容中的图片转换为base64格式
 * @param markdown Markdown内容
 * @returns Promise<string> 转换后的Markdown内容
 */
export async function convertImagesToBase64(markdown: string): Promise<string> {
  if (!markdown) return markdown

  const imageRegex = /!\[([^\]]*)\]\(([^)]+)\)/g
  let result = markdown
  let match

  while ((match = imageRegex.exec(markdown)) !== null) {
    const [fullMatch, alt, url] = match

    try {
      // 直接使用原始URL，让imageUrlToBase64函数内部处理URL转换
      const base64 = await imageUrlToBase64(url)
      // 只有当成功转换为base64时才替换
      if (base64 && base64.startsWith('data:')) {
        result = result.replace(fullMatch, `![${alt}](${base64})`)
      } else {
        // 如果转换失败，保持原始URL
        console.warn(`图片转换失败，保持原始URL: ${url}`)
        result = result.replace(fullMatch, `![${alt}](${url})`)
      }
    } catch (error) {
      console.error(`转换图片失败: ${url}`, error)
      // 如果转换失败，保持原始URL
      result = result.replace(fullMatch, `![${alt}](${url})`)
    }
  }

  return result
}

/**
 * 将HTML字符串中的<img>图片转换为base64
 * 在复制富文本到剪贴板时使用，保证粘贴后图片可见
 */
export async function convertImagesInHtmlToBase64(html: string): Promise<string> {
  if (!html) return html

  // 使用DOM解析，保留现有属性（如style/width/height）
  const container = document.createElement('div')
  container.innerHTML = html

  const images = Array.from(container.querySelectorAll('img')) as HTMLImageElement[]
  for (const img of images) {
    const src = img.getAttribute('src') || ''
    if (!src) continue
    try {
      const normalized = normalizeImageUrl(src)
      const base64 = await imageUrlToBase64(normalized)
      if (base64 && base64.startsWith('data:')) {
        img.setAttribute('src', base64)
      }
    } catch (e) {
      // 出错时保持原始src
      console.warn('convert img to base64 failed:', src, e)
    }
  }

  return container.innerHTML
}

/**
 * 创建增强的富文本复制内容，包含base64图片
 * @param markdown Markdown内容
 * @returns 包含HTML和纯文本的复制数据
 */
export async function createEnhancedRichCopyData(
  markdown: string,
): Promise<{ html: string; text: string }> {
  // 先处理图片路径
  const processedMarkdown = processImageUrlsInMarkdown(markdown)

  // 渲染为HTML
  const renderedHtml = markdownToHtml(processedMarkdown)

  // 将HTML中的<img>图片转换为base64，保证粘贴后可见
  const htmlWithBase64 = await convertImagesInHtmlToBase64(renderedHtml)

  // 纯文本保留处理后的Markdown（不必包含base64）
  const text = processedMarkdown

  return { html: htmlWithBase64, text }
}

/**
 * 执行增强的富文本复制到剪贴板（包含base64图片）
 * @param markdown Markdown内容
 * @returns Promise<boolean> 复制是否成功
 */
export async function copyEnhancedRichContent(markdown: string): Promise<boolean> {
  try {
    const { html, text } = await createEnhancedRichCopyData(markdown)

    // 创建包含多种格式的剪贴板数据
    const clipboardData = [
      new ClipboardItem({
        'text/html': new Blob([html], { type: 'text/html' }),
        'text/plain': new Blob([text], { type: 'text/plain' }),
      }),
    ]

    await navigator.clipboard.write(clipboardData)
    return true
  } catch (error) {
    console.error('复制富文本内容失败:', error)

    // 如果富文本复制失败，尝试简单的文本复制
    try {
      await navigator.clipboard.writeText(markdown)
      console.warn('富文本复制失败，已降级为纯文本复制')
      return true
    } catch (fallbackError) {
      console.error('纯文本复制也失败:', fallbackError)
      return false
    }
  }
}
