<template>
  <div class="dev-tools-container">
    <div class="tools-header">
      <h1>🛠️ 开发者工具箱</h1>
      <p class="subtitle">从设计到部署，一站式开发工具集合</p>
      
      <!-- 搜索框 -->
      <div class="search-box">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索工具... (如: JSON、颜色、编码)"
          class="search-input"
        />
        <span class="search-icon">🔍</span>
      </div>
      
      <!-- 分类标签 -->
      <div class="category-tabs">
        <button 
          v-for="cat in categories" 
          :key="cat.id"
          :class="['tab-btn', { active: activeCategory === cat.id }]"
          @click="activeCategory = cat.id"
        >
          {{ cat.icon }} {{ cat.name }}
        </button>
      </div>
    </div>
    
    <!-- 工具网格 -->
    <div class="tools-grid">
      <div 
        v-for="tool in filteredTools" 
        :key="tool.id"
        class="tool-card"
        @click="openTool(tool)"
      >
        <div class="tool-icon">{{ tool.icon }}</div>
        <div class="tool-info">
          <h3>{{ tool.name }}</h3>
          <p>{{ tool.description }}</p>
        </div>
        <span class="tool-category-tag">{{ getCategoryName(tool.category) }}</span>
      </div>
    </div>
    
    <!-- 工具弹窗 -->
    <div v-if="activeTool" class="tool-modal-overlay" @click.self="closeTool">
      <div class="tool-modal">
        <div class="modal-header">
          <h2>{{ activeTool.icon }} {{ activeTool.name }}</h2>
          <button class="close-btn" @click="closeTool">✕</button>
        </div>
        <div class="modal-body">
          <component :is="activeTool.component" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, markRaw } from 'vue'

// 导入所有工具组件
import JsonFormatter from '@/components/tools/JsonFormatter.vue'
import ImageCompressor from '@/components/tools/ImageCompressor.vue'
import RegexTester from '@/components/tools/RegexTester.vue'
import RmbConverter from '@/components/tools/RmbConverter.vue'
import Base64Tool from '@/components/tools/Base64Tool.vue'
import UrlEncoder from '@/components/tools/UrlEncoder.vue'
import ColorConverter from '@/components/tools/ColorConverter.vue'
import TimestampTool from '@/components/tools/TimestampTool.vue'
import HashGenerator from '@/components/tools/HashGenerator.vue'
import UuidGenerator from '@/components/tools/UuidGenerator.vue'
import QrCodeTool from '@/components/tools/QrCodeTool.vue'
import MarkdownPreview from '@/components/tools/MarkdownPreview.vue'
import CssFormatter from '@/components/tools/CssFormatter.vue'
import HtmlFormatter from '@/components/tools/HtmlFormatter.vue'
import JsFormatter from '@/components/tools/JsFormatter.vue'
import DiffTool from '@/components/tools/DiffTool.vue'
import LoremIpsum from '@/components/tools/LoremIpsum.vue'
import PasswordGenerator from '@/components/tools/PasswordGenerator.vue'
import NumberBaseConverter from '@/components/tools/NumberBaseConverter.vue'
import UnitConverter from '@/components/tools/UnitConverter.vue'
import CronParser from '@/components/tools/CronParser.vue'
import JwtDecoder from '@/components/tools/JwtDecoder.vue'
import SqlFormatter from '@/components/tools/SqlFormatter.vue'
import CurlConverter from '@/components/tools/CurlConverter.vue'
import SvgOptimizer from '@/components/tools/SvgOptimizer.vue'
import CssGradientGenerator from '@/components/tools/CssGradientGenerator.vue'
import BoxShadowGenerator from '@/components/tools/BoxShadowGenerator.vue'
import FlexboxPlayground from '@/components/tools/FlexboxPlayground.vue'
import AspectRatioCalculator from '@/components/tools/AspectRatioCalculator.vue'
import CharacterCounter from '@/components/tools/CharacterCounter.vue'
import StringCaseConverter from '@/components/tools/StringCaseConverter.vue'
import MockDataGenerator from '@/components/tools/MockDataGenerator.vue'
import JsonToTs from '@/components/tools/JsonToTs.vue'
import CssToTailwind from '@/components/tools/CssToTailwind.vue'
import HttpStatusCodes from '@/components/tools/HttpStatusCodes.vue'
import HtmlEntityConverter from '@/components/tools/HtmlEntityConverter.vue'
import MetaTagGenerator from '@/components/tools/MetaTagGenerator.vue'
import TextDedupe from '@/components/tools/TextDedupe.vue'
import ChineseConverter from '@/components/tools/ChineseConverter.vue'
import JsonPathFinder from '@/components/tools/JsonPathFinder.vue'
import PlaceholderImage from '@/components/tools/PlaceholderImage.vue'

