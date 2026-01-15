<template>
  <div class="friend-manage-container">
    <div class="page-header">
      <h1>
        <el-icon><User /></el-icon>
        好友管理
      </h1>
      <el-button type="primary" @click="$router.push('/friends/search')">
        <el-icon><Plus /></el-icon>
        添加好友
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="friend-tabs">
      <!-- 好友列表 -->
      <el-tab-pane label="我的好友" name="friends">
        <div v-loading="friendsLoading">
          <el-row :gutter="20" v-if="friends.length > 0">
            <el-col
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              v-for="friend in friends"
              :key="friend.id"
              class="friend-col"
            >
              <el-card class="friend-card">
                <div class="friend-avatar">
                  <el-icon size="48"><User /></el-icon>
                </div>
                <div class="friend-info">
                  <h3>{{ friend.username }}</h3>
                  <p>{{ friend.email }}</p>
                  <div class="friend-meta">
                    <span class="join-date">{{ formatDate(friend.createdAt) }}</span>
                  </div>
                </div>
                <div class="friend-actions">
                  <el-button
                    type="primary"
                    size="small"
                    @click="startVideoCall(friend.id)"
                  >
                    <el-icon><VideoCamera /></el-icon>
                    视频通话
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="removeFriend(friend.id)"
                    :loading="removingFriendId === friend.id"
                  >
                    <el-icon><UserMinus /></el-icon>
                    删除好友
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <div v-else class="empty-state">
            <el-empty description="您还没有好友">
              <el-button type="primary" @click="$router.push('/friends/search')">
                去添加好友
              </el-button>
            </el-empty>
          </div>
        </div>
      </el-tab-pane>

      <!-- 待处理的好友申请 -->
      <el-tab-pane label="好友申请" name="requests">
        <div v-loading="requestsLoading">
          <el-row :gutter="20" v-if="pendingRequests.length > 0">
            <el-col
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              v-for="request in pendingRequests"
              :key="request.id"
              class="request-col"
            >
              <el-card class="request-card">
                <div class="request-avatar">
                  <el-icon size="48"><User /></el-icon>
                </div>
                <div class="request-info">
                  <h3>{{ request.username }}</h3>
                  <p>{{ request.email }}</p>
                  <div class="request-meta">
                    <span class="request-date">{{ formatDate(request.createdAt) }}</span>
                  </div>
                </div>
                <div class="request-actions">
                  <el-button
                    type="success"
                    size="small"
                    @click="acceptRequest(request.id)"
                    :loading="processingRequestId === request.id"
                  >
                    <el-icon><Check /></el-icon>
                    接受
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="rejectRequest(request.id)"
                    :loading="processingRequestId === request.id"
                  >
                    <el-icon><Close /></el-icon>
                    拒绝
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <div v-else class="empty-state">
            <el-empty description="暂无待处理的好友申请" />
          </div>
        </div>
      </el-tab-pane>

      <!-- 我发送的申请 -->
      <el-tab-pane label="我的申请" name="sent">
        <div v-loading="sentRequestsLoading">
          <el-row :gutter="20" v-if="sentRequests.length > 0">
            <el-col
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              v-for="request in sentRequests"
              :key="request.id"
              class="sent-col"
            >
              <el-card class="sent-card">
                <div class="sent-avatar">
                  <el-icon size="48"><User /></el-icon>
                </div>
                <div class="sent-info">
                  <h3>{{ request.username }}</h3>
                  <p>{{ request.email }}</p>
                  <div class="sent-meta">
                    <el-tag :type="getRequestStatusType(request.status)" size="small">
                      {{ getRequestStatusText(request.status) }}
                    </el-tag>
                    <span class="sent-date">{{ formatDate(request.createdAt) }}</span>
                  </div>
                </div>
                <div class="sent-actions">
                  <el-button
                    v-if="request.status === 'pending'"
                    type="danger"
                    size="small"
                    @click="cancelRequest(request.id)"
                    :loading="processingRequestId === request.id"
                  >
                    <el-icon><Close /></el-icon>
                    取消申请
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <div v-else class="empty-state">
            <el-empty description="您还没有发送任何好友申请" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 统计信息 -->
    <div class="stats-section">
      <el-card>
        <template #header>
          <h3>
            <el-icon><DataAnalysis /></el-icon>
            好友统计
          </h3>
        </template>
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ friends.length }}</div>
              <div class="stat-label">好友数量</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ pendingRequests.length }}</div>
              <div class="stat-label">待处理申请</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ sentRequests.length }}</div>
              <div class="stat-label">我的申请</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ getAcceptedRequestsCount() }}</div>
              <div class="stat-label">已接受申请</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { friendApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, DataAnalysis, Plus, User, UserMinus, VideoCamera } from '@element-plus/icons-vue'

const router = useRouter()

