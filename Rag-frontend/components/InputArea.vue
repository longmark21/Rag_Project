<template>
  <div class="input-area">
    <div class="input-wrap">
      <el-input
        v-model="message"
        type="textarea"
        :rows="2"
        :disabled="isSending"
        placeholder="输入问题，Enter 发送，Shift+Enter 换行..."
        class="chat-input"
        @keyup.enter.exact="handleSend"
        @keyup.enter.shift="handleShiftEnter"
      />
      <button
        class="send-btn"
        :class="{ active: message.trim() && !isSending, loading: isSending }"
        :disabled="!message.trim() || isSending"
        @click="handleSend"
      >
        <svg v-if="!isSending" width="16" height="16" viewBox="0 0 24 24" fill="none">
          <line x1="22" y1="2" x2="11" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <polygon points="22 2 15 22 11 13 2 9 22 2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
        </svg>
        <div v-else class="btn-spinner"></div>
      </button>
    </div>

    <div class="input-footer">
      <div class="context-info" :class="{ 'has-docs': store.selectedDocumentIds.length > 0 }">
        <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          <polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
        </svg>
        <span v-if="store.selectedDocumentIds.length > 0">
          基于 <strong>{{ store.selectedDocumentIds.length }}</strong> 个文档作答
        </span>
        <span v-else>基于全部文档作答</span>
      </div>
      <div class="hint">Enter 发送</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '../store'
import { ElMessage } from 'element-plus'

const store = useChatStore()
const message = ref('')
const isSending = ref(false)

const handleSend = async () => {
  const trimmed = message.value.trim()
  if (!trimmed || isSending.value) return

  isSending.value = true
  store.addUserMessage(trimmed)
  message.value = ''
  const thinkingMsg = store.addThinkingMessage()
  isSending.value = false

  try {
    const response = await fetch('/api/v1/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        question: trimmed,
        sessionId: 'test-session',
        fileIds: store.selectedDocumentIds,
      }),
    })
    const data = await response.json()
    if (data.success) {
      store.updateAssistantMessage(thinkingMsg, data.data, data.sources)
    } else {
      store.updateAssistantMessage(thinkingMsg, '抱歉，发送消息时出现错误，请稍后重试。')
    }
  } catch (error) {
    console.error('Failed to send message:', error)
    store.updateAssistantMessage(thinkingMsg, '抱歉，发送消息时出现错误，请稍后重试。')
  }
}

const handleShiftEnter = () => {}
</script>

<style scoped>
.input-area {
  position: relative;
  z-index: 1;
  background: var(--bg-surface);
  border-top: 1px solid var(--border);
  padding: 14px 24px 16px;
}

.input-wrap {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.chat-input { flex: 1; }

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1.5px solid var(--border);
  background: var(--bg-elevated);
  color: var(--text-muted);
  cursor: not-allowed;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;
  outline: none;
  margin-bottom: 1px;
}
.send-btn.active {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px var(--accent-glow);
}
.send-btn.active:hover {
  background: var(--accent-soft);
  border-color: var(--accent-soft);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px var(--accent-glow);
}
.send-btn.active:active {
  transform: translateY(0);
}

.btn-spinner {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding: 0 2px;
}
.context-info {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--text-muted);
  transition: color 0.2s;
}
.context-info.has-docs { color: var(--accent-soft); }
.context-info strong { color: var(--accent-soft); }
.hint {
  font-size: 11px;
  color: var(--text-muted);
  opacity: 0.5;
}
</style>