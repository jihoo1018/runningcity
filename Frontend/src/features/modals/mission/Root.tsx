// src/features/modals/mission/Root.tsx
import React, { useEffect, useMemo, useState } from "react";
import { Modal } from "@/shared/ui/Modal";
import { apiGet, apiPost } from "@/shared/api";
import { useModalRouter } from "@/app/modal/useModalRouter";
import type { ModalProps } from "@/app/modal/types";
import { useAuthStore } from "@/features/auth/model/useAuthStore"; 

type ApiResponse<T> = { code: string; message: string; data: T };

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

const fmt = (v: number) => (Number.isFinite(v) ? v.toFixed(2) : "0.00");
const getErr = (e: unknown) =>
  e instanceof Error ? e.message : typeof e === "string" ? e : JSON.stringify(e);

export default function MissionRoot({ onClose }: ModalProps) {
  const { to } = useModalRouter();

  const uid = useAuthStore.getState().user?.userId;

  const [mission, setMission] = useState<DailyMissionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<{ type: "error" | "success"; message: string } | null>(null);

  const showToast = (msg: string, type: "error" | "success" = "error") => {
    setToast({ message: msg, type });
    window.setTimeout(() => setToast(null), 2200);
  };

  const progressWidth = useMemo(
    () => `${mission?.progressPercent ?? 0}%`,
    [mission]
  );

  const load = async () => {
    try {
      if (!uid) {
        showToast("로그인이 필요합니다.");
        return;
      }
      setLoading(true);
      const res = await apiGet<ApiResponse<DailyMissionResponse>>(
        `/users/${uid}/daily-missions/today`
      );
      setMission(res.data);
    } catch (e) {
      showToast(getErr(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // 최초 오픈 시 1회 로드

  const displayDate =
    mission?.date?.trim()
      ? mission.date
      : mission?.serverTime
      ? mission.serverTime.slice(0, 10)
      : new Date().toISOString().slice(0, 10);

  const claim = async () => {
    if (!mission) return;
    if (!uid) return showToast("로그인이 필요합니다.");

    if (mission.claimed) return showToast("수령 불가: 이미 보상을 받았습니다.");
    if (!mission.completed) return showToast("수령 불가: 미션을 먼저 완료하세요.");

    try {
      setLoading(true);
      await apiPost<ApiResponse<DailyMissionResponse>>(
        `/users/${uid}/daily-missions/today/claim`
      );
      // 성공 시 확인 모달로 전환
      to("mission", "confirm", { date: displayDate });
      await load();
    } catch (e) {
      showToast(`요청 실패: ${getErr(e)}`);
    } finally {
      setLoading(false);
    }
  };

  const footer = (
    <button
      className="px-4 py-3 rounded-xl font-semibold"
      style={{
        width: "100%",
        border: "1px solid rgba(79,232,255,0.6)",
        background: "transparent",
        color: "#E8F6FF",
        boxShadow: "0 0 0 1px rgba(79,232,255,0.35) inset",
      }}
      onClick={claim}
      disabled={loading || !mission}
    >
      보상 수령
    </button>
  );

  return (
    <Modal open onClose={onClose} title="일일 미션" footer={footer}>
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

      {!mission ? (
        <div className="flex justify-center">
          <button
            className="px-3 py-2 rounded-md text-white"
            style={{ background: "#607d8b" }}
            onClick={load}
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
            <div><b>목표 거리</b> {fmt(mission.targetKm)} km</div>
            <div><b>현재 거리</b> {fmt(mission.currentKm)} km</div>
            <div><b>진행률</b> {mission.progressPercent}%</div>
            <div>
              <b>상태</b> {mission.completed ? "완료" : "진행중"} / {mission.claimed ? "수령완료" : "미수령"}
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
                  width: progressWidth,
                }}
              />
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}
