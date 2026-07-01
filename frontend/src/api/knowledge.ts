import request from './request'
import type { ApiResponse, PageResult, PageParams } from '@/types'

export interface KnowledgeDocVO {
  id: number
  title: string
  docType: string
  status: number
  chunkCount: number
  createdBy: string
  createdAt: string
}

export interface ChunkVO {
  id: number
  docId: number
  chunkIndex: number
  content: string
}

export function getKnowledgeDocs(params: PageParams) {
  return request.get<ApiResponse<PageResult<KnowledgeDocVO>>>('/knowledge/docs', { params })
}

export function uploadKnowledgeDoc(formData: FormData) {
  return request.post<ApiResponse>('/knowledge/docs', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deleteKnowledgeDoc(id: number) {
  return request.delete<ApiResponse>(`/knowledge/docs/${id}`)
}

export function getKnowledgeChunks(docId: number) {
  return request.get<ApiResponse<ChunkVO[]>>(`/knowledge/docs/${docId}/chunks`)
}

export function retrieveKnowledge(query: string, topK?: number) {
  return request.post<ApiResponse<ChunkVO[]>>('/knowledge/retrieve', { query, topK })
}
