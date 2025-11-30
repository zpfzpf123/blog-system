// 代码高亮工具类
export class CodeHighlighter {
  // 高亮HTML代码
  static highlightHTML(code: string): string {
    return code
      .replace(/(&lt;\/?)(\w+)(.*?)(&gt;)/g, '<span class="html-tag">$1$2</span><span class="html-attr">$3</span><span class="html-tag">$4</span>')
      .replace(/(\w+)=/g, '<span class="html-attr-name">$1</span>=')
      .replace(/="([^"]*)"/g, '="<span class="html-attr-value">$1</span>"')
  }

  // 高亮CSS代码
  static highlightCSS(code: string): string {
    return code
      .replace(/([\w-]+)\s*:/g, '<span class="css-property">$1</span>:')
      .replace(/:\s*([^;]+);/g, ': <span class="css-value">$1</span>;')
      .replace(/(@[\w-]+)/g, '<span class="css-at-rule">$1</span>')
      .replace(/(\/\*.*?\*\/)/gs, '<span class="css-comment">$1</span>')
      .replace(/(\{|\})/g, '<span class="css-bracket">$1</span>')
  }

  // 高亮JavaScript代码
  static highlightJS(code: string): string {
    const keywords = ['function', 'var', 'let', 'const', 'if', 'else', 'for', 'while', 'return', 'class', 'extends', 'import', 'export', 'default']
    let highlighted = code

    // 高亮关键字
    keywords.forEach(keyword => {
      const regex = new RegExp(`\\b${keyword}\\b`, 'g')
      highlighted = highlighted.replace(regex, `<span class="js-keyword">${keyword}</span>`)
    })

    // 高亮字符串
    highlighted = highlighted
      .replace(/'([^']*)'/g, '<span class="js-string">\'$1\'</span>')
      .replace(/"([^"]*)"/g, '<span class="js-string">"$1"</span>')
      .replace(/`([^`]*)`/g, '<span class="js-template">`$1`</span>')

    // 高亮注释
    highlighted = highlighted
      .replace(/(\/\/.*$)/gm, '<span class="js-comment">$1</span>')
      .replace(/(\/\*.*?\*\/)/gs, '<span class="js-comment">$1</span>')

    // 高亮数字
    highlighted = highlighted.replace(/\b(\d+\.?\d*)\b/g, '<span class="js-number">$1</span>')

    return highlighted
  }

  // 根据语言类型高亮代码
  static highlight(code: string, language: string): string {
    switch (language.toLowerCase()) {
      case 'html':
        return this.highlightHTML(code)
      case 'css':
        return this.highlightCSS(code)
      case 'javascript':
      case 'js':
        return this.highlightJS(code)
      default:
        return code
    }
  }

  // 格式化代码（添加缩进）
  static formatCode(code: string, language: string): string {
    // 简单的代码格式化
    let formatted = code.trim()
    
    if (language === 'css') {
      formatted = formatted
        .replace(/\{/g, ' {\n  ')
        .replace(/\}/g, '\n}\n')
        .replace(/;/g, ';\n  ')
        .replace(/\n\s*\n/g, '\n')
    }
    
    return formatted
  }

  // 获取代码行数
  static getLineCount(code: string): number {
    return code.split('\n').length
  }

  // 添加行号
  static addLineNumbers(code: string): string {
    const lines = code.split('\n')
    return lines
      .map((line, index) => `<span class="line-number">${index + 1}</span>${line}`)
      .join('\n')
  }
}

export default CodeHighlighter