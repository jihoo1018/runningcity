// src/pages/showroom/index.tsx
import { useState, useEffect, useRef } from "react";
import { ShowroomTabs } from "@/entities/showroom/ui/ShowroomTabs";
import { CharacterCard } from "@/entities/showroom/ui/CharacterCard";
import { ShowroomActions } from "@/entities/showroom/ui/ShowroomActions";
import {
  fetchGetShowroomMe,
  fetchGetGlobalShowroom,
  fetchGetFriendShowroom,
} from "@/entities/showroom/api/me";
import { MyOffice, RandomAvatar } from "@/entities/showroom/model/type";
import { useAvatarStore } from "@/features/avatar/model/avatarStore";
import { useShowroomLoader } from "@/entities/showroom/api/useShowroomLoader";

export default function ShowroomPage() {
  const [tab, setTab] = useState<"me" | "friend" | "global">("me");
  const [myShowroom, setMyShowroom] = useState<MyOffice | null>(null);
  const [friendList, setFriendList] = useState<RandomAvatar[]>([]);
  const [globalList, setGlobalList] = useState<RandomAvatar[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const sync = useAvatarStore((s) => s.syncFromServer);
  const slots = useAvatarStore((s) => s.slots);

  const touchStartX = useRef(0);
  const touchEndX = useRef(0);

  /** 탭 변경 시 데이터 로드 */
  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");
        setCurrentIndex(0);

        if (tab === "me") {
          const data = await fetchGetShowroomMe();
          setMyShowroom(data);

          if (Object.keys(slots).length === 0) {
            sync(data.equippedItemList);
          }
        } else if (tab === "friend") {
          const data = await fetchGetFriendShowroom();
          setFriendList(data);
        } else if (tab === "global") {
          const data = await fetchGetGlobalShowroom(20);
          setGlobalList(data);
        }
      } catch (err: any) {
        setError(err.message || "데이터를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [tab]);

  /** 스와이프 관련 */
  const handleTouchStart = (e: React.TouchEvent) => {
    touchStartX.current = e.touches[0].clientX;
  };
  const handleTouchMove = (e: React.TouchEvent) => {
    touchEndX.current = e.touches[0].clientX;
  };
  const handleTouchEnd = () => {
    const diff = touchStartX.current - touchEndX.current;
    const threshold = 50;

    const list = tab === "friend" ? friendList : globalList;
    if (list.length === 0) return;

    if (diff > threshold && currentIndex < list.length - 1) {
      setCurrentIndex((prev) => prev + 1);
    } else if (diff < -threshold && currentIndex > 0) {
      setCurrentIndex((prev) => prev - 1);
    }
  };

  /** 현재 표시할 데이터 */
  const getCurrentData = () => {
    if (tab === "me") return myShowroom;
    if (tab === "friend") return friendList[currentIndex];
    if (tab === "global") return globalList[currentIndex];
    return null;
  };

  const currentData = getCurrentData();

  const showPagination =
    (tab === "friend" && friendList.length > 1) || (tab === "global" && globalList.length > 1);

  return (
    <div className="text-content relative flex h-full flex-col">
      {/* 상단 탭 */}
      <ShowroomTabs tab={tab} setTab={setTab} />

      {/* 로딩 */}
      {loading && (
        <div className="flex flex-1 items-center justify-center text-gray-400">불러오는 중...</div>
      )}

      {/* 에러 */}
      {!loading && error && (
        <div className="flex flex-1 items-center justify-center text-red-400">{error}</div>
      )}

      {/* 데이터가 있을 때 */}
      {!loading && !error && currentData && (
        <>
          {/* --------------------------------------------- */}
          {/*      🔥 세로 중앙 정렬 + CharacterCard 영역    */}
          {/* --------------------------------------------- */}
          <div className="flex flex-1 flex-col justify-center">
            {/* 캐릭터 카드 */}
            <div
              className="flex flex-1 items-center justify-center py-4"
              onTouchStart={tab !== "me" ? handleTouchStart : undefined}
              onTouchMove={tab !== "me" ? handleTouchMove : undefined}
              onTouchEnd={tab !== "me" ? handleTouchEnd : undefined}
            >
              <CharacterCard tab={tab} data={currentData} />
            </div>

            {/* 페이지 네비게이터 (친구/글로벌) */}
            {showPagination && (
              <div className="mt-2 mb-2 flex justify-center gap-2">
                {(tab === "friend" ? friendList : globalList).map((_, idx) => (
                  <div
                    key={idx}
                    className={`h-2 rounded-full transition-all ${
                      idx === currentIndex ? "bg-primary w-6" : "w-2 bg-gray-500"
                    }`}
                  />
                ))}
              </div>
            )}

            {/* 내 사무실일 때 하단 버튼 */}
            {tab === "me" && (
              <div className="flex justify-center pb-10">
                <ShowroomActions tab={tab} />
              </div>
            )}
          </div>
        </>
      )}

      {/* 데이터 없음 */}
      {!loading && !error && !currentData && (
        <div className="flex flex-1 items-center justify-center text-gray-400">
          {tab === "friend" && "친구가 없습니다"}
          {tab === "global" && "글로벌 유저가 없습니다"}
        </div>
      )}
    </div>
  );
}
