// src/entities/entry/ui/EntryDetailModalContent.tsx
import { useEffect, useState } from "react";
import { MiniMap } from "@/entities/entry/ui/MiniMap";
import { EntryDetail } from "@/entities/entry/model/types";

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
  return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
};
// EntryDetailModalContent.tsx
type Props = {
  entry: EntryDetail;
  userPosition: { lat: number; lng: number } | null;
  onShowLevelModal: (entry: EntryDetail) => void;
  showStartButton?: boolean; // ← 버튼 노출 여부
};

export const EntryDetailModalContent = ({
  entry,
  userPosition,
  onShowLevelModal,
  showStartButton = true, // 기본은 true
}: Props) => {
  const [distance, setDistance] = useState<number | null>(null);

  useEffect(() => {
    if (!userPosition) return;
    const dist = calculateDistanceKm(
      userPosition.lat,
      userPosition.lng,
      entry.latitude,
      entry.longitude,
    );
    setDistance(dist);
  }, [userPosition, entry]);

  const isInsideZone = distance !== null && distance <= 2;

  return (
    <div className="max-h-[calc(100vh-200px)] overflow-hidden text-[12px] text-[#E6FFFF]">
      <MiniMap latitude={entry.latitude} longitude={entry.longitude} name={entry.courseNm} />

      <div className="mt-4 w-full">
  <table className="w-full text-left border-separate border-spacing-y-2">
    <tbody>

      {/* 지역 */}
      <tr>
        <th className="w-20 font-bold text-[#E6FFFF] align-top text-xs">지역</th>
        <td className="text-[12px] text-custom-gray">{entry.region}</td>
      </tr>

      {/* 코스 설명 */}
      {entry.courseDesc && (
        <tr>
          <th className="w-20 font-bold text-[#E6FFFF] align-top text-xs">코스 설명</th>
          <td className="text-[12px] text-custom-gray whitespace-pre-line">
            {entry.courseDesc}
          </td>
        </tr>
      )}

      {/* 난이도 */}
      {entry.difficulty && (
        <tr>
          <th className="w-20 font-bold text-[#E6FFFF] align-top text-xs">난이도</th>
          <td className="text-[12px] text-custom-gray">{entry.difficulty}</td>
        </tr>
      )}

      {/* 소요시간 */}
      {entry.duration && (
        <tr>
          <th className="w-20 font-bold text-[#E6FFFF] align-top text-xs">소요 시간</th>
          <td className="text-[12px] text-custom-gray">{entry.duration}</td>
        </tr>
      )}

      {/* 주소 */}
      {entry.address && (
        <tr>
          <th className="w-20 font-bold text-[#E6FFFF] align-top text-xs">주소</th>
          <td className="text-[12px] text-custom-gray">{entry.address}</td>
        </tr>
      )}
    </tbody>
  </table>
</div>


      <div className="text-center mt-4">
        {distance !== null ? (
          <p>
            현재 거리:
            <strong className={`ml-1 ${isInsideZone ? "text-green-400" : "text-red-400"}`}>
              {distance.toFixed(2)} km
            </strong>
            {isInsideZone ? " ✅ (범위 내)" : " ❌ (범위 밖)"}
          </p>
        ) : (
          <p>📡 위치 정보 수신 중...</p>
        )}

        {/* ⭐ 전체기지에서는 버튼 숨김 */}
        {showStartButton && (
          <button
            disabled={!isInsideZone}
            onClick={() => isInsideZone && onShowLevelModal(entry)}
            className={`
              w-full py-2 mt-3 rounded-md font-bold transition
              ${isInsideZone
                ? "bg-[#E6FFFF] text-[#1D2330] active:scale-95"
                : "bg-transparent text-[#94B8B8] cursor-not-allowed"}
            `}
          >
            {isInsideZone ? "개인 잠입 시작" : "2km 이내 접근 필요"}
          </button>
        )}
      </div>
    </div>
  );
};
