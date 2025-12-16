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
        class="anim-card playing"
      >
        <!-- 预览区 - 根据分类使用不同容器 -->
        <div class="preview-area">
          
          <!-- 加载动画 -->
          <template v-if="item.category === '加载动画'">
            <div class="demo-loader active" :class="getAnimClass(item)">
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
            <button class="demo-button active" :class="getAnimClass(item)">
              点击体验
            </button>
          </template>

          <!-- 文字动画 -->
          <template v-else-if="item.category === '文字动画'">
            <div class="demo-text active" :class="getAnimClass(item)">
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
            <div class="demo-hover-box active" :class="getAnimClass(item)">
              <span>悬停我</span>
            </div>
          </template>

          <!-- 图形变换 -->
          <template v-else-if="item.category === '图形变换'">
            <div class="demo-shape active" :class="getAnimClass(item)"></div>
          </template>

          <!-- 入场动画 -->
          <template v-else-if="item.category === '入场动画'">
            <div 
              class="demo-enter-box active" 
              :class="getAnimClass(item)"
            >
              <span>✨</span>
            </div>
          </template>

          <!-- 强调效果 -->
          <template v-else-if="item.category === '强调效果'">
            <div class="demo-emphasis active" :class="getAnimClass(item)">
              <span>🔔</span>
            </div>
          </template>

          <!-- 背景特效 -->
          <template v-else-if="item.category === '背景特效'">
            <div class="demo-background active" :class="getAnimClass(item)"></div>
          </template>

          <!-- 退出动画 -->
          <template v-else-if="item.category === '退出动画'">
            <div 
              class="demo-exit-box active" 
              :class="getAnimClass(item)"
            >
              <span>👋</span>
            </div>
          </template>

          <!-- 边框动画 -->
          <template v-else-if="item.category === '边框动画'">
            <div class="demo-border active" :class="getAnimClass(item)"></div>
          </template>

          <!-- 阴影效果 -->
          <template v-else-if="item.category === '阴影效果'">
            <div class="demo-shadow active" :class="getAnimClass(item)"></div>
          </template>

          <!-- 3D特效 -->
          <template v-else-if="item.category === '3D特效'">
            <div class="demo-3d active" :class="getAnimClass(item)"></div>
          </template>

          <!-- 滤镜特效 -->
          <template v-else-if="item.category === '滤镜特效'">
            <div class="demo-filter active" :class="getAnimClass(item)">
              <span>🎭</span>
            </div>
          </template>

          <!-- 变形特效 -->
          <template v-else-if="item.category === '变形特效'">
            <div class="demo-transform active" :class="getAnimClass(item)"></div>
          </template>

          <!-- 大屏动画 -->
          <template v-else-if="item.category === '大屏动画'">
            <div class="demo-bigscreen active">
              <!-- 流光边框 -->
              <div v-if="item.title === '流光边框'" class="bigscreen-container">
                <div class="box glow-border"><span class="box-text">数据面板</span></div>
              </div>
              <!-- 扫光效果 -->
              <div v-else-if="item.title === '扫光效果'" class="bigscreen-container">
                <div class="box shine-box">📊</div>
              </div>
              <!-- 呼吸光晕 -->
              <div v-else-if="item.title === '呼吸光晕'" class="bigscreen-container">
                <div class="box breath-glow"></div>
              </div>
              <!-- 雷达扫描 -->
              <div v-else-if="item.title === '雷达扫描'" class="bigscreen-container">
                <div class="box radar-scan"></div>
              </div>
              <!-- 粒子漂浮 -->
              <div v-else-if="item.title === '粒子漂浮'" class="bigscreen-container">
                <div class="box particle-float"></div>
              </div>
              <!-- 波纹扩散 -->
              <div v-else-if="item.title === '波纹扩散'" class="bigscreen-container">
                <div class="box ripple-wave"></div>
              </div>
              <!-- 霓虹文字 -->
              <div v-else-if="item.title === '霓虹文字'" class="bigscreen-container">
                <div class="box neon-text">NEON</div>
              </div>
              <!-- 进度条动画 -->
              <div v-else-if="item.title === '进度条动画'" class="bigscreen-container">
                <div class="box progress-bar"></div>
              </div>
              <!-- 数据卡片悬浮 -->
              <div v-else-if="item.title === '数据卡片悬浮'" class="bigscreen-container">
                <div class="box hover-card"></div>
              </div>
              <!-- 渐变流动边框 -->
              <div v-else-if="item.title === '渐变流动边框'" class="bigscreen-container">
                <div class="box gradient-border"><span class="box-text">流动边框</span></div>
              </div>
              <!-- 脉冲圆环 -->
              <div v-else-if="item.title === '脉冲圆环'" class="bigscreen-container">
                <div class="box pulse-ring"></div>
              </div>
              <!-- 数字滚动 -->
              <div v-else-if="item.title === '数字滚动'" class="bigscreen-container">
                <div class="box digit-roll">8888</div>
              </div>
              <!-- 六边形网格 -->
              <div v-else-if="item.title === '六边形网格'" class="bigscreen-container">
                <div class="box hex-grid"></div>
              </div>
              <!-- 能量环 -->
              <div v-else-if="item.title === '能量环'" class="bigscreen-container">
                <div class="box energy-ring"></div>
              </div>
              <!-- 数据流 -->
              <div v-else-if="item.title === '数据流'" class="bigscreen-container">
                <div class="box data-flow"></div>
              </div>
              <!-- 闪烁光点 -->
              <div v-else-if="item.title === '闪烁光点'" class="bigscreen-container">
                <div class="box blink-dots"></div>
              </div>
              <!-- 波浪线条 -->
              <div v-else-if="item.title === '波浪线条'" class="bigscreen-container">
                <div class="box wave-line"></div>
              </div>
              <!-- 旋转方块 -->
              <div v-else-if="item.title === '旋转方块'" class="bigscreen-container">
                <div class="box rotate-cube"></div>
              </div>
              <!-- 扫描线 -->
              <div v-else-if="item.title === '扫描线'" class="bigscreen-container">
                <div class="box scan-line"></div>
              </div>
              <!-- 环形进度 -->
              <div v-else-if="item.title === '环形进度'" class="bigscreen-container">
                <div class="box ring-progress"></div>
              </div>
              <!-- 粒子连线 -->
              <div v-else-if="item.title === '粒子连线'" class="bigscreen-container">
                <div class="box particle-line"></div>
              </div>
              <!-- 电路板 -->
              <div v-else-if="item.title === '电路板'" class="bigscreen-container">
                <div class="box circuit-board"></div>
              </div>
              <!-- DNA螺旋 -->
              <div v-else-if="item.title === 'DNA螺旋'" class="bigscreen-container">
                <div class="box dna-helix">
                  <span></span><span></span><span></span><span></span><span></span>
                </div>
              </div>
              <!-- 音频波形 -->
              <div v-else-if="item.title === '音频波形'" class="bigscreen-container">
                <div class="box audio-wave">
                  <span></span><span></span><span></span><span></span><span></span><span></span><span></span>
                </div>
              </div>
              <!-- 星空背景 -->
              <div v-else-if="item.title === '星空背景'" class="bigscreen-container">
                <div class="box starfield"></div>
              </div>
              <!-- 加载圆环 -->
              <div v-else-if="item.title === '加载圆环'" class="bigscreen-container">
                <div class="box tech-loader"></div>
              </div>
              <!-- 信号波 -->
              <div v-else-if="item.title === '信号波'" class="bigscreen-container">
                <div class="box signal-wave"></div>
              </div>
              <!-- 矩阵雨 -->
              <div v-else-if="item.title === '矩阵雨'" class="bigscreen-container">
                <div class="box matrix-rain"></div>
              </div>
              <!-- 光束扫射 -->
              <div v-else-if="item.title === '光束扫射'" class="bigscreen-container">
                <div class="box light-beam"></div>
              </div>
              <!-- 粒子爆发 -->
              <div v-else-if="item.title === '粒子爆发'" class="bigscreen-container">
                <div class="box particle-burst"></div>
              </div>
              <!-- 全息投影 -->
              <div v-else-if="item.title === '全息投影'" class="bigscreen-container">
                <div class="box hologram">HOLO</div>
              </div>
              <!-- 水波纹 -->
              <div v-else-if="item.title === '水波纹'" class="bigscreen-container">
                <div class="box water-ripple"></div>
              </div>
              <!-- 闪电效果 -->
              <div v-else-if="item.title === '闪电效果'" class="bigscreen-container">
                <div class="box lightning"></div>
              </div>
              <!-- 齿轮旋转 -->
              <div v-else-if="item.title === '齿轮旋转'" class="bigscreen-container">
                <div class="box gear-rotate"></div>
              </div>
              <!-- 心跳监测 -->
              <div v-else-if="item.title === '心跳监测'" class="bigscreen-container">
                <div class="box heartbeat"></div>
              </div>
              <!-- 指南针 -->
              <div v-else-if="item.title === '指南针'" class="bigscreen-container">
                <div class="box compass"></div>
              </div>
              <!-- 时钟动画 -->
              <div v-else-if="item.title === '时钟动画'" class="bigscreen-container">
                <div class="box tech-clock"></div>
              </div>
              <!-- 火焰效果 -->
              <div v-else-if="item.title === '火焰效果'" class="bigscreen-container">
                <div class="box flame"></div>
              </div>
              <!-- 卫星轨道 -->
              <div v-else-if="item.title === '卫星轨道'" class="bigscreen-container">
                <div class="box satellite-orbit"></div>
              </div>
              <!-- 音量指示 -->
              <div v-else-if="item.title === '音量指示'" class="bigscreen-container">
                <div class="box volume-bar"></div>
              </div>
              <!-- 目标锁定 -->
              <div v-else-if="item.title === '目标锁定'" class="bigscreen-container">
                <div class="box target-lock"></div>
              </div>
              <!-- 指纹扫描 -->
              <div v-else-if="item.title === '指纹扫描'" class="bigscreen-container">
                <div class="box fingerprint"></div>
              </div>
              <!-- 心电图 -->
              <div v-else-if="item.title === '心电图'" class="bigscreen-container">
                <div class="box heartbeat-line"></div>
              </div>
              <!-- 时钟指针 -->
              <div v-else-if="item.title === '时钟指针'" class="bigscreen-container">
                <div class="box clock-hand"></div>
              </div>
              <!-- 齿轮转动 -->
              <div v-else-if="item.title === '齿轮转动'" class="bigscreen-container">
                <div class="box gear-spin"></div>
              </div>
              <!-- 数据上传 -->
              <div v-else-if="item.title === '数据上传'" class="bigscreen-container">
                <div class="box data-upload"></div>
              </div>
              <!-- 声波扩散 -->
              <div v-else-if="item.title === '声波扩散'" class="bigscreen-container">
                <div class="box sound-wave"></div>
              </div>
              <!-- 温度计 -->
              <div v-else-if="item.title === '温度计'" class="bigscreen-container">
                <div class="box thermometer"></div>
              </div>
              <!-- 立体方块 -->
              <div v-else-if="item.title === '立体方块'" class="bigscreen-container">
                <div class="box cube-3d"></div>
              </div>
              <!-- 信号强度 -->
              <div v-else-if="item.title === '信号强度'" class="bigscreen-container">
                <div class="box signal-strength"></div>
              </div>
              <!-- 双环旋转 -->
              <div v-else-if="item.title === '双环旋转'" class="bigscreen-container">
                <div class="box double-ring"></div>
              </div>
              <!-- 数据节点 -->
              <div v-else-if="item.title === '数据节点'" class="bigscreen-container">
                <div class="box data-node"></div>
              </div>
              <!-- 电池充电 -->
              <div v-else-if="item.title === '电池充电'" class="bigscreen-container">
                <div class="box battery-charge"></div>
              </div>
              <!-- 仪表盘 -->
              <div v-else-if="item.title === '仪表盘'" class="bigscreen-container">
                <div class="box gauge-meter"></div>
              </div>
              <!-- 像素加载 -->
              <div v-else-if="item.title === '像素加载'" class="bigscreen-container">
                <div class="box pixel-load"></div>
              </div>
              <!-- 箭头流动 -->
              <div v-else-if="item.title === '箭头流动'" class="bigscreen-container">
                <div class="box arrow-flow"></div>
              </div>
              <!-- 警报闪烁 -->
              <div v-else-if="item.title === '警报闪烁'" class="bigscreen-container">
                <div class="box alert-blink"></div>
              </div>
              <!-- 涟漪按钮 -->
              <div v-else-if="item.title === '涟漪按钮'" class="bigscreen-container">
                <div class="box ripple-btn"></div>
              </div>
              <!-- 光标闪烁 -->
              <div v-else-if="item.title === '光标闪烁'" class="bigscreen-container">
                <div class="box cursor-blink"></div>
              </div>
              <!-- 蜂巢脉动 -->
              <div v-else-if="item.title === '蜂巢脉动'" class="bigscreen-container">
                <div class="box honeycomb"></div>
              </div>
              <!-- 菱形闪耀 -->
              <div v-else-if="item.title === '菱形闪耀'" class="bigscreen-container">
                <div class="box diamond-shine"></div>
              </div>
              <!-- 三角旋涡 -->
              <div v-else-if="item.title === '三角旋涡'" class="bigscreen-container">
                <div class="box triangle-vortex"></div>
              </div>
              <!-- 同心圆扩散 -->
              <div v-else-if="item.title === '同心圆扩散'" class="bigscreen-container">
                <div class="box concentric"></div>
              </div>
              <!-- 眼睛眨动 -->
              <div v-else-if="item.title === '眼睛眨动'" class="bigscreen-container">
                <div class="box eye-blink"></div>
              </div>
              <!-- 摆钟摇摆 -->
              <div v-else-if="item.title === '摆钟摇摆'" class="bigscreen-container">
                <div class="box pendulum"></div>
              </div>
              <!-- 弹簧伸缩 -->
              <div v-else-if="item.title === '弹簧伸缩'" class="bigscreen-container">
                <div class="box spring-bounce"></div>
              </div>
              <!-- 沙漏计时 -->
              <div v-else-if="item.title === '沙漏计时'" class="bigscreen-container">
                <div class="box hourglass"></div>
              </div>
              <!-- 磁场线条 -->
              <div v-else-if="item.title === '磁场线条'" class="bigscreen-container">
                <div class="box magnetic-field"></div>
              </div>
              <!-- 量子隧道 -->
              <div v-else-if="item.title === '量子隧道'" class="bigscreen-container">
                <div class="box quantum-tunnel"></div>
              </div>
              <!-- 电磁脉冲 -->
              <div v-else-if="item.title === '电磁脉冲'" class="bigscreen-container">
                <div class="box emp-pulse"></div>
              </div>
              <!-- 数据立方体 -->
              <div v-else-if="item.title === '数据立方体'" class="bigscreen-container">
                <div class="box data-cube"></div>
              </div>
              <!-- 神经网络 -->
              <div v-else-if="item.title === '神经网络'" class="bigscreen-container">
                <div class="box neural-net"></div>
              </div>
              <!-- 引力波 -->
              <div v-else-if="item.title === '引力波'" class="bigscreen-container">
                <div class="box gravity-wave"></div>
              </div>
              <!-- 等离子球 -->
              <div v-else-if="item.title === '等离子球'" class="bigscreen-container">
                <div class="box plasma-ball"></div>
              </div>
              <!-- 黑洞吸引 -->
              <div v-else-if="item.title === '黑洞吸引'" class="bigscreen-container">
                <div class="box black-hole"></div>
              </div>
              <!-- 能量护盾 -->
              <div v-else-if="item.title === '能量护盾'" class="bigscreen-container">
                <div class="box energy-shield"></div>
              </div>
              <!-- 虫洞传送 -->
              <div v-else-if="item.title === '虫洞传送'" class="bigscreen-container">
                <div class="box wormhole"></div>
              </div>
              <!-- 全息地球 -->
              <div v-else-if="item.title === '全息地球'" class="bigscreen-container">
                <div class="box holo-earth"></div>
              </div>
              <!-- 星云漩涡 -->
              <div v-else-if="item.title === '星云漩涡'" class="bigscreen-container">
                <div class="box nebula-vortex"></div>
              </div>
              <!-- 激光扫描 -->
              <div v-else-if="item.title === '激光扫描'" class="bigscreen-container">
                <div class="box laser-scan"></div>
              </div>
              <!-- 原子轨道 -->
              <div v-else-if="item.title === '原子轨道'" class="bigscreen-container">
                <div class="box atom-orbit"></div>
              </div>
              <!-- 频谱分析 -->
              <div v-else-if="item.title === '频谱分析'" class="bigscreen-container">
                <div class="box spectrum-analyzer"></div>
              </div>
              <!-- 力场屏障 -->
              <div v-else-if="item.title === '力场屏障'" class="bigscreen-container">
                <div class="box force-field"></div>
              </div>
              <!-- 数据矩阵 -->
              <div v-else-if="item.title === '数据矩阵'" class="bigscreen-container">
                <div class="box data-matrix"></div>
              </div>
              <!-- 脉冲雷达 -->
              <div v-else-if="item.title === '脉冲雷达'" class="bigscreen-container">
                <div class="box pulse-radar"></div>
              </div>
              <!-- 晶体生长 -->
              <div v-else-if="item.title === '晶体生长'" class="bigscreen-container">
                <div class="box crystal-grow"></div>
              </div>
              <!-- 传送门 -->
              <div v-else-if="item.title === '传送门'" class="bigscreen-container">
                <div class="box portal-gate"></div>
              </div>
              <!-- 能量核心 -->
              <div v-else-if="item.title === '能量核心'" class="bigscreen-container">
                <div class="box energy-core"></div>
              </div>
              <!-- 默认 -->
              <div v-else class="bigscreen-container">
                <div class="box"></div>
              </div>
            </div>
          </template>

          <!-- 默认 -->
          <template v-else>
            <div class="demo-default active" :class="getAnimClass(item)"></div>
          </template>

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
  '变形特效': '🔀',
  '大屏动画': '🖥️'
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
  // 匹配第一个类名，支持 .class { 和 .class span { 等格式
  const match = item.cssCode.match(/\.([\w-]+)/)
  return match ? match[1] : ''
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
  // 处理CSS让所有动画无限循环播放
  const css = animations.value.map(a => {
    let code = a.cssCode
    // 移除 .active 选择器条件
    code = code.replace(/\.active\s*/g, '')
    // 将 forwards 替换为 infinite
    code = code.replace(/\bforwards\b/g, 'infinite')
    // 将单独的 alternate 替换为 infinite alternate
    code = code.replace(/animation:([^;]*)\balternate\b(?!\s*infinite)/g, 'animation:$1infinite alternate')
    return code
  }).join('\n')
  el.textContent = css
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

