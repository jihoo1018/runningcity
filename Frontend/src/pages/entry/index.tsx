// src/pages/entry/index.tsx

import { useState, useEffect } from "react";
import { MapView } from "@/entities/entry/ui/MapView";
import { EntryDetailModalContent } from "@/entities/entry/ui/EntryDetailModalContent";
import { Modal } from "@/shared/ui";
import { useModalRouter } from "@/app/modal/useModalRouter";

import { Entry, EntryDetail } from "@/entities/entry/model/types";
import {
  fetchGetEntryList,
  fetchGetAllEntryList,
  fetchGetEntryDetail,
} from "@/entities/entry/api";

/** 거리 계산 */
const calculateDistanceKm = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) ** 2;

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
      { enableHighAccuracy: true }
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
    const data =
      mode === "today" ? await fetchGetEntryList() : await fetchGetAllEntryList();

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
      userId: 1,
    });
  };

  return (
    <div className="w-full flex flex-col bg-custom-black h-full text-custom-white overflow-hidden">
      {/* 탭 선택 */}
      <div className="px-1 pt-4 mb-3">
        <div className="flex w-full gap-3 justify-around">
          <button
            onClick={() => {
              setViewMode("today");
              fetchEntries("today");
            }}
            className={`w-2/5 border rounded-md py-2.5 
              text-mg font-bold
              ${
                viewMode === "today"
                  ? "bg-primary text-black border-primary"
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
            className={`w-2/5 border rounded-md py-2.5 
              text-mg font-bold
              ${
                viewMode === "all"
                  ? "bg-primary text-black border-primary"
                  : "bg-custom-black text-custom-gray border-custom-gray"
              }`}
          >
            전체 기지
          </button>
        </div>
      </div>


      {/* 지도 */}
      <div className="w-full h-[30vh] px-4 flex justify-center mt-2 mb-3">
        <MapView
          markers={
            entries
              ? (Array.isArray(entries) ? entries : Object.values(entries).flat()).map(
                  (e) => ({
                    baseId: e.baseId,
                    name: e.courseNm,
                    latitude: e.latitude,
                    longitude: e.longitude,
                  })
                )
              : []
          }
          onMarkerClick={fetchEntryDetail}
        />
      </div>

      {/* 리스트 */}
      <div className="flex-1 overflow-y-auto px-4 mt-2 pb-3">
        {viewMode === "today" && (
          <h3 className="text-xl font-semibold text-primary mb-3">
            도심 수색기지
          </h3>
        )}

        {/* 전체 기지 */}
        {viewMode === "all" &&
          entries &&
          !Array.isArray(entries) &&
          Object.entries(entries).map(([groupNo, list]) => (
            <div
              key={groupNo}
              className="border border-custom-white rounded-lg mb-7 p-3 shadow"
            >
              <h3 className="text-xl font-semibold text-primary mb-2">
                {
                  ["초계 정찰기지", "도심 수색기지", "중부 통제기지"][Number(groupNo)] ??
                  `기지 ${Number(groupNo) + 1}`
                }
              </h3>

              <ul>
                {list.map((e) => (
                  <li
                    key={e.baseId}
                    className="flex justify-between py-3 border-b border-custom-gray cursor-pointer"
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
              className="border border-custom-white rounded-lg mb-3 p-3 shadow cursor-pointer"
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
        <Modal open onClose={() => setSelectedEntry(null)} title={selectedEntry.courseNm} className="!max-h-[85vh]">
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
