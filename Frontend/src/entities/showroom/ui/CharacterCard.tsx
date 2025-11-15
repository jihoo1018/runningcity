import { MyOffice } from "../model/type";
import { metersToKm, formatPace } from "@/shared/lib/format";

type Props = { tab: "me" | "friend" | "global"; data: MyOffice };

export const CharacterCard = ({ tab, data }: Props) => {
  return (
    <div className="relative mx-auto mt-2 w-full max-w-[360px] px-3 sm:max-w-[380px] sm:px-0 md:max-w-[420px]">
      {/* 칩 프레임 */}
      <div className="chip-frame relative w-full rounded-xl p-[2px]">
        {/* 내부 글래스 패널 */}
        <div className="rounded-xl border border-[#5bd0ff]/30 bg-[#0c101c]/70 px-4 py-6 shadow-[0_0_15px_rgba(60,160,255,0.35)] backdrop-blur-md sm:px-5">
          {/* 상단 구역 */}
          <div className="flex items-center justify-between">
            <div className="text-xs font-semibold tracking-[0.15em] text-[#67e8f9] sm:text-sm">
              LV {data.userLv}
            </div>
            <div className="text-[10px] text-gray-400 sm:text-xs">UID: {data.userId}</div>
          </div>

          {/* 이미지 패널 */}
          <div className="relative mt-4 h-48 overflow-hidden rounded-lg border border-[#70f3ff]/40 bg-gradient-to-b from-[#182235] to-[#0b0f18] shadow-[0_0_20px_rgba(0,200,255,0.3)] sm:h-56 md:h-64">
            <div className="hologram absolute inset-0 opacity-40" />
            <div className="flex h-full items-center justify-center text-sm text-gray-500">
              캐릭터 이미지
            </div>
          </div>

          {/* 이름 */}
          <div className="mt-4 text-lg font-bold tracking-wide text-white sm:text-xl">
            {data.userNm}
          </div>

          {/* 구분선 */}
          <div className="mt-3 h-[1px] w-full bg-gradient-to-r from-transparent via-[#5bd0ff] to-transparent opacity-50"></div>

          {/* 정보 패널 */}
          <div className="mt-4 grid grid-cols-2 gap-4 text-sm sm:grid-cols-2 sm:text-base">
            <Stat label="총 러닝" value={`${metersToKm(data.totalDist)}`} />
            <Stat label="최장 거리" value={`${metersToKm(data.longestDist)}`} />
            <Stat label="평균 페이스" value={formatPace(data.avgPace)} />
            <Stat label="최고 기록" value={formatPace(data.bestPace)} />
            <Stat label="잠입" value={`${data.totalEntryCnt}회`} />
          </div>
        </div>
      </div>
    </div>
  );
};

const Stat = ({ label, value }: { label: string; value: string }) => (
  <div className="flex flex-col">
    <span className="text-xs text-gray-400 sm:text-sm">{label}</span>
    <span className="font-medium text-white">{value}</span>
  </div>
);
