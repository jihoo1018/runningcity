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
  fetchGetEntryDetail,
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
  } | null>({
    lat: 37.501280686148306,
    lng: 127.03960748829459,
  });

  // const BASE_URL = "/api/v1/entry";

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
      const data =
        mode === "today"
          ? await fetchGetEntryList()
          : await fetchGetAllEntryList();
      // ✅ userPosition이 있다면 즉시 거리 계산
      if (userPosition) {
        const updated = recalcDistances(
          data,
          userPosition.lat,
          userPosition.lng
        );
        setEntries(updated);
      } else {
        setEntries(data);
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

      // const res = await fetch(`${BASE_URL}/${baseId}`);
      // const json: ApiResponse<EntryDetail> = await res.json();
      // if (json.status !== 200) throw new Error(json.message);
      // setSelectedEntry(json.data);
      const data = await fetchGetEntryDetail(baseId);
      setSelectedEntry(data);
    } catch (e: any) {
      alert("상세 정보를 불러오지 못했습니다.");
    }
  };

  /** ✅ 상세 모달 → 난이도 모달 전환 */
  const handleShowLevelModal = (entry: EntryDetail) => {
    setSelectedEntry(null); // 기존 상세 닫기
    setLevelModalEntry(entry); // 새 난이도 모달 열기
  };

  /** ✅ 거리 계산 함수 (공통) */
  const recalcDistances = (
    entries: Record<string, Entry[]> | Entry[] | null,
    userLat: number,
    userLng: number
  ): Record<string, Entry[]> | Entry[] | null => {
    if (!entries) return null;

    const calcDistance = (entry: Entry) => {
      return calculateDistanceKm(
        userLat,
        userLng,
        entry.latitude,
        entry.longitude
      );
    };

    if (Array.isArray(entries)) {
      // today 모드
      const updated = entries.map((e) => ({
        ...e,
        computedDistanceKm: calcDistance(e),
      }));
      updated.sort(
        (a, b) => (a.computedDistanceKm ?? 0) - (b.computedDistanceKm ?? 0)
      );
      return updated;
    } else {
      // all 모드
      const grouped: Record<string, Entry[]> = {};
      Object.entries(entries).forEach(([groupNo, list]) => {
        grouped[groupNo] = list.map((e) => ({
          ...e,
          computedDistanceKm: calcDistance(e),
        }));
        grouped[groupNo].sort(
          (a, b) => (a.computedDistanceKm ?? 0) - (b.computedDistanceKm ?? 0)
        );
      });
      return grouped;
    }
  };

  /** ✅ 최초 진입 시 오늘의 기지 불러오기 */
  useEffect(() => {
    fetchEntries("today");
  }, []);

  /** ✅ userPosition 변경 시 거리 재계산 (서버 호출 X) */
  useEffect(() => {
    if (userPosition && entries) {
      console.log("📍 위치 변경 감지 → 거리 재계산:", userPosition);
      const updated = recalcDistances(
        entries,
        userPosition.lat,
        userPosition.lng
      );
      setEntries(updated);
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
        width: "100%",
        height: "100%",
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        backgroundColor: "#1D2330",
        color: "#E6FFFF",
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
            backgroundColor: viewMode === "today" ? "#00E6FF" : "#1D2330",
            color: viewMode === "today" ? "#1D2330" : "#94B8B8",
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
            backgroundColor: viewMode === "all" ? "#00E6FF" : "#1D2330",
            color: viewMode === "all" ? "#1D2330" : "#94B8B8",
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
                      backgroundColor: "none",
                      border: "1px solid #E6FFFF",
                      borderRadius: "8px",
                      marginBottom: "8px",
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
                              {entry.computedDistanceKm
                                ? `${entry.computedDistanceKm.toFixed(
                                    2
                                  )} km 거리`
                                : "거리 계산 중..."}
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
                      backgroundColor: "none",
                      border: "1px solid #E6FFFF",
                      borderRadius: "8px",
                      marginBottom: "8px",
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
