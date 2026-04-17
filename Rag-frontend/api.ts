// API 接口定义

// 文档类型
export interface Document {
  id: number;
  fileName: string;
  originalFileName?: string;
  fileUrl: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  createdBy: string;
  createdAt: string;
  fileSize?: number;
  fileType?: string;
}

// 消息类型
export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
  sources?: {
    name: string;
    page: number;
  }[];
}

// 聊天请求
export interface ChatRequest {
  question: string;
  sessionId: string;
  fileIds?: number[];
}

// 聊天响应
export interface ChatResponse {
  text: string;
  sources: {
    name: string;
    page: number;
  }[];
}

// 上传响应
export interface UploadResponse {
  documentId?: number;
  fileName?: string;
  originalFileName?: string;
  status?: string;
  message?: string;
  success?: boolean;
  data?: Document;
}

// API 基础 URL
const API_BASE_URL = '/api/v1';

// 上传文件
export const uploadFile = async (file: File): Promise<UploadResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_BASE_URL}/documents/upload`, {
    method: 'POST',
    body: formData,
  });

  return response.json();
};

// 获取文档列表
export const getDocuments = async (): Promise<{ success: boolean; data: Document[]; message?: string }> => {
  const response = await fetch(`${API_BASE_URL}/documents`);
  return response.json();
};

// 删除文档
export const deleteDocument = async (id: number): Promise<boolean> => {
  const response = await fetch(`${API_BASE_URL}/documents/${id}`, {
    method: 'DELETE',
  });
  return response.ok;
};

// 聊天（非流式）
export const chat = async (request: ChatRequest): Promise<{ success: boolean; data: string; sources: { name: string; page: number }[] }> => {
  const response = await fetch(`${API_BASE_URL}/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return response.json();
};

// 流式聊天（保留用于未来扩展）
export const streamChat = async (
  request: ChatRequest,
  onChunk: (chunk: string) => void,
  onComplete: (sources: { name: string; page: number }[]) => void
): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.body) {
    throw new Error('No response body');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const chunk = decoder.decode(value, { stream: true });
    buffer += chunk;

    // 解析 SSE 格式：data: xxx\n\n
    const lines = buffer.split('\n');
    buffer = lines.pop() || ''; // 保留最后一个不完整的行

    for (const line of lines) {
      const trimmedLine = line.trim();
      
      // 忽略 event 行和 done 标记
      if (trimmedLine.startsWith('event:') || trimmedLine === 'data: done') {
        continue;
      }
      
      if (trimmedLine.startsWith('data: ')) {
        const data = trimmedLine.substring(6).trim(); // 移除 'data: ' 前缀并去除首尾空格
        if (data && data !== '[DONE]') {
          onChunk(data);
        }
      }
    }
  }

  // 完成时调用回调
  onComplete([]);
};

// Mock 数据，用于演示
const mockDocuments: Document[] = [
  {
    id: 1,
    fileName: '技术文档.pdf',
    fileUrl: '/uploads/技术文档.pdf',
    status: 'completed',
    createdBy: 'admin',
    createdAt: '2026-04-15T10:00:00',
    fileSize: 1024000,
    fileType: 'pdf',
  },
  {
    id: 2,
    fileName: '产品需求.docx',
    fileUrl: '/uploads/产品需求.docx',
    status: 'completed',
    createdBy: 'admin',
    createdAt: '2026-04-15T11:00:00',
    fileSize: 2048000,
    fileType: 'docx',
  },
  {
    id: 3,
    fileName: '会议记录.txt',
    fileUrl: '/uploads/会议记录.txt',
    status: 'processing',
    createdBy: 'admin',
    createdAt: '2026-04-15T12:00:00',
    fileSize: 512000,
    fileType: 'txt',
  },
];

// Mock 数据获取
export const getMockDocuments = (): Document[] => {
  return mockDocuments;
};

// Mock 流式聊天
export const mockStreamChat = async (
  request: ChatRequest,
  onChunk: (chunk: string) => void,
  onComplete: (sources: { name: string; page: number }[]) => void
): Promise<void> => {
  const mockResponse = '您好！我是基于 RAG 技术的智能助手，很高兴为您服务。请问有什么我可以帮助您的吗？';
  const mockSources = [
    { name: '技术文档.pdf', page: 5 },
    { name: '产品需求.docx', page: 3 },
  ];

  // 模拟打字机效果
  for (let i = 0; i < mockResponse.length; i++) {
    await new Promise(resolve => setTimeout(resolve, 50));
    onChunk(mockResponse[i]);
  }

  onComplete(mockSources);
};