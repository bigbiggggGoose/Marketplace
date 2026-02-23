<template>
  <div class="page">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我收到的申请" name="received">
          <div v-loading="receivedLoading">
            <div v-if="receivedRequests.length === 0" class="empty">暂无收到的申请</div>
            <div v-for="req in receivedRequests" :key="req.id" class="request-card">
              <div class="header">
                <span class="item-title">{{ req.itemTitle }}</span>
                <el-tag :type="getStatusType(req.status)">{{ getStatusText(req.status) }}</el-tag>
              </div>
              <div class="info">
                <div>买家：{{ req.buyerName }}（数量：{{ req.quantity }}）</div>
                <div v-if="req.message" class="message">留言：{{ req.message }}</div>
                <div class="time">{{ formatTime(req.createdAt) }}</div>
              </div>
              <div v-if="req.sellerOfferPrice" class="offer-info">
                <div style="color: #f56c6c; font-weight: 600; margin: 8px 0;">
                  商家报价：¥{{ req.sellerOfferPrice }} × {{ req.sellerOfferQuantity || req.quantity }}
                </div>
              </div>
              <div class="actions">
                <el-button v-if="req.status === 'PENDING'" type="success" size="small" @click="handleProcess(req.id, 'accept')">接受</el-button>
                <el-button v-if="req.status === 'PENDING'" type="danger" size="small" @click="handleProcess(req.id, 'reject')">拒绝</el-button>
                <el-button v-if="req.status === 'ACCEPTED'" type="primary" size="small" @click="openOfferDialog(req)">发送报价</el-button>
                <el-button type="default" size="small" @click="openChatDialog(req)">查看详情/聊天</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="我发起的申请" name="sent">
          <div v-loading="sentLoading">
            <div v-if="sentRequests.length === 0" class="empty">暂无发起的申请</div>
            <div v-for="req in sentRequests" :key="req.id" class="request-card">
              <div class="header">
                <span class="item-title" @click="router.push(`/items/${req.itemId}`)" style="cursor: pointer; color: #409eff;">
                  {{ req.itemTitle }}
                </span>
                <el-tag :type="getStatusType(req.status)">{{ getStatusText(req.status) }}</el-tag>
              </div>
              <div class="info">
                <div>卖家：{{ req.sellerName }}（数量：{{ req.quantity }}）</div>
                <div v-if="req.message" class="message">留言：{{ req.message }}</div>
                <div v-if="req.sellerOfferPrice" class="offer-info">
                  <div style="color: #f56c6c; font-weight: 600; margin: 8px 0;">
                    商家报价：¥{{ req.sellerOfferPrice }} × {{ req.sellerOfferQuantity || req.quantity }}
                  </div>
                </div>
                <div class="time">{{ formatTime(req.createdAt) }}</div>
              </div>
              <div class="actions">
                <el-button v-if="req.status === 'SELLER_OFFERED'" type="success" size="small" @click="handleConfirmPayment(req.id)">确认支付</el-button>
                <el-button type="default" size="small" @click="openChatDialog(req)">查看详情/聊天</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 商家报价对话框 -->
    <el-dialog v-model="offerDialogVisible" title="发送报价" width="500px">
      <el-form label-width="100px">
        <el-form-item label="报价金额" required>
          <el-input-number v-model="offerForm.price" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="offerForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="留言">
          <el-input v-model="offerForm.message" type="textarea" :rows="4" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="offerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSendOffer" :loading="offerLoading">发送</el-button>
      </template>
    </el-dialog>

    <!-- 聊天/详情对话框 -->
    <el-dialog v-model="chatDialogVisible" title="交易详情" width="700px">
      <div v-if="selectedRequest">
        <div class="chat-info">
          <div><strong>商品：</strong>{{ selectedRequest.itemTitle }}</div>
          <div><strong>买家：</strong>{{ selectedRequest.buyerName }}</div>
          <div><strong>卖家：</strong>{{ selectedRequest.sellerName }}</div>
          <div><strong>状态：</strong><el-tag :type="getStatusType(selectedRequest.status)">{{ getStatusText(selectedRequest.status) }}</el-tag></div>
          <div v-if="selectedRequest.sellerOfferPrice" style="margin-top: 12px; padding: 12px; background: #f5f5f5; border-radius: 4px;">
            <div style="color: #f56c6c; font-weight: 600; font-size: 18px;">
              报价：¥{{ selectedRequest.sellerOfferPrice }} × {{ selectedRequest.sellerOfferQuantity || selectedRequest.quantity }}
            </div>
          </div>
        </div>
        <el-divider />
        <div class="chat-messages" style="margin-top: 16px; max-height: 400px; overflow-y: auto; padding: 8px;">
          <div v-if="tradeMessages.length === 0 && !messagesLoading" class="empty-message">暂无消息</div>
          <div v-loading="messagesLoading">
            <div v-for="msg in tradeMessages" :key="msg.id" class="message-item" 
                 :class="{ 'message-own': msg.senderId === currentUserId }">
              <div class="message-header">
                <span class="sender-name">{{ msg.senderName }}</span>
                <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div class="message-content">{{ msg.content }}</div>
            </div>
          </div>
        </div>
        <el-divider />
        <div class="chat-input">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keydown.ctrl.enter="handleSendMessage"
          />
          <div style="margin-top: 8px; text-align: right;">
            <el-button type="primary" @click="handleSendMessage" :loading="messageSending" :disabled="!newMessage.trim()">
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiGetReceivedRequests, apiGetMyRequests, apiProcessRequest, apiSendSellerOffer, apiConfirmPayment, apiSendTradeMessage, apiGetTradeMessages, type TradeMessage } from '@/api/trade'
import type { TradeRequest } from '@/types'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('received')
const receivedRequests = ref<TradeRequest[]>([])
const sentRequests = ref<TradeRequest[]>([])
const receivedLoading = ref(false)
const sentLoading = ref(false)
const offerDialogVisible = ref(false)
const chatDialogVisible = ref(false)
const selectedRequest = ref<TradeRequest | null>(null)
const offerForm = ref({
  price: 0,
  quantity: 1,
  message: ''
})
const offerLoading = ref(false)
const tradeMessages = ref<TradeMessage[]>([])
const messagesLoading = ref(false)
const newMessage = ref('')
const messageSending = ref(false)

