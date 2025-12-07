<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入金额 (数字):</label>
      <input v-model="amount" type="text" placeholder="例如: 12345.67" @input="convert" />
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div class="result-box" v-if="result">
      <label>大写金额:</label>
      <div class="result-text">{{ result }}</div>
      <button @click="copy" class="copy-btn">复制</button>
    </div>
    <div class="examples">
      <span class="label">示例:</span>
      <button v-for="ex in examples" :key="ex" @click="amount = ex; convert()" class="example-btn">{{ ex }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const amount = ref('')
const result = ref('')
const error = ref('')
const examples = ['100', '1234.56', '10000', '123456789.01']

const convert = () => {
  error.value = ''
  result.value = ''
  if (!amount.value) return
  
  const num = parseFloat(amount.value.replace(/,/g, ''))
  if (isNaN(num)) {
    error.value = '请输入有效的数字'
    return
  }
  if (num < 0) {
    error.value = '金额不能为负数'
    return
  }
  if (num > 999999999999.99) {
    error.value = '金额超出范围'
    return
  }
  
  result.value = numberToChinese(num)
}

const numberToChinese = (num: number): string => {
  const digits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const units = ['', '拾', '佰', '仟']
  const bigUnits = ['', '万', '亿']
  
  if (num === 0) return '零元整'
  
  const [intPart, decPart] = num.toFixed(2).split('.')
  let result = ''
  
  // 处理整数部分
  const intStr = intPart.replace(/^0+/, '')
  if (intStr) {
    const groups: string[] = []
    let temp = intStr
    while (temp.length > 0) {
      groups.unshift(temp.slice(-4))
      temp = temp.slice(0, -4)
    }
    
    groups.forEach((group, gIdx) => {
      const groupIdx = groups.length - 1 - gIdx
      let groupResult = ''
      let hasZero = false
      
      for (let i = 0; i < group.length; i++) {
        const digit = parseInt(group[i])
        const unitIdx = group.length - 1 - i
        
        if (digit === 0) {
          hasZero = true
        } else {
          if (hasZero) {
            groupResult += '零'
            hasZero = false
          }
          groupResult += digits[digit] + units[unitIdx]
        }
      }
      
      if (groupResult) {
        result += groupResult + bigUnits[groupIdx]
      }
    })
    result += '元'
  }
  
  // 处理小数部分
  const jiao = parseInt(decPart[0])
  const fen = parseInt(decPart[1])
  
  if (jiao === 0 && fen === 0) {
    result += '整'
  } else {
    if (jiao > 0) {
      result += digits[jiao] + '角'
    } else if (intStr) {
      result += '零'
    }
    if (fen > 0) {
      result += digits[fen] + '分'
    }
  }
  
  return result
}

const copy = async () => {
  await navigator.clipboard.writeText(result.value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
input { padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-size: 1.1rem; }
input:focus { outline: none; border-color: #667eea; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
.result-box { background: #f8f9fa; padding: 1rem; border-radius: 8px; position: relative; }
.result-text { font-size: 1.3rem; color: #e74c3c; font-weight: 600; margin-top: 0.5rem; word-break: break-all; }
.copy-btn { position: absolute; top: 1rem; right: 1rem; padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 4px; background: white; cursor: pointer; }
.copy-btn:hover { border-color: #667eea; color: #667eea; }
.examples { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; }
.examples .label { color: #666; font-size: 0.9rem; }
.example-btn { padding: 0.25rem 0.75rem; border: 1px solid #ddd; border-radius: 15px; background: white; cursor: pointer; }
.example-btn:hover { border-color: #667eea; color: #667eea; }
</style>
