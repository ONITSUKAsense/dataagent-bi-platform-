<template>
  <div class="report-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>报告中心</span>
          <el-input v-model="searchTitle" placeholder="搜索报告标题" clearable style="width: 200px" @clear="fetchData" @keyup.enter="fetchData" />
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe @row-click="goDetail">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="报告标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdBy" label="创建人" width="120" />
        <el-table-column prop="version" label="版本" width="70" align="center">
          <template #default="{ row }">v{{ row.version }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已生成' : '生成中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right" @click.stop>
          <template #default="{ row }">
            <el-button text type="primary" @click="router.push(`/reports/${row.id}`)">查看</el-button>
            <el-button text type="success" @click="handleExport(row)">导出</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReports, deleteReport, exportReportPdf, type ReportVO } from '@/api/report'

const router = useRouter()
const loading = ref(false)
const tableData = ref<ReportVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const searchTitle = ref('')

async function fetchData() {
  loading.value = true
  try {
    const res = await getReports({ page: page.value, pageSize: pageSize.value, title: searchTitle.value || undefined })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function goDetail(row: ReportVO) {
  router.push(`/reports/${row.id}`)
}

async function handleDelete(id: number) {
  await deleteReport(id)
  ElMessage.success('删除成功')
  fetchData()
}

async function handleExport(row: ReportVO) {
  try {
    const res = await exportReportPdf(row.id)
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `report_${row.id}.html`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.warning('导出功能暂未完全实现')
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
