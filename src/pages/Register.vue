<template>
  <div class="auth-center">
    <el-card class="auth-card" shadow="hover">
      <p class="eyebrow">加入社区</p>
      <h2 class="title">注册新账号</h2>
      <p class="subtext">发布闲置好物，连接买家，让每一次交易都井井有条。</p>

      <el-form :model="form" label-width="100px" @keyup.enter="register">
        <el-form-item label="用户名">
          <el-input v-model.trim="form.username" maxlength="50" placeholder="设置登录用户名" />
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input v-model="form.password" type="password" show-password placeholder="至少6位字符" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.password2" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model.trim="form.email" placeholder="name@example.com" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model.trim="form.phone" placeholder="手机号或座机" />
        </el-form-item>
        <el-form-item class="actions">
          <el-button type="primary" :loading="loading" @click="register" class="submit-btn">注册</el-button>
          <el-button link @click="$router.push({ name: 'login' })">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiRegister } from '@/api/auth'
import { ElMessage } from 'element-plus'

const form = reactive({ username: '', password: '', password2: '', email: '', phone: '' })
const router = useRouter()
const loading = ref(false)

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phonePattern = /^(\+?\d{6,20}|(\d{3,4}-)?\d{7,8})$/

async function register() {
  if (!form.username.trim()) {
    ElMessage.error('请输入用户名')
    return
  }
  if (!form.password || form.password.length < 6) {
    ElMessage.error('密码至少需要6位字符')
    return
  }
  if (form.password !== form.password2) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (form.email && !emailPattern.test(form.email)) {
    ElMessage.error('请输入正确的邮箱地址')
    return
  }
  if (form.phone && !phonePattern.test(form.phone)) {
    ElMessage.error('请输入正确的联系电话（可为手机号或区号-座机）')
    return
  }
  try {
    loading.value = true
    await apiRegister({
      username: form.username.trim(),
      password: form.password,
      email: form.email || undefined,
      phone: form.phone || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.push({ name: 'login' })
  } catch (e: any) {
    const msg = e?.response?.data?.error || '注册失败，请稍后重试'
    ElMessage.error(msg)
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
  width: 520px;
  border-radius: 28px;
  border: none;
  padding: 32px 40px 36px;
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
  padding: 0 32px;
  border-radius: 999px;
}
</style>