/* 大屏动画容器 */
.demo-bigscreen {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #0a1628 0%, #0d2137 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.bigscreen-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.demo-bigscreen .box-text {
  position: relative;
  z-index: 10;
  color: #00f0ff;
  font-size: 14px;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
}

/* ========== 大屏动画效果 ========== */

/* 流光边框 */
.glow-border {
  width: 200px;
  height: 120px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.glow-border::before {
  content: "";
  position: absolute;
  inset: -2px;
  background: conic-gradient(from 0deg, #00f0ff, #00ff88, #a855f7, #00f0ff);
  animation: glow-rotate 3s linear infinite;
  z-index: 1;
}
.glow-border::after {
  content: "";
  position: absolute;
  inset: 2px;
  background: #0a1628;
  border-radius: 6px;
  z-index: 2;
}
@keyframes glow-rotate {
  to { transform: rotate(360deg); }
}

/* 扫光效果 */
.shine-box {
  width: 180px;
  height: 100px;
  background: linear-gradient(135deg, #0a2540, #0d3050);
  border: 1px solid rgba(0, 200, 255, 0.3);
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #00ff88;
}
.shine-box::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shine-sweep 2s infinite;
}
@keyframes shine-sweep {
  to { left: 150%; }
}

/* 呼吸光晕 */
.breath-glow {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle, #00f0ff 0%, transparent 70%);
  animation: breathe-glow 2s ease-in-out infinite;
  display: flex;
  align-items: center;
  justify-content: center;
}
.breath-glow::after {
  content: "";
  width: 40px;
  height: 40px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
}
@keyframes breathe-glow {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.2); opacity: 1; }
}

