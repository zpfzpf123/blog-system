<template>
  <el-drawer
    v-model="visible"
    title="阅读主题设置"
    direction="rtl"
    size="420px"
    :destroy-on-close="false"
  >
    <div class="theme-settings">
      <!-- 代码高亮主题 -->
      <div class="setting-section">
        <div class="section-header">
          <el-icon><Monitor /></el-icon>
          <span>代码高亮主题</span>
        </div>
        <div class="theme-tabs">
          <el-radio-group v-model="codeThemeType" size="small">
            <el-radio-button value="light">亮色</el-radio-button>
            <el-radio-button value="dark">暗色</el-radio-button>
            <el-radio-button value="all">全部</el-radio-button>
          </el-radio-group>
        </div>
        <div class="theme-grid">
          <div
            v-for="theme in filteredCodeThemes"
            :key="theme.name"
            :class="['theme-item', { active: themeStore.codeTheme === theme.name }]"
            @click="themeStore.setCodeTheme(theme.name)"
          >
            <div class="theme-preview code-preview" :class="theme.name">
              <code>const x = 1;</code>
            </div>
            <span class="theme-label">{{ theme.label }}</span>
          </div>
        </div>
      </div>

      <!-- Markdown 内容风格 -->
      <div class="setting-section">
        <div class="section-header">
          <el-icon><Document /></el-icon>
          <span>内容风格</span>
        </div>
        <div class="style-list">
          <div
            v-for="style in contentStyles"
            :key="style.name"
            :class="['style-item', { active: themeStore.contentStyle === style.name }]"
            @click="themeStore.setContentStyle(style.name)"
          >
            <div class="style-info">
              <div class="style-name">{{ style.label }}</div>
              <div class="style-desc">{{ style.desc }}</div>
            </div>
            <el-icon v-if="themeStore.contentStyle === style.name" class="check-icon"><Check /></el-icon>
          </div>
        </div>
      </div>

      <!-- 目录风格 -->
      <div class="setting-section">
        <div class="section-header">
          <el-icon><List /></el-icon>
          <span>目录风格</span>
        </div>
        <div class="style-list">
          <div
            v-for="style in tocStyles"
            :key="style.name"
            :class="['style-item', { active: themeStore.tocStyle === style.name }]"
            @click="themeStore.setTocStyle(style.name)"
          >
            <div class="style-info">
              <div class="style-name">{{ style.label }}</div>
              <div class="style-desc">{{ style.desc }}</div>
            </div>
            <el-icon v-if="themeStore.tocStyle === style.name" class="check-icon"><Check /></el-icon>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="setting-actions">
        <el-button @click="themeStore.resetToDefault">恢复默认</el-button>
        <el-button type="primary" @click="visible = false">完成</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElDrawer, ElRadioGroup, ElRadioButton, ElButton, ElIcon } from 'element-plus'
import { Monitor, Document, List, Check } from '@element-plus/icons-vue'
import { useThemeStore, codeThemes, contentStyles, tocStyles } from '@/stores/theme'

const visible = ref(false)
const themeStore = useThemeStore()
const codeThemeType = ref<'light' | 'dark' | 'all'>('all')

const filteredCodeThemes = computed(() => {
  if (codeThemeType.value === 'all') return codeThemes
  return codeThemes.filter(t => t.type === codeThemeType.value)
})

const open = () => {
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped>
.theme-settings {
  padding: 0 4px;
}

.setting-section {
  margin-bottom: 28px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-default);
}

.section-header .el-icon {
  color: var(--primary);
}

.theme-tabs {
  margin-bottom: 12px;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  max-height: 280px;
  overflow-y: auto;
  padding-right: 4px;
}

.theme-grid::-webkit-scrollbar {
  width: 4px;
}

.theme-grid::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 2px;
}

.theme-grid::-webkit-scrollbar-track {
  background: transparent;
}

.theme-item {
  cursor: pointer;
  border-radius: 8px;
  border: 2px solid var(--border-default);
  overflow: hidden;
  transition: all 0.2s ease;
}

.theme-item:hover {
  border-color: var(--primary);
  transform: translateY(-2px);
}

