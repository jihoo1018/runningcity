import { apiGet, apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import { RunningSession } from "@/entities/entry/model/types";

// TODO 나중에 지우기. 임시로 clientSecretKey 발급하기 위한 함수
function generateUUID(): string {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

// 잠입 결과 데이터 받는 함수
export async function getEntryResult(sessionId: number): Promise<RunningSession> {
  const data: RunningSession = {
    clientSecretKey: generateUUID(),
    sessionId,
    userId: 1,
    startTime: 1699876530000,
    endTime: 1699878330000,
    // rewards: {
    //   exp: 800,
    //   credit: Math.floor(800 / 20),
    // },
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
export async function completeEntrySession(runningSession: RunningSession): Promise<boolean> {
  const res = await apiPost<ApiResponse<boolean>>(
    `/sessions/${runningSession.sessionId}/finish?userId=${runningSession.userId}`,
    runningSession,
  );
  if (res.status !== 200 || res.code !== "COMMON_2000") {
    throw new Error(res.message || "잠입 세션 생성 실패");
  }
  return true;
}
