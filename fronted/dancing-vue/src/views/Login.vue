<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="animated-background">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 顶部导航 -->
    <div class="top-nav">
      <el-button text @click="$router.push('/')" class="back-btn">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>

    <!-- 登录卡片 -->
    <div class="login-container">
      <div class="login-card">
        <!-- 左侧装饰区域 -->
        <div class="card-decoration">
          <div class="decoration-content">
            <div class="icon-wrapper">
              <el-icon class="dance-icon"><VideoPlay /></el-icon>
            </div>
            <h1 class="welcome-title">欢迎回来</h1>
            <p class="welcome-subtitle">登录开始您的舞蹈之旅</p>
            <div class="decorative-elements">
              <div class="element element-1"></div>
              <div class="element element-2"></div>
              <div class="element element-3"></div>
            </div>
          </div>
        </div>

        <!-- 右侧表单区域 -->
        <div class="card-form">
          <div class="form-header">
            <h2 class="form-title">登录账号</h2>
            <p class="form-subtitle">居家广场舞平台</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  clearable
                >
                  <template #prefix>
                    <el-icon class="input-icon"><User /></el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrapper">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Lock /></el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                @click="handleLogin"
                :loading="loading"
                :disabled="!loginForm.username || !loginForm.password"
              >
                <span v-if="!loading">立即登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <el-divider class="divider">
              <span class="divider-text">还没有账号？</span>
            </el-divider>
            <el-button text class="register-link" @click="$router.push('/register')">
              立即注册
              <el-icon class="ml-1"><Right /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, HomeFilled, VideoPlay, Right } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const result = await userStore.login(loginForm.username, loginForm.password)

    if (result.success) {
      ElMessage.success(result.message)
      router.push('/')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 动态背景 */
.animated-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  top: -10%;
  left: -10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  bottom: -10%;
  right: -10%;
  animation-delay: 5s;
}

.orb-3 {
  width: 450px;
  height: 450px;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* 顶部导航 */
.top-nav {
  position: absolute;
  top: 30px;
  left: 30px;
  z-index: 10;
}

.back-btn {
  color: white;
  font-size: 15px;
  font-weight: 500;
  padding: 10px 20px;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

/* 登录容器 */
.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1000px;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 登录卡片 */
.login-card {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
  min-height: 600px;
}

/* 左侧装饰 */
.card-decoration {
  flex: 0 0 380px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.decoration-content {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
}

.icon-wrapper {
  width: 120px;
  height: 120px;
  margin: 0 auto 30px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.7);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 20px rgba(255, 255, 255, 0);
  }
}

.dance-icon {
  font-size: 60px;
  color: white;
}

.welcome-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 15px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.welcome-subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.95;
  font-weight: 300;
}

.decorative-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.element {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.element-1 {
  width: 200px;
  height: 200px;
  top: -50px;
  right: -50px;
  animation: float 15s ease-in-out infinite;
}

.element-2 {
  width: 150px;
  height: 150px;
  bottom: -30px;
  left: -30px;
  animation: float 20s ease-in-out infinite reverse;
}

.element-3 {
  width: 100px;
  height: 100px;
  top: 50%;
  right: 20px;
  animation: float 18s ease-in-out infinite;
}

/* 右侧表单 */
.card-form {
  flex: 1;
  padding: 60px 70px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 40px;
  text-align: center;
}

.form-title {
  font-size: 32px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 10px;
}

.form-subtitle {
  font-size: 15px;
  color: #718096;
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-bottom: 20px;
}

.input-wrapper {
  margin-bottom: 8px;
  width: 100%;
}

.input-wrapper :deep(.el-input__wrapper) {
  padding: 16px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.input-wrapper :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.input-wrapper :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.25);
}

.input-icon {
  color: #667eea;
  font-size: 18px;
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-top: 20px;
}

.divider {
  margin: 20px 0;
}

.divider-text {
  color: #718096;
  font-size: 14px;
  padding: 0 15px;
}

.register-link {
  color: #667eea;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: #764ba2;
  transform: translateX(5px);
}

.ml-1 {
  margin-left: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
  }

  .card-decoration {
    padding: 40px 20px;
    min-height: 250px;
  }

  .icon-wrapper {
    width: 80px;
    height: 80px;
  }

  .dance-icon {
    font-size: 40px;
  }

  .welcome-title {
    font-size: 28px;
  }

  .welcome-subtitle {
    font-size: 14px;
  }

  .card-form {
    padding: 40px 30px;
  }

  .form-title {
    font-size: 26px;
  }

  .top-nav {
    top: 20px;
    left: 20px;
  }
}

@media (max-width: 480px) {
  .card-form {
    padding: 30px 20px;
  }

  .form-title {
    font-size: 24px;
  }

  .submit-btn {
    height: 45px;
    font-size: 15px;
  }
}
</style>