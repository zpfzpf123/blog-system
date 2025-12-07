<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入JSON:</label>
      <textarea v-model="input" placeholder="粘贴JSON内容..." rows="8"></textarea>
    </div>
    <div class="btn-group">
      <button @click="format" class="btn primary">格式化</button>
      <button @click="compress" class="btn">压缩</button>
      <button @click="validate" class="btn">校验</button>
      <button @click="copy" class="btn">复制</button>
      <button @click="clear" class="btn danger">清空</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="success" class="success-msg">{{ success }}</div>
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
const success = ref('')

const clearMessages = () => {
  error.value = ''
  success.value = ''
}

const format = () => {
  clearMessages()
  try {
    const parsed = JSON.parse(input.value)
    output.value = JSON.stringify(parsed, null, 2)
    success.value = '格式化成功!'
  } catch (e) {
    error.value = 'JSON格式错误: ' + (e as Error).message
  }
}

const compress = () => {
  clearMessages()
  try {
    const parsed = JSON.parse(input.value)
    output.value = JSON.stringify(parsed)
    success.value = '压缩成功!'
  } catch (e) {
    error.value = 'JSON格式错误: ' + (e as Error).message
  }
}

const validate = () => {
  clearMessages()
  try {
    JSON.parse(input.value)
    success.value = '✓ JSON格式正确!'
  } catch (e) {
    error.value = '✗ JSON格式错误: ' + (e as Error).message
  }
}

const copy = async () => {
  clearMessages()
  try {
    await navigator.clipboard.writeText(output.value || input.value)
    success.value = '已复制到剪贴板!'
  } catch {
    error.value = '复制失败'
  }
}

const clear = () => {
  input.value = ''
  output.value = ''
  clearMessages()
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group, .output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: 'Consolas', monospace; font-size: 0.9rem; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.btn-group { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; transition: all 0.3s; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.btn.primary:hover { background: #5a6fd6; }
.btn.danger { color: #e74c3c; border-color: #e74c3c; }
.btn.danger:hover { background: #e74c3c; color: white; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
.success-msg { padding: 0.75rem; background: #efe; color: #080; border-radius: 6px; }
</style>
