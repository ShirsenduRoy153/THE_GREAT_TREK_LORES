export type ConnectionInitiator = 'GROUP' | 'ORGANIZER';
export type ConnectionStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CONFIRMED' | 'CLOSED';

export interface ConnectionDto {
  id: string;
  groupId: string;
  groupName: string;
  groupDestination: string;
  groupStartDate?: string;
  groupEndDate?: string;
  organizerId: string;
  organizerName: string;
  organizerLocation?: string;
  initiatedBy: ConnectionInitiator;
  message?: string;
  status: ConnectionStatus;
  createdAt: string;
}
