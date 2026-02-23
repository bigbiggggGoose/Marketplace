<template>
  <div class="item-card" @click="$router.push({ name: 'item-detail', params: { id: item.id } })">
    <div class="media">
      <img class="thumb" :src="item.images[0] || defaultCover" alt="thumb" />
      <el-tag size="small" :type="statusType" class="status-badge">{{ statusText }}</el-tag>
      <el-tag v-if="item.reviewStatus === 'PENDING'" size="small" type="warning" class="review-badge">待审核</el-tag>
    </div>
    <div class="info">
      <h4 class="title">{{ item.title }}</h4>
      <div class="price-row">
        <span class="price">¥ {{ item.price }}</span>
        <span class="views">{{ item.views }} 次浏览</span>
      </div>
      <div class="seller" v-if="item.sellerName">{{ item.sellerName }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Item } from '@/types'

const props = defineProps<{ item: Item }>()
const item = computed(() => props.item)
const defaultCover = 'https://via.placeholder.com/400x300?text=Item'

const statusText = computed(() => {
  switch (props.item.status) {
    case 'OUT_OF_STOCK':
      return '缺货'
    case 'OFFLINE':
      return '已下架'
    default:
      return '在售'
  }
})

const statusType = computed(() => {
  switch (props.item.status) {
    case 'OUT_OF_STOCK':
      return 'warning'
    case 'OFFLINE':
      return 'info'
    default:
      return 'success'
  }
})
</script>

<style scoped>
.item-card {
  cursor: pointer;
  background: var(--surface-card);
  border-radius: 20px;
  padding: 12px 12px 16px;
  box-shadow: var(--shadow-card);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 35px rgba(20, 28, 68, 0.12);
}

.media {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
}

.thumb {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--brand-dark);
  line-height: 1.4;
}

.price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.price {
  font-weight: 700;
  font-size: 18px;
  color: #ff6b3d;
}

.views {
  color: var(--brand-muted);
}

.seller {
  font-size: 12px;
  color: var(--brand-muted);
}

.status-badge,
.review-badge {
  position: absolute;
  top: 10px;
  border-radius: 999px;
  padding: 0 10px;
}

.status-badge {
  left: 10px;
}

.review-badge {
  right: 10px;
}
</style>
