<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElIcon } from 'element-plus'
import { RefreshLeft } from '@element-plus/icons-vue'
import { useBlogStats } from '@/composables/useBlogStats'
import type { DistributionPoint, RankedPoint, SummaryMetrics } from '@/utils/blogStats'

defineOptions({
  name: 'StatisticsDashboardPage',
})

type MetricFormat = 'int' | 'decimal1' | 'decimal2'
type HoverGroup =
  | 'trend'
  | 'donut'
  | 'tagHeat'
  | 'tagDensity'
  | 'contentDepth'
  | 'recency'
  | 'hour'
  | 'weekday'
type HoverState = Partial<Record<HoverGroup, string>>

interface SummaryCard {
  key: string
  label: string
  value: number
  unit: string
  helper: string
  accent: string
  format: MetricFormat
}

interface TrendPointView {
  label: string
  count: number
  x: number
  y: number
}

interface DonutSegment extends RankedPoint {
  color: string
  dashArray: string
  dashOffset: number
}

interface RatioBar extends DistributionPoint {
  ratio: number
  color: string
}

interface FreshnessBar extends DistributionPoint {
  ratio: number
  color: string
}

interface WeekdayCell {
  label: string
  count: number
  ratio: number
}

interface HeatBar extends RankedPoint {
  ratio: number
  color: string
}

interface AnimatedHeroMetrics {
  monthlyGrowth: number
  contentScore: number
  freshPostsRate: number
  freshPostsCount: number
}

const { loading, errorMessage, latestUpdatedAt, stats, totalInteractions, refresh } = useBlogStats()
const formatter = new Intl.NumberFormat('zh-CN')

/**
 * 统计页只重塑视觉层级与交互反馈，所有展示都复用既有统计口径，
 * 避免在改版时引入新的业务含义或改变原有内容结构。
 */

const animatedSummary = ref<SummaryMetrics>({
  totalPosts: 0,
  totalCategories: 0,
  totalTags: 0,
  avgTagsPerPost: 0,
  categorizedRate: 0,
  recent30dPosts: 0,
  avgReadingMinutes: 0,
})

// Hero KPIs use an extra tween so status changes feel more impactful.
const animatedHeroMetrics = ref<AnimatedHeroMetrics>({
  monthlyGrowth: 0,
  contentScore: 0,
  freshPostsRate: 0,
  freshPostsCount: 0,
})

// 统一记录各类图表当前悬停项，让不同图表共用同一套高亮/弱化反馈。
const hoverState = ref<HoverState>({})

// 统计页改版沿用全站紫蓝主色，并补充青色、琥珀和绿色作为图表强调色。
const palette = ['#3468ff', '#5d8bff', '#f08a5d', '#10b981', '#0ea5e9', '#94a3b8']
const densityPalette = ['#dbeafe', '#93c5fd', '#5d8bff', '#f08a5d']
const depthPalette = ['#dcfce7', '#86efac', '#38bdf8', '#3468ff']
const recencyPalette = ['#10b981', '#38bdf8', '#f59e0b', '#94a3b8']

const TREND_WIDTH = 700
const TREND_HEIGHT = 260
const TREND_PADDING_X = 40
const TREND_PADDING_Y = 24
const DONUT_RADIUS = 72
const DONUT_CIRCUMFERENCE = 2 * Math.PI * DONUT_RADIUS

const setHoveredItem = (group: HoverGroup, key: string) => {
  hoverState.value = {
    ...hoverState.value,
    [group]: key,
  }
}

const clearHoveredItem = (group: HoverGroup) => {
  const nextState = { ...hoverState.value }
  delete nextState[group]
  hoverState.value = nextState
}

const isHoveredItem = (group: HoverGroup, key: string): boolean => hoverState.value[group] === key

const isDimmedItem = (group: HoverGroup, key: string): boolean => {
  const activeKey = hoverState.value[group]
  return Boolean(activeKey && activeKey !== key)
}

const formatInteger = (value: number): string => formatter.format(Math.max(0, Math.round(value)))

const formatValue = (value: number, format: MetricFormat): string => {
  if (format === 'int') {
    return formatInteger(value)
  }
  if (format === 'decimal1') {
    return value.toFixed(1)
  }
  return value.toFixed(2)
}

// 将统计工具层的英文原始标签转换成中文显示，避免页面出现中英混排。
const toDisplayCategoryName = (value: string): string => {
  const normalizedValue = value.trim()
  return normalizedValue === 'Uncategorized' ? '未分类' : normalizedValue
}

const toDisplayWeekday = (value: string): string => {
  const weekdayMap: Record<string, string> = {
    Mon: '周一',
    Tue: '周二',
    Wed: '周三',
    Thu: '周四',
    Fri: '周五',
    Sat: '周六',
    Sun: '周日',
  }

  return weekdayMap[value] ?? value
}

const formattedUpdatedAt = computed(() => {
  if (!latestUpdatedAt.value) {
    return '等待首次加载'
  }

  const parsedDate = new Date(latestUpdatedAt.value)
  if (Number.isNaN(parsedDate.getTime())) {
    return '时间不可用'
  }

  return parsedDate.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
})

const currentMonthCount = computed(() => {
  const trend = stats.value.monthlyTrend
  return trend.length > 0 ? trend[trend.length - 1].count : 0
})

const previousMonthCount = computed(() => {
  const trend = stats.value.monthlyTrend
  return trend.length > 1 ? trend[trend.length - 2].count : 0
})

const monthlyGrowth = computed(() => {
  const current = currentMonthCount.value
  const previous = previousMonthCount.value

  if (previous === 0) {
    return current > 0 ? 100 : 0
  }

  return Math.round(((current - previous) / previous) * 100)
})

const contentScore = computed(() => {
  const score =
    stats.value.summary.categorizedRate * 0.55 +
    stats.value.summary.avgTagsPerPost * 20 +
    stats.value.summary.recent30dPosts * 7 +
    stats.value.summary.avgReadingMinutes * 8

  return Math.min(100, Math.round(score))
})

const freshPostsCount = computed(() => {
  const seven = stats.value.recencyDistribution[0]?.count ?? 0
  const thirty = stats.value.recencyDistribution[1]?.count ?? 0
  return seven + thirty
})

const freshPostsRate = computed(() => {
  if (stats.value.summary.totalPosts === 0) {
    return 0
  }
  return Number(((freshPostsCount.value / stats.value.summary.totalPosts) * 100).toFixed(1))
})

const summaryCards = computed<SummaryCard[]>(() => {
  return [
    {
      key: 'total-posts',
      label: '文章总量',
      value: animatedSummary.value.totalPosts,
      unit: '篇',
      helper: '内容资产总盘子',
      accent: '#1d4ed8',
      format: 'int',
    },
    {
      key: 'new-30d',
      label: '近30天新增',
      value: animatedSummary.value.recent30dPosts,
      unit: '篇',
      helper: '衡量更新频率',
      accent: '#22c55e',
      format: 'int',
    },
    {
      key: 'coverage',
      label: '分类覆盖率',
      value: animatedSummary.value.categorizedRate,
      unit: '%',
      helper: '分类是否完整',
      accent: '#f97316',
      format: 'decimal1',
    },
    {
      key: 'touch',
      label: '标签触达',
      value: totalInteractions.value,
      unit: '次',
      helper: '标签被使用总次数',
      accent: '#06b6d4',
      format: 'int',
    },
    {
      key: 'avg-tags',
      label: '篇均标签',
      value: animatedSummary.value.avgTagsPerPost,
      unit: '个',
      helper: '主题颗粒度',
      accent: '#8b5cf6',
      format: 'decimal2',
    },
    {
      key: 'avg-reading',
      label: '篇均阅读',
      value: animatedSummary.value.avgReadingMinutes,
      unit: '分钟',
      helper: '按标题+摘要估算',
      accent: '#0ea5e9',
      format: 'decimal1',
    },
  ]
})

const trendMaxCount = computed(() => {
  return Math.max(...stats.value.monthlyTrend.map((item) => item.count), 1)
})

const trendPoints = computed<TrendPointView[]>(() => {
  const points = stats.value.monthlyTrend
  if (points.length === 0) {
    return []
  }

  const stepX =
    points.length > 1
      ? (TREND_WIDTH - TREND_PADDING_X * 2) / (points.length - 1)
      : TREND_WIDTH - TREND_PADDING_X * 2

  return points.map((item, index) => {
    const x = TREND_PADDING_X + stepX * index
    const y =
      TREND_HEIGHT -
      TREND_PADDING_Y -
      (item.count / trendMaxCount.value) * (TREND_HEIGHT - TREND_PADDING_Y * 2)

    return {
      ...item,
      x,
      y,
    }
  })
})

