import request from './request'
import type { ApiResponse, PageResult, PageParams } from '@/types'

export interface PromptTemplateVO {
  id: number
  name: string
  code: string
  description: string
  content: string
  variables: PromptVariable[]
  version: number
  status: number
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface PromptVariable {
  name: string
  type: string
  defaultValue?: string
  required: boolean
  description?: string
}

export function getPrompts(params: PageParams) {
  return request.get<ApiResponse<PageResult<PromptTemplateVO>>>('/prompts', { params })
}

export function getPromptDetail(id: number) {
  return request.get<ApiResponse<PromptTemplateVO>>(`/prompts/${id}`)
}

export function addPrompt(data: any) {
  return request.post<ApiResponse>('/prompts', data)
}

export function updatePrompt(id: number, data: any) {
  return request.put<ApiResponse>(`/prompts/${id}`, data)
}

export function deletePrompt(id: number) {
  return request.delete<ApiResponse>(`/prompts/${id}`)
}

export function testPrompt(id: number, variables: Record<string, string>) {
  return request.post<ApiResponse<string>>(`/prompts/${id}/test`, variables)
}

export function publishPrompt(id: number) {
  return request.post<ApiResponse>(`/prompts/${id}/publish`)
}
