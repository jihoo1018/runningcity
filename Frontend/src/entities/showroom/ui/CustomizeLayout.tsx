// src\entities\showroom\ui\CustomizeLayout.tsx
import React from "react";
import { BackIconButton, CloseIconButton } from "@/shared/ui/IconButtons";
import { LPCCharacterRenderer } from "@/entities/showroom/ui/LPCCharacterRenderer";
import { EquippedItem } from "@/entities/showroom/model/type";
import { fetchGetInventoryList } from "@/entities/showroom/api/customize";

type Props = {
  mode: "clothes" | "character";
  setMode: (mode: "clothes" | "character") => void;
  tab: string;
  setTab: (tab: string) => void;
  tabList: string[];
  equippedItems: EquippedItem[];
  children: React.ReactNode;
  onBack: () => void;
};

export const CustomizeLayout = ({
  mode,
  setMode,
  tab,
  setTab,
  tabList,
  equippedItems,
  children,
  onBack,
}: Props) => {
  return (
    <div className="mx-auto flex w-full max-w-[480px] flex-col sm:px-6">
      {/* 상단 */}
      <div className="flex items-center justify-between">
        <BackIconButton variant="ghost" onClick={onBack} />

        <div className="flex gap-3 sm:gap-4">
          {/* 옷 */}
          <button
            className={`h-10 w-10 rounded-xl text-sm sm:h-12 sm:w-12 sm:text-base ${
              mode === "clothes" ? "bg-primary text-black" : "border border-gray-500 text-white"
            }`}
            onClick={() => {
              setMode("clothes");
              setTab("상의");
            }}
          >
            옷
          </button>

          {/* 얼굴 */}
          <button
            className={`h-10 w-10 rounded-xl text-sm sm:h-12 sm:w-12 sm:text-base ${
              mode === "character" ? "bg-primary text-black" : "border border-gray-500 text-white"
            }`}
            onClick={() => {
              setMode("character");
              setTab("머리");
            }}
          >
            얼굴
          </button>
        </div>
      </div>

      {/* 캐릭터 프리뷰 */}
      <div className="flex justify-center py-4">
        <LPCCharacterRenderer items={equippedItems} animation="walk" direction={0} />
        {/* <div className="flex h-80 w-50 items-center justify-center rounded-xl bg-gray-200 sm:h-80 sm:w-40 md:h-[360px] md:w-48">
          캐릭터
        </div> */}
      </div>

      {/* 탭 – 모바일에서 스크롤 가능 */}
      <div className="scrollbar-hide flex justify-start gap-2 overflow-x-auto sm:justify-around">
        {tabList.map((item) => (
          <button
            key={item}
            className={`rounded px-2 py-2 whitespace-nowrap text-white ${
              tab === item ? "bg-primary" : "border-custom-gray border"
            }`}
            onClick={() => setTab(item)}
          >
            {item}
          </button>
        ))}
      </div>

      {/* 콘텐츠 */}
      <div className="mt-2 flex-1">{children}</div>
    </div>
  );
};
