const TOKEN_KEY = 'token'
const USER_KEY = 'auth_user'

export function saveToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function loadToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function saveUser(user: unknown) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function loadUser<T = unknown>(): T | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function clearUser() {
  localStorage.removeItem(USER_KEY)
}
