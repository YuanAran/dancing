<template>
  <el-container class="layout-container" :class="{ 'is-auth': isAuthPage }" direction="vertical">
    <!-- 顶部导航栏 -->
    <el-header v-if="!isAuthPage" class="header">
      <div class="header-content">
        <div class="logo">
          <el-icon><VideoPlay /></el-icon>
          <span>居家广场舞平台</span>
        </div>
        
        <el-menu
          :default-active="activeIndex"
          class="nav-menu"
          mode="horizontal"
          @select="handleSelect"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/posts">帖子广场</el-menu-item>
          <el-menu-item index="/videos">视频列表</el-menu-item>
          <el-menu-item v-if="userStore.isLoggedIn" index="/friends">好友管理</el-menu-item>
        </el-menu>

        <div class="user-actions">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleUserCommand">
              <span class="user-info">
                <el-icon><User /></el-icon>
                {{ userStore.user?.username }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="myPosts">我的帖子</el-dropdown-item>
                  <el-dropdown-item command="myVideos">我的视频</el-dropdown-item>
                  <el-dropdown-item command="videoCall">
                    <el-icon><VideoCamera /></el-icon>
                    视频通话
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-main class="main-content" :class="{ 'auth-main': isAuthPage }">
      <div class="content-inner" :class="{ 'auth-inner': isAuthPage }">
        <router-view />
      </div>
    </el-main>

    <!-- 底部 -->
    <el-footer v-if="!isAuthPage" class="footer">
      <div class="footer-content">
        <p>&copy; 2024 居家广场舞平台. All rights reserved.</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeIndex = computed(() => route.path)

const isAuthPage = computed(() => ['/login', '/register'].includes(route.path))

// 处理菜单选择
const handleSelect = (index) => {
  router.push(index)
}

// 处理用户下拉菜单命令
const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'myPosts':
      router.push('/posts/my')
      break
    case 'myVideos':
      router.push('/videos/my')
      break
    case 'videoCall':
      router.push('/video-call')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/')
      } catch (error) {
        // 用户取消
      }
      break
  }
}

// 组件挂载时检查用户登录状态
onMounted(async () => {
  if (!userStore.isLoggedIn) {
    await userStore.getCurrentUser()
  }
})
</script>

<style scoped>
.layout-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.layout-container.is-auth {
  min-height: auto;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: 700;
  gap: 10px;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.logo .el-icon {
  font-size: 28px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.nav-menu {
  background: transparent;
  border: none;
  flex: 1;
  justify-content: center;
  margin: 0 40px;
}

.nav-menu .el-menu-item {
  color: white;
  border-bottom: 3px solid transparent;
  font-weight: 500;
  font-size: 16px;
  padding: 0 20px;
  transition: all 0.3s ease;
}

.nav-menu .el-menu-item:hover,
.nav-menu .el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.15);
  border-bottom-color: #ffd700;
  transform: translateY(-2px);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 10px 18px;
  border-radius: 30px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  font-weight: 600;
  font-size: 15px;
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  border-color: rgba(255, 255, 255, 0.6);
}

.user-info .el-icon {
  font-size: 18px;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}

.main-content {
  padding: 20px 24px;
  background: transparent;
  flex: 0 0 auto !important;
  height: auto !important;
}

.auth-main {
  padding: 0;
}

.content-inner {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.auth-inner {
  max-width: none;
  width: 100%;
}

.footer {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  text-align: center;
  padding: 20px 0;
  margin-top: 0;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
}

.footer-content p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

/* 响应式设计优化 */
@media (max-width: 1024px) {
  .header-content {
    padding: 0 20px;
  }
  
  .main-content {
    padding: 16px 20px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 12px 20px;
    gap: 12px;
  }
  
  .logo {
    font-size: 18px;
  }
  
  .nav-menu {
    margin: 0;
    width: 100%;
    justify-content: space-around;
  }
  
  .nav-menu .el-menu-item {
    padding: 0 10px;
    font-size: 13px;
  }
  
  .user-actions {
    width: 100%;
    justify-content: center;
  }
  
  .main-content {
    padding: 16px 16px;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 10px 16px;
  }
  
  .main-content {
    padding: 12px 12px;
  }
  
  .nav-menu .el-menu-item {
    padding: 0 6px;
    font-size: 12px;
  }
}

/* 动画效果 */
.header {
  animation: slideDown 0.6s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.main-content {
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
