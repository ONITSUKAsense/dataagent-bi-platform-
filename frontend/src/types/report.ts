export interface ReportVO {
  id: number
  title: string
  description: string
  contentMd: string
  contentHtml: string
  chartConfig: Record<string, any>
  sessionId: string
  status: number
  version: number
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface ReportVersionVO {
  id: number
  reportId: number
  version: number
  contentMd: string
  changeNote: string
  createdBy: string
  createdAt: string
}
