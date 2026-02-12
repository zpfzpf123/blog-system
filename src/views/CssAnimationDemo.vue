<template>
  <div class="animation-lab light-lab">
    <!-- 页面标题 -->
    <header class="lab-header">
      <div class="header-content">
        <div class="header-text">
          <h1>CSS 动画实验室</h1>
          <p>精选 {{ animations.length }} 个实用动画效果</p>
        </div>
        <div class="header-actions">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input 
              v-model="searchQuery" 
              placeholder="搜索动画..." 
              type="text"
            />
          </div>
          <button class="add-btn-primary" @click="openAddModal">
            <span>+</span> 新增动画
          </button>
        </div>
      </div>
    </header>

    <section class="lab-overview">
      <article class="overview-card">
        <span class="overview-label">动画总数</span>
        <strong class="overview-value">{{ animations.length }}</strong>
      </article>
      <article class="overview-card">
        <span class="overview-label">筛选结果</span>
        <strong class="overview-value">{{ displayedAnimations.length }}</strong>
      </article>
      <article class="overview-card">
        <span class="overview-label">分类数量</span>
        <strong class="overview-value">{{ categories.length }}</strong>
      </article>
      <article class="overview-card overview-card-wide">
        <span class="overview-label">当前分类</span>
        <strong class="overview-value">{{ allCategories.find((cat) => cat.key === activeCategory)?.label || '全部' }}</strong>
      </article>
    </section>

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
              <template v-else-if="item.title.includes('堆叠')">
                <!-- 堆叠方块使用伪元素 -->
              </template>
              <template v-else-if="item.title === 'DNA螺旋'">
                <!-- DNA螺旋需要3个span -->
                <span></span><span></span><span></span>
              </template>
              <template v-else-if="item.title === '音波'">
                <!-- 音波需要5个span -->
                <span></span><span></span><span></span><span></span><span></span>
              </template>
              <template v-else>
                <!-- 默认方块元素（弹跳方块、方块旋转、翻转方块等） -->
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
                <div class="dna-helix">
                  <span></span><span></span><span></span><span></span><span></span>
                </div>
              </div>
              <!-- 音频波形 -->
              <div v-else-if="item.title === '音频波形'" class="bigscreen-container">
                <div class="audio-wave">
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
              <!-- 科技边框 -->
              <div v-else-if="item.title === '科技边框'" class="bigscreen-container">
                <div class="box tech-border"></div>
              </div>
              <!-- 数据瀑布 -->
              <div v-else-if="item.title === '数据瀑布'" class="bigscreen-container">
                <div class="box data-waterfall"></div>
              </div>
              <!-- 环形仪表 -->
              <div v-else-if="item.title === '环形仪表'" class="bigscreen-container">
                <div class="box ring-gauge"></div>
              </div>
              <!-- 粒子星云 -->
              <div v-else-if="item.title === '粒子星云'" class="bigscreen-container">
                <div class="box particle-nebula"></div>
              </div>
              <!-- 数据隧道 -->
              <div v-else-if="item.title === '数据隧道'" class="bigscreen-container">
                <div class="box data-tunnel"></div>
              </div>
              <!-- 脉冲网格 -->
              <div v-else-if="item.title === '脉冲网格'" class="bigscreen-container">
                <div class="box pulse-grid"></div>
              </div>
              <!-- 旋转星环 -->
              <div v-else-if="item.title === '旋转星环'" class="bigscreen-container">
                <div class="box star-ring"></div>
              </div>
              <!-- 数据波动 -->
              <div v-else-if="item.title === '数据波动'" class="bigscreen-container">
                <div class="box data-wave"></div>
              </div>
              <!-- 能量柱 -->
              <div v-else-if="item.title === '能量柱'" class="bigscreen-container">
                <div class="box energy-bar"></div>
              </div>
              <!-- 科技圆环 -->
              <div v-else-if="item.title === '科技圆环'" class="bigscreen-container">
                <div class="box tech-circle"></div>
              </div>
              <!-- 流光卡片 -->
              <div v-else-if="item.title === '流光卡片'" class="bigscreen-container">
                <div class="box glow-card"></div>
              </div>
              <!-- 数据球体 -->
              <div v-else-if="item.title === '数据球体'" class="bigscreen-container">
                <div class="box data-sphere"></div>
              </div>
              <!-- 扫描雷达 -->
              <div v-else-if="item.title === '扫描雷达'" class="bigscreen-container">
                <div class="box scan-radar"></div>
              </div>
              <!-- 能量波纹 -->
              <div v-else-if="item.title === '能量波纹'" class="bigscreen-container">
                <div class="box energy-ripple"></div>
              </div>
              <!-- 科技标题 -->
              <div v-else-if="item.title === '科技标题'" class="bigscreen-container">
                <div class="box tech-title">DATA</div>
              </div>
              <!-- 粒子轨迹 -->
              <div v-else-if="item.title === '粒子轨迹'" class="bigscreen-container">
                <div class="box particle-trail"></div>
              </div>
              <!-- 数据环绕 -->
              <div v-else-if="item.title === '数据环绕'" class="bigscreen-container">
                <div class="box data-orbit"></div>
              </div>
              <!-- 光束聚焦 -->
              <div v-else-if="item.title === '光束聚焦'" class="bigscreen-container">
                <div class="box beam-focus"></div>
              </div>
              <!-- 科技按钮 -->
              <div v-else-if="item.title === '科技按钮'" class="bigscreen-container">
                <div class="box tech-button">START</div>
              </div>
              <!-- 赛博朋克线条 -->
              <div v-else-if="item.title === '赛博朋克线条'" class="bigscreen-container">
                <div class="box cyber-lines"></div>
              </div>
              <!-- 故障文字 -->
              <div v-else-if="item.title === '故障文字'" class="bigscreen-container">
                <div class="box glitch-text">ERROR</div>
              </div>
              <!-- 液态金属 -->
              <div v-else-if="item.title === '液态金属'" class="bigscreen-container">
                <div class="box liquid-metal"></div>
              </div>
              <!-- 音乐均衡器 -->
              <div v-else-if="item.title === '音乐均衡器'" class="bigscreen-container">
                <div class="box music-equalizer"></div>
              </div>
              <!-- 棱镜折射 -->
              <div v-else-if="item.title === '棱镜折射'" class="bigscreen-container">
                <div class="box prism-refract"></div>
              </div>
              <!-- 太空站舱门 -->
              <div v-else-if="item.title === '太空站舱门'" class="bigscreen-container">
                <div class="box airlock-door"></div>
              </div>
              <!-- 全息投影仪 -->
              <div v-else-if="item.title === '全息投影仪'" class="bigscreen-container">
                <div class="box holo-projector"></div>
              </div>
              <!-- 量子纠缠 -->
              <div v-else-if="item.title === '量子纠缠'" class="bigscreen-container">
                <div class="box quantum-entangle"></div>
              </div>
              <!-- 数据加密 -->
              <div v-else-if="item.title === '数据加密'" class="bigscreen-container">
                <div class="box data-encrypt"></div>
              </div>
              <!-- 星际跃迁 -->
              <div v-else-if="item.title === '星际跃迁'" class="bigscreen-container">
                <div class="box warp-drive"></div>
              </div>
              <!-- 能量充能 -->
              <div v-else-if="item.title === '能量充能'" class="bigscreen-container">
                <div class="box energy-charge"></div>
              </div>
              <!-- 分子结构 -->
              <div v-else-if="item.title === '分子结构'" class="bigscreen-container">
                <div class="box molecule"></div>
              </div>
              <!-- 信息流 -->
              <div v-else-if="item.title === '信息流'" class="bigscreen-container">
                <div class="box info-stream"></div>
              </div>
              <!-- 防火墙 -->
              <div v-else-if="item.title === '防火墙'" class="bigscreen-container">
                <div class="box firewall"></div>
              </div>
              <!-- 波形示波器 -->
              <div v-else-if="item.title === '波形示波器'" class="bigscreen-container">
                <div class="box oscilloscope"></div>
              </div>
              <!-- 代码雨 -->
              <div v-else-if="item.title === '代码雨'" class="bigscreen-container">
                <div class="box code-rain"></div>
              </div>
              <!-- 脑电波 -->
              <div v-else-if="item.title === '脑电波'" class="bigscreen-container">
                <div class="box brain-wave"></div>
              </div>
              <!-- 声纹识别 -->
              <div v-else-if="item.title === '声纹识别'" class="bigscreen-container">
                <div class="box voice-print"></div>
              </div>
              <!-- 虚拟键盘 -->
              <div v-else-if="item.title === '虚拟键盘'" class="bigscreen-container">
                <div class="box virtual-keyboard"></div>
              </div>
              <!-- 人脸识别 -->
              <div v-else-if="item.title === '人脸识别'" class="bigscreen-container">
                <div class="box face-scan"></div>
              </div>
              <!-- 数据同步 -->
              <div v-else-if="item.title === '数据同步'" class="bigscreen-container">
                <div class="box data-sync"></div>
              </div>
              <!-- 网络拓扑 -->
              <div v-else-if="item.title === '网络拓扑'" class="bigscreen-container">
                <div class="box network-topology"></div>
              </div>
              <!-- 云端存储 -->
              <div v-else-if="item.title === '云端存储'" class="bigscreen-container">
                <div class="box cloud-storage"></div>
              </div>
              <!-- 安全验证 -->
              <div v-else-if="item.title === '安全验证'" class="bigscreen-container">
                <div class="box security-check"></div>
              </div>
              <!-- 温度监控 -->
              <div v-else-if="item.title === '温度监控'" class="bigscreen-container">
                <div class="box temp-monitor"></div>
              </div>
              <!-- 电量显示 -->
              <div v-else-if="item.title === '电量显示'" class="bigscreen-container">
                <div class="box power-display"></div>
              </div>
              <!-- 速度仪表 -->
              <div v-else-if="item.title === '速度仪表'" class="bigscreen-container">
                <div class="box speed-gauge"></div>
              </div>
              <!-- 信号塔 -->
              <div v-else-if="item.title === '信号塔'" class="bigscreen-container">
                <div class="box signal-tower"></div>
              </div>
              <!-- 数据库 -->
              <div v-else-if="item.title === '数据库'" class="bigscreen-container">
                <div class="box database-icon"></div>
              </div>
              <!-- CPU监控 -->
              <div v-else-if="item.title === 'CPU监控'" class="bigscreen-container">
                <div class="box cpu-monitor"></div>
              </div>
              <!-- 内存条 -->
              <div v-else-if="item.title === '内存条'" class="bigscreen-container">
                <div class="box memory-bar"></div>
              </div>
              <!-- 磁盘扫描 -->
              <div v-else-if="item.title === '磁盘扫描'" class="bigscreen-container">
                <div class="box disk-scan"></div>
              </div>
              <!-- 网速监测 -->
              <div v-else-if="item.title === '网速监测'" class="bigscreen-container">
                <div class="box network-speed"></div>
              </div>
              <!-- 服务器状态 -->
              <div v-else-if="item.title === '服务器状态'" class="bigscreen-container">
                <div class="box server-status"></div>
              </div>
              <!-- 时间戳 -->
              <div v-else-if="item.title === '时间戳'" class="bigscreen-container">
                <div class="box timestamp"></div>
              </div>
              <!-- 坐标定位 -->
              <div v-else-if="item.title === '坐标定位'" class="bigscreen-container">
                <div class="box gps-location"></div>
              </div>
              <!-- 二维码 -->
              <div v-else-if="item.title === '二维码'" class="bigscreen-container">
                <div class="box qr-code"></div>
              </div>
              <!-- 蓝牙连接 -->
              <div v-else-if="item.title === '蓝牙连接'" class="bigscreen-container">
                <div class="box bluetooth"></div>
              </div>
              <!-- WiFi信号 -->
              <div v-else-if="item.title === 'WiFi信号'" class="bigscreen-container">
                <div class="box wifi-signal"></div>
              </div>
              <!-- 音频播放 -->
              <div v-else-if="item.title === '音频播放'" class="bigscreen-container">
                <div class="box audio-player"></div>
              </div>
              <!-- 视频帧 -->
              <div v-else-if="item.title === '视频帧'" class="bigscreen-container">
                <div class="box video-frame"></div>
              </div>
              <!-- 3D立方体 -->
              <div v-else-if="item.title === '3D立方体'" class="bigscreen-container">
                <div class="box cube-3d-rotate"></div>
              </div>
              <!-- 数据饼图 -->
              <div v-else-if="item.title === '数据饼图'" class="bigscreen-container">
                <div class="box pie-chart"></div>
              </div>
              <!-- 折线图 -->
              <div v-else-if="item.title === '折线图'" class="bigscreen-container">
                <div class="box line-chart"></div>
              </div>
              <!-- 柱状图 -->
              <div v-else-if="item.title === '柱状图'" class="bigscreen-container">
                <div class="box bar-chart"></div>
              </div>
              <!-- 指纹解锁 -->
              <div v-else-if="item.title === '指纹解锁'" class="bigscreen-container">
                <div class="box fingerprint-unlock"></div>
              </div>
              <!-- 虹膜扫描 -->
              <div v-else-if="item.title === '虹膜扫描'" class="bigscreen-container">
                <div class="box iris-scan"></div>
              </div>
              <!-- 心率监测 -->
              <div v-else-if="item.title === '心率监测'" class="bigscreen-container">
                <div class="box heart-rate"></div>
              </div>
              <!-- 血氧饱和 -->
              <div v-else-if="item.title === '血氧饱和'" class="bigscreen-container">
                <div class="box oxygen-level"></div>
              </div>
              <!-- 步数统计 -->
              <div v-else-if="item.title === '步数统计'" class="bigscreen-container">
                <div class="box step-counter"></div>
              </div>
              <!-- 卡路里 -->
              <div v-else-if="item.title === '卡路里'" class="bigscreen-container">
                <div class="box calorie-burn"></div>
              </div>
              <!-- 天气图标 -->
              <div v-else-if="item.title === '天气图标'" class="bigscreen-container">
                <div class="box weather-icon"></div>
              </div>
              <!-- 风速指示 -->
              <div v-else-if="item.title === '风速指示'" class="bigscreen-container">
                <div class="box wind-speed"></div>
              </div>
              <!-- 湿度计 -->
              <div v-else-if="item.title === '湿度计'" class="bigscreen-container">
                <div class="box humidity-gauge"></div>
              </div>
              <!-- 气压表 -->
              <div v-else-if="item.title === '气压表'" class="bigscreen-container">
                <div class="box pressure-meter"></div>
              </div>
              <!-- 紫外线 -->
              <div v-else-if="item.title === '紫外线'" class="bigscreen-container">
                <div class="box uv-index"></div>
              </div>
              <!-- 日出日落 -->
              <div v-else-if="item.title === '日出日落'" class="bigscreen-container">
                <div class="box sunrise-sunset"></div>
              </div>
              <!-- 月相显示 -->
              <div v-else-if="item.title === '月相显示'" class="bigscreen-container">
                <div class="box moon-phase"></div>
              </div>
              <!-- 潮汐指示 -->
              <div v-else-if="item.title === '潮汐指示'" class="bigscreen-container">
                <div class="box tide-indicator"></div>
              </div>
              <!-- 空气质量 -->
              <div v-else-if="item.title === '空气质量'" class="bigscreen-container">
                <div class="box air-quality"></div>
              </div>
              <!-- 默认 -->
              <div v-else class="bigscreen-container">
                <div class="box" :class="getAnimClass(item)"></div>
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

      <div v-if="displayedAnimations.length === 0" class="no-results">
        <span class="empty-icon">🔍</span>
        <p>没有找到相关动画</p>
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
const searchQuery = ref('')

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
  let result = animations.value
  
  // Category filter
  if (activeCategory.value !== 'all') {
    result = result.filter(a => a.category === activeCategory.value)
  }
  
  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(a => 
      a.title.toLowerCase().includes(query) || 
      a.description.toLowerCase().includes(query)
    )
  }
  
  return result
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
    
    // 调试：打印弹跳方块的CSS
    if (a.title === '弹跳方块') {
      console.log('弹跳方块原始CSS:', a.cssCode)
      console.log('弹跳方块处理后CSS:', code)
    }
    
    return code
  }).join('\n')
  el.textContent = css
  
  // 调试：打印完整的动态CSS
  console.log('动态注入的CSS:', css)
}

