<template>
  <div class="animation-demo-container">

    <!-- 分类导航 + 操作区 -->
    <div class="category-bar">
      <div class="category-nav">
        <button 
          v-for="cat in categories" 
          :key="cat"
          :class="['category-btn', { active: currentCategory === cat }]"
          @click="currentCategory = cat"
        >
          {{ cat === 'all' ? '全部动画' : cat }}
        </button>
      </div>

      <div class="toolbar">
        <div class="stats-chip" v-if="!loading">
          <span class="chip-icon">📊</span>
          <span class="chip-text">已收录 <strong class="chip-number">{{ filteredAnimations.length }}</strong> 个动画</span>
        </div>
        <button class="primary-btn" @click="openCreateDialog">
          <span class="btn-icon">＋</span>
          <span>新增动画</span>
        </button>
      </div>
    </div>

    <!-- 动画列表 -->
    <div class="animations-grid" v-if="!loading">
      <div v-for="anim in filteredAnimations" :key="anim.id" class="animation-card">
        <!-- 演示区域 -->
        <div class="preview-area">
          
          <!-- 1. 文字特效 & 打字机 -->
          <div v-if="anim.category === '文字特效' || anim.title.includes('打字机')" class="demo-container">
            <h3 class="demo-text" :class="extractClassName(anim.cssCode)">
              Hello World
            </h3>
          </div>

          <!-- 2. 交互特效 (按钮) -->
          <div v-else-if="anim.category === '交互特效' || anim.title.includes('按钮')" class="demo-container">
            <button class="demo-btn" :class="extractClassName(anim.cssCode)">
              Hover Me
            </button>
          </div>

          <!-- 3. 加载动画 -->
          <div v-else-if="anim.category === '加载动画'" class="demo-container">
             <div v-if="anim.title.includes('圆点') || anim.title.toLowerCase().includes('dots')" class="dots-container">
               <div class="dot"></div><div class="dot"></div><div class="dot"></div>
             </div>
             <div v-else-if="anim.title.includes('条形') || anim.title.includes('波浪') || anim.title.toLowerCase().includes('bar') || anim.title.toLowerCase().includes('wave')" class="bars-container" :class="extractClassName(anim.cssCode)">
               <div class="bar"></div><div class="bar"></div><div class="bar"></div><div class="bar"></div><div class="bar"></div>
             </div>
             <div v-else :class="extractClassName(anim.cssCode)"></div>
          </div>

          <!-- 4. 强调动画 (铃铛/心形) -->
          <div v-else-if="anim.category === '强调动画'" class="demo-container">
            <div class="demo-icon-wrapper" :class="extractClassName(anim.cssCode)">
              <span class="demo-emoji">{{ (anim.title.includes('心') || anim.title.toLowerCase().includes('heart')) ? '❤️' : '🔔' }}</span>
            </div>
          </div>

          <!-- 5. 进入/退出动画 (头像/圆形) -->
          <div v-else-if="anim.category === '进入动画' || anim.category === '退出动画'" class="demo-container">
            <div class="demo-avatar" :class="extractClassName(anim.cssCode)">
              <span class="demo-emoji">👤</span>
            </div>
          </div>

          <!-- 6. 炫酷特效 (卡片) -->
          <div v-else-if="anim.category === '炫酷特效'" class="demo-container">
            <div class="demo-card-special" :class="extractClassName(anim.cssCode)">
              <span>Special Effect</span>
            </div>
          </div>

          <!-- 7. 默认兜底 (方块) -->
          <div v-else class="demo-box" :class="extractClassName(anim.cssCode)">
            <span class="demo-icon">🎨</span>
          </div>

          <div class="demo-label">{{ anim.title }}</div>
        </div>

        <!-- 信息区域 -->
        <div class="info-area">
          <p class="anim-desc">{{ anim.description }}</p>
          
          <!-- 代码区域 -->
          <div class="code-section">
            <div class="code-header" @click="toggleCode(anim.id)">
              <span class="code-label">CSS 代码</span>
              <span class="expand-icon">{{ expandedIds.includes(anim.id) ? '▼' : '▶' }}</span>
            </div>
            
            <div v-if="expandedIds.includes(anim.id)" class="code-content">
              <pre><code>{{ anim.cssCode }}</code></pre>
              <button class="copy-btn" @click="copyCode(anim.cssCode, $event)">
                <span class="btn-icon">📋</span> 复制
              </button>
            </div>
          </div>

          <div class="card-actions">
            <button class="text-btn" @click="openEditDialog(anim)">编辑</button>
            <button class="text-btn danger" @click="deleteAnimation(anim.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载动画库...</p>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredAnimations.length === 0" class="empty-state">
      <span class="empty-icon">📭</span>
      <p>该分类下暂无动画</p>
    </div>

    <!-- 新增 / 编辑动画弹窗 -->
    <div v-if="showDialog" class="modal-overlay">
      <div class="modal">
        <h2 class="modal-title">{{ isEditMode ? '编辑动画' : '新增动画' }}</h2>
        <div class="modal-body">
          <div class="modal-left">
            <div class="form-row">
              <label>标题</label>
              <input v-model="form.title" type="text" placeholder="请输入动画标题" />
            </div>
            <div class="form-row">
              <label>分类</label>
              <select v-model="form.category">
                <option value="" disabled>请选择动画分类</option>
                <option v-for="cat in categoryOptions" :key="cat" :value="cat">
                  {{ cat }}
                </option>
              </select>
            </div>
            <div class="form-row">
              <label>描述</label>
              <textarea v-model="form.description" rows="2" placeholder="简单描述动画用途" />
            </div>
            <div class="form-row">
              <label>CSS 代码</label>
              <textarea v-model="form.cssCode" rows="8" placeholder="粘贴完整的 @keyframes 和类定义" />
            </div>
          </div>

          <div class="modal-right" v-if="form.cssCode.trim()">
            <div class="preview-section">
              <div class="preview-header">预览</div>
              <div class="preview-content">
                <div class="preview-box-wrapper">
                  
                  <!-- 1. 文字特效 & 打字机 -->
                  <div v-if="form.category === '文字特效' || form.title.includes('打字机')" class="demo-container">
                    <h3 class="demo-text" :class="extractClassName(form.cssCode)">
                      Hello World
                    </h3>
                  </div>

                  <!-- 2. 交互特效 (按钮) -->
                  <div v-else-if="form.category === '交互特效' || form.title.includes('按钮')" class="demo-container">
                    <button class="demo-btn" :class="extractClassName(form.cssCode)">
                      Hover Me
                    </button>
                  </div>

                  <!-- 3. 加载动画 -->
                  <div v-else-if="form.category === '加载动画'" class="demo-container">
                     <div v-if="form.title.includes('圆点') || form.title.toLowerCase().includes('dots')" class="dots-container">
                       <div class="dot"></div><div class="dot"></div><div class="dot"></div>
                     </div>
                     <div v-else-if="form.title.includes('条形') || form.title.includes('波浪') || form.title.toLowerCase().includes('bar') || form.title.toLowerCase().includes('wave')" class="bars-container" :class="extractClassName(form.cssCode)">
                       <div class="bar"></div><div class="bar"></div><div class="bar"></div><div class="bar"></div><div class="bar"></div>
                     </div>
                     <div v-else :class="extractClassName(form.cssCode)"></div>
                  </div>

                  <!-- 4. 强调动画 (铃铛/心形) -->
                  <div v-else-if="form.category === '强调动画'" class="demo-container">
                    <div class="demo-icon-wrapper" :class="extractClassName(form.cssCode)">
                      <span class="demo-emoji">{{ (form.title.includes('心') || form.title.toLowerCase().includes('heart')) ? '❤️' : '🔔' }}</span>
                    </div>
                  </div>

                  <!-- 5. 进入/退出动画 (头像/圆形) -->
                  <div v-else-if="form.category === '进入动画' || form.category === '退出动画'" class="demo-container">
                    <div class="demo-avatar" :class="extractClassName(form.cssCode)">
                      <span class="demo-emoji">👤</span>
                    </div>
                  </div>

                  <!-- 6. 炫酷特效 (卡片) -->
                  <div v-else-if="form.category === '炫酷特效'" class="demo-container">
                    <div class="demo-card-special" :class="extractClassName(form.cssCode)">
                      <span>Special Effect</span>
                    </div>
                  </div>

                  <!-- 7. 默认兜底 (方块) -->
                  <div v-else class="demo-box preview-box" :class="extractClassName(form.cssCode)">
                    <span class="demo-icon">🎨</span>
                  </div>

                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="secondary-btn" @click="closeDialog">取消</button>
          <button class="primary-btn" @click="submitForm">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface Animation {
  id: number
  title: string
  category: string
  cssCode: string
  description: string
}

