<template>
  <div class="tool-content">
    <div class="upload-area" @drop.prevent="handleDrop" @dragover.prevent @click="triggerUpload">
      <input type="file" ref="fileInput" @change="handleFile" accept="image/*" hidden />
      <div class="upload-hint">
        <span class="icon">📁</span>
        <p>点击或拖拽图片到此处</p>
        <p class="sub">支持 JPG、PNG、WebP 格式</p>
      </div>
    </div>
    
    <div v-if="originalImage" class="settings">
      <div class="setting-item">
        <label>压缩质量: {{ quality }}%</label>
        <input type="range" v-model="quality" min="10" max="100" @change="compress" />
      </div>
      <div class="setting-item">
        <label>最大宽度:</label>
        <input type="number" v-model="maxWidth" placeholder="不限制" @change="compress" />
      </div>
      <div class="setting-item">
        <label>输出格式:</label>
        <select v-model="outputFormat" @change="compress">
          <option value="image/jpeg">JPEG</option>
          <option value="image/png">PNG</option>
          <option value="image/webp">WebP</option>
        </select>
      </div>
    </div>
    
    <div v-if="originalImage" class="preview-container">
      <div class="preview-item">
        <h4>原图 ({{ formatSize(originalSize) }})</h4>
        <img :src="originalImage" alt="原图" />
      </div>
      <div class="preview-item">
        <h4>压缩后 ({{ formatSize(compressedSize) }}) <span class="savings">节省 {{ savings }}%</span></h4>
        <img :src="compressedImage" alt="压缩后" />
      </div>
    </div>
    
    <button v-if="compressedImage" @click="download" class="btn primary full">下载压缩图片</button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const fileInput = ref<HTMLInputElement>()
const originalImage = ref('')
const compressedImage = ref('')
const originalSize = ref(0)
const compressedSize = ref(0)
const quality = ref(80)
const maxWidth = ref<number | null>(null)
const outputFormat = ref('image/jpeg')

const savings = computed(() => {
  if (!originalSize.value) return 0
  return Math.round((1 - compressedSize.value / originalSize.value) * 100)
})

const triggerUpload = () => fileInput.value?.click()

const handleDrop = (e: DragEvent) => {
  const file = e.dataTransfer?.files[0]
  if (file) processFile(file)
}

const handleFile = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) processFile(file)
}

const processFile = (file: File) => {
  originalSize.value = file.size
  const reader = new FileReader()
  reader.onload = (e) => {
    originalImage.value = e.target?.result as string
    compress()
  }
  reader.readAsDataURL(file)
}

const compress = () => {
  if (!originalImage.value) return
  
  const img = new Image()
  img.onload = () => {
    const canvas = document.createElement('canvas')
    let width = img.width
    let height = img.height
    
    if (maxWidth.value && width > maxWidth.value) {
      height = (maxWidth.value / width) * height
      width = maxWidth.value
    }
    
    canvas.width = width
    canvas.height = height
    
    const ctx = canvas.getContext('2d')!
    ctx.drawImage(img, 0, 0, width, height)
    
    compressedImage.value = canvas.toDataURL(outputFormat.value, quality.value / 100)
    compressedSize.value = Math.round((compressedImage.value.length - 22) * 3 / 4)
  }
  img.src = originalImage.value
}

const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

const download = () => {
  const link = document.createElement('a')
  link.download = `compressed.${outputFormat.value.split('/')[1]}`
  link.href = compressedImage.value
  link.click()
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.upload-area { border: 2px dashed #ddd; border-radius: 12px; padding: 3rem; text-align: center; cursor: pointer; transition: all 0.3s; }
.upload-area:hover { border-color: #667eea; background: #f8f9ff; }
.upload-hint .icon { font-size: 3rem; }
.upload-hint p { margin: 0.5rem 0; color: #666; }
.upload-hint .sub { font-size: 0.85rem; color: #999; }
.settings { display: flex; flex-wrap: wrap; gap: 1rem; padding: 1rem; background: #f8f9fa; border-radius: 8px; }
.setting-item { display: flex; flex-direction: column; gap: 0.25rem; min-width: 150px; }
.setting-item label { font-size: 0.85rem; color: #666; }
.setting-item input[type="range"] { width: 100%; }
.setting-item input[type="number"], .setting-item select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
.preview-container { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.preview-item { text-align: center; }
.preview-item h4 { margin: 0 0 0.5rem; font-size: 0.9rem; }
.preview-item img { max-width: 100%; max-height: 200px; border-radius: 8px; border: 1px solid #eee; }
.savings { color: #27ae60; font-weight: normal; }
.btn { padding: 0.75rem 1.5rem; border: none; border-radius: 8px; cursor: pointer; font-size: 1rem; }
.btn.primary { background: #667eea; color: white; }
.btn.primary:hover { background: #5a6fd6; }
.btn.full { width: 100%; }
</style>
