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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { WebSocketClient } from '@/utils/websocket'
import { ElMessage } from 'element-plus'
import { Phone, VideoCamera, Microphone, User } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const inCall = ref(false)
const roomId = ref('')
const currentUser = ref(null)
const remoteUser = ref('')
const isCreator = ref(false)
const hasLocalOffer = ref(false)

// 视频流
const localVideo = ref(null)
const remoteVideo = ref(null)
const localStream = ref(null)
const remoteStream = ref(null)
const isVideoEnabled = ref(true)
const isAudioEnabled = ref(true)

// WebRTC
let peerConnection = null
let wsClient = null
let queuedOffer = null
let queuedAnswer = null
let queuedIceCandidates = []
let remoteJoined = false

// 表单
const roomForm = ref({
  roomId: '',
  targetUserId: null
})

// STUN服务器配置
const rtcConfiguration = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' }
  ]
}

// 初始化
onMounted(async () => {
  currentUser.value = userStore.user
  if (!currentUser.value) {
    await userStore.getCurrentUser()
    currentUser.value = userStore.user
  }

  // 从路由参数获取房间ID或目标用户ID
  if (route.query.roomId) {
    roomForm.value.roomId = route.query.roomId
  }
  if (route.query.targetUserId) {
    roomForm.value.targetUserId = parseInt(route.query.targetUserId)
  }

  // 如果有房间ID或目标用户ID，自动加入
  if (route.query.roomId || route.query.targetUserId) {
    createOrJoinRoom()
  }
})

// 清理
onBeforeUnmount(() => {
  endCall()
})

// 创建或加入房间
const createOrJoinRoom = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post(
      'https://192.168.1.12:8080/api/video-call/create-room',
      roomForm.value,
      {
        headers: { Authorization: token }
      }
    )

    if (response.data.code === 200) {
      const data = response.data.data || {}
      roomId.value = data.roomId
      isCreator.value = !!data.isCreator
      hasLocalOffer.value = false
      inCall.value = true
      
      // 连接WebSocket
      await connectWebSocket()
      
      // 初始化WebRTC
      await initWebRTC()
      
      ElMessage.success('已加入房间')
    } else {
      ElMessage.error(response.data.message || '加入房间失败')
    }
  } catch (error) {
    console.error('加入房间失败:', error)
    ElMessage.error('加入房间失败')
  } finally {
    loading.value = false
  }
}

// 连接WebSocket
const connectWebSocket = () => {
  return new Promise((resolve, reject) => {
    const token = localStorage.getItem('token')
    wsClient = new WebSocketClient()
    
    wsClient.connect(
      token,
      () => {
        // 订阅房间消息
        wsClient.subscribeToRoom(roomId.value, handleSignalMessage)
        resolve()
      },
      (error) => {
        console.error('WebSocket连接失败:', error)
        reject(error)
      }
    )
  })
}

// 处理信令消息
const handleSignalMessage = async (message) => {
  const { type } = message

  if (type === 'offer') {
    if (!peerConnection) {
      queuedOffer = message
      return
    }
    await processOffer(message)
  } else if (type === 'answer') {
    if (!peerConnection) {
      queuedAnswer = message
      return
    }
    await processAnswer(message)
  } else if (type === 'ice-candidate') {
    const { candidate } = message
    // 收到ICE候选，添加到连接
    await addIceCandidateSafely(candidate)
  } else if (type === 'user-joined') {
    const { userId, username } = message
    if (userId === currentUser.value?.id) {
      return
    }
    remoteJoined = true
    remoteUser.value = username
    ElMessage.info(`${username} 加入了通话`)
    await maybeCreateOffer()
  } else if (type === 'user-left') {
    const { userId, username } = message
    if (userId === currentUser.value?.id) {
      return
    }
    remoteJoined = false
    ElMessage.info(`${username} 离开了通话`)
    if (remoteStream.value) {
      remoteStream.value.getTracks().forEach(track => track.stop())
      remoteStream.value = null
    }
  }
}

const addIceCandidateSafely = async (candidate) => {
  if (!candidate) {
    return
  }

  const iceCandidate = new RTCIceCandidate(candidate)

  if (!peerConnection || !peerConnection.remoteDescription) {
    queuedIceCandidates.push(iceCandidate)
    return
  }

  await peerConnection.addIceCandidate(iceCandidate)
}

const drainIceCandidates = async () => {
  if (!peerConnection || !peerConnection.remoteDescription || queuedIceCandidates.length === 0) {
    return
  }

  const candidates = queuedIceCandidates
  queuedIceCandidates = []

  for (const candidate of candidates) {
    await peerConnection.addIceCandidate(candidate)
  }
}

const maybeCreateOffer = async () => {
  if (!peerConnection || !wsClient || !roomId.value) {
    return
  }

  if (!isCreator.value || hasLocalOffer.value || !remoteJoined) {
    return
  }

  const offer = await peerConnection.createOffer()
  await peerConnection.setLocalDescription(offer)

  wsClient.sendSignal(roomId.value, {
    type: 'offer',
    sdp: offer,
    userId: currentUser.value.id
  })

  hasLocalOffer.value = true
}

