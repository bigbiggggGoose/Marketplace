<template>
  <div class="page">
    <h3>我的商品</h3>
    <div class="grid">
      <div v-for="it in items" :key="it.id" class="item-wrapper">
        <ItemCard :item="it" />
        <div class="actions">
          <el-button size="small" @click="handleEdit(it.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(it.id)">删除</el-button>
        </div>
        <div class="review" v-if="it.reviewStatus === 'PENDING'">
          <el-tag size="small" type="warning">等待管理员确认</el-tag>
        </div>
      </div>
    </div>
    <div v-if="!items.length" class="empty">暂无发布，去发布一个吧～</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ItemCard from '@/components/ItemCard.vue'
import type { Item } from '@/types'
import http from '@/api/http'
import { apiDeleteItem } from '@/api/items'

const router = useRouter()
const items = ref<Item[]>([])

async function loadMy() {
  const res = await http.get<Item[]>('/items/my')
  items.value = res.data
}

const handleEdit = (id: string) => {
  router.push(`/items/${id}/edit`)
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '确认删除', {
      type: 'warning'
    })
    await apiDeleteItem(id)
    ElMessage.success('删除成功')
    await loadMy()
  } catch (e) {
    // 用户取消
  }
}

onMounted(loadMy)
</script>

<style scoped>
.page { padding: 16px; max-width: 1024px; margin: 0 auto; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; }
.item-wrapper { position: relative; }
.review {
  margin-top: 8px;
}
.actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}
.item-wrapper:hover .actions {
  opacity: 1;
}
.empty { color: #888; padding: 24px 0; }
</style>


