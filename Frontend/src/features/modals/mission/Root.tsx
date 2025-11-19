// src/features/modals/mission/Root.tsx
import React, { useEffect, useMemo, useState } from "react";
import { Modal } from "@/shared/ui/Modal";
import { apiGet, apiPost } from "@/shared/api";
import { useModalRouter } from "@/app/modal/useModalRouter";
import type { ModalProps } from "@/app/modal/types";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import { CommonButton } from "@/shared/ui";

type DailyMissionResponse = {
  id: number | null;
  date: string;
  serverTime: string;
  targetKm: number;
  currentKm: number;
  progressPercent: number;
  completed: boolean;
  claimed: boolean;
};

type WeeklyMissionResponse = {
  missionId: number | null;
  userId: number;
  weekStart: string; // ISO
  weekEnd: string; // ISO
  targetDays: number;
  completedDays: number;
  progressPercent: number;
  completed: boolean;
  claimed: boolean;
  createdAt: string; // ISO
  updatedAt: string; // ISO
};

type Tab = "daily" | "weekly";

const fmt = (v: number) => (Number.isFinite(v) ? v.toFixed(2) : "0.00");
const getErr = (e: unknown) =>
  e instanceof Error ? e.message : typeof e === "string" ? e : JSON.stringify(e);

// 공통 언랩 유틸
function unwrap<T>(res: any): T {
  if (res == null) return res as T;
  if ("data" in res && res.data != null) {
    const body = res.data;
    if (typeof body === "object" && body && "data" in body && (body as any).data != null) {
      return (body as any).data as T;
    }
    if (typeof body === "object" && body && "result" in body) {
      return (body as any).result as T;
    }
    return body as T;
  }
  if (typeof res === "object" && res && "data" in res && (res as any).data == null) {
    return res as T;
  }
  if (typeof res === "object" && res && "data" in res) {
    return (res as any).data as T;
  }
  if (typeof res === "object" && res && "result" in res) {
    return (res as any).result as T;
  }
  return res as T;
}

// 배터리 컴포넌트
type BatteryProps = {
  progress: number;
};
const Battery: React.FC<BatteryProps> = ({ progress }) => {
  const pct = Math.max(0, Math.min(100, Number.isFinite(progress) ? progress : 0));

  return (
    <div className="mt-2 flex items-center justify-center">
      <div
        style={{
          width: 80,
          height: 140,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          gap: 4,
        }}
      >
        {/* 배터리 머리 */}
        <div
          style={{
            width: 40,
            height: 10,
            borderRadius: 999,
            background: "rgba(241,245,249,0.55)",
          }}
        />

        {/* 배터리 몸통 */}
        <div
          style={{
            position: "relative",
            width: "100%",
            flex: 1,
            borderRadius: 24,
            border: "2px solid rgba(148, 231, 255, 0.7)",
            padding: 4,
            background: "rgba(15,23,42,0.9)",
            boxShadow: "0 0 20px rgba(15,23,42,0.8)",
            overflow: "hidden",
          }}
        >
          {/* 빈 배경 */}
          <div
            style={{
              position: "absolute",
              inset: 4,
              borderRadius: 18,
              background:
                "radial-gradient(circle at 30% 0%, rgba(148,231,255,0.12), transparent 55%), #020617",
            }}
          />

          {/* 채워지는 에너지 */}
          <div
            className="
              absolute left-1 right-1 bottom-1
              rounded-[18px]
              bg-gradient-to-b
              from-primary
              via-primary/90
              to-primary/80
              shadow-[0_0_20px_rgba(0,0,0,0.25)]
              transition-all duration-300 ease-in-out
            "
            style={{ height: `${pct}%` }}
          />


        </div>
      </div>
    </div>
  );
};

// 기본 햄스터 대사
function getDailyHamsterText(mission: DailyMissionResponse | null): string {
  if (!mission) return "오늘 미션 정보를 불러오는 중이야! 잠시만 기다려줘.";

  if (mission.claimed) return "이미 오늘의 선물은 받아갔어! 내일도 같이 달려보자!";
  if (mission.completed) return "와, 오늘 목표를 다 채웠어! 여기 와서 선물을 받아가!";

  const p = mission.progressPercent ?? 0;
  if (p <= 0) return "아직 에너지가 모자라... 오늘도 같이 한 걸음씩 가보자!";

  if (p < 50) return "좋은 출발이야! 더 달리면 배터리가 쭉쭉 차오를 거야!";
  return "거의 다 모였어! 조금만 더 달리면 오늘 선물을 줄 수 있어!";
}

