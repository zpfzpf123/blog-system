export interface Category {
  id: number
  name: string
  description?: string
  createdAt?: string
  updatedAt?: string
}

export interface Post {
  id: number
  title: string
  content: string
  summary?: string
  coverImage?: string
  categoryId: number
  tags?: string[]
  createdAt: string
  updatedAt: string
}

export interface Tag {
  id: number
  name: string
  createdAt?: string
  updatedAt?: string
}

export interface PaginationMeta {
  currentPage: number
  totalPages: number
  totalItems: number
  itemsPerPage: number
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}

export interface PaginatedResponse<T> {
  success: boolean
  data: T[]
  meta: PaginationMeta
  message?: string
}
