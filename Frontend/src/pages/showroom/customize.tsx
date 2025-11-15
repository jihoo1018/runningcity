// src\pages\showroom\customize.tsx

import { useState } from "react";
import { CustomizeLayout } from "@/entities/showroom/ui/CustomizeLayout";
import { ItemGrid } from "@/entities/showroom/ui/ItemGrid";

const CustomizePage = () => {
  const [mode, setMode] = useState<"clothes" | "character">("clothes");
  const [tab, setTab] = useState("상의");
  const [page, setPage] = useState(0);

  // 탭 목록은 mode에 따라 변경됨
  const clothesTabs = ["상의", "하의", "신발"];
  const characterTabs = ["머리", "눈", "머리색"];

  const tabs = mode === "clothes" ? clothesTabs : characterTabs;

  // 테스트용 mock 데이터
  const mockItems = [
    "아이템1",
    "아이템2",
    "아이템3",
    "아이템4",
    "아이템5",
    "아이템6",
    "아이템7",
    "아이템8",
  ];

  return (
    <CustomizeLayout
      mode={mode}
      setMode={setMode}
      tab={tab}
      setTab={setTab}
      tabList={tabs}
      onBack={() => history.back()}
    >
      <ItemGrid
        items={mockItems.slice(page * 6, page * 6 + 6)}
        onPrev={() => setPage((prev) => Math.max(prev - 1, 0))}
        onNext={() => setPage((prev) => prev + 1)}
      />
    </CustomizeLayout>
  );
};

export default CustomizePage;
