import { apiGet, apiPost } from "./http";
import { ApiResponse } from "./types";

// 운동 세션 데이터 전체 타입
export interface RunningSession {
  clientSecretKey: string; // UUID 키
  sessionId: number | null; // 세션 ID (nullable)
  userId: string; // 사용자 ID
  startTime: number; // 시작 시각 (밀리초)
  endTime: number; // 종료 시각 (밀리초)
  summary: SummaryData; // 요약 데이터
  cadenceRecords: CadenceRecord[]; // 케이던스 기록 리스트
  heartRateRecords: HeartRateRecord[]; // 심박수 기록 리스트
  gpsPoints: GpsPoint[]; // GPS 포인트 리스트
  // exp: number; // EXP 획득 경험치
  // credit: number; // credit 획득 크레딧
  dataChipCnt: number; // 데이터칩 개수
  rewards?: rewards;
}

// ✅ 리워드 데이터 타입
export interface rewards {
  exp: number; // exp
  credit: number; // credit
}

// ✅ 요약 정보 타입
export interface SummaryData {
  totalSteps: number; // 총 걸음 수
  totalDistance: number; // 총 거리 (미터)
  totalCalories: number; // 소모 칼로리 (kcal)
  avgHeartRate: number; // 평균 심박수 (bpm)
  duration: number; // 운동 지속 시간 (초)
  avgCadence: number; // 평균 케이던스 (spm)
  avgPace: number; // 평균 페이스 (초/km)
  elevation: number; // 평균 고도 (m)
}

// ✅ 케이던스 데이터 타입
export interface CadenceRecord {
  seq: number; // 레코드 ID
  cadence: number; // 케이던스 (spm)
  createdAt: number; // 측정 시각 (밀리초)
}

// ✅ 심박수 데이터 타입
export interface HeartRateRecord {
  seq: number; // 레코드 ID
  heartRate: number; // 심박수 (bpm)
  createdAt: number; // 측정 시각 (밀리초)
}

// ✅ GPS 포인트 데이터 타입
export interface GpsPoint {
  seq: number; // 레코드 ID
  latitude: number; // 위도
  longitude: number; // 경도
  altitude: number; // 고도 (m)
  speed: number; // 속도 (m/s)
  createdAt: number; // 측정 시각 (밀리초)
}

// 잠입 결과 데이터 받는 함수
export async function getEntryResult(
  sessionId: number
): Promise<RunningSession> {
  const data: RunningSession = {
    clientSecretKey: "run_a1b2c3d4",
    sessionId,
    userId: "default_user",
    startTime: 1699876530000,
    endTime: 1699878330000,
    rewards: {
      exp: 800,
      credit: Math.floor(800 / 20),
    },
    dataChipCnt: Math.floor(5000.0 / 1000),
    summary: {
      totalSteps: 5280,
      totalDistance: 5000.0,
      totalCalories: 450,
      avgHeartRate: 145,
      duration: 1800,
      avgCadence: 170,
      avgPace: 300,
      elevation: 125.5,
    },
    cadenceRecords: [
      { seq: 1, cadence: 165.0, createdAt: 1699876535000 },
      { seq: 2, cadence: 170.0, createdAt: 1699876540000 },
      { seq: 3, cadence: 168.0, createdAt: 1699876545000 },
    ],
    heartRateRecords: [
      { seq: 1, heartRate: 140, createdAt: 1699876535000 },
      { seq: 2, heartRate: 145, createdAt: 1699876540000 },
      { seq: 3, heartRate: 148, createdAt: 1699876545000 },
    ],
    gpsPoints: [
      {
        seq: 1,
        latitude: 37.5665,
        longitude: 126.978,
        altitude: 50.0,
        speed: 3.33,
        createdAt: 1699876535000,
      },
      {
        seq: 2,
        latitude: 37.5666,
        longitude: 126.9781,
        altitude: 51.2,
        speed: 3.45,
        createdAt: 1699876540000,
      },
      {
        seq: 3,
        latitude: 37.5667,
        longitude: 126.9782,
        altitude: 52.5,
        speed: 3.4,
        createdAt: 1699876545000,
      },
    ],
  };

  // Promise를 반환하도록
  return Promise.resolve(data);
}

// 잠입 결과 완료 전송
export async function completeEntrySession(
  runningSession: RunningSession
): Promise<boolean> {
  const res = await apiPost<ApiResponse<boolean>>(
    `/api/v1/sessions/${runningSession.sessionId}/finish`,
    runningSession
  );
  if (res.status !== 200 || res.code !== "COMMON_2000") {
    throw new Error(res.message || "잠입 세션 생성 실패");
  }
  return true;
}
