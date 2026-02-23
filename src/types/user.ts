export interface User {
  id: string | number
  username: string
  role: 'user' | 'admin'
  email?: string
  phone?: string
  avatar?: string
}

