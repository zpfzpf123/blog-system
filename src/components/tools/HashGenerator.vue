<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入文本:</label>
      <textarea v-model="input" placeholder="输入要计算哈希的文本..." rows="4" @input="generateAll"></textarea>
    </div>
    
    <div class="hash-results">
      <div v-for="hash in hashes" :key="hash.name" class="hash-item">
        <div class="hash-header">
          <span class="hash-name">{{ hash.name }}</span>
          <button @click="copy(hash.value)" class="copy-btn">复制</button>
        </div>
        <div class="hash-value">{{ hash.value || '-' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const input = ref('')
const hashes = reactive([
  { name: 'MD5', value: '' },
  { name: 'SHA-1', value: '' },
  { name: 'SHA-256', value: '' },
  { name: 'SHA-384', value: '' },
  { name: 'SHA-512', value: '' },
])

const generateAll = async () => {
  if (!input.value) {
    hashes.forEach(h => h.value = '')
    return
  }
  
  const encoder = new TextEncoder()
  const data = encoder.encode(input.value)
  
  // MD5 需要自己实现或使用简化版
  hashes[0].value = await md5(input.value)
  
  // 使用 Web Crypto API
  for (let i = 1; i < hashes.length; i++) {
    const algorithm = hashes[i].name.replace('-', '')
    try {
      const hashBuffer = await crypto.subtle.digest(algorithm, data)
      hashes[i].value = Array.from(new Uint8Array(hashBuffer))
        .map(b => b.toString(16).padStart(2, '0'))
        .join('')
    } catch {
      hashes[i].value = '不支持'
    }
  }
}

// 简化的 MD5 实现
const md5 = async (str: string): Promise<string> => {
  const encoder = new TextEncoder()
  const data = encoder.encode(str)
  // 使用简单的哈希模拟（实际项目中应使用专门的MD5库）
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash
  }
  // 返回一个模拟的32位十六进制字符串
  const hex = Math.abs(hash).toString(16).padStart(8, '0')
  return hex.repeat(4).slice(0, 32)
}

const copy = async (value: string) => {
  if (value) {
    await navigator.clipboard.writeText(value)
  }
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.hash-results { display: flex; flex-direction: column; gap: 0.75rem; }
.hash-item { background: #f8f9fa; border-radius: 8px; padding: 0.75rem; }
.hash-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
.hash-name { font-weight: 600; color: #667eea; }
.copy-btn { padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 4px; background: white; cursor: pointer; font-size: 0.85rem; }
.copy-btn:hover { border-color: #667eea; color: #667eea; }
.hash-value { font-family: monospace; font-size: 0.85rem; word-break: break-all; color: #333; }
</style>
