<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入文本:</label>
      <textarea v-model="input" placeholder="输入要转换的文本..." rows="3" @input="convert"></textarea>
    </div>
    
    <div class="conversions">
      <div v-for="c in conversions" :key="c.name" class="conversion-item">
        <div class="conversion-header">
          <span class="conversion-name">{{ c.name }}</span>
          <button @click="copy(c.value)" class="copy-btn">复制</button>
        </div>
        <div class="conversion-value">{{ c.value }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'

const input = ref('hello world example')
const conversions = reactive([
  { name: '小写', value: '' },
  { name: '大写', value: '' },
  { name: '首字母大写', value: '' },
  { name: '每词首字母大写', value: '' },
  { name: '驼峰命名 (camelCase)', value: '' },
  { name: '帕斯卡命名 (PascalCase)', value: '' },
  { name: '下划线命名 (snake_case)', value: '' },
  { name: '短横线命名 (kebab-case)', value: '' },
  { name: '常量命名 (CONSTANT_CASE)', value: '' },
])

const convert = () => {
  const text = input.value
  const words = text.toLowerCase().split(/[\s_-]+/).filter(w => w)
  
  conversions[0].value = text.toLowerCase()
  conversions[1].value = text.toUpperCase()
  conversions[2].value = text.charAt(0).toUpperCase() + text.slice(1).toLowerCase()
  conversions[3].value = text.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ')
  conversions[4].value = words.map((w, i) => i === 0 ? w : w.charAt(0).toUpperCase() + w.slice(1)).join('')
  conversions[5].value = words.map(w => w.charAt(0).toUpperCase() + w.slice(1)).join('')
  conversions[6].value = words.join('_')
  conversions[7].value = words.join('-')
  conversions[8].value = words.join('_').toUpperCase()
}

const copy = async (value: string) => {
  await navigator.clipboard.writeText(value)
}

convert()
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.conversions { display: flex; flex-direction: column; gap: 0.75rem; }
.conversion-item { padding: 0.75rem; background: #f8f9fa; border-radius: 8px; }
.conversion-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
.conversion-name { font-size: 0.85rem; color: #666; }
.copy-btn { padding: 0.2rem 0.5rem; border: 1px solid #ddd; border-radius: 4px; background: white; cursor: pointer; font-size: 0.8rem; }
.copy-btn:hover { border-color: #667eea; color: #667eea; }
.conversion-value { font-family: monospace; font-size: 0.95rem; word-break: break-all; }
</style>