const processOffer = async ({ sdp, userId, username }) => {
  if (!peerConnection || isCreator.value) {
    return
  }

  if (peerConnection.signalingState !== 'stable') {
    console.warn('当前状态非stable，忽略offer', peerConnection.signalingState)
    return
  }

  await peerConnection.setRemoteDescription(new RTCSessionDescription(sdp))
  await drainIceCandidates()
  const answer = await peerConnection.createAnswer()
  await peerConnection.setLocalDescription(answer)

  wsClient.sendSignal(roomId.value, {
    type: 'answer',
    sdp: answer,
    userId: currentUser.value.id
  })

  if (userId !== currentUser.value?.id) {
    remoteUser.value = username
  }
}

const processAnswer = async ({ sdp }) => {
  if (!peerConnection || !isCreator.value) {
    return
  }

  if (peerConnection.signalingState !== 'have-local-offer') {
    console.warn('当前状态非have-local-offer，忽略answer', peerConnection.signalingState)
    return
  }

  await peerConnection.setRemoteDescription(new RTCSessionDescription(sdp))
  await drainIceCandidates()
}

// 初始化WebRTC
const initWebRTC = async () => {
  try {
    // 获取本地媒体流
    localStream.value = await navigator.mediaDevices.getUserMedia({
      video: true,
      audio: true
    })

    await nextTick()

    if (localVideo.value) {
      localVideo.value.srcObject = localStream.value
      localVideo.value.play?.().catch(() => {})
    }

    // 创建RTCPeerConnection
    peerConnection = new RTCPeerConnection(rtcConfiguration)

    peerConnection.onconnectionstatechange = () => {
      console.log('pc.connectionState:', peerConnection?.connectionState)
    }

    peerConnection.oniceconnectionstatechange = () => {
      console.log('pc.iceConnectionState:', peerConnection?.iceConnectionState)
    }

    peerConnection.onsignalingstatechange = () => {
      console.log('pc.signalingState:', peerConnection?.signalingState)
    }

    peerConnection.onicegatheringstatechange = () => {
      console.log('pc.iceGatheringState:', peerConnection?.iceGatheringState)
    }

    // 添加本地流轨道
    localStream.value.getTracks().forEach(track => {
      peerConnection.addTrack(track, localStream.value)
    })

    // 处理远程流
    peerConnection.ontrack = (event) => {
      remoteStream.value = event.streams[0]
      if (remoteVideo.value) {
        remoteVideo.value.srcObject = remoteStream.value
        remoteVideo.value.play?.().catch(() => {})
      }
    }

    // 处理ICE候选
    peerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        wsClient.sendSignal(roomId.value, {
          type: 'ice-candidate',
          candidate: event.candidate,
          userId: currentUser.value.id
        })
      }
    }

    // 发送加入通知
    wsClient.sendSignal(roomId.value, {
      type: 'user-joined',
      userId: currentUser.value.id,
      username: currentUser.value.username
    })

    await maybeCreateOffer()

    if (!isCreator.value && queuedOffer) {
      const cachedOffer = queuedOffer
      queuedOffer = null
      await processOffer(cachedOffer)
    }

    if (isCreator.value && queuedAnswer) {
      const cachedAnswer = queuedAnswer
      queuedAnswer = null
      await processAnswer(cachedAnswer)
    }
  } catch (error) {
    console.error('初始化WebRTC失败:', error)
    ElMessage.error('无法访问摄像头或麦克风')
  }
}

// 切换视频
const toggleVideo = () => {
  if (localStream.value) {
    const videoTrack = localStream.value.getVideoTracks()[0]
    if (videoTrack) {
      videoTrack.enabled = !isVideoEnabled.value
      isVideoEnabled.value = !isVideoEnabled.value
    }
  }
}

// 切换音频
const toggleAudio = () => {
  if (localStream.value) {
    const audioTrack = localStream.value.getAudioTracks()[0]
    if (audioTrack) {
      audioTrack.enabled = !isAudioEnabled.value
      isAudioEnabled.value = !isAudioEnabled.value
    }
  }
}

// 结束通话
const endCall = async () => {
  try {
    // 发送离开通知
    if (wsClient && roomId.value) {
      const token = localStorage.getItem('token')
      await axios.post(
        'https://192.168.1.12:8080/api/video-call/leave-room',
        { roomId: roomId.value },
        { headers: { Authorization: token } }
      )
    }

    // 停止本地流
    if (localStream.value) {
      localStream.value.getTracks().forEach(track => track.stop())
      localStream.value = null
    }

    // 停止远程流
    if (remoteStream.value) {
      remoteStream.value.getTracks().forEach(track => track.stop())
      remoteStream.value = null
    }

    // 关闭WebRTC连接
    if (peerConnection) {
      peerConnection.close()
      peerConnection = null
    }

    // 断开WebSocket
    if (wsClient) {
      wsClient.disconnect()
      wsClient = null
    }

    inCall.value = false
    roomId.value = ''
    remoteUser.value = ''
    isCreator.value = false
    hasLocalOffer.value = false
    queuedOffer = null
    queuedAnswer = null
    queuedIceCandidates = []
    remoteJoined = false

    ElMessage.success('已结束通话')
    router.push('/')
  } catch (error) {
    console.error('结束通话失败:', error)
  }
}
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

