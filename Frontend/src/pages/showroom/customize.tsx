// src/pages/showroom/customize.tsx
import { useEffect, useState, useMemo } from "react";
import { useModalRouter } from "@/app/modal/useModalRouter";
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
  const { open } = useModalRouter();

  useEffect(() => {
    // Tab 바뀔때마다 page 0으로 초기화
    setPage(0);
  }, [tab]);

  useEffect(() => {
    fetchGetInventoryList().then(setInventory);
    if (Object.keys(slots).length === 0) {
      fetchGetEquippedItems().then(sync);
    }
  }, []);

  /** 🔥 useMemo 안에서 tabToSub + filterItems 정의 */
  const filteredItems = useMemo(() => {
    // COMPLETE 타입은 커스터마이징에서 제외 (완성된 아바타이므로 개별 파츠로 사용 불가)
    const compositeInventory = inventory.filter((i) => i.spriteType !== "COMPLETE");

    const tabToSub = (tab: string) =>
      ({
        헤어: "hair",
        얼굴: "heads",
        표정: "faces",
        눈썹: "eyebrows",
        눈: "eyes",
        코: "nose",
        귀: "ears",
      })[tab];

    if (mode === "body") {
      if (tab === "상의")
        return compositeInventory.filter((i) => ["tshirt", "longsleeve"].includes(i.subcategory));
      if (tab === "하의") return compositeInventory.filter((i) => i.subcategory === "shorts");
      if (tab === "피부색") return compositeInventory.filter((i) => i.category === "bodies");
    }

    if (mode === "head") {
      if (tab === "헤어") return compositeInventory.filter((i) => i.category === "hair");
      return compositeInventory.filter((i) => i.subcategory === tabToSub(tab));
    }

    return compositeInventory;
  }, [inventory, mode, tab]);

  async function handleSave() {
    const payload = toArray();
    await fetchSave(payload);
    open("showroom", "alert", { msg: "현재 착장이 저장되었습니다." });
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
      onSave={handleSave}
    >
      <ItemGrid
        items={filteredItems.slice(page * 6, page * 6 + 6)}
        onPrev={() => setPage((p) => Math.max(0, p - 1))}
        onNext={() =>
          setPage((p) => {
            const maxPage = Math.floor((filteredItems.length - 1) / 6);
            return Math.min(maxPage, p + 1);
          })
        }
        onSelect={equip}
        disablePrev={page === 0}
        disableNext={page >= Math.floor((filteredItems.length - 1) / 6)}
      />
    </CustomizeLayout>
  );
}