/* 雷达扫描 */
.radar-scan {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 2px solid rgba(0, 200, 255, 0.3);
  position: relative;
  background: #0a1628;
}
.radar-scan::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent 0%, rgba(0, 240, 255, 0.4) 10%, transparent 20%);
  animation: radar-sweep 2s linear infinite;
}
.radar-scan::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes radar-sweep {
  to { transform: rotate(360deg); }
}

/* 粒子漂浮 */
.particle-float {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.particle-float::before,
.particle-float::after {
  content: "";
  position: absolute;
  width: 6px;
  height: 6px;
  background: #00f0ff;
  border-radius: 50%;
  animation: particle-move 3s ease-in-out infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.particle-float::before {
  top: 20%;
  left: 30%;
  animation-delay: 0s;
}
.particle-float::after {
  top: 60%;
  left: 70%;
  animation-delay: 1.5s;
}
@keyframes particle-move {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

/* 波纹扩散 */
.ripple-wave {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ripple-wave::before,
.ripple-wave::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: ripple-expand 2s ease-out infinite;
}
.ripple-wave::before {
  width: 40px;
  height: 40px;
}
.ripple-wave::after {
  width: 40px;
  height: 40px;
  animation-delay: 1s;
}
@keyframes ripple-expand {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(3); opacity: 0; }
}

/* 霓虹文字 */
.neon-text {
  font-size: 36px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 0 10px #00f0ff, 0 0 20px #00f0ff, 0 0 40px #00f0ff, 0 0 80px #00f0ff;
  animation: neon-flicker 1.5s ease-in-out infinite alternate;
}
@keyframes neon-flicker {
  from { text-shadow: 0 0 10px #00f0ff, 0 0 20px #00f0ff, 0 0 40px #00f0ff; }
  to { text-shadow: 0 0 5px #00f0ff, 0 0 10px #00f0ff, 0 0 20px #00f0ff, 0 0 40px #00f0ff, 0 0 80px #00f0ff; }
}

/* 进度条动画 */
.progress-bar {
  width: 200px;
  height: 12px;
  background: rgba(0, 50, 100, 0.4);
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}
.progress-bar::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  width: 70%;
  height: 100%;
  background: linear-gradient(90deg, #00f0ff, #00ff88);
  border-radius: 6px;
  box-shadow: 0 0 10px #00f0ff;
}
.progress-bar::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: progress-shine 2s infinite;
}
@keyframes progress-shine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 数据卡片悬浮 */
.hover-card {
  width: 160px;
  height: 100px;
  background: linear-gradient(135deg, rgba(0, 100, 200, 0.3), rgba(0, 50, 100, 0.2));
  border: 1px solid rgba(0, 200, 255, 0.3);
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  animation: hover-float 3s ease-in-out infinite;
}
@keyframes hover-float {
  0%, 100% { transform: translateY(0); box-shadow: 0 5px 15px rgba(0, 200, 255, 0.2); }
  50% { transform: translateY(-10px); box-shadow: 0 20px 40px rgba(0, 200, 255, 0.3); }
}

/* 渐变流动边框 */
.gradient-border {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gradient-border::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 8px;
  padding: 2px;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7, #00f0ff);
  background-size: 300% 100%;
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: gradient-flow 3s linear infinite;
}
@keyframes gradient-flow {
  0% { background-position: 0% 0%; }
  100% { background-position: 300% 0%; }
}

/* 脉冲圆环 */
.pulse-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid #00f0ff;
  position: relative;
  animation: pulse-scale 2s ease-out infinite;
}
.pulse-ring::before {
  content: "";
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  border: 2px solid #00f0ff;
  animation: pulse-scale 2s ease-out infinite 0.5s;
  opacity: 0.6;
}
@keyframes pulse-scale {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* 数字滚动 */
.digit-roll {
  font-size: 48px;
  font-weight: bold;
  color: #00f0ff;
  font-family: 'Courier New', monospace;
  text-shadow: 0 0 20px #00f0ff;
  animation: digit-flicker 0.1s steps(1) infinite;
}
@keyframes digit-flicker {
  0% { content: "1234"; opacity: 1; }
  25% { opacity: 0.8; }
  50% { opacity: 1; }
  75% { opacity: 0.9; }
  100% { opacity: 1; }
}

/* 六边形网格 */
.hex-grid {
  width: 200px;
  height: 120px;
  background: 
    linear-gradient(30deg, #0a1628 12%, transparent 12.5%, transparent 87%, #0a1628 87.5%, #0a1628),
    linear-gradient(150deg, #0a1628 12%, transparent 12.5%, transparent 87%, #0a1628 87.5%, #0a1628),
    linear-gradient(30deg, #0a1628 12%, transparent 12.5%, transparent 87%, #0a1628 87.5%, #0a1628),
    linear-gradient(150deg, #0a1628 12%, transparent 12.5%, transparent 87%, #0a1628 87.5%, #0a1628),
    linear-gradient(60deg, rgba(0, 240, 255, 0.1) 25%, transparent 25.5%, transparent 75%, rgba(0, 240, 255, 0.1) 75%, rgba(0, 240, 255, 0.1)),
    linear-gradient(60deg, rgba(0, 240, 255, 0.1) 25%, transparent 25.5%, transparent 75%, rgba(0, 240, 255, 0.1) 75%, rgba(0, 240, 255, 0.1));
  background-size: 40px 70px;
  background-position: 0 0, 0 0, 20px 35px, 20px 35px, 0 0, 20px 35px;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
  animation: hex-pulse 2s ease-in-out infinite;
}
@keyframes hex-pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}

/* 能量环 */
.energy-ring {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
}
.energy-ring::before {
  content: "";
  position: absolute;
  inset: 0;
  border: 4px solid transparent;
  border-top-color: #00f0ff;
  border-right-color: #00ff88;
  border-radius: 50%;
  animation: energy-spin 1s linear infinite;
}
.energy-ring::after {
  content: "";
  position: absolute;
  inset: 15px;
  border: 3px solid transparent;
  border-bottom-color: #a855f7;
  border-left-color: #ec4899;
  border-radius: 50%;
  animation: energy-spin 1.5s linear infinite reverse;
}
@keyframes energy-spin {
  to { transform: rotate(360deg); }
}

/* 数据流 */
.data-flow {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.data-flow::before,
.data-flow::after {
  content: "";
  position: absolute;
  width: 2px;
  height: 30px;
  background: linear-gradient(to bottom, transparent, #00f0ff, transparent);
  animation: data-fall 1.5s linear infinite;
}
.data-flow::before {
  left: 30%;
  animation-delay: 0s;
}
.data-flow::after {
  left: 70%;
  animation-delay: 0.7s;
}
@keyframes data-fall {
  0% { top: -30px; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 120px; opacity: 0; }
}

/* 闪烁光点 */
.blink-dots {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.blink-dots::before,
.blink-dots::after {
  content: "";
  position: absolute;
  width: 4px;
  height: 4px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 10px #00f0ff, 40px 20px 0 #00ff88, 80px 50px 0 #a855f7, 120px 30px 0 #ec4899, 160px 70px 0 #00f0ff;
  animation: blink-star 2s ease-in-out infinite;
}
.blink-dots::before {
  top: 20px;
  left: 20px;
}
.blink-dots::after {
  top: 60px;
  left: 10px;
  animation-delay: 1s;
  box-shadow: 0 0 10px #00ff88, 50px -20px 0 #00f0ff, 100px 10px 0 #ec4899, 140px -10px 0 #a855f7;
}
@keyframes blink-star {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

/* 波浪线条 */
.wave-line {
  width: 200px;
  height: 80px;
  position: relative;
  overflow: hidden;
}
.wave-line::before {
  content: "";
  position: absolute;
  width: 400px;
  height: 100%;
  background: repeating-linear-gradient(
    90deg,
    transparent 0px,
    transparent 10px,
    #00f0ff 10px,
    #00f0ff 12px
  );
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 80'%3E%3Cpath d='M0,40 Q25,10 50,40 T100,40 T150,40 T200,40' stroke='white' stroke-width='4' fill='none'/%3E%3C/svg%3E");
  -webkit-mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 80'%3E%3Cpath d='M0,40 Q25,10 50,40 T100,40 T150,40 T200,40' stroke='white' stroke-width='4' fill='none'/%3E%3C/svg%3E");
  animation: wave-move 2s linear infinite;
}
@keyframes wave-move {
  0% { transform: translateX(0); }
  100% { transform: translateX(-200px); }
}

/* 旋转方块 */
.rotate-cube {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #00f0ff, #00ff88);
  animation: cube-rotate 3s ease-in-out infinite;
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.5);
}
@keyframes cube-rotate {
  0% { transform: rotate(0deg) scale(1); border-radius: 10px; }
  25% { transform: rotate(90deg) scale(0.8); border-radius: 50%; }
  50% { transform: rotate(180deg) scale(1); border-radius: 10px; }
  75% { transform: rotate(270deg) scale(0.8); border-radius: 50%; }
  100% { transform: rotate(360deg) scale(1); border-radius: 10px; }
}

/* 扫描线 */
.scan-line {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
}
.scan-line::before {
  content: "";
  position: absolute;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  box-shadow: 0 0 20px #00f0ff;
  animation: scan-move 2s ease-in-out infinite;
}
@keyframes scan-move {
  0%, 100% { top: 0; }
  50% { top: calc(100% - 3px); }
}

/* 环形进度 */
.ring-progress {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: conic-gradient(#00f0ff 0deg, #00ff88 120deg, #a855f7 240deg, #00f0ff 360deg);
  position: relative;
  animation: ring-rotate 3s linear infinite;
}
.ring-progress::before {
  content: "";
  position: absolute;
  inset: 8px;
  background: #0a1628;
  border-radius: 50%;
}
.ring-progress::after {
  content: "75%";
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00f0ff;
  font-size: 16px;
  font-weight: bold;
}
@keyframes ring-rotate {
  to { transform: rotate(360deg); }
}

/* 粒子连线 */
.particle-line {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
  overflow: hidden;
}
.particle-line::before {
  content: "";
  position: absolute;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  top: 30px;
  left: 30px;
  box-shadow: 
    0 0 10px #00f0ff,
    60px 40px 0 #00ff88,
    120px 20px 0 #a855f7,
    140px 60px 0 #ec4899;
  animation: particle-pulse 2s ease-in-out infinite;
}
.particle-line::after {
  content: "";
  position: absolute;
  top: 34px;
  left: 34px;
  width: 120px;
  height: 1px;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7);
  transform-origin: left;
  animation: line-draw 2s ease-in-out infinite;
}
@keyframes particle-pulse {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}
@keyframes line-draw {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

/* 电路板 */
.circuit-board {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
  overflow: hidden;
}
.circuit-board::before {
  content: "";
  position: absolute;
  inset: 10px;
  background: 
    linear-gradient(90deg, transparent 49%, rgba(0, 240, 255, 0.3) 49%, rgba(0, 240, 255, 0.3) 51%, transparent 51%),
    linear-gradient(0deg, transparent 49%, rgba(0, 240, 255, 0.3) 49%, rgba(0, 240, 255, 0.3) 51%, transparent 51%);
  background-size: 20px 20px;
}
.circuit-board::after {
  content: "";
  position: absolute;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  top: 20px;
  left: 20px;
  box-shadow: 
    40px 0 0 #00ff88,
    80px 20px 0 #a855f7,
    120px 0 0 #ec4899,
    160px 20px 0 #00f0ff,
    0 40px 0 #00ff88,
    40px 60px 0 #a855f7,
    80px 40px 0 #00f0ff,
    120px 60px 0 #00ff88,
    160px 40px 0 #ec4899;
  animation: circuit-blink 1.5s ease-in-out infinite;
}
@keyframes circuit-blink {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

/* DNA螺旋 */
.dna-helix {
  width: 60px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: center;
}
.dna-helix span {
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #00f0ff, transparent 40%, transparent 60%, #00ff88);
  border-radius: 2px;
  animation: dna-twist 2s ease-in-out infinite;
}
.dna-helix span:nth-child(1) { animation-delay: 0s; }
.dna-helix span:nth-child(2) { animation-delay: 0.2s; }
.dna-helix span:nth-child(3) { animation-delay: 0.4s; }
.dna-helix span:nth-child(4) { animation-delay: 0.6s; }
.dna-helix span:nth-child(5) { animation-delay: 0.8s; }
@keyframes dna-twist {
  0%, 100% { transform: scaleX(1); }
  50% { transform: scaleX(-1); }
}

/* 音频波形 */
.audio-wave {
  width: 180px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.audio-wave span {
  width: 8px;
  background: linear-gradient(to top, #00f0ff, #00ff88);
  border-radius: 4px;
  animation: audio-bounce 1s ease-in-out infinite;
}
.audio-wave span:nth-child(1) { height: 20px; animation-delay: 0s; }
.audio-wave span:nth-child(2) { height: 40px; animation-delay: 0.1s; }
.audio-wave span:nth-child(3) { height: 60px; animation-delay: 0.2s; }
.audio-wave span:nth-child(4) { height: 80px; animation-delay: 0.3s; }
.audio-wave span:nth-child(5) { height: 60px; animation-delay: 0.4s; }
.audio-wave span:nth-child(6) { height: 40px; animation-delay: 0.5s; }
.audio-wave span:nth-child(7) { height: 20px; animation-delay: 0.6s; }
@keyframes audio-bounce {
  0%, 100% { transform: scaleY(0.3); }
  50% { transform: scaleY(1); }
}

/* 星空背景 */
.starfield {
  width: 200px;
  height: 120px;
  background: radial-gradient(ellipse at center, #0d2137 0%, #0a1628 100%);
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}
.starfield::before,
.starfield::after {
  content: "";
  position: absolute;
  width: 2px;
  height: 2px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 
    20px 10px 0 #fff, 50px 30px 0 #00f0ff, 80px 15px 0 #fff,
    120px 40px 0 #00ff88, 150px 20px 0 #fff, 180px 50px 0 #a855f7,
    30px 60px 0 #fff, 70px 80px 0 #00f0ff, 110px 70px 0 #fff,
    140px 90px 0 #ec4899, 170px 75px 0 #fff, 10px 100px 0 #00ff88;
  animation: twinkle 2s ease-in-out infinite;
}
.starfield::after {
  animation-delay: 1s;
  box-shadow: 
    15px 25px 0 #00f0ff, 45px 45px 0 #fff, 75px 35px 0 #00ff88,
    105px 55px 0 #fff, 135px 30px 0 #a855f7, 165px 65px 0 #fff,
    25px 85px 0 #ec4899, 55px 95px 0 #fff, 95px 105px 0 #00f0ff;
}
@keyframes twinkle {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

/* 加载圆环 */
.tech-loader {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid rgba(0, 240, 255, 0.1);
  border-top-color: #00f0ff;
  position: relative;
  animation: tech-spin 1s linear infinite;
}
.tech-loader::before {
  content: "";
  position: absolute;
  inset: 8px;
  border-radius: 50%;
  border: 3px solid rgba(0, 255, 136, 0.1);
  border-bottom-color: #00ff88;
  animation: tech-spin 1.5s linear infinite reverse;
}
.tech-loader::after {
  content: "";
  position: absolute;
  inset: 20px;
  border-radius: 50%;
  border: 2px solid rgba(168, 85, 247, 0.1);
  border-left-color: #a855f7;
  animation: tech-spin 2s linear infinite;
}
@keyframes tech-spin {
  to { transform: rotate(360deg); }
}

/* 信号波 */
.signal-wave {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.signal-wave::before,
.signal-wave::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: signal-expand 2s ease-out infinite;
}
.signal-wave::before {
  width: 20px;
  height: 20px;
}
.signal-wave::after {
  width: 20px;
  height: 20px;
  animation-delay: 0.5s;
}
@keyframes signal-expand {
  0% { transform: scale(1); opacity: 1; border-color: #00f0ff; }
  50% { border-color: #00ff88; }
  100% { transform: scale(5); opacity: 0; border-color: #a855f7; }
}

/* 矩阵雨 */
.matrix-rain {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  font-family: monospace;
}
.matrix-rain::before,
.matrix-rain::after {
  content: "01001010";
  position: absolute;
  font-size: 12px;
  color: #00ff88;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  animation: matrix-fall 2s linear infinite;
  text-shadow: 0 0 10px #00ff88;
}
.matrix-rain::before {
  left: 20%;
  animation-delay: 0s;
}
.matrix-rain::after {
  left: 60%;
  content: "11010101";
  animation-delay: 1s;
}
@keyframes matrix-fall {
  0% { top: -80px; opacity: 1; }
  100% { top: 120px; opacity: 0.3; }
}

/* 光束扫射 */
.light-beam {
  width: 120px;
  height: 120px;
  position: relative;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.1) 0%, transparent 70%);
}
.light-beam::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #00f0ff, transparent);
  transform-origin: left center;
  animation: beam-rotate 2s linear infinite;
  box-shadow: 0 0 20px #00f0ff;
}
.light-beam::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff;
}
@keyframes beam-rotate {
  to { transform: rotate(360deg); }
}

/* 粒子爆发 */
.particle-burst {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.particle-burst::before {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
  animation: burst-center 2s ease-in-out infinite;
}
.particle-burst::after {
  content: "";
  position: absolute;
  width: 6px;
  height: 6px;
  background: #00ff88;
  border-radius: 50%;
  box-shadow: 
    30px 0 0 #00f0ff,
    -30px 0 0 #00ff88,
    0 30px 0 #a855f7,
    0 -30px 0 #ec4899,
    21px 21px 0 #00f0ff,
    -21px -21px 0 #00ff88,
    21px -21px 0 #a855f7,
    -21px 21px 0 #ec4899;
  animation: burst-particles 2s ease-in-out infinite;
}
@keyframes burst-center {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.5); }
}
@keyframes burst-particles {
  0%, 100% { transform: scale(0.5); opacity: 0; }
  50% { transform: scale(1); opacity: 1; }
}

/* 全息投影 */
.hologram {
  width: 160px;
  height: 100px;
  background: linear-gradient(180deg, rgba(0, 240, 255, 0.1) 0%, rgba(0, 240, 255, 0.05) 100%);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 10px #00f0ff;
  position: relative;
  animation: holo-flicker 0.1s steps(2) infinite;
}
.hologram::before {
  content: "";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent 0px,
    transparent 2px,
    rgba(0, 240, 255, 0.03) 2px,
    rgba(0, 240, 255, 0.03) 4px
  );
  pointer-events: none;
}
.hologram::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  border-radius: 8px 8px 0 0;
}
@keyframes holo-flicker {
  0% { opacity: 1; }
  50% { opacity: 0.95; }
}

/* 水波纹 */
.water-ripple {
  width: 150px;
  height: 150px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.water-ripple::before,
.water-ripple::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: water-spread 3s ease-out infinite;
}
.water-ripple::before {
  width: 30px;
  height: 30px;
}
.water-ripple::after {
  width: 30px;
  height: 30px;
  animation-delay: 1s;
}
@keyframes water-spread {
  0% { transform: scale(1); opacity: 1; border-width: 3px; }
  100% { transform: scale(4); opacity: 0; border-width: 1px; }
}

/* 指纹扫描 */
.fingerprint {
  width: 100px;
  height: 120px;
  position: relative;
  background: #0a1628;
  border-radius: 50px 50px 8px 8px;
  border: 2px solid rgba(0, 200, 255, 0.3);
  overflow: hidden;
}
.fingerprint::before {
  content: "";
  position: absolute;
  inset: 10px;
  background: repeating-linear-gradient(
    0deg,
    transparent 0px,
    transparent 3px,
    rgba(0, 240, 255, 0.2) 3px,
    rgba(0, 240, 255, 0.2) 4px
  );
  border-radius: 40px 40px 4px 4px;
}
.fingerprint::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  box-shadow: 0 0 20px #00f0ff;
  animation: finger-scan 2s ease-in-out infinite;
}
@keyframes finger-scan {
  0%, 100% { top: 10px; }
  50% { top: calc(100% - 14px); }
}

/* 闪电效果 */
.lightning {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  color: #00f0ff;
  text-shadow: 0 0 20px #00f0ff, 0 0 40px #00f0ff;
  animation: lightning-flash 2s ease-in-out infinite;
}
.lightning::before {
  content: "⚡";
}
.lightning::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.2) 0%, transparent 70%);
  animation: lightning-glow 2s ease-in-out infinite;
}
@keyframes lightning-flash {
  0%, 100% { opacity: 1; }
  10% { opacity: 0.3; }
  20% { opacity: 1; }
  30% { opacity: 0.5; }
}
@keyframes lightning-glow {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

/* 心电图 */
.heartbeat-line {
  width: 200px;
  height: 80px;
  position: relative;
  overflow: hidden;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
}
.heartbeat-line::before {
  content: "";
  position: absolute;
  width: 400px;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent 0%, transparent 10%,
    #00ff88 10%, #00ff88 10.5%,
    transparent 10.5%, transparent 20%,
    #00ff88 20%, #00ff88 22%,
    transparent 22%, transparent 25%,
    #00ff88 25%, #00ff88 30%,
    transparent 30%, transparent 50%
  );
  background-size: 100px 100%;
  animation: heartbeat-move 2s linear infinite;
}
@keyframes heartbeat-move {
  0% { transform: translateX(0); }
  100% { transform: translateX(-200px); }
}

/* 时钟指针 */
.clock-hand {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid rgba(0, 200, 255, 0.3);
  position: relative;
  background: #0a1628;
}
.clock-hand::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 3px;
  height: 35px;
  background: #00f0ff;
  transform-origin: bottom center;
  transform: translateX(-50%);
  animation: clock-tick 4s linear infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.clock-hand::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  background: #00ff88;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px #00ff88;
}
@keyframes clock-tick {
  to { transform: translateX(-50%) rotate(360deg); }
}

/* 齿轮转动 */
.gear-spin {
  width: 80px;
  height: 80px;
  position: relative;
}
.gear-spin::before {
  content: "";
  position: absolute;
  width: 60px;
  height: 60px;
  border: 8px dashed #00f0ff;
  border-radius: 50%;
  animation: gear-rotate 3s linear infinite;
}
.gear-spin::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 15px #00f0ff;
}
@keyframes gear-rotate {
  to { transform: rotate(360deg); }
}

/* 数据上传 */
.data-upload {
  width: 80px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}
.data-upload::before {
  content: "";
  width: 60px;
  height: 40px;
  border: 3px solid #00f0ff;
  border-radius: 8px;
  border-bottom: none;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}
.data-upload::after {
  content: "";
  width: 8px;
  height: 8px;
  background: #00ff88;
  border-radius: 50%;
  position: absolute;
  bottom: 50px;
  animation: upload-move 1s ease-in-out infinite;
  box-shadow: 0 0 10px #00ff88;
}
@keyframes upload-move {
  0% { bottom: 10px; opacity: 1; }
  100% { bottom: 80px; opacity: 0; }
}

/* 声波扩散 */
.sound-wave {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sound-wave::before {
  content: "";
  position: absolute;
  width: 30px;
  height: 30px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
}
.sound-wave::after {
  content: "";
  position: absolute;
  width: 30px;
  height: 30px;
  border: 3px solid #00f0ff;
  border-radius: 50%;
  animation: sound-expand 1.5s ease-out infinite;
  box-shadow: 
    0 0 0 15px rgba(0, 240, 255, 0.3),
    0 0 0 30px rgba(0, 240, 255, 0.2),
    0 0 0 45px rgba(0, 240, 255, 0.1);
}
@keyframes sound-expand {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(2.5); opacity: 0; }
}

/* 目标锁定 */
.target-lock {
  width: 100px;
  height: 100px;
  position: relative;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: target-pulse 2s ease-in-out infinite;
}
.target-lock::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #00f0ff 30%, transparent 30%, transparent 70%, #00f0ff 70%);
}
.target-lock::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #00f0ff 30%, transparent 30%, transparent 70%, #00f0ff 70%);
}
@keyframes target-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(0.9); opacity: 0.7; }
}

/* 卫星轨道 */
.satellite-orbit {
  width: 120px;
  height: 120px;
  position: relative;
  border: 2px dashed rgba(0, 200, 255, 0.3);
  border-radius: 50%;
}
.satellite-orbit::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff;
}
.satellite-orbit::after {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  background: #00ff88;
  border-radius: 50%;
  top: -5px;
  left: 50%;
  transform: translateX(-50%);
  box-shadow: 0 0 10px #00ff88;
  animation: orbit-move 3s linear infinite;
  transform-origin: center 65px;
}
@keyframes orbit-move {
  to { transform: translateX(-50%) rotate(360deg); }
}

