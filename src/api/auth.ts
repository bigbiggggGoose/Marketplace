import http from './http'
import type { User } from '@/types/user'

export interface LoginPayload {
  username: string
  password: string
}
export interface LoginResponse {
  token: string
  user: User
}

export function apiLogin(data: LoginPayload) {
  return http.post<LoginResponse>('/auth/login', data).then(r => ({
    token: r.data.token,
    user: normalizeUser(r.data.user)
  }))
}

export interface RegisterPayload {
  username: string
  password: string
  email?: string
  phone?: string
}
export function apiRegister(data: RegisterPayload) {
  return http.post('/auth/register', data).then(r => r.data)
}

export function apiLogout() {
  return http.post('/auth/logout').then(r => r.data)
}

export function apiGetMe() {
  return http.get<User>('/auth/me').then(r => normalizeUser(r.data))
}

export function apiUpdateMe(data: { email?: string; phone?: string }) {
  return http.put('/auth/me', null, { params: data }).then(r => r.data)
}

export function normalizeUser(user: any): User {
  return {
    id: user?.id ?? '',
    username: user?.username || '',
    email: user.email,
    phone: user.phone,
    avatar: user.avatar,
    role: user?.role?.toString().toLowerCase() === 'admin' ? 'admin' : 'user'
  }
}


