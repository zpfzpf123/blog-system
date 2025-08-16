import axios from './axios'

// AI生成网站分类的响应类型
export interface AICategoryGenerationResult {
  description: string
  color: string
}

// AI生成博客分类的响应类型
export interface AIBlogCategoryGenerationResult {
  description: string
  color: string
  icon?: string
}

// AI生成网站信息的响应类型
export interface AIWebsiteGenerationResult {
  name: string
  description: string
  categoryId: number
  icon: string
}

// AI生成分类的请求类型
export interface AICategoryGenerationRequest {
  categoryName: string
}

/**
 * AI服务类
 * 用于调用Ollama模型进行AI生成
 */
export class AIService {
  private static readonly OLLAMA_BASE_URL = 'http://localhost:11434'
  private static readonly MODEL_NAME = 'deepseek-r1:8b'

  /**
   * 生成网站分类描述和颜色
   * @param categoryName 分类名称
   * @param existingCategories 已存在的分类列表，用于避免颜色重复
   * @returns 生成的分类描述和颜色
   */
  static async generateWebsiteCategory(
    categoryName: string,
    existingCategories: Array<{ name: string; color: string }> = [],
  ): Promise<AICategoryGenerationResult> {
    try {
      // 构建提示词
      const prompt = this.buildWebsiteCategoryPrompt(categoryName, existingCategories)

      // 直接使用fetch调用Ollama API，避免CORS问题
      const response = await fetch(`${this.OLLAMA_BASE_URL}/api/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model: this.MODEL_NAME,
          prompt: prompt,
          stream: false,
          options: {
            temperature: 0.7,
            top_p: 0.9,
            max_tokens: 500,
          },
        }),
      })

      if (!response.ok) {
        throw new Error(`Ollama API调用失败: ${response.status}`)
      }

      const data = await response.json()
      const aiResponse = data.response
      return this.parseWebsiteCategoryResponse(aiResponse, categoryName, existingCategories)
    } catch (error) {
      console.error('AI生成网站分类失败:', error)
      // 如果AI调用失败，返回默认值
      return this.getDefaultWebsiteCategoryData(categoryName, existingCategories)
    }
  }

  /**
   * 生成网站信息（名称、描述、分类、图标、标签）
   * @param url 网站地址
   * @param scrapedInfo 从网站抓取的信息（包含title、description、keywords、favicon、success、error）
   * @param categories 可用的分类列表
   * @returns 生成的网站信息
   */
  static async generateWebsiteInfo(
    url: string,
    scrapedInfo: {
      title?: string
      description?: string
      keywords?: string
      favicon?: string
      success?: boolean
      error?: string
    },
    categories: Array<{ id: number; name: string }>,
  ): Promise<AIWebsiteGenerationResult> {
    try {
      // 构建提示词
      const prompt = this.buildWebsiteInfoPrompt(url, scrapedInfo, categories)

      // 直接使用fetch调用Ollama API，避免CORS问题
      const response = await fetch(`${this.OLLAMA_BASE_URL}/api/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model: this.MODEL_NAME,
          prompt: prompt,
          stream: false,
          options: {
            temperature: 0.7,
            top_p: 0.9,
            max_tokens: 800,
          },
        }),
      })

      if (!response.ok) {
        throw new Error(`Ollama API调用失败: ${response.status}`)
      }

      const data = await response.json()
      const aiResponse = data.response
      return this.parseWebsiteInfoResponse(aiResponse, url, categories)
    } catch (error) {
      console.error('AI生成网站信息失败:', error)
      // 如果AI调用失败，返回默认值
      return this.getDefaultWebsiteData(url, scrapedInfo, categories)
    }
  }

  /**
   * 生成博客分类描述、颜色和图标
   * @param categoryName 分类名称
   * @returns 生成的博客分类描述、颜色和图标
   */
  static async generateBlogCategory(categoryName: string): Promise<AIBlogCategoryGenerationResult> {
    try {
      // 构建提示词
      const prompt = this.buildBlogCategoryPrompt(categoryName)

      // 直接使用fetch调用Ollama API，避免CORS问题
      const response = await fetch(`${this.OLLAMA_BASE_URL}/api/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model: this.MODEL_NAME,
          prompt: prompt,
          stream: false,
          options: {
            temperature: 0.7,
            top_p: 0.9,
            max_tokens: 500,
          },
        }),
      })

      if (!response.ok) {
        throw new Error(`Ollama API调用失败: ${response.status}`)
      }

      const data = await response.json()
      const aiResponse = data.response
      return this.parseBlogCategoryResponse(aiResponse, categoryName)
    } catch (error) {
      console.error('AI生成博客分类失败:', error)
      // 如果AI调用失败，返回默认值
      return this.getDefaultBlogCategoryData(categoryName)
    }
  }

  /**
   * 构建网站分类生成的提示词
   * @param categoryName 分类名称
   * @param existingCategories 已存在的分类列表
   * @returns 提示词
   */
  private static buildWebsiteCategoryPrompt(
    categoryName: string,
    existingCategories: Array<{ name: string; color: string }> = [],
  ): string {
    let existingColorsInfo = ''
    if (existingCategories.length > 0) {
      const colorList = existingCategories.map((cat) => `${cat.name}: ${cat.color}`).join('、')
      existingColorsInfo = `\n\n注意：以下分类颜色已被使用，请避免重复：\n${colorList}\n\n请为"${categoryName}"选择一个不同的颜色。`
    }

    return `你是一个专业的网站分类助手。请为网站分类"${categoryName}"生成以下内容：

1. 分类描述：生成一个简洁、专业的分类描述，说明这个分类包含什么类型的网站，用途是什么。描述应该简洁明了，不超过50个字。

2. 分类颜色：为这个分类推荐一个合适的颜色，颜色应该与分类主题相关，并且适合在网页上显示。请返回一个十六进制颜色代码（如#409EFF）。${existingColorsInfo}

重要：你必须严格按照以下JSON格式返回，不要添加任何其他内容，不要使用markdown格式：

{
  "description": "分类描述内容",
  "color": "#颜色代码"
}

示例：
{
  "description": "包含各种开发相关的工具和资源",
  "color": "#409EFF"
}`
  }

  /**
   * 构建网站信息生成的提示词
   * @param url 网站地址
   * @param scrapedInfo 从网站抓取的信息（包含title、description、keywords、favicon、success、error）
   * @param categories 可用的分类列表
   * @returns 提示词
   */
  private static buildWebsiteInfoPrompt(
    url: string,
    scrapedInfo: {
      title?: string
      description?: string
      keywords?: string
      favicon?: string
      success?: boolean
      error?: string
    },
    categories: Array<{ id: number; name: string }>,
  ): string {
    // 构建抓取信息的详细描述
    let scrapedInfoText = ''
    let hasValidScrapedInfo = false

    if (
      scrapedInfo.success !== false &&
      (scrapedInfo.title || scrapedInfo.description || scrapedInfo.keywords)
    ) {
      hasValidScrapedInfo = true
      scrapedInfoText = `\n\n📋 网站抓取信息分析：\n`

      if (scrapedInfo.title) {
        scrapedInfoText += `📌 页面标题: "${scrapedInfo.title}"\n`
      }

      if (scrapedInfo.description) {
        scrapedInfoText += `📝 页面描述: "${scrapedInfo.description}"\n`
      }

      if (scrapedInfo.keywords) {
        scrapedInfoText += `🏷️ 页面关键词: "${scrapedInfo.keywords}"\n`
      }

      if (scrapedInfo.favicon) {
        scrapedInfoText += `🎨 网站图标: ${scrapedInfo.favicon}\n`
      }
    } else if (scrapedInfo.error) {
      scrapedInfoText = `\n\n⚠️ 网站抓取失败: ${scrapedInfo.error}\n`
    } else {
      scrapedInfoText = `\n\n⚠️ 未能获取到网站详细信息，请根据URL进行分析\n`
    }

    // 构建分类选择指导
    const categoryGuide =
      categories.length > 0
        ? `\n📂 可选分类列表：\n${categories.map((c) => `  ${c.id}. ${c.name}`).join('\n')}\n`
        : '\n⚠️ 暂无可选分类，请选择默认分类ID: 1\n'

    return `🤖 AI网站信息生成助手

🔗 目标网站: ${url}

${scrapedInfoText}

${categoryGuide}

📋 请根据以上信息，为网站生成以下内容：

1. **网站名称** (name):
   - 基于页面标题、描述或URL生成简洁准确的名称
   - 如果抓取到标题，优先使用标题；否则根据URL域名生成
   - 名称应该简洁明了，不超过20个字符

2. **网站描述** (description):
   - 基于页面描述、关键词和网站功能生成专业描述
   - 描述应该说明网站的主要功能、用途和特色
   - 长度控制在50-100字之间，语言简洁专业

3. **所属分类** (categoryId):
   - 从上述分类列表中选择最合适的一个
   - 根据网站的功能、主题和内容类型进行判断
   - 如果都不合适，选择最接近的分类

4. **网站图标** (icon):
   - 推荐Element Plus图标库中的图标名称
   - 根据网站类型和功能选择合适的图标
   - 常用图标：Link(链接)、Monitor(技术)、Star(推荐)、Document(文档)、Edit(编辑)等

💡 分析建议：
${
  hasValidScrapedInfo
    ? '- 优先参考抓取到的页面标题和描述\n- 根据关键词判断网站主题和功能\n- 结合URL域名进行综合分析'
    : '- 根据URL域名和路径分析网站类型\n- 参考常见的网站分类标准\n- 生成通用的描述和图标'
}

📤 请严格按照以下JSON格式返回，不要添加任何其他内容：

{
  "name": "网站名称",
  "description": "网站描述",
  "categoryId": 分类ID数字,
  "icon": "图标名称"
}

✅ 示例输出：
{
  "name": "GitHub",
  "description": "全球最大的代码托管平台，支持Git版本控制，为开发者提供代码管理、协作和部署服务",
  "categoryId": 1,
  "icon": "Link"
}`
  }

  /**
   * 构建博客分类生成的提示词
   * @param categoryName 分类名称
   * @returns 提示词
   */
  private static buildBlogCategoryPrompt(categoryName: string): string {
    return `你是一个专业的博客分类助手。请为博客分类"${categoryName}"生成以下内容：

1. 分类描述：生成一个简洁、专业的分类描述，说明这个分类包含什么类型的博客文章，主题是什么。描述应该简洁明了，不超过50个字。

2. 分类颜色：为这个分类推荐一个合适的颜色，颜色应该与分类主题相关，并且适合在网页上显示。请返回一个十六进制颜色代码（如#409EFF）。

3. 分类图标：为这个分类推荐一个合适的图标名称，使用Element Plus图标库中的图标名称（如：Document、Edit、Star等）。

重要：你必须严格按照以下JSON格式返回，不要添加任何其他内容，不要使用markdown格式：

{
  "description": "分类描述内容",
  "color": "#颜色代码",
  "icon": "图标名称"
}

示例：
{
  "description": "分享技术心得、开发经验和解决方案",
  "color": "#409EFF",
  "icon": "Monitor"
}`
  }

  /**
   * 解析网站分类AI响应
   * @param aiResponse AI的响应文本
   * @param categoryName 分类名称
   * @param existingCategories 已存在的分类列表
   * @returns 解析后的结果
   */
  private static parseWebsiteCategoryResponse(
    aiResponse: string,
    categoryName: string,
    existingCategories: Array<{ name: string; color: string }> = [],
  ): AICategoryGenerationResult {
    console.log('AI原始响应:', aiResponse)

    try {
      // 清理响应文本，移除可能的markdown格式
      let cleanResponse = aiResponse.trim()

      // 如果响应被markdown代码块包围，提取内容
      if (cleanResponse.startsWith('```json')) {
        cleanResponse = cleanResponse.replace(/```json\s*/, '').replace(/\s*```/, '')
      } else if (cleanResponse.startsWith('```')) {
        cleanResponse = cleanResponse.replace(/```\s*/, '').replace(/\s*```/, '')
      }

      console.log('清理后的响应:', cleanResponse)

      // 尝试解析JSON响应
      const jsonMatch = cleanResponse.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        const jsonStr = jsonMatch[0]
        console.log('提取的JSON字符串:', jsonStr)

        const parsed = JSON.parse(jsonStr)
        console.log('解析后的对象:', parsed)

        if (parsed.description && parsed.color) {
          const result = {
            description: parsed.description.trim(),
            color: parsed.color.trim(),
          }
          console.log('最终结果:', result)
          return result
        } else {
          console.warn('JSON中缺少必要字段:', parsed)
        }
      } else {
        console.warn('未找到JSON格式的响应')
      }
    } catch (error) {
      console.warn('AI响应解析失败，使用默认值:', error)
    }

    // 如果解析失败，返回默认值
    console.log('使用默认值')
    return this.getDefaultWebsiteCategoryData(categoryName)
  }

  /**
   * 解析网站信息AI响应
   * @param aiResponse AI的响应文本
   * @param url 网站地址
   * @param categories 可用分类
   * @returns 解析后的结果
   */
  private static parseWebsiteInfoResponse(
    aiResponse: string,
    url: string,
    categories: Array<{ id: number; name: string }>,
  ): AIWebsiteGenerationResult {
    console.log('AI原始响应:', aiResponse)

    try {
      // 清理响应文本，移除可能的markdown格式
      let cleanResponse = aiResponse.trim()

      // 如果响应被markdown代码块包围，提取内容
      if (cleanResponse.startsWith('```json')) {
        cleanResponse = cleanResponse.replace(/```json\s*/, '').replace(/\s*```/, '')
      } else if (cleanResponse.startsWith('```')) {
        cleanResponse = cleanResponse.replace(/```\s*/, '').replace(/\s*```/, '')
      }

      console.log('清理后的响应:', cleanResponse)

      // 尝试解析JSON响应
      const jsonMatch = cleanResponse.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        const jsonStr = jsonMatch[0]
        console.log('提取的JSON字符串:', jsonStr)

        const parsed = JSON.parse(jsonStr)
        console.log('解析后的对象:', parsed)

        if (parsed.name && parsed.description && parsed.categoryId && parsed.icon) {
          const result = {
            name: parsed.name.trim(),
            description: parsed.description.trim(),
            categoryId: parseInt(parsed.categoryId),
            icon: parsed.icon.trim(),
          }
          console.log('最终结果:', result)
          return result
        } else {
          console.warn('JSON中缺少必要字段:', parsed)
        }
      } else {
        console.warn('未找到JSON格式的响应')
      }
    } catch (error) {
      console.warn('AI响应解析失败，使用默认值:', error)
    }

    // 如果解析失败，返回默认值
    console.log('使用默认值')
    return this.getDefaultWebsiteData(url, {}, categories)
  }

  /**
   * 解析博客分类AI响应
   * @param aiResponse AI的响应文本
   * @param categoryName 分类名称
   * @returns 解析后的结果
   */
  private static parseBlogCategoryResponse(
    aiResponse: string,
    categoryName: string,
  ): AIBlogCategoryGenerationResult {
    console.log('AI原始响应:', aiResponse)

    try {
      // 清理响应文本，移除可能的markdown格式
      let cleanResponse = aiResponse.trim()

      // 如果响应被markdown代码块包围，提取内容
      if (cleanResponse.startsWith('```json')) {
        cleanResponse = cleanResponse.replace(/```json\s*/, '').replace(/\s*```/, '')
      } else if (cleanResponse.startsWith('```')) {
        cleanResponse = cleanResponse.replace(/```\s*/, '').replace(/\s*```/, '')
      }

      console.log('清理后的响应:', cleanResponse)

      // 尝试解析JSON响应
      const jsonMatch = cleanResponse.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        const jsonStr = jsonMatch[0]
        console.log('提取的JSON字符串:', jsonStr)

        const parsed = JSON.parse(jsonStr)
        console.log('解析后的对象:', parsed)

        if (parsed.description && parsed.color) {
          const result = {
            description: parsed.description.trim(),
            color: parsed.color.trim(),
            icon: parsed.icon ? parsed.icon.trim() : undefined,
          }
          console.log('最终结果:', result)
          return result
        } else {
          console.warn('JSON中缺少必要字段:', parsed)
        }
      } else {
        console.warn('未找到JSON格式的响应')
      }
    } catch (error) {
      console.warn('AI响应解析失败，使用默认值:', error)
    }

    // 如果解析失败，返回默认值
    console.log('使用默认值')
    return this.getDefaultBlogCategoryData(categoryName)
  }

  /**
   * 获取默认网站分类数据
   * @param categoryName 分类名称
   * @param existingCategories 已存在的分类列表
   * @returns 默认的分类数据
   */
  private static getDefaultWebsiteCategoryData(
    categoryName: string,
    existingCategories: Array<{ name: string; color: string }> = [],
  ): AICategoryGenerationResult {
    // 根据分类名称提供一些智能的默认值
    const defaultColors: { [key: string]: string } = {
      开发工具: '#409EFF',
      学习资源: '#67C23A',
      娱乐休闲: '#E6A23C',
      设计创意: '#F56C6C',
      新闻资讯: '#909399',
      购物消费: '#FF6B6B',
      社交网络: '#9C27B0',
      工具软件: '#2196F3',
    }

    const defaultDescriptions: { [key: string]: string } = {
      开发工具: '包含各种开发相关的工具和资源',
      学习资源: '提供学习和教育相关的网站和工具',
      娱乐休闲: '娱乐、游戏、休闲相关的网站',
      设计创意: '设计、创意、艺术相关的资源和工具',
      新闻资讯: '新闻、资讯、时事相关的网站',
      购物消费: '购物、消费、电商相关的网站',
      社交网络: '社交、沟通、社区相关的平台',
      工具软件: '实用工具和软件资源',
    }

    // 尝试匹配分类名称
    for (const [key, color] of Object.entries(defaultColors)) {
      if (categoryName.includes(key) || key.includes(categoryName)) {
        // 检查颜色是否已被使用
        const isColorUsed = existingCategories.some((cat) => cat.color === color)
        if (!isColorUsed) {
          return {
            description: defaultDescriptions[key] || `关于${categoryName}的网站集合`,
            color: color,
          }
        }
      }
    }

    // 如果没有匹配或颜色重复，选择一个未使用的颜色
    const usedColors = new Set(existingCategories.map((cat) => cat.color))
    const availableColors = Object.values(defaultColors).filter((color) => !usedColors.has(color))

    if (availableColors.length > 0) {
      return {
        description: `关于${categoryName}的网站集合`,
        color: availableColors[0],
      }
    }

    // 如果所有默认颜色都被使用，生成一个随机颜色
    const randomColor = this.generateRandomColor(usedColors)
    return {
      description: `关于${categoryName}的网站集合`,
      color: randomColor,
    }
  }

  /**
   * 生成随机颜色，避免与已有颜色重复
   * @param usedColors 已使用的颜色集合
   * @returns 随机颜色
   */
  private static generateRandomColor(usedColors: Set<string>): string {
    const colors = [
      '#FF6B6B',
      '#4ECDC4',
      '#45B7D1',
      '#96CEB4',
      '#FFEAA7',
      '#DDA0DD',
      '#98D8C8',
      '#F7DC6F',
      '#BB8FCE',
      '#85C1E9',
      '#F8C471',
      '#82E0AA',
      '#F1948A',
      '#85C1E9',
      '#D7BDE2',
      '#F9E79F',
      '#ABEBC6',
      '#D5A6BD',
      '#A9CCE3',
      '#FAD7A0',
    ]

    // 过滤掉已使用的颜色
    const availableColors = colors.filter((color) => !usedColors.has(color))

    if (availableColors.length > 0) {
      return availableColors[Math.floor(Math.random() * availableColors.length)]
    }

    // 如果所有颜色都被使用，生成一个完全随机的颜色
    const letters = '0123456789ABCDEF'
    let color = '#'
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * letters.length)]
    }
    return color
  }

  /**
   * 获取默认网站数据
   * @param url 网站地址
   * @param scrapedInfo 抓取的信息
   * @param categories 可用分类
   * @returns 默认的网站数据
   */
  private static getDefaultWebsiteData(
    url: string,
    scrapedInfo: { title?: string; description?: string; keywords?: string },
    categories: Array<{ id: number; name: string }>,
  ): AIWebsiteGenerationResult {
    // 从URL提取域名作为默认名称
    let defaultName = url
    try {
      const urlObj = new URL(url)
      defaultName = urlObj.hostname.replace('www.', '')
    } catch (e) {
      // 如果URL解析失败，使用原始URL
    }

    // 使用第一个可用分类
    const defaultCategoryId = categories.length > 0 ? categories[0].id : 1

    return {
      name: scrapedInfo.title || defaultName,
      description: scrapedInfo.description || `关于${defaultName}的网站`,
      categoryId: defaultCategoryId,
      icon: 'Link',
    }
  }

  /**
   * 获取默认博客分类数据
   * @param categoryName 分类名称
   * @returns 默认的分类数据
   */
  private static getDefaultBlogCategoryData(categoryName: string): AIBlogCategoryGenerationResult {
    // 根据分类名称提供一些智能的默认值
    const defaultColors: { [key: string]: string } = {
      技术分享: '#409EFF',
      学习笔记: '#67C23A',
      生活随笔: '#E6A23C',
      设计思考: '#F56C6C',
      行业资讯: '#909399',
      产品分析: '#FF6B6B',
      经验总结: '#9C27B0',
      工具推荐: '#2196F3',
    }

    const defaultDescriptions: { [key: string]: string } = {
      技术分享: '分享技术心得、开发经验和解决方案',
      学习笔记: '记录学习过程中的思考和总结',
      生活随笔: '记录生活感悟和日常思考',
      设计思考: '分享设计理念和创意想法',
      行业资讯: '关注行业动态和最新趋势',
      产品分析: '分析产品功能和用户体验',
      经验总结: '总结工作和生活中的经验教训',
      工具推荐: '推荐实用的工具和资源',
    }

    const defaultIcons: { [key: string]: string } = {
      技术分享: 'Monitor',
      学习笔记: 'Document',
      生活随笔: 'Edit',
      设计思考: 'Brush',
      行业资讯: 'News',
      产品分析: 'View',
      经验总结: 'Star',
      工具推荐: 'Tools',
    }

    // 尝试匹配分类名称
    for (const [key, color] of Object.entries(defaultColors)) {
      if (categoryName.includes(key) || key.includes(categoryName)) {
        return {
          description: defaultDescriptions[key] || `关于${categoryName}的文章集合`,
          color: color,
          icon: defaultIcons[key] || 'Document',
        }
      }
    }

    // 如果没有匹配，返回通用默认值
    return {
      description: `关于${categoryName}的文章集合`,
      color: '#409EFF',
      icon: 'Document',
    }
  }
}

