<template>
  <div class="live-watch">
    <div class="watch-top">
      <el-button text type="primary" @click="handleLeave">
        <el-icon><ArrowLeft /></el-icon>
        离开直播间
      </el-button>
      <div class="watch-meta">
        <h1 class="room-title">{{ displayTitle }}</h1>
        <span class="room-id">房间 {{ roomId }}</span>
      </div>
      <div class="spacer" />
    </div>

    <div class="watch-body">
      <div class="video-panel" v-loading="loading" element-loading-text="正在连接…">
        <div class="video-wrap">
          <video
            ref="remoteVideo"
            class="host-video"
            autoplay
            playsinline
          />
          <div v-if="inCall && !remoteStream" class="video-wait">
            <el-icon class="wait-icon"><VideoCamera /></el-icon>
            <p>等待主播画面…</p>
          </div>
        </div>
      </div>

      <aside class="chat-panel">
        <div class="chat-header">
          <el-icon><ChatDotRound /></el-icon>
          <span>聊天</span>
        </div>
        <div ref="chatListRef" class="chat-list">
          <div
            v-for="(c, idx) in chatMessages"
            :key="idx"
            class="chat-item"
          >
            <span class="chat-user">{{ c.user }}</span>
            <span class="chat-text">{{ c.text }}</span>
            <span class="chat-time">{{ c.time }}</span>
          </div>
          <div v-if="chatMessages.length === 0" class="chat-empty">暂无消息</div>
        </div>
        <div class="chat-input-row">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="说点什么…"
            @keydown.enter.exact.prevent="send"
          />
          <el-button type="primary" :disabled="!draft.trim()" @click="send">发送</el-button>
        </div>
      </aside>
    </div>

    <el-card class="music-card" shadow="never">
      <template #header>
        <div class="music-head">
          <span>直播音乐</span>
          <span class="music-now">{{ nowPlayingTitle || '未播放' }}</span>
        </div>
      </template>
      <audio ref="viewerMusicAudio" class="viewer-music-audio" controls preload="metadata" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLiveRoomStore } from '@/stores/liveRoom'
import { useVideoRoomWebRTC } from '@/composables/useVideoRoomWebRTC'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ChatDotRound, VideoCamera } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const liveRoomStore = useLiveRoomStore()

const {
  loading,
  inCall,
  remoteVideo,
  remoteStream,
  joinRoom,
  leaveRoom,
  sendChat
} = useVideoRoomWebRTC()

const roomId = computed(() => route.params.roomId)
const meta = computed(() => liveRoomStore.getRoom(roomId.value))
const displayTitle = computed(() => meta.value?.title || '直播间')

const chatMessages = ref([])
const chatListRef = ref(null)
const draft = ref('')
const viewerMusicAudio = ref(null)
const nowPlayingTitle = ref('')

function padTime(d) {
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function appendChat(msg) {
  const text = (msg.text || '').trim()
  if (!text) return
  chatMessages.value.push({
    user: msg.username || '观众',
    text,
    time: padTime(new Date(msg.ts || Date.now()))
  })
  nextTick(() => {
    const el = chatListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function send() {
  const t = draft.value.trim()
  if (!t) return
  if (sendChat(t)) {
    draft.value = ''
  }
}

const getAudioUrl = (path) => {
  if (!path) return ''
  const cleanPath = path.replace(/^[/\\]+/, '').replace(/\\/g, '/')
  return `https://192.168.1.12:8080/api/files/audio?path=${encodeURIComponent(cleanPath)}`
}

const applyMusicControl = async (msg) => {
  const audio = viewerMusicAudio.value
  if (!audio) return
  const action = msg.action

  if (action === 'change' && msg.musicPath) {
    audio.src = getAudioUrl(msg.musicPath)
    nowPlayingTitle.value = msg.musicTitle || '直播音乐'
    audio.load()
    if (typeof msg.currentTime === 'number') {
      audio.currentTime = Math.max(0, msg.currentTime)
    }
    return
  }
  if (typeof msg.currentTime === 'number') {
    audio.currentTime = Math.max(0, msg.currentTime)
  }
  if (action === 'play') {
    try {
      await audio.play()
    } catch (e) {
      // 某些浏览器自动播放受限，保留 controls 允许用户手动播放
    }
  } else if (action === 'pause') {
    audio.pause()
  }
}

onMounted(async () => {
  if (!roomId.value) {
    router.replace('/live')
    return
  }

  const ok = await joinRoom(roomId.value, {
    silentToast: true,
    mode: 'viewer',
    onUserJoined: () => {},
    onUserLeft: () => {},
    onChatMessage: appendChat,
    onMusicControl: applyMusicControl
  })

  if (!ok) {
    router.push('/live')
  }
})

onUnmounted(async () => {
  if (viewerMusicAudio.value) {
    viewerMusicAudio.value.pause()
  }
  if (inCall.value) {
    await leaveRoom({ skipMessage: true })
  }
})

watch(chatMessages, () => {
  nextTick(() => {
    const el = chatListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}, { deep: true })

const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定离开直播间吗？', '提示', {
      confirmButtonText: '离开',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await leaveRoom({ skipMessage: true })
    ElMessage.success('已离开')
    router.push('/live')
  } catch {
    /* cancel */
  }
}
</script>

<style scoped>
.live-watch {
  max-width: 1100px;
  margin: 0 auto;
}

.watch-top {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.watch-meta {
  flex: 1;
  text-align: center;
  min-width: 200px;
}

.room-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}

.room-id {
  font-size: 13px;
  color: #909399;
}

.spacer {
  width: 80px;
}

.watch-body {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
  align-items: stretch;
}

.music-card {
  margin-top: 16px;
  border-radius: 12px;
}

.music-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.music-now {
  color: #606266;
  font-size: 13px;
}

.viewer-music-audio {
  width: 100%;
}

.video-panel {
  border-radius: 16px;
  overflow: hidden;
  background: #0d1117;
  min-height: 360px;
}

.video-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
}

.host-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.video-wait {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
}

.wait-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.chat-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 360px;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px;
  max-height: 360px;
  min-height: 200px;
}

.chat-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 8px 10px;
  align-items: baseline;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.chat-user {
  color: #667eea;
  font-weight: 600;
  white-space: nowrap;
}

.chat-text {
  color: #303133;
  word-break: break-word;
}

.chat-time {
  font-size: 12px;
  color: #c0c4cc;
  white-space: nowrap;
}

.chat-empty {
  text-align: center;
  color: #c0c4cc;
  font-size: 14px;
  padding: 24px 8px;
}

.chat-input-row {
  padding: 12px;
  border-top: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@media (max-width: 900px) {
  .watch-body {
    grid-template-columns: 1fr;
  }

  .chat-list {
    max-height: 240px;
  }
}
</style>
