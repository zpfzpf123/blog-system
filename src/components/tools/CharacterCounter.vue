<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入文本:</label>
      <textarea v-model="text" placeholder="输入或粘贴文本..." rows="8" @input="analyze"></textarea>
    </div>
    <div class="stats-grid">
      <div class="stat-item">
        <span class="stat-value">{{ stats.chars }}</span>
        <span class="stat-label">字符数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.charsNoSpace }}</span>
        <span class="stat-label">字符(不含空格)</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.words }}</span>
        <span class="stat-label">单词数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.lines }}</span>
        <span class="stat-label">行数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.paragraphs }}</span>
        <span class="stat-label">段落数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.chinese }}</span>
        <span class="stat-label">中文字符</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.english }}</span>
        <span class="stat-label">英文字母</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ stats.numbers }}</span>
        <span class="stat-label">数字</span>
      </div>
    </div>
    <div class="reading-time">
      预计阅读时间: {{ readingTime }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'

const text = ref('')
const stats = reactive({
  chars: 0,
  charsNoSpace: 0,
  words: 0,
  lines: 0,
  paragraphs: 0,
  chinese: 0,
  english: 0,
  numbers: 0
})

const analyze = () => {
  const t = text.value
  stats.chars = t.length
  stats.charsNoSpace = t.replace(/\s/g, '').length
  stats.words = t.trim() ? t.trim().split(/\s+/).length : 0
  stats.lines = t ? t.split('\n').length : 0
  stats.paragraphs = t.trim() ? t.trim().split(/\n\s*\n/).filter(p => p.trim()).length : 0
  stats.chinese = (t.match(/[\u4e00-\u9fa5]/g) || []).length
  stats.english = (t.match(/[a-zA-Z]/g) || []).length
  stats.numbers = (t.match(/\d/g) || []).length
}

const readingTime = computed(() => {
  // 中文约300字/分钟，英文约200词/分钟
  const chineseMinutes = stats.chinese / 300
  const englishMinutes = stats.words / 200
  const totalMinutes = Math.ceil(chineseMinutes + englishMinutes)
  if (totalMinutes < 1) return '不到1分钟'
  return `约${totalMinutes}分钟`
})
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; }
.stat-item { text-align: center; padding: 1rem; background: #f8f9fa; border-radius: 8px; }
.stat-value { display: block; font-size: 1.5rem; font-weight: bold; color: #667eea; }
.stat-label { display: block; font-size: 0.8rem; color: #666; margin-top: 0.25rem; }
.reading-time { text-align: center; padding: 0.75rem; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 8px; }
@media (max-width: 600px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
