import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage, ChatSession } from '@/types'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<string | null>(null)
  const messages = ref<ChatMessage[]>([])
  const isLoading = ref(false)

  function setSessions(list: ChatSession[]) {
    sessions.value = list
  }

  function setCurrentSession(sessionId: string) {
    currentSessionId.value = sessionId
  }

  function setMessages(list: ChatMessage[]) {
    messages.value = list
  }

  function addMessage(msg: ChatMessage) {
    messages.value.push(msg)
  }

  function setLoading(val: boolean) {
    isLoading.value = val
  }

  return { sessions, currentSessionId, messages, isLoading, setSessions, setCurrentSession, setMessages, addMessage, setLoading }
})
