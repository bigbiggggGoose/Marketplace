<template>
  <div class="auth-center">
    <el-card class="auth-card" shadow="hover">
      <p class="eyebrow">欢迎回来</p>
      <h2 class="title">登录账号</h2>
      <p class="subtext">继续管理你的商品、交易与收藏。</p>

      <el-form :model="form" label-width="80px" @keyup.enter="login">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="输入用户名" />
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input v-model="form.password" type="password" show-password placeholder="输入密码" />
        </el-form-item>
        <el-form-item class="actions">
          <el-button type="primary" :loading="loading" @click="login" class="submit-btn">登录</el-button>
          <el-button link @click="$router.push({ name: 'register' })">还没有账号？去注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { apiLogin } from '@/api/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function login() {
  if (!form.username.trim() || !form.password) return
  try {
    loading.value = true
    const res = await apiLogin({ username: form.username.trim(), password: form.password })
    auth.login(res.token, res.user)
    router.push({ name: 'profile' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-center {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(circle at top, rgba(255, 208, 170, 0.25), transparent 55%);
}

.auth-card {
  width: 420px;
  border-radius: 28px;
  border: none;
  padding: 32px 36px 36px;
  box-shadow: var(--shadow-soft);
}

.eyebrow {
  font-size: 13px;
  color: var(--brand-primary);
  letter-spacing: 1px;
  margin: 0 0 6px;
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--brand-dark);
}

.subtext {
  margin: 6px 0 24px;
  color: var(--brand-muted);
  font-size: 14px;
}

.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.submit-btn {
  padding: 0 28px;
  border-radius: 999px;
}
</style>
