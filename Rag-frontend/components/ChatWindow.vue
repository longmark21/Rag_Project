<template>
  <div class="chat-window">
    <!-- Header -->
    <div class="chat-header">
      <div class="header-left">
        <div class="ai-avatar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M8 12h8M12 8v8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div>
          <div class="header-title">智能助手</div>
          <div class="header-sub">RAG 知识库问答系统</div>
        </div>
      </div>
      <div class="header-status">
        <span class="status-indicator"></span>
        <span>在线</span>
      </div>
    </div>

    <!-- Messages -->
    <div ref="messagesContainer" class="messages-wrap">
      <!-- Timestamp -->
      <div class="time-stamp">
        <span>今天 {{ new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }}</span>
      </div>

      <!-- Empty state -->
      <div v-if="store.messages.length === 0" class="empty-state">
        <div class="empty-orb">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
          </svg>
        </div>
        <h3>开始对话</h3>
        <p>选择文档后提问，我将基于知识库内容为您解答</p>
      </div>

      <!-- Message list -->
      <div class="messages">
        <div
          v-for="message in store.messages"
          :key="message.id"
          class="msg-row"
          :class="message.role === 'user' ? 'msg-user' : 'msg-ai'"
        >
          <!-- AI avatar -->
          <div v-if="message.role === 'assistant'" class="msg-avatar ai-av">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M8 12h8M12 8v8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>

          <div class="bubble-wrap">
            <div class="bubble" :class="message.role === 'user' ? 'bubble-user' : 'bubble-ai'">
              <!-- Thinking animation -->
              <div v-if="message.content === '正在思考中...'" class="thinking">
                <div class="thinking-dots">
                  <span></span><span></span><span></span>
                </div>
                <span class="thinking-text">思考中</span>
              </div>

              <!-- User message -->
              <div v-else-if="message.role === 'user'" class="user-text">{{ message.content }}</div>

              <!-- AI message -->
              <div v-else class="markdown-body" v-html="renderMarkdown(message.content)"></div>
            </div>

            <!-- Sources -->
            <div v-if="message.role === 'assistant' && message.sources && message.sources.length > 0" class="sources">
              <div class="sources-label">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                  <polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                </svg>
                参考来源
              </div>
              <div class="source-tags">
                <span
                  v-for="(source, i) in message.sources"
                  :key="i"
                  class="source-tag"
                  @click="handleSourceClick(source)"
                >
                  {{ source.name }}
                  <span class="source-page">P{{ source.page }}</span>
                </span>
              </div>
            </div>
          </div>

          <!-- User avatar -->
          <div v-if="message.role === 'user'" class="msg-avatar user-av">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Input -->
    <InputArea />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useChatStore } from '../store'
import InputArea from './InputArea.vue'
import MarkdownIt from 'markdown-it'
import { ElMessage } from 'element-plus'

const store = useChatStore()
const messagesContainer = ref<HTMLElement>()
const md = new MarkdownIt({
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="language-' + lang + '"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>'
      } catch (_) {}
    }
    return '<pre class="language-text"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

watch(() => store.messages.length, () => scrollToBottom())

const scrollToBottom = () => {
  setTimeout(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  }, 100)
}

const renderMarkdown = (content: string) => md.render(content)

const handleSourceClick = (source: { name: string; page: number }) => {
  ElMessage.info(`查看文档：${source.name} 第${source.page}页`)
}

onMounted(() => scrollToBottom())
</script>

<style scoped>
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-base);
  height: 100%;
  overflow: hidden;
  position: relative;
}

/* Background texture */
.chat-window::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 60% 40% at 70% 20%, #7c6af708 0%, transparent 60%),
    radial-gradient(ellipse 40% 30% at 20% 80%, #7c6af705 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* Header */
.chat-header {
  position: relative;
  z-index: 1;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border);
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.ai-avatar {
  width: 36px; height: 36px;
  background: var(--accent-glow);
  border: 1px solid var(--accent);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent);
  flex-shrink: 0;
}
.header-title { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.header-sub { font-size: 11.5px; color: var(--text-muted); margin-top: 2px; }
.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--success);
}
.status-indicator {
  width: 7px; height: 7px;
  background: var(--success);
  border-radius: 50%;
  box-shadow: 0 0 8px var(--success);
  animation: pulse-green 2s infinite;
}
@keyframes pulse-green {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* Messages area */
.messages-wrap {
  position: relative;
  z-index: 1;
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.time-stamp {
  text-align: center;
  margin-bottom: 24px;
}
.time-stamp span {
  font-size: 11px;
  color: var(--text-muted);
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  padding: 4px 12px;
  border-radius: 20px;
}

/* Empty state */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 60px 20px;
  gap: 12px;
}
.empty-orb {
  width: 64px; height: 64px;
  background: var(--accent-glow);
  border: 1px solid var(--accent);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent);
  margin-bottom: 4px;
}
.empty-state h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}
.empty-state p {
  font-size: 13px;
  color: var(--text-muted);
  max-width: 260px;
  line-height: 1.6;
}

/* Message rows */
.messages { display: flex; flex-direction: column; gap: 20px; }

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  animation: fadeUp 0.25s ease;
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}
.msg-user { flex-direction: row-reverse; }

.msg-avatar {
  width: 30px; height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.ai-av { background: var(--accent-glow); border: 1px solid var(--accent); color: var(--accent); }
.user-av { background: var(--bg-elevated); border: 1px solid var(--border); color: var(--text-secondary); }

.bubble-wrap { display: flex; flex-direction: column; gap: 6px; max-width: 68%; }
.msg-user .bubble-wrap { align-items: flex-end; }

.bubble {
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.65;
  position: relative;
}
.bubble-user {
  background: var(--user-bubble);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.bubble-ai {
  background: var(--ai-bubble);
  border: 1px solid var(--border);
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
}
.user-text { white-space: pre-wrap; }

/* Thinking */
.thinking {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}
.thinking-dots { display: flex; gap: 5px; }
.thinking-dots span {
  width: 7px; height: 7px;
  background: var(--text-muted);
  border-radius: 50%;
  animation: bounce 1.4s infinite;
}
.thinking-dots span:nth-child(2) { animation-delay: 0.2s; }
.thinking-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-7px); opacity: 1; }
}
.thinking-text { font-size: 13px; color: var(--text-muted); }

/* Sources */
.sources { display: flex; flex-direction: column; gap: 6px; }
.sources-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}
.source-tags { display: flex; flex-wrap: wrap; gap: 5px; }
.source-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--accent-soft);
  background: var(--accent-glow);
  border: 1px solid var(--accent);
  padding: 3px 9px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.15s;
}
.source-tag:hover { background: #7c6af730; }
.source-page {
  font-size: 10px;
  color: var(--text-muted);
  background: var(--bg-elevated);
  padding: 0 5px;
  border-radius: 4px;
}
</style>