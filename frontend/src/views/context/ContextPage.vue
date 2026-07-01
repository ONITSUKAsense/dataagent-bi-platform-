<template>
  <div class="context-page">
    <!-- Toolbar -->
    <el-card class="mb-4">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-input v-model="searchSessionId" placeholder="搜索会话ID" clearable @clear="fetchData" @keyup.enter="fetchData" />
        </el-col>
        <el-col :span="6">
          <el-input v-model="searchKey" placeholder="搜索上下文 Key" clearable @clear="fetchData" @keyup.enter="fetchData" />
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleCleanExpired">清理过期</el-button>
        </el-col>
        <el-col :span="6" style="text-align: right;">
          <el-button type="success" @click="addDialogVisible = true">添加上下文</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Table -->
    <el-card>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="sessionId" label="会话ID" width="200" show-overflow-tooltip />
        <el-table-column prop="contextKey" label="Key" width="200" show-overflow-tooltip />
        <el-table-column prop="contextValue" label="Value" min-width="260" show-overflow-tooltip />
        <el-table-column prop="expireAt" label="过期时间" width="180">
          <template #default="{ row }">
            <span v-if="row.expireAt" :class="{ 'text-danger': isExpired(row.expireAt) }">
              {{ row.expireAt }}
            </span>
            <span v-else class="text-muted">永不过期</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="warning" @click="handleSetExpire(row)">过期</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add Dialog -->
    <el-dialog v-model="addDialogVisible" title="添加上下文" width="550px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="会话ID" required>
          <el-input v-model="addForm.sessionId" placeholder="输入会话ID" />
        </el-form-item>
        <el-form-item label="Key" required>
          <el-input v-model="addForm.contextKey" placeholder="上下文 Key" />
        </el-form-item>
        <el-form-item label="Value" required>
          <el-input v-model="addForm.contextValue" type="textarea" :rows="4" placeholder="上下文值 (JSON)" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="addForm.expireAt" type="datetime" placeholder="可选" format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAddSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑上下文" width="550px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="会话ID">
          <el-input v-model="editForm.sessionId" disabled />
        </el-form-item>
        <el-form-item label="Key">
          <el-input v-model="editForm.contextKey" disabled />
        </el-form-item>
        <el-form-item label="Value" required>
          <el-input v-model="editForm.contextValue" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Set Expire Dialog -->
    <el-dialog v-model="expireDialogVisible" title="设置过期时间" width="450px">
      <el-form label-width="100px">
        <el-form-item label="过期时间" required>
          <el-date-picker v-model="expireTime" type="datetime" placeholder="选择过期时间" format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="expireDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleExpireSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyContext, addContext, updateContext, deleteContext, setContextExpire, cleanExpiredContext, type ContextVO } from '@/api/context'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ContextVO[]>([])
const searchSessionId = ref('')
const searchKey = ref('')

const filteredData = computed(() => {
  let data = tableData.value
  if (searchSessionId.value) {
    data = data.filter(d => d.sessionId.includes(searchSessionId.value))
  }
  if (searchKey.value) {
    data = data.filter(d => d.contextKey.includes(searchKey.value))
  }
  return data
})

// Add
const addDialogVisible = ref(false)
const addForm = ref({ sessionId: '', contextKey: '', contextValue: '', expireAt: null as string | null })

// Edit
const editDialogVisible = ref(false)
const editContextId = ref(0)
const editForm = ref({ sessionId: '', contextKey: '', contextValue: '' })

// Expire
const expireDialogVisible = ref(false)
const expireContextId = ref(0)
const expireTime = ref<Date | null>(null)

function isExpired(dateStr: string): boolean {
  return new Date(dateStr) < new Date()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyContext()
    tableData.value = res.data.data
  } finally {
    loading.value = false
  }
}

async function handleAddSubmit() {
  if (!addForm.value.sessionId || !addForm.value.contextKey || !addForm.value.contextValue) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    const data: any = {
      sessionId: addForm.value.sessionId,
      contextKey: addForm.value.contextKey,
      contextValue: addForm.value.contextValue,
    }
    if (addForm.value.expireAt) {
      data.expireAt = new Date(addForm.value.expireAt).toISOString().replace('T', ' ').substring(0, 19)
    }
    await addContext(data)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    addForm.value = { sessionId: '', contextKey: '', contextValue: '', expireAt: null }
    fetchData()
  } finally {
    submitting.value = false
  }
}

function handleEdit(row: ContextVO) {
  editContextId.value = row.id
  editForm.value = {
    sessionId: row.sessionId,
    contextKey: row.contextKey,
    contextValue: row.contextValue,
  }
  editDialogVisible.value = true
}

async function handleEditSubmit() {
  submitting.value = true
  try {
    await updateContext(editContextId.value, { contextValue: editForm.value.contextValue })
    ElMessage.success('已更新')
    editDialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

function handleSetExpire(row: ContextVO) {
  expireContextId.value = row.id
  expireTime.value = null
  expireDialogVisible.value = true
}

async function handleExpireSubmit() {
  if (!expireTime.value) {
    ElMessage.warning('请选择过期时间')
    return
  }
  submitting.value = true
  try {
    const expireAt = expireTime.value.toISOString().replace('T', ' ').substring(0, 19)
    await setContextExpire(expireContextId.value, { expireAt })
    ElMessage.success('已设置')
    expireDialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  await deleteContext(id)
  ElMessage.success('已删除')
  fetchData()
}

async function handleCleanExpired() {
  await cleanExpiredContext()
  ElMessage.success('已清理过期上下文')
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
.text-danger { color: #f56c6c; font-weight: 600; }
.text-muted { color: #909399; }
</style>
