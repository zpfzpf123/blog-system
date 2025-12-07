<template>
  <div class="tool-content">
    <div class="editor-container">
      <div class="editor-pane">
        <div class="pane-header">Markdown</div>
        <textarea v-model="markdown" placeholder="输入 Markdown 内容..." @input="render"></textarea>
      </div>
      <div class="preview-pane">
        <div class="pane-header">预览</div>
        <div class="preview-content" v-html="html"></div>
      </div>
    </div>
    <div class="btn-group">
      <button @click="copyHtml" class="btn">复制 HTML</button>
      <button @click="insertTemplate" class="btn">插入模板</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const markdown = ref('')
const html = ref('')

const template = `# 标题

## 二级标题

这是一段**粗体**和*斜体*文本。

### 列表

- 项目 1
- 项目 2
- 项目 3

### 代码

\`\`\`javascript
const hello = 'world';
console.log(hello);
\`\`\`

### 链接

[访问链接](https://example.com)

### 引用

> 这是一段引用文本

### 表格

| 列1 | 列2 | 列3 |
|-----|-----|-----|
| A   | B   | C   |
| D   | E   | F   |
`

// 简单的 Markdown 解析器
const render = () => {
  let text = markdown.value
  
  // 转义 HTML
  text = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  
  // 代码块
  text = text.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>')
  
  // 行内代码
  text = text.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // 标题
  text = text.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  text = text.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  text = text.replace(/^# (.+)$/gm, '<h1>$1</h1>')
  
  // 粗体和斜体
  text = text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/\*(.+?)\*/g, '<em>$1</em>')
  
  // 链接
  text = text.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 图片
  text = text.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" />')
  
  // 引用
  text = text.replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>')
  
  // 无序列表
  text = text.replace(/^- (.+)$/gm, '<li>$1</li>')
  text = text.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
  
  // 表格 (简化处理)
  text = text.replace(/\|(.+)\|/g, (match) => {
    const cells = match.split('|').filter(c => c.trim())
    if (cells.every(c => /^-+$/.test(c.trim()))) return ''
    const cellHtml = cells.map(c => `<td>${c.trim()}</td>`).join('')
    return `<tr>${cellHtml}</tr>`
  })
  text = text.replace(/(<tr>.*<\/tr>\n?)+/g, '<table>$&</table>')
  
  // 段落
  text = text.replace(/\n\n/g, '</p><p>')
  text = '<p>' + text + '</p>'
  text = text.replace(/<p><\/p>/g, '')
  text = text.replace(/<p>(<h[1-6]>)/g, '$1')
  text = text.replace(/(<\/h[1-6]>)<\/p>/g, '$1')
  text = text.replace(/<p>(<pre>)/g, '$1')
  text = text.replace(/(<\/pre>)<\/p>/g, '$1')
  text = text.replace(/<p>(<ul>)/g, '$1')
  text = text.replace(/(<\/ul>)<\/p>/g, '$1')
  text = text.replace(/<p>(<table>)/g, '$1')
  text = text.replace(/(<\/table>)<\/p>/g, '$1')
  text = text.replace(/<p>(<blockquote>)/g, '$1')
  text = text.replace(/(<\/blockquote>)<\/p>/g, '$1')
  
  html.value = text
}

const copyHtml = async () => {
  await navigator.clipboard.writeText(html.value)
}

const insertTemplate = () => {
  markdown.value = template
  render()
}

onMounted(() => {
  markdown.value = template
  render()
})
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; height: 500px; }
.editor-container { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; flex: 1; min-height: 0; }
.editor-pane, .preview-pane { display: flex; flex-direction: column; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }
.pane-header { padding: 0.5rem 1rem; background: #f8f9fa; font-weight: 600; border-bottom: 1px solid #ddd; }
.editor-pane textarea { flex: 1; border: none; padding: 1rem; resize: none; font-family: monospace; }
.editor-pane textarea:focus { outline: none; }
.preview-content { flex: 1; padding: 1rem; overflow-y: auto; }
.preview-content :deep(h1) { font-size: 1.5rem; margin: 0.5rem 0; }
.preview-content :deep(h2) { font-size: 1.3rem; margin: 0.5rem 0; }
.preview-content :deep(h3) { font-size: 1.1rem; margin: 0.5rem 0; }
.preview-content :deep(pre) { background: #f8f9fa; padding: 1rem; border-radius: 6px; overflow-x: auto; }
.preview-content :deep(code) { background: #f0f0f0; padding: 0.1rem 0.3rem; border-radius: 3px; font-family: monospace; }
.preview-content :deep(pre code) { background: none; padding: 0; }
.preview-content :deep(blockquote) { border-left: 3px solid #667eea; padding-left: 1rem; color: #666; margin: 0.5rem 0; }
.preview-content :deep(table) { border-collapse: collapse; width: 100%; }
.preview-content :deep(td) { border: 1px solid #ddd; padding: 0.5rem; }
.preview-content :deep(ul) { padding-left: 1.5rem; }
.preview-content :deep(a) { color: #667eea; }
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
</style>
