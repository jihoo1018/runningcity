// src/entities/run/model/types.ts

export interface RunningMetrics {
  heartRate: number; // 심박수 (bpm)
  pace: number; // 페이스 (초/km)
  time: number; // 시간 (초)
  distance: number; // 거리 (km)
  energy?: number; // 에너지 (선택사항)
}

export type RunningState = 'IDLE' | 'RUNNING' | 'PAUSED' | 'STOPPED';

export interface RunningSession {
  sessionId: number;
  userId: number;
  startTime: string;
  endTime?: string;
  metrics: RunningMetrics;
  state: RunningState;
}

