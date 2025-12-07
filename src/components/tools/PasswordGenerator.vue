<template>
  <div class="tool-content">
    <div class="password-display">
      <input v-model="password" readonly class="password-input" />
      <button @click="copy" class="copy-btn">复制</button>
    </div>
    <div class="strength-bar">
      <div class="strength-fill" :style="{ width: strength + '%', background: strengthColor }"></div>
    </div>
    <div class="strength-text">强度: {{ strengthText }}</div>
    
    <div class="settings">
      <div class="setting-item">
        <label>长度: {{ length }}</label>
        <input type="range" v-model.number="length" min="4" max="64" @input="generate" />
      </div>
      <div class="checkboxes">
        <label><input type="checkbox" v-model="options.uppercase" @change="generate" /> 大写字母 (A-Z)</label>
        <label><input type="checkbox" v-model="options.lowercase" @change="generate" /> 小写字母 (a-z)</label>
        <label><input type="checkbox" v-model="options.numbers" @change="generate" /> 数字 (0-9)</label>
        <label><input type="checkbox" v-model="options.symbols" @change="generate" /> 特殊符号 (!@#$...)</label>
      </div>
    </div>
    
    <button @click="generate" class="btn primary full">重新生成</button>
    
    <div class="history" v-if="history.length">
      <label>历史记录:</label>
      <div class="history-list">
        <div v-for="(p, i) in history" :key="i" class="history-item" @click="password = p">{{ p }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'

const password = ref('')
const length = ref(16)
const history = ref<string[]>([])
const options = reactive({
  uppercase: true,
  lowercase: true,
  numbers: true,
  symbols: true
})

const chars = {
  uppercase: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
  lowercase: 'abcdefghijklmnopqrstuvwxyz',
  numbers: '0123456789',
  symbols: '!@#$%^&*()_+-=[]{}|;:,.<>?'
}

const generate = () => {
  let charset = ''
  if (options.uppercase) charset += chars.uppercase
  if (options.lowercase) charset += chars.lowercase
  if (options.numbers) charset += chars.numbers
  if (options.symbols) charset += chars.symbols
  
  if (!charset) {
    options.lowercase = true
    charset = chars.lowercase
  }
  
  let result = ''
  for (let i = 0; i < length.value; i++) {
    result += charset.charAt(Math.floor(Math.random() * charset.length))
  }
  
  if (password.value && !history.value.includes(password.value)) {
    history.value.unshift(password.value)
    if (history.value.length > 5) history.value.pop()
  }
  
  password.value = result
}

const strength = computed(() => {
  let score = 0
  const p = password.value
  if (p.length >= 8) score += 25
  if (p.length >= 12) score += 15
  if (p.length >= 16) score += 10
  if (/[a-z]/.test(p)) score += 10
  if (/[A-Z]/.test(p)) score += 10
  if (/[0-9]/.test(p)) score += 10
  if (/[^a-zA-Z0-9]/.test(p)) score += 20
  return Math.min(100, score)
})

const strengthColor = computed(() => {
  if (strength.value < 30) return '#e74c3c'
  if (strength.value < 60) return '#f39c12'
  if (strength.value < 80) return '#3498db'
  return '#27ae60'
})

const strengthText = computed(() => {
  if (strength.value < 30) return '弱'
  if (strength.value < 60) return '中等'
  if (strength.value < 80) return '强'
  return '非常强'
})

const copy = async () => {
  await navigator.clipboard.writeText(password.value)
}

onMounted(generate)
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.password-display { display: flex; gap: 0.5rem; }
.password-input { flex: 1; padding: 1rem; font-family: monospace; font-size: 1.1rem; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa; }
.copy-btn { padding: 0 1.5rem; border: 1px solid #667eea; border-radius: 8px; background: white; color: #667eea; cursor: pointer; }
.copy-btn:hover { background: #667eea; color: white; }
.strength-bar { height: 6px; background: #eee; border-radius: 3px; overflow: hidden; }
.strength-fill { height: 100%; transition: all 0.3s; }
.strength-text { font-size: 0.9rem; color: #666; }
.settings { display: flex; flex-direction: column; gap: 1rem; }
.setting-item label { font-size: 0.9rem; color: #666; display: block; margin-bottom: 0.5rem; }
.setting-item input[type="range"] { width: 100%; }
.checkboxes { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; }
.checkboxes label { display: flex; align-items: center; gap: 0.5rem; font-size: 0.9rem; cursor: pointer; }
.btn { padding: 0.75rem 1.5rem; border: none; border-radius: 8px; cursor: pointer; }
.btn.primary { background: #667eea; color: white; }
.btn.full { width: 100%; }
.history label { font-size: 0.9rem; color: #666; }
.history-list { display: flex; flex-direction: column; gap: 0.25rem; margin-top: 0.5rem; }
.history-item { padding: 0.5rem; background: #f8f9fa; border-radius: 4px; font-family: monospace; font-size: 0.85rem; cursor: pointer; }
.history-item:hover { background: #e8e9ea; }
</style>
