<template>
  <div class="sidebar">
    <!-- Header -->
    <div class="sidebar-header">
      <div class="brand">
        <div class="brand-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <span class="brand-name">知识库</span>
      </div>
    </div>

    <!-- Upload -->
    <div class="section upload-section">
      <el-upload
        class="upload-area"
        :auto-upload="false"
        :on-change="handleFileChange"
        :show-file-list="false"
        accept=".pdf,.doc,.docx,.txt"
        drag
      >
        <div class="upload-inner">
          <div class="upload-icon-wrap">
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
          </div>
          <div class="upload-label">拖拽文件到此处</div>
          <div class="upload-sub">或 <em>点击选择文件</em></div>
        </div>
        <template #tip>
          <div class="upload-tip">PDF · Word · TXT</div>
        </template>
      </el-upload>
    </div>

    <!-- Divider -->
    <div class="section-label">
      <span>已上传文档</span>
      <span class="doc-count" v-if="store.documents.length">{{ store.documents.length }}</span>
    </div>

    <!-- Document List -->
    <div class="doc-list">
      <!-- Loading -->
      <div v-if="store.isLoading" class="state-center">
        <div class="spinner"></div>
        <span>加载中...</span>
      </div>

      <!-- Empty -->
      <div v-else-if="store.documents.length === 0" class="state-center empty">
        <div class="empty-icon">
          <el-icon><DocumentIcon /></el-icon>
        </div>
        <p>暂无文档</p>
        <p class="empty-sub">上传文件开始使用</p>
      </div>

      <!-- Items -->
      <div v-else class="doc-items">
        <div
          v-for="document in store.documents"
          :key="document.id"
          class="doc-item"
          :class="{ selected: store.selectedDocumentIds.includes(document.id) }"
        >
          <div class="doc-row">
            <el-checkbox
              :checked="store.selectedDocumentIds.includes(document.id)"
              @change="() => handleDocumentSelection(document.id)"
            />
            <div class="doc-info" @click="handleDocumentSelection(document.id)">
              <div class="doc-name">{{ document.originalFileName }}</div>
              <div class="doc-meta">
                {{ formatFileSize(document.fileSize) }}
                <span class="dot">·</span>
                {{ document.createdAt ? document.createdAt.split('T')[0] : '—' }}
              </div>
            </div>
            <el-dropdown trigger="click">
              <button class="more-btn">
                <el-icon><More /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleViewDocument(document)">
                    <el-icon class="mr-2"><View /></el-icon>查看
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleDeleteDocument(document.id)" danger>
                    <el-icon class="mr-2"><Delete /></el-icon>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="doc-status">
            <span class="status-dot" :class="getStatusClass(document.status)"></span>
            <span class="status-text">{{ getStatusText(document.status) }}</span>
            <span v-if="document.status === 'processing'" class="processing-pulse">向量化中</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="sidebar-footer">
      <div class="selection-info" :class="{ active: store.selectedDocumentIds.length > 0 }">
        <div class="selection-dot"></div>
        <span v-if="store.selectedDocumentIds.length > 0">
          已选 <strong>{{ store.selectedDocumentIds.length }}</strong> 个文档
        </span>
        <span v-else class="text-muted">未选择文档</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useChatStore } from '../store'
import type { Document } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, View, Delete, More, Loading, Document as DocumentIcon } from '@element-plus/icons-vue'

const store = useChatStore()
let pollInterval: number | null = null

const hasProcessingDocuments = computed(() =>
  store.documents.some(doc => {
    const s = doc.status?.toLowerCase() || ''
    return s === 'pending' || s === 'processing'
  })
)

