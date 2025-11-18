// src/pages/recordlist/index.tsx

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import { FullPageLoader } from "@/shared/ui/Loader";
import { apiGet } from "@/shared/api";

type CalendarDay = {
  day: number;
  hasRecord: boolean;
};

type RecordItem = {
  sessionId: number;
  date: string;
  distanceKm: number;
  avgPace: string;
  runningTime: string;
  runningType: string;
  startTime: string;
};

type MonthlyResponse = {
  year: number;
  month: number;
  monthSummary: {
    totalDistanceKm: number;
    totalRuns: number;
    avgPace: string;
  };
  calendarDays: CalendarDay[];
  records: RecordItem[];
};

export default function RecordListPage() {
  const userId = useAuthStore((s) => s.user?.userId);
  const navigate = useNavigate();

  // 기본: 오늘 날짜 기준으로 초기화
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1); // JS는 0~11

  const [data, setData] = useState<MonthlyResponse | null>(null);
  const [loading, setLoading] = useState(true);

  // 페이지네이션
  const PAGE_SIZE = 10;
  const [page, setPage] = useState(1);

  useEffect(() => {
    if (!userId) return;

    setLoading(true);

    // 쿼리스트링 안전하게 만들기
    const qs = new URLSearchParams({
      userId: String(userId),
      year: String(year),
      month: String(month),
    }).toString();

    // 기존에 쓰던 엔드포인트 경로로만 바꿔 넣으면 됨
    apiGet<MonthlyResponse>(`/report?${qs}`)
      .then((json) => {
        setData(json);
        setPage(1); // 새 달로 바뀌면 1페이지로
      })
      .catch((err) => {
        console.error("월간 리포트 조회 실패:", err);
        // 필요하면 에러 상태도 세팅
        // setError(err);
      })
      .finally(() => setLoading(false));
  }, [userId, year, month]);

  const handlePrevMonth = () => {
    setData(null);
    setPage(1);
    if (month === 1) {
      setYear((y) => y - 1);
      setMonth(12);
    } else {
      setMonth((m) => m - 1);
    }
  };

  const handleNextMonth = () => {
    setData(null);
    setPage(1);
    if (month === 12) {
      setYear((y) => y + 1);
      setMonth(1);
    } else {
      setMonth((m) => m + 1);
    }
  };

  // 러닝 타입 한글 변환
  const toKoreanType = (t: string) => {
    if (t === "NORMAL") return "에너지";
    if (t === "INTERVAL") return "기지 잠입";
    if (t === "ENTRY") return "잠입";
    return t;
  };

  // 날짜 포맷팅 (2025.10.22. (수) 형식)
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const weekdays = ["일", "월", "화", "수", "목", "금", "토"];
    const weekday = weekdays[date.getDay()];
    return `${year}.${month}.${day}. (${weekday})`;
  };

  // 시간 포맷 (시:분)
