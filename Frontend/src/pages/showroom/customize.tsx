// src\pages\showroom\customize.tsx

import { useState, useEffect } from "react";
import { CustomizeLayout } from "@/entities/showroom/ui/CustomizeLayout";
import { ItemGrid } from "@/entities/showroom/ui/ItemGrid";
import { fetchGetInventoryList } from "@/entities/showroom/api/customize";
import { InventoryItem, EquippedItem } from "@/entities/showroom/model/type";

const CustomizePage = () => {
  const [mode, setMode] = useState<"clothes" | "character">("clothes");
  const [tab, setTab] = useState("상의");
  const [page, setPage] = useState(0);

  const [inventoryItems, setInventoryItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [equippedItems, setEquippedItems] = useState<EquippedItem[]>([
    {
      equippedId: 1,
      itemId: 1,
      style: null, // head 파츠만 style 존재
      category: "bodies",
      subcategory: "male",
      basePath: "\\spritesheets\\bodies\\male\\{animation}\\light.png",
    },
    {
      equippedId: 2,
      itemId: 2,
      style: null, // head 파츠만 style 존재
      category: "clothes",
      subcategory: "longsleeve",
      basePath: "\\spritesheets\\clothes\\male\\longsleeve\\{animation}\\white.png",
    },
    {
      equippedId: 3,
      itemId: 3,
      category: "head",
      subcategory: "eyes",
      style: "anger",
      basePath: "\\spritesheets\\head\\eyes\\anger\\{animation}\\blue.png",
    },
    {
      equippedId: 4,
      itemId: 4,
      style: null, // head 파츠만 style 존재
      category: "hair",
      subcategory: "long",
      basePath: "\\spritesheets\\hair\\buzzcut\\{animation}\\dark_gray.png",
    },
    {
      equippedId: 5,
      itemId: 5,
      style: null, // head 파츠만 style 존재
      category: "clothes",
      subcategory: "shorts",
      basePath: "\\spritesheets\\clothes\\male\\shorts\\{animation}\\black.png",
    },
  ]);

  useEffect(() => {
    const load = async () => {
      try {
        const items = await fetchGetInventoryList();
        setInventoryItems(items);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  // 탭 목록은 mode에 따라 변경됨
  const clothesTabs = ["상의", "하의", "신발"];
  const characterTabs = ["머리", "눈", "머리색"];

  const tabs = mode === "clothes" ? clothesTabs : characterTabs;

  // 테스트용 mock 데이터
  // const mockItems = [
  //   "아이템1",
  //   "아이템2",
  //   "아이템3",
  //   "아이템4",
  //   "아이템5",
  //   "아이템6",
  //   "아이템7",
  //   "아이템8",
  // ];

  // 변환
  function convertToEquipped(item: InventoryItem): EquippedItem {
    return {
      equippedId: 0, // 새로 장착한거니까 임시값(서버 저장 후 서버값으로 업데이트)
      itemId: item.itemId,
      category: item.category,
      subcategory: item.subcategory,
      style: item.style ?? null,
      basePath: item.basePath,
    };
  }

  function getClothesSlot(item: { subcategory: string }) {
    if (item.subcategory === "shorts") return "bottom";

    // 상의 전부
    if (["longsleeve", "tshirt", "shortsleeves"].includes(item.subcategory)) {
      return "top";
    }

    return "other"; // 확장용
  }

  const handleEquip = (invItem: InventoryItem) => {
    const eqItem: EquippedItem = {
      equippedId: 0,
      itemId: invItem.itemId,
      category: invItem.category,
      subcategory: invItem.subcategory,
      style: invItem.style ?? null,
      basePath: invItem.basePath,
    };

    setEquippedItems((prev) => {
      // HEAD
      if (eqItem.category === "head") {
        return [
          ...prev.filter((i) => !(i.category === "head" && i.subcategory === eqItem.subcategory)),
          eqItem,
        ];
      }

      // CLOTHES
      if (eqItem.category === "clothes") {
        const newSlot = getClothesSlot(invItem);

        return [
          ...prev.filter((i) => {
            if (i.category !== "clothes") return true;
            return getClothesSlot(i) !== newSlot;
          }),
          eqItem,
        ];
      }

      // BODIES / HAIR
      return [...prev.filter((i) => i.category !== eqItem.category), eqItem];
    });
  };

  //탭별 필터링
  function filterItemsByTab(items: InventoryItem[]) {
    if (mode === "clothes") {
      if (tab === "상의") {
        return items.filter((i) => getClothesSlot(i) === "top");
      }
      if (tab === "하의") {
        return items.filter((i) => getClothesSlot(i) === "bottom");
      }
    }

    if (mode === "character") {
      if (tab === "머리") return items.filter((i) => i.category === "hair");
      if (tab === "눈") return items.filter((i) => i.subcategory === "eyes");
      if (tab === "머리색") return items.filter((i) => i.category === "hair");
    }

    return items;
  }

  return (
    <CustomizeLayout
      mode={mode}
      setMode={setMode}
      tab={tab}
      setTab={setTab}
      tabList={tabs}
      equippedItems={equippedItems}
      onBack={() => history.back()}
    >
      <ItemGrid
        items={filterItemsByTab(inventoryItems).slice(page * 8, page * 8 + 8)}
        onPrev={() => setPage((prev) => Math.max(prev - 1, 0))}
        onNext={() => setPage((prev) => prev + 1)}
        onSelect={handleEquip}
      />
    </CustomizeLayout>
  );
};

export default CustomizePage;
