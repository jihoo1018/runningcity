// src/entities/showroom/model/types.ts

export type Showroom = {
  userNm: string;
  userLv: number;
  userId: number;
  totalDist: number; // 유저가 총 달린 거리
  longestDist: number; // 유저가 달린 최장 거리
  avgPace: number; // 유저 평균 페이스
  bestPace: number; // 유저 최고 기록(페이스)
  totalEntryCnt: number; // 유저 총 잠입 횟수
  equippedItemList: EquippedItem[];
};

export type EquippedItem = {
  equippedId: number;
  itemId: number;
  category: string;
  subcategory: string;
};
