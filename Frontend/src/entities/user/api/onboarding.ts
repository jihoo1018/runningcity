// src/entities/user/api/onboarding.ts

import { apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import type { FitnessLevel } from "../model/types";

export interface OnboardingRequest {
  hasRunningHistory: boolean;
  fitnessLevel: FitnessLevel;
  targetDistanceKm: number;
  restingHeartRate?: number | null;
  hasSmartWatch: boolean;
}

export interface OnboardingResponse {
  userId: number;
  onboardingCompletedAt: string;
  profile: {
    hasRunningHistory: boolean;
    fitnessLevel: FitnessLevel;
    targetDistanceKm: number;
    restingHeartRate?: number;
    hasSmartWatch: boolean;
  };
}

export interface UpdatePreferenceGoalRequest {
  targetDistanceKm?: number;
}

/**
 * 온보딩 완료 API
 */
export async function completeOnboarding(
  userId: number,
  data: OnboardingRequest,
): Promise<OnboardingResponse> {
  const response = await apiPost<ApiResponse<OnboardingResponse>, OnboardingRequest>(
    `/users/${userId}/onboarding`,
    data,
  );
  return response.data;
}

export async function updatePreference(
  userId: number,
  data: UpdatePreferenceGoalRequest,
): Promise<OnboardingResponse> {
  const response = await apiPost<ApiResponse<OnboardingResponse>, UpdatePreferenceGoalRequest>(
    `/users/${userId}/onboarding`,
    data,
    "PATCH",
  );
  return response.data;
}
