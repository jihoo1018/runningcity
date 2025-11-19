// src/shared/lib/format.ts

/** 거리(m)를 km 문자열로 변환. 소수 2자리. null/undefined면 "-" */
export const metersToKm = (m?: number | null) =>
  m == null ? "-" : (m / 1000).toFixed(2) + "km";

/** 페이스(sec/km)를 "m'ss\"" 형태로 변환 (예: 142s/km -> 2'22") */
export const formatPace = (secPerKm?: number | null) => {
  if (secPerKm == null) return "-";
  const m = Math.floor(secPerKm / 60);
  const s = Math.floor(secPerKm % 60);
  return `${m}'${String(s).padStart(2, "0")}"`;
};

/** 시간(초)을 "HH:MM:SS" 형태로 변환 (예: 61 -> 00:01:01) */
export const formatDuration = (sec?: number | null) => {
  if (sec == null) return "-";
  const h = Math.floor(sec / 3600);
  const m = Math.floor((sec % 3600) / 60);
  const s = Math.floor(sec % 60);
  return [h, m, s].map((v) => String(v).padStart(2, "0")).join(":");
};