function getWeeklyHamsterText(mission: WeeklyMissionResponse | null): string {
  if (!mission) return "이번 주 미션 정보를 불러오는 중이야!";

  if (mission.claimed) return "이미 이번주 선물은 받아갔어! 다음 주도 기대할게!";
  if (mission.completed) return "이번 주 목표까지 꽉 채웠어! 선물 받아가고 푹 쉬자!";

  const p = mission.progressPercent ?? 0;
  if (p <= 0) return "이번 주 에너지가 아직 거의 없네... 천천히 같이 모아보자!";
  if (p < 50) return "좋은 페이스야! 이번 주 배터리도 서서히 차오르는 중이야!";
  return "이제 거의 다 왔어! 조금만 더 달리면 이번주 선물을 줄 수 있어!";
}

export default function MissionRoot({ onClose }: ModalProps) {
  const { to } = useModalRouter();
  const uid = useAuthStore.getState().user?.userId;

  const [tab, setTab] = useState<Tab>("daily");
  const [daily, setDaily] = useState<DailyMissionResponse | null>(null);
  const [weekly, setWeekly] = useState<WeeklyMissionResponse | null>(null);
  const [loading, setLoading] = useState(false);

  // 햄스터 대사 오버라이드 (버튼 클릭 등)
  const [hamsterOverride, setHamsterOverride] = useState<{
    target: Tab;
    message: string;
  } | null>(null);

  const speak = (target: Tab, message: string) => {
    setHamsterOverride({ target, message });
    window.setTimeout(() => {
      setHamsterOverride((prev) => (prev && prev.target === target ? null : prev));
    }, 2200);
  };

  const dailyFill = useMemo(() => daily?.progressPercent ?? 0, [daily]);
  const weeklyFill = useMemo(() => weekly?.progressPercent ?? 0, [weekly]);

  const loadDaily = async () => {
    if (!uid) {
      speak("daily", "로그인이 필요해! 먼저 로그인하고 다시 와줘.");
      return;
    }
    try {
      setLoading(true);
      const res = await apiGet(`/users/${uid}/daily-missions/today`);
      const data = unwrap<DailyMissionResponse>(res);
      setDaily(data ?? null);
    } catch (e) {
      console.error(getErr(e));
      speak("daily", "앗, 서버와 연결이 잘 안 돼... 잠시 후에 다시 시도해줘!");
    } finally {
      setLoading(false);
    }
  };

  const loadWeekly = async () => {
    if (!uid) {
      speak("weekly", "로그인이 필요해! 먼저 로그인하고 다시 와줘.");
      return;
    }
    try {
      setLoading(true);
      const res = await apiGet(`/users/${uid}/weekly-missions/this-week`);
      const data = unwrap<WeeklyMissionResponse>(res);
      setWeekly(data ?? null);
    } catch (e) {
      console.error(getErr(e));
      speak("weekly", "앗, 서버와 연결이 잘 안 돼... 잠시 후에 다시 시도해줘!");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void (async () => {
      await loadDaily();
      await loadWeekly();
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const displayDate = (daily as any)?.date?.trim?.()
    ? (daily as any).date
    : (daily as any)?.serverTime
      ? String((daily as any).serverTime).slice(0, 10)
      : new Date().toISOString().slice(0, 10);

  const claimDaily = async () => {
    if (!daily) return;
    if (!uid) {
      speak("daily", "로그인이 필요해! 먼저 로그인하고 다시 와줘.");
      return;
    }
    if (daily.claimed) {
      speak("daily", "이미 오늘의 선물은 받아갔어! 내일도 같이 달리자!");
      return;
    }
    if (!daily.completed) {
      speak("daily", "아직 에너지가 부족해... 오늘 목표를 채우면 선물을 줄 수 있어!");
      return;
    }

    try {
      setLoading(true);
      await apiPost(`/users/${uid}/daily-missions/today/claim`);
      to("mission", "confirm", { date: displayDate });
      await loadDaily();
      await loadWeekly();
      speak("daily", "짜잔! 오늘의 선물을 가져가 줘서 고마워!");
    } catch (e) {
      console.error(getErr(e));
      speak("daily", "앗, 뭔가 잘못됐어... 잠시 후 다시 눌러줘!");
    } finally {
      setLoading(false);
    }
  };

  const claimWeekly = async () => {
    if (!weekly) return;
    if (!uid) {
      speak("weekly", "로그인이 필요해! 먼저 로그인하고 다시 와줘.");
      return;
    }
    if (weekly.claimed) {
      speak("weekly", "이미 이번주 선물은 받아갔어! 다음 주도 기대할게!");
      return;
    }
    if (!weekly.completed) {
      speak("weekly", "이번 주 목표가 아직이야... 조금만 더 힘내보자!");
      return;
    }

    try {
      setLoading(true);
      await apiPost(`/users/${uid}/weekly-missions/this-week/claim`);
      await loadWeekly();
      speak("weekly", "우와! 이번 주도 완주했어! 선물 잘 챙겼지?");
    } catch (e) {
      console.error(getErr(e));
      speak("weekly", "앗, 서버가 살짝 쉬는 중인가봐... 잠시 후 다시 시도해줘!");
    } finally {
      setLoading(false);
    }
  };

  const footer =
    tab === "daily" ? (
      <CommonButton
        variant="outline"
        onClick={claimDaily}
        disabled={loading || !daily || !daily.completed}
      >
        보상 수령
      </CommonButton>
    ) : (
      <CommonButton
        variant="outline"
        onClick={claimWeekly}
        disabled={loading || !weekly || !weekly.completed}
      >
        주간 보상 수령
      </CommonButton>
    );

  const dailySpeech =
    hamsterOverride?.target === "daily" ? hamsterOverride.message : getDailyHamsterText(daily);

  const weeklySpeech =
    hamsterOverride?.target === "weekly" ? hamsterOverride.message : getWeeklyHamsterText(weekly);

  return (
    <Modal open onClose={onClose} title="미션" footer={footer}>
      {/* 탭 */}
      <div className="mb-4 flex gap-2">
        <button
          onClick={() => setTab("daily")}
          className={`flex-1 rounded-lg py-2 ${
            tab === "daily" ? "bg-slate-700 text-white" : "bg-slate-800 text-slate-300"
          }`}
        >
          일일
        </button>
        <button
          onClick={() => setTab("weekly")}
          className={`flex-1 rounded-lg py-2 ${
            tab === "weekly" ? "bg-slate-700 text-white" : "bg-slate-800 text-slate-300"
          }`}
        >
          주간
        </button>
      </div>

      {/* 컨텐츠 */}
      {tab === "daily" ? (
        !daily ? (
          <div className="flex flex-col items-center gap-3">
            <div className="text-xs text-slate-300">
              오늘 미션을 불러오지 못했어. 다시 시도할까?
            </div>
            <button
              className="rounded-md px-3 py-2 text-white"
              style={{ background: "#607d8b" }}
              onClick={loadDaily}
              disabled={loading}
            >
              불러오기
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-5">
            {/* 상단 목표 표시 */}
            <div className="text-center">
              <div className="mb-1 text-xs text-slate-300">오늘의 목표</div>
              <div className="text-3xl font-bold text-cyan-300">
                {fmt(daily.targetKm)}
                <span className="ml-1 text-base text-slate-200">km</span>
              </div>
            </div>

            {/* 배터리 */}
            <Battery progress={dailyFill} />

            {/* 햄스터 + 말풍선 */}
            <div className="mt-2 flex w-full items-center gap-3">
              <div className="flex h-16 w-16 items-center justify-center overflow-hidden rounded-xl bg-slate-900/60">
                {/* TODO: 실제 햄스터 이미지로 교체 */}
                <span style={{ fontSize: 32 }}>🐹</span>
              </div>
              <div
                className="flex-1 rounded-2xl px-3 py-2 text-sm leading-relaxed text-slate-50"
                style={{
                  background: "rgba(15,23,42,0.95)",
                  border: "1px solid rgba(148,231,255,0.6)",
                  boxShadow: "0 0 12px rgba(56,189,248,0.45)",
                }}
              >
                {dailySpeech}
              </div>
            </div>
          </div>
        )
      ) : !weekly ? (
        <div className="flex flex-col items-center gap-3">
          <div className="text-xs text-slate-300">
            이번 주 미션을 불러오지 못했어. 다시 시도해볼까?
          </div>
          <button
            className="rounded-md px-3 py-2 text-white"
            style={{ background: "#607d8b" }}
            onClick={loadWeekly}
            disabled={loading}
          >
            불러오기
          </button>
        </div>
      ) : (
        <div className="flex flex-col items-center gap-5">
          {/* 상단 주간 목표 표시 */}
          <div className="text-center">
            <div className="mb-1 text-xs text-slate-300">이번 주 목표</div>
            <div className="text-3xl font-bold text-cyan-300">
              {weekly.targetDays}
              <span className="ml-1 text-base text-slate-200">일</span>
            </div>
          </div>

          {/* 배터리 */}
          <Battery progress={weeklyFill} />

          {/* 햄스터 + 말풍선 */}
          <div className="mt-2 flex w-full items-center gap-3">
            <div className="flex h-16 w-16 items-center justify-center overflow-hidden rounded-xl bg-slate-900/60">
              {/* TODO: 실제 햄스터 이미지로 교체 */}
              <span style={{ fontSize: 32 }}>🐹</span>
            </div>
            <div
              className="flex-1 rounded-2xl px-3 py-2 text-sm leading-relaxed text-slate-50"
              style={{
                background: "rgba(15,23,42,0.95)",
                border: "1px solid rgba(148,231,255,0.6)",
                boxShadow: "0 0 12px rgba(56,189,248,0.45)",
              }}
            >
              {weeklySpeech}
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}
