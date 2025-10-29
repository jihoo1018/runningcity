import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";

export type MissionModalData = {
  id: number;
  date: string;
  serverTime: string;
  targetKm: number;
  currentKm: number;
  progressPercent: number;
  completed: boolean;
  claimed: boolean;
  rewardCoins: number;
};

const fmt = (v: number) => (Number.isFinite(v) ? v.toFixed(2) : "0.00");
const getErr = (e: unknown) =>
  e instanceof Error ? e.message : typeof e === "string" ? e : JSON.stringify(e);

export default function MissionModal({
  open,
  onClose,
  onClaimed,
}: {
  open: boolean;
  onClose: () => void;
  onClaimed?: (coins: number) => void;
}) {
  const [mission, setMission] = useState<MissionModalData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const progressWidth = useMemo(
    () => `${mission?.progressPercent ?? 0}%`,
    [mission]
  );

  const load = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiGet<MissionModalData>("/api/v1/daily-mission/modal");
      setMission(data);
    } catch (e) {
      setError(getErr(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open) load();
  }, [open]);

  const addKm = async (delta: number) => {
    if (!mission) return;
    try {
      setLoading(true);
      setError(null);
      await apiPost<void>(
        `/api/v1/daily-mission/${mission.id}/progress?addKm=${delta}`,
        undefined,
        "PATCH"
      ); // 본문/응답 없음 → void
      await load();
    } catch (e) {
      setError(getErr(e));
    } finally {
      setLoading(false);
    }
  };

  const claim = async () => {
    if (!mission) return;
    if (mission.claimed) return alert("오늘은 이미 보상을 받았습니다!");
    if (!mission.completed) return alert("미션을 먼저 완료하세요.");

    try {
      setLoading(true);
      setError(null);
      const data = await apiPost<{ rewardCoins: number; claimed: boolean }>(
        `/api/v1/daily-mission/${mission.id}/claim`
      ); // 이건 JSON 반환
      alert(`보상 ${data.rewardCoins ?? 0} 코인을 수령했습니다! 🎉`);
      onClaimed?.(data.rewardCoins ?? 0);
      await load();
    } catch (e) {
      alert(`요청 실패: ${getErr(e)}`);
      setError(getErr(e));
    } finally {
      setLoading(false);
    }
  };

  const reset = async () => {
    try {
      setLoading(true);
      setError(null);
      await apiPost<void>(`/api/v1/daily-mission/reset`); // 본문/응답 없음
      await load();
    } catch (e) {
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
          <button style={styles.close} onClick={onClose}>✕</button>
        </div>

        {loading && <div style={styles.badge}>Loading...</div>}
        {error && (
          <div style={{ ...styles.badge, background: "#ffebee", color: "#c62828" }}>
            Error: {error}
          </div>
        )}

        {!mission ? (
          <button style={styles.button} onClick={load}>불러오기</button>
        ) : (
          <>
            <div style={styles.kv}>
              <div><b>날짜</b> {mission.date}</div>
              <div><b>목표 거리</b> {fmt(mission.targetKm)} km</div>
              <div><b>현재 거리</b> {fmt(mission.currentKm)} km</div>
              <div><b>진행률</b> {mission.progressPercent}%</div>
              <div><b>상태</b> {mission.completed ? "완료" : "진행중"} / {mission.claimed ? "수령완료" : "미수령"}</div>
              <div><b>보상</b> {mission.rewardCoins} 코인</div>
            </div>

            <div style={{ margin: "12px 0" }}>
              <div style={styles.track}>
                <div style={{ ...styles.fill, width: progressWidth }} />
              </div>
            </div>

            <div style={styles.actions}>
              <button style={styles.button} onClick={() => addKm(0.5)} disabled={loading}>+0.5 km</button>
              <button style={styles.button} onClick={() => addKm(1)} disabled={loading}>+1.0 km</button>
              <button
                style={{ ...styles.button, background: "#1e88e5" }}
                onClick={claim}
                disabled={loading || !mission.completed || mission.claimed}
                title={!mission.completed ? "미션 완료 후 수령 가능" : (mission.claimed ? "이미 수령됨" : "수령 가능")}
              >
                보상 수령
              </button>
              <button style={{ ...styles.button, background: "#8e24aa" }} onClick={reset} disabled={loading}>
                초기화
              </button>
              <button style={{ ...styles.button, background: "#455a64" }} onClick={load} disabled={loading}>
                새로고침
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
    position: "fixed", inset: 0, background: "rgba(0,0,0,0.35)",
    display: "flex", alignItems: "center", justifyContent: "center", zIndex: 50,
  },
  modal: {
    width: 560, maxWidth: "92vw", background: "#fff", borderRadius: 16,
    boxShadow: "0 12px 30px rgba(0,0,0,0.18)", padding: 16,
  },
  header: { display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 8 },
  close: { background: "transparent", border: "none", fontSize: 18, cursor: "pointer" },
  kv: { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8, background: "#fafafa", padding: 12, borderRadius: 12, fontSize: 14 },
  track: { width: "100%", height: 14, background: "#eceff1", borderRadius: 999, overflow: "hidden" },
  fill: { height: "100%", background: "linear-gradient(90deg,#42a5f5,#26c6da)", transition: "width 250ms ease" },
  actions: { display: "flex", gap: 8, marginTop: 10, flexWrap: "wrap" },
  button: { padding: "10px 14px", background: "#607d8b", color: "#fff", border: "none", borderRadius: 10, cursor: "pointer" },
  badge: { display: "inline-block", padding: "4px 8px", borderRadius: 8, background: "#e3f2fd", color: "#1565c0", marginBottom: 8 },
};
