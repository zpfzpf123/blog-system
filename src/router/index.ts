/*
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-11-30 19:34:19
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-11-30 20:17:04
 * @FilePath: \ai博客\blog\src\router\index.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/Home.vue'
import BlogDetail from '../views/BlogDetail.vue'
import PostCreate from '../views/PostCreate.vue'
import PostEdit from '../views/PostEdit.vue'
import AIChat from '../views/AIChat.vue'
import GitUserManager from '../views/GitUserManager.vue'
import projectManager from '../views/ProjectManager.vue'
import ProjectDetail from '../views/ProjectDetail.vue'
import CommitDetail from '../views/CommitDetail.vue'
import cssAnimations from '../views/CssAnimationDemo.vue'
import WebsiteCollection from '../views/WebsiteCollection.vue'
import DevTools from '../views/DevTools.vue'
import PageTemplates from '../views/PageTemplates.vue'
import TemplatePreview from '../views/TemplatePreview.vue'

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
      path: '/ai-chat',
      name: 'AIChat',
      component: AIChat,
    },

    {
      path: '/websites',
      name: 'WebsiteCollection',
      component: WebsiteCollection,
    },
    {
      path: '/git-users',
      name: 'GitUserManager',
      component: GitUserManager,
    },
    {
      path: '/project-manager',
      name: 'ProjectManager',
      component: projectManager,
    },
    {
      path: '/project-manager/:id',
      name: 'ProjectDetail',
      component: ProjectDetail,
      props: true,
    },
    {
      path: '/project-manager/:id/commit',
      name: 'CommitDetail',
      component: CommitDetail,
      props: true,
    },
    {
      path: '/css-animations',
      name: 'cssAnimations',
      component: cssAnimations,
    },
    {
      path: '/dev-tools',
      name: 'DevTools',
      component: DevTools,
    },
    {
      path: '/page-templates',
      name: 'PageTemplates',
      component: PageTemplates,
    },
    {
      path: '/template-preview/:id',
      name: 'TemplatePreview',
      component: TemplatePreview,
      props: true,
    },
  ],
})

export default router
