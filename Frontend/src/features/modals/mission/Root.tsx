// src/features/modals/mission/Root.tsx
import React, { useEffect, useMemo, useState } from "react";
import { Modal } from "@/shared/ui/Modal";
import { apiGet, apiPost } from "@/shared/api";
import { useModalRouter } from "@/app/modal/useModalRouter";
import type { ModalProps } from "@/app/modal/types";
import { useAuthStore } from "@/features/auth/model/useAuthStore";

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
  weekEnd: string;   // ISO
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

// 공통 언랩 유틸: axios 래퍼가 무엇을 리턴해도 안전하게 실제 payload만 뽑아온다.
function unwrap<T>(res: any): T {
  if (res == null) return res as T;
  // axios 전체 응답이 들어온 경우
  if ("data" in res && res.data != null) {
    const body = res.data;
    // 우리 공용 ApiResponse { code, message, data }
    if (typeof body === "object" && body && "data" in body && (body as any).data != null) {
      return (body as any).data as T;
    }
    // 어떤 프로젝트는 { result }로 감싸기도 함
    if (typeof body === "object" && body && "result" in body) {
      return (body as any).result as T;
    }
    return body as T;
  }
  // 이미 본문만 반환하는 래퍼일 수도 있음
  if (typeof res === "object" && res && "data" in res && (res as any).data == null) {
    // data 키가 있지만 null인 특이 케이스 방어
    return res as T;
  }
  // { code,message,data } 그대로 온 경우
  if (typeof res === "object" && res && "data" in res) {
    return (res as any).data as T;
  }
  // { result } 그대로 온 경우
  if (typeof res === "object" && res && "result" in res) {
    return (res as any).result as T;
  }
  return res as T;
}

