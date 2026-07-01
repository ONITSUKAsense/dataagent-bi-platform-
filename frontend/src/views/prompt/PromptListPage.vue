<template>
  <div class="prompt-list-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>Prompt 管理</span>
          <el-button type="primary" @click="router.push('/prompts/0')">新建 Prompt</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="code" label="编码" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="80" align="center">
          <template #default="{ row }">v{{ row.version }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="router.push(`/prompts/${row.id}`)">编辑</el-button>
            <el-button v-if="row.status === 0" text type="success" @click="handlePublish(row)">发布</el-button>
            <el-button text type="warning" @click="handleTest(row)">测试</el-button>
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

    <!-- Test Dialog -->
    <el-dialog v-model="testVisible" :title="'测试渲染 - ' + testPromptName" width="700px">
      <el-form :model="testVars" label-width="120px">
        <el-form-item v-for="v in testVariables" :key="v.name" :label="v.name">
          <el-input v-model="testVars[v.name]" :placeholder="v.description || v.name" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div v-if="testResult" class="test-result">
        <div class="result-label">渲染结果：</div>
        <pre class="result-content">{{ testResult }}</pre>
      </div>
      <template #footer>
        <el-button @click="testVisible = false">关闭</el-button>
        <el-button type="primary" :loading="testing" @click="runTest">渲染测试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPrompts, deletePrompt, publishPrompt, testPrompt, type PromptTemplateVO, type PromptVariable } from '@/api/prompt'

const router = useRouter()
const loading = ref(false)
const tableData = ref<PromptTemplateVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

// Test
const testVisible = ref(false)
const testing = ref(false)
const testPromptName = ref('')
const testVariables = ref<PromptVariable[]>([])
const testVars = ref<Record<string, string>>({})
const testResult = ref('')
const testPromptId = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const res = await getPrompts({ page: page.value, pageSize: pageSize.value })
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function handleTest(row: PromptTemplateVO) {
  testPromptId.value = row.id
  testPromptName.value = row.name
  testVariables.value = row.variables || []
  testVars.value = {}
  testResult.value = ''
  testVisible.value = true
}

async function runTest() {
  testing.value = true
  try {
    const res = await testPrompt(testPromptId.value, testVars.value)
    testResult.value = res.data.data
  } catch {
    ElMessage.error('测试失败')
  } finally {
    testing.value = false
  }
}

async function handlePublish(row: PromptTemplateVO) {
  await publishPrompt(row.id)
  ElMessage.success('已发布')
  fetchData()
}

async function handleDelete(id: number) {
  await deletePrompt(id)
  ElMessage.success('已删除')
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.test-result { margin-top: 16px; }
.result-label { font-weight: 600; margin-bottom: 8px; color: #606266; }
.result-content { background: #f5f7fa; padding: 16px; border-radius: 6px; white-space: pre-wrap; font-size: 13px; max-height: 300px; overflow-y: auto; }
</style>
