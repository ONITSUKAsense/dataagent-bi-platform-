import request from '@/api/request'
import type { ApiResponse, PageResult, PageParams } from '@/types'

export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  roleId: number
  roleName: string
  status: number
  createdAt: string
}

export function getUsers(params: PageParams & { username?: string; status?: number }) {
  return request.get<ApiResponse<PageResult<UserVO>>>('/users', { params })
}

export function getUserDetail(id: number) {
  return request.get<ApiResponse<UserVO>>(`/users/${id}`)
}

export function addUser(data: any) {
  return request.post<ApiResponse>('/users', data)
}

export function updateUser(id: number, data: any) {
  return request.put<ApiResponse>(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete<ApiResponse>(`/users/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return request.put<ApiResponse>(`/users/${id}/status`, { status })
}
