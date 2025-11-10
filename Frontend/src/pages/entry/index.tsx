import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { MapView, MapMarker } from "../../components/MapView";
import { EntryDetailModalContent } from "../../components/EntryDetailModalContent";
import { Modal } from "../../components/Modal";
import { LevelSelectModal } from "../../components/LevelSelectModal";
import {
  Entry,
  EntryDetail,
  GroupedEntryResponse,
  fetchGetEntryList,
  fetchGetAllEntryList,
} from "@/shared/api/entry";

export type ApiResponse<T> = {
  status: number;
  code: string;
  message: string;
  data: T;
  error?: any | null;
};

/** ✅ 위도·경도 간 거리 계산 (Haversine 공식) */
const calculateDistanceKm = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
) => {
  const R = 6371; // 지구 반경 (km)
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

const EntryPage = () => {
  const navigate = useNavigate();
  const [entries, setEntries] = useState<
    Record<string, Entry[]> | Entry[] | null
  >(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [viewMode, setViewMode] = useState<"today" | "all">("today");
  const [selectedEntry, setSelectedEntry] = useState<EntryDetail | null>(null);
  const [levelModalEntry, setLevelModalEntry] = useState<EntryDetail | null>(
    null
  ); // ✅ 난이도 모달 상태

  // ✅ 사용자 위치 상태
  const [userPosition, setUserPosition] = useState<{
    lat: number;
    lng: number;
  } | null>(null);

  const BASE_URL = "/api/v1/entry";

  /** ✅ 위치 수동 초기화 (한 번만) */
  useEffect(() => {
    if (!navigator.geolocation) return;

    console.log("📍 userPosition 업데이트:", userPosition);

    // ✅ 최초 1회: 현재 위치 가져오기
    navigator.geolocation.getCurrentPosition(
      (pos) =>
        setUserPosition({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        }),
      (err) => console.warn("초기 위치 불러오기 실패:", err),
      { enableHighAccuracy: true, timeout: 5000 }
    );

    // ✅ 이후 실시간 감시
    const watchId = navigator.geolocation.watchPosition(
      (pos) =>
        setUserPosition({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        }),
      (err) => console.warn("위치 추적 실패:", err),
      { enableHighAccuracy: true, maximumAge: 2000, timeout: 5000 }
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, []);

  /** ✅ Entry 리스트 불러오기 */
  const fetchEntries = async (mode: "today" | "all") => {
    setLoading(true);
    setError("");

    try {
      // ✅ userPosition이 없을 때 기본값 사용
      const { lat: userLat, lng: userLng } = userPosition || {
        lat: 37.5665,
        lng: 126.978,
      };

      const data =
        mode === "today"
          ? await fetchGetEntryList()
          : await fetchGetAllEntryList();

      // ✅ 위치 기준으로 거리 계산
      const calcDistance = (entry: Entry) =>
        calculateDistanceKm(userLat, userLng, entry.latitude, entry.longitude);

      if (mode === "today") {
        // TypeScript에게 data가 Entry[] 타입임을 확실히 알려주기
        const entries = data as Entry[];

        const entriesWithDistance: Entry[] = entries.map((entry) => ({
          ...entry,
          computedDistanceKm: calcDistance(entry),
        }));

        entriesWithDistance.sort(
          (a, b) => (a.computedDistanceKm ?? 0) - (b.computedDistanceKm ?? 0)
        );
        setEntries(entriesWithDistance);
      } else {
        // 여긴 GroupedEntryResponse 타입
        const grouped = data as GroupedEntryResponse;

        const updated: Record<string, Entry[]> = {};
        Object.entries(grouped).forEach(([groupNo, list]) => {
          updated[groupNo] = list.map((entry) => ({
            ...entry,
            computedDistanceKm: calcDistance(entry),
          }));
          updated[groupNo].sort(
            (a, b) => (a.computedDistanceKm ?? 0) - (b.computedDistanceKm ?? 0)
          );
        });
        setEntries(updated);
      }
    } catch (err: any) {
      console.error(err);
      setError(err.message || "데이터를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  /** ✅ 단일 상세 조회 */
  const fetchEntryDetail = async (baseId: number) => {
    try {
      // 🕓 위치 준비 대기: 최대 3초간 재시도
      let retry = 0;
      while (!userPosition && retry < 6) {
        console.log("📡 위치 준비 중... 대기", retry);
        await new Promise((resolve) => setTimeout(resolve, 500)); // 0.5초 간격
        retry++;
      }

      if (!userPosition) {
        alert("📍 현재 위치 정보를 가져올 수 없습니다. GPS를 활성화해주세요.");
        return;
      }

      const res = await fetch(`${BASE_URL}/${baseId}`);
      const json: ApiResponse<EntryDetail> = await res.json();
      if (json.status !== 200) throw new Error(json.message);

      setSelectedEntry(json.data);
    } catch (e: any) {
      alert("상세 정보를 불러오지 못했습니다.");
    }
  };

  /** ✅ 상세 모달 → 난이도 모달 전환 */
  const handleShowLevelModal = (entry: EntryDetail) => {
    setSelectedEntry(null); // 기존 상세 닫기
    setLevelModalEntry(entry); // 새 난이도 모달 열기
  };

  /** ✅ 최초 진입 시 오늘의 기지 불러오기 */
  useEffect(() => {
    fetchEntries("today");
  }, []);

  /** ✅ userPosition이 갱신되면 거리 자동 갱신 */
  useEffect(() => {
    if (userPosition) {
      console.log("📍 위치 변경 감지:", userPosition);
      fetchEntries(viewMode); // 현재 모드(today/all) 유지하면서 다시 계산
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userPosition]);

  /** ✅ 버튼 클릭 시 모드 변경 */
  const handleModeChange = (mode: "today" | "all") => {
    setViewMode(mode);
    fetchEntries(mode);
  };

  return (
    <div
      style={{
        width: "100vw",
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        backgroundColor: "#f9fafb",
        padding: "20px",
        boxSizing: "border-box",
      }}
    >
      {/* 모드 전환 버튼 */}
      <div
        style={{
          display: "flex",
          width: "100%",
          gap: "10px",
          justifyContent: "space-around",
          marginBottom: "16px",
        }}
      >
        <button
          onClick={() => handleModeChange("today")}
          style={{
            backgroundColor: viewMode === "today" ? "#3b82f6" : "#d4d4d4",
            color: viewMode === "today" ? "white" : "black",
            textAlign: "center",
            width: "40%",
            border: "1px solid",
            borderRadius: "6px",
            padding: "6px",
          }}
        >
          오늘 활성화 기지
        </button>
        <button
          onClick={() => handleModeChange("all")}
          style={{
            backgroundColor: viewMode === "all" ? "#3b82f6" : "#d4d4d4",
            color: viewMode === "all" ? "white" : "black",
            textAlign: "center",
            width: "40%",
            border: "1px solid",
            borderRadius: "6px",
            padding: "6px",
          }}
        >
          전체 기지 보기
        </button>
      </div>

      {/* 지도 표시 */}
      {error && <div style={{ color: "red" }}>❌ {error}</div>}
      {!loading && !error && entries && (
        <>
          <MapView
            markers={
              (entries instanceof Array
                ? entries
                : Object.values(entries).flat()
              ).map((e) => ({
                baseId: e.baseId,
                name: e.courseNm,
                latitude: e.latitude,
                longitude: e.longitude,
              })) as MapMarker[]
            }
            onMarkerClick={(baseId) => fetchEntryDetail(baseId)}
          />

          {/* 목록 */}
          <div
            style={{
              width: "100%",
              overflowY: "auto",
              maxHeight: "80vh",
            }}
          >
            {viewMode === "all" && !(entries instanceof Array)
              ? Object.entries(entries).map(([groupNo, list]) => (
                  <div
                    key={groupNo}
                    style={{
                      backgroundColor: "#fff",
                      borderRadius: "8px",
                      marginBottom: "12px",
                      padding: "10px",
                      boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                    }}
                  >
                    <h3 style={{ color: "#2563eb" }}>그룹 {groupNo}</h3>
                    <ul style={{ listStyle: "none", padding: 0 }}>
                      {list.map((entry) => (
                        <li
                          key={entry.baseId}
                          onClick={() => fetchEntryDetail(entry.baseId)}
                          style={{
                            display: "flex",
                            justifyContent: "space-between",
                            padding: "6px 0",
                            borderBottom: "1px solid #e5e7eb",
                            fontSize: "13px",
                            cursor: "pointer",
                          }}
                        >
                          <div>
                            <strong>{entry.courseNm}</strong>
                            <div style={{ color: "#6b7280" }}>
                              {entry.region}
                            </div>
                          </div>
                          <div style={{ textAlign: "right" }}>
                            <div>
                              {entry.computedDistanceKm?.toFixed(2)} km 거리
                            </div>
                          </div>
                        </li>
                      ))}
                    </ul>
                  </div>
                ))
              : (entries as Entry[]).map((entry) => (
                  <div
                    key={entry.baseId}
                    onClick={() => fetchEntryDetail(entry.baseId)}
                    style={{
                      backgroundColor: "#fff",
                      borderRadius: "8px",
                      marginBottom: "10px",
                      padding: "10px",
                      boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                      cursor: "pointer",
                    }}
                  >
                    <strong>{entry.courseNm}</strong>
                    <div style={{ color: "#6b7280", fontSize: "12px" }}>
                      {entry.region} · {entry.computedDistanceKm?.toFixed(2)} km
                      거리
                    </div>
                  </div>
                ))}
          </div>
        </>
      )}

      {/* ✅ 상세 모달 */}
      {selectedEntry && (
        <Modal
          title={selectedEntry.courseNm}
          onClose={() => setSelectedEntry(null)}
        >
          <EntryDetailModalContent
            entry={selectedEntry}
            userPosition={userPosition}
            onShowLevelModal={handleShowLevelModal}
          />
        </Modal>
      )}

      {/* ✅ 난이도 선택 모달 */}
      {levelModalEntry && (
        <LevelSelectModal
          entryName={levelModalEntry.courseNm}
          baseId={levelModalEntry.baseId}
          onClose={() => setLevelModalEntry(null)}
        />
      )}
    </div>
  );
};

export default EntryPage;
