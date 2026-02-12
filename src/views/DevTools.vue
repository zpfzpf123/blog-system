<script setup lang="ts">
import { computed, markRaw, onBeforeUnmount, onMounted, shallowRef, watch } from 'vue'
import type { Component } from 'vue'

import AspectRatioCalculator from '@/components/tools/AspectRatioCalculator.vue'
import Base64Tool from '@/components/tools/Base64Tool.vue'
import BoxShadowGenerator from '@/components/tools/BoxShadowGenerator.vue'
import CharacterCounter from '@/components/tools/CharacterCounter.vue'
import ChineseConverter from '@/components/tools/ChineseConverter.vue'
import ColorConverter from '@/components/tools/ColorConverter.vue'
import CronParser from '@/components/tools/CronParser.vue'
import CssFormatter from '@/components/tools/CssFormatter.vue'
import CssGradientGenerator from '@/components/tools/CssGradientGenerator.vue'
import CssToTailwind from '@/components/tools/CssToTailwind.vue'
import CurlConverter from '@/components/tools/CurlConverter.vue'
import DiffTool from '@/components/tools/DiffTool.vue'
import FlexboxPlayground from '@/components/tools/FlexboxPlayground.vue'
import HashGenerator from '@/components/tools/HashGenerator.vue'
import HtmlEntityConverter from '@/components/tools/HtmlEntityConverter.vue'
import HtmlFormatter from '@/components/tools/HtmlFormatter.vue'
import HttpStatusCodes from '@/components/tools/HttpStatusCodes.vue'
import ImageCompressor from '@/components/tools/ImageCompressor.vue'
import JsFormatter from '@/components/tools/JsFormatter.vue'
import JsonFormatter from '@/components/tools/JsonFormatter.vue'
import JsonPathFinder from '@/components/tools/JsonPathFinder.vue'
import JsonToTs from '@/components/tools/JsonToTs.vue'
import JwtDecoder from '@/components/tools/JwtDecoder.vue'
import LoremIpsum from '@/components/tools/LoremIpsum.vue'
import MarkdownPreview from '@/components/tools/MarkdownPreview.vue'
import MetaTagGenerator from '@/components/tools/MetaTagGenerator.vue'
import MockDataGenerator from '@/components/tools/MockDataGenerator.vue'
import NumberBaseConverter from '@/components/tools/NumberBaseConverter.vue'
import PasswordGenerator from '@/components/tools/PasswordGenerator.vue'
import PlaceholderImage from '@/components/tools/PlaceholderImage.vue'
import QrCodeTool from '@/components/tools/QrCodeTool.vue'
import RegexTester from '@/components/tools/RegexTester.vue'
import RmbConverter from '@/components/tools/RmbConverter.vue'
import SqlFormatter from '@/components/tools/SqlFormatter.vue'
import StringCaseConverter from '@/components/tools/StringCaseConverter.vue'
import SvgOptimizer from '@/components/tools/SvgOptimizer.vue'
import TextDedupe from '@/components/tools/TextDedupe.vue'
import TimestampTool from '@/components/tools/TimestampTool.vue'
import UnitConverter from '@/components/tools/UnitConverter.vue'
import UrlEncoder from '@/components/tools/UrlEncoder.vue'
import UuidGenerator from '@/components/tools/UuidGenerator.vue'

type CategoryId = 'all' | 'format' | 'encode' | 'generate' | 'css' | 'text' | 'image' | 'dev'
type ToolCategory = Exclude<CategoryId, 'all'>

interface CategoryItem {
  id: CategoryId
  name: string
  icon: string
  hint: string
}

interface ToolItem {
  id: string
  name: string
  description: string
  icon: string
  category: ToolCategory
  component: Component
}

const searchQuery = shallowRef('')
const activeCategory = shallowRef<CategoryId>('all')
const activeTool = shallowRef<ToolItem | null>(null)

