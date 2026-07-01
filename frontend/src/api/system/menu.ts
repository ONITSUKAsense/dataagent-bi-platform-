import request from '@/api/request'
import type { ApiResponse } from '@/types'

export interface MenuVO {
  id: number
  name: string
  permissionCode: string
  path: string
  component: string
  icon: string
  parentId: number
  sort: number
  type: number
  visible: boolean
  status: number
  children?: MenuVO[]
}

export function getMenus() {
  return request.get<ApiResponse<MenuVO[]>>('/menus')
}

export function getUserMenus() {
  return request.get<ApiResponse<MenuVO[]>>('/menus/user')
}

export function addMenu(data: any) {
  return request.post<ApiResponse>('/menus', data)
}

export function updateMenu(id: number, data: any) {
  return request.put<ApiResponse>(`/menus/${id}`, data)
}

export function deleteMenu(id: number) {
  return request.delete<ApiResponse>(`/menus/${id}`)
}
