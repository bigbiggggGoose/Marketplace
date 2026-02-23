<template>
  <div class="panel">
    <h4>留言板</h4>
    <div class="list" v-loading="loading">
      <div v-if="messages.length === 0" class="empty">暂无留言</div>
      <div v-for="m in messages" :key="m.id" class="msg">
        <div class="msg-header">
          <span class="content-line">
            用户名：<router-link class="user" :to="`/users/${m.senderId}`">{{ m.senderName }}</router-link>
            ・ 评论：{{ m.content }}
            ・ 评论时间：{{ formatExact(m.createdAt) }}
          </span>
        </div>
      </div>
    </div>
    <div class="composer" v-if="authStore.isAuthed && !isSeller">
      <el-input 
        v-model="text" 
        placeholder="给卖家留言..." 
        @keyup.enter="send"
        :disabled="sending"
      />
      <el-button type="primary" @click="send" :loading="sending">发送</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { apiGetMessages, apiSendMessage, type Message } from '@/api/message'

const props = defineProps<{
  itemId?: string | number | null
  sellerId?: string | number | null
}>()

const authStore = useAuthStore()
const messages = ref<Message[]>([])
const text = ref('')
const loading = ref(false)
const sending = ref(false)

const normalizedItemId = computed(() => {
  if (props.itemId === 0 || props.itemId) {
    return String(props.itemId)
  }
  return ''
})

const sellerIdStr = computed(() => (props.sellerId === 0 || props.sellerId ? String(props.sellerId) : ''))
const currentUserId = computed(() => {
  if (authStore.isAuthed && authStore.currentUser?.id !== undefined && authStore.currentUser?.id !== null) {
    return String(authStore.currentUser.id)
  }
  return ''
})

const isSeller = computed(() => {
  return authStore.isAuthed && sellerIdStr.value && sellerIdStr.value === currentUserId.value
})

const loadMessages = async () => {
  if (!normalizedItemId.value) {
    messages.value = []
    return
  }
  try {
    loading.value = true
    const data = await apiGetMessages(normalizedItemId.value)
    messages.value = data
  } catch (e) {
    console.error('加载留言失败', e)
  } finally {
    loading.value = false
  }
}

const send = async () => {
  if (!text.value.trim() || sending.value) return
  if (!authStore.isAuthed || !authStore.currentUser) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    sending.value = true
    await apiSendMessage(normalizedItemId.value, {
      content: text.value.trim()
    })
    text.value = ''
    await loadMessages()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '发送失败')
  } finally {
    sending.value = false
  }
}

const pad2 = (n: number) => (n < 10 ? `0${n}` : String(n))
const formatExact = (time: string) => {
  const d = new Date(time)
  const yy = String(d.getFullYear()).slice(-2)
  const MM = pad2(d.getMonth() + 1)
  const dd = pad2(d.getDate())
  const hh = pad2(d.getHours())
  const mm = pad2(d.getMinutes())
  return `${yy}/${MM}/${dd} ${hh}:${mm}`
}

watch(
  () => props.itemId,
  () => {
    loadMessages()
  },
  { immediate: true }
)
</script>

<style scoped>
.panel { border: 1px solid #eee; border-radius: 8px; background: #fff; padding: 16px; }
.panel h4 { margin: 0 0 12px; font-size: 16px; }
.list { max-height: 280px; overflow: auto; padding: 12px; }
.empty { text-align: center; color: #999; padding: 20px; }
.msg { margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; }
.msg:last-child { border-bottom: none; }
.msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.user { color: #409eff; font-weight: 500; }
.content { color: #333; line-height: 1.6; }
.content-line { color: #333; }
.time { color: #999; font-size: 12px; }
.composer { display: flex; gap: 8px; padding-top: 12px; border-top: 1px solid #eee; }
</style>


