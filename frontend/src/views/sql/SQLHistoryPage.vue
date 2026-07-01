<template>
  <div class="sql-page">
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6" v-for="s in statCards" :key="s.label">
        <el-card shadow="hover">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="page-header">
          <span>SQL 执行历史</span>
          <el-input v-model="searchQuestion" placeholder="搜索问题" clearable style="width: 200px" @keyup.enter="fetchData" />
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="originalQuestion" label="问题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="generatedSql" label="生成的 SQL" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="sql-code">{{ row.generatedSql }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="rowCount" label="行数" width="70" align="center" />
        <el-table-column prop="costMs" label="耗时" width="80" align="center">
          <template #default="{ row }">{{ row.costMs }}ms</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isFavorite" label="收藏" width="80" align="center">
          <template #default="{ row }">
            <el-icon :color="row.isFavorite ? '#e6a23c' : '#c0c4cc'" @click="handleToggleFavorite(row)" style="cursor: pointer">
              <StarFilled v-if="row.isFavorite" />
              <Star v-else />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="tokenCost" label="Token" width="80" align="center" />
        <el-table-column prop="createdAt" label="时间" width="180" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { getSQLHistory, toggleFavorite, getSQLStats, type SQLHistoryVO } from '@/api/sql'

const loading = ref(false)
const tableData = ref<SQLHistoryVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const searchQuestion = ref('')
const stats = ref({ totalCount: 0, successCount: 0, favoriteCount: 0, avgCostMs: 0 })

const statCards = computed(() => [
  { label: '总执行次数', value: stats.value.totalCount },
  { label: '成功次数', value: stats.value.successCount },
  { label: '收藏次数', value: stats.value.favoriteCount },
  { label: '平均耗时', value: stats.value.avgCostMs + 'ms' },
])

async function fetchData() {
  loading.value = true
  try {
    const res = await getSQLHistory({ page: page.value, pageSize: pageSize.value, question: searchQuestion.value || undefined })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    const res = await getSQLStats()
    stats.value = res.data.data as any
  } catch { /* ignore */ }
}

async function handleToggleFavorite(row: SQLHistoryVO) {
  await toggleFavorite(row.id)
  row.isFavorite = !row.isFavorite
  ElMessage.success(row.isFavorite ? '已收藏' : '已取消收藏')
  fetchStats()
}

onMounted(() => { fetchData(); fetchStats() })
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.sql-code { font-size: 12px; background: #f5f7fa; padding: 2px 6px; border-radius: 3px; color: #606266; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
