<template>
  <div class="knowledge-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>知识库</span>
          <el-upload
            :http-request="handleUpload"
            :show-file-list="false"
            accept=".pdf,.docx,.md,.txt"
          >
            <el-button type="primary" :loading="uploading">上传文档</el-button>
          </el-upload>
        </div>
      </template>

      <!-- Stats -->
      <el-row :gutter="20" class="mb-4">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-value">{{ stats.totalDocs }}</div>
            <div class="stat-label">文档总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-value">{{ stats.totalChunks }}</div>
            <div class="stat-label">切片总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-value">{{ stats.readyDocs }}</div>
            <div class="stat-label">已就绪</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-value">{{ stats.processingDocs }}</div>
            <div class="stat-label">处理中</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="文档名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="docType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.docType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="切片数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="viewChunks(row)">查看切片</el-button>
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

    <!-- Chunks Dialog -->
    <el-dialog v-model="chunksVisible" :title="'切片详情 - ' + selectedDocTitle" width="700px">
      <div v-if="chunks.length === 0" class="empty-chunks">暂无切片数据</div>
      <div v-for="c in chunks" :key="c.id" class="chunk-item">
        <div class="chunk-header">#{{ c.chunkIndex }}</div>
        <div class="chunk-content">{{ c.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadProps } from 'element-plus'
import { getKnowledgeDocs, uploadKnowledgeDoc, deleteKnowledgeDoc, getKnowledgeChunks, type KnowledgeDocVO, type ChunkVO } from '@/api/knowledge'

const loading = ref(false)
const uploading = ref(false)
const tableData = ref<KnowledgeDocVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const chunksVisible = ref(false)
const selectedDocTitle = ref('')
const chunks = ref<ChunkVO[]>([])

const stats = reactive({
  totalDocs: 0, totalChunks: 0, readyDocs: 0, processingDocs: 0,
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getKnowledgeDocs({ page: page.value, pageSize: pageSize.value })
    tableData.value = res.data.data.records
    total.value = res.data.data.total

    // Calculate stats
    const all = res.data.data.records
    stats.totalDocs = total.value
    stats.totalChunks = all.reduce((s, d) => s + (d.chunkCount || 0), 0)
    stats.readyDocs = all.filter(d => d.status === 1).length
    stats.processingDocs = all.filter(d => d.status === 0).length
  } finally {
    loading.value = false
  }
}

async function handleUpload(options: any) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await uploadKnowledgeDoc(formData)
    ElMessage.success('上传成功')
    fetchData()
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleDelete(id: number) {
  await deleteKnowledgeDoc(id)
  ElMessage.success('已删除')
  fetchData()
}

async function viewChunks(row: KnowledgeDocVO) {
  selectedDocTitle.value = row.title
  try {
    const res = await getKnowledgeChunks(row.id)
    chunks.value = res.data.data
  } catch {
    chunks.value = []
  }
  chunksVisible.value = true
}

function statusType(status: number): 'warning' | 'success' | 'danger' | 'info' {
  return status === 0 ? 'warning' : status === 1 ? 'success' : 'danger'
}

function statusLabel(status: number): string {
  return status === 0 ? '处理中' : status === 1 ? '已就绪' : '失败'
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.mb-4 { margin-bottom: 16px; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.chunk-item { margin-bottom: 12px; padding: 12px; background: #f5f7fa; border-radius: 6px; }
.chunk-header { font-size: 12px; color: #909399; margin-bottom: 4px; }
.chunk-content { font-size: 14px; line-height: 1.6; color: #303133; }
.empty-chunks { text-align: center; color: #909399; padding: 40px 0; }
</style>
