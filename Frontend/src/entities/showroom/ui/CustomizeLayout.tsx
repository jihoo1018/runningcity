// src/entities/showroom/ui/CustomizeLayout.tsx
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
  onSave: () => void;
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
  onSave,
}: Props) => {
  return (
    <div className="flex h-full w-full flex-col">
      {/* <div
       className="mx-auto flex max-w-[480px] flex-col overflow-hidden"
       style={{ height: "calc(100vh - 64px)" }} // 👈 하단 nav 제외한 높이
     >*/}
      {/* 상단 UI */}
      <div className="flex flex-none items-center justify-between px-2 py-3">
        <BackIconButton onClick={onBack} />

        <div className="flex gap-3">
          <button
            className={`text-button rounded-xl px-4 py-2 ${
              mode === "body"
                ? "bg-primary text-custom-black"
                : "border-custom-gray text-custom-white border"
            }`}
            onClick={() => {
              setMode("body");
              setTab("상의");
            }}
          >
            몸
          </button>

          <button
            className={`text-button rounded-xl px-4 py-2 ${
              mode === "head"
                ? "bg-primary text-custom-black"
                : "border-custom-gray text-custom-white border"
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

      {/* 캐릭터 */}
      <div className="flex flex-none justify-center py-3">
        <LPCCharacterRenderer items={equippedItems} direction={2} animation="walk" />
      </div>

      {/* 탭 리스트 */}
      <div className="scrollbar-hide flex flex-none gap-2 overflow-x-auto px-2 pb-2">
        {tabList.map((t) => (
          <button
            key={t}
            className={`text-button rounded px-3 py-2 ${
              tab === t
                ? "bg-primary text-custom-black"
                : "border-custom-gray text-custom-white border"
            }`}
            onClick={() => setTab(t)}
          >
            {t}
          </button>
        ))}
      </div>

      {/* 아이템 그리드 — 남은 공간을 모두 차지 */}
      {/* <div className="overflow-auto">{children}</div> */}
      {/* 아이템 그리드 영역 고정 높이 */}
      <div className="relative flex-none" style={{ height: "240px" }}>
        {children}
      </div>

      {/* 저장하기 버튼 */}
      {/* <div className="flex-none px-2 py-4">
        <button
          className="border-primary text-button w-full rounded-xl border py-3"
          onClick={onSave}
        >
          저장하기
        </button>
      </div> */}

      {/* 저장하기 버튼 */}
      <div className="mt-15 flex-none px-2 py-4">
        <button
          onClick={onSave}
          className="border-primary text-button bg-section-bg w-full rounded-xl border py-3"
        >
          저장하기
        </button>
      </div>
    </div>
  );
};
