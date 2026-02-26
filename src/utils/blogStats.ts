export interface NamedEntity {
  id: number
  name: string
}

export interface BlogSourcePost {
  id: number
  title: string
  desc: string
  author: string
  createdAt: string
  category: NamedEntity | null
  tags: NamedEntity[]
}

export interface SummaryMetrics {
  totalPosts: number
  totalCategories: number
  totalTags: number
  avgTagsPerPost: number
  categorizedRate: number
  recent30dPosts: number
  avgReadingMinutes: number
}

export interface TrendPoint {
  label: string
  count: number
}

export interface RankedPoint {
  name: string
  count: number
  percentage: number
}

export interface DistributionPoint {
  label: string
  count: number
}

export interface BuildBlogStatsOptions {
  now?: Date
  months?: number
}

export interface BlogStatsResult {
  summary: SummaryMetrics
  monthlyTrend: TrendPoint[]
  categoryShare: RankedPoint[]
  tagHeat: RankedPoint[]
  hourDistribution: DistributionPoint[]
  weekdayDistribution: DistributionPoint[]
  tagDensityDistribution: DistributionPoint[]
  contentDepthDistribution: DistributionPoint[]
  recencyDistribution: DistributionPoint[]
}

const DEFAULT_MONTH_WINDOW = 6
const DAY_IN_MS = 24 * 60 * 60 * 1000
const HOUR_BUCKETS = ['00-03', '04-07', '08-11', '12-15', '16-19', '20-23'] as const
const WEEKDAY_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] as const
const TAG_DENSITY_LABELS = ['0 标签', '1 标签', '2 标签', '3+ 标签'] as const
const CONTENT_DEPTH_LABELS = ['轻量', '标准', '长文', '深读'] as const
const RECENCY_LABELS = ['7天内', '8-30天', '31-90天', '90天以上'] as const

const toMonthKey = (date: Date): string => {
  const year = date.getUTCFullYear()
  const month = String(date.getUTCMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

const toFixedNumber = (value: number, fractionDigits = 2): number => {
  if (!Number.isFinite(value)) {
    return 0
  }
  return Number(value.toFixed(fractionDigits))
}

const toRankedPoints = (counter: Map<string, number>, total: number): RankedPoint[] => {
  return Array.from(counter.entries())
    .sort((left, right) => {
      if (right[1] !== left[1]) {
        return right[1] - left[1]
      }
      return left[0].localeCompare(right[0])
    })
    .map(([name, count]) => ({
      name,
      count,
      percentage: total > 0 ? toFixedNumber((count / total) * 100, 1) : 0,
    }))
}

const buildMonthWindow = (now: Date, months: number): string[] => {
  const safeMonths = Number.isFinite(months) ? Math.max(1, Math.floor(months)) : DEFAULT_MONTH_WINDOW
  const labels: string[] = []

  for (let offset = safeMonths - 1; offset >= 0; offset -= 1) {
    const base = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth() - offset, 1))
    labels.push(toMonthKey(base))
  }

  return labels
}

const resolveTagDensityLabel = (tagCount: number): (typeof TAG_DENSITY_LABELS)[number] => {
  if (tagCount <= 0) {
    return '0 标签'
  }
  if (tagCount === 1) {
    return '1 标签'
  }
  if (tagCount === 2) {
    return '2 标签'
  }
  return '3+ 标签'
}

const resolveContentDepthLabel = (textLength: number): (typeof CONTENT_DEPTH_LABELS)[number] => {
  if (textLength <= 80) {
    return '轻量'
  }
  if (textLength <= 180) {
    return '标准'
  }
  if (textLength <= 320) {
    return '长文'
  }
  return '深读'
}

const resolveRecencyLabel = (ageDays: number): (typeof RECENCY_LABELS)[number] => {
  if (ageDays <= 7) {
    return '7天内'
  }
  if (ageDays <= 30) {
    return '8-30天'
  }
  if (ageDays <= 90) {
    return '31-90天'
  }
  return '90天以上'
}

