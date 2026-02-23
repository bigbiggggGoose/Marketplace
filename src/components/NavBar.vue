<template>
  <header class="nav glass-nav">
    <div class="nav-left">
      <div class="brand" @click="$router.push({ name: 'home' })">
        <span class="brand-title">二手平台</span>
        <span class="brand-tagline">甄选好物 · 安心交易</span>
      </div>
      <div class="nav-buttons">
        <el-button class="ghost-btn" @click="$router.push({ name: 'items' })">浏览</el-button>
        <el-button class="solid-btn" type="primary" @click="goPublish">发布</el-button>
      </div>
    </div>

    <div v-if="isAuthed" class="user-inline">
      <el-tag size="small" :type="isAdmin ? 'danger' : 'success'" class="role-tag">
        {{ isAdmin ? '管理员' : '普通用户' }}
      </el-tag>
      <div class="user-meta">
        <span class="user-name-inline">{{ username }}</span>
        <span class="user-caption">欢迎回来</span>
      </div>
      <el-divider direction="vertical" />
      <div class="nav-links">
        <el-button link @click="go('profile')">个人资料</el-button>
        <el-button link @click="go('my-items')">我的商品</el-button>
        <el-button link @click="go('favorites')">我的收藏</el-button>
        <el-button link @click="go('trade-requests')">交易管理</el-button>
        <el-button v-if="isAdmin" link type="warning" class="admin-link" @click="go('admin-review')">
          审核 / 举报
        </el-button>
      </div>
      <el-button link type="danger" class="logout-link" @click="logout">退出登录</el-button>
    </div>

    <div v-else class="auth-links">
      <el-button link @click="$router.push({ name: 'login' })">登录</el-button>
      <el-button link @click="$router.push({ name: 'register' })">注册</el-button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const auth = useAuthStore()
const isAuthed = computed(() => auth.isAuthed)
const username = computed(() => auth.currentUser?.username || '用户')
const isAdmin = computed(() => auth.isAdmin)

function logout() {
  auth.logout()
  router.push({ name: 'home' })
}

function goPublish() {
  if (auth.isAuthed) {
    router.push({ name: 'publish' })
  } else {
    router.push({ name: 'login' })
  }
}

function go(name: string) {
  router.push({ name })
}
</script>

<style scoped>
.nav {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 32px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 24px rgba(15, 18, 38, 0.08);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: pointer;
}

.brand-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--brand-primary);
  letter-spacing: 1px;
}

.brand-tagline {
  font-size: 12px;
  color: var(--brand-muted);
}

.nav-buttons {
  display: flex;
  gap: 12px;
}

.ghost-btn {
  border: 1px solid var(--brand-primary);
  color: var(--brand-primary);
  background: rgba(255, 122, 0, 0.08);
}

.solid-btn {
  padding: 0 18px;
  box-shadow: 0 12px 20px rgba(255, 122, 0, 0.25);
}

.ghost-btn,
.solid-btn {
  border-radius: 999px;
  transition: transform 0.2s ease;
}

.ghost-btn:hover,
.solid-btn:hover {
  transform: translateY(-1px);
}

.user-inline {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.role-tag {
  border-radius: 999px;
  font-weight: 600;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-name-inline {
  font-weight: 600;
  color: var(--brand-dark);
}

.user-caption {
  font-size: 12px;
  color: var(--brand-muted);
}

.nav-links {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.nav-links :deep(.el-button.is-link) {
  padding: 0 6px;
  color: var(--brand-dark);
  font-weight: 500;
}

.admin-link {
  font-weight: 600;
}

.logout-link {
  color: #f56c6c;
}

.auth-links {
  display: flex;
  gap: 16px;
}
</style>
