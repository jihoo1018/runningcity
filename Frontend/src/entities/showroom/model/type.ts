// src/entities/showroom/model/types.ts

export type MyOffice = {
  // userNm: string;
  // userLv: number;
  // userId: number;
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
  category: "bodies" | "clothes" | "hair" | "head";
  subcategory: string;
  style?: string | null; // head 파츠만 style 존재
  basePath: string; // DB에 저장된 경로
};

export type InventoryItem = {
  inventoryId: number;
  itemId: number;
  quantity: number;
  category: "bodies" | "clothes" | "hair" | "head";
  subcategory: string;
  style: string;
  color: string;
  name: string;
  assetKey: string;
  basePath: string;
  rarity: string;
  priceCr: string;
  obtainMethod: string;
};

export type PrivacySetting = {
  userId: number;
  isGlobalPublic: boolean;
  showTotalRunning: boolean;
  showMaxDistance: boolean;
  showAvgPace: boolean;
  showBestPace: boolean;
  showHikingCount: boolean;
  tags: string[];
};

export type ItemGridProps = {
  items: InventoryItem[];
  onPrev: () => void;
  onNext: () => void;
  onSelect: (item: InventoryItem) => void;
};

// ============================================
// 랜덤 아바타 (친구/글로벌 쇼룸용)
// ============================================
export type RandomAvatar = {
  userId: number;
  nickname: string;
  level: number;
  equippedItems: RandomAvatarEquippedItem[];
};

export type RandomAvatarEquippedItem = {
  itemId: number;
  category: "bodies" | "clothes" | "hair" | "head";
  subcategory: string;
  style?: string | null;
  basePath: string;
};