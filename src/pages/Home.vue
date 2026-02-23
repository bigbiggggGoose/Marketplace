<template>
  <div class="page-shell home-page">
    <section class="page-hero home-hero">
      <span class="pill">安全 · 高效 · 同城交易</span>
      <h1>二手好物，一站式轻松完成交易</h1>
      <p>精选每一件闲置物品，搭配可靠的审核与沟通机制，帮助你快速找到下一位拥有者。</p>
      <div class="hero-actions">
        <el-input v-model="keyword" placeholder="搜索二手商品..." class="search" @keyup.enter="goSearch" />
        <el-button type="primary" size="large" @click="goSearch">立即浏览</el-button>
      </div>
      <div class="stats-row">
        <div class="stat">
          <div class="value">100+</div>
          <div class="label">同城活跃卖家</div>
        </div>
        <div class="stat">
          <div class="value">300+</div>
          <div class="label">每周上新商品</div>
        </div>
        <div class="stat">
          <div class="value">24h</div>
          <div class="label">审核响应时间</div>
        </div>
      </div>
    </section>

    <section class="recommend glass-card">
      <div class="section-head">
        <div>
          <p class="eyebrow">今日精选</p>
          <h3 class="section-title">为你推荐</h3>
          <p class="section-desc">根据浏览热度和新鲜度实时刷新，看看是否有心仪好物。</p>
        </div>
        <el-button text type="primary" @click="$router.push({ name: 'items' })">查看全部</el-button>
      </div>

      <div v-if="items.length" class="grid">
        <ItemCard v-for="it in items" :key="it.id" :item="it" />
      </div>
      <div v-else class="empty">暂时没有推荐商品，去浏览页看看吧～</div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ItemCard from '@/components/ItemCard.vue'
import type { Item } from '@/types'
import http from '@/api/http'
import { useRouter } from 'vue-router'

const router = useRouter()
const keyword = ref('')
const items = ref<Item[]>([])

async function loadRecommended() {
  const res = await http.get<Item[]>('/items/recommended', { params: { limit: 12 } })
  items.value = res.data
}

function goSearch() {
  router.push({ name: 'items', query: { keyword: keyword.value } })
}

onMounted(() => {
  loadRecommended()
})
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
}
.home-hero {
  box-shadow: var(--shadow-soft);
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}
.hero-actions .search {
  flex: 1;
  min-width: 220px;
}
.recommend {
  padding: 32px;
}
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}
.eyebrow {
  font-size: 12px;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: var(--brand-muted);
  margin: 0 0 6px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}
.empty {
  padding: 40px 0;
  text-align: center;
  color: var(--brand-muted);
}
</style>
