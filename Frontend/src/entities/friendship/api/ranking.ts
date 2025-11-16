// src/entities/friendship/api/ranking.ts
import { apiGet } from "@/shared/api/http";
import type { ApiResponse } from "@/shared/api/types";
import type { FriendRankingResponse } from "../model/types";

export async function fetchFriendRanking(userId: number): Promise<FriendRankingResponse> {
  const response = await apiGet<ApiResponse<FriendRankingResponse>>(
    `/friends/ranking?userId=${userId}`
  );
  
  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message || "친구 랭킹 조회에 실패했습니다.");
  }
  
  return response.data;
}

