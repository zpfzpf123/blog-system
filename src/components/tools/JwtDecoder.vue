<template>
  <div class="tool-content">
    <div class="input-group">
      <label>JWT Token:</label>
      <textarea v-model="token" placeholder="粘贴JWT Token..." rows="4" @input="decode"></textarea>
    </div>
    <div v-if="error" class="error-msg">{{ error }}</div>
    <div v-if="decoded" class="decoded-sections">
      <div class="section header">
        <h4>Header (头部)</h4>
        <pre>{{ decoded.header }}</pre>
      </div>
      <div class="section payload">
        <h4>Payload (载荷)</h4>
        <pre>{{ decoded.payload }}</pre>
        <div v-if="claims.length" class="claims">
          <div v-for="claim in claims" :key="claim.key" class="claim-item">
            <span class="claim-key">{{ claim.key }}:</span>
            <span class="claim-value">{{ claim.value }}</span>
          </div>
        </div>
      </div>
      <div class="section signature">
        <h4>Signature (签名)</h4>
        <code>{{ decoded.signature }}</code>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const token = ref('')
const error = ref('')
const decoded = ref<{ header: string; payload: string; signature: string } | null>(null)
const claims = reactive<Array<{ key: string; value: string }>>([])

const decode = () => {
  error.value = ''
  decoded.value = null
  claims.length = 0
  
  if (!token.value.trim()) return
  
  const parts = token.value.trim().split('.')
  if (parts.length !== 3) {
    error.value = 'JWT格式错误，应包含3个部分'
    return
  }
  
  try {
    const header = JSON.parse(atob(parts[0]))
    const payload = JSON.parse(atob(parts[1]))
    
    decoded.value = {
      header: JSON.stringify(header, null, 2),
      payload: JSON.stringify(payload, null, 2),
      signature: parts[2]
    }
    
    // 解析常见claims
    const claimNames: Record<string, string> = {
      iss: '签发者',
      sub: '主题',
      aud: '受众',
      exp: '过期时间',
      nbf: '生效时间',
      iat: '签发时间',
      jti: 'JWT ID'
    }
    
    Object.entries(payload).forEach(([key, value]) => {
      let displayValue = String(value)
      if (['exp', 'nbf', 'iat'].includes(key) && typeof value === 'number') {
        const date = new Date(value * 1000)
        displayValue = `${value} (${date.toLocaleString('zh-CN')})`
      }
      claims.push({
        key: claimNames[key] || key,
        value: displayValue
      })
    })
  } catch (e) {
    error.value = '解析失败，请确保输入有效的JWT'
  }
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; font-size: 0.85rem; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.error-msg { padding: 0.75rem; background: #fee; color: #c00; border-radius: 6px; }
.decoded-sections { display: flex; flex-direction: column; gap: 1rem; }
.section { padding: 1rem; border-radius: 8px; }
.section h4 { margin: 0 0 0.5rem; font-size: 0.9rem; }
.section pre { margin: 0; font-size: 0.85rem; white-space: pre-wrap; word-break: break-all; }
.section code { font-size: 0.8rem; word-break: break-all; }
.header { background: #fff3cd; }
.header h4 { color: #856404; }
.payload { background: #d4edda; }
.payload h4 { color: #155724; }
.signature { background: #cce5ff; }
.signature h4 { color: #004085; }
.claims { margin-top: 1rem; padding-top: 1rem; border-top: 1px solid rgba(0,0,0,0.1); }
.claim-item { display: flex; gap: 0.5rem; padding: 0.25rem 0; font-size: 0.85rem; }
.claim-key { font-weight: 600; min-width: 80px; }
.claim-value { color: #333; }
</style>
