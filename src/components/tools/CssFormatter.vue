<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入CSS:</label>
      <textarea v-model="input" placeholder="粘贴CSS代码..." rows="8"></textarea>
    </div>
    <div class="btn-group">
      <button @click="format" class="btn primary">格式化</button>
      <button @click="minify" class="btn">压缩</button>
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
  let css = input.value
  // 简单的CSS格式化
  css = css.replace(/\s*{\s*/g, ' {\n  ')
  css = css.replace(/;\s*/g, ';\n  ')
  css = css.replace(/\s*}\s*/g, '\n}\n\n')
  css = css.replace(/  }/g, '}')
  css = css.replace(/\n\n+/g, '\n\n')
  output.value = css.trim()
}

const minify = () => {
  let css = input.value
  css = css.replace(/\/\*[\s\S]*?\*\//g, '')
  css = css.replace(/\s+/g, ' ')
  css = css.replace(/\s*([{}:;,])\s*/g, '$1')
  css = css.replace(/;}/g, '}')
  output.value = css.trim()
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
</style>