const categories: CategoryItem[] = [
  { id: 'all', name: '全部', icon: '📚', hint: '一次查看全部开发工具' },
  { id: 'format', name: '格式化', icon: '🧼', hint: '代码与结构化文本整理' },
  { id: 'encode', name: '编码转换', icon: '🔁', hint: '编码、进制与单位转换' },
  { id: 'generate', name: '生成器', icon: '✨', hint: '随机、哈希与二维码生成' },
  { id: 'css', name: 'CSS 工具', icon: '🎨', hint: '常见样式调优与可视化助手' },
  { id: 'text', name: '文本处理', icon: '📝', hint: '文本分析与内容处理' },
  { id: 'image', name: '图片工具', icon: '🖼️', hint: '图片压缩和图形优化' },
  { id: 'dev', name: '开发辅助', icon: '🧰', hint: '接口、协议与工程辅助工具' },
]

const tools: ToolItem[] = [
  { id: 'json', name: 'JSON 格式化', description: 'JSON 格式化、压缩与校验', icon: '🧷', category: 'format', component: markRaw(JsonFormatter) },
  { id: 'css', name: 'CSS 格式化', description: 'CSS 代码美化与压缩', icon: '🎨', category: 'format', component: markRaw(CssFormatter) },
  { id: 'html', name: 'HTML 格式化', description: 'HTML 代码排版与压缩', icon: '🧱', category: 'format', component: markRaw(HtmlFormatter) },
  { id: 'js', name: 'JavaScript 格式化', description: 'JavaScript 代码美化', icon: '📜', category: 'format', component: markRaw(JsFormatter) },
  { id: 'sql', name: 'SQL 格式化', description: 'SQL 语句格式化与校验', icon: '🗃️', category: 'format', component: markRaw(SqlFormatter) },

  { id: 'base64', name: 'Base64 编解码', description: '文本与图片 Base64 相互转换', icon: '🔤', category: 'encode', component: markRaw(Base64Tool) },
  { id: 'url', name: 'URL 编解码', description: 'URL 编码与解码', icon: '🔗', category: 'encode', component: markRaw(UrlEncoder) },
  { id: 'color', name: '颜色转换', description: 'HEX、RGB、HSL 互转', icon: '🌈', category: 'encode', component: markRaw(ColorConverter) },
  { id: 'timestamp', name: '时间戳转换', description: '时间戳与日期互转', icon: '🕒', category: 'encode', component: markRaw(TimestampTool) },
  { id: 'number', name: '进制转换', description: '二、八、十、十六进制转换', icon: '🔢', category: 'encode', component: markRaw(NumberBaseConverter) },
  { id: 'rmb', name: '人民币大写', description: '金额数字转中文大写', icon: '💴', category: 'encode', component: markRaw(RmbConverter) },
  { id: 'unit', name: '单位换算', description: '长度、重量、温度等单位转换', icon: '📏', category: 'encode', component: markRaw(UnitConverter) },
  { id: 'entity', name: 'HTML 实体转换', description: 'HTML 实体编解码', icon: '🔣', category: 'encode', component: markRaw(HtmlEntityConverter) },

  { id: 'uuid', name: 'UUID 生成器', description: '生成 UUID / GUID', icon: '🆔', category: 'generate', component: markRaw(UuidGenerator) },
  { id: 'hash', name: 'Hash 生成器', description: 'MD5、SHA1、SHA256 哈希生成', icon: '🔐', category: 'generate', component: markRaw(HashGenerator) },
  { id: 'password', name: '密码生成器', description: '生成高强度随机密码', icon: '🔑', category: 'generate', component: markRaw(PasswordGenerator) },
  { id: 'qrcode', name: '二维码工具', description: '二维码生成与解析', icon: '📱', category: 'generate', component: markRaw(QrCodeTool) },
  { id: 'lorem', name: 'Lorem 文本', description: '快速生成占位文本', icon: '🧾', category: 'generate', component: markRaw(LoremIpsum) },

  { id: 'gradient', name: '渐变生成器', description: '生成 CSS 渐变代码', icon: '🌅', category: 'css', component: markRaw(CssGradientGenerator) },
  { id: 'shadow', name: '阴影生成器', description: '可视化生成 Box Shadow', icon: '🌫️', category: 'css', component: markRaw(BoxShadowGenerator) },
  { id: 'flexbox', name: 'Flexbox 演练场', description: 'Flex 布局可视化练习', icon: '📐', category: 'css', component: markRaw(FlexboxPlayground) },
  { id: 'aspect', name: '宽高比计算', description: '快速计算等比例尺寸', icon: '🖼️', category: 'css', component: markRaw(AspectRatioCalculator) },
  { id: 'css2tw', name: 'CSS 转 Tailwind', description: 'CSS 规则转 Tailwind 类名', icon: '🌀', category: 'css', component: markRaw(CssToTailwind) },

  { id: 'regex', name: '正则测试', description: '正则表达式测试与匹配', icon: '🧪', category: 'text', component: markRaw(RegexTester) },
  { id: 'diff', name: '文本对比', description: '两段文本差异比较', icon: '🆚', category: 'text', component: markRaw(DiffTool) },
  { id: 'counter', name: '字符统计', description: '字数、字符、行数统计', icon: '📊', category: 'text', component: markRaw(CharacterCounter) },
  { id: 'case', name: '大小写转换', description: '驼峰、下划线、大小写互转', icon: '🔠', category: 'text', component: markRaw(StringCaseConverter) },
  { id: 'markdown', name: 'Markdown 预览', description: 'Markdown 实时预览', icon: '📰', category: 'text', component: markRaw(MarkdownPreview) },
  { id: 'dedupe', name: '文本去重', description: '去除重复行并保留顺序', icon: '🧹', category: 'text', component: markRaw(TextDedupe) },
  { id: 'chinese', name: '简繁转换', description: '简体与繁体中文互转', icon: '🀄', category: 'text', component: markRaw(ChineseConverter) },

  { id: 'compress', name: '图片压缩', description: '在线压缩图片体积', icon: '🗜️', category: 'image', component: markRaw(ImageCompressor) },
  { id: 'svg', name: 'SVG 优化', description: 'SVG 代码压缩优化', icon: '✂️', category: 'image', component: markRaw(SvgOptimizer) },
  { id: 'placeholder', name: '占位图生成', description: '生成占位图片 URL', icon: '🧩', category: 'image', component: markRaw(PlaceholderImage) },

  { id: 'jwt', name: 'JWT 解析', description: '解码并查看 JWT 内容', icon: '🎟️', category: 'dev', component: markRaw(JwtDecoder) },
  { id: 'cron', name: 'Cron 解析', description: '解释 Cron 表达式含义', icon: '⏱️', category: 'dev', component: markRaw(CronParser) },
  { id: 'curl', name: 'cURL 转换', description: 'cURL 与代码片段互转', icon: '🛰️', category: 'dev', component: markRaw(CurlConverter) },
  { id: 'mock', name: 'Mock 数据生成', description: '生成测试用假数据', icon: '🎲', category: 'dev', component: markRaw(MockDataGenerator) },
  { id: 'json2ts', name: 'JSON 转 TS 接口', description: '从 JSON 自动生成 TypeScript 类型', icon: '📘', category: 'dev', component: markRaw(JsonToTs) },
  { id: 'jsonpath', name: 'JSONPath 查找', description: '查找 JSON 中所有路径', icon: '🧭', category: 'dev', component: markRaw(JsonPathFinder) },
  { id: 'http', name: 'HTTP 状态码速查', description: '常见 HTTP 状态码手册', icon: '📮', category: 'dev', component: markRaw(HttpStatusCodes) },
  { id: 'meta', name: 'Meta 标签生成', description: 'SEO Meta 标签快速生成', icon: '🏷️', category: 'dev', component: markRaw(MetaTagGenerator) },
]

