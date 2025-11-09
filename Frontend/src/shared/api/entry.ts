// src/shared/api/entry.ts

import { apiPost } from "./http";
import { ApiResponse } from "./types";

// 잠입 기지 관련 타입
// export type FitnessLevel =
//   | "BEGINNER"
//   | "INTERMEDIATE"
//   | "ADVANCED"
//   | "EXPERT"
//   | "ELITE";

// export interface OnboardingRequest {
//   hasRunningHistory: boolean;
//   fitnessLevel: FitnessLevel;
//   targetDistanceKm: number;
//   restingHeartRate?: number | null;
//   hasSmartWatch: boolean;
// }

// export interface OnboardingResponse {
//   userId: number;
//   onboardingCompletedAt: string;
//   profile: {
//     hasRunningHistory: boolean;
//     fitnessLevel: FitnessLevel;
//     targetDistanceKm: number;
//     restingHeartRate?: number;
//     hasSmartWatch: boolean;
//   };
// }

/**
 * 온보딩 완료 API
 */
// export async function completeOnboarding(
//   userId: number,
//   data: OnboardingRequest
// ): Promise<OnboardingResponse> {
//   const response = await apiPost<
//     ApiResponse<OnboardingResponse>,
//     OnboardingRequest
//   >(`/api/v1/users/${userId}/onboarding`, data);
//   return response.data;
// }