watch(animations, injectStyles, { deep: true })

onMounted(async () => {
  try {
    const [aRes, cRes] = await Promise.all([fetch(API), fetch(CAT_API)])
    if (aRes.ok) {
      animations.value = await aRes.json()
      console.log('Animation Lab - Loaded animations:', animations.value.length)
      // Debug: Log Big Screen animations
      const bigScreenAnims = animations.value.filter(a => a.category === '大屏动画')
      console.log('Big Screen Animations:', bigScreenAnims.map(a => a.title))
      
      // Debug: 检查DNA螺旋和音频波形
      const dnaAnims = animations.value.filter(a => a.title.includes('DNA螺旋'))
      const audioAnims = animations.value.filter(a => a.title.includes('音频波形'))
      console.log('DNA螺旋动画:', dnaAnims.map(a => ({ id: a.id, title: a.title, category: a.category })))
      console.log('音频波形动画:', audioAnims.map(a => ({ id: a.id, title: a.title, category: a.category })))
    }
    if (cRes.ok) categories.value = await cRes.json()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.animation-lab {
  min-height: 100vh;
  background: #0f172a;
  background-image: 
    radial-gradient(at 0% 0%, rgba(139, 92, 246, 0.15) 0px, transparent 50%),
    radial-gradient(at 100% 0%, rgba(6, 182, 212, 0.15) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(236, 72, 153, 0.15) 0px, transparent 50%);
  padding: 40px 60px;
  color: #f8fafc;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

/* 头部设计 */
.lab-header {
  margin-bottom: 60px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  max-width: 1600px;
  margin: 0 auto;
  padding-bottom: 30px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.header-text h1 {
  font-size: 3.5rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #fff 0%, #94a3b8 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 12px;
}

.header-text p {
  color: #64748b;
  font-size: 1.1rem;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 20px;
  align-items: center;
}

/* 搜索框 */
.search-box {
  position: relative;
  width: 300px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  font-size: 1rem;
}

.search-box input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  color: #fff;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.search-box input:focus {
  outline: none;
  background: rgba(30, 41, 59, 0.9);
  border-color: #8b5cf6;
  box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.15);
}

/* 新增按钮 */
.add-btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.3);
}

.add-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.4);
}

/* 分类导航 */
.category-nav {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 50px;
  flex-wrap: wrap;
  margin-left: auto;
  margin-right: auto;
}

.cat-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(30, 41, 59, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 100px;
  color: #94a3b8;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

.cat-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  transform: translateY(-1px);
}

.cat-btn.active {
  background: #fff;
  color: #0f172a;
  border-color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 20px rgba(255, 255, 255, 0.2);
}

.cat-count {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 700;
}

.cat-btn.active .cat-count {
  background: rgba(0, 0, 0, 0.08);
  color: #0f172a;
}

/* 动画网格 */
.animation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 32px;
  max-width: 1600px;
  margin: 0 auto;
}

