// src/entities/entry/model/types.ts

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

export type RewardType =
  | "EXP_SMALL"
  | "EXP_MEDIUM"
  | "EXP_LARGE"
  | "CR_SMALL"
  | "CR_MEDIUM"
  | "CR_LARGE"
  | "EXP_CR";

export interface Reward {
  type: RewardType;
  name: string;
  exp?: number;
  cr?: number;
  message: string;
}

export type ApiResponse<T> = {
  status: number;
  code: string;
  message: string;
  data: T;
  error?: any | null;
};

// 운동 세션 데이터 전체 타입
export interface RunningSession {
  clientSecretKey: string; // UUID 키
  sessionId: number | null; // 세션 ID (nullable)
  userId: number; // 사용자 ID
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
