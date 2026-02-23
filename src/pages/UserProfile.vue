<template>
  <div class="page">
    <el-card>
      <h3>个人资料</h3>
      <el-form :model="form" label-width="96px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" type="email" placeholder="name@example.com" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="手机号或座机" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { apiGetMe, apiUpdateMe } from '@/api/auth'
import { ElMessage } from 'element-plus'

const form = reactive({ username: '', email: '', phone: '' })
const saving = ref(false)
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const phonePattern = /^(\+?\d{6,20}|(\d{3,4}-)?\d{7,8})$/

onMounted(async () => {
  const me = await apiGetMe()
  form.username = me.username
  form.email = me.email || ''
  form.phone = me.phone || ''
})

async function save() {
  if (form.email && !emailPattern.test(form.email)) {
    ElMessage.error('请输入正确的邮箱地址')
    return
  }
  if (form.phone && !phonePattern.test(form.phone)) {
    ElMessage.error('请输入正确的联系电话')
    return
  }
  try {
    saving.value = true
    await apiUpdateMe({
      email: form.email || undefined,
      phone: form.phone || undefined
    })
    ElMessage.success('已保存')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.error || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page { padding: 16px; max-width: 800px; margin: 0 auto; }
h3 { margin: 0 0 12px; }
</style>


