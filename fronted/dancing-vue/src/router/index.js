import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/videos',
      name: 'videos',
      component: () => import('@/views/VideoList.vue')
    },
    {
      path: '/videos/upload',
      name: 'videoUpload',
      component: () => import('@/views/VideoUpload.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/videos/my',
      name: 'myVideos',
      component: () => import('@/views/MyVideos.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/videos/:id',
      name: 'videoDetail',
      component: () => import('@/views/VideoDetail.vue')
    },
    {
      path: '/friends',
      name: 'friends',
      component: () => import('@/views/FriendManage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/friends/search',
      name: 'friendSearch',
      component: () => import('@/views/FriendSearch.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/posts',
      name: 'posts',
      component: () => import('@/views/PostList.vue')
    },
    {
      path: '/posts/create',
      name: 'postCreate',
      component: () => import('@/views/PostCreate.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/posts/my',
      name: 'myPosts',
      component: () => import('@/views/MyPosts.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/posts/:id',
      name: 'postDetail',
      component: () => import('@/views/PostDetail.vue')
    },
    {
      path: '/video-call',
      name: 'videoCall',
      component: () => import('@/views/VideoCall.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 简化的路由守卫
router.beforeEach((to, from, next) => {
  // 暂时跳过认证检查，等store初始化后再处理
  next()
})

export default router
