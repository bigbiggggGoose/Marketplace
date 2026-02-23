import http from './http'
import type { Item } from '@/types'
import type { PageResult } from './items'

export interface AdminItemQuery {
  keyword?: string
  status?: string
  reviewStatus?: string
  page?: number
  pageSize?: number
}

export interface ReportRecord {
  id: number
  itemId: number
  reporterId: number
  reason: string
  description?: string
  status: string
  createdAt: string
  processedAt?: string
}

export function apiAdminGetPendingItems() {
  return http.get<Item[]>('/admin/items/review').then(r => r.data)
}

export function apiAdminProcessItem(itemId: string | number, action: 'approve' | 'take_down' | 'mark_resolved' | 'put_on') {
  return http.post<Item>(`/admin/items/${itemId}/review`, null, { params: { action } }).then(r => r.data)
}

export function apiAdminQueryItems(params: AdminItemQuery) {
  return http.get<PageResult<Item>>('/admin/items', { params }).then(r => r.data)
}

export function apiAdminGetReports(params: { status?: string; page?: number; pageSize?: number } = {}) {
  return http.get<PageResult<ReportRecord>>('/admin/reports', { params }).then(r => r.data)
}

export function apiAdminProcessReport(reportId: number | string, action: 'approve' | 'ignore') {
  return http.post(`/admin/reports/${reportId}/process`, null, { params: { action } }).then(r => r.data)
}
