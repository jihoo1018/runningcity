// src/pages/showroom/index.tsx
import { useState, useEffect, useRef } from "react";
import { ShowroomTabs } from "@/entities/showroom/ui/ShowroomTabs";
import { CharacterCard } from "@/entities/showroom/ui/CharacterCard";
import { ShowroomActions } from "@/entities/showroom/ui/ShowroomActions";
import {
  fetchGetShowroomMe,
  fetchGetGlobalShowroom,
  fetchGetFriendShowroom
} from "@/entities/showroom/api/me";
import { MyOffice, RandomAvatar } from "@/entities/showroom/model/type";
import { useShowroomLoader } from "@/entities/showroom/api/useShowroomLoader";
import { useAvatarStore } from "@/features/avatar/model/avatarStore";

export default function ShowroomPage() {
  const [tab, setTab] = useState<"me" | "friend" | "global">("me");
  const [myShowroom, setMyShowroom] = useState<MyOffice | null>(null);
  const [friendList, setFriendList] = useState<RandomAvatar[]>([]);
  const [globalList, setGlobalList] = useState<RandomAvatar[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const { loadShowroom } = useShowroomLoader();
  const sync = useAvatarStore((s) => s.syncFromServer); // 슬롯 동기화 함수
  const slots = useAvatarStore((s) => s.slots);

  // 스와이프 관련
  const touchStartX = useRef(0);
  const touchEndX = useRef(0);

  // 탭 변경 시 데이터 로드
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError("");
        setCurrentIndex(0); // 탭 변경 시 인덱스 초기화

        if (tab === "me") {
          const data = await fetchGetShowroomMe();
          setMyShowroom(data);
        } else if (tab === "friend") {
          const data = await fetchGetFriendShowroom();
          setFriendList(data);
          // 친구가 없으면 에러는 아니고 그냥 빈 상태
          if (data.length === 0) {
            console.log('친구 목록이 비어있습니다.');
          }
        } else if (tab === "global") {
          console.log('[ShowroomPage] 글로벌 탭 데이터 로딩 시작');
          const data = await fetchGetGlobalShowroom(20); // 글로벌은 20명
          console.log('[ShowroomPage] 글로벌 데이터 받음:', data);
          console.log('[ShowroomPage] 글로벌 데이터 개수:', data.length);
          setGlobalList(data);
          console.log('[ShowroomPage] globalList 상태 업데이트 완료');

          // 글로벌 유저가 없으면 에러는 아니고 그냥 빈 상태
          if (data.length === 0) {
            console.log('[ShowroomPage] 글로벌 유저 목록이 비어있습니다.');
          } else {
            console.log(`[ShowroomPage] 글로벌 유저 ${data.length}명 로드 완료`);
          }
        }
      } catch (err: any) {
        console.error('데이터 로드 에러:', err);
        // 404 에러는 이미 API 레벨에서 처리됨
        setError(err.message || "데이터를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [tab]);

  // 스와이프 핸들러
  const handleTouchStart = (e: React.TouchEvent) => {
    touchStartX.current = e.touches[0].clientX;
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    touchEndX.current = e.touches[0].clientX;
  };

  const handleTouchEnd = () => {
    const diffX = touchStartX.current - touchEndX.current;
    const threshold = 50; // 최소 스와이프 거리

    const list = tab === "friend" ? friendList : globalList;
    if (list.length === 0) return;

    if (diffX > threshold && currentIndex < list.length - 1) {
      // 왼쪽 스와이프 → 다음
      setCurrentIndex((prev) => prev + 1);
    } else if (diffX < -threshold && currentIndex > 0) {
      // 오른쪽 스와이프 → 이전
      setCurrentIndex((prev) => prev - 1);
    }
  };

  // 현재 보여줄 데이터 결정
  const getCurrentData = () => {
    if (tab === "me") return myShowroom;
    if (tab === "friend") {
      console.log(`[getCurrentData] 친구 탭, currentIndex=${currentIndex}, friendList.length=${friendList.length}`);
      return friendList[currentIndex];
    }
    if (tab === "global") {
      console.log(`[getCurrentData] 글로벌 탭, currentIndex=${currentIndex}, globalList.length=${globalList.length}`);
      const data = globalList[currentIndex];
      console.log('[getCurrentData] 글로벌 현재 데이터:', data);
      return data;
    }
    return null;
  };

  const currentData = getCurrentData();
  console.log(`[ShowroomPage] 현재 탭: ${tab}, 현재 데이터:`, currentData);

  const showPagination = (tab === "friend" && friendList.length > 1) ||
                         (tab === "global" && globalList.length > 1);

  return (
    <div className="text-content flex flex-col">
      <ShowroomTabs tab={tab} setTab={setTab} />

      {loading && (
        <div className="text-center text-gray-400 py-8">불러오는 중...</div>
      )}

      {error && (
        <div className="text-center text-red-400 py-8">{error}</div>
      )}

      {!loading && !error && currentData && (
        <>
          <div
            className="flex items-start justify-center py-2"
            onTouchStart={tab !== "me" ? handleTouchStart : undefined}
            onTouchMove={tab !== "me" ? handleTouchMove : undefined}
            onTouchEnd={tab !== "me" ? handleTouchEnd : undefined}
          >
            <CharacterCard
              tab={tab}
              data={currentData}
            />
          </div>

          {/* 페이지 인디케이터 (친구/글로벌 탭에만) */}
          {showPagination && (
            <div className="flex justify-center gap-2 mt-2">
              {(tab === "friend" ? friendList : globalList).map((_, idx) => (
                <div
                  key={idx}
                  className={`h-2 rounded-full transition-all ${
                    idx === currentIndex
                      ? "w-6 bg-primary"
                      : "w-2 bg-gray-500"
                  }`}
                />
              ))}
            </div>
          )}

          {/* "내 사무실" 탭일 때만 액션 버튼 표시 */}
          {tab === "me" && <ShowroomActions tab={tab} />}
        </>
      )}

      {/* 데이터 없을 때 */}
      {!loading && !error && !currentData && (
        <div className="text-center text-gray-400 py-8">
          {tab === "friend" && (
            <div>
              <div className="text-lg mb-2">👥</div>
              <div>친구가 없습니다</div>
              <div className="text-xs mt-2 text-gray-500">
                친구를 추가하거나 API가 준비 중일 수 있습니다
              </div>
            </div>
          )}
          {tab === "global" && (
            <div>
              <div className="text-lg mb-2">🌍</div>
              <div>글로벌 유저가 없습니다</div>
              <div className="text-xs mt-2 text-gray-500">
                다른 유저가 없거나 API가 준비 중일 수 있습니다
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
