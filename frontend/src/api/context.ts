import request from './request'
import type { ApiResponse } from '@/types'

export interface ContextVO {
  id: number
  sessionId: string
  contextKey: string
  contextValue: string
  expireAt: string | null
  createdAt: string
  updatedAt: string
}

export function getSessionContext(sessionId: string) {
  return request.get<ApiResponse<ContextVO[]>>(`/context/session/${sessionId}`)
}

export function getMyContext() {
  return request.get<ApiResponse<ContextVO[]>>('/context/my')
}

export function addContext(data: {
  sessionId: string
  contextKey: string
  contextValue: string
  expireAt?: string
}) {
  return request.post<ApiResponse>('/context', data)
}

export function updateContext(id: number, data: { contextValue: string }) {
  return request.put<ApiResponse>(`/context/${id}`, data)
}

export function setContextExpire(id: number, data: { expireAt: string }) {
  return request.put<ApiResponse>(`/context/${id}/expire`, data)
}

export function clearSessionContext(sessionId: string) {
  return request.delete<ApiResponse>(`/context/session/${sessionId}`)
}

export function deleteContext(id: number) {
  return request.delete<ApiResponse>(`/context/${id}`)
}

export function cleanExpiredContext() {
  return request.post<ApiResponse>('/context/clean-expired')
}
