export interface Notification {
  id: number;
  message: string;
  type: string;
  referenceId?: string;
  read: boolean;
  createdAt: string;
}

export interface UnreadCount {
  unreadCount: number;
}

