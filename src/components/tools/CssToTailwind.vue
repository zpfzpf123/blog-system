<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入CSS:</label>
      <textarea v-model="input" placeholder="display: flex;
justify-content: center;
align-items: center;
padding: 16px;
margin: 8px;
background-color: #3b82f6;
border-radius: 8px;" rows="8" @input="convert"></textarea>
    </div>
    <div v-if="warnings.length" class="warnings">
      <div v-for="(w, i) in warnings" :key="i" class="warning-item">⚠️ {{ w }}</div>
    </div>
    <div class="output-group">
      <label>Tailwind Classes:</label>
      <div class="tailwind-output">{{ output || '输入CSS后自动转换' }}</div>
      <button @click="copy" class="btn" :disabled="!output">复制</button>
    </div>
    <div class="reference">
      <h4>常用对照表</h4>
      <div class="ref-grid">
        <div v-for="(item, i) in references" :key="i" class="ref-item">
          <code>{{ item.css }}</code>
          <span>→</span>
          <code class="tw">{{ item.tw }}</code>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const input = ref('')
const output = ref('')
const warnings = ref<string[]>([])

const cssToTailwindMap: Record<string, (v: string) => string> = {
  'display': (v) => ({ flex: 'flex', block: 'block', inline: 'inline', 'inline-block': 'inline-block', grid: 'grid', none: 'hidden', 'inline-flex': 'inline-flex' }[v] || ''),
  'justify-content': (v) => ({ 'flex-start': 'justify-start', 'flex-end': 'justify-end', center: 'justify-center', 'space-between': 'justify-between', 'space-around': 'justify-around', 'space-evenly': 'justify-evenly' }[v] || ''),
  'align-items': (v) => ({ 'flex-start': 'items-start', 'flex-end': 'items-end', center: 'items-center', baseline: 'items-baseline', stretch: 'items-stretch' }[v] || ''),
  'flex-direction': (v) => ({ row: 'flex-row', 'row-reverse': 'flex-row-reverse', column: 'flex-col', 'column-reverse': 'flex-col-reverse' }[v] || ''),
  'flex-wrap': (v) => ({ wrap: 'flex-wrap', nowrap: 'flex-nowrap', 'wrap-reverse': 'flex-wrap-reverse' }[v] || ''),
  'position': (v) => ({ relative: 'relative', absolute: 'absolute', fixed: 'fixed', sticky: 'sticky', static: 'static' }[v] || ''),
  'text-align': (v) => ({ left: 'text-left', center: 'text-center', right: 'text-right', justify: 'text-justify' }[v] || ''),
  'font-weight': (v) => {
    const map: Record<string, string> = { '100': 'font-thin', '200': 'font-extralight', '300': 'font-light', '400': 'font-normal', '500': 'font-medium', '600': 'font-semibold', '700': 'font-bold', '800': 'font-extrabold', '900': 'font-black', bold: 'font-bold', normal: 'font-normal' }
    return map[v] || ''
  },
  'overflow': (v) => ({ hidden: 'overflow-hidden', auto: 'overflow-auto', scroll: 'overflow-scroll', visible: 'overflow-visible' }[v] || ''),
  'cursor': (v) => ({ pointer: 'cursor-pointer', default: 'cursor-default', 'not-allowed': 'cursor-not-allowed', wait: 'cursor-wait', text: 'cursor-text', move: 'cursor-move' }[v] || ''),
}

const sizeMap: Record<string, string> = {
  '0': '0', '1px': 'px', '2px': '0.5', '4px': '1', '6px': '1.5', '8px': '2', '10px': '2.5', '12px': '3', '14px': '3.5', '16px': '4', '20px': '5', '24px': '6', '28px': '7', '32px': '8', '36px': '9', '40px': '10', '44px': '11', '48px': '12', '56px': '14', '64px': '16', '80px': '20', '96px': '24', '100%': 'full', '50%': '1/2', 'auto': 'auto'
}

