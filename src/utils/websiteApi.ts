import axios from './axios'
import type { AxiosResponse } from 'axios'

// 类型定义
export interface Website {
  id: number
  name: string
  url: string
  description: string
  categoryIds: number[]
  categoryNames?: string[]
  categoryColors?: string[]
  icon?: string
  createdAt: string
  updatedAt: string

  isFavorite: boolean
  isActive: boolean
}

export interface Category {
  id: number
  name: string
  description: string
  color: string
  sortOrder: number
  isActive: boolean
  createdAt: string
  updatedAt: string
  websiteCount?: number
}

export interface WebsiteCreateRequest {
  name: string
  url: string
  description: string
  categoryIds: number[]
  icon?: string
  favicon?: string
  screenshot?: string
  isFavorite?: boolean
}

export interface WebsiteUpdateRequest extends WebsiteCreateRequest {
  id: number
}

export interface WebsiteScrapeResponse {
  title?: string
  description?: string
  keywords?: string
  favicon?: string
  success: boolean
  error?: string
}

export interface CategoryCreateRequest {
  name: string
  description: string
  color: string
  sortOrder?: number
}

export interface WebsiteQueryRequest {
  page?: number
  size?: number
  categoryIds?: number[] | null
  keyword?: string
  isFavorite?: boolean
  isActive?: boolean
  // 高级搜索参数
  name?: string
  description?: string
  startDate?: string
  endDate?: string
}

export interface WebsitesResponse {
  websites: Website[]
  totalCount: number
  totalPages: number
  pagination: {
    currentPage: number
    pageSize: number
    totalPages: number
    totalPosts: number
    hasNext: boolean
    hasPrev: boolean
  }
}

export interface WebsiteStatistics {
  totalWebsites: number
  totalCategories: number
  totalTags: number

  favoriteWebsites: number
  activeWebsites: number
}

export interface ImportResult {
  totalCount: number
  successCount: number
  failedCount: number
  errors: string[]
}

