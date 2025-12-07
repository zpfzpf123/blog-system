
<!--
 * @Author: 18582297328 zpfzpf123@example.com
 * @Date: 2025-08-04 12:55:14
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-11-30 16:26:13
 * @FilePath: \blog\src\App.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { HomeFilled, Setting, ChatDotRound, Link, ArrowUpBold, MagicStick, FolderOpened, Key, Tools } from '@element-plus/icons-vue'
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
    <header class="app-header">
      <div class="header-content">
        <!-- Logo区域 -->
        <div class="header-brand">
          <RouterLink to="/" class="brand-link">
            <div class="brand-logo">
              <span class="logo-icon">✨</span>
            </div>
            <span class="brand-text">DevHub</span>
          </RouterLink>
        </div>
        
        <!-- 导航菜单 -->
        <nav class="main-nav">
          <RouterLink to="/" class="nav-item">
            <el-icon class="nav-icon"><HomeFilled /></el-icon>
            <span class="nav-text">首页</span>
          </RouterLink>
          <RouterLink to="/admin" class="nav-item">
            <el-icon class="nav-icon"><Setting /></el-icon>
            <span class="nav-text">管理</span>
          </RouterLink>
          <RouterLink to="/websites" class="nav-item">
            <el-icon class="nav-icon"><Link /></el-icon>
            <span class="nav-text">网站</span>
          </RouterLink>
          <RouterLink to="/ai-chat" class="nav-item">
            <el-icon class="nav-icon"><ChatDotRound /></el-icon>
            <span class="nav-text">AI</span>
          </RouterLink>
          <RouterLink to="/css-animations" class="nav-item">
            <el-icon class="nav-icon"><MagicStick /></el-icon>
            <span class="nav-text">动画</span>
          </RouterLink>
          <RouterLink to="/project-manager" class="nav-item">
            <el-icon class="nav-icon"><FolderOpened /></el-icon>
            <span class="nav-text">项目</span>
          </RouterLink>
          <RouterLink to="/git-users" class="nav-item">
            <el-icon class="nav-icon"><Key /></el-icon>
            <span class="nav-text">Git</span>
          </RouterLink>
          <RouterLink to="/dev-tools" class="nav-item">
            <el-icon class="nav-icon"><Tools /></el-icon>
            <span class="nav-text">工具</span>
          </RouterLink>
        </nav>
        
        <!-- 右侧操作区 -->
        <div class="header-actions">
          <div class="theme-indicator" title="当前主题">
            <span class="indicator-dot"></span>
          </div>
        </div>
      </div>
    </header>

    <main>
      <RouterView />
    </main>

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
          <circle class="progress-ring-bg" cx="60" cy="60" r="54" stroke-width="4" />
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
:deep(.el-dialog) {
  margin: 5vh auto 50px !important;
}

/* 布局容器 */
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-body);
  font-family: var(--font-sans);
}

/* 顶部导航栏 */
.app-header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  background: var(--bg-glass-strong);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.08);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02), 0 4px 12px rgba(99, 102, 241, 0.04);
  animation: slideDown 0.5s var(--ease-out);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-100%);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 var(--spacing-6);
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  gap: var(--spacing-8);
}

/* Logo品牌区 */
.header-brand {
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  text-decoration: none;
  transition: transform var(--transition-fast);
}

.brand-link:hover {
  transform: scale(1.02);
}

.brand-logo {
  width: 40px;
  height: 40px;
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-normal);
}

.brand-link:hover .brand-logo {
  transform: rotate(-5deg) scale(1.05);
  box-shadow: var(--shadow-primary-lg);
}

.logo-icon {
  font-size: 1.4rem;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

.brand-text {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

/* 导航菜单 */
.main-nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  flex: 1;
  justify-content: center;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-4);
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
  font-size: var(--text-sm);
  text-decoration: none;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity var(--transition-normal);
  border-radius: inherit;
}

.nav-item:hover {
  color: var(--primary-color);
  background: var(--primary-lighter);
}

.nav-item:hover .nav-icon {
  transform: scale(1.1) translateY(-1px);
}

.nav-item.router-link-exact-active {
  color: var(--text-inverse);
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary);
}

.nav-item.router-link-exact-active .nav-icon {
  color: var(--text-inverse);
}

.nav-icon {
  font-size: 1.15rem;
  transition: all var(--transition-normal);
}

