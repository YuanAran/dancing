<template>
  <div class="post-detail">
    <el-card v-loading="loading" class="detail-card" shadow="hover">
      <template v-if="!loading && post">
        <!-- 帖子头部 -->
        <div class="post-header">
          <div class="author-section">
            <el-avatar :size="48">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="author-info">
              <div class="author-name">发帖人：{{ post.username }}</div>
              <div class="post-meta">
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                <span v-if="post.updatedAt !== post.createdAt" class="edit-mark">
                  (已编辑)
                </span>
              </div>
            </div>
          </div>

          <div v-if="isAuthor" class="post-actions">
            <el-button text type="primary" @click="handleEdit">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button text type="danger" @click="handleDelete">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>

        <el-divider />

        <!-- 帖子内容 -->
        <div class="post-content">
          <h1 class="post-title">{{ post.title }}</h1>
          <div class="post-text">{{ post.content }}</div>
        </div>

        <el-divider />

        <!-- 帖子互动 -->
        <div class="post-interactions">
          <el-button
            :type="post.isLiked ? 'danger' : 'default'"
            :plain="!post.isLiked"
            @click="handleLike"
          >
            <el-icon><StarFilled /></el-icon>
            {{ post.isLiked ? '已赞' : '点赞' }} ({{ post.likesCount || 0 }})
          </el-button>

          <el-button text type="primary" @click="showLikeUsers">
            查看点赞用户
          </el-button>
        </div>

        <el-divider />

        <div class="comments-section">
          <div class="comments-header">
            <span class="comments-title">评论</span>
            <span class="comments-count">({{ comments.length }})</span>
          </div>

          <div v-if="userStore.isLoggedIn" class="comment-editor">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="写下你的评论..."
            />
            <div class="comment-editor-actions">
              <el-button type="primary" :loading="commentSubmitting" @click="submitComment">
                发表评论
              </el-button>
            </div>
          </div>

          <el-empty v-if="!commentsLoading && comments.length === 0" description="暂无评论" :image-size="100" />

          <div v-loading="commentsLoading" class="comments-list">
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <div class="comment-left">
                <el-avatar :size="32">
                  <el-icon><User /></el-icon>
                </el-avatar>
              </div>
              <div class="comment-main">
                <div class="comment-meta">
                  <span class="comment-username">{{ c.username || '用户' }}</span>
                  <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ c.content }}</div>
              </div>
              <div class="comment-actions">
                <el-button
                  v-if="isCommentAuthor(c)"
                  text
                  type="danger"
                  size="small"
                  @click="deleteComment(c)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <el-empty v-else-if="!loading && !post" description="帖子不存在或已被删除" />
    </el-card>

    <!-- 点赞用户对话框 -->
    <el-dialog
      v-model="likeUsersVisible"
      title="点赞用户"
      width="500px"
      :append-to-body="true"
    >
      <el-scrollbar max-height="400px">
        <div v-if="likeUsers.length > 0" class="like-users-list">
          <div v-for="user in likeUsers" :key="user.id" class="like-user-item">
            <el-avatar :size="32">
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="username">{{ user.username }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无点赞" :image-size="100" />
      </el-scrollbar>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editVisible"
      title="编辑帖子"
      width="700px"
      :append-to-body="true"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="editForm.title"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="10"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { postApi, commentApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const loading = ref(false)
const likeUsersVisible = ref(false)
const likeUsers = ref([])
const editVisible = ref(false)
const updating = ref(false)

const comments = ref([])
const commentsLoading = ref(false)
const newComment = ref('')
const commentSubmitting = ref(false)

const editFormRef = ref(null)
const editForm = reactive({
  title: '',
  content: ''
})

const editRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度应在 5 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, max: 5000, message: '内容长度应在 10 到 5000 个字符', trigger: 'blur' }
  ]
}

const isAuthor = computed(() => {
  return post.value && userStore.user && post.value.userId === userStore.user.id
})

