import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const filePath = resolve(__dirname, '../views/WebsiteCollection.vue')
const sfcSource = readFileSync(filePath, 'utf8')
const templateStartTag = '<template>'
const templateStartIndex = sfcSource.indexOf(templateStartTag)
const templateEndIndex = sfcSource.lastIndexOf('</template>')
const templateSource =
  templateStartIndex >= 0 && templateEndIndex > templateStartIndex
    ? sfcSource.slice(templateStartIndex + templateStartTag.length, templateEndIndex)
    : ''
const scopedStyleStartTag = '<style scoped>'
const scopedStyleStartIndex = sfcSource.indexOf(scopedStyleStartTag)
const scopedStyleEndIndex = sfcSource.indexOf('</style>', scopedStyleStartIndex)
const scopedStyleSource =
  scopedStyleStartIndex >= 0 && scopedStyleEndIndex > scopedStyleStartIndex
    ? sfcSource.slice(scopedStyleStartIndex + scopedStyleStartTag.length, scopedStyleEndIndex)
    : ''

describe('WebsiteCollection layout structure', () => {
  it('keeps header, search and category filter inside one compact top panel', () => {
    const topPanelStart = templateSource.indexOf('<div class="collection-top-panel">')
    const topPanelEnd = templateSource.indexOf('<!-- 顶部控制区结束 -->', topPanelStart)
    const pageHeaderStart = templateSource.indexOf('<div class="page-header">', topPanelStart)
    const searchSectionStart = templateSource.indexOf('<div class="search-section', topPanelStart)
    const categoryFilterStart = templateSource.indexOf('<div class="category-filter">', topPanelStart)

    expect(topPanelStart).toBeGreaterThan(-1)
    expect(topPanelEnd).toBeGreaterThan(topPanelStart)
    expect(pageHeaderStart).toBeGreaterThan(topPanelStart)
    expect(categoryFilterStart).toBeGreaterThan(topPanelStart)
    expect(searchSectionStart).toBeGreaterThan(topPanelStart)

    expect(pageHeaderStart).toBeLessThan(topPanelEnd)
    expect(categoryFilterStart).toBeLessThan(topPanelEnd)
    expect(searchSectionStart).toBeLessThan(topPanelEnd)
  })

  it('keeps top-level grid tracks and children shrinkable to avoid right-side clipping', () => {
    expect(scopedStyleSource).toMatch(
      /\.website-collection\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\);/,
    )
    expect(scopedStyleSource).toMatch(
      /\.collection-top-panel\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\);/,
    )
    expect(scopedStyleSource).toMatch(
      /\.category-filter\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\);/,
    )
    expect(scopedStyleSource).toMatch(/\.website-collection\s*>\s*\*\s*\{[\s\S]*min-width:\s*0;/)
  })

  it('wraps category chips to multiple lines without horizontal scrolling', () => {
    expect(scopedStyleSource).toMatch(
      /\.categories-container\s*:deep\(\.el-radio-group\)\s*\{[\s\S]*flex-wrap:\s*wrap;/,
    )
    expect(scopedStyleSource).not.toMatch(
      /\.categories-container\s*:deep\(\.el-radio-group\)\s*\{[\s\S]*overflow-x:\s*auto;/,
    )
  })
})
