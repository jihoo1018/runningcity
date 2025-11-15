// src/entities/friendship/api/request.ts
import { apiGet, apiPost } from "@/shared/api/http";
import type { ApiResponse } from "@/shared/api/types";
import type {
  MyCodeResponse,
  SentRequestResponse,
  SendFriendRequestBody,
  SendFriendRequestResponse,
  ReceivedRequestResponse,
  AcceptFriendRequestResponse,
} from "../model/types";

export async function fetchMyCode(userId: number): Promise<MyCodeResponse> {
  const response = await apiGet<ApiResponse<MyCodeResponse>>(
    `/friends/my-code?userId=${userId}`
  );
  
  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message || "내 코드 조회에 실패했습니다.");
  }
  
  return response.data;
}

export async function fetchSentRequests(userId: number): Promise<SentRequestResponse> {
  const response = await apiGet<ApiResponse<SentRequestResponse>>(
    `/friends/sent-requests?userId=${userId}`
  );
  
  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message || "보낸 요청 조회에 실패했습니다.");
  }
  
  return response.data;
}

export async function sendFriendRequest(
  userId: number,
  body: SendFriendRequestBody
): Promise<SendFriendRequestResponse> {
  const response = await apiPost<ApiResponse<SendFriendRequestResponse>, SendFriendRequestBody>(
    `/friends/request?userId=${userId}`,
    body
  );
  
  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message || "친구 요청 전송에 실패했습니다.");
  }
  
  return response.data;
}

export async function cancelFriendRequest(
  userId: number,
  friendshipId: number
): Promise<void> {
  const response = await apiPost<ApiResponse<void>>(
    `/friends/request/${friendshipId}?userId=${userId}`,
    undefined,
    "DELETE"
  );
  
  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message || "친구 요청 취소에 실패했습니다.");
  }
}

export async function fetchReceivedRequests(userId: number): Promise<ReceivedRequestResponse> {
  const response = await apiGet<ApiResponse<ReceivedRequestResponse>>(
    `/friends/received-requests?userId=${userId}`
  );
  
  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message || "받은 요청 조회에 실패했습니다.");
  }
  
  return response.data;
}

export async function acceptFriendRequest(
  userId: number,
  friendshipId: number
): Promise<AcceptFriendRequestResponse> {
  const response = await apiPost<ApiResponse<AcceptFriendRequestResponse>>(
    `/friends/request/${friendshipId}/accept?userId=${userId}`
  );
  
  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message || "친구 요청 수락에 실패했습니다.");
  }
  
  return response.data;
}

export async function rejectFriendRequest(
  userId: number,
  friendshipId: number
): Promise<void> {
  const response = await apiPost<ApiResponse<void>>(
    `/friends/request/${friendshipId}/reject?userId=${userId}`
  );
  
  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message || "친구 요청 거절에 실패했습니다.");
  }
}

