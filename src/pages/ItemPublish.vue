<template>
  <div class="page">
    <el-card>
      <h3>发布二手商品</h3>
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
        <el-form-item label="图片">
          <UploadImage @update:images="(list) => (form.images = list)" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="5" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交发布</el-button>
          <el-button>保存草稿</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import UploadImage from '@/components/UploadImage.vue'
import { apiCreateItem } from '@/api/items'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
const router = useRouter()

const form = reactive({
  title: '',
  price: 0,
  stock: 1,
  category: '',
  images: [] as string[],
  description: ''
})
const loading = ref(false)

async function submit() {
  if (!form.title || !form.price || !form.category || form.images.length === 0) {
    ElMessage.error('请填写标题/价格/分类并至少上传一张图片')
    return
  }
  try {
    loading.value = true
    const item = await apiCreateItem({
      title: form.title,
      price: form.price,
      stock: form.stock,
      category: form.category,
      images: form.images,
      description: form.description
    })
    ElMessage.success('发布成功')
    router.push({ name: 'item-detail', params: { id: item.id } })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { padding: 16px; max-width: 800px; margin: 0 auto; }
.w240 { width: 240px; }
h3 { margin: 0 0 12px; }
</style>


