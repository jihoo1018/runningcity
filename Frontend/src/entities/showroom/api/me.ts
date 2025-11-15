// src/entities/showroom/api/me.ts
import { apiGet, apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import { MyOffice, EquippedItem } from "@/entities/showroom/model/type";

// 쇼룸 첫화면 데이터 조회
export const fetchGetShowroomMe = async (userId: number) => {
  const res = await apiGet<ApiResponse<MyOffice>>(`/showroom/office/${userId}`);
  if (res.status !== 200 || res.code !== "MY_OFFICE_2000") {
    throw new Error(res.message || "내 사무실 조회 실패");
  }
  return res.data;
};