const trendPath = computed(() => trendPoints.value.map((item) => `${item.x},${item.y}`).join(' '))

const trendArea = computed(() => {
  if (trendPoints.value.length === 0) {
    return ''
  }

  const first = trendPoints.value[0]
  const last = trendPoints.value[trendPoints.value.length - 1]
  const baseY = TREND_HEIGHT - TREND_PADDING_Y
  const segments = trendPoints.value.map((point) => `L ${point.x} ${point.y}`).join(' ')

  return `M ${first.x} ${baseY} ${segments} L ${last.x} ${baseY} Z`
})

const trendGrid = computed(() => {
  return Array.from({ length: 5 }).map((_, index) => {
    const ratio = index / 4
    const y = TREND_PADDING_Y + ratio * (TREND_HEIGHT - TREND_PADDING_Y * 2)
    const value = Math.round((1 - ratio) * trendMaxCount.value)
    return { y, value }
  })
})

const activeTrendPoint = computed<TrendPointView | null>(() => {
  const key = hoverState.value.trend
  return key ? (trendPoints.value.find((item) => item.label === key) ?? null) : null
})

const trendTooltipStyle = computed<Record<string, string> | null>(() => {
  if (!activeTrendPoint.value) {
    return null
  }

  // SVG 图表使用百分比定位浮层，避免 tooltip 在边缘区域被裁切。
  const xPercent = (activeTrendPoint.value.x / TREND_WIDTH) * 100
  const yPercent = (activeTrendPoint.value.y / TREND_HEIGHT) * 100
  const clampedX = Math.max(10, Math.min(xPercent, 90))
  const clampedY = Math.max(12, Math.min(yPercent, 86))

  return {
    '--x': `${clampedX}%`,
    '--y': `${clampedY}%`,
  }
})

const mergedCategoryShare = computed<RankedPoint[]>(() => {
  const localizedSource = stats.value.categoryShare.map((item) => ({
    ...item,
    name: toDisplayCategoryName(item.name),
  }))

  if (localizedSource.length <= 5) {
    return localizedSource
  }

  const head = localizedSource.slice(0, 4)
  const tailCount = localizedSource.slice(4).reduce((sum, item) => sum + item.count, 0)
  const percentage =
    stats.value.summary.totalPosts > 0
      ? Number(((tailCount / stats.value.summary.totalPosts) * 100).toFixed(1))
      : 0

  return [...head, { name: '其他', count: tailCount, percentage }]
})

const donutSegments = computed<DonutSegment[]>(() => {
  if (mergedCategoryShare.value.length === 0) {
    return []
  }

  const total = mergedCategoryShare.value.reduce((sum, item) => sum + item.count, 0)
  let offset = 0

  return mergedCategoryShare.value.map((item, index) => {
    const ratio = total > 0 ? item.count / total : 0
    const dash = DONUT_CIRCUMFERENCE * ratio
    const segment = {
      ...item,
      color: palette[index % palette.length],
      dashArray: `${dash} ${DONUT_CIRCUMFERENCE - dash}`,
      dashOffset: -offset,
    }

    offset += dash
    return segment
  })
})

// 环图中心文案跟随悬停联动，保留原有信息结构的同时补足视觉反馈。
const activeDonutSegment = computed<DonutSegment | null>(() => {
  const key = hoverState.value.donut
  return key ? (donutSegments.value.find((item) => item.name === key) ?? null) : null
})

const donutCenterValue = computed(() => {
  return activeDonutSegment.value
    ? formatInteger(activeDonutSegment.value.count)
    : formatInteger(stats.value.summary.totalPosts)
})

const donutCenterLabel = computed(() => {
  return activeDonutSegment.value
    ? `${activeDonutSegment.value.name} · ${activeDonutSegment.value.percentage}%`
    : '篇文章'
})

const topTags = computed(() => stats.value.tagHeat.slice(0, 8))

const topTagBars = computed<HeatBar[]>(() => {
  const source = topTags.value
  const max = Math.max(...source.map((item) => item.count), 1)

  return source.map((item, index) => ({
    ...item,
    ratio: item.count / max,
    color: palette[index % palette.length],
  }))
})

const tagDensityBars = computed<RatioBar[]>(() => {
  const source = stats.value.tagDensityDistribution
  const max = Math.max(...source.map((item) => item.count), 1)

  return source.map((item, index) => ({
    ...item,
    ratio: item.count / max,
    color: densityPalette[index % densityPalette.length],
  }))
})

const contentDepthBars = computed<RatioBar[]>(() => {
  const source = stats.value.contentDepthDistribution
  const max = Math.max(...source.map((item) => item.count), 1)

  return source.map((item, index) => ({
    ...item,
    ratio: item.count / max,
    color: depthPalette[index % depthPalette.length],
  }))
})

const recencyBars = computed<FreshnessBar[]>(() => {
  const total = Math.max(stats.value.summary.totalPosts, 1)
  return stats.value.recencyDistribution.map((item, index) => ({
    ...item,
    ratio: item.count / total,
    color: recencyPalette[index % recencyPalette.length],
  }))
})

const hourBars = computed<RatioBar[]>(() => {
  const max = Math.max(...stats.value.hourDistribution.map((item) => item.count), 1)

  return stats.value.hourDistribution.map((item, index) => ({
    ...item,
    ratio: item.count / max,
    color: palette[index % palette.length],
  }))
})

const weekdayCells = computed<WeekdayCell[]>(() => {
  const max = Math.max(...stats.value.weekdayDistribution.map((item) => item.count), 1)

  return stats.value.weekdayDistribution.map((item) => ({
    label: toDisplayWeekday(item.label),
    count: item.count,
    ratio: item.count / max,
  }))
})

const activeTagHeatBar = computed<HeatBar | null>(() => {
  const key = hoverState.value.tagHeat
  return key ? (topTagBars.value.find((item) => item.name === key) ?? null) : null
})

const activeTagDensityBar = computed<RatioBar | null>(() => {
  const key = hoverState.value.tagDensity
  return key ? (tagDensityBars.value.find((item) => item.label === key) ?? null) : null
})

const activeContentDepthBar = computed<RatioBar | null>(() => {
  const key = hoverState.value.contentDepth
  return key ? (contentDepthBars.value.find((item) => item.label === key) ?? null) : null
})

const activeRecencyBar = computed<FreshnessBar | null>(() => {
  const key = hoverState.value.recency
  return key ? (recencyBars.value.find((item) => item.label === key) ?? null) : null
})

const activeHourBar = computed<RatioBar | null>(() => {
  const key = hoverState.value.hour
  return key ? (hourBars.value.find((item) => item.label === key) ?? null) : null
})

const activeWeekdayCell = computed<WeekdayCell | null>(() => {
  const key = hoverState.value.weekday
  return key ? (weekdayCells.value.find((item) => item.label === key) ?? null) : null
})

const hasNoData = computed(() => !loading.value && stats.value.summary.totalPosts === 0)

let summaryFrameId: number | undefined
let heroFrameId: number | undefined

// 数字卡片保留缓动动画，避免刷新后直接跳值造成页面显得生硬。
const animateSummary = (target: SummaryMetrics) => {
  if (summaryFrameId !== undefined) {
    cancelAnimationFrame(summaryFrameId)
  }

  const start = { ...animatedSummary.value }
  const begin = performance.now()
  const duration = 900

  const step = (time: number) => {
    const progress = Math.min((time - begin) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)

    animatedSummary.value = {
      totalPosts: Math.round(start.totalPosts + (target.totalPosts - start.totalPosts) * eased),
      totalCategories: Math.round(
        start.totalCategories + (target.totalCategories - start.totalCategories) * eased,
      ),
      totalTags: Math.round(start.totalTags + (target.totalTags - start.totalTags) * eased),
      avgTagsPerPost: Number(
        (start.avgTagsPerPost + (target.avgTagsPerPost - start.avgTagsPerPost) * eased).toFixed(2),
      ),
      categorizedRate: Number(
        (start.categorizedRate + (target.categorizedRate - start.categorizedRate) * eased).toFixed(
          1,
        ),
      ),
      recent30dPosts: Math.round(
        start.recent30dPosts + (target.recent30dPosts - start.recent30dPosts) * eased,
      ),
      avgReadingMinutes: Number(
        (
          start.avgReadingMinutes +
          (target.avgReadingMinutes - start.avgReadingMinutes) * eased
        ).toFixed(1),
      ),
    }

    if (progress < 1) {
      summaryFrameId = requestAnimationFrame(step)
    }
  }

  summaryFrameId = requestAnimationFrame(step)
}

