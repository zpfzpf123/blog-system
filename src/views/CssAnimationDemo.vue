<template>
  <div class="animation-lab">
    <!-- 页面标题 -->
    <header class="lab-header">
      <h1>CSS 动画实验室</h1>
      <p>精选 {{ animations.length }} 个实用动画效果</p>
    </header>

    <!-- 分类筛选 -->
    <nav class="category-nav">
      <button
        v-for="cat in allCategories"
        :key="cat.key"
        :class="['cat-btn', { active: activeCategory === cat.key }]"
        @click="activeCategory = cat.key"
      >
        <span class="cat-icon">{{ cat.icon }}</span>
        <span class="cat-name">{{ cat.label }}</span>
        <span class="cat-count" v-if="cat.key !== 'all'">{{ getCategoryCount(cat.key) }}</span>
      </button>
    </nav>

    <!-- 动画卡片 -->
    <div class="animation-grid" v-if="!loading">
      <div
        v-for="item in displayedAnimations"
        :key="item.id"
        class="anim-card"
        :class="{ playing: playingId === item.id }"
      >
        <!-- 预览区 - 根据分类使用不同容器 -->
        <div class="preview-area" @click="togglePlay(item.id)">
          
          <!-- 加载动画 -->
          <template v-if="item.category === '加载动画'">
            <div class="demo-loader" :class="[getAnimClass(item), { active: playingId === item.id }]">
              <template v-if="item.title.includes('圆点') || item.title.includes('脉冲')">
                <span></span><span></span><span></span>
              </template>
              <template v-else-if="item.title.includes('波浪')">
                <span></span><span></span><span></span><span></span><span></span>
              </template>
              <template v-else-if="item.title.includes('进度')">
                <!-- 进度条容器 -->
              </template>
            </div>
          </template>

          <!-- 按钮特效 -->
          <template v-else-if="item.category === '按钮特效'">
            <button class="demo-button" :class="[getAnimClass(item), { active: playingId === item.id }]">
              点击体验
            </button>
          </template>

          <!-- 文字动画 -->
          <template v-else-if="item.category === '文字动画'">
            <div class="demo-text" :class="[getAnimClass(item), { active: playingId === item.id }]">
              <template v-if="item.title.includes('弹跳') || item.title.includes('波浪')">
                <span>H</span><span>e</span><span>l</span><span>l</span><span>o</span>
              </template>
              <template v-else>
                Hello World
              </template>
            </div>
          </template>

          <!-- 悬停效果 -->
          <template v-else-if="item.category === '悬停效果'">
            <div class="demo-hover-box" :class="[getAnimClass(item), { active: playingId === item.id }]">
              <span>悬停我</span>
            </div>
          </template>

          <!-- 图形变换 -->
          <template v-else-if="item.category === '图形变换'">
            <div class="demo-shape" :class="[getAnimClass(item), { active: playingId === item.id }]"></div>
          </template>

          <!-- 入场动画 -->
          <template v-else-if="item.category === '入场动画'">
            <div 
              class="demo-enter-box" 
              :class="[getAnimClass(item), { active: playingId === item.id }]"
              :key="playingId === item.id ? 'playing' : 'idle'"
            >
              <span>✨</span>
            </div>
          </template>

          <!-- 强调效果 -->
          <template v-else-if="item.category === '强调效果'">
            <div class="demo-emphasis" :class="[getAnimClass(item), { active: playingId === item.id }]">
              <span>🔔</span>
            </div>
          </template>

          <!-- 背景特效 -->
          <template v-else-if="item.category === '背景特效'">
            <div class="demo-background" :class="[getAnimClass(item), { active: playingId === item.id }]"></div>
          </template>

          <!-- 退出动画 -->
          <template v-else-if="item.category === '退出动画'">
            <div 
              class="demo-exit-box" 
              :class="[getAnimClass(item), { active: playingId === item.id }]"
              :key="playingId === item.id ? 'playing' : 'idle'"
            >
              <span>👋</span>
            </div>
          </template>

          <!-- 边框动画 -->
          <template v-else-if="item.category === '边框动画'">
            <div class="demo-border" :class="[getAnimClass(item), { active: playingId === item.id }]"></div>
          </template>

          <!-- 阴影效果 -->
          <template v-else-if="item.category === '阴影效果'">
            <div class="demo-shadow" :class="[getAnimClass(item), { active: playingId === item.id }]"></div>
          </template>

          <!-- 3D特效 -->
          <template v-else-if="item.category === '3D特效'">
            <div class="demo-3d" :class="[getAnimClass(item), { active: playingId === item.id }]"></div>
          </template>

          <!-- 滤镜特效 -->
          <template v-else-if="item.category === '滤镜特效'">
            <div class="demo-filter" :class="[getAnimClass(item), { active: playingId === item.id }]">
              <span>🎭</span>
            </div>
          </template>

          <!-- 变形特效 -->
          <template v-else-if="item.category === '变形特效'">
            <div class="demo-transform" :class="[getAnimClass(item), { active: playingId === item.id }]"></div>
          </template>

          <!-- 默认 -->
          <template v-else>
            <div class="demo-default" :class="[getAnimClass(item), { active: playingId === item.id }]"></div>
          </template>

          <div class="play-indicator">
            <span v-if="playingId === item.id">⏸ 暂停</span>
            <span v-else>▶ 播放</span>
          </div>
        </div>

        <!-- 卡片信息 -->
        <div class="card-info">
          <div class="info-header">
            <h3>{{ item.title }}</h3>
            <span class="category-tag">{{ item.category }}</span>
          </div>
          <p class="description">{{ item.description }}</p>
          <div class="card-actions">
            <button class="action-btn primary" @click.stop="showCodeModal(item)">
              <span>📋</span> 复制代码
            </button>
            <button class="action-btn danger" @click.stop="deleteAnim(item.id)">
              <span>🗑️</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 添加卡片 -->
      <div class="anim-card add-card" @click="openAddModal">
        <div class="add-inner">
          <span class="add-icon">+</span>
          <span>添加新动画</span>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-box">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 代码弹窗 -->
    <Teleport to="body">
      <div v-if="codeModal.show" class="modal-mask" @click.self="codeModal.show = false">
        <div class="code-dialog">
          <div class="dialog-header">
            <h3>{{ codeModal.title }}</h3>
            <button class="close-btn" @click="codeModal.show = false">×</button>
          </div>
          <div class="dialog-body">
            <pre><code>{{ codeModal.code }}</code></pre>
          </div>
          <div class="dialog-footer">
            <button class="copy-btn" @click="copyCode">
              {{ copySuccess ? '✓ 已复制到剪贴板' : '复制代码' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 添加弹窗 -->
    <Teleport to="body">
      <div v-if="addModal.show" class="modal-mask" @click.self="addModal.show = false">
        <div class="form-dialog">
          <div class="dialog-header">
            <h3>添加新动画</h3>
            <button class="close-btn" @click="addModal.show = false">×</button>
          </div>
          <div class="dialog-body">
            <div class="form-field">
              <label>动画名称</label>
              <input v-model="addModal.form.title" placeholder="输入动画名称" />
            </div>
            <div class="form-field">
              <label>分类</label>
              <select v-model="addModal.form.category">
                <option value="">选择分类</option>
                <option v-for="c in categoryList" :key="c" :value="c">{{ c }}</option>
              </select>
            </div>
            <div class="form-field">
              <label>描述</label>
              <input v-model="addModal.form.description" placeholder="简短描述" />
            </div>
            <div class="form-field">
              <label>CSS代码</label>
              <textarea v-model="addModal.form.cssCode" rows="10" placeholder="粘贴CSS代码"></textarea>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="cancel-btn" @click="addModal.show = false">取消</button>
            <button class="save-btn" @click="saveAnimation">保存</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'

interface Animation {
  id: number
  title: string
  category: string
  cssCode: string
  description: string
}

const API = 'http://localhost:4567/api/animations'
const CAT_API = 'http://localhost:4567/api/animation-categories'

const animations = ref<Animation[]>([])
const categories = ref<{ id: number; name: string }[]>([])
const loading = ref(true)
const activeCategory = ref('all')
const playingId = ref<number | null>(null)
const copySuccess = ref(false)

const codeModal = ref({ show: false, title: '', code: '' })
const addModal = ref({
  show: false,
  form: { title: '', category: '', description: '', cssCode: '' }
})

const categoryIcons: Record<string, string> = {
  '悬停效果': '👆',
  '加载动画': '⏳',
  '按钮特效': '🔘',
  '文字动画': '✍️',
  '图形变换': '⬡',
  '入场动画': '🎬',
  '退出动画': '🚪',
  '强调效果': '⚡',
  '背景特效': '🌈',
  '边框动画': '🔲',
  '阴影效果': '🌑',
  '3D特效': '🎲',
  '滤镜特效': '🎭',
  '变形特效': '🔀'
}

const allCategories = computed(() => {
  const cats = categories.value.map(c => ({
    key: c.name,
    label: c.name,
    icon: categoryIcons[c.name] || '✨'
  }))
  return [{ key: 'all', label: '全部', icon: '🎨' }, ...cats]
})

const categoryList = computed(() => categories.value.map(c => c.name))

const displayedAnimations = computed(() => {
  if (activeCategory.value === 'all') return animations.value
  return animations.value.filter(a => a.category === activeCategory.value)
})

function getCategoryCount(cat: string) {
  return animations.value.filter(a => a.category === cat).length
}

function getAnimClass(item: Animation) {
  const match = item.cssCode.match(/\.([\w-]+)\s*\{/)
  return match ? match[1] : ''
}

function togglePlay(id: number) {
  playingId.value = playingId.value === id ? null : id
}

function showCodeModal(item: Animation) {
  codeModal.value = { show: true, title: item.title, code: item.cssCode }
  copySuccess.value = false
}

async function copyCode() {
  try {
    await navigator.clipboard.writeText(codeModal.value.code)
    copySuccess.value = true
    setTimeout(() => (copySuccess.value = false), 2000)
  } catch {
    alert('复制失败')
  }
}

function openAddModal() {
  addModal.value = {
    show: true,
    form: { title: '', category: '', description: '', cssCode: '' }
  }
}

async function saveAnimation() {
  const { title, category, cssCode } = addModal.value.form
  if (!title || !category || !cssCode) {
    alert('请填写完整')
    return
  }
  try {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(addModal.value.form)
    })
    if (res.ok) {
      animations.value.push(await res.json())
      addModal.value.show = false
    }
  } catch (e) {
    console.error(e)
  }
}

async function deleteAnim(id: number) {
  if (!confirm('确定删除？')) return
  try {
    await fetch(`${API}/${id}`, { method: 'DELETE' })
    animations.value = animations.value.filter(a => a.id !== id)
  } catch (e) {
    console.error(e)
  }
}

function injectStyles() {
  let el = document.getElementById('dynamic-css') as HTMLStyleElement
  if (!el) {
    el = document.createElement('style')
    el.id = 'dynamic-css'
    document.head.appendChild(el)
  }
  el.textContent = animations.value.map(a => a.cssCode).join('\n')
}

watch(animations, injectStyles, { deep: true })

onMounted(async () => {
  try {
    const [aRes, cRes] = await Promise.all([fetch(API), fetch(CAT_API)])
    if (aRes.ok) animations.value = await aRes.json()
    if (cRes.ok) categories.value = await cRes.json()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.animation-lab {
  min-height: 100vh;
  background: linear-gradient(135deg, #0c0c1d 0%, #1a1a3e 100%);
  padding: 40px 32px;
  color: #fff;
}

/* 头部 */
.lab-header {
  text-align: center;
  margin-bottom: 40px;
}

.lab-header h1 {
  font-size: 2.8rem;
  font-weight: 800;
  background: linear-gradient(120deg, #a855f7, #ec4899, #06b6d4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px;
}

.lab-header p {
  color: #64748b;
  font-size: 1rem;
}

/* 分类导航 */
.category-nav {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.cat-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 50px;
  color: #94a3b8;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s;
}

.cat-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.cat-btn.active {
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.4);
}

.cat-icon {
  font-size: 1.1rem;
}

.cat-count {
  background: rgba(255, 255, 255, 0.15);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.75rem;
}

/* 动画网格 */
.animation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  max-width: 1600px;
  margin: 0 auto;
}

.anim-card {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s;
}

.anim-card:hover {
  border-color: rgba(139, 92, 246, 0.4);
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.anim-card.playing {
  border-color: #06b6d4;
  box-shadow: 0 0 30px rgba(6, 182, 212, 0.2);
}

/* 预览区 */
.preview-area {
  height: 200px;
  background: linear-gradient(180deg, rgba(139, 92, 246, 0.08) 0%, transparent 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.play-indicator {
  position: absolute;
  bottom: 12px;
  font-size: 0.75rem;
  color: #64748b;
  opacity: 0;
  transition: opacity 0.2s;
}

.preview-area:hover .play-indicator {
  opacity: 1;
}

/* ========== 各类演示容器 ========== */

/* 加载动画容器 */
.demo-loader {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.demo-loader span {
  width: 12px;
  height: 12px;
  background: #8b5cf6;
  border-radius: 50%;
}

/* 按钮容器 */
.demo-button {
  padding: 14px 32px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

/* 文字容器 */
.demo-text {
  font-size: 2rem;
  font-weight: 800;
  color: #fff;
}

.demo-text span {
  display: inline-block;
}

/* 悬停容器 */
.demo-hover-box {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
}

/* 图形容器 */
.demo-shape {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #8b5cf6, #06b6d4);
  border-radius: 16px;
}

/* 入场动画容器 */
.demo-enter-box {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  opacity: 0;
}

.demo-enter-box.active {
  opacity: 1;
}

/* 强调效果容器 */
.demo-emphasis {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f59e0b, #ec4899);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}

/* 背景特效容器 */
.demo-background {
  width: 160px;
  height: 100px;
  border-radius: 12px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
}

/* 退出动画容器 */
.demo-exit-box {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #ef4444, #f97316);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}

.demo-exit-box.active {
  opacity: 0;
}

/* 边框动画容器 */
.demo-border {
  width: 100px;
  height: 100px;
  background: transparent;
  border: 4px solid #8b5cf6;
  border-radius: 16px;
}

/* 阴影效果容器 */
.demo-shadow {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(139, 92, 246, 0.4);
}

/* 3D特效容器 */
.demo-3d {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #06b6d4, #8b5cf6);
  border-radius: 16px;
}

/* 滤镜特效容器 */
.demo-filter {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #f59e0b, #8b5cf6);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
}

/* 变形特效容器 */
.demo-transform {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ec4899, #06b6d4);
  border-radius: 16px;
}

/* 默认容器 */
.demo-default {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border-radius: 16px;
}

/* 卡片信息 */
.card-info {
  padding: 20px;
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.info-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.category-tag {
  font-size: 0.75rem;
  padding: 4px 10px;
  background: rgba(139, 92, 246, 0.15);
  color: #a78bfa;
  border-radius: 20px;
}

.description {
  color: #64748b;
  font-size: 0.85rem;
  margin: 0 0 16px;
  line-height: 1.5;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 10px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.primary {
  flex: 1;
  background: rgba(139, 92, 246, 0.15);
  color: #a78bfa;
}

.action-btn.primary:hover {
  background: rgba(139, 92, 246, 0.25);
}

.action-btn.danger {
  background: rgba(239, 68, 68, 0.1);
  color: #f87171;
}

.action-btn.danger:hover {
  background: rgba(239, 68, 68, 0.2);
}

/* 添加卡片 */
.add-card {
  border-style: dashed;
  cursor: pointer;
  min-height: 300px;
}

.add-card:hover {
  border-color: #8b5cf6;
  background: rgba(139, 92, 246, 0.05);
}

.add-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
  padding: 40px;
}

.add-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  border: 2px dashed #4b5563;
  border-radius: 50%;
  transition: all 0.2s;
}

.add-card:hover .add-icon {
  border-color: #8b5cf6;
  color: #8b5cf6;
}

/* 加载状态 */
.loading-box {
  text-align: center;
  padding: 80px;
  color: #64748b;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(139, 92, 246, 0.2);
  border-top-color: #8b5cf6;
  border-radius: 50%;
  margin: 0 auto 16px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.code-dialog,
.form-dialog {
  background: #1e1e3f;
  border-radius: 20px;
  width: 100%;
  max-width: 640px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.dialog-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.close-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.05);
  border: none;
  border-radius: 50%;
  color: #94a3b8;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.dialog-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.dialog-body pre {
  margin: 0;
  padding: 20px;
  background: #0f0f2a;
  border-radius: 12px;
  overflow-x: auto;
}

.dialog-body code {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 0.85rem;
  color: #e2e8f0;
  white-space: pre-wrap;
  line-height: 1.6;
}

.dialog-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.copy-btn,
.save-btn {
  padding: 12px 28px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.copy-btn:hover,
.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(139, 92, 246, 0.4);
}

.cancel-btn {
  padding: 12px 28px;
  background: rgba(255, 255, 255, 0.05);
  border: none;
  border-radius: 10px;
  color: #94a3b8;
  font-weight: 600;
  cursor: pointer;
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* 表单 */
.form-field {
  margin-bottom: 20px;
}

.form-field label {
  display: block;
  margin-bottom: 8px;
  font-size: 0.9rem;
  color: #94a3b8;
}

.form-field input,
.form-field select,
.form-field textarea {
  width: 100%;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  color: #fff;
  font-size: 0.95rem;
  font-family: inherit;
  box-sizing: border-box;
}

.form-field input:focus,
.form-field select:focus,
.form-field textarea:focus {
  outline: none;
  border-color: #8b5cf6;
}

.form-field textarea {
  resize: vertical;
  font-family: 'Fira Code', monospace;
  font-size: 0.85rem;
}

/* 响应式 */
@media (max-width: 768px) {
  .animation-lab {
    padding: 24px 16px;
  }

  .lab-header h1 {
    font-size: 2rem;
  }

  .animation-grid {
    grid-template-columns: 1fr;
  }

  .category-nav {
    gap: 8px;
  }

  .cat-btn {
    padding: 8px 14px;
    font-size: 0.85rem;
  }
}
</style>
