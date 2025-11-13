import { apiGet, apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import {
  Entry,
  EntryDetail,
  GroupedEntryResponse,
  CreateSession,
} from "@/entities/entry/model/types";

/** ✅ 활성화된 기지 리스트 조회 */
export async function fetchGetEntryList(): Promise<Entry[]> {
  const res = await apiGet<ApiResponse<Entry[]>>(`/entry/list/today`);
  if (res.status !== 200 || res.code !== "ENTRY_2001") {
    throw new Error(res.message || "활성화 기지 조회 실패");
  }
  return res.data;
}

/** ✅ 전체 기지 리스트 조회 (그룹 구조) */
export async function fetchGetAllEntryList(): Promise<GroupedEntryResponse> {
  const res = await apiGet<ApiResponse<GroupedEntryResponse>>(`/entry/list/all`);
  if (res.status !== 200 || res.code !== "ENTRY_2001") {
    throw new Error(res.message || "전체 기지 조회 실패");
  }
  return res.data;
}

export async function fetchGetEntryDetail(baseId: number): Promise<EntryDetail> {
  const res = await apiGet<ApiResponse<EntryDetail>>(`/entry/${baseId}`);
  if (res.status !== 200 || res.code !== "ENTRY_2000") {
    throw new Error(res.message || "기지 상세 조회 실패");
  }
  return res.data;
}

/** ✅ 개인 잠입 세션 시작 */
export async function fetchStartEntry(userId: number, baseId: number): Promise<CreateSession> {
  const res = await apiPost<ApiResponse<CreateSession>>(`/sessions?userId=${userId}`, {
    type: "ENTRY",
    deviceType: "WATCH",
    baseId: baseId,
  });
  if (res.status !== 200 || res.code !== "COMMON_2000") {
    throw new Error(res.message || "잠입 세션 생성 실패");
  }
  return res.data;
}