const currentUserId = computed(() => {
  return authStore.currentUser?.id ? Number(authStore.currentUser.id) : null
})

const loadReceived = async () => {
  try {
    receivedLoading.value = true
    const data = await apiGetReceivedRequests()
    receivedRequests.value = data
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '加载失败')
  } finally {
    receivedLoading.value = false
  }
}

const loadSent = async () => {
  try {
    sentLoading.value = true
    const data = await apiGetMyRequests()
    sentRequests.value = data
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '加载失败')
  } finally {
    sentLoading.value = false
  }
}

const handleProcess = async (requestId: string, action: 'accept' | 'reject') => {
  try {
    const actionText = action === 'accept' ? '接受' : '拒绝'
    await ElMessageBox.confirm(`确定要${actionText}这个交易申请吗？`, '确认操作', {
      type: action === 'accept' ? 'success' : 'warning'
    })
    await apiProcessRequest(requestId, action)
    ElMessage.success('操作成功')
    await loadReceived()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.error || '操作失败')
    }
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    ACCEPTED: 'success',
    REJECTED: 'danger',
    SELLER_OFFERED: 'warning',
    BUYER_CONFIRMED: 'success',
    COMPLETED: 'info',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    ACCEPTED: '已接受',
    REJECTED: '已拒绝',
    SELLER_OFFERED: '商家已报价',
    BUYER_CONFIRMED: '买家已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const openOfferDialog = (req: TradeRequest) => {
  selectedRequest.value = req
  offerForm.value = {
    price: 0,
    quantity: req.quantity,
    message: ''
  }
  offerDialogVisible.value = true
}

const handleSendOffer = async () => {
  if (!selectedRequest.value || offerForm.value.price <= 0) {
    ElMessage.warning('请输入有效的报价金额')
    return
  }
  try {
    offerLoading.value = true
    await apiSendSellerOffer(String(selectedRequest.value.id), {
      price: offerForm.value.price,
      quantity: offerForm.value.quantity,
      message: offerForm.value.message
    })
    ElMessage.success('报价发送成功')
    offerDialogVisible.value = false
    await loadReceived()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '发送失败')
  } finally {
    offerLoading.value = false
  }
}

const openChatDialog = async (req: TradeRequest) => {
  selectedRequest.value = req
  chatDialogVisible.value = true
  newMessage.value = ''
  await loadTradeMessages(req.id)
}

const loadTradeMessages = async (requestId: string | number) => {
  try {
    messagesLoading.value = true
    const messages = await apiGetTradeMessages(String(requestId))
    tradeMessages.value = messages
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '加载消息失败')
  } finally {
    messagesLoading.value = false
  }
}

const handleSendMessage = async () => {
  if (!selectedRequest.value || !newMessage.value.trim() || messageSending.value) return
  
  try {
    messageSending.value = true
    await apiSendTradeMessage(String(selectedRequest.value.id), {
      content: newMessage.value.trim()
    })
    newMessage.value = ''
    await loadTradeMessages(selectedRequest.value.id)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '发送失败')
  } finally {
    messageSending.value = false
  }
}

const handleConfirmPayment = async (requestId: string) => {
  try {
    await ElMessageBox.confirm('确认支付后，交易将完成，库存将被扣除。确定要继续吗？', '确认支付', {
      type: 'warning'
    })
    await apiConfirmPayment(requestId)
    ElMessage.success('支付确认成功')
    await loadSent()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.error || '确认失败')
    }
  }
}

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadReceived()
  loadSent()
})
</script>

<style scoped>
.page { padding: 16px; max-width: 1000px; margin: 0 auto; }
.empty { text-align: center; padding: 40px; color: #999; }
.request-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.item-title {
  font-weight: 600;
  font-size: 16px;
}
.info {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}
.info > div {
  margin-bottom: 4px;
}
.message {
  color: #333;
  margin: 8px 0;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
}
.time {
  color: #999;
  font-size: 12px;
}
.actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
.chat-info {
  font-size: 14px;
  line-height: 1.8;
}
.chat-messages {
  border: 1px solid #eee;
  border-radius: 4px;
  background: #fafafa;
}
.empty-message {
  text-align: center;
  padding: 20px;
  color: #999;
}
.message-item {
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}
.message-item.message-own {
  background: #e3f2fd;
  border-color: #90caf9;
}
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.sender-name {
  font-weight: 600;
  color: #409eff;
  font-size: 13px;
}
.message-time {
  font-size: 12px;
  color: #999;
}
.message-content {
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.chat-input {
  margin-top: 12px;
}
</style>

