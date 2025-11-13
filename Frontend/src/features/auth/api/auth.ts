// src/features/auth/api/auth.ts
import { apiPost } from "@/shared/api/http";
import type { ApiResponse } from "@/shared/api/types";
import type { LoginBody, LoginResponse } from "../model/types";

export async function login(body: LoginBody): Promise<ApiResponse<LoginResponse>> {
  // 개발 환경에서는 /api 프록시가 자동으로 붙고, 배포에서는 ENV에 설정된 API_ORIGIN이 사용됩니다.
  return await apiPost<ApiResponse<LoginResponse>, LoginBody>("/auth/login", body);
}