// 顶部 KPI 保持缓动动画，让刷新后的数值变化更柔和。
const animateHeroMetrics = (target: AnimatedHeroMetrics) => {
  if (heroFrameId !== undefined) {
    cancelAnimationFrame(heroFrameId)
  }

  const start = { ...animatedHeroMetrics.value }
  const begin = performance.now()
  const duration = 1080

  const step = (time: number) => {
    const progress = Math.min((time - begin) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 4)

    animatedHeroMetrics.value = {
      monthlyGrowth: start.monthlyGrowth + (target.monthlyGrowth - start.monthlyGrowth) * eased,
      contentScore: start.contentScore + (target.contentScore - start.contentScore) * eased,
      freshPostsRate: Number(
        (start.freshPostsRate + (target.freshPostsRate - start.freshPostsRate) * eased).toFixed(1),
      ),
      freshPostsCount: Math.round(
        start.freshPostsCount + (target.freshPostsCount - start.freshPostsCount) * eased,
      ),
    }

    if (progress < 1) {
      heroFrameId = requestAnimationFrame(step)
    }
  }

  heroFrameId = requestAnimationFrame(step)
}

watch(
  () => stats.value.summary,
  (next) => {
    animateSummary(next)
  },
  { immediate: true, deep: true },
)

watch(
  () => ({
    monthlyGrowth: monthlyGrowth.value,
    contentScore: contentScore.value,
    freshPostsRate: freshPostsRate.value,
    freshPostsCount: freshPostsCount.value,
  }),
  (next) => {
    animateHeroMetrics(next)
  },
  { immediate: true, deep: true },
)

onMounted(async () => {
  await refresh()
})

onUnmounted(() => {
  if (summaryFrameId !== undefined) {
    cancelAnimationFrame(summaryFrameId)
  }
  if (heroFrameId !== undefined) {
    cancelAnimationFrame(heroFrameId)
  }
})
</script>

