<template>
  <div class="page">
    <el-card>
      <h3>编辑商品</h3>
      <el-form :model="form" label-width="84px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="价格"><el-input v-model.number="form.price" type="number" /></el-form-item>
        <el-form-item label="库存"><el-input v-model.number="form.stock" type="number" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="选择分类" class="w240">
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
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="选择状态" class="w240">
            <el-option label="在售" value="ON_SALE" />
            <el-option label="缺货" value="OUT_OF_STOCK" />
            <el-option label="已下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片">
          <UploadImage v-model:file-list="fileList" @update:images="(list) => (form.images = list)" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="5" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="loading">保存修改</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import UploadImage from '@/components/UploadImage.vue'
import { apiGetItem, apiUpdateItem } from '@/api/items'
import type { UploadFile } from 'element-plus'

const router = useRouter()
const route = useRoute()

const form = reactive({
  title: '',
  price: 0,
  stock: 0,
  category: '',
  images: [] as string[],
  description: '',
  status: 'ON_SALE'
})

const fileList = ref<UploadFile[]>([])
const loading = ref(false)

const loadItem = async () => {
  try {
    const id = String(route.params.id)
    const item = await apiGetItem(id)
    form.title = item.title
    form.price = item.price
    form.stock = item.stock
    form.category = item.category
    form.images = item.images || []
    form.description = item.description || ''
    // 只允许三种状态
    const allowed = ['ON_SALE', 'OUT_OF_STOCK', 'OFFLINE'] as const
    form.status = (allowed.includes(item.status as any) ? item.status : 'ON_SALE') as any
    
    // 设置文件列表用于显示
    fileList.value = item.images.map((url, index) => ({
      uid: String(index),
      name: `image-${index}.jpg`,
      url: url,
      status: 'success'
    } as UploadFile))
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '加载失败')
    router.back()
  }
}

async function submit() {
  if (!form.title || !form.price || !form.category) {
    ElMessage.error('请填写标题/价格/分类')
    return
  }
  try {
    loading.value = true
    const id = String(route.params.id)
    await apiUpdateItem(id, {
      title: form.title,
      price: form.price,
      stock: form.stock,
      category: form.category,
      images: form.images,
      description: form.description,
      status: form.status
    })
    ElMessage.success('修改成功')
    router.push({ name: 'item-detail', params: { id: route.params.id } })
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '修改失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadItem()
})
</script>

<style scoped>
.page { padding: 16px; max-width: 800px; margin: 0 auto; }
.w240 { width: 240px; }
h3 { margin: 0 0 12px; }
</style>

