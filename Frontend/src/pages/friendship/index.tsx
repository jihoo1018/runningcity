import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import { fetchFriendRanking } from "@/entities/friendship/api/ranking";
import type { FriendRankingItem } from "@/entities/friendship/model/types";
import { FullPageLoader } from "@/shared/ui/Loader";
import { RunIcon } from "@/shared/assets/icons";
import { getLevelInfo } from "@/entities/user/model/leveling";

export default function FriendshipPage() {
  const navigate = useNavigate();
  const userId = useAuthStore((s) => s.user?.userId);
  const [ranking, setRanking] = useState<FriendRankingItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!userId) {
      setError("로그인이 필요합니다.");
      setLoading(false);
      return;
    }

    loadFriendRanking();
  }, [userId]);

  const loadFriendRanking = async () => {
    if (!userId) return;

    try {
      setLoading(true);
      setError(null);
      const data = await fetchFriendRanking(userId);
      setRanking(data);
    } catch (err: any) {
      console.error("친구 랭킹 조회 실패:", err);
      
      // 에러 응답 처리
      const errorData = err?.response?.data;
      if (errorData?.message) {
        setError(errorData.message);
      } else if (err?.response?.status === 404) {
        setError("사용자를 찾을 수 없습니다.");
      } else if (err?.response?.status === 500) {
        setError("서버 내부 오류가 발생했습니다.");
      } else {
        setError(err?.message || "친구 랭킹을 불러오는데 실패했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <FullPageLoader />;
  }

  return (
    <div className="flex h-full w-full flex-col gap-5">
      {/* 상단 탭 */}
      <div className="flex gap-3 px-1">
        <button
          onClick={() => {}}
          className="flex-1 rounded-xl bg-primary py-3 shadow-md"
        >
          <p className="text-button text-custom-black">친구 목록</p>
        </button>
        <button
          onClick={() => navigate("/friendship/request")}
          className="flex-1 rounded-xl bg-section-bg py-3 border border-custom-gray"
        >
          <p className="text-button text-custom-gray">친구 요청</p>
        </button>
      </div>

      {/* 에러 */}
      {error && (
        <div className="rounded-xl bg-accent-red/10 p-4 text-center">
          <p className="text-content text-accent-red">{error}</p>
        </div>
      )}

      {/* 리스트 */}
      {!error && (
        <div className="flex flex-1 flex-col gap-4 overflow-y-auto pr-1">
          {ranking.length === 0 ? (
            <div className="flex flex-1 items-center justify-center">
              <p className="text-content text-custom-gray">친구가 없습니다.</p>
            </div>
          ) : (
            ranking.map((item) => (
              <div
                key={item.userId}
                className={`
                  flex items-center gap-4 rounded-2xl p-4 shadow-md
                  ${item.isMe
                    ? "bg-primary/20 border-2 border-primary"
                    : "bg-section-bg"}
                `}
              >
                {/* 랭킹 숫자 */}
                <div className="w-12 flex justify-center">
                  <p
                    className={`
                      text-title 
                      ${item.isMe ? "text-primary" : "text-custom-white"}
                    `}
                  >
                    {item.rank}
                  </p>
                </div>

                {/* 프로필 */}
                <div
                  className={`
                    h-16 w-16 rounded-full flex items-center justify-center 
                    overflow-hidden border
                    ${item.isMe ? "border-primary" : "border-custom-gray"}
                  `}
                >
                  {item.profileImageUrl ? (
                    <img
                      src={item.profileImageUrl}
                      alt={item.nickname}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <RunIcon
                      className={`size-8 ${item.isMe ? "text-primary" : "text-custom-gray"}`}
                    />
                  )}
                </div>

                {/* 레벨 & 닉네임 */}
                <div className="flex flex-col gap-1 flex-1">
                  <p className="text-label text-custom-gray">
                    레벨 {(() => {
                      const levelInfo = getLevelInfo(item.totalExp);
                      return levelInfo.level > 50 ? 50 : levelInfo.level;
                    })()}
                  </p>
                  <p
                    className={`
                      text-content-bold 
                      ${item.isMe ? "text-primary" : "text-custom-white"}
                    `}
                  >
                    {item.nickname}
                  </p>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
