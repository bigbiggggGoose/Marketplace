import { defineStore } from 'pinia'
import { saveToken, loadToken, clearToken, saveUser, loadUser, clearUser } from '@/utils/storage'
import type { User } from '@/types/user'
import { apiGetMe } from '@/api/auth'

interface AuthState {
  token: string | null
  currentUser: User | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: loadToken(),
    currentUser: loadUser<User>()
  }),
  getters: {
    isAuthed: (state) => !!state.token,
    isAdmin: (state) => state.currentUser?.role === 'admin'
  },
  actions: {
    login(token: string, user: User) {
      this.token = token
      this.currentUser = user
      saveToken(token)
      saveUser(user)
    },
    logout() {
      this.token = null
      this.currentUser = null
      clearToken()
      clearUser()
    },
    async hydrate() {
      if (!this.token || this.currentUser) return
      try {
        const user = await apiGetMe()
        this.currentUser = user
        saveUser(user)
      } catch {
        this.logout()
      }
    }
  }
})


