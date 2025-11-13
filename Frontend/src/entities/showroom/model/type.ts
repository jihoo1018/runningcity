// src/entities/showroom/model/types.ts

export type Showroom = {
  baseId: number;
  courseNm: string;
  region: string;
  latitude: number;
  longitude: number;
  groupNo: number;
  computedDistanceKm?: number; // 클라이언트 계산용
};
