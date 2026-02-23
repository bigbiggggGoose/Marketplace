import http from './http'
import type { PublicUser, Item } from '@/types'

export function apiGetUser(userId: string) {
  return http.get<PublicUser>(`/users/${userId}`).then(r => r.data)
}

export function apiGetUserItems(userId: string) {
  return http.get<Item[]>(`/users/${userId}/items`).then(r => r.data)
}


