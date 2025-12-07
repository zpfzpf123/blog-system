<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入SVG代码:</label>
      <textarea v-model="input" placeholder="粘贴SVG代码..." rows="6" @input="optimize"></textarea>
    </div>
    
    <div v-if="input" class="stats">
      <span>原始: {{ originalSize }} 字节</span>
      <span>优化后: {{ optimizedSize }} 字节</span>
      <span class="savings">节省: {{ savings }}%</span>
    </div>
    
    <div class="btn-group">
      <button @click="optimize" class="btn primary">优化</button>
      <button @click="copy" class="btn">复制</button>
      <button @click="download" class="btn">下载</button>
    </div>
    
    <div v-if="output" class="preview-section">
      <div class="preview-box">
        <div v-html="output" class="svg-preview"></div>
      </div>
    </div>
    
    <div class="output-group">
      <label>优化后的SVG:</label>
      <textarea v-model="output" readonly rows="6"></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const input = ref('')
const output = ref('')

const originalSize = computed(() => new Blob([input.value]).size)
const optimizedSize = computed(() => new Blob([output.value]).size)
const savings = computed(() => {
  if (!originalSize.value) return 0
  return Math.round((1 - optimizedSize.value / originalSize.value) * 100)
})

const optimize = () => {
  if (!input.value) {
    output.value = ''
    return
  }
  
  let svg = input.value
  
  // 移除注释
  svg = svg.replace(/<!--[\s\S]*?-->/g, '')
  
  // 移除多余空白
  svg = svg.replace(/\s+/g, ' ')
  svg = svg.replace(/>\s+</g, '><')
  
  // 移除不必要的属性
  svg = svg.replace(/\s*(xmlns:xlink|xml:space|version)="[^"]*"/g, '')
  
  // 移除空的属性
  svg = svg.replace(/\s+\w+=""/g, '')
  
  // 移除默认值
  svg = svg.replace(/\s*fill-opacity="1"/g, '')
  svg = svg.replace(/\s*stroke-opacity="1"/g, '')
  svg = svg.replace(/\s*opacity="1"/g, '')
  
  // 简化颜色
  svg = svg.replace(/#([0-9a-fA-F])\1([0-9a-fA-F])\2([0-9a-fA-F])\3/g, '#$1$2$3')
  
  // 移除空的 g 标签
  svg = svg.replace(/<g>\s*<\/g>/g, '')
  
  output.value = svg.trim()
}

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}

const download = () => {
  const blob = new Blob([output.value], { type: 'image/svg+xml' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.download = 'optimized.svg'
  link.href = url
  link.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group, .output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; font-size: 0.85rem; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.stats { display: flex; gap: 1.5rem; padding: 0.75rem; background: #f8f9fa; border-radius: 6px; font-size: 0.9rem; }
.stats .savings { color: #27ae60; font-weight: 600; }
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.preview-section { display: flex; justify-content: center; }
.preview-box { padding: 1rem; background: #f8f9fa; border-radius: 8px; }
.svg-preview { max-width: 200px; max-height: 150px; }
.svg-preview :deep(svg) { max-width: 100%; max-height: 100%; }
</style>