const searchQuery = ref('')
const activeCategory = ref('all')
const activeTool = ref<any>(null)

// 工具分类
const categories = [
  { id: 'all', name: '全部', icon: '📦' },
  { id: 'format', name: '格式化', icon: '📝' },
  { id: 'encode', name: '编码转换', icon: '🔄' },
  { id: 'generate', name: '生成器', icon: '⚡' },
  { id: 'css', name: 'CSS工具', icon: '🎨' },
  { id: 'text', name: '文本处理', icon: '📄' },
  { id: 'image', name: '图片工具', icon: '🖼️' },
  { id: 'dev', name: '开发辅助', icon: '💻' },
]

// 工具列表
const tools = [
  // 格式化工具
  { id: 'json', name: 'JSON格式化', description: 'JSON格式化、压缩、校验', icon: '📋', category: 'format', component: markRaw(JsonFormatter) },
  { id: 'css', name: 'CSS格式化', description: 'CSS代码美化与压缩', icon: '🎨', category: 'format', component: markRaw(CssFormatter) },
  { id: 'html', name: 'HTML格式化', description: 'HTML代码美化与压缩', icon: '🌐', category: 'format', component: markRaw(HtmlFormatter) },
  { id: 'js', name: 'JS格式化', description: 'JavaScript代码美化', icon: '📜', category: 'format', component: markRaw(JsFormatter) },
  { id: 'sql', name: 'SQL格式化', description: 'SQL语句美化与格式化', icon: '🗃️', category: 'format', component: markRaw(SqlFormatter) },
  
  // 编码转换
  { id: 'base64', name: 'Base64编解码', description: '文本/图片Base64转换', icon: '🔐', category: 'encode', component: markRaw(Base64Tool) },
  { id: 'url', name: 'URL编解码', description: 'URL编码与解码', icon: '🔗', category: 'encode', component: markRaw(UrlEncoder) },
  { id: 'color', name: '颜色转换', description: 'HEX/RGB/HSL颜色转换', icon: '🌈', category: 'encode', component: markRaw(ColorConverter) },
  { id: 'timestamp', name: '时间戳转换', description: '时间戳与日期互转', icon: '⏰', category: 'encode', component: markRaw(TimestampTool) },
  { id: 'number', name: '进制转换', description: '二/八/十/十六进制转换', icon: '🔢', category: 'encode', component: markRaw(NumberBaseConverter) },
  { id: 'rmb', name: '人民币大小写', description: '金额数字转中文大写', icon: '💰', category: 'encode', component: markRaw(RmbConverter) },
  { id: 'unit', name: '单位换算', description: '长度/重量/温度等单位换算', icon: '📏', category: 'encode', component: markRaw(UnitConverter) },
  
  // 生成器
  { id: 'uuid', name: 'UUID生成器', description: '生成UUID/GUID', icon: '🆔', category: 'generate', component: markRaw(UuidGenerator) },
  { id: 'hash', name: 'Hash生成器', description: 'MD5/SHA1/SHA256哈希', icon: '🔒', category: 'generate', component: markRaw(HashGenerator) },
  { id: 'password', name: '密码生成器', description: '生成安全随机密码', icon: '🔑', category: 'generate', component: markRaw(PasswordGenerator) },
  { id: 'qrcode', name: '二维码工具', description: '生成与解析二维码', icon: '📱', category: 'generate', component: markRaw(QrCodeTool) },
  { id: 'lorem', name: 'Lorem文本', description: '生成占位文本', icon: '📝', category: 'generate', component: markRaw(LoremIpsum) },
  
  // CSS工具
  { id: 'gradient', name: '渐变生成器', description: 'CSS渐变代码生成', icon: '🎨', category: 'css', component: markRaw(CssGradientGenerator) },
  { id: 'shadow', name: '阴影生成器', description: 'Box Shadow生成器', icon: '🌑', category: 'css', component: markRaw(BoxShadowGenerator) },
  { id: 'flexbox', name: 'Flexbox演练场', description: 'Flexbox布局可视化', icon: '📐', category: 'css', component: markRaw(FlexboxPlayground) },
  { id: 'aspect', name: '宽高比计算', description: '计算等比例尺寸', icon: '🖼️', category: 'css', component: markRaw(AspectRatioCalculator) },
  
  // 文本处理
  { id: 'regex', name: '正则测试', description: '正则表达式测试与匹配', icon: '🔍', category: 'text', component: markRaw(RegexTester) },
  { id: 'diff', name: '文本对比', description: '对比两段文本差异', icon: '📊', category: 'text', component: markRaw(DiffTool) },
  { id: 'counter', name: '字符统计', description: '统计字数/字符/行数', icon: '🔢', category: 'text', component: markRaw(CharacterCounter) },
  { id: 'case', name: '大小写转换', description: '驼峰/下划线/大小写转换', icon: '🔠', category: 'text', component: markRaw(StringCaseConverter) },
  { id: 'markdown', name: 'Markdown预览', description: 'Markdown实时预览', icon: '📖', category: 'text', component: markRaw(MarkdownPreview) },
  
  // 图片工具
  { id: 'compress', name: '图片压缩', description: '在线压缩图片大小', icon: '🗜️', category: 'image', component: markRaw(ImageCompressor) },
  { id: 'svg', name: 'SVG优化', description: 'SVG代码优化压缩', icon: '✨', category: 'image', component: markRaw(SvgOptimizer) },
  
  // 开发辅助
  { id: 'jwt', name: 'JWT解析', description: '解析JWT Token内容', icon: '🎫', category: 'dev', component: markRaw(JwtDecoder) },
  { id: 'cron', name: 'Cron解析', description: 'Cron表达式解析', icon: '⏱️', category: 'dev', component: markRaw(CronParser) },
  { id: 'curl', name: 'cURL转换', description: 'cURL转代码/代码转cURL', icon: '🌐', category: 'dev', component: markRaw(CurlConverter) },
  { id: 'mock', name: 'Mock数据生成', description: '生成测试用假数据', icon: '🎲', category: 'dev', component: markRaw(MockDataGenerator) },
  { id: 'json2ts', name: 'JSON转TS接口', description: 'JSON自动生成TypeScript类型', icon: '📘', category: 'dev', component: markRaw(JsonToTs) },
  { id: 'jsonpath', name: 'JSON路径查找', description: '查找JSON中所有路径', icon: '🔎', category: 'dev', component: markRaw(JsonPathFinder) },
  { id: 'http', name: 'HTTP状态码', description: 'HTTP状态码速查手册', icon: '📡', category: 'dev', component: markRaw(HttpStatusCodes) },
  { id: 'css2tw', name: 'CSS转Tailwind', description: 'CSS转Tailwind类名', icon: '🌊', category: 'css', component: markRaw(CssToTailwind) },
  { id: 'meta', name: 'Meta标签生成', description: '生成SEO Meta标签', icon: '🏷️', category: 'dev', component: markRaw(MetaTagGenerator) },
  { id: 'entity', name: 'HTML实体转换', description: 'HTML实体编解码', icon: '🔣', category: 'encode', component: markRaw(HtmlEntityConverter) },
  { id: 'dedupe', name: '文本去重', description: '去除重复行', icon: '🧹', category: 'text', component: markRaw(TextDedupe) },
  { id: 'chinese', name: '简繁转换', description: '简体繁体中文互转', icon: '🀄', category: 'text', component: markRaw(ChineseConverter) },
  { id: 'placeholder', name: '占位图生成', description: '生成占位图片URL', icon: '🖼️', category: 'image', component: markRaw(PlaceholderImage) },
]