const categoryCounts = computed<Record<CategoryId, number>>(() => {
  const countMap: Record<CategoryId, number> = {
    all: tools.length,
    format: 0,
    encode: 0,
    generate: 0,
    css: 0,
    text: 0,
    image: 0,
    dev: 0,
  }

  for (const tool of tools) {
    countMap[tool.category] += 1
  }

  return countMap
})

const activeCategoryName = computed(() => {
  return categories.find((item) => item.id === activeCategory.value)?.name ?? '全部'
})

const activeCategoryHint = computed(() => {
  return categories.find((item) => item.id === activeCategory.value)?.hint ?? ''
})

const normalizedSearchQuery = computed(() => searchQuery.value.trim().toLowerCase())

const filteredTools = computed<ToolItem[]>(() => {
  return tools.filter((tool) => {
    const matchCategory = activeCategory.value === 'all' || tool.category === activeCategory.value

    if (!matchCategory) {
      return false
    }

    if (!normalizedSearchQuery.value) {
      return true
    }

    const searchable = `${tool.id} ${tool.name} ${tool.description}`.toLowerCase()
    return searchable.includes(normalizedSearchQuery.value)
  })
})

const resultHint = computed(() => {
  const total = filteredTools.value.length

  if (!normalizedSearchQuery.value) {
    return `当前共 ${total} 个可用工具，点击卡片即可打开。`
  }

  return `关键词 “${searchQuery.value.trim()}” 匹配到 ${total} 个结果。`
})

