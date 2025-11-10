// src/pages/entry/index.tsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { MapView, MapMarker } from "../../components/MapView"; // 카카오 지도
// import { MiniMap } from "../../components/MiniMap";
import { EntryDetailModalContent } from "../../components/EntryDetailModalContent";

import { Modal } from "../../components/Modal"; // 모달

type Entry = {
  baseId: number;
  courseNm: string;
  region: string;
  latitude: number;
  longitude: number;
  groupNo: number;
  computedDistanceKm?: number; // ✅ 프론트 계산용 거리(km)
};

type EntryDetail = {
  baseId: number;
  courseNm: string;
  region: string;
  latitude: number;
  longitude: number;
  groupNo: number;
  distanceKm?: number;
  difficulty?: string;
  duration?: string;
  courseDesc?: string;
  address?: string;
  dataSource?: string;
};

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

  const BASE_URL = "/api/v1/entry";

  /** ✅ API 호출 */
  const fetchEntries = async (mode: "today" | "all") => {
    setLoading(true);
    setError("");

    try {
      // 📍 현재 위치 가져오기
      const position = await new Promise<GeolocationPosition>(
        (resolve, reject) =>
          navigator.geolocation.getCurrentPosition(resolve, () => {
            // 실패 시 임시 좌표
            resolve({
              coords: {
                latitude: 37.5665,
                longitude: 126.978,
              },
            } as GeolocationPosition);
          })
      );
      const { latitude: userLat, longitude: userLng } = position.coords;

      // 📦 API 호출
      const endpoint =
        mode === "today" ? `${BASE_URL}/list/today` : `${BASE_URL}/list/all`;
      const res = await fetch(endpoint);
      const json: ApiResponse<any> = await res.json();

      if (json.status !== 200) throw new Error(json.message);

      // ✅ “오늘의 기지” (단일 리스트)
      if (mode === "today") {
        const entriesWithDistance: Entry[] = json.data.map((entry: Entry) => ({
          ...entry,
          computedDistanceKm: calculateDistanceKm(
            userLat,
            userLng,
            entry.latitude,
            entry.longitude
          ),
        }));

        // 거리순 정렬
        entriesWithDistance.sort(
          (a, b) => (a.computedDistanceKm ?? 0) - (b.computedDistanceKm ?? 0)
        );

        setEntries(entriesWithDistance);
      } else {
        // ✅ “전체 기지” (그룹별 구조)
        const groupedEntries: Record<string, Entry[]> = {};
        Object.entries(json.data).forEach(([groupNo, list]) => {
          groupedEntries[groupNo] = (list as Entry[]).map((entry) => ({
            ...entry,
            computedDistanceKm: calculateDistanceKm(
              userLat,
              userLng,
              entry.latitude,
              entry.longitude
            ),
          }));
          // 그룹 내부 거리순 정렬
          groupedEntries[groupNo].sort(
            (a, b) => (a.computedDistanceKm ?? 0) - (b.computedDistanceKm ?? 0)
          );
        });

        setEntries(groupedEntries);
      }
    } catch (err: any) {
      console.error(err);
      setError(err.message || "데이터를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  /** 단일 상세 조회 */
  const fetchEntryDetail = async (baseId: number) => {
    try {
      const res = await fetch(`${BASE_URL}/${baseId}`);
      const json: ApiResponse<EntryDetail> = await res.json();
      if (json.status !== 200) throw new Error(json.message);
      setSelectedEntry(json.data);
    } catch (e: any) {
      alert("상세 정보를 불러오지 못했습니다.");
    }
  };

  /** ✅ 최초 진입 시 오늘의 기지 불러오기 */
  useEffect(() => {
    fetchEntries("today");
  }, []);

  /** ✅ 버튼 클릭 시 모드 변경 */
  const handleModeChange = (mode: "today" | "all") => {
    setViewMode(mode);
    fetchEntries(mode);
  };

  /** ✅ 렌더링 */
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
      {/* 버튼 영역 */}
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
          전체 기지
        </button>
      </div>

      {/* 목록 영역 */}
      {loading && <div>⏳ 불러오는 중...</div>}
      {error && <div style={{ color: "red" }}>❌ {error}</div>}
      {!loading && !error && entries && (
        <>
          {/* ✅ 지도 표시 */}
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
            onMarkerClick={(baseId) => fetchEntryDetail(baseId)} // ✅ 클릭 시 상세 조회
          />

          {/* ✅ 목록 표시 */}
          <div
            style={{
              width: "100%",
              overflowY: "auto",
              maxHeight: "80vh",
            }}
          >
            {/* ✅ 전체 기지 (그룹별) */}
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
                          onClick={() => fetchEntryDetail(entry.baseId)} // 클릭 시 상세 API 호출
                          style={{
                            display: "flex",
                            justifyContent: "space-between",
                            padding: "6px 0",
                            borderBottom: "1px solid #e5e7eb",
                            fontSize: "13px",
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
              : // 오늘의 기지 (단일 리스트)
                (entries as Entry[]).map((entry) => (
                  <div
                    key={entry.baseId}
                    onClick={() => fetchEntryDetail(entry.baseId)} // 클릭 시 상세 API 호출
                    style={{
                      backgroundColor: "#fff",
                      borderRadius: "8px",
                      marginBottom: "10px",
                      padding: "10px",
                      boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
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

      {/* 모달 */}
      {selectedEntry && (
        <Modal
          title={selectedEntry.courseNm}
          onClose={() => setSelectedEntry(null)}
        >
          <EntryDetailModalContent entry={selectedEntry} />
        </Modal>
        // <Modal
        //   title={selectedEntry.courseNm}
        //   onClose={() => setSelectedEntry(null)}
        // >
        //   <div
        //     style={{ fontSize: "14px", lineHeight: "1.6", color: "#374151" }}
        //   >
        //     {Object.entries(selectedEntry).map(([key, value]) => (
        //       <div key={key} style={{ marginBottom: "6px" }}>
        //         <strong
        //           style={{ textTransform: "capitalize", color: "#2563eb" }}
        //         >
        //           {key}:
        //         </strong>{" "}
        //         <span>{value ?? "정보 없음"}</span>
        //       </div>
        //     ))}

        //     {/* ✅ 미니 지도 표시 */}
        //     <MiniMap
        //       latitude={selectedEntry.latitude}
        //       longitude={selectedEntry.longitude}
        //       name={selectedEntry.courseNm}
        //     />
        //   </div>
        // </Modal>
      )}
    </div>
  );
};

export default EntryPage;
