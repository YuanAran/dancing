import { ref, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { WebSocketClient } from '@/utils/websocket'
import { videoCallApi } from '@/api'
import { ElMessage } from 'element-plus'

const rtcConfiguration = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' }
  ]
}

export function useVideoRoomWebRTC() {
  const userStore = useUserStore()

  const loading = ref(false)
  const inCall = ref(false)
  const roomId = ref('')
  const currentUser = ref(null)
  const remoteUser = ref('')
  const isCreator = ref(false)
  const hasLocalOffer = ref(false)

  const localVideo = ref(null)
  const remoteVideo = ref(null)
  const localStream = ref(null)
  const remoteStream = ref(null)
  const isVideoEnabled = ref(true)
  const isAudioEnabled = ref(true)

  let peerConnection = null
  let wsClient = null
  let queuedOffer = null
  let queuedAnswer = null
  let queuedIceCandidates = []
  let remoteJoined = false
  let mediaMode = 'publish'

  const onUserJoined = ref(null)
  const onUserLeft = ref(null)
  const onChatMessage = ref(null)
  const onMusicControl = ref(null)

  const addIceCandidateSafely = async (candidate) => {
    if (!candidate) return
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

  const processOffer = async ({ sdp, userId, username }) => {
    if (!peerConnection || isCreator.value) return
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
    if (!peerConnection || !isCreator.value) return
    if (peerConnection.signalingState !== 'have-local-offer') {
      console.warn('当前状态非have-local-offer，忽略answer', peerConnection.signalingState)
      return
    }
    await peerConnection.setRemoteDescription(new RTCSessionDescription(sdp))
    await drainIceCandidates()
  }

  const maybeCreateOffer = async () => {
    if (!peerConnection || !wsClient || !roomId.value) return
    if (!isCreator.value || hasLocalOffer.value || !remoteJoined) return
    const offer = await peerConnection.createOffer()
    await peerConnection.setLocalDescription(offer)
    wsClient.sendSignal(roomId.value, {
      type: 'offer',
      sdp: offer,
      userId: currentUser.value.id
    })
    hasLocalOffer.value = true
  }

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
      await addIceCandidateSafely(candidate)
    } else if (type === 'live-chat') {
      if (onChatMessage.value) {
        onChatMessage.value(message)
      }
    } else if (type === 'live-music-control') {
      if (onMusicControl.value) {
        onMusicControl.value(message)
      }
    } else if (type === 'user-joined') {
      const { userId, username } = message
      if (userId === currentUser.value?.id) return
      remoteJoined = true
      remoteUser.value = username
      if (onUserJoined.value) onUserJoined.value({ userId, username })
      else ElMessage.info(`${username} 加入了通话`)
      await maybeCreateOffer()
    } else if (type === 'user-left') {
      const { userId, username } = message
      if (userId === currentUser.value?.id) return
      remoteJoined = false
      if (onUserLeft.value) onUserLeft.value({ userId, username })
      else ElMessage.info(`${username} 离开了通话`)
      if (remoteStream.value) {
        remoteStream.value.getTracks().forEach((track) => track.stop())
        remoteStream.value = null
      }
    }
  }

  const connectWebSocket = () => {
    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('token')
      wsClient = new WebSocketClient()
      wsClient.connect(
        token,
        () => {
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

  const initWebRTC = async () => {
    peerConnection = new RTCPeerConnection(rtcConfiguration)

    peerConnection.ontrack = (event) => {
      remoteStream.value = event.streams[0]
      if (remoteVideo.value) {
        remoteVideo.value.srcObject = remoteStream.value
        remoteVideo.value.play?.().catch(() => {})
      }
    }

    peerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        wsClient.sendSignal(roomId.value, {
          type: 'ice-candidate',
          candidate: event.candidate,
          userId: currentUser.value.id
        })
      }
    }

    if (mediaMode === 'viewer') {
      peerConnection.addTransceiver('video', { direction: 'recvonly' })
      peerConnection.addTransceiver('audio', { direction: 'recvonly' })
      localStream.value = null
    } else {
      localStream.value = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
      })

      await nextTick()

      if (localVideo.value) {
        localVideo.value.srcObject = localStream.value
        localVideo.value.play?.().catch(() => {})
      }

      localStream.value.getTracks().forEach((track) => {
        peerConnection.addTrack(track, localStream.value)
      })
    }

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
  }

  const joinRoom = async (roomIdArg, options = {}) => {
    loading.value = true
    mediaMode = options.mode === 'viewer' ? 'viewer' : 'publish'
    onUserJoined.value = options.onUserJoined || null
    onUserLeft.value = options.onUserLeft || null
    onChatMessage.value = options.onChatMessage || null
    onMusicControl.value = options.onMusicControl || null
    try {
      currentUser.value = userStore.user
      if (!currentUser.value) {
        await userStore.getCurrentUser()
        currentUser.value = userStore.user
      }
      if (!currentUser.value) {
        ElMessage.error('请先登录')
        return false
      }

      const payload = {
        roomId: roomIdArg || '',
        targetUserId: options.targetUserId ?? null,
        joinOnly: mediaMode === 'viewer'
      }

      const response = await videoCallApi.createRoom(payload)
      if (response.code !== 200) {
        ElMessage.error(response.message || '加入房间失败')
        return false
      }

      const data = response.data || {}
      roomId.value = data.roomId
      isCreator.value = !!data.isCreator
      hasLocalOffer.value = false
      inCall.value = true
      remoteJoined = false

      await connectWebSocket()
      await initWebRTC()

      if (!options.silentToast) {
        ElMessage.success(mediaMode === 'viewer' ? '已进入直播间' : '已加入房间')
      }
      return true
    } catch (error) {
      console.error('加入房间失败:', error)
      ElMessage.error('加入房间失败')
      return false
    } finally {
      loading.value = false
    }
  }

  const sendChat = (text) => {
    const t = String(text || '').trim()
    if (!t || !wsClient || !roomId.value || !currentUser.value) return false
    const payload = {
      type: 'live-chat',
      text: t.slice(0, 500),
      userId: currentUser.value.id,
      username: currentUser.value.username,
      ts: Date.now()
    }
    wsClient.sendSignal(roomId.value, payload)
    return true
  }

  const sendMusicControl = (payload = {}) => {
    if (!wsClient || !roomId.value || !currentUser.value) return false
    wsClient.sendSignal(roomId.value, {
      type: 'live-music-control',
      userId: currentUser.value.id,
      username: currentUser.value.username,
      ts: Date.now(),
      ...payload
    })
    return true
  }

  const toggleVideo = () => {
    if (!localStream.value) return
    const videoTrack = localStream.value.getVideoTracks()[0]
    if (videoTrack) {
      videoTrack.enabled = !isVideoEnabled.value
      isVideoEnabled.value = !isVideoEnabled.value
    }
  }

  const toggleAudio = () => {
    if (!localStream.value) return
    const audioTrack = localStream.value.getAudioTracks()[0]
    if (audioTrack) {
      audioTrack.enabled = !isAudioEnabled.value
      isAudioEnabled.value = !isAudioEnabled.value
    }
  }

  const leaveRoom = async (options = {}) => {
    const { skipApi = false, skipMessage = false } = options
    try {
      if (!skipApi && wsClient && roomId.value) {
        await videoCallApi.leaveRoom({ roomId: roomId.value })
      }
    } catch (e) {
      console.error('leave-room:', e)
    }

    if (localStream.value) {
      localStream.value.getTracks().forEach((track) => track.stop())
      localStream.value = null
    }
    if (remoteStream.value) {
      remoteStream.value.getTracks().forEach((track) => track.stop())
      remoteStream.value = null
    }
    if (peerConnection) {
      peerConnection.close()
      peerConnection = null
    }
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
    onUserJoined.value = null
    onUserLeft.value = null
    onChatMessage.value = null
    onMusicControl.value = null
    mediaMode = 'publish'

    if (!skipMessage) {
      ElMessage.success('已结束通话')
    }
  }

  return {
    loading,
    inCall,
    roomId,
    currentUser,
    remoteUser,
    isCreator,
    localVideo,
    remoteVideo,
    localStream,
    remoteStream,
    isVideoEnabled,
    isAudioEnabled,
    joinRoom,
    leaveRoom,
    sendChat,
    sendMusicControl,
    toggleVideo,
    toggleAudio
  }
}
