<template>
  <div class="friend-search-container">
    <div class="page-header">
      <h1>
        <el-icon><Search /></el-icon>
        搜索好友
      </h1>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form @submit.prevent="handleSearch" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入用户名或邮箱进行搜索..."
            size="large"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button
                type="primary"
                @click="handleSearch"
                :loading="searching"
              >
                搜索
              </el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 搜索结果 -->
    <div v-if="searchResults.length > 0" class="search-results">
      <h2>
        <el-icon><User /></el-icon>
        搜索结果 ({{ searchResults.length }})
      </h2>
      
      <el-row :gutter="20" v-loading="searching">
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          v-for="user in searchResults"
          :key="user.id"
          class="user-col"
        >
          <el-card class="user-card">
            <div class="user-avatar">
              <el-icon size="48"><User /></el-icon>
            </div>
            
            <div class="user-info">
              <h3>{{ user.username }}</h3>
              <p>{{ user.email }}</p>
              <div class="user-meta">
                <span class="join-date">{{ formatDate(user.createdAt) }}</span>
              </div>
            </div>
            
            <div class="user-actions">
              <el-button
                v-if="!isFriend(user.id) && !hasPendingRequest(user.id)"
                type="primary"
                size="small"
                @click="sendFriendRequest(user.id)"
                :loading="sendingRequestId === user.id"
              >
                <el-icon><UserPlus /></el-icon>
                添加好友
              </el-button>
              
              <el-button
                v-else-if="hasPendingRequest(user.id)"
                type="warning"
                size="small"
                disabled
              >
                <el-icon><Clock /></el-icon>
                申请中
              </el-button>
              
              <el-button
                v-else
                type="success"
                size="small"
                disabled
              >
                <el-icon><Check /></el-icon>
                已是好友
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 无搜索结果 -->
    <div v-else-if="hasSearched && !searching" class="no-results">
      <el-empty description="没有找到相关用户">
        <el-button type="primary" @click="clearSearch">
          重新搜索
        </el-button>
      </el-empty>
    </div>

    <!-- 搜索提示 -->
    <div v-if="!hasSearched" class="search-tips">
      <el-card>
        <template #header>
          <h3>
            <el-icon><InfoFilled /></el-icon>
            搜索提示
          </h3>
        </template>
        <ul class="tips-list">
          <li>可以通过用户名进行搜索</li>
          <li>可以通过邮箱地址进行搜索</li>
          <li>支持模糊搜索，输入部分关键词即可</li>
          <li>搜索到用户后可以发送好友申请</li>
        </ul>
      </el-card>
    </div>

    <!-- 最近搜索 -->
    <div v-if="recentSearches.length > 0" class="recent-searches">
      <el-card>
        <template #header>
          <h3>
            <el-icon><Clock /></el-icon>
            最近搜索
          </h3>
        </template>
        <div class="recent-tags">
          <el-tag
            v-for="search in recentSearches"
            :key="search"
            class="recent-tag"
            @click="searchByKeyword(search)"
            effect="plain"
          >
            {{ search }}
          </el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { friendApi } from '@/api'
import { ElMessage } from 'element-plus'

const searchKeyword = ref('')
const searching = ref(false)
const hasSearched = ref(false)
const sendingRequestId = ref(null)
const searchResults = ref([])
const recentSearches = ref([])

// 搜索用户
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  searching.value = true
  hasSearched.value = true
  
  try {
    const response = await friendApi.searchUsers(searchKeyword.value.trim())
    
    if (response.code === 200) {
      searchResults.value = response.data || []
      
      // 保存到最近搜索
      const keyword = searchKeyword.value.trim()
      if (!recentSearches.value.includes(keyword)) {
        recentSearches.value.unshift(keyword)
        if (recentSearches.value.length > 5) {
          recentSearches.value.pop()
        }
        // 保存到本地存储
        localStorage.setItem('recentSearches', JSON.stringify(recentSearches.value))
      }
    } else {
      ElMessage.error(response.message || '搜索失败')
      searchResults.value = []
    }
  } catch (error) {
    ElMessage.error('搜索失败，请重试')
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

// 发送好友申请
const sendFriendRequest = async (friendId) => {
  try {
    sendingRequestId.value = friendId
    const response = await friendApi.sendFriendRequest(friendId)
    
    if (response.code === 200) {
      ElMessage.success('好友申请已发送')
      // 更新按钮状态
      const user = searchResults.value.find(u => u.id === friendId)
      if (user) {
        user.requestStatus = 'pending'
      }
    } else {
      ElMessage.error(response.message || '发送申请失败')
    }
  } catch (error) {
    ElMessage.error('发送申请失败，请重试')
  } finally {
    sendingRequestId.value = null
  }
}

// 检查是否是好友
const isFriend = (userId) => {
  // 这里需要从好友列表中检查
  // 暂时返回false
  return false
}

// 检查是否有待处理的申请
const hasPendingRequest = (userId) => {
  const user = searchResults.value.find(u => u.id === userId)
  return user?.requestStatus === 'pending'
}

// 通过关键词搜索
const searchByKeyword = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
  hasSearched.value = false
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 从本地存储加载最近搜索
const loadRecentSearches = () => {
  try {
    const saved = localStorage.getItem('recentSearches')
    if (saved) {
      recentSearches.value = JSON.parse(saved)
    }
  } catch (error) {
    console.error('加载最近搜索失败:', error)
  }
}

onMounted(() => {
  loadRecentSearches()
})
</script>

<style scoped>
.friend-search-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  max-width: 600px;
  margin: 0 auto;
}

.search-results {
  margin-bottom: 20px;
}

.search-results h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  color: #333;
}

.user-col {
  margin-bottom: 20px;
}

.user-card {
  transition: transform 0.3s, box-shadow 0.3s;
  height: 280px;
  display: flex;
  flex-direction: column;
}

.user-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.user-avatar {
  background: #f5f5f5;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-bottom: 16px;
  border-radius: 8px;
}

.user-info {
  flex: 1;
  text-align: center;
}

.user-info h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.user-info p {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
}

.user-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 16px;
}

.user-actions {
  display: flex;
  justify-content: center;
  margin-top: auto;
  padding-top: 16px;
}

.no-results {
  text-align: center;
  padding: 60px 0;
}

.search-tips {
  margin-bottom: 20px;
}

.search-tips h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 1.8;
}

.tips-list li {
  margin-bottom: 8px;
}

.recent-searches {
  margin-bottom: 20px;
}

.recent-searches h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
}

.recent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.recent-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.recent-tag:hover {
  background: #409eff;
  color: white;
}

@media (max-width: 768px) {
  .search-form {
    max-width: 100%;
  }
  
  .user-card {
    height: auto;
  }
  
  .recent-tags {
    justify-content: center;
  }
}
</style>
