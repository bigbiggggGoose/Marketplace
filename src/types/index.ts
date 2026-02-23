export type ItemStatus =
  | 'ON_SALE'
  | 'OUT_OF_STOCK'
  | 'OFFLINE'
  | 'PENDING'
  | 'REJECTED'

export interface Item {
  id: string
  title: string
  price: number
  stock: number
  images: string[]
  category: string
  description?: string
  sellerId: string
  sellerName?: string
  createdAt: string
  views: number
  status?: ItemStatus
  reviewStatus?: 'PENDING' | 'APPROVED' | 'RESOLVED'
}

export interface TradeRequest {
  id: number
  itemId: number
  itemTitle: string
  buyerId: number
  buyerName: string
  sellerId: number
  sellerName: string
  quantity: number
  message?: string
  sellerOfferPrice?: number
  sellerOfferQuantity?: number
  buyerConfirmed?: boolean
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'SELLER_OFFERED' | 'BUYER_CONFIRMED' | 'COMPLETED' | 'CANCELLED'
  createdAt: string
  updatedAt?: string
}

export interface PaginationQuery {
  page: number
  pageSize: number
}

export interface PublicUser {
  id: number
  username: string
  email?: string
  phone?: string
  role?: string
}