const setActiveCategory = (categoryId: CategoryId): void => {
  activeCategory.value = categoryId
}

const getCategoryName = (categoryId: ToolCategory): string => {
  return categories.find((category) => category.id === categoryId)?.name ?? ''
}

const openTool = (tool: ToolItem): void => {
  activeTool.value = tool
}

const closeTool = (): void => {
  activeTool.value = null
}

const handleEscClose = (event: KeyboardEvent): void => {
  if (event.key === 'Escape' && activeTool.value) {
    closeTool()
  }
}

watch(activeTool, (tool) => {
  if (typeof document === 'undefined') {
    return
  }

  document.body.style.overflow = tool ? 'hidden' : ''
})

onMounted(() => {
  window.addEventListener('keydown', handleEscClose)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleEscClose)

  if (typeof document !== 'undefined') {
    document.body.style.overflow = ''
  }
})
</script>

<template>
  <div class="dev-tools-page">
    <section class="hero-section">
      <div class="hero-shell">
        <div class="hero-glow hero-glow-left" aria-hidden="true"></div>
        <div class="hero-glow hero-glow-right" aria-hidden="true"></div>

        <div class="hero-content">
          <p class="hero-eyebrow">Developer Utility Suite</p>
          <h1 class="hero-title">开发者工具箱</h1>
          <p class="hero-subtitle">
            从格式化到编码转换，把高频开发小工具集中在一个轻量、清晰、可视化的工作台。
          </p>

          <div class="hero-metrics">
            <article class="metric-card">
              <span class="metric-label">工具总数</span>
              <strong class="metric-value">{{ tools.length }}</strong>
            </article>
            <article class="metric-card">
              <span class="metric-label">当前结果</span>
              <strong class="metric-value">{{ filteredTools.length }}</strong>
            </article>
            <article class="metric-card">
              <span class="metric-label">分类数量</span>
              <strong class="metric-value">{{ categories.length - 1 }}</strong>
            </article>
          </div>

          <label for="tool-search" class="search-box">
            <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.8"
                d="M21 21l-4.35-4.35m1.85-5.15a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
            <input
              id="tool-search"
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="搜索工具：JSON、颜色转换、JWT、正则..."
            />
            <kbd class="search-tip">ESC</kbd>
          </label>
        </div>
      </div>
    </section>

    <section class="toolbar-section">
      <div class="toolbar-shell">
        <div class="toolbar-summary">
          <h2 class="toolbar-title">{{ activeCategoryName }}</h2>
          <p class="toolbar-subtitle">{{ activeCategoryHint }}</p>
        </div>

        <div class="category-tabs" role="tablist" aria-label="工具分类">
          <button
            v-for="category in categories"
            :key="category.id"
            type="button"
            class="category-tab"
            :class="{ 'is-active': activeCategory === category.id }"
            @click="setActiveCategory(category.id)"
          >
            <span class="tab-main">
              <span class="tab-icon">{{ category.icon }}</span>
              <span class="tab-text">{{ category.name }}</span>
            </span>
            <span class="tab-count">{{ categoryCounts[category.id] }}</span>
          </button>
        </div>
      </div>
    </section>

    <section class="tools-section">
      <div class="tools-shell">
        <header class="tools-header">
          <h3 class="tools-title">{{ activeCategoryName }}工具列表</h3>
          <p class="tools-description">{{ resultHint }}</p>
        </header>

        <transition-group name="tool-grid" tag="div" class="tools-grid">
          <button
            v-for="(tool, index) in filteredTools"
            :key="tool.id"
            type="button"
            class="tool-card"
            :class="`tool-card--${tool.category}`"
            @click="openTool(tool)"
          >
            <span class="tool-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="tool-topline"></span>

            <div class="tool-head">
              <span class="tool-icon">{{ tool.icon }}</span>
              <span class="tool-category">{{ getCategoryName(tool.category) }}</span>
            </div>

            <h4 class="tool-name">{{ tool.name }}</h4>
            <p class="tool-description">{{ tool.description }}</p>

            <span class="tool-action">
              打开工具
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.9" d="M5 12h14m-6-6l6 6-6 6" />
              </svg>
            </span>
          </button>
        </transition-group>

        <div v-if="filteredTools.length === 0" class="empty-state">
          <div class="empty-icon" aria-hidden="true">🔍</div>
          <h4 class="empty-title">没有找到匹配工具</h4>
          <p class="empty-text">换一个关键词试试，或切换到“全部”分类浏览。</p>
        </div>
      </div>
    </section>

    <transition name="modal-fade">
      <div v-if="activeTool" class="modal-overlay" @click.self="closeTool">
        <transition name="modal-zoom" appear>
          <section v-if="activeTool" :key="activeTool.id" class="modal-panel">
            <header class="modal-header">
              <div class="modal-title-wrap">
                <span class="modal-icon">{{ activeTool.icon }}</span>
                <div>
                  <h4 class="modal-title">{{ activeTool.name }}</h4>
                  <p class="modal-subtitle">{{ activeTool.description }}</p>
                </div>
              </div>

              <button type="button" class="modal-close" aria-label="关闭工具弹窗" @click="closeTool">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.9" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </header>

            <div class="modal-body">
              <component :is="activeTool.component" />
            </div>
          </section>
        </transition>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.dev-tools-page {
  --page-bg: linear-gradient(160deg, #f4f8ff 0%, #fffaf2 48%, #f3fbff 100%);
  --surface: #ffffff;
  --surface-soft: rgba(255, 255, 255, 0.82);
  --ink-strong: #1b2a45;
  --ink-main: #324869;
  --ink-soft: #637998;
  --line: #d8e3f6;
  --line-strong: #c2d3ef;
  --primary: #2d6df6;
  --primary-soft: #e8f0ff;
  --accent: #ff7a5b;
  --mint: #20b3a0;
  --focus: #396ff8;
  --shadow-soft: 0 16px 38px rgba(28, 64, 135, 0.14);
  --shadow-lift: 0 22px 48px rgba(27, 60, 120, 0.2);

  min-height: 100vh;
  background: var(--page-bg);
  color: var(--ink-main);
  font-family: 'Outfit', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  padding-bottom: 56px;
  position: relative;
}

.dev-tools-page::before {
  content: '';
  position: fixed;
  inset: -10vh -16vw 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 8% 12%, rgba(77, 132, 255, 0.3) 0%, rgba(77, 132, 255, 0) 48%),
    radial-gradient(circle at 90% 6%, rgba(255, 143, 92, 0.24) 0%, rgba(255, 143, 92, 0) 45%),
    radial-gradient(circle at 52% 90%, rgba(27, 192, 167, 0.18) 0%, rgba(27, 192, 167, 0) 55%);
  z-index: 0;
}

