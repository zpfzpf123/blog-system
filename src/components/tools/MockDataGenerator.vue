<template>
  <div class="tool-content">
    <div class="settings">
      <div class="setting-item">
        <label>数据类型:</label>
        <select v-model="dataType" @change="generate">
          <option value="user">用户信息</option>
          <option value="product">商品数据</option>
          <option value="order">订单数据</option>
          <option value="article">文章数据</option>
          <option value="comment">评论数据</option>
          <option value="address">地址数据</option>
        </select>
      </div>
      <div class="setting-item">
        <label>数量:</label>
        <input v-model.number="count" type="number" min="1" max="100" @change="generate" />
      </div>
      <div class="setting-item">
        <label>格式:</label>
        <select v-model="format" @change="generate">
          <option value="json">JSON</option>
          <option value="array">JSON数组</option>
        </select>
      </div>
    </div>
    <button @click="generate" class="btn primary">生成数据</button>
    <div class="output-group">
      <label>生成结果:</label>
      <textarea v-model="output" readonly rows="15"></textarea>
      <button @click="copy" class="btn">复制</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const dataType = ref('user')
const count = ref(5)
const format = ref('array')
const output = ref('')

const names = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十', '郑十一', '王小明', '李小红', '陈大海']
const emails = ['qq.com', '163.com', 'gmail.com', 'outlook.com', '126.com']
const phones = ['138', '139', '150', '151', '186', '187', '188', '189']
const cities = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安', '南京', '重庆']
const products = ['iPhone 15', 'MacBook Pro', 'iPad Air', 'AirPods Pro', '小米14', '华为Mate60', 'ThinkPad X1', 'Surface Pro']
const titles = ['Vue3实战指南', 'React最佳实践', 'TypeScript入门', 'Node.js高级编程', '前端性能优化', '微服务架构设计']

const random = (min: number, max: number) => Math.floor(Math.random() * (max - min + 1)) + min
const randomItem = <T>(arr: T[]): T => arr[random(0, arr.length - 1)]
const randomId = () => Math.random().toString(36).slice(2, 10)
const randomDate = () => new Date(Date.now() - random(0, 365 * 24 * 60 * 60 * 1000)).toISOString().split('T')[0]

const generators: Record<string, () => object> = {
  user: () => ({
    id: randomId(),
    name: randomItem(names),
    email: `user${random(100, 999)}@${randomItem(emails)}`,
    phone: randomItem(phones) + String(random(10000000, 99999999)),
    age: random(18, 60),
    city: randomItem(cities),
    avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${randomId()}`,
    createdAt: randomDate()
  }),
  product: () => ({
    id: randomId(),
    name: randomItem(products),
    price: random(99, 9999),
    stock: random(0, 1000),
    category: randomItem(['电子产品', '服装', '食品', '家居', '图书']),
    rating: (random(30, 50) / 10).toFixed(1),
    sales: random(100, 10000),
    image: `https://picsum.photos/200/200?random=${random(1, 1000)}`
  }),
  order: () => ({
    orderId: 'ORD' + Date.now() + random(100, 999),
    userId: randomId(),
    products: Array.from({ length: random(1, 3) }, () => ({ name: randomItem(products), quantity: random(1, 5), price: random(99, 999) })),
    totalAmount: random(100, 5000),
    status: randomItem(['pending', 'paid', 'shipped', 'delivered', 'cancelled']),
    createdAt: randomDate()
  }),
  article: () => ({
    id: randomId(),
    title: randomItem(titles),
    author: randomItem(names),
    summary: '这是一篇关于技术的文章摘要，介绍了相关的核心概念和实践经验...',
    views: random(100, 10000),
    likes: random(10, 500),
    tags: Array.from({ length: random(2, 4) }, () => randomItem(['Vue', 'React', 'TypeScript', 'Node.js', 'CSS', 'JavaScript'])),
    publishedAt: randomDate()
  }),
  comment: () => ({
    id: randomId(),
    userId: randomId(),
    userName: randomItem(names),
    content: randomItem(['写得很好，学到了很多！', '感谢分享，非常实用', '期待更多内容', '讲解很清晰', '收藏了']),
    likes: random(0, 100),
    createdAt: randomDate()
  }),
  address: () => ({
    id: randomId(),
    name: randomItem(names),
    phone: randomItem(phones) + String(random(10000000, 99999999)),
    province: randomItem(cities),
    city: randomItem(['朝阳区', '海淀区', '浦东新区', '天河区', '福田区', '西湖区']),
    detail: `${randomItem(['幸福路', '和平街', '建设大道', '科技园路'])}${random(1, 200)}号`,
    isDefault: random(0, 1) === 1
  })
}

const generate = () => {
  const items = Array.from({ length: count.value }, () => generators[dataType.value]())
  output.value = format.value === 'array' 
    ? JSON.stringify(items, null, 2)
    : items.map(item => JSON.stringify(item, null, 2)).join('\n\n')
}

const copy = async () => { await navigator.clipboard.writeText(output.value) }

onMounted(generate)
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.settings { display: flex; flex-wrap: wrap; gap: 1rem; }
.setting-item { display: flex; flex-direction: column; gap: 0.25rem; }
.setting-item label { font-size: 0.85rem; color: #666; }
.setting-item select, .setting-item input { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; min-width: 120px; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-family: monospace; font-size: 0.85rem; resize: vertical; }
</style>
