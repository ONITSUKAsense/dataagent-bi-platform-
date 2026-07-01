import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentUser } from '@/api/auth'
import { clearAuth, setToken, setRefreshToken } from '@/utils/storage'
import type { UserInfo } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])
  const isLoggedIn = ref(false)

  async function fetchUser() {
    try {
      const res = await getCurrentUser()
      user.value = res.data.data
      permissions.value = res.data.data.permissions || []
      isLoggedIn.value = true
    } catch {
      clearAuth()
      isLoggedIn.value = false
    }
  }

  function loginSuccess(token: string, refreshToken: string) {
    setToken(token)
    setRefreshToken(refreshToken)
    isLoggedIn.value = true
  }

  function logout() {
    clearAuth()
    user.value = null
    permissions.value = []
    isLoggedIn.value = false
  }

  function hasPermission(code: string): boolean {
    return permissions.value.includes(code)
  }

  return { user, permissions, isLoggedIn, fetchUser, loginSuccess, logout, hasPermission }
})
