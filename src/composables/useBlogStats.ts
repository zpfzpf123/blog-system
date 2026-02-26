import { computed, ref } from 'vue'
import axios from '@/utils/axios'
import { buildBlogStats, type BlogSourcePost, type BlogStatsResult, type NamedEntity } from '@/utils/blogStats'

interface PostsPagination {
  totalPages?: number
}

interface PostsResponse {
  posts?: unknown
  pagination?: PostsPagination
}

const PAGE_SIZE = 100
const MAX_PAGINATION_FALLBACK_PAGES = 20

const normalizeNamedEntities = (input: unknown): NamedEntity[] => {
  if (!Array.isArray(input)) {
    return []
  }

  return input
    .map((item) => {
      if (typeof item !== 'object' || item === null) {
        return null
      }

      const rawId = (item as { id?: unknown }).id
      const rawName = (item as { name?: unknown }).name

      if (typeof rawId !== 'number' || typeof rawName !== 'string') {
        return null
      }

      return {
        id: rawId,
        name: rawName,
      }
    })
    .filter((item): item is NamedEntity => item !== null)
}

const normalizePosts = (input: unknown): BlogSourcePost[] => {
  if (!Array.isArray(input)) {
    return []
  }

  return input
    .map((item) => {
      if (typeof item !== 'object' || item === null) {
        return null
      }

      const rawPost = item as {
        id?: unknown
        title?: unknown
        desc?: unknown
        author?: unknown
        createdAt?: unknown
        category?: unknown
        tags?: unknown
      }

      if (
        typeof rawPost.id !== 'number' ||
        typeof rawPost.title !== 'string' ||
        typeof rawPost.createdAt !== 'string'
      ) {
        return null
      }

      const categoryCandidate = rawPost.category
      const category =
        typeof categoryCandidate === 'object' &&
        categoryCandidate !== null &&
        typeof (categoryCandidate as { id?: unknown }).id === 'number' &&
        typeof (categoryCandidate as { name?: unknown }).name === 'string'
          ? {
              id: (categoryCandidate as { id: number }).id,
              name: (categoryCandidate as { name: string }).name,
            }
          : null

      const tags = normalizeNamedEntities(rawPost.tags)

      return {
        id: rawPost.id,
        title: rawPost.title,
        desc: typeof rawPost.desc === 'string' ? rawPost.desc : '',
        author: typeof rawPost.author === 'string' ? rawPost.author : '',
        createdAt: rawPost.createdAt,
        category,
        tags,
      }
    })
    .filter((item): item is BlogSourcePost => item !== null)
}

const extractPostsResponse = (input: unknown): PostsResponse => {
  if (typeof input !== 'object' || input === null) {
    return {}
  }

  const response = input as PostsResponse
  return {
    posts: response.posts,
    pagination: response.pagination,
  }
}

export const useBlogStats = () => {
  const loading = ref(false)
  const errorMessage = ref('')
  const latestUpdatedAt = ref('')
  const categories = ref<NamedEntity[]>([])
  const tags = ref<NamedEntity[]>([])
  const posts = ref<BlogSourcePost[]>([])
  const stats = ref<BlogStatsResult>(buildBlogStats([], [], [], { months: 6 }))

  const fetchPostPage = async (page: number): Promise<PostsResponse> => {
    const response = await axios.get('/api/posts', {
      params: {
        page,
        limit: PAGE_SIZE,
        sortBy: 'id',
        sortOrder: 'desc',
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return extractPostsResponse(response.data)
  }

  const fetchAllPosts = async (): Promise<BlogSourcePost[]> => {
    const firstPage = await fetchPostPage(1)
    const allPosts = [...normalizePosts(firstPage.posts)]

    const reportedTotalPages = firstPage.pagination?.totalPages
    if (typeof reportedTotalPages === 'number' && reportedTotalPages > 1) {
      for (let page = 2; page <= reportedTotalPages; page += 1) {
        const pageResponse = await fetchPostPage(page)
        const pagePosts = normalizePosts(pageResponse.posts)
        allPosts.push(...pagePosts)
      }
      return allPosts
    }

    for (let page = 2; page <= MAX_PAGINATION_FALLBACK_PAGES; page += 1) {
      const pageResponse = await fetchPostPage(page)
      const pagePosts = normalizePosts(pageResponse.posts)

      if (pagePosts.length === 0) {
        break
      }

      allPosts.push(...pagePosts)

      if (pagePosts.length < PAGE_SIZE) {
        break
      }
    }

    return allPosts
  }

  const refresh = async () => {
    loading.value = true
    errorMessage.value = ''

    try {
      const [categoryResponse, tagResponse, postList] = await Promise.all([
        axios.get('/api/categories'),
        axios.get('/api/tags'),
        fetchAllPosts(),
      ])

      categories.value = normalizeNamedEntities(categoryResponse.data)
      tags.value = normalizeNamedEntities(tagResponse.data)
      posts.value = postList
      stats.value = buildBlogStats(posts.value, categories.value, tags.value, { months: 6 })
      latestUpdatedAt.value = new Date().toISOString()
    } catch (error) {
      console.error('Failed to load statistics data:', error)
      errorMessage.value = '加载统计数据失败，请稍后重试。'
    } finally {
      loading.value = false
    }
  }

  const totalInteractions = computed(() => {
    return stats.value.tagHeat.reduce((sum, item) => sum + item.count, 0)
  })

  return {
    loading,
    errorMessage,
    latestUpdatedAt,
    categories,
    tags,
    posts,
    stats,
    totalInteractions,
    refresh,
  }
}
