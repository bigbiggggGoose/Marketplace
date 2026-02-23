<template>
  <div class="page">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审核商品" name="items">
          <div class="toolbar">
            <el-button size="small" :loading="reviewLoading" @click="loadReview">刷新</el-button>
          </div>
          <el-table :data="reviewItems" size="small" v-loading="reviewLoading" empty-text="暂无待审核商品">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="sellerName" label="卖家" width="140" />
            <el-table-column label="状态" width="120">
              <template #default="scope">
                <el-tag size="small" :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核标记" width="140">
              <template #default="scope">
                <el-tag size="small" type="info">{{ reviewText(scope.row.reviewStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="180">
              <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="260">
              <template #default="scope">
                <el-button size="small" @click="goDetail(scope.row.id)">查看</el-button>
                <el-button size="small" type="success" @click="handleItem(scope.row.id, 'approve')">确认没问题</el-button>
                <el-button size="small" type="warning" @click="handleItem(scope.row.id, 'mark_resolved')">标记已查看</el-button>
                <el-button size="small" type="danger" @click="handleItem(scope.row.id, 'take_down')">立即下架</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="举报处理" name="reports">
          <div class="toolbar">
            <el-select v-model="reportStatus" size="small" style="width: 140px" @change="loadReports">
              <el-option label="待处理" value="PENDING" />
              <el-option label="已处理" value="PROCESSED" />
              <el-option label="已忽略" value="IGNORED" />
              <el-option label="全部" value="ALL" />
            </el-select>
            <el-button size="small" :loading="reportLoading" @click="loadReports">刷新</el-button>
          </div>
          <el-table :data="reports" size="small" v-loading="reportLoading" empty-text="暂无举报">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="itemId" label="商品ID" width="100" />
            <el-table-column prop="reason" label="原因" width="120" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="createdAt" label="时间" width="180">
              <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag size="small" :type="reportType(scope.row.status)">{{ reportText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button size="small" @click="goDetail(scope.row.itemId)">查看商品</el-button>
                <el-button size="small" type="success" :disabled="scope.row.status !== 'PENDING'" @click="handleReport(scope.row.id, 'approve')">确认问题</el-button>
                <el-button size="small" type="warning" :disabled="scope.row.status !== 'PENDING'" @click="handleReport(scope.row.id, 'ignore')">忽略</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiAdminGetPendingItems, apiAdminProcessItem, apiAdminGetReports, apiAdminProcessReport, type ReportRecord } from '@/api/admin'
import type { Item } from '@/types'

const router = useRouter()
const activeTab = ref('items')
const reviewItems = ref<Item[]>([])
const reviewLoading = ref(false)
const reports = ref<ReportRecord[]>([])
const reportStatus = ref<'PENDING' | 'PROCESSED' | 'IGNORED' | 'ALL'>('PENDING')
const reportLoading = ref(false)

const loadReview = async () => {
  try {
    reviewLoading.value = true
    reviewItems.value = await apiAdminGetPendingItems()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '加载待审核商品失败')
  } finally {
    reviewLoading.value = false
  }
}

const loadReports = async () => {
  try {
    reportLoading.value = true
    const res = await apiAdminGetReports({ status: reportStatus.value, page: 0, pageSize: 50 })
    reports.value = res.list || []
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '加载举报失败')
  } finally {
    reportLoading.value = false
  }
}

const handleItem = async (id: number | string, action: 'approve' | 'take_down' | 'mark_resolved' | 'put_on') => {
  try {
    await apiAdminProcessItem(id, action)
    ElMessage.success('操作成功')
    await loadReview()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '操作失败')
  }
}

const handleReport = async (id: number | string, action: 'approve' | 'ignore') => {
  try {
    await apiAdminProcessReport(id, action)
    ElMessage.success('处理成功')
    await loadReports()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '处理失败')
  }
}

const goDetail = (id: number | string) => {
  router.push({ name: 'item-detail', params: { id } })
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const statusText = (status?: string) => {
  if (!status) return '未知'
  const map: Record<string, string> = {
    ON_SALE: '在售',
    OUT_OF_STOCK: '缺货',
    OFFLINE: '已下架'
  }
  return map[status] || status
}

const statusType = (status?: string) => {
  const map: Record<string, string> = {
    ON_SALE: 'success',
    OUT_OF_STOCK: 'warning',
    OFFLINE: 'info'
  }
  return map[status || ''] || 'info'
}

const reviewText = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: '待查看',
    APPROVED: '已确认',
    RESOLVED: '已处理'
  }
  return map[status || ''] || '未知'
}

const reportText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    PROCESSED: '已处理',
    IGNORED: '已忽略'
  }
  return map[status] || status
}

const reportType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    PROCESSED: 'success',
    IGNORED: 'info'
  }
  return map[status] || 'info'
}

onMounted(() => {
  loadReview()
  loadReports()
})
</script>

<style scoped>
.page { padding: 16px; max-width: 1200px; margin: 0 auto; }
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
</style>


