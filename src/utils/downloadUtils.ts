// 下载工具类
export class DownloadUtils {
  // 下载文本文件
  static downloadText(content: string, filename: string, mimeType: string = 'text/plain'): void {
    const blob = new Blob([content], { type: mimeType })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  // 下载HTML文件
  static downloadHTML(htmlContent: string, cssContent: string, jsContent: string = '', filename: string = 'animation.html'): void {
    const fullHTML = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>动画演示</title>
    <style>
        body {
            margin: 0;
            padding: 20px;
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background: #f5f5f5;
        }
        
        ${cssContent}
    </style>
</head>
<body>
    ${htmlContent}
    
    ${jsContent ? `<script>${jsContent}</script>` : ''}
</body>
</html>`

    this.downloadText(fullHTML, filename, 'text/html')
  }

  // 下载CSS文件
  static downloadCSS(content: string, filename: string = 'styles.css'): void {
    this.downloadText(content, filename, 'text/css')
  }

  // 下载JavaScript文件
  static downloadJS(content: string, filename: string = 'script.js'): void {
    this.downloadText(content, filename, 'text/javascript')
  }

  // 下载JSON文件
  static downloadJSON(data: any, filename: string = 'data.json'): void {
    const jsonString = JSON.stringify(data, null, 2)
    this.downloadText(jsonString, filename, 'application/json')
  }

  // 复制到剪贴板
  static async copyToClipboard(text: string): Promise<boolean> {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text)
        return true
      } else {
        // 降级方案
        const textArea = document.createElement('textarea')
        textArea.value = text
        textArea.style.position = 'fixed'
        textArea.style.left = '-999999px'
        textArea.style.top = '-999999px'
        document.body.appendChild(textArea)
        textArea.focus()
        textArea.select()
        
        const result = document.execCommand('copy')
        document.body.removeChild(textArea)
        return result
      }
    } catch (error) {
      console.error('复制失败:', error)
      return false
    }
  }

  // 生成文件名（基于当前时间）
  static generateFilename(prefix: string = 'file', extension: string = 'txt'): string {
    const now = new Date()
    const timestamp = now.toISOString().replace(/[:.]/g, '-').slice(0, -5)
    return `${prefix}_${timestamp}.${extension}`
  }

  // 格式化文件大小
  static formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes'
    
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  // 验证文件类型
  static isValidFileType(filename: string, allowedTypes: string[]): boolean {
    const extension = filename.split('.').pop()?.toLowerCase()
    return extension ? allowedTypes.includes(extension) : false
  }

  // 创建下载链接
  static createDownloadLink(content: string, filename: string, mimeType: string = 'text/plain'): string {
    const blob = new Blob([content], { type: mimeType })
    return URL.createObjectURL(blob)
  }

  // 批量下载文件（打包成ZIP）
  static async downloadZip(files: Array<{name: string, content: string}>, zipName: string = 'files.zip'): Promise<void> {
    // 这里需要引入JSZip库来实现ZIP功能
    // 由于没有引入JSZip，这里提供一个简化版本
    console.warn('ZIP下载功能需要JSZip库支持')
    
    // 简化版：逐个下载文件
    files.forEach((file, index) => {
      setTimeout(() => {
        this.downloadText(file.content, file.name)
      }, index * 100) // 延迟下载避免浏览器阻止
    })
  }
}

export default DownloadUtils