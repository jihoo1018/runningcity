// src/entities/user/api/nickname.ts

import { apiPost } from '@/shared/api/http';
import { ApiResponse } from '@/shared/api/types';

export interface NicknameUpdateRequest {
  nickname: string;
}

export interface NicknameUpdateResponse {
  userId: number;
  nickname: string;
}

/**
 * 닉네임 업데이트 API
 */
export async function updateNickname(
  userId: number,
  nickname: string
): Promise<NicknameUpdateResponse> {
  const response = await apiPost<ApiResponse<NicknameUpdateResponse>, NicknameUpdateRequest>(
    `/users/${userId}/nickname`,
    { nickname }
  );
  return response.data;
}

