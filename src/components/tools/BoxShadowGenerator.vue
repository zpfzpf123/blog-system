<template>
  <div class="tool-content">
    <div class="preview-area">
      <div class="preview-box" :style="{ boxShadow: shadowCss }"></div>
    </div>
    
    <div class="controls">
      <div class="control-item">
        <label>水平偏移: {{ offsetX }}px</label>
        <input type="range" v-model.number="offsetX" min="-50" max="50" @input="updateShadow" />
      </div>
      <div class="control-item">
        <label>垂直偏移: {{ offsetY }}px</label>
        <input type="range" v-model.number="offsetY" min="-50" max="50" @input="updateShadow" />
      </div>
      <div class="control-item">
        <label>模糊半径: {{ blur }}px</label>
        <input type="range" v-model.number="blur" min="0" max="100" @input="updateShadow" />
      </div>
      <div class="control-item">
        <label>扩展半径: {{ spread }}px</label>
        <input type="range" v-model.number="spread" min="-50" max="50" @input="updateShadow" />
      </div>
      <div class="control-item">
        <label>颜色:</label>
        <div class="color-input">
          <input type="color" v-model="color" @input="updateShadow" />
          <input type="text" v-model="color" @input="updateShadow" />
        </div>
      </div>
      <div class="control-item">
        <label>透明度: {{ opacity }}%</label>
        <input type="range" v-model.number="opacity" min="0" max="100" @input="updateShadow" />
      </div>
      <div class="control-item checkbox">
        <label><input type="checkbox" v-model="inset" @change="updateShadow" /> 内阴影 (inset)</label>
      </div>
    </div>
    
    <div class="presets">
      <span class="label">预设:</span>
      <div class="preset-buttons">
        <button v-for="(p, i) in presets" :key="i" @click="applyPreset(p)">{{ p.name }}</button>
      </div>
    </div>
    
    <div class="output-group">
      <label>CSS代码:</label>
      <textarea v-model="shadowCss" readonly rows="2"></textarea>
      <button @click="copy" class="btn">复制</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const offsetX = ref(5)
const offsetY = ref(5)
const blur = ref(15)
const spread = ref(0)
const color = ref('#000000')
const opacity = ref(20)
const inset = ref(false)
const shadowCss = ref('')

const presets = [
  { name: '轻柔', offsetX: 0, offsetY: 2, blur: 10, spread: 0, color: '#000000', opacity: 10, inset: false },
  { name: '悬浮', offsetX: 0, offsetY: 10, blur: 30, spread: -5, color: '#000000', opacity: 20, inset: false },
  { name: '深邃', offsetX: 0, offsetY: 20, blur: 40, spread: -10, color: '#000000', opacity: 30, inset: false },
  { name: '内凹', offsetX: 0, offsetY: 2, blur: 5, spread: 0, color: '#000000', opacity: 30, inset: true },
  { name: '发光', offsetX: 0, offsetY: 0, blur: 20, spread: 5, color: '#667eea', opacity: 50, inset: false },
]

const updateShadow = () => {
  const rgba = hexToRgba(color.value, opacity.value / 100)
  const insetStr = inset.value ? 'inset ' : ''
  shadowCss.value = `${insetStr}${offsetX.value}px ${offsetY.value}px ${blur.value}px ${spread.value}px ${rgba}`
}

const hexToRgba = (hex: string, alpha: number): string => {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const applyPreset = (p: typeof presets[0]) => {
  offsetX.value = p.offsetX
  offsetY.value = p.offsetY
  blur.value = p.blur
  spread.value = p.spread
  color.value = p.color
  opacity.value = p.opacity
  inset.value = p.inset
  updateShadow()
}

const copy = async () => {
  await navigator.clipboard.writeText(`box-shadow: ${shadowCss.value};`)
}

onMounted(updateShadow)
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.preview-area { display: flex; justify-content: center; align-items: center; padding: 3rem; background: #f8f9fa; border-radius: 12px; }
.preview-box { width: 120px; height: 120px; background: white; border-radius: 12px; }
.controls { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.control-item { display: flex; flex-direction: column; gap: 0.25rem; }
.control-item label { font-size: 0.85rem; color: #666; }
.control-item input[type="range"] { width: 100%; }
.control-item.checkbox { grid-column: span 2; }
.control-item.checkbox label { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; }
.color-input { display: flex; gap: 0.5rem; }
.color-input input[type="color"] { width: 50px; height: 35px; border: none; cursor: pointer; }
.color-input input[type="text"] { flex: 1; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; font-family: monospace; }
.presets .label { font-size: 0.9rem; color: #666; }
.preset-buttons { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.5rem; }
.preset-buttons button { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; background: white; cursor: pointer; }
.preset-buttons button:hover { border-color: #667eea; color: #667eea; }
.output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: none; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; align-self: flex-start; }
.btn:hover { border-color: #667eea; color: #667eea; }
</style>
