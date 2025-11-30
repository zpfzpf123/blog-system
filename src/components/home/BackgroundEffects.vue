<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'

// 简单的粒子系统
const initParticles = () => {
  const canvas = document.getElementById('particle-canvas') as HTMLCanvasElement
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  let width = window.innerWidth
  let height = window.innerHeight
  canvas.width = width
  canvas.height = height

  const particles: Particle[] = []
  const particleCount = window.innerWidth < 768 ? 20 : 50 // 移动端减少粒子数

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
      this.vx = (Math.random() - 0.5) * 0.5
      this.vy = (Math.random() - 0.5) * 0.5
      this.size = Math.random() * 3
      this.color = `rgba(${100 + Math.random() * 100}, ${100 + Math.random() * 100}, 255, ${0.1 + Math.random() * 0.2})`
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

  const animate = () => {
    ctx.clearRect(0, 0, width, height)
    particles.forEach((p) => {
      p.update()
      p.draw()
    })
    animationId = requestAnimationFrame(animate)
  }

  animate()

  const handleResize = () => {
    width = window.innerWidth
    height = window.innerHeight
    canvas.width = width
    canvas.height = height
  }

  window.addEventListener('resize', handleResize)

  return () => {
    cancelAnimationFrame(animationId)
    window.removeEventListener('resize', handleResize)
  }
}

let cleanup: (() => void) | undefined

onMounted(() => {
  cleanup = initParticles()
})

onUnmounted(() => {
  if (cleanup) cleanup()
})
</script>

<template>
  <div class="background-container">
    <div class="gradient-bg"></div>
    <canvas id="particle-canvas" class="particle-canvas"></canvas>
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
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
  background: linear-gradient(
    135deg,
    #fdfbff 0%,
    #f0f4ff 15%,
    #e5eaff 30%,
    #f3e8ff 45%,
    #fdf2f8 60%,
    #f0f9ff 75%,
    #fefeff 90%,
    #ffffff 100%
  );
  background-size: 400% 400%;
  animation: gradientFlow 25s ease infinite;
  opacity: 0.95;
}

.particle-canvas {
  position: absolute;
  inset: 0;
  opacity: 0.4;
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
  opacity: 0.35;
  animation: float 25s infinite ease-in-out;
  mix-blend-mode: multiply;
}

.shape-1 {
  top: -15%;
  left: -15%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #c7d2fe 0%, #e0e7ff 50%, transparent 100%);
  animation-delay: 0s;
}

.shape-2 {
  top: 35%;
  right: -15%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #fbcfe8 0%, #fce7f3 50%, transparent 100%);
  animation-delay: -8s;
}

.shape-3 {
  bottom: -15%;
  left: 15%;
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, #e9d5ff 0%, #f3e8ff 50%, transparent 100%);
  animation-delay: -15s;
}

@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  25% { background-position: 50% 75%; }
  50% { background-position: 100% 50%; }
  75% { background-position: 50% 25%; }
  100% { background-position: 0% 50%; }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg) scale(1); }
  25% { transform: translate(40px, -60px) rotate(15deg) scale(1.05); }
  50% { transform: translate(-30px, 30px) rotate(-10deg) scale(0.95); }
  75% { transform: translate(20px, -40px) rotate(8deg) scale(1.02); }
}
</style>
