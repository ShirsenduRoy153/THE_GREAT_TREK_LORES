export type RoomType = 'MAIN_GROUP' | 'ORGANIZER_DISCUSSION';
export type ParticipantRole = 'GROUP_ADMIN' | 'MEMBER' | 'ORGANIZER';
export type MessageType = 'TEXT' | 'IMAGE' | 'FILE';

export interface ChatRoomDto {
  id: string;
  groupId: string;
  groupName: string;
  connectionId?: string;
  roomType: RoomType;
  title: string;
  participantCount: number;
  lastMessage?: string;
  lastMessageTime?: string;
  createdAt: string;
}

export interface ChatMessageDto {
  id: string;
  chatRoomId: string;
  senderUserId: string;
  senderDisplayName: string;
  senderRole: ParticipantRole;
  messageType: MessageType;
  messageText?: string;
  mediaUrl?: string;
  mediaFileName?: string;
  createdAt: string;
}
