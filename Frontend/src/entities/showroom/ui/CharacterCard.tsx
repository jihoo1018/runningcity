import { LPCCharacterRenderer } from "./LPCCharacterRenderer";
import { useAvatarStore, slotsToArray } from "@/features/avatar/model/avatarStore";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import type { MyOffice } from "@/entities/showroom/model/type";

type Props = {
  tab: "me" | "friend" | "global";
  data?: MyOffice;
};

export const CharacterCard = ({ tab, data }: Props) => {
  const user = useAuthStore((s) => s.user);
  const slots = useAvatarStore((s) => s.slots);

  const equippedArray = slotsToArray(slots);

  return (
    <div className="relative mx-auto mt-2 w-full max-w-[360px] px-3">
      <div className="chip-frame relative w-full rounded-xl p-[2px]">
        <div className="rounded-xl border border-[#5bd0ff]/30 bg-[#0c101c]/70 px-4 py-6">
          <div className="flex items-center justify-between">
            <div className="text-xs font-semibold text-[#67e8f9]">LV {user?.level ?? 1}</div>
            <div className="text-[10px] text-gray-400">UID: {user?.userId}</div>
          </div>

          <div className="relative mt-4 h-48 overflow-hidden rounded-lg border border-[#70f3ff]/40">
            <div className="flex h-full items-center justify-center">
              <LPCCharacterRenderer items={equippedArray} direction={2} />
            </div>
          </div>

          <div className="mt-4 text-lg font-bold text-white">
            {user?.nickname ?? "러닝시티 유저"}
          </div>

          {tab === "me" && data && (
            <div className="mt-4 grid grid-cols-2 gap-4 text-sm">
              <div>총 러닝: {data.totalDist}</div>
              <div>최장 거리: {data.longestDist}</div>
              <div>평균 페이스: {data.avgPace}</div>
              <div>최고 기록: {data.bestPace}</div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
