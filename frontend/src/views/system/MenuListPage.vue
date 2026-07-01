<template>
  <div class="menu-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="openAdd(0)">新增根菜单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe row-key="id" default-expand-all>
        <el-table-column prop="name" label="菜单名称" tree-node />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="permissionCode" label="权限编码" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.type === 0" size="small">目录</el-tag>
            <el-tag v-else-if="row.type === 1" type="success" size="small">菜单</el-tag>
            <el-tag v-else type="warning" size="small">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="visible" label="可见" width="70">
          <template #default="{ row }">
            <el-tag :type="row.visible ? 'success' : 'info'" size="small">
              {{ row.visible ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openAdd(row.id)">添加子菜单</el-button>
            <el-button text type="warning" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除（含子菜单）？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="菜单名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="父级ID">
          <el-input-number v-model="form.parentId" :min="0" :disabled="!isEdit" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" placeholder="/example" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="form.component" placeholder="views/example.vue" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Menu | Setting | User" />
        </el-form-item>
        <el-form-item label="权限编码">
          <el-input v-model="form.permissionCode" placeholder="sys:example" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="可见">
          <el-switch v-model="form.visible" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMenus, addMenu, updateMenu, deleteMenu, type MenuVO } from '@/api/system/menu'

const loading = ref(false)
const tableData = ref<MenuVO[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)

const form = reactive({
  name: '', parentId: 0, path: '', component: '', icon: '',
  permissionCode: '', type: 0, sort: 0, visible: true, status: 1,
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getMenus()
    tableData.value = res.data.data
  } finally {
    loading.value = false
  }
}

function openAdd(parentId: number) {
  isEdit.value = false; editId.value = null
  form.name = ''; form.parentId = parentId; form.path = ''; form.component = ''
  form.icon = ''; form.permissionCode = ''; form.type = 1; form.sort = 0
  form.visible = true; form.status = 1
  dialogVisible.value = true
}

function openEdit(row: MenuVO) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name; form.parentId = row.parentId; form.path = row.path || ''
  form.component = row.component || ''; form.icon = row.icon || ''
  form.permissionCode = row.permissionCode || ''; form.type = row.type
  form.sort = row.sort; form.visible = row.visible; form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const data = { ...form }
  if (isEdit.value && editId.value) {
    await updateMenu(editId.value, data)
    ElMessage.success('更新成功')
  } else {
    await addMenu(data)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(id: number) {
  await deleteMenu(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
