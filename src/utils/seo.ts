/**
 * SEO 优化工具
 * 用于动态管理页面标题、描述等 SEO 信息
 */

/**
 * 设置页面标题
 * @param title 页面标题
 * @param siteName 网站名称，默认为 'AI智能博客系统'
 */
export function setPageTitle(title: string, siteName = 'AI智能博客系统') {
  if (title) {
    document.title = `${title} - ${siteName}`
  } else {
    document.title = siteName
  }
  
  // 更新 Open Graph 标题
  updateMetaTag('og:title', document.title)
}

/**
 * 设置页面描述
 * @param description 页面描述
 */
export function setPageDescription(description: string) {
  updateMetaTag('description', description)
  updateMetaTag('og:description', description)
  updateMetaTag('twitter:description', description)
}

/**
 * 设置页面关键词
 * @param keywords 关键词，字符串或数组
 */
export function setPageKeywords(keywords: string | string[]) {
  const keywordsStr = Array.isArray(keywords) ? keywords.join(', ') : keywords
  updateMetaTag('keywords', keywordsStr)
}

/**
 * 更新 meta 标签
 * @param name meta 标签的 name 或 property 属性
 * @param content meta 标签的 content 属性
 */
function updateMetaTag(name: string, content: string) {
  if (!content) return

  // 查找现有的 meta 标签
  let meta = document.querySelector(`meta[name="${name}"]`) as HTMLMetaElement
  if (!meta) {
    meta = document.querySelector(`meta[property="${name}"]`) as HTMLMetaElement
  }

  if (meta) {
    meta.setAttribute('content', content)
  } else {
    // 创建新的 meta 标签
    meta = document.createElement('meta')
    if (name.startsWith('og:') || name.startsWith('twitter:')) {
      meta.setAttribute('property', name)
    } else {
      meta.setAttribute('name', name)
    }
    meta.setAttribute('content', content)
    document.head.appendChild(meta)
  }
}

/**
 * 设置 Canonical URL
 * @param url Canonical URL
 */
export function setCanonicalUrl(url: string) {
  let link = document.querySelector('link[rel="canonical"]') as HTMLLinkElement
  
  if (link) {
    link.setAttribute('href', url)
  } else {
    link = document.createElement('link')
    link.setAttribute('rel', 'canonical')
    link.setAttribute('href', url)
    document.head.appendChild(link)
  }
}

/**
 * 设置页面 SEO 信息
 * @param options SEO 配置选项
 */
export function setPageSEO(options: {
  title?: string
  description?: string
  keywords?: string | string[]
  canonical?: string
  ogImage?: string
}) {
  if (options.title) {
    setPageTitle(options.title)
  }
  
  if (options.description) {
    setPageDescription(options.description)
  }
  
  if (options.keywords) {
    setPageKeywords(options.keywords)
  }
  
  if (options.canonical) {
    setCanonicalUrl(options.canonical)
  }
  
  if (options.ogImage) {
    updateMetaTag('og:image', options.ogImage)
  }
}
