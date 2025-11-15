import { useEffect, useRef } from "react";

export type MapMarker = {
  baseId: number;
  name: string;
  latitude: number;
  longitude: number;
};

type MapViewProps = {
  markers: MapMarker[];
  onMarkerClick?: (baseId: number) => void;
};

export const MapView = ({ markers, onMarkerClick }: MapViewProps) => {
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const waitForKakao = () =>
      new Promise<void>((resolve) => {
        if (window.kakao && window.kakao.maps) return resolve();
        const check = setInterval(() => {
          if (window.kakao && window.kakao.maps) {
            clearInterval(check);
            resolve();
          }
        }, 100);
      });

    const initMap = async () => {
      await waitForKakao();

      // autoload=false 대응
      if (!window.kakao.maps.LatLng) {
        await new Promise<void>((resolve) => {
          window.kakao.maps.load(() => resolve());
        });
      }

      if (!mapRef.current) return;

      // 지도 생성
      const map = new window.kakao.maps.Map(mapRef.current, {
        center: new window.kakao.maps.LatLng(37.5665, 126.978),
        level: 8,
      });

      // ✅ 각 entry 마다 마커 + 반경 2km 원 추가
      markers.forEach((m) => {
        const position = new window.kakao.maps.LatLng(m.latitude, m.longitude);

        // 마커 표시
        const marker = new window.kakao.maps.Marker({
          position,
          map,
          title: m.name,
        });

        // 클릭 이벤트 → 상세보기
        window.kakao.maps.event.addListener(marker, "click", () => onMarkerClick?.(m.baseId));

        // ✅ 반경 2km 원 추가
        const circle = new window.kakao.maps.Circle({
          center: position,
          radius: 2000, // 미터 단위 (2km)
          strokeWeight: 2,
          strokeColor: "#3b82f6", // 테두리 파란색
          strokeOpacity: 0.7,
          strokeStyle: "solid",
          fillColor: "#3b82f6",
          fillOpacity: 0.15, // 투명도
        });
        circle.setMap(map);
      });

      // 전체 마커 기준으로 지도 범위 맞추기
      if (markers.length > 0) {
        const bounds = new window.kakao.maps.LatLngBounds();
        markers.forEach((m) =>
          bounds.extend(new window.kakao.maps.LatLng(m.latitude, m.longitude)),
        );
        map.setBounds(bounds);
      }
    };

    initMap();
  }, [markers]);

  return (
    <div
      ref={mapRef}
      style={{
        width: "100%",
        height: "50vw",
        borderRadius: "10px",
        marginBottom: "20px",
        boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
      }}
    />
  );
};
