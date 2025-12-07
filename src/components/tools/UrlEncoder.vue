<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入内容:</label>
      <textarea v-model="input" placeholder="输入URL或已编码的字符串..." rows="4"></textarea>
    </div>
    <div class="btn-group">
      <button @click="encode" class="btn primary">URL编码 →</button>
      <button @click="decode" class="btn primary">← URL解码</button>
      <button @click="encodeComponent" class="btn">编码组件</button>
      <button @click="copy" class="btn">复制</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div class="output-group">
      <label>输出结果:</label>
      <textarea v-model="output" readonly rows="4"></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const input = ref('')
const output = ref('')
const error = ref('')

const encode = () => {
  error.value = ''
  try {
    output.value = encodeURI(input.value)
  } catch (e) {
    error.value = '编码失败'
  }
}

const decode = () => {
  error.value = ''
  try {
    output.value = decodeURI(input.value)
  } catch (e) {
    error.value = '解码失败'
  }
}

const encodeComponent = () => {
  error.value = ''
  try {
    output.value = encodeURIComponent(input.value)
  } catch (e) {
    error.value = '编码失败'
  }
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
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
</style>
