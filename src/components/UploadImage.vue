<template>
  <el-upload
    :action="action"
    :headers="headers"
    list-type="picture-card"
    v-model:file-list="fileList"
    :on-success="onSuccess"
    :on-remove="onRemove"
    accept="image/*"
  >
    <el-icon><Plus /></el-icon>
  </el-upload>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { UploadFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'

const props = defineProps<{ action?: string }>()
const emit = defineEmits<{
  (e: 'update:images', images: string[]): void
}>()

const action = props.action ?? '/api/upload'
const fileList = ref<UploadFile[]>([])
const auth = useAuthStore()
const headers = computed(() => auth.token ? { Authorization: `Bearer ${auth.token}` } : {})

function onSuccess(response: any, file: UploadFile) {
  // 假设后端返回 { url: '...' }
  file.url = response?.url || file.url
  // 确保当前文件在受控列表中（v-model 已同步）
  emit('update:images', (fileList.value || []).map(f => f.url!).filter(Boolean))
}

function onRemove() {
  emit('update:images', (fileList.value || []).map(f => f.url!).filter(Boolean))
}
</script>


