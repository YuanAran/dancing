import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

/**
 * WebSocket工具类 - 用于视频通话信令
 */
export class WebSocketClient {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.subscriptions = new Map()
  }

  /**
   * 连接到WebSocket服务器
   */
  connect(token, onConnected, onError) {
    if (this.connected) {
      if (onConnected) onConnected()
      return
    }

    // 使用SockJS连接
    const socket = new SockJS('https://192.168.1.113:8080/ws/video-call')
    this.stompClient = Stomp.over(socket)

    // 禁用调试信息
    this.stompClient.debug = () => {}

    // 连接
    this.stompClient.connect(
      {
        Authorization: token
      },
      () => {
        this.connected = true
        if (onConnected) onConnected()
      },
      (error) => {
        this.connected = false
        if (onError) onError(error)
      }
    )
  }

  /**
   * 订阅房间消息
   */
  subscribeToRoom(roomId, callback) {
    if (!this.connected || !this.stompClient) {
      console.error('WebSocket未连接')
      return null
    }

    const subscription = this.stompClient.subscribe(`/topic/room/${roomId}`, (message) => {
      try {
        const data = JSON.parse(message.body)
        if (callback) callback(data)
      } catch (error) {
        console.error('解析消息失败:', error)
      }
    })

    this.subscriptions.set(roomId, subscription)
    return subscription
  }

  /**
   * 发送信令消息
   */
  sendSignal(roomId, message) {
    if (!this.connected || !this.stompClient) {
      console.error('WebSocket未连接')
      return false
    }

    const payload = {
      ...message,
      roomId: roomId
    }

    this.stompClient.send('/app/video-call/signal', {}, JSON.stringify(payload))
    return true
  }

  /**
   * 取消订阅房间
   */
  unsubscribeFromRoom(roomId) {
    const subscription = this.subscriptions.get(roomId)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(roomId)
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    // 取消所有订阅
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe()
    })
    this.subscriptions.clear()

    // 断开连接
    if (this.stompClient && this.connected) {
      this.stompClient.disconnect()
    }

    this.connected = false
    this.stompClient = null
  }
}

