import request from './request'
import type { ApiResponse, PageResult, PageParams } from '@/types'
import type { ReportVO, ReportVersionVO } from '@/types'
export type { ReportVO, ReportVersionVO }

export function getReports(params: PageParams & { title?: string }) {
  return request.get<ApiResponse<PageResult<ReportVO>>>('/reports', { params })
}

export function getReportDetail(id: number) {
  return request.get<ApiResponse<ReportVO>>(`/reports/${id}`)
}

export function addReport(data: { title: string; contentMd: string; sessionId: string }) {
  return request.post<ApiResponse<ReportVO>>('/reports', data)
}

export function updateReport(id: number, data: any) {
  return request.put<ApiResponse>(`/reports/${id}`, data)
}

export function deleteReport(id: number) {
  return request.delete<ApiResponse>(`/reports/${id}`)
}

export function getReportVersions(id: number) {
  return request.get<ApiResponse<ReportVersionVO[]>>(`/reports/${id}/versions`)
}

export function createReportVersion(id: number, data: { changeNote: string }) {
  return request.post<ApiResponse>(`/reports/${id}/versions`, data)
}

export function restoreReportVersion(id: number, data: { versionId: number }) {
  return request.post<ApiResponse>(`/reports/${id}/restore`, data)
}

export function exportReportPdf(id: number) {
  return request.post(`/reports/${id}/export/pdf`, {}, { responseType: 'blob' })
}

export function exportReportExcel(id: number) {
  return request.post(`/reports/${id}/export/excel`, {}, { responseType: 'blob' })
}

export function shareReport(id: number) {
  return request.post<ApiResponse<{ url: string }>>(`/reports/${id}/share`)
}
