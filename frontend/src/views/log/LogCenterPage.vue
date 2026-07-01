<template>
  <div class="log-center-page">
    <el-tabs v-model="activeTab" @tab-change="fetchData">
      <el-tab-pane label="Agent 日志" name="agent">
        <el-card>
          <template #header>
            <div class="toolbar">
              <el-select v-model="agentTypeFilter" placeholder="Agent 类型" clearable style="width:150px" @change="fetchData">
                <el-option label="SQL生成" value="sql_generator" />
                <el-option label="图表生成" value="chart_generator" />
                <el-option label="报告生成" value="report_generator" />
              </el-select>
              <el-select v-model="statusFilter" placeholder="状态" clearable style="width:120px;margin-left:8px" @change="fetchData">
                <el-option label="成功" :value="0" />
                <el-option label="失败" :value="1" />
              </el-select>
            </div>
          </template>
          <el-table :data="tableData" v-loading="loading" border stripe @row-click="showDetail">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="agentType" label="Agent 类型" width="130">
              <template #default="{ row }">{{ agentTypeLabel(row.agentType) }}</template>
            </el-table-column>
            <el-table-column prop="llmModel" label="模型" width="120" />
            <el-table-column prop="input" label="输入" min-width="200" show-overflow-tooltip />
            <el-table-column prop="totalTokens" label="Token" width="90" align="right" />
            <el-table-column prop="costMs" label="耗时(ms)" width="100" align="right" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
                  {{ row.status === 0 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="180" />
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="page" v-model:page-size="pageSize"
              :total="total" layout="total, prev, pager, next"
              @current-change="fetchData" />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="错误日志" name="error">
        <el-card>
          <el-table :data="errorData" v-loading="errorLoading" border stripe @row-click="showDetail">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="agentType" label="Agent 类型" width="130" />
            <el-table-column prop="errorMsg" label="错误信息" min-width="300" show-overflow-tooltip />
            <el-table-column prop="input" label="输入" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="时间" width="180" />
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="errorPage" v-model:page-size="errorPageSize"
              :total="errorTotal" layout="total, prev, pager, next"
              @current-change="fetchErrorLogs" />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="SQL 日志" name="sql">
        <el-card>
          <el-table :data="sqlLogData" v-loading="sqlLoading" border stripe @row-click="showDetail">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="input" label="问题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="output" label="生成的 SQL" min-width="300" show-overflow-tooltip />
            <el-table-column prop="totalTokens" label="Token" width="90" align="right" />
            <el-table-column prop="createdAt" label="时间" width="180" />
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="sqlPage" v-model:page-size="sqlPageSize"
              :total="sqlTotal" layout="total, prev, pager, next"
              @current-change="fetchSQLLogs" />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Token 统计" name="token">
        <el-card>
          <template #header>
            <div class="toolbar">
              <span>Token 用量趋势 (近7天)</span>
            </div>
          </template>
          <div v-if="tokenStats.length === 0" class="empty-chart">暂无数据</div>
          <ChartRenderer v-else :option="chartOption" :height="400" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Detail Drawer -->
    <el-drawer v-model="detailVisible" title="日志详情" size="600px">
      <div v-if="detailData" class="detail-wrap">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="Agent 类型">{{ detailData.agentType }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ detailData.llmModel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.status === 0 ? 'success' : 'danger'" size="small">
              {{ detailData.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Token">{{ detailData.totalTokens }}</el-descriptions-item>
          <el-descriptions-item label="耗时(ms)">{{ detailData.costMs }}</el-descriptions-item>
          <el-descriptions-item label="时间" :span="2">{{ detailData.createdAt }}</el-descriptions-item>
          <el-descriptions-item v-if="detailData.errorMsg" label="错误信息" :span="2">
            <span class="text-danger">{{ detailData.errorMsg }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div class="detail-section">
          <div class="section-label">输入：</div>
          <pre class="section-content">{{ detailData.input }}</pre>
        </div>
        <div class="detail-section">
          <div class="section-label">输出：</div>
          <pre class="section-content">{{ detailData.output }}</pre>
        </div>
      </div>
      <div v-else class="empty-detail">加载中...</div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAgentLogs, getErrorLogs, getSQLLogs, getAgentLogDetail, getTokenStats, type AgentLogVO, type TokenStatsVO } from '@/api/log'
import ChartRenderer from '@/components/ChartRenderer.vue'

const activeTab = ref('agent')

// Agent logs
const loading = ref(false)
const tableData = ref<AgentLogVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const agentTypeFilter = ref('')
const statusFilter = ref()

// Error logs
const errorLoading = ref(false)
const errorData = ref<AgentLogVO[]>([])
const errorTotal = ref(0)
const errorPage = ref(1)
const errorPageSize = ref(10)

// SQL logs
const sqlLoading = ref(false)
const sqlLogData = ref<AgentLogVO[]>([])
const sqlTotal = ref(0)
const sqlPage = ref(1)
const sqlPageSize = ref(10)

// Token stats
const tokenStats = ref<TokenStatsVO[]>([])

// Detail drawer
const detailVisible = ref(false)
const detailData = ref<AgentLogVO | null>(null)

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' as const },
  xAxis: { type: 'category' as const, data: tokenStats.value.map(t => t.date) },
  yAxis: { type: 'value' as const, name: 'Tokens' },
  series: [{
    name: 'Token 用量', type: 'line' as const, smooth: true,
    data: tokenStats.value.map(t => t.totalTokens),
    areaStyle: { opacity: 0.3 },
  }],
}) as any)

function agentTypeLabel(type: string): string {
  const map: Record<string, string> = {
    sql_generator: 'SQL生成', chart_generator: '图表生成', report_generator: '报告生成',
  }
  return map[type] || type
}

async function fetchData() {
  if (activeTab.value === 'agent') {
    loading.value = true
    try {
      const res = await getAgentLogs({
        page: page.value, pageSize: pageSize.value,
        agentType: agentTypeFilter.value || undefined,
        status: statusFilter.value !== '' && statusFilter.value !== undefined ? Number(statusFilter.value) : undefined,
      })
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    } finally { loading.value = false }
  } else if (activeTab.value === 'error') {
    fetchErrorLogs()
  } else if (activeTab.value === 'sql') {
    fetchSQLLogs()
  } else if (activeTab.value === 'token') {
    try {
      const res = await getTokenStats({})
      tokenStats.value = res.data.data
    } catch { tokenStats.value = [] }
  }
}

async function fetchErrorLogs() {
  errorLoading.value = true
  try {
    const res = await getErrorLogs({ page: errorPage.value, pageSize: errorPageSize.value })
    errorData.value = res.data.data.records
    errorTotal.value = res.data.data.total
  } finally { errorLoading.value = false }
}

async function fetchSQLLogs() {
  sqlLoading.value = true
  try {
    const res = await getSQLLogs({ page: sqlPage.value, pageSize: sqlPageSize.value })
    sqlLogData.value = res.data.data.records
    sqlTotal.value = res.data.data.total
  } finally { sqlLoading.value = false }
}

async function showDetail(row: AgentLogVO) {
  try {
    const res = await getAgentLogDetail(row.id)
    detailData.value = res.data.data
  } catch { detailData.value = row }
  detailVisible.value = true
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.toolbar { display: flex; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.empty-chart { text-align: center; color: #909399; padding: 60px 0; }
.detail-wrap { padding: 0 8px; }
.detail-section { margin-top: 20px; }
.section-label { font-weight: 600; color: #606266; margin-bottom: 8px; }
.section-content { background: #f5f7fa; padding: 12px; border-radius: 6px; white-space: pre-wrap; font-size: 13px; max-height: 300px; overflow-y: auto; }
.text-danger { color: #f56c6c; }
</style>
