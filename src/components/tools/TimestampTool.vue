<template>
  <div class="tool-content">
    <div class="current-time">
      <span class="label">当前时间戳:</span>
      <span class="timestamp">{{ currentTimestamp }}</span>
      <button @click="copyValue(currentTimestamp.toString())" class="copy-btn">复制</button>
    </div>
    
    <div class="section">
      <h4>时间戳 → 日期</h4>
      <div class="input-row">
        <input v-model="timestampInput" type="text" placeholder="输入时间戳 (秒或毫秒)" />
        <button @click="toDate" class="btn primary">转换</button>
      </div>
      <div v-if="dateResult" class="result">{{ dateResult }}</div>
    </div>
    
    <div class="section">
      <h4>日期 → 时间戳</h4>
      <div class="input-row">
        <input v-model="dateInput" type="datetime-local" />
        <button @click="toTimestamp" class="btn primary">转换</button>
      </div>
      <div v-if="timestampResult" class="result">
        <div>秒: {{ timestampResult.seconds }} <button @click="copyValue(timestampResult.seconds.toString())" class="copy-sm">复制</button></div>
        <div>毫秒: {{ timestampResult.milliseconds }} <button @click="copyValue(timestampResult.milliseconds.toString())" class="copy-sm">复制</button></div>
      </div>
    </div>
    
    <div class="quick-times">
      <span class="label">快捷时间:</span>
      <button v-for="t in quickTimes" :key="t.label" @click="applyQuickTime(t)" class="quick-btn">{{ t.label }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const currentTimestamp = ref(Math.floor(Date.now() / 1000))
const timestampInput = ref('')
const dateInput = ref('')
const dateResult = ref('')
const timestampResult = ref<{ seconds: number; milliseconds: number } | null>(null)

let timer: number

const quickTimes = [
  { label: '今天0点', getValue: () => new Date().setHours(0, 0, 0, 0) },
  { label: '明天0点', getValue: () => new Date().setHours(24, 0, 0, 0) },
  { label: '本周一', getValue: () => { const d = new Date(); d.setDate(d.getDate() - d.getDay() + 1); d.setHours(0, 0, 0, 0); return d.getTime() } },
  { label: '本月1号', getValue: () => new Date(new Date().getFullYear(), new Date().getMonth(), 1).getTime() },
]

onMounted(() => {
  timer = window.setInterval(() => {
    currentTimestamp.value = Math.floor(Date.now() / 1000)
  }, 1000)
  
  const now = new Date()
  dateInput.value = now.toISOString().slice(0, 16)
})

onUnmounted(() => {
  clearInterval(timer)
})

const toDate = () => {
  const ts = parseInt(timestampInput.value)
  if (isNaN(ts)) {
    dateResult.value = '请输入有效的时间戳'
    return
  }
  
  // 自动判断秒还是毫秒
  const date = new Date(ts > 9999999999 ? ts : ts * 1000)
  dateResult.value = date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    weekday: 'long'
  })
}

const toTimestamp = () => {
  const date = new Date(dateInput.value)
  if (isNaN(date.getTime())) {
    timestampResult.value = null
    return
  }
  timestampResult.value = {
    seconds: Math.floor(date.getTime() / 1000),
    milliseconds: date.getTime()
  }
}

const applyQuickTime = (t: { getValue: () => number }) => {
  const ms = t.getValue()
  timestampInput.value = Math.floor(ms / 1000).toString()
  toDate()
}

const copyValue = async (value: string) => {
  await navigator.clipboard.writeText(value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1.5rem; }
.current-time { display: flex; align-items: center; gap: 1rem; padding: 1rem; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 8px; color: white; }
.current-time .label { font-size: 0.9rem; }
.current-time .timestamp { font-size: 1.5rem; font-weight: bold; font-family: monospace; }
.current-time .copy-btn { padding: 0.25rem 0.75rem; background: rgba(255,255,255,0.2); border: none; color: white; border-radius: 4px; cursor: pointer; }
.section h4 { margin: 0 0 0.75rem; color: #333; }
.input-row { display: flex; gap: 0.5rem; }
.input-row input { flex: 1; padding: 0.75rem; border: 1px solid #ddd; border-radius: 6px; }
.input-row input:focus { outline: none; border-color: #667eea; }
.btn { padding: 0.75rem 1.5rem; border: none; border-radius: 6px; cursor: pointer; }
.btn.primary { background: #667eea; color: white; }
.result { margin-top: 0.75rem; padding: 1rem; background: #f8f9fa; border-radius: 6px; font-family: monospace; }
.result div { margin: 0.25rem 0; display: flex; align-items: center; gap: 0.5rem; }
.copy-sm { padding: 0.15rem 0.5rem; border: 1px solid #ddd; border-radius: 4px; background: white; cursor: pointer; font-size: 0.8rem; }
.quick-times { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
.quick-times .label { color: #666; font-size: 0.9rem; }
.quick-btn { padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 15px; background: white; cursor: pointer; }
.quick-btn:hover { border-color: #667eea; color: #667eea; }
</style>
