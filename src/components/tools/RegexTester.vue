<template>
  <div class="tool-content">
    <div class="input-group">
      <label>正则表达式:</label>
      <div class="regex-input">
        <span class="delimiter">/</span>
        <input v-model="pattern" placeholder="输入正则表达式" />
        <span class="delimiter">/</span>
        <input v-model="flags" class="flags" placeholder="gi" maxlength="6" />
      </div>
    </div>
    
    <div class="quick-patterns">
      <span class="label">常用:</span>
      <button v-for="p in quickPatterns" :key="p.name" @click="applyPattern(p)" class="quick-btn">
        {{ p.name }}
      </button>
    </div>
    
    <div class="input-group">
      <label>测试文本:</label>
      <textarea v-model="testText" placeholder="输入要测试的文本..." rows="6"></textarea>
    </div>
    
    <div v-if="error" class="error-msg">{{ error }}</div>
    
    <div class="results">
      <div class="result-header">
        <span>匹配结果: {{ matches.length }} 个</span>
      </div>
      <div class="matches-list" v-if="matches.length">
        <div v-for="(match, i) in matches" :key="i" class="match-item">
          <span class="match-index">#{{ i + 1 }}</span>
          <span class="match-text">{{ match[0] }}</span>
          <span class="match-pos">位置: {{ match.index }}</span>
        </div>
      </div>
      <div v-else class="no-match">无匹配结果</div>
    </div>
    
    <div class="highlighted-text" v-html="highlightedText"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const pattern = ref('')
const flags = ref('g')
const testText = ref('')
const error = ref('')

const quickPatterns = [
  { name: '邮箱', pattern: '[\\w.-]+@[\\w.-]+\\.\\w+', flags: 'gi' },
  { name: '手机号', pattern: '1[3-9]\\d{9}', flags: 'g' },
  { name: 'URL', pattern: 'https?://[\\w.-]+(?:/[\\w./-]*)?', flags: 'gi' },
  { name: 'IP地址', pattern: '\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}', flags: 'g' },
  { name: '身份证', pattern: '\\d{17}[\\dXx]', flags: 'g' },
  { name: '中文', pattern: '[\\u4e00-\\u9fa5]+', flags: 'g' },
  { name: '数字', pattern: '\\d+', flags: 'g' },
]

const applyPattern = (p: { pattern: string; flags: string }) => {
  pattern.value = p.pattern
  flags.value = p.flags
}

const matches = computed(() => {
  if (!pattern.value || !testText.value) return []
  error.value = ''
  try {
    const regex = new RegExp(pattern.value, flags.value)
    const results: RegExpExecArray[] = []
    let match
    if (flags.value.includes('g')) {
      while ((match = regex.exec(testText.value)) !== null) {
        results.push(match)
        if (results.length > 100) break
      }
    } else {
      match = regex.exec(testText.value)
      if (match) results.push(match)
    }
    return results
  } catch (e) {
    error.value = '正则表达式错误: ' + (e as Error).message
    return []
  }
})

const highlightedText = computed(() => {
  if (!pattern.value || !testText.value || matches.value.length === 0) {
    return escapeHtml(testText.value)
  }
  try {
    const regex = new RegExp(pattern.value, flags.value)
    return escapeHtml(testText.value).replace(
      new RegExp(pattern.value, flags.value),
      '<mark>$&</mark>'
    )
  } catch {
    return escapeHtml(testText.value)
  }
})

const escapeHtml = (text: string) => {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
.regex-input { display: flex; align-items: center; background: #f8f9fa; border: 1px solid #ddd; border-radius: 8px; padding: 0.5rem; }
.regex-input input { flex: 1; border: none; background: transparent; padding: 0.5rem; font-family: monospace; font-size: 1rem; }
.regex-input input:focus { outline: none; }
.regex-input .flags { width: 50px; text-align: center; }
.delimiter { color: #e74c3c; font-size: 1.2rem; font-weight: bold; }
.quick-patterns { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
.quick-patterns .label { color: #666; font-size: 0.9rem; }
.quick-btn { padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 15px; background: white; cursor: pointer; font-size: 0.85rem; }
.quick-btn:hover { border-color: #667eea; color: #667eea; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
.results { background: #f8f9fa; border-radius: 8px; padding: 1rem; }
.result-header { font-weight: 600; margin-bottom: 0.5rem; }
.matches-list { display: flex; flex-direction: column; gap: 0.5rem; max-height: 200px; overflow-y: auto; }
.match-item { display: flex; align-items: center; gap: 1rem; padding: 0.5rem; background: white; border-radius: 4px; }
.match-index { color: #667eea; font-weight: 600; }
.match-text { flex: 1; font-family: monospace; background: #fff3cd; padding: 0.25rem 0.5rem; border-radius: 4px; }
.match-pos { color: #999; font-size: 0.85rem; }
.no-match { color: #999; text-align: center; padding: 1rem; }
.highlighted-text { padding: 1rem; background: white; border: 1px solid #ddd; border-radius: 8px; white-space: pre-wrap; font-family: monospace; max-height: 200px; overflow-y: auto; }
.highlighted-text :deep(mark) { background: #fff3cd; padding: 0 2px; border-radius: 2px; }
</style>
