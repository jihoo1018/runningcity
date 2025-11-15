//src\entities\showroom\api\privacySetting.ts
import { apiGet, apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import { PrivacySetting } from "@/entities/showroom/model/type";

export const fetchGetPrivacySetting = async (userId: number) => {
  const res = await apiGet<ApiResponse<PrivacySetting>>(`/showroom/privacy/${userId}`);
  if (res.status !== 200 || res.code !== "PRIVACY_SETTING_2000") {
    throw new Error(res.message || "내 사무실 설정 정보 조회 실패");
  }
  return res.data;
};

//TODO 현재 설정 저장
