<template>
  <div class="tool-content">
    <div class="preview" :style="{ background: gradientCss }"></div>
    
    <div class="controls">
      <div class="control-row">
        <label>类型:</label>
        <select v-model="type" @change="updateGradient">
          <option value="linear">线性渐变</option>
          <option value="radial">径向渐变</option>
        </select>
      </div>
      
      <div v-if="type === 'linear'" class="control-row">
        <label>角度: {{ angle }}°</label>
        <input type="range" v-model.number="angle" min="0" max="360" @input="updateGradient" />
      </div>
      
      <div class="colors-section">
        <label>颜色节点:</label>
        <div v-for="(stop, i) in colorStops" :key="i" class="color-stop">
          <input type="color" v-model="stop.color" @input="updateGradient" />
          <input type="number" v-model.number="stop.position" min="0" max="100" @input="updateGradient" />
          <span>%</span>
          <button v-if="colorStops.length > 2" @click="removeStop(i)" class="remove-btn">✕</button>
        </div>
        <button @click="addStop" class="add-btn">+ 添加颜色</button>
      </div>
    </div>
    
    <div class="presets">
      <span class="label">预设:</span>
      <div class="preset-grid">
        <div v-for="(p, i) in presets" :key="i" class="preset-item" :style="{ background: p.css }" @click="applyPreset(p)"></div>
      </div>
    </div>
    
    <div class="output-group">
      <label>CSS代码:</label>
      <textarea v-model="gradientCss" readonly rows="2"></textarea>
      <button @click="copy" class="btn">复制</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

const type = ref('linear')
const angle = ref(90)
const gradientCss = ref('')
const colorStops = reactive([
  { color: '#667eea', position: 0 },
  { color: '#764ba2', position: 100 }
])

const presets = [
  { css: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', stops: [{ color: '#667eea', position: 0 }, { color: '#764ba2', position: 100 }] },
  { css: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', stops: [{ color: '#f093fb', position: 0 }, { color: '#f5576c', position: 100 }] },
  { css: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', stops: [{ color: '#4facfe', position: 0 }, { color: '#00f2fe', position: 100 }] },
  { css: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', stops: [{ color: '#43e97b', position: 0 }, { color: '#38f9d7', position: 100 }] },
  { css: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', stops: [{ color: '#fa709a', position: 0 }, { color: '#fee140', position: 100 }] },
  { css: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)', stops: [{ color: '#a8edea', position: 0 }, { color: '#fed6e3', position: 100 }] },
]

const updateGradient = () => {
  const stops = colorStops.map(s => `${s.color} ${s.position}%`).join(', ')
  if (type.value === 'linear') {
    gradientCss.value = `linear-gradient(${angle.value}deg, ${stops})`
  } else {
    gradientCss.value = `radial-gradient(circle, ${stops})`
  }
}

const addStop = () => {
  const lastPos = colorStops[colorStops.length - 1]?.position || 0
  colorStops.push({ color: '#ffffff', position: Math.min(100, lastPos + 20) })
  updateGradient()
}

const removeStop = (index: number) => {
  colorStops.splice(index, 1)
  updateGradient()
}

const applyPreset = (preset: { stops: Array<{ color: string; position: number }> }) => {
  colorStops.length = 0
  preset.stops.forEach(s => colorStops.push({ ...s }))
  updateGradient()
}

const copy = async () => {
  await navigator.clipboard.writeText(`background: ${gradientCss.value};`)
}

onMounted(updateGradient)
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.preview { height: 120px; border-radius: 12px; border: 1px solid #ddd; }
.controls { display: flex; flex-direction: column; gap: 1rem; }
.control-row { display: flex; align-items: center; gap: 1rem; }
.control-row label { min-width: 100px; font-size: 0.9rem; color: #666; }
.control-row select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
.control-row input[type="range"] { flex: 1; }
.colors-section label { display: block; font-size: 0.9rem; color: #666; margin-bottom: 0.5rem; }
.color-stop { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem; }
.color-stop input[type="color"] { width: 50px; height: 35px; border: none; cursor: pointer; }
.color-stop input[type="number"] { width: 60px; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
.remove-btn { width: 24px; height: 24px; border: none; background: #fee; color: #c00; border-radius: 50%; cursor: pointer; }
.add-btn { padding: 0.5rem 1rem; border: 1px dashed #ddd; border-radius: 6px; background: white; cursor: pointer; }
.add-btn:hover { border-color: #667eea; color: #667eea; }
.presets .label { font-size: 0.9rem; color: #666; }
.preset-grid { display: flex; gap: 0.5rem; margin-top: 0.5rem; flex-wrap: wrap; }
.preset-item { width: 50px; height: 30px; border-radius: 6px; cursor: pointer; border: 2px solid transparent; }
.preset-item:hover { border-color: #333; }
.output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: none; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; align-self: flex-start; }
.btn:hover { border-color: #667eea; color: #667eea; }
</style>
