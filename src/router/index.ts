import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Home from '@/pages/Home.vue'
import { useAuthStore } from '@/store/auth'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: Home },
  { path: '/login', name: 'login', component: () => import('@/pages/Login.vue') },
  { path: '/register', name: 'register', component: () => import('@/pages/Register.vue') },
  { path: '/items', name: 'items', component: () => import('@/pages/ItemList.vue') },
  { path: '/items/:id', name: 'item-detail', component: () => import('@/pages/ItemDetail.vue') },
  { path: '/items/:id/edit', name: 'item-edit', component: () => import('@/pages/ItemEdit.vue'), meta: { requiresAuth: true } },
  { path: '/publish', name: 'publish', component: () => import('@/pages/ItemPublish.vue'), meta: { requiresAuth: true } },
  { path: '/profile', name: 'profile', component: () => import('@/pages/UserProfile.vue'), meta: { requiresAuth: true } },
  { path: '/my/items', name: 'my-items', component: () => import('@/pages/MyItems.vue'), meta: { requiresAuth: true } },
  { path: '/my/favorites', name: 'favorites', component: () => import('@/pages/Favorites.vue'), meta: { requiresAuth: true } },
  { path: '/trade/requests', name: 'trade-requests', component: () => import('@/pages/TradeRequests.vue'), meta: { requiresAuth: true } },
  { path: '/users/:id', name: 'user-public', component: () => import('@/pages/UserPublic.vue') },
  { path: '/admin/review', name: 'admin-review', component: () => import('@/pages/AdminReview.vue'), meta: { requiresAdmin: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 简单守卫占位，可与 Pinia 鉴权联动
router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  const isAuthed = auth.isAuthed
  const isAdmin = auth.isAdmin
  if (to.meta.requiresAuth && !isAuthed) return next({ name: 'login' })
  if (to.meta.requiresAdmin && !isAdmin) return next({ name: 'home' })
  return next()
})

export default router