const convert = () => {
  warnings.value = []
  const classes: string[] = []
  
  const lines = input.value.split(/[;\n]/).filter(l => l.trim())
  
  for (const line of lines) {
    const [prop, ...valueParts] = line.split(':')
    if (!prop || !valueParts.length) continue
    
    const property = prop.trim().toLowerCase()
    const value = valueParts.join(':').trim()
    
    // 直接映射
    if (cssToTailwindMap[property]) {
      const tw = cssToTailwindMap[property](value)
      if (tw) classes.push(tw)
      else warnings.value.push(`${property}: ${value} 无法转换`)
      continue
    }
    
    // 尺寸类属性
    if (['padding', 'margin', 'width', 'height', 'gap'].includes(property)) {
      const prefix = { padding: 'p', margin: 'm', width: 'w', height: 'h', gap: 'gap' }[property]
      const size = sizeMap[value] || value.replace('px', '')
      classes.push(`${prefix}-${size}`)
      continue
    }
    
    // padding/margin 方向
    const dirMatch = property.match(/^(padding|margin)-(top|right|bottom|left)$/)
    if (dirMatch) {
      const prefix = dirMatch[1] === 'padding' ? 'p' : 'm'
      const dir = { top: 't', right: 'r', bottom: 'b', left: 'l' }[dirMatch[2]]
      const size = sizeMap[value] || value.replace('px', '')
      classes.push(`${prefix}${dir}-${size}`)
      continue
    }
    
    // border-radius
    if (property === 'border-radius') {
      const radiusMap: Record<string, string> = { '0': 'rounded-none', '2px': 'rounded-sm', '4px': 'rounded', '6px': 'rounded-md', '8px': 'rounded-lg', '12px': 'rounded-xl', '16px': 'rounded-2xl', '24px': 'rounded-3xl', '9999px': 'rounded-full', '50%': 'rounded-full' }
      classes.push(radiusMap[value] || `rounded-[${value}]`)
      continue
    }
    
    // 颜色
    if (property === 'background-color' || property === 'background') {
      if (value.startsWith('#') || value.startsWith('rgb')) {
        classes.push(`bg-[${value}]`)
      }
      continue
    }
    
    if (property === 'color') {
      if (value.startsWith('#') || value.startsWith('rgb')) {
        classes.push(`text-[${value}]`)
      }
      continue
    }
    
    warnings.value.push(`${property}: ${value} 无法转换`)
  }
  
  output.value = classes.join(' ')
}

const references = [
  { css: 'display: flex', tw: 'flex' },
  { css: 'justify-content: center', tw: 'justify-center' },
  { css: 'align-items: center', tw: 'items-center' },
  { css: 'padding: 16px', tw: 'p-4' },
  { css: 'margin: 8px', tw: 'm-2' },
  { css: 'border-radius: 8px', tw: 'rounded-lg' },
  { css: 'width: 100%', tw: 'w-full' },
  { css: 'position: relative', tw: 'relative' },
]

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group, .output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.warnings { display: flex; flex-direction: column; gap: 0.25rem; }
.warning-item { font-size: 0.85rem; color: #b45309; padding: 0.25rem 0.5rem; background: #fef3c7; border-radius: 4px; }
.tailwind-output { padding: 1rem; background: #1e293b; color: #38bdf8; border-radius: 8px; font-family: monospace; min-height: 60px; word-break: break-all; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; align-self: flex-start; }
.btn:hover:not(:disabled) { border-color: #667eea; color: #667eea; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.reference h4 { margin: 0 0 0.75rem; font-size: 0.95rem; }
.ref-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0.5rem; }
.ref-item { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; padding: 0.5rem; background: #f8f9fa; border-radius: 4px; }
.ref-item code { background: #e2e8f0; padding: 0.15rem 0.4rem; border-radius: 3px; }
.ref-item code.tw { background: #1e293b; color: #38bdf8; }
</style>
