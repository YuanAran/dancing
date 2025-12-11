<template>
  <div class="video-list-container">
    <div class="page-header">
      <h1>
        <el-icon><VideoPlay /></el-icon>
        视频列表
      </h1>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索视频..."
          class="search-input"
          @input="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button
          v-if="userStore.isLoggedIn"
          type="primary"
          @click="$router.push('/videos/upload')"
        >
          <el-icon><Upload /></el-icon>
          上传视频
        </el-button>
      </div>
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
        <el-card class="video-card" @click="goToVideo(video.id)">
          <div class="video-thumbnail">
            <img
              v-if="video.thumbnailPath"
              :src="getImageUrl(video.thumbnailPath)"
              alt="video thumbnail"
              class="video-cover"
            />
            <div v-else class="video-placeholder">
              <el-icon size="48"><VideoPlay /></el-icon>
            </div>
            <div class="play-overlay">
              <el-icon size="24"><VideoPlay /></el-icon>
            </div>
          </div>
          
          <div class="video-info">
            <h3 class="video-title">{{ video.title }}</h3>
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
            
            <div class="video-actions" @click.stop>
              <el-button
                v-if="userStore.isLoggedIn && video.uploaderId === userStore.user?.id"
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
      <el-empty description="暂无视频">
        <el-button
          v-if="userStore.isLoggedIn"
          type="primary"
          @click="$router.push('/videos/upload')"
        >
          上传第一个视频
        </el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="videos.length > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { videoApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const videos = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const deletingVideoId = ref(null)
//
// const getImageUrl = (path) => {
//   if (!path) return ''
//   const cleanPath = path.replace(/^[/\\]+/, '').replace(/\\/g, '/')
//   return `/api/files/image?path=${encodeURIComponent(cleanPath)}`
// }
const getImageUrl = (path) => {
  if (!path) return ''
  const cleanPath = path.replace(/^[/\\]+/, '').replace(/\\/g, '/')
  return `https://localhost:8080/api/files/image?path=${encodeURIComponent(cleanPath)}`
}

// 获取视频列表
const getVideoList = async () => {
  loading.value = true
  try {
    let response
    if (searchKeyword.value.trim()) {
      response = await videoApi.searchVideos(searchKeyword.value.trim())
    } else {
      response = await videoApi.getVideoList()
    }
    
    if (response.code === 200) {
      videos.value = response.data
      total.value = response.data.length
    } else {
      ElMessage.error('获取视频列表失败')
    }
  } catch (error) {
    ElMessage.error('获取视频列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索视频
const handleSearch = () => {
  currentPage.value = 1
  getVideoList()
}

// 跳转到视频详情页
const goToVideo = (videoId) => {
  router.push(`/videos/${videoId}`)
}

// 删除视频
const deleteVideo = async (videoId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个视频吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    deletingVideoId.value = videoId
    const response = await videoApi.deleteVideo(videoId)
    
    if (response.code === 200) {
      ElMessage.success('视频删除成功')
      getVideoList() // 重新获取列表
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

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  getVideoList()
})
</script>

<style scoped>
.video-list-container {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 300px;
}

.video-col {
  margin-bottom: 20px;
}

.video-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  height: 320px;
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
}

.video-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.video-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
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
}

.video-title {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  min-height: 2.8em;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.uploader-info,
.upload-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-actions {
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .search-input {
    width: 100%;
  }
  
  .video-card {
    height: auto;
  }
  
  .video-thumbnail {
    height: 120px;
  }
}
</style>
