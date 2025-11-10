// src/entities/report/model/types.ts
export type ApiResponse<T> = {
  status: number;
  code: string;
  message: string;
  data: T;
  error: unknown | null;
};

export type ReportDetail = {
  type: "NORMAL" | "ENTRY";
  summary: {
    totalSteps: number | null;
    totalDistance: number | null;   // meters
    totalCalories: number | null;
    avgHeartRate: number | null;
    duration: number | null;        // seconds
    avgCadence: number | null;
    avgPace: number | null;         // sec/km
    elevation: number | null;
  };
  rewards?: { credit?: number; exp?: number };
  route?: { geojson: string };      // GeoJSON LineString (WGS84, [lon,lat])
};

export type ReportListItem = {
  sessionId: number;
  startTime: string;                // ISO
  type: "NORMAL" | "ENTRY" | string;
  totalDistance: number;            // 백엔드 스펙에 맞춰 단위 고정
  duration: number;
  avgPace: number;
};

export type ReportMonthPayload = {
  content: ReportListItem[];
  MonthtotalDistance: number;
  totalAvgPace: number;
  totalSession: number;
};
