<template>
  <div class="live-home">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><Monitor /></el-icon>
        <h1>直播</h1>
      </div>
      <div class="header-actions">
        <el-button @click="fetchRooms" :loading="listLoading">刷新列表</el-button>
        <el-button type="primary" size="large" @click="goCreate">
          <el-icon><Plus /></el-icon>
          创建直播间
        </el-button>
      </div>
    </div>

    <el-card class="list-card" shadow="hover" v-loading="listLoading">
      <el-table :data="roomRows" stripe empty-text="暂无直播间，可先创建" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="roomId" label="直播间 ID" min-width="200">
          <template #default="{ row }">
            <code class="room-id-code">{{ row.roomId }}</code>
            <el-button type="primary" link @click="copyId(row.roomId)">复制</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="主播" width="120" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="enterWatch(row.roomId)">进入观看</el-button>
            <el-button type="success" link @click="enterHost(row.roomId)">进入开播</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { liveApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Monitor, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const roomRows = ref([])
const listLoading = ref(false)

let pollTimer = null

async function fetchRooms() {
  listLoading.value = true
  try {
    const res = await liveApi.listRooms()
    if (res.code === 200 && Array.isArray(res.data)) {
      roomRows.value = res.data.map((r) => ({
        roomId: r.roomId,
        title: r.title,
        creatorName: r.creatorName || '—',
        createdAt: r.createdAt
      }))
    } else if (res.code !== 200) {
      ElMessage.warning(res.message || '获取列表失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取列表失败')
  } finally {
    listLoading.value = false
  }
}

function formatTime(ts) {
  if (ts == null) return '—'
  const d = new Date(typeof ts === 'number' ? ts : Number(ts))
  return d.toLocaleString('zh-CN', { hour12: false })
}

function copyId(roomId) {
  navigator.clipboard?.writeText(roomId).then(
    () => ElMessage.success('已复制'),
    () => ElMessage.warning('复制失败，请手动复制')
  )
}

function goCreate() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再创建直播间')
    router.push({ path: '/login', query: { redirect: '/live/create' } })
    return
  }
  router.push('/live/create')
}

function requireAuthThen(path, query) {
  if (!userStore.isLoggedIn) {
    const q = query ? `?${new URLSearchParams(query).toString()}` : ''
    router.push({ path: '/login', query: { redirect: `${path}${q}` } })
    return false
  }
  return true
}

function enterWatch(roomId) {
  if (!requireAuthThen(`/live/watch/${roomId}`)) return
  router.push({ name: 'liveWatch', params: { roomId } })
}

function enterHost(roomId) {
  if (!requireAuthThen(`/live/${roomId}`)) return
  router.push({ name: 'liveBroadcast', params: { roomId } })
}

onMounted(() => {
  fetchRooms()
  pollTimer = window.setInterval(fetchRooms, 5000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.live-home {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 0.02em;
}

.title-icon {
  font-size: 32px;
  color: #667eea;
}

.list-card {
  border-radius: 16px;
  min-height: 200px;
}

.room-id-code {
  font-size: 13px;
  background: #f4f4f5;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 8px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    width: 100%;
    justify-content: stretch;
  }

  .header-actions .el-button {
    flex: 1;
  }
}
</style>
