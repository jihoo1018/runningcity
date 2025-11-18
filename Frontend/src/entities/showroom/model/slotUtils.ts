// src/entities/showroom/model/slotUtils.ts
import { EquippedItem, InventoryItem } from "@/entities/showroom/model/type";
import { defaultEquippedItems } from "@/entities/showroom/model/defaultAvatar";

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

// InventoryItem → EquippedItem
export function toEquipped(inv: InventoryItem): EquippedItem {
  return {
    equippedId: 0,
    itemId: inv.itemId,
    category: inv.category,
    subcategory: inv.subcategory,
    style: inv.style ?? null,
    basePath: inv.basePath,
    spriteType: inv.spriteType,
  };
}

// 서버 배열 → 슬롯 구조 변환
export function mapArrayToSlots(list: EquippedItem[]): AvatarSlots {
  const slots = structuredClone(emptySlots);

  for (const item of list) {
    if (item.category === "bodies") {
      slots.bodies = item;
      continue;
    }
    if (item.category === "clothes" && item.subcategory) {
      if (["tshirt", "longsleeve", "shortsleeves"].includes(item.subcategory))
        slots.clothes_top = item;
      else if (item.subcategory === "shorts") slots.clothes_bottom = item;
      continue;
    }
    if (item.category === "hair") {
      slots.hair = item;
      continue;
    }
    if (item.category === "head" && item.subcategory) {
      if (item.subcategory in slots) {
        slots[item.subcategory as keyof AvatarSlots] = item;
      }
      continue;
    }
  }

  return slots;
}

// 슬롯 → 배열 (서버 저장용)
export function slotsToArray(slots: AvatarSlots): EquippedItem[] {
  return Object.values(slots).filter((v): v is EquippedItem => v !== null);
}

// 기본값 병합
export function mergeWithDefaults(slots: AvatarSlots): AvatarSlots {
  const merged = structuredClone(slots);

  for (const def of defaultEquippedItems) {
    if (def.category === "bodies") {
      merged.bodies = merged.bodies ?? def;
      continue;
    }

    const key = def.subcategory as keyof AvatarSlots;

    if (!merged[key]) merged[key] = def;
  }

  return merged;
}
