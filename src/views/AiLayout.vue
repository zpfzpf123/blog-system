<template>
  <div class="ai-layout">
    <!-- 左侧侧边栏 -->
    <aside class="ai-sidebar">
      <div class="sidebar-header">
        <span class="logo-icon">🧠</span>
        <span class="logo-text">AI 实验室</span>
      </div>
      
      <nav class="sidebar-nav">
        <router-link to="/ai/chat" class="nav-item" active-class="active">
          <span class="nav-icon">🤖</span>
          <span class="nav-text">智能问答</span>
        </router-link>
        
        <router-link to="/ai/animations" class="nav-item" active-class="active">
          <span class="nav-icon">✨</span>
          <span class="nav-text">CSS 动画库</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <router-link to="/" class="back-home">
          <span class="icon">🏠</span> 返回首页
        </router-link>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main class="ai-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<style scoped>
.ai-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #f5f7fa;
}

.ai-sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #e1e8ed;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  z-index: 10;
  box-shadow: 2px 0 10px rgba(0,0,0,0.02);
}

.sidebar-header {
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #f1f3f5;
  gap: 10px;
}

.logo-icon {
  font-size: 1.8rem;
}

.logo-text {
  font-size: 1.2rem;
  font-weight: 700;
  color: #2c3e50;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  text-decoration: none;
  color: #495057;
  border-radius: 12px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.nav-item:hover {
  background: #f8f9fa;
  color: #667eea;
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.nav-icon {
  font-size: 1.2rem;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #f1f3f5;
}

.back-home {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  text-decoration: none;
  color: #6c757d;
  font-size: 0.9rem;
  border-radius: 8px;
  transition: all 0.2s;
  border: 1px dashed #dee2e6;
}

.back-home:hover {
  background: #f8f9fa;
  color: #2c3e50;
  border-color: #adb5bd;
}

.ai-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .ai-sidebar {
    width: 70px;
  }
  
  .logo-text, .nav-text, .sidebar-footer span {
    display: none;
  }
  
  .sidebar-header {
    justify-content: center;
    padding: 0;
  }
  
  .nav-item {
    justify-content: center;
    padding: 12px;
  }
  
  .back-home {
    padding: 10px 0;
  }
}
</style>
