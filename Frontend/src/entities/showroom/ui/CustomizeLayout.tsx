// src/entities/showroom/ui/CustomizeLayout.tsx
import React from "react";
import { BackIconButton } from "@/shared/ui/IconButtons";
import { LPCCharacterRenderer } from "@/entities/showroom/ui/LPCCharacterRenderer";
import { EquippedItem } from "@/entities/showroom/model/type";

type Props = {
  mode: "body" | "head";
  setMode: (m: "body" | "head") => void;
  tab: string;
  setTab: (t: string) => void;
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
    <div className="mx-auto flex max-w-[480px] flex-col">
      {/* 상단 버튼 */}
      <div className="flex items-center justify-between">
        <BackIconButton onClick={onBack} />

        <div className="flex gap-3">
          <button
            className={`rounded-xl px-4 py-2 ${
              mode === "body" ? "bg-primary text-black" : "border border-gray-500 text-white"
            }`}
            onClick={() => {
              setMode("body");
              setTab("상의");
            }}
          >
            몸
          </button>

          <button
            className={`rounded-xl px-4 py-2 ${
              mode === "head" ? "bg-primary text-black" : "border border-gray-500 text-white"
            }`}
            onClick={() => {
              setMode("head");
              setTab("헤어");
            }}
          >
            머리
          </button>
        </div>
      </div>

      {/* 캐릭터 프리뷰 */}
      <div className="flex justify-center py-4">
        <LPCCharacterRenderer items={equippedItems} animation="walk" direction={2} />
      </div>

      {/* 하위 탭 */}
      <div className="scrollbar-hide flex gap-2 overflow-x-auto">
        {tabList.map((t) => (
          <button
            key={t}
            className={`rounded px-3 py-2 ${
              tab === t ? "bg-primary text-black" : "border border-gray-600 text-white"
            }`}
            onClick={() => setTab(t)}
          >
            {t}
          </button>
        ))}
      </div>

      <div className="mt-3">{children}</div>
    </div>
  );
};
