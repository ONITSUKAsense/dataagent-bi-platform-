export interface ChatMessage {
  id: number
  sessionId: string
  role: 'user' | 'assistant' | 'system'
  content: string
  messageType: 'text' | 'chart' | 'report'
  metadata?: ChartMetadata | null
  createdAt: string
}

export interface ChartMetadata {
  chartType: string
  option: Record<string, any>
  data: Record<string, any>[]
}

export interface ChatSession {
  sessionId: string
  title: string
  messageCount: number
  createdAt: string
  updatedAt: string
}

export interface AskParams {
  question: string
  sessionId?: string
}
