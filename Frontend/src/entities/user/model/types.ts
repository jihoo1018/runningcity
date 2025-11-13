// src/entities/user/model/types.ts

export type FitnessLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT' | 'ELITE';

export interface UserProfile {
  userId: number;
  nickname: string;
  hasRunningHistory: boolean;
  fitnessLevel: FitnessLevel;
  targetDistanceKm: number;
  restingHeartRate?: number;
  hasSmartWatch: boolean;
}

export interface User {
  userId: number;
  nickname: string;
  profile?: UserProfile;
  onboardingCompletedAt?: string;
}

