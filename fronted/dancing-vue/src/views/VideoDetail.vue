<template>
  <div class="video-detail-container" v-loading="loading">
    <div v-if="video" class="video-content">
      <!-- 视频播放区域 -->
      <div class="video-player-section">
        <el-card class="player-card">
          <div class="video-player">
            <video
              v-if="video.filePath"
              :src="getVideoUrl(video.filePath)"
              controls
              preload="metadata"
              class="video-element"
            >
              您的浏览器不支持视频播放
            </video>
            <div v-else class="no-video">
              <el-icon size="64"><VideoPlay /></el-icon>
              <p>视频文件不存在</p>
            </div>
          </div>
          
          <div class="video-info-header">
            <h1 class="video-title">{{ video.title }}</h1>
            <div class="video-meta">
              <div class="uploader-info">
                <el-icon><User /></el-icon>
                <span>{{ video.uploaderName }}</span>
              </div>
              <div class="upload-time">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(video.createdAt) }}</span>
              </div>
              <div class="video-actions" v-if="userStore.isLoggedIn && video.uploaderId === userStore.user?.id">
                <el-button
                  type="danger"
                  @click="deleteVideo"
                  :loading="deleting"
                >
                  <el-icon><Delete /></el-icon>
                  删除视频
                </el-button>
              </div>
            </div>
          </div>
          
          <div class="video-description">
            <h3>视频描述</h3>
            <p>{{ video.description }}</p>
          </div>
        </el-card>
      </div>
      
      <!-- 相关视频推荐 -->
      <div class="related-videos">
        <h2>
          <el-icon><VideoPlay /></el-icon>
          相关视频
        </h2>
        <el-row :gutter="20" v-loading="relatedLoading">
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            v-for="relatedVideo in relatedVideos"
            :key="relatedVideo.id"
            class="related-video-col"
          >
            <el-card class="related-video-card" @click="goToVideo(relatedVideo.id)">
              <div class="related-thumbnail">
                <el-icon size="32"><VideoPlay /></el-icon>
              </div>
              <div class="related-info">
                <h4>{{ relatedVideo.title }}</h4>
                <p>{{ relatedVideo.uploaderName }}</p>
                <span class="related-date">{{ formatDate(relatedVideo.createdAt) }}</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <div v-if="relatedVideos.length === 0 && !relatedLoading" class="no-related">
          <el-empty description="暂无相关视频" />
        </div>
      </div>
    </div>
    
    <!-- 视频不存在 -->
    <div v-else-if="!loading" class="video-not-found">
      <el-empty description="视频不存在或已被删除">
        <el-button type="primary" @click="$router.push('/videos')">
          返回视频列表
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { videoApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const relatedLoading = ref(false)
const deleting = ref(false)
const video = ref(null)
const relatedVideos = ref([])

// 获取视频详情
const getVideoDetail = async () => {
  loading.value = true
  try {
    const videoId = route.params.id
    const response = await videoApi.getVideoDetail(videoId)
    
    if (response.code === 200) {
      video.value = response.data
      getRelatedVideos()
    } else {
      ElMessage.error(response.message || '视频不存在')
    }
  } catch (error) {
    ElMessage.error('获取视频详情失败')
  } finally {
    loading.value = false
  }
}

// 获取相关视频
const getRelatedVideos = async () => {
  relatedLoading.value = true
  try {
    const response = await videoApi.getVideoList()
    if (response.code === 200) {
      // 过滤掉当前视频，取前6个作为相关视频
      relatedVideos.value = response.data
        .filter(v => v.id !== video.value.id)
        .slice(0, 6)
    }
  } catch (error) {
    console.error('获取相关视频失败:', error)
  } finally {
    relatedLoading.value = false
  }
}

// 获取视频URL
const getVideoUrl = (filePath) => {
  if (!filePath) {
    return ''
  }
  // 将文件路径中的反斜杠转换为正斜杠（Windows路径兼容）
  const normalizedPath = filePath.replace(/\\/g, '/')
  // 构建视频文件访问URL
  const videoUrl = `https://localhost:8080/api/files/video?path=${encodeURIComponent(normalizedPath)}`
  console.log('视频文件路径:', filePath, '-> URL:', videoUrl)
  return videoUrl
}

// 删除视频
const deleteVideo = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个视频吗？删除后无法恢复！', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    deleting.value = true
    const response = await videoApi.deleteVideo(video.value.id)
    
    if (response.code === 200) {
      ElMessage.success('视频删除成功')
      router.push('/videos/my')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除视频失败')
    }
  } finally {
    deleting.value = false
  }
}

// 跳转到其他视频
const goToVideo = (videoId) => {
  router.push(`/videos/${videoId}`)
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  getVideoDetail()
})
</script>

<style scoped>
.video-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.video-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.video-player-section {
  grid-column: 1;
}

.player-card {
  margin-bottom: 20px;
}

.video-player {
  width: 100%;
  margin-bottom: 20px;
}

.video-element {
  width: 100%;
  max-height: 500px;
  border-radius: 8px;
}

.no-video {
  background: #f5f5f5;
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  border-radius: 8px;
}

.video-info-header {
  margin-bottom: 20px;
}

.video-title {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 24px;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.uploader-info,
.upload-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
}

.video-actions {
  margin-left: auto;
}

.video-description h3 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 18px;
}

.video-description p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.related-videos {
  grid-column: 2;
}

.related-videos h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  color: #333;
  font-size: 20px;
}

.related-video-col {
  margin-bottom: 16px;
}

.related-video-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.related-video-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.related-thumbnail {
  background: #f5f5f5;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-bottom: 12px;
}

.related-info h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-info p {
  margin: 0 0 4px 0;
  color: #666;
  font-size: 12px;
}

.related-date {
  color: #999;
  font-size: 12px;
}

.no-related {
  text-align: center;
  padding: 40px 0;
}

.video-not-found {
  text-align: center;
  padding: 60px 0;
}

@media (max-width: 768px) {
  .video-content {
    grid-template-columns: 1fr;
  }
  
  .related-videos {
    grid-column: 1;
  }
  
  .video-title {
    font-size: 20px;
  }
  
  .video-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .video-actions {
    margin-left: 0;
  }
}
</style>
