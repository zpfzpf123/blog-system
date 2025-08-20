# CSS动效代码折叠功能实现总结

## 功能概述

成功为CSS动效展示页面添加了代码折叠和展开功能，所有代码块默认设置为折叠状态，用户可以根据需要展开查看完整代码。

## 新增功能特性

### 1. 代码折叠/展开控制

- **默认状态**: 所有代码块初始状态为折叠
- **展开按钮**: 显示"展开"文字，点击后展开代码
- **折叠按钮**: 展开后显示"折叠"文字，点击后折叠代码
- **平滑动画**: 使用CSS过渡效果实现平滑的折叠/展开动画

### 2. 用户界面优化

- **双按钮布局**: 折叠/展开按钮 + 复制按钮
- **视觉区分**: 折叠按钮使用灰色，复制按钮使用蓝色
- **状态指示**: 按钮文字动态变化，清晰显示当前状态

### 3. 交互体验

- **一键操作**: 点击按钮即可切换代码显示状态
- **即时反馈**: 按钮状态立即更新
- **保持状态**: 每个动效的折叠状态独立管理

## 技术实现

### 1. 数据结构更新

```typescript
// 为每个动画对象添加 isFolded 属性
{
  id: 'fade-in',
  name: '淡入效果',
  description: '...',
  className: 'fade-in-demo',
  cssCode: '...',
  copied: false,
  isFolded: true,  // 新增：默认折叠
}
```

### 2. 新增功能函数

```typescript
// 代码折叠/展开功能
const toggleCode = (animationId: string) => {
  const allAnimations = [
    ...basicAnimations.value,
    ...hoverAnimations.value,
    ...loadingAnimations.value,
    ...transform3DAnimations.value,
  ]
  const animation = allAnimations.find((a) => a.id === animationId)
  if (animation) {
    animation.isFolded = !animation.isFolded
  }
}
```

### 3. 模板更新

```vue
<div class="code-controls">
  <button @click="toggleCode(animation.id)" class="fold-btn">
    <span v-if="animation.isFolded">展开</span>
    <span v-else>折叠</span>
  </button>
  <button @click="copyCode(animation.cssCode)" class="copy-btn">
    <!-- 复制按钮内容 -->
  </button>
</div>
<div class="code-content" :class="{ folded: animation.isFolded }">
  <pre><code>{{ animation.cssCode }}</code></pre>
</div>
```

### 4. 样式实现

```css
.code-content {
  max-height: 300px;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.code-content.folded {
  max-height: 0;
}

.code-controls {
  display: flex;
  gap: 8px;
}

.fold-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 5px 12px;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s ease;
}
```

## 功能分类

### 1. 基础动画 (3个)

- 淡入效果 - 默认折叠
- 向上滑入 - 默认折叠
- 弹跳效果 - 默认折叠

### 2. 悬停效果 (3个)

- 缩放悬停 - 默认折叠
- 发光悬停 - 默认折叠
- 旋转悬停 - 默认折叠

### 3. 加载动画 (3个)

- 旋转加载器 - 默认折叠
- 脉冲效果 - 默认折叠
- 波浪加载 - 默认折叠

### 4. 3D变换 (3个)

- 翻转卡片 - 默认折叠
- 立方体旋转 - 默认折叠
- 3D倾斜 - 默认折叠

## 使用方法

### 1. 查看动效

- 页面加载后所有动效自动播放
- 代码块默认处于折叠状态

### 2. 展开代码

- 点击"展开"按钮查看完整CSS代码
- 代码区域平滑展开显示

### 3. 折叠代码

- 点击"折叠"按钮隐藏代码
- 代码区域平滑折叠隐藏

### 4. 复制代码

- 点击"复制"按钮复制CSS代码
- 复制成功后显示"已复制!"提示

## 用户体验优势

### 1. 页面整洁

- 默认折叠状态保持页面简洁
- 减少视觉干扰，突出动效展示

### 2. 按需查看

- 用户可根据需要选择查看代码
- 避免信息过载，提升浏览体验

### 3. 交互友好

- 按钮状态清晰，操作直观
- 平滑动画效果，视觉体验良好

### 4. 功能完整

- 保留原有的代码复制功能
- 新增折叠功能，功能更加完善

## 技术特点

### 1. 响应式设计

- 支持移动端和桌面端
- 按钮布局自适应不同屏幕尺寸

### 2. 性能优化

- 使用CSS过渡动画，性能优秀
- 状态管理简洁，内存占用小

### 3. 代码质量

- TypeScript类型安全
- Vue 3 Composition API
- 组件化设计，易于维护

## 总结

代码折叠功能的成功实现，显著提升了CSS动效展示页面的用户体验。通过默认折叠状态和直观的展开/折叠控制，用户可以在保持页面整洁的同时，按需查看详细的CSS代码。该功能与现有的动效展示和代码复制功能完美结合，为用户提供了一个功能完整、体验优秀的CSS动效学习平台。
