<template>
  <div class="home">
    <!-- 轮播图区域 -->
    <el-carousel :interval="4000" type="card" height="300px" class="hero-carousel">
      <el-carousel-item>
        <div class="carousel-item">
          <h2>欢迎来到居家广场舞平台</h2>
          <p>专业的广场舞教学视频平台</p>
        </div>
      </el-carousel-item>
      <el-carousel-item>
        <div class="carousel-item">
          <h2>海量教学视频</h2>
          <p>从基础到进阶，满足不同水平需求</p>
        </div>
      </el-carousel-item>
      <el-carousel-item>
        <div class="carousel-item">
          <h2>社区互动</h2>
          <p>与舞友们交流学习心得</p>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 快速操作 -->
    <div class="quick-actions" v-if="userStore.isLoggedIn">
      <h2 class="section-title">
        <el-icon><Lightning /></el-icon>
        快速操作
      </h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="action-card" @click="$router.push('/posts/create')">
            <div class="action-icon post-icon">
              <el-icon size="40"><Edit /></el-icon>
            </div>
            <h3>发布帖子</h3>
            <p>分享您的想法</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="action-card" @click="$router.push('/videos/upload')">
            <div class="action-icon upload-icon">
              <el-icon size="40"><Upload /></el-icon>
            </div>
            <h3>上传视频</h3>
            <p>分享您的舞蹈作品</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="action-card" @click="$router.push('/friends/search')">
            <div class="action-icon friend-icon">
              <el-icon size="40"><Plus /></el-icon>
            </div>
            <h3>添加好友</h3>
            <p>结识更多舞友</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="action-card" @click="$router.push('/videos/my')">
            <div class="action-icon video-icon">
              <el-icon size="40"><Collection /></el-icon>
            </div>
            <h3>我的视频</h3>
            <p>管理您的作品</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" v-if="userStore.isLoggedIn">
          <div class="action-card" @click="$router.push('/video-call')">
            <div class="action-icon call-icon">
              <el-icon size="40"><VideoCamera /></el-icon>
            </div>
            <h3>视频通话</h3>
            <p>与好友视频聊天</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 最新视频 -->
    <div class="latest-videos">
      <h2 class="section-title">
        <el-icon><VideoPlay /></el-icon>
        最新视频
      </h2>
      <el-row :gutter="20" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="video in latestVideos" :key="video.id">
          <el-card class="video-card" @click="goToVideo(video.id)">
            <div class="video-thumbnail">
              <el-icon size="48"><VideoPlay /></el-icon>
              <div class="play-overlay">
                <el-icon size="24"><VideoPlay /></el-icon>
              </div>
            </div>
            <div class="video-info">
              <h4 class="video-title">{{ video.title }}</h4>
              <p class="video-description">{{ video.description }}</p>
              <div class="video-meta">
                <div class="uploader-info">
                  <el-icon><User /></el-icon>
                  <span>{{ video.uploaderName }}</span>
                </div>
                <div class="upload-time">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(video.createdAt) }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div v-if="latestVideos.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无视频" />
      </div>
      
      <div class="more-videos">
        <el-button type="primary" @click="$router.push('/videos')">
          查看更多视频
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { videoApi } from '@/api'
import { ElMessage } from 'element-plus'
import {
  Lightning,
  Edit,
  Upload,
  Plus,
  Collection,
  VideoCamera,
  VideoPlay,
  User,
  Calendar,
  ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const latestVideos = ref([])

// 获取最新视频
const getLatestVideos = async () => {
  loading.value = true
  try {
    const response = await videoApi.getVideoList()
    if (response.code === 200) {
      latestVideos.value = response.data.slice(0, 8) // 只显示前8个
    }
  } catch (error) {
    ElMessage.error('获取视频列表失败')
  } finally {
    loading.value = false
  }
}

// 跳转到视频详情页
const goToVideo = (videoId) => {
  router.push(`/videos/${videoId}`)
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  getLatestVideos()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
  overflow-x: hidden;
}

.hero-carousel {
  margin-bottom: 30px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.carousel-item {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  border-radius: 20px;
  position: relative;
  overflow: hidden;
}

.carousel-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.1"/><circle cx="10" cy="60" r="0.5" fill="white" opacity="0.1"/><circle cx="90" cy="40" r="0.5" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.carousel-item h2 {
  font-size: 28px;
  margin-bottom: 12px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

.carousel-item p {
  font-size: 16px;
  opacity: 0.95;
  font-weight: 400;
  position: relative;
  z-index: 1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
  font-weight: 700;
}

.section-title .el-icon {
  color: #667eea;
  font-size: 28px;
}

.latest-videos {
  margin-bottom: 30px;
}

.video-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 300px;
  overflow: hidden;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

.video-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.video-thumbnail {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

.video-thumbnail::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.1) 50%, transparent 70%);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(102, 126, 234, 0.9);
  color: white;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.video-card:hover .play-overlay {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.1);
}

