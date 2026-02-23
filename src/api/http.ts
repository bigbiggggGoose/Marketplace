import axios from 'axios'
import { useAuthStore } from '@/store/auth'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res,
  (err) => {
    // 可以在此统一提示或上报
    return Promise.reject(err)
  }
)

export default http


