<template>
  <div class="profile-container">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2><el-icon><User /></el-icon> 个人信息</h2>
        </div>
      </template>
      
      <el-form 
        ref="profileFormRef" 
        :model="profileForm" 
        :rules="profileRules" 
        class="profile-form"
        @submit.prevent="updateProfile"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="updateProfile" :loading="loading">
            更新信息
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计信息卡片 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2><el-icon><DataAnalysis /></el-icon> 我的统计</h2>
        </div>
      </template>
      
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="8" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon" size="24"><VideoPlay /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.videoCount || 0 }}</div>
                <div class="stat-label">上传视频</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="8" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon" size="24"><User /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.friendCount || 0 }}</div>
                <div class="stat-label">好友数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="8" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon" size="24"><View /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.viewCount || 0 }}</div>
                <div class="stat-label">视频播放</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="8" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon" size="24"><Clock /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ formatJoinDate(userStats.joinDate) }}</div>
                <div class="stat-label">注册天数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const profileFormRef = ref()
const loading = ref(false)

const profileForm = reactive({
  username: '',
  email: ''
})

const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const userStats = reactive({
  videoCount: 0,
  friendCount: 0,
  viewCount: 0,
  joinDate: null
})

// 初始化表单
const initForm = () => {
  if (userStore.user) {
    profileForm.username = userStore.user.username || ''
    profileForm.email = userStore.user.email || ''
  }
}

// 更新用户信息
const updateProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    loading.value = true
    
    const result = await userStore.updateUser(profileForm.username, profileForm.email)
    
    if (result.success) {
      ElMessage.success(result.message)
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('表单验证失败')
  } finally {
    loading.value = false
  }
}

// 获取用户统计信息
const getUserStats = async () => {
  // 这里可以调用API获取用户的统计信息
  // 暂时使用模拟数据
  userStats.videoCount = 0
  userStats.friendCount = 0
  userStats.viewCount = 0
  userStats.joinDate = userStore.user?.createdAt || new Date()
}

// 格式化注册天数
const formatJoinDate = (dateString) => {
  if (!dateString) return '0'
  const joinDate = new Date(dateString)
  const now = new Date()
  const diffTime = Math.abs(now - joinDate)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays
}

onMounted(() => {
  initForm()
  getUserStats()
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 16px;
}

.card-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
  font-size: 20px;
}

.profile-form {
  max-width: 500px;
}

.stats-row {
  margin-top: 16px;
}

.stat-card {
  height: 80px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  color: #667eea;
  margin-right: 12px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

@media (max-width: 768px) {
  .profile-form {
    max-width: 100%;
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 6px;
  }
}
</style>