const animations = ref<Animation[]>([])
const animationCategories = ref<{ id: number; name: string }[]>([])
const loading = ref(true)
const currentCategory = ref('all')
const expandedIds = ref<number[]>([])
const showDialog = ref(false)
const isEditMode = ref(false)
const form = ref({
  id: null as number | null,
  title: '',
  category: '',
  cssCode: '',
  description: '',
})

const apiBase = 'http://localhost:4567/api/animations'
const categoryApiBase = 'http://localhost:4567/api/animation-categories'

// 获取所有动画
const fetchAnimations = async () => {
  try {
    const response = await fetch(apiBase)
    if (response.ok) {
      animations.value = await response.json()
    }
  } catch (error) {
    console.error('Failed to fetch animations:', error)
  } finally {
    loading.value = false
  }
}

// 获取所有动画分类
const fetchAnimationCategories = async () => {
  try {
    const response = await fetch(categoryApiBase)
    if (response.ok) {
      animationCategories.value = await response.json()
    }
  } catch (error) {
    console.error('Failed to fetch animation categories:', error)
  }
}

// 提取分类（来自动画分类表）
const categories = computed(() => {
  const cats = animationCategories.value.map(c => c.name)
  return ['all', ...cats]
})

// 下拉选项同样使用动画分类表
const categoryOptions = computed(() => animationCategories.value.map(c => c.name))

