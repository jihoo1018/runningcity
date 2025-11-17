// src/features/avatar/model/useAvatarStore.ts
import { create } from "zustand";
import {
  AvatarSlots,
  emptySlots,
  mapArrayToSlots,
  mergeWithDefaults,
  toEquipped,
  slotsToArray,
} from "@/entities/showroom/model/slotUtils";
import { EquippedItem, InventoryItem } from "@/entities/showroom/model/type";

interface AvatarStore {
  inventory: InventoryItem[];
  slots: AvatarSlots;

  setInventory: (list: InventoryItem[]) => void;
  syncFromServer: (serverList: EquippedItem[]) => void;
  equip: (inv: InventoryItem) => void;
  toArray: () => EquippedItem[];
}

export const useAvatarStore = create<AvatarStore>((set, get) => ({
  inventory: [],
  slots: emptySlots,

  setInventory: (list) => set({ inventory: list }),

  syncFromServer: (serverList) => {
    const serverSlots = mapArrayToSlots(serverList);
    const merged = mergeWithDefaults(serverSlots);

    set({ slots: merged });
    console.log("📥 서버 동기화 완료:", merged);
  },

  equip: (inv) => {
    const slots = { ...get().slots };
    const eq = toEquipped(inv);

    if (inv.category === "bodies") {
      slots.bodies = eq;
    } else if (inv.category === "clothes") {
      if (["tshirt", "longsleeve", "shortsleeves"].includes(inv.subcategory))
        slots.clothes_top = eq;
      else if (inv.subcategory === "shorts") slots.clothes_bottom = eq;
    } else if (inv.category === "hair") {
      slots.hair = eq;
    } else if (inv.category === "head") {
      if (inv.subcategory in slots) {
        slots[inv.subcategory as keyof AvatarSlots] = eq;
      }
    }

    set({ slots });
  },

  toArray: () => slotsToArray(get().slots),
}));