/* 温度计 */
.thermometer {
  width: 40px;
  height: 120px;
  position: relative;
  background: #0a1628;
  border: 2px solid rgba(0, 200, 255, 0.3);
  border-radius: 20px 20px 40px 40px;
  overflow: hidden;
}
.thermometer::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 30px;
  background: #ec4899;
  border-radius: 50%;
  box-shadow: 0 0 15px #ec4899;
}
.thermometer::after {
  content: "";
  position: absolute;
  bottom: 25px;
  left: 50%;
  transform: translateX(-50%);
  width: 12px;
  background: linear-gradient(to top, #ec4899, #00f0ff);
  border-radius: 6px;
  animation: temp-rise 3s ease-in-out infinite;
}
@keyframes temp-rise {
  0%, 100% { height: 30px; }
  50% { height: 70px; }
}

/* 立体方块 */
.cube-3d {
  width: 80px;
  height: 80px;
  position: relative;
  transform-style: preserve-3d;
  animation: cube-spin 4s linear infinite;
}
.cube-3d::before {
  content: "";
  position: absolute;
  inset: 0;
  background: rgba(0, 240, 255, 0.3);
  border: 2px solid #00f0ff;
  transform: translateZ(40px);
}
.cube-3d::after {
  content: "";
  position: absolute;
  inset: 0;
  background: rgba(0, 255, 136, 0.3);
  border: 2px solid #00ff88;
  transform: rotateY(90deg) translateZ(40px);
}
@keyframes cube-spin {
  to { transform: rotateX(360deg) rotateY(360deg); }
}

/* 信号强度 */
.signal-strength {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
}
.signal-strength::after {
  content: "";
  width: 8px;
  background: #00f0ff;
  border-radius: 2px;
  animation: signal-bar 1.5s ease-in-out infinite;
  box-shadow: -12px 0 0 #00f0ff, -24px 0 0 #00f0ff, -36px 0 0 #00f0ff;
}
@keyframes signal-bar {
  0%, 100% { height: 15px; box-shadow: -12px 0 0 #00f0ff, -24px 0 0 rgba(0, 240, 255, 0.5), -36px 0 0 rgba(0, 240, 255, 0.3); }
  50% { height: 40px; box-shadow: -12px 0 0 #00f0ff, -24px 0 0 #00f0ff, -36px 0 0 #00f0ff; }
}

/* 双环旋转 */
.double-ring {
  width: 100px;
  height: 100px;
  position: relative;
}
.double-ring::before,
.double-ring::after {
  content: "";
  position: absolute;
  inset: 0;
  border: 3px solid transparent;
  border-radius: 50%;
}
.double-ring::before {
  border-top-color: #00f0ff;
  border-bottom-color: #00f0ff;
  animation: double-ring-spin 2s linear infinite;
}
.double-ring::after {
  border-left-color: #00ff88;
  border-right-color: #00ff88;
  animation: double-ring-spin 2s linear infinite reverse;
}
@keyframes double-ring-spin {
  to { transform: rotate(360deg); }
}

/* 数据节点 */
.data-node {
  width: 150px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.data-node::before {
  content: "";
  position: absolute;
  width: 12px;
  height: 12px;
  background: #00f0ff;
  border-radius: 50%;
  top: 20px;
  left: 20px;
  box-shadow: 100px 0 0 #00ff88, 50px 50px 0 #a855f7;
  animation: node-pulse 2s ease-in-out infinite;
}
.data-node::after {
  content: "";
  position: absolute;
  top: 26px;
  left: 32px;
  width: 80px;
  height: 2px;
  background: linear-gradient(90deg, #00f0ff, #00ff88);
  animation: node-connect 2s ease-in-out infinite;
}
@keyframes node-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
@keyframes node-connect {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

/* 电池充电 */
.battery-charge {
  width: 80px;
  height: 40px;
  border: 3px solid #00f0ff;
  border-radius: 6px;
  position: relative;
  background: #0a1628;
}
.battery-charge::before {
  content: "";
  position: absolute;
  right: -10px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 16px;
  background: #00f0ff;
  border-radius: 0 3px 3px 0;
}
.battery-charge::after {
  content: "";
  position: absolute;
  left: 4px;
  top: 4px;
  bottom: 4px;
  background: linear-gradient(90deg, #00f0ff, #00ff88);
  border-radius: 3px;
  animation: charge-fill 2s ease-in-out infinite;
}
@keyframes charge-fill {
  0%, 100% { width: 10%; }
  50% { width: 90%; }
}

/* 仪表盘 */
.gauge-meter {
  width: 120px;
  height: 60px;
  border: 4px solid rgba(0, 200, 255, 0.3);
  border-bottom: none;
  border-radius: 60px 60px 0 0;
  position: relative;
  background: #0a1628;
  overflow: hidden;
}
.gauge-meter::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 4px;
  height: 50px;
  background: #00f0ff;
  transform-origin: bottom center;
  animation: gauge-swing 3s ease-in-out infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.gauge-meter::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 50%;
  width: 14px;
  height: 14px;
  background: #00ff88;
  border-radius: 50%;
  transform: translateX(-50%);
}
@keyframes gauge-swing {
  0%, 100% { transform: rotate(-60deg); }
  50% { transform: rotate(60deg); }
}

/* 像素加载 */
.pixel-load {
  width: 80px;
  height: 20px;
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
}
.pixel-load::before {
  content: "";
  width: 16px;
  height: 16px;
  background: #00f0ff;
  animation: pixel-bounce 0.6s ease-in-out infinite;
  box-shadow: 20px 0 0 #00ff88, 40px 0 0 #a855f7;
}
@keyframes pixel-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 箭头流动 */
.arrow-flow {
  width: 150px;
  height: 40px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.arrow-flow::before {
  content: "→→→";
  font-size: 24px;
  color: #00f0ff;
  letter-spacing: 10px;
  animation: arrow-move 1.5s linear infinite;
  text-shadow: 0 0 10px #00f0ff;
}
@keyframes arrow-move {
  0% { opacity: 0; transform: translateX(-20px); }
  50% { opacity: 1; }
  100% { opacity: 0; transform: translateX(20px); }
}

/* 警报闪烁 */
.alert-blink {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle, #ec4899 0%, transparent 70%);
  position: relative;
  animation: alert-flash 0.5s ease-in-out infinite;
}
.alert-blink::before {
  content: "!";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 40px;
  font-weight: bold;
  color: #fff;
}
.alert-blink::after {
  content: "";
  position: absolute;
  inset: -5px;
  border: 3px solid #ec4899;
  border-radius: 50%;
  animation: alert-ring 1s ease-out infinite;
}
@keyframes alert-flash {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
@keyframes alert-ring {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* 涟漪按钮 */
.ripple-btn {
  width: 120px;
  height: 50px;
  background: linear-gradient(135deg, #00f0ff, #00ff88);
  border-radius: 25px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0a1628;
  font-weight: bold;
  font-size: 14px;
}
.ripple-btn::before {
  content: "CLICK";
}
.ripple-btn::after {
  content: "";
  position: absolute;
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  animation: ripple-spread 1.5s ease-out infinite;
}
@keyframes ripple-spread {
  0% { transform: scale(0); opacity: 1; }
  100% { transform: scale(6); opacity: 0; }
}

/* 光标闪烁 */
.cursor-blink {
  width: 150px;
  height: 40px;
  background: #0a1628;
  border: 1px solid rgba(0, 200, 255, 0.3);
  border-radius: 4px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  font-family: monospace;
  color: #00ff88;
  font-size: 12px;
}
.cursor-blink::before {
  content: "root@server:~$";
}
.cursor-blink::after {
  content: "";
  width: 8px;
  height: 18px;
  background: #00f0ff;
  margin-left: 5px;
  animation: cursor-flash 1s steps(1) infinite;
}
@keyframes cursor-flash {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* 蜂巢脉动 */
.honeycomb {
  width: 100px;
  height: 100px;
  position: relative;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  background: linear-gradient(135deg, #00f0ff, #00ff88);
  animation: honeycomb-pulse 2s ease-in-out infinite;
}
.honeycomb::before {
  content: "";
  position: absolute;
  inset: 4px;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  background: #0a1628;
}
@keyframes honeycomb-pulse {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 1; }
}

/* 菱形闪耀 */
.diamond-shine {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #00f0ff, #a855f7);
  transform: rotate(45deg);
  position: relative;
  animation: diamond-glow 2s ease-in-out infinite;
  box-shadow: 0 0 20px #00f0ff;
}
.diamond-shine::before {
  content: "";
  position: absolute;
  inset: 8px;
  background: #0a1628;
}
@keyframes diamond-glow {
  0%, 100% { box-shadow: 0 0 20px #00f0ff; }
  50% { box-shadow: 0 0 40px #a855f7, 0 0 60px #00f0ff; }
}

/* 三角旋涡 */
.triangle-vortex {
  width: 0;
  height: 0;
  border-left: 50px solid transparent;
  border-right: 50px solid transparent;
  border-bottom: 86px solid #00f0ff;
  position: relative;
  animation: vortex-spin 2s linear infinite;
  filter: drop-shadow(0 0 10px #00f0ff);
}
.triangle-vortex::before {
  content: "";
  position: absolute;
  top: 30px;
  left: -30px;
  width: 0;
  height: 0;
  border-left: 30px solid transparent;
  border-right: 30px solid transparent;
  border-bottom: 52px solid #0a1628;
}
@keyframes vortex-spin {
  to { transform: rotate(360deg); }
}

/* 同心圆扩散 */
.concentric {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.concentric::before,
.concentric::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: concentric-expand 2s ease-out infinite;
}
.concentric::before {
  width: 20px;
  height: 20px;
}
.concentric::after {
  width: 20px;
  height: 20px;
  animation-delay: 0.6s;
}
@keyframes concentric-expand {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(5); opacity: 0; }
}

/* 眼睛眨动 */
.eye-blink {
  width: 80px;
  height: 40px;
  background: #0a1628;
  border: 3px solid #00f0ff;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
}
.eye-blink::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 10px #00f0ff;
}
.eye-blink::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: #0a1628;
  transform-origin: bottom;
  animation: eyelid-blink 3s ease-in-out infinite;
}
@keyframes eyelid-blink {
  0%, 90%, 100% { transform: scaleY(0); }
  95% { transform: scaleY(1); }
}

/* 摆钟摇摆 */
.pendulum {
  width: 4px;
  height: 80px;
  background: linear-gradient(to bottom, #00f0ff, #00ff88);
  position: relative;
  transform-origin: top center;
  animation: pendulum-swing 2s ease-in-out infinite;
}
.pendulum::after {
  content: "";
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, #00f0ff, #0a1628);
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
}
@keyframes pendulum-swing {
  0%, 100% { transform: rotate(30deg); }
  50% { transform: rotate(-30deg); }
}

/* 弹簧伸缩 */
.spring-bounce {
  width: 40px;
  height: 60px;
  position: relative;
}
.spring-bounce::before {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(0deg, transparent 0px, transparent 8px, #00f0ff 8px, #00f0ff 10px);
  animation: spring-stretch 1s ease-in-out infinite;
}
.spring-bounce::after {
  content: "";
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 20px;
  background: #00ff88;
  border-radius: 4px;
  animation: spring-ball 1s ease-in-out infinite;
}
@keyframes spring-stretch {
  0%, 100% { transform: scaleY(1); }
  50% { transform: scaleY(1.5); }
}
@keyframes spring-ball {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(30px); }
}

/* 沙漏计时 */
.hourglass {
  width: 60px;
  height: 100px;
  position: relative;
}
.hourglass::before,
.hourglass::after {
  content: "";
  position: absolute;
  left: 0;
  width: 0;
  height: 0;
  border-left: 30px solid transparent;
  border-right: 30px solid transparent;
}
.hourglass::before {
  top: 0;
  border-top: 45px solid #00f0ff;
  animation: sand-top 2s linear infinite;
}
.hourglass::after {
  bottom: 0;
  border-bottom: 45px solid #00ff88;
  animation: sand-bottom 2s linear infinite;
}
@keyframes sand-top {
  0% { border-top-width: 45px; }
  100% { border-top-width: 5px; }
}
@keyframes sand-bottom {
  0% { border-bottom-width: 5px; }
  100% { border-bottom-width: 45px; }
}

/* 磁场线条 */
.magnetic-field {
  width: 150px;
  height: 100px;
  position: relative;
  overflow: hidden;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.magnetic-field::before {
  content: "";
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background: repeating-conic-gradient(from 0deg, transparent 0deg, transparent 10deg, rgba(0, 240, 255, 0.1) 10deg, rgba(0, 240, 255, 0.1) 20deg);
  animation: magnetic-rotate 10s linear infinite;
}
.magnetic-field::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff;
}
@keyframes magnetic-rotate {
  to { transform: rotate(360deg); }
}

/* 水波纹 */
.water-ripple {
  width: 150px;
  height: 150px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.water-ripple::before,
.water-ripple::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: water-expand 3s ease-out infinite;
}
.water-ripple::before {
  width: 30px;
  height: 30px;
  animation-delay: 0s;
}
.water-ripple::after {
  width: 30px;
  height: 30px;
  animation-delay: 1s;
}
@keyframes water-expand {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(4); opacity: 0; }
}

/* 闪电效果 */
.lightning {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lightning::before {
  content: "⚡";
  font-size: 60px;
  color: #00f0ff;
  text-shadow: 0 0 20px #00f0ff, 0 0 40px #00f0ff;
  animation: lightning-flash 2s ease-in-out infinite;
}
@keyframes lightning-flash {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  10% { opacity: 1; transform: scale(1.2); }
  20% { opacity: 0.5; transform: scale(1); }
  30% { opacity: 1; transform: scale(1.1); }
  40% { opacity: 0.3; transform: scale(1); }
}

/* 齿轮旋转 */
.gear-rotate {
  width: 80px;
  height: 80px;
  position: relative;
}
.gear-rotate::before {
  content: "⚙";
  font-size: 70px;
  color: #00f0ff;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: gear-spin 3s linear infinite;
  text-shadow: 0 0 10px #00f0ff;
}
.gear-rotate::after {
  content: "⚙";
  font-size: 40px;
  color: #00ff88;
  position: absolute;
  top: -10px;
  right: -20px;
  animation: gear-spin-reverse 2s linear infinite;
  text-shadow: 0 0 10px #00ff88;
}
@keyframes gear-spin {
  to { transform: translate(-50%, -50%) rotate(360deg); }
}
@keyframes gear-spin-reverse {
  to { transform: rotate(-360deg); }
}

/* 心跳监测 */
.heartbeat {
  width: 200px;
  height: 80px;
  position: relative;
  overflow: hidden;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
}
.heartbeat::before {
  content: "";
  position: absolute;
  width: 400px;
  height: 2px;
  background: repeating-linear-gradient(
    90deg,
    #00f0ff 0px, #00f0ff 30px,
    transparent 30px, transparent 35px,
    #00f0ff 35px, #00f0ff 40px,
    #00ff88 40px, #00ff88 45px,
    #00f0ff 45px, #00f0ff 50px,
    transparent 50px, transparent 100px
  );
  top: 50%;
  left: 0;
  animation: heartbeat-move 2s linear infinite;
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes heartbeat-move {
  0% { transform: translateX(0); }
  100% { transform: translateX(-200px); }
}

/* 指南针 */
.compass {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid rgba(0, 200, 255, 0.5);
  position: relative;
  background: radial-gradient(circle, #0d2137 0%, #0a1628 100%);
}
.compass::before {
  content: "";
  position: absolute;
  top: 10%;
  left: 50%;
  width: 4px;
  height: 40%;
  background: linear-gradient(to bottom, #ec4899, #00f0ff);
  transform-origin: bottom center;
  transform: translateX(-50%);
  animation: compass-swing 3s ease-in-out infinite;
  border-radius: 2px;
}
.compass::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes compass-swing {
  0%, 100% { transform: translateX(-50%) rotate(-30deg); }
  50% { transform: translateX(-50%) rotate(30deg); }
}

/* 时钟动画 */
.tech-clock {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid #00f0ff;
  position: relative;
  background: #0a1628;
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.3);
}
.tech-clock::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 3px;
  height: 30px;
  background: #00f0ff;
  transform-origin: bottom center;
  transform: translate(-50%, -100%);
  animation: clock-hour 12s linear infinite;
  border-radius: 2px;
  box-shadow: 0 0 10px #00f0ff;
}
.tech-clock::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 2px;
  height: 40px;
  background: #00ff88;
  transform-origin: bottom center;
  transform: translate(-50%, -100%);
  animation: clock-minute 60s linear infinite;
  border-radius: 2px;
  box-shadow: 0 0 10px #00ff88;
}
@keyframes clock-hour {
  to { transform: translate(-50%, -100%) rotate(360deg); }
}
@keyframes clock-minute {
  to { transform: translate(-50%, -100%) rotate(360deg); }
}

/* 火焰效果 */
.flame {
  width: 60px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.flame::before {
  content: "";
  width: 40px;
  height: 60px;
  background: linear-gradient(to top, #ff6b35, #f7931e, #ffcc02);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: flame-flicker 0.5s ease-in-out infinite alternate;
  box-shadow: 0 0 30px #ff6b35, 0 0 60px #f7931e;
}
.flame::after {
  content: "";
  position: absolute;
  bottom: 0;
  width: 20px;
  height: 30px;
  background: linear-gradient(to top, #00f0ff, #00ff88);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: flame-flicker 0.3s ease-in-out infinite alternate-reverse;
}
@keyframes flame-flicker {
  0% { transform: scaleY(1) scaleX(1); }
  100% { transform: scaleY(1.1) scaleX(0.9); }
}

/* 卫星轨道 */
.satellite-orbit {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.satellite-orbit::before {
  content: "";
  position: absolute;
  width: 100px;
  height: 100px;
  border: 2px solid rgba(0, 240, 255, 0.3);
  border-radius: 50%;
}
.satellite-orbit::after {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 15px #00f0ff;
  animation: satellite-move 3s linear infinite;
}
@keyframes satellite-move {
  0% { transform: rotate(0deg) translateX(50px) rotate(0deg); }
  100% { transform: rotate(360deg) translateX(50px) rotate(-360deg); }
}

/* 音量指示 */
.volume-bar {
  width: 150px;
  height: 80px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
}
.volume-bar::before,
.volume-bar::after {
  content: "";
  width: 20px;
  background: linear-gradient(to top, #00f0ff, #00ff88, #a855f7);
  border-radius: 4px 4px 0 0;
  animation: volume-bounce 0.8s ease-in-out infinite;
}
.volume-bar::before {
  height: 60px;
  animation-delay: 0s;
}
.volume-bar::after {
  height: 40px;
  animation-delay: 0.2s;
}
@keyframes volume-bounce {
  0%, 100% { transform: scaleY(0.3); }
  50% { transform: scaleY(1); }
}

/* 目标锁定 */
.target-lock {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.target-lock::before {
  content: "";
  position: absolute;
  width: 80px;
  height: 80px;
  border: 3px solid #00f0ff;
  border-radius: 50%;
  animation: target-pulse 1.5s ease-in-out infinite;
}
.target-lock::after {
  content: "+";
  font-size: 40px;
  color: #00f0ff;
  font-weight: 100;
  text-shadow: 0 0 10px #00f0ff;
  animation: target-rotate 4s linear infinite;
}
@keyframes target-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.7; }
}
@keyframes target-rotate {
  to { transform: rotate(360deg); }
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
