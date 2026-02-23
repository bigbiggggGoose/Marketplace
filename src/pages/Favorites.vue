<template>
  <div class="page">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h2>我的收藏</h2>
        </div>
      </template>
      <div v-loading="loading">
        <div v-if="items.length === 0" class="empty">暂无收藏的商品</div>
        <div v-else class="item-grid">
          <ItemCard v-for="item in items" :key="item.id" :item="item" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ItemCard from '@/components/ItemCard.vue'
import { apiGetMyFavorites } from '@/api/favorites'
import type { Item } from '@/types'

const items = ref<Item[]>([])
const loading = ref(false)

const loadFavorites = async () => {
  try {
    loading.value = true
    const data = await apiGetMyFavorites()
    items.value = data
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.page { padding: 16px; max-width: 1200px; margin: 0 auto; }
.empty { text-align: center; padding: 40px; color: #999; }
.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}
</style>

