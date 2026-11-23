export type UserRole = 'USER' | 'ORGANIZER' | 'ADMIN';

export type ExperienceLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export type SocialPlatform = 'INSTAGRAM' | 'WHATSAPP' | 'FACEBOOK' | 'LINKEDIN' | 'SPOTIFY' | 'OTHER';

export type VisibilityLevel = 'PRIVATE' | 'GROUP_MEMBERS' | 'PUBLIC';

export interface AuthUser {
  userId: string;
  email: string;
  role: UserRole;
  displayName: string;
}

export interface SocialLink {
  id?: string;
  platform: SocialPlatform;
  url: string;
  visibility: VisibilityLevel;
}

export interface TrekHistory {
  id?: string;
  trekName: string;
  location?: string;
  startDate?: string;
  endDate?: string;
  description?: string;
}

export interface UserProfileData {
  userId: string;
  email: string;
  role: UserRole;
  displayName: string;
  profilePhotoUrl?: string;
  location?: string;
  occupation?: string;
  bio?: string;
  trekkingExperienceLevel: ExperienceLevel;
  socialLinks: SocialLink[];
  trekHistory: TrekHistory[];
}
