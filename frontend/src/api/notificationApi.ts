import { apiClient } from './client';
import { Notification, UnreadCount, PagedResponse } from '../types';

export const notificationApi = {
  getNotifications: async (unreadOnly?: boolean, page = 0, size = 20): Promise<PagedResponse<Notification>> => {
    const response = await apiClient.get<PagedResponse<Notification>>('/notifications', {
      params: { unreadOnly, page, size },
    });
    return response.data;
  },

  getUnreadCount: async (): Promise<UnreadCount> => {
    const response = await apiClient.get<UnreadCount>('/notifications/unread-count');
    return response.data;
  },

  markAsRead: async (id: number): Promise<Notification> => {
    const response = await apiClient.patch<Notification>(`/notifications/${id}/read`);
    return response.data;
  },

  markAllAsRead: async (): Promise<void> => {
    await apiClient.patch('/notifications/read-all');
  },
};