// 网站相关API
export const websiteApi = {
  // 获取网站列表
  getWebsites: (params: WebsiteQueryRequest): Promise<WebsitesResponse> => {
    const queryParams = new URLSearchParams()
    if (params.page !== undefined) queryParams.append('page', params.page.toString())
    if (params.size !== undefined) queryParams.append('size', params.size.toString())
    if (
      params.categoryIds !== undefined &&
      params.categoryIds !== null &&
      params.categoryIds.length > 0
    ) {
      console.log('添加 categoryIds 参数:', params.categoryIds)
      params.categoryIds.forEach((id) => {
        queryParams.append('categoryIds', id.toString())
      })
    } else {
      console.log('categoryIds 未定义或为空，不添加参数')
    }
    if (params.keyword) queryParams.append('keyword', params.keyword)
    if (params.isFavorite !== undefined)
      queryParams.append('isFavorite', params.isFavorite.toString())
    if (params.isActive !== undefined) queryParams.append('isActive', params.isActive.toString())

    // 高级搜索参数
    if (params.name) queryParams.append('name', params.name)
    if (params.description) queryParams.append('description', params.description)
    if (params.startDate) queryParams.append('startDate', params.startDate)
    if (params.endDate) queryParams.append('endDate', params.endDate)

    const url = `/api/websites?${queryParams.toString()}`
    console.log('发送请求到:', url)
    console.log('完整请求参数:', params)

    return axios.get(url).then((res: AxiosResponse) => res.data)
  },

  // 获取所有网站（用于状态监控）
  getAllWebsites: (): Promise<Website[]> => {
    return axios.get('/api/websites/all').then((res: AxiosResponse) => res.data)
  },

  // 根据ID获取网站
  getWebsiteById: (id: number): Promise<Website> => {
    return axios.get(`/api/websites/${id}`).then((res: AxiosResponse) => res.data)
  },

  // 抓取网站信息
  scrapeWebsiteInfo: (url: string): Promise<WebsiteScrapeResponse> => {
    // 配置：是否启用Python爬虫服务
    const USE_PYTHON_SCRAPER = false // 设置为false直接使用Spring Boot服务，避免重复请求

    if (USE_PYTHON_SCRAPER) {
      // 优先调用 Python 爬虫服务
      const PY_SCRAPER = 'http://localhost:8001'
      return axios
        .post(`${PY_SCRAPER}/scrape`, { url }, { timeout: 5000 }) // 添加5秒超时
        .then((res: AxiosResponse) => res.data)
        .catch((error) => {
          console.log('Python爬虫服务失败，回退到Spring Boot服务:', error.message)
          return axios.post('/api/websites/scrape', { url }).then((r: AxiosResponse) => r.data)
        })
    } else {
      // 直接使用Spring Boot服务
      return axios.post('/api/websites/scrape', { url }).then((r: AxiosResponse) => r.data)
    }
  },

  // 根据URL获取网站
  getWebsiteByUrl: (url: string): Promise<Website> => {
    return axios
      .get(`/api/websites/url?url=${encodeURIComponent(url)}`)
      .then((res: AxiosResponse) => res.data)
  },

  // 创建网站
  createWebsite: (data: WebsiteCreateRequest): Promise<Website> => {
    return axios.post('/api/websites', data).then((res: AxiosResponse) => res.data)
  },

  // 更新网站
  updateWebsite: (id: number, data: WebsiteCreateRequest): Promise<Website> => {
    return axios.put(`/api/websites/${id}`, data).then((res: AxiosResponse) => res.data)
  },

  // 删除网站
  deleteWebsite: (id: number): Promise<void> => {
    return axios.delete(`/api/websites/${id}`).then(() => {})
  },

  // 批量删除网站
  batchDeleteWebsites: (ids: number[]): Promise<void> => {
    return axios.delete('/api/websites/batch', { data: ids }).then(() => {})
  },

  // 根据分类获取网站
  getWebsitesByCategory: (categoryId: number): Promise<Website[]> => {
    return axios.get(`/api/websites/category/${categoryId}`).then((res: AxiosResponse) => res.data)
  },

  // 根据收藏状态获取网站
  getWebsitesByFavorite: (isFavorite: boolean): Promise<Website[]> => {
    return axios
      .get(`/api/websites/favorite?isFavorite=${isFavorite}`)
      .then((res: AxiosResponse) => res.data)
  },

  // 搜索网站
  searchWebsites: (keyword: string): Promise<Website[]> => {
    return axios
      .get(`/api/websites/search?keyword=${encodeURIComponent(keyword)}`)
      .then((res: AxiosResponse) => res.data)
  },

  // 根据标签获取网站
  getWebsitesByTag: (tagName: string): Promise<Website[]> => {
    return axios
      .get(`/api/websites/tag/${encodeURIComponent(tagName)}`)
      .then((res: AxiosResponse) => res.data)
  },

  // 获取热门网站
  getPopularWebsites: (limit: number = 10): Promise<Website[]> => {
    return axios.get(`/api/websites/popular?limit=${limit}`).then((res: AxiosResponse) => res.data)
  },

  // 获取最新网站
  getRecentWebsites: (limit: number = 10): Promise<Website[]> => {
    return axios.get(`/api/websites/recent?limit=${limit}`).then((res: AxiosResponse) => res.data)
  },

  // 切换收藏状态
  toggleFavorite: (id: number): Promise<void> => {
    return axios.post(`/api/websites/${id}/favorite`).then(() => {})
  },

  // 检查网站状态
  checkWebsiteStatus: (id: number): Promise<void> => {
    return axios.post(`/api/websites/${id}/check`).then(() => {})
  },

  // 批量检查网站状态
  batchCheckWebsiteStatus: (): Promise<void> => {
    return axios.post('/api/websites/check/batch').then(() => {})
  },

  // 获取网站统计信息
  getWebsiteStatistics: (): Promise<WebsiteStatistics> => {
    return axios.get('/api/websites/statistics').then((res: AxiosResponse) => res.data)
  },

  // 导入网站数据
  importWebsites: (data: WebsiteCreateRequest[]): Promise<ImportResult> => {
    return axios.post('/api/websites/import', data).then((res: AxiosResponse) => res.data)
  },

  // 导出网站数据
  exportWebsites: (params: WebsiteQueryRequest): Promise<Website[]> => {
    const queryParams = new URLSearchParams()
    if (params.page !== undefined) queryParams.append('page', params.page.toString())
    if (params.size !== undefined) queryParams.append('size', params.size.toString())
    if (
      params.categoryIds !== undefined &&
      params.categoryIds !== null &&
      params.categoryIds.length > 0
    ) {
      params.categoryIds.forEach((id) => {
        queryParams.append('categoryIds', id.toString())
      })
    }
    if (params.keyword) queryParams.append('keyword', params.keyword)
    if (params.isFavorite !== undefined)
      queryParams.append('isFavorite', params.isFavorite.toString())
    if (params.isActive !== undefined) queryParams.append('isActive', params.isActive.toString())

    // 高级搜索参数
    if (params.name) queryParams.append('name', params.name)
    if (params.description) queryParams.append('description', params.description)
    if (params.startDate) queryParams.append('startDate', params.startDate)
    if (params.endDate) queryParams.append('endDate', params.endDate)

    return axios
      .get(`/api/websites/export?${queryParams.toString()}`)
      .then((res: AxiosResponse) => res.data)
  },
}

// 分类相关API
export const categoryApi = {
  // 获取所有分类
  getAllCategories: (): Promise<Category[]> => {
    return axios.get('/api/websites/categories').then((res: AxiosResponse) => res.data)
  },

  // 获取分类列表（分页）
  getCategoriesWithPagination: (
    page: number = 1,
    size: number = 10,
  ): Promise<{ items: Category[]; total: number; page: number; size: number }> => {
    return axios
      .get(`/api/websites/categories?page=${page}&size=${size}`)
      .then((res: AxiosResponse) => res.data)
  },

  // 搜索分类
  searchCategories: (name: string): Promise<Category[]> => {
    return axios
      .get(`/api/websites/categories/search?keyword=${encodeURIComponent(name)}`)
      .then((res: AxiosResponse) => res.data)
  },

  // 创建分类
  createCategory: (data: CategoryCreateRequest): Promise<Category> => {
    return axios.post('/api/websites/categories', data).then((res: AxiosResponse) => res.data)
  },

  // 更新分类
  updateCategory: (id: number, data: CategoryCreateRequest): Promise<Category> => {
    return axios.put(`/api/websites/categories/${id}`, data).then((res: AxiosResponse) => res.data)
  },

  // 删除分类
  deleteCategory: (id: number): Promise<void> => {
    return axios.delete(`/api/websites/categories/${id}`).then(() => {})
  },

  // 批量更新分类顺序
  updateCategoryOrder: (orderData: Array<{ id: number; sortOrder: number }>): Promise<void> => {
    return axios.put('/api/websites/categories/order', orderData).then(() => {})
  },

  // 获取热门分类
  getPopularCategories: (limit: number = 10): Promise<Category[]> => {
    return axios
      .get(`/api/websites/categories/popular?limit=${limit}`)
      .then((res: AxiosResponse) => res.data)
  },
}
