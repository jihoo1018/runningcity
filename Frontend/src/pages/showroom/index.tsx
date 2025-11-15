import { useState, useEffect } from "react";
import { ShowroomTabs } from "@/entities/showroom/ui/ShowroomTabs";
import { CharacterCard } from "@/entities/showroom/ui/CharacterCard";
import { ShowroomActions } from "@/entities/showroom/ui/ShowroomActions";
import { fetchGetShowroomMe } from "@/entities/showroom/api/me";
import { onNext, onNextGlobal } from "@/entities/showroom/api/showroom";
import { EquippedItem, MyOffice } from "@/entities/showroom/model/type";

const ShowroomPage = () => {
  const [tab, setTab] = useState<"me" | "friend" | "global">("me");
  const [myShowroom, setMyShowroom] = useState<MyOffice | null>({
    userNm: "러닝ㅈi존 유리",
    userLv: 72,
    userId: 1,
    totalDist: 2142000, // 유저가 총 달린 거리
    longestDist: 10123, // 유저가 달린 최장 거리
    avgPace: 824, // 유저 평균 페이스
    bestPace: 634, // 유저 최고 기록(페이스)
    totalEntryCnt: 812, // 유저 총 잠입 횟수
    equippedItemList: [],
  }); // TODO 서버에서 받아온 내 사무실 데이터로 변경하기(지금은 임시 데이터)
  const [userId, setUserId] = useState(1); // TODO 나중에 실 유저 아이디 받아와야 함

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // 첫 로딩 시 내 사무실 가져오기
  useEffect(() => {
    const loadShowroom = async () => {
      try {
        setLoading(true);
        setError("");

        // const data = await fetchGetShowroomMe(userId);
        // setMyShowroom(data); // 받아온 데이터 저장
      } catch (err: any) {
        console.error(err);
        setError(err.message || "내 사무실 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    loadShowroom();
  }, []);

  return (
    <div className="flex flex-col">
      {/* 상단 탭 */}
      <ShowroomTabs tab={tab} setTab={setTab} />

      {/* 로딩 / 에러 처리 */}
      {loading && (
        <div className="flex flex-1 items-center justify-center text-gray-400">불러오는 중...</div>
      )}
      {error && <div className="flex flex-1 items-center justify-center text-red-400">{error}</div>}

      {/* 데이터 있을 때만 렌더링 */}
      {!loading && !error && myShowroom && (
        <>
          {/* 캐릭터 카드 */}
          <div className="flex items-start justify-center py-2">
            <CharacterCard tab={tab} data={myShowroom} />
          </div>

          {/* 하단 액션 버튼 */}
          <ShowroomActions tab={tab} onNext={onNext} onNextGlobal={onNextGlobal} />
        </>
      )}
    </div>
  );
};

export default ShowroomPage;
