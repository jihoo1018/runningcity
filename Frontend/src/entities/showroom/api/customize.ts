//src\entities\showroom\api\customize.ts
import { apiGet, apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import { EquippedItem, InventoryItem } from "@/entities/showroom/model/type";

// 유저 보유 아이템 리스트 조회
export const fetchGetInventoryList = async (userId: number) => {
  const res = await apiGet<ApiResponse<InventoryItem>>(`/showroom/clothes/${userId}`);
  if (res.status !== 200 || res.code !== "CHANGE_CLOTHES_2000") {
    throw new Error(res.message || "유저 보유 아이템 리스트 조회");
  }
  return res.data;
};

//TODO 유저 현재 착장 저장
