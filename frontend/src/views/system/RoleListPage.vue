<template>
  <div class="role-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>角色管理</span>
          <el-button type="primary" @click="openAdd">新增角色</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="success" @click="openAssign(row)">分配权限</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.code" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- Permission Assignment Dialog -->
    <el-dialog v-model="assignVisible" title="分配权限" width="500px">
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { getRoles, addRole, updateRole, deleteRole, assignPermissions, type RoleVO } from '@/api/system/role'
import { getAllPermissions, type PermissionVO } from '@/api/system/permission'

const loading = ref(false)
const tableData = ref<RoleVO[]>([])
const dialogVisible = ref(false)
const assignVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const assignRoleId = ref<number | null>(null)
const permissionTree = ref<PermissionVO[]>([])
const treeRef = ref<InstanceType<typeof ElTree>>()

const form = reactive({
  name: '', code: '', sort: 0, status: 1, remark: '',
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoles()
    tableData.value = res.data.data
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false; editId.value = null
  form.name = ''; form.code = ''; form.sort = 0; form.status = 1; form.remark = ''
  dialogVisible.value = true
}

function openEdit(row: RoleVO) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name; form.code = row.code; form.sort = row.sort; form.status = row.status; form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (isEdit.value && editId.value) {
    await updateRole(editId.value, form)
    ElMessage.success('更新成功')
  } else {
    await addRole(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(id: number) {
  await deleteRole(id)
  ElMessage.success('删除成功')
  fetchData()
}

async function openAssign(row: RoleVO) {
  assignRoleId.value = row.id
  assignVisible.value = true
  const res = await getAllPermissions()
  permissionTree.value = res.data.data
  await nextTick()
  if (treeRef.value && row.permissionIds) {
    treeRef.value.setCheckedKeys(row.permissionIds)
  }
}

async function handleAssign() {
  if (!assignRoleId.value) return
  const keys = treeRef.value?.getCheckedKeys(false) as number[]
  await assignPermissions(assignRoleId.value, keys)
  ElMessage.success('权限分配成功')
  assignVisible.value = false
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