<template>
  <div class="stats-page">
    <div class="stats-aurora stats-aurora--violet"></div>
    <div class="stats-aurora stats-aurora--peach"></div>
    <div class="stats-grid-overlay"></div>

    <div class="stats-shell">
      <section class="hero-panel">
        <div class="hero-copy">
          <div class="hero-badges">
            <p class="hero-eyebrow">内容数据台</p>
            <span class="hero-mode">浅色主题</span>
          </div>

          <div class="hero-heading">
            <h1 class="hero-title">内容统计中心</h1>
            <p class="hero-description">
              聚焦内容结构与更新质量，从标签密度、内容深度、新鲜度和发布节奏快速洞察站点状态。
            </p>
          </div>

          <div class="hero-tags">
            <span class="hero-tag"
              >主导分类 · {{ mergedCategoryShare[0]?.name || '暂无数据' }}</span
            >
            <span class="hero-tag">最热标签 · {{ topTags[0]?.name || '暂无数据' }}</span>
            <span class="hero-tag">近 30 天 · {{ freshPostsCount }} 篇新内容</span>
          </div>
        </div>

        <div class="hero-command">
          <div class="command-card command-card--action">
            <div class="command-head">
              <div>
                <span class="command-label">数据同步</span>
                <strong class="command-title">实时刷新统计快照</strong>
              </div>
              <span class="command-badge">在线</span>
            </div>

            <button class="refresh-button" :disabled="loading" @click="refresh">
              <el-icon class="refresh-icon" :class="{ 'is-spinning': loading }">
                <RefreshLeft />
              </el-icon>
              <span>{{ loading ? '正在同步...' : '刷新数据' }}</span>
            </button>

            <p class="updated-at">最近同步：{{ formattedUpdatedAt }}</p>
          </div>

          <div class="hero-status-grid">
            <article class="status-tile">
              <span class="status-label">本月变化</span>
              <strong
                class="status-value"
                :class="{ 'is-negative': animatedHeroMetrics.monthlyGrowth < 0 }"
              >
                {{ animatedHeroMetrics.monthlyGrowth >= 0 ? '+' : ''
                }}{{ Math.round(animatedHeroMetrics.monthlyGrowth) }}%
              </strong>
            </article>
            <article class="status-tile">
              <span class="status-label">内容健康分</span>
              <strong class="status-value">{{
                Math.round(animatedHeroMetrics.contentScore)
              }}</strong>
            </article>
          </div>
        </div>
      </section>

      <section class="insight-strip">
        <article class="insight-chip">
          <span class="insight-index">01</span>
          <span class="insight-label">主导分类</span>
          <strong class="insight-value">{{ mergedCategoryShare[0]?.name || '暂无数据' }}</strong>
          <span class="insight-sub">{{ mergedCategoryShare[0]?.percentage ?? 0 }}% 占比</span>
        </article>
        <article class="insight-chip">
          <span class="insight-index">02</span>
          <span class="insight-label">最热标签</span>
          <strong class="insight-value">{{ topTags[0]?.name || '暂无数据' }}</strong>
          <span class="insight-sub">{{ topTags[0]?.count ?? 0 }} 次触达</span>
        </article>
        <article class="insight-chip">
          <span class="insight-index">03</span>
          <span class="insight-label">新鲜内容占比</span>
          <strong class="insight-value"
            >{{ animatedHeroMetrics.freshPostsRate.toFixed(1) }}%</strong
          >
          <span class="insight-sub"
            >近 30 天新增 {{ formatInteger(animatedHeroMetrics.freshPostsCount) }} 篇</span
          >
        </article>
      </section>

      <section class="metric-grid">
        <article
          v-for="(card, index) in summaryCards"
          :key="card.key"
          class="metric-card"
          :style="{ '--delay': `${index * 70}ms`, '--accent': card.accent }"
        >
          <div class="metric-topline">
            <p class="metric-label">{{ card.label }}</p>
            <span class="metric-accent-dot"></span>
          </div>
          <p class="metric-value-row">
            <span class="metric-value">{{ formatValue(card.value, card.format) }}</span>
            <span class="metric-unit">{{ card.unit }}</span>
          </p>
          <p class="metric-helper">{{ card.helper }}</p>
          <div class="metric-ribbon"></div>
          <div class="metric-glow"></div>
        </article>
      </section>

      <div v-if="errorMessage" class="error-banner">
        <p>{{ errorMessage }}</p>
        <button class="error-action" @click="refresh">重试加载</button>
      </div>

      <section v-if="loading" class="loading-board">
        <div class="loading-line" v-for="index in 8" :key="index"></div>
      </section>

      <section v-else-if="hasNoData" class="empty-board">
        <h2>暂无统计数据</h2>
        <p>当前项目还没有可用文章，请先在首页创建或导入内容。</p>
      </section>

      <section v-else class="chart-grid">
        <article class="chart-card chart-card--interactive trend-card">
          <header class="chart-header">
            <div>
              <p class="chart-kicker">发布走势</p>
              <h3>发布趋势（近 6 个月）</h3>
            </div>
            <span>{{ stats.monthlyTrend.reduce((sum, item) => sum + item.count, 0) }} 篇</span>
          </header>

          <div class="trend-panel">
            <svg
              class="trend-svg"
              :viewBox="`0 0 ${TREND_WIDTH} ${TREND_HEIGHT}`"
              preserveAspectRatio="none"
            >
              <defs>
                <linearGradient id="trendAreaGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="rgba(52, 104, 255, 0.32)" />
                  <stop offset="60%" stop-color="rgba(93, 139, 255, 0.12)" />
                  <stop offset="100%" stop-color="rgba(255, 255, 255, 0.02)" />
                </linearGradient>
              </defs>

              <line
                v-for="tick in trendGrid"
                :key="`line-${tick.y}`"
                :x1="TREND_PADDING_X"
                :x2="TREND_WIDTH - TREND_PADDING_X"
                :y1="tick.y"
                :y2="tick.y"
                class="trend-grid-line"
              />

              <text
                v-for="tick in trendGrid"
                :key="`label-${tick.y}`"
                :x="12"
                :y="tick.y + 4"
                class="trend-grid-text"
              >
                {{ tick.value }}
              </text>

              <line
                v-if="activeTrendPoint"
                class="trend-focus-line"
                :x1="activeTrendPoint.x"
                :x2="activeTrendPoint.x"
                :y1="TREND_PADDING_Y"
                :y2="TREND_HEIGHT - TREND_PADDING_Y"
              />

              <path class="trend-area" :d="trendArea"></path>
              <polyline
                class="trend-line"
                :class="{ 'is-focused': Boolean(activeTrendPoint) }"
                :points="trendPath"
              ></polyline>

              <g
                v-for="point in trendPoints"
                :key="point.label"
                class="trend-point-group"
                :class="{
                  'is-active': isHoveredItem('trend', point.label),
                  'is-dimmed': isDimmedItem('trend', point.label),
                }"
                @mouseenter="setHoveredItem('trend', point.label)"
                @mouseleave="clearHoveredItem('trend')"
              >
                <circle
                  class="trend-point-halo"
                  :cx="point.x"
                  :cy="point.y"
                  :r="isHoveredItem('trend', point.label) ? 18 : 11"
                />
                <circle
                  class="trend-point"
                  :cx="point.x"
                  :cy="point.y"
                  :r="isHoveredItem('trend', point.label) ? 7 : 5"
                />
                <text
                  class="trend-point-value"
                  :x="point.x"
                  :y="isHoveredItem('trend', point.label) ? point.y - 18 : point.y - 11"
                >
                  {{ point.count }}
                </text>
              </g>
            </svg>
            <div
              v-if="activeTrendPoint && trendTooltipStyle"
              class="trend-floating-tooltip"
              :style="trendTooltipStyle"
            >
              <span class="tooltip-month">{{ activeTrendPoint.label }}</span>
              <strong class="tooltip-value">{{ activeTrendPoint.count }} 篇</strong>
            </div>
          </div>

          <div class="trend-label-row">
            <span
              v-for="point in trendPoints"
              :key="point.label"
              class="trend-month"
              :class="{ 'is-active': isHoveredItem('trend', point.label) }"
              tabindex="0"
              @mouseenter="setHoveredItem('trend', point.label)"
              @mouseleave="clearHoveredItem('trend')"
              @focus="setHoveredItem('trend', point.label)"
              @blur="clearHoveredItem('trend')"
            >
              {{ point.label.slice(5) }}
            </span>
          </div>
        </article>

        <article class="chart-card chart-card--interactive donut-card">
          <header class="chart-header">
            <div>
              <p class="chart-kicker">分类结构</p>
              <h3>分类占比</h3>
            </div>
            <span>总文章 {{ formatInteger(stats.summary.totalPosts) }}</span>
          </header>

          <div class="donut-content">
            <div class="donut-shell">
              <svg viewBox="0 0 220 220">
                <circle class="donut-track" cx="110" cy="110" :r="DONUT_RADIUS" />
                <circle
                  v-for="segment in donutSegments"
                  :key="segment.name"
                  class="donut-segment"
                  :class="{
                    'is-active': isHoveredItem('donut', segment.name),
                    'is-dimmed': isDimmedItem('donut', segment.name),
                  }"
                  cx="110"
                  cy="110"
                  :r="DONUT_RADIUS"
                  :stroke="segment.color"
                  :stroke-dasharray="segment.dashArray"
                  :stroke-dashoffset="segment.dashOffset"
                  @mouseenter="setHoveredItem('donut', segment.name)"
                  @mouseleave="clearHoveredItem('donut')"
                />
              </svg>
              <div class="donut-center">
                <strong>{{ donutCenterValue }}</strong>
                <span>{{ donutCenterLabel }}</span>
              </div>
            </div>

            <ul class="legend-list">
              <li
                v-for="segment in donutSegments"
                :key="`legend-${segment.name}`"
                class="legend-item"
                :class="{
                  'is-active': isHoveredItem('donut', segment.name),
                  'is-dimmed': isDimmedItem('donut', segment.name),
                }"
                tabindex="0"
                @mouseenter="setHoveredItem('donut', segment.name)"
                @mouseleave="clearHoveredItem('donut')"
                @focus="setHoveredItem('donut', segment.name)"
                @blur="clearHoveredItem('donut')"
              >
                <span class="legend-dot" :style="{ backgroundColor: segment.color }"></span>
                <span class="legend-name">{{ segment.name }}</span>
                <span class="legend-value">{{ segment.count }} / {{ segment.percentage }}%</span>
              </li>
            </ul>
            <p class="donut-hover-tip" :class="{ 'is-active': Boolean(activeDonutSegment) }">
              {{
                activeDonutSegment
                  ? `${activeDonutSegment.name} 当前占比 ${activeDonutSegment.percentage}% (${activeDonutSegment.count} 篇)`
                  : '悬停圆环或图例查看分类占比'
              }}
            </p>
          </div>
        </article>

        <article class="chart-card chart-card--interactive small-card">
          <header class="chart-header">
            <div>
              <p class="chart-kicker">标签热度</p>
              <h3>标签热度前 8</h3>
            </div>
            <span>主题曝光</span>
          </header>
          <p class="chart-hover-tip" :class="{ 'is-active': Boolean(activeTagHeatBar) }">
            <span>当前焦点</span>
            <strong>{{
              activeTagHeatBar
                ? `${activeTagHeatBar.name} · ${activeTagHeatBar.count} 次触达`
                : '悬停条形查看标签热度'
            }}</strong>
          </p>

          <div class="bars-list">
            <article
              v-for="(tag, index) in topTagBars"
              :key="tag.name"
              class="bars-row bars-row--interactive"
              :class="{
                'is-active': isHoveredItem('tagHeat', tag.name),
                'is-dimmed': isDimmedItem('tagHeat', tag.name),
              }"
              :style="{ '--delay': `${index * 60}ms`, '--bar-color': tag.color }"
              tabindex="0"
              @mouseenter="setHoveredItem('tagHeat', tag.name)"
              @mouseleave="clearHoveredItem('tagHeat')"
              @focus="setHoveredItem('tagHeat', tag.name)"
              @blur="clearHoveredItem('tagHeat')"
            >
              <div class="bars-head">
                <span>{{ tag.name }}</span>
                <strong>{{ tag.count }}</strong>
              </div>
              <div class="bars-track">
                <span class="bars-fill" :style="{ width: `${tag.ratio * 100}%` }"></span>
              </div>
            </article>
          </div>
        </article>

        <article class="chart-card chart-card--interactive small-card">
          <header class="chart-header">
            <div>
              <p class="chart-kicker">标签密度</p>
              <h3>标签密度结构</h3>
            </div>
            <span>每篇标签数</span>
          </header>
          <p class="chart-hover-tip" :class="{ 'is-active': Boolean(activeTagDensityBar) }">
            <span>当前焦点</span>
            <strong>{{
              activeTagDensityBar
                ? `${activeTagDensityBar.label} · ${activeTagDensityBar.count} 篇`
                : '悬停条形查看标签密度'
            }}</strong>
          </p>

          <div class="bars-list">
            <article
              v-for="(bar, index) in tagDensityBars"
              :key="bar.label"
              class="bars-row bars-row--interactive"
              :class="{
                'is-active': isHoveredItem('tagDensity', bar.label),
                'is-dimmed': isDimmedItem('tagDensity', bar.label),
              }"
              :style="{ '--delay': `${index * 50}ms`, '--bar-color': bar.color }"
              tabindex="0"
              @mouseenter="setHoveredItem('tagDensity', bar.label)"
              @mouseleave="clearHoveredItem('tagDensity')"
              @focus="setHoveredItem('tagDensity', bar.label)"
              @blur="clearHoveredItem('tagDensity')"
            >
              <div class="bars-head">
                <span>{{ bar.label }}</span>
                <strong>{{ bar.count }}</strong>
              </div>
              <div class="bars-track">
                <span class="bars-fill" :style="{ width: `${bar.ratio * 100}%` }"></span>
              </div>
            </article>
          </div>
        </article>

        <article class="chart-card chart-card--interactive small-card">
          <header class="chart-header">
            <div>
              <p class="chart-kicker">内容深度</p>
              <h3>内容深度分布</h3>
            </div>
            <span>按长度估算</span>
          </header>
          <p class="chart-hover-tip" :class="{ 'is-active': Boolean(activeContentDepthBar) }">
            <span>当前焦点</span>
            <strong>{{
              activeContentDepthBar
                ? `${activeContentDepthBar.label} · ${activeContentDepthBar.count} 篇`
                : '悬停柱体查看内容深度'
            }}</strong>
          </p>

          <div class="depth-columns">
            <article
              v-for="bar in contentDepthBars"
              :key="bar.label"
              class="depth-column chart-column"
              :class="{
                'is-active': isHoveredItem('contentDepth', bar.label),
                'is-dimmed': isDimmedItem('contentDepth', bar.label),
              }"
              :style="{ '--column-color': bar.color }"
              tabindex="0"
              @mouseenter="setHoveredItem('contentDepth', bar.label)"
              @mouseleave="clearHoveredItem('contentDepth')"
              @focus="setHoveredItem('contentDepth', bar.label)"
              @blur="clearHoveredItem('contentDepth')"
            >
              <span class="depth-count">{{ bar.count }}</span>
              <div class="depth-track">
                <span
                  class="depth-fill"
                  :style="{ height: `${Math.max(bar.ratio * 100, 5)}%` }"
                ></span>
              </div>
              <span class="depth-label">{{ bar.label }}</span>
            </article>
          </div>
        </article>

        <article class="chart-card chart-card--interactive small-card">
          <header class="chart-header">
            <div>
              <p class="chart-kicker">内容新鲜度</p>
              <h3>内容新鲜度</h3>
            </div>
            <span>时间分层</span>
          </header>
          <p class="chart-hover-tip" :class="{ 'is-active': Boolean(activeRecencyBar) }">
            <span>当前焦点</span>
            <strong>{{
              activeRecencyBar
                ? `${activeRecencyBar.label} · ${(activeRecencyBar.ratio * 100).toFixed(1)}%`
                : '悬停条形查看内容新鲜度'
            }}</strong>
          </p>

          <div class="bars-list">
            <article
              v-for="(bar, index) in recencyBars"
              :key="bar.label"
              class="bars-row bars-row--interactive"
              :class="{
                'is-active': isHoveredItem('recency', bar.label),
                'is-dimmed': isDimmedItem('recency', bar.label),
              }"
              :style="{ '--delay': `${index * 50}ms`, '--bar-color': bar.color }"
              tabindex="0"
              @mouseenter="setHoveredItem('recency', bar.label)"
              @mouseleave="clearHoveredItem('recency')"
              @focus="setHoveredItem('recency', bar.label)"
              @blur="clearHoveredItem('recency')"
            >
              <div class="bars-head">
                <span>{{ bar.label }}</span>
                <strong>{{ (bar.ratio * 100).toFixed(1) }}%</strong>
              </div>
              <div class="bars-track">
                <span class="bars-fill" :style="{ width: `${bar.ratio * 100}%` }"></span>
              </div>
            </article>
          </div>
        </article>

        <article class="chart-card chart-card--interactive small-card">
          <header class="chart-header">
            <div>
              <p class="chart-kicker">发布时间</p>
              <h3>发布时间段分布</h3>
            </div>
            <span>UTC 时区</span>
          </header>
          <p class="chart-hover-tip" :class="{ 'is-active': Boolean(activeHourBar) }">
            <span>当前焦点</span>
            <strong>{{
              activeHourBar
                ? `${activeHourBar.label} · ${activeHourBar.count} 篇`
                : '悬停柱体查看发布时间分布'
            }}</strong>
          </p>

          <div class="hour-columns">
            <article
              v-for="bar in hourBars"
              :key="bar.label"
              class="hour-column chart-column"
              :class="{
                'is-active': isHoveredItem('hour', bar.label),
                'is-dimmed': isDimmedItem('hour', bar.label),
              }"
              :style="{ '--column-color': bar.color }"
              tabindex="0"
              @mouseenter="setHoveredItem('hour', bar.label)"
              @mouseleave="clearHoveredItem('hour')"
              @focus="setHoveredItem('hour', bar.label)"
              @blur="clearHoveredItem('hour')"
            >
              <span class="hour-count">{{ bar.count }}</span>
              <div class="hour-track">
                <span
                  class="hour-fill"
                  :style="{ height: `${Math.max(bar.ratio * 100, 5)}%` }"
                ></span>
              </div>
              <span class="hour-label">{{ bar.label }}</span>
            </article>
          </div>
        </article>

        <article class="chart-card chart-card--interactive small-card">
          <header class="chart-header">
            <div>
              <p class="chart-kicker">星期热力</p>
              <h3>周内活跃热度</h3>
            </div>
            <span>周一至周日</span>
          </header>
          <p class="chart-hover-tip" :class="{ 'is-active': Boolean(activeWeekdayCell) }">
            <span>当前焦点</span>
            <strong>{{
              activeWeekdayCell
                ? `${activeWeekdayCell.label} · ${activeWeekdayCell.count} 篇`
                : '悬停卡片查看周内热度分布'
            }}</strong>
          </p>

          <div class="weekday-grid">
            <article
              v-for="cell in weekdayCells"
              :key="cell.label"
              class="weekday-cell"
              :class="{
                'is-active': isHoveredItem('weekday', cell.label),
                'is-dimmed': isDimmedItem('weekday', cell.label),
              }"
              :style="{ '--heat': `${0.2 + cell.ratio * 0.8}` }"
              tabindex="0"
              @mouseenter="setHoveredItem('weekday', cell.label)"
              @mouseleave="clearHoveredItem('weekday')"
              @focus="setHoveredItem('weekday', cell.label)"
              @blur="clearHoveredItem('weekday')"
            >
              <span class="weekday-name">{{ cell.label }}</span>
              <strong class="weekday-count">{{ cell.count }}</strong>
            </article>
          </div>
        </article>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@500;600;700;800&family=Noto+Sans+SC:wght@400;500;700&display=swap');

