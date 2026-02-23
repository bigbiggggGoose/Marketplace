import http from './http'
import type { Item } from '@/types'

export interface QueryItemsParams {
  keyword?: string
  category?: string
  sort?: 'new' | 'price_asc' | 'views'
  page?: number
  pageSize?: number
}
export interface PageResult<T> {
  total: number
  list: T[]
  page?: number
  pageSize?: number
}

export function apiQueryItems(params: QueryItemsParams) {
  return http.post<PageResult<Item>>('/items/query', params).then(r => r.data)
}

export function apiGetItem(id: string) {
  return http.get<Item>(`/items/${id}`).then(r => r.data)
}

export interface CreateItemPayload {
  title: string
  price: number
  stock: number
  category: string
  images: string[]
  description?: string
}
export function apiCreateItem(data: CreateItemPayload) {
  return http.post<Item>('/items', data).then(r => r.data)
}

export interface UpdateItemPayload {
  title?: string
  price?: number
  stock?: number
  category?: string
  images?: string[]
  description?: string
  status?: string
}
export function apiUpdateItem(id: string, data: UpdateItemPayload) {
  return http.put<Item>(`/items/${id}`, data).then(r => r.data)
}

export function apiDeleteItem(id: string) {
  return http.delete(`/items/${id}`).then(r => r.data)
}


