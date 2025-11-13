import { useEffect, useRef } from "react";

type MiniMapProps = {
  latitude: number;
  longitude: number;
  name: string;
};

export const MiniMap = ({ latitude, longitude, name }: MiniMapProps) => {
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const loadKakao = async () => {
      // SDK 로드 대기
      if (!window.kakao || !window.kakao.maps) {
        await new Promise<void>((resolve) => {
          const check = setInterval(() => {
            if (window.kakao && window.kakao.maps) {
              clearInterval(check);
              resolve();
            }
          }, 100);
        });
      }

      // autoload=false 대응
      if (!window.kakao.maps.LatLng) {
        await new Promise<void>((resolve) => {
          window.kakao.maps.load(() => resolve());
        });
      }

      if (!mapRef.current) return;

      // 지도 생성
      const center = new window.kakao.maps.LatLng(latitude, longitude);
      const map = new window.kakao.maps.Map(mapRef.current, {
        center,
        level: 6, // 더 가까이 보기
      });

      // 마커 생성
      const marker = new window.kakao.maps.Marker({
        position: center,
        map,
        title: name,
      });

      // 반경 2km 원 추가
      const circle = new window.kakao.maps.Circle({
        center,
        radius: 2000,
        strokeWeight: 2,
        strokeColor: "#3b82f6",
        strokeOpacity: 0.7,
        strokeStyle: "solid",
        fillColor: "#3b82f6",
        fillOpacity: 0.15,
      });
      circle.setMap(map);
    };

    loadKakao();
  }, [latitude, longitude, name]);

  return (
    <div
      ref={mapRef}
      style={{
        width: "100%",
        height: "250px",
        borderRadius: "8px",
        marginTop: "10px",
      }}
    />
  );
};
