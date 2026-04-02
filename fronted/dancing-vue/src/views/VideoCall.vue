<template>
  <div class="video-call-container">
    <el-card class="call-card">
      <template #header>
        <div class="call-header">
          <h2>视频通话</h2>
          <el-button v-if="inCall" type="danger" @click="endCall">
            <el-icon><Phone /></el-icon>
            结束通话
          </el-button>
        </div>
      </template>

      <!-- 房间信息 -->
      <div v-if="!inCall" class="room-setup">
        <el-form :model="roomForm" label-width="100px">
          <el-form-item label="房间ID">
            <el-input v-model="roomForm.roomId" placeholder="留空自动生成" />
          </el-form-item>
          <el-form-item label="目标用户ID">
            <el-input-number v-model="roomForm.targetUserId" :min="1" placeholder="可选" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="createOrJoinRoom" :loading="loading">
              创建/加入房间
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 视频通话区域 -->
      <div v-if="inCall" class="video-call-area">
        <div class="video-grid">
          <!-- 本地视频 -->
          <div class="video-wrapper local-video">
            <video ref="localVideo" autoplay muted playsinline class="video-element"></video>
            <div class="video-label">我 ({{ currentUser?.username }})</div>
            <div class="video-controls">
              <el-button
                :type="isVideoEnabled ? 'primary' : 'danger'"
                circle
                @click="toggleVideo"
                :title="isVideoEnabled ? '关闭摄像头' : '开启摄像头'"
              >
                <el-icon><VideoCamera /></el-icon>
              </el-button>
              <el-button
                :type="isAudioEnabled ? 'primary' : 'danger'"
                circle
                @click="toggleAudio"
                :title="isAudioEnabled ? '关闭麦克风' : '开启麦克风'"
              >
                <el-icon><Microphone /></el-icon>
              </el-button>
            </div>
          </div>

          <!-- 远程视频 -->
          <div class="video-wrapper remote-video">
            <video ref="remoteVideo" autoplay playsinline class="video-element"></video>
            <div class="video-label">对方</div>
            <div v-if="!remoteStream" class="waiting">
              <el-icon size="64"><User /></el-icon>
              <p>等待对方加入...</p>
            </div>
          </div>
        </div>

        <!-- 通话信息 -->
        <div class="call-info">
          <p>房间ID: {{ roomId }}</p>
          <p v-if="remoteUser">对方: {{ remoteUser }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useVideoRoomWebRTC } from '@/composables/useVideoRoomWebRTC'
import { Phone, VideoCamera, Microphone, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const {
  loading,
  inCall,
  roomId,
  currentUser,
  remoteUser,
  localVideo,
  remoteVideo,
  remoteStream,
  isVideoEnabled,
  isAudioEnabled,
  joinRoom,
  leaveRoom,
  toggleVideo,
  toggleAudio
} = useVideoRoomWebRTC()

const roomForm = ref({
  roomId: '',
  targetUserId: null
})

const createOrJoinRoom = async () => {
  const ok = await joinRoom(roomForm.value.roomId, {
    targetUserId: roomForm.value.targetUserId
  })
  if (ok) {
    roomForm.value.roomId = roomId.value
  }
}

const endCall = async () => {
  await leaveRoom()
  router.push('/')
}

onMounted(async () => {
  if (route.query.roomId) {
    roomForm.value.roomId = route.query.roomId
  }
  if (route.query.targetUserId) {
    roomForm.value.targetUserId = parseInt(route.query.targetUserId, 10)
  }

  if (route.query.roomId || route.query.targetUserId) {
    await createOrJoinRoom()
  }
})

onBeforeUnmount(() => {
  if (inCall.value) {
    leaveRoom({ skipMessage: true })
  }
})
</script>

<style scoped>
.video-call-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.call-card {
  min-height: 600px;
}

.call-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.call-header h2 {
  margin: 0;
}

.room-setup {
  padding: 40px 0;
}

.video-call-area {
  padding: 20px 0;
}

.video-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.video-wrapper {
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16/9;
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-label {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.video-controls {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.waiting {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: white;
}

.waiting p {
  margin-top: 10px;
}

.call-info {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.call-info p {
  margin: 5px 0;
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style>