export default function MissionRoot({ onClose }: ModalProps) {
  const { to } = useModalRouter();
  const uid = useAuthStore.getState().user?.userId;

  const [tab, setTab] = useState<Tab>("daily");
  const [daily, setDaily] = useState<DailyMissionResponse | null>(null);
  const [weekly, setWeekly] = useState<WeeklyMissionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<{ type: "error" | "success"; message: string } | null>(null);

  const showToast = (msg: string, type: "error" | "success" = "error") => {
    setToast({ message: msg, type });
    window.setTimeout(() => setToast(null), 2200);
  };

  const dailyProgressWidth = useMemo(() => `${daily?.progressPercent ?? 0}%`, [daily]);
  const weeklyProgressWidth = useMemo(() => `${weekly?.progressPercent ?? 0}%`, [weekly]);

  const loadDaily = async () => {
    if (!uid) return showToast("로그인이 필요합니다.");
    try {
      setLoading(true);
      const res = await apiGet(`/users/${uid}/daily-missions/today`);
      const data = unwrap<DailyMissionResponse>(res);
      setDaily(data ?? null);
    } catch (e) {
      showToast(getErr(e));
    } finally {
      setLoading(false);
    }
  };

  const loadWeekly = async () => {
    if (!uid) return;
    try {
      setLoading(true);
      const res = await apiGet(`/users/${uid}/weekly-missions/this-week`);
      const data = unwrap<WeeklyMissionResponse>(res);
      setWeekly(data ?? null);
    } catch (e) {
      showToast(getErr(e));
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

  const displayDate =
    (daily as any)?.date?.trim?.()
      ? (daily as any).date
      : (daily as any)?.serverTime
      ? String((daily as any).serverTime).slice(0, 10)
      : new Date().toISOString().slice(0, 10);

  const claimDaily = async () => {
    if (!daily) return;
    if (!uid) return showToast("로그인이 필요합니다.");
    if (daily.claimed) return showToast("수령 불가: 이미 보상을 받았습니다.");
    if (!daily.completed) return showToast("수령 불가: 미션을 먼저 완료하세요.");

    try {
      setLoading(true);
      await apiPost(`/users/${uid}/daily-missions/today/claim`);
      to("mission", "confirm", { date: displayDate });
      await loadDaily();
      await loadWeekly(); // 주간 집계 반영
    } catch (e) {
      showToast(`요청 실패: ${getErr(e)}`);
    } finally {
      setLoading(false);
    }
  };

  const claimWeekly = async () => {
    if (!weekly) return;
    if (!uid) return showToast("로그인이 필요합니다.");
    if (weekly.claimed) return showToast("이미 주간 보상을 수령했습니다.");
    if (!weekly.completed) return showToast("아직 주간 목표를 달성하지 못했습니다.");

    try {
      setLoading(true);
      await apiPost(`/users/${uid}/weekly-missions/this-week/claim`);
      showToast("주간 보상 수령 완료! 🏆", "success");
      await loadWeekly();
    } catch (e) {
      showToast(`요청 실패: ${getErr(e)}`);
    } finally {
      setLoading(false);
    }
  };

  const footer =
    tab === "daily" ? (
      <button
        className="px-4 py-3 rounded-xl font-semibold"
        style={{
          width: "100%",
          border: "1px solid rgba(79,232,255,0.6)",
          background: "transparent",
          color: "#E8F6FF",
          boxShadow: "0 0 0 1px rgba(79,232,255,0.35) inset",
        }}
        onClick={claimDaily}
        disabled={loading || !daily}
      >
        보상 수령
      </button>
    ) : (
      <button
        className="px-4 py-3 rounded-xl font-semibold"
        style={{
          width: "100%",
          border: "1px solid rgba(79,232,255,0.6)",
          background: "transparent",
          color: "#E8F6FF",
          boxShadow: "0 0 0 1px rgba(79,232,255,0.35) inset",
        }}
        onClick={claimWeekly}
        disabled={loading || !weekly}
      >
        주간 보상 수령
      </button>
    );

  return (
    <Modal open onClose={onClose} title="미션" footer={footer}>
      {/* 토스트 */}
      {toast && (
        <div
          style={{
            position: "absolute",
            top: 12,
            right: 12,
            padding: "6px 10px",
            borderRadius: 8,
            fontSize: 13,
            boxShadow: "0 4px 12px rgba(0,0,0,0.12)",
            background: toast.type === "error" ? "#ffebee" : "#e8f5e9",
            color: toast.type === "error" ? "#c62828" : "#2e7d32",
          }}
        >
          {toast.message}
        </div>
      )}

      {/* 탭 */}
      <div className="mb-4 flex gap-2">
        <button
          onClick={() => setTab("daily")}
          className={`flex-1 rounded-lg py-2 ${tab === "daily" ? "bg-slate-700 text-white" : "bg-slate-800 text-slate-300"}`}
        >
          일일
        </button>
        <button
          onClick={() => setTab("weekly")}
          className={`flex-1 rounded-lg py-2 ${tab === "weekly" ? "bg-slate-700 text-white" : "bg-slate-800 text-slate-300"}`}
        >
          주간
        </button>
      </div>

      {/* 컨텐츠 */}
      {tab === "daily" ? (
        !daily ? (
          <div className="flex justify-center">
            <button
              className="px-3 py-2 rounded-md text-white"
              style={{ background: "#607d8b" }}
              onClick={loadDaily}
              disabled={loading}
            >
              불러오기
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: 8,
                background: "rgba(255,255,255,0.03)",
                padding: 12,
                borderRadius: 12,
                fontSize: 14,
              }}
            >
              <div><b>날짜</b> {displayDate}</div>
              <div><b>목표 거리</b> {fmt(daily.targetKm)} km</div>
              <div><b>현재 거리</b> {fmt(daily.currentKm)} km</div>
              <div><b>진행률</b> {daily.progressPercent}%</div>
              <div>
                <b>상태</b> {daily.completed ? "완료" : "진행중"} / {daily.claimed ? "수령완료" : "미수령"}
              </div>
            </div>

            <div style={{ marginTop: 4 }}>
              <div
                style={{
                  width: "100%",
                  height: 14,
                  background: "rgba(255,255,255,0.06)",
                  borderRadius: 999,
                  overflow: "hidden",
                }}
              >
                <div
                  style={{
                    height: "100%",
                    background: "linear-gradient(90deg,#42a5f5,#26c6da)",
                    transition: "width 250ms ease",
                    width: dailyProgressWidth,
                  }}
                />
              </div>
            </div>
          </div>
        )
      ) : !weekly ? (
        <div className="flex justify-center">
          <button
            className="px-3 py-2 rounded-md text-white"
            style={{ background: "#607d8b" }}
            onClick={loadWeekly}
            disabled={loading}
          >
            불러오기
          </button>
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: 8,
              background: "rgba(255,255,255,0.03)",
              padding: 12,
              borderRadius: 12,
              fontSize: 14,
            }}
          >
            <div><b>주 시작</b> {weekly.weekStart.slice(0, 10)}</div>
            <div><b>주 종료</b> {weekly.weekEnd.slice(0, 10)}</div>
            <div><b>완료 일수</b> {weekly.completedDays} / {weekly.targetDays}</div>
            <div><b>진행률</b> {weekly.progressPercent}%</div>
            <div>
              <b>상태</b> {weekly.completed ? "목표 달성" : "진행중"} / {weekly.claimed ? "수령완료" : "미수령"}
            </div>
          </div>

          <div style={{ marginTop: 4 }}>
            <div
              style={{
                width: "100%",
                height: 14,
                background: "rgba(255,255,255,0.06)",
                borderRadius: 999,
                overflow: "hidden",
              }}
            >
              <div
                style={{
                  height: "100%",
                  background: "linear-gradient(90deg,#42a5f5,#26c6da)",
                  transition: "width 250ms ease",
                  width: weeklyProgressWidth,
                }}
              />
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}
