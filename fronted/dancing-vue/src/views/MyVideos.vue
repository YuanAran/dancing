<template>
  <div class="my-videos-container">
    <div class="page-header">
      <h1>
        <el-icon><Collection /></el-icon>
        我的视频
      </h1>
      <el-button type="primary" @click="$router.push('/videos/upload')">
        <el-icon><Upload /></el-icon>
        上传新视频
      </el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
        v-for="video in videos"
        :key="video.id"
        class="video-col"
      >
        <el-card class="video-card">
          <div class="video-thumbnail" @click="goToVideo(video.id)">
            <el-icon size="48"><VideoPlay /></el-icon>
            <div class="play-overlay">
              <el-icon size="24"><VideoPlay /></el-icon>
            </div>
          </div>
          
          <div class="video-info">
            <h3 class="video-title" @click="goToVideo(video.id)">{{ video.title }}</h3>
            <p class="video-description">{{ video.description }}</p>
            
            <div class="video-meta">
              <div class="upload-time">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(video.createdAt) }}</span>
              </div>
              <div class="video-status">
                <el-tag :type="getStatusType(video.status)" size="small">
                  {{ getStatusText(video.status) }}
                </el-tag>
              </div>
            </div>
            
            <div class="video-actions">
              <el-button
                type="primary"
                size="small"
                @click="goToVideo(video.id)"
              >
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="deleteVideo(video.id)"
                :loading="deletingVideoId === video.id"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <div v-if="videos.length === 0 && !loading" class="empty-state">
      <el-empty description="您还没有上传任何视频">
        <el-button type="primary" @click="$router.push('/videos/upload')">
          上传第一个视频
        </el-button>
      </el-empty>
    </div>

    <!-- 统计信息 -->
    <div v-if="videos.length > 0" class="stats-section">
      <el-card>
        <template #header>
          <h3>
            <el-icon><DataAnalysis /></el-icon>
            视频统计
          </h3>
        </template>
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ videos.length }}</div>
              <div class="stat-label">总视频数</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ getTotalSize() }}</div>
              <div class="stat-label">总大小</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ getLatestUpload() }}</div>
              <div class="stat-label">最新上传</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ getAverageSize() }}</div>
              <div class="stat-label">平均大小</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { videoApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const videos = ref([])
const deletingVideoId = ref(null)

// 获取我的视频列表
const getMyVideos = async () => {
  loading.value = true
  try {
    const response = await videoApi.getMyVideos()
    if (response.code === 200) {
      videos.value = response.data
    } else {
      ElMessage.error('获取视频列表失败')
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

// 删除视频
const deleteVideo = async (videoId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个视频吗？删除后无法恢复！', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    deletingVideoId.value = videoId
    const response = await videoApi.deleteVideo(videoId)
    
    if (response.code === 200) {
      ElMessage.success('视频删除成功')
      getMyVideos() // 重新获取列表
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除视频失败')
    }
  } finally {
    deletingVideoId.value = null
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'active': return 'success'
    case 'processing': return 'warning'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'active': return '正常'
    case 'processing': return '处理中'
    case 'failed': return '失败'
    default: return '未知'
  }
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 获取总大小
const getTotalSize = () => {
  // 这里应该根据实际的文件大小计算，暂时返回模拟数据
  return `${(videos.value.length * 15.2).toFixed(1)} MB`
}

// 获取最新上传时间
const getLatestUpload = () => {
  if (videos.value.length === 0) return '无'
  const latest = videos.value.reduce((latest, video) => {
    return new Date(video.createdAt) > new Date(latest.createdAt) ? video : latest
  })
  return formatDate(latest.createdAt)
}

// 获取平均大小
const getAverageSize = () => {
  if (videos.value.length === 0) return '0 MB'
  return `${(15.2).toFixed(1)} MB`
}

onMounted(() => {
  getMyVideos()
})
</script>

<style scoped>
.my-videos-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-header h1 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
}

.video-col {
  margin-bottom: 20px;
}

.video-card {
  transition: transform 0.3s, box-shadow 0.3s;
  height: 360px;
  overflow: hidden;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.video-thumbnail {
  background: #f5f5f5;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  position: relative;
  margin-bottom: 12px;
  cursor: pointer;
}

.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.video-card:hover .play-overlay {
  opacity: 1;
}

.video-info {
  padding: 0 4px;
  height: calc(100% - 152px);
  display: flex;
  flex-direction: column;
}

.video-title {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.video-title:hover {
  color: #409eff;
}

.video-description {
  color: #666;
  font-size: 14px;
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
  align-items: center;
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.upload-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.stats-section {
  margin-top: 40px;
}

.stats-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
}

.stat-item {
  text-align: center;
  padding: 20px 0;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .video-card {
    height: auto;
  }
  
  .video-info {
    height: auto;
  }
  
  .video-thumbnail {
    height: 120px;
  }
}
</style>