// 获取帖子详情
const fetchPostDetail = async () => {
  loading.value = true
  try {
    const response = await postApi.getPostDetail(route.params.id)
    if (response.code === 200) {
      post.value = response.data
      fetchComments()
    } else {
      ElMessage.error(response.message || '获取帖子详情失败')
      post.value = null
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
    post.value = null
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  commentsLoading.value = true
  try {
    const response = await commentApi.getPostComments(route.params.id)
    if (response.code === 200) {
      comments.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取评论失败')
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    ElMessage.error('获取评论失败')
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentSubmitting.value = true
  try {
    const response = await commentApi.createPostComment(route.params.id, { content: newComment.value })
    if (response.code === 200) {
      ElMessage.success('评论成功')
      newComment.value = ''
      fetchComments()
    } else {
      ElMessage.error(response.message || '评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  } finally {
    commentSubmitting.value = false
  }
}

const isCommentAuthor = (c) => {
  return userStore.user && c && c.userId === userStore.user.id
}

const deleteComment = async (c) => {
  if (!c || !c.id) return

  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await commentApi.deleteComment(c.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchComments()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 点赞/取消点赞
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const response = await postApi.toggleLike(post.value.id)
    if (response.code === 200) {
      post.value.isLiked = !post.value.isLiked
      post.value.likesCount += post.value.isLiked ? 1 : -1
      ElMessage.success(post.value.isLiked ? '点赞成功' : '已取消点赞')
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 显示点赞用户
const showLikeUsers = async () => {
  try {
    const response = await postApi.getLikeUsers(post.value.id)
    if (response.code === 200) {
      likeUsers.value = response.data || []
      likeUsersVisible.value = true
    } else {
      ElMessage.error(response.message || '获取点赞用户失败')
    }
  } catch (error) {
    console.error('获取点赞用户失败:', error)
    ElMessage.error('获取点赞用户失败')
  }
}

// 编辑帖子
const handleEdit = () => {
  editForm.title = post.value.title
  editForm.content = post.value.content
  editVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  // 先进行表单验证
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请检查输入内容')
    return
  }

  updating.value = true
  try {
    const response = await postApi.updatePost(post.value.id, {
      title: editForm.title,
      content: editForm.content
    })

    if (response.code === 200) {
      ElMessage.success('更新成功')
      editVisible.value = false
      fetchPostDetail()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
}

// 删除帖子
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后无法恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await postApi.deletePost(post.value.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      router.push('/posts')
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

// 将后端返回的时间统一按北京时间解析
const parseToBeijingDate = (time) => {
  if (!time) return null
  if (time instanceof Date) return time

  if (typeof time === 'string') {
    // 兼容 "yyyy-MM-dd HH:mm:ss" 和 ISO 字符串
    const normalized = time.replace(' ', 'T')

    // 已经带有时区信息的，直接用
    if (normalized.endsWith('Z') || normalized.includes('+')) {
      return new Date(normalized)
    }

    // 默认按北京时区处理
    return new Date(normalized + '+08:00')
  }

  return new Date(time)
}

// 格式化时间（展示为北京时间）
const formatTime = (time) => {
  const date = parseToBeijingDate(time)
  if (!date) return ''

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}

onMounted(() => {
  fetchPostDetail()
})
</script>

<style scoped>
.post-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.comments-section {
  padding-top: 10px;
}

.comments-header {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 12px;
}

.comments-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.comments-count {
  font-size: 13px;
  color: #909399;
}

.comment-editor {
  margin-bottom: 16px;
}

.comment-editor-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #fafafa;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 6px;
}

.comment-username {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  align-items: flex-start;
}

.detail-card {
  border-radius: 12px;
  overflow: hidden;
  min-height: 400px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.author-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.post-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.edit-mark {
  font-style: italic;
}

.post-actions {
  display: flex;
  gap: 8px;
}

.post-content {
  padding: 20px 0;
}

.post-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 20px 0;
  line-height: 1.3;
}

.post-text {
  font-size: 16px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.post-interactions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.like-users-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.like-user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.like-user-item:hover {
  background-color: #f5f7fa;
}

.like-user-item .username {
  font-size: 15px;
  color: #303133;
}

@media (max-width: 768px) {
  .post-detail {
    padding: 15px;
  }

  .post-title {
    font-size: 22px;
  }

  .post-text {
    font-size: 15px;
  }

  .post-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .post-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>

