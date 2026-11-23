export type GroupStatus = 'OPEN' | 'PLANNING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
export type MembershipRole = 'ADMIN' | 'MEMBER';
export type MembershipStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'LEFT' | 'REMOVED';

export type TrekDifficulty = 'EASY' | 'MODERATE' | 'DIFFICULT' | 'STRENUOUS';
export type DepartureStatus = 'OPEN' | 'FULL' | 'CANCELLED' | 'COMPLETED';

export interface TripGroupSummary {
  id: string;
  name: string;
  destination: string;
  description?: string;
  preferredStartDate?: string;
  preferredEndDate?: string;
  datesFlexible: boolean;
  maxMembers: number;
  currentMemberCount: number;
  status: GroupStatus;
  createdByUserId: string;
  createdByName: string;
  createdAt: string;
}

export interface GroupMembership {
  membershipId: string;
  userId: string;
  displayName: string;
  profilePhotoUrl?: string;
  experienceLevel: string;
  role: MembershipRole;
  status: MembershipStatus;
  joinedAt?: string;
}

export interface TripGroupDetail {
  id: string;
  name: string;
  destination: string;
  description?: string;
  preferredStartDate?: string;
  preferredEndDate?: string;
  datesFlexible: boolean;
  maxMembers: number;
  status: GroupStatus;
  createdByUserId: string;
  createdByName: string;
  isCurrentUserAdmin: boolean;
  currentUserMembershipStatus?: MembershipStatus;
  members: GroupMembership[];
  createdAt: string;
}

export interface TrekListingSummary {
  id: string;
  organizerId: string;
  organizerName: string;
  title: string;
  destination: string;
  description?: string;
  difficulty: TrekDifficulty;
  durationDays: number;
  startingPrice: number;
  coverImageUrl?: string;
  listingStatus: string;
  upcomingDepartureCount: number;
}

export interface TrekDeparture {
  id: string;
  trekListingId: string;
  trekTitle: string;
  destination: string;
  organizerName: string;
  startDate: string;
  endDate: string;
  price: number;
  capacity: number;
  availableSlots: number;
  departureStatus: DepartureStatus;
}

export interface TrekListingDetail {
  id: string;
  organizer: {
    id: string;
    organizerName: string;
    description?: string;
    location?: string;
    websiteUrl?: string;
  };
  title: string;
  destination: string;
  description?: string;
  difficulty: TrekDifficulty;
  durationDays: number;
  startingPrice: number;
  coverImageUrl?: string;
  departures: TrekDeparture[];
}
