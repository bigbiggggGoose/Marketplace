import http from './http'

export function apiReport(itemId: string | number, payload: { reason: string; desc?: string }) {
  const id = itemId === 0 || itemId ? String(itemId) : ''
  return http.post(`/items/${id}/report`, payload).then(r => r.data)
}


