<template>
  <div class="post-list">
    <div class="page-header">
      <h1>
        <el-icon><ChatDotRound /></el-icon>
        帖子广场
      </h1>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索帖子..."
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button v-if="userStore.isLoggedIn" type="primary" @click="$router.push('/posts/create')">
          <el-icon><Edit /></el-icon>
          发布帖子
        </el-button>
      </div>
    </div>

    <el-divider />

    <div v-loading="loading" class="posts-container">
      <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />
      
      <div v-else class="posts-grid">
        <el-card
          v-for="post in posts"
          :key="post.id"
          class="post-card"
          shadow="hover"
          @click="goToDetail(post.id)"
        >
          <div class="post-header">
            <div class="author-info">
              <el-avatar :size="36">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="author-details">
                <span class="author-name">发帖人：{{ post.username }}</span>
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              </div>
            </div>
          </div>

          <div class="post-content">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-text">{{ truncateText(post.content, 150) }}</p>
          </div>

          <div class="post-footer">
            <div class="post-stats">
              <span class="stat-item">
                <el-icon :class="{ liked: post.isLiked }"><StarFilled /></el-icon>
                {{ post.likesCount || 0 }} 赞
              </span>
            </div>
            <el-button text type="primary" @click.stop="goToDetail(post.id)">
              查看详情
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { postApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const posts = ref([])
const loading = ref(false)
const searchKeyword = ref('')

// 获取帖子列表
const fetchPosts = async () => {
  loading.value = true
  try {
    const response = await postApi.getPostList()
    if (response.code === 200) {
      posts.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取帖子列表失败')
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索帖子
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    fetchPosts()
    return
  }

  loading.value = true
  try {
    const response = await postApi.searchPosts(searchKeyword.value)
    if (response.code === 200) {
      posts.value = response.data || []
    } else {
      ElMessage.error(response.message || '搜索失败')
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/posts/${id}`)
}

// 将后端返回的时间统一按北京时间解析
const parseToBeijingDate = (time) => {
  if (!time) return null
  if (time instanceof Date) return time

  if (typeof time === 'string') {
    const normalized = time.replace(' ', 'T')

    if (normalized.endsWith('Z') || normalized.includes('+')) {
      return new Date(normalized)
    }

    return new Date(normalized + '+08:00')
  }

  return new Date(time)
}

// 格式化时间（相对时间，基于北京时间）
const formatTime = (time) => {
  const date = parseToBeijingDate(time)
  if (!date) return ''

  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN')
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.post-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.page-header h1 {
  font-size: clamp(24px, 4vw, 32px);
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: clamp(200px, 30vw, 300px);
}

.posts-container {
  min-height: 400px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.post-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.post-header {
  margin-bottom: 15px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.post-time {
  font-size: 13px;
  color: #909399;
}

.post-content {
  margin-bottom: 15px;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #909399;
}

.stat-item .el-icon {
  font-size: 16px;
  transition: color 0.3s;
}

.stat-item .el-icon.liked {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .post-list {
    padding: 15px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    width: 100%;
  }

  .search-input {
    flex: 1;
    min-width: 0;
  }

  .posts-grid {
    grid-template-columns: 1fr;
  }
}
</style>