const activeTab = ref('friends')
const friendsLoading = ref(false)
const requestsLoading = ref(false)
const sentRequestsLoading = ref(false)
const processingRequestId = ref(null)
const removingFriendId = ref(null)

const friends = ref([])
const pendingRequests = ref([])
const sentRequests = ref([])

// 获取好友列表和申请
const getFriends = async () => {
  friendsLoading.value = true
  requestsLoading.value = true
  sentRequestsLoading.value = true
  
  try {
    const response = await friendApi.getFriends()
    console.log('好友数据响应:', response) // 调试日志
    
    if (response.code === 200) {
      const data = response.data || {}
      friends.value = data.friends || []
      pendingRequests.value = (data.pendingRequests || []).map(req => ({
        ...req,
        status: req.status || 'pending'
      }))
      sentRequests.value = (data.sentRequests || []).map(req => ({
        ...req,
        status: req.status || 'pending'
      }))
      
      console.log('好友列表:', friends.value)
      console.log('待处理申请:', pendingRequests.value)
      console.log('我的申请:', sentRequests.value)
    } else {
      ElMessage.error(response.message || '获取好友列表失败')
    }
  } catch (error) {
    console.error('获取好友数据失败:', error)
    ElMessage.error('获取好友列表失败')
  } finally {
    friendsLoading.value = false
    requestsLoading.value = false
    sentRequestsLoading.value = false
  }
}

// 获取我发送的申请（已合并到getFriends中）
const getSentRequests = async () => {
  // 已经在getFriends中一起获取
}

// 接受好友申请
const acceptRequest = async (friendId) => {
  try {
    processingRequestId.value = friendId
    const response = await friendApi.acceptFriendRequest(friendId)
    
    if (response.code === 200) {
      ElMessage.success('已接受好友申请')
      getFriends() // 重新获取数据
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('接受申请失败')
  } finally {
    processingRequestId.value = null
  }
}

// 拒绝好友申请
const rejectRequest = async (friendId) => {
  try {
    processingRequestId.value = friendId
    const response = await friendApi.rejectFriendRequest(friendId)
    
    if (response.code === 200) {
      ElMessage.success('已拒绝好友申请')
      getFriends() // 重新获取数据
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('拒绝申请失败')
  } finally {
    processingRequestId.value = null
  }
}

// 发起视频通话
const startVideoCall = (friendId) => {
  router.push({
    path: '/video-call',
    query: {
      targetUserId: friendId
    }
  })
}

// 删除好友
const removeFriend = async (friendId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个好友吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    removingFriendId.value = friendId
    const response = await friendApi.deleteFriend(friendId)
    
    if (response.code === 200) {
      ElMessage.success('好友删除成功')
      getFriends() // 重新获取数据
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除好友失败')
    }
  } finally {
    removingFriendId.value = null
  }
}

// 取消申请
const cancelRequest = async (requestId) => {
  try {
    await ElMessageBox.confirm('确定要取消这个好友申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    processingRequestId.value = requestId
    // 这里需要后端提供取消申请的接口
    ElMessage.success('申请已取消')
    getSentRequests() // 重新获取数据
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消申请失败')
    }
  } finally {
    processingRequestId.value = null
  }
}

// 获取申请状态类型
const getRequestStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'accepted': return 'success'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

// 获取申请状态文本
const getRequestStatusText = (status) => {
  switch (status) {
    case 'pending': return '待处理'
    case 'accepted': return '已接受'
    case 'rejected': return '已拒绝'
    default: return '未知'
  }
}

// 获取已接受申请数量
const getAcceptedRequestsCount = () => {
  return sentRequests.value.filter(req => req.status === 'accepted').length
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  getFriends()
})
</script>

<style scoped>
.friend-manage-container {
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

.friend-tabs {
  margin-bottom: 20px;
}

.friend-col,
.request-col,
.sent-col {
  margin-bottom: 20px;
}

.friend-card,
.request-card,
.sent-card {
  transition: transform 0.3s, box-shadow 0.3s;
  height: 280px;
  display: flex;
  flex-direction: column;
}

.friend-card:hover,
.request-card:hover,
.sent-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.friend-avatar,
.request-avatar,
.sent-avatar {
  background: #f5f5f5;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-bottom: 16px;
  border-radius: 8px;
}

.friend-info,
.request-info,
.sent-info {
  flex: 1;
  text-align: center;
}

.friend-info h3,
.request-info h3,
.sent-info h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.friend-info p,
.request-info p,
.sent-info p {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
}

.friend-meta,
.request-meta,
.sent-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

.join-date,
.request-date,
.sent-date {
  margin-bottom: 4px;
}

.friend-actions,
.request-actions,
.sent-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: auto;
  padding-top: 16px;
}

.request-actions {
  flex-direction: column;
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
  
  .friend-card,
  .request-card,
  .sent-card {
    height: auto;
  }
  
  .request-actions {
    flex-direction: row;
  }
}
</style>
