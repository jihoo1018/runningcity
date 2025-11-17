// src/pages/entry/index.tsx

import { useState, useEffect } from "react";
import { MapView } from "@/entities/entry/ui/MapView";
import { EntryDetailModalContent } from "@/entities/entry/ui/EntryDetailModalContent";
import { Modal } from "@/shared/ui";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import { Entry, EntryDetail } from "@/entities/entry/model/types";
import { fetchGetEntryList, fetchGetAllEntryList, fetchGetEntryDetail } from "@/entities/entry/api";

/** 거리 계산 */
const calculateDistanceKm = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;

  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
};

const EntryPage = () => {
  const { open } = useModalRouter();

  const [entries, setEntries] = useState<Record<string, Entry[]> | Entry[] | null>(null);
  const [viewMode, setViewMode] = useState<"today" | "all">("today");
  const [selectedEntry, setSelectedEntry] = useState<EntryDetail | null>(null);

  const [userPosition, setUserPosition] = useState({
    lat: 37.501280686148306,
    lng: 127.03960748829459,
  });

  /** 현재 위치 */
  useEffect(() => {
    navigator.geolocation?.getCurrentPosition(
      (pos) =>
        setUserPosition({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        }),
      () => {},
      { enableHighAccuracy: true },
    );
  }, []);

  const recalcDistances = (data: any, lat: number, lng: number) => {
    if (!data) return null;

    const calc = (e: Entry) => calculateDistanceKm(lat, lng, e.latitude, e.longitude);

    // 1) data가 배열인 경우
    if (Array.isArray(data)) {
      return data.map((e) => ({
        ...e,
        computedDistanceKm: calc(e),
      }));
    }

    // 2) 객체인 경우
    const cloned: Record<string, Entry[]> = {};

    Object.entries(data).forEach(([g, list]) => {
      if (!Array.isArray(list)) {
        console.warn("❌ list가 배열이 아님 → ", list);
        cloned[g] = [];
        return;
      }

      cloned[g] = list.map((e) => ({
        ...e,
        computedDistanceKm: calc(e),
      }));
    });

    return cloned;
  };

  const fetchEntries = async (mode: "today" | "all") => {
    const data = mode === "today" ? await fetchGetEntryList() : await fetchGetAllEntryList();

    if (userPosition) {
      setEntries(recalcDistances(data, userPosition.lat, userPosition.lng));
    } else setEntries(data);
  };

  useEffect(() => {
    fetchEntries("today");
  }, []);

  useEffect(() => {
    if (userPosition && entries) {
      setEntries(recalcDistances(entries, userPosition.lat, userPosition.lng));
    }
  }, [userPosition]);

  const fetchEntryDetail = async (baseId: number) => {
    const data = await fetchGetEntryDetail(baseId);
    setSelectedEntry(data);
  };

  /** 상세 → 잠입시작 모달 */
  const handleShowLevelModal = (entry: EntryDetail) => {
    setSelectedEntry(null);

    open("entry", "level", {
      entryName: entry.courseNm,
      baseId: entry.baseId,
      userId: useAuthStore.getState().user?.userId,
    });
  };

  return (
    <div className="bg-custom-black text-custom-white flex h-full w-full flex-col gap-6 overflow-hidden pt-4">
      {/* 탭 선택 */}
      <div className="flex w-full justify-between gap-3">
        <button
          onClick={() => {
            setViewMode("today");
            fetchEntries("today");
          }}
          className={`text-mg w-[50%] rounded-md border py-2.5 font-bold ${
            viewMode === "today"
              ? "bg-primary border-primary text-black"
              : "bg-custom-black text-custom-gray border-custom-gray"
          }`}
        >
          오늘 활성 기지
        </button>

        <button
          onClick={() => {
            setViewMode("all");
            fetchEntries("all");
          }}
          className={`text-mg w-[50%] rounded-md border py-2.5 font-bold ${
            viewMode === "all"
              ? "bg-primary border-primary text-black"
              : "bg-custom-black text-custom-gray border-custom-gray"
          }`}
        >
          전체 기지
        </button>
      </div>

      {/* 지도 */}
      <div className="flex h-[50vw] w-full justify-center">
        <MapView
          markers={
            entries
              ? (Array.isArray(entries) ? entries : Object.values(entries).flat()).map((e) => ({
                  baseId: e.baseId,
                  name: e.courseNm,
                  latitude: e.latitude,
                  longitude: e.longitude,
                }))
              : []
          }
          onMarkerClick={fetchEntryDetail}
        />
      </div>

      {/* 리스트 */}
      <div className="mt-2 flex-1 overflow-y-auto pb-3">
        {viewMode === "today" && (
          <h3 className="text-primary mb-3 text-xl font-semibold">도심 수색기지</h3>
        )}

        {/* 전체 기지 */}
        {viewMode === "all" &&
          entries &&
          !Array.isArray(entries) &&
          Object.entries(entries).map(([groupNo, list]) => (
            <div key={groupNo} className="border-custom-white mb-7 rounded-lg border p-3 shadow">
              <h3 className="text-primary mb-2 text-xl font-semibold">
                {["초계 정찰기지", "도심 수색기지", "중부 통제기지"][Number(groupNo)] ??
                  `기지 ${Number(groupNo) + 1}`}
              </h3>

              <ul className="divide-custom-gray divide-y">
                {list.map((e) => (
                  <li
                    key={e.baseId}
                    className="flex cursor-pointer justify-between py-3"
                    onClick={() => fetchEntryDetail(e.baseId)}
                  >
                    <div>
                      <strong>{e.courseNm}</strong>
                      <div className="text-custom-gray">{e.region}</div>
                    </div>
                    <div>{e.computedDistanceKm?.toFixed(2)} km</div>
                  </li>
                ))}
              </ul>
            </div>
          ))}

        {/* 오늘 활성 기지 (단일 리스트) */}
        {Array.isArray(entries) &&
          entries.map((e) => (
            <div
              key={e.baseId}
              className="border-custom-white mb-3 cursor-pointer rounded-lg border p-3 shadow"
              onClick={() => fetchEntryDetail(e.baseId)}
            >
              <strong>{e.courseNm}</strong>
              <div className="text-custom-gray text-xs">
                {e.region} · {e.computedDistanceKm?.toFixed(2)} km
              </div>
            </div>
          ))}
      </div>

      {/* 상세 모달 */}
      {selectedEntry && (
        <Modal
          open
          onClose={() => setSelectedEntry(null)}
          title={selectedEntry.courseNm}
          className="!max-h-[85vh]"
        >
          <EntryDetailModalContent
            entry={selectedEntry}
            userPosition={userPosition}
            onShowLevelModal={handleShowLevelModal}
            showStartButton={viewMode === "today"} // ⭐ 전체 기지면 false
          />
        </Modal>
      )}
    </div>
  );
};

export default EntryPage;
