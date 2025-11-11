// src/entities/report/api/report.ts
import { apiGet } from "@/shared/api/http";
import { ENV } from "@/shared/config/env";
import type { ApiResponse, ReportDetail, ReportMonthPayload } from "../model/types";
// import { fetchReportDetailMock, fetchReportMonthMock } from "./report.mock";

export async function fetchReportDetail(sid: number): Promise<ReportDetail> {
//    if (ENV.MOCK_API) return fetchReportDetailMock(sid); // ✅ 목 스위치
  const env = await apiGet<ApiResponse<ReportDetail>>(`/report/${sid}`);
  if (env.status !== 200 || env.code !== "COMMON_2000") throw new Error(env.message);
  return env.data;
}

export async function fetchReportMonth(params?: { year?: number; month?: number }) {
//    if (ENV.MOCK_API) return fetchReportMonthMock(params); // ✅ 목 스위치
  const qs = new URLSearchParams();
  if (params?.year) qs.set("year", String(params.year));
  if (params?.month) qs.set("month", String(params.month));
  const path = qs.toString() ? `/report?${qs.toString()}` : `/report`;

  const env = await apiGet<ApiResponse<ReportMonthPayload>>(path);
  if (env.status !== 200 || env.code !== "COMMON_2000") throw new Error(env.message);
  return env.data;
}
