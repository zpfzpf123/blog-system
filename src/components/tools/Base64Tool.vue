<template>
  <div class="tool-content">
    <div class="tabs">
      <button :class="{ active: mode === 'text' }" @click="mode = 'text'">文本编解码</button>
      <button :class="{ active: mode === 'image' }" @click="mode = 'image'">图片转Base64</button>
    </div>
    
    <template v-if="mode === 'text'">
      <div class="input-group">
        <label>输入内容:</label>
        <textarea v-model="input" placeholder="输入文本或Base64字符串..." rows="5"></textarea>
      </div>
      <div class="btn-group">
        <button @click="encode" class="btn primary">编码 →</button>
        <button @click="decode" class="btn primary">← 解码</button>
        <button @click="copy" class="btn">复制结果</button>
      </div>
      <div v-if="error" class="error-msg">{{ error }}</div>
      <div class="output-group">
        <label>输出结果:</label>
        <textarea v-model="output" readonly rows="5"></textarea>
      </div>
    </template>
    
    <template v-else>
      <div class="upload-area" @drop.prevent="handleDrop" @dragover.prevent @click="triggerUpload">
        <input type="file" ref="fileInput" @change="handleFile" accept="image/*" hidden />
        <p>点击或拖拽图片到此处</p>
      </div>
      <div v-if="imageBase64" class="image-result">
        <img :src="imageBase64" alt="预览" class="preview-img" />
        <textarea v-model="imageBase64" readonly rows="5"></textarea>
        <button @click="copyImage" class="btn primary full">复制Base64</button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const mode = ref<'text' | 'image'>('text')
const input = ref('')
const output = ref('')
const error = ref('')
const imageBase64 = ref('')
const fileInput = ref<HTMLInputElement>()

const encode = () => {
  error.value = ''
  try {
    output.value = btoa(unescape(encodeURIComponent(input.value)))
  } catch (e) {
    error.value = '编码失败'
  }
}

const decode = () => {
  error.value = ''
  try {
    output.value = decodeURIComponent(escape(atob(input.value)))
  } catch (e) {
    error.value = '解码失败，请确保输入有效的Base64字符串'
  }
}

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}

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
  const reader = new FileReader()
  reader.onload = (e) => {
    imageBase64.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

const copyImage = async () => {
  await navigator.clipboard.writeText(imageBase64.value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.tabs { display: flex; gap: 0.5rem; }
.tabs button { flex: 1; padding: 0.75rem; border: 1px solid #ddd; background: white; cursor: pointer; border-radius: 8px; }
.tabs button.active { background: #667eea; color: white; border-color: #667eea; }
.input-group, .output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.btn.full { width: 100%; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
.upload-area { border: 2px dashed #ddd; border-radius: 12px; padding: 2rem; text-align: center; cursor: pointer; }
.upload-area:hover { border-color: #667eea; }
.image-result { display: flex; flex-direction: column; gap: 1rem; }
.preview-img { max-width: 200px; max-height: 150px; border-radius: 8px; margin: 0 auto; }
</style>