onMounted(() => {
  store.loadDocuments()
  pollInterval = window.setInterval(() => {
    if (hasProcessingDocuments.value) store.loadDocuments()
  }, 5000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

const handleFileChange = async (file: any) => {
  const result = await store.uploadFile(file.raw)
  if (result.success) {
    ElMessage.success('文件上传成功')
    store.loadDocuments()
  } else {
    ElMessage.error(result.message || '文件上传失败')
  }
}

const handleDocumentSelection = (id: number) => {
  store.toggleDocumentSelection(id)
}

const handleViewDocument = (document: Document) => {
  ElMessage.info(`查看文档: ${document.fileName}`)
}

const handleDeleteDocument = (id: number) => {
  ElMessageBox.confirm('确定要删除这个文档吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    store.deleteDocument(id)
    ElMessage.success('文档删除成功')
  }).catch(() => {})
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'completed': return 'success'
    case 'processing': return 'warning'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待处理', processing: '处理中', completed: '已完成', failed: '失败'
  }
  return map[status] || status
}

const formatFileSize = (size?: number) => {
  if (!size) return '—'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}
</script>

<style scoped>
.sidebar {
  width: 280px;
  min-width: 280px;
  background: var(--bg-surface);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* Header */
.sidebar-header {
  padding: 20px 18px 16px;
  border-bottom: 1px solid var(--border-subtle);
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-icon {
  width: 32px;
  height: 32px;
  background: var(--accent-glow);
  border: 1px solid var(--accent);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent);
}
.brand-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

/* Upload */
.upload-section {
  padding: 14px 14px 10px;
}
.upload-inner {
  padding: 16px 0 12px;
  text-align: center;
}
.upload-icon-wrap {
  width: 40px;
  height: 40px;
  background: var(--accent-glow);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
}
.upload-icon { font-size: 20px !important; color: var(--accent) !important; }
.upload-label { font-size: 13px; color: var(--text-secondary); font-weight: 500; margin-bottom: 3px; }
.upload-sub { font-size: 12px; color: var(--text-muted); }
.upload-sub em { color: var(--accent-soft); font-style: normal; }
.upload-tip {
  font-size: 11px;
  color: var(--text-muted);
  text-align: center;
  margin-top: 6px;
  letter-spacing: 1px;
}

/* Section label */
.section-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 18px 8px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--text-muted);
}
.doc-count {
  background: var(--bg-elevated);
  color: var(--text-secondary);
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 20px;
  font-weight: 500;
  border: 1px solid var(--border);
}

/* Doc list */
.doc-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px;
}

.state-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--text-muted);
  font-size: 13px;
  gap: 8px;
}

.spinner {
  width: 20px; height: 20px;
  border: 2px solid var(--border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty-icon {
  font-size: 32px;
  color: var(--text-muted);
  margin-bottom: 4px;
  opacity: 0.5;
}
.empty-sub { font-size: 12px; color: var(--text-muted); opacity: 0.6; }

/* Doc items */
.doc-items { display: flex; flex-direction: column; gap: 6px; padding-bottom: 8px; }

.doc-item {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.doc-item:hover {
  border-color: var(--border);
  background: var(--bg-hover);
}
.doc-item.selected {
  border-color: var(--accent);
  background: var(--accent-glow);
}

.doc-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.doc-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}
.doc-name {
  font-size: 12.5px;
  color: var(--text-primary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}
.doc-meta {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 3px;
}
.dot { margin: 0 4px; }

.more-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  font-size: 14px;
  transition: all 0.15s;
  flex-shrink: 0;
}
.more-btn:hover { color: var(--text-primary); background: var(--bg-hover); }

.doc-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}
.status-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-dot.success { background: var(--success); box-shadow: 0 0 6px var(--success); }
.status-dot.warning { background: var(--warning); animation: pulse 1.5s infinite; }
.status-dot.danger  { background: var(--danger); }
.status-dot.info    { background: var(--text-muted); }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.status-text { font-size: 11px; color: var(--text-muted); }
.processing-pulse {
  font-size: 10px;
  color: var(--warning);
  background: #fbbf2415;
  padding: 1px 6px;
  border-radius: 4px;
  margin-left: 2px;
}

/* Footer */
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-subtle);
}
.selection-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-muted);
  transition: all 0.2s;
}
.selection-info.active { color: var(--accent-soft); }
.selection-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--text-muted);
  transition: all 0.2s;
}
.selection-info.active .selection-dot {
  background: var(--accent);
  box-shadow: 0 0 8px var(--accent);
}
.selection-info strong { color: var(--accent-soft); font-weight: 600; }
.text-muted { color: var(--text-muted); }

.section { /* utility */ }
</style>