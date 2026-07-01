import request from './request'
import type { ApiResponse, PageResult, PageParams } from '@/types'

export interface SQLHistoryVO {
  id: number
  originalQuestion: string
  generatedSql: string
  rowCount: number
  costMs: number
  status: number
  isFavorite: boolean
  tokenCost: number
  createdAt: string
}

export function getSQLHistory(params: PageParams & { question?: string }) {
  return request.get<ApiResponse<PageResult<SQLHistoryVO>>>('/sql/history', { params })
}

export function getSQLDetail(id: number) {
  return request.get<ApiResponse<SQLHistoryVO>>(`/sql/history/${id}`)
}

export function toggleFavorite(id: number) {
  return request.put<ApiResponse>(`/sql/history/${id}/favorite`)
}

export function getSQLStats() {
  return request.get<ApiResponse<Record<string, any>>>('/sql/stats')
}
