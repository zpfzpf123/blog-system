<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入数值:</label>
      <input v-model="input" placeholder="输入数字" @input="convert" />
    </div>
    <div class="base-select">
      <label>输入进制:</label>
      <div class="base-buttons">
        <button v-for="b in bases" :key="b.value" :class="{ active: inputBase === b.value }" @click="inputBase = b.value; convert()">{{ b.label }}</button>
      </div>
    </div>
    <div class="results">
      <div v-for="b in bases" :key="b.value" class="result-item">
        <span class="base-label">{{ b.label }}:</span>
        <span class="base-value">{{ results[b.value] || '-' }}</span>
        <button @click="copyValue(results[b.value])" class="copy-btn">复制</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const input = ref('')
const inputBase = ref(10)
const results = reactive<Record<number, string>>({})

const bases = [
  { label: '二进制', value: 2 },
  { label: '八进制', value: 8 },
  { label: '十进制', value: 10 },
  { label: '十六进制', value: 16 },
]

const convert = () => {
  if (!input.value) {
    bases.forEach(b => results[b.value] = '')
    return
  }
  
  try {
    const decimal = parseInt(input.value, inputBase.value)
    if (isNaN(decimal)) {
      bases.forEach(b => results[b.value] = '无效')
      return
    }
    
    results[2] = decimal.toString(2)
    results[8] = decimal.toString(8)
    results[10] = decimal.toString(10)
    results[16] = decimal.toString(16).toUpperCase()
  } catch {
    bases.forEach(b => results[b.value] = '错误')
  }
}

const copyValue = async (value: string) => {
  if (value) await navigator.clipboard.writeText(value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
input { padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; font-size: 1.1rem; }
input:focus { outline: none; border-color: #667eea; }
.base-select label { font-size: 0.9rem; color: #666; margin-bottom: 0.5rem; display: block; }
.base-buttons { display: flex; gap: 0.5rem; }
.base-buttons button { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; background: white; cursor: pointer; }
.base-buttons button.active { background: #667eea; color: white; border-color: #667eea; }
.results { display: flex; flex-direction: column; gap: 0.75rem; }
.result-item { display: flex; align-items: center; gap: 1rem; padding: 0.75rem; background: #f8f9fa; border-radius: 8px; }
.base-label { width: 80px; font-weight: 600; color: #667eea; }
.base-value { flex: 1; font-family: monospace; font-size: 1rem; word-break: break-all; }
.copy-btn { padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 4px; background: white; cursor: pointer; }
.copy-btn:hover { border-color: #667eea; color: #667eea; }
</style>
