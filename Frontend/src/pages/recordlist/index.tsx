// src/pages/recordlist/index.tsx
import { useEffect, useState } from 'react';
import Navbar from '../../widgets/navbar/ui/Navbar';

type CalendarDay = {
  day: number;
  hasRecord: boolean;
};

type RecordItem = {
  date: string;
  distanceKm: number;
  avgPace: string;
  runningTime: string;
  runningType: string;
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

const userId = 'google_1234'; // 임시

export default function RecordListPage() {
  // ⬇️ 오늘 날짜 기준으로 초기화
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1); // JS는 0~11

  const [data, setData] = useState<MonthlyResponse | null>(null);
  const [loading, setLoading] = useState(true);

  // 페이지네이션
  const PAGE_SIZE = 7;
  const [page, setPage] = useState(1);

  useEffect(() => {
    setLoading(true);
    fetch(
      `http://localhost:8080/api/v1/recordlist/month?userId=${userId}&year=${year}&month=${month}`
    )
      .then((res) => res.json())
      .then((json) => {
        setData(json);
        setPage(1); // 달 바뀌면 첫 페이지로
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [year, month]);

  const handlePrevMonth = () => {
    setData(null);
    setPage(1);
    setMonth((prev) => {
      if (prev === 1) {
        setYear((y) => y - 1);
        return 12;
      }
      return prev - 1;
    });
  };

  const handleNextMonth = () => {
    setData(null);
    setPage(1);
    setMonth((prev) => {
      if (prev === 12) {
        setYear((y) => y + 1);
        return 1;
      }
      return prev + 1;
    });
  };

  if (loading) {
    return (
      <div
        style={{
          width: '100%',
          height: '100%',
          minWidth: '100vw',
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
        }}
      >
        <div style={{ padding: 16, flex: 1 }}>로딩중...</div>
        <Navbar activeTab="기록" />
      </div>
    );
  }

  if (!data) {
    return (
      <div
        style={{
          width: '100%',
          height: '100%',
          minWidth: '100vw',
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
        }}
      >
        <div style={{ padding: 16, flex: 1 }}>데이터가 없습니다.</div>
        <Navbar activeTab="기록" />
      </div>
    );
  }

  const { monthSummary, calendarDays, records } = data;

  // 페이지네이션 계산
  const totalPages = Math.max(1, Math.ceil(records.length / PAGE_SIZE));
  const startIdx = (page - 1) * PAGE_SIZE;
  const pagedRecords = records.slice(startIdx, startIdx + PAGE_SIZE);

  // 러닝 타입 한글화
  const toKoreanType = (t: string) => {
    if (t === 'NORMAL') return '일반';
    return '침입';
  };

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        minWidth: '100vw',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: '#f3f4f6',
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
      }}
    >
      {/* 스크롤 영역 */}
      <div
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '16px 16px 80px',
          maxWidth: 960,
          margin: '0 auto',
          width: '100%',
          scrollbarWidth: 'none',
          msOverflowStyle: 'none',
        }}
      >
        {/* 상단 헤더 + 달 이동 */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            marginBottom: 12,
          }}
        >
          <button
            onClick={handlePrevMonth}
            style={{
              border: 'none',
              background: '#fff',
              borderRadius: 8,
              padding: '4px 8px',
              cursor: 'pointer',
              boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
            }}
          >
            ◀
          </button>
          <h1 style={{ fontSize: 18, margin: 0 }}>
            {year}년 {month}월 러닝 기록
          </h1>
          <button
            onClick={handleNextMonth}
            style={{
              border: 'none',
              background: '#fff',
              borderRadius: 8,
              padding: '4px 8px',
              cursor: 'pointer',
              boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
            }}
          >
            ▶
          </button>
        </div>

        {/* 월 요약 */}
        <div
          style={{
            display: 'flex',
            gap: 12,
            marginBottom: 20,
            flexWrap: 'wrap',
          }}
        >
          <SummaryCard
            label="총 거리"
            value={`${monthSummary.totalDistanceKm} km`}
          />
          <SummaryCard label="총 러닝" value={`${monthSummary.totalRuns}회`} />
          <SummaryCard label="평균 페이스" value={monthSummary.avgPace} />
        </div>

        {/* 캘린더 */}
        <div style={{ marginBottom: 28 }}>
          <h2 style={{ fontSize: 16, marginBottom: 8 }}>캘린더</h2>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(44px, 1fr))',
              gap: 8,
              background: '#f7f7f7',
              padding: 8,
              borderRadius: 12,
            }}
          >
            {calendarDays.map((dayObj) => (
              <button
                key={dayObj.day}
                type="button"
                style={{
                    minHeight: 60,
                    border: 'none',
                    borderRadius: 8,
                    cursor: 'pointer',
                    background: dayObj.hasRecord ? '#e0f2ff' : '#fff',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'flex-start',
                    justifyContent: 'flex-start',
                    padding: '6px 6px',
                    gap: 4,
                }}
                onClick={() => {
                  // 여기서는 그냥 클릭 가능하게만
                }}
              >
                <span style={{ fontWeight: 600, fontSize: 13 }}>
                  {dayObj.day}
                </span>
                {dayObj.hasRecord && (
                  <span
                    style={{
                      fontSize: 10,
                      background: '#0ea5e9',
                      color: 'white',
                      padding: '2px 5px',
                      borderRadius: 999,
                    }}
                  >
                    기록 있음
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>

        {/* 리스트 */}
        <div>
          <h2 style={{ fontSize: 16, marginBottom: 8 }}>기록 리스트</h2>
          {records.length === 0 ? (
            <div
              style={{ padding: 12, background: '#fafafa', borderRadius: 8 }}
            >
              기록이 없습니다.
            </div>
          ) : (
            <>
              <ul
                style={{ display: 'flex', flexDirection: 'column', gap: 10 }}
              >
                {pagedRecords.map((r) => (
                  <li
                    key={r.date + r.runningTime}
                    style={{
                      border: '1px solid #eee',
                      borderRadius: 10,
                      padding: '10px 12px',
                      display: 'flex',
                      gap: 10,
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      background: '#fff',
                    }}
                  >
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontWeight: 600, fontSize: 14 }}>
                        {r.date} · {toKoreanType(r.runningType)}
                      </div>
                      <div
                        style={{ fontSize: 12, color: '#555', lineHeight: 1.4 }}
                      >
                        {r.distanceKm} km · 페이스 {r.avgPace} · 시간{' '}
                        {r.runningTime}
                      </div>
                    </div>
                    <button
                      type="button"
                      style={{
                        background: '#0ea5e9',
                        color: 'white',
                        border: 'none',
                        borderRadius: 6,
                        padding: '6px 10px',
                        cursor: 'pointer',
                        whiteSpace: 'nowrap',
                      }}
                      onClick={() => {
                        // 상세는 다른 사람이 만드니까 비워둠
                      }}
                    >
                      상세
                    </button>
                  </li>
                ))}
              </ul>

              {/* 페이지네이션 */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'center',
                  gap: 8,
                  marginTop: 16,
                }}
              >
                <button
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                  disabled={page === 1}
                  style={{
                    padding: '4px 10px',
                    borderRadius: 6,
                    border: '1px solid #ddd',
                    background: page === 1 ? '#eee' : '#fff',
                  }}
                >
                  이전
                </button>
                <span style={{ fontSize: 12 }}>
                  {page} / {totalPages}
                </span>
                <button
                  onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                  disabled={page === totalPages}
                  style={{
                    padding: '4px 10px',
                    borderRadius: 6,
                    border: '1px solid #ddd',
                    background: page === totalPages ? '#eee' : '#fff',
                  }}
                >
                  다음
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* 하단 네비바 */}
      <Navbar activeTab="기록" />
    </div>
  );
}

function SummaryCard({ label, value }: { label: string; value: string }) {
  return (
    <div
      style={{
        flex: '1 1 120px',
        background: '#fff',
        border: '1px solid #eee',
        borderRadius: 10,
        padding: '10px 12px',
        minWidth: 110,
      }}
    >
      <div style={{ fontSize: 11, color: '#777' }}>{label}</div>
      <div style={{ fontSize: 15, fontWeight: 600 }}>{value}</div>
    </div>
  );
}
