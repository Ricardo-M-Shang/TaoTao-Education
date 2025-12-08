import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/user'
import type { LoginParams, UserInfo } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  // 登录
  async function loginAction(params: LoginParams) {
    const res = await login(params)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    
    // 保存用户信息
    userInfo.value = {
      id: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
      role: res.data.role
    }
    
    return res
  }

  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }

  // 退出登录
  async function logoutAction() {
    try {
      await logout()
    } catch (error) {
      console.error('退出登录失败', error)
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    loginAction,
    fetchUserInfo,
    logoutAction
  }
})

