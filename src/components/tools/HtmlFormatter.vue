<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入HTML:</label>
      <textarea v-model="input" placeholder="粘贴HTML代码..." rows="8"></textarea>
    </div>
    <div class="btn-group">
      <button @click="format" class="btn primary">格式化</button>
      <button @click="minify" class="btn">压缩</button>
      <button @click="escape" class="btn">转义</button>
      <button @click="unescape" class="btn">反转义</button>
      <button @click="copy" class="btn">复制</button>
    </div>
    <div class="output-group">
      <label>输出结果:</label>
      <textarea v-model="output" readonly rows="10"></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const input = ref('')
const output = ref('')

const format = () => {
  let html = input.value
  let formatted = ''
  let indent = 0
  const tab = '  '
  
  html = html.replace(/>\s+</g, '><')
  const tags = html.match(/<[^>]+>|[^<]+/g) || []
  
  tags.forEach(tag => {
    if (tag.match(/^<\/\w/)) {
      indent--
    }
    formatted += tab.repeat(Math.max(0, indent)) + tag.trim() + '\n'
    if (tag.match(/^<\w[^>]*[^\/]>$/) && !tag.match(/^<(br|hr|img|input|meta|link)/i)) {
      indent++
    }
  })
  
  output.value = formatted.trim()
}

const minify = () => {
  let html = input.value
  html = html.replace(/<!--[\s\S]*?-->/g, '')
  html = html.replace(/\s+/g, ' ')
  html = html.replace(/>\s+</g, '><')
  output.value = html.trim()
}

const escape = () => {
  output.value = input.value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const unescape = () => {
  output.value = input.value
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
}

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group, .output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.btn-group { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
</style>
