<template>
  <div class="page">
    <el-card class="profile">
      <div class="header">
        <el-avatar :size="64">{{ user?.username?.[0]?.toUpperCase() }}</el-avatar>
        <div class="info">
          <div class="name">{{ user?.username }}</div>
          <div class="meta">邮箱：{{ user?.email || '未公开' }} · 联系：{{ user?.phone || '未公开' }}</div>
        </div>
      </div>
    </el-card>
    <div class="section">
      <h3>TA 的商品</h3>
      <div class="grid">
        <ItemCard v-for="it in items" :key="it.id" :item="it" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import ItemCard from '@/components/ItemCard.vue'
import { apiGetUser, apiGetUserItems } from '@/api/users'
import type { PublicUser, Item } from '@/types'

const route = useRoute()
const user = ref<PublicUser>()
const items = ref<Item[]>([])

onMounted(async () => {
  const userId = String(route.params.id)
  user.value = await apiGetUser(userId)
  items.value = await apiGetUserItems(userId)
})
</script>

<style scoped>
.page { padding: 16px; max-width: 1024px; margin: 0 auto; }
.profile { margin-bottom: 16px; }
.header { display: flex; gap: 12px; align-items: center; }
.info .name { font-weight: 700; font-size: 18px; }
.info .meta { color: #666; font-size: 13px; margin-top: 4px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; }
</style>


