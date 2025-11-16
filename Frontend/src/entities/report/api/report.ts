// src/entities/report/api/report.ts
import { apiGet, apiPost } from "@/shared/api/http";
import { ENV } from "@/shared/config/env";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import type { ApiResponse, ReportDetail, ReportMonthPayload } from "../model/types";
// import { fetchReportDetailMock, fetchReportMonthMock } from "./report.mock";

export async function fetchReportDetail(sid: number): Promise<ReportDetail> {
  //    if (ENV.MOCK_API) return fetchReportDetailMock(sid); // ✅ 목 스위치
  const uid = useAuthStore.getState().user?.userId;
  const qs = new URLSearchParams();
  if (uid != null) qs.set("userId", String(uid));
  const path = qs.toString() ? `/report/${sid}?${qs.toString()}` : `/report/${sid}`;
  const env = await apiGet<ApiResponse<ReportDetail>>(path);
  if (env.status !== 200 || env.code !== "COMMON_2000") throw new Error(env.message);
  return env.data;
}

export async function regenerateAiReport(sid: number): Promise<string | null> {
  const uid = useAuthStore.getState().user?.userId;
  const qs = new URLSearchParams();
  if (uid != null) qs.set("userId", String(uid));

  const path = qs.toString() ? `/report/${sid}/ai?${qs.toString()}` : `/report/${sid}/ai`;

  const response = await apiPost<ApiResponse<{ content: string } | null>>(path);

  if (response.status !== 200 || response.code !== "COMMON_2000") {
    throw new Error(response.message);
  }

  return response.data?.content ?? null;
}
