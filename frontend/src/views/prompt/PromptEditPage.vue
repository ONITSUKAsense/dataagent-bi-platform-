<template>
  <div class="prompt-edit-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <el-button text @click="router.back()">← 返回</el-button>
          <span>{{ isNew ? '新建 Prompt' : '编辑 Prompt' }}</span>
          <div>
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          </div>
        </div>
      </template>

      <el-form :model="form" label-width="100px" v-loading="loading">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="Prompt 名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="编码" required>
          <el-input v-model="form.code" placeholder="唯一编码，如 SQL_GENERATOR" :disabled="!isNew" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="简要描述用途" maxlength="500" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag v-if="form.status === 1" type="success">已发布 (v{{ form.version }})</el-tag>
          <el-tag v-else type="info">草稿</el-tag>
        </el-form-item>
        <el-form-item label="模板内容" required>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="使用 {variable_name} 引用变量"
          />
        </el-form-item>
        <el-form-item label="变量定义">
          <div class="variables-wrap">
            <div v-for="(v, i) in form.variables" :key="i" class="variable-row">
              <el-input v-model="v.name" placeholder="变量名" class="var-input" />
              <el-select v-model="v.type" class="var-type">
                <el-option label="string" value="string" />
                <el-option label="number" value="number" />
                <el-option label="boolean" value="boolean" />
              </el-select>
              <el-checkbox v-model="v.required">必填</el-checkbox>
              <el-input v-model="v.defaultValue" placeholder="默认值" class="var-default" />
              <el-button text type="danger" @click="form.variables.splice(i, 1)">删除</el-button>
            </div>
            <el-button type="primary" plain @click="addVariable">+ 添加变量</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Preview Card -->
    <el-card class="mt-4">
      <template #header>
        <span>预览</span>
      </template>
      <pre class="preview-content">{{ form.content || '暂无内容' }}</pre>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPromptDetail, addPrompt, updatePrompt, type PromptTemplateVO, type PromptVariable } from '@/api/prompt'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)

const isNew = computed(() => Number(route.params.id) === 0)

const form = ref<PromptTemplateVO>({
  id: 0,
  name: '',
  code: '',
  description: '',
  content: '',
  variables: [],
  version: 1,
  status: 0,
  createdBy: '',
  createdAt: '',
  updatedAt: '',
})

function addVariable() {
  form.value.variables.push({
    name: '',
    type: 'string',
    defaultValue: '',
    required: false,
  })
}

async function fetchDetail() {
  const id = Number(route.params.id)
  if (id === 0) return
  loading.value = true
  try {
    const res = await getPromptDetail(id)
    form.value = res.data.data
    if (!form.value.variables) form.value.variables = []
  } catch {
    ElMessage.error('加载失败')
    router.back()
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!form.value.name || !form.value.code || !form.value.content) {
    ElMessage.warning('请填写名称、编码和模板内容')
    return
  }
  saving.value = true
  try {
    const data = { ...form.value }
    data.variables = data.variables.filter(v => v.name)
    if (isNew.value) {
      await addPrompt(data)
      ElMessage.success('创建成功')
    } else {
      await updatePrompt(Number(route.params.id), data)
      ElMessage.success('已保存')
    }
    router.back()
  } finally {
    saving.value = false
  }
}

onMounted(() => { fetchDetail() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.mt-4 { margin-top: 16px; }
.variables-wrap { width: 100%; }
.variable-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.var-input { width: 150px; }
.var-type { width: 120px; }
.var-default { width: 150px; }
.preview-content { background: #f5f7fa; padding: 16px; border-radius: 6px; white-space: pre-wrap; font-size: 13px; max-height: 400px; overflow-y: auto; }
</style>
