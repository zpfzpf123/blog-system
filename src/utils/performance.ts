/**
 * 性能优化工具函数
 * 包含防抖、节流、图片懒加载等功能
 */

/**
 * 防抖函数
 * @param func 要防抖的函数
 * @param wait 等待时间（毫秒）
 * @param immediate 是否立即执行
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
  immediate = false
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null

  return function (this: any, ...args: Parameters<T>) {
    const context = this

    const later = () => {
      timeout = null
      if (!immediate) func.apply(context, args)
    }

    const callNow = immediate && !timeout

    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(later, wait)

    if (callNow) func.apply(context, args)
  }
}

/**
 * 节流函数
 * @param func 要节流的函数
 * @param wait 等待时间（毫秒）
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let previous = 0

  return function (this: any, ...args: Parameters<T>) {
    const context = this
    const now = Date.now()
    const remaining = wait - (now - previous)

    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func.apply(context, args)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now()
        timeout = null
        func.apply(context, args)
      }, remaining)
    }
  }
}

/**
 * 图片懒加载
 * @param selector 选择器，默认为所有 data-src 属性的图片
 */
export function lazyLoadImages(selector = 'img[data-src]') {
  const images = document.querySelectorAll<HTMLImageElement>(selector)
  
  if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries, observer) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const img = entry.target as HTMLImageElement
          const src = img.getAttribute('data-src')
          
          if (src) {
            img.src = src
            img.removeAttribute('data-src')
            imageObserver.unobserve(img)
          }
        }
      })
    })

    images.forEach((img) => imageObserver.observe(img))
  } else {
    // 降级处理：不支持 IntersectionObserver 的浏览器直接加载
    images.forEach((img) => {
      const src = img.getAttribute('data-src')
      if (src) {
        img.src = src
        img.removeAttribute('data-src')
      }
    })
  }
}

/**
 * 请求动画帧节流
 * @param func 要执行的函数
 */
export function rafThrottle<T extends (...args: any[]) => any>(
  func: T
): (...args: Parameters<T>) => void {
  let rafId: number | null = null

  return function (this: any, ...args: Parameters<T>) {
    const context = this

    if (rafId) {
      cancelAnimationFrame(rafId)
    }

    rafId = requestAnimationFrame(() => {
      func.apply(context, args)
      rafId = null
    })
  }
}

/**
 * 预加载资源
 * @param urls 资源 URL 数组
 */
export function preloadResources(urls: string[]) {
  urls.forEach((url) => {
    const link = document.createElement('link')
    link.rel = 'preload'
    
    // 根据文件扩展名设置 as 属性
    if (url.endsWith('.css')) {
      link.as = 'style'
    } else if (url.endsWith('.js')) {
      link.as = 'script'
    } else if (/\.(jpg|jpeg|png|gif|webp)$/i.test(url)) {
      link.as = 'image'
    } else if (url.endsWith('.woff') || url.endsWith('.woff2')) {
      link.as = 'font'
      link.crossOrigin = 'anonymous'
    }
    
    link.href = url
    document.head.appendChild(link)
  })
}

/**
 * 延迟加载组件
 * @param componentLoader 组件加载函数
 * @param delay 延迟时间（毫秒）
 */
export function lazyComponent<T>(
  componentLoader: () => Promise<T>,
  delay = 200
): Promise<T> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(componentLoader())
    }, delay)
  })
}