export const buildBlogStats = (
  posts: BlogSourcePost[],
  categories: NamedEntity[],
  tags: NamedEntity[],
  options: BuildBlogStatsOptions = {},
): BlogStatsResult => {
  const now = options.now ?? new Date()
  const monthLabels = buildMonthWindow(now, options.months ?? DEFAULT_MONTH_WINDOW)
  const monthlyCounter = new Map(monthLabels.map((label) => [label, 0]))

  const categoryCounter = new Map<string, number>()
  const tagCounter = new Map<string, number>()
  const hourCounter = new Map<string, number>(HOUR_BUCKETS.map((label) => [label, 0]))
  const weekdayCounter = new Map<string, number>(WEEKDAY_LABELS.map((label) => [label, 0]))
  const tagDensityCounter = new Map<string, number>(TAG_DENSITY_LABELS.map((label) => [label, 0]))
  const contentDepthCounter = new Map<string, number>(CONTENT_DEPTH_LABELS.map((label) => [label, 0]))
  const recencyCounter = new Map<string, number>(RECENCY_LABELS.map((label) => [label, 0]))

  let totalTagAssignments = 0
  let categorizedPosts = 0
  let recent30dPosts = 0
  let readingMinutesTotal = 0

  for (const post of posts) {
    const date = new Date(post.createdAt)

    if (!Number.isNaN(date.getTime())) {
      const monthKey = toMonthKey(date)
      if (monthlyCounter.has(monthKey)) {
        monthlyCounter.set(monthKey, (monthlyCounter.get(monthKey) ?? 0) + 1)
      }

      const hourBucket = HOUR_BUCKETS[Math.min(5, Math.floor(date.getUTCHours() / 4))]
      hourCounter.set(hourBucket, (hourCounter.get(hourBucket) ?? 0) + 1)

      const weekdayIndex = (date.getUTCDay() + 6) % 7
      const weekdayLabel = WEEKDAY_LABELS[weekdayIndex]
      weekdayCounter.set(weekdayLabel, (weekdayCounter.get(weekdayLabel) ?? 0) + 1)

      const ageDays = Math.floor((now.getTime() - date.getTime()) / DAY_IN_MS)
      const recencyLabel = resolveRecencyLabel(ageDays)
      recencyCounter.set(recencyLabel, (recencyCounter.get(recencyLabel) ?? 0) + 1)

      if (ageDays <= 30) {
        recent30dPosts += 1
      }
    }

    const categoryName = post.category?.name?.trim() || 'Uncategorized'
    if (categoryName !== 'Uncategorized') {
      categorizedPosts += 1
    }
    categoryCounter.set(categoryName, (categoryCounter.get(categoryName) ?? 0) + 1)

    const titleLength = post.title.trim().length
    const descLength = post.desc.trim().length
    const contentLength = titleLength + descLength
    const contentDepth = resolveContentDepthLabel(contentLength)
    contentDepthCounter.set(contentDepth, (contentDepthCounter.get(contentDepth) ?? 0) + 1)
    readingMinutesTotal += contentLength / 380

    const densityLabel = resolveTagDensityLabel(post.tags.length)
    tagDensityCounter.set(densityLabel, (tagDensityCounter.get(densityLabel) ?? 0) + 1)

    for (const tag of post.tags) {
      const tagName = tag.name?.trim()
      if (!tagName) {
        continue
      }
      totalTagAssignments += 1
      tagCounter.set(tagName, (tagCounter.get(tagName) ?? 0) + 1)
    }
  }

  const totalPosts = posts.length
  const summary: SummaryMetrics = {
    totalPosts,
    totalCategories: categories.length,
    totalTags: tags.length,
    avgTagsPerPost: totalPosts > 0 ? toFixedNumber(totalTagAssignments / totalPosts, 2) : 0,
    categorizedRate: totalPosts > 0 ? toFixedNumber((categorizedPosts / totalPosts) * 100, 1) : 0,
    recent30dPosts,
    avgReadingMinutes: totalPosts > 0 ? toFixedNumber(readingMinutesTotal / totalPosts, 1) : 0,
  }

  const monthlyTrend: TrendPoint[] = monthLabels.map((label) => ({
    label,
    count: monthlyCounter.get(label) ?? 0,
  }))

  return {
    summary,
    monthlyTrend,
    categoryShare: toRankedPoints(categoryCounter, Math.max(totalPosts, 1)),
    tagHeat: toRankedPoints(tagCounter, Math.max(totalPosts, 1)),
    hourDistribution: HOUR_BUCKETS.map((label) => ({
      label,
      count: hourCounter.get(label) ?? 0,
    })),
    weekdayDistribution: WEEKDAY_LABELS.map((label) => ({
      label,
      count: weekdayCounter.get(label) ?? 0,
    })),
    tagDensityDistribution: TAG_DENSITY_LABELS.map((label) => ({
      label,
      count: tagDensityCounter.get(label) ?? 0,
    })),
    contentDepthDistribution: CONTENT_DEPTH_LABELS.map((label) => ({
      label,
      count: contentDepthCounter.get(label) ?? 0,
    })),
    recencyDistribution: RECENCY_LABELS.map((label) => ({
      label,
      count: recencyCounter.get(label) ?? 0,
    })),
  }
}
