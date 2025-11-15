//src\entities\showroom\api\customize.ts
import { apiGet, apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import { EquippedItem, InventoryItem } from "@/entities/showroom/model/type";
import { useAuthStore } from "@/features/auth/model/useAuthStore";

const userId = useAuthStore.getState().user?.userId;

// 유저 보유 아이템 리스트 조회
export async function fetchGetInventoryList() {
  const res = await apiGet<ApiResponse<InventoryItem[]>>(`/showroom/clothes/${userId}`);
  if (res.status !== 200 || res.code !== "CHANGE_CLOTHES_2000") {
    throw new Error(res.message || "유저 보유 아이템 리스트 조회");
  }
  return res.data;
}

// 유저 현재 착장 저장
export async function fetchSave(itemList: EquippedItem[]): Promise<void> {
  const res = await apiPost<ApiResponse<void>>(`/showroom/clothes/${userId}`, itemList);
  if (res.status !== 200 || res.code !== "CHANGE_CLOTHES_2001") {
    throw new Error(res.message || "유저 착장 저장 실패");
  }
  return res.data;
}
