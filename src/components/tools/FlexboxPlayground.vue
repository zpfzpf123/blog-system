<template>
  <div class="tool-content">
    <div class="playground">
      <div class="container-preview" :style="containerStyle">
        <div v-for="i in itemCount" :key="i" class="item-preview">{{ i }}</div>
      </div>
    </div>
    
    <div class="controls">
      <h4>容器属性</h4>
      <div class="control-grid">
        <div class="control-item">
          <label>flex-direction:</label>
          <select v-model="container.flexDirection">
            <option>row</option>
            <option>row-reverse</option>
            <option>column</option>
            <option>column-reverse</option>
          </select>
        </div>
        <div class="control-item">
          <label>justify-content:</label>
          <select v-model="container.justifyContent">
            <option>flex-start</option>
            <option>flex-end</option>
            <option>center</option>
            <option>space-between</option>
            <option>space-around</option>
            <option>space-evenly</option>
          </select>
        </div>
        <div class="control-item">
          <label>align-items:</label>
          <select v-model="container.alignItems">
            <option>stretch</option>
            <option>flex-start</option>
            <option>flex-end</option>
            <option>center</option>
            <option>baseline</option>
          </select>
        </div>
        <div class="control-item">
          <label>flex-wrap:</label>
          <select v-model="container.flexWrap">
            <option>nowrap</option>
            <option>wrap</option>
            <option>wrap-reverse</option>
          </select>
        </div>
        <div class="control-item">
          <label>gap:</label>
          <input type="text" v-model="container.gap" placeholder="10px" />
        </div>
        <div class="control-item">
          <label>子元素数量:</label>
          <input type="number" v-model.number="itemCount" min="1" max="12" />
        </div>
      </div>
    </div>
    
    <div class="output-group">
      <label>CSS代码:</label>
      <textarea :value="cssCode" readonly rows="6"></textarea>
      <button @click="copy" class="btn">复制</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'

const itemCount = ref(5)
const container = reactive({
  flexDirection: 'row',
  justifyContent: 'flex-start',
  alignItems: 'stretch',
  flexWrap: 'nowrap',
  gap: '10px'
})

const containerStyle = computed(() => ({
  display: 'flex',
  flexDirection: container.flexDirection,
  justifyContent: container.justifyContent,
  alignItems: container.alignItems,
  flexWrap: container.flexWrap,
  gap: container.gap
}))

const cssCode = computed(() => {
  return `.container {
  display: flex;
  flex-direction: ${container.flexDirection};
  justify-content: ${container.justifyContent};
  align-items: ${container.alignItems};
  flex-wrap: ${container.flexWrap};
  gap: ${container.gap};
}`
})

const copy = async () => {
  await navigator.clipboard.writeText(cssCode.value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.playground { background: #f8f9fa; border-radius: 12px; padding: 1rem; min-height: 200px; }
.container-preview { min-height: 180px; background: white; border: 2px dashed #ddd; border-radius: 8px; padding: 10px; }
.item-preview { min-width: 50px; min-height: 50px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; display: flex; align-items: center; justify-content: center; border-radius: 6px; font-weight: bold; }
.controls h4 { margin: 0 0 0.75rem; color: #333; }
.control-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; }
.control-item { display: flex; flex-direction: column; gap: 0.25rem; }
.control-item label { font-size: 0.8rem; color: #666; }
.control-item select, .control-item input { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
.output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: none; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; align-self: flex-start; }
.btn:hover { border-color: #667eea; color: #667eea; }
@media (max-width: 600px) {
  .control-grid { grid-template-columns: 1fr 1fr; }
}
</style>
