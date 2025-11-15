// src/pages/showroom/customize.tsx

import { useEffect, useState } from "react";
import { CustomizeLayout } from "@/entities/showroom/ui/CustomizeLayout";
import { ItemGrid } from "@/entities/showroom/ui/ItemGrid";
import { fetchGetInventoryList, fetchSave } from "@/entities/showroom/api/customize";
import { EquippedItem, InventoryItem } from "@/entities/showroom/model/type";

const BODY_TABS = ["상의", "하의", "피부색"];
const HEAD_TABS = ["헤어", "얼굴", "표정", "눈썹", "눈", "코", "귀"];

export default function CustomizePage() {
  const [mode, setMode] = useState<"body" | "head">("body");
  const [tab, setTab] = useState("상의");
  const [page, setPage] = useState(0);

  const [inventoryItems, setInventoryItems] = useState<InventoryItem[]>([]);
  const [equippedItems, setEquippedItems] = useState<EquippedItem[]>([
    // 기본 장착 아이템 (기존 네가 사용하던 부분)
    {
      equippedId: 0,
      itemId: 0,
      category: "bodies",
      subcategory: "male",
      style: null,
      basePath: "\\spritesheets\\bodies\\male\\{animation}\\light.png",
    },
    {
      equippedId: 0,
      itemId: 0,
      category: "head",
      subcategory: "heads",
      style: null,
      basePath: "\\spritesheets\\head\\heads\\{animation}\\light.png",
    },
    {
      equippedId: 0,
      itemId: 0,
      category: "head",
      subcategory: "eyes",
      style: "default",
      basePath: "\\spritesheets\\head\\eyes\\default\\{animation}\\blue.png",
    },
    {
      equippedId: 0,
      itemId: 0,
      category: "head",
      subcategory: "nose",
      style: null,
      basePath: "\\spritesheets\\head\\nose\\{animation}\\light.png",
    },
  ]);

  useEffect(() => {
    fetchGetInventoryList().then(setInventoryItems);
  }, []);

  // --- 슬롯 규칙 ---
  const BODY_SLOT: Record<string, string> = {
    tshirt: "top",
    longsleeve: "top",
    shorts: "bottom",
    male: "skin",
    female: "skin",
  };

  const HEAD_SLOT: Record<string, string> = {
    hair: "hair",
    heads: "head",
    faces: "face",
    eyebrows: "eyebrows",
    eyes: "eyes",
    nose: "nose",
    ears: "ears",
  };

  function toEquipped(item: InventoryItem): EquippedItem {
    return {
      equippedId: 0,
      itemId: item.itemId,
      category: item.category,
      subcategory: item.subcategory,
      style: item.style ?? null,
      basePath: item.basePath,
    };
  }

  // --- 장착 처리 ---
  function handleEquip(inv: InventoryItem) {
    const eq = toEquipped(inv);

    setEquippedItems((prev) => {
      if (mode === "body") {
        const slot = BODY_SLOT[inv.subcategory];
        return [...prev.filter((i) => BODY_SLOT[i.subcategory] !== slot), eq];
      }

      if (mode === "head") {
        const slot = HEAD_SLOT[inv.subcategory];
        return [...prev.filter((i) => HEAD_SLOT[i.subcategory] !== slot), eq];
      }

      return prev;
    });
  }

  function tabToSub(tab: string) {
    return (
      {
        헤어: "hair",
        얼굴: "heads",
        표정: "faces",
        눈썹: "eyebrows",
        눈: "eyes",
        코: "nose",
        귀: "ears",
      } as any
    )[tab];
  }

  function filterItems() {
    if (mode === "body") {
      if (tab === "상의")
        return inventoryItems.filter((i) => ["tshirt", "longsleeve"].includes(i.subcategory));
      if (tab === "하의") return inventoryItems.filter((i) => i.subcategory === "shorts");
      if (tab === "피부색") return inventoryItems.filter((i) => i.category === "bodies");
    }

    if (mode === "head") return inventoryItems.filter((i) => i.subcategory === tabToSub(tab));

    return inventoryItems;
  }

  // --- 저장 ---
  async function handleSave() {
    try {
      await fetchSave(equippedItems);
      alert("착장이 저장되었습니다!");
    } catch (err: any) {
      console.error(err);
      alert("저장 중 오류가 발생했습니다.");
    }
  }

  return (
    <CustomizeLayout
      mode={mode}
      setMode={setMode}
      tab={tab}
      setTab={setTab}
      tabList={mode === "body" ? BODY_TABS : HEAD_TABS}
      equippedItems={equippedItems}
      onBack={() => history.back()}
    >
      <ItemGrid
        items={filterItems().slice(page * 8, page * 8 + 8)}
        onPrev={() => setPage((p) => Math.max(0, p - 1))}
        onNext={() => setPage((p) => p + 1)}
        onSelect={handleEquip}
      />

      {/* 저장 버튼 */}
      <button
        className="bg-primary mt-4 w-full rounded-xl py-3 font-bold text-black"
        onClick={handleSave}
      >
        저장하기
      </button>
    </CustomizeLayout>
  );
}
