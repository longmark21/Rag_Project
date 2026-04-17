import { defineStore } from 'pinia'
import type { Document, Message } from './api'
import { getDocuments, uploadFile, deleteDocument, chat } from './api'
import { nextTick } from 'vue'

interface ChatState {
  documents: Document[];
  selectedDocumentIds: number[];
  messages: Message[];
  isLoading: boolean;
  isUploading: boolean;
}

export const useChatStore = defineStore('chat', {
  state: (): ChatState => ({
    documents: [],
    selectedDocumentIds: [],
    messages: [],
    isLoading: false,
    isUploading: false,
  }),

  getters: {
    selectedDocuments: (state) => {
      return state.documents.filter(doc => state.selectedDocumentIds.includes(doc.id));
    },
  },

  actions: {
    // 加载文档列表
    async loadDocuments() {
      this.isLoading = true;
      try {
        const response = await getDocuments();
        if (response.success) {
          this.documents = response.data || [];
        } else {
          console.error('Failed to load documents:', response.message);
        }
      } catch (error) {
        console.error('Failed to load documents:', error);
      } finally {
        this.isLoading = false;
      }
    },

    // 切换文档选择
    toggleDocumentSelection(id: number) {
      const index = this.selectedDocumentIds.indexOf(id);
      if (index > -1) {
        this.selectedDocumentIds.splice(index, 1);
      } else {
        this.selectedDocumentIds.push(id);
      }
    },

    // 添加用户消息
    addUserMessage(message: string): Message {
      const userMessage: Message = {
        id: `user-${Date.now()}`,
        role: 'user',
        content: message,
        timestamp: Date.now(),
      };
      this.messages = [...this.messages, userMessage];
      return userMessage;
    },

    // 添加 AI 消息（思考中状态）
    addThinkingMessage(): Message {
      const assistantMessage: Message = {
        id: `assistant-${Date.now()}`,
        role: 'assistant',
        content: '正在思考中...',
        timestamp: Date.now(),
        sources: [],
      };
      this.messages = [...this.messages, assistantMessage];
      return assistantMessage;
    },

    // 更新 AI 消息内容
    updateAssistantMessage(message: Message, content: string, sources?: any[]) {
      message.content = content;
      if (sources) {
        message.sources = sources;
      }
      this.messages = [...this.messages];
    },

    // 发送消息
    async sendMessage(message: string) {
      // 添加 AI 回复消息（空状态）
      const assistantMessage: Message = {
        id: `assistant-${Date.now()}`,
        role: 'assistant',
        content: '正在思考中...',
        timestamp: Date.now(),
        sources: [],
      };

      // 使用展开运算符创建新数组，强制触发响应式更新
      this.messages = [...this.messages, assistantMessage];
      
      // 强制触发响应式更新，确保界面立即显示"正在思考中..."
      await nextTick();

      // 使用非流式聊天
      try {
        const response = await chat({
          question: message,
          sessionId: 'test-session',
          fileIds: this.selectedDocumentIds,
        });
        
        if (response.success) {
          assistantMessage.content = response.data;
          assistantMessage.sources = response.sources || [];
        } else {
          assistantMessage.content = '抱歉，发送消息时出现错误，请稍后重试。';
        }
      } catch (error) {
        console.error('Failed to send message:', error);
        assistantMessage.content = '抱歉，发送消息时出现错误，请稍后重试。';
      }
    },

    // 上传文件
    async uploadFile(file: File) {
      this.isUploading = true;
      try {
        const response = await uploadFile(file);
        // 后端返回的是 documentId, fileName, status, message
        // 需要转换为前端的 Document 格式
        if (response.documentId && response.fileName) {
          const newDocument: Document = {
            id: response.documentId,
            fileName: response.fileName,
            originalFileName: response.originalFileName || response.fileName,
            fileUrl: `/uploads/${response.fileName}`,
            status: (response.status as 'pending' | 'processing' | 'completed' | 'failed') || 'processing',
            createdBy: 'admin',
            createdAt: new Date().toISOString(),
            fileSize: file.size,
            fileType: file.type.split('/')[1],
          };
          this.documents = [...this.documents, newDocument];
          return { success: true, data: newDocument, message: response.message };
        } else {
          return { success: false, message: response.message || '上传失败' };
        }
      } catch (error) {
        console.error('Failed to upload file:', error);
        return { success: false, message: '上传失败' };
      } finally {
        this.isUploading = false;
      }
    },

    // 删除文档
    async deleteDocument(id: number) {
      try {
        const success = await deleteDocument(id);
        if (success) {
          this.documents = this.documents.filter(doc => doc.id !== id);
          this.selectedDocumentIds = this.selectedDocumentIds.filter(selectedId => selectedId !== id);
        }
      } catch (error) {
        console.error('Failed to delete document:', error);
      }
    },
  },
});
