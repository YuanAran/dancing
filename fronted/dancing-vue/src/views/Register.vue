<template>
  <div class="register-page">
    <div class="animated-bg">
      <div class="orb orb1"></div>
      <div class="orb orb2"></div>
      <div class="orb orb3"></div>
    </div>

    <div class="top-bar">
      <el-button text @click="$router.push('/')" class="back-link">
        <el-icon><HomeFilled /></el-icon>
        <span>返回首页</span>
      </el-button>
    </div>

    <div class="content">
      <div class="card">
        <div class="left-side">
          <div class="brand">
            <div class="icon-box">
              <el-icon class="icon"><UserFilled /></el-icon>
            </div>
            <h1 class="title">加入我们</h1>
            <p class="subtitle">开启舞蹈之旅</p>
          </div>

          <div class="features">
            <div class="feature">
              <el-icon><VideoPlay /></el-icon>
              <span>海量视频课程</span>
            </div>
            <div class="feature">
              <el-icon><User /></el-icon>
              <span>专业导师指导</span>
            </div>
            <div class="feature">
              <el-icon><Star /></el-icon>
              <span>社区互动交流</span>
            </div>
          </div>
        </div>

        <div class="right-side">
          <div class="form-head">
            <h2>创建新账号</h2>
            <p>填写信息，快速注册</p>
          </div>

          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" @submit.prevent="handleRegister">
            <el-form-item prop="username">
              <label class="label">用户名</label>
              <el-input v-model="registerForm.username" placeholder="3-20个字符" size="large" clearable>
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-form-item prop="email">
              <label class="label">邮箱地址</label>
              <el-input v-model="registerForm.email" placeholder="your@email.com" size="large" clearable>
                <template #prefix><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>

            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item prop="password">
                  <label class="label">设置密码</label>
                  <el-input v-model="registerForm.password" type="password" placeholder="至少6个字符" size="large" show-password>
                    <template #prefix><el-icon><Lock /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item prop="confirmPassword">
                  <label class="label">确认密码</label>
                  <el-input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入" size="large" show-password @keyup.enter="handleRegister">
                    <template #prefix><el-icon><Lock /></el-icon></template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="pwd-strength" v-if="registerForm.password">
              <span>密码强度：</span>
              <div class="bar"><div class="fill" :class="pwdLevel"></div></div>
              <span class="text" :class="pwdLevel">{{ pwdText }}</span>
            </div>

            <el-button type="primary" size="large" class="submit" @click="handleRegister" :loading="loading" :disabled="!isFormValid">
              <el-icon v-if="!loading"><Check /></el-icon>
              <span>{{ loading ? '注册中...' : '立即注册' }}</span>
            </el-button>
          </el-form>

          <div class="footer">
            <el-divider><span class="divider-txt">已有账号？</span></el-divider>
            <el-button text class="login-btn" @click="$router.push('/login')">
              立即登录 <el-icon><Right /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, HomeFilled, UserFilled, Right, VideoPlay, Star, Check } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur'
    }
  ]
}

const isFormValid = computed(() => registerForm.username && registerForm.email && registerForm.password && registerForm.confirmPassword)

