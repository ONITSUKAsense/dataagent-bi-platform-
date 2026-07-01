import request from './request'
import type { ApiResponse, ChatSession, ChatMessage } from '@/types'

export function askQuestion(params: { question: string; sessionId?: string }) {
  return request.post<ApiResponse<any>>('/chatbi/ask', params)
}

export function askQuestionStream(params: { question: string; sessionId?: string }) {
  return request.post<ApiResponse<any>>('/chatbi/ask/stream', params)
}

export function getSessions() {
  return request.get<ApiResponse<ChatSession[]>>('/chatbi/sessions')
}

export function deleteSession(sessionId: string) {
  return request.delete<ApiResponse>(`/chatbi/sessions/${sessionId}`)
}

export function getMessages(sessionId: string) {
  return request.get<ApiResponse<ChatMessage[]>>('/chatbi/messages', {
    params: { sessionId },
  })
}
