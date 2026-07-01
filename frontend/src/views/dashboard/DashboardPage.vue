<template>
  <div class="dashboard">
    <!-- Stats Cards -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: card.bg }">
            <el-icon :size="24" color="#fff"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" class="mt-4">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Agent 调用趋势（近30天）</span>
              <el-radio-group v-model="trendType" size="small" @change="updateAgentChart">
                <el-radio-button value="agent">调用次数</el-radio-button>
                <el-radio-button value="token">Token 消耗</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="agentChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>最近报告</template>
          <div v-if="recentReports.length === 0" class="empty-state">暂无报告数据</div>
          <div v-for="item in recentReports" :key="item.id" class="report-row" @click="router.push(`/reports/${item.id}`)">
            <div class="report-title">
              <el-tag :type="statusType(item.status)" size="small" class="report-status">
                {{ statusLabel(item.status) }}
              </el-tag>
              <span class="report-name">{{ item.title }}</span>
            </div>
            <div class="report-meta">
              <span>{{ item.createdBy }}</span>
              <span>{{ item.createdAt }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Bottom Stats -->
    <el-row :gutter="20" class="mt-4">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>平台概览</template>
          <el-row :gutter="20">
            <el-col :span="8" v-for="item in overviewItems" :key="item.label">
              <div class="overview-item">
                <div class="overview-value">{{ item.value }}</div>
                <div class="overview-label">{{ item.label }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getDashboardStats, getRecentReports, getAgentTrend, getTokenTrend, getUserStats } from '@/api/dashboard'
import type { DashboardStats, TrendItem, RecentReport } from '@/types'

const router = useRouter()
const agentChartRef = ref<HTMLElement>()
let agentChart: echarts.ECharts | null = null
const trendType = ref<'agent' | 'token'>('agent')

const stats = ref<DashboardStats>({
  userCount: 0, reportCount: 0, agentCallCount: 0, tokenConsumed: 0, todayCallCount: 0,
})

const recentReports = ref<RecentReport[]>([])
const agentTrendData = ref<TrendItem[]>([])
const tokenTrendData = ref<TrendItem[]>([])
const newUsersThisWeek = ref(0)

const statCards = computed(() => [
  { label: '用户总数', value: stats.value.userCount, icon: 'User', bg: 'linear-gradient(135deg, #409eff, #337ecc)' },
  { label: '报告总数', value: stats.value.reportCount, icon: 'Document', bg: 'linear-gradient(135deg, #67c23a, #529b2e)' },
  { label: 'Agent 调用', value: stats.value.agentCallCount, icon: 'Cpu', bg: 'linear-gradient(135deg, #e6a23c, #cf9236)' },
  { label: 'Token 消耗', value: formatToken(stats.value.tokenConsumed), icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #f56c6c, #d9534f)' },
])

const overviewItems = computed(() => [
  { label: '用户总数', value: stats.value.userCount },
  { label: '本周新增用户', value: newUsersThisWeek.value },
  { label: '今日 Agent 调用', value: stats.value.todayCallCount },
  { label: '报告总数', value: stats.value.reportCount },
  { label: '累计 Token', value: formatToken(stats.value.tokenConsumed) },
])

function formatToken(n: number): string {
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K'
  return String(n)
}

function statusType(status: number): 'warning' | 'success' | 'danger' {
  return status === 0 ? 'warning' : status === 1 ? 'success' : 'danger'
}

function statusLabel(status: number): string {
  return status === 0 ? '生成中' : status === 1 ? '已生成' : '失败'
}

function updateAgentChart() {
  if (!agentChart) return
  const data = trendType.value === 'agent' ? agentTrendData.value : tokenTrendData.value
  agentChart.setOption({
    xAxis: { data: data.map(d => d.date) },
    series: [{ data: data.map(d => d.count) }],
  })
}

function initAgentChart() {
  if (!agentChartRef.value) return
  agentChart = echarts.init(agentChartRef.value)
  agentChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: [],
      axisLine: { lineStyle: { color: '#e0e0e0' } },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f5f5f5' } },
    },
    series: [{
      type: 'line',
      smooth: true,
      data: [],
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' },
        ]),
      },
      itemStyle: { color: '#409eff' },
    }],
  })
  updateAgentChart()
}

async function fetchData() {
  try {
    const [statsRes, reportsRes, agentRes, tokenRes, userRes] = await Promise.all([
      getDashboardStats(),
      getRecentReports(),
      getAgentTrend(),
      getTokenTrend(),
      getUserStats(),
    ])
    stats.value = statsRes.data.data
    recentReports.value = reportsRes.data.data
    agentTrendData.value = agentRes.data.data
    tokenTrendData.value = tokenRes.data.data
    newUsersThisWeek.value = userRes.data.data?.newUsersThisWeek || 0
    updateAgentChart()
  } catch {
    // Silently handle — initial state shows zeros
  }
}

function handleResize() {
  agentChart?.resize()
}

onMounted(() => {
  fetchData()
  initAgentChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  agentChart?.dispose()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  cursor: default;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  margin-left: 16px;
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.mt-4 { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.report-row {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.report-row:hover { background: #fafafa; }
.report-row:last-child { border-bottom: none; }
.report-title { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.report-name { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.report-status { flex-shrink: 0; }
.report-meta { display: flex; justify-content: space-between; font-size: 12px; color: #909399; margin-left: 50px; }
.empty-state { text-align: center; color: #909399; padding: 40px 0; }
.overview-item { text-align: center; padding: 16px 0; }
.overview-value { font-size: 32px; font-weight: 700; color: #303133; }
.overview-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>
