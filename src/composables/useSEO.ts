/**
 * SEO Composable
 * 在组件中使用 SEO 优化功能
 */
import { onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { setPageSEO } from '@/utils/seo'

/**
 * 使用 SEO 优化的组合式函数
 * @param options SEO 配置选项
 */
export function useSEO(options: {
  title?: string
  description?: string
  keywords?: string | string[]
  ogImage?: string
}) {
  const route = useRoute()

  onMounted(() => {
    // 设置 SEO 信息
    setPageSEO({
      ...options,
      canonical: window.location.href,
    })
  })

  // 路由变化时更新 SEO
  const updateSEO = () => {
    setPageSEO({
      ...options,
      canonical: window.location.href,
    })
  }

  // 在路由变化时调用（可在组件中手动调用）
  return {
    updateSEO,
  }
}
