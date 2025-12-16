import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:4567'

export interface PageTemplate {
  id?: number
  name: string
  description: string
  category: string
  techStack?: string  // 技术栈，如: Vue3+ElementPlus
  htmlCode: string
  cssCode: string
  jsCode?: string
  thumbnailUrl?: string
  viewCount?: number
  copyCount?: number
  createdAt?: string
  updatedAt?: string
}

// 预定义的技术栈选项
export const TECH_STACKS = [
  { value: 'Vue3+ElementPlus', label: 'Vue3 + Element Plus', icon: '💚' },
  { value: 'Vue3+AntDesign', label: 'Vue3 + Ant Design', icon: '🐜' },
  { value: 'React+AntDesign', label: 'React + Ant Design', icon: '⚛️' },
  { value: 'React+MUI', label: 'React + MUI', icon: '🎨' },
  { value: 'Vue2+ElementUI', label: 'Vue2 + Element UI', icon: '🌿' },
  { value: 'HTML+CSS', label: '原生 HTML/CSS', icon: '🌐' },
  { value: 'TailwindCSS', label: 'Tailwind CSS', icon: '🎐' },
  { value: 'Bootstrap', label: 'Bootstrap', icon: '🅱️' }
]

export const TEMPLATE_CATEGORIES = [
  { value: 'login', label: '登录注册', icon: '🔐' },
  { value: 'form', label: '表单模板', icon: '📝' },
  { value: 'table', label: '表格模板', icon: '📊' },
  { value: 'card', label: '卡片组件', icon: '🃏' },
  { value: 'layout', label: '布局模板', icon: '📐' },
  { value: 'modal', label: '弹窗模板', icon: '🪟' },
  { value: 'list', label: '列表模板', icon: '📋' },
  { value: 'nav', label: '导航模板', icon: '🧭' },
  { value: 'dashboard', label: '仪表盘', icon: '📈' },
  { value: 'bigscreen', label: '大屏模板', icon: '🖥️' },
  { value: 'other', label: '其他', icon: '📦' }
]

class PageTemplateService {
  async getAllTemplates(): Promise<PageTemplate[]> {
    const res = await axios.get(`${API_BASE}/api/page-templates`)
    return res.data
  }

  async getTemplateById(id: number): Promise<PageTemplate> {
    const res = await axios.get(`${API_BASE}/api/page-templates/${id}`)
    return res.data
  }

  async getTemplatesByCategory(category: string): Promise<PageTemplate[]> {
    const res = await axios.get(`${API_BASE}/api/page-templates/category/${category}`)
    return res.data
  }

  async searchTemplates(keyword: string): Promise<PageTemplate[]> {
    const res = await axios.get(`${API_BASE}/api/page-templates/search`, { params: { keyword } })
    return res.data
  }

  async getCategories(): Promise<string[]> {
    const res = await axios.get(`${API_BASE}/api/page-templates/categories`)
    return res.data
  }

  async getTechStacks(): Promise<string[]> {
    const res = await axios.get(`${API_BASE}/api/page-templates/tech-stacks`)
    return res.data
  }

  async getTemplatesByTechStack(techStack: string): Promise<PageTemplate[]> {
    const res = await axios.get(`${API_BASE}/api/page-templates/tech-stack/${encodeURIComponent(techStack)}`)
    return res.data
  }

  async getPopularTemplates(): Promise<PageTemplate[]> {
    const res = await axios.get(`${API_BASE}/api/page-templates/popular`)
    return res.data
  }

  async createTemplate(template: PageTemplate): Promise<PageTemplate> {
    const res = await axios.post(`${API_BASE}/api/page-templates`, template)
    return res.data
  }

  async updateTemplate(id: number, template: PageTemplate): Promise<PageTemplate> {
    const res = await axios.put(`${API_BASE}/api/page-templates/${id}`, template)
    return res.data
  }

  async deleteTemplate(id: number): Promise<void> {
    await axios.delete(`${API_BASE}/api/page-templates/${id}`)
  }

  async incrementViewCount(id: number): Promise<void> {
    await axios.post(`${API_BASE}/api/page-templates/${id}/view`)
  }

  async incrementCopyCount(id: number): Promise<void> {
    await axios.post(`${API_BASE}/api/page-templates/${id}/copy`)
  }
}

export default new PageTemplateService()
