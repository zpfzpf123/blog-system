import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入全局样式（顺序很重要）
import '@/assets/styles/variables.css'  // CSS变量定义
import '@/assets/styles/animations.css' // 动画库
import '@/assets/styles/main.css'       // 主样式

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')