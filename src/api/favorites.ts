import http from './http'
import type { Item } from '@/types'

export function apiToggleFavorite(itemId: string) {
  return http.post<{ message: string; favorited: boolean }>(`/favorites/items/${itemId}/toggle`).then(r => r.data)
}

export function apiCheckFavorite(itemId: string) {
  return http.get<boolean>(`/favorites/items/${itemId}/check`).then(r => r.data)
}

export function apiGetMyFavorites() {
  return http.get<Item[]>('/favorites/my').then(r => r.data)
}

