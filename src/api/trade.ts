import http from './http'
import type { TradeRequest } from '@/types'

export interface CreateTradeRequestPayload {
  quantity: number
  message?: string
}

export function apiCreateTradeRequest(itemId: string, data: CreateTradeRequestPayload) {
  return http.post<TradeRequest>(`/trade/items/${itemId}/request`, data).then(r => r.data)
}

export function apiGetMyRequests() {
  return http.get<TradeRequest[]>('/trade/my-requests').then(r => r.data)
}

export function apiGetReceivedRequests() {
  return http.get<TradeRequest[]>('/trade/received-requests').then(r => r.data)
}

export function apiProcessRequest(requestId: string, action: 'accept' | 'reject') {
  return http.post(`/trade/requests/${requestId}/process?action=${action}`).then(r => r.data)
}

export interface SellerOfferPayload {
  price: number
  quantity?: number
  message?: string
}

export function apiSendSellerOffer(requestId: string, data: SellerOfferPayload) {
  return http.post(`/trade/requests/${requestId}/offer`, data).then(r => r.data)
}

export function apiConfirmPayment(requestId: string) {
  return http.post(`/trade/requests/${requestId}/confirm`).then(r => r.data)
}

export interface TradeMessage {
  id: number
  tradeRequestId: number
  senderId: number
  senderName: string
  content: string
  createdAt: string
}

export interface TradeMessageRequest {
  content: string
}

export function apiSendTradeMessage(requestId: string, data: TradeMessageRequest) {
  return http.post<TradeMessage>(`/trade/requests/${requestId}/messages`, data).then(r => r.data)
}

export function apiGetTradeMessages(requestId: string) {
  return http.get<TradeMessage[]>(`/trade/requests/${requestId}/messages`).then(r => r.data)
}

