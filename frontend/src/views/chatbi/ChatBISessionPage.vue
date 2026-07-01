<template>
  <div class="session-page">
    <el-button text @click="router.back()">← 返回</el-button>
    <div class="chat-container">
      <div class="chat-messages">
        <div v-for="msg in chatStore.messages" :key="msg.id" :class="['message', msg.role]">
          <div class="message-content">{{ msg.content }}</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input v-model="question" placeholder="继续提问..." @keydown.enter.native.prevent="handleAsk" />
        <el-button type="primary" @click="handleAsk">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()
const question = ref('')

onMounted(() => {
  const sessionId = route.params.sessionId as string
  chatStore.setCurrentSession(sessionId)
})

async function handleAsk() {
  // Will be implemented in S5
}
</script>

<style scoped>
.session-page { height: 100%; display: flex; flex-direction: column; }
.chat-container { flex: 1; display: flex; flex-direction: column; background: #fff; border-radius: 8px; margin-top: 12px; }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px; }
.chat-input { display: flex; gap: 8px; padding: 16px 20px; border-top: 1px solid #f0f0f0; }
.chat-input .el-input { flex: 1; }
</style>
