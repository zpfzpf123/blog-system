<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入文本 (每行一项):</label>
      <textarea v-model="input" placeholder="第一行
第二行
第一行
第三行" rows="8"></textarea>
    </div>
    <div class="options">
      <label><input type="checkbox" v-model="options.trim" /> 去除首尾空格</label>
      <label><input type="checkbox" v-model="options.ignoreCase" /> 忽略大小写</label>
      <label><input type="checkbox" v-model="options.ignoreEmpty" /> 忽略空行</label>
      <label><input type="checkbox" v-model="options.sort" /> 排序结果</label>
    </div>
    <div class="btn-group">
      <button @click="dedupe" class="btn primary">去重</button>
      <button @click="findDupes" class="btn">查找重复</button>
      <button @click="copy" class="btn">复制</button>
    </div>
    <div class="stats" v-if="stats.total">
      原始: {{ stats.total }} 行 | 去重后: {{ stats.unique }} 行 | 重复: {{ stats.dupes }} 行
    </div>
    <div class="output-group">
      <label>处理结果:</label>
      <textarea v-model="output" readonly rows="8"></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const input = ref('')
const output = ref('')
const options = reactive({
  trim: true,
  ignoreCase: false,
  ignoreEmpty: true,
  sort: false
})
const stats = reactive({ total: 0, unique: 0, dupes: 0 })

const getLines = () => {
  let lines = input.value.split('\n')
  if (options.trim) lines = lines.map(l => l.trim())
  if (options.ignoreEmpty) lines = lines.filter(l => l)
  return lines
}

const dedupe = () => {
  const lines = getLines()
  stats.total = lines.length
  
  const seen = new Set<string>()
  const unique: string[] = []
  
  for (const line of lines) {
    const key = options.ignoreCase ? line.toLowerCase() : line
    if (!seen.has(key)) {
      seen.add(key)
      unique.push(line)
    }
  }
  
  stats.unique = unique.length
  stats.dupes = stats.total - stats.unique
  
  output.value = (options.sort ? unique.sort() : unique).join('\n')
}

const findDupes = () => {
  const lines = getLines()
  stats.total = lines.length
  
  const count = new Map<string, number>()
  for (const line of lines) {
    const key = options.ignoreCase ? line.toLowerCase() : line
    count.set(key, (count.get(key) || 0) + 1)
  }
  
  const dupes = [...count.entries()]
    .filter(([_, c]) => c > 1)
    .map(([k, c]) => `${k} (${c}次)`)
  
  stats.unique = count.size
  stats.dupes = dupes.length
  
  output.value = dupes.length ? dupes.join('\n') : '没有重复项'
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
.options { display: flex; flex-wrap: wrap; gap: 1rem; padding: 0.75rem; background: #f8f9fa; border-radius: 8px; }
.options label { display: flex; align-items: center; gap: 0.5rem; font-weight: normal; cursor: pointer; }
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.stats { padding: 0.5rem 1rem; background: #e0f2fe; color: #0369a1; border-radius: 6px; font-size: 0.9rem; }
</style>
