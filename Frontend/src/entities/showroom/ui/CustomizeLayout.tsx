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
                ? "bg-cyan-400 text-black shadow-[0_0_10px_rgba(0,255,255,0.8)]"
                : "bg-section-bg border-custom-gray text-custom-gray border hover:border-cyan-300"
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
                ? "bg-cyan-400 text-black shadow-[0_0_10px_rgba(0,255,255,0.8)]"
                : "bg-section-bg border-custom-gray text-custom-gray border hover:border-cyan-300"
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
      <div className="m-10 flex flex-none scale-150 justify-center">
        <LPCCharacterRenderer items={equippedItems} direction={2} animation="walk" />
      </div>

      {/* 탭 리스트 */}
      <div className="scrollbar-hide flex flex-none flex-nowrap gap-2 overflow-x-auto px-2 pb-2">
        {tabList.map((t) => (
          <button
            key={t}
            className={`text-button flex-shrink-0 rounded px-4 py-2 whitespace-nowrap ${
              tab === t
                ? "bg-cyan-400 text-black shadow-[0_0_10px_rgba(0,255,255,0.8)]"
                : "border-custom-gray bg-section-bg text-custom-gray border hover:border-cyan-300"
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
      <div className="mt-4 flex-none px-2 py-4">
        <button
          onClick={onSave}
          className="bg-primary mt-2 w-full rounded-xl from-cyan-400 to-cyan-600 py-3 text-center font-semibold text-black shadow-[0_0_15px_rgba(0,255,255,0.5)] transition-all hover:shadow-[0_0_25px_rgba(0,255,255,0.8)]"
        >
          저장하기
        </button>
      </div>
    </div>
  );
};
