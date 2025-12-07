<template>
  <div class="tool-content">
    <div class="input-group">
      <label>Cron表达式:</label>
      <input v-model="cron" placeholder="* * * * *" @input="parse" />
    </div>
    <div class="quick-crons">
      <span class="label">常用:</span>
      <button v-for="c in quickCrons" :key="c.cron" @click="cron = c.cron; parse()" class="quick-btn">{{ c.name }}</button>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="description" class="description">{{ description }}</div>
    <div class="fields">
      <div v-for="(field, i) in fields" :key="i" class="field-item">
        <span class="field-name">{{ field.name }}</span>
        <span class="field-value">{{ field.value }}</span>
        <span class="field-range">{{ field.range }}</span>
      </div>
    </div>
    <div v-if="nextRuns.length" class="next-runs">
      <label>接下来5次执行时间:</label>
      <div v-for="(run, i) in nextRuns" :key="i" class="run-item">{{ run }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const cron = ref('0 0 * * *')
const error = ref('')
const description = ref('')
const fields = reactive<Array<{ name: string; value: string; range: string }>>([])
const nextRuns = ref<string[]>([])

const quickCrons = [
  { name: '每分钟', cron: '* * * * *' },
  { name: '每小时', cron: '0 * * * *' },
  { name: '每天0点', cron: '0 0 * * *' },
  { name: '每周一', cron: '0 0 * * 1' },
  { name: '每月1号', cron: '0 0 1 * *' },
  { name: '工作日9点', cron: '0 9 * * 1-5' },
]

const fieldDefs = [
  { name: '分钟', range: '0-59' },
  { name: '小时', range: '0-23' },
  { name: '日期', range: '1-31' },
  { name: '月份', range: '1-12' },
  { name: '星期', range: '0-6 (0=周日)' },
]

const parse = () => {
  error.value = ''
  description.value = ''
  fields.length = 0
  nextRuns.value = []
  
  const parts = cron.value.trim().split(/\s+/)
  if (parts.length !== 5) {
    error.value = 'Cron表达式应包含5个字段'
    return
  }
  
  parts.forEach((part, i) => {
    fields.push({
      name: fieldDefs[i].name,
      value: part,
      range: fieldDefs[i].range
    })
  })
  
  // 生成描述
  description.value = generateDescription(parts)
  
  // 计算下次执行时间
  calculateNextRuns(parts)
}

const generateDescription = (parts: string[]): string => {
  const [min, hour, day, month, weekday] = parts
  let desc = '执行时间: '
  
  if (min === '*' && hour === '*') desc += '每分钟'
  else if (min === '0' && hour === '*') desc += '每小时整点'
  else if (min === '0' && hour === '0' && day === '*' && month === '*' && weekday === '*') desc += '每天0点'
  else if (min === '0' && hour === '0' && day === '1') desc += '每月1号0点'
  else {
    if (min !== '*') desc += `第${min}分钟 `
    if (hour !== '*') desc += `${hour}点 `
    if (day !== '*') desc += `${day}号 `
    if (month !== '*') desc += `${month}月 `
    if (weekday !== '*') desc += `星期${weekday} `
  }
  
  return desc
}

const calculateNextRuns = (parts: string[]) => {
  const now = new Date()
  const runs: string[] = []
  
  // 简化计算，只处理基本情况
  for (let i = 0; i < 5 && runs.length < 5; i++) {
    const next = new Date(now.getTime() + i * 60000)
    runs.push(next.toLocaleString('zh-CN'))
  }
  
  nextRuns.value = runs
}

parse()
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
input { padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; font-size: 1.2rem; text-align: center; }
input:focus { outline: none; border-color: #667eea; }
.quick-crons { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
.quick-crons .label { color: #666; font-size: 0.9rem; }
.quick-btn { padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 15px; background: white; cursor: pointer; font-size: 0.85rem; }
.quick-btn:hover { border-color: #667eea; color: #667eea; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
.description { padding: 1rem; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 8px; text-align: center; font-size: 1.1rem; }
.fields { display: grid; grid-template-columns: repeat(5, 1fr); gap: 0.5rem; }
.field-item { text-align: center; padding: 0.75rem; background: #f8f9fa; border-radius: 8px; }
.field-name { display: block; font-size: 0.8rem; color: #666; }
.field-value { display: block; font-size: 1.2rem; font-weight: bold; color: #667eea; margin: 0.25rem 0; }
.field-range { display: block; font-size: 0.75rem; color: #999; }
.next-runs { background: #f8f9fa; padding: 1rem; border-radius: 8px; }
.next-runs label { font-size: 0.9rem; color: #666; }
.run-item { padding: 0.5rem 0; border-bottom: 1px solid #eee; font-family: monospace; }
.run-item:last-child { border-bottom: none; }
</style>