.video-info {
  padding: 0 8px;
  height: calc(100% - 132px);
  display: flex;
  flex-direction: column;
}

.video-title {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-description {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-top: auto;
}

.uploader-info,
.upload-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.more-videos {
  text-align: center;
  margin-top: 20px;
}

.more-videos .el-button {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
}

.quick-actions {
  margin-bottom: 40px;
  padding: 30px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 20px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.action-card {
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(102, 126, 234, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.action-card:hover {
  transform: translateY(-12px) scale(1.03);
  box-shadow: 0 20px 48px rgba(102, 126, 234, 0.25);
  border-color: rgba(102, 126, 234, 0.3);
}

.action-card:hover::before {
  opacity: 1;
}

.action-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.4s ease;
  position: relative;
  z-index: 1;
}

.post-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(250, 112, 154, 0.4);
}

.upload-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.friend-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(245, 87, 108, 0.4);
}

.video-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(79, 172, 254, 0.4);
}

.call-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(250, 112, 154, 0.4);
}

.action-card:hover .action-icon {
  transform: scale(1.15) rotateZ(5deg);
}

.action-card h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
  font-weight: 700;
  position: relative;
  z-index: 1;
}

.action-card p {
  margin: 0;
  color: #666;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .home {
    max-width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .carousel-item h2 {
    font-size: 24px;
  }
  
  .carousel-item p {
    font-size: 14px;
  }
  
  .section-title {
    font-size: 20px;
  }
  
  .video-card {
    height: 260px;
  }
  
  .video-thumbnail {
    height: 100px;
  }
  
  .quick-actions {
    padding: 20px;
  }
  
  .action-card {
    height: 160px;
    padding: 24px 16px;
  }
  
  .action-icon {
    width: 70px;
    height: 70px;
  }
  
  .action-card h3 {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .carousel-item h2 {
    font-size: 20px;
  }
  
  .carousel-item p {
    font-size: 12px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .video-card {
    height: 240px;
  }
  
  .video-thumbnail {
    height: 80px;
  }
  
  .quick-actions {
    padding: 16px;
  }
  
  .action-card {
    height: 140px;
    padding: 20px 12px;
    margin-bottom: 12px;
  }
  
  .action-icon {
    width: 60px;
    height: 60px;
  }
  
  .action-card h3 {
    font-size: 15px;
  }
  
  .action-card p {
    font-size: 12px;
  }
}

/* 加载动画 */
.video-card,
.action-card {
  animation: fadeInUp 0.6s ease-out;
}

.video-card:nth-child(1),
.action-card:nth-child(1) { animation-delay: 0.1s; }
.video-card:nth-child(2),
.action-card:nth-child(2) { animation-delay: 0.2s; }
.video-card:nth-child(3),
.action-card:nth-child(3) { animation-delay: 0.3s; }
.video-card:nth-child(4) { animation-delay: 0.4s; }

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