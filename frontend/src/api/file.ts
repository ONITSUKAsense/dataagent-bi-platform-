import request from './request'
import type { ApiResponse, PageResult, PageParams } from '@/types'

export interface FileVO {
  id: number
  originalName: string
  size: number
  type: string
  ext: string
  url: string
  createdAt: string
}

export function uploadFile(formData: FormData) {
  return request.post<ApiResponse<FileVO>>('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getFiles(params: PageParams & { type?: string }) {
  return request.get<ApiResponse<PageResult<FileVO>>>('/files', { params })
}

export function deleteFile(id: number) {
  return request.delete<ApiResponse>(`/files/${id}`)
}

