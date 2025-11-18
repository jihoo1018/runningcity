// src/entities/showroom/model/types.ts

export type MyOffice = {
  // userNm: string;
  // userLv: number;
  // userId: number;
  totalDist: number; // 유저가 총 달린 거리
  maxDist: number; // 유저가 달린 최장 거리
  avgPace: number; // 유저 평균 페이스
  bestPace: number; // 유저 최고 기록(페이스)
  totalEntryCnt: number; // 유저 총 잠입 횟수
  equippedItemList: EquippedItem[];
  privacySetting?: PrivacySetting; // Privacy 설정 (선택적)
};

export type EquippedItem = {
  equippedId: number;
  itemId: number | null;
  category: "bodies" | "clothes" | "hair" | "head" | null;
  subcategory: string | null;
  style?: string | null; // head 파츠만 style 존재
  basePath: string; // DB에 저장된 경로 (COMPOSITE: 템플릿 경로, COMPLETE: 완성된 스프라이트 경로)
  spriteType?: "COMPOSITE" | "COMPLETE" | undefined; // 스프라이트 타입
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
  basePath: string; // COMPOSITE: 템플릿 경로, COMPLETE: 완성된 스프라이트 경로
  rarity: string;
  priceCr: string;
  obtainMethod: string;
  spriteType?: "COMPOSITE" | "COMPLETE" | undefined; // 스프라이트 타입
};

export type PrivacySetting = {
  userId: number;
  globalPublic: boolean;
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
  // 🆕 통계 정보
  totalDist: number;
  maxDist: number;
  avgPace: number;
  bestPace: number;
  totalEntryCnt: number;
  // 🆕 Privacy 설정
  privacySetting: PrivacySetting;
};

export type RandomAvatarEquippedItem = {
  itemId: number | null;
  category: "bodies" | "clothes" | "hair" | "head" | null;
  subcategory: string | null;
  style?: string | null;
  basePath: string; // COMPOSITE: 템플릿 경로, COMPLETE: 완성된 스프라이트 경로
  spriteType?: "COMPOSITE" | "COMPLETE"; // 스프라이트 타입
};