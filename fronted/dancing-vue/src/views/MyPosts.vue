<template>
  <div class="my-posts">
    <div class="page-header">
      <h1>
        <el-icon><Collection /></el-icon>
        我的帖子
      </h1>
      <el-button type="primary" @click="$router.push('/posts/create')">
        <el-icon><Edit /></el-icon>
        发布新帖子
      </el-button>
    </div>

    <el-divider />

    <div v-loading="loading" class="posts-container">
      <el-empty v-if="!loading && posts.length === 0" description="还没有发布帖子" />
      
      <div v-else class="posts-list">
        <el-card
          v-for="post in posts"
          :key="post.id"
          class="post-item"
          shadow="hover"
        >
          <div class="post-header">
            <div class="post-info" @click="goToDetail(post.id)">
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-excerpt">{{ truncateText(post.content, 100) }}</p>
              <div class="post-meta">
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(post.createdAt) }}
                </span>
                <span class="meta-item">
                  <el-icon><StarFilled /></el-icon>
                  {{ post.likesCount || 0 }} 赞
                </span>
              </div>
            </div>

            <div class="post-actions">
              <el-button text type="primary" @click="goToDetail(post.id)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button text type="danger" @click="handleDelete(post.id)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const posts = ref([])
const loading = ref(false)

// 获取我的帖子
const fetchMyPosts = async () => {
  loading.value = true
  try {
    const response = await postApi.getMyPosts()
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

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/posts/${id}`)
}

// 删除帖子
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后无法恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await postApi.deletePost(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchMyPosts() // 重新加载列表
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
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
  fetchMyPosts()
})
</script>

<style scoped>
.my-posts {
  max-width: 1000px;
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

.posts-container {
  min-height: 300px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-item {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.post-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.post-info {
  flex: 1;
  cursor: pointer;
  min-width: 0;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
  line-height: 1.4;
  transition: color 0.3s;
}

.post-info:hover .post-title {
  color: #409eff;
}

.post-excerpt {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.post-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .my-posts {
    padding: 15px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .post-header {
    flex-direction: column;
  }

  .post-actions {
    flex-direction: row;
    justify-content: flex-end;
  }
}
</style>

