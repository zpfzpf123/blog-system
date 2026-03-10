import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const homeViewPath = resolve(__dirname, '../views/Home.vue')
const websiteCollectionPath = resolve(__dirname, '../views/WebsiteCollection.vue')
const viteConfigPath = resolve(__dirname, '../../vite.config.ts')

const homeViewSource = readFileSync(homeViewPath, 'utf8')
const websiteCollectionSource = readFileSync(websiteCollectionPath, 'utf8')
const viteConfigSource = readFileSync(viteConfigPath, 'utf8')

describe('Navigation runtime guards', () => {
  it('imports CollectionTag icon when Home template uses CollectionTag', () => {
    expect(homeViewSource).toMatch(/<CollectionTag\s*\/>/)
    expect(homeViewSource).toMatch(
      /import\s*\{[\s\S]*\bCollectionTag\b[\s\S]*\}\s*from\s*'@element-plus\/icons-vue'/,
    )
  })

  it('cleans manual input timer on WebsiteCollection unmount', () => {
    expect(websiteCollectionSource).toMatch(
      /import\s*\{[\s\S]*\bonUnmounted\b[\s\S]*\}\s*from\s*'vue'/,
    )
    expect(websiteCollectionSource).toMatch(
      /onUnmounted\s*\(\s*\(\)\s*=>\s*\{[\s\S]*if\s*\(manualInputTimer\)[\s\S]*clearTimeout\(manualInputTimer\)/,
    )
  })

  it('uses explicit host and hmr settings in vite dev server config', () => {
    expect(viteConfigSource).toMatch(/server\s*:\s*\{[\s\S]*host\s*:\s*(true|'0\.0\.0\.0'|"0\.0\.0\.0")/)
    expect(viteConfigSource).toMatch(/server\s*:\s*\{[\s\S]*hmr\s*:\s*\{/)
  })
})
