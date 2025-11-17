// src/entities/showroom/api/me.ts
import { apiGet, apiPost } from "@/shared/api/http";
import { ApiResponse } from "@/shared/api/types";
import { MyOffice, EquippedItem, RandomAvatar } from "@/entities/showroom/model/type";
import { useAuthStore } from "@/features/auth/model/useAuthStore";

// 쇼룸 첫화면 데이터 조회
export const fetchGetShowroomMe = async () => {
  const userId = useAuthStore.getState().user?.userId;
  const res = await apiGet<ApiResponse<MyOffice>>(`/showroom/office/${userId}`);
  if (res.status !== 200 || res.code !== "MY_OFFICE_2000") {
    throw new Error(res.message || "내 사무실 조회 실패");
  }
  return res.data;
};

// ============================================
// 글로벌 쇼룸 - 랜덤 유저 아바타 조회
// ============================================
export const fetchGetGlobalShowroom = async (size: number = 20) => {
  try {
    const userId = useAuthStore.getState().user?.userId;
    console.log(`[글로벌 쇼룸] 요청: userId=${userId}, size=${size}`);
    
    const res = await apiGet<ApiResponse<RandomAvatar[]>>(
      `/showroom/global/${userId}?size=${size}`
    );
    
    console.log('[글로벌 쇼룸] 응답:', res);
    console.log('[글로벌 쇼룸] 데이터:', res.data);
    console.log('[글로벌 쇼룸] 데이터 개수:', res.data?.length);
    
    if (res.status !== 200) {
      throw new Error(res.message || "글로벌 쇼룸 조회 실패");
    }
    
    // 데이터가 null이면 빈 배열로 변환
    const data = res.data || [];
    console.log(`[글로벌 쇼룸] 최종 반환 데이터 개수: ${data.length}`);
    
    return data;
  } catch (error: any) {
    console.error('[글로벌 쇼룸] 에러 발생:', error);
    
    // 404 에러면 글로벌 유저가 없는 것으로 처리
    if (error.message?.includes('404')) {
      console.warn('글로벌 API가 아직 준비되지 않았거나 유저가 없습니다.');
      return [];
    }
    throw error;
  }
};

// ============================================
// 친구 쇼룸 - 친구들의 아바타 조회
// ============================================
export const fetchGetFriendShowroom = async (size?: number) => {
  try {
    const userId = useAuthStore.getState().user?.userId;
    const url = size
      ? `/showroom/friends/${userId}?size=${size}`
      : `/showroom/friends/${userId}`;
    const res = await apiGet<ApiResponse<RandomAvatar[]>>(url);
    if (res.status !== 200) {
      throw new Error(res.message || "친구 쇼룸 조회 실패");
    }
    return res.data;
  } catch (error: any) {
    // 404 에러면 친구가 없는 것으로 처리
    if (error.message?.includes('404')) {
      console.warn('친구 API가 아직 준비되지 않았거나 친구가 없습니다.');
      return [];
    }
    throw error;
  }
};