const formatTimeHHMM = (isoString: string) => {
  const date = new Date(isoString);
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${hours}:${minutes}`;
};


  if (loading) {
    return <FullPageLoader />;
  }

  if (!data) {
    return (
      <div className="bg-section-bg flex h-full w-full items-center justify-center">
        <p className="text-content text-custom-gray">기록이 없습니다.</p>
      </div>
    );
  }

  const { monthSummary, calendarDays, records } = data;

  // 페이지네이션 계산
  const totalPages = Math.max(1, Math.ceil(records.length / PAGE_SIZE));
  const startIdx = (page - 1) * PAGE_SIZE;
  const pagedRecords = records.slice(startIdx, startIdx + PAGE_SIZE);

  return (
    <div className="flex h-full w-full flex-col">
      {/* 스크롤 영역 */}
      <div
        className="flex-1 overflow-y-auto"
        style={{ scrollbarWidth: "none", msOverflowStyle: "none" }}
      >
        {/* 상단 헤더 + 월 이동 */}
        <div className="mb-6 flex items-center justify-center gap-4">
          <button
            onClick={handlePrevMonth}
            className="text-button text-custom-white hover:text-primary flex h-20 w-20 items-center justify-center transition-all hover:shadow-[0_0_8px_rgba(0,230,255,0.3)]"
          >
            &lt;
          </button>
          <h1 className="text-subtitle text-custom-white !text-[23px] drop-shadow-[0_0_4px_rgba(0,230,255,0.3)]">
            {year}년 {month}월
          </h1>
          <button
            onClick={handleNextMonth}
            className="text-button text-custom-white hover:text-primary flex h-20 w-20 items-center justify-center transition-all hover:shadow-[0_0_8px_rgba(0,230,255,0.3)]"
          >
            &gt;
          </button>
        </div>

        {/* 요약 통계 */}
        <div className="mb-6 grid grid-cols-3 gap-3">
          <div className="border-custom-gray bg-section-bg rounded-xl border p-4">
            <div className="text-label text-custom-gray mb-2 text-center">총 거리</div>
            <div className="text-subtitle text-primary text-center !text-[18px]">
              {Number(monthSummary.totalDistanceKm.toFixed(1))}km
            </div>
          </div>
          <div className="border-custom-gray bg-section-bg rounded-xl border p-4">
            <div className="text-label text-custom-gray mb-2 text-center">평균 페이스</div>
            <div className="text-subtitle text-primary text-center !text-[18px]">
              {monthSummary.avgPace}
            </div>
          </div>
          <div className="border-custom-gray bg-section-bg rounded-xl border p-4">
            <div className="text-label text-custom-gray mb-2 text-center">러닝</div>
            <div className="text-subtitle text-primary text-center !text-[18px]">
              {monthSummary.totalRuns}
            </div>
          </div>
        </div>

        {/* 달력 영역 */}
        <div className="mb-7">
          <div className="border-primary/40 bg-section-bg/60 grid grid-cols-6 gap-3 rounded-xl border p-4">
            {calendarDays.map((dayObj) => (
              <button
                key={dayObj.day}
                type="button"
                className={`flex h-12 w-12 items-center justify-center rounded-full transition-all ${
                  dayObj.hasRecord
                    ? "bg-primary/15 border-primary text-primary border-2"
                    : "bg-custom-black/30 border-custom-gray/40 text-custom-gray border opacity-50"
                } `}
                onClick={() => {
                  // 나중에 이 날짜로 스크롤 이동 같은 거 붙이고 싶으면 여기서 처리
                }}
              >
                <span
                  className={`text-content-bold ${
                    dayObj.hasRecord ? "text-primary" : "text-custom-gray"
                  }`}
                >
                  {dayObj.day}
                </span>
              </button>
            ))}
          </div>
        </div>

        {/* 리스트 영역 */}
        <div>
          {records.length === 0 ? (
            <div className="bg-section-bg border-custom-gray rounded-xl border p-4 text-center">
              <p className="text-content text-custom-gray">아직 러닝 기록이 없습니다.</p>
            </div>
          ) : (
            <>
              <ul className="flex flex-col gap-3">
                {pagedRecords.map((r) => (
                  <li
                    key={r.sessionId}
                    className="bg-section-bg border-custom-gray hover:border-primary flex items-center justify-between gap-4 rounded-xl border px-4 pt-5 pb-4 transition-all hover:shadow-[0_0_10px_rgba(0,230,255,0.2)]"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="text-content-bold text-primary mb-4 drop-shadow-[0_0_4px_rgba(0,230,255,0.3)]">
                        {formatDate(r.date)} · {formatTimeHHMM(r.startTime)}
                      </div>
                      <div className="flex gap-6">
                        <div className="flex flex-col">
                          <div className="text-label text-custom-gray/70">거리</div>
                          <div className="text-content-bold text-custom-white">
                            {Number(r.distanceKm.toFixed(1))}km
                          </div>
                        </div>
                        <div className="flex flex-col">
                          <div className="text-label text-custom-gray/70">평균 페이스</div>
                          <div className="text-content-bold text-custom-white">{r.avgPace}</div>
                        </div>
                        <div className="flex flex-col">
                          <div className="text-label text-custom-gray/70">시간</div>
                          <div className="text-content-bold text-custom-white">{r.runningTime}</div>
                        </div>
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                      <div className="border-primary/40 bg-section-bg text-desc text-custom-gray rounded-full border px-3 py-1 whitespace-nowrap">
                        {toKoreanType(r.runningType)}
                      </div>
                      <button
                        type="button"
                        className="border-primary/50 bg-custom-black text-button text-custom-white hover:border-primary hover:bg-primary/10 rounded-lg border px-4 py-1.5 whitespace-nowrap transition-all"
                        onClick={() => {
                          if (!userId) return;
                          navigate(`/report/${r.sessionId}?userId=${userId}`);
                        }}
                      >
                        상세
                      </button>
                    </div>
                  </li>
                ))}
              </ul>

              {/* 페이지네이션 */}
              <div className="mt-4 flex items-center justify-center gap-3">
                <button
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                  disabled={page === 1}
                  className={`text-desc rounded-lg border px-3 py-1.5 transition-all ${
                    page === 1
                      ? "border-custom-gray/40 bg-section-bg text-custom-gray cursor-not-allowed opacity-50"
                      : "border-primary/40 bg-section-bg text-custom-white hover:border-primary hover:bg-primary/10 hover:shadow-[0_0_6px_rgba(0,230,255,0.3)]"
                  } `}
                >
                  이전
                </button>
                <span className="border-primary/40 bg-section-bg text-desc text-custom-white rounded-lg border px-3 py-1.5">
                  {page} / {totalPages}
                </span>
                <button
                  onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                  disabled={page === totalPages}
                  className={`text-desc rounded-lg border px-3 py-1.5 transition-all ${
                    page === totalPages
                      ? "border-custom-gray/40 bg-section-bg text-custom-gray cursor-not-allowed opacity-50"
                      : "border-primary/40 bg-section-bg text-custom-white hover:border-primary hover:bg-primary/10 hover:shadow-[0_0_6px_rgba(0,230,255,0.3)]"
                  } `}
                >
                  다음
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
