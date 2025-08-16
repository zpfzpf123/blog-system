<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { HomeFilled, Setting, ChatDotRound, Link, ArrowUpBold } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import { ref, onMounted, onUnmounted } from 'vue'

// 滚动到顶部功能
const isScrollToTopVisible = ref(false)
const isScrollToTopHovered = ref(false)
const scrollProgress = ref(0)

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  })
}

const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  const progress = docHeight > 0 ? Math.min((scrollTop / docHeight) * 100, 100) : 0

  scrollProgress.value = Math.round(progress)
  isScrollToTopVisible.value = scrollTop > 300
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="layout-container">
    <header>
      <div class="header-content">
        <nav class="main-nav">
          <RouterLink to="/">
            <el-icon class="nav-icon animated-nav-icon"><HomeFilled /></el-icon>
            <span>首页</span>
          </RouterLink>
          <RouterLink to="/admin">
            <el-icon class="nav-icon animated-nav-icon"><Setting /></el-icon>
            <span>管理</span>
          </RouterLink>
          <RouterLink to="/websites">
            <el-icon class="nav-icon animated-nav-icon"><Link /></el-icon>
            <span>网站合集</span>
          </RouterLink>
          <RouterLink to="/ai-chat">
            <el-icon class="nav-icon animated-nav-icon"><ChatDotRound /></el-icon>
            <span>AI问答</span>
          </RouterLink>
        </nav>
      </div>
    </header>

    <main>
      <RouterView />
    </main>

    <footer>
      <div class="footer-content">
        <p>&copy; 2025 我的博客. All rights reserved.</p>
      </div>
    </footer>

    <!-- 滚动到顶部按钮 -->
    <Transition name="scroll-to-top">
      <div
        v-show="isScrollToTopVisible"
        class="scroll-to-top-btn"
        @click="scrollToTop"
        :class="{ 'scroll-to-top-btn--hover': isScrollToTopHovered }"
        @mouseenter="isScrollToTopHovered = true"
        @mouseleave="isScrollToTopHovered = false"
      >
        <!-- 进度环 -->
        <svg class="progress-ring" viewBox="0 0 120 120">
          <circle
            class="progress-ring-bg"
            cx="60"
            cy="60"
            r="54"
            stroke-width="4"
          />
          <circle
            class="progress-ring-fill"
            cx="60"
            cy="60"
            r="54"
            stroke-width="4"
            :stroke-dasharray="`${2 * Math.PI * 54}`"
            :stroke-dashoffset="`${2 * Math.PI * 54 * (1 - scrollProgress / 100)}`"
          />
        </svg>

        <!-- 图标 -->
        <el-icon class="scroll-to-top-icon">
          <ArrowUpBold v-if="isScrollToTopHovered" />
          <span v-else class="progress-text">{{ scrollProgress }}%</span>
        </el-icon>

        <div class="scroll-to-top-ripple" v-if="isScrollToTopHovered"></div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f6fa;
  font-family: 'Segoe UI', 'PingFang SC', 'Hiragino Sans', Arial, sans-serif;
}
header {
  background: #e9edf3;
  color: #222;
  padding: 1.2rem 0 1rem 0;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  box-shadow: none;
  position: relative;
  z-index: 10;
  animation: fadeInDown 0.8s;
}
header::after {
  display: none;
}
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.header-content {
  /* max-width: 1200px; */
  margin: 0 auto;
  padding: 0 40px;
}
.main-nav {
  display: flex;
  gap: 2.2rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.1);
  padding: 0.3rem 1.2rem;
  align-items: center;
  margin: 0 auto;
  justify-content: flex-start;
  transition:
    box-shadow 0.3s,
    background 0.3s;
}
.main-nav:hover {
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.18);
  background: rgba(255, 255, 255, 0.18);
}
.main-nav a {
  color: #222;
  text-decoration: none;
  font-weight: 600;
  font-size: 1.08rem;
  padding: 0.5rem 1.2rem;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition:
    background 0.3s,
    color 0.3s,
    transform 0.2s;
  position: relative;
  overflow: hidden;
}
.main-nav a.router-link-exact-active {
  background: linear-gradient(90deg, #42e695 0%, #3bb2b8 100%);
  color: #fff;
  box-shadow: 0 2px 12px rgba(66, 230, 149, 0.12);
  transform: scale(1.08);
}
.main-nav a:hover {
  background: linear-gradient(90deg, #43cea2 0%, #185a9d 100%);
  color: #fff;
  transform: scale(1.07) translateY(-2px);
  box-shadow: 0 4px 16px rgba(66, 230, 149, 0.18);
}
.nav-icon {
  font-size: 1.3em;
  margin-right: 2px;
  color: #fff;
  transition:
    color 0.3s,
    transform 0.3s;
}
.animated-nav-icon {
  animation: iconPulse 2.2s infinite;
}
@keyframes iconPulse {
  0%,
  100% {
    filter: drop-shadow(0 0 0 #42a5f5);
    transform: scale(1);
  }
  50% {
    filter: drop-shadow(0 0 8px #42a5f5);
    transform: scale(1.18) rotate(-8deg);
  }
}
.main-nav a:hover .nav-icon {
  color: #ffd700;
  transform: scale(1.18) rotate(-8deg);
}
main {
  flex: 1;
  padding: 2.5rem 0 2rem 0;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 600px;
  background: transparent;
}
.main-content-card {
  width: 100%;

  max-width: 100%;
  min-height: 700px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 24px;
  box-shadow: 0 8px 40px 0 rgba(102, 126, 234, 0.13);
  padding: 32px 18px 32px 18px;
  margin: 0 12px;
  transition:
    box-shadow 0.3s,
    background 0.3s;
  animation: fadeInUp 0.8s;
}
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
footer {
  background: #e9edf3;
  color: #222;
  padding: 1rem 0;
  margin-top: auto;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
  box-shadow: none;
}
.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
  color: #666;
  font-size: 1rem;
}
@media (max-width: 900px) {
  .main-content-card {
    padding: 16px 4px 16px 4px;
    min-height: 400px;
    border-radius: 14px;
  }
  main {
    padding: 1.2rem 0 1rem 0;
  }
}
@media (max-width: 600px) {
  .main-content-card {
    padding: 6px 2px 6px 2px;
    min-height: 200px;
    border-radius: 8px;
  }
  main {
    padding: 0.5rem 0 0.5rem 0;
  }
}

/* 滚动到顶部按钮样式 */
.scroll-to-top-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  overflow: hidden;
}

.scroll-to-top-btn:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #42e695 0%, #3bb2b8 100%);
}

.scroll-to-top-btn--hover {
  animation: pulse 0.6s ease-in-out;
}

.scroll-to-top-icon {
  font-size: 24px;
  color: white;
  transition: all 0.3s ease;
  z-index: 2;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scroll-to-top-btn:hover .scroll-to-top-icon {
  transform: translateY(-2px);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

/* 进度环样式 */
.progress-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  z-index: 1;
}

.progress-ring-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.2);
  stroke-linecap: round;
}

.progress-ring-fill {
  fill: none;
  stroke: white;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.3s ease;
}

/* 进度文本样式 */
.progress-text {
  font-size: 12px;
  font-weight: 600;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.scroll-to-top-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: ripple 0.6s ease-out;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes ripple {
  0% {
    width: 0;
    height: 0;
    opacity: 1;
  }
  100% {
    width: 100px;
    height: 100px;
    opacity: 0;
  }
}

/* 进入和离开动画 */
.scroll-to-top-enter-active,
.scroll-to-top-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scroll-to-top-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.8);
}

.scroll-to-top-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.8);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .scroll-to-top-btn {
    bottom: 20px;
    right: 20px;
    width: 48px;
    height: 48px;
  }

  .scroll-to-top-icon {
    font-size: 20px;
  }

  .progress-text {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .scroll-to-top-btn {
    bottom: 15px;
    right: 15px;
    width: 44px;
    height: 44px;
  }

  .scroll-to-top-icon {
    font-size: 18px;
  }

  .progress-text {
    font-size: 9px;
  }
}
</style>
<style>
body,
#app {
  background: #f7f8fa !important;
}
</style>
