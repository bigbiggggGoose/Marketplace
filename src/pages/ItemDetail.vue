<template>
  <div class="page">
    <el-card>
      <div class="detail">
        <div class="images">
          <img v-for="(img, idx) in item.images" :key="idx" class="cover" :src="img" />
        </div>
        <div class="info">
          <h2>{{ item.title }}</h2>
          <div class="price">¥ {{ item.price }}</div>
          <div class="meta">
            分类：{{ item.category }} · 浏览：{{ item.views }} · 库存：{{ item.stock }}
            <el-tag style="margin-left: 8px" :type="statusInfo.type">{{ statusInfo.text }}</el-tag>
          </div>
          <div class="seller-info" v-if="item.sellerId">
            卖家：
            <router-link
              class="seller-link"
              :to="{ name: 'user-public', params: { id: item.sellerId } }"
            >
              {{ item.sellerName || '查看卖家资料' }}
            </router-link>
            <el-button link type="primary" class="seller-button" @click="goSellerProfile">
              查看TA的其它商品
            </el-button>
          </div>
          <div class="actions" v-if="authStore.isAuthed">
            <template v-if="isMyItem">
              <el-button type="primary" @click="handleEdit">编辑</el-button>
              <el-button type="danger" @click="handleDelete">删除</el-button>
            </template>
            <template v-else>
              <el-button 
                type="primary" 
                @click="tradeRequestVisible = true" 
                :disabled="!canPurchase">
                申请交易
              </el-button>
              <el-button 
                :type="isFavorited ? 'warning' : 'default'" 
                @click="handleToggleFavorite"
                :loading="favoriteLoading">
                <el-icon v-if="!favoriteLoading" style="margin-right: 4px;">
                  <StarFilled v-if="isFavorited" />
                  <Star v-else />
                </el-icon>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </el-button>
              <el-button type="danger" plain @click="reportVisible = true">举报</el-button>
            </template>
          </div>
        </div>
      </div>
      <div class="desc">
        <h4>描述</h4>
        <p>{{ item.description || '暂无描述' }}</p>
      </div>
    </el-card>
    <div class="panel">
      <MessagePanel :item-id="item.id" :seller-id="item.sellerId" />
    </div>
    <ReportDialog v-model="reportVisible" :item-id="item.id" />
    
    <!-- 交易申请对话框 -->
    <el-dialog v-model="tradeRequestVisible" title="申请交易" width="500px">
      <el-form label-width="84px">
        <el-form-item label="数量">
          <el-input v-model.number="tradeQuantity" type="number" :min="1" :max="Math.max(1, item.stock)" />
        </el-form-item>
        <el-form-item label="留言">
          <el-input v-model="tradeRequestMessage" type="textarea" :rows="4" placeholder="请输入交易留言（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tradeRequestVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTradeRequest" :loading="tradeRequestLoading">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import type { Item } from '@/types'
import MessagePanel from '@/components/MessagePanel.vue'
import ReportDialog from '@/components/ReportDialog.vue'
import { useAuthStore } from '@/store/auth'
import { apiGetItem, apiDeleteItem } from '@/api/items'
import { apiCreateTradeRequest } from '@/api/trade'
import { apiToggleFavorite, apiCheckFavorite } from '@/api/favorites'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const reportVisible = ref(false)
const tradeRequestVisible = ref(false)
const tradeRequestMessage = ref('')
const tradeQuantity = ref(1)
const tradeRequestLoading = ref(false)
const isFavorited = ref(false)
const favoriteLoading = ref(false)

const item = ref<Item>({
  id: '',
  title: '',
  price: 0,
  stock: 0,
  images: [],
  category: '',
  sellerId: '',
  createdAt: '',
  views: 0,
  description: '',
  status: 'ON_SALE'
})

const isMyItem = computed(() => {
  return authStore.isAuthed && authStore.currentUser && item.value.sellerId === authStore.currentUser.id
})

const statusInfo = computed(() => {
  const status = item.value.status
  switch (status) {
    case 'OUT_OF_STOCK':
      return { text: '缺货', type: 'warning' }
    case 'OFFLINE':
      return { text: '已下架', type: 'info' }
    default:
      return { text: '在售', type: 'success' }
  }
})

const canPurchase = computed(() => {
  return item.value.status === 'ON_SALE' && 
         item.value.stock > 0 &&
         item.value.reviewStatus === 'APPROVED'
})

const loadItem = async () => {
  const id = String(route.params.id)
  const data = await apiGetItem(id)
  item.value = data
  if (authStore.isAuthed && !isMyItem.value) {
    checkFavorite()
  }
}

const checkFavorite = async () => {
  try {
    isFavorited.value = await apiCheckFavorite(item.value.id)
  } catch (e) {
    // 忽略错误
  }
}

const handleToggleFavorite = async () => {
  if (favoriteLoading.value) return
  try {
    favoriteLoading.value = true
    const result = await apiToggleFavorite(item.value.id)
    isFavorited.value = result.favorited
    ElMessage.success(result.message)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

onMounted(() => {
  loadItem()
})

watch(tradeRequestVisible, (visible) => {
  if (visible) {
    const max = item.value.stock && item.value.stock > 0 ? item.value.stock : 1
    tradeQuantity.value = Math.min(1, max)
  }
})

const handleEdit = () => {
  router.push(`/items/${item.value.id}/edit`)
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '确认删除', {
      type: 'warning'
    })
    await apiDeleteItem(item.value.id)
    ElMessage.success('删除成功')
    router.push('/items')
  } catch (e) {
    // 用户取消
  }
}

const handleCreateTradeRequest = async () => {
  if (tradeRequestLoading.value) return
  try {
    tradeRequestLoading.value = true
    const rawQty = Math.max(1, Number(tradeQuantity.value) || 1)
    const maxQty = item.value.stock && item.value.stock > 0 ? item.value.stock : rawQty
    const quantity = Math.min(rawQty, maxQty)
    await apiCreateTradeRequest(item.value.id, {
      quantity,
      message: tradeRequestMessage.value || undefined
    })
    ElMessage.success('交易申请已提交')
    tradeRequestVisible.value = false
    tradeRequestMessage.value = ''
    tradeQuantity.value = 1
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '提交失败')
  } finally {
    tradeRequestLoading.value = false
  }
}

const goSellerProfile = () => {
  if (!item.value.sellerId) return
  router.push({ name: 'user-public', params: { id: item.value.sellerId } })
}
</script>

<style scoped>
.page { padding: 16px; max-width: 1024px; margin: 0 auto; }
.detail { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.images { display: flex; flex-direction: column; gap: 8px; }
.cover { width: 100%; border-radius: 8px; }
.actions { margin-top: 16px; display: flex; gap: 8px; }
.info .price { color: #f56c6c; font-size: 24px; font-weight: 700; margin: 8px 0; }
.seller-info { margin-top: 8px; font-size: 14px; color: #666; display: flex; align-items: center; gap: 8px; }
.seller-link { color: #409eff; font-weight: 500; }
.seller-button { padding: 0; }
.desc { margin-top: 16px; }
.panel { margin-top: 16px; }
@media (max-width: 768px) {
  .detail { grid-template-columns: 1fr; }
}
</style>


