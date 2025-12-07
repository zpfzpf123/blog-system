<template>
  <div class="tool-content">
    <div class="ratio-section">
      <h4>计算宽高比</h4>
      <div class="input-row">
        <div class="input-item">
          <label>宽度:</label>
          <input v-model.number="width1" type="number" @input="calculateRatio" />
        </div>
        <span class="separator">×</span>
        <div class="input-item">
          <label>高度:</label>
          <input v-model.number="height1" type="number" @input="calculateRatio" />
        </div>
        <span class="separator">=</span>
        <div class="result">{{ ratio }}</div>
      </div>
    </div>
    
    <div class="scale-section">
      <h4>等比例缩放</h4>
      <div class="input-row">
        <div class="input-item">
          <label>原始宽度:</label>
          <input v-model.number="originalWidth" type="number" @input="calculateScale" />
        </div>
        <div class="input-item">
          <label>原始高度:</label>
          <input v-model.number="originalHeight" type="number" @input="calculateScale" />
        </div>
      </div>
      <div class="input-row">
        <div class="input-item">
          <label>新宽度:</label>
          <input v-model.number="newWidth" type="number" @input="scaleByWidth" />
        </div>
        <div class="input-item">
          <label>新高度:</label>
          <input v-model.number="newHeight" type="number" @input="scaleByHeight" />
        </div>
      </div>
    </div>
    
    <div class="common-ratios">
      <h4>常用宽高比</h4>
      <div class="ratio-grid">
        <div v-for="r in commonRatios" :key="r.name" class="ratio-item" @click="applyRatio(r)">
          <div class="ratio-preview" :style="{ aspectRatio: r.ratio }"></div>
          <span class="ratio-name">{{ r.name }}</span>
          <span class="ratio-value">{{ r.display }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const width1 = ref(1920)
const height1 = ref(1080)
const ratio = ref('16:9')

const originalWidth = ref(1920)
const originalHeight = ref(1080)
const newWidth = ref(1280)
const newHeight = ref(720)

const commonRatios = [
  { name: '16:9', ratio: '16/9', display: '1920×1080', w: 16, h: 9 },
  { name: '4:3', ratio: '4/3', display: '1024×768', w: 4, h: 3 },
  { name: '21:9', ratio: '21/9', display: '2560×1080', w: 21, h: 9 },
  { name: '1:1', ratio: '1/1', display: '1080×1080', w: 1, h: 1 },
  { name: '9:16', ratio: '9/16', display: '1080×1920', w: 9, h: 16 },
  { name: '3:2', ratio: '3/2', display: '1500×1000', w: 3, h: 2 },
]

const gcd = (a: number, b: number): number => b === 0 ? a : gcd(b, a % b)

const calculateRatio = () => {
  if (!width1.value || !height1.value) return
  const divisor = gcd(width1.value, height1.value)
  ratio.value = `${width1.value / divisor}:${height1.value / divisor}`
}

const calculateScale = () => {
  if (!originalWidth.value || !originalHeight.value) return
  const aspectRatio = originalWidth.value / originalHeight.value
  newHeight.value = Math.round(newWidth.value / aspectRatio)
}

const scaleByWidth = () => {
  if (!originalWidth.value || !originalHeight.value) return
  const aspectRatio = originalWidth.value / originalHeight.value
  newHeight.value = Math.round(newWidth.value / aspectRatio)
}

const scaleByHeight = () => {
  if (!originalWidth.value || !originalHeight.value) return
  const aspectRatio = originalWidth.value / originalHeight.value
  newWidth.value = Math.round(newHeight.value * aspectRatio)
}

const applyRatio = (r: { w: number; h: number }) => {
  width1.value = r.w * 100
  height1.value = r.h * 100
  calculateRatio()
  
  originalWidth.value = r.w * 100
  originalHeight.value = r.h * 100
  calculateScale()
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1.5rem; }
h4 { margin: 0 0 0.75rem; color: #333; font-size: 1rem; }
.input-row { display: flex; align-items: flex-end; gap: 1rem; flex-wrap: wrap; }
.input-item { display: flex; flex-direction: column; gap: 0.25rem; }
.input-item label { font-size: 0.85rem; color: #666; }
.input-item input { width: 120px; padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; font-size: 1rem; }
.input-item input:focus { outline: none; border-color: #667eea; }
.separator { font-size: 1.2rem; color: #999; padding-bottom: 0.5rem; }
.result { font-size: 1.5rem; font-weight: bold; color: #667eea; padding-bottom: 0.25rem; }
.ratio-section, .scale-section { padding: 1rem; background: #f8f9fa; border-radius: 8px; }
.common-ratios h4 { margin-bottom: 1rem; }
.ratio-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 0.75rem; }
.ratio-item { text-align: center; cursor: pointer; padding: 0.5rem; border-radius: 8px; transition: background 0.3s; }
.ratio-item:hover { background: #f0f0f0; }
.ratio-preview { width: 100%; max-width: 60px; margin: 0 auto 0.5rem; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 4px; min-height: 30px; }
.ratio-name { display: block; font-weight: 600; font-size: 0.9rem; }
.ratio-value { display: block; font-size: 0.75rem; color: #999; }
@media (max-width: 600px) {
  .ratio-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
