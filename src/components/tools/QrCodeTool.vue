<template>
  <div class="tool-content">
    <div class="tabs">
      <button :class="{ active: mode === 'generate' }" @click="mode = 'generate'">生成二维码</button>
      <button :class="{ active: mode === 'scan' }" @click="mode = 'scan'">解析二维码</button>
    </div>
    
    <template v-if="mode === 'generate'">
      <div class="input-group">
        <label>输入内容:</label>
        <textarea v-model="content" placeholder="输入文本、网址等..." rows="3" @input="generateQR"></textarea>
      </div>
      <div class="settings">
        <div class="setting-item">
          <label>尺寸:</label>
          <select v-model="size" @change="generateQR">
            <option value="128">128 x 128</option>
            <option value="200">200 x 200</option>
            <option value="256">256 x 256</option>
            <option value="300">300 x 300</option>
          </select>
        </div>
      </div>
      <div v-if="qrImage" class="qr-preview">
        <img :src="qrImage" alt="QR Code" />
        <button @click="downloadQR" class="btn primary">下载二维码</button>
      </div>
    </template>
    
    <template v-else>
      <div class="upload-area" @drop.prevent="handleDrop" @dragover.prevent @click="triggerUpload">
        <input type="file" ref="fileInput" @change="handleFile" accept="image/*" hidden />
        <p>点击或拖拽二维码图片到此处</p>
      </div>
      <div v-if="scanResult" class="scan-result">
        <label>解析结果:</label>
        <div class="result-text">{{ scanResult }}</div>
        <button @click="copyResult" class="btn">复制</button>
      </div>
      <div v-if="scanError" class="error-msg">{{ scanError }}</div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const mode = ref<'generate' | 'scan'>('generate')
const content = ref('https://example.com')
const size = ref('200')
const qrImage = ref('')
const scanResult = ref('')
const scanError = ref('')
const fileInput = ref<HTMLInputElement>()

// 使用 QR Server API 生成二维码
const generateQR = () => {
  if (!content.value) {
    qrImage.value = ''
    return
  }
  qrImage.value = `https://api.qrserver.com/v1/create-qr-code/?size=${size.value}x${size.value}&data=${encodeURIComponent(content.value)}`
}

const downloadQR = () => {
  const link = document.createElement('a')
  link.download = 'qrcode.png'
  link.href = qrImage.value
  link.click()
}

const triggerUpload = () => fileInput.value?.click()

const handleDrop = (e: DragEvent) => {
  const file = e.dataTransfer?.files[0]
  if (file) scanQR(file)
}

const handleFile = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) scanQR(file)
}

const scanQR = async (file: File) => {
  scanError.value = ''
  scanResult.value = ''
  
  // 使用 API 解析二维码
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await fetch('https://api.qrserver.com/v1/read-qr-code/', {
      method: 'POST',
      body: formData
    })
    const data = await response.json()
    if (data[0]?.symbol[0]?.data) {
      scanResult.value = data[0].symbol[0].data
    } else {
      scanError.value = '无法识别二维码，请确保图片清晰'
    }
  } catch {
    scanError.value = '解析失败，请重试'
  }
}

const copyResult = async () => {
  await navigator.clipboard.writeText(scanResult.value)
}

onMounted(() => {
  generateQR()
})
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.tabs { display: flex; gap: 0.5rem; }
.tabs button { flex: 1; padding: 0.75rem; border: 1px solid #ddd; background: white; cursor: pointer; border-radius: 8px; }
.tabs button.active { background: #667eea; color: white; border-color: #667eea; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.settings { display: flex; gap: 1rem; }
.setting-item { display: flex; flex-direction: column; gap: 0.25rem; }
.setting-item select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
.qr-preview { text-align: center; padding: 1rem; background: #f8f9fa; border-radius: 8px; }
.qr-preview img { max-width: 100%; margin-bottom: 1rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.upload-area { border: 2px dashed #ddd; border-radius: 12px; padding: 2rem; text-align: center; cursor: pointer; }
.upload-area:hover { border-color: #667eea; }
.scan-result { padding: 1rem; background: #f8f9fa; border-radius: 8px; }
.result-text { margin: 0.5rem 0; padding: 0.75rem; background: white; border-radius: 6px; word-break: break-all; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
</style>