const pwdLevel = computed(() => {
  const pwd = registerForm.password
  if (!pwd) return 'weak'
  let s = 0
  if (pwd.length >= 8) s++
  if (pwd.length >= 12) s++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) s++
  if (/\d/.test(pwd)) s++
  if (/[!@#$%^&*]/.test(pwd)) s++
  return s <= 2 ? 'weak' : s <= 3 ? 'medium' : 'strong'
})

const pwdText = computed(() => ({ weak: '弱', medium: '中', strong: '强' }[pwdLevel.value]))

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
    loading.value = true
    const result = await userStore.register(registerForm.username, registerForm.password, registerForm.email)
    if (result.success) {
      ElMessage.success(result.message)
      router.push('/login')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page{min-height:100vh;position:relative;display:flex;align-items:center;justify-content:center;padding:20px;overflow:hidden;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%)}
.animated-bg{position:absolute;top:0;left:0;width:100%;height:100%;z-index:0}
.orb{position:absolute;border-radius:50%;filter:blur(80px);opacity:0.5;animation:float 20s ease-in-out infinite}
.orb1{width:500px;height:500px;background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);top:-10%;left:-10%}
.orb2{width:400px;height:400px;background:linear-gradient(135deg,#4facfe 0%,#00f2fe 100%);bottom:-10%;right:-10%;animation-delay:5s}
.orb3{width:450px;height:450px;background:linear-gradient(135deg,#43e97b 0%,#38f9d7 100%);top:50%;left:50%;transform:translate(-50%,-50%);animation-delay:10s}
@keyframes float{0%,100%{transform:translate(0,0) scale(1)}33%{transform:translate(30px,-50px) scale(1.1)}66%{transform:translate(-20px,20px) scale(0.9)}}
.top-bar{position:absolute;top:30px;left:30px;z-index:10}
.back-link{color:#fff;font-size:15px;font-weight:500;padding:12px 24px;border-radius:30px;background:rgba(255,255,255,0.15);backdrop-filter:blur(10px);border:1px solid rgba(255,255,255,0.25);transition:all 0.3s;display:flex;align-items:center;gap:8px}
.back-link:hover{background:rgba(255,255,255,0.25);transform:translateY(-2px)}
.content{position:relative;z-index:1;width:100%;max-width:1100px;animation:fadeIn 0.8s}
@keyframes fadeIn{from{opacity:0;transform:translateY(40px)}to{opacity:1;transform:translateY(0)}}
.card{display:flex;background:rgba(255,255,255,0.98);backdrop-filter:blur(20px);border-radius:28px;overflow:hidden;box-shadow:0 25px 70px rgba(0,0,0,0.3);border:1px solid rgba(255,255,255,0.6);min-height:700px}
.left-side{flex:0 0 380px;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:60px 40px;color:#fff;position:relative}
.brand{text-align:center;margin-bottom:50px}
.icon-box{width:110px;height:110px;margin:0 auto 25px;background:rgba(255,255,255,0.2);border-radius:50%;display:flex;align-items:center;justify-content:center;backdrop-filter:blur(10px);border:3px solid rgba(255,255,255,0.3);animation:pulse 3s ease-in-out infinite}
@keyframes pulse{0%,100%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,255,255,0.7)}50%{transform:scale(1.05);box-shadow:0 0 0 20px rgba(255,255,255,0)}}
.icon-box .icon{font-size:55px}
.brand .title{font-size:38px;font-weight:700;margin:0 0 12px;text-shadow:0 2px 15px rgba(0,0,0,0.15)}
.brand .subtitle{font-size:17px;margin:0;opacity:0.95;font-weight:300}
.features{display:flex;flex-direction:column;gap:20px}
.feature{display:flex;align-items:center;gap:15px;padding:18px 20px;background:rgba(255,255,255,0.15);border-radius:16px;backdrop-filter:blur(10px);border:1px solid rgba(255,255,255,0.2);transition:all 0.3s;font-size:15px;font-weight:500}
.feature:hover{background:rgba(255,255,255,0.22);transform:translateX(8px)}
.feature .el-icon{font-size:22px}
.right-side{flex:1;padding:60px 70px 45px;display:flex;flex-direction:column;justify-content:center}
.form-head{margin-bottom:35px;text-align:center}
.form-head h2{font-size:34px;font-weight:700;color:#1a202c;margin:0 0 8px}
.form-head p{font-size:15px;color:#718096;margin:0}
.label{display:block;font-size:14px;font-weight:600;color:#2d3748;margin-bottom:8px}
.el-form-item{margin-bottom:20px}
:deep(.el-input__wrapper){padding:13px 16px;border-radius:14px;box-shadow:0 2px 10px rgba(0,0,0,0.06);transition:all 0.3s;border:1px solid rgba(0,0,0,0.05)}
:deep(.el-input__wrapper:hover){box-shadow:0 4px 14px rgba(102,126,234,0.18);border-color:rgba(102,126,234,0.3)}
:deep(.el-input__wrapper.is-focus){box-shadow:0 4px 18px rgba(102,126,234,0.28);border-color:#667eea}
:deep(.el-input__prefix .el-icon){color:#667eea;font-size:18px}
.pwd-strength{display:flex;align-items:center;gap:12px;margin-bottom:20px;font-size:13px}
.pwd-strength .bar{flex:1;height:6px;background:#e2e8f0;border-radius:10px;overflow:hidden}
.pwd-strength .fill{height:100%;transition:all 0.4s;border-radius:10px}
.pwd-strength .fill.weak{width:33%;background:linear-gradient(90deg,#f56565,#fc8181)}
.pwd-strength .fill.medium{width:66%;background:linear-gradient(90deg,#ed8936,#f6ad55)}
.pwd-strength .fill.strong{width:100%;background:linear-gradient(90deg,#48bb78,#68d391)}
.pwd-strength .text{font-weight:600}
.pwd-strength .text.weak{color:#f56565}
.pwd-strength .text.medium{color:#ed8936}
.pwd-strength .text.strong{color:#48bb78}
.submit{width:100%;height:52px;font-size:16px;font-weight:600;border-radius:14px;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);border:none;box-shadow:0 8px 24px rgba(102,126,234,0.4);transition:all 0.3s;margin-top:10px;display:flex;align-items:center;justify-content:center;gap:8px}
.submit:hover:not(:disabled){transform:translateY(-2px);box-shadow:0 12px 28px rgba(102,126,234,0.5)}
.submit:disabled{opacity:0.65;cursor:not-allowed}
.footer{text-align:center;margin-top:20px}
.divider{margin:18px 0}
.divider-txt{color:#718096;padding:0 16px;background:#fff}
.login-btn{color:#667eea;font-size:15px;font-weight:600;transition:all 0.3s;padding:10px 20px}
.login-btn:hover{color:#764ba2;transform:translateX(4px)}
@media (max-width:768px){.card{flex-direction:column;min-height:auto}.left-side{flex:none;padding:45px 30px}.icon-box{width:90px;height:90px}.icon-box .icon{font-size:45px}.brand .title{font-size:30px}.brand .subtitle{font-size:15px}.brand{margin-bottom:35px}.right-side{padding:40px 30px}.form-head h2{font-size:28px}.top-bar{top:20px;left:20px}}
@media (max-width:480px){.right-side{padding:30px 20px}.form-head h2{font-size:24px}.submit{height:48px;font-size:15px}}
</style>