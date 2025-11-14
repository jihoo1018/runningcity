/**
 * 🎨 캐릭터 데이터 저장소 (Zustand Store)
 * 
 * 역할:
 * - 아이템 목록을 메모리에 저장
 * - 착용 중인 아이템 관리
 * - 매번 API 호출 안 하게 캐싱
 */

import { create } from 'zustand';

// ============================================
// 📦 타입 정의 (DB에서 받는 아이템 구조)
// ============================================
interface CharacterItem {
  itemId: number;
  category: string;       // "bodies", "clothes", "hair", "head"
  subcategory: string;    // "male", "longsleeve", "eyes"
  style: string | null;   // "anger", "neutral" 등
  color: string | null;   // "light", "red", "blonde"
  name: string;           // "남성 라이트 바디"
//   assetKey: string;       // "male_light" > 이건 안쓸 수도
  basePath: string;       // "\\AssetsStore\\...\\{animation}\\light.png"
  rarity: string;         // "common", "rare", "epic", "legendary"
}

// ============================================
// 📦 Store 타입 정의
// ============================================
interface AvatarStore {
  // ──────────────────────────────────────
  // 💾 저장된 데이터
  // ──────────────────────────────────────
  
  /** 보유 중인 전체 아이템 목록 (인벤토리) */
  inventoryItems: CharacterItem[];
  
  /** 착용 중인 아이템들 */
  //추가 필요!!
  equippedItems: {
    bodies?: CharacterItem;
    clothes?: CharacterItem;
    hair?: CharacterItem;
    head_eyes?: CharacterItem;
    head_ears?: CharacterItem;
    // 필요한 만큼 추가...
  };
  
  /** 현재 선택된 애니메이션 */
  currentAnimation: string;
  
  /** 마지막으로 데이터를 불러온 시간 */
  lastLoadTime: number | null;
  
  // ──────────────────────────────────────
  // 🎮 데이터 조작 함수들
  // ──────────────────────────────────────
  
  /** 인벤토리 저장 (API에서 받은 데이터) */
  setInventory: (items: CharacterItem[]) => void;
  
  /** 아이템 착용 */
  equipItem: (item: CharacterItem) => void;
  
  /** 아이템 해제 */
  unequipItem: (category: string) => void;
  
  /** 애니메이션 변경 */
  setAnimation: (animation: string) => void;
  
  /** 캐시가 유효한지 확인 (5분 이내면 유효) */
  isCacheValid: () => boolean;
  
  /** 전체 초기화 */
  reset: () => void;
}

// ============================================
// 🏪 Zustand Store 생성
// ============================================
export const useAvatarStore = create<AvatarStore>((set, get) => ({
  // ──────────────────────────────────────
  // 초기값 설정
  // ──────────────────────────────────────
  inventoryItems: [],
  equippedItems: {},
  currentAnimation: 'walk',
  lastLoadTime: null,
  
  // ──────────────────────────────────────
  // 함수 1: 인벤토리 저장
  // ──────────────────────────────────────
  setInventory: (items) => {
    set({
      inventoryItems: items,
      lastLoadTime: Date.now(), // 현재 시간 저장
    });
    
    console.log('✅ 인벤토리 저장 완료:', items.length, '개');
  },
  
  // ──────────────────────────────────────
  // 함수 2: 아이템 착용
  // ──────────────────────────────────────
  equipItem: (item) => {
    set((state) => ({
      equippedItems: {
        ...state.equippedItems,
        [item.category]: item, // category를 키로 사용
      },
    }));
    
    console.log('✅ 착용:', item.name);
  },
  
  // ──────────────────────────────────────
  // 함수 3: 아이템 해제
  // ──────────────────────────────────────
  unequipItem: (category) => {
    set((state) => {
      const newEquipped = { ...state.equippedItems };
    //   delete newEquipped[category];
      return { equippedItems: newEquipped };
    });
    
    console.log('✅ 해제:', category);
  },
  
  // ──────────────────────────────────────
  // 함수 4: 애니메이션 변경
  // ──────────────────────────────────────
  setAnimation: (animation) => {
    set({ currentAnimation: animation });
    console.log('✅ 애니메이션 변경:', animation);
  },
  
  // ──────────────────────────────────────
  // 함수 5: 캐시 유효성 검사
  // ──────────────────────────────────────
  isCacheValid: () => {
    const { lastLoadTime } = get();
    
    // 데이터를 불러온 적이 없으면 무효
    if (!lastLoadTime) return false;
    
    // 5분(300,000ms) 이내면 유효
    const CACHE_DURATION = 5 * 60 * 1000;
    const isValid = Date.now() - lastLoadTime < CACHE_DURATION;
    
    console.log('🔍 캐시 유효성:', isValid);
    return isValid;
  },
  
  // ──────────────────────────────────────
  // 함수 6: 초기화
  // ──────────────────────────────────────
  reset: () => {
    set({
      inventoryItems: [],
      equippedItems: {},
      currentAnimation: 'walk',
      lastLoadTime: null,
    });
    
    console.log('🔄 Store 초기화 완료');
  },
}));