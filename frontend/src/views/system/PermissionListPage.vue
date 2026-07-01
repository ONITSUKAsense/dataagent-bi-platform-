<template>
  <div class="perm-page">
    <el-card>
      <template #header>
        <span>权限管理</span>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe row-key="id" default-expand-all>
        <el-table-column prop="name" label="权限名称" tree-node />
        <el-table-column prop="code" label="权限编码" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" size="small">菜单</el-tag>
            <el-tag v-else-if="row.type === 2" type="success" size="small">按钮</el-tag>
            <el-tag v-else type="warning" size="small">接口</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPermissions, type PermissionVO } from '@/api/system/permission'

const loading = ref(false)
const tableData = ref<PermissionVO[]>([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getPermissions()
    tableData.value = res.data.data
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchData() })
</script>
