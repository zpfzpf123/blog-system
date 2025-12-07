<template>
  <div class="tool-content">
    <div class="tabs">
      <button :class="{ active: mode === 'toCode' }" @click="mode = 'toCode'">cURL → 代码</button>
      <button :class="{ active: mode === 'toCurl' }" @click="mode = 'toCurl'">代码 → cURL</button>
    </div>
    
    <template v-if="mode === 'toCode'">
      <div class="input-group">
        <label>cURL命令:</label>
        <textarea v-model="curlInput" placeholder="粘贴cURL命令..." rows="4"></textarea>
      </div>
      <div class="lang-select">
        <label>目标语言:</label>
        <select v-model="targetLang">
          <option value="javascript">JavaScript (fetch)</option>
          <option value="axios">JavaScript (axios)</option>
          <option value="python">Python (requests)</option>
        </select>
      </div>
      <button @click="convertToCode" class="btn primary">转换</button>
    </template>
    
    <template v-else>
      <div class="form-builder">
        <div class="form-row">
          <label>Method:</label>
          <select v-model="request.method">
            <option>GET</option>
            <option>POST</option>
            <option>PUT</option>
            <option>DELETE</option>
            <option>PATCH</option>
          </select>
        </div>
        <div class="form-row">
          <label>URL:</label>
          <input v-model="request.url" placeholder="https://api.example.com/endpoint" />
        </div>
        <div class="form-row">
          <label>Headers:</label>
          <textarea v-model="request.headers" placeholder="Content-Type: application/json" rows="2"></textarea>
        </div>
        <div class="form-row">
          <label>Body:</label>
          <textarea v-model="request.body" placeholder='{"key": "value"}' rows="3"></textarea>
        </div>
      </div>
      <button @click="convertToCurl" class="btn primary">生成cURL</button>
    </template>
    
    <div v-if="output" class="output-group">
      <label>输出结果:</label>
      <textarea v-model="output" readonly rows="8"></textarea>
      <button @click="copy" class="btn">复制</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const mode = ref<'toCode' | 'toCurl'>('toCode')
const curlInput = ref('')
const targetLang = ref('javascript')
const output = ref('')
const request = reactive({
  method: 'GET',
  url: '',
  headers: '',
  body: ''
})

const convertToCode = () => {
  const curl = curlInput.value
  
  // 简单解析cURL
  const urlMatch = curl.match(/curl\s+['"]?([^'">\s]+)['"]?/) || curl.match(/-X\s+\w+\s+['"]?([^'">\s]+)['"]?/)
  const methodMatch = curl.match(/-X\s+(\w+)/)
  const headerMatches = [...curl.matchAll(/-H\s+['"]([^'"]+)['"]/g)]
  const dataMatch = curl.match(/-d\s+['"]([^'"]+)['"]/) || curl.match(/--data\s+['"]([^'"]+)['"]/)
  
  const url = urlMatch?.[1] || ''
  const method = methodMatch?.[1] || 'GET'
  const headers: Record<string, string> = {}
  headerMatches.forEach(m => {
    const [key, value] = m[1].split(': ')
    if (key && value) headers[key] = value
  })
  const data = dataMatch?.[1] || ''
  
  switch (targetLang.value) {
    case 'javascript':
      output.value = generateFetch(url, method, headers, data)
      break
    case 'axios':
      output.value = generateAxios(url, method, headers, data)
      break
    case 'python':
      output.value = generatePython(url, method, headers, data)
      break
  }
}

const generateFetch = (url: string, method: string, headers: Record<string, string>, data: string) => {
  const opts = [`  method: '${method}'`]
  if (Object.keys(headers).length) {
    opts.push(`  headers: ${JSON.stringify(headers, null, 4).replace(/\n/g, '\n  ')}`)
  }
  if (data) {
    opts.push(`  body: '${data}'`)
  }
  return `fetch('${url}', {\n${opts.join(',\n')}\n})\n  .then(res => res.json())\n  .then(data => console.log(data))`
}

const generateAxios = (url: string, method: string, headers: Record<string, string>, data: string) => {
  const config = [`  method: '${method.toLowerCase()}'`, `  url: '${url}'`]
  if (Object.keys(headers).length) {
    config.push(`  headers: ${JSON.stringify(headers, null, 4).replace(/\n/g, '\n  ')}`)
  }
  if (data) {
    config.push(`  data: ${data}`)
  }
  return `axios({\n${config.join(',\n')}\n})\n  .then(res => console.log(res.data))`
}

const generatePython = (url: string, method: string, headers: Record<string, string>, data: string) => {
  let code = `import requests\n\n`
  code += `response = requests.${method.toLowerCase()}(\n`
  code += `    '${url}'`
  if (Object.keys(headers).length) {
    code += `,\n    headers=${JSON.stringify(headers)}`
  }
  if (data) {
    code += `,\n    json=${data}`
  }
  code += `\n)\nprint(response.json())`
  return code
}

const convertToCurl = () => {
  let curl = `curl -X ${request.method} '${request.url}'`
  
  if (request.headers) {
    request.headers.split('\n').forEach(h => {
      if (h.trim()) curl += ` \\\n  -H '${h.trim()}'`
    })
  }
  
  if (request.body && ['POST', 'PUT', 'PATCH'].includes(request.method)) {
    curl += ` \\\n  -d '${request.body}'`
  }
  
  output.value = curl
}

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.tabs { display: flex; gap: 0.5rem; }
.tabs button { flex: 1; padding: 0.75rem; border: 1px solid #ddd; background: white; cursor: pointer; border-radius: 8px; }
.tabs button.active { background: #667eea; color: white; border-color: #667eea; }
.input-group, .output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; font-size: 0.9rem; }
textarea, input { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; }
textarea:focus, input:focus { outline: none; border-color: #667eea; }
.lang-select { display: flex; align-items: center; gap: 0.5rem; }
.lang-select select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
.form-builder { display: flex; flex-direction: column; gap: 0.75rem; }
.form-row { display: flex; flex-direction: column; gap: 0.25rem; }
.form-row select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
</style>
