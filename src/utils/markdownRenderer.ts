import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

// 配置marked选项
marked.setOptions({
  breaks: true,
  gfm: true,
})

// 使用更简单的方法，不自定义渲染器
// 直接使用marked的默认渲染，然后通过后处理添加ID

// 生成目录的函数
export function generateTOC(content: string): string {
  const lines = content.split('\n')
  const headings: Array<{ level: number; text: string; id: string }> = []

  lines.forEach((line) => {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      const id = text.toLowerCase().replace(/[^\w]+/g, '-')
      headings.push({ level, text, id })
    }
  })

  if (headings.length === 0) return ''

  let toc = '<ul class="toc-list">'
  headings.forEach((heading) => {
    const indent = (heading.level - 1) * 20
    toc += `<li class="toc-item toc-level-${heading.level}" style="padding-left: ${indent}px;">
      <a href="#${heading.id}" class="toc-link">${heading.text}</a>
    </li>`
  })
  toc += '</ul>'

  return toc
}

// 渲染markdown内容
export function renderMarkdown(content: string): string {
  // 先渲染markdown
  let html = marked.parse(content)
  
  // 后处理：为标题添加ID
  html = html.replace(/<h([1-6])>(.*?)<\/h\1>/g, (match, level, text) => {
    const escapedText = text.toLowerCase().replace(/[^\w]+/g, '-')
    return `<h${level} id="${escapedText}">${text}</h${level}>`
  })
  
  // 后处理：为代码块添加高亮和按钮
  html = html.replace(/<pre><code class="language-(\w+)">(.*?)<\/code><\/pre>/gs, (match, language, code) => {
    const validLanguage = hljs.getLanguage(language) ? language : 'plaintext'
    const highlighted = hljs.highlight(code, { language: validLanguage }).value
    const codeId = 'code-' + Math.random().toString(36).substr(2, 9)
    
    return `<pre class="hljs" data-language="${validLanguage}" id="${codeId}">
      <div class="code-header">
        <div class="code-header-actions">
          <button class="toggle-code-btn" onclick="toggleCode('${codeId}')">展开</button>
          <button class="copy-code-btn" onclick="copyCode('${codeId}')">复制</button>
        </div>
      </div>
      <code class="language-${validLanguage}" style="display:none;">${highlighted}</code>
    </pre>`
  })
  
  // 处理没有语言标识的代码块
  html = html.replace(/<pre><code>(.*?)<\/code><\/pre>/gs, (match, code) => {
    const highlighted = hljs.highlightAuto(code).value
    const codeId = 'code-' + Math.random().toString(36).substr(2, 9)
    
    return `<pre class="hljs" id="${codeId}">
      <div class="code-header">
        <div class="code-header-actions">
          <button class="toggle-code-btn" onclick="toggleCode('${codeId}')">展开</button>
          <button class="copy-code-btn" onclick="copyCode('${codeId}')">复制</button>
        </div>
      </div>
      <code style="display:none;">${highlighted}</code>
    </pre>`
  })
  
  return html
}

// 处理图片URL
export function processImageUrls(content: string): string {
  return content.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (match, alt, src) => {
    if (src.startsWith('http')) {
      return match
    }
    // 这里可以添加图片URL处理逻辑
    return `![${alt}](${src})`
  })
}
