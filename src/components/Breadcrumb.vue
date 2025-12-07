<script setup lang="ts">
import { computed, type Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElBreadcrumb, ElBreadcrumbItem } from 'element-plus'
import { House, Document, Setting, Edit, Plus, Folder } from '@element-plus/icons-vue'

// 定义组件名称
defineOptions({
  name: 'BreadcrumbNav',
})

const route = useRoute()
const router = useRouter()

// 定义面包屑项类型
interface BreadcrumbItem {
  name: string
  path?: string
  icon?: Component
}

// 根据当前路由生成面包屑
const breadcrumbItems = computed(() => {
  const items: BreadcrumbItem[] = []

  // 添加首页
  items.push({
    name: '首页',
    path: '/',
    icon: House,
  })

  // 根据路由名称添加对应的面包屑
  switch (route.name) {
    case 'BlogDetail':
      items.push({ name: '博客详情', path: undefined, icon: Document })
      break
    case 'PostCreate':
      items.push({ name: '创建文章', path: undefined, icon: Plus })
      break
    case 'PostEdit':
      items.push({ name: '编辑文章', path: undefined, icon: Edit })
      break
  }

  return items
})

// 处理面包屑点击
const handleBreadcrumbClick = (item: BreadcrumbItem) => {
  if (item.path) {
    router.push(item.path)
  }
}
</script>

<template>
  <div class="breadcrumb-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item
        v-for="(item, index) in breadcrumbItems"
        :key="index"
        :class="{ clickable: item.path, current: !item.path }"
        @click="handleBreadcrumbClick(item)"
      >
        <div class="breadcrumb-content">
          <el-icon v-if="item.icon" class="breadcrumb-icon">
            <component :is="item.icon" />
          </el-icon>
          <span class="breadcrumb-text">{{ item.name }}</span>
        </div>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<style scoped>
.breadcrumb-container {
  padding: 16px 24px;
  background: linear-gradient(90deg, #f8fafc 60%, #e0e7ef 100%);
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(102, 126, 234, 0.1);
  margin-bottom: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  position: relative;
  overflow: visible;
  animation: fadeInDown 0.7s;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.breadcrumb-container:hover {
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.18);
  transform: translateY(-2px) scale(1.01);
}

.breadcrumb-container :deep(.el-breadcrumb) {
  font-size: 15px;
  line-height: 1.5;
}

.breadcrumb-container :deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

.breadcrumb-content {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 7px 14px;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  background: none;
}

.breadcrumb-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, #e0e7ef33, transparent);
  transition: left 0.4s ease;
}

.breadcrumb-content:hover::before {
  left: 100%;
}

.breadcrumb-icon {
  font-size: 18px;
  color: #667eea;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 2px 8px #a3bffa33);
  animation: iconGlow 2.5s infinite;
}

@keyframes iconGlow {
  0%,
  100% {
    filter: drop-shadow(0 2px 8px #a3bffa33);
  }
  50% {
    filter: drop-shadow(0 0px 16px #a3bffa99);
  }
}

.breadcrumb-text {
  font-weight: 500;
  color: #606266;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
}

.clickable {
  cursor: pointer;
}

.clickable .breadcrumb-content:hover {
  background: rgba(102, 126, 234, 0.08);
  transform: translateY(-1px) scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.12);
}

.clickable .breadcrumb-icon {
  color: #667eea;
}

.clickable .breadcrumb-icon:hover {
  transform: rotate(12deg) scale(1.2);
  color: #764ba2;
}

.clickable .breadcrumb-text:hover {
  color: #667eea;
  font-weight: 600;
}

.current .breadcrumb-content {
  background: linear-gradient(135deg, #e0e7ef 60%, #f8fafc 100%);
  border: 1px solid #e0e7ef;
}

.current .breadcrumb-icon {
  color: #764ba2;
  animation: pulse 2s ease-in-out infinite;
}

.current .breadcrumb-text {
  color: #303133;
  font-weight: 600;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.breadcrumb-container :deep(.el-breadcrumb__separator) {
  color: #c0c4cc;
  margin: 0 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  animation: separatorGlow 3s ease-in-out infinite;
}

@keyframes separatorGlow {
  0%,
  100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .breadcrumb-container {
    padding: 10px 8px;
    margin-bottom: 12px;
    border-radius: 8px;
  }

  .breadcrumb-content {
    padding: 4px 8px;
  }

  .breadcrumb-icon {
    font-size: 15px;
  }
}
</style>
