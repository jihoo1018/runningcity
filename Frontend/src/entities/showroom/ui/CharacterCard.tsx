import { LPCCharacterRenderer } from "./LPCCharacterRenderer";
import { useAvatarStore } from "@/features/avatar/model/avatarStore";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import type { MyOffice, RandomAvatar } from "@/entities/showroom/model/type";
import { slotsToArray } from "@/entities/showroom/model/slotUtils";
import { getLevelInfo } from "@/entities/user/model/leveling";
import { metersToKm, formatPace, formatDuration } from "@/shared/lib/format";
import { ENV } from "@/shared/config/env";
import { useState, useEffect } from "react";
import { NotificationIcon } from "@/shared/assets/icons";

type Props = {
  tab: "me" | "friend" | "global";
  data?: MyOffice | RandomAvatar;
};

export const CharacterCard = ({ tab, data }: Props) => {
  const user = useAuthStore((s) => s.user);
  const slots = useAvatarStore((s) => s.slots);
  const userLevelInfo = getLevelInfo(user?.totalExp ?? 0);

  // 랜덤 배경 이미지 (1~4)
  const [backgroundImage, setBackgroundImage] = useState<string>("");
  const [isImageLoaded, setIsImageLoaded] = useState(false);

  useEffect(() => {
    // 1~4 중 랜덤 선택
    const randomNum = Math.floor(Math.random() * 4) + 1;
    const bgUrl = `${ENV.ASSETS_ORIGIN}/background/character_card_background${randomNum}.png`;

    // 이미지 preload
    const img = new Image();
    img.onload = () => {
      setBackgroundImage(bgUrl);
      setIsImageLoaded(true);
    };
    img.onerror = () => {
      // 이미지 로드 실패 시 기본 배경 사용
      setIsImageLoaded(true);
    };
    img.src = bgUrl;
  }, []);

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

  // const displayUserId = isRandomAvatar ? (data as RandomAvatar).userId : user?.userId;

  return (
    <div className="text-content-bold relative mx-auto mt-2 w-full max-w-[360px] px-3">
      <div className="chip-frame relative w-full rounded-xl p-[2px]">
        <div className="border-primary bg-section-bg rounded-xl border px-4 py-6">
          <div className="flex items-center justify-between">
            <div className="text-button text-content-bold text-[#67e8f9]">LV {displayLevel}</div>
            {/* <div className="text-custom-gray text-[10px]">UID: {displayUserId}</div> */}
          </div>

          <div
            className="border-primary/60 relative mt-4 h-48 overflow-hidden rounded-lg border bg-cover bg-center bg-no-repeat transition-opacity duration-500"
            style={{
              backgroundImage: backgroundImage ? `url(${backgroundImage})` : undefined,
              backgroundColor: '#1a1a3e',
              opacity: isImageLoaded ? 1 : 0.8
            }}
          >
            {/* 로딩 중일 때 약간의 블러 효과 */}
            {!isImageLoaded && (
              <div className="absolute inset-0 bg-[#1a1a3e]/50 backdrop-blur-sm flex items-center justify-center">
                <div className="text-primary text-xs animate-pulse">Loading...</div>
              </div>
            )}
            <div className="flex h-full items-center justify-center relative z-10">
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

          {/* 친구/글로벌 탭 - Privacy 설정에 따라 통계 표시 */}
          {(tab === "friend" || tab === "global") && isRandomAvatar && (() => {
            const avatar = data as RandomAvatar;
            const privacy = avatar.privacySetting;

            // Privacy 설정에 따라 표시할 항목 필터링
            const statsToShow: Array<{ label: string; value: string }> = [];

            if (privacy.showTotalRunning) {
              statsToShow.push({ label: "총 러닝", value: metersToKm(avatar.totalDist) });
            }
            if (privacy.showMaxDistance) {
              statsToShow.push({ label: "최장 거리", value: metersToKm(avatar.maxDist) });
            }
            if (privacy.showAvgPace) {
              statsToShow.push({ label: "평균 페이스", value: formatPace(avatar.avgPace) });
            }
            if (privacy.showBestPace) {
              statsToShow.push({ label: "최고 페이스", value: formatPace(avatar.bestPace) });
            }
            if (privacy.showHikingCount) {
              statsToShow.push({ label: "잠입 횟수", value: `${avatar.totalEntryCnt ?? 0} 회` });
            }

            return statsToShow.length > 0 ? (
              <div className="text-content mt-4 grid grid-cols-2 gap-4">
                {statsToShow.map((stat, idx) => (
                  <div key={idx}>
                    <div className="text-custom-gray text-content-bold">{stat.label}</div>
                    <div className="font-medium">{stat.value}</div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="mt-4 text-center text-xs text-gray-400">
                🔒 이 사용자는 통계를 비공개로 설정했습니다
              </div>
            );
          })()}

          {/* 태그 표시 (모든 탭) */}
          {(() => {
            let tags: string[] = [];

            // me 탭: MyOffice의 privacySetting
            if (tab === "me" && isMyOffice && data) {
              tags = (data as MyOffice).privacySetting?.tags || [];
            }
            // 친구/글로벌 탭: RandomAvatar의 privacySetting
            else if ((tab === "friend" || tab === "global") && isRandomAvatar) {
              tags = (data as RandomAvatar).privacySetting?.tags || [];
            }

            // 태그 색상 배열 (Tailwind)
            const tagColors = [
              "bg-blue-500/20 text-blue-400 border-blue-500/30",
              "bg-purple-500/20 text-purple-400 border-purple-500/30",
              "bg-pink-500/20 text-pink-400 border-pink-500/30",
              "bg-green-500/20 text-green-400 border-green-500/30",
              "bg-yellow-500/20 text-yellow-400 border-yellow-500/30",
              "bg-red-500/20 text-red-400 border-red-500/30",
              "bg-cyan-500/20 text-cyan-400 border-cyan-500/30",
              "bg-orange-500/20 text-orange-400 border-orange-500/30",
            ];

            return tags.length > 0 ? (
              <div className="mt-4">
                <div className="text-custom-gray text-xs mb-2">태그</div>
                <div className="flex flex-wrap gap-2">
                  {tags.map((tag, idx) => (
                    <span
                      key={idx}
                      className={`text-xs px-3 py-1 rounded-full border ${tagColors[idx % tagColors.length]}`}
                    >
                      #{tag}
                    </span>
                  ))}
                </div>
              </div>
            ) : null;
          })()}

          {/* 스와이프 안내 (친구/글로벌 탭) */}
          {(tab === "friend" || tab === "global") && (
            <div className="mt-3 flex items-center justify-center gap-2 text-xs text-gray-400">
              <NotificationIcon className="w-4 h-4 text-primary animate-pulse" />
              <span>스와이프해서 다른 유저 보기</span>
              <NotificationIcon className="w-4 h-4 text-primary animate-pulse" />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
