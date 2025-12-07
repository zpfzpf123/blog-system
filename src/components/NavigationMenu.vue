<template>
  <div class="navigation-menu">
    <div class="menu-toggle" @click="toggleMenu" v-if="isMobile">
      <span>☰</span>
    </div>
    <nav :class="{ 'mobile-menu': isMobile, open: isMenuOpen }">
      <ul class="menu-root">
        <li class="menu-item">
          <router-link to="/" @click="closeMenu">首页</router-link>
        </li>
        <li
          class="menu-item has-children"
          @mouseenter="showSubmenu('admin')"
          @mouseleave="hideSubmenu"
        >
          <a href="#" @click.prevent="toggleSubmenu('admin')">管理 <span class="arrow">▼</span></a>
          <ul class="submenu" v-show="activeMenu === 'admin' || !isMobile">
            <li class="submenu-item">
              <router-link to="/admin/posts" @click="closeMenu">文章管理</router-link>
            </li>
            <li class="submenu-item">
              <router-link to="/admin/api/posts/create?from=admin" @click="closeMenu"
                >新建文章</router-link
              >
            </li>
          </ul>
        </li>
        <li class="menu-item">
          <router-link to="/websites" @click="closeMenu">网站合集</router-link>
        </li>

        <li class="menu-item">
          <router-link to="/ai-chat" @click="closeMenu">AI问答</router-link>
        </li>
        <li class="menu-item">
          <router-link to="/dev-tools" @click="closeMenu">🛠️ 工具箱</router-link>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const activeMenu = ref('')
const isMenuOpen = ref(false)
const isMobile = ref(false)

const showSubmenu = (menuName: string) => {
  if (!isMobile.value) {
    activeMenu.value = menuName
  }
}
const hideSubmenu = () => {
  if (!isMobile.value) {
    activeMenu.value = ''
  }
}
const toggleSubmenu = (menuName: string) => {
  if (isMobile.value) {
    activeMenu.value = activeMenu.value === menuName ? '' : menuName
  }
}
const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}
const closeMenu = () => {
  isMenuOpen.value = false
  activeMenu.value = ''
}

const checkIsMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    isMenuOpen.value = false
  }
}

onMounted(() => {
  checkIsMobile()
  window.addEventListener('resize', checkIsMobile)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkIsMobile)
})
</script>

<style scoped>
.navigation-menu {
  position: relative;
}

.menu-toggle {
  display: none;
  cursor: pointer;
  font-size: 1.5rem;
  padding: 0.5rem;
}

nav {
  display: flex;
}

.menu-root {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  align-items: center;
}

.menu-item {
  position: relative;
  margin-right: 1rem;
}

.menu-item > a {
  display: block;
  color: #333; /* 更改链接颜色 */
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.menu-item > a:hover {
  background-color: rgba(0, 0, 0, 0.1); /* 更改悬停背景�?*/
}

.menu-item.has-children > a {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.arrow {
  font-size: 0.7rem;
}

.submenu {
  position: absolute;
  top: 100%;
  left: 0;
  background-color: #f8f9fa; /* 更改子菜单背景色 */
  list-style: none;
  padding: 0.5rem 0;
  margin: 0;
  min-width: 150px;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.submenu-item > a {
  display: block;
  color: #333; /* 更改子菜单链接颜�?*/
  text-decoration: none;
  padding: 0.5rem 1rem;
  transition: background-color 0.3s;
}

.submenu-item > a:hover {
  background-color: rgba(0, 0, 0, 0.1); /* 更改子菜单悬停背景色 */
}

/* 移动端样�?*/
@media (max-width: 768px) {
  .menu-toggle {
    display: block;
  }

  nav {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background-color: #f8f9fa; /* 更改移动端菜单背景色 */
    z-index: 1000;
  }

  nav.open {
    display: block;
  }

  .menu-root {
    flex-direction: column;
    align-items: stretch;
  }

  .menu-item {
    margin-right: 0;
  }

  .menu-item > a {
    padding: 1rem;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  }

  .submenu {
    position: static;
    box-shadow: none;
    border-radius: 0;
  }

  .submenu-item > a {
    padding: 1rem 2rem;
  }
}
</style>
