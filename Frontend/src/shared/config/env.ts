// src/shared/config/env.ts

export const ENV = {
  API_ORIGIN: import.meta.env?.VITE_API_ORIGIN || "http://localhost:8080",
  NODE_ENV: import.meta.env?.NODE_ENV || "development",
  DEV: import.meta.env?.DEV || false,
  PROD: import.meta.env?.PROD || false,
  KAKAO_MAP_APP_KEY: import.meta.env?.VITE_KAKAO_MAP_APP_KEY as string,
  ASSETS_ORIGIN: "http://k13a405.p.ssafy.io/store-assets",
  // MOCK_API: Boolean(import.meta.env?.VITE_MOCK_API === "1" || import.meta.env?.VITE_MOCK_API === "true"),
} as const;
