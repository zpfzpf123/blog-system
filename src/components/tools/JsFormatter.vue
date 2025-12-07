<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入JavaScript:</label>
      <textarea v-model="input" placeholder="粘贴JavaScript代码..." rows="8"></textarea>
    </div>
    <div class="btn-group">
      <button @click="format" class="btn primary">格式化</button>
      <button @click="minify" class="btn">压缩</button>
      <button @click="copy" class="btn">复制</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
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
const error = ref('')

const format = () => {
  error.value = ''
  try {
    // 简单的JS格式化
    let js = input.value
    js = js.replace(/([{;])\s*/g, '$1\n')
    js = js.replace(/\s*}/g, '\n}')
    js = js.replace(/,\s*/g, ', ')
    
    // 添加缩进
    let indent = 0
    const lines = js.split('\n')
    const formatted = lines.map(line => {
      line = line.trim()
      if (line.endsWith('}') || line.startsWith('}')) {
        indent = Math.max(0, indent - 1)
      }
      const result = '  '.repeat(indent) + line
      if (line.endsWith('{')) {
        indent++
      }
      return result
    }).filter(line => line.trim())
    
    output.value = formatted.join('\n')
  } catch (e) {
    error.value = '格式化失败'
  }
}

const minify = () => {
  error.value = ''
  let js = input.value
  // 移除注释
  js = js.replace(/\/\/.*$/gm, '')
  js = js.replace(/\/\*[\s\S]*?\*\//g, '')
  // 压缩空白
  js = js.replace(/\s+/g, ' ')
  js = js.replace(/\s*([{}();,:])\s*/g, '$1')
  output.value = js.trim()
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
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
</style>
