//src\entities\showroom\api\privacySetting.ts
import { apiGet, apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import { PrivacySetting } from "@/entities/showroom/model/type";
import { useAuthStore } from "@/features/auth/model/useAuthStore";

// 유저 설정 조회
export const fetchGetPrivacySetting = async () => {
  const userId = useAuthStore.getState().user?.userId;
  const res = await apiGet<ApiResponse<PrivacySetting>>(`/showroom/privacy/${userId}`);
  if (res.status !== 200 || res.code !== "PRIVACY_SETTING_2000") {
    throw new Error(res.message || "내 사무실 설정 정보 조회 실패");
  }
  return res.data;
};

// 현재 유저 설정 저장
export async function fetchSavePrivacySetting(privacySetting: PrivacySetting): Promise<void> {
  const userId = useAuthStore.getState().user?.userId;
  const res = await apiPost<ApiResponse<void>>(`/showroom/privacy/${userId}`, privacySetting);

  if (res.status !== 200 || res.code !== "PRIVACY_SETTING_2001") {
    throw new Error(res.message || "내 사무실 설정 정보 저장 실패");
  }
  return res.data;
}