/**
 * 便捷函数：生成网站分类描述和颜色
 * @param categoryName 分类名称
 * @returns 生成的分类描述和颜色
 */
export const generateCategoryWithOllama = async (
  categoryName: string,
  existingCategories: Array<{ name: string; color: string }> = [],
): Promise<AICategoryGenerationResult> => {
  return AIService.generateWebsiteCategory(categoryName, existingCategories)
}

export const generateWebsiteInfoWithOllama = async (
  url: string,
  scrapedInfo: {
    title?: string
    description?: string
    keywords?: string
    favicon?: string
    success?: boolean
    error?: string
  },
  categories: Array<{ id: number; name: string }>,
): Promise<AIWebsiteGenerationResult> => {
  return AIService.generateWebsiteInfo(url, scrapedInfo, categories)
}

/**
 * 便捷函数：生成博客分类描述、颜色和图标
 * @param categoryName 分类名称
 * @returns 生成的博客分类描述、颜色和图标
 */
export const generateBlogCategoryWithOllama = async (
  categoryName: string,
): Promise<AIBlogCategoryGenerationResult> => {
  return AIService.generateBlogCategory(categoryName)
}

// 博客文章相关的AI生成接口
export interface AIBlogGenerationResult {
  success: boolean
  content?: string
  error?: string
  provider: string
  model?: string
}

