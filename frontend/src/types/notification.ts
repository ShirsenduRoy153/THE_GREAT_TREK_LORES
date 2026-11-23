export type NotificationType =
  | 'JOIN_REQUEST'
  | 'JOIN_ACCEPTED'
  | 'JOIN_REJECTED'
  | 'CONNECTION_ENQUIRY'
  | 'CONNECTION_PITCH'
  | 'CONNECTION_ACCEPTED'
  | 'CONNECTION_CONFIRMED';

export interface NotificationDto {
  id: string;
  notificationType: NotificationType;
  title: string;
  body?: string;
  relatedEntityType?: string;
  relatedEntityId?: string;
  isRead: boolean;
  createdAt: string;
}
