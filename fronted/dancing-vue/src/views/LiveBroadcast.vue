<template>
  <div class="live-broadcast">
    <div class="broadcast-top">
      <el-button text type="primary" @click="handleLeave">
        <el-icon><ArrowLeft /></el-icon>
        退出直播
      </el-button>
      <div class="live-meta">
        <el-tag type="danger" effect="dark" round class="live-tag">
          <span class="pulse" />
          LIVE
        </el-tag>
        <h1 class="room-title">{{ displayTitle }}</h1>
        <span class="room-id">ID: {{ roomId }}</span>
        <el-tag v-if="inCall" size="small" type="info" class="rtc-state">
          {{ connectionHint }}
        </el-tag>
      </div>
      <div class="viewer-box">
        <el-icon><User /></el-icon>
        <span class="viewer-label">在线观众</span>
        <strong class="viewer-count">{{ viewerCount }}</strong>
      </div>
    </div>

    <div class="broadcast-body">
      <div class="stage-panel" v-loading="loading" element-loading-text="连接中…">
        <div class="stage-inner">
          <div class="stage-video-wrap">
            <video
              ref="localVideo"
              class="stage-video"
              autoplay
              muted
              playsinline
            />
            <div v-if="!inCall && !loading" class="stage-fallback">
              <el-icon class="stage-icon"><VideoCamera /></el-icon>
              <p>无法进入直播，请返回重试</p>
            </div>
            <div class="stage-bar">
              <span class="bar-name">{{ currentUser?.username || '主播' }}</span>
              <div class="bar-actions">
                <el-button
                  :type="isVideoEnabled ? 'primary' : 'danger'"
                  circle
                  size="small"
                  @click="toggleVideo"
                  :title="isVideoEnabled ? '关闭摄像头' : '开启摄像头'"
                >
                  <el-icon><VideoCamera /></el-icon>
                </el-button>
                <el-button
                  :type="isAudioEnabled ? 'primary' : 'danger'"
                  circle
                  size="small"
                  @click="toggleAudio"
                  :title="isAudioEnabled ? '关闭麦克风' : '开启麦克风'"
                >
                  <el-icon><Microphone /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <video
            ref="remoteVideo"
            class="webrtc-hidden"
            autoplay
            playsinline
          />
        </div>
      </div>

      <aside class="chat-panel">
        <div class="chat-header">
          <el-icon><ChatDotRound /></el-icon>
          <span>公屏</span>
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
            v-model="chatDraft"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="输入消息"
            @keydown.enter.exact.prevent="sendHostChat"
          />
          <el-button type="primary" :disabled="!chatDraft.trim()" @click="sendHostChat">发送</el-button>
        </div>
      </aside>
    </div>

    <el-card class="music-panel" shadow="never">
      <template #header>
        <div class="music-header">
          <span>直播间音乐</span>
          <el-tag size="small" type="success">主播控制，全员同步</el-tag>
        </div>
      </template>
      <div class="music-body">
        <el-select
          v-model="selectedMusicId"
          placeholder="选择要播放的音乐"
          filterable
          clearable
          class="music-select"
          @change="handleMusicChange"
        >
          <el-option
            v-for="item in musicList"
            :key="item.id"
            :label="item.title"
            :value="item.id"
          />
        </el-select>
        <audio
          ref="hostMusicAudio"
          class="host-music-audio"
          controls
          preload="metadata"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLiveRoomStore } from '@/stores/liveRoom'
import { useVideoRoomWebRTC } from '@/composables/useVideoRoomWebRTC'
import { musicApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ChatDotRound, Microphone, User, VideoCamera } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const liveRoomStore = useLiveRoomStore()

const {
  loading,
  inCall,
  currentUser,
  localVideo,
  remoteVideo,
  remoteStream,
  isVideoEnabled,
  isAudioEnabled,
  joinRoom,
  leaveRoom,
  sendChat,
  sendMusicControl,
  toggleVideo,
  toggleAudio
} = useVideoRoomWebRTC()

const roomId = computed(() => route.params.roomId)
const meta = computed(() => liveRoomStore.getRoom(roomId.value))
const displayTitle = computed(() => meta.value?.title || '直播间')

const viewerIds = ref(new Set())
const viewerCount = computed(() => viewerIds.value.size)

const connectionHint = computed(() => {
  if (!inCall.value) return ''
  if (remoteStream.value) return '已连接'
  return '等待中'
})

const chatMessages = ref([])
const chatDraft = ref('')
const chatListRef = ref(null)
const hostMusicAudio = ref(null)
const musicList = ref([])
const selectedMusicId = ref(null)

