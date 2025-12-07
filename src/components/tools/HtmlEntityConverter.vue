<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入内容:</label>
      <textarea v-model="input" placeholder="输入文本或HTML实体..." rows="4"></textarea>
    </div>
    <div class="btn-group">
      <button @click="encode" class="btn primary">转为实体 →</button>
      <button @click="decode" class="btn primary">← 解码实体</button>
      <button @click="copy" class="btn">复制</button>
    </div>
    <div class="output-group">
      <label>输出结果:</label>
      <textarea v-model="output" readonly rows="4"></textarea>
    </div>
    <div class="common-entities">
      <h4>常用HTML实体</h4>
      <div class="entity-grid">
        <div v-for="e in entities" :key="e.char" class="entity-item" @click="insertEntity(e)">
          <span class="char">{{ e.char }}</span>
          <code>{{ e.entity }}</code>
          <span class="name">{{ e.name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const input = ref('')
const output = ref('')

const entities = [
  { char: '<', entity: '&lt;', name: '小于' },
  { char: '>', entity: '&gt;', name: '大于' },
  { char: '&', entity: '&amp;', name: '和号' },
  { char: '"', entity: '&quot;', name: '双引号' },
  { char: "'", entity: '&#39;', name: '单引号' },
  { char: ' ', entity: '&nbsp;', name: '空格' },
  { char: '©', entity: '&copy;', name: '版权' },
  { char: '®', entity: '&reg;', name: '注册' },
  { char: '™', entity: '&trade;', name: '商标' },
  { char: '€', entity: '&euro;', name: '欧元' },
  { char: '¥', entity: '&yen;', name: '人民币' },
  { char: '→', entity: '&rarr;', name: '右箭头' },
  { char: '←', entity: '&larr;', name: '左箭头' },
  { char: '↑', entity: '&uarr;', name: '上箭头' },
  { char: '↓', entity: '&darr;', name: '下箭头' },
  { char: '×', entity: '&times;', name: '乘号' },
  { char: '÷', entity: '&divide;', name: '除号' },
  { char: '±', entity: '&plusmn;', name: '正负' },
]

const encode = () => {
  output.value = input.value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const decode = () => {
  const textarea = document.createElement('textarea')
  textarea.innerHTML = input.value
  output.value = textarea.value
}

const insertEntity = (e: { entity: string }) => {
  input.value += e.entity
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
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.common-entities h4 { margin: 0 0 0.75rem; font-size: 0.95rem; }
.entity-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.5rem; }
.entity-item { display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem; background: #f8f9fa; border-radius: 4px; cursor: pointer; font-size: 0.85rem; }
.entity-item:hover { background: #e8e9ea; }
.char { font-size: 1.1rem; min-width: 20px; }
.entity-item code { background: #e2e8f0; padding: 0.1rem 0.3rem; border-radius: 3px; font-size: 0.75rem; }
.name { color: #666; font-size: 0.75rem; }
</style>
