<template>
  <div class="tool-content">
    <div class="color-preview" :style="{ background: previewColor }"></div>
    <div class="color-inputs">
      <div class="input-row">
        <label>HEX:</label>
        <input v-model="hex" @input="fromHex" placeholder="#FF5733" />
        <button @click="copyValue(hex)" class="copy-btn">复制</button>
      </div>
      <div class="input-row">
        <label>RGB:</label>
        <input v-model="rgb" @input="fromRgb" placeholder="rgb(255, 87, 51)" />
        <button @click="copyValue(rgb)" class="copy-btn">复制</button>
      </div>
      <div class="input-row">
        <label>HSL:</label>
        <input v-model="hsl" @input="fromHsl" placeholder="hsl(11, 100%, 60%)" />
        <button @click="copyValue(hsl)" class="copy-btn">复制</button>
      </div>
      <div class="input-row">
        <label>RGBA:</label>
        <input v-model="rgba" readonly placeholder="rgba(255, 87, 51, 1)" />
        <button @click="copyValue(rgba)" class="copy-btn">复制</button>
      </div>
    </div>
    <div class="color-picker-row">
      <label>选择颜色:</label>
      <input type="color" v-model="pickerColor" @input="fromPicker" />
    </div>
    <div class="preset-colors">
      <span class="label">常用颜色:</span>
      <div class="color-grid">
        <div v-for="c in presetColors" :key="c" class="preset-color" :style="{ background: c }" @click="setColor(c)"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const hex = ref('#667eea')
const rgb = ref('rgb(102, 126, 234)')
const hsl = ref('hsl(229, 76%, 66%)')
const rgba = ref('rgba(102, 126, 234, 1)')
const pickerColor = ref('#667eea')
const previewColor = ref('#667eea')

const presetColors = [
  '#e74c3c', '#e67e22', '#f1c40f', '#2ecc71', '#1abc9c',
  '#3498db', '#9b59b6', '#34495e', '#95a5a6', '#000000',
  '#ffffff', '#667eea', '#764ba2', '#ff6b6b', '#4ecdc4'
]

const setColor = (color: string) => {
  hex.value = color
  fromHex()
}

const fromPicker = () => {
  hex.value = pickerColor.value
  fromHex()
}

const fromHex = () => {
  const h = hex.value.replace('#', '')
  if (!/^[0-9A-Fa-f]{6}$/.test(h)) return
  
  const r = parseInt(h.slice(0, 2), 16)
  const g = parseInt(h.slice(2, 4), 16)
  const b = parseInt(h.slice(4, 6), 16)
  
  updateColors(r, g, b)
}

const fromRgb = () => {
  const match = rgb.value.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/)
  if (!match) return
  
  const r = parseInt(match[1])
  const g = parseInt(match[2])
  const b = parseInt(match[3])
  
  updateColors(r, g, b)
}

const fromHsl = () => {
  const match = hsl.value.match(/hsl\((\d+),\s*(\d+)%,\s*(\d+)%\)/)
  if (!match) return
  
  const h = parseInt(match[1]) / 360
  const s = parseInt(match[2]) / 100
  const l = parseInt(match[3]) / 100
  
  const { r, g, b } = hslToRgb(h, s, l)
  updateColors(r, g, b)
}

const updateColors = (r: number, g: number, b: number) => {
  hex.value = '#' + [r, g, b].map(x => x.toString(16).padStart(2, '0')).join('')
  rgb.value = `rgb(${r}, ${g}, ${b})`
  rgba.value = `rgba(${r}, ${g}, ${b}, 1)`
  pickerColor.value = hex.value
  previewColor.value = hex.value
  
  const { h, s, l } = rgbToHsl(r, g, b)
  hsl.value = `hsl(${Math.round(h * 360)}, ${Math.round(s * 100)}%, ${Math.round(l * 100)}%)`
}

const rgbToHsl = (r: number, g: number, b: number) => {
  r /= 255; g /= 255; b /= 255
  const max = Math.max(r, g, b), min = Math.min(r, g, b)
  let h = 0, s = 0, l = (max + min) / 2
  
  if (max !== min) {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    switch (max) {
      case r: h = ((g - b) / d + (g < b ? 6 : 0)) / 6; break
      case g: h = ((b - r) / d + 2) / 6; break
      case b: h = ((r - g) / d + 4) / 6; break
    }
  }
  return { h, s, l }
}

const hslToRgb = (h: number, s: number, l: number) => {
  let r, g, b
  if (s === 0) {
    r = g = b = l
  } else {
    const hue2rgb = (p: number, q: number, t: number) => {
      if (t < 0) t += 1
      if (t > 1) t -= 1
      if (t < 1/6) return p + (q - p) * 6 * t
      if (t < 1/2) return q
      if (t < 2/3) return p + (q - p) * (2/3 - t) * 6
      return p
    }
    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q
    r = hue2rgb(p, q, h + 1/3)
    g = hue2rgb(p, q, h)
    b = hue2rgb(p, q, h - 1/3)
  }
  return { r: Math.round(r * 255), g: Math.round(g * 255), b: Math.round(b * 255) }
}

const copyValue = async (value: string) => {
  await navigator.clipboard.writeText(value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.color-preview { height: 80px; border-radius: 12px; border: 1px solid #ddd; }
.color-inputs { display: flex; flex-direction: column; gap: 0.75rem; }
.input-row { display: flex; align-items: center; gap: 0.5rem; }
.input-row label { width: 50px; font-weight: 600; color: #333; }
.input-row input { flex: 1; padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; font-family: monospace; }
.input-row input:focus { outline: none; border-color: #667eea; }
.copy-btn { padding: 0.5rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; background: white; cursor: pointer; }
.copy-btn:hover { border-color: #667eea; color: #667eea; }
.color-picker-row { display: flex; align-items: center; gap: 1rem; }
.color-picker-row input[type="color"] { width: 60px; height: 40px; border: none; cursor: pointer; }
.preset-colors .label { font-size: 0.9rem; color: #666; }
.color-grid { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.5rem; }
.preset-color { width: 32px; height: 32px; border-radius: 6px; cursor: pointer; border: 2px solid transparent; transition: transform 0.2s; }
.preset-color:hover { transform: scale(1.1); border-color: #333; }
</style>
