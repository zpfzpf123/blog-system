<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入JSON:</label>
      <textarea v-model="input" placeholder='{"name": "张三", "age": 25, "skills": ["vue", "react"]}' rows="8" @input="convert"></textarea>
    </div>
    <div class="options">
      <label><input type="checkbox" v-model="options.exportType" @change="convert" /> 导出类型</label>
      <label><input type="checkbox" v-model="options.optional" @change="convert" /> 可选属性</label>
      <label><input type="checkbox" v-model="options.readonly" @change="convert" /> 只读属性</label>
      <div class="name-input">
        <span>接口名:</span>
        <input v-model="interfaceName" @input="convert" placeholder="IData" />
      </div>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div class="output-group">
      <label>TypeScript 接口:</label>
      <textarea v-model="output" readonly rows="10"></textarea>
      <button @click="copy" class="btn">复制</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const input = ref('')
const output = ref('')
const error = ref('')
const interfaceName = ref('IData')
const options = reactive({
  exportType: true,
  optional: false,
  readonly: false
})

const convert = () => {
  error.value = ''
  if (!input.value.trim()) {
    output.value = ''
    return
  }
  
  try {
    const json = JSON.parse(input.value)
    output.value = jsonToTs(json, interfaceName.value)
  } catch (e) {
    error.value = 'JSON格式错误'
  }
}

const jsonToTs = (obj: any, name: string, indent = 0): string => {
  const spaces = '  '.repeat(indent)
  const prefix = options.exportType ? 'export ' : ''
  const readonly = options.readonly ? 'readonly ' : ''
  const optional = options.optional ? '?' : ''
  
  if (Array.isArray(obj)) {
    if (obj.length === 0) return 'any[]'
    const itemType = typeof obj[0] === 'object' ? jsonToTs(obj[0], name + 'Item', indent) : getTsType(obj[0])
    return `${itemType}[]`
  }
  
  if (typeof obj !== 'object' || obj === null) {
    return getTsType(obj)
  }
  
  let result = `${prefix}interface ${name} {\n`
  
  for (const [key, value] of Object.entries(obj)) {
    const type = getPropertyType(value, key, indent + 1)
    result += `${spaces}  ${readonly}${key}${optional}: ${type};\n`
  }
  
  result += `${spaces}}`
  return result
}

const getPropertyType = (value: any, key: string, indent: number): string => {
  if (value === null) return 'null'
  if (Array.isArray(value)) {
    if (value.length === 0) return 'any[]'
    if (typeof value[0] === 'object' && value[0] !== null) {
      return `${capitalize(key)}Item[]`
    }
    return `${getTsType(value[0])}[]`
  }
  if (typeof value === 'object') {
    return capitalize(key)
  }
  return getTsType(value)
}

const getTsType = (value: any): string => {
  if (value === null) return 'null'
  if (typeof value === 'string') return 'string'
  if (typeof value === 'number') return 'number'
  if (typeof value === 'boolean') return 'boolean'
  return 'any'
}

const capitalize = (str: string) => str.charAt(0).toUpperCase() + str.slice(1)

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group, .output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; font-size: 0.9rem; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.options { display: flex; flex-wrap: wrap; gap: 1rem; align-items: center; padding: 0.75rem; background: #f8f9fa; border-radius: 8px; }
.options label { display: flex; align-items: center; gap: 0.5rem; font-weight: normal; cursor: pointer; }
.name-input { display: flex; align-items: center; gap: 0.5rem; }
.name-input input { padding: 0.4rem; border: 1px solid #ddd; border-radius: 4px; width: 100px; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; align-self: flex-start; }
.btn:hover { border-color: #667eea; color: #667eea; }
</style>
