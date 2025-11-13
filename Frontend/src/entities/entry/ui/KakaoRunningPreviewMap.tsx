import { useEffect, useRef } from "react";
import { loadKakaoMapsSDK } from "@/shared/lib/kakao/loadKakaoMapsSDK";
import { GpsPoint } from "@/entities/entry/model/types";

interface KakaoRunningPreviewMapProps {
  points: GpsPoint[]; // GPS 포인트 배열
  height?: number;
}

/**
 * 🏃‍♂️ KakaoRunningPreviewMap
 * 러닝 도중 또는 종료 직후, GPS 포인트 배열을 지도에 시각화
 */
export default function KakaoRunningPreviewMap({
  points,
  height = 200,
}: KakaoRunningPreviewMapProps) {
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!points || points.length < 2) return; // 좌표 최소 2개 이상 필요

    let canceled = false;

    (async () => {
      await loadKakaoMapsSDK();
      if (canceled || !mapRef.current) return;

      const kakao = (window as any).kakao;

      // 지도 생성 (첫 좌표 기준)
      const map = new kakao.maps.Map(mapRef.current, {
        center: new kakao.maps.LatLng(points[0].latitude, points[0].longitude),
        level: 5,
      });

      // 경로 라인 생성
      const linePath = points.map((p) => new kakao.maps.LatLng(p.latitude, p.longitude));
      const polyline = new kakao.maps.Polyline({
        path: linePath,
        strokeWeight: 7,
        // strokeColor: "#00E6FF",
        // strokeOpacity: 0.85,
        // strokeStyle: "solid",
      });
      polyline.setMap(map);

      // 지도 범위 자동 조정
      const bounds = new kakao.maps.LatLngBounds();
      linePath.forEach((pos) => bounds.extend(pos));
      map.setBounds(bounds);

      // 시작 / 종료 마커 표시
      // const startMarker = new kakao.maps.Marker({
      //   position: linePath[0],
      //   title: "출발 지점",
      // });
      // const endMarker = new kakao.maps.Marker({
      //   position: linePath[linePath.length - 1],
      //   title: "도착 지점",
      // });
      // startMarker.setMap(map);
      // endMarker.setMap(map);
    })();

    return () => {
      canceled = true;
    };
  }, [points]);

  return (
    <div
      ref={mapRef}
      style={{
        width: "100%",
        height,
        borderRadius: "8px",
        border: "1px solid #1B3240",
      }}
    />
  );
}
