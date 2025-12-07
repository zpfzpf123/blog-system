<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'

// 性能优化：检测是否应该减少动画
const shouldReduceMotion = () => {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

// 轻量级粒子系统 - 大幅减少粒子数量和更新频率
const initParticles = () => {
  if (shouldReduceMotion()) return // 如果用户偏好减少动画，直接跳过

  const canvas = document.getElementById('particle-canvas') as HTMLCanvasElement
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  let width = window.innerWidth
  let height = window.innerHeight
  canvas.width = width
  canvas.height = height

  const particles: Particle[] = []
  // 大幅减少粒子数量：移动端8个，桌面端15个
  const particleCount = window.innerWidth < 768 ? 8 : 15

  class Particle {
    x: number
    y: number
    vx: number
    vy: number
    size: number
    color: string

    constructor() {
      this.x = Math.random() * width
      this.y = Math.random() * height
      // 降低移动速度
      this.vx = (Math.random() - 0.5) * 0.2
      this.vy = (Math.random() - 0.5) * 0.2
      this.size = Math.random() * 2 + 1
      this.color = `rgba(${120 + Math.random() * 60}, ${120 + Math.random() * 60}, 255, ${0.08 + Math.random() * 0.12})`
    }

    update() {
      this.x += this.vx
      this.y += this.vy

      if (this.x < 0) this.x = width
      if (this.x > width) this.x = 0
      if (this.y < 0) this.y = height
      if (this.y > height) this.y = 0
    }

    draw() {
      if (!ctx) return
      ctx.beginPath()
      ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
      ctx.fillStyle = this.color
      ctx.fill()
    }
  }

  for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle())
  }

  let animationId: number
  let lastTime = 0
  const fps = 30 // 限制帧率为30fps以减少CPU占用

  const animate = (currentTime: number) => {
    animationId = requestAnimationFrame(animate)
    
    // 帧率限制
    if (currentTime - lastTime < 1000 / fps) return
    lastTime = currentTime

    ctx.clearRect(0, 0, width, height)
    particles.forEach((p) => {
      p.update()
      p.draw()
    })
  }

  animationId = requestAnimationFrame(animate)

  // 防抖处理resize事件
  let resizeTimeout: number
  const handleResize = () => {
    clearTimeout(resizeTimeout)
    resizeTimeout = window.setTimeout(() => {
      width = window.innerWidth
      height = window.innerHeight
      canvas.width = width
      canvas.height = height
    }, 200)
  }

  window.addEventListener('resize', handleResize)

  return () => {
    cancelAnimationFrame(animationId)
    clearTimeout(resizeTimeout)
    window.removeEventListener('resize', handleResize)
  }
}

let cleanup: (() => void) | undefined

onMounted(() => {
  // 延迟初始化，避免影响首屏渲染
  setTimeout(() => {
    cleanup = initParticles()
  }, 500)
})

onUnmounted(() => {
  if (cleanup) cleanup()
})
</script>

<template>
  <div class="background-container">
    <div class="gradient-bg"></div>
    <canvas id="particle-canvas" class="particle-canvas"></canvas>
    <!-- 简化为2个形状，移除第三个 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
    </div>
  </div>
</template>

<style scoped>
.background-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
}

.gradient-bg {
  position: absolute;
  inset: 0;
  /* 简化渐变，减少颜色停止点 */
  background: linear-gradient(
    135deg,
    #fdfbff 0%,
    #f0f4ff 30%,
    rgba(243, 232, 255, 0.5) 60%,
    #ffffff 100%
  );
  /* 移除动画，使用静态渐变 */
  opacity: 0.98;
}

.particle-canvas {
  position: absolute;
  inset: 0;
  opacity: 0.25;
  mix-blend-mode: screen;
}

.bg-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.25;
  /* 大幅延长动画周期，减少CPU占用 */
  animation: float 60s infinite linear;
  mix-blend-mode: multiply;
  /* 使用 transform 优化性能 */
  will-change: transform;
  contain: layout style paint;
}

.shape-1 {
  top: -10%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, 
    rgba(99, 102, 241, 0.3) 0%, 
    rgba(139, 92, 246, 0.15) 50%,
    transparent 100%);
  animation-delay: 0s;
}

.shape-2 {
  top: 40%;
  right: -10%;
  width: 450px;
  height: 450px;
  background: radial-gradient(circle, 
    rgba(236, 72, 153, 0.25) 0%, 
    rgba(251, 146, 60, 0.12) 50%,
    transparent 100%);
  animation-delay: -30s;
}

/* 移除 ::after 伪元素动画，减少渲染负担 */

/* 简化的浮动动画 */
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

/* 减少动画（无障碍） */
@media (prefers-reduced-motion: reduce) {
  .shape {
    animation: none;
  }
}

/* 移动端进一步简化 */
@media (max-width: 768px) {
  .shape {
    animation: none;
    opacity: 0.15;
  }
  
  .particle-canvas {
    opacity: 0.15;
  }
}
</style>