const getCategoryName = (categoryId: string) => {
  return categories.find(c => c.id === categoryId)?.name || ''
}

const filteredTools = computed(() => {
  return tools.filter(tool => {
    const matchCategory = activeCategory.value === 'all' || tool.category === activeCategory.value
    const matchSearch = !searchQuery.value || 
      tool.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      tool.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    return matchCategory && matchSearch
  })
})

const openTool = (tool: any) => {
  activeTool.value = tool
}

const closeTool = () => {
  activeTool.value = null
}
</script>

<style scoped>
.dev-tools-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: var(--spacing-6);
  min-height: 100vh;
}

/* 头部区域 */
.tools-header {
  text-align: center;
  margin-bottom: var(--spacing-8);
  animation: fadeInUp 0.6s var(--ease-out);
}

.tools-header h1 {
  font-size: var(--text-4xl);
  font-weight: var(--font-extrabold);
  margin-bottom: var(--spacing-3);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

.subtitle {
  color: var(--text-secondary);
  font-size: var(--text-lg);
  margin-bottom: var(--spacing-6);
  font-weight: var(--font-medium);
}

/* 搜索框 */
.search-box {
  position: relative;
  max-width: 560px;
  margin: 0 auto var(--spacing-6);
}

.search-input {
  width: 100%;
  padding: var(--spacing-4) var(--spacing-4) var(--spacing-4) var(--spacing-12);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-full);
  font-size: var(--text-base);
  background: var(--bg-card);
  color: var(--text-main);
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.search-input:hover {
  border-color: var(--gray-300);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-focus), var(--shadow-md);
}