/* 为不同导航项设置图标颜色 */
.nav-item:nth-child(1) .nav-icon { color: var(--primary-color); }
.nav-item:nth-child(2) .nav-icon { color: #8b5cf6; }
.nav-item:nth-child(3) .nav-icon { color: #06b6d4; }
.nav-item:nth-child(4) .nav-icon { color: #10b981; }
.nav-item:nth-child(5) .nav-icon { color: #f97316; }
.nav-item:nth-child(6) .nav-icon { color: #ec4899; }
.nav-item:nth-child(7) .nav-icon { color: #14b8a6; }
.nav-item:nth-child(8) .nav-icon { color: #eab308; }

.nav-item.router-link-exact-active .nav-icon,
.nav-item:hover .nav-icon {
  color: inherit;
}

.nav-text {
  white-space: nowrap;
}

/* 右侧操作区 */
.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  flex-shrink: 0;
}

.theme-indicator {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: var(--bg-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.theme-indicator:hover {
  background: var(--primary-lighter);
}

.indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--gradient-primary);
  animation: pulse 2s infinite;
}

/* 主内容区 */
main {
  flex: 1;
  display: block;
  min-height: calc(100vh - 64px);
  background: transparent;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .header-content {
    padding: 0 var(--spacing-4);
    gap: var(--spacing-4);
  }
  
  .main-nav {
    gap: 0;
  }
  
  .nav-item {
    padding: var(--spacing-2) var(--spacing-3);
  }
  
  .nav-text {
    display: none;
  }
  
  .nav-icon {
    font-size: 1.3rem;
  }
  
  .brand-text {
    display: none;
  }
}

@media (max-width: 640px) {
  .header-content {
    height: 56px;
    padding: 0 var(--spacing-3);
  }
  
  .brand-logo {
    width: 36px;
    height: 36px;
  }
  
  .nav-item {
    padding: var(--spacing-2);
  }
  
  .nav-icon {
    font-size: 1.2rem;
  }
  
  .header-actions {
    display: none;
  }
}

/* 滚动到顶部按钮 */
.scroll-to-top-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 52px;
  height: 52px;
  background: var(--bg-glass-strong);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--shadow-lg), 0 0 0 1px rgba(99, 102, 241, 0.1);
  transition: all var(--transition-spring);
  z-index: var(--z-fixed);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.scroll-to-top-btn:hover {
  transform: translateY(-6px) scale(1.08);
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary-lg);
  border-color: transparent;
}

.scroll-to-top-btn:active {
  transform: translateY(-2px) scale(1.02);
}

.scroll-to-top-icon {
  font-size: 20px;
  color: var(--primary-color);
  transition: all var(--transition-normal);
  z-index: 2;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scroll-to-top-btn:hover .scroll-to-top-icon {
  color: var(--text-inverse);
  transform: translateY(-2px);
}

/* 进度环 */
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
  stroke: var(--gray-200);
  stroke-linecap: round;
}

.progress-ring-fill {
  fill: none;
  stroke: var(--primary-color);
  stroke-linecap: round;
  transition: stroke-dashoffset var(--transition-normal);
}

.scroll-to-top-btn:hover .progress-ring-fill {
  stroke: rgba(255, 255, 255, 0.8);
}

.progress-text {
  font-size: 11px;
  font-weight: var(--font-bold);
  color: var(--primary-color);
  transition: all var(--transition-normal);
}

.scroll-to-top-btn:hover .progress-text {
  color: var(--text-inverse);
}

.scroll-to-top-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.4);
  border-radius: var(--radius-full);
  transform: translate(-50%, -50%);
  animation: rippleEffect 0.6s ease-out;
}

@keyframes rippleEffect {
  0% { width: 0; height: 0; opacity: 1; }
  100% { width: 100px; height: 100px; opacity: 0; }
}

/* 滚动按钮动画 */
.scroll-to-top-enter-active,
.scroll-to-top-leave-active {
  transition: all var(--transition-spring);
}

.scroll-to-top-enter-from {
  opacity: 0;
  transform: translateY(24px) scale(0.6);
}

.scroll-to-top-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.8);
}

/* 响应式 */
@media (max-width: 768px) {
  .scroll-to-top-btn {
    bottom: 24px;
    right: 24px;
    width: 46px;
    height: 46px;
  }

  .scroll-to-top-icon {
    font-size: 18px;
  }

  .progress-text {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .scroll-to-top-btn {
    bottom: 20px;
    right: 20px;
    width: 42px;
    height: 42px;
  }
}
</style>
<style>
/* 全局平滑滚动 */
html {
  scroll-behavior: smooth;
}

body,
#app {
  background: #f7f8fa !important;
}
</style>
