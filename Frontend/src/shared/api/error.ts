import type { ApiResponse } from "./types";

interface ApiErrorLike extends Error {
  response?: {
    status: number;
    data?: ApiResponse<unknown> | null;
  };
}

export function getApiErrorMessage(
  err: unknown,
  fallback = "요청 처리 중 오류가 발생했습니다.",
): string {
  const error = err as ApiErrorLike;
  const data = error.response?.data;

  const detailMsg = data?.error?.details?.[0]?.message;
  if (detailMsg) return detailMsg;

  if (data?.message) return data.message;

  return fallback;
}
