import request from './request'
import type { ApiResponse, DashboardStats, TrendItem, RecentReport } from '@/types'

export function getDashboardStats() {
  return request.get<ApiResponse<DashboardStats>>('/dashboard/stats')
}

export function getRecentReports() {
  return request.get<ApiResponse<RecentReport[]>>('/dashboard/recent-reports')
}

export function getAgentTrend() {
  return request.get<ApiResponse<TrendItem[]>>('/dashboard/agent-trend')
}

export function getTokenTrend() {
  return request.get<ApiResponse<TrendItem[]>>('/dashboard/token-trend')
}

export function getUserStats() {
  return request.get<ApiResponse<Record<string, any>>>('/dashboard/user-stats')
}
