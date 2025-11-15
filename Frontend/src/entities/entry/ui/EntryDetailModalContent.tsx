import { useEffect, useState } from "react";
import { MiniMap } from "@/entities/entry/ui/MiniMap";
import { EntryDetail } from "@/entities/entry/model/types";

/** 거리 계산 함수 (Haversine 공식) */
const calculateDistanceKm = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  const R = 6371; // 지구 반경 (km)
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

type EntryDetailModalContentProps = {
  entry: EntryDetail;
  userPosition: { lat: number; lng: number } | null;
  onShowLevelModal: (entry: EntryDetail) => void; // ✅ 부모에 모달 전환 요청
};

export const EntryDetailModalContent = ({
  entry,
  userPosition,
  onShowLevelModal,
}: EntryDetailModalContentProps) => {
  const [distance, setDistance] = useState<number | null>(null);

  useEffect(() => {
    if (!userPosition) {
      console.log("userPosition 없음");
      return;
    }

    console.log("✅ 위치 업데이트됨:", userPosition);

    const dist = calculateDistanceKm(
      userPosition.lat,
      userPosition.lng,
      entry.latitude,
      entry.longitude,
    );
    console.log("📏 계산된 거리:", dist);
    setDistance(dist);
  }, [userPosition, entry]);

  /** 2km 반경 내인지 판별 */
  const isInsideZone = distance !== null && distance <= 2;
  // TODO 테스트시에만 무조건 true로, 실제는 위 코드 써야함
  // const isInsideZone = true;

  /** ✅ 잠입 버튼 클릭 시 동작 */
  const showLevelModalHandler = () => {
    if (!isInsideZone) return;
    onShowLevelModal(entry); // ✅ 부모로 이벤트 전달
  };

  return (
    <div
      style={{
        fontSize: "12px",
        color: "#E6FFFF",
      }}
    >
      {/* 지도 표시 */}
      <MiniMap latitude={entry.latitude} longitude={entry.longitude} name={entry.courseNm} />

      {/* 상세 정보 */}
      <div style={{ marginTop: "12px" }}>
        <p>
          <strong>지역:</strong> {entry.region}
        </p>
        {entry.courseDesc && (
          <p>
            <strong>코스 설명:</strong> {entry.courseDesc}
          </p>
        )}
        {entry.difficulty && (
          <p>
            <strong>난이도:</strong> {entry.difficulty}
          </p>
        )}
        {entry.duration && (
          <p>
            <strong>소요 시간:</strong> {entry.duration}
          </p>
        )}
        {entry.address && (
          <p>
            <strong>주소:</strong> {entry.address}
          </p>
        )}
      </div>

      {/* 거리 및 잠입 버튼 */}
      <div style={{ textAlign: "center", marginTop: "16px" }}>
        {distance !== null ? (
          <p>
            현재 거리:{" "}
            <strong
              style={{
                color: isInsideZone ? "#10b981" : "#ef4444",
              }}
            >
              {distance.toFixed(2)} km
            </strong>{" "}
            {isInsideZone ? "✅ (범위 내)" : "❌ (범위 밖)"}
          </p>
        ) : (
          <p>📡 위치 정보 수신 중...</p>
        )}

        <button
          disabled={!isInsideZone}
          onClick={showLevelModalHandler}
          style={{
            width: "100%",
            padding: "10px",
            borderRadius: "8px",
            border: "none",
            color: isInsideZone ? "#1D2330" : "#94B8B8",
            backgroundColor: isInsideZone ? "#E6FFFF" : "none",
            fontWeight: "bold",
            marginTop: "10px",
            cursor: isInsideZone ? "pointer" : "not-allowed",
            transition: "0.2s ease-in-out",
          }}
        >
          {isInsideZone ? "개인 잠입 시작" : "2km 이내 접근 필요"}
        </button>
      </div>
    </div>
  );
};
