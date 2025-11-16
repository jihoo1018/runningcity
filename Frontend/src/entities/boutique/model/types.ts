// src/entities/boutique/model/types.ts

// 아이템 카테고리 타입
export type ItemCategory = 'bodies' | 'clothes' | 'hair' | 'head';

// 서브카테고리 타입
export type ItemSubcategory = 
  // bodies
  | 'male'
  // clothes  
  | 'longsleeve' | 'shorts' | 'shortsleeves' | 'tshirt'
  // hair
  | 'afro' | 'buzzcut' | 'dreadlocks_long' | 'long_hair' | 'messy1' | 'pixie'
  // head
  | 'ears' | 'eyebrows' | 'eyes' | 'faces' | 'heads' | 'nose';

// 스타일 타입
export type ItemStyle = 
  // eyebrows styles
  | 'thick' | 'thin'
  // eyes styles  
  | 'anger' | 'closing' | 'default_eye' | 'eyeroll'
  | 'look_l' | 'look_r' | 'neutral' | 'sad' | 'sad2'
  | 'shame' | 'shock'
  // faces styles
  | 'blush' | 'closed' | 'happy'
  // ears styles
  | 'medium';

// 희귀도 타입
export type ItemRarity = 'common' | 'rare' | 'epic' | 'legendary';

// 획득 방법 타입
export type ObtainMethod = 'gacha' | 'store';

// 부티크 아이템 타입
export interface BoutiqueItem {
  itemId: number;
  category: ItemCategory;
  subcategory: ItemSubcategory;
  style?: ItemStyle;
  color?: string;
  name: string;
  assetKey: string;
  basePath: string;
  rarity: ItemRarity;
  priceCr: number;
  obtainMethod: ObtainMethod;
  createdAt: string;
  updatedAt: string;
}

// 스토어 응답 타입 (백엔드 StoreResponse와 매칭)
export interface StoreResponse {
  itemId: number;
  category: ItemCategory;
  subcategory: ItemSubcategory;
  style?: ItemStyle;
  color?: string;
  name: string;
  assetKey: string;
  basePath: string;
  rarity: ItemRarity;
  priceCr: number;
  isPurchased: boolean; // 사용자가 이미 구매했는지 여부 (백엔드 StoreResponse.isPurchased)
}

// 아이템 구매 요청 타입 (백엔드 PurchaseRequest와 매칭)
export interface PurchaseItemRequest {
  itemId: number;
}

// 아이템 구매 응답 타입 (백엔드 PurchaseResponse와 매칭)
export interface PurchaseItemResponse {
  itemId: number;        // 구매한 아이템 ID
  itemName: string;      // 아이템 이름
  paidCr: number;        // 지불한 크레딧
  remainingCr: number;   // 남은 크레딧
}

// 카테고리별 필터 옵션
export interface CategoryFilter {
  category: ItemCategory;
  label: string;
  icon?: string;
}

// 정렬 옵션
export type SortOption = 'name' | 'price_low' | 'price_high' | 'rarity';

export interface SortFilter {
  value: SortOption;
  label: string;
}
