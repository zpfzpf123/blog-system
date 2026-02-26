import { describe, expect, it } from 'vitest'

import { createHomeStateSnapshot, parseHomeStateSnapshot } from '@/views/homeState'

describe('home state snapshot', () => {
  it('keeps search keyword in snapshot', () => {
    const snapshot = createHomeStateSnapshot({
      scrollTop: 640,
      page: 2,
      pageSize: 12,
      categoryIds: [1, 3],
      tagIds: [9],
      searchKeyword: 'vue router',
      lastViewedId: 88,
    })

    expect(snapshot.searchKeyword).toBe('vue router')
  })

  it('restores search keyword from persisted state', () => {
    const raw = JSON.stringify({
      scrollTop: 640,
      page: 2,
      pageSize: 12,
      categoryIds: [1, 3],
      tagIds: [9],
      searchKeyword: 'pinia',
      lastViewedId: 88,
    })

    const restored = parseHomeStateSnapshot(raw)

    expect(restored?.searchKeyword).toBe('pinia')
  })

  it('supports legacy state without search keyword', () => {
    const raw = JSON.stringify({
      scrollTop: 640,
      page: 2,
      pageSize: 12,
      categoryIds: [1, 3],
      tagIds: [9],
      lastViewedId: 88,
    })

    const restored = parseHomeStateSnapshot(raw)

    expect(restored?.searchKeyword).toBe('')
  })
})
