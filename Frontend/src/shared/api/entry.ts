// src/shared/api/entry.ts

import { apiPost } from "./http";
import { ApiResponse } from "./types";

// 📁 src/types/entry.ts
export type EntryDetail = {
  baseId: number;
  courseNm: string;
  region: string;
  latitude: number;
  longitude: number;
  groupNo: number; // ✅ groupNo 반드시 포함
  distanceKm?: number;
  difficulty?: string;
  duration?: string;
  courseDesc?: string;
  address?: string;
  dataSource?: string;
};

export type CreateSession = {
  sessionId: number;
};

export async function fetchStartEntry(baseId: number): Promise<CreateSession> {
  const env = await apiPost<ApiResponse<CreateSession>>(`/api/v1/sessions`, {
    type: "NORMAL", // "NORMAL" | "ENTRY"
    deviceType: "PHONE",
    baseId: baseId,
  });
  if (env.status !== 200 || env.code !== "COMMON_2000")
    throw new Error(env.message);
  return env.data;
}
