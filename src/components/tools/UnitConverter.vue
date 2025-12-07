<template>
  <div class="tool-content">
    <div class="category-tabs">
      <button v-for="cat in categories" :key="cat.id" :class="{ active: activeCategory === cat.id }" @click="activeCategory = cat.id; reset()">
        {{ cat.icon }} {{ cat.name }}
      </button>
    </div>
    
    <div class="converter">
      <div class="input-row">
        <input v-model.number="fromValue" type="number" @input="convert('from')" />
        <select v-model="fromUnit" @change="convert('from')">
          <option v-for="u in currentUnits" :key="u.id" :value="u.id">{{ u.name }}</option>
        </select>
      </div>
      <div class="swap-btn" @click="swap">⇅</div>
      <div class="input-row">
        <input v-model.number="toValue" type="number" @input="convert('to')" />
        <select v-model="toUnit" @change="convert('from')">
          <option v-for="u in currentUnits" :key="u.id" :value="u.id">{{ u.name }}</option>
        </select>
      </div>
    </div>
    
    <div class="formula" v-if="formula">{{ formula }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const activeCategory = ref('length')
const fromValue = ref<number>(1)
const toValue = ref<number>(0)
const fromUnit = ref('')
const toUnit = ref('')
const formula = ref('')

const categories = [
  { id: 'length', name: '长度', icon: '📏' },
  { id: 'weight', name: '重量', icon: '⚖️' },
  { id: 'area', name: '面积', icon: '⬜' },
  { id: 'volume', name: '体积', icon: '📦' },
  { id: 'temperature', name: '温度', icon: '🌡️' },
  { id: 'data', name: '数据', icon: '💾' },
]

const units: Record<string, Array<{ id: string; name: string; ratio: number }>> = {
  length: [
    { id: 'mm', name: '毫米', ratio: 0.001 },
    { id: 'cm', name: '厘米', ratio: 0.01 },
    { id: 'm', name: '米', ratio: 1 },
    { id: 'km', name: '千米', ratio: 1000 },
    { id: 'in', name: '英寸', ratio: 0.0254 },
    { id: 'ft', name: '英尺', ratio: 0.3048 },
    { id: 'mi', name: '英里', ratio: 1609.344 },
  ],
  weight: [
    { id: 'mg', name: '毫克', ratio: 0.000001 },
    { id: 'g', name: '克', ratio: 0.001 },
    { id: 'kg', name: '千克', ratio: 1 },
    { id: 't', name: '吨', ratio: 1000 },
    { id: 'oz', name: '盎司', ratio: 0.0283495 },
    { id: 'lb', name: '磅', ratio: 0.453592 },
  ],
  area: [
    { id: 'mm2', name: '平方毫米', ratio: 0.000001 },
    { id: 'cm2', name: '平方厘米', ratio: 0.0001 },
    { id: 'm2', name: '平方米', ratio: 1 },
    { id: 'km2', name: '平方千米', ratio: 1000000 },
    { id: 'ha', name: '公顷', ratio: 10000 },
    { id: 'acre', name: '英亩', ratio: 4046.86 },
  ],
  volume: [
    { id: 'ml', name: '毫升', ratio: 0.001 },
    { id: 'l', name: '升', ratio: 1 },
    { id: 'm3', name: '立方米', ratio: 1000 },
    { id: 'gal', name: '加仑', ratio: 3.78541 },
  ],
  temperature: [
    { id: 'c', name: '摄氏度', ratio: 1 },
    { id: 'f', name: '华氏度', ratio: 1 },
    { id: 'k', name: '开尔文', ratio: 1 },
  ],
  data: [
    { id: 'b', name: '字节', ratio: 1 },
    { id: 'kb', name: 'KB', ratio: 1024 },
    { id: 'mb', name: 'MB', ratio: 1048576 },
    { id: 'gb', name: 'GB', ratio: 1073741824 },
    { id: 'tb', name: 'TB', ratio: 1099511627776 },
  ],
}

const currentUnits = computed(() => units[activeCategory.value] || [])

const reset = () => {
  fromValue.value = 1
  toValue.value = 0
  fromUnit.value = currentUnits.value[0]?.id || ''
  toUnit.value = currentUnits.value[1]?.id || ''
  convert('from')
}

const convert = (direction: 'from' | 'to') => {
  if (activeCategory.value === 'temperature') {
    convertTemperature(direction)
    return
  }
  
  const fromU = currentUnits.value.find(u => u.id === fromUnit.value)
  const toU = currentUnits.value.find(u => u.id === toUnit.value)
  if (!fromU || !toU) return
  
  if (direction === 'from') {
    toValue.value = Number(((fromValue.value * fromU.ratio) / toU.ratio).toPrecision(10))
  } else {
    fromValue.value = Number(((toValue.value * toU.ratio) / fromU.ratio).toPrecision(10))
  }
  
  formula.value = `${fromValue.value} ${fromU.name} = ${toValue.value} ${toU.name}`
}

const convertTemperature = (direction: 'from' | 'to') => {
  const from = direction === 'from' ? fromUnit.value : toUnit.value
  const to = direction === 'from' ? toUnit.value : fromUnit.value
  const value = direction === 'from' ? fromValue.value : toValue.value
  
  let result: number
  // 先转为摄氏度
  let celsius = value
  if (from === 'f') celsius = (value - 32) * 5 / 9
  if (from === 'k') celsius = value - 273.15
  
  // 再转为目标单位
  if (to === 'c') result = celsius
  else if (to === 'f') result = celsius * 9 / 5 + 32
  else result = celsius + 273.15
  
  if (direction === 'from') {
    toValue.value = Number(result.toFixed(2))
  } else {
    fromValue.value = Number(result.toFixed(2))
  }
}

const swap = () => {
  const temp = fromUnit.value
  fromUnit.value = toUnit.value
  toUnit.value = temp
  convert('from')
}

reset()
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.category-tabs { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.category-tabs button { padding: 0.5rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; background: white; cursor: pointer; font-size: 0.9rem; }
.category-tabs button.active { background: #667eea; color: white; border-color: #667eea; }
.converter { display: flex; flex-direction: column; gap: 0.5rem; align-items: center; }
.input-row { display: flex; gap: 0.5rem; width: 100%; }
.input-row input { flex: 1; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-size: 1.1rem; }
.input-row input:focus { outline: none; border-color: #667eea; }
.input-row select { padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; min-width: 120px; }
.swap-btn { width: 40px; height: 40px; border-radius: 50%; background: #667eea; color: white; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 1.2rem; }
.formula { text-align: center; padding: 0.75rem; background: #f8f9fa; border-radius: 8px; color: #666; }
</style>