.search-input::placeholder {
  color: var(--text-placeholder);
}

.search-icon {
  position: absolute;
  left: var(--spacing-5);
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.3rem;
  pointer-events: none;
}

/* 分类标签 */
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--spacing-2);
  margin-bottom: var(--spacing-4);
}

.tab-btn {
  padding: var(--spacing-2) var(--spacing-4);
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-card);
  cursor: pointer;
  transition: all var(--transition-normal);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-regular);
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}

.tab-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--primary-lighter);
  transform: translateY(-2px);
}

.tab-btn.active {
  background: var(--gradient-primary);
  color: var(--text-inverse);
  border-color: transparent;
  box-shadow: var(--shadow-primary);
  transform: translateY(-2px);
}

/* 工具网格 */
.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-5);
}

.tool-card {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  padding: var(--spacing-5);
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--border-light);
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-4);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.tool-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--gradient-primary);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform var(--transition-normal);
}

.tool-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-card-hover);
  border-color: rgba(99, 102, 241, 0.2);
}

.tool-card:hover::before {
  transform: scaleX(1);
}

.tool-icon {
  font-size: 2.8rem;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  transition: transform var(--transition-normal);
}

.tool-card:hover .tool-icon {
  transform: scale(1.1) rotate(-5deg);
}

.tool-info {
  flex: 1;
  min-width: 0;
}

.tool-info h3 {
  margin: 0 0 var(--spacing-2);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-main);
  transition: color var(--transition-fast);
}

.tool-card:hover .tool-info h3 {
  color: var(--primary-color);
}

.tool-info p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
}

.tool-category-tag {
  position: absolute;
  top: var(--spacing-3);
  right: var(--spacing-3);
  font-size: var(--text-xs);
  padding: var(--spacing-1) var(--spacing-3);
  background: var(--bg-muted);
  border-radius: var(--radius-full);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
  transition: all var(--transition-fast);
}

.tool-card:hover .tool-category-tag {
  background: var(--primary-lighter);
  color: var(--primary-color);
}

/* 弹窗样式 */
.tool-modal-overlay {
  position: fixed;
  inset: 0;
  background: var(--bg-overlay);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal);
  padding: var(--spacing-4);
  animation: fadeIn 0.2s var(--ease-out);
}

.tool-modal {
  background: var(--bg-card);
  border-radius: var(--radius-2xl);
  width: 100%;
  max-width: 960px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-2xl);
  animation: scaleIn 0.3s var(--ease-spring);
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-4) var(--spacing-6);
  border-bottom: 1px solid var(--border-light);
  background: var(--gradient-primary);
  color: var(--text-inverse);
}

.modal-header h2 {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.close-btn {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: var(--text-inverse);
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: rotate(90deg);
}

.modal-body {
  padding: var(--spacing-6);
  overflow-y: auto;
  flex: 1;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .dev-tools-container {
    padding: var(--spacing-4);
  }
  
  .tools-header h1 {
    font-size: var(--text-2xl);
  }
  
  .subtitle {
    font-size: var(--text-base);
  }
  
  .tools-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-4);
  }
  
  .tool-card {
    padding: var(--spacing-4);
  }
  
  .tool-icon {
    font-size: 2.2rem;
  }
  
  .tool-modal {
    max-height: 95vh;
    border-radius: var(--radius-xl);
  }
  
  .modal-body {
    padding: var(--spacing-4);
  }
}
</style>
