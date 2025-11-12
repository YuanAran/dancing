import { defineStore } from 'pinia'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    isLoggedIn: false,
    token: null
  }),

  getters: {
    getUserInfo: (state) => state.user,
    getIsLoggedIn: (state) => state.isLoggedIn
  },

  actions: {
    // 用户登录
    async login(username, password) {
      try {
        const response = await userApi.login({ username, password })
        if (response.code === 200) {
          // 保存用户信息和Token
          this.user = response.data.user
          this.token = response.data.token
          this.isLoggedIn = true
          
          // 将Token保存到localStorage
          localStorage.setItem('token', response.data.token)
          
          return { success: true, message: response.message }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        return { success: false, message: '登录失败，请检查网络连接' }
      }
    },

    // 用户注册
    async register(username, password, email) {
      try {
        const response = await userApi.register({ username, password, email })
        if (response.code === 200) {
          return { success: true, message: response.message }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        return { success: false, message: '注册失败，请检查网络连接' }
      }
    },

    // 获取当前用户信息
    async getCurrentUser() {
      try {
        // 先从localStorage读取Token
        const token = localStorage.getItem('token')
        if (!token) {
          this.logout()
          return null
        }
        
        const response = await userApi.getCurrentUser()
        if (response.code === 200) {
          this.user = response.data
          this.token = token
          this.isLoggedIn = true
          return response.data
        } else {
          this.logout()
          return null
        }
      } catch (error) {
        this.logout()
        return null
      }
    },

    // 更新用户信息
    async updateUser(username, email) {
      try {
        const response = await userApi.updateUser({ username, email })
        if (response.code === 200) {
          // 重新获取用户信息
          await this.getCurrentUser()
          return { success: true, message: response.message }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        return { success: false, message: '更新失败，请检查网络连接' }
      }
    },

    // 用户退出
    async logout() {
      try {
        await userApi.logout()
      } catch (error) {
        console.error('退出登录失败:', error)
      } finally {
        this.user = null
        this.token = null
        this.isLoggedIn = false
        // 清除localStorage中的Token
        localStorage.removeItem('token')
      }
    },

    // 初始化：从localStorage恢复登录状态
    init() {
      const token = localStorage.getItem('token')
      if (token) {
        this.token = token
        // 尝试获取用户信息
        this.getCurrentUser()
      }
    }
  }
})
