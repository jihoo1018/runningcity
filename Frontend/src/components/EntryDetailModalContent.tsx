import { useEffect, useState } from "react";
import { MiniMap } from "./MiniMap";

type EntryDetail = {
  baseId: number;
  courseNm: string;
  region: string;
  latitude: number;
  longitude: number;
  difficulty?: string;
  duration?: string;
  courseDesc?: string;
  address?: string;
  dataSource?: string;
};

const calculateDistanceKm = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
) => {
  const R = 6371;
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

export const EntryDetailModalContent = ({ entry }: { entry: EntryDetail }) => {
  const [userPos, setUserPos] = useState<{ lat: number; lng: number } | null>(
    null
  );
  const [distance, setDistance] = useState<number | null>(null);

  // ✅ 실시간 위치 추적
  useEffect(() => {
    if (!navigator.geolocation) return;

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        setUserPos({ lat: latitude, lng: longitude });

        const dist = calculateDistanceKm(
          latitude,
          longitude,
          entry.latitude,
          entry.longitude
        );
        setDistance(dist);
      },
      (err) => {
        console.warn("위치 추적 실패:", err);
      },
      {
        enableHighAccuracy: true,
        maximumAge: 1000,
        timeout: 10000,
      }
    );

    // ✅ 컴포넌트 unmount 시 추적 중단
    return () => {
      navigator.geolocation.clearWatch(watchId);
    };
  }, [entry]);

  const isInsideZone = distance !== null && distance <= 2;

  return (
    <div style={{ fontSize: "14px", color: "#374151" }}>
      {/* 지도 */}
      <MiniMap
        latitude={entry.latitude}
        longitude={entry.longitude}
        name={entry.courseNm}
      />

      {/* 거리 표시 */}
      <div style={{ marginTop: "12px", textAlign: "center" }}>
        {distance !== null ? (
          <p>
            현재 거리:{" "}
            <strong style={{ color: isInsideZone ? "#10b981" : "#ef4444" }}>
              {distance.toFixed(2)} km
            </strong>{" "}
            {isInsideZone ? "✅ (범위 내)" : "❌ (범위 밖)"}
          </p>
        ) : (
          <p>📡 위치 정보 수신 중...</p>
        )}

        {/* ✅ 잠입 버튼 */}
        <button
          disabled={!isInsideZone}
          onClick={() => {
            if (isInsideZone) {
              // ✅ 1️⃣ Android WebView 환경일 경우 → 러닝 시작 명령 보내기
              if (window.Android?.startRunning) {
                window.Android.startRunning();
                console.log("🏃‍♀️ AndroidBridge.startRunning() 호출됨");
              } else {
                console.log("⚠️ AndroidBridge 미탑재. (웹환경)");
              }

              // ✅ 2️⃣ 웹 디버깅용 알림
              alert(`✅ '${entry.courseNm}' 잠입 개시!`);
            }
          }}
          style={{
            width: "100%",
            padding: "10px",
            marginTop: "8px",
            border: "none",
            borderRadius: "8px",
            backgroundColor: isInsideZone ? "#10b981" : "#9ca3af",
            color: "white",
            fontWeight: "bold",
            cursor: isInsideZone ? "pointer" : "not-allowed",
            transition: "background 0.3s",
          }}
        >
          잠입 {isInsideZone ? "시작" : "(범위 밖)"}
        </button>
      </div>
    </div>
  );
};
