import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import {
  fetchMyCode,
  fetchSentRequests,
  fetchReceivedRequests,
  sendFriendRequest,
  cancelFriendRequest,
  acceptFriendRequest,
  rejectFriendRequest,
} from "@/entities/friendship/api/request";
import type { SentRequestItem, ReceivedRequestItem } from "@/entities/friendship/model/types";
import { FullPageLoader } from "@/shared/ui/Loader";
import { RunIcon } from "@/shared/assets/icons";

export default function FriendRequestPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const userId = useAuthStore((s) => s.user?.userId);
  
  // URL 쿼리 파라미터에서 탭 확인
  const tabFromUrl = searchParams.get("tab");
  const initialTab = tabFromUrl === "received" ? "received" : "sent";
  
  const [activeTab, setActiveTab] = useState<"sent" | "received">(initialTab);
  const [myCode, setMyCode] = useState<string>("");
  const [friendCode, setFriendCode] = useState<string>("");
  const [sentRequests, setSentRequests] = useState<SentRequestItem[]>([]);
  const [receivedRequests, setReceivedRequests] = useState<ReceivedRequestItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!userId) {
      setError("로그인이 필요합니다.");
      setLoading(false);
      return;
    }

    // URL 파라미터가 변경되면 탭 업데이트
    const tabFromUrl = searchParams.get("tab");
    if (tabFromUrl === "received") {
      setActiveTab("received");
    } else {
      setActiveTab("sent");
    }
  }, [searchParams, userId]);

  useEffect(() => {
    if (!userId) return;
    loadData();
  }, [userId, activeTab]);

  const loadData = async () => {
    if (!userId) return;

    try {
      setLoading(true);
      setError(null);
      setSuccessMessage(null);

      // 내 코드 조회
      const codeData = await fetchMyCode(userId);
      setMyCode(codeData.userCode);

      // 요청 목록 조회
      if (activeTab === "sent") {
        const requests = await fetchSentRequests(userId);
        setSentRequests(requests);
      } else {
        const requests = await fetchReceivedRequests(userId);
        setReceivedRequests(requests);
      }
    } catch (err: any) {
      console.error("데이터 로드 실패:", err);
      const errorData = err?.response?.data;
      if (errorData?.message) {
        setError(errorData.message);
      } else {
        setError(err?.message || "데이터를 불러오는데 실패했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCopyCode = async () => {
    try {
      // myCode가 없으면 다시 API 호출
      let codeToCopy = myCode;
      if (!codeToCopy && userId) {
        try {
          const codeData = await fetchMyCode(userId);
          codeToCopy = codeData.userCode;
          setMyCode(codeData.userCode);
        } catch (err) {
          setError("코드를 불러오는데 실패했습니다.");
          return;
        }
      }

      if (!codeToCopy) {
        setError("복사할 코드가 없습니다.");
        return;
      }

      // 클립보드에 복사
      if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(codeToCopy);
      } else {
        // Fallback: 구식 방법
        const textArea = document.createElement("textarea");
        textArea.value = codeToCopy;
        textArea.style.position = "fixed";
        textArea.style.left = "-999999px";
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        try {
          document.execCommand("copy");
        } catch (err) {
          setError("코드 복사에 실패했습니다.");
          return;
        }
        document.body.removeChild(textArea);
      }

      setSuccessMessage("코드가 복사되었습니다.");
      setTimeout(() => setSuccessMessage(null), 2000);
    } catch (err) {
      console.error("코드 복사 실패:", err);
      setError("코드 복사에 실패했습니다.");
    }
  };

  const handleSendRequest = async () => {
    if (!userId || !friendCode.trim()) {
      setError("코드를 입력해주세요.");
      return;
    }

    try {
      setError(null);
      setSuccessMessage(null);
      await sendFriendRequest(userId, { friendCode: friendCode.trim() });
      setSuccessMessage("친구 요청을 보냈습니다.");
      setFriendCode("");
      // 요청 목록 새로고침
      await loadData();
    } catch (err: any) {
      console.error("친구 요청 전송 실패:", err);
      const errorData = err?.response?.data;
      if (errorData?.message) {
        setError(errorData.message);
      } else {
        setError(err?.message || "친구 요청 전송에 실패했습니다.");
      }
    }
  };

  const handleCancelRequest = async (friendshipId: number) => {
    if (!userId) return;

    try {
      setError(null);
      await cancelFriendRequest(userId, friendshipId);
      setSuccessMessage("친구 요청을 취소했습니다.");
      // 요청 목록 새로고침
      await loadData();
    } catch (err: any) {
      console.error("친구 요청 취소 실패:", err);
      const errorData = err?.response?.data;
      if (errorData?.message) {
        setError(errorData.message);
      } else {
        setError(err?.message || "친구 요청 취소에 실패했습니다.");
      }
    }
  };

  const handleAcceptRequest = async (friendshipId: number) => {
    if (!userId) return;

    try {
      setError(null);
      await acceptFriendRequest(userId, friendshipId);
      setSuccessMessage("친구 요청을 수락했습니다.");
      // 요청 목록 새로고침
      await loadData();
    } catch (err: any) {
      console.error("친구 요청 수락 실패:", err);
      const errorData = err?.response?.data;
      if (errorData?.message) {
        setError(errorData.message);
      } else {
        setError(err?.message || "친구 요청 수락에 실패했습니다.");
      }
    }
  };

  const handleRejectRequest = async (friendshipId: number) => {
    if (!userId) return;

    try {
      setError(null);
      await rejectFriendRequest(userId, friendshipId);
      setSuccessMessage("친구 요청을 거절했습니다.");
      // 요청 목록 새로고침
      await loadData();
    } catch (err: any) {
      console.error("친구 요청 거절 실패:", err);
      const errorData = err?.response?.data;
      if (errorData?.message) {
        setError(errorData.message);
      } else {
        setError(err?.message || "친구 요청 거절에 실패했습니다.");
      }
    }
  };

  if (loading) {
    return <FullPageLoader />;
  }

  return (
    <div className="flex h-full w-full flex-col gap-5 px-1">
      {/* 상단 탭 */}
      <div className="flex gap-3">
        <button
          onClick={() => navigate("/friendship")}
          className="flex-1 rounded-xl py-3 border border-custom-gray bg-section-bg"
        >
          <p className="text-button text-custom-gray">친구 목록</p>
        </button>
        <button
          className="flex-1 rounded-xl py-3 bg-primary shadow-md"
        >
          <p className="text-button text-custom-black">친구 요청</p>
        </button>
      </div>

      {/* 친구 추가 카드 */}
      <div className="rounded-2xl bg-section-bg p-3 flex flex-col gap-3 shadow-md w-full">
        {/* 코드 입력 + 버튼 */}
        <div className="flex gap-2 w-full min-w-0">
          <input
            type="text"
            value={friendCode}
            onChange={(e) => setFriendCode(e.target.value)}
            placeholder="코드를 입력해주세요"
            className="
              flex-1 rounded-xl px-3 py-2 min-w-0
              bg-custom-black/30
              border border-custom-gray
              text-custom-white placeholder:text-custom-gray
              focus:border-primary outline-none text-content
            "
            onKeyDown={(e) => e.key === "Enter" && handleSendRequest()}
          />
          <button
            onClick={handleSendRequest}
            className="
              rounded-xl px-3 py-2 shrink-0 whitespace-nowrap
              bg-custom-black/40
              text-custom-white text-content-bold
              border border-custom-gray
              hover:border-primary transition
            "
          >
            친구 추가
          </button>
        </div>

        {/* 내 코드 */}
        <div className="flex items-center justify-between gap-2 w-full min-w-0">
          <p className="text-content text-custom-white opacity-80 truncate min-w-0">
            내 코드 : <span className="text-content-bold text-primary">{myCode}</span>
          </p>
          <button
            onClick={handleCopyCode}
            className="
              rounded-full px-3 py-1 shrink-0 whitespace-nowrap
              bg-custom-black/40
              border border-custom-gray
              text-desc text-custom-gray
              hover:border-primary transition
            "
          >
            복사
          </button>
        </div>
      </div>

      {/* 메시지 */}
      {successMessage && (
        <div className="rounded-xl bg-accent-green/10 p-4 text-center">
          <p className="text-content text-accent-green">{successMessage}</p>
        </div>
      )}
      {error && (
        <div className="rounded-xl bg-accent-red/10 p-4 text-center">
          <p className="text-content text-accent-red">{error}</p>
        </div>
      )}

      {/* 요청 탭 */}
      <div className="flex gap-3">
        <button
          onClick={() => {
            setActiveTab("sent");
            loadData();
          }}
          className={`
            flex-1 rounded-xl py-3
            ${activeTab === "sent"
              ? "bg-section-bg text-custom-white"
              : "border border-custom-gray text-custom-gray"}
          `}
        >
          <p className={`text-center ${activeTab === "sent" ? "text-content-bold" : "text-content"}`}>
            보낸 요청
          </p>
        </button>
        <button
          onClick={() => {
            setActiveTab("received");
            loadData();
          }}
          className={`
            flex-1 rounded-xl py-3
            ${activeTab === "received"
              ? "bg-section-bg text-custom-white"
              : "border border-custom-gray text-custom-gray"}
          `}
        >
          <p className={`text-center ${activeTab === "received" ? "text-content-bold" : "text-content"}`}>
            받은 요청
          </p>
        </button>
      </div>

      {/* 요청 목록 */}
      <div className="flex flex-1 flex-col gap-4 overflow-y-auto pr-1 pb-20 mt-2">
        {activeTab === "sent" ? (
          sentRequests.length === 0 ? (
            <div className="flex flex-1 items-center justify-center">
              <p className="text-content text-custom-gray">보낸 요청이 없습니다.</p>
            </div>
          ) : (
            sentRequests.map((item) => (
              <div
                key={item.friendshipId}
                className="
                  flex items-center gap-4 p-4 rounded-2xl
                  bg-section-bg border border-custom-gray shadow-sm
                "
              >
                {/* 프로필 */}
                <div className="h-14 w-14 rounded-full overflow-hidden border border-custom-gray bg-custom-black">
                  {item.friendProfileImageUrl ? (
                    <img src={item.friendProfileImageUrl} alt={item.friendNickname} className="w-full h-full object-cover" />
                  ) : (
                    <div className="flex items-center justify-center h-full">
                      <RunIcon className="size-7 text-custom-gray" />
                    </div>
                  )}
                </div>

                {/* 정보 */}
                <div className="flex flex-col flex-1 gap-1">
                  <p className="text-label text-custom-gray">레벨 {item.friendLevel}</p>
                  <p className="text-content-bold text-custom-white">{item.friendNickname}</p>
                </div>

                {/* 버튼 */}
                <button
                  onClick={() => handleCancelRequest(item.friendshipId)}
                  className="
                    rounded-xl px-4 py-2
                    bg-custom-black/40
                    border border-custom-gray
                    text-custom-white text-content-bold
                    hover:border-primary transition
                  "
                >
                  취소
                </button>
              </div>
            ))
          )
        ) : receivedRequests.length === 0 ? (
          <div className="flex flex-1 items-center justify-center">
            <p className="text-content text-custom-gray">받은 요청이 없습니다.</p>
          </div>
        ) : (
          receivedRequests.map((item) => (
            <div
              key={item.friendshipId}
              className="
                flex items-center gap-4 p-4 rounded-2xl
                bg-section-bg border border-custom-gray shadow-sm
              "
            >
              {/* 프로필 */}
              <div className="h-14 w-14 rounded-full overflow-hidden border border-custom-gray bg-custom-black">
                {item.requesterProfileImageUrl ? (
                  <img src={item.requesterProfileImageUrl} alt={item.requesterNickname} className="w-full h-full object-cover" />
                ) : (
                  <div className="flex items-center justify-center h-full">
                    <RunIcon className="size-7 text-custom-gray" />
                  </div>
                )}
              </div>

              {/* 정보 */}
              <div className="flex flex-col flex-1 gap-1">
                <p className="text-label text-custom-gray">레벨 {item.requesterLevel}</p>
                <p className="text-content-bold text-custom-white">{item.requesterNickname}</p>
              </div>

              {/* 버튼 */}
              <div className="flex gap-2">
                <button
                  onClick={() => handleAcceptRequest(item.friendshipId)}
                  className="
                    rounded-xl px-4 py-2 bg-accent-green text-custom-black text-content-bold
                  "
                >
                  수락
                </button>
                <button
                  onClick={() => handleRejectRequest(item.friendshipId)}
                  className="
                    rounded-xl px-4 py-2 
                    bg-custom-black/40 border border-custom-gray
                    text-custom-white text-content-bold
                    hover:border-accent-red transition
                  "
                >
                  거절
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

