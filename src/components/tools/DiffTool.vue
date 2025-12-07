<template>
  <div class="tool-content">
    <div class="diff-inputs">
      <div class="input-pane">
        <label>原始文本:</label>
        <textarea v-model="text1" placeholder="输入原始文本..." rows="8"></textarea>
      </div>
      <div class="input-pane">
        <label>对比文本:</label>
        <textarea v-model="text2" placeholder="输入对比文本..." rows="8"></textarea>
      </div>
    </div>
    <div class="btn-group">
      <button @click="compare" class="btn primary">对比差异</button>
      <button @click="swap" class="btn">交换</button>
      <button @click="clear" class="btn">清空</button>
    </div>
    <div v-if="diffResult.length" class="diff-result">
      <div class="stats">
        <span class="added">+{{ stats.added }} 新增</span>
        <span class="removed">-{{ stats.removed }} 删除</span>
        <span class="unchanged">{{ stats.unchanged }} 未变</span>
      </div>
      <div class="diff-lines">
        <div v-for="(line, i) in diffResult" :key="i" :class="['diff-line', line.type]">
          <span class="line-num">{{ line.lineNum }}</span>
          <span class="line-prefix">{{ line.prefix }}</span>
          <span class="line-content">{{ line.content }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const text1 = ref('')
const text2 = ref('')
const diffResult = ref<Array<{ type: string; content: string; prefix: string; lineNum: string }>>([])
const stats = reactive({ added: 0, removed: 0, unchanged: 0 })

const compare = () => {
  const lines1 = text1.value.split('\n')
  const lines2 = text2.value.split('\n')
  
  diffResult.value = []
  stats.added = 0
  stats.removed = 0
  stats.unchanged = 0
  
  // 简单的逐行对比
  const maxLen = Math.max(lines1.length, lines2.length)
  
  for (let i = 0; i < maxLen; i++) {
    const l1 = lines1[i]
    const l2 = lines2[i]
    
    if (l1 === undefined) {
      diffResult.value.push({ type: 'added', content: l2, prefix: '+', lineNum: String(i + 1) })
      stats.added++
    } else if (l2 === undefined) {
      diffResult.value.push({ type: 'removed', content: l1, prefix: '-', lineNum: String(i + 1) })
      stats.removed++
    } else if (l1 === l2) {
      diffResult.value.push({ type: 'unchanged', content: l1, prefix: ' ', lineNum: String(i + 1) })
      stats.unchanged++
    } else {
      diffResult.value.push({ type: 'removed', content: l1, prefix: '-', lineNum: String(i + 1) })
      diffResult.value.push({ type: 'added', content: l2, prefix: '+', lineNum: '' })
      stats.removed++
      stats.added++
    }
  }
}

const swap = () => {
  const temp = text1.value
  text1.value = text2.value
  text2.value = temp
}

const clear = () => {
  text1.value = ''
  text2.value = ''
  diffResult.value = []
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.diff-inputs { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.input-pane { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.diff-result { background: #f8f9fa; border-radius: 8px; overflow: hidden; }
.stats { padding: 0.75rem 1rem; border-bottom: 1px solid #ddd; display: flex; gap: 1rem; }
.stats .added { color: #27ae60; }
.stats .removed { color: #e74c3c; }
.stats .unchanged { color: #666; }
.diff-lines { max-height: 300px; overflow-y: auto; font-family: monospace; font-size: 0.9rem; }
.diff-line { display: flex; padding: 0.25rem 0.5rem; }
.diff-line.added { background: #e6ffed; }
.diff-line.removed { background: #ffeef0; }
.line-num { width: 40px; color: #999; text-align: right; padding-right: 0.5rem; }
.line-prefix { width: 20px; font-weight: bold; }
.diff-line.added .line-prefix { color: #27ae60; }
.diff-line.removed .line-prefix { color: #e74c3c; }
.line-content { flex: 1; white-space: pre-wrap; word-break: break-all; }
</style>
