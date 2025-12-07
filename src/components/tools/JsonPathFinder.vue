<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入JSON:</label>
      <textarea v-model="input" placeholder='{"user": {"name": "张三", "address": {"city": "北京"}}}' rows="6" @input="parse"></textarea>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="paths.length" class="paths-section">
      <div class="search-box">
        <input v-model="search" placeholder="搜索路径或值..." />
      </div>
      <div class="paths-list">
        <div v-for="p in filteredPaths" :key="p.path" class="path-item" @click="copyPath(p.path)">
          <code class="path">{{ p.path }}</code>
          <span class="type" :class="p.type">{{ p.type }}</span>
          <span class="value">{{ p.displayValue }}</span>
        </div>
      </div>
    </div>
    <div v-if="copiedPath" class="copied-msg">已复制: {{ copiedPath }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const input = ref('')
const error = ref('')
const search = ref('')
const copiedPath = ref('')
const paths = ref<Array<{ path: string; value: any; type: string; displayValue: string }>>([])

const parse = () => {
  error.value = ''
  paths.value = []
  if (!input.value.trim()) return
  
  try {
    const json = JSON.parse(input.value)
    extractPaths(json, '$')
  } catch (e) {
    error.value = 'JSON格式错误'
  }
}

const extractPaths = (obj: any, currentPath: string) => {
  const type = getType(obj)
  const displayValue = getDisplayValue(obj, type)
  
  paths.value.push({ path: currentPath, value: obj, type, displayValue })
  
  if (type === 'object' && obj !== null) {
    for (const [key, value] of Object.entries(obj)) {
      const newPath = /^[a-zA-Z_][a-zA-Z0-9_]*$/.test(key) 
        ? `${currentPath}.${key}` 
        : `${currentPath}["${key}"]`
      extractPaths(value, newPath)
    }
  } else if (type === 'array') {
    (obj as any[]).forEach((item, index) => {
      extractPaths(item, `${currentPath}[${index}]`)
    })
  }
}

const getType = (value: any): string => {
  if (value === null) return 'null'
  if (Array.isArray(value)) return 'array'
  return typeof value
}

const getDisplayValue = (value: any, type: string): string => {
  if (type === 'object') return `{${Object.keys(value).length} keys}`
  if (type === 'array') return `[${value.length} items]`
  if (type === 'string') return `"${value.length > 30 ? value.slice(0, 30) + '...' : value}"`
  return String(value)
}

const filteredPaths = computed(() => {
  if (!search.value) return paths.value
  const s = search.value.toLowerCase()
  return paths.value.filter(p => 
    p.path.toLowerCase().includes(s) || 
    p.displayValue.toLowerCase().includes(s)
  )
})

const copyPath = async (path: string) => {
  await navigator.clipboard.writeText(path)
  copiedPath.value = path
  setTimeout(() => copiedPath.value = '', 2000)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
.search-box input { width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; margin-bottom: 0.5rem; }
.paths-list { max-height: 300px; overflow-y: auto; }
.path-item { display: flex; align-items: center; gap: 0.75rem; padding: 0.5rem; border-radius: 4px; cursor: pointer; }
.path-item:hover { background: #f0f0f0; }
.path { flex: 1; font-size: 0.85rem; color: #667eea; }
.type { font-size: 0.75rem; padding: 0.15rem 0.4rem; border-radius: 3px; }
.type.string { background: #dcfce7; color: #166534; }
.type.number { background: #dbeafe; color: #1e40af; }
.type.boolean { background: #fef3c7; color: #92400e; }
.type.object { background: #f3e8ff; color: #7c3aed; }
.type.array { background: #ffe4e6; color: #be123c; }
.type.null { background: #f1f5f9; color: #64748b; }
.value { font-size: 0.8rem; color: #666; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.copied-msg { padding: 0.5rem; background: #dcfce7; color: #166534; border-radius: 6px; text-align: center; }
</style>
