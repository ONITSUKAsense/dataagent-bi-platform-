<template>
  <div class="chatbi-layout">
    <!-- Session sidebar -->
    <div class="session-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <el-button type="primary" @click="newSession" :icon="Plus" style="width: 100%">
          新建会话
        </el-button>
      </div>
      <div class="session-list">
        <div
          v-for="s in chatStore.sessions"
          :key="s.sessionId"
          :class="['session-item', { active: s.sessionId === chatStore.currentSessionId }]"
          @click="switchSession(s.sessionId)"
        >
          <div class="session-title">{{ s.title }}</div>
          <div class="session-meta">{{ s.messageCount }}条 · {{ formatDate(s.updatedAt) }}</div>
          <el-button
            text
            type="danger"
            size="small"
            class="delete-btn"
            @click.stop="handleDelete(s.sessionId)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <div v-if="chatStore.sessions.length === 0" class="empty-sessions">
          暂无对话记录
        </div>
      </div>
    </div>

    <!-- Main chat area -->
    <div class="chat-main">
      <div class="messages-container" ref="messagesRef">
        <div v-if="chatStore.messages.length === 0" class="welcome">
          <div class="welcome-icon">
            <el-icon :size="56" color="#409eff"><ChatDotSquare /></el-icon>
          </div>
          <h2>DataAgent 智能分析助手</h2>
          <p>输入自然语言，AI 自动分析数据并生成 BI 报告</p>
          <div class="suggestion-tags">
            <el-tag
              v-for="s in suggestions"
              :key="s"
              @click="quickAsk(s)"
              class="suggestion-tag"
              effect="plain"
            >
              {{ s }}
            </el-tag>
          </div>
        </div>

        <div v-for="msg in chatStore.messages" :key="msg.id" class="message-group">
          <!-- User message -->
          <div v-if="msg.role === 'user'" class="message user">
            <div class="message-bubble user-bubble">
              {{ msg.content }}
            </div>
          </div>

          <!-- Assistant message -->
          <div v-if="msg.role === 'assistant'" class="message assistant">
            <div class="avatar assistant-avatar">
              <el-icon :size="20" color="#409eff"><Cpu /></el-icon>
            </div>
            <div class="message-body">
              <!-- Chart -->
              <div v-if="chartOptions[msg.id]" class="chart-wrapper">
                <ChartRenderer :option="chartOptions[msg.id].option" :height="320" />
              </div>
              <!-- Markdown report -->
              <div class="message-bubble assistant-bubble" v-if="msg.content">
                <MarkdownRenderer :content="msg.content" />
              </div>
              <!-- Action buttons -->
              <div class="message-actions">
                <el-button text size="small" @click="copyContent(msg.content)">
                  <el-icon><CopyDocument /></el-icon> 复制
                </el-button>
                <el-button text size="small" @click="saveReport(msg)">
                  <el-icon><Document /></el-icon> 保存报告
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="chatStore.isLoading" class="message assistant">
          <div class="avatar assistant-avatar">
            <el-icon :size="20" color="#409eff"><Cpu /></el-icon>
          </div>
          <div class="message-body">
            <div class="message-bubble assistant-bubble loading">
              <span class="dot-pulse"></span>
              <span style="margin-left: 8px; color: #909399">AI 正在分析...</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Input area -->
      <div class="input-area">
        <el-input
          v-model="question"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题，如：近30天销售额"
          @keydown.enter.native.prevent="handleAsk"
          :disabled="chatStore.isLoading"
        />
        <div class="input-actions">
          <span class="input-tip">Enter 发送，Shift+Enter 换行</span>
          <el-button type="primary" :loading="chatStore.isLoading" @click="handleAsk" :icon="Promotion">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, CopyDocument, Document, Promotion, ChatDotSquare, Cpu } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat'
import { askQuestion, getSessions, getMessages, deleteSession } from '@/api/chatbi'
import { addReport } from '@/api/report'
import ChartRenderer from '@/components/ChartRenderer.vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import { formatDate } from '@/utils/format'

const chatStore = useChatStore()
const question = ref('')
const messagesRef = ref<HTMLElement>()
const sidebarCollapsed = ref(false)
const chartOptions = reactive<Record<number, { option: Record<string, any>; chartType: string }>>({})

const suggestions = [
  '近30天销售额是多少？',
  '本月用户增长趋势',
  '各品类销售占比分析',
  '上个月订单数据统计',
]

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function loadSessions() {
  try {
    const res = await getSessions()
    chatStore.setSessions(res.data.data)
  } catch {
    // Silently handle
  }
}

async function loadMessages(sessionId: string) {
  try {
    const res = await getMessages(sessionId)
    chatStore.setMessages(res.data.data)
    // Parse chart options
    for (const msg of res.data.data) {
      if (msg.metadata && typeof msg.metadata === 'object') {
        const meta = msg.metadata as any
        if (meta.option) {
          chartOptions[msg.id] = {
            option: meta.option,
            chartType: meta.chartType || 'bar',
          }
        }
      }
    }
    scrollToBottom()
  } catch {
    // Silently handle
  }
}

