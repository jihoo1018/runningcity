import { apiGet, apiPost } from "./http";
import { ApiResponse } from "./types";

/** ✅ 기본 엔트리 정보 */
export type Entry = {
  baseId: number;
  courseNm: string;
  region: string;
  latitude: number;
  longitude: number;
  groupNo: number;
  computedDistanceKm?: number; // 클라이언트 계산용
};

/** ✅ 상세 엔트리 정보 */
export type EntryDetail = {
  baseId: number;
  courseNm: string;
  region: string;
  latitude: number;
  longitude: number;
  groupNo: number;
  distanceKm?: number;
  difficulty?: string;
  duration?: string;
  courseDesc?: string;
  address?: string;
  dataSource?: string;
};

/** ✅ 그룹별 응답 구조 */
export type GroupedEntryResponse = Record<string, Entry[]>;

/** ✅ 세션 생성 응답 */
export type CreateSession = {
  sessionId: number;
};

/** ✅ 활성화된 기지 리스트 조회 */
export async function fetchGetEntryList(): Promise<Entry[]> {
  const res = await apiGet<ApiResponse<Entry[]>>(`/api/v1/entry/list/today`);
  if (res.status !== 200 || res.code !== "ENTRY_2001") {
    throw new Error(res.message || "활성화 기지 조회 실패");
  }
  return res.data;
}

/** ✅ 전체 기지 리스트 조회 (그룹 구조) */
export async function fetchGetAllEntryList(): Promise<GroupedEntryResponse> {
  const res = await apiGet<ApiResponse<GroupedEntryResponse>>(
    `/api/v1/entry/list/all`
  );
  if (res.status !== 200 || res.code !== "ENTRY_2001") {
    throw new Error(res.message || "전체 기지 조회 실패");
  }
  return res.data;
}

export async function fetchGetEntryDetail(
  baseId: number
): Promise<EntryDetail> {
  const res = await apiGet<ApiResponse<EntryDetail>>(`/api/v1/entry/${baseId}`);
  if (res.status !== 200 || res.code !== "ENTRY_2000") {
    throw new Error(res.message || "기지 상세 조회 실패");
  }
  return res.data;
}

/** ✅ 개인 잠입 세션 시작 */
export async function fetchStartEntry(baseId: number): Promise<CreateSession> {
  const res = await apiPost<ApiResponse<CreateSession>>(`/api/v1/sessions`, {
    type: "ENTRY",
    deviceType: "WATCH",
    baseId: baseId,
  });
  if (res.status !== 200 || res.code !== "COMMON_2000") {
    throw new Error(res.message || "잠입 세션 생성 실패");
  }
  return res.data;
}
