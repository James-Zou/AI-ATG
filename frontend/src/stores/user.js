import { defineStore } from 'pinia'
import { login } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    // Session/Cookie 认证不需要在前端存储 token
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    // 基于 userInfo 判断登录状态（只要有 userId 就认为已登录）
    isLoggedIn: (state) => !!state.userInfo?.id,
    userId: (state) => state.userInfo?.id || null,
    username: (state) => state.userInfo?.username || '',
    role: (state) => state.userInfo?.role || 'tester'
  },
  
  actions: {
    // 登录
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        // 后端已改为基于 Session/Cookie 认证，不再返回 token
        // 只需要保存 userInfo 即可
        const { userInfo } = res.data
        
        this.userInfo = userInfo
        
        // 保存到 localStorage
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        
        return res
      } catch (error) {
        throw error
      }
    },
    
    // 登出
    logout() {
      // 清除用户信息（Session 由后端管理，前端只需清除本地缓存）
      this.userInfo = {}
      localStorage.removeItem('userInfo')
    },
    
    // 更新用户信息
    updateUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }
  }
})