// 筛选动画
const filteredAnimations = computed(() => {
  if (currentCategory.value === 'all') return animations.value
  return animations.value.filter(a => a.category === currentCategory.value)
})

// 表单相关逻辑
const resetForm = () => {
  form.value = {
    id: null,
    title: '',
    category: '',
    cssCode: '',
    description: '',
  }
}

const openCreateDialog = () => {
  resetForm()
  isEditMode.value = false
  if (animationCategories.value.length > 0) {
    form.value.category = animationCategories.value[0].name
  }
  showDialog.value = true
}

const openEditDialog = (anim: Animation) => {
  form.value = {
    id: anim.id,
    title: anim.title,
    category: anim.category,
    cssCode: anim.cssCode,
    description: anim.description,
  }
  isEditMode.value = true
  showDialog.value = true
}

const closeDialog = () => {
  showDialog.value = false
}

const submitForm = async () => {
  if (!form.value.title.trim() || !form.value.category.trim() || !form.value.cssCode.trim()) {
    alert('标题、分类和 CSS 代码为必填项')
    return
  }

  const payload = {
    title: form.value.title.trim(),
    category: form.value.category.trim(),
    cssCode: form.value.cssCode,
    description: form.value.description,
  }

  try {
    if (isEditMode.value && form.value.id != null) {
      const response = await fetch(`${apiBase}/${form.value.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (response.ok) {
        const updated = await response.json()
        const index = animations.value.findIndex(a => a.id === updated.id)
        if (index !== -1) {
          animations.value[index] = updated
        }
      }
    } else {
      const response = await fetch(apiBase, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (response.ok) {
        const created = await response.json()
        animations.value.push(created)
      }
    }

    showDialog.value = false
  } catch (error) {
    console.error('保存动画失败:', error)
  }
}

const deleteAnimation = async (id: number) => {
  if (!confirm('确定要删除这个动画吗？')) return

  try {
    const response = await fetch(`${apiBase}/${id}`, {
      method: 'DELETE',
    })
    if (response.ok) {
      animations.value = animations.value.filter(a => a.id !== id)
    }
  } catch (error) {
    console.error('删除动画失败:', error)
  }
}

// 解析 CSS 样式，注入到演示元素
// 注意：为了演示方便，这里我们解析 keyframes 并注入到 style 标签中
// 实际上在 Vue 中动态注入 Keyframes 比较麻烦，这里我们采用一种简单策略：
// 仅仅将 animation 属性应用到 style，但 keyframes 需要全局注入或者 scoped 注入。
// 这里的简单做法是：直接把 cssCode 放到 style 标签里插入 head (仅演示用)
const injectStyles = () => {
  const styleId = 'dynamic-animations-style'
  let styleEl = document.getElementById(styleId)
  if (!styleEl) {
    styleEl = document.createElement('style')
    styleEl.id = styleId
    document.head.appendChild(styleEl)
  }
  
  let cssContent = ''
  animations.value.forEach(anim => {
    // 直接使用数据库中的CSS代码，不做额外处理
    cssContent += anim.cssCode + '\n'
  })
  styleEl.textContent = cssContent
}

// 为表单中的 CSS 提供单独的预览样式注入
const injectPreviewStyle = () => {
  const styleId = 'dynamic-animation-preview-style'
  let styleEl = document.getElementById(styleId)
  if (!styleEl) {
    styleEl = document.createElement('style')
    styleEl.id = styleId
    document.head.appendChild(styleEl)
  }

  styleEl.textContent = form.value.cssCode || ''
}

// 提取动画类名
const getAnimationStyles = (cssCode: string) => {
  // 这是一个简化的提取逻辑，假设 cssCode 中包含 .classname { ... }
  // 我们尝试从代码中提取类名，或者直接应用 animation 属性
  // 为了演示效果，我们假设后端返回的 CSS 代码包含了 .classname 的定义
  // 而我们在 demo-box 上应用这个 classname
  
  // 正则匹配 .class-name
  const match = cssCode.match(/\.([\w-]+)\s*\{/)
  if (match) {
    // 返回一个对象，这就相当于 :style="{}"，但我们需要 :class
    // 既然这样，我们还是直接在 template 里用 :class 比较麻烦，
    // 不如直接解析 animation 属性？
    // 或者，最稳妥的方式是：我们把 cssCode 里的 keyframes 和 class 都注入到页面 head
    // 然后这里返回 class 名。
    return { animation: 'none' } // 占位，实际通过 class 控制
  }
  return {}
}

// 监听数据变化注入样式
import { watch } from 'vue'
watch(animations, () => {
  injectStyles()
}, { deep: true })

// 监听表单 CSS 变化，实时更新预览样式
watch(
  () => form.value.cssCode,
  () => {
    injectPreviewStyle()
  }
)

// 但上面的 getAnimationStyles 绑定 style 不太对，应该绑定 class。
// 修正：我们直接用 DOM 操作或者 class 绑定。
// 简单起见，我们在 onMounted 之后，给每个 demo-box 添加对应的 class
// 更好的方法：
// 在 template 中： :class="extractClassName(anim.cssCode)"

const extractClassName = (cssCode: string) => {
  const match = cssCode.match(/\.([\w-]+)\s*\{/)
  return match ? match[1] : ''
}

// 复制代码
const copyCode = async (code: string, event: Event) => {
  const btn = event.target as HTMLButtonElement
  const originalText = btn.innerHTML
  
  try {
    await navigator.clipboard.writeText(code)
    btn.innerHTML = '<span class="btn-icon">✅</span> 已复制'
    setTimeout(() => {
      btn.innerHTML = originalText
    }, 2000)
  } catch (err) {
    console.error('复制失败', err)
    btn.innerHTML = '❌ 失败'
  }
}

const toggleCode = (id: number) => {
  const index = expandedIds.value.indexOf(id)
  if (index === -1) {
    expandedIds.value.push(id)
  } else {
    expandedIds.value.splice(index, 1)
  }
}

onMounted(() => {
  fetchAnimations()
  fetchAnimationCategories()
})
</script>

<style scoped>
.animation-demo-container {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  background: #f8f9fa;
}

.page-subtitle {
  color: #6c757d;
  font-size: 1.1rem;
}

.category-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.category-nav {
  display: flex;
  justify-content: flex-start;
  gap: 15px;
  flex-wrap: wrap;
}

.category-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  background: white;
  color: #6c757d;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.category-btn.active, .category-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.3);
}

.animations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
  padding: 10px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
}

.stats-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(99, 102, 241, 0.05);
  color: #475569;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  user-select: none;
}

.stats-chip:hover {
  border-color: rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(99, 102, 241, 0.1);
}

.chip-icon {
  font-size: 1.1rem;
}

.chip-number {
  color: #6366f1;
  font-weight: 800;
  font-size: 1.1rem;
  margin: 0 2px;
}

.primary-btn,
.secondary-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.primary-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.secondary-btn {
  background: #f1f3f5;
  color: #495057;
}

.secondary-btn:hover {
  background: #e9ecef;
}

.animation-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  transition: transform 0.3s ease;
  border: 1px solid rgba(0,0,0,0.05);
}

.animation-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.preview-area {
  height: 180px;
  background: #f1f3f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border-bottom: 1px solid #e9ecef;
}

.demo-box {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 新增演示组件样式 */
.demo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.demo-text {
  font-size: 1.5rem;
  font-weight: 800;
  color: #4b5563;
  margin: 0;
}

.demo-btn {
  padding: 10px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.demo-loading-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 铃铛图标容器 */
.demo-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 头像容器 */
.demo-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 炫酷卡片 */
.demo-card-special {
  width: 140px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.demo-label {
  margin-top: 15px;
  font-weight: 600;
  color: #495057;
}

.info-area {
  padding: 20px;
}

.anim-desc {
  color: #6c757d;
  font-size: 0.9rem;
  margin-bottom: 15px;
  line-height: 1.5;
}

.code-section {
  background: #282c34;
  border-radius: 8px;
  overflow: hidden;
}

.code-header {
  padding: 8px 12px;
  background: rgba(255,255,255,0.1);
  color: #abb2bf;
  font-size: 0.8rem;
  display: flex;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
}

.code-header:hover {
  background: rgba(255,255,255,0.15);
}

.code-content {
  padding: 12px;
  position: relative;
}

pre {
  margin: 0;
  white-space: pre-wrap;
  color: #abb2bf;
  font-family: 'Fira Code', monospace;
  font-size: 0.85rem;
}

.copy-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  background: rgba(255,255,255,0.1);
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 0.75rem;
  cursor: pointer;
  transition: background 0.2s;
}

.copy-btn:hover {
  background: rgba(255,255,255,0.2);
}

.card-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.text-btn {
  background: transparent;
  border: none;
  color: #4b5563;
  font-size: 0.85rem;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.text-btn:hover {
  background: #f1f3f5;
}

.text-btn.danger {
  color: #e11d48;
}

.text-btn.danger:hover {
  background: #fee2e2;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 50px;
  color: #6c757d;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e9ecef;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal {
  width: 100%;
  max-width: 640px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
  padding: 20px 22px 18px;
}

.modal-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 14px;
}

.modal-body {
  display: flex;
  gap: 16px;
  max-height: 60vh;
  overflow: hidden;
}

.modal-left {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 4px;
  overflow-y: auto;
}

.modal-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding-left: 4px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row label {
  font-size: 0.85rem;
  color: #6b7280;
}

.form-row input,
.form-row textarea,
.form-row select {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 8px 10px;
  font-size: 0.9rem;
  font-family: inherit;
  resize: vertical;
}

.form-row input:focus,
.form-row textarea:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.2);
}

.modal-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.preview-section {
  width: 100%;
  background: #f3f4f6;
  border-radius: 14px;
  border: 1px dashed #e5e7eb;
  padding: 10px 12px 14px;
}

.preview-header {
  font-size: 0.85rem;
  color: #6b7280;
  margin-bottom: 8px;
}

.preview-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-box-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
}

.preview-box {
  width: 120px;
  height: 120px;
}

@media (max-width: 768px) {
  .animations-grid {
    grid-template-columns: 1fr;
  }

  .modal {
    max-width: 100%;
  }

  .modal-body {
    flex-direction: column;
    max-height: 70vh;
    overflow-y: auto;
  }

  .modal-right {
    padding-left: 0;
  }
}
</style>

<!-- 非 Scoped 样式，用于定义默认外观，允许被动态注入的 CSS 覆盖 -->
<style>
.demo-box {
  background: linear-gradient(135deg, #FF9A9E 0%, #FECFEF 99%, #FECFEF 100%);
  border-radius: 12px;
  font-size: 2rem;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.demo-btn {
  background: #6366f1;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  box-shadow: 0 4px 6px rgba(99, 102, 241, 0.3);
}

.demo-icon-wrapper {
  width: 64px;
  height: 64px;
  background: #fef3c7;
  border-radius: 50%;
  font-size: 2rem;
  color: #d97706;
  box-shadow: 0 4px 6px rgba(217, 119, 6, 0.2);
}

.demo-avatar {
  width: 64px;
  height: 64px;
  background: #e0e7ff;
  border-radius: 50%;
  font-size: 2rem;
  border: 2px solid #6366f1;
  color: #6366f1;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.2);
}

.demo-card-special {
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border-radius: 12px;
  color: white;
  font-weight: bold;
  box-shadow: 0 10px 20px rgba(139, 92, 246, 0.3);
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.bars-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 40px;
}

.bar {
  width: 6px;
  height: 100%;
  background-color: #6366f1;
  border-radius: 4px;
}
</style>
