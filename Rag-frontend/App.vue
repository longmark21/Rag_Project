<template>
  <div class="app flex h-screen overflow-hidden">
    <Sidebar />
    <ChatWindow />
  </div>
</template>

<script setup lang="ts">
import Sidebar from './components/Sidebar.vue'
import ChatWindow from './components/ChatWindow.vue'
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;600&family=Space+Grotesk:wght@400;500;600&display=swap');

:root {
  --bg-base: #0d0f14;
  --bg-surface: #13161e;
  --bg-elevated: #1a1e28;
  --bg-hover: #20263380;
  --border: #ffffff0f;
  --border-subtle: #ffffff08;
  --accent: #7c6af7;
  --accent-glow: #7c6af730;
  --accent-soft: #a89bf8;
  --text-primary: #eeedf2;
  --text-secondary: #8b8fa8;
  --text-muted: #50546a;
  --success: #34d399;
  --warning: #fbbf24;
  --danger: #f87171;
  --user-bubble: #7c6af7;
  --ai-bubble: #1a1e28;
  --radius: 12px;
  --radius-sm: 8px;
  --shadow: 0 4px 24px #00000040;
  --shadow-lg: 0 8px 48px #00000060;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  background: var(--bg-base);
  color: var(--text-primary);
  font-family: 'Noto Sans SC', 'Space Grotesk', sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* Scrollbar */
::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: var(--border); border-radius: 4px; }
::-webkit-scrollbar-thumb:hover { background: var(--text-muted); }

.app {
  font-family: 'Noto Sans SC', 'Space Grotesk', sans-serif;
  background: var(--bg-base);
}

/* === Element Plus Overrides === */
.el-upload-dragger {
  background: var(--bg-elevated) !important;
  border: 1.5px dashed var(--border) !important;
  border-radius: var(--radius) !important;
  transition: all 0.2s ease !important;
}
.el-upload-dragger:hover {
  border-color: var(--accent) !important;
  background: var(--accent-glow) !important;
}
.el-upload-dragger .el-icon--upload { color: var(--accent) !important; font-size: 32px !important; }
.el-upload__text { color: var(--text-secondary) !important; font-size: 13px !important; }
.el-upload__text em { color: var(--accent-soft) !important; }
.el-upload__tip { color: var(--text-muted) !important; }

.el-checkbox__inner {
  background: var(--bg-elevated) !important;
  border-color: var(--border) !important;
  border-radius: 4px !important;
}
.el-checkbox__input.is-checked .el-checkbox__inner {
  background: var(--accent) !important;
  border-color: var(--accent) !important;
}

.el-tag {
  border-radius: 6px !important;
  font-size: 11px !important;
  font-weight: 500 !important;
  border: none !important;
  padding: 2px 8px !important;
}
.el-tag--success { background: #34d39920 !important; color: var(--success) !important; }
.el-tag--warning { background: #fbbf2420 !important; color: var(--warning) !important; }
.el-tag--danger  { background: #f8717120 !important; color: var(--danger) !important; }
.el-tag--info    { background: var(--bg-elevated) !important; color: var(--text-secondary) !important; }

.el-button {
  border-radius: var(--radius-sm) !important;
  font-family: inherit !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
}
.el-button--primary {
  background: var(--accent) !important;
  border-color: var(--accent) !important;
  color: #fff !important;
}
.el-button--primary:hover {
  background: var(--accent-soft) !important;
  border-color: var(--accent-soft) !important;
  box-shadow: 0 0 20px var(--accent-glow) !important;
}
.el-button--primary:disabled,
.el-button--primary.is-disabled {
  background: var(--bg-elevated) !important;
  border-color: var(--border) !important;
  color: var(--text-muted) !important;
  box-shadow: none !important;
}
.el-button:not(.el-button--primary) {
  background: var(--bg-elevated) !important;
  border-color: var(--border) !important;
  color: var(--text-secondary) !important;
}
.el-button:not(.el-button--primary):hover {
  background: var(--bg-hover) !important;
  color: var(--text-primary) !important;
}

.el-dropdown__popper,
.el-dropdown-menu {
  background: var(--bg-elevated) !important;
  border: 1px solid var(--border) !important;
  border-radius: var(--radius-sm) !important;
  box-shadow: var(--shadow-lg) !important;
  padding: 4px !important;
}
.el-dropdown-menu__item {
  color: var(--text-secondary) !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  padding: 8px 12px !important;
  transition: all 0.15s !important;
}
.el-dropdown-menu__item:hover {
  background: var(--bg-hover) !important;
  color: var(--text-primary) !important;
}
.el-dropdown-menu__item.is-danger:hover {
  background: #f8717115 !important;
  color: var(--danger) !important;
}

.el-textarea__inner {
  background: var(--bg-elevated) !important;
  border: 1.5px solid var(--border) !important;
  border-radius: var(--radius) !important;
  color: var(--text-primary) !important;
  font-family: inherit !important;
  font-size: 14px !important;
  resize: none !important;
  transition: border-color 0.2s ease !important;
  padding: 12px 14px !important;
  line-height: 1.6 !important;
}
.el-textarea__inner::placeholder { color: var(--text-muted) !important; }
.el-textarea__inner:focus {
  border-color: var(--accent) !important;
  box-shadow: 0 0 0 3px var(--accent-glow) !important;
  outline: none !important;
}

.el-message-box {
  background: var(--bg-elevated) !important;
  border: 1px solid var(--border) !important;
  border-radius: var(--radius) !important;
}
.el-message-box__title { color: var(--text-primary) !important; }
.el-message-box__content { color: var(--text-secondary) !important; }

/* Markdown content styles */
.markdown-body { color: var(--text-primary); line-height: 1.75; font-size: 14px; }
.markdown-body p { margin-bottom: 0.75em; }
.markdown-body p:last-child { margin-bottom: 0; }
.markdown-body code {
  background: var(--bg-base);
  color: var(--accent-soft);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.875em;
  font-family: 'Fira Code', 'Cascadia Code', monospace;
}
.markdown-body pre {
  background: var(--bg-base);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 14px 16px;
  overflow-x: auto;
  margin: 10px 0;
}
.markdown-body pre code {
  background: none;
  padding: 0;
  color: var(--text-primary);
  font-size: 13px;
}
.markdown-body ul, .markdown-body ol { padding-left: 1.5em; margin-bottom: 0.75em; }
.markdown-body li { margin-bottom: 0.25em; }
.markdown-body strong { color: var(--text-primary); font-weight: 600; }
.markdown-body h1,.markdown-body h2,.markdown-body h3 {
  color: var(--text-primary);
  font-weight: 600;
  margin: 1em 0 0.5em;
}
.markdown-body blockquote {
  border-left: 3px solid var(--accent);
  padding-left: 12px;
  color: var(--text-secondary);
  margin: 0.75em 0;
}
</style>