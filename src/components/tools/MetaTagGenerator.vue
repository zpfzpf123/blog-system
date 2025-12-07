<template>
  <div class="tool-content">
    <div class="form-section">
      <h4>基本信息</h4>
      <div class="form-grid">
        <div class="form-item">
          <label>页面标题:</label>
          <input v-model="meta.title" placeholder="网站标题" @input="generate" />
        </div>
        <div class="form-item">
          <label>页面描述:</label>
          <textarea v-model="meta.description" placeholder="网站描述 (建议150字以内)" rows="2" @input="generate"></textarea>
        </div>
        <div class="form-item">
          <label>关键词:</label>
          <input v-model="meta.keywords" placeholder="关键词1, 关键词2, 关键词3" @input="generate" />
        </div>
        <div class="form-item">
          <label>作者:</label>
          <input v-model="meta.author" placeholder="作者名称" @input="generate" />
        </div>
      </div>
    </div>
    
    <div class="form-section">
      <h4>Open Graph (社交分享)</h4>
      <div class="form-grid">
        <div class="form-item">
          <label>OG标题:</label>
          <input v-model="meta.ogTitle" placeholder="分享标题" @input="generate" />
        </div>
        <div class="form-item">
          <label>OG图片:</label>
          <input v-model="meta.ogImage" placeholder="https://example.com/image.jpg" @input="generate" />
        </div>
        <div class="form-item">
          <label>网站URL:</label>
          <input v-model="meta.ogUrl" placeholder="https://example.com" @input="generate" />
        </div>
        <div class="form-item">
          <label>类型:</label>
          <select v-model="meta.ogType" @change="generate">
            <option value="website">网站</option>
            <option value="article">文章</option>
            <option value="product">产品</option>
          </select>
        </div>
      </div>
    </div>
    
    <div class="output-group">
      <label>生成的Meta标签:</label>
      <textarea v-model="output" readonly rows="12"></textarea>
      <button @click="copy" class="btn">复制代码</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

const meta = reactive({
  title: '',
  description: '',
  keywords: '',
  author: '',
  ogTitle: '',
  ogImage: '',
  ogUrl: '',
  ogType: 'website'
})

const output = ref('')

const generate = () => {
  const tags: string[] = []
  
  if (meta.title) {
    tags.push(`<title>${meta.title}</title>`)
  }
  if (meta.description) {
    tags.push(`<meta name="description" content="${meta.description}">`)
  }
  if (meta.keywords) {
    tags.push(`<meta name="keywords" content="${meta.keywords}">`)
  }
  if (meta.author) {
    tags.push(`<meta name="author" content="${meta.author}">`)
  }
  
  tags.push('')
  tags.push('<!-- Open Graph / Facebook -->')
  tags.push(`<meta property="og:type" content="${meta.ogType}">`)
  if (meta.ogUrl) tags.push(`<meta property="og:url" content="${meta.ogUrl}">`)
  if (meta.ogTitle || meta.title) tags.push(`<meta property="og:title" content="${meta.ogTitle || meta.title}">`)
  if (meta.description) tags.push(`<meta property="og:description" content="${meta.description}">`)
  if (meta.ogImage) tags.push(`<meta property="og:image" content="${meta.ogImage}">`)
  
  tags.push('')
  tags.push('<!-- Twitter -->')
  tags.push('<meta property="twitter:card" content="summary_large_image">')
  if (meta.ogUrl) tags.push(`<meta property="twitter:url" content="${meta.ogUrl}">`)
  if (meta.ogTitle || meta.title) tags.push(`<meta property="twitter:title" content="${meta.ogTitle || meta.title}">`)
  if (meta.description) tags.push(`<meta property="twitter:description" content="${meta.description}">`)
  if (meta.ogImage) tags.push(`<meta property="twitter:image" content="${meta.ogImage}">`)
  
  output.value = tags.join('\n')
}

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}

onMounted(generate)
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1.5rem; }
.form-section h4 { margin: 0 0 0.75rem; color: #333; font-size: 1rem; }
.form-grid { display: flex; flex-direction: column; gap: 0.75rem; }
.form-item { display: flex; flex-direction: column; gap: 0.25rem; }
.form-item label { font-size: 0.85rem; color: #666; }
.form-item input, .form-item textarea, .form-item select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
.form-item input:focus, .form-item textarea:focus { outline: none; border-color: #667eea; }
.output-group { display: flex; flex-direction: column; gap: 0.5rem; }
.output-group label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; font-size: 0.85rem; resize: vertical; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; align-self: flex-start; }
.btn:hover { border-color: #667eea; color: #667eea; }
</style>
