import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// 代码高亮主题列表 (文件名必须与 highlight.js CDN 上的一致)
export const codeThemes = [
  // 亮色主题
  { name: 'github', label: 'GitHub', type: 'light' },
  { name: 'atom-one-light', label: 'Atom One Light', type: 'light' },
  { name: 'vs', label: 'Visual Studio', type: 'light' },
  { name: 'xcode', label: 'Xcode', type: 'light' },
  { name: 'idea', label: 'IntelliJ IDEA', type: 'light' },
  { name: 'stackoverflow-light', label: 'StackOverflow', type: 'light' },
  { name: 'tokyo-night-light', label: 'Tokyo Night', type: 'light' },
  { name: 'rose-pine-dawn', label: 'Rose Pine Dawn', type: 'light' },
  { name: 'arduino-light', label: 'Arduino', type: 'light' },
  { name: 'a11y-light', label: 'A11y Light', type: 'light' },
  { name: 'color-brewer', label: 'Color Brewer', type: 'light' },
  { name: 'googlecode', label: 'Google Code', type: 'light' },
  { name: 'lightfair', label: 'Lightfair', type: 'light' },
  { name: 'panda-syntax-light', label: 'Panda Light', type: 'light' },
  { name: 'qtcreator-light', label: 'Qt Creator', type: 'light' },
  // 暗色主题
  { name: 'github-dark', label: 'GitHub Dark', type: 'dark' },
  { name: 'atom-one-dark', label: 'Atom One Dark', type: 'dark' },
  { name: 'vs2015', label: 'VS2015 Dark', type: 'dark' },
  { name: 'monokai', label: 'Monokai', type: 'dark' },
  { name: 'monokai-sublime', label: 'Monokai Sublime', type: 'dark' },
  { name: 'nord', label: 'Nord', type: 'dark' },
  { name: 'tokyo-night-dark', label: 'Tokyo Night', type: 'dark' },
  { name: 'night-owl', label: 'Night Owl', type: 'dark' },
  { name: 'obsidian', label: 'Obsidian', type: 'dark' },
  { name: 'stackoverflow-dark', label: 'StackOverflow', type: 'dark' },
  { name: 'androidstudio', label: 'Android Studio', type: 'dark' },
  { name: 'agate', label: 'Agate', type: 'dark' },
  { name: 'rainbow', label: 'Rainbow', type: 'dark' },
  { name: 'gradient-dark', label: 'Gradient Dark', type: 'dark' },
  { name: 'shades-of-purple', label: 'Shades of Purple', type: 'dark' },
  { name: 'panda-syntax-dark', label: 'Panda Dark', type: 'dark' },
  { name: 'rose-pine', label: 'Rose Pine', type: 'dark' },
  { name: 'rose-pine-moon', label: 'Rose Pine Moon', type: 'dark' },
  { name: 'a11y-dark', label: 'A11y Dark', type: 'dark' },
  { name: 'an-old-hope', label: 'An Old Hope', type: 'dark' },
  { name: 'arta', label: 'Arta', type: 'dark' },
  { name: 'codepen-embed', label: 'CodePen', type: 'dark' },
  { name: 'dark', label: 'Dark', type: 'dark' },
  { name: 'devibeans', label: 'Devibeans', type: 'dark' },
  { name: 'far', label: 'FAR', type: 'dark' },
  { name: 'felipec', label: 'Felipec', type: 'dark' },
  { name: 'hybrid', label: 'Hybrid', type: 'dark' },
  { name: 'ir-black', label: 'IR Black', type: 'dark' },
  { name: 'kimbie-dark', label: 'Kimbie Dark', type: 'dark' },
  { name: 'lioshi', label: 'Lioshi', type: 'dark' },
  { name: 'nnfx-dark', label: 'NNFX Dark', type: 'dark' },
  { name: 'paraiso-dark', label: 'Paraiso Dark', type: 'dark' },
  { name: 'qtcreator-dark', label: 'Qt Creator Dark', type: 'dark' },
  { name: 'srcery', label: 'Srcery', type: 'dark' },
  { name: 'sunburst', label: 'Sunburst', type: 'dark' },
  { name: 'tomorrow-night-blue', label: 'Tomorrow Night Blue', type: 'dark' },
  { name: 'tomorrow-night-bright', label: 'Tomorrow Night Bright', type: 'dark' },
  { name: 'xt256', label: 'XT256', type: 'dark' },
]

// Markdown 内容风格
export const contentStyles = [
  {
    name: 'juejin',
    label: '掘金风格',
    desc: '类似掘金的技术博客风格',
  },
  {
    name: 'github',
    label: 'GitHub 风格',
    desc: '经典的 GitHub Markdown 样式',
  },
  {
    name: 'minimal',
    label: '极简风格',
    desc: '极简主义，专注内容',
  },
  {
    name: 'elegant',
    label: '优雅风格',
    desc: '柔和的色调，优雅的排版',
  },
  {
    name: 'tech',
    label: '科技风格',
    desc: '现代感强，蓝紫色调',
  },
  {
    name: 'warm',
    label: '暖色风格',
    desc: '温暖的色调，护眼舒适',
  },
  {
    name: 'notion',
    label: 'Notion 风格',
    desc: '类似 Notion 的简洁风格',
  },
]

// 目录风格
export const tocStyles = [
  { name: 'default', label: '默认风格', desc: '简洁的目录样式' },
  { name: 'compact', label: '紧凑风格', desc: '更小的间距，适合长目录' },
  { name: 'card', label: '卡片风格', desc: '每个目录项都是独立卡片' },
  { name: 'line', label: '线条风格', desc: '带连接线的目录' },
  { name: 'dot', label: '圆点风格', desc: '带圆点标记的目录' },
]

export const useThemeStore = defineStore('theme', () => {
  // 从 localStorage 读取保存的设置
  const savedCodeTheme = localStorage.getItem('blog-code-theme') || 'atom-one-dark'
  const savedContentStyle = localStorage.getItem('blog-content-style') || 'juejin'
  const savedTocStyle = localStorage.getItem('blog-toc-style') || 'compact'

  const codeTheme = ref(savedCodeTheme)
  const contentStyle = ref(savedContentStyle)
  const tocStyle = ref(savedTocStyle)

  // 监听变化并保存到 localStorage
  watch(codeTheme, (val) => {
    localStorage.setItem('blog-code-theme', val)
  })

  watch(contentStyle, (val) => {
    localStorage.setItem('blog-content-style', val)
  })

  watch(tocStyle, (val) => {
    localStorage.setItem('blog-toc-style', val)
  })

  const setCodeTheme = (theme: string) => {
    codeTheme.value = theme
  }

  const setContentStyle = (style: string) => {
    contentStyle.value = style
  }

  const setTocStyle = (style: string) => {
    tocStyle.value = style
  }

  const resetToDefault = () => {
    codeTheme.value = 'atom-one-dark'
    contentStyle.value = 'juejin'
    tocStyle.value = 'compact'
  }

  return {
    codeTheme,
    contentStyle,
    tocStyle,
    setCodeTheme,
    setContentStyle,
    setTocStyle,
    resetToDefault,
  }
})