/* 视觉基座：沿用全站浅色 token，并为统计页叠加更轻盈的未来感背景。 */
.stats-page {
  --stats-ink: #18283f;
  --stats-ink-soft: #6a7890;
  --stats-ink-soft-strong: #53627b;
  --stats-border: rgba(15, 23, 42, 0.08);
  --stats-border-strong: rgba(52, 104, 255, 0.22);
  --stats-surface: rgba(255, 255, 255, 0.84);
  --stats-surface-strong: rgba(255, 255, 255, 0.96);
  --stats-shadow: 0 34px 80px rgba(15, 23, 42, 0.08), 0 18px 34px rgba(52, 104, 255, 0.06);
  --stats-card-shadow: 0 22px 48px rgba(15, 23, 42, 0.05), 0 12px 22px rgba(52, 104, 255, 0.04);
  position: relative;
  min-height: calc(100vh - 64px);
  padding: 28px clamp(14px, 2vw, 34px) 48px;
  overflow: hidden;
  background:
    radial-gradient(1200px 620px at -10% -12%, rgba(52, 104, 255, 0.14) 0%, transparent 70%),
    radial-gradient(840px 520px at 105% 4%, rgba(255, 166, 112, 0.16) 0%, transparent 72%),
    radial-gradient(680px 420px at 55% 100%, rgba(16, 185, 129, 0.08) 0%, transparent 76%),
    linear-gradient(180deg, #fcfdff 0%, #f7f9fc 42%, #eef3f8 100%);
  color: var(--stats-ink);
}

.stats-grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(24, 40, 63, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(24, 40, 63, 0.035) 1px, transparent 1px);
  background-size: 34px 34px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.2), rgba(0, 0, 0, 0));
  pointer-events: none;
}

.stats-aurora {
  position: absolute;
  border-radius: 999px;
  filter: blur(14px);
  opacity: 0.72;
  animation: auroraFloat 16s ease-in-out infinite;
  pointer-events: none;
}

.stats-aurora--violet {
  width: 380px;
  height: 380px;
  left: -120px;
  top: -96px;
  background: radial-gradient(circle, rgba(96, 165, 250, 0.28) 0%, rgba(96, 165, 250, 0) 72%);
}

.stats-aurora--peach {
  width: 340px;
  height: 340px;
  right: -86px;
  top: 110px;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.24) 0%, rgba(251, 146, 60, 0) 72%);
  animation-delay: -4s;
}

.stats-shell {
  position: relative;
  z-index: 2;
  max-width: 1680px;
  margin: 0 auto;
  display: grid;
  gap: 16px;
}

/* 英雄区：保留原有入口信息，仅重组层级和版式，让首屏更有舞台感。 */
.hero-panel,
.command-card,
.status-tile,
.insight-chip,
.metric-card,
.chart-card,
.legend-item,
.bars-row--interactive,
.chart-column,
.weekday-cell,
.refresh-button,
.error-action,
.trend-month {
  transition:
    transform var(--transition-normal),
    box-shadow var(--transition-normal),
    border-color var(--transition-normal),
    background var(--transition-normal),
    opacity var(--transition-fast),
    color var(--transition-fast),
    filter var(--transition-fast);
}

.hero-panel {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.42fr) minmax(320px, 0.88fr);
  gap: clamp(16px, 2vw, 24px);
  padding: clamp(22px, 2.4vw, 32px);
  border-radius: 34px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(247, 250, 255, 0.82));
  border: 1px solid rgba(255, 255, 255, 0.78);
  box-shadow: var(--stats-shadow);
  backdrop-filter: blur(22px) saturate(155%);
  overflow: hidden;
  animation: riseIn 0.65s var(--ease-out);
}

.hero-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(52, 104, 255, 0.08), transparent 38%),
    radial-gradient(circle at 82% 18%, rgba(251, 146, 60, 0.12), transparent 24%);
  pointer-events: none;
}

