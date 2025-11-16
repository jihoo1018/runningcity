/**
 * 🎨 AvatarStore – 슬롯 기반 착장 저장소 (완전 통합 버전)
 */
import { create } from "zustand";
import { EquippedItem, InventoryItem } from "@/entities/showroom/model/type";

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
 * EquippedItem[] → 슬롯 구조
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

    if (category === "head") {
      if (subcategory in slots) {
        slots[subcategory as keyof AvatarSlots] = item;
      }
    }
  }

  return slots;
}

/* -------------------------------------------------------
 * 슬롯 구조 → EquippedItem[]
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
  animation: "walk" | "run";

  setInventory: (list: InventoryItem[]) => void;
  syncFromServer: (list: EquippedItem[]) => void;
  equip: (inv: InventoryItem) => void;
  toArray: () => EquippedItem[];
}

export const useAvatarStore = create<AvatarStore>((set, get) => ({
  inventory: [],
  slots: emptySlots,
  animation: "walk",

  setInventory: (list) => set({ inventory: list }),

  syncFromServer: (list) => {
    set({ slots: mapArrayToSlots(list) });
    console.log("📥 서버 착장 동기화:", list);
  },

  equip: (inv) => {
    const eq = toEquipped(inv);
    const prev = get().slots;
    const newSlots = { ...prev };

    if (inv.category === "bodies") newSlots.bodies = eq;

    if (inv.category === "clothes") {
      if (["tshirt", "longsleeve", "shortsleeves"].includes(inv.subcategory))
        newSlots.clothes_top = eq;

      if (inv.subcategory === "shorts") newSlots.clothes_bottom = eq;
    }

    if (inv.category === "hair") newSlots.hair = eq;

    if (inv.category === "head" && inv.subcategory in newSlots)
      newSlots[inv.subcategory as keyof AvatarSlots] = eq;

    set({ slots: newSlots });
    console.log("👕 착장:", eq);
  },

  toArray: () => slotsToArray(get().slots),
}));
