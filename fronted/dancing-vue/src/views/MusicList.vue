<template>
  <div class="music-list-container">
    <div class="page-header">
      <h1>
        <el-icon><Headset /></el-icon>
        在线音乐
      </h1>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索音乐..."
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
          @click="$router.push('/music/upload')"
        >
          <el-icon><Upload /></el-icon>
          上传音乐
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col
        :xs="24"
        :sm="12"
        :md="8"
        v-for="music in musics"
        :key="music.id"
        class="music-col"
      >
        <el-card class="music-card">
          <h3 class="music-title">{{ music.title }}</h3>
          <p class="music-description">{{ music.description }}</p>

          <audio
            class="audio-player"
            controls
            :src="getAudioUrl(music.filePath)"
            preload="none"
          />

          <div class="music-meta">
            <span>
              <el-icon><User /></el-icon>
              {{ music.uploaderName }}
            </span>
            <span>
              <el-icon><Calendar /></el-icon>
              {{ formatDate(music.createdAt) }}
            </span>
          </div>

          <div class="music-actions">
            <el-button
              v-if="userStore.isLoggedIn && music.uploaderId === userStore.user?.id"
              type="danger"
              size="small"
              @click="deleteMusic(music.id)"
              :loading="deletingMusicId === music.id"
            >
              删除
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="!loading && musics.length === 0" class="empty-state">
      <el-empty description="暂无音乐">
        <el-button
          v-if="userStore.isLoggedIn"
          type="primary"
          @click="$router.push('/music/upload')"
        >
          上传第一首音乐
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { musicApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const musics = ref([])
const searchKeyword = ref('')
const deletingMusicId = ref(null)

const getAudioUrl = (path) => {
  if (!path) return ''
  const cleanPath = path.replace(/^[/\\]+/, '').replace(/\\/g, '/')
  return `https://192.168.1.12:8080/api/files/audio?path=${encodeURIComponent(cleanPath)}`
}

const getMusicList = async () => {
  loading.value = true
  try {
    const response = searchKeyword.value.trim()
      ? await musicApi.searchMusics(searchKeyword.value.trim())
      : await musicApi.getMusicList()

    if (response.code === 200) {
      musics.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取音乐列表失败')
    }
  } catch (error) {
    ElMessage.error('获取音乐列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  getMusicList()
}

const deleteMusic = async (musicId) => {
  try {
    await ElMessageBox.confirm('确定要删除这首音乐吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    deletingMusicId.value = musicId
    const response = await musicApi.deleteMusic(musicId)
    if (response.code === 200) {
      ElMessage.success('音乐删除成功')
      getMusicList()
    } else {
      ElMessage.error(response.message || '音乐删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('音乐删除失败')
    }
  } finally {
    deletingMusicId.value = null
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('zh-CN')
}

onMounted(() => {
  getMusicList()
})
</script>

<style scoped>
.music-list-container {
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
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 280px;
}

.music-col {
  margin-bottom: 16px;
}

.music-card {
  min-height: 240px;
}

.music-title {
  margin: 0 0 8px 0;
}

.music-description {
  color: #666;
  min-height: 40px;
}

.audio-player {
  width: 100%;
  margin: 12px 0;
}

.music-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #888;
  margin-bottom: 10px;
}

.music-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.music-actions {
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .header-actions {
    width: 100%;
    flex-direction: column;
  }
  .search-input {
    width: 100%;
  }
}
</style>
