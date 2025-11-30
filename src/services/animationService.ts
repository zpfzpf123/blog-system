// 动画数据接口定义
export interface Animation {
  id: string
  name: string
  description: string
  category: string
  subcategory: string
  tags: string[]
  htmlCode: string
  cssCode: string
  jsCode?: string
  thumbnailUrl?: string
  author: {
    name: string
    email: string
    avatar?: string
  }
  createdAt: string
  updatedAt: string
  likes: number
  views: number
}

export interface Category {
  id: string
  name: string
  description: string
  count: number
  subcategories: Subcategory[]
}

export interface Subcategory {
  id: string
  name: string
  count: number
}

// 模拟数据
const mockAnimations: Animation[] = [
  {
    id: '1',
    name: '旋转加载器',
    description: '简单的旋转加载动画效果',
    category: 'loading',
    subcategory: 'spinner',
    tags: ['加载', '旋转', 'CSS'],
    htmlCode: '<div class="demo-spinner"></div>',
    cssCode: `.demo-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: demo-spin 1s linear infinite;
}

@keyframes demo-spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}`,
    author: {
      name: '开发者',
      email: 'dev@example.com'
    },
    createdAt: '2025-01-01',
    updatedAt: '2025-01-01',
    likes: 15,
    views: 120
  },
  {
    id: '2',
    name: '脉冲按钮',
    description: '带有脉冲效果的按钮动画',
    category: 'hover',
    subcategory: 'button',
    tags: ['按钮', '脉冲', '交互'],
    htmlCode: '<button class="demo-pulse-btn">点击我</button>',
    cssCode: `.demo-pulse-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  animation: demo-pulse 2s infinite;
}

@keyframes demo-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}`,
    author: {
      name: '设计师',
      email: 'designer@example.com'
    },
    createdAt: '2025-01-02',
    updatedAt: '2025-01-02',
    likes: 23,
    views: 89
  },
  {
    id: '3',
    name: '弹跳球',
    description: '上下弹跳的小球动画',
    category: 'special',
    subcategory: 'physics',
    tags: ['弹跳', '球', '物理'],
    htmlCode: '<div class="demo-bounce-ball"></div>',
    cssCode: `.demo-bounce-ball {
  width: 30px;
  height: 30px;
  background: #e74c3c;
  border-radius: 50%;
  animation: demo-bounce 1s infinite;
}

@keyframes demo-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}`,
    author: {
      name: '动画师',
      email: 'animator@example.com'
    },
    createdAt: '2025-01-03',
    updatedAt: '2025-01-03',
    likes: 31,
    views: 156
  }
]

const mockCategories: Category[] = [
  {
    id: 'all',
    name: '全部',
    description: '所有动画效果',
    count: mockAnimations.length,
    subcategories: []
  },
  {
    id: 'loading',
    name: '加载动画',
    description: '各种加载效果和进度指示器',
    count: mockAnimations.filter(a => a.category === 'loading').length,
    subcategories: [
      { id: 'spinner', name: '旋转器', count: 1 },
      { id: 'progress', name: '进度条', count: 0 }
    ]
  },
  {
    id: 'hover',
    name: '悬停效果',
    description: '鼠标悬停时的交互动画',
    count: mockAnimations.filter(a => a.category === 'hover').length,
    subcategories: [
      { id: 'button', name: '按钮', count: 1 },
      { id: 'card', name: '卡片', count: 0 }
    ]
  },
  {
    id: 'special',
    name: '特效动画',
    description: '创意特效和视觉效果',
    count: mockAnimations.filter(a => a.category === 'special').length,
    subcategories: [
      { id: 'physics', name: '物理效果', count: 1 },
      { id: 'particle', name: '粒子效果', count: 0 }
    ]
  }
]

// API 服务类
export class AnimationService {
  // 获取所有动画
  static async getAllAnimations(): Promise<Animation[]> {
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 300))
    return mockAnimations
  }

  // 根据ID获取动画
  static async getAnimationById(id: string): Promise<Animation | null> {
    await new Promise(resolve => setTimeout(resolve, 200))
    return mockAnimations.find(anim => anim.id === id) || null
  }

  // 获取分类列表
  static async getCategories(): Promise<Category[]> {
    await new Promise(resolve => setTimeout(resolve, 100))
    return mockCategories
  }

  // 搜索动画
  static async searchAnimations(query: string): Promise<Animation[]> {
    await new Promise(resolve => setTimeout(resolve, 200))
    return mockAnimations.filter(anim =>
      anim.name.toLowerCase().includes(query.toLowerCase()) ||
      anim.description.toLowerCase().includes(query.toLowerCase()) ||
      anim.tags.some(tag => tag.toLowerCase().includes(query.toLowerCase()))
    )
  }

  // 按分类筛选动画
  static async getAnimationsByCategory(categoryId: string): Promise<Animation[]> {
    await new Promise(resolve => setTimeout(resolve, 200))
    if (categoryId === 'all') return mockAnimations
    return mockAnimations.filter(anim => anim.category === categoryId)
  }

  // 增加点赞数
  static async likeAnimation(id: string): Promise<boolean> {
    await new Promise(resolve => setTimeout(resolve, 100))
    const animation = mockAnimations.find(anim => anim.id === id)
    if (animation) {
      animation.likes++
      return true
    }
    return false
  }

  // 增加浏览数
  static async viewAnimation(id: string): Promise<boolean> {
    await new Promise(resolve => setTimeout(resolve, 50))
    const animation = mockAnimations.find(anim => anim.id === id)
    if (animation) {
      animation.views++
      return true
    }
    return false
  }
}

export default AnimationService