function newSession() {
  chatStore.setCurrentSession(null as any)
  chatStore.setMessages([])
  question.value = ''
}

function switchSession(sessionId: string) {
  chatStore.setCurrentSession(sessionId)
  loadMessages(sessionId)
}

async function handleDelete(sessionId: string) {
  try {
    await ElMessageBox.confirm('确认删除此会话？', '提示', { type: 'warning' })
    await deleteSession(sessionId)
    ElMessage.success('已删除')
    if (chatStore.currentSessionId === sessionId) {
      newSession()
    }
    loadSessions()
  } catch {
    // Cancelled
  }
}

async function quickAsk(text: string) {
  question.value = text
  await handleAsk()
}

async function handleAsk() {
  if (!question.value.trim() || chatStore.isLoading) return

  const q = question.value
  question.value = ''
  chatStore.setLoading(true)

  try {
    // Optimistically add user message
    const tempUserMsg = {
      id: Date.now(),
      sessionId: chatStore.currentSessionId || '',
      role: 'user' as const,
      content: q,
      messageType: 'text',
      createdAt: new Date().toISOString(),
    }
    chatStore.addMessage(tempUserMsg as any)
    scrollToBottom()

    const res = await askQuestion({
      question: q,
      sessionId: chatStore.currentSessionId || undefined,
    })

    const data = res.data.data
    const sessionId = data.sessionId

    // Add assistant message
    const msgId = Date.now() + 1
    chatStore.addMessage({
      id: msgId,
      sessionId,
      role: 'assistant',
      content: data.answer || '',
      messageType: data.chartOption ? 'report' : 'text',
      createdAt: new Date().toISOString(),
    } as any)

    if (data.chartOption) {
      chartOptions[msgId] = {
        option: data.chartOption,
        chartType: data.chartType || 'bar',
      }
    }

    chatStore.setCurrentSession(sessionId)
    scrollToBottom()
    loadSessions()
  } catch (e: any) {
    ElMessage.error(e?.message || '请求失败，请重试')
  } finally {
    chatStore.setLoading(false)
  }
}

async function copyContent(content: string) {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败')
  }
}

async function saveReport(msg: any) {
  try {
    await addReport({
      title: `分析报告 ${formatDate(msg.createdAt)}`,
      contentMd: msg.content,
      sessionId: msg.sessionId,
    })
    ElMessage.success('报告已保存')
  } catch {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.chatbi-layout {
  display: flex;
  height: calc(100vh - 140px);
  gap: 16px;
}

/* Session sidebar */
.session-sidebar {
  width: 260px;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  transition: width 0.3s;
}
.session-sidebar.collapsed { width: 0; border: none; }
.sidebar-header { padding: 12px; border-bottom: 1px solid #f0f0f0; }
.session-list { flex: 1; overflow-y: auto; padding: 8px; }
.session-item {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  margin-bottom: 4px;
  transition: background 0.2s;
}
.session-item:hover { background: #f5f7fa; }
.session-item.active { background: #ecf5ff; }
.session-title { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-meta { font-size: 12px; color: #909399; margin-top: 2px; }
.delete-btn { position: absolute; right: 4px; top: 50%; transform: translateY(-50%); display: none; }
.session-item:hover .delete-btn { display: block; }
.empty-sessions { text-align: center; color: #909399; padding: 40px 0; font-size: 13px; }

/* Chat main */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* Welcome */
.welcome {
  text-align: center;
  padding: 80px 20px;
}
.welcome-icon { margin-bottom: 16px; }
.welcome h2 { font-size: 24px; color: #303133; margin: 0 0 8px; }
.welcome p { color: #909399; margin: 0 0 24px; }
.suggestion-tags { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.suggestion-tag { cursor: pointer; font-size: 13px; padding: 6px 12px; }

/* Messages */
.message-group { margin-bottom: 20px; }
.message { display: flex; margin-bottom: 16px; }
.message.user { justify-content: flex-end; }
.message.assistant { align-items: flex-start; }

.user-bubble {
  background: #409eff;
  color: #fff;
  padding: 12px 18px;
  border-radius: 18px 18px 4px 18px;
  max-width: 70%;
  line-height: 1.5;
  word-break: break-word;
}

.assistant-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 12px;
}

.message-body { flex: 1; min-width: 0; }
.assistant-bubble {
  background: #f5f7fa;
  padding: 16px 20px;
  border-radius: 4px 18px 18px 18px;
  line-height: 1.6;
}

.chart-wrapper {
  margin-bottom: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.message-actions {
  margin-top: 8px;
  display: flex;
  gap: 4px;
  padding-left: 4px;
}

.loading { display: flex; align-items: center; }
.dot-pulse {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: pulse 1.2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

/* Input area */
.input-area {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}
.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.input-tip { font-size: 12px; color: #c0c4cc; }
</style>
