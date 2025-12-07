<template>
  <div class="tool-content">
    <div class="settings">
      <div class="setting-item">
        <label>生成数量:</label>
        <input v-model.number="count" type="number" min="1" max="100" />
      </div>
      <div class="setting-item">
        <label>格式:</label>
        <select v-model="format">
          <option value="standard">标准 (带连字符)</option>
          <option value="nohyphen">无连字符</option>
          <option value="upper">大写</option>
          <option value="braces">带花括号</option>
        </select>
      </div>
    </div>
    <div class="btn-group">
      <button @click="generate" class="btn primary">生成 UUID</button>
      <button @click="copyAll" class="btn">复制全部</button>
      <button @click="clear" class="btn">清空</button>
    </div>
    <div class="uuid-list">
      <div v-for="(uuid, i) in uuids" :key="i" class="uuid-item">
        <span class="uuid-text">{{ uuid }}</span>
        <button @click="copyOne(uuid)" class="copy-btn">复制</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const count = ref(5)
const format = ref('standard')
const uuids = ref<string[]>([])

const generateUUID = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

const formatUUID = (uuid: string): string => {
  switch (format.value) {
    case 'nohyphen':
      return uuid.replace(/-/g, '')
    case 'upper':
      return uuid.toUpperCase()
    case 'braces':
      return `{${uuid}}`
    default:
      return uuid
  }
}

const generate = () => {
  uuids.value = Array.from({ length: count.value }, () => formatUUID(generateUUID()))
}

const copyOne = async (uuid: string) => {
  await navigator.clipboard.writeText(uuid)
}

const copyAll = async () => {
  await navigator.clipboard.writeText(uuids.value.join('\n'))
}

const clear = () => {
  uuids.value = []
}

// 初始生成
generate()
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.settings { display: flex; flex-wrap: wrap; gap: 1rem; }
.setting-item { display: flex; flex-direction: column; gap: 0.25rem; }
.setting-item label { font-size: 0.85rem; color: #666; }
.setting-item input, .setting-item select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; min-width: 150px; }
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.uuid-list { display: flex; flex-direction: column; gap: 0.5rem; max-height: 300px; overflow-y: auto; }
.uuid-item { display: flex; align-items: center; justify-content: space-between; padding: 0.75rem; background: #f8f9fa; border-radius: 6px; }
.uuid-text { font-family: monospace; font-size: 0.9rem; }
.copy-btn { padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 4px; background: white; cursor: pointer; }
.copy-btn:hover { border-color: #667eea; color: #667eea; }
</style>
