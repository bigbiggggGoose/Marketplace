import http from './http'

export interface Message {
  id: number
  itemId: number
  senderId: number
  senderName: string
  receiverId: number
  receiverName: string
  content: string
  createdAt: string
}

export interface CreateMessagePayload {
  content: string
}

export function apiGetMessages(itemId: string | number) {
  const id = itemId === 0 || itemId ? String(itemId) : ''
  return http.get<Message[]>(`/items/${id}/messages`).then(r => r.data)
}

export function apiSendMessage(itemId: string | number, data: CreateMessagePayload) {
  const id = itemId === 0 || itemId ? String(itemId) : ''
  return http.post<Message>(`/items/${id}/messages`, data).then(r => r.data)
}

