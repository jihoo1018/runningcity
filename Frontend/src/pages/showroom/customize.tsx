import { useEffect, useState } from "react";
import { CustomizeLayout } from "@/entities/showroom/ui/CustomizeLayout";
import { ItemGrid } from "@/entities/showroom/ui/ItemGrid";
import {
  fetchGetInventoryList,
  fetchGetEquippedItems,
  fetchSave,
} from "@/entities/showroom/api/customize";
import { useAvatarStore } from "@/features/avatar/model/avatarStore";
import { slotsToArray } from "@/entities/showroom/model/slotUtils";

const BODY_TABS = ["상의", "하의", "피부색"];
const HEAD_TABS = ["헤어", "얼굴", "표정", "눈썹", "눈", "코", "귀"];

export default function CustomizePage() {
  const [mode, setMode] = useState<"body" | "head">("body");
  const [tab, setTab] = useState("상의");
  const [page, setPage] = useState(0);

  const slots = useAvatarStore((s) => s.slots);
  const inventory = useAvatarStore((s) => s.inventory);
  const setInventory = useAvatarStore((s) => s.setInventory);
  const equip = useAvatarStore((s) => s.equip);
  const sync = useAvatarStore((s) => s.syncFromServer);
  const toArray = useAvatarStore((s) => s.toArray);

  // 인벤토리 / 서버착장 불러오기
  useEffect(() => {
    fetchGetInventoryList().then(setInventory);
    fetchGetEquippedItems().then(sync);
  }, []);

  // 탭 → subcategory 변환
  function tabToSub(tab: string) {
    return {
      헤어: "hair",
      얼굴: "heads",
      표정: "faces",
      눈썹: "eyebrows",
      눈: "eyes",
      코: "nose",
      귀: "ears",
    }[tab];
  }

  // 인벤토리 필터링
  function filterItems() {
    if (mode === "body") {
      if (tab === "상의")
        return inventory.filter((i) => ["tshirt", "longsleeve"].includes(i.subcategory));
      if (tab === "하의") return inventory.filter((i) => i.subcategory === "shorts");
      if (tab === "피부색") return inventory.filter((i) => i.category === "bodies");
    }

    if (mode === "head") {
      if (tab === "헤어") return inventory.filter((i) => i.category === "hair");
      return inventory.filter((i) => i.subcategory === tabToSub(tab));
    }

    return inventory;
  }

  async function handleSave() {
    const payload = toArray();
    await fetchSave(payload);
    alert("저장되었습니다!");
  }

  return (
    <CustomizeLayout
      mode={mode}
      setMode={setMode}
      tab={tab}
      setTab={setTab}
      tabList={mode === "body" ? BODY_TABS : HEAD_TABS}
      equippedItems={slotsToArray(slots)}
      onBack={() => history.back()}
    >
      <ItemGrid
        items={filterItems().slice(page * 8, page * 8 + 8)}
        onPrev={() => setPage((p) => Math.max(0, p - 1))}
        onNext={() => setPage((p) => p + 1)}
        onSelect={equip}
      />

      <button
        className="bg-primary mt-4 w-full rounded-xl py-3 font-bold text-black"
        onClick={handleSave}
      >
        저장하기
      </button>
    </CustomizeLayout>
  );
}