.anim-card {
  background: rgba(30, 41, 59, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  position: relative;
}

.anim-card:hover {
  border-color: rgba(139, 92, 246, 0.3);
  transform: translateY(-8px);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(139, 92, 246, 0.1);
  background: rgba(30, 41, 59, 0.6);
}

/* 预览区 */
.preview-area {
  height: 240px;
  background: radial-gradient(circle at center, rgba(139, 92, 246, 0.08) 0%, transparent 70%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
}

/* 无结果状态 */
.no-results {
  grid-column: 1 / -1;
  text-align: center;
  padding: 100px 0;
  color: #64748b;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 20px;
  opacity: 0.5;
}



/* ========== 各类演示容器 ========== */

/* 加载动画容器 */
.demo-loader {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 当加载动画容器没有子元素时（如弹跳方块），容器本身作为动画元素 */
.demo-loader:empty {
  display: block !important;
}

/* 确保弹跳方块等动画类能正确应用到空容器 */
.demo-loader.loader-bounce {
  display: block !important;
  width: 20px !important;
  height: 20px !important;
  background: linear-gradient(135deg, #8b5cf6, #f72585) !important;
  border-radius: 4px !important;
  animation: bounce-demo 0.6s ease-in-out infinite alternate !important;
}

@keyframes bounce-demo {
  from { transform: translateY(0) scale(1, 1); }
  to { transform: translateY(-25px) scale(0.9, 1.1); }
}

.demo-loader.loader-square {
  display: block !important;
}

.demo-loader.loader-flip {
  display: block !important;
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
  position: relative;
  z-index: 1;
}

/* 旋转边框按钮 */
.demo-button.btn-rotate-border {
  background: transparent;
  overflow: hidden;
}

.demo-button.btn-rotate-border::before {
  content: "";
  position: absolute;
  inset: -50%;
  background: conic-gradient(#8b5cf6, #f72585, #06b6d4, #8b5cf6);
  animation: btn-rotate-border 3s linear infinite;
  z-index: -2;
}

.demo-button.btn-rotate-border::after {
  content: "";
  position: absolute;
  inset: 3px;
  background: #1a1a3e;
  border-radius: 9px;
  z-index: -1;
}

@keyframes btn-rotate-border {
  to { transform: rotate(360deg); }
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
  background: #1a1a3e;
  border-radius: 16px;
  position: relative;
}

/* 边框旋转渐变动画 */
.demo-border.border-gradient-spin {
  overflow: hidden;
}

.demo-border.border-gradient-spin::before {
  content: "";
  position: absolute;
  inset: -50%;
  background: conic-gradient(#8b5cf6, #f72585, #06b6d4, #8b5cf6);
  animation: gradient-spin-border 3s linear infinite;
}

.demo-border.border-gradient-spin::after {
  content: "";
  position: absolute;
  inset: 4px;
  background: #1a1a3e;
  border-radius: inherit;
}

@keyframes gradient-spin-border {
  to { transform: rotate(360deg); }
}

/* 普通边框旋转动画 */
.demo-border.btn-border-rotate {
  border: 3px solid;
  border-image: linear-gradient(45deg, #8b5cf6, #f72585, #06b6d4) 1;
  animation: border-hue 3s linear infinite;
}

@keyframes border-hue {
  to { filter: hue-rotate(360deg); }
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

/* DNA螺旋 - 使用:deep()穿透scoped */
:deep(.dna-helix) {
  width: 80px !important;
  height: 100px !important;
  position: relative !important;
  display: flex !important;
  flex-direction: column !important;
  justify-content: space-around !important;
  align-items: center !important;
  background: rgba(0, 240, 255, 0.1);
  border-radius: 8px;
  padding: 10px;
}

:deep(.dna-helix span) {
  display: block !important;
  width: 100% !important;
  height: 6px !important;
  background: linear-gradient(90deg, #00f0ff, transparent 40%, transparent 60%, #00ff88) !important;
  border-radius: 3px !important;
  animation: dna-twist 2s ease-in-out infinite !important;
}

:deep(.dna-helix span:nth-child(1)) { animation-delay: 0s !important; }
:deep(.dna-helix span:nth-child(2)) { animation-delay: 0.2s !important; }
:deep(.dna-helix span:nth-child(3)) { animation-delay: 0.4s !important; }
:deep(.dna-helix span:nth-child(4)) { animation-delay: 0.6s !important; }
:deep(.dna-helix span:nth-child(5)) { animation-delay: 0.8s !important; }

@keyframes dna-twist {
  0%, 100% { transform: scaleX(1); }
  50% { transform: scaleX(-1); }
}

/* 音频波形 - 使用:deep()穿透scoped */
:deep(.audio-wave) {
  width: 180px !important;
  height: 80px !important;
  display: flex !important;
  align-items: flex-end !important;
  justify-content: center !important;
  gap: 6px !important;
  background: rgba(0, 255, 136, 0.1);
  border-radius: 8px;
  padding: 10px;
}

:deep(.audio-wave span) {
  display: block !important;
  width: 10px !important;
  background: linear-gradient(to top, #00f0ff, #00ff88) !important;
  border-radius: 4px !important;
  animation: audio-bounce 1s ease-in-out infinite !important;
}

:deep(.audio-wave span:nth-child(1)) { height: 20px !important; animation-delay: 0s !important; }
:deep(.audio-wave span:nth-child(2)) { height: 40px !important; animation-delay: 0.1s !important; }
:deep(.audio-wave span:nth-child(3)) { height: 60px !important; animation-delay: 0.2s !important; }
:deep(.audio-wave span:nth-child(4)) { height: 80px !important; animation-delay: 0.3s !important; }
:deep(.audio-wave span:nth-child(5)) { height: 60px !important; animation-delay: 0.4s !important; }
:deep(.audio-wave span:nth-child(6)) { height: 40px !important; animation-delay: 0.5s !important; }
:deep(.audio-wave span:nth-child(7)) { height: 20px !important; animation-delay: 0.6s !important; }

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

/* ========== 新增大屏动画效果 ========== */

/* 科技边框 */
.tech-border {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
}
.tech-border::before {
  content: "";
  position: absolute;
  inset: 0;
  border: 2px solid transparent;
  border-radius: 8px;
  background: linear-gradient(#0a1628, #0a1628) padding-box,
              linear-gradient(90deg, #00f0ff, #00ff88, #a855f7, #00f0ff) border-box;
  background-size: 100% 100%, 400% 100%;
  animation: border-flow 3s linear infinite;
}
.tech-border::after {
  content: "PANEL";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #00f0ff;
  font-size: 14px;
  font-weight: bold;
  text-shadow: 0 0 10px #00f0ff;
}
@keyframes border-flow {
  0% { background-position: 0 0, 0% 0; }
  100% { background-position: 0 0, 400% 0; }
}

/* 数据瀑布 */
.data-waterfall {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.data-waterfall::before,
.data-waterfall::after {
  content: "10110100";
  position: absolute;
  font-family: monospace;
  font-size: 10px;
  color: #00f0ff;
  writing-mode: vertical-rl;
  animation: waterfall-drop 2s linear infinite;
  text-shadow: 0 0 5px #00f0ff;
}
.data-waterfall::before {
  left: 20%;
  animation-delay: 0s;
}
.data-waterfall::after {
  left: 60%;
  content: "01101011";
  color: #00ff88;
  animation-delay: 1s;
  text-shadow: 0 0 5px #00ff88;
}
@keyframes waterfall-drop {
  0% { top: -60px; opacity: 1; }
  100% { top: 120px; opacity: 0.3; }
}

/* 环形仪表 */
.ring-gauge {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  position: relative;
  background: conic-gradient(
    #00f0ff 0deg 90deg,
    #00ff88 90deg 180deg,
    #a855f7 180deg 270deg,
    rgba(0, 200, 255, 0.2) 270deg 360deg
  );
  animation: gauge-rotate 4s linear infinite;
}
.ring-gauge::before {
  content: "";
  position: absolute;
  inset: 12px;
  background: #0a1628;
  border-radius: 50%;
}
.ring-gauge::after {
  content: "78%";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #00f0ff;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 0 0 10px #00f0ff;
}
@keyframes gauge-rotate {
  to { transform: rotate(360deg); }
}

/* 粒子星云 */
.particle-nebula {
  width: 200px;
  height: 120px;
  background: radial-gradient(ellipse at center, rgba(0, 240, 255, 0.1) 0%, #0a1628 70%);
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}
.particle-nebula::before {
  content: "";
  position: absolute;
  width: 4px;
  height: 4px;
  background: #00f0ff;
  border-radius: 50%;
  top: 30%;
  left: 20%;
  box-shadow: 
    30px 20px 0 #00ff88,
    60px -10px 0 #a855f7,
    90px 30px 0 #ec4899,
    120px 10px 0 #00f0ff,
    150px 40px 0 #00ff88,
    20px 60px 0 #a855f7,
    80px 70px 0 #00f0ff,
    140px 80px 0 #ec4899;
  animation: nebula-float 4s ease-in-out infinite;
}
.particle-nebula::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 40%, rgba(0, 240, 255, 0.2) 0%, transparent 50%);
  animation: nebula-pulse 3s ease-in-out infinite;
}
@keyframes nebula-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
@keyframes nebula-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

/* 数据隧道 */
.data-tunnel {
  width: 120px;
  height: 120px;
  position: relative;
  perspective: 200px;
}
.data-tunnel::before,
.data-tunnel::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 8px;
  animation: tunnel-zoom 2s linear infinite;
}
.data-tunnel::before {
  inset: 0;
}
.data-tunnel::after {
  inset: 20px;
  border-color: #00ff88;
  animation-delay: 0.5s;
}
@keyframes tunnel-zoom {
  0% { transform: translateZ(-100px) scale(0.5); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateZ(50px) scale(1.5); opacity: 0; }
}

/* 脉冲网格 */
.pulse-grid {
  width: 200px;
  height: 120px;
  background: 
    linear-gradient(rgba(0, 240, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  position: relative;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.pulse-grid::before {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff, 0 0 40px #00f0ff;
  animation: grid-pulse 2s ease-out infinite;
}
.pulse-grid::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(0, 240, 255, 0.2) 0%, transparent 50%);
  animation: grid-wave 2s ease-out infinite;
}
@keyframes grid-pulse {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
  100% { transform: translate(-50%, -50%) scale(3); opacity: 0; }
}
@keyframes grid-wave {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(2); opacity: 0; }
}

/* 旋转星环 */
.star-ring {
  width: 100px;
  height: 100px;
  position: relative;
}
.star-ring::before {
  content: "";
  position: absolute;
  inset: 0;
  border: 3px dashed #00f0ff;
  border-radius: 50%;
  animation: star-orbit 4s linear infinite;
}
.star-ring::after {
  content: "★";
  position: absolute;
  top: -8px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 16px;
  color: #00ff88;
  text-shadow: 0 0 10px #00ff88;
  animation: star-orbit 4s linear infinite;
  transform-origin: center 58px;
}
@keyframes star-orbit {
  to { transform: rotate(360deg); }
}

/* 数据波动 */
.data-wave {
  width: 200px;
  height: 80px;
  position: relative;
  overflow: hidden;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.data-wave::before {
  content: "";
  position: absolute;
  width: 400px;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent 0%, 
    #00f0ff 10%, 
    transparent 20%,
    #00ff88 30%,
    transparent 40%,
    #a855f7 50%,
    transparent 60%,
    #00f0ff 70%,
    transparent 80%,
    #00ff88 90%,
    transparent 100%
  );
  animation: wave-scroll 3s linear infinite;
}
@keyframes wave-scroll {
  0% { transform: translateX(-200px); }
  100% { transform: translateX(0); }
}

/* 能量柱 */
.energy-bar {
  width: 160px;
  height: 30px;
  display: flex;
  gap: 4px;
  align-items: flex-end;
}
.energy-bar::before {
  content: "";
  flex: 1;
  height: 100%;
  background: linear-gradient(to top, #00f0ff, #00ff88);
  border-radius: 2px;
  animation: bar-grow 1s ease-in-out infinite;
  box-shadow: 
    8px 0 0 #00f0ff,
    16px 0 0 #00ff88,
    24px 0 0 #a855f7,
    32px 0 0 #ec4899,
    40px 0 0 #00f0ff,
    48px 0 0 #00ff88,
    56px 0 0 #a855f7;
}
@keyframes bar-grow {
  0%, 100% { transform: scaleY(0.3); }
  50% { transform: scaleY(1); }
}

/* 科技圆环 */
.tech-circle {
  width: 100px;
  height: 100px;
  position: relative;
}
.tech-circle::before {
  content: "";
  position: absolute;
  inset: 0;
  border: 4px solid transparent;
  border-top-color: #00f0ff;
  border-radius: 50%;
  animation: tech-rotate 1.5s linear infinite;
}
.tech-circle::after {
  content: "";
  position: absolute;
  inset: 15px;
  border: 3px solid transparent;
  border-bottom-color: #00ff88;
  border-left-color: #00ff88;
  border-radius: 50%;
  animation: tech-rotate 2s linear infinite reverse;
}
@keyframes tech-rotate {
  to { transform: rotate(360deg); }
}

/* 流光卡片 */
.glow-card {
  width: 160px;
  height: 100px;
  background: linear-gradient(135deg, rgba(0, 100, 200, 0.2), rgba(0, 50, 100, 0.1));
  border: 1px solid rgba(0, 200, 255, 0.3);
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}
.glow-card::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(from 0deg, transparent, #00f0ff, transparent, #00ff88, transparent);
  animation: card-glow 4s linear infinite;
  opacity: 0.3;
}
.glow-card::after {
  content: "";
  position: absolute;
  inset: 2px;
  background: linear-gradient(135deg, #0a1628, #0d2137);
  border-radius: 10px;
}
@keyframes card-glow {
  to { transform: rotate(360deg); }
}

/* 数据球体 */
.data-sphere {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #00f0ff, #0a1628);
  position: relative;
  box-shadow: 0 0 30px rgba(0, 240, 255, 0.5), inset 0 0 20px rgba(0, 240, 255, 0.3);
  animation: sphere-float 3s ease-in-out infinite;
}
.data-sphere::before {
  content: "";
  position: absolute;
  inset: 5px;
  border: 1px dashed rgba(0, 240, 255, 0.5);
  border-radius: 50%;
  animation: sphere-rotate 5s linear infinite;
}
.data-sphere::after {
  content: "";
  position: absolute;
  top: 10%;
  left: 20%;
  width: 20px;
  height: 10px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  filter: blur(2px);
}
@keyframes sphere-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
@keyframes sphere-rotate {
  to { transform: rotate(360deg); }
}

/* 扫描雷达 */
.scan-radar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 2px solid rgba(0, 200, 255, 0.4);
  position: relative;
  background: radial-gradient(circle, #0d2137 0%, #0a1628 100%);
}
.scan-radar::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent 0%, rgba(0, 240, 255, 0.5) 30%, transparent 60%);
  animation: radar-scan 2s linear infinite;
}
.scan-radar::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 15px #00f0ff, 30px -20px 0 4px #00ff88, -25px 15px 0 3px #a855f7;
}
@keyframes radar-scan {
  to { transform: rotate(360deg); }
}

/* 能量波纹 */
.energy-ripple {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.energy-ripple::before {
  content: "";
  position: absolute;
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
}
.energy-ripple::after {
  content: "";
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #00f0ff;
  border-radius: 50%;
  animation: energy-expand 1.5s ease-out infinite;
}
@keyframes energy-expand {
  0% { transform: scale(1); opacity: 1; border-color: #00f0ff; }
  50% { border-color: #00ff88; }
  100% { transform: scale(4); opacity: 0; border-color: #a855f7; }
}

/* 科技标题 */
.tech-title {
  font-size: 32px;
  font-weight: bold;
  color: transparent;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7, #00f0ff);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  animation: title-shine 3s linear infinite;
  text-shadow: 0 0 30px rgba(0, 240, 255, 0.5);
  position: relative;
}
.tech-title::after {
  content: "DATA";
  position: absolute;
  top: 0;
  left: 0;
  color: transparent;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7, #00f0ff);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  filter: blur(10px);
  animation: title-shine 3s linear infinite;
}
@keyframes title-shine {
  0% { background-position: 0% 50%; }
  100% { background-position: 200% 50%; }
}

/* 粒子轨迹 */
.particle-trail {
  width: 150px;
  height: 100px;
  position: relative;
  overflow: hidden;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.particle-trail::before {
  content: "";
  position: absolute;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  top: 50%;
  animation: trail-move 3s ease-in-out infinite;
  box-shadow: 0 0 10px #00f0ff, 0 0 20px #00f0ff;
}
.particle-trail::after {
  content: "";
  position: absolute;
  width: 100px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00f0ff);
  top: calc(50% + 3px);
  animation: trail-fade 3s ease-in-out infinite;
}
@keyframes trail-move {
  0% { left: -10px; }
  100% { left: 150px; }
}
@keyframes trail-fade {
  0% { left: -100px; opacity: 0; }
  20% { opacity: 1; }
  80% { opacity: 1; }
  100% { left: 50px; opacity: 0; }
}

/* 数据环绕 */
.data-orbit {
  width: 100px;
  height: 100px;
  position: relative;
}
.data-orbit::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, #00f0ff, #0a1628);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff;
}
.data-orbit::after {
  content: "";
  position: absolute;
  inset: 0;
  border: 2px dashed rgba(0, 200, 255, 0.3);
  border-radius: 50%;
  animation: orbit-spin 6s linear infinite;
}
@keyframes orbit-spin {
  to { transform: rotate(360deg); }
}

/* 光束聚焦 */
.beam-focus {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.beam-focus::before {
  content: "";
  position: absolute;
  width: 0;
  height: 0;
  border-left: 60px solid transparent;
  border-right: 60px solid transparent;
  border-top: 100px solid rgba(0, 240, 255, 0.1);
  animation: beam-pulse 2s ease-in-out infinite;
}
.beam-focus::after {
  content: "";
  position: absolute;
  bottom: 10px;
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 30px #00f0ff, 0 0 60px #00f0ff;
  animation: focus-glow 2s ease-in-out infinite;
}
@keyframes beam-pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.8; }
}
@keyframes focus-glow {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

/* 科技按钮 */
.tech-button {
  width: 120px;
  height: 50px;
  background: linear-gradient(135deg, rgba(0, 100, 200, 0.3), rgba(0, 50, 100, 0.2));
  border: 2px solid #00f0ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00f0ff;
  font-size: 14px;
  font-weight: bold;
  position: relative;
  overflow: hidden;
  text-shadow: 0 0 10px #00f0ff;
  animation: button-glow 2s ease-in-out infinite;
}
.tech-button::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.3), transparent);
  animation: button-shine 2s infinite;
}
.tech-button::after {
  content: "";
  position: absolute;
  inset: -2px;
  border: 2px solid transparent;
  border-radius: 8px;
  background: linear-gradient(#0a1628, #0a1628) padding-box,
              linear-gradient(90deg, #00f0ff, #00ff88) border-box;
  opacity: 0;
  animation: button-border 2s ease-in-out infinite;
}
@keyframes button-glow {
  0%, 100% { box-shadow: 0 0 10px rgba(0, 240, 255, 0.3); }
  50% { box-shadow: 0 0 20px rgba(0, 240, 255, 0.6), 0 0 40px rgba(0, 240, 255, 0.3); }
}
@keyframes button-shine {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}
@keyframes button-border {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
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
  padding: 24px;
  background: rgba(30, 41, 59, 0.3);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.info-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #f8fafc;
}

.category-tag {
  font-size: 0.75rem;
  padding: 4px 12px;
  background: rgba(139, 92, 246, 0.1);
  color: #a78bfa;
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: 100px;
  font-weight: 600;
}

.description {
  color: #94a3b8;
  font-size: 0.9rem;
  margin: 0 0 20px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.primary {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  color: #e2e8f0;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.action-btn.primary:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.1);
}

.action-btn.danger {
  width: 40px;
  padding: 0;
  background: rgba(239, 68, 68, 0.1);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.1);
}

.action-btn.danger:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.2);
}

/* 加载状态 */
.loading-box {
  text-align: center;
  padding: 100px 0;
  color: #94a3b8;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(139, 92, 246, 0.1);
  border-top-color: #8b5cf6;
  border-radius: 50%;
  margin: 0 auto 20px;
  animation: spin 1s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.code-dialog,
.form-dialog {
  background: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  width: 100%;
  max-width: 700px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.dialog-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #f8fafc;
}

.close-btn {
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 1.5rem;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.dialog-body {
  padding: 32px;
  overflow-y: auto;
  flex: 1;
}

.dialog-body pre {
  margin: 0;
  padding: 24px;
  background: #0f172a;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  overflow-x: auto;
}

.dialog-body code {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.9rem;
  color: #e2e8f0;
  white-space: pre-wrap;
  line-height: 1.6;
}

.dialog-footer {
  padding: 24px 32px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  background: rgba(30, 41, 59, 0.3);
}

.copy-btn,
.save-btn {
  padding: 12px 28px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.copy-btn:hover,
.save-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(139, 92, 246, 0.4);
}

.cancel-btn {
  padding: 12px 28px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #94a3b8;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.2);
}

/* 表单 */
.form-field {
  margin-bottom: 24px;
}

.form-field label {
  display: block;
  margin-bottom: 10px;
  font-size: 0.95rem;
  font-weight: 500;
  color: #cbd5e1;
}

.form-field input,
.form-field select,
.form-field textarea {
  width: 100%;
  padding: 14px 18px;
  background: #0f172a;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 0.95rem;
  font-family: inherit;
  box-sizing: border-box;
  transition: all 0.2s;
}

.form-field input:focus,
.form-field select:focus,
.form-field textarea:focus {
  outline: none;
  border-color: #8b5cf6;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15);
}

.form-field textarea {
  resize: vertical;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.9rem;
  line-height: 1.5;
}

/* 赛博朋克线条 */
.cyber-lines {
  width: 160px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
}
.cyber-lines::before {
  content: "";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    -45deg,
    transparent 0px,
    transparent 10px,
    rgba(0, 240, 255, 0.1) 10px,
    rgba(0, 240, 255, 0.1) 11px
  );
  animation: cyber-shift 3s linear infinite;
}
.cyber-lines::after {
  content: "CYBER";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ec4899;
  font-size: 16px;
  font-weight: bold;
  text-shadow: 0 0 10px #ec4899, 2px 2px 0 #00f0ff;
}
@keyframes cyber-shift {
  0% { background-position: 0 0; }
  100% { background-position: 28px 28px; }
}

/* 故障文字 */
.glitch-text {
  position: relative;
  font-size: 28px;
  font-weight: bold;
  color: #fff;
  text-shadow: 2px 0 #00f0ff, -2px 0 #ec4899;
  animation: glitch-shake 0.5s infinite;
}
.glitch-text::before,
.glitch-text::after {
  content: "ERROR";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
.glitch-text::before {
  color: #00f0ff;
  animation: glitch-clip 2s infinite;
  clip-path: polygon(0 0, 100% 0, 100% 45%, 0 45%);
  transform: translate(-3px, 0);
}
.glitch-text::after {
  color: #ec4899;
  animation: glitch-clip 2s infinite reverse;
  clip-path: polygon(0 55%, 100% 55%, 100% 100%, 0 100%);
  transform: translate(3px, 0);
}
@keyframes glitch-shake {
  0%, 100% { transform: translate(0); }
  20% { transform: translate(-2px, 2px); }
  40% { transform: translate(2px, -2px); }
  60% { transform: translate(-1px, 1px); }
  80% { transform: translate(1px, -1px); }
}
@keyframes glitch-clip {
  0%, 100% { clip-path: polygon(0 0, 100% 0, 100% 45%, 0 45%); }
  50% { clip-path: polygon(0 20%, 100% 20%, 100% 60%, 0 60%); }
}

/* 液态金属 */
.liquid-metal {
  width: 100px;
  height: 100px;
  position: relative;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  animation: liquid-morph 4s ease-in-out infinite;
  box-shadow: 
    inset -20px -20px 40px rgba(0, 240, 255, 0.2),
    inset 20px 20px 40px rgba(255, 255, 255, 0.1),
    0 0 30px rgba(0, 240, 255, 0.3);
}
.liquid-metal::before {
  content: "";
  position: absolute;
  top: 20%;
  left: 20%;
  width: 30%;
  height: 30%;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  filter: blur(5px);
}
@keyframes liquid-morph {
  0%, 100% { border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%; }
  25% { border-radius: 58% 42% 75% 25% / 76% 46% 54% 24%; }
  50% { border-radius: 50% 50% 33% 67% / 55% 27% 73% 45%; }
  75% { border-radius: 33% 67% 58% 42% / 63% 68% 32% 37%; }
}

/* 音乐均衡器 */
.music-equalizer {
  width: 140px;
  height: 80px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  background: #0a1628;
  border-radius: 8px;
}
.music-equalizer::before,
.music-equalizer::after {
  content: "";
  width: 12px;
  background: linear-gradient(to top, #00f0ff, #a855f7, #ec4899);
  border-radius: 3px;
  animation: eq-bar 0.6s ease-in-out infinite alternate;
}
.music-equalizer::before {
  height: 60px;
  animation-delay: 0s;
}
.music-equalizer::after {
  height: 40px;
  animation-delay: 0.15s;
}
@keyframes eq-bar {
  0% { transform: scaleY(0.2); }
  100% { transform: scaleY(1); }
}

/* 棱镜折射 */
.prism-refract {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.prism-refract::before {
  content: "";
  width: 0;
  height: 0;
  border-left: 40px solid transparent;
  border-right: 40px solid transparent;
  border-bottom: 70px solid rgba(255, 255, 255, 0.1);
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.3));
}
.prism-refract::after {
  content: "";
  position: absolute;
  right: 10px;
  top: 50%;
  width: 50px;
  height: 30px;
  background: linear-gradient(to bottom, 
    #ff0000 0%, #ff7f00 17%, #ffff00 33%, 
    #00ff00 50%, #0000ff 67%, #8b00ff 83%, #ff00ff 100%);
  transform: translateY(-50%) skewY(-15deg);
  opacity: 0.8;
  animation: rainbow-pulse 2s ease-in-out infinite;
}
@keyframes rainbow-pulse {
  0%, 100% { opacity: 0.5; width: 40px; }
  50% { opacity: 0.9; width: 60px; }
}

/* 太空站舱门 */
.airlock-door {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 8px;
  overflow: hidden;
}
.airlock-door::before,
.airlock-door::after {
  content: "";
  position: absolute;
  top: 0;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, #0d2137, #1a3a5c);
  border: 1px solid rgba(0, 240, 255, 0.3);
  animation: door-open 3s ease-in-out infinite;
}
.airlock-door::before {
  left: 0;
  border-right: 2px solid #00f0ff;
  transform-origin: left;
}
.airlock-door::after {
  right: 0;
  border-left: 2px solid #00f0ff;
  transform-origin: right;
  animation-name: door-open-right;
}
@keyframes door-open {
  0%, 20%, 80%, 100% { transform: scaleX(1); }
  40%, 60% { transform: scaleX(0.1); }
}
@keyframes door-open-right {
  0%, 20%, 80%, 100% { transform: scaleX(1); }
  40%, 60% { transform: scaleX(0.1); }
}

/* 全息投影仪 */
.holo-projector {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.holo-projector::before {
  content: "";
  width: 60px;
  height: 20px;
  background: linear-gradient(to top, #0d2137, #1a3a5c);
  border-radius: 4px;
  border: 1px solid #00f0ff;
  margin-top: auto;
}
.holo-projector::after {
  content: "";
  position: absolute;
  bottom: 20px;
  width: 40px;
  height: 80px;
  background: linear-gradient(to top, rgba(0, 240, 255, 0.4), transparent);
  clip-path: polygon(20% 100%, 80% 100%, 100% 0%, 0% 0%);
  animation: holo-flicker 0.1s infinite;
}
@keyframes holo-flicker {
  0%, 100% { opacity: 0.8; }
  50% { opacity: 0.6; }
}

/* 量子纠缠 */
.quantum-entangle {
  width: 150px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.quantum-entangle::before,
.quantum-entangle::after {
  content: "";
  width: 25px;
  height: 25px;
  background: radial-gradient(circle, #00f0ff, transparent);
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
  animation: quantum-pulse 1s ease-in-out infinite;
}
.quantum-entangle::after {
  background: radial-gradient(circle, #ec4899, transparent);
  box-shadow: 0 0 20px #ec4899;
  animation-delay: 0.5s;
}
@keyframes quantum-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(0.5); opacity: 0.5; }
}

/* 数据加密 */
.data-encrypt {
  width: 80px;
  height: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.data-encrypt::before {
  content: "";
  width: 50px;
  height: 40px;
  border: 4px solid #00f0ff;
  border-bottom: none;
  border-radius: 25px 25px 0 0;
  animation: lock-shake 2s ease-in-out infinite;
}
.data-encrypt::after {
  content: "🔒";
  font-size: 40px;
  margin-top: -5px;
  animation: lock-glow 2s ease-in-out infinite;
}
@keyframes lock-shake {
  0%, 100% { transform: translateY(0); }
  10%, 30% { transform: translateY(-5px); }
  20% { transform: translateY(-3px); }
  40%, 100% { transform: translateY(0); }
}
@keyframes lock-glow {
  0%, 100% { filter: drop-shadow(0 0 5px #00f0ff); }
  50% { filter: drop-shadow(0 0 15px #00f0ff); }
}

/* 星际跃迁 */
.warp-drive {
  width: 150px;
  height: 100px;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  perspective: 500px;
}
.warp-drive::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 4px;
  height: 4px;
  background: #fff;
  border-radius: 50%;
  box-shadow:
    20px -30px 0 #00f0ff,
    -30px 20px 0 #fff,
    40px 10px 0 #a855f7,
    -20px -20px 0 #00ff88,
    50px -10px 0 #ec4899,
    -40px 30px 0 #fff,
    10px 40px 0 #00f0ff;
  animation: warp-zoom 1s linear infinite;
}
@keyframes warp-zoom {
  0% { transform: translate(-50%, -50%) scale(0.1); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translate(-50%, -50%) scale(3); opacity: 0; }
}

/* 能量充能 */
.energy-charge {
  width: 60px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 30px;
  overflow: hidden;
}
.energy-charge::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 0%;
  background: linear-gradient(to top, #00f0ff, #00ff88, #a855f7);
  animation: charge-fill 3s ease-in-out infinite;
  box-shadow: 0 0 20px #00f0ff;
}
.energy-charge::after {
  content: "";
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 10px;
  background: #00f0ff;
  border-radius: 0 0 10px 10px;
}
@keyframes charge-fill {
  0%, 100% { height: 10%; }
  50% { height: 90%; }
}

/* 分子结构 */
.molecule {
  width: 100px;
  height: 100px;
  position: relative;
  animation: molecule-rotate 4s linear infinite;
}
.molecule::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 15px #00f0ff;
}
.molecule::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 80px;
  height: 80px;
  border: 2px dashed rgba(0, 240, 255, 0.5);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow:
    inset 40px 0 0 -32px #00ff88,
    inset -40px 0 0 -32px #a855f7,
    inset 0 40px 0 -32px #ec4899,
    inset 0 -40px 0 -32px #00f0ff;
}
@keyframes molecule-rotate {
  to { transform: rotate(360deg); }
}

/* 信息流 */
.info-stream {
  width: 180px;
  height: 60px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.info-stream::before {
  content: ">>> DATA STREAM >>> LOADING >>> SYNC >>> ";
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  white-space: nowrap;
  color: #00f0ff;
  font-family: monospace;
  font-size: 12px;
  animation: stream-scroll 5s linear infinite;
  text-shadow: 0 0 5px #00f0ff;
}
@keyframes stream-scroll {
  0% { transform: translateY(-50%) translateX(100%); }
  100% { transform: translateY(-50%) translateX(-100%); }
}

/* 防火墙 */
.firewall {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.firewall::before {
  content: "🛡️";
  font-size: 50px;
  animation: shield-pulse 2s ease-in-out infinite;
  filter: drop-shadow(0 0 10px #00f0ff);
}
.firewall::after {
  content: "";
  position: absolute;
  width: 90px;
  height: 90px;
  border: 2px solid transparent;
  border-top-color: #00f0ff;
  border-right-color: #00ff88;
  border-radius: 50%;
  animation: shield-rotate 3s linear infinite;
}
@keyframes shield-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
@keyframes shield-rotate {
  to { transform: rotate(360deg); }
}

/* 波形示波器 */
.oscilloscope {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 2px solid #1a3a5c;
  overflow: hidden;
}
.oscilloscope::before {
  content: "";
  position: absolute;
  inset: 0;
  background: 
    linear-gradient(rgba(0, 240, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}
.oscilloscope::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 0;
  width: 200%;
  height: 2px;
  background: #00f0ff;
  box-shadow: 0 0 10px #00f0ff;
  animation: wave-scroll 2s linear infinite;
}
@keyframes wave-scroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

/* 代码雨 */
.code-rain {
  width: 160px;
  height: 120px;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  font-family: monospace;
}
.code-rain::before {
  content: "10101 01010 11001 00110 10101 01010";
  position: absolute;
  top: -100%;
  left: 0;
  right: 0;
  height: 200%;
  color: #00ff00;
  font-size: 10px;
  line-height: 1.5;
  text-align: center;
  animation: code-fall 3s linear infinite;
  text-shadow: 0 0 5px #00ff00;
}
@keyframes code-fall {
  0% { transform: translateY(0); }
  100% { transform: translateY(50%); }
}

/* 脑电波 */
.brain-wave {
  width: 180px;
  height: 80px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(168, 85, 247, 0.3);
  overflow: hidden;
}
.brain-wave::before {
  content: "";
  position: absolute;
  top: 50%;
  left: -100%;
  width: 200%;
  height: 3px;
  background: linear-gradient(90deg, 
    transparent 0%, #a855f7 20%, #ec4899 40%, 
    #a855f7 60%, transparent 80%);
  animation: brain-scan 2s linear infinite;
  box-shadow: 0 0 15px #a855f7;
}
.brain-wave::after {
  content: "BRAIN ACTIVITY";
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #a855f7;
  letter-spacing: 1px;
}
@keyframes brain-scan {
  0% { transform: translateX(0) translateY(-50%); }
  100% { transform: translateX(50%) translateY(-50%); }
}

/* 声纹识别 */
.voice-print {
  width: 150px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.voice-print::before,
.voice-print::after {
  content: "";
  width: 4px;
  background: linear-gradient(to top, #00f0ff, #a855f7);
  border-radius: 2px;
  animation: voice-bar 0.4s ease-in-out infinite alternate;
}
.voice-print::before {
  height: 60px;
  animation-delay: 0s;
}
.voice-print::after {
  height: 40px;
  animation-delay: 0.1s;
}
@keyframes voice-bar {
  0% { transform: scaleY(0.3); }
  100% { transform: scaleY(1); }
}

/* 虚拟键盘 */
.virtual-keyboard {
  width: 160px;
  height: 80px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 4px;
  padding: 10px;
}
.virtual-keyboard::before {
  content: "";
  position: absolute;
  width: 20px;
  height: 20px;
  background: rgba(0, 240, 255, 0.3);
  border-radius: 4px;
  animation: key-press 1s steps(6) infinite;
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes key-press {
  0% { top: 10px; left: 10px; }
  16% { top: 10px; left: 35px; }
  33% { top: 10px; left: 60px; }
  50% { top: 40px; left: 85px; }
  66% { top: 40px; left: 110px; }
  83% { top: 40px; left: 135px; }
  100% { top: 10px; left: 10px; }
}

/* 人脸识别 */
.face-scan {
  width: 100px;
  height: 120px;
  position: relative;
  border: 2px solid #00f0ff;
  border-radius: 50% 50% 45% 45%;
  background: rgba(0, 240, 255, 0.05);
}
.face-scan::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  animation: face-scan-line 2s linear infinite;
  box-shadow: 0 0 15px #00f0ff;
}
.face-scan::after {
  content: "SCANNING...";
  position: absolute;
  bottom: -25px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #00f0ff;
  animation: scan-blink 1s infinite;
}
@keyframes face-scan-line {
  0%, 100% { top: 0; }
  50% { top: calc(100% - 3px); }
}
@keyframes scan-blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* 数据同步 */
.data-sync {
  width: 150px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
}
.data-sync::before,
.data-sync::after {
  content: "";
  width: 40px;
  height: 40px;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.3);
  animation: sync-pulse 1.5s ease-in-out infinite;
}
.data-sync::after {
  animation-delay: 0.75s;
}
@keyframes sync-pulse {
  0%, 100% { transform: scale(1); box-shadow: 0 0 10px rgba(0, 240, 255, 0.3); }
  50% { transform: scale(1.1); box-shadow: 0 0 20px rgba(0, 240, 255, 0.6); }
}

/* 网络拓扑 */
.network-topology {
  width: 140px;
  height: 140px;
  position: relative;
}
.network-topology::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff;
  animation: node-pulse 2s ease-in-out infinite;
}
.network-topology::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100px;
  height: 100px;
  border: 2px dashed rgba(0, 240, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: topology-rotate 8s linear infinite;
  box-shadow:
    inset 50px 0 0 -42px #00ff88,
    inset -50px 0 0 -42px #a855f7,
    inset 0 50px 0 -42px #ec4899;
}
@keyframes node-pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.2); }
}
@keyframes topology-rotate {
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

/* 云端存储 */
.cloud-storage {
  width: 120px;
  height: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.cloud-storage::before {
  content: "☁️";
  font-size: 50px;
  filter: drop-shadow(0 0 10px #00f0ff);
  animation: cloud-float 3s ease-in-out infinite;
}
.cloud-storage::after {
  content: "↑↓";
  position: absolute;
  bottom: 10px;
  font-size: 20px;
  color: #00f0ff;
  animation: arrow-blink 1s infinite;
  text-shadow: 0 0 10px #00f0ff;
}
@keyframes cloud-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
@keyframes arrow-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 安全验证 */
.security-check {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.security-check::before {
  content: "✓";
  font-size: 50px;
  color: #00ff88;
  text-shadow: 0 0 20px #00ff88;
  animation: check-pop 2s ease-in-out infinite;
}
.security-check::after {
  content: "";
  position: absolute;
  width: 80px;
  height: 80px;
  border: 3px solid #00ff88;
  border-radius: 50%;
  animation: ring-expand 2s ease-out infinite;
}
@keyframes check-pop {
  0%, 100% { transform: scale(1); }
  10% { transform: scale(1.3); }
  20% { transform: scale(1); }
}
@keyframes ring-expand {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* 温度监控 */
.temp-monitor {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.temp-monitor::before {
  content: "36.5°";
  font-size: 28px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 15px #00ff88;
  animation: temp-flicker 3s ease-in-out infinite;
}
.temp-monitor::after {
  content: "NORMAL";
  font-size: 12px;
  color: #00ff88;
  margin-top: 10px;
  padding: 4px 12px;
  border: 1px solid #00ff88;
  border-radius: 20px;
}
@keyframes temp-flicker {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* 电量显示 */
.power-display {
  width: 80px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.power-display::before {
  content: "⚡";
  font-size: 40px;
  animation: power-flash 1s ease-in-out infinite;
}
.power-display::after {
  content: "85%";
  font-size: 24px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 10px #00f0ff;
  margin-top: 10px;
}
@keyframes power-flash {
  0%, 100% { filter: drop-shadow(0 0 5px #ffcc00); transform: scale(1); }
  50% { filter: drop-shadow(0 0 20px #ffcc00); transform: scale(1.1); }
}

/* 速度仪表 */
.speed-gauge {
  width: 120px;
  height: 120px;
  position: relative;
  border-radius: 50%;
  background: conic-gradient(from 180deg, #00ff88 0deg, #00f0ff 90deg, #a855f7 180deg, transparent 180deg);
}
.speed-gauge::before {
  content: "";
  position: absolute;
  inset: 10px;
  background: #0a1628;
  border-radius: 50%;
}
.speed-gauge::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 4px;
  height: 40px;
  background: linear-gradient(to top, #ec4899, transparent);
  transform-origin: bottom center;
  transform: translate(-50%, -100%);
  animation: needle-swing 2s ease-in-out infinite;
  border-radius: 2px;
}
@keyframes needle-swing {
  0%, 100% { transform: translate(-50%, -100%) rotate(-60deg); }
  50% { transform: translate(-50%, -100%) rotate(60deg); }
}

/* 信号塔 */
.signal-tower {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}
.signal-tower::before {
  content: "";
  width: 8px;
  height: 60px;
  background: linear-gradient(to top, #1a3a5c, #00f0ff);
  border-radius: 4px;
}
.signal-tower::after {
  content: "";
  position: absolute;
  top: 10px;
  width: 80px;
  height: 40px;
  border: 2px solid transparent;
  border-top-color: #00f0ff;
  border-radius: 50%;
  animation: signal-emit 1.5s ease-out infinite;
  opacity: 0;
}
@keyframes signal-emit {
  0% { transform: scale(0.3); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* 数据库 */
.database-icon {
  width: 80px;
  height: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.database-icon::before,
.database-icon::after {
  content: "";
  width: 80px;
  height: 25px;
  background: linear-gradient(to bottom, #1a3a5c, #0d2137);
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: db-pulse 2s ease-in-out infinite;
}
.database-icon::after {
  animation-delay: 0.3s;
}
@keyframes db-pulse {
  0%, 100% { box-shadow: 0 0 5px rgba(0, 240, 255, 0.3); }
  50% { box-shadow: 0 0 15px rgba(0, 240, 255, 0.6); }
}

/* CPU监控 */
.cpu-monitor {
  width: 100px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cpu-monitor::before {
  content: "CPU";
  font-size: 14px;
  color: #00f0ff;
  position: absolute;
  top: 10px;
}
.cpu-monitor::after {
  content: "78%";
  font-size: 28px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 10px #00ff88;
}

/* 内存条 */
.memory-bar {
  width: 160px;
  height: 40px;
  position: relative;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 6px;
  overflow: hidden;
}
.memory-bar::before {
  content: "";
  position: absolute;
  top: 4px;
  left: 4px;
  bottom: 4px;
  width: 0%;
  background: linear-gradient(90deg, #00f0ff, #00ff88);
  border-radius: 3px;
  animation: memory-fill 3s ease-in-out infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.memory-bar::after {
  content: "RAM 64%";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 0 5px #000;
  z-index: 1;
}
@keyframes memory-fill {
  0%, 100% { width: 40%; }
  50% { width: 80%; }
}

/* 磁盘扫描 */
.disk-scan {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
  background: conic-gradient(#00f0ff 0deg, transparent 0deg);
  animation: disk-progress 3s linear infinite;
}
.disk-scan::before {
  content: "";
  position: absolute;
  inset: 8px;
  background: #0a1628;
  border-radius: 50%;
}
.disk-scan::after {
  content: "SCAN";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 14px;
  font-weight: bold;
  color: #00f0ff;
}
@keyframes disk-progress {
  0% { background: conic-gradient(#00f0ff 0deg, transparent 0deg); }
  25% { background: conic-gradient(#00f0ff 90deg, transparent 90deg); }
  50% { background: conic-gradient(#00f0ff 180deg, transparent 180deg); }
  75% { background: conic-gradient(#00f0ff 270deg, transparent 270deg); }
  100% { background: conic-gradient(#00f0ff 360deg, transparent 360deg); }
}

/* 网速监测 */
.network-speed {
  width: 140px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.network-speed::before {
  content: "↓ 125 Mb/s";
  font-size: 16px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 10px #00ff88;
}
.network-speed::after {
  content: "↑ 48 Mb/s";
  font-size: 14px;
  color: #00f0ff;
  text-shadow: 0 0 8px #00f0ff;
}

/* 服务器状态 */
.server-status {
  width: 120px;
  height: 80px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 255, 136, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.server-status::before {
  content: "";
  width: 12px;
  height: 12px;
  background: #00ff88;
  border-radius: 50%;
  box-shadow: 0 0 10px #00ff88, 0 0 20px #00ff88;
  animation: status-blink 2s ease-in-out infinite;
}
.server-status::after {
  content: "ONLINE";
  font-size: 14px;
  font-weight: bold;
  color: #00ff88;
  letter-spacing: 2px;
}
@keyframes status-blink {
  0%, 100% { opacity: 1; box-shadow: 0 0 10px #00ff88, 0 0 20px #00ff88; }
  50% { opacity: 0.6; box-shadow: 0 0 5px #00ff88; }
}

/* 时间戳 */
.timestamp {
  width: 160px;
  height: 60px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: monospace;
}
.timestamp::before {
  content: "12:34:56";
  font-size: 28px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 15px #00f0ff;
  animation: time-tick 1s steps(1) infinite;
}
@keyframes time-tick {
  0% { content: "12:34:56"; }
  50% { content: "12:34:57"; }
}

/* 坐标定位 */
.gps-location {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gps-location::before {
  content: "📍";
  font-size: 40px;
  animation: pin-bounce 1s ease-in-out infinite;
  filter: drop-shadow(0 0 10px #ec4899);
}
.gps-location::after {
  content: "";
  position: absolute;
  bottom: 20px;
  width: 40px;
  height: 10px;
  background: rgba(236, 72, 153, 0.3);
  border-radius: 50%;
  animation: pin-shadow 1s ease-in-out infinite;
}
@keyframes pin-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}
@keyframes pin-shadow {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(0.6); opacity: 0.3; }
}

/* 二维码 */
.qr-code {
  width: 100px;
  height: 100px;
  position: relative;
  background: #fff;
  border-radius: 8px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2px;
  padding: 8px;
}
.qr-code::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  animation: qr-scan 2s linear infinite;
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes qr-scan {
  0% { top: 0; }
  100% { top: 100%; }
}

/* 蓝牙连接 */
.bluetooth {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bluetooth::before {
  content: "";
  width: 40px;
  height: 60px;
  background: transparent;
  border: 3px solid #00f0ff;
  clip-path: polygon(50% 0%, 100% 25%, 50% 50%, 100% 75%, 50% 100%, 50% 60%, 0% 75%, 50% 50%, 0% 25%, 50% 40%);
  animation: bt-pulse 2s ease-in-out infinite;
  filter: drop-shadow(0 0 10px #00f0ff);
}
.bluetooth::after {
  content: "";
  position: absolute;
  width: 80px;
  height: 80px;
  border: 2px solid rgba(0, 240, 255, 0.3);
  border-radius: 50%;
  animation: bt-wave 1.5s ease-out infinite;
}
@keyframes bt-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
@keyframes bt-wave {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* WiFi信号 */
.wifi-signal {
  width: 80px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.wifi-signal::before {
  content: "";
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 10px #00f0ff;
}
.wifi-signal::after {
  content: "";
  position: absolute;
  bottom: 15px;
  width: 60px;
  height: 45px;
  border: 3px solid transparent;
  border-top-color: #00f0ff;
  border-radius: 50% 50% 0 0;
  box-shadow: 
    0 -15px 0 -12px #00f0ff,
    0 -30px 0 -9px #00f0ff;
  animation: wifi-pulse 1.5s ease-in-out infinite;
}
@keyframes wifi-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* 音频播放 */
.audio-player {
  width: 180px;
  height: 60px;
  position: relative;
  background: #0a1628;
  border-radius: 30px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  align-items: center;
  padding: 0 15px;
  gap: 10px;
}
.audio-player::before {
  content: "▶";
  font-size: 16px;
  color: #00f0ff;
}
.audio-player::after {
  content: "";
  flex: 1;
  height: 4px;
  background: rgba(0, 240, 255, 0.2);
  border-radius: 2px;
  position: relative;
  overflow: hidden;
}

/* 视频帧 */
.video-frame {
  width: 160px;
  height: 100px;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #1a3a5c;
}
.video-frame::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(45deg, 
    rgba(0, 240, 255, 0.1) 25%, transparent 25%,
    transparent 75%, rgba(0, 240, 255, 0.1) 75%);
  background-size: 20px 20px;
  animation: video-noise 0.5s steps(2) infinite;
}
.video-frame::after {
  content: "▶";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 30px;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 0 0 20px #00f0ff;
}
@keyframes video-noise {
  0% { background-position: 0 0; }
  100% { background-position: 20px 20px; }
}

/* 3D立方体 */
.cube-3d-rotate {
  width: 80px;
  height: 80px;
  position: relative;
  transform-style: preserve-3d;
  animation: cube-spin 4s linear infinite;
}
.cube-3d-rotate::before,
.cube-3d-rotate::after {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid #00f0ff;
  background: rgba(0, 240, 255, 0.1);
}
.cube-3d-rotate::before {
  transform: translateZ(40px);
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.5);
}
.cube-3d-rotate::after {
  transform: translateZ(-40px);
}
@keyframes cube-spin {
  0% { transform: rotateX(0deg) rotateY(0deg); }
  100% { transform: rotateX(360deg) rotateY(360deg); }
}

/* 数据饼图 */
.pie-chart {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
  background: conic-gradient(
    #00f0ff 0deg 120deg,
    #00ff88 120deg 200deg,
    #a855f7 200deg 280deg,
    #ec4899 280deg 360deg
  );
  animation: pie-rotate 8s linear infinite;
}
.pie-chart::before {
  content: "";
  position: absolute;
  inset: 20px;
  background: #0a1628;
  border-radius: 50%;
}
.pie-chart::after {
  content: "DATA";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: bold;
  color: #fff;
}
@keyframes pie-rotate {
  to { transform: rotate(360deg); }
}

/* 折线图 */
.line-chart {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  overflow: hidden;
}
.line-chart::before {
  content: "";
  position: absolute;
  inset: 10px;
  background: 
    linear-gradient(rgba(0, 240, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}
.line-chart::after {
  content: "";
  position: absolute;
  bottom: 20px;
  left: 10px;
  width: 160px;
  height: 60px;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7);
  clip-path: polygon(0% 100%, 10% 60%, 25% 80%, 40% 30%, 55% 50%, 70% 20%, 85% 40%, 100% 10%, 100% 100%);
}

/* 柱状图 */
.bar-chart {
  width: 160px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
  padding: 15px;
}
.bar-chart::before,
.bar-chart::after {
  content: "";
  width: 25px;
  background: linear-gradient(to top, #00f0ff, #00ff88);
  border-radius: 4px 4px 0 0;
  animation: bar-grow 2s ease-out infinite;
}
.bar-chart::before {
  height: 60px;
  animation-delay: 0s;
}
.bar-chart::after {
  height: 45px;
  animation-delay: 0.2s;
}
@keyframes bar-grow {
  0% { transform: scaleY(0); transform-origin: bottom; }
  50%, 100% { transform: scaleY(1); transform-origin: bottom; }
}

/* 指纹解锁 */
.fingerprint-unlock {
  width: 80px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.fingerprint-unlock::before {
  content: "";
  width: 60px;
  height: 80px;
  background: repeating-linear-gradient(
    0deg,
    transparent 0px,
    transparent 3px,
    rgba(0, 240, 255, 0.3) 3px,
    rgba(0, 240, 255, 0.3) 4px
  );
  border-radius: 30px 30px 40px 40px;
  animation: fp-scan 2s ease-in-out infinite;
}
.fingerprint-unlock::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #00f0ff;
  box-shadow: 0 0 15px #00f0ff;
  animation: fp-line 2s ease-in-out infinite;
}
@keyframes fp-scan {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
@keyframes fp-line {
  0%, 100% { top: 0; }
  50% { top: calc(100% - 4px); }
}

/* 虹膜扫描 */
.iris-scan {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
  background: radial-gradient(circle, #0a1628 30%, #1a3a5c 60%, #0a1628 100%);
  border: 3px solid #00f0ff;
  overflow: hidden;
}
.iris-scan::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  background: #000;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 0 8px #00f0ff, 0 0 0 12px rgba(0, 240, 255, 0.3);
}
.iris-scan::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00ff88, transparent);
  animation: iris-line 1.5s linear infinite;
  box-shadow: 0 0 10px #00ff88;
}
@keyframes iris-line {
  0% { top: 0; }
  100% { top: 100%; }
}

/* 心率监测 */
.heart-rate {
  width: 140px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(236, 72, 153, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.heart-rate::before {
  content: "❤️";
  font-size: 30px;
  animation: heart-beat 1s ease-in-out infinite;
}
.heart-rate::after {
  content: "72 BPM";
  font-size: 20px;
  font-weight: bold;
  color: #ec4899;
  text-shadow: 0 0 10px #ec4899;
  margin-top: 5px;
}
@keyframes heart-beat {
  0%, 100% { transform: scale(1); }
  15% { transform: scale(1.3); }
  30% { transform: scale(1); }
  45% { transform: scale(1.2); }
  60% { transform: scale(1); }
}

/* 血氧饱和 */
.oxygen-level {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.oxygen-level::before {
  content: "SpO₂";
  font-size: 14px;
  color: #64748b;
  margin-bottom: 5px;
}
.oxygen-level::after {
  content: "98%";
  font-size: 36px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 15px #00f0ff;
  animation: oxy-pulse 2s ease-in-out infinite;
}
@keyframes oxy-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* 步数统计 */
.step-counter {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.step-counter::before {
  content: "👟";
  font-size: 35px;
  animation: step-walk 0.5s ease-in-out infinite alternate;
}
.step-counter::after {
  content: "8,542";
  font-size: 24px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 10px #00ff88;
  margin-top: 8px;
}
@keyframes step-walk {
  0% { transform: translateX(-5px) rotate(-10deg); }
  100% { transform: translateX(5px) rotate(10deg); }
}

/* 卡路里 */
.calorie-burn {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(255, 136, 0, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.calorie-burn::before {
  content: "🔥";
  font-size: 30px;
  animation: flame-dance 0.3s ease-in-out infinite alternate;
}
.calorie-burn::after {
  content: "486 kcal";
  font-size: 18px;
  font-weight: bold;
  color: #ff8800;
  text-shadow: 0 0 10px #ff8800;
  margin-top: 5px;
}
@keyframes flame-dance {
  0% { transform: scale(1) rotate(-5deg); }
  100% { transform: scale(1.1) rotate(5deg); }
}

/* 天气图标 */
.weather-icon {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.weather-icon::before {
  content: "☀️";
  font-size: 50px;
  animation: sun-rotate 10s linear infinite;
  filter: drop-shadow(0 0 15px #ffcc00);
}
.weather-icon::after {
  content: "28°C";
  font-size: 20px;
  font-weight: bold;
  color: #ffcc00;
  margin-top: 8px;
}
@keyframes sun-rotate {
  to { transform: rotate(360deg); }
}

/* 风速指示 */
.wind-speed {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wind-speed::before {
  content: "💨";
  font-size: 40px;
  animation: wind-blow 1s ease-in-out infinite;
}
.wind-speed::after {
  content: "12 km/h";
  position: absolute;
  bottom: 10px;
  font-size: 14px;
  font-weight: bold;
  color: #00f0ff;
}
@keyframes wind-blow {
  0%, 100% { transform: translateX(0) scale(1); }
  50% { transform: translateX(10px) scale(1.1); }
}

/* 湿度计 */
.humidity-gauge {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.humidity-gauge::before {
  content: "💧";
  font-size: 40px;
  animation: drop-bounce 2s ease-in-out infinite;
  filter: drop-shadow(0 0 10px #00f0ff);
}
.humidity-gauge::after {
  content: "65%";
  font-size: 24px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 10px #00f0ff;
  margin-top: 8px;
}
@keyframes drop-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 气压表 */
.pressure-meter {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(168, 85, 247, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.pressure-meter::before {
  content: "hPa";
  font-size: 12px;
  color: #64748b;
}
.pressure-meter::after {
  content: "1013";
  font-size: 32px;
  font-weight: bold;
  color: #a855f7;
  text-shadow: 0 0 15px #a855f7;
  animation: pressure-flicker 3s ease-in-out infinite;
}
@keyframes pressure-flicker {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

/* 紫外线 */
.uv-index {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.uv-index::before {
  content: "";
  width: 60px;
  height: 60px;
  background: radial-gradient(circle, #a855f7 0%, transparent 70%);
  border-radius: 50%;
  animation: uv-glow 2s ease-in-out infinite;
}
.uv-index::after {
  content: "UV 6";
  position: absolute;
  font-size: 18px;
  font-weight: bold;
  color: #a855f7;
  text-shadow: 0 0 10px #a855f7;
}
@keyframes uv-glow {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 1; }
}

/* 日出日落 */
.sunrise-sunset {
  width: 160px;
  height: 80px;
  position: relative;
  background: linear-gradient(to top, #0a1628 50%, #1a3a5c 100%);
  border-radius: 8px;
  overflow: hidden;
}
.sunrise-sunset::before {
  content: "";
  position: absolute;
  bottom: 20px;
  left: 20px;
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, #ffcc00, #ff8800);
  border-radius: 50%;
  box-shadow: 0 0 20px #ffcc00;
  animation: sun-rise 4s ease-in-out infinite;
}
.sunrise-sunset::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 20px;
  background: #0a1628;
}
@keyframes sun-rise {
  0%, 100% { transform: translateX(0) translateY(0); }
  50% { transform: translateX(100px) translateY(-30px); }
}

/* 月相显示 */
.moon-phase {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.moon-phase::before {
  content: "";
  width: 70px;
  height: 70px;
  background: linear-gradient(90deg, #f0f0f0 50%, transparent 50%);
  border-radius: 50%;
  box-shadow: inset -5px 0 10px rgba(0, 0, 0, 0.3), 0 0 20px rgba(240, 240, 240, 0.3);
  animation: moon-glow 3s ease-in-out infinite;
}
.moon-phase::after {
  content: "🌙";
  position: absolute;
  font-size: 12px;
  bottom: 5px;
  color: #94a3b8;
}
@keyframes moon-glow {
  0%, 100% { box-shadow: inset -5px 0 10px rgba(0, 0, 0, 0.3), 0 0 20px rgba(240, 240, 240, 0.3); }
  50% { box-shadow: inset -5px 0 10px rgba(0, 0, 0, 0.3), 0 0 30px rgba(240, 240, 240, 0.5); }
}

/* 潮汐指示 */
.tide-indicator {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.tide-indicator::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: linear-gradient(to top, #00f0ff, rgba(0, 240, 255, 0.3));
  animation: tide-wave 3s ease-in-out infinite;
}
.tide-indicator::after {
  content: "HIGH";
  position: absolute;
  top: 15px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 14px;
  font-weight: bold;
  color: #00f0ff;
}
@keyframes tide-wave {
  0%, 100% { height: 30%; }
  50% { height: 60%; }
}

/* 空气质量 */
.air-quality {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.air-quality::before {
  content: "AQI";
  font-size: 14px;
  color: #64748b;
  margin-bottom: 5px;
}
.air-quality::after {
  content: "42";
  font-size: 42px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 20px #00ff88;
  animation: aqi-glow 2s ease-in-out infinite;
}
@keyframes aqi-glow {
  0%, 100% { text-shadow: 0 0 20px #00ff88; }
  50% { text-shadow: 0 0 30px #00ff88, 0 0 40px #00ff88; }
}

/* 响应式 */
@media (max-width: 768px) {
  .animation-lab {
    padding: 24px 16px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
  }

  .header-text h1 {
    font-size: 2.5rem;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    width: 100%;
  }

  .animation-grid {
    grid-template-columns: 1fr;
  }

  .category-nav {
    gap: 8px;
  }

  .cat-btn {
    padding: 8px 16px;
    font-size: 0.85rem;
  }
}

/* ===== Light Redesign Overrides ===== */
.light-lab {
  --lab-bg: #f7f3ec;
  --lab-surface: #fffefb;
  --lab-surface-soft: #f9f5ef;
  --lab-border: #d8d0c4;
  --lab-border-strong: #bcae99;
  --lab-title: #1f262e;
  --lab-main: #414c58;
  --lab-subtle: #788392;
  --lab-accent: #194fc9;
  --lab-accent-2: #c96a2f;
  --lab-radius-xl: 28px;
  --lab-radius-lg: 18px;
  --lab-radius-md: 12px;
  --lab-shadow-soft: 0 16px 34px rgba(41, 44, 54, 0.12);
  --lab-shadow-card: 0 14px 28px rgba(38, 40, 47, 0.1);
  --lab-display-font: 'Bodoni Moda', 'STZhongsong', 'Noto Serif SC', serif;
  --lab-body-font: 'Plus Jakarta Sans', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  min-height: 100vh;
  padding: 24px;
  color: var(--lab-main);
  font-family: var(--lab-body-font);
  background:
    radial-gradient(circle at 12% -10%, rgba(25, 79, 201, 0.12) 0%, rgba(25, 79, 201, 0) 34%),
    radial-gradient(circle at 89% 0%, rgba(201, 106, 47, 0.16) 0%, rgba(201, 106, 47, 0) 36%),
    linear-gradient(175deg, #fdfaf5 0%, #f7f2ea 48%, #f4efe7 100%);
  position: relative;
  isolation: isolate;
}

.light-lab::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  background-image:
    linear-gradient(rgba(97, 105, 123, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(97, 105, 123, 0.07) 1px, transparent 1px);
  background-size: 34px 34px;
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.4), transparent 78%);
}

.light-lab > * {
  position: relative;
  z-index: 1;
}

.light-lab .lab-header {
  max-width: 1520px;
  margin: 0 auto 18px;
}

.light-lab .header-content {
  display: grid;
  grid-template-columns: minmax(420px, 1.2fr) minmax(320px, 0.8fr);
  gap: 20px;
  align-items: end;
  padding: clamp(18px, 3vw, 34px);
  border-radius: var(--lab-radius-xl);
  border: 1px solid var(--lab-border-strong);
  background:
    linear-gradient(130deg, rgba(255, 255, 255, 0.96) 0%, rgba(249, 245, 238, 0.96) 52%, rgba(244, 238, 228, 0.95) 100%);
  box-shadow: var(--lab-shadow-soft);
  overflow: hidden;
  position: relative;
}

.light-lab .header-content::after {
  content: '展览版';
  position: absolute;
  right: -8px;
  top: -4px;
  font-size: 11px;
  letter-spacing: 0.16em;
  color: rgba(56, 66, 79, 0.28);
  font-weight: 700;
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

.light-lab .header-text h1 {
  margin: 0;
  color: var(--lab-title);
  font-family: var(--lab-display-font);
  font-size: clamp(2rem, 4vw, 3.3rem);
  line-height: 1.05;
  letter-spacing: -0.02em;
  -webkit-text-fill-color: initial;
  background: none;
}

.light-lab .header-text p {
  margin: 12px 0 0;
  color: var(--lab-subtle);
  font-size: 1.02rem;
}

.light-lab .header-actions {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: end;
}

.light-lab .search-box {
  width: min(380px, 46vw);
}

.light-lab .search-box input {
  background: #fff;
  border: 1px solid var(--lab-border);
  border-radius: 12px;
  color: var(--lab-main);
  box-shadow: 0 1px 0 rgba(148, 163, 184, 0.1);
}

.light-lab .search-box input::placeholder {
  color: #8ca0ba;
}

.light-lab .search-box input:focus {
  border-color: #8eacf1;
  box-shadow: 0 0 0 4px rgba(58, 112, 255, 0.14);
}

.light-lab .search-icon {
  color: #7f95b2;
}

.light-lab .add-btn-primary {
  border-radius: 10px;
  background: linear-gradient(110deg, var(--lab-accent) 0%, #325fc9 62%, #7f4fbe 130%);
  box-shadow: 0 10px 20px rgba(38, 45, 67, 0.26);
  padding-inline: 20px;
}

.light-lab .add-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(37, 83, 173, 0.34);
}

.light-lab .lab-overview {
  max-width: 1520px;
  margin: 0 auto 18px;
  display: grid;
  grid-template-columns: 1.15fr 1fr 1fr 1.3fr;
  gap: 12px;
}

.light-lab .overview-card {
  border-radius: 12px;
  border: 1px solid #d7cec0;
  border-left: 5px solid #2c3f62;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 242, 233, 0.96) 100%);
  box-shadow: 0 10px 20px rgba(41, 44, 54, 0.1);
  padding: 14px 16px;
  display: grid;
  gap: 6px;
  position: relative;
  overflow: hidden;
}

.light-lab .overview-card::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(90deg, rgba(44, 63, 98, 0.08) 1px, transparent 1px);
  background-size: 10px 100%;
  opacity: 0.3;
}

.light-lab .overview-card-wide {
  border-left-color: #ae5d33;
  background: linear-gradient(120deg, #fcf4ec 0%, #fffbf7 100%);
}

.light-lab .overview-label {
  font-size: 12px;
  letter-spacing: 0.04em;
  text-transform: none;
  color: #5f6980;
  font-weight: 700;
}

.light-lab .overview-value {
  color: var(--lab-title);
  font-size: 1.48rem;
  line-height: 1.1;
  font-family: var(--lab-display-font);
}

.light-lab .category-nav {
  position: sticky;
  top: 10px;
  z-index: 6;
  max-width: 1520px;
  margin: 0 auto 16px;
  justify-content: flex-start;
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid #d8d0c4;
  background: rgba(255, 252, 247, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: var(--lab-shadow-soft);
}

.light-lab .cat-btn {
  background: #f8f3eb;
  border: 1px solid #d8d0c4;
  color: #586171;
}

.light-lab .cat-btn:hover {
  background: #f1e8db;
  color: #354052;
  border-color: #c3b49f;
}

.light-lab .cat-btn.active {
  background: linear-gradient(120deg, #2c3f62 0%, #43567a 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 10px 22px rgba(44, 53, 72, 0.28);
}

.light-lab .cat-btn .cat-count {
  background: #e6ded0;
  color: #5f6a7b;
}

.light-lab .cat-btn.active .cat-count {
  background: rgba(255, 255, 255, 0.24);
  color: #fff;
}

.light-lab .animation-grid {
  max-width: 1520px;
  margin: 0 auto;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 18px;
}

.light-lab .anim-card {
  border-radius: 20px;
  border: 1px solid #ddd2c3;
  background: var(--lab-surface);
  box-shadow: var(--lab-shadow-card);
}

.light-lab .anim-card:hover {
  transform: translateY(-6px);
  border-color: #c6b69f;
  background: #fff;
  box-shadow: 0 24px 38px rgba(36, 39, 47, 0.16);
}

.light-lab .preview-area {
  min-height: 220px;
  border-bottom: 1px solid #e8ddce;
  background:
    radial-gradient(circle at 12% 10%, rgba(44, 63, 98, 0.12) 0%, rgba(44, 63, 98, 0) 36%),
    radial-gradient(circle at 90% 0%, rgba(174, 93, 51, 0.12) 0%, rgba(174, 93, 51, 0) 32%),
    linear-gradient(180deg, #faf5ed 0%, #f2e9dd 100%);
}

.light-lab .demo-bigscreen {
  border-radius: 16px;
  border: 1px solid #c7d8f3;
  background: linear-gradient(145deg, #e7eefb 0%, #dbe7f8 100%);
  box-shadow: inset 0 0 22px rgba(16, 32, 58, 0.08);
}

.light-lab .card-info {
  background: #fff;
  padding: 16px;
}

.light-lab .info-header h3 {
  color: var(--lab-title);
  font-size: 1.06rem;
}

.light-lab .category-tag {
  border-radius: 999px;
  border: 1px solid #cad9f1;
  background: #f0f5ff;
  color: #406291;
}

.light-lab .description {
  color: var(--lab-subtle);
  line-height: 1.65;
}

.light-lab .card-actions {
  gap: 8px;
}

.light-lab .action-btn {
  border-radius: 10px;
  border: 1px solid var(--lab-border);
  background: #f9fbff;
  color: #4b658a;
}

.light-lab .action-btn.primary {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(120deg, var(--lab-accent) 0%, #4678ff 100%);
}

.light-lab .action-btn.primary:hover {
  filter: brightness(1.03);
}

.light-lab .action-btn.danger {
  color: #cb3a3a;
  border-color: #f3c4c4;
  background: #fff5f5;
}

.light-lab .action-btn.danger:hover {
  background: #ffeaea;
  border-color: #ea9898;
}

.light-lab .no-results,
.light-lab .loading-box {
  grid-column: 1 / -1;
  border: 1px dashed var(--lab-border-strong);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.84);
  color: var(--lab-subtle);
}

.light-lab .spinner {
  border-color: #d5e2f8;
  border-top-color: var(--lab-accent);
}

.modal-mask {
  background: rgba(233, 240, 252, 0.7);
  backdrop-filter: blur(12px);
}

.code-dialog,
.form-dialog {
  border: 1px solid #d5e3f8;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 30px 64px rgba(16, 32, 58, 0.28);
}

.dialog-header {
  border-bottom: 1px solid #e1eaf7;
  background: linear-gradient(120deg, #f3f7ff 0%, #fff7f2 100%);
}

.dialog-header h3 {
  color: #193053;
}

.close-btn {
  border: 1px solid #d0def4;
  background: #fff;
  color: #4e6a91;
}

.close-btn:hover {
  border-color: #a7c0e8;
  color: #2f568c;
  background: #f4f8ff;
}

.dialog-body {
  background: #fbfdff;
}

.dialog-body pre {
  border: 1px solid #d8e4f8;
  background: #0f1f36;
}

.dialog-footer {
  border-top: 1px solid #e1eaf7;
  background: #fff;
}

.copy-btn,
.save-btn {
  border-radius: 10px;
  color: #ffffff;
  background: linear-gradient(120deg, var(--lab-accent, #194fc9) 0%, #4678ff 100%);
}

.cancel-btn {
  border-radius: 10px;
  border-color: #cad9f2;
  color: #4d678d;
  background: #f7faff;
}

.form-field label {
  color: #4d678d;
}

.form-field input,
.form-field select,
.form-field textarea {
  border: 1px solid #d7e4f8;
  background: #fff;
  color: #294469;
}

.form-field input:focus,
.form-field select:focus,
.form-field textarea:focus {
  border-color: #8fafe9;
  box-shadow: 0 0 0 4px rgba(58, 112, 255, 0.14);
}

@media (max-width: 1180px) {
  .light-lab .header-content {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .light-lab .header-actions {
    width: 100%;
    grid-template-columns: 1fr;
  }

  .light-lab .search-box {
    width: 100%;
  }

  .light-lab .lab-overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .light-lab {
    padding: 14px;
  }

  .light-lab .header-content {
    border-radius: 16px;
    padding: 14px;
  }

  .light-lab .header-content::after {
    display: none;
  }

  .light-lab .header-text h1 {
    font-size: 1.9rem;
  }

  .light-lab .lab-overview {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .light-lab .category-nav {
    top: 6px;
    padding: 10px;
  }

  .light-lab .animation-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .light-lab .preview-area {
    min-height: 200px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .light-lab .anim-card,
  .light-lab .cat-btn,
  .light-lab .add-btn-primary,
  .light-lab .action-btn {
    transition-duration: 0.01ms !important;
  }
}
</style>
