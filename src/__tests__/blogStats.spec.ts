import { describe, expect, it } from 'vitest'
import { buildBlogStats, type BlogSourcePost, type NamedEntity } from '@/utils/blogStats'

const categories: NamedEntity[] = [
  { id: 1, name: 'Frontend' },
  { id: 2, name: 'Backend' },
  { id: 3, name: 'DevOps' },
  { id: 4, name: 'AI' },
]

const tags: NamedEntity[] = [
  { id: 11, name: 'Vue' },
  { id: 12, name: 'TypeScript' },
  { id: 13, name: 'Java' },
  { id: 14, name: 'Spring' },
  { id: 15, name: 'Animation' },
  { id: 16, name: 'Docker' },
  { id: 17, name: 'API' },
]

const makeText = (length: number): string => '文'.repeat(length)

const posts: BlogSourcePost[] = [
  {
    id: 1,
    title: 'A',
    desc: makeText(40),
    author: 'Alice',
    createdAt: '2025-10-05T02:20:00Z',
    category: { id: 1, name: 'Frontend' },
    tags: [
      { id: 11, name: 'Vue' },
      { id: 12, name: 'TypeScript' },
    ],
  },
  {
    id: 2,
    title: 'B',
    desc: makeText(95),
    author: 'Bob',
    createdAt: '2025-10-19T09:40:00Z',
    category: { id: 1, name: 'Frontend' },
    tags: [{ id: 11, name: 'Vue' }],
  },
  {
    id: 3,
    title: 'C',
    desc: makeText(205),
    author: 'Alice',
    createdAt: '2025-11-01T13:00:00Z',
    category: { id: 2, name: 'Backend' },
    tags: [
      { id: 13, name: 'Java' },
      { id: 14, name: 'Spring' },
    ],
  },
  {
    id: 4,
    title: 'D',
    desc: makeText(360),
    author: '',
    createdAt: '2025-11-16T18:10:00Z',
    category: null,
    tags: [],
  },
  {
    id: 5,
    title: 'E',
    desc: makeText(70),
    author: 'Alice',
    createdAt: '2025-12-01T22:45:00Z',
    category: { id: 1, name: 'Frontend' },
    tags: [
      { id: 11, name: 'Vue' },
      { id: 15, name: 'Animation' },
    ],
  },
  {
    id: 6,
    title: 'F',
    desc: makeText(130),
    author: 'Carol',
    createdAt: '2026-01-11T07:15:00Z',
    category: { id: 3, name: 'DevOps' },
    tags: [{ id: 16, name: 'Docker' }],
  },
  {
    id: 7,
    title: 'G',
    desc: makeText(250),
    author: 'Bob',
    createdAt: '2026-01-20T15:35:00Z',
    category: { id: 2, name: 'Backend' },
    tags: [
      { id: 13, name: 'Java' },
      { id: 17, name: 'API' },
    ],
  },
  {
    id: 8,
    title: 'H',
    desc: makeText(420),
    author: 'Alice',
    createdAt: '2026-02-03T11:00:00Z',
    category: { id: 2, name: 'Backend' },
    tags: [{ id: 17, name: 'API' }],
  },
]

describe('buildBlogStats', () => {
  it('returns empty-safe structures when no posts are provided', () => {
    const stats = buildBlogStats([], [], [], {
      now: new Date('2026-02-15T00:00:00Z'),
      months: 6,
    })

    expect(stats.summary).toEqual({
      totalPosts: 0,
      totalCategories: 0,
      totalTags: 0,
      avgTagsPerPost: 0,
      categorizedRate: 0,
      recent30dPosts: 0,
      avgReadingMinutes: 0,
    })

    expect(stats.monthlyTrend).toHaveLength(6)
    expect(stats.monthlyTrend.every((point) => point.count === 0)).toBe(true)
    expect(stats.hourDistribution.reduce((sum, item) => sum + item.count, 0)).toBe(0)
    expect(stats.weekdayDistribution.reduce((sum, item) => sum + item.count, 0)).toBe(0)
    expect(stats.categoryShare).toEqual([])
    expect(stats.tagHeat).toEqual([])

    expect(stats.tagDensityDistribution.map((item) => item.count)).toEqual([0, 0, 0, 0])
    expect(stats.contentDepthDistribution.map((item) => item.count)).toEqual([0, 0, 0, 0])
    expect(stats.recencyDistribution.map((item) => item.count)).toEqual([0, 0, 0, 0])
  })

  it('builds summary metrics and rich chart-ready series from post data', () => {
    const stats = buildBlogStats(posts, categories, tags, {
      now: new Date('2026-02-15T00:00:00Z'),
      months: 6,
    })

    expect(stats.summary).toEqual({
      totalPosts: 8,
      totalCategories: 4,
      totalTags: 7,
      avgTagsPerPost: 1.38,
      categorizedRate: 87.5,
      recent30dPosts: 2,
      avgReadingMinutes: 0.5,
    })

    expect(stats.monthlyTrend.map((point) => point.label)).toEqual([
      '2025-09',
      '2025-10',
      '2025-11',
      '2025-12',
      '2026-01',
      '2026-02',
    ])

    expect(stats.monthlyTrend.map((point) => point.count)).toEqual([0, 2, 2, 1, 2, 1])

    expect(stats.categoryShare.slice(0, 3).map((item) => [item.name, item.count])).toEqual([
      ['Backend', 3],
      ['Frontend', 3],
      ['DevOps', 1],
    ])

    expect(stats.tagHeat.slice(0, 3).map((item) => [item.name, item.count])).toEqual([
      ['Vue', 3],
      ['API', 2],
      ['Java', 2],
    ])

    expect(stats.tagDensityDistribution.map((item) => [item.label, item.count])).toEqual([
      ['0 标签', 1],
      ['1 标签', 3],
      ['2 标签', 4],
      ['3+ 标签', 0],
    ])

    expect(stats.contentDepthDistribution.map((item) => [item.label, item.count])).toEqual([
      ['轻量', 2],
      ['标准', 2],
      ['长文', 2],
      ['深读', 2],
    ])

    expect(stats.recencyDistribution.map((item) => [item.label, item.count])).toEqual([
      ['7天内', 0],
      ['8-30天', 2],
      ['31-90天', 3],
      ['90天以上', 3],
    ])

    expect(stats.hourDistribution.map((item) => item.count)).toEqual([1, 1, 2, 2, 1, 1])
    expect(stats.weekdayDistribution.reduce((sum, item) => sum + item.count, 0)).toBe(8)
  })
})
