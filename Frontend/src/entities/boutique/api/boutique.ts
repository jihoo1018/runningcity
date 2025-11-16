// src/entities/boutique/api/boutique.ts
import { apiGet, apiPost } from "@/shared/api/http";
import type { ApiResponse } from "@/shared/api/types";
import type { 
  StoreResponse, 
  PurchaseItemRequest, 
  PurchaseItemResponse,
  GachaRequest,
  GachaResponse
} from "../model/types";

/**
 * 스토어에서 구매할 수 있는 아이템 목록 조회
 * @param userId 사용자 ID
 * @returns 스토어 아이템 목록
 */
export async function getStoreItems(userId: number): Promise<ApiResponse<StoreResponse[]>> {
  return await apiGet<ApiResponse<StoreResponse[]>>(`/boutique/store/${userId}`);
}

/**
 * 아이템 구매
 * @param userId 사용자 ID
 * @param request 구매 요청 데이터 (itemId)
 * @returns 구매 결과
 */
export async function purchaseItem(
  userId: number, 
  request: PurchaseItemRequest
): Promise<ApiResponse<PurchaseItemResponse>> {
  return await apiPost<ApiResponse<PurchaseItemResponse>, PurchaseItemRequest>(
    `/boutique/store/${userId}`, 
    request
  );
}

/**
 * 사용자의 CR(화폐) 잔액 조회
 * 실제로는 /users/{userId} API를 호출하여 totalCredit 정보를 가져옵니다.
 * @param userId 사용자 ID
 * @returns CR 잔액 정보
 */
export async function getUserCurrency(userId: number): Promise<ApiResponse<{ cr: number }>> {
  const response = await apiGet<ApiResponse<UserCurrencyResponse>>(`/users/${userId}`);
  
  // UserResponse에서 totalCredit을 cr로 변환하여 반환
  return {
    ...response,
    data: {
      cr: response.data.totalCredit ?? 0
    }
  };
}

/**
 * 가챠 뽑기
 * @param userId 사용자 ID
 * @param request 가챠 요청 (single or multi)
 * @returns 가챠 결과
 */
export async function drawGacha(
  userId: number,
  request: GachaRequest
): Promise<ApiResponse<GachaResponse>> {
  return await apiPost<ApiResponse<GachaResponse>, GachaRequest>(
    `/boutique/gacha/${userId}`,
    request
  );
}

// 백엔드 UserResponse 타입 (필요한 필드만)
interface UserCurrencyResponse {
  userId: number;
  totalCredit?: number;
  // 다른 필드들은 생략
}