.hero-panel > * {
  position: relative;
  z-index: 1;
}

.hero-panel:hover {
  transform: translateY(-2px);
  box-shadow:
    0 42px 90px rgba(15, 23, 42, 0.1),
    0 18px 32px rgba(52, 104, 255, 0.08);
}

.hero-copy {
  display: grid;
  align-content: space-between;
  gap: 22px;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-eyebrow,
.hero-mode {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  font-family: 'Outfit', var(--font-sans);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.hero-eyebrow {
  background: rgba(52, 104, 255, 0.12);
  color: #2458e8;
}

.hero-mode {
  background: rgba(251, 146, 60, 0.14);
  color: #c96a1a;
}

.hero-heading {
  display: grid;
  gap: 16px;
}

.hero-title {
  margin: 0;
  max-width: 8.4ch;
  font-family: 'Outfit', 'Noto Sans SC', var(--font-sans);
  font-size: clamp(36px, 5vw, 68px);
  line-height: 0.96;
  letter-spacing: -0.06em;
}

.hero-description {
  margin: 0;
  max-width: 56ch;
  font-size: 15px;
  line-height: 1.8;
  color: var(--stats-ink-soft);
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(52, 104, 255, 0.1);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.88),
    0 10px 18px rgba(15, 23, 42, 0.05);
  font-size: 13px;
  color: var(--stats-ink-soft-strong);
}

.hero-command {
  display: grid;
  gap: 14px;
  align-content: stretch;
}

.command-card {
  padding: 18px;
  border-radius: 28px;
  background: linear-gradient(
    145deg,
    rgba(52, 104, 255, 0.1),
    rgba(255, 255, 255, 0.92) 48%,
    rgba(255, 245, 236, 0.92)
  );
  border: 1px solid rgba(52, 104, 255, 0.1);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.82),
    0 20px 34px rgba(15, 23, 42, 0.06);
}

.command-card--action:hover {
  transform: translateY(-3px);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.84),
    0 24px 40px rgba(52, 104, 255, 0.12);
}

.command-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.command-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--stats-ink-soft-strong);
}

.command-title {
  display: block;
  font-size: 18px;
  line-height: 1.3;
}

.command-badge {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(99, 102, 241, 0.14);
  font-family: 'Outfit', var(--font-sans);
  font-size: 12px;
  font-weight: 700;
  color: var(--primary-color);
}

.refresh-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 50px;
  width: 100%;
  border-radius: 18px;
  border: none;
  background: linear-gradient(135deg, #3468ff 0%, #537fe7 58%, #f08a5d 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 18px 36px rgba(52, 104, 255, 0.22);
}

.refresh-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 24px 44px rgba(52, 104, 255, 0.28);
}

.refresh-button:disabled {
  cursor: not-allowed;
  opacity: 0.82;
}

.refresh-icon.is-spinning {
  animation: rotate360 0.9s linear infinite;
}

.updated-at {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--stats-ink-soft-strong);
}

.hero-status-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.status-tile {
  position: relative;
  overflow: hidden;
  padding: 18px;
  border-radius: 24px;
  border: 1px solid rgba(52, 104, 255, 0.1);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 255, 0.82));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 18px 28px rgba(15, 23, 42, 0.05);
}

.status-tile::after {
  content: '';
  position: absolute;
  width: 140px;
  height: 140px;
  right: -42px;
  bottom: -70px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(129, 140, 248, 0.18) 0%, rgba(129, 140, 248, 0) 72%);
}

.status-tile:hover {
  transform: translateY(-4px);
  border-color: var(--stats-border-strong);
  box-shadow: 0 24px 34px rgba(99, 102, 241, 0.12);
}

.status-label {
  position: relative;
  z-index: 1;
  display: block;
  margin-bottom: 10px;
  font-size: 12px;
  color: var(--stats-ink-soft-strong);
}

.status-value {
  position: relative;
  z-index: 1;
  font-family: 'Outfit', var(--font-sans);
  font-size: 34px;
  line-height: 1;
  letter-spacing: -0.04em;
  font-variant-numeric: tabular-nums;
  background: linear-gradient(135deg, #3468ff 0%, #f08a5d 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.status-value.is-negative {
  background: none;
  -webkit-text-fill-color: initial;
  color: var(--danger-color);
}

/* 洞察卡和指标卡：继续使用原有数据，只在视觉上强化主次和节奏。 */
.insight-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.insight-chip {
  position: relative;
  overflow: hidden;
  padding: 20px 18px 18px 74px;
  border-radius: 26px;
  border: 1px solid var(--stats-border);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.96), rgba(247, 249, 255, 0.82));
  box-shadow: var(--stats-card-shadow);
}

.insight-chip::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(99, 102, 241, 0.06),
    transparent 42%,
    rgba(236, 72, 153, 0.08)
  );
  pointer-events: none;
}

.insight-chip:hover {
  transform: translateY(-4px);
  border-color: var(--stats-border-strong);
  box-shadow:
    0 28px 48px rgba(99, 102, 241, 0.14),
    0 12px 20px rgba(15, 23, 42, 0.05);
}

.insight-index {
  position: absolute;
  left: 18px;
  top: 18px;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-family: 'Outfit', var(--font-sans);
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #3468ff 0%, #5d8bff 100%);
  box-shadow: 0 12px 22px rgba(99, 102, 241, 0.24);
}

.insight-label,
.insight-value,
.insight-sub {
  position: relative;
  z-index: 1;
}

.insight-label {
  font-size: 12px;
  color: var(--stats-ink-soft-strong);
}

.insight-value {
  display: block;
  margin-top: 10px;
  font-size: 24px;
  line-height: 1.3;
}

.insight-sub {
  display: block;
  margin-top: 10px;
  font-size: 13px;
  color: var(--stats-ink-soft);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  position: relative;
  overflow: hidden;
  min-height: 176px;
  padding: 18px 18px 20px;
  border-radius: 26px;
  border: 1px solid var(--stats-border);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.98), rgba(249, 250, 255, 0.82));
  box-shadow: var(--stats-card-shadow);
  animation: riseIn 0.6s var(--ease-out) both;
  animation-delay: var(--delay, 0ms);
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 18px;
  right: 18px;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--accent), rgba(255, 255, 255, 0.94));
}

.metric-card:hover {
  transform: translateY(-4px);
  border-color: var(--stats-border-strong);
  box-shadow:
    0 30px 52px rgba(99, 102, 241, 0.14),
    0 12px 20px rgba(15, 23, 42, 0.05);
}

.metric-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.metric-label {
  margin: 0;
  font-size: 13px;
  color: var(--stats-ink-soft-strong);
}

.metric-accent-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 0 8px rgba(99, 102, 241, 0.06);
}

.metric-value-row {
  margin: 28px 0 10px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.metric-value {
  font-family: 'Outfit', var(--font-sans);
  font-size: clamp(34px, 3vw, 48px);
  font-weight: 700;
  line-height: 1;
  letter-spacing: -0.05em;
  font-variant-numeric: tabular-nums;
  animation: numberPop 0.85s var(--ease-out) both;
}

.metric-unit {
  font-size: 13px;
  color: var(--stats-ink-soft-strong);
}

.metric-helper {
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
  color: var(--stats-ink-soft);
}

.metric-ribbon {
  position: absolute;
  width: 86px;
  height: 86px;
  right: 18px;
  bottom: 14px;
  border-radius: 26px;
  background: linear-gradient(135deg, var(--accent), rgba(255, 255, 255, 0.98));
  opacity: 0.16;
  transform: rotate(12deg);
}

.metric-glow {
  position: absolute;
  width: 170px;
  height: 170px;
  right: -72px;
  top: -70px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent) 0%, rgba(255, 255, 255, 0) 72%);
  opacity: 0.14;
}

.error-banner,
.loading-board,
.empty-board {
  border-radius: 26px;
  border: 1px solid var(--stats-border);
  background: var(--stats-surface-strong);
  box-shadow: var(--stats-card-shadow);
}

.error-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  color: var(--danger-color);
}

.error-action {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
  color: #fff;
  font-weight: 700;
}

.error-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 28px rgba(239, 68, 68, 0.24);
}

.loading-board {
  padding: 20px;
  display: grid;
  gap: 12px;
}

.loading-line {
  height: 16px;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    rgba(226, 232, 240, 0.45) 0%,
    rgba(165, 180, 252, 0.52) 45%,
    rgba(226, 232, 240, 0.45) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.4s linear infinite;
}

.empty-board {
  padding: 40px 20px;
  text-align: center;
}

.empty-board h2 {
  margin: 0 0 10px;
  font-size: 28px;
}

