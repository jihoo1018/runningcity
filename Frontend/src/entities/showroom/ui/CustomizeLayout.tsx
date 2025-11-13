// src\entities\showroom\ui\CustomizeLayout.tsx

import React from "react";

type Props = {
  mode: "clothes" | "character";
  setMode: (mode: "clothes" | "character") => void;
  tab: string;
  setTab: (tab: string) => void;
  tabList: string[];
  children: React.ReactNode;
  onBack: () => void;
};

export const CustomizeLayout = ({
  mode,
  setMode,
  tab,
  setTab,
  tabList,
  children,
  onBack,
}: Props) => {
  return (
    <div className="flex min-h-screen flex-col bg-white">
      {/* 상단 */}
      <div className="flex justify-between p-4">
        <button className="h-20 w-20 rounded-full bg-gray-200" onClick={onBack}>
          뒤로가기
        </button>

        <div className="flex gap-4">
          {/* 옷 버튼 → mode 변경 */}
          <button
            className={`h-20 w-20 rounded-full ${
              mode === "clothes" ? "bg-gray-800 text-white" : "bg-gray-200"
            }`}
            onClick={() => {
              setMode("clothes");
              setTab("상의"); // 기본 탭
            }}
          >
            옷
          </button>

          {/* 캐릭터 버튼 → mode 변경 */}
          <button
            className={`h-20 w-20 rounded-full ${
              mode === "character" ? "bg-gray-800 text-white" : "bg-gray-200"
            }`}
            onClick={() => {
              setMode("character");
              setTab("머리"); // 기본 탭
            }}
          >
            캐릭터
          </button>
        </div>
      </div>

      {/* 캐릭터 프리뷰 영역 */}
      <div className="flex justify-center py-4">
        <div className="flex h-80 w-40 items-center justify-center rounded-xl bg-gray-200">
          캐릭터
        </div>
      </div>

      {/* 탭 */}
      <div className="flex justify-around bg-gray-200 p-2">
        {tabList.map((item) => (
          <button
            key={item}
            className={`rounded bg-gray-600 px-4 py-2 text-white ${
              tab === item ? "opacity-100" : "opacity-50"
            }`}
            onClick={() => setTab(item)}
          >
            {item}
          </button>
        ))}
      </div>

      {/* 아이템 영역 */}
      <div className="flex-1 bg-gray-200 p-4">{children}</div>
    </div>
  );
};
