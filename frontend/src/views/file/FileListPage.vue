<template>
  <div class="file-list-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>文件中心</span>
          <el-upload
            :http-request="handleUpload"
            :show-file-list="false"
            multiple
          >
            <el-button type="primary" :loading="uploading">上传文件</el-button>
          </el-upload>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="originalName" label="文件名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="size" label="大小" width="120" align="right">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column prop="type" label="MIME 类型" width="200" show-overflow-tooltip />
        <el-table-column prop="ext" label="扩展名" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.ext || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="previewFile(row)">预览</el-button>
            <el-popconfirm title="确认删除该文件？" @confirm="handleDelete(row.id)">
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

    <!-- Preview Dialog -->
    <el-dialog v-model="previewVisible" title="文件预览" width="800px" top="5vh">
      <div v-if="previewType === 'image'" class="preview-image-wrap">
        <img :src="previewUrl" class="preview-image" />
      </div>
      <div v-else-if="previewType === 'pdf'" class="preview-iframe-wrap">
        <iframe :src="previewUrl" class="preview-iframe" />
      </div>
      <div v-else class="preview-text-wrap">
        <p>暂不支持预览该类型文件，请直接下载查看。</p>
        <el-button type="primary" @click="downloadFile">下载文件</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFiles, uploadFile, deleteFile, type FileVO } from '@/api/file'

const loading = ref(false)
const uploading = ref(false)
const tableData = ref<FileVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref<'image' | 'pdf' | 'other'>('other')

function formatSize(bytes: number): string {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getFiles({ page: page.value, pageSize: pageSize.value })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

async function handleUpload(options: any) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await uploadFile(formData)
    ElMessage.success('上传成功')
    fetchData()
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleDelete(id: number) {
  await deleteFile(id)
  ElMessage.success('已删除')
  fetchData()
}

function previewFile(row: FileVO) {
  previewUrl.value = row.url
  const ext = (row.ext || '').toLowerCase()
  if (['.png', '.jpg', '.jpeg', '.gif', '.svg', '.webp'].includes(ext)) {
    previewType.value = 'image'
  } else if (ext === '.pdf') {
    previewType.value = 'pdf'
  } else {
    previewType.value = 'other'
  }
  previewVisible.value = true
}

function downloadFile() {
  window.open(previewUrl.value, '_blank')
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.preview-image-wrap { text-align: center; }
.preview-image { max-width: 100%; max-height: 70vh; object-fit: contain; }
.preview-iframe-wrap { height: 70vh; }
.preview-iframe { width: 100%; height: 100%; border: none; }
.preview-text-wrap { text-align: center; padding: 40px 0; color: #909399; }
</style>