function padTime(d) {
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function appendChat(msg) {
  const text = (msg.text || '').trim()
  if (!text) return
  chatMessages.value.push({
    user: msg.username || '用户',
    text,
    time: padTime(new Date(msg.ts || Date.now()))
  })
  nextTick(() => {
    const el = chatListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function sendHostChat() {
  const t = chatDraft.value.trim()
  if (!t) return
  if (sendChat(t)) {
    chatDraft.value = ''
  }
}

const getAudioUrl = (path) => {
  if (!path) return ''
  const cleanPath = path.replace(/^[/\\]+/, '').replace(/\\/g, '/')
  return `https://192.168.1.12:8080/api/files/audio?path=${encodeURIComponent(cleanPath)}`
}

function bindHostMusicEvents() {
  const audio = hostMusicAudio.value
  if (!audio) return
  audio.onplay = () => {
    sendMusicControl({
      action: 'play',
      currentTime: audio.currentTime || 0
    })
  }
  audio.onpause = () => {
    sendMusicControl({
      action: 'pause',
      currentTime: audio.currentTime || 0
    })
  }
  audio.onseeked = () => {
    sendMusicControl({
      action: 'seek',
      currentTime: audio.currentTime || 0
    })
  }
}

async function fetchMusicList() {
  try {
    const res = await musicApi.getMusicList()
    if (res.code === 200) {
      musicList.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    console.error('获取音乐列表失败:', e)
  }
}

function handleMusicChange() {
  const audio = hostMusicAudio.value
  const selected = musicList.value.find((m) => m.id === selectedMusicId.value)
  if (!audio || !selected) return

  const src = getAudioUrl(selected.filePath)
  audio.src = src
  audio.load()
  sendMusicControl({
    action: 'change',
    musicId: selected.id,
    musicTitle: selected.title,
    musicPath: selected.filePath,
    currentTime: 0
  })
}

onMounted(async () => {
  if (!roomId.value) {
    router.replace('/live')
    return
  }
  if (!meta.value) {
    liveRoomStore.registerRoom({
      roomId: roomId.value,
      title: `直播间 ${roomId.value}`
    })
  }

  const onUserJoined = ({ userId }) => {
    const next = new Set(viewerIds.value)
    next.add(userId)
    viewerIds.value = next
    const audio = hostMusicAudio.value
    const selected = musicList.value.find((m) => m.id === selectedMusicId.value)
    if (audio && selected) {
      sendMusicControl({
        action: 'change',
        musicId: selected.id,
        musicTitle: selected.title,
        musicPath: selected.filePath,
        currentTime: audio.currentTime || 0
      })
      sendMusicControl({
        action: audio.paused ? 'pause' : 'play',
        currentTime: audio.currentTime || 0
      })
    }
  }
  const onUserLeft = ({ userId }) => {
    const next = new Set(viewerIds.value)
    next.delete(userId)
    viewerIds.value = next
  }

  const ok = await joinRoom(roomId.value, {
    silentToast: true,
    mode: 'publish',
    onUserJoined,
    onUserLeft,
    onChatMessage: appendChat
  })

  if (!ok) {
    router.push('/live')
    return
  }
  await fetchMusicList()
  await nextTick()
  bindHostMusicEvents()
})

onUnmounted(async () => {
  if (hostMusicAudio.value) {
    hostMusicAudio.value.pause()
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
    await ElMessageBox.confirm('确定离开？', '提示', {
      confirmButtonText: '离开',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await leaveRoom({ skipMessage: true })
    liveRoomStore.clearRoom(roomId.value)
    ElMessage.success('已离开直播间')
    router.push('/live')
  } catch {
    /* cancel */
  }
}
</script>

<style scoped>
.live-broadcast {
  max-width: 1200px;
  margin: 0 auto;
}

.broadcast-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.live-meta {
  flex: 1;
  min-width: 200px;
  text-align: center;
}

.rtc-state {
  margin-top: 8px;
}

.live-tag {
  font-weight: 700;
  letter-spacing: 0.05em;
}

.pulse {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
  margin-right: 6px;
  animation: pulse 1.2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.85);
  }
}

.room-title {
  margin: 10px 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}

.room-id {
  font-size: 13px;
  color: #909399;
  word-break: break-all;
}

.viewer-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12px;
  font-size: 15px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.35);
}

.viewer-box .el-icon {
  font-size: 20px;
}

.viewer-label {
  opacity: 0.95;
}

.viewer-count {
  font-size: 22px;
  margin-left: 4px;
}

.broadcast-body {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  align-items: stretch;
  min-height: 420px;
}

.music-panel {
  margin-top: 16px;
  border-radius: 12px;
}

.music-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.music-body {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 14px;
  align-items: center;
}

.music-select {
  width: 100%;
}

.host-music-audio {
  width: 100%;
}

.stage-panel {
  border-radius: 16px;
  overflow: hidden;
  background: #0d1117;
  min-height: 400px;
  position: relative;
}

.stage-inner {
  position: relative;
  min-height: 400px;
}

.stage-video-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
}

.stage-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.stage-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
  padding: 24px;
}

.stage-icon {
  font-size: 56px;
  margin-bottom: 12px;
}

.stage-bar {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.75));
}

.bar-name {
  color: #fff;
  font-weight: 600;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.8);
}

.bar-actions {
  display: flex;
  gap: 10px;
}

.webrtc-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
  left: 0;
  top: 0;
}

.chat-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 400px;
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
  max-height: 520px;
}

.chat-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 8px 10px;
  align-items: baseline;
  padding: 10px 0;
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
  padding: 40px 16px;
}

.chat-input-row {
  padding: 12px;
  border-top: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;
}

@media (max-width: 900px) {
  .broadcast-body {
    grid-template-columns: 1fr;
  }

  .chat-list {
    max-height: 280px;
  }

  .music-body {
    grid-template-columns: 1fr;
  }
}
</style>
