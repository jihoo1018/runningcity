/**
 * 🎨 AvatarStore – 슬롯 기반 착장 저장소 (최종 안정 버전)
 */

import { create } from "zustand";
import { EquippedItem, InventoryItem } from "@/entities/showroom/model/type";
import { defaultEquippedItems } from "@/entities/showroom/model/defaultAvatar";

/* -------------------------------------------------------
 * 🎯 슬롯 타입
 * ----------------------------------------------------- */
export type AvatarSlots = {
  bodies: EquippedItem | null;
  clothes_top: EquippedItem | null;
  clothes_bottom: EquippedItem | null;
  clothes_shoes: EquippedItem | null;

  hair: EquippedItem | null;

  heads: EquippedItem | null;
  faces: EquippedItem | null;
  eyes: EquippedItem | null;
  eyebrows: EquippedItem | null;
  nose: EquippedItem | null;
  ears: EquippedItem | null;
};

/* -------------------------------------------------------
 * 빈 슬롯
 * ----------------------------------------------------- */
export const emptySlots: AvatarSlots = {
  bodies: null,
  clothes_top: null,
  clothes_bottom: null,
  clothes_shoes: null,
  hair: null,
  heads: null,
  faces: null,
  eyes: null,
  eyebrows: null,
  nose: null,
  ears: null,
};

/* -------------------------------------------------------
 * InventoryItem → EquippedItem
 * ----------------------------------------------------- */
export function toEquipped(inv: InventoryItem): EquippedItem {
  return {
    equippedId: 0,
    itemId: inv.itemId,
    category: inv.category,
    subcategory: inv.subcategory,
    style: inv.style ?? null,
    basePath: inv.basePath,
  };
}

/* -------------------------------------------------------
 * EquippedItem[] → 슬롯 구조로 매핑
 * ----------------------------------------------------- */
export function mapArrayToSlots(list: EquippedItem[]): AvatarSlots {
  const slots: AvatarSlots = structuredClone(emptySlots);

  for (const item of list) {
    const { category, subcategory } = item;

    if (category === "bodies") {
      slots.bodies = item;
      continue;
    }

    if (category === "clothes") {
      if (["tshirt", "longsleeve", "shortsleeves"].includes(subcategory)) slots.clothes_top = item;

      if (subcategory === "shorts") slots.clothes_bottom = item;

      continue;
    }

    if (category === "hair") {
      slots.hair = item;
      continue;
    }

    if (category === "head" && subcategory in slots) {
      slots[subcategory as keyof AvatarSlots] = item;
      continue;
    }
  }

  return slots;
}

/* -------------------------------------------------------
 * 기본(default) + 서버 착장 병합
 * ----------------------------------------------------- */
export function mergeSlots(base: AvatarSlots, override: AvatarSlots): AvatarSlots {
  const result = { ...base };

  for (const key of Object.keys(base) as (keyof AvatarSlots)[]) {
    // 🔥 bodies는 기본값 유지 (서버에 없어도 유지)
    if (key === "bodies") continue;

    if (override[key] !== null) {
      result[key] = override[key];
    }
  }

  return result;
}

/* -------------------------------------------------------
 * 슬롯 → 배열 (중복 제거 없음!)
 * ----------------------------------------------------- */
export function slotsToArray(slots: AvatarSlots): EquippedItem[] {
  return Object.values(slots).filter((v): v is EquippedItem => v !== null && v !== undefined);
}

/* -------------------------------------------------------
 * Zustand Store
 * ----------------------------------------------------- */
interface AvatarStore {
  inventory: InventoryItem[];
  slots: AvatarSlots;

  setInventory: (list: InventoryItem[]) => void;
  syncFromServer: (list: EquippedItem[]) => void;
  equip: (inv: InventoryItem) => void;
  toArray: () => EquippedItem[];
}

export const useAvatarStore = create<AvatarStore>((set, get) => ({
  inventory: [],
  slots: emptySlots,

  setInventory: (list) => set({ inventory: list }),

  /** 서버 착장 → 기본과 병합 */
  syncFromServer: (serverList) => {
    const base = mapArrayToSlots(defaultEquippedItems);
    const override = mapArrayToSlots(serverList);
    const merged = mergeSlots(base, override);

    set({ slots: merged });
  },

  /** 착장 변경 */
  equip: (inv) => {
    const eq = toEquipped(inv);
    const prev = get().slots;
    const next = { ...prev };

    if (inv.category === "bodies") next.bodies = eq;

    if (inv.category === "clothes") {
      if (["tshirt", "longsleeve", "shortsleeves"].includes(inv.subcategory)) next.clothes_top = eq;
      if (inv.subcategory === "shorts") next.clothes_bottom = eq;
    }

    if (inv.category === "hair") next.hair = eq;

    if (inv.category === "head" && inv.subcategory in next)
      next[inv.subcategory as keyof AvatarSlots] = eq;

    set({ slots: next });
  },

  toArray: () => slotsToArray(get().slots),
}));
