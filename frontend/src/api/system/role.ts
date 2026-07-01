import request from '@/api/request'
import type { ApiResponse } from '@/types'

export interface RoleVO {
  id: number
  name: string
  code: string
  sort: number
  status: number
  remark: string
  permissionIds: number[]
  createdAt: string
}

export function getRoles() {
  return request.get<ApiResponse<RoleVO[]>>('/roles')
}

export function getRoleDetail(id: number) {
  return request.get<ApiResponse<RoleVO>>(`/roles/${id}`)
}

export function addRole(data: any) {
  return request.post<ApiResponse>('/roles', data)
}

export function updateRole(id: number, data: any) {
  return request.put<ApiResponse>(`/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete<ApiResponse>(`/roles/${id}`)
}

export function assignPermissions(id: number, permissionIds: number[]) {
  return request.put<ApiResponse>(`/roles/${id}/permissions`, { permissionIds })
}
