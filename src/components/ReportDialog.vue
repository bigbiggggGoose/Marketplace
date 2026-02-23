<template>
  <el-dialog v-model="visible" title="举报" width="420px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="原因">
        <el-select v-model="form.reason" placeholder="选择原因">
          <el-option label="假货/欺诈" value="fraud" />
          <el-option label="违规信息" value="illegal" />
          <el-option label="骚扰/广告" value="spam" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.desc" type="textarea" placeholder="补充说明（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { apiReport } from '@/api/report'
import { ElMessage } from 'element-plus'

const props = defineProps<{ modelValue: boolean; itemId: string | number }>()
const emit = defineEmits<{ (e: 'update:modelValue', val: boolean): void }>()
const visible = ref(props.modelValue)
watch(() => props.modelValue, v => (visible.value = v))
watch(visible, v => emit('update:modelValue', v))

const form = ref({ reason: '', desc: '' })
async function submit() {
  await apiReport(props.itemId, form.value)
  ElMessage.success('已提交举报')
  visible.value = false
}
</script>


