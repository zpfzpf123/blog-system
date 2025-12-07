<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入SQL:</label>
      <textarea v-model="input" placeholder="粘贴SQL语句..." rows="6"></textarea>
    </div>
    <div class="btn-group">
      <button @click="format" class="btn primary">格式化</button>
      <button @click="minify" class="btn">压缩</button>
      <button @click="uppercase" class="btn">关键字大写</button>
      <button @click="copy" class="btn">复制</button>
    </div>
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

const keywords = ['SELECT', 'FROM', 'WHERE', 'AND', 'OR', 'JOIN', 'LEFT', 'RIGHT', 'INNER', 'OUTER', 
  'ON', 'GROUP BY', 'ORDER BY', 'HAVING', 'LIMIT', 'OFFSET', 'INSERT', 'INTO', 'VALUES', 
  'UPDATE', 'SET', 'DELETE', 'CREATE', 'TABLE', 'ALTER', 'DROP', 'INDEX', 'AS', 'DISTINCT',
  'COUNT', 'SUM', 'AVG', 'MAX', 'MIN', 'BETWEEN', 'IN', 'NOT', 'NULL', 'IS', 'LIKE', 'UNION']

const format = () => {
  let sql = input.value.trim()
  
  // 关键字换行
  const breakBefore = ['SELECT', 'FROM', 'WHERE', 'AND', 'OR', 'JOIN', 'LEFT JOIN', 'RIGHT JOIN', 
    'INNER JOIN', 'GROUP BY', 'ORDER BY', 'HAVING', 'LIMIT', 'UNION']
  
  breakBefore.forEach(kw => {
    const regex = new RegExp(`\\b${kw}\\b`, 'gi')
    sql = sql.replace(regex, `\n${kw.toUpperCase()}`)
  })
  
  // 缩进
  sql = sql.replace(/\n(AND|OR)/gi, '\n  $1')
  
  output.value = sql.trim()
}

const minify = () => {
  let sql = input.value
  sql = sql.replace(/--.*$/gm, '')
  sql = sql.replace(/\s+/g, ' ')
  output.value = sql.trim()
}

const uppercase = () => {
  let sql = input.value
  keywords.forEach(kw => {
    const regex = new RegExp(`\\b${kw}\\b`, 'gi')
    sql = sql.replace(regex, kw.toUpperCase())
  })
  output.value = sql
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
</style>