.empty-board p {
  margin: 0;
  color: var(--stats-ink-soft);
}

/* 图表区域：统一卡片语言，再通过 hover / focus 强化可读性和点击反馈。 */
.chart-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(12, minmax(0, 1fr));
}

.chart-grid > .chart-card {
  animation: cardReveal 0.6s var(--ease-out) both;
}

.chart-grid > .chart-card:nth-child(1) {
  animation-delay: 40ms;
}

.chart-grid > .chart-card:nth-child(2) {
  animation-delay: 90ms;
}

.chart-grid > .chart-card:nth-child(3) {
  animation-delay: 140ms;
}

.chart-grid > .chart-card:nth-child(4) {
  animation-delay: 190ms;
}

.chart-grid > .chart-card:nth-child(5) {
  animation-delay: 240ms;
}

.chart-grid > .chart-card:nth-child(6) {
  animation-delay: 290ms;
}

.chart-grid > .chart-card:nth-child(7) {
  animation-delay: 340ms;
}

.chart-grid > .chart-card:nth-child(8) {
  animation-delay: 390ms;
}

.chart-card {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  border-radius: 30px;
  border: 1px solid var(--stats-border);
  background: linear-gradient(165deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 255, 0.82));
  box-shadow: var(--stats-card-shadow);
  backdrop-filter: blur(18px) saturate(150%);
  padding: 20px;
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, rgba(52, 104, 255, 0.92), rgba(94, 130, 220, 0.86), rgba(240, 138, 93, 0.76));
  background-size: 200% 100%;
}

.chart-card::after {
  content: '';
  position: absolute;
  width: 220px;
  height: 220px;
  right: -82px;
  top: -92px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(52, 104, 255, 0.12) 0%, rgba(52, 104, 255, 0) 74%);
  pointer-events: none;
}

.chart-card > * {
  position: relative;
  z-index: 1;
}

.chart-card--interactive:hover {
  transform: translateY(-6px);
  border-color: var(--stats-border-strong);
  box-shadow:
    0 34px 64px rgba(52, 104, 255, 0.12),
    0 18px 28px rgba(15, 23, 42, 0.08);
}

.chart-card--interactive:hover::before {
  animation: gradientShift 1.6s linear infinite;
}

.chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-header > div {
  display: grid;
  gap: 4px;
}

.chart-kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--stats-ink-soft-strong);
}

.chart-header h3 {
  margin: 0;
  font-size: 22px;
  line-height: 1.3;
}

.chart-header span {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(52, 104, 255, 0.08);
  border: 1px solid rgba(52, 104, 255, 0.1);
  font-size: 12px;
  color: var(--stats-ink-soft-strong);
}

.trend-card {
  grid-column: span 7;
}

.donut-card {
  grid-column: span 5;
}

.small-card {
  grid-column: span 4;
}

.trend-panel {
  position: relative;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(249, 250, 255, 0.96), rgba(255, 255, 255, 0.76));
  border: 1px solid rgba(52, 104, 255, 0.08);
  padding: 8px 0 4px;
  overflow: hidden;
}

.trend-floating-tooltip {
  position: absolute;
  left: var(--x);
  top: var(--y);
  transform: translate(-50%, -128%);
  min-width: 120px;
  padding: 8px 10px;
  border-radius: 14px;
  border: 1px solid rgba(99, 102, 241, 0.18);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 22px rgba(99, 102, 241, 0.18);
  backdrop-filter: blur(14px);
  pointer-events: none;
  animation: tooltipRise 180ms var(--ease-out);
}

.trend-floating-tooltip::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -6px;
  width: 10px;
  height: 10px;
  border-right: 1px solid rgba(99, 102, 241, 0.18);
  border-bottom: 1px solid rgba(99, 102, 241, 0.18);
  background: rgba(255, 255, 255, 0.94);
  transform: translateX(-50%) rotate(45deg);
}

.tooltip-month {
  display: block;
  font-size: 11px;
  color: var(--stats-ink-soft-strong);
}

.tooltip-value {
  display: block;
  margin-top: 3px;
  font-family: 'Outfit', var(--font-sans);
  font-size: 18px;
  line-height: 1;
}

.trend-svg {
  width: 100%;
  height: 280px;
  overflow: visible;
}

.trend-grid-line {
  stroke: rgba(148, 163, 184, 0.3);
}

.trend-grid-text {
  fill: rgba(91, 100, 139, 0.72);
  font-size: 10px;
}

.trend-area {
  fill: url(#trendAreaGradient);
}

.trend-focus-line {
  stroke: rgba(99, 102, 241, 0.24);
  stroke-width: 2;
  stroke-dasharray: 5 6;
}

.trend-line {
  fill: none;
  stroke: #6366f1;
  stroke-width: 4;
  stroke-linecap: round;
  stroke-linejoin: round;
  filter: drop-shadow(0 12px 22px rgba(99, 102, 241, 0.2));
}

.trend-line.is-focused {
  filter: drop-shadow(0 16px 28px rgba(99, 102, 241, 0.28));
}

.trend-point-group {
  cursor: pointer;
  transition: opacity var(--transition-fast);
}

.trend-point-group.is-dimmed {
  opacity: 0.36;
}

.trend-point-halo {
  fill: rgba(99, 102, 241, 0.1);
  transition:
    r var(--transition-fast),
    fill var(--transition-fast),
    opacity var(--transition-fast);
}

.trend-point-group.is-active .trend-point-halo {
  fill: rgba(99, 102, 241, 0.18);
  animation: haloPulse 1.2s ease-in-out infinite;
}

.trend-point {
  fill: #fff;
  stroke: #6366f1;
  stroke-width: 3;
  transition:
    r var(--transition-fast),
    filter var(--transition-fast),
    fill var(--transition-fast);
}

.trend-point-group.is-active .trend-point {
  fill: #eef2ff;
  filter: drop-shadow(0 8px 16px rgba(99, 102, 241, 0.22));
}

.trend-point-value {
  fill: #4f46e5;
  font-size: 11px;
  text-anchor: middle;
  font-weight: 700;
  transition:
    fill var(--transition-fast),
    transform var(--transition-fast),
    opacity var(--transition-fast);
}

.trend-point-group.is-active .trend-point-value {
  fill: #312e81;
}

.trend-label-row {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 6px;
  margin-top: 14px;
  padding: 6px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(99, 102, 241, 0.08);
}

.trend-month {
  padding: 10px 8px;
  border-radius: 14px;
  text-align: center;
  font-size: 12px;
  color: var(--stats-ink-soft);
  cursor: pointer;
  outline: none;
}

.trend-month:hover,
.trend-month.is-active,
.trend-month:focus-visible {
  color: #4f46e5;
  background: rgba(99, 102, 241, 0.12);
  transform: translateY(-1px);
}

.donut-content {
  display: grid;
  grid-template-columns: minmax(0, 250px) minmax(0, 1fr);
  align-items: center;
  gap: 16px;
}

.donut-shell {
  position: relative;
  width: min(240px, 100%);
  aspect-ratio: 1;
  margin: 0 auto;
}

.donut-shell svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.donut-shell::after {
  content: '';
  position: absolute;
  inset: 28px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.9) 0%,
    rgba(255, 255, 255, 0.14) 72%,
    transparent 100%
  );
  pointer-events: none;
}

.donut-track,
.donut-segment {
  fill: none;
  stroke-width: 24;
}

.donut-track {
  stroke: rgba(148, 163, 184, 0.18);
}

.donut-segment {
  stroke-linecap: round;
  cursor: pointer;
  transition:
    stroke-width var(--transition-fast),
    opacity var(--transition-fast),
    filter var(--transition-fast);
}

.donut-segment.is-active {
  stroke-width: 28;
  filter: drop-shadow(0 10px 16px rgba(15, 23, 42, 0.14));
  animation: donutSegmentPulse 1.25s ease-in-out infinite alternate;
}

.donut-segment.is-dimmed {
  opacity: 0.34;
}

.donut-center {
  position: absolute;
  inset: 44px;
  border-radius: 50%;
  display: grid;
  place-content: center;
  text-align: center;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(255, 255, 255, 0.72));
  border: 1px solid rgba(99, 102, 241, 0.08);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.84),
    0 16px 30px rgba(99, 102, 241, 0.08);
}

