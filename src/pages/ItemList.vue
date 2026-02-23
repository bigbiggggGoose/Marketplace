<template>
  <div class="page">
    <el-card class="filters">
      <el-input v-model="q.keyword" placeholder="关键词" class="w200" />
      <el-select v-model="q.category" placeholder="分类" class="w160">
        <el-option label="全部" value="" />
        <el-option label="电器" value="电器" />
        <el-option label="配件" value="配件" />
        <el-option label="文具" value="文具" />
        <el-option label="手机" value="手机" />
        <el-option label="家具" value="家具" />
        <el-option label="图书" value="图书" />
        <el-option label="服装" value="服装" />
        <el-option label="食品" value="食品" />
        <el-option label="珠宝" value="珠宝" />
      </el-select>
      <el-select v-model="q.sort" placeholder="排序" class="w160">
        <el-option label="最新" value="new" />
        <el-option label="价格升序" value="price_asc" />
        <el-option label="浏览量" value="views" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
    </el-card>

    <div class="grid">
      <ItemCard v-for="it in items" :key="it.id" :item="it" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import ItemCard from '@/components/ItemCard.vue'
import type { Item } from '@/types'
import { apiQueryItems } from '@/api/items'

const route = useRoute()
const q = reactive({ keyword: '', category: '', sort: 'new' })
const items = ref<Item[]>([])
async function search() {
  const res = await apiQueryItems({ ...q, page: 1, pageSize: 20 })
  items.value = res.list
}
onMounted(() => {
  if (typeof route.query.keyword === 'string') {
    q.keyword = route.query.keyword
  }
  search()
})

watch(() => route.query.keyword, (val) => {
  if (typeof val === 'string') {
    q.keyword = val
    search()
  }
})
</script>

<style scoped>
.page { padding: 16px; max-width: 1024px; margin: 0 auto; }
.filters { display: flex; gap: 8px; margin-bottom: 12px; }
.w160 { width: 160px; }
.w200 { width: 200px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; }
</style>


