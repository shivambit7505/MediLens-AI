import { apiClient } from './client';
import { ConversationSummary, ConversationDetail, ChatMessage } from '../types';

export const chatApi = {
  getConversations: async (): Promise<ConversationSummary[]> => {
    const response = await apiClient.get<ConversationSummary[]>('/chat/conversations');
    return response.data;
  },

  getConversation: async (id: string): Promise<ConversationDetail> => {
    const response = await apiClient.get<ConversationDetail>(`/chat/conversations/${id}`);
    return response.data;
  },

  createConversation: async (data: { reportId?: string; title?: string; initialMessage?: string }): Promise<ConversationDetail> => {
    const response = await apiClient.post<ConversationDetail>('/chat/conversations', data);
    return response.data;
  },

  sendMessage: async (conversationId: string, message: string): Promise<ChatMessage> => {
    const response = await apiClient.post<ChatMessage>(`/chat/conversations/${conversationId}/messages`, {
      message,
    });
    return response.data;
  },

  deleteConversation: async (id: string): Promise<void> => {
    await apiClient.delete(`/chat/conversations/${id}`);
  },
};
