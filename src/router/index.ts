import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/Home.vue'
import BlogDetail from '../views/BlogDetail.vue'
import Admin from '../views/Admin.vue'
import PostManager from '../views/PostManager.vue'
import PostCreate from '../views/PostCreate.vue'
import PostEdit from '../views/PostEdit.vue'
import Categories from '../views/Categories.vue'
import AIChat from '../views/AIChat.vue'

import WebsiteCollection from '../views/WebsiteCollection.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/blog/:id',
      name: 'BlogDetail',
      component: BlogDetail,
      props: true,
    },
    {
      path: '/admin',
      name: 'Admin',
      component: Admin,
    },
    {
      path: '/admin/posts',
      name: 'PostManager',
      component: PostManager,
    },
    {
      path: '/admin/posts/create',
      name: 'PostCreate',
      component: PostCreate,
    },
    {
      path: '/admin/posts/edit/:id',
      name: 'PostEdit',
      component: PostEdit,
      props: true,
    },
    {
      path: '/categories',
      name: 'Categories',
      component: Categories,
    },
    {
      path: '/ai-chat',
      name: 'AIChat',
      component: AIChat,
    },

    {
      path: '/websites',
      name: 'WebsiteCollection',
      component: WebsiteCollection,
    },
  ],
})

export default router
