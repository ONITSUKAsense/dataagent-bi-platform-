import request from '@/api/request'
import type { ApiResponse } from '@/types'

export interface PermissionVO {
  id: number
  name: string
  code: string
  type: number
  parentId: number
  children?: PermissionVO[]
}

export function getPermissions() {
  return request.get<ApiResponse<PermissionVO[]>>('/permissions')
}

export function getAllPermissions() {
  return request.get<ApiResponse<PermissionVO[]>>('/permissions/all')
}
