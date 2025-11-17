import { LPCCharacterRenderer } from "./LPCCharacterRenderer";
import { useAvatarStore } from "@/features/avatar/model/avatarStore";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import type { MyOffice, RandomAvatar } from "@/entities/showroom/model/type";
import { slotsToArray } from "@/entities/showroom/model/slotUtils";
import { getLevelInfo } from "@/entities/user/model/leveling";
import { metersToKm, formatPace, formatDuration } from "@/shared/lib/format";

type Props = {
  tab: "me" | "friend" | "global";
  data?: MyOffice | RandomAvatar;
};

export const CharacterCard = ({ tab, data }: Props) => {
  const user = useAuthStore((s) => s.user);
  const slots = useAvatarStore((s) => s.slots);
  const userLevelInfo = getLevelInfo(user?.totalExp ?? 0);

  // 데이터 타입 구분
  const isMyOffice = data && "equippedItemList" in data;
  const isRandomAvatar = data && "equippedItems" in data;

  // 착장 아이템 결정
  const getEquippedItems = () => {
    if (tab === "me") {
      return slotsToArray(slots); // 내 사무실은 AvatarStore에서
    }
    if (isRandomAvatar) {
      const avatar = data as RandomAvatar;
      // RandomAvatar의 equippedItems를 LPCCharacterRenderer가 받는 형태로 변환
      return avatar.equippedItems.map((item, idx) => ({
        equippedId: idx, // 더미 ID (렌더링에는 영향 없음)
        itemId: item.itemId,
        category: item.category,
        subcategory: item.subcategory,
        style: item.style ?? null,
        basePath: item.basePath,
      }));
    }
    return [];
  };

  const equippedArray = getEquippedItems();

  // 유저 정보 결정
  const displayLevel = isRandomAvatar ? (data as RandomAvatar).level : (userLevelInfo.level ?? 1);

  const displayNickname = isRandomAvatar
    ? (data as RandomAvatar).nickname
    : (user?.nickname ?? "러닝시티 유저");

  const displayUserId = isRandomAvatar ? (data as RandomAvatar).userId : user?.userId;

  return (
    <div className="text-content-bold relative mx-auto mt-2 w-full max-w-[360px] px-3">
      <div className="chip-frame relative w-full rounded-xl p-[2px]">
        <div className="border-primary rounded-xl border bg-[#0c101c]/70 px-4 py-6">
          <div className="flex items-center justify-between">
            <div className="text-button text-content-bold text-[#67e8f9]">LV {displayLevel}</div>
            <div className="text-custom-gray text-[10px]">UID: {displayUserId}</div>
          </div>

          <div className="border-primary/60 relative mt-4 h-48 overflow-hidden rounded-lg border">
            <div className="flex h-full items-center justify-center">
              <LPCCharacterRenderer items={equippedArray} direction={2} />
            </div>
          </div>

          <div className="mt-4 text-lg font-bold text-white">{displayNickname}</div>

          {/* 내 사무실일 때만 상세 통계 표시 (원래 기능 유지) */}
          {tab === "me" && isMyOffice && (
            <div className="text-content mt-4 grid grid-cols-2 gap-4">
              <div>
                <div className="text-custom-gray text-content-bold">총 러닝</div>
                <div className=""> {metersToKm((data as MyOffice).totalDist)}</div>
              </div>
              <div>
                <div className="text-custom-gray text-content-bold">최장 거리</div>
                <div className="font-medium"> {metersToKm((data as MyOffice).maxDist)}</div>
              </div>
              <div>
                <div className="text-custom-gray text-content-bold">평균 페이스</div>
                <div className="font-medium">{formatPace((data as MyOffice).avgPace)}</div>
              </div>
              <div>
                <div className="text-custom-gray text-content-bold">최고 페이스</div>
                <div className="font-medium">{formatPace((data as MyOffice).bestPace)}</div>
              </div>
              <div>
                <div className="text-custom-gray text-content-bold">잠입 횟수</div>
                <div className="font-medium"> {(data as MyOffice).totalEntryCnt ?? 0} 회</div>
              </div>
            </div>
          )}

          {/* 친구/글로벌일 때는 스와이프 안내 */}
          {(tab === "friend" || tab === "global") && (
            <div className="mt-4 text-center text-xs text-gray-400">
              👈 스와이프해서 다른 유저 보기 👉
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
