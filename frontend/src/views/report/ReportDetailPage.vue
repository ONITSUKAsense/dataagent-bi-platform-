<template>
  <div class="report-detail" v-loading="loading">
    <div class="detail-header">
      <el-button text @click="router.back()">← 返回列表</el-button>
      <div class="header-actions" v-if="report">
        <el-button @click="showVersions = true">版本管理 (v{{ report.version }})</el-button>
        <el-button @click="handleExportPdf">导出 HTML</el-button>
        <el-button @click="handleExportExcel">导出 CSV</el-button>
        <el-button type="primary" @click="handleShare">分享</el-button>
      </div>
    </div>

    <div v-if="report" class="detail-body">
      <h1>{{ report.title }}</h1>
      <div class="meta">
        <span>创建人：{{ report.createdBy }}</span>
        <span>创建时间：{{ report.createdAt }}</span>
        <span>版本：v{{ report.version }}</span>
      </div>
      <el-divider />
      <MarkdownRenderer :content="report.contentMd || ''" />
    </div>

    <div v-else-if="!loading" class="empty">报告不存在</div>

    <!-- Version Dialog -->
    <el-dialog v-model="showVersions" title="版本管理" width="600px">
      <el-table :data="versions" border stripe>
        <el-table-column prop="version" label="版本" width="80">
          <template #default="{ row }">v{{ row.version }}</template>
        </el-table-column>
        <el-table-column prop="changeNote" label="变更说明" />
        <el-table-column prop="createdBy" label="操作人" width="120" />
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleRestore(row)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showVersions = false">关闭</el-button>
        <el-button type="primary" @click="handleNewVersion">创建新版本</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportDetail, getReportVersions, createReportVersion, restoreReportVersion, exportReportPdf, exportReportExcel, shareReport, type ReportVO, type ReportVersionVO } from '@/api/report'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const report = ref<ReportVO | null>(null)
const versions = ref<ReportVersionVO[]>([])
const showVersions = ref(false)

async function fetchDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getReportDetail(id)
    report.value = res.data.data
  } catch {
    report.value = null
  } finally {
    loading.value = false
  }
}

async function fetchVersions() {
  try {
    const id = Number(route.params.id)
    const res = await getReportVersions(id)
    versions.value = res.data.data
  } catch {
    versions.value = []
  }
}

async function handleNewVersion() {
  try {
    const { value } = await ElMessageBox.prompt('输入版本说明', '新版本')
    await createReportVersion(Number(route.params.id), { changeNote: value || '新版本' })
    ElMessage.success('版本已创建')
    fetchVersions()
    fetchDetail()
  } catch {
    // cancelled
  }
}

async function handleRestore(version: ReportVersionVO) {
  try {
    await ElMessageBox.confirm(`确认恢复到 v${version.version}？`, '提示')
    await restoreReportVersion(Number(route.params.id), { versionId: version.id })
    ElMessage.success('已恢复')
    fetchDetail()
  } catch {
    // cancelled
  }
}

async function handleShare() {
  try {
    const res = await shareReport(Number(route.params.id))
    await navigator.clipboard.writeText(res.data.data.url)
    ElMessage.success('分享链接已复制')
  } catch {
    ElMessage.warning('分享功能暂不可用')
  }
}

async function handleExportPdf() {
  try {
    const res = await exportReportPdf(Number(route.params.id))
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url; a.download = `report_${route.params.id}.html`
    a.click(); URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.warning('导出失败') }
}

async function handleExportExcel() {
  try {
    const res = await exportReportExcel(Number(route.params.id))
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url; a.download = `report_${route.params.id}.csv`
    a.click(); URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.warning('导出失败') }
}

onMounted(() => { fetchDetail(); fetchVersions() })
</script>

<style scoped>
.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header-actions { display: flex; gap: 8px; }
.detail-body { background: #fff; padding: 32px 40px; border-radius: 8px; }
.detail-body h1 { margin: 0 0 12px; font-size: 24px; }
.meta { display: flex; gap: 24px; color: #909399; font-size: 13px; }
.empty { text-align: center; color: #909399; padding: 80px 0; }
</style>
