export interface HomeStateSnapshot {
  scrollTop: number
  page: number
  pageSize: number
  categoryIds: number[]
  tagIds: number[]
  searchKeyword: string
  lastViewedId: number | null
}

type HomeStateInput = {
  scrollTop?: unknown
  page?: unknown
  pageSize?: unknown
  categoryIds?: unknown
  tagIds?: unknown
  searchKeyword?: unknown
  lastViewedId?: unknown
}

const toFiniteNumber = (value: unknown, fallback: number): number => {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback
}

const toPositiveInteger = (value: unknown, fallback: number): number => {
  const parsed = toFiniteNumber(value, fallback)
  if (parsed <= 0) {
    return fallback
  }
  return Math.floor(parsed)
}

const toIdArray = (value: unknown): number[] => {
  if (!Array.isArray(value)) {
    return []
  }

  return value.filter((item): item is number => typeof item === 'number' && Number.isFinite(item))
}

const toSearchKeyword = (value: unknown): string => {
  return typeof value === 'string' ? value : ''
}

const toNullableId = (value: unknown): number | null => {
  return typeof value === 'number' && Number.isFinite(value) ? value : null
}

export const createHomeStateSnapshot = (input: HomeStateInput): HomeStateSnapshot => {
  return {
    scrollTop: Math.max(toFiniteNumber(input.scrollTop, 0), 0),
    page: toPositiveInteger(input.page, 1),
    pageSize: toPositiveInteger(input.pageSize, 12),
    categoryIds: toIdArray(input.categoryIds),
    tagIds: toIdArray(input.tagIds),
    searchKeyword: toSearchKeyword(input.searchKeyword),
    lastViewedId: toNullableId(input.lastViewedId),
  }
}

export const parseHomeStateSnapshot = (raw: string | null): HomeStateSnapshot | null => {
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw) as HomeStateInput
    return createHomeStateSnapshot(parsed)
  } catch {
    return null
  }
}
