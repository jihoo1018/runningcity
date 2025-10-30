// src/shared/api/onboarding.ts

import { apiPost } from './http';

export interface NicknameUpdateRequest {
  nickname: string;
}

export interface NicknameUpdateResponse {
  userId: number;
  nickname: string;
}

export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

/**
 * 닉네임 업데이트 API
 */
export async function updateNickname(
  userId: number,
  nickname: string
): Promise<NicknameUpdateResponse> {
  const response = await apiPost<ApiResponse<NicknameUpdateResponse>, NicknameUpdateRequest>(
    `/api/v1/users/${userId}/nickname`,
    { nickname }
  );
  return response.data;
}

// 온보딩 관련 타입
export type FitnessLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT' | 'ELITE';

export interface OnboardingRequest {
  hasRunningHistory: boolean;
  fitnessLevel: FitnessLevel;
  targetDistanceKm: number;
  restingHeartRate?: number | null;
  hasSmartWatch: boolean;
}

export interface OnboardingResponse {
  userId: number;
  hasRunningHistory: boolean;
  fitnessLevel: FitnessLevel;
  targetDistanceKm: number;
  restingHeartRate?: number;
  hasSmartWatch: boolean;
}

/**
 * 온보딩 완료 API
 */
export async function completeOnboarding(
  userId: number,
  data: OnboardingRequest
): Promise<OnboardingResponse> {
  const response = await apiPost<ApiResponse<OnboardingResponse>, OnboardingRequest>(
    `/api/v1/users/${userId}/onboarding`,
    data
  );
  return response.data;
}
