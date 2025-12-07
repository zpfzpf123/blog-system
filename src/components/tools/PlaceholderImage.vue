<template>
  <div class="tool-content">
    <div class="settings">
      <div class="setting-row">
        <div class="setting-item">
          <label>宽度:</label>
          <input v-model.number="width" type="number" min="1" max="2000" @input="generate" />
        </div>
        <span class="x">×</span>
        <div class="setting-item">
          <label>高度:</label>
          <input v-model.number="height" type="number" min="1" max="2000" @input="generate" />
        </div>
      </div>
      <div class="setting-row">
        <div class="setting-item">
          <label>背景色:</label>
          <input v-model="bgColor" type="color" @input="generate" />
        </div>
        <div class="setting-item">
          <label>文字色:</label>
          <input v-model="textColor" type="color" @input="generate" />
        </div>
      </div>
      <div class="setting-item full">
        <label>显示文字:</label>
        <input v-model="text" placeholder="留空显示尺寸" @input="generate" />
      </div>
      <div class="setting-item">
        <label>格式:</label>
        <select v-model="format" @change="generate">
          <option value="png">PNG</option>
          <option value="jpg">JPG</option>
          <option value="svg">SVG</option>
        </select>
      </div>
    </div>
    
    <div class="presets">
      <span class="label">常用尺寸:</span>
      <button v-for="p in presets" :key="p.name" @click="applyPreset(p)">{{ p.name }}</button>
    </div>
    
    <div class="preview">
      <img :src="imageUrl" :alt="`${width}x${height}`" />
    </div>
    
    <div class="output-group">
      <label>图片URL:</label>
      <div class="url-box">
        <input :value="imageUrl" readonly />
        <button @click="copyUrl" class="btn">复制</button>
      </div>
      <div class="code-snippets">
        <button @click="copyHtml" class="btn sm">复制HTML</button>
        <button @click="copyMarkdown" class="btn sm">复制Markdown</button>
        <button @click="copyCss" class="btn sm">复制CSS</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const width = ref(400)
const height = ref(300)
const bgColor = ref('#cccccc')
const textColor = ref('#666666')
const text = ref('')
const format = ref('png')

const presets = [
  { name: '头像', w: 100, h: 100 },
  { name: '缩略图', w: 150, h: 150 },
  { name: '卡片', w: 300, h: 200 },
  { name: '横幅', w: 800, h: 200 },
  { name: '16:9', w: 640, h: 360 },
  { name: '4:3', w: 640, h: 480 },
]

const imageUrl = computed(() => {
  const bg = bgColor.value.replace('#', '')
  const fg = textColor.value.replace('#', '')
  const t = text.value || `${width.value}×${height.value}`
  return `https://via.placeholder.com/${width.value}x${height.value}/${bg}/${fg}.${format.value}?text=${encodeURIComponent(t)}`
})

const generate = () => {}

const applyPreset = (p: { w: number; h: number }) => {
  width.value = p.w
  height.value = p.h
}

const copyUrl = async () => {
  await navigator.clipboard.writeText(imageUrl.value)
}

const copyHtml = async () => {
  await navigator.clipboard.writeText(`<img src="${imageUrl.value}" alt="placeholder" width="${width.value}" height="${height.value}">`)
}

const copyMarkdown = async () => {
  await navigator.clipboard.writeText(`![placeholder](${imageUrl.value})`)
}

const copyCss = async () => {
  await navigator.clipboard.writeText(`background-image: url('${imageUrl.value}');`)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.settings { display: flex; flex-direction: column; gap: 0.75rem; }
.setting-row { display: flex; align-items: flex-end; gap: 0.5rem; }
.setting-item { display: flex; flex-direction: column; gap: 0.25rem; }
.setting-item.full { width: 100%; }
.setting-item label { font-size: 0.85rem; color: #666; }
.setting-item input, .setting-item select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
.setting-item input[type="color"] { width: 60px; height: 36px; padding: 2px; }
.setting-item input[type="number"] { width: 80px; }
.x { font-size: 1.2rem; color: #999; padding-bottom: 0.5rem; }
.presets { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
.presets .label { font-size: 0.9rem; color: #666; }
.presets button { padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 15px; background: white; cursor: pointer; }
.presets button:hover { border-color: #667eea; color: #667eea; }
.preview { display: flex; justify-content: center; padding: 1rem; background: #f8f9fa; border-radius: 8px; }
.preview img { max-width: 100%; max-height: 200px; border: 1px solid #ddd; }
.output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
.url-box { display: flex; gap: 0.5rem; }
.url-box input { flex: 1; padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; font-family: monospace; font-size: 0.85rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.sm { padding: 0.25rem 0.75rem; font-size: 0.85rem; }
.code-snippets { display: flex; gap: 0.5rem; }
</style>