.theme-item.active {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.theme-preview {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 11px;
}

.theme-preview code {
  padding: 4px 8px;
  border-radius: 4px;
}

/* 代码主题预览色 */
.code-preview.github { background: #f6f8fa; color: #24292e; }
.code-preview.github-dark { background: #0d1117; color: #c9d1d9; }
.code-preview.atom-one-light { background: #fafafa; color: #383a42; }
.code-preview.atom-one-dark { background: #282c34; color: #abb2bf; }
.code-preview.vs { background: #fff; color: #000; }
.code-preview.vs2015 { background: #1e1e1e; color: #dcdcdc; }
.code-preview.monokai { background: #272822; color: #f8f8f2; }
.code-preview.monokai-sublime { background: #23241f; color: #f8f8f2; }
.code-preview.nord { background: #2e3440; color: #d8dee9; }
.code-preview.tokyo-night-dark { background: #1a1b26; color: #a9b1d6; }
.code-preview.tokyo-night-light { background: #d5d6db; color: #343b58; }
.code-preview.night-owl { background: #011627; color: #d6deeb; }
.code-preview.dracula { background: #282a36; color: #f8f8f2; }
.code-preview.obsidian { background: #282b2e; color: #e0e2e4; }
.code-preview.stackoverflow-light { background: #f6f6f6; color: #2f3337; }
.code-preview.stackoverflow-dark { background: #1c1b1b; color: #f6f6f6; }
.code-preview.xcode { background: #fff; color: #000; }
.code-preview.idea { background: #fff; color: #000; }
.code-preview.androidstudio { background: #282b2e; color: #a9b7c6; }
.code-preview.agate { background: #333; color: #fff; }
.code-preview.rainbow { background: #474949; color: #d1d9e1; }
.code-preview.gradient-dark { background: linear-gradient(135deg, #1a1a2e, #16213e); color: #eee; }
.code-preview.shades-of-purple { background: #2d2b55; color: #e3dfff; }
.code-preview.panda-syntax-dark { background: #292a2b; color: #e6e6e6; }
.code-preview.panda-syntax-light { background: #e6e6e6; color: #292a2b; }
.code-preview.rose-pine { background: #191724; color: #e0def4; }
.code-preview.rose-pine-moon { background: #232136; color: #e0def4; }
.code-preview.rose-pine-dawn { background: #faf4ed; color: #575279; }
.code-preview.arduino-light { background: #fff; color: #434f54; }
.code-preview.a11y-light { background: #fefefe; color: #545454; }
.code-preview.a11y-dark { background: #2b2b2b; color: #f8f8f2; }
.code-preview.color-brewer { background: #fff; color: #000; }
.code-preview.googlecode { background: #fff; color: #000; }
.code-preview.lightfair { background: #f8f8f8; color: #444; }
.code-preview.qtcreator-light { background: #fff; color: #000; }
.code-preview.qtcreator-dark { background: #000; color: #aaa; }
.code-preview.an-old-hope { background: #1c1d21; color: #c0c5ce; }
.code-preview.arta { background: #222; color: #aaa; }
.code-preview.codepen-embed { background: #222; color: #fff; }
.code-preview.dark { background: #444; color: #ddd; }
.code-preview.devibeans { background: #1a1a1a; color: #abb2bf; }
.code-preview.far { background: #000080; color: #0ff; }
.code-preview.felipec { background: #272822; color: #f8f8f2; }
.code-preview.hybrid { background: #1d1f21; color: #c5c8c6; }
.code-preview.ir-black { background: #000; color: #f6f3e8; }
.code-preview.kimbie-dark { background: #221a0f; color: #d3af86; }
.code-preview.lioshi { background: #303030; color: #c5c8c6; }
.code-preview.nnfx-dark { background: #333; color: #fff; }
.code-preview.paraiso-dark { background: #2f1e2e; color: #a39e9b; }
.code-preview.srcery { background: #1c1b19; color: #fce8c3; }
.code-preview.sunburst { background: #000; color: #f8f8f8; }
.code-preview.tomorrow-night-blue { background: #002451; color: #fff; }
.code-preview.tomorrow-night-bright { background: #000; color: #eaeaea; }
.code-preview.xt256 { background: #000; color: #eaeaea; }

.theme-label {
  display: block;
  text-align: center;
  padding: 6px 4px;
  font-size: 11px;
  color: var(--text-secondary);
  background: var(--bg-muted);
}

.theme-item.active .theme-label {
  color: var(--primary);
  font-weight: 600;
}

.style-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.style-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 10px;
  border: 2px solid var(--border-default);
  cursor: pointer;
  transition: all 0.2s ease;
}

.style-item:hover {
  border-color: var(--primary);
  background: var(--bg-muted);
}

.style-item.active {
  border-color: var(--primary);
  background: var(--primary-bg);
}

.style-info {
  flex: 1;
}

.style-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.style-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.check-icon {
  color: var(--primary);
  font-size: 18px;
}

.setting-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid var(--border-default);
}
</style>
