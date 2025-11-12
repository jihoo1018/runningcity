import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../../shared/api";

type ApiResponse<T> = {
  code: string;
  message: string;
  data: T;
};

type DailyMissionResponse = {
  id: number | null;
  date: string;
  serverTime: string;
  targetKm: number;
  currentKm: number;
  progressPercent: number;
  completed: boolean;
  claimed: boolean;
  // ✅ rewardCoins 제거
};

interface MissionModalProps {
  open: boolean;
  onClose: () => void;
  onClaimed?: () => void; // ✅ 인자 없이
  userId: number;
}

const fmt = (v: number) => (Number.isFinite(v) ? v.toFixed(2) : "0.00");
const getErr = (e: unknown) =>
  e instanceof Error ? e.message : typeof e === "string" ? e : JSON.stringify(e);

export default function MissionModal({
  open,
  onClose,
  onClaimed,
  userId,
}: MissionModalProps) {
  const [mission, setMission] = useState<DailyMissionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<{ type: "error" | "success"; message: string } | null>(null);

  const showToast = (msg: string, type: "error" | "success" = "error") => {
    setToast({ message: msg, type });
    setTimeout(() => setToast(null), 2500);
  };

  const progressWidth = useMemo(
    () => `${mission?.progressPercent ?? 0}%`,
    [mission]
  );

  const load = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await apiGet<ApiResponse<DailyMissionResponse>>(
        `/api/v1/users/${userId}/daily-missions/today`
      );
      setMission(res.data);
    } catch (e) {
      setError(getErr(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open) {
      void load();
    }
  }, [open]);

  const displayDate =
    mission?.date && mission.date.trim()
      ? mission.date
      : mission?.serverTime
      ? mission.serverTime.slice(0, 10)
      : new Date().toISOString().slice(0, 10);

  const claim = async () => {
    if (!mission) return;

    if (mission.claimed) {
      showToast("수령 불가능: 오늘은 이미 보상을 받았습니다.");
      return;
    }
    if (!mission.completed) {
      showToast("수령 불가능: 미션을 먼저 완료하세요.");
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await apiPost<ApiResponse<DailyMissionResponse>>(
        `/api/v1/users/${userId}/daily-missions/today/claim`
      );
      showToast("보상 수령! 🎉", "success");
      onClaimed?.();
      await load();
    } catch (e) {
      showToast(`요청 실패: ${getErr(e)}`);
      setError(getErr(e));
    } finally {
      setLoading(false);
    }
  };

  if (!open) return null;

  return (
    <div style={styles.backdrop} onClick={onClose}>
      <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div style={styles.header}>
          <h3 style={{ margin: 0 }}>일일 미션</h3>
          <button style={styles.close} onClick={onClose}>
            ✕
          </button>
        </div>

        {/* 토스트 */}
        {toast && (
          <div
            style={{
              ...styles.toast,
              background: toast.type === "error" ? "#ffebee" : "#e8f5e9",
              color: toast.type === "error" ? "#c62828" : "#2e7d32",
            }}
          >
            {toast.message}
          </div>
        )}

        {loading && <div style={styles.badge}>Loading...</div>}
        {error && (
          <div
            style={{
              ...styles.badge,
              background: "#ffebee",
              color: "#c62828",
            }}
          >
            Error: {error}
          </div>
        )}

        {!mission ? (
          <button style={styles.button} onClick={load} disabled={loading}>
            불러오기
          </button>
        ) : (
          <>
            <div style={styles.kv}>
              <div>
                <b>날짜</b> {displayDate}
              </div>
              <div>
                <b>목표 거리</b> {fmt(mission.targetKm)} km
              </div>
              <div>
                <b>현재 거리</b> {fmt(mission.currentKm)} km
              </div>
              <div>
                <b>진행률</b> {mission.progressPercent}%
              </div>
              <div>
                <b>상태</b>{" "}
                {mission.completed ? "완료" : "진행중"} /{" "}
                {mission.claimed ? "수령완료" : "미수령"}
              </div>
            </div>

            <div style={{ margin: "12px 0" }}>
              <div style={styles.track}>
                <div style={{ ...styles.fill, width: progressWidth }} />
              </div>
            </div>

            <div style={styles.actions}>
              <button
                style={{ ...styles.button, background: "#1e88e5" }}
                onClick={claim}
                disabled={loading}
              >
                보상 수령
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  backdrop: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.35)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 50,
  },
  modal: {
    position: "relative",
    width: 560,
    maxWidth: "92vw",
    background: "#fff",
    borderRadius: 16,
    boxShadow: "0 12px 30px rgba(0,0,0,0.18)",
    padding: 16,
  },
  header: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 8,
  },
  close: {
    background: "transparent",
    border: "none",
    fontSize: 18,
    cursor: "pointer",
  },
  kv: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: 8,
    background: "#fafafa",
    padding: 12,
    borderRadius: 12,
    fontSize: 14,
  },
  track: {
    width: "100%",
    height: 14,
    background: "#eceff1",
    borderRadius: 999,
    overflow: "hidden",
  },
  fill: {
    height: "100%",
    background: "linear-gradient(90deg,#42a5f5,#26c6da)",
    transition: "width 250ms ease",
  },
  actions: {
    display: "flex",
    gap: 8,
    marginTop: 10,
    flexWrap: "wrap",
  },
  button: {
    padding: "10px 14px",
    background: "#607d8b",
    color: "#fff",
    border: "none",
    borderRadius: 10,
    cursor: "pointer",
  },
  badge: {
    display: "inline-block",
    padding: "4px 8px",
    borderRadius: 8,
    background: "#e3f2fd",
    color: "#1565c0",
    marginBottom: 8,
  },
  toast: {
    position: "absolute",
    top: 12,
    right: 12,
    padding: "6px 10px",
    borderRadius: 8,
    fontSize: 13,
    boxShadow: "0 4px 12px rgba(0,0,0,0.12)",
  },
};