.dev-tools-page > * {
  position: relative;
  z-index: 1;
}

.hero-section {
  padding: clamp(40px, 7vw, 84px) 20px 18px;
}

.hero-shell {
  position: relative;
  margin: 0 auto;
  border-radius: 36px;
  border: 1px solid #d7e4fb;
  background:
    linear-gradient(140deg, rgba(255, 255, 255, 0.92) 0%, rgba(246, 251, 255, 0.98) 46%, rgba(255, 250, 241, 0.94) 100%);
  box-shadow: var(--shadow-soft);
  padding: clamp(28px, 4vw, 52px);
  overflow: hidden;
}

.hero-shell::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(90deg, rgba(75, 118, 209, 0.08) 1px, transparent 1px),
    linear-gradient(rgba(75, 118, 209, 0.08) 1px, transparent 1px);
  background-size: 54px 54px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.7), transparent 90%);
}

.hero-glow {
  position: absolute;
  width: min(32vw, 380px);
  aspect-ratio: 1;
  border-radius: 999px;
  filter: blur(6px);
}

.hero-glow-left {
  left: -120px;
  top: -120px;
  background: radial-gradient(circle, rgba(69, 129, 255, 0.35) 0%, rgba(69, 129, 255, 0) 72%);
}

.hero-glow-right {
  right: -90px;
  bottom: -160px;
  background: radial-gradient(circle, rgba(255, 141, 88, 0.28) 0%, rgba(255, 141, 88, 0) 72%);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-eyebrow {
  margin: 0;
  color: #4b5f82;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 600;
}

.hero-title {
  margin: 12px 0 0;
  font-size: clamp(2rem, 5vw, 3.7rem);
  line-height: 1.05;
  letter-spacing: -0.02em;
  color: var(--ink-strong);
}

.hero-subtitle {
  max-width: 760px;
  margin: 18px 0 0;
  color: var(--ink-soft);
  line-height: 1.75;
  font-size: clamp(0.98rem, 2.4vw, 1.14rem);
}

.hero-metrics {
  margin-top: 26px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid #dbe6f8;
  border-radius: 18px;
  padding: 14px 16px;
  backdrop-filter: blur(8px);
}

.metric-label {
  display: block;
  color: #6a7e9d;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.metric-value {
  display: block;
  margin-top: 8px;
  color: #1f3461;
  font-size: clamp(1.35rem, 3vw, 1.9rem);
  line-height: 1;
}

.search-box {
  margin-top: 22px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-radius: 20px;
  border: 1px solid #cddcf4;
  background: rgba(255, 255, 255, 0.93);
  box-shadow: 0 16px 38px rgba(41, 79, 149, 0.12);
  padding: 8px 10px 8px 14px;
}

.search-icon {
  width: 20px;
  height: 20px;
  color: #6e82a7;
  flex-shrink: 0;
}

.search-input {
  width: 100%;
  border: 0;
  background: transparent;
  font-size: 15px;
  color: #244067;
  min-height: 44px;
  font-family: inherit;
}

.search-input::placeholder {
  color: #93a6c2;
}

.search-input:focus {
  outline: none;
}

.search-tip {
  padding: 4px 10px;
  border-radius: 10px;
  background: #edf3ff;
  border: 1px solid #d2ddf5;
  color: #5d77a7;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Menlo, Consolas, monospace;
}

.toolbar-section {
  position: sticky;
  top: 0;
  z-index: 18;
  padding: 0 20px;
}

.toolbar-shell {
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px 20px;
  border-radius: 20px;
  border: 1px solid rgba(198, 214, 240, 0.9);
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(12px) saturate(140%);
  padding: 14px 16px;
}

.toolbar-title {
  margin: 0;
  color: #1d3258;
  font-size: 1.1rem;
}

.toolbar-subtitle {
  margin: 4px 0 0;
  color: #6d82a3;
  font-size: 0.9rem;
}

.category-tabs {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.category-tabs::-webkit-scrollbar {
  height: 6px;
}

.category-tabs::-webkit-scrollbar-thumb {
  background: #c8d6ee;
  border-radius: 20px;
}

.category-tab {
  flex: 0 0 auto;
  border: 1px solid #d4e0f4;
  background: #f8fbff;
  border-radius: 14px;
  min-height: 42px;
  padding: 8px 12px;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  cursor: pointer;
  transition: 0.28s ease;
  font-family: inherit;
}

.category-tab:hover {
  border-color: #b9cced;
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(57, 94, 161, 0.12);
}

.category-tab.is-active {
  background: linear-gradient(135deg, #2f6df6 0%, #5f8ef6 55%, #ff845d 120%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 12px 26px rgba(53, 93, 173, 0.26);
}

.tab-main {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tab-icon {
  font-size: 1rem;
}

.tab-text {
  font-size: 0.88rem;
  font-weight: 600;
}

.tab-count {
  min-width: 22px;
  height: 22px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.78rem;
  font-weight: 700;
  color: #4f6488;
  background: #e9f0ff;
}

.category-tab.is-active .tab-count {
  background: rgba(255, 255, 255, 0.24);
  color: #ffffff;
}

.tools-section {
  padding: 26px 20px 0;
}

.tools-shell {
  margin: 0 auto;
}

.tools-header {
  margin-bottom: 18px;
}

.tools-title {
  margin: 0;
  font-size: clamp(1.2rem, 2.6vw, 1.48rem);
  color: #22395f;
}

.tools-description {
  margin: 8px 0 0;
  color: #657c9f;
  line-height: 1.65;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 14px;
}

.tool-card {
  --card-accent: #2f6df6;
  --card-accent-soft: #ebf2ff;
  --card-accent-border: #ccdcff;

  position: relative;
  text-align: left;
  border: 1px solid var(--card-accent-border);
  border-radius: 20px;
  background: linear-gradient(165deg, #ffffff 0%, var(--card-accent-soft) 220%);
  min-height: 186px;
  padding: 18px;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 10px 26px rgba(40, 76, 143, 0.12);
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
}

.tool-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lift);
  border-color: var(--card-accent);
}

.tool-topline {
  position: absolute;
  inset: 0 0 auto;
  height: 4px;
  background: linear-gradient(90deg, transparent 0%, var(--card-accent) 30%, transparent 100%);
  opacity: 0.85;
}

.tool-index {
  position: absolute;
  right: 14px;
  top: 12px;
  color: rgba(67, 88, 125, 0.52);
  font-size: 11px;
  letter-spacing: 0.08em;
  font-weight: 700;
}

.tool-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.tool-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.45rem;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(204, 220, 246, 0.9);
}

.tool-category {
  color: #58719a;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  padding: 5px 9px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(203, 217, 242, 0.95);
}

.tool-name {
  margin: 14px 0 8px;
  color: #20375f;
  font-size: 1.03rem;
  line-height: 1.35;
}

.tool-description {
  margin: 0;
  color: #5f769a;
  line-height: 1.62;
  font-size: 0.91rem;
}

.tool-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  color: var(--card-accent);
  font-size: 0.9rem;
  font-weight: 700;
}

.tool-action svg {
  width: 14px;
  height: 14px;
  transition: transform 0.28s ease;
}

.tool-card:hover .tool-action svg {
  transform: translateX(4px);
}

.tool-card--format {
  --card-accent: #366ff5;
  --card-accent-soft: #edf3ff;
  --card-accent-border: #cddcff;
}

.tool-card--encode {
  --card-accent: #7f56f7;
  --card-accent-soft: #f4efff;
  --card-accent-border: #ddd1ff;
}

.tool-card--generate {
  --card-accent: #f06754;
  --card-accent-soft: #fff0eb;
  --card-accent-border: #ffd8cd;
}

.tool-card--css {
  --card-accent: #1f9eb7;
  --card-accent-soft: #eaf8fb;
  --card-accent-border: #c8e9f0;
}

.tool-card--text {
  --card-accent: #2f8652;
  --card-accent-soft: #ecf8f0;
  --card-accent-border: #cde8d8;
}

.tool-card--image {
  --card-accent: #d08a19;
  --card-accent-soft: #fff7e9;
  --card-accent-border: #f4dfba;
}

.tool-card--dev {
  --card-accent: #5b6f93;
  --card-accent-soft: #f2f5fb;
  --card-accent-border: #d4ddef;
}

.empty-state {
  margin-top: 22px;
  border-radius: 22px;
  border: 1px dashed #c3d4f1;
  background: rgba(255, 255, 255, 0.78);
  padding: 48px 20px;
  text-align: center;
}

.empty-icon {
  width: 62px;
  height: 62px;
  margin: 0 auto;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 1.6rem;
  background: #edf4ff;
  border: 1px solid #d8e5fb;
}

.empty-title {
  margin: 16px 0 6px;
  color: #274069;
  font-size: 1.06rem;
}

.empty-text {
  margin: 0;
  color: #6e84a7;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 60;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(232, 241, 255, 0.7);
  backdrop-filter: blur(10px);
}

.modal-panel {
  width: min(1120px, 100%);
  max-height: 92vh;
  border-radius: 24px;
  border: 1px solid #cad9f3;
  background: var(--surface);
  box-shadow: 0 28px 68px rgba(26, 53, 104, 0.32);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 18px;
  border-bottom: 1px solid #dce6f7;
  background: linear-gradient(120deg, #edf4ff 0%, #fff5ef 100%);
}

.modal-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.modal-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 1.3rem;
  background: #ffffff;
  border: 1px solid #d6e2f8;
  flex-shrink: 0;
}

.modal-title {
  margin: 0;
  color: #1f3760;
  font-size: 1.1rem;
}

.modal-subtitle {
  margin: 4px 0 0;
  color: #6780a5;
  font-size: 0.88rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: min(72vw, 700px);
}

.modal-close {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid #d0ddf4;
  background: #ffffff;
  color: #49658f;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.modal-close:hover {
  transform: rotate(90deg);
  color: #244f93;
  border-color: #9fb8e7;
  box-shadow: 0 10px 22px rgba(49, 86, 155, 0.2);
}

.modal-close svg {
  width: 18px;
  height: 18px;
}

.modal-body {
  padding: 18px;
  overflow: auto;
  background: linear-gradient(180deg, #ffffff 0%, #fcfdff 100%);
}

.tool-grid-enter-active,
.tool-grid-leave-active,
.tool-grid-move {
  transition: all 0.36s ease;
}

.tool-grid-enter-from,
.tool-grid-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.25s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-zoom-enter-active,
.modal-zoom-leave-active {
  transition: all 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}

.modal-zoom-enter-from,
.modal-zoom-leave-to {
  opacity: 0;
  transform: scale(0.96) translateY(12px);
}

.category-tab:focus-visible,
.tool-card:focus-visible,
.modal-close:focus-visible,
.search-input:focus-visible {
  outline: 2px solid var(--focus);
  outline-offset: 2px;
}

@media (max-width: 960px) {
  .hero-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar-shell {
    border-radius: 16px;
  }

  .tools-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  }
}

@media (max-width: 760px) {
  .hero-section,
  .toolbar-section,
  .tools-section {
    padding-left: 14px;
    padding-right: 14px;
  }

  .hero-shell {
    border-radius: 26px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .search-tip {
    display: none;
  }

  .toolbar-shell {
    padding: 12px;
  }

  .category-tab {
    min-height: 40px;
    padding: 7px 10px;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .tool-card {
    min-height: 172px;
  }

  .modal-overlay {
    padding: 10px;
  }

  .modal-panel {
    max-height: 95vh;
    border-radius: 18px;
  }

  .modal-body {
    padding: 14px;
  }

  .modal-subtitle {
    white-space: normal;
    max-width: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
