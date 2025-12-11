import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'https://localhost:8080/api',
  timeout: 10000,
  withCredentials: true // 支持cookies
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 判断是否是公开接口（不需要Token）
    const url = config.url || ''
    const isPublicApi = url.includes('/user/register') || 
                        url.includes('/user/login') || 
                        url === '/user/current'
    
    // 如果不是公开接口，必须携带Token
    if (!isPublicApi) {
      const token = localStorage.getItem('token')
      if (!token) {
        // 如果没有Token，阻止请求并跳转到登录页
        const error = new Error('未登录，请先登录')
        error.config = config
        error.response = {
          status: 401,
          data: { code: 401, message: '未登录，请先登录' }
        }
        // 如果不在登录页，则跳转
        if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
      // 添加Token到请求头
      config.headers.Authorization = token
    } else {
      // 公开接口，如果有Token也带上（可选）
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = token
      }
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API请求错误:', error)
    // 如果返回401，清除Token并跳转到登录页
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      // 如果不在登录页，则跳转
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// 用户相关API
export const userApi = {
  // 用户注册
  register: (data) => api.post('/user/register', data),
  
  // 用户登录
  login: (data) => api.post('/user/login', data),
  
  // 获取当前用户信息
  getCurrentUser: () => api.get('/user/current'),
  
  // 用户退出
  logout: () => api.post('/user/logout'),
  
  // 更新用户信息
  updateUser: (data) => api.post('/user/update', data)
}

// 视频相关API
export const videoApi = {
  // 获取视频列表
  getVideoList: () => api.get('/videos'),
  
  // 上传视频
  uploadVideo: (formData) => api.post('/videos/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  
  // 获取视频详情
  getVideoDetail: (id) => api.get(`/videos/${id}`),
  
  // 获取我的视频
  getMyVideos: () => api.get('/videos/my'),
  
  // 删除视频
  deleteVideo: (id) => api.delete(`/videos/${id}`),
  
  // 搜索视频
  searchVideos: (keyword) => api.get(`/videos/search?keyword=${keyword}`)
}

// 好友相关API
export const friendApi = {
  // 搜索用户
  searchUsers: (keyword) => api.post('/friends/search', { keyword }),
  
  // 发送好友申请
  sendFriendRequest: (friendId) => api.post('/friends/send-request', { friendId }),
  
  // 接受好友申请
  acceptFriendRequest: (friendId) => api.post('/friends/accept', { friendId }),
  
  // 拒绝好友申请
  rejectFriendRequest: (friendId) => api.post('/friends/reject', { friendId }),
  
  // 删除好友
  deleteFriend: (friendId) => api.post('/friends/delete', { friendId }),
  
  // 获取好友列表
  getFriends: () => api.get('/friends/manage'),
  
  // 获取待处理的好友申请
  getPendingRequests: () => api.get('/friends/pending')
}

// 帖子相关API
export const postApi = {
  // 获取帖子列表
  getPostList: () => api.get('/posts/list'),
  
  // 发布帖子
  createPost: (data) => api.post('/posts/create', data),
  
  // 获取帖子详情
  getPostDetail: (id) => api.get(`/posts/${id}`),
  
  // 获取我的帖子
  getMyPosts: () => api.get('/posts/my'),
  
  // 获取指定用户的帖子
  getUserPosts: (userId) => api.get(`/posts/user/${userId}`),
  
  // 更新帖子
  updatePost: (id, data) => api.put(`/posts/${id}`, data),
  
  // 删除帖子
  deletePost: (id) => api.delete(`/posts/${id}`),
  
  // 点赞/取消点赞
  toggleLike: (id) => api.post(`/posts/${id}/like`),
  
  // 获取点赞用户列表
  getLikeUsers: (id) => api.get(`/posts/${id}/likes`),
  
  // 搜索帖子
  searchPosts: (keyword) => api.get(`/posts/search?keyword=${keyword}`)
}

// 视频通话相关API
export const videoCallApi = {
  // 创建或加入房间
  createRoom: (data) => api.post('/video-call/create-room', data),
  
  // 获取房间信息
  getRoomInfo: (roomId) => api.get(`/video-call/room/${roomId}`),
  
  // 离开房间
  leaveRoom: (data) => api.post('/video-call/leave-room', data)
}

export default api