.donut-center strong {
  font-family: 'Outfit', var(--font-sans);
  font-size: 42px;
  line-height: 1;
  letter-spacing: -0.05em;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 52%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.donut-center span {
  margin-top: 8px;
  font-size: 12px;
  color: var(--stats-ink-soft);
}

.legend-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.donut-hover-tip {
  margin: 12px 0 0;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px dashed rgba(99, 102, 241, 0.2);
  background: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  color: var(--stats-ink-soft-strong);
  line-height: 1.6;
}

.donut-hover-tip.is-active {
  border-style: solid;
  border-color: rgba(99, 102, 241, 0.34);
  color: var(--stats-ink);
}

.legend-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 18px;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  outline: none;
}

.legend-item:hover,
.legend-item.is-active,
.legend-item:focus-visible {
  transform: translateX(4px);
  border-color: var(--stats-border-strong);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 24px rgba(99, 102, 241, 0.12);
}

.legend-item.is-dimmed {
  opacity: 0.5;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-name,
.legend-value {
  font-size: 13px;
}

.legend-value {
  color: var(--stats-ink-soft);
}

.chart-hover-tip {
  margin: 0 0 12px;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px dashed rgba(99, 102, 241, 0.18);
  background: rgba(255, 255, 255, 0.66);
  display: grid;
  gap: 4px;
}

.chart-hover-tip span {
  font-size: 11px;
  color: var(--stats-ink-soft-strong);
  letter-spacing: 0.06em;
}

.chart-hover-tip strong {
  font-size: 13px;
  color: var(--stats-ink-soft);
  line-height: 1.5;
}

.chart-hover-tip.is-active {
  border-style: solid;
  border-color: rgba(99, 102, 241, 0.3);
}

.chart-hover-tip.is-active strong {
  color: var(--stats-ink);
}

.bars-list {
  display: grid;
  gap: 12px;
}

.bars-row {
  display: grid;
  gap: 8px;
}

.bars-row--interactive {
  padding: 12px 14px;
  border-radius: 18px;
  border: 1px solid transparent;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(247, 248, 255, 0.6));
  cursor: pointer;
  outline: none;
}

.bars-row--interactive:hover,
.bars-row--interactive.is-active,
.bars-row--interactive:focus-visible {
  transform: translateX(4px);
  border-color: var(--stats-border-strong);
  box-shadow: 0 16px 26px rgba(99, 102, 241, 0.12);
}

.bars-row--interactive.is-dimmed {
  opacity: 0.52;
}

.bars-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  font-size: 13px;
}

.bars-head strong {
  font-family: 'Outfit', var(--font-sans);
  font-size: 18px;
  line-height: 1;
  color: var(--stats-ink);
}

.bars-track {
  width: 100%;
  height: 12px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.8);
  overflow: hidden;
}

.bars-fill {
  position: relative;
  display: block;
  height: 100%;
  width: 0;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--bar-color), rgba(255, 255, 255, 0.98));
  box-shadow: 0 10px 18px rgba(99, 102, 241, 0.12);
  transform-origin: left center;
  overflow: hidden;
  animation: growX 0.72s var(--ease-out) both;
  animation-delay: var(--delay, 0ms);
}

.bars-fill::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    100deg,
    transparent 0%,
    rgba(255, 255, 255, 0.56) 50%,
    transparent 100%
  );
  opacity: 0;
}

.bars-row--interactive:hover .bars-fill::after,
.bars-row--interactive.is-active .bars-fill::after,
.bars-row--interactive:focus-visible .bars-fill::after {
  opacity: 1;
  animation: barSweep 1.2s linear infinite;
}

.depth-columns,
.hour-columns {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
}

.hour-columns {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.depth-column,
.hour-column {
  display: grid;
  justify-items: center;
  gap: 10px;
  padding: 14px 10px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(247, 248, 255, 0.58));
}

.chart-column {
  border: 1px solid transparent;
  cursor: pointer;
  outline: none;
}

.chart-column:hover,
.chart-column.is-active,
.chart-column:focus-visible {
  transform: translateY(-4px);
  border-color: var(--stats-border-strong);
  box-shadow: 0 16px 28px rgba(99, 102, 241, 0.12);
}

.chart-column.is-dimmed {
  opacity: 0.5;
}

.depth-track,
.hour-track {
  width: 100%;
  max-width: 58px;
  height: 160px;
  padding: 6px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(226, 232, 240, 0.86), rgba(255, 255, 255, 0.52));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.76);
  display: flex;
  align-items: flex-end;
}

.depth-fill,
.hour-fill {
  width: 100%;
  min-height: 12px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), var(--column-color));
  box-shadow: 0 12px 20px rgba(99, 102, 241, 0.12);
  animation: growY 0.76s var(--ease-out) both;
  transform-origin: bottom center;
}

.depth-count,
.hour-count {
  font-family: 'Outfit', var(--font-sans);
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
  color: var(--stats-ink);
}

.depth-label,
.hour-label {
  font-size: 12px;
  color: var(--stats-ink-soft);
}

.weekday-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 12px;
}

.weekday-cell {
  position: relative;
  overflow: hidden;
  padding: 18px 10px;
  border-radius: 22px;
  text-align: center;
  border: 1px solid rgba(99, 102, 241, 0.12);
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.94),
    rgba(99, 102, 241, calc(var(--heat) * 0.24))
  );
  cursor: pointer;
  outline: none;
}

.weekday-cell::after {
  content: '';
  position: absolute;
  width: 92px;
  height: 92px;
  right: -18px;
  top: -26px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.7) 0%, rgba(255, 255, 255, 0) 72%);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.weekday-cell:hover,
.weekday-cell.is-active,
.weekday-cell:focus-visible {
  transform: translateY(-4px);
  border-color: var(--stats-border-strong);
  box-shadow: 0 18px 30px rgba(99, 102, 241, 0.12);
}

.weekday-cell:hover::after,
.weekday-cell.is-active::after,
.weekday-cell:focus-visible::after {
  opacity: 1;
}

.weekday-cell.is-dimmed {
  opacity: 0.52;
}

.weekday-name,
.weekday-count {
  position: relative;
  z-index: 1;
}

.weekday-name {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--stats-ink-soft-strong);
}

.weekday-count {
  font-family: 'Outfit', var(--font-sans);
  font-size: clamp(22px, 2.3vw, 34px);
  line-height: 1;
}

@media (max-width: 1320px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .small-card {
    grid-column: span 6;
  }
}

@media (max-width: 1120px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .trend-card,
  .donut-card,
  .small-card {
    grid-column: span 12;
  }

  .donut-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .insight-strip,
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .hour-columns {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .weekday-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .stats-page {
    padding: 16px 12px 30px;
  }

  .hero-title {
    max-width: none;
  }

  .hero-status-grid,
  .command-head {
    grid-template-columns: 1fr;
  }

  .chart-header,
  .command-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .trend-label-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .hour-columns,
  .depth-columns,
  .weekday-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .insight-chip {
    padding-left: 70px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .stats-aurora,
  .metric-card,
  .bars-fill,
  .depth-fill,
  .hour-fill,
  .loading-line,
  .refresh-icon.is-spinning {
    animation: none !important;
  }

  .hero-panel,
  .command-card,
  .status-tile,
  .insight-chip,
  .metric-card,
  .chart-card,
  .legend-item,
  .bars-row--interactive,
  .chart-column,
  .weekday-cell,
  .refresh-button,
  .error-action,
  .trend-month {
    transition-duration: 1ms !important;
  }
}

@keyframes auroraFloat {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(0, -16px, 0) scale(1.06);
  }
}

@keyframes riseIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}

@keyframes growX {
  from {
    transform: scaleX(0);
  }

  to {
    transform: scaleX(1);
  }
}

@keyframes growY {
  from {
    transform: scaleY(0.18);
  }

  to {
    transform: scaleY(1);
  }
}

@keyframes rotate360 {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@keyframes numberPop {
  0% {
    opacity: 0.65;
    transform: translateY(6px) scale(0.96);
  }

  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes cardReveal {
  from {
    opacity: 0;
    transform: translateY(18px) scale(0.985);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes gradientShift {
  from {
    background-position: 0% 50%;
  }

  to {
    background-position: 100% 50%;
  }
}

@keyframes haloPulse {
  0%,
  100% {
    opacity: 0.68;
  }

  50% {
    opacity: 1;
  }
}

@keyframes donutSegmentPulse {
  from {
    filter: drop-shadow(0 10px 16px rgba(15, 23, 42, 0.14));
  }

  to {
    filter: drop-shadow(0 16px 24px rgba(99, 102, 241, 0.28));
  }
}

@keyframes barSweep {
  0% {
    transform: translateX(-140%);
  }

  100% {
    transform: translateX(140%);
  }
}

@keyframes tooltipRise {
  from {
    opacity: 0;
    transform: translate(-50%, -118%);
  }

  to {
    opacity: 1;
    transform: translate(-50%, -128%);
  }
}
</style>
