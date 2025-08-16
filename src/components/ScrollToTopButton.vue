<template>
  <Transition name="scroll-to-top">
    <div
      v-show="isVisible"
      class="scroll-to-top-btn"
      @click="scrollToTop"
      :class="{ 'scroll-to-top-btn--hover': isHovered }"
      @mouseenter="isHovered = true"
      @mouseleave="isHovered = false"
      :style="customStyle"
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
        <component :is="icon" v-if="isHovered" />
        <span v-else class="progress-text">{{ scrollProgress }}%</span>
      </el-icon>

      <div class="scroll-to-top-ripple" v-if="isHovered"></div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ArrowUpBold, Top } from '@element-plus/icons-vue'

interface Props {
  threshold?: number // 显示按钮的滚动阈值
  icon?: string // 图标名称
  position?: 'bottom-right' | 'bottom-left' | 'top-right' | 'top-left' // 按钮位置
  size?: 'small' | 'medium' | 'large' // 按钮大小
  color?: string // 自定义颜色
}

const props = withDefaults(defineProps<Props>(), {
  threshold: 300,
  icon: 'ArrowUpBold',
  position: 'bottom-right',
  size: 'medium',
  color: '',
})

const isVisible = ref(false)
const isHovered = ref(false)
const scrollProgress = ref(0)

// 图标映射
const iconMap = {
  ArrowUpBold,
  Top,
}

const icon = computed(() => iconMap[props.icon as keyof typeof iconMap] || ArrowUpBold)

// 自定义样式
const customStyle = computed(() => {
  const styles: Record<string, string> = {}

  // 位置样式
  switch (props.position) {
    case 'bottom-left':
      styles.bottom = '30px'
      styles.left = '30px'
      styles.right = 'auto'
      break
    case 'top-right':
      styles.top = '30px'
      styles.right = '30px'
      styles.bottom = 'auto'
      break
    case 'top-left':
      styles.top = '30px'
      styles.left = '30px'
      styles.bottom = 'auto'
      styles.right = 'auto'
      break
    default: // bottom-right
      styles.bottom = '30px'
      styles.right = '30px'
  }

  // 大小样式
  switch (props.size) {
    case 'small':
      styles.width = '44px'
      styles.height = '44px'
      break
    case 'large':
      styles.width = '64px'
      styles.height = '64px'
      break
    default: // medium
      styles.width = '56px'
      styles.height = '56px'
  }

  // 自定义颜色
  if (props.color) {
    styles.background = props.color
  }

  return styles
})

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
  isVisible.value = scrollTop > props.threshold
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.scroll-to-top-btn {
  position: fixed;
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
    bottom: 20px !important;
    right: 20px !important;
    width: 48px !important;
    height: 48px !important;
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
    bottom: 15px !important;
    right: 15px !important;
    width: 44px !important;
    height: 44px !important;
  }

  .scroll-to-top-icon {
    font-size: 18px;
  }

  .progress-text {
    font-size: 9px;
  }
}
</style>
