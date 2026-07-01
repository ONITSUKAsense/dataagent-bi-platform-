import request from './request'
import type { ApiResponse, PageResult, PageParams } from '@/types'

export interface AgentLogVO {
  id: number
  userId: number
  username: string
  sessionId: string
  agentType: string
  llmModel: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
  costMs: number
  status: number
  errorMsg?: string
  input?: string
  output?: string
  steps?: string
  createdAt: string
}

export interface TokenStatsVO {
  date: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

export function getAgentLogs(params: PageParams & { agentType?: string; status?: number }) {
  return request.get<ApiResponse<PageResult<AgentLogVO>>>('/logs/agent', { params })
}

export function getAgentLogDetail(id: number) {
  return request.get<ApiResponse<AgentLogVO>>(`/logs/agent/${id}`)
}

export function getErrorLogs(params: PageParams) {
  return request.get<ApiResponse<PageResult<any>>>('/logs/error', { params })
}

export function getSQLLogs(params: PageParams) {
  return request.get<ApiResponse<PageResult<any>>>('/logs/sql', { params })
}

export function getTokenStats(params: { startDate?: string; endDate?: string }) {
  return request.get<ApiResponse<TokenStatsVO[]>>('/logs/token-stats', { params })
}
