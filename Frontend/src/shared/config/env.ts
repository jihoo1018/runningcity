// src/shared/config/env.ts

export const ENV = {
  API_ORIGIN: import.meta.env?.VITE_API_ORIGIN || "http://localhost:8080",
  NODE_ENV: import.meta.env?.NODE_ENV || "development",
  DEV: import.meta.env?.DEV || false,
  PROD: import.meta.env?.PROD || false,
} as const;
