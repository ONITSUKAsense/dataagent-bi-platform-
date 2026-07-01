export interface DashboardStats {
  userCount: number
  reportCount: number
  agentCallCount: number
  tokenConsumed: number
  todayCallCount: number
}

export interface TrendItem {
  date: string
  count: number
}

export interface RecentReport {
  id: number
  title: string
  createdBy: string
  createdAt: string
  status: number
  version: number
}