export interface AIBlogCategoryResponse {
  success: boolean
  categoryId?: number
  error?: string
  provider: string
  model?: string
}

export interface AIBlogTagsResponse {
  success: boolean
  tagIds?: number[]
  newTags?: string[]
  error?: string
  provider: string
  model?: string
}

/**
 * 博客文章AI生成服务
 */
export class BlogAIService {
  private static readonly OLLAMA_BASE_URL = 'http://localhost:11434'
  private static readonly MODEL_NAME = 'deepseek-r1:8b'

  /**
   * 生成文章摘要
   */
  static async generateSummary(content: string, title: string): Promise<AIBlogGenerationResult> {
    try {
      const prompt = this.buildSummaryPrompt(content, title)

      const response = await fetch(`${this.OLLAMA_BASE_URL}/api/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: this.MODEL_NAME,
          prompt: prompt,
          stream: false,
          options: { temperature: 0.7, top_p: 0.9, max_tokens: 200 },
        }),
      })

      if (!response.ok) {
        throw new Error(`Ollama API调用失败: ${response.status}`)
      }

      const data = await response.json()
      const summary = this.cleanAIResponse(data.response)

      return {
        success: true,
        content: summary,
        provider: 'ollama',
        model: this.MODEL_NAME,
      }
    } catch (error) {
      return {
        success: false,
        error: `AI生成失败: ${error}`,
        provider: 'ollama',
        model: this.MODEL_NAME,
      }
    }
  }

  /**
   * 生成文章标题
   */
  static async generateTitle(content: string): Promise<AIBlogGenerationResult> {
    try {
      const prompt = this.buildTitlePrompt(content)

      const response = await fetch(`${this.OLLAMA_BASE_URL}/api/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: this.MODEL_NAME,
          prompt: prompt,
          stream: false,
          options: { temperature: 0.7, top_p: 0.9, max_tokens: 100 },
        }),
      })

      if (!response.ok) {
        throw new Error(`Ollama API调用失败: ${response.status}`)
      }

      const data = await response.json()
      const title = this.cleanAIResponse(data.response)

      return {
        success: true,
        content: title,
        provider: 'ollama',
        model: this.MODEL_NAME,
      }
    } catch (error) {
      return {
        success: false,
        error: `AI生成失败: ${error}`,
        provider: 'ollama',
        model: this.MODEL_NAME,
      }
    }
  }

  /**
   * 生成分类建议
   */
  static async generateCategory(
    content: string,
    title: string,
    categories: Array<{ id: number; name: string }>,
  ): Promise<AIBlogCategoryResponse> {
    try {
      const prompt = this.buildCategoryPrompt(content, title, categories)

      const response = await fetch(`${this.OLLAMA_BASE_URL}/api/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: this.MODEL_NAME,
          prompt: prompt,
          stream: false,
          options: { temperature: 0.3, top_p: 0.9, max_tokens: 50 },
        }),
      })

      if (!response.ok) {
        throw new Error(`Ollama API调用失败: ${response.status}`)
      }

      const data = await response.json()
      const categoryId = this.extractCategoryId(data.response, categories)

      if (categoryId !== null) {
        return {
          success: true,
          categoryId,
          provider: 'ollama',
          model: this.MODEL_NAME,
        }
      } else {
        return {
          success: false,
          error: '无法解析分类ID',
          provider: 'ollama',
          model: this.MODEL_NAME,
        }
      }
    } catch (error) {
      return {
        success: false,
        error: `AI生成失败: ${error}`,
        provider: 'ollama',
        model: this.MODEL_NAME,
      }
    }
  }

  /**
   * 生成标签建议
   */
  static async generateTags(
    content: string,
    title: string,
    tags: Array<{ id: number; name: string }>,
  ): Promise<AIBlogTagsResponse> {
    try {
      const prompt = this.buildTagsPrompt(content, title, tags)

      const response = await fetch(`${this.OLLAMA_BASE_URL}/api/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: this.MODEL_NAME,
          prompt: prompt,
          stream: false,
          options: { temperature: 0.3, top_p: 0.9, max_tokens: 100 },
        }),
      })

      if (!response.ok) {
        throw new Error(`Ollama API调用失败: ${response.status}`)
      }

      const data = await response.json()
      const result = this.extractTagIds(data.response, tags)

      if (result.tagIds.length > 0 || result.newTags.length > 0) {
        return {
          success: true,
          tagIds: result.tagIds,
          newTags: result.newTags,
          provider: 'ollama',
          model: this.MODEL_NAME,
        }
      } else {
        return {
          success: false,
          error: '无法解析标签ID',
          provider: 'ollama',
          model: this.MODEL_NAME,
        }
      }
    } catch (error) {
      return {
        success: false,
        error: `AI生成失败: ${error}`,
        provider: 'ollama',
        model: this.MODEL_NAME,
      }
    }
  }

  /**
   * 统一生成博客所有信息（标题、摘要、分类、标签）
   */
  static async generateAllInfo(
    content: string,
    categories: Array<{ id: number; name: string }>,
    tags: Array<{ id: number; name: string }>,
  ): Promise<AIBlogAllGenerationResult> {
    try {
      const prompt = this.buildAllInfoPrompt(content, categories, tags)

      const response = await fetch(`${this.OLLAMA_BASE_URL}/api/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: this.MODEL_NAME,
          prompt: prompt,
          stream: false,
          options: { temperature: 0.7, top_p: 0.9, max_tokens: 500 },
        }),
      })

      if (!response.ok) {
        throw new Error(`Ollama API调用失败: ${response.status}`)
      }

      const data = await response.json()
      const aiResponse = this.cleanAIResponse(data.response)

      return this.parseAllInfoResponse(aiResponse, categories, tags)
    } catch (error) {
      console.error('AI统一生成失败:', error)
      // 返回默认值
      return {
        title: '未生成标题',
        summary: '未生成摘要',
        categoryId: null,
        tagIds: [],
        newTags: [],
      }
    }
  }

  // 私有方法
  private static buildSummaryPrompt(content: string, title: string): string {
    return `请为以下文章生成摘要，要求：
1. 智能提取文章的核心要点，生成简洁的摘要
2. 文章标题：${title}
3. 摘要长度：不超过100字
4. 语言：中文
5. 格式：纯文本，不要包含"摘要："等前缀

文章内容：
${content.substring(0, 2000)}${content.length > 2000 ? '...' : ''}

请生成摘要：`
  }

  private static buildTitlePrompt(content: string): string {
    return `请为以下文章生成一个合适的标题，要求：
1. 标题要简洁明了，突出文章主题
2. 标题长度：不超过20字
3. 语言：中文
4. 格式：纯文本，不要包含"标题："等前缀

文章内容：
${content.substring(0, 2000)}${content.length > 2000 ? '...' : ''}

请生成标题：`
  }

  private static buildCategoryPrompt(
    content: string,
    title: string,
    categories: Array<{ id: number; name: string }>,
  ): string {
    const categoryList = categories.map((c) => `${c.id}:${c.name}`).join('、')
    return `请为以下文章推荐最合适的分类，要求：
1. 从以下分类中选择最合适的一个：${categoryList}
2. 只返回分类ID数字，不要其他内容
3. 根据文章内容和标题判断分类

文章标题：${title}
文章内容：
${content.substring(0, 1500)}${content.length > 1500 ? '...' : ''}

请选择分类ID：`
  }

  private static buildTagsPrompt(
    content: string,
    title: string,
    tags: Array<{ id: number; name: string }>,
  ): string {
    const tagList = tags.map((t) => `${t.id}:${t.name}`).join('、')
    return `请为以下文章推荐合适的标签，要求：
1. 从以下现有标签中选择一个或者多个最合适的：${tagList}
2. 如果现有标签不够合适，可以推荐1-3个新的标签名称
3. 返回格式：现有标签ID用逗号分隔，新标签用"新标签:"前缀，例如："1,3,新标签:人工智能,新标签:深度学习"
4. 根据文章内容和标题判断标签

文章标题：${title}
文章内容：
${content.substring(0, 1500)}${content.length > 1500 ? '...' : ''}

请推荐标签：`
  }

  private static buildAllInfoPrompt(
    content: string,
    categories: Array<{ id: number; name: string }>,
    tags: Array<{ id: number; name: string }>,
  ): string {
    const categoryList = categories.map((c) => `${c.id}:${c.name}`).join('、')
    const tagList = tags.map((t) => `${t.id}:${t.name}`).join('、')

    return `请为以下文章生成完整的信息，包括标题、摘要、分类和标签。请严格按照以下JSON格式返回，不要包含其他内容：

{
  "title": "文章标题（不超过20字）",
  "summary": "文章摘要（不超过100字）",
  "categoryId": 分类ID数字,
  "tagIds": [标签ID数组],
  "newTags": ["新标签1", "新标签2"]
}

要求：
1. 标题要简洁明了，突出文章主题，不超过20字
2. 摘要要智能提取文章核心要点，不超过100字
3. 分类从以下选项中选择最合适的一个：${categoryList}
4. 标签从以下选项中选择最合适的1-3个：${tagList}
5. 如果现有标签不够合适，可以推荐1-3个新标签
6. 所有内容都必须是中文

文章内容：
${content.substring(0, 2000)}${content.length > 2000 ? '...' : ''}

请生成完整信息：`
  }

  private static parseAllInfoResponse(
    response: string,
    categories: Array<{ id: number; name: string }>,
    tags: Array<{ id: number; name: string }>,
  ): AIBlogAllGenerationResult {
    try {
      // 尝试解析JSON格式
      const jsonMatch = response.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        const jsonStr = jsonMatch[0]
        const parsed = JSON.parse(jsonStr)

        return {
          title: parsed.title || '未生成标题',
          summary: parsed.summary || '未生成摘要',
          categoryId: this.validateCategoryId(parsed.categoryId, categories),
          tagIds: this.validateTagIds(parsed.tagIds || [], tags),
          newTags: Array.isArray(parsed.newTags) ? parsed.newTags : [],
        }
      }

      // 如果JSON解析失败，尝试从文本中提取信息
      return this.extractInfoFromText(response, categories, tags)
    } catch (error) {
      console.error('解析AI响应失败:', error)
      return {
        title: '未生成标题',
        summary: '未生成摘要',
        categoryId: null,
        tagIds: [],
        newTags: [],
      }
    }
  }

  private static validateCategoryId(
    categoryId: any,
    categories: Array<{ id: number; name: string }>,
  ): number | null {
    if (typeof categoryId === 'number' && categories.some((c) => c.id === categoryId)) {
      return categoryId
    }
    return null
  }

  private static validateTagIds(
    tagIds: any[],
    tags: Array<{ id: number; name: string }>,
  ): number[] {
    if (!Array.isArray(tagIds)) return []
    return tagIds.filter((id) => typeof id === 'number' && tags.some((t) => t.id === id))
  }

  private static extractInfoFromText(
    text: string,
    categories: Array<{ id: number; name: string }>,
    tags: Array<{ id: number; name: string }>,
  ): AIBlogAllGenerationResult {
    const lines = text
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line)

    let title = '未生成标题'
    let summary = '未生成摘要'
    let categoryId: number | null = null
    let tagIds: number[] = []
    let newTags: string[] = []

    for (const line of lines) {
      // 提取标题
      if (line.includes('标题') || line.includes('title')) {
        const titleMatch = line.match(/[：:]\s*(.+)/)
        if (titleMatch) {
          title = titleMatch[1].trim()
        }
      }

      // 提取摘要
      if (line.includes('摘要') || line.includes('summary')) {
        const summaryMatch = line.match(/[：:]\s*(.+)/)
        if (summaryMatch) {
          summary = summaryMatch[1].trim()
        }
      }

      // 提取分类
      if (line.includes('分类') || line.includes('category')) {
        const categoryMatch = line.match(/\d+/)
        if (categoryMatch) {
          const id = parseInt(categoryMatch[0])
          if (categories.some((c) => c.id === id)) {
            categoryId = id
          }
        }
      }

      // 提取标签
      if (line.includes('标签') || line.includes('tag')) {
        const tagResult = this.extractTagIds(line, tags)
        tagIds = tagResult.tagIds
        newTags = tagResult.newTags
      }
    }

    return {
      title,
      summary,
      categoryId,
      tagIds,
      newTags,
    }
  }

  private static cleanAIResponse(response: string): string {
    return response.replace(/<think>[\s\S]*?<\/think>/gi, '').trim()
  }

  private static extractCategoryId(
    response: string,
    categories: Array<{ id: number; name: string }>,
  ): number | null {
    const cleanResponse = response.trim()
    const categoryId = parseInt(cleanResponse)

    if (!isNaN(categoryId) && categories.some((c) => c.id === categoryId)) {
      return categoryId
    }

    const numberMatch = cleanResponse.match(/\d+/)
    if (numberMatch) {
      const id = parseInt(numberMatch[0])
      if (categories.some((c) => c.id === id)) {
        return id
      }
    }

    return null
  }

  private static extractTagIds(
    response: string,
    tags: Array<{ id: number; name: string }>,
  ): { tagIds: number[]; newTags: string[] } {
    const cleanResponse = response.trim()
    const tagIds: number[] = []
    const newTags: string[] = []

    const tokens = cleanResponse.split(/[,，\s]+/)
    for (const raw of tokens) {
      const token = raw.trim()

      if (token.startsWith('新标签:')) {
        const newTagName = token.replace('新标签:', '').trim()
        if (newTagName) newTags.push(newTagName)
        continue
      }

      if (/^\d+$/.test(token)) {
        const id = Number(token)
        if (tags.some((t) => t.id === id)) tagIds.push(id)
      }
    }

    return { tagIds, newTags }
  }
}

// 便捷函数
export const generateBlogSummary = async (
  content: string,
  title: string,
): Promise<AIBlogGenerationResult> => {
  return BlogAIService.generateSummary(content, title)
}

export const generateBlogTitle = async (content: string): Promise<AIBlogGenerationResult> => {
  return BlogAIService.generateTitle(content)
}

export const generateBlogCategory = async (
  content: string,
  title: string,
  categories: Array<{ id: number; name: string }>,
): Promise<AIBlogCategoryResponse> => {
  return BlogAIService.generateCategory(content, title, categories)
}

export const generateBlogTags = async (
  content: string,
  title: string,
  tags: Array<{ id: number; name: string }>,
): Promise<AIBlogTagsResponse> => {
  return BlogAIService.generateTags(content, title, tags)
}

// 新增：统一生成博客所有信息的接口
export interface AIBlogAllGenerationResult {
  title: string
  summary: string
  categoryId: number | null
  tagIds: number[]
  newTags: string[]
}

export const generateBlogAllInfo = async (
  content: string,
  categories: Array<{ id: number; name: string }>,
  tags: Array<{ id: number; name: string }>,
): Promise<AIBlogAllGenerationResult> => {
  return BlogAIService.generateAllInfo(content, categories, tags)
